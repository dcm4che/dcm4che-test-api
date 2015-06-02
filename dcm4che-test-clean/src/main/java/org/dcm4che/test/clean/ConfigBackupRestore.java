package org.dcm4che.test.clean;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.CommonDicomConfiguration.DicomConfigurationRootNode;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;

/**
 * Web service which allows to restore the complete Configuration to an initial
 * state.
 * 
 * @author Roman K
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@ApplicationScoped
@Path("/config-restore")
public class ConfigBackupRestore {

    private DicomConfigurationManager configurationManager;

    private Map<String, Object> initialConfig;

    @PostConstruct
    public void init() throws ConfigurationException {

        configurationManager = DicomConfigurationBuilder.newConfigurationBuilder(System.getProperties()).build();

        Configuration configurationStorage = configurationManager.getConfigurationStorage();
        initialConfig = (Map<String, Object>) configurationStorage.getConfigurationNode("/", DicomConfigurationRootNode.class);
    }


    @GET
    @Path("/")
    public Response restoreConfig() throws ConfigurationException {

        configurationManager.getConfigurationStorage().persistNode("/", initialConfig, DicomConfigurationRootNode.class);

        return Response.status(Status.OK).entity("Successfully restored configuration").build();
    }

}
