package org.dcm4che.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.dcm4che.test.annotations.RemoteConnectionParameters;
import org.dcm4che.test.common.BasicTest;
import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.tool.wadouri.test.WadoURIResponse;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.RetrieveSuppressionCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    public static void addCoercionTemplate(AttributeCoercion ac, ApplicationEntity ae, DicomConfiguration remoteConfig) {
        ArchiveAEExtension aeExt = ae.getAEExtension(ArchiveAEExtension.class);
        AttributeCoercions coercions = aeExt.getAttributeCoercions();
        coercions.add(ac);
        aeExt.setAttributeCoercions(coercions);
        Device arcDevice = ae.getDevice();
        try {
            remoteConfig.merge(arcDevice);
            remoteConfig.sync();
        } catch (ConfigurationException e) {
            LOG.error("Unable to merge and sync device after coercions change, {}", e);
        }
    }
    
    public static void removeCoercionTemplate(AttributeCoercion ac, ApplicationEntity ae, DicomConfiguration remoteConfig) {
        ArchiveAEExtension aeExt = ae.getAEExtension(ArchiveAEExtension.class);
        AttributeCoercions coercions = aeExt.getAttributeCoercions();
        for(Iterator<AttributeCoercion> iter = coercions.iterator(); iter.hasNext();) {
            if(ac.getCommonName().equalsIgnoreCase(iter.next().getCommonName()))
                iter.remove();
        }
        aeExt.setAttributeCoercions(coercions);
        Device arcDevice = ae.getDevice();
        try {
            remoteConfig.merge(arcDevice);
            remoteConfig.sync();
        } catch (ConfigurationException e) {
            LOG.error("Unable to merge and sync device after coercions change, {}", e);
        }

    }

    public static void addDBCustomAttribute(String archiveDeviceName, Entity
            entity, DicomConfiguration remoteConfig
            , int tagToAdd, VR vr, int sequenceTag) throws
            ConfigurationException {

        addDBCustomAttribute(archiveDeviceName, entity, remoteConfig, 1, null,
                tagToAdd, vr, sequenceTag);
    }

    public static void addDBPrivateCustomAttribute(String archiveDeviceName,
            Entity entity, DicomConfiguration remoteConfig, String privateCreator,
            int tagToAdd, VR vr, int sequenceTag) throws ConfigurationException {

        addDBCustomAttribute(archiveDeviceName, entity, remoteConfig, 1,
                privateCreator, tagToAdd, vr, sequenceTag);
    }

    public static void addDBCustomAttribute2(String archiveDeviceName, Entity
            entity, DicomConfiguration remoteConfig
            , int tagToAdd, VR vr, int sequenceTag) throws
            ConfigurationException {

        addDBCustomAttribute(archiveDeviceName, entity, remoteConfig, 2, null,
                tagToAdd, vr, sequenceTag);
    }

    public static void addDBPrivateCustomAttribute2(String archiveDeviceName,
            Entity entity, DicomConfiguration remoteConfig, String privateCreator,
            int tagToAdd, VR vr, int sequenceTag) throws ConfigurationException {

        addDBCustomAttribute(archiveDeviceName, entity, remoteConfig, 2,
                privateCreator, tagToAdd, vr, sequenceTag);
    }

    public static void addDBCustomAttribute3(String archiveDeviceName, Entity
            entity, DicomConfiguration remoteConfig
            , int tagToAdd, VR vr, int sequenceTag) throws
            ConfigurationException {

        addDBCustomAttribute(archiveDeviceName, entity, remoteConfig, 3, null,
                tagToAdd, vr, sequenceTag);
    }

    public static void addDBPrivateCustomAttribute3(String archiveDeviceName,
            Entity entity, DicomConfiguration remoteConfig, String privateCreator,
            int tagToAdd, VR vr, int sequenceTag) throws ConfigurationException {

        addDBCustomAttribute(archiveDeviceName, entity, remoteConfig, 3,
                privateCreator, tagToAdd, vr, sequenceTag);
    }

    private static void addDBCustomAttribute(String archiveDeviceName, Entity
            entity, DicomConfiguration remoteConfig,
           int attributeNumber, String privateCreator, int tagToAdd, VR vr,
           int sequenceTag)
            throws ConfigurationException {

        Device dev = remoteConfig.findDevice(archiveDeviceName);
        ArchiveDeviceExtension arcDevExt = dev
                .getDeviceExtension(ArchiveDeviceExtension.class);
        AttributeFilter filter = arcDevExt.getAttributeFilter(entity);

        if (attributeNumber==1)
            if(sequenceTag>0)
                filter.setCustomAttribute1(new ValueSelector(privateCreator,
                        tagToAdd, vr, 0, new ItemPointer(sequenceTag)));
            else
                filter.setCustomAttribute1(new ValueSelector(privateCreator,
                        tagToAdd, vr, 0));
        else if (attributeNumber==2)
            if(sequenceTag>0)
                filter.setCustomAttribute2(new ValueSelector(privateCreator,
                        tagToAdd, vr, 0, new ItemPointer(sequenceTag)));
            else
                filter.setCustomAttribute2(new ValueSelector(privateCreator,
                        tagToAdd, vr, 0));
        else if (attributeNumber==3)
            if(sequenceTag>0)
                filter.setCustomAttribute3(new ValueSelector(privateCreator,
                        tagToAdd, vr, 0, new ItemPointer(sequenceTag)));
            else
                filter.setCustomAttribute3(new ValueSelector(privateCreator,
                        tagToAdd, vr, 0));

        remoteConfig.merge(dev);
    }

    public static String reloadServerConfig(BasicTest test) throws MalformedURLException, IOException {
        Properties defaultParams = test.getProperties();
        //get remote connection parameters
        RemoteConnectionParameters remoteParams = 
                (RemoteConnectionParameters) test.getParams().get("RemoteConnectionParameters");
        String baseURL =  defaultParams.getProperty("remoteConn.url");

        String webContext = defaultParams.getProperty("remoteConn.webcontext");
        baseURL+=webContext.endsWith("/")?webContext:"/"+webContext;
        HttpURLConnection connection = (HttpURLConnection) 
                new URL(baseURL + (baseURL.endsWith("/")?"ctrl/reload":"/ctrl/reload"))
                .openConnection();
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("charset", "utf-8");
        String responseCode = connection.getResponseCode()+"";
        connection.disconnect();
        return responseCode;
    }


    public static void adjustRemoteConfigurationForDestinationSCP(String scpToAdjust, BasicTest test, String connectionToKeep) {
        DicomConfiguration remote = test.getRemoteConfig();
        Device scpDevice = null;
        try {
            scpDevice = remote.findDevice(scpToAdjust);
            if(scpDevice == null)
                throw new ConfigurationException();
        } catch (ConfigurationException e) {
            LOG.error("Unable to load scp device from remote configuration", e);
        }
        
        
        for (Iterator<Connection> iter = scpDevice.getConnections().iterator();iter.hasNext();) {
            Connection conn = iter.next();
            if(!conn.getCommonName().equalsIgnoreCase(connectionToKeep)) {
                for (ApplicationEntity ae : scpDevice.getApplicationEntities()) {
                    ae.removeConnection(conn);
                }
                iter.remove();
            }
        }

        try {
            remote.merge(scpDevice);
        } catch (ConfigurationException e) {
            LOG.error("Error merging device in the remote configuration", e);
        }
    }

    public static boolean containsSOPClassAndTransferSyntax(String sopClass,
            Collection<TransferCapability> collection, String transferSyntax) {
        for (TransferCapability tc : collection) {
            if (tc.getSopClass().equalsIgnoreCase(sopClass))
                if (transferSyntax == null)
                    return true;
                else
                    for (String ts : tc.getTransferSyntaxes())
                        if (ts.equalsIgnoreCase(transferSyntax))
                            return true;
        }
        return false;
    }

    public static Attributes getWadoURIResponseAttributes(WadoURIResponse resp) {
        File f = new File(resp.getRetrievedInstance());
        DicomInputStream din = null;
        try {
            din = new DicomInputStream(f);
            return din.readDataset(-1, Tag.PixelData);
        } catch (IOException e) {
            return null;
        }
        finally {
            try {
                din.close();
            } catch (IOException e) {
                LOG.error("Error closing dicom input stream , {}",e);
            }
        }
    }

    public static Device createPixConsumer(String deviceName, String hl7ApplicationName, String host, String port, String connectionName) {
        Device hl7Dev = new Device(deviceName);
        HL7DeviceExtension hl7Device = new HL7DeviceExtension();
        hl7Dev.addDeviceExtension(hl7Device);
        HL7Application app = new HL7Application(hl7ApplicationName);
        hl7Device.addHL7Application(app);
        Connection conn = new Connection(connectionName, host, Integer.parseInt(port));
        conn.setProtocol(Connection.Protocol.HL7);
        hl7Dev.addConnection(conn);
        app.addConnection(conn);
        return hl7Dev;
    }

    public static void removeArchiveSuppressionCriteria(String arcDeviceName, String arcAETitle, DicomConfiguration remoteConfig) throws ConfigurationException {
        Device arcDevice = remoteConfig.findDevice(arcDeviceName);
        ApplicationEntity ae = arcDevice.getApplicationEntity(arcAETitle);
        ArchiveAEExtension arcAEExt = ae.getAEExtension(ArchiveAEExtension.class);
        RetrieveSuppressionCriteria suppress = new RetrieveSuppressionCriteria();
        suppress.setCheckTransferCapabilities(false);
        arcAEExt.setRetrieveSuppressionCriteria(suppress);
        remoteConfig.merge(arcDevice);
    }
}
