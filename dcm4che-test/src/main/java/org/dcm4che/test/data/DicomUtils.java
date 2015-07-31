//
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che.test.data;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;

/**
 * Helper methods for working with DICOM objects in tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class DicomUtils {

    /**
     * Read DICOM File Metadata from a file.
     *
     * @param dicomFile
     * @return attributes
     * @throws IOException
     */
    public static Attributes readMetaData(Path dicomFile) throws IOException
    {
        return read(new FileInputStream(dicomFile.toFile()), true);
    }

    /**
     * Read DICOM object from a file including all bulk data.
     * 
     * @param dicomFile
     * @return attributes
     * @throws IOException
     */
    public static Attributes read(Path dicomFile) throws IOException
    {
        return read(new FileInputStream(dicomFile.toFile()), false);
    }

    /**
     * Read DICOM object from a byte array including all bulk data.
     * 
     * @param binaryDicomObject
     * @return attributes
     * @throws IOException
     */
    public static Attributes read(byte[] binaryDicomObject, boolean metaOnly) throws IOException
    {
        ByteArrayInputStream binaryDicomObjectStream = new ByteArrayInputStream(binaryDicomObject);
        return read(binaryDicomObjectStream, metaOnly);
    }

    /**
     * Read DICOM object from input stream including all bulk data.
     * 
     * @param binaryDicomObjectStream
     * @return attributes
     * @throws IOException
     */
    public static Attributes read( InputStream binaryDicomObjectStream, boolean metaOnly) throws IOException
    {
        try(DicomInputStream dicomIn = new DicomInputStream( binaryDicomObjectStream ))
        {
            if (metaOnly)
                return dicomIn.readFileMetaInformation();
            else
                return dicomIn.readDataset(-1, -1);
        }
    }

    /**
     * Check that the given dataset contains all tags given by a reference
     * dataset and also their values are equal.
     * 
     * @param dataset
     *            dataset to check
     * @param referenceDataset
     *            reference dataset
     * @return true if all tags contained and values equal, false otherwise
     */
    public static boolean contains(Attributes dataset, Attributes referenceDataset) {

        Attributes selection = new Attributes(dataset, false, referenceDataset);

        return selection.equals(referenceDataset);
    }

    /**
     * Select datasets from a list (or Sequence) of datasets that contain all
     * tag values given by a reference dataset.
     * 
     * @param candidateDatasets
     *            list of candidate datasets
     * @param referenceDataset
     *            reference dataset
     * @return all datasets that contain all of the tag values of the reference
     *         dataset
     */
    public static List<Attributes> selectAttributesThatContain(List<Attributes> candidateDatasets, Attributes referenceDataset)
    {
        List<Attributes> matches = new ArrayList<>();
        for (Attributes candidate : candidateDatasets)
        {
            if (contains(candidate, referenceDataset))
                matches.add(candidate);
        }
        return matches;
    }

}
