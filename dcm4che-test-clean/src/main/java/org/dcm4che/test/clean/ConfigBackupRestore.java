package org.dcm4che.test.clean;

import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;

/**
 * @author Roman K
 */
@ApplicationScoped
@Path("/config-restore")
public class ConfigBackupRestore {

    private DicomConfigurationManager configurationManager;
    private Map<String, Object> defaultConfig;


    @PostConstruct
    public void init() throws ConfigurationException {

        configurationManager = DicomConfigurationBuilder.newConfigurationBuilder(System.getProperties()).build();

        Configuration configurationStorage = configurationManager.getConfigurationStorage();
        defaultConfig = (Map<String, Object>) configurationStorage.getConfigurationNode("/", null);
    }


    @GET
    @Path("/")
    public void restoreConfig() throws ConfigurationException {
        configurationManager.getConfigurationStorage().persistNode("/", defaultConfig, null);
    }

}

