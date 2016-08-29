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
import java.util.Arrays;
import java.util.List;

import org.dcm4che.test.data.DicomUtils.IncludeFileMetaInformation;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.TagUtils;
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
     * Note: to read a DICOM object from a file you can use the
     * {@link DicomUtils#read} methods.
     * 
     * @param dataset
     *            dataset
     * @param referenceDataset
     *            reference dataset
     */
    public static void assertEquals(Attributes dataset, Attributes referenceDataset)
    {
        assertEqualsIgnoringTags(dataset, referenceDataset);
    }

    /**
     * Check that the content of the given DICOM dataset is equal to the given
     * reference dataset ignoring some tags.
     * 
     * @param dataset
     *            dataset
     * @param referenceDataset
     *            reference dataset
     * @param tagsToIgnore
     *            tags to ignore (e.g. {@link Tag#ImplementationVersionName})
     */
    public static void assertEqualsIgnoringTags(Attributes dataset, Attributes referenceDataset, int... tagsToIgnore) {
    	assertEqualsIgnoringTags(dataset, referenceDataset, null, tagsToIgnore);
    }
    /**
     * Check that the content of the given DICOM dataset is equal to the given
     * reference dataset ignoring some tags.
     * 
     * @param dataset
     *            dataset
     * @param referenceDataset
     *            reference dataset
     * @param privateTagsToIgnore
     *            private tags to ignore
     * @param tagsToIgnore
     *            tags to ignore (e.g. {@link Tag#ImplementationVersionName})
     */
    public static void assertEqualsIgnoringTags(Attributes dataset, Attributes referenceDataset, PrivateTag[] privateTagsToIgnore, int... tagsToIgnore) {

    	int[] allTagsToIgnoreForDataset = getAllTagsToIgnore(tagsToIgnore, privateTagsToIgnore, dataset);
        if (allTagsToIgnoreForDataset != null && allTagsToIgnoreForDataset.length > 0) {
        	int[] allTagsToIgnoreForReferenceDataset = getAllTagsToIgnore(tagsToIgnore, privateTagsToIgnore, referenceDataset);
            Attributes filteredDataset = new Attributes(dataset.bigEndian(), dataset.size());
            Attributes filteredReferenceDataset = new Attributes(dataset.bigEndian(), dataset.size());
            filteredDataset.addNotSelected(dataset, allTagsToIgnoreForDataset);
            filteredReferenceDataset.addNotSelected(referenceDataset, allTagsToIgnoreForReferenceDataset);
            dataset = filteredDataset;
            referenceDataset = filteredReferenceDataset;
        }

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
    
    private static int[] getAllTagsToIgnore(int[] tagsToIgnore, PrivateTag[] privateTagsToIgnore, Attributes attrs) {
    	int[] toIgnore;
    	if (privateTagsToIgnore == null || privateTagsToIgnore.length == 0) {
    		toIgnore = tagsToIgnore;
    	} else {
	    	int privateTag, privateCreatorTag;
	    	int[] additionalToIgnore = new int[privateTagsToIgnore.length << 1];
	    	int idx = 0;
	    	for (PrivateTag privTag : privateTagsToIgnore) {
	    		privateTag = attrs.tagOf(privTag.creator, privTag.tag);
	    		additionalToIgnore[idx++] = privateTag;
	    		privateCreatorTag = TagUtils.creatorTagOf(privateTag);
	    		if (containsNot(additionalToIgnore, privateCreatorTag, idx)) {
	    			additionalToIgnore[idx++] = privateCreatorTag;
	    		}
	    	}
	    	if (tagsToIgnore != null && tagsToIgnore.length > 0) {
	    		toIgnore = new int[tagsToIgnore.length + idx];
	    		System.arraycopy(tagsToIgnore, 0, toIgnore, 0, tagsToIgnore.length);
	    		System.arraycopy(additionalToIgnore, 0, toIgnore, tagsToIgnore.length, idx);
	    	} else if (idx < additionalToIgnore.length) {
	    		toIgnore = Arrays.copyOf(additionalToIgnore, idx);
	    	} else {
	    		toIgnore = additionalToIgnore;
	    	}
    	}
    	Arrays.sort(toIgnore);
    	return toIgnore;
    }
    
    private static boolean containsNot(int[] ia, int value, int idx) {
    	for (int i=0; i < idx; i++) {
    		if (ia[i] == value)
    			return false;
    	}
    	return true;
    }

    /**
     * Check that the content of the given DICOM file is equal to the given
     * reference DICOM file.
     * 
     * This method also considers file meta information and bulk data. Use
     * {@link DicomUtils#read(Path,IncludeFileMetaInformation, IncludeBulkData)}
     * to read in the files yourself if you want to control whether file meta
     * information and bulk is considered.
     * 
     * @param dicomFile
     *            DICOM file
     * @param dicomReferenceFile
     *            reference DICOM file
     * @throws IOException
     */
    public static void assertEquals(Path dicomFile, Path dicomReferenceFile) throws IOException
    {
        assertEqualsIgnoringTags(dicomFile, dicomReferenceFile);
    }

    /**
     * Check that the content of the given DICOM file is equal to the given
     * reference DICOM file ignoring some tags.
     * 
     * This method also considers file meta information and bulk data. Use
     * {@link DicomUtils#read(Path,IncludeFileMetaInformation, IncludeBulkData)}
     * to read in the files yourself if you want to control whether file meta
     * information and bulk is considered.
     * 
     * @param dicomFile
     *            DICOM file
     * @param dicomReferenceFile
     *            reference DICOM file
     * @param tagsToIgnore
     *            tags to ignore (e.g. {@link Tag#ImplementationVersionName})
     * @throws IOException
     */
    public static void assertEqualsIgnoringTags(Path dicomFile, Path dicomReferenceFile, int... tagsToIgnore) throws IOException {
    	assertEqualsIgnoringTags(dicomFile, dicomReferenceFile, null, tagsToIgnore);
    }
    
    public static void assertEqualsIgnoringTags(Path dicomFile, Path dicomReferenceFile, PrivateTag[] privateTagsToIgnore, int... tagsToIgnore) throws IOException {
        assertEqualsIgnoringTags(DicomUtils.read(dicomFile), DicomUtils.read(dicomReferenceFile), tagsToIgnore);
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
        assertValueForTag(dataset,tag,value,ElementDictionary.getElementDictionary(null));
    }

    /**
     * Check that dataset contains the given private tag and the value is as expected.
     *
     * @param dataset
     *            dataset
     * @param tag
     *            tag to check for
     * @param value
     *            value of tag to check for
     * @param dictionary
     *            private dictionary
     */
    public static void assertValueForTag(Attributes dataset, int tag, String value,ElementDictionary dictionary) {
        String tagKeyword = appendParent(dataset, ElementDictionary.keywordOf(tag, dictionary.getPrivateCreator()));
        Assert.assertTrue("Dataset should contain tag " + tagKeyword, dataset.contains(dictionary.getPrivateCreator(),tag));
        Assert.assertEquals("Value of tag " + tagKeyword + " should be equal", value,
                dataset.getString(dictionary.getPrivateCreator(),tag,dictionary.vrOf(tag)));
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

        String tagKeyword = appendParent(dataset, ElementDictionary.keywordOf(tag, null));
        Assert.assertTrue("Dataset should contain a value for tag " + tagKeyword, dataset.containsValue(tag));
    }

    /**
     * Check that dataset contains the given tag and has exactly one code item with given code value and designator.
     * 
     * @param dataset
     *            dataset
     * @param tag
     *            tag to check for
     * @param codeValue
     *            expected CodeValue
     * @param designator
     *            expected CodingSchemeDesignator
     */
    public static void assertCode(Attributes dataset, int tag, String codeValue, String designator) {

        String tagKeyword = appendParent(dataset, ElementDictionary.keywordOf(tag, null));
        Assert.assertTrue("Dataset should contain a value for tag " + tagKeyword, dataset.containsValue(tag));
        Assert.assertTrue("Only one item schould be in the code sequence " + tagKeyword,dataset.getSequence(tag).size() == 1);
        Attributes item = dataset.getNestedDataset(tag);
        Assert.assertEquals("CodeValue of " + tagKeyword, codeValue, item.getString(Tag.CodeValue));
        Assert.assertEquals("CodingSchemeDesignator of " + tagKeyword, designator, item.getString(Tag.CodingSchemeDesignator));
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

        if (!contains) {
            Attributes filteredDataset = DicomUtils.filterDatasetIgnoringSequenceItems(dataset, referenceDataset);
            Attributes expected = referenceDataset.getRemovedOrModified(filteredDataset);
            Attributes actual = DicomUtils.filterDatasetIgnoringSequenceItems(filteredDataset, expected);
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
    
    private static String appendParent(Attributes dataset, String str) {
        if (dataset.getParent() != null) {
            return appendParent(dataset.getParent(), ElementDictionary.keywordOf(dataset.getParentSequenceTag(), dataset.getParentSequencePrivateCreator())+"/"+str);
        }
        return "/"+str;
    }

    public static class PrivateTag {
    	public final String creator;
    	public final int tag;
    	public PrivateTag(String privateCreator, int privateTag) {
    		if (!TagUtils.isPrivateGroup(privateTag)) {
    			throw new IllegalArgumentException("Not a private tag! tag:"+privateTag);
    		}
    		creator = privateCreator;
    		tag = privateTag;
    	}
    }
}
