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
package org.dcm4che.test.common;


import org.dcm4che.test.annotations.TestLocalConfig;
import org.dcm4che.test.annotations.TestParamDefaults;
import org.dcm4che.test.utils.TestingProperties;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */
public class BasicTest {

    private static Logger log = LoggerFactory.getLogger(BasicTest.class);

    public DicomConfiguration localConfig;

    private Boolean isRunningInIDE;

    @Rule
    public IntegrationTestingRule rule = new IntegrationTestingRule(this);

    /**
     * Name of the currently executing method within the test
     */
    public String currentMethodName = "none";
    /**
     * Use this method to differentiate between tests running on a CI and test being debugged/implemented by devs/testers,
     * to auto-set things like timeouts, enabling all tests (e.g. heavy tests) by default, etc.
     *
     * @return true if running a test inside an IDE like eclipse or IDEA
     */
    public boolean isRunningInsideIDE() {
        if (isRunningInIDE == null)
            try {
                throw new RuntimeException();
            } catch (RuntimeException e) {
                String firstStackElemClassName = e.getStackTrace()[e.getStackTrace().length - 1].getClassName();
                isRunningInIDE = firstStackElemClassName.contains("intellij") || firstStackElemClassName.contains("eclipse");

                if (isRunningInIDE)
                    System.out.println(" **************************************************************" +
                            "\n Detected that the test started from within an IDE. DEV mode activated: " +
                            "\n - all filters disabled (e.g. reported issue, heavy, etc)" +
                            "\n - unlimited timeouts " +
                            "\n **************************************************************");
                else
                    System.out.println("Detected that the test is started as standalone (DEV mode is NOT active)");
            }
        return isRunningInIDE;
    }

    public void setLocalConfig(String defaultLocalConfigSystemProperty) throws ConfigurationException {
        File LocalConfigFile = null;
        if(defaultLocalConfigSystemProperty == null) {
            try {
                LocalConfigFile = Files.createTempFile("tempdefaultconfig", "json").toFile();

                Files.copy(BasicTest.class.getClassLoader()
                        .getResourceAsStream("defaultConfig.json")
                        , LocalConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LocalConfigFile.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            LocalConfigFile = new File (defaultLocalConfigSystemProperty);
        }
        this.localConfig = DicomConfigurationBuilder.newJsonConfigurationBuilder(LocalConfigFile.getPath()).build();
    }

    public DicomConfiguration getLocalConfig() {
        return localConfig;
    }


    public void resetConfigAndDB() {

    }

    private static Map<String, Annotation> params = new HashMap<String, Annotation>();

    public Map<String, Annotation> getParams() {
        return params;
    }

    public Properties getProperties() {
        return TestingProperties.get();
    }

    protected void addParam(String key, Annotation anno) {
        params.put(key, anno);
    }

    protected void clearParams() {
        params.clear();
    }

    public void init(String currentMethod) {

        this.currentMethodName = currentMethod;

        try {
            if (this.getParams().containsKey("defaultParams")
                    && this.getParams().get("defaultParams") != null)
                System.setProperty("defaultParams", ((TestParamDefaults)
                        this.getParams().get("defaultParams")).propertiesFile());
            if (this.getParams().containsKey("defaultLocalConfig")
                    && this.getParams().get("defaultLocalConfig") != null)
                System.setProperty("defaultLocalConfig", ((TestLocalConfig)
                        this.getParams().get("defaultLocalConfig")).configFile());
            this.setLocalConfig(System.getProperty("defaultLocalConfig"));
        } catch (ConfigurationException e) {
            throw new TestToolException(e);
        }
    }




    /**
     * @return Directory containing test data (mesa).
     */
    public Path getTestdataDirectory() {
        return Paths.get(getProperties().getProperty("testdata.directory")).toAbsolutePath().normalize();
    }

    /**
     * @return Directory which should be used to store temporary files created
     * within a test. Will be cleaned before every test.
     */
    public Path getBaseTemporaryDirectory() {
        Path tmpDir = Paths.get(getProperties().getProperty("base.tmp.directory"), this.getClass().getName(), currentMethodName).toAbsolutePath().normalize();
        if (!Files.isDirectory(tmpDir)) {
            try {
                Files.createDirectories(tmpDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tmpDir;
    }

    /**
     * Create a new temporary directory for this test that is guaranteed to be
     * unique.
     * <p/>
     * Useful for storing temporary files generated within the test.
     *
     * @param prefix arbitrary string which will be used as a prefix for the
     *               temporary directory
     * @return path to newly created temporary directory
     */
    public Path createTempDirectory(String prefix) {

        Path tmpDir = getBaseTemporaryDirectory();

        Path subTempDirectory;
        try {
            subTempDirectory = Files.createTempDirectory(tmpDir, prefix + "_");
        } catch (IOException e) {
            // normally shouldn't happen unless the hard disk is full or something as severe
            throw new RuntimeException(e);
        }

        log.info("Created temporary directory: {}", subTempDirectory);

        return subTempDirectory;
    }


}
