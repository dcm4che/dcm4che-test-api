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


import org.apache.commons.cli.MissingArgumentException;
import org.dcm4che.test.annotations.TestLocalConfig;
import org.dcm4che.test.annotations.TestParamDefaults;
import org.dcm4che.test.common.TestToolFactory.TestToolType;
import org.dcm4che.test.utils.RemoteDicomConfigFactory;
import org.dcm4che.test.utils.TestingProperties;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.dcmgen.test.DcmGenResult;
import org.dcm4che3.tool.dcmgen.test.DcmGenTool;
import org.dcm4che3.tool.findscu.test.QueryTool;
import org.dcm4che3.tool.movescu.test.MoveResult;
import org.dcm4che3.tool.movescu.test.MoveTool;
import org.dcm4che3.tool.mppsscu.test.MppsTool;
import org.dcm4che3.tool.storescu.test.StoreResult;
import org.dcm4che3.tool.storescu.test.StoreTool;
import org.dcm4chee.archive.conf.defaults.DefaultArchiveConfigurationFactory;
import org.dcm4chee.archive.conf.defaults.DefaultDicomConfigInitializer;
import org.junit.Assert;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */
public abstract class BasicTest {

    private static Logger log = LoggerFactory.getLogger(TestToolFactory.class);

    @Rule
    public TestParametersRule rule = new TestParametersRule(this);

    /**
     * Name of the currently executing method within the test
     */
    private String currentMethodName = "none";

    private DicomConfiguration localConfig;

    private DicomConfigurationManager remoteConfig = null;

    public DicomConfigurationManager getRemoteConfig() {
        if (remoteConfig == null) {
            String baseURL = getProperties().getProperty("remoteConn.url")+"/config/data";
            remoteConfig = RemoteDicomConfigFactory.createRemoteDicomConfiguration(baseURL);
        }
        return remoteConfig;
    }

    private static Map<String, Annotation> params = new HashMap<String, Annotation>();

    public Map<String, Annotation> getParams() {
        return params;
    }

    public Properties getProperties() {
        return TestingProperties.get();
    }

    protected void addParam( String key, Annotation anno) {
        params.put(key, anno);
    }
    protected void clearParams() {
        params.clear();
    }

    public void init(String currentMethod) {

        this.currentMethodName = currentMethod;

        try {
            if(this.getParams().containsKey("defaultParams") 
                    && this.getParams().get("defaultParams") != null)
                System.setProperty("defaultParams", ((TestParamDefaults)
                        this.getParams().get("defaultParams")).propertiesFile());
            if(this.getParams().containsKey("defaultLocalConfig")
                    && this.getParams().get("defaultLocalConfig") != null)
                System.setProperty("defaultLocalConfig", ((TestLocalConfig)
                        this.getParams().get("defaultLocalConfig")).configFile());

        } catch (ConfigurationException e) {
            throw new TestToolException(e);
        }
    }

    public StoreResult store(String description, String fileName) {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        try {
            storeTool.store(description, fileName);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        StoreResult storeResult = storeTool.getResult();
        Assert.assertTrue("Store", storeResult.getFilesSent() > 0 && storeResult.getFailures() == 0);
        return storeResult;
    }

    public TestResult storeResource(String description, String fileName) throws MissingArgumentException {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);
        File f = new File(fileName);
        storeTool.setbaseDir(f.getParent()==null?"target/test-classes/":f.getParent());
        try {
            storeTool.store(description, fileName);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        StoreResult storeResult = storeTool.getResult();
        Assert.assertTrue("Store", storeResult.getFilesSent() > 0 && storeResult.getFailures() == 0);
        return storeResult;
    }   
    public TestResult query(String description, Attributes keys, boolean fuzzy
            , boolean datatimeCombine, int expectedMatches) throws MissingArgumentException {
        QueryTool queryTool = (QueryTool) TestToolFactory.createToolForTest(TestToolType.QueryTool, this);
        if(expectedMatches > -1)
        queryTool.setExpectedMatches(expectedMatches);
        queryTool.addAll(keys);
            try {
                    queryTool.query(description, fuzzy, datatimeCombine);
            } catch (Exception e) {
                throw new TestToolException(e);
            }
        return queryTool.getResult();
    }

    public TestResult move(String description, Attributes moveAttrs, int expectedMatches) throws MissingArgumentException {
        MoveTool tool = (MoveTool) TestToolFactory.createToolForTest(TestToolType.MoveTool, this);
        tool.setExpectedMatches(expectedMatches);
        tool.addAll(moveAttrs);
        try {
            tool.move(description);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        MoveResult result = (MoveResult) tool.getResult();
        return result;
    }
    public TestResult mpps(String description, String fileName) throws MissingArgumentException {
        MppsTool mppsTool = (MppsTool) TestToolFactory.createToolForTest(TestToolType.MppsTool, this);
        try {
            mppsTool.mppsscu(description, fileName);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        return mppsTool.getResult();
    }

    private TestResult storeGenerated(String description, File file) throws MissingArgumentException {
        StoreTool storeTool = (StoreTool) TestToolFactory.createToolForTest(TestToolType.StoreTool, this);

        try {
            //get whole study
            storeTool.store(description, file.getAbsolutePath());
            deleteDirectory(file);
        } catch (Exception e) {
            throw new TestToolException(e);
        }
        return storeTool.getResult();
    }

    private void deleteDirectory(File file) {

        if (file.isDirectory()) {
            for (int i = 0; i < file.listFiles().length; i++) {
                deleteDirectory(file.listFiles()[i]);
            }
        }
        else {
            file.delete();
        }

    }

    public TestResult generateAndSend(String description, Attributes overrideAttributes) throws MissingArgumentException {
        DcmGenTool dcmGenTool = (DcmGenTool) TestToolFactory.createToolForTest(TestToolType.DcmGenTool, this);
        TestResult storeResult;
        dcmGenTool.generateFiles(description, overrideAttributes);
        DcmGenResult result = (DcmGenResult) dcmGenTool.getResult();
        storeResult = storeGenerated(description, dcmGenTool.getOutputDir());
        return storeResult;
    }

    public TestResult generateAndSend(String description, Attributes overrideAttributes, File otherSeedFile) throws MissingArgumentException {
        DcmGenTool dcmGenTool = (DcmGenTool) TestToolFactory.createToolForTest(TestToolType.DcmGenTool, this);
        TestResult storeResult;
        dcmGenTool.generateFiles(description, overrideAttributes, otherSeedFile);
        DcmGenResult result = (DcmGenResult) dcmGenTool.getResult();
        storeResult = storeGenerated(description, dcmGenTool.getOutputDir());
        return storeResult;
    }

    public DicomConfiguration getLocalConfig() {
        if (localConfig == null) {

            Configuration configuration = new InMemoryConfiguration();
            HashMap extensionsByClass = new HashMap();

            CommonDicomConfigurationWithHL7 dicomConfig = new CommonDicomConfigurationWithHL7(configuration, extensionsByClass);

            DefaultArchiveConfigurationFactory.FactoryParams params = new DefaultArchiveConfigurationFactory.FactoryParams();

            params.useGroupBasedTCConfig = false;

            // rely on timeouts in the archive
            // and set it to unlimited for tools for easy debugging
            params.socketTimeout = 0;

            new DefaultDicomConfigInitializer().persistDefaultConfig(
                dicomConfig,
                    dicomConfig,
                    params
            );


            localConfig = dicomConfig;

        }

        return localConfig;
    }

    /**
     * @return Directory containing test data (mesa).
     */
    public Path getTestdataDirectory()
    {
        return Paths.get(getProperties().getProperty("testdata.directory")).toAbsolutePath().normalize();
    }

    /**
     * @return Directory which should be used to store temporary files created
     *         within a test. Will be cleaned before every test.
     */
    public Path getBaseTemporaryDirectory()
    {
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
     * 
     * Useful for storing temporary files generated within the test.
     * 
     * @param prefix
     *            arbitrary string which will be used as a prefix for the
     *            temporary directory
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
