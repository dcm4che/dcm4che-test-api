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
import java.util.Map.Entry;

import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.dcmqrscp.DcmQRSCP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class ExternalDeviceTool implements TestTool {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalDeviceTool.class);
    
    private TestResult result;
    private ExternalDeviceDcmQRSCP qrscp;
    
    private final Builder builder;
  
    public static class Builder {
        private ExternalDeviceToolConfig cfg;
        private DicomService cStoreSCP;
        private DicomService stgCmtSCP;
        
        public Builder toolConfig(ExternalDeviceToolConfig cfg) {
            this.cfg = cfg;
            return this;
        }
        
        public Builder cStoreSCP(DicomService cStoreSCP) {
            this.cStoreSCP = cStoreSCP;
            return this;
        }
        
        public Builder stgCmtSCP(DicomService stgCmt) {
            this.stgCmtSCP = stgCmt;
            return this;
        }
        
        public ExternalDeviceTool build() throws IOException {
            return new ExternalDeviceTool(this);
        }
   
    }
    
    private ExternalDeviceTool(Builder builder) throws IOException {
        this.builder = builder;
        
        Device device = builder.cfg.getDevice();
        device.setExecutor(builder.cfg.getExecutor());
        device.setScheduledExecutor(builder.cfg.getScheduledExecutorService());
        ApplicationEntity ae = device.getApplicationEntity(builder.cfg.getAeTitle());
        
        qrscp = new ExternalDeviceDcmQRSCP();
        qrscp.setDevice(device);
        qrscp.setApplicationEntity(ae);
        qrscp.setDicomDirReader(builder.cfg.getDicomDirWriter());
        
        qrscp.init();
        
        qrscp.setFilePathFormat(builder.cfg.getFilePathFormat());
        qrscp.setRecordFactory(new RecordFactory());
        
        for(Entry<String,Connection> entry : builder.cfg.getRemoteConnections().entrySet()) {
            qrscp.addRemoteConnection(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
        return result;
    }
    
    public void start() {
        try {
            qrscp.getDevice().bindConnections();
        } catch(Exception e) {
            LOG.error("Error while binding connections of external device tool", e);
        }
    }
    
    public void stop() {
        try {
            qrscp.getDevice().waitForNoOpenConnections();
        } catch (InterruptedException e) {
            //ignore and go ahead and unbind
        }
        qrscp.getDevice().unbindConnections();
    }
    
    public ApplicationEntity getApplicationEntity() {
        return qrscp.getApplicationEntity();
    }
    
    private class ExternalDeviceDcmQRSCP extends DcmQRSCP<InstanceLocator> {
        
        public ExternalDeviceDcmQRSCP() throws IOException {
            super();
        }
        
        @Override
        protected void addCStoreSCPService(DicomServiceRegistry serviceRegistry) {
            if(builder.cStoreSCP != null)
                serviceRegistry.addDicomService(builder.cStoreSCP);
        }
        
        @Override
        protected void addStgCmtSCPService(DicomServiceRegistry serviceRegistry) {
            if(builder.stgCmtSCP != null)
                serviceRegistry.addDicomService(builder.stgCmtSCP);
        }
       
    }

}
