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
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.tool.dcmqrscp.StgCmtSCPImpl;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class BehavioralStgCmtSCP {
    
    public static final class Builder {
        private ExternalDeviceToolConfig toolCfg;

        private final Map<String,ReturnState> instanceStatusMap = new HashMap<String, ReturnState>();
        private boolean[] requestSuccessBehavior = new boolean[] { true };
        private DimseRequestRecorder requestRecorder;
        
        public Builder qrSCPConfig(ExternalDeviceToolConfig toolCfg) {
            this.toolCfg = toolCfg;
            return this;
        }
        
        public Builder request(boolean... success) {
            this.requestSuccessBehavior = success;
            return this;
        }
        
        public Builder returnInstanceState(String sopInstanceUID, int... states) {
            instanceStatusMap.put(sopInstanceUID, new ReturnState(states));
            return this;
        }
        
        public Builder attachRequestRecorder(DimseRequestRecorder requestRecorder) {
            this.requestRecorder = requestRecorder;
            return this;
        }
        
        public BehavioralStgCmtSCPImpl build() {
            BehavioralStgCmtSCPImpl stgCmtSCP = new BehavioralStgCmtSCPImpl(toolCfg.getDicomDirWriter(), 
                    toolCfg.getRemoteConnections(), toolCfg.isStgCmtOnSameAssoc(), toolCfg.getExecutor(),
                    instanceStatusMap);
            if(requestRecorder != null) {
                stgCmtSCP.addInterceptor(requestRecorder);
            }
            stgCmtSCP.addInterceptor(new RequestInterceptor(requestSuccessBehavior));
            return stgCmtSCP;
        }

    }
    
    private static class InterceptableStgCmtSCPImpl extends StgCmtSCPImpl {
        private List<DicomServiceInterceptor> interceptors = new ArrayList<DicomServiceInterceptor>();
       
        private InterceptableStgCmtSCPImpl(DicomDirReader dicomDirReader,
                Map<String, Connection> remoteConnections, boolean stgCmtOnSameAssoc,
                Executor executor) {
            super(dicomDirReader, remoteConnections, stgCmtOnSameAssoc, executor);
        }
        
        @Override
        protected void onDimseRQ(Association as, PresentationContext pc,
                Dimse dimse, Attributes cmd, Attributes data) throws IOException {
            for(DicomServiceInterceptor interceptor : interceptors) {
                interceptor.beforeDimseRQ(as, pc, dimse, cmd, data);
            }
            
            super.onDimseRQ(as, pc, dimse, cmd, data);
        }
        
        protected void addInterceptor(DicomServiceInterceptor interceptor) {
            interceptors.add(interceptor);
        }
     
    }
    
    public static class BehavioralStgCmtSCPImpl extends InterceptableStgCmtSCPImpl {

        // I own Alex a beer for making this public ;)
        public final Map<String,Integer> verifiedInstances = new HashMap<String, Integer>();
        private final Map<String,ReturnState> instanceStatusMap;
        
        
        private BehavioralStgCmtSCPImpl(DicomDirReader dicomDirReader,
                Map<String, Connection> remoteConnections, boolean stgCmtOnSameAssoc,
                Executor executor, Map<String,ReturnState> instanceStatusMap) {
            super(dicomDirReader, remoteConnections, stgCmtOnSameAssoc, executor);
            this.instanceStatusMap = instanceStatusMap;
        }
        
        @Override
        protected Map<String,Integer> calculateMatches(Map<String, String> requestMap) throws DicomServiceException {
            Map<String, Integer> localInstanceStatusMap = super.calculateMatches(requestMap);
            
            for(Entry<String,ReturnState> entry : instanceStatusMap.entrySet()) {
                String iuid = entry.getKey();
                if(requestMap.containsKey(iuid)) {
                    localInstanceStatusMap.put(iuid, entry.getValue().state());
                }
            }
            
            synchronized(this) {
                verifiedInstances.putAll(localInstanceStatusMap);
                notifyAll();
            }
            
            return localInstanceStatusMap;
        }

        
        public void waitForStgCmtResponse(long timeout, int resultStatus, String... sopInstanceUIDs) throws InterruptedException {
            long leftTimeout = timeout;
            boolean failed = true;
            
            synchronized (this) {
                while (failed) {
                    failed = false;
                    for (String sopInstanceUID : sopInstanceUIDs) {
                        Integer status = verifiedInstances.get(sopInstanceUID);
                        if (status == null || status != resultStatus) {
                            if(timeout <= 0) {
                                wait();
                            } else {
                                if(leftTimeout <= 0) {
                                    throw new InterruptedException("Timeout passed: Not all stg-cmt responses sent!");
                                }
                                
                                long now = System.currentTimeMillis();
                                wait(leftTimeout);
                                leftTimeout -= System.currentTimeMillis() - now;
                            }
                            
                            failed = true;
                            break;
                        }
                    }
                    
                }
            }
            
        }
        
    }
    
    private static class RequestInterceptor implements DicomServiceInterceptor {
        private final boolean[] requestSuccessBehavior;
        private int request;
        
        private RequestInterceptor(boolean[] requestSuccessBehavior) {
            this.requestSuccessBehavior = requestSuccessBehavior;
        }
       
        @Override
        public void beforeDimseRQ(Association as, PresentationContext pc, Dimse dimse,
                Attributes cmd, Attributes actionInfo) throws IOException {
            boolean success = request < requestSuccessBehavior.length - 1 ? requestSuccessBehavior[request] : 
                requestSuccessBehavior[requestSuccessBehavior.length - 1];
            request++;
            if(!success) {
                throw new DicomServiceException(Status.ProcessingFailure, "Storage commitment request failed");
            }
            
        }
        
    }
    
    private static final class ReturnState {
        private int call;
        private final int[] states;
        
        private ReturnState(int[] states) {
            this.states = states;
        }
        
        private int state() {
            int state;
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
