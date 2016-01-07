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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.tool.common.FilesetInfo;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class ExternalDeviceToolConfig {

    private static final Logger log = LoggerFactory.getLogger(ExternalDeviceToolConfig.class);

    private final HashMap<String, Connection> remoteConnections = new HashMap<String, Connection>();
    private boolean stgCmtOnSameAssoc;
    private Executor executor = Executors.newCachedThreadPool();
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private DicomDirWriter dicomDirWriter;
    private String aeTitle;
    private Device device;
    private int port;
    private RecordFactory recordFactory = new RecordFactory();
    private String filePathFormat = "DICOM/{0020000D,hash}/{0020000E,hash}/{00080018,hash}";

    /**
     * Quick config to use in tests
     * @param newDevice
     * @param aet
     * @param hostname
     * @param port
     * @param remoteConfig
     * @throws ConfigurationException
     */
    public static void remoteConfigureSimpleExtArc(String newDevice, String aet, String hostname, int port, DicomConfigurationManager remoteConfig) throws ConfigurationException {

        log.debug("Create Connection dicom");
        Connection dicom = new Connection("dicom", hostname, port);
        dicom.setProtocol(Connection.Protocol.DICOM);
        dicom.setConnectionInstalled(true);
        dicom.setTlsProtocols(new String[] { "TLSv1", "SSLv3" });
        log.debug("Connection dicom:\n{}", dicom);

        log.debug("Create ApplicationEntity {}", hostname);
        ApplicationEntity applicationEntity = new ApplicationEntity(aet);
        applicationEntity.setAssociationAcceptor(true);
        applicationEntity.setAssociationInitiator(true);
        List<Connection> listConnections = new ArrayList<Connection>();
        listConnections.add(dicom);
        applicationEntity.setConnections(listConnections);
        EnumSet<TCGroupConfigAEExtension.DefaultGroup> scpGroups = EnumSet.of(TCGroupConfigAEExtension.DefaultGroup.QUERY, TCGroupConfigAEExtension.DefaultGroup.RETRIEVE, TCGroupConfigAEExtension.DefaultGroup.STORAGE,
                TCGroupConfigAEExtension.DefaultGroup.STORAGE_COMMITMENT);
        EnumSet<TCGroupConfigAEExtension.DefaultGroup> scuGroups = EnumSet.of(TCGroupConfigAEExtension.DefaultGroup.STORAGE, TCGroupConfigAEExtension.DefaultGroup.STORAGE_COMMITMENT);
        TCGroupConfigAEExtension tCGroupConfigAEExtension = new TCGroupConfigAEExtension(scpGroups, scuGroups);
        applicationEntity.addAEExtension(tCGroupConfigAEExtension);
        log.debug("ApplicationEntity {}:\n{}", newDevice.toUpperCase(), applicationEntity);

        log.debug("Create Device {}", newDevice);
        Device extDevice = new Device(newDevice);
        extDevice.addConnection(dicom);
        extDevice.addApplicationEntity(applicationEntity);
        log.debug("Device {}:\n{}", newDevice, extDevice);

        remoteConfig.merge(extDevice);
    }

    public ExternalDeviceToolConfig dicomDirWriter(DicomDirWriter dicomDirWriter) {
        this.dicomDirWriter = dicomDirWriter;
        return this;
    }
    
    public ExternalDeviceToolConfig dicomDir(File dicomDir) throws IOException {
        FilesetInfo fsInfo = new FilesetInfo();
        DicomDirWriter.createEmptyDirectory(dicomDir,
                UIDUtils.createUIDIfNull(fsInfo.getFilesetUID()),
                fsInfo.getFilesetID(), fsInfo.getDescriptorFile(),
                fsInfo.getDescriptorFileCharset());
        return dicomDirWriter(DicomDirWriter.open(dicomDir));
    }
    
    public ExternalDeviceToolConfig device(Device device) {
        this.device = device;
        return this;
    }
    
    public ExternalDeviceToolConfig addRemoteConnection(String name, Connection conn) {
        remoteConnections.put(name, conn);
        return this;
    }
    
    public ExternalDeviceToolConfig addRemoteConnection(String name, String hostname, int port) {
        Connection conn = new Connection();
        conn.setHostname(hostname);
        conn.setPort(port);
        
        addRemoteConnection(name, conn);
        return this;
    }
    
    public ExternalDeviceToolConfig stgCmtOnSameAssociation(boolean stgCmtOnSameAssoc) {
        this.stgCmtOnSameAssoc = stgCmtOnSameAssoc;
        return this;
    }
    
    public ExternalDeviceToolConfig executor(Executor executor) {
        this.executor = executor;
        return this;
    }
    
    public ExternalDeviceToolConfig scheduledExecutor(ScheduledExecutorService scheduledExecutor) {
        this.scheduledExecutor = scheduledExecutor;
        return this;
    }
    
    public ExternalDeviceToolConfig recordFactory(RecordFactory recordFactory) {
        this.recordFactory = recordFactory;
        return this;
    }
    
    public ExternalDeviceToolConfig filePathFormat(String filepathFormat) {
        this.filePathFormat = filepathFormat;
        return this;
    }
    
    public ExternalDeviceToolConfig aeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
        return this;
    }
    
    public ExternalDeviceToolConfig port(int port) {
        this.port = port;
        return this;
    }
    
    public HashMap<String, Connection> getRemoteConnections() {
        return remoteConnections;
    }

    public boolean isStgCmtOnSameAssoc() {
        return stgCmtOnSameAssoc;
    }

    public Executor getExecutor() {
        return executor;
    }
    
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutor;
    }

    public DicomDirWriter getDicomDirWriter() {
        return dicomDirWriter;
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public int getPort() {
        return port;
    }
    
    public Device getDevice() {
        return device;
    }

    public RecordFactory getRecordFactory() {
        return recordFactory;
    }

    public String getFilePathFormat() {
        return filePathFormat;
    }
    
    
  
}
