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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link DicomAssert} class.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class DicomAssertTest {

    @Test
    public void testAssertEquals() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes reference = new Attributes();
        reference.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        reference.setNull(Tag.PatientName, VR.PN);
        reference.setString(Tag.PatientID, VR.LO, "PatientID");
        reference.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        DicomAssert.assertEquals(dataset, reference);
    }

    @Test
    public void testAssertNotEquals() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes reference = new Attributes();
        reference.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        reference.setNull(Tag.PatientName, VR.PN);
        reference.setString(Tag.PatientID, VR.LO, "PatientID2");
        reference.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        try {
            DicomAssert.assertEquals(dataset, reference);
        } catch (AssertionError expected) {
            return; // expected
        }

        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertNotEquals2() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes reference = new Attributes();
        reference.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        reference.setString(Tag.PatientID, VR.LO, "PatientID");
        reference.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        try {
            DicomAssert.assertEquals(dataset, reference);
        } catch (AssertionError expected) {
            return; // expected
        }

        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertEqualsIgnoringTags() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes reference = new Attributes();
        reference.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        reference.setNull(Tag.PatientName, VR.PN);
        reference.setString(Tag.PatientID, VR.LO, "PatientID2"); // different (ignored)
        reference.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        DicomAssert.assertEqualsIgnoringTags(dataset, reference, Tag.PatientID);
    }

    @Test
    public void testAssertNotEqualsIgnoringTags() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes reference = new Attributes();
        reference.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        reference.setNull(Tag.PatientName, VR.PN);
        reference.setString(Tag.PatientID, VR.LO, "PatientID2"); // different (ignored)
        reference.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID2"); // different (NOT ignored)

        try {
            DicomAssert.assertEqualsIgnoringTags(dataset, reference, Tag.PatientID);
        } catch (AssertionError expected) {
            return; // expected
        }

        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertContains() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID");

        DicomAssert.assertContains(dataset, referenceDataset);
    }

    @Test
    public void testAssertNotContains1() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID2");

        try {
            DicomAssert.assertContains(dataset, referenceDataset);
        } catch (AssertionError expected) {
            return; // expected
        }

        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertNotContains2() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        referenceDataset.setString(Tag.PatientName, VR.PN, "value");

        try {
            DicomAssert.assertContains(dataset, referenceDataset);
        } catch (AssertionError expected) {
            return; // expected
        }

        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertContains_Sequence() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        Sequence requestAttributesSequence = dataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID");
        Sequence referenceRequestAttributesSequence = referenceDataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes referenceRqAttrs1 = new Attributes();
        referenceRqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        referenceRqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes referenceRqAttrs2 = new Attributes();
        referenceRqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        referenceRqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        referenceRequestAttributesSequence.add(referenceRqAttrs1);
        referenceRequestAttributesSequence.add(referenceRqAttrs2);

        DicomAssert.assertContains(dataset, referenceDataset);
    }

    @Test
    public void testAssertNotContains_Sequence() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        Sequence requestAttributesSequence = dataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID");
        Sequence referenceRequestAttributesSequence = referenceDataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes referenceRqAttrs1 = new Attributes();
        referenceRqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        referenceRqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes referenceRqAttrs2 = new Attributes();
        referenceRqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        referenceRqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        // note the changed order:
        referenceRequestAttributesSequence.add(referenceRqAttrs2);
        referenceRequestAttributesSequence.add(referenceRqAttrs1);

        try {
        DicomAssert.assertContains(dataset, referenceDataset);
        } catch (AssertionError expected) {
            return; // expected
        }
        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertNotContains_Sequence2() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        Sequence requestAttributesSequence = dataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID");
        Sequence referenceRequestAttributesSequence = referenceDataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes referenceRqAttrs1 = new Attributes();
        referenceRqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        referenceRqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        referenceRequestAttributesSequence.add(referenceRqAttrs1);
        // second item missing
        // note: sequences are always compared as a whole, therefore the missing item leads to an AssertionError

        try {
            DicomAssert.assertContains(dataset, referenceDataset);
        } catch (AssertionError expected) {
            return; // expected
        }
        Assert.fail("Expecting exception");
    }

    @Test
    public void testAssertNotContains_Sequence3() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        Sequence requestAttributesSequence = dataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID");
        Sequence referenceRequestAttributesSequence = referenceDataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes referenceRqAttrs1 = new Attributes();
        referenceRqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        referenceRqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes referenceRqAttrs2 = new Attributes();
        referenceRqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        referenceRqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        Attributes referenceRqAttrs3 = new Attributes();
        referenceRqAttrs3.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID3");
        referenceRqAttrs3.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID3");
        referenceRequestAttributesSequence.add(referenceRqAttrs1);
        referenceRequestAttributesSequence.add(referenceRqAttrs2);
        referenceRequestAttributesSequence.add(referenceRqAttrs3); // additional item

        try {
            DicomAssert.assertContains(dataset, referenceDataset);
        } catch (AssertionError expected) {
            return; // expected
        }
        Assert.fail("Expecting exception");
    }
    
    @Test
    public void testAssertNotContains_Sequence4() throws Exception {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        dataset.setString(Tag.PatientID, VR.LO, "PatientID");
        dataset.setString(Tag.IssuerOfPatientID, VR.LO, "IssuerOfPatientID");
        Sequence requestAttributesSequence = dataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes rqAttrs1 = new Attributes();
        rqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        rqAttrs1.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID1");
        Attributes rqAttrs2 = new Attributes();
        rqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        rqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        requestAttributesSequence.add(rqAttrs1);
        requestAttributesSequence.add(rqAttrs2);

        Attributes referenceDataset = new Attributes();
        referenceDataset.setString(Tag.AccessionNumber, VR.SH, "AccessionNumber");
        dataset.setNull(Tag.PatientName, VR.PN);
        referenceDataset.setString(Tag.PatientID, VR.LO, "PatientID");
        Sequence referenceRequestAttributesSequence = referenceDataset.newSequence(Tag.RequestAttributesSequence, 2);
        Attributes referenceRqAttrs1 = new Attributes();
        referenceRqAttrs1.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID1");
        // missing
        Attributes referenceRqAttrs2 = new Attributes();
        referenceRqAttrs2.setString(Tag.RequestedProcedureID, VR.LO, "RequestedProcedureID2");
        referenceRqAttrs2.setString(Tag.ScheduledProcedureStepID, VR.LO, "ScheduledProcedureStepID2");
        referenceRequestAttributesSequence.add(referenceRqAttrs1);
        referenceRequestAttributesSequence.add(referenceRqAttrs2);
        // note: sequences are always compared as a whole, therefore the missing tag within an item leads to an AssertionError

        try {
            DicomAssert.assertContains(dataset, referenceDataset);
        } catch (AssertionError expected) {
            return; // expected
        }
        Assert.fail("Expecting exception");
    }

}
