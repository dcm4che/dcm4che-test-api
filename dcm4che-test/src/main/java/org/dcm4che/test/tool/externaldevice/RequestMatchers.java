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

package org.dcm4che.test.tool.externaldevice;

import static java.lang.String.format;

import java.util.List;

import org.dcm4che.test.tool.externaldevice.DimseRequestRecorder.DimseRequest;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class RequestMatchers {

    public static Matcher<List<? super DimseRequestRecorder.DimseRequest>> containsCStoreRequest(String sopInstanceUID, int numberOfExpectedRequests) {
        return new IsCStoreRequest(sopInstanceUID, numberOfExpectedRequests);
    }
    
    public static Matcher<List<? super DimseRequestRecorder.DimseRequest>> containsStgCmtRequest(String sopInstanceUID, int numberOfExpectedRequests) {
        return new IsStgCmtRequest(sopInstanceUID, numberOfExpectedRequests);
    }
    
    private static abstract class ContainsInstanceRequest extends TypeSafeDiagnosingMatcher<List<? super DimseRequestRecorder.DimseRequest>> {
        private final String sopInstanceUID;
        private final int numberOfExpectedRequests;
        
        private ContainsInstanceRequest(String sopInstanceUID, int numberOfExpectedRequests) {
            this.sopInstanceUID = sopInstanceUID;
            this.numberOfExpectedRequests = numberOfExpectedRequests;
        }
        
        @Override
        public void describeTo(Description description) {
            description.appendText(format("contains %d request for SOP instance with UID %s", numberOfExpectedRequests, sopInstanceUID));
        }

        @Override
        protected boolean matchesSafely(List<? super DimseRequest> requests, Description mismatchDescription) {
            int actualRequests = 0;
            for(Object o : requests) {
                if(!(o instanceof DimseRequest)) {
                    continue;
                }
                
                DimseRequest request = (DimseRequest)o;
                String[] sopInstanceUIDs = parseSopInstanceUID(request.getCmd(), request.getData());
                if(sopInstanceUIDs == null || sopInstanceUIDs.length != 1) {
                    continue;
                }
                
                if(sopInstanceUIDs[0].equals(this.sopInstanceUID)) {
                    actualRequests++;
                }
            }
            
            if(actualRequests == numberOfExpectedRequests) {
                return true;
            } else {
                mismatchDescription.appendText(format("does contain %d request for SOP instance with UID %s", actualRequests, sopInstanceUID));
                return false;
            }
        }
        
        protected abstract String[] parseSopInstanceUID(Attributes cmd, Attributes data);
          
    }
    
    private static class IsCStoreRequest extends ContainsInstanceRequest {
        
        private IsCStoreRequest(String sopInstanceUID, int numberOfExpectedRequests) {
            super(sopInstanceUID, numberOfExpectedRequests);
        }
       
        protected String[] parseSopInstanceUID(Attributes cmd, Attributes data) {
            return new String[] { cmd.getString(Tag.AffectedSOPInstanceUID) };
        }

    }
    
    private static class IsStgCmtRequest extends ContainsInstanceRequest {
      
        private IsStgCmtRequest(String sopInstanceUID, int numberOfExpectedRequests) {
            super(sopInstanceUID, numberOfExpectedRequests);
        }
        
        protected String[] parseSopInstanceUID(Attributes cmd, Attributes data) {
            Sequence requestSeq = data.getSequence(Tag.ReferencedSOPSequence);
            int size = requestSeq.size();
            String[] sopIUIDs = new String[size];
            for (int i = 0; i < sopIUIDs.length; i++) {
                Attributes item = requestSeq.get(i);
                sopIUIDs[i] = item.getString(Tag.ReferencedSOPInstanceUID);
            }
            
            return sopIUIDs;
        }

    }
    
}
