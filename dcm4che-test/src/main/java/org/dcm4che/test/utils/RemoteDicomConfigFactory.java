package org.dcm4che.test.utils;

import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.util.ConfigNodeUtil;
import org.dcm4che3.conf.core.util.PathPattern;
import org.dcm4che3.conf.dicom.CommonDicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.conf.dicom.DicomPath;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchiveHL7ApplicationExtension;
import org.dcm4chee.archive.conf.NoneIOCMChangeRequestorExtension;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Roman K
 */
public class RemoteDicomConfigFactory {


    public static DicomConfigurationManager createRemoteDicomConfiguration(String remoteEndpointURL) {

        RemoteConfiguration remoteConfiguration = new RemoteConfiguration(remoteEndpointURL);
        try {
            DicomConfigurationBuilder builder = new DicomConfigurationBuilder().
                    registerCustomConfigurationStorage(remoteConfiguration);

            builder.registerDeviceExtension(HL7DeviceExtension.class);
            builder.registerDeviceExtension(AuditLogger.class);
            builder.registerDeviceExtension(AuditRecordRepository.class);
            builder.registerDeviceExtension(ImageReaderExtension.class);
            builder.registerDeviceExtension(ImageWriterExtension.class);

            builder.registerDeviceExtension(ArchiveDeviceExtension.class);
            builder.registerDeviceExtension(StorageDeviceExtension.class);
            builder.registerDeviceExtension(NoneIOCMChangeRequestorExtension.class);
            builder.registerAEExtension(ArchiveAEExtension.class);
            builder.registerHL7ApplicationExtension(ArchiveHL7ApplicationExtension.class);
            builder.registerAEExtension(WebServiceAEExtension.class);
            builder.registerAEExtension(ExternalArchiveAEExtension.class);
            builder.registerAEExtension(TCGroupConfigAEExtension.class);

            return builder.build();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Can't initialize remote configuration", e);
        }
    }

    public static class RemoteConfiguration implements Configuration {

        @Path("/config")
        private static interface RESTDicomConfigAPI {

            @GET
            @Path("/device/{deviceName}")
            @Produces(MediaType.APPLICATION_JSON)
            Map<String, Object> getDeviceConfig(@PathParam(value = "deviceName") String deviceName) throws ConfigurationException;

            @DELETE
            @Path("/device/{deviceName}")
            @Produces(MediaType.APPLICATION_JSON)
            void removeDevice(@PathParam(value = "deviceName") String deviceName) throws ConfigurationException;

            @POST
            @Path("/device/{deviceName}")
            @Produces(MediaType.APPLICATION_JSON)
            @Consumes(MediaType.APPLICATION_JSON)
            void modifyDeviceConfig(@Context UriInfo ctx, @PathParam(value = "deviceName") String deviceName, Map<String, Object> config) throws ConfigurationException;

            @GET
            @Path("/devices")
            @Produces(MediaType.APPLICATION_JSON)
            @Consumes(MediaType.APPLICATION_JSON)
            List<Map<String, Object>> list() throws ConfigurationException;

            @GET
            @Path("/transferCapabilities")
            @Produces(MediaType.APPLICATION_JSON)
            Map<String, Object> getTransferCapabilitiesConfig();

            @GET
            @Path("/exportFullConfiguration")
            @Produces(MediaType.APPLICATION_JSON)
            Map<String, Object> getFullConfig();

            @POST
            @Path("/importFullConfiguration")
            @Consumes(MediaType.APPLICATION_JSON)
            public void setFullConfig(Map<String, Object> config);
        }

        /**
         * jax rs client
         */
        RESTDicomConfigAPI remoteEndpoint;

        public RemoteConfiguration() {
        }

        public RemoteConfiguration(String remoteEndpointURL) {

            // create jax-rs client
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(remoteEndpointURL);
            ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
            remoteEndpoint = rtarget.proxy(RESTDicomConfigAPI.class);
        }


        @Override
        public Map<String, Object> getConfigurationRoot() throws ConfigurationException {
            return remoteEndpoint.getFullConfig();
        }

        @Override
        public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {

            // if connection
            try {
                String deviceName = DicomPath.ConnectionByCnRef.parse(path).getParam("deviceName");
                Map<String, Object> deviceConfig = remoteEndpoint.getDeviceConfig(deviceName);

                // make dummy config tree with this one device
                Map<String, Object> dummyRoot = new DefaultBeanVitalizer().createConfigNodeFromInstance(new CommonDicomConfiguration.DicomConfigurationRootNode());
                ConfigNodeUtil.replaceNode(dummyRoot, DicomPath.DeviceByName.set("deviceName", deviceName).path(), deviceConfig);

                // get connection from dummy
                return ConfigNodeUtil.getNode(dummyRoot, path);

            } catch (IllegalArgumentException e) {
                //noop
            }


            // if TCConfig
            try {

                DicomPath.TCGroups.parse(path);
                return remoteEndpoint.getTransferCapabilitiesConfig();

            } catch (IllegalArgumentException e) {
                //noop
            }


            try {

                return remoteEndpoint.getDeviceConfig(DicomPath.DeviceByName.parse(path).getParam("deviceName"));

            } catch (IllegalArgumentException e) {
                throw new ConfigurationException("This action is not supported when using the remote config", e);
            }
        }

        @Override
        public boolean nodeExists(String path) throws ConfigurationException {
            if (path.equals(DicomPath.ConfigRoot.path())) return true;

            return remoteEndpoint.getDeviceConfig(DicomPath.DeviceByName.parse(path).getParam("deviceName")) != null;
        }

        @Override
        public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

            // if using import
            if ("/".equals(path)) {
                remoteEndpoint.setFullConfig(configNode);
                return;
            }

            try {
                remoteEndpoint.modifyDeviceConfig(null, DicomPath.DeviceByName.parse(path).getParam("deviceName"), configNode);
            } catch (Exception e) {
                throw new ConfigurationException("This action is not supported when using the remote config", e);
            }
        }

        @Override
        public void refreshNode(String path) throws ConfigurationException {

        }

        @Override
        public void removeNode(String path) throws ConfigurationException {

            // if its not a remove device operation - throw an exception
            PathPattern.PathParser parsedPath = null;
            try {
                parsedPath = DicomPath.DeviceByName.parse(path);
            } catch (Exception e) {
                throw new RuntimeException("Not supported");
            }

            remoteEndpoint.removeDevice(parsedPath.getParam("deviceName"));


        }

        @Override
        public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
            Map<String, Object> fullConfig = remoteEndpoint.getFullConfig();
            return ConfigNodeUtil.search(fullConfig, liteXPathExpression);
        }

        @Override
        public void lock() {
            //noop
        }

        @Override
        public void runBatch(Batch batch) {
            batch.run();
        }


    }


}
