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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assertions on DICOM objects for tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class DicomAssert {

    private static final Logger log = LoggerFactory.getLogger(DicomAssert.class);

    /**
     * Check that the content of the given DICOM dataset is equal to the given
     * reference dataset.
     * 
     * @param dataset
     *            dataset
     * @param referenceDataset
     *            reference dataset
     */
    public static void assertEquals(Attributes dataset, Attributes referenceDataset)
    {
        boolean equal = dataset.equals(referenceDataset);

        if (!equal)
        {
            Attributes expected = referenceDataset.getRemovedOrModified(dataset);
            Attributes actual = dataset.getRemovedOrModified(referenceDataset);
            log.info("Expected: \n{}", expected);
            log.info("Actual: \n{}", actual);

            Assert.fail("The dicom objects are not equal");
        }
    }

    /**
     * Check that the content of the given DICOM file is equal to the given
     * reference DICOM file.
     * 
     * File meta information is NOT considered.
     * 
     * @param dicomFile
     *            DICOM file
     * @param dicomReferenceFile
     *            reference DICOM file
     * @throws IOException
     */
    public static void assertEquals(Path dicomFile, Path dicomReferenceFile) throws IOException
    {
        assertEquals(DicomUtils.read(dicomFile), DicomUtils.read(dicomReferenceFile));
    }

    /**
     * Check that dataset contains the given tag and the value is as expected.
     * 
     * @param dataset
     *            dataset
     * @param tag
     *            tag to check for
     * @param value
     *            value of tag to check for
     */
    public static void assertValueForTag(Attributes dataset, int tag, String value) {

        String tagKeyword = ElementDictionary.keywordOf(tag, null);
        Assert.assertTrue("Dataset should contain tag " + tagKeyword, dataset.contains(tag));
        Assert.assertEquals("Value of tag " + tagKeyword + " should be equal", value, dataset.getString(tag));
    }

    /**
     * Check that dataset contains the given tag and the value is not
     * empty/null.
     * 
     * @param dataset
     *            dataset
     * @param tag
     *            tag to check for
     */
    public static void assertValueContained(Attributes dataset, int tag) {

        String tagKeyword = ElementDictionary.keywordOf(tag, null);
        Assert.assertTrue("Dataset should contain a value for tag " + tagKeyword, dataset.containsValue(tag));
    }

    /**
     * Check that the given dataset contains all tag values given by a reference
     * dataset.
     * 
     * @param dataset
     *            dataset to check
     * @param referenceDataset
     *            reference dataset
     */
    public static void assertContains(Attributes dataset, Attributes referenceDataset)
    {
        boolean contains = DicomUtils.contains(dataset, referenceDataset);

        if (!contains)
        {
            Attributes selection = new Attributes(dataset, false, referenceDataset);
            Attributes expected = referenceDataset.getRemovedOrModified(selection);
            Attributes actual = new Attributes(selection, false, expected);
            log.info("Expected: \n{}", expected);
            log.info("Actual: \n{}", actual);

            Assert.fail("The dataset does not contain all tags of the test dataset or the values differ.");
        }
    }

    /**
     * Check that ALL of the given datasets contain all of the tag values given
     * by a reference dataset.
     * 
     * @param datasets
     *            datasets to check
     * @param referenceDataset
     *            reference dataset
     */
    public static void assertAllContain(List<Attributes> datasets, Attributes referenceDataset)
    {
        if (datasets.isEmpty())
            Assert.fail("No datasets");

        for (Attributes dataset : datasets)
        {
            assertContains(dataset, referenceDataset);
        }
    }

    /**
     * Check that EXACTLY ONE of the given datasets contains all of the tag
     * values given by a reference dataset.
     * 
     * This is useful for checking if a sequence contains a specific item.
     * 
     * @param datasets
     *            datasets to check
     * @param referenceDataset
     *            reference dataset
     * @return the one dataset that contained the reference tag values
     */
    public static Attributes assertOneContains(List<Attributes> datasets, Attributes referenceDataset)
    {
        List<Attributes> matches = DicomUtils.selectAttributesThatContain(datasets, referenceDataset);

        Assert.assertEquals("Expecting exactly one dataset matching " + referenceDataset.toString(), 1, matches.size());

        return matches.get(0);
    }

}
