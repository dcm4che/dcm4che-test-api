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
import org.dcm4che3.data.Attributes.Visitor;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;

/**
 * Helper methods for working with DICOM objects in tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class DicomUtils {

    public enum IncludeFileMetaInformation {
        /**
         * Only read the DICOM dataset without file meta information.
         */
        DATASET_ONLY,

        /**
         * Only read the file meta information.
         */
        FILE_META_INFORMATION_ONLY,

        /**
         * Read both file meta information and the dataset, and merge the two.
         */
        DATASET_MERGED_WITH_FILE_META_INFORMATION
    }

    /**
     * Read DICOM object from a file including all bulk data and including file
     * meta information.
     * 
     * @param dicomFile
     * @return attributes
     * @throws IOException
     */
    public static Attributes read(Path dicomFile) throws IOException {
        return read(dicomFile, IncludeFileMetaInformation.DATASET_MERGED_WITH_FILE_META_INFORMATION, IncludeBulkData.YES);
    }

    /**
     * Read DICOM File meta information from a file.
     * 
     * Note: This is only a special area within the DICOM file which includes
     * the TransferSyntaxUID and other meta information, but NOT the normal
     * DICOM header.
     *
     * @param dicomFile
     * @return attributes
     * @throws IOException
     */
    public static Attributes readFileMetaInformation(Path dicomFile) throws IOException {
        return read(dicomFile, IncludeFileMetaInformation.FILE_META_INFORMATION_ONLY, IncludeBulkData.NO);
    }

    /**
     * Read DICOM object from a file.
     * 
     * @param dicomFile
     * @param includeFileMetaInformation
     *            specifies how to handle file meta information when reading the
     *            dataset
     * @param includeBulkData
     *            specifies whether and how to include bulk data
     * @return attributes
     * @throws IOException
     */
    public static Attributes read(Path dicomFile, IncludeFileMetaInformation includeFileMetaInformation, IncludeBulkData includeBulkData) throws IOException {
        return read(new FileInputStream(dicomFile.toFile()), includeFileMetaInformation, includeBulkData);
    }

    /**
     * Read DICOM object from a byte array including all bulk data.
     * 
     * @param binaryDicomObject
     * @param includeFileMetaInformation
     *            specifies how to handle file meta information when reading the
     *            dataset
     * @param includeBulkData
     *            specifies whether and how to include bulk data
     * @return attributes
     * @throws IOException
     */
    public static Attributes read(byte[] binaryDicomObject, IncludeFileMetaInformation includeFileMetaInformation, IncludeBulkData includeBulkData) throws IOException {
        ByteArrayInputStream binaryDicomObjectStream = new ByteArrayInputStream(binaryDicomObject);
        return read(binaryDicomObjectStream, includeFileMetaInformation, includeBulkData);
    }

    /**
     * Read DICOM object from input stream including all bulk data.
     * 
     * @param binaryDicomObjectStream
     * @param includeFileMetaInformation
     *            specifies how to handle file meta information when reading the
     *            dataset
     * @param includeBulkData
     *            specifies whether and how to include bulk data
     * @return attributes
     * @throws IOException
     */
    public static Attributes read(InputStream binaryDicomObjectStream, IncludeFileMetaInformation includeFileMetaInformation, IncludeBulkData includeBulkData) throws IOException {
        try (DicomInputStream dicomIn = new DicomInputStream(binaryDicomObjectStream)) {

            dicomIn.setIncludeBulkData(includeBulkData);

            Attributes fmi = dicomIn.readFileMetaInformation();
            if (includeFileMetaInformation == IncludeFileMetaInformation.FILE_META_INFORMATION_ONLY) {
                return fmi;
            } else {
                Attributes dataset = dicomIn.readDataset(-1, -1);

                if (includeFileMetaInformation == IncludeFileMetaInformation.DATASET_MERGED_WITH_FILE_META_INFORMATION) {
                    if (fmi != null) {
                        dataset.addAll(fmi);
                    }
                }

                return dataset;
            }
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

        Attributes filteredDataset = filterDatasetIgnoringSequenceItems(dataset, referenceDataset);

        return filteredDataset.equals(referenceDataset);
    }

    /**
     * Remove all tags from dataset that are not containing within the
     * selection.
     * 
     * Different to {@link Attributes#addSelected(Attributes, Attributes)} this
     * will consider sequence tags as a whole, i.e. not filter sequence items
     * and sub-sequences.
     * 
     * @param dataset
     * @param selection
     * @return filtered dataset
     */
    protected static Attributes filterDatasetIgnoringSequenceItems(Attributes dataset, Attributes selection) {
        Attributes selectionWithoutSequenceItems = new Attributes(selection);
        // clear all sequences, which will ensure that the filtering will not go into sequence items and sub-sequences
        try {
            selectionWithoutSequenceItems.accept(new Visitor() {
                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    if (value instanceof Sequence) {
                        ((Sequence) value).clear();
                    }
                    return true;
                }
            }, false);
        } catch (Exception e) {
            throw new RuntimeException(e); // should never happen
        }

        return new Attributes(dataset, false, selectionWithoutSequenceItems);
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
    public static List<Attributes> selectAttributesThatContain(List<Attributes> candidateDatasets, Attributes referenceDataset) {
        List<Attributes> matches = new ArrayList<>();
        for (Attributes candidate : candidateDatasets)
        {
            if (contains(candidate, referenceDataset))
                matches.add(candidate);
        }
        return matches;
    }

}
