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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.tool.dcmqrscp.CStoreSCPImpl;
import org.dcm4che3.util.AttributesFormat;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class BehavioralCStoreSCP {
    
    public static final class Builder {
        private ExternalDeviceToolConfig toolCfg;
        private final Map<String,ReturnState> instanceStatusMap = new HashMap<String, ReturnState>();
        
        private DimseRequestRecorder requestRecorder;
        
        public Builder qrSCPConfig(ExternalDeviceToolConfig toolCfg) {
            this.toolCfg = toolCfg;
            return this;
        }
        
        public Builder request(String iuid, boolean... success) {
            this.instanceStatusMap.put(iuid, new ReturnState(success));
            return this;
        }
        
        public Builder attachRequestRecorder(DimseRequestRecorder requestRecorder) {
            this.requestRecorder = requestRecorder;
            return this;
        }
        
        public InterceptableCStoreSCPImpl build() {
            InterceptableCStoreSCPImpl cStore = new InterceptableCStoreSCPImpl(toolCfg.getDicomDirWriter(), new AttributesFormat(toolCfg.getFilePathFormat()), toolCfg.getRecordFactory());
            if(requestRecorder != null) {
                cStore.addInterceptor(requestRecorder);
            }
            cStore.addInterceptor(new RequestInterceptor(instanceStatusMap));
            return cStore;
        }

    }
    
    public static class InterceptableCStoreSCPImpl extends CStoreSCPImpl {
        private List<DicomServiceInterceptor> interceptors = new ArrayList<DicomServiceInterceptor>();
        
        public InterceptableCStoreSCPImpl(DicomDirWriter dicomDirWriter,
                AttributesFormat filePathFormat, RecordFactory recordFactory) {
            super(dicomDirWriter, filePathFormat, recordFactory);
        }
        
        public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse,
                Attributes rq, PDVInputStream data) throws IOException {
            for(DicomServiceInterceptor interceptor : interceptors) {
                interceptor.beforeDimseRQ(as, pc, dimse, rq, null);
            }
            
            super.onDimseRQ(as, pc, dimse, rq, data);
        }
        
        private void addInterceptor(DicomServiceInterceptor interceptor) {
            interceptors.add(interceptor);
        }
        
    }
    
    private static class RequestInterceptor implements DicomServiceInterceptor {
        private final Map<String,ReturnState> instanceStatusMap;
        
        private RequestInterceptor(Map<String,ReturnState> instanceStatusMap) {
            this.instanceStatusMap = instanceStatusMap;
        }
       
        @Override
        public void beforeDimseRQ(Association as, PresentationContext pc, Dimse dimse,
                Attributes cmd, Attributes actionInfo) throws IOException {
            String sopInstanceUID = cmd.getString(Tag.AffectedSOPInstanceUID);
            ReturnState returnState = instanceStatusMap.get(sopInstanceUID);
            if(returnState != null && !returnState.state()) {
                throw new DicomServiceException(Status.ProcessingFailure, "Store failed");
            }
        }

    }
    
    private static final class ReturnState {
        private int call;
        private final boolean[] states;
        
        private ReturnState(boolean[] states) {
            this.states = states;
        }
        
        private boolean state() {
            boolean state;
            if(call > states.length - 1) {
                state = states[states.length -1];
            } else {
                state = states[call];
            }
            
            call++;
            return state;
        }
    }

}
