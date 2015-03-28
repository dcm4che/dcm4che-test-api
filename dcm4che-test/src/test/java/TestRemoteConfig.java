import org.dcm4che.test.utils.RemoteDicomConfigFactory;
import org.dcm4che.test.utils.TestUtils;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

/**
 * @author Roman K
 */

//@RunWith(JUnit4.class)
public class TestRemoteConfig {

    //@Test
    public void testConfig() throws ConfigurationException {

        DicomConfiguration remoteDicomConfiguration = RemoteDicomConfigFactory.createRemoteDicomConfiguration("http://localhost:8082/dcm4chee-conf-web-1.0.0-SNAPSHOT-unsecure/data");
        Device device = remoteDicomConfiguration.findDevice("dcm4chee-arc");
        System.out.println(device.getDeviceName());

        device.setSoftwareVersions(new String[]{"124.321"});
        remoteDicomConfiguration.merge(device);

    }

}
