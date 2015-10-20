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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.dcm4che.test.common.TestToolFactory;
import org.dcm4che.test.tool.externaldevice.BehavioralCStoreSCP.InterceptableCStoreSCPImpl;
import org.dcm4che.test.tool.externaldevice.BehavioralStgCmtSCP.BehavioralStgCmtSCPImpl;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.tool.movescu.test.MoveResult;
import org.dcm4che3.tool.movescu.test.MoveTool;
import org.dcm4che3.tool.storescp.test.StoreSCPTool;
import org.dcm4che3.tool.storescu.test.StoreResult;
import org.dcm4che3.tool.storescu.test.StoreTool;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class ExternalDeviceToolTest {
    private static final String extDeviceName = "dcmext";
    private static final String extDeviceAeTitle = "DCMEXT";
    
    private static final String storeScpDeviceName = "storescp";
    private static final String storeScpAeTitle = "STORESCP";
    
    private static final String storeScuDeviceName = "storescu";
    private static final String storeScuAeTitle = "STORESCU";
    
    private static final String moveScuDeviceName = "movescu";
    private static final String moveScuAeTitle = "MOVESCU";
    
    private static final String INSTANCE_FILE_NAME = "CTInstance1.dcm";
    private static final String STUDY_INSTANCE_UID = "1.2.840.113564.9.1.20080804154410.20101007123228.282585210";
    
    private ExternalDeviceTool runningExtDevice;

    @Test
    public void testCStoreOnExternalDevice() throws Exception {
        File extDeviceStorageDir = createTempDir("ExternalDeviceToolTest_testCStore");
        
        DicomConfiguration dicomCfg = getDicomConfig();
        ExternalDeviceToolConfig extDeviceToolCfg = TestToolFactory.createExternalDeviceToolConfig(dicomCfg, extDeviceStorageDir, extDeviceName, extDeviceAeTitle, storeScpDeviceName, storeScpAeTitle);
   
        BehavioralStgCmtSCPImpl stgCmtSCP = new BehavioralStgCmtSCP.Builder()
            .qrSCPConfig(extDeviceToolCfg)
            .build();
        
        InterceptableCStoreSCPImpl cStoreSCP = new BehavioralCStoreSCP.Builder()
            .qrSCPConfig(extDeviceToolCfg)
            .build();
    
        ExternalDeviceTool extDevice = new ExternalDeviceTool.Builder()
            .toolConfig(extDeviceToolCfg)
            .cStoreSCP(cStoreSCP)
            .stgCmtSCP(stgCmtSCP)
            .build();
        extDevice.start();
        runningExtDevice = extDevice;
        
        Device storeToolDevice = dicomCfg.findDevice(storeScuDeviceName);
        Connection storeToolConn = storeToolDevice.connectionWithEqualsRDN(new Connection("dicom", "0.0.0.0"));
        
        String userDir = System.getProperty("user.dir");
        File baseStoreToolDir = new File(userDir, "src/test/resources/testdata");
        StoreTool storeScuTool = new StoreTool(
                "localhost", 11122, 
                extDeviceAeTitle, baseStoreToolDir, storeToolDevice, 
                storeScuAeTitle, storeToolConn);
        storeScuTool.store("Store test instance to external device tool", INSTANCE_FILE_NAME);
        
        StoreResult storeResult = storeScuTool.getResult();
        Assert.assertEquals(1, storeResult.getFilesSent());
        Assert.assertEquals(0, storeResult.getFailures());
    }
    
    @Test
    public void testCMoveOnExternalDevice() throws Exception {
        File extDeviceStorageDir = createTempDir("ExternalDeviceToolTest_testCMove");
        
        DicomConfiguration dicomCfg = getDicomConfig();
        ExternalDeviceToolConfig extDeviceToolCfg = TestToolFactory.createExternalDeviceToolConfig(dicomCfg, extDeviceStorageDir, extDeviceName, extDeviceAeTitle, storeScpDeviceName, storeScpAeTitle);
   
        BehavioralStgCmtSCPImpl stgCmtSCP = new BehavioralStgCmtSCP.Builder()
            .qrSCPConfig(extDeviceToolCfg)
            .build();
        
        InterceptableCStoreSCPImpl cStoreSCP = new BehavioralCStoreSCP.Builder()
            .qrSCPConfig(extDeviceToolCfg)
            .build();
    
        ExternalDeviceTool extDevice = new ExternalDeviceTool.Builder()
            .toolConfig(extDeviceToolCfg)
            .cStoreSCP(cStoreSCP)
            .stgCmtSCP(stgCmtSCP)
            .build();
        extDevice.start();
        runningExtDevice = extDevice;
        
        Device storeToolDevice = dicomCfg.findDevice(storeScuDeviceName);
        Connection storeToolConn = storeToolDevice.connectionWithEqualsRDN(new Connection("dicom", "0.0.0.0"));
        
        String userDir = System.getProperty("user.dir");
        File baseStoreToolDir = new File(userDir, "src/test/resources/testdata");
        StoreTool storeScuTool = new StoreTool(
                "localhost", 11122, 
                extDeviceAeTitle, baseStoreToolDir, storeToolDevice, 
                storeScuAeTitle, storeToolConn);
        storeScuTool.store("Store test instance to external device tool", INSTANCE_FILE_NAME);
        
        StoreResult storeResult = storeScuTool.getResult();
        Assert.assertEquals(1, storeResult.getFilesSent());
        Assert.assertEquals(0, storeResult.getFailures());
        
        Device storeScpDevice = dicomCfg.findDevice(storeScpDeviceName);
        File baseStoreScpDir = new File(userDir, "target");
        Connection storeScpConn = storeScpDevice.connectionWithEqualsRDN(new Connection(storeScpAeTitle, "0.0.0.0"));
        StoreSCPTool storeScpTool = new StoreSCPTool(baseStoreScpDir, storeScpDevice, storeScpAeTitle, storeScpConn, false);
        storeScpTool.start("Starting store SCP to receive files from C-Move by external device tool");
        
        Device moveToolDevice = dicomCfg.findDevice(moveScuDeviceName);
        Connection moveToolConn = moveToolDevice.connectionWithEqualsRDN(new Connection("dicom", "0.0.0.0"));
        
        MoveTool moveTool = new MoveTool("localhost", 11122, extDeviceAeTitle, storeScpAeTitle, "STUDY", "STUDY", true, 
                moveToolDevice, moveScuAeTitle, moveToolConn);
   
        moveTool.addTag(Tag.StudyInstanceUID, STUDY_INSTANCE_UID);
        
        moveTool.move("Moving back stored instance from external device");
        MoveResult moveResult = (MoveResult)moveTool.getResult();
        
        Assert.assertEquals(1, moveResult.getNumResponses());
        Assert.assertEquals(1, moveResult.getNumSuccess());
        Assert.assertEquals(0, moveResult.getNumFail());
    }
    
    public void after() {
        if(runningExtDevice != null) {
            runningExtDevice.stop();
        }
    }
    
    private static File createTempDir(String name) throws IOException {
        File tmpDir = Files.createTempDirectory(name).toFile();
        tmpDir.deleteOnExit();
        return tmpDir;
    }
    
    private static DicomConfiguration getDicomConfig() throws IOException, ConfigurationException {
        File localConfigFile = Files.createTempFile("tempdefaultconfig", "json").toFile();
        
        Files.copy(ExternalDeviceToolTest.class.getClassLoader()
                .getResourceAsStream("externalDeviceToolTestConfig.json")
                , localConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        localConfigFile.deleteOnExit();
        return DicomConfigurationBuilder.newJsonConfigurationBuilder(localConfigFile.getPath()).build();
    }
    
}
