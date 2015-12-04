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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.dcm4che.test.annotations.DcmGenParameters;
import org.dcm4che.test.annotations.EchoParameters;
import org.dcm4che.test.annotations.GetParameters;
import org.dcm4che.test.annotations.MoveParameters;
import org.dcm4che.test.annotations.MppsParameters;
import org.dcm4che.test.annotations.QCParameters;
import org.dcm4che.test.annotations.QidoRSParameters;
import org.dcm4che.test.annotations.QueryParameters;
import org.dcm4che.test.annotations.RemoteConnectionParameters;
import org.dcm4che.test.annotations.StgCmtParameters;
import org.dcm4che.test.annotations.StoreParameters;
import org.dcm4che.test.annotations.StoreSCPParameters;
import org.dcm4che.test.annotations.StowRSParameters;
import org.dcm4che.test.annotations.WadoRSParameters;
import org.dcm4che.test.annotations.WadoURIParameters;
import org.dcm4che.test.tool.EchoTool;
import org.dcm4che.test.tool.externaldevice.ExternalDeviceToolConfig;
import org.dcm4che.test.tool.ianscp.IanSCPTestTool;
import org.dcm4che.test.utils.TestUtils;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Code;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.dcmgen.test.DcmGenTool;
import org.dcm4che3.tool.findscu.test.QueryTool;
import org.dcm4che3.tool.getscu.test.RetrieveTool;
import org.dcm4che3.tool.movescu.test.MoveTool;
import org.dcm4che3.tool.mppsscp.test.MPPSSCPTool;
import org.dcm4che3.tool.mppsscu.test.MppsTool;
import org.dcm4che3.tool.qc.QCOperation;
import org.dcm4che3.tool.qc.test.QCTool;
import org.dcm4che3.tool.qidors.test.QidoRSTool;
import org.dcm4che3.tool.stgcmtscu.test.StgCmtTool;
import org.dcm4che3.tool.storescp.test.StoreSCPTool;
import org.dcm4che3.tool.storescu.test.StoreTool;
import org.dcm4che3.tool.stowrs.test.StowRSTool;
import org.dcm4che3.tool.wadors.test.WadoRSTool;
import org.dcm4che3.tool.wadouri.test.WadoURITool;

/**
 * Factory to create tools for testing.
 * 
 * @author Hesham elbadawi <bsdreko@gmail.com>
 */
public class TestToolFactory {

    public enum TestToolType {
        StoreTool,
        GetTool,
        MoveTool,
        QueryTool,
        MppsTool,
        StorageCommitmentTool,
        StowTool,
        QidoTool,
        WadoURITool,
        WadoRSTool,
        StoreSCPTool,
        DcmGenTool,
        QCTool,
        EchoTool,
        MppsScpTool,
        IanScpTool
    }

    public static TestTool createToolForTest(TestToolType type, BasicTest test) {

        Properties defaultParams = test.getProperties();

        RemoteConnectionParameters remoteParams =
                (RemoteConnectionParameters) test.getParams().get("RemoteConnectionParameters");

        String host = defaultParams.getProperty("remoteConn.hostname");

        int port = remoteParams == null ?
                Integer.valueOf(defaultParams.getProperty("remoteConn.port"))
                : Integer.valueOf(remoteParams.port());

        String baseURL = getBaseURL(test);
        String webContext = getWebContext(test);

        if (host == null || baseURL == null || webContext == null)
            throw new RuntimeException("Not all the properties are set");

        switch (type) {

        case StoreTool:

            return createStoreTool(test, defaultParams, host, port);

        case QueryTool:

            return createQueryTool(test, defaultParams, host, port);

        case MppsTool:

            return createMPPSTool(test, defaultParams, host, port);

        case GetTool:

            return createGetTool(test, defaultParams, host, port);

        case DcmGenTool:

            return createDcmGenTool(test, defaultParams);

        case StorageCommitmentTool:

            return createStorageCommitmentTool(test, defaultParams, host, port);

        case MoveTool:

            return createMoveTool(test, defaultParams, host, port);

        case StowTool:

            return createStowTool(test, baseURL, webContext);

        case QidoTool:

            return createQidoTool(test, baseURL, webContext);

        case WadoURITool:

            return createWadoURITool(test, defaultParams, baseURL, webContext);

        case WadoRSTool:

            return createWadoRSTool(test, baseURL, webContext);

        case QCTool:

            return createQCTool(test);

        case StoreSCPTool:

            return createStoreSCPTool(test, defaultParams);

        case EchoTool:

            return createEchoTool(test, defaultParams, host, port);

        case MppsScpTool:

            try {
                Device mppsscp = getDicomConfiguration(test).findDevice("mppsscp");
                return new MPPSSCPTool(mppsscp);
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }

        case IanScpTool:

            return createIanScpTestTool(test);

        default:
            throw new IllegalArgumentException("Unsupported TestToolType specified"
                    + ", unable to create tool");
        }
    }

    private static IanSCPTestTool createIanScpTestTool(BasicTest test) {
        Device ianscp;
        try {
            ianscp = getDicomConfiguration(test).findDevice("ianscp");
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

        Path storageDirectory = test.createTempDirectory("IanSCP");

        return new IanSCPTestTool(ianscp, storageDirectory);
    }

    private static String getBaseURL(BasicTest test) {
        return test.getProperties().getProperty("remoteConn.url");
    }

    private static String getWebContext(BasicTest test) {
        return test.getProperties().getProperty("remoteConn.webcontext");
    }

    private static TestTool createStoreSCPTool(BasicTest test, Properties defaultParams) {

        StoreSCPParameters storeSCPParams = (StoreSCPParameters) test.getParams().get("StoreSCPParameters");

        File storeDir;
        if (storeSCPParams == null || storeSCPParams.storageDirectory().isEmpty()) {
            storeDir = test.createTempDirectory("StoreSCP").toFile();
        } else {
            storeDir = new File(storeSCPParams.storageDirectory());
        }

        String sourceDevice = storeSCPParams != null ? storeSCPParams.sourceDevice() : "storescp";

        String sourceAETitle = storeSCPParams != null ? storeSCPParams.sourceAETitle() : "STORESCP";

        boolean noStore = storeSCPParams != null ? storeSCPParams.noStore() : false;

        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection(
                    (String) (storeSCPParams != null && storeSCPParams.connection() != null?
                            storeSCPParams.connection():defaultParams.get("storescp.connection")), ""));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

        return new StoreSCPTool(storeDir, device, sourceAETitle, conn, noStore);
    }

    private static TestTool createQCTool(BasicTest test) throws IllegalArgumentException {
        TestTool tool;
        QCParameters qcParams = (QCParameters) test.getParams().get("QCParameters");
        
        if(qcParams == null)
            throw new IllegalArgumentException("QCParameters annotation"
                    + "must be used to create QCTool");
        String url = qcParams.url();
        QCOperation operation = qcParams.operation();
        String targetStudy = qcParams.targetStudyUID();
        String codeStr = qcParams.qcRejectionCodeString();
        String[] codeComponents = codeStr.split(":");
        Code code = null;
        if(codeComponents.length < 3)
            throw new IllegalArgumentException("Code specified must contain"
                    + " at least value, scheme designator and meaning");
        else
            if(codeComponents.length == 3)
                code = new Code(codeComponents[0], codeComponents[2], null,codeComponents[1]);
            else if(codeStr.split(":").length == 4)
                code = new Code(codeComponents[0], codeComponents[2]
                        , codeComponents[3], codeComponents[1]);
        
        tool = new QCTool(url, operation, code, targetStudy);
        return tool;
    }

    public static WadoRSTool createWadoRSTool(BasicTest test, WadoRSParameters params) {
        return createWadoRSTool(test, getBaseURL(test), getWebContext(test), params);
    }

    private static WadoRSTool createWadoRSTool(BasicTest test, String baseURL, String webContext) throws IllegalArgumentException {
        WadoRSParameters wadoRSParams = (WadoRSParameters) test.getParams().get("WadoRSParameters");

        return createWadoRSTool(test, baseURL, webContext, wadoRSParams);
    }

    private static WadoRSTool createWadoRSTool(BasicTest test, String baseURL, String webContext, WadoRSParameters wadoRSParams) throws IllegalArgumentException {
        if (wadoRSParams == null)
            throw new IllegalArgumentException("WadoRSParameters annotation"
                    + " must be used to create a WadoRS tool");
        String url = wadoRSParams.url();

        String retrieveDirString = wadoRSParams.retrieveDir();
        File retrieveDir;
        if (retrieveDirString.isEmpty()) {
            retrieveDir = test.createTempDirectory("WADORS").toFile();
        } else {
            retrieveDir = new File(retrieveDirString);
        }

        return new WadoRSTool(baseURL + "/" + webContext + (url.startsWith("/") ? url : "/" + url), retrieveDir);
    }

    private static TestTool createWadoURITool(BasicTest test, Properties defaultParams, String baseURL, String webContext) throws IllegalArgumentException {
        WadoURIParameters wadoUriParams = (WadoURIParameters) test.getParams().get("WadoURIParameters");

        if(wadoUriParams == null)
            throw new IllegalArgumentException("WadoURIParameters annotation"
                    + " must be used to create a WadoURI tool");
        String url = wadoUriParams.url();
        String studyUID = wadoUriParams.studyUID() != null ? wadoUriParams.studyUID() : null;
        String seriesUID = wadoUriParams.seriesUID() != null ? wadoUriParams.seriesUID() : null;
        String objectUID = wadoUriParams.objectUID() != null ? wadoUriParams.objectUID() : null;
        String contentType = wadoUriParams.contentType() != null ? wadoUriParams.contentType() : null;
        //non-mandatory
        String charset = wadoUriParams.charset();
        String annotation = wadoUriParams.annotation();
        String regionCoordinates = wadoUriParams.regionCoordinates();
        String windowCenter = wadoUriParams.windowCenter();
        String windowWidth = wadoUriParams.windowWidth();
        String presentationSeriesUID = wadoUriParams.presentationSeriesUID();
        String presentationUID = wadoUriParams.presentationUID();
        String transferSyntax = wadoUriParams.transferSyntax();
        boolean anonymize = wadoUriParams.anonymize();
        int rows = wadoUriParams.rows();
        int columns = wadoUriParams.columns();
        int[] frameNumbers = wadoUriParams.frameNumbers();
        int imageQuality = wadoUriParams.imageQuality();

        File retrieveDir;
        if (wadoUriParams.retrieveDir().isEmpty()) {
            retrieveDir = test.createTempDirectory("WADOURI").toFile();
        } else {
            retrieveDir = new File(wadoUriParams.retrieveDir());
        }

        return new WadoURITool(baseURL + "/" + webContext + (url.startsWith("/") ? url : "/" + url)
                ,studyUID, seriesUID, objectUID
                , contentType, charset, anonymize
                , annotation, rows, columns
                , regionCoordinates, windowCenter, windowWidth
                , frameNumbers, imageQuality, presentationSeriesUID
                , presentationUID, transferSyntax, retrieveDir);
    }

    private static TestTool createQidoTool(BasicTest test, String baseURL, String webContext) throws IllegalArgumentException {
        QidoRSParameters qidoParams = (QidoRSParameters) test.getParams().get("QidoRSParameters");

        String url = qidoParams != null && qidoParams.url() != null ? qidoParams.url() : null;
        if (url == null)
            throw new IllegalArgumentException("To create a QidoRS Tool a url must be specified" + " in the QidoParameters annotation");
        String limit = qidoParams != null && !"-1".equals(qidoParams.limit()) ? qidoParams.limit() : null;
        boolean fuzzy = qidoParams != null && qidoParams.fuzzyMatching() ? qidoParams.fuzzyMatching() : false;
        boolean timezone = qidoParams != null && qidoParams.timezoneAdjustment() ? qidoParams.timezoneAdjustment() : false;
        boolean returnAll = qidoParams != null ? qidoParams.returnAll() : false;
        String offset = qidoParams != null ? qidoParams.offset() : "0";
        return new QidoRSTool(baseURL + "/" + webContext + (url.startsWith("/") ? url : "/" + url), limit, fuzzy, timezone, returnAll, offset);
    }

    private static TestTool createStowTool(BasicTest test, String baseURL, String webContext) {
        TestTool tool;
        StowRSParameters stowParams = (StowRSParameters) test.getParams().get("StowRSParameters");

        String url = stowParams != null && stowParams.url() != null ? stowParams.url()
                : null;
        if (url == null)
            throw new IllegalArgumentException("To create a StowRS Tool a url must be specified"
                    + " in the StowParameters annotation");
        tool = new StowRSTool(baseURL + "/" + webContext + (url.startsWith("/") ? url : "/" + url));
        return tool;
    }

    private static TestTool createMoveTool(BasicTest test, Properties defaultParams, String host, int port) {
        TestTool tool;
        MoveParameters moveParams = (MoveParameters) test.getParams().get("MoveParameters");

        String aeTitle = moveParams != null && !"NULL".equalsIgnoreCase(moveParams.aeTitle()) ? moveParams.aeTitle() : defaultParams.getProperty("move.aetitle");

        String retrieveLevel = moveParams != null && !"NULL".equalsIgnoreCase(moveParams.retrieveLevel()) ? moveParams.retrieveLevel() : defaultParams.getProperty("move.level");

        String queryInformationModel = moveParams != null ? moveParams.retrieveInformationModel() : defaultParams.getProperty("move.informationmodel");

        boolean relational = moveParams != null ? moveParams.relational() : Boolean.valueOf(defaultParams.getProperty("move.relational"));

        String destAEtitle = moveParams != null && !"NULL".equalsIgnoreCase(moveParams.destAEtitle()) ? moveParams.destAEtitle() : defaultParams.getProperty("move.destaetitle");

        String sourceDevice = moveParams != null ? moveParams.sourceDevice() : "movescu";
        String sourceAETitle = moveParams != null ? moveParams.sourceAETitle() : "MOVESCU";
        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection(
                    (String) (moveParams != null && moveParams.connection() != null?
                            moveParams.connection():defaultParams.get("move.connection")), ""));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        tool = new MoveTool(host, port, aeTitle, destAEtitle, retrieveLevel
                , queryInformationModel, relational, device, sourceAETitle, conn);
        return tool;
    }

    private static TestTool createStorageCommitmentTool(BasicTest test, Properties defaultParams, String host, int port) {
        StgCmtParameters stgcmtParams = (StgCmtParameters) test.getParams().get("StgCmtParameters");

        String aeTitle = stgcmtParams != null && !"NULL".equalsIgnoreCase(stgcmtParams.aeTitle()) ? stgcmtParams.aeTitle() : defaultParams.getProperty("stgcmt.aetitle");

        File baseDir = (stgcmtParams != null && !"NULL".equalsIgnoreCase(stgcmtParams.baseDirectory())) ? new File(stgcmtParams.baseDirectory()) : test.getTestdataDirectory().toFile();

        File stgCmtStorageDirectory = (stgcmtParams != null && !"NULL".equalsIgnoreCase(stgcmtParams.storageDirectory())) ? new File(stgcmtParams.storageDirectory()) : test.createTempDirectory("STGCMT").toFile();

        String sourceDevice = stgcmtParams != null ? stgcmtParams.sourceDevice() : "stgcmtscu";
        String sourceAETitle = stgcmtParams != null ? stgcmtParams.sourceAETitle() : "STGCMTSCU";
        Device device;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

        String connectionName = (String) (stgcmtParams != null && stgcmtParams.connection() != null ? stgcmtParams.connection() : defaultParams.get("stgcmt.connection"));
        Connection conn = device.connectionWithEqualsRDN(new Connection(connectionName, ""));

        // adjust the remote device so that it will use the correct connection (popped up when fixing DCMTEST-40)
        TestUtils.adjustRemoteConfigurationForDestinationSCP(device.getDeviceName(), test, connectionName);

        return new StgCmtTool(host, port, aeTitle, baseDir, stgCmtStorageDirectory, device, sourceAETitle, conn);
    }

    private static TestTool createDcmGenTool(BasicTest test, Properties defaultParams) {
        DcmGenParameters genParams = (DcmGenParameters) test.getParams().get("DcmGenParameters");

        Path testdataDir = test.getTestdataDirectory();

        File seedFile = new File(testdataDir.toFile(), genParams.seedFile());
        File outputDir;
        String outputDirString = genParams.outputDir();
        if (outputDirString.isEmpty()) {
            outputDir = test.createTempDirectory("DCMGEN").toFile();
        } else {
            outputDir = new File(outputDirString);
        }
        int instanceCnt = genParams.instanceCount();
        int seriesCnt = genParams.seriesCount();
        return new DcmGenTool(instanceCnt, seriesCnt, outputDir, seedFile);
    }

    private static TestTool createGetTool(BasicTest test, Properties defaultParams, String host, int port) {
        GetParameters getParams = (GetParameters) test.getParams().get("GetParameters");

        String aeTitle = getParams != null && !"NULL".equalsIgnoreCase(getParams.aeTitle()) ? getParams.aeTitle() : defaultParams.getProperty("retrieve.aetitle");

        String retrieveLevel = getParams != null && getParams.retrieveLevel() != null ? getParams.retrieveLevel() : defaultParams.getProperty("retrieve.level");

        String queryInformationModel = getParams != null ? getParams.retrieveInformationModel() : defaultParams.getProperty("retrieve.informationmodel");

        boolean relational = getParams != null ? getParams.relational() : Boolean.valueOf(defaultParams.getProperty("retrieve.relational"));

        File retrieveDir;
        if (getParams == null || getParams.retrieveDir().isEmpty()) {
            retrieveDir = test.createTempDirectory("RETRIEVE").toFile();
        } else {
            retrieveDir = new File(getParams.retrieveDir());
        }

        String sourceDevice = getParams != null ? getParams.sourceDevice() : "getscu";
        String sourceAETitle = getParams != null ? getParams.sourceAETitle() : "GETSCU";
        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection((String) (getParams != null && getParams.connection() != null ? getParams.connection() : defaultParams.get("retrieve.connection")), ""));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        return new RetrieveTool(host, port, aeTitle, retrieveDir, device, sourceAETitle, retrieveLevel, queryInformationModel, relational, conn);
    }

    private static TestTool createMPPSTool(BasicTest test, Properties defaultParams, String host, int port) {
        MppsParameters mppsParams = (MppsParameters) test.getParams().get("MppsParameters");

        String aeTitle = mppsParams != null && !"NULL".equalsIgnoreCase(mppsParams.aeTitle()) ? mppsParams.aeTitle() : defaultParams.getProperty("mpps.aetitle");

        String mppsDir = defaultParams.getProperty("mpps.directory");
        if (mppsDir == null) throw new RuntimeException("mpps.directory not set in properties!");
        File baseDir = new File(mppsDir);

        String sourceDevice = mppsParams != null ? mppsParams.sourceDevice() : "mppsscu";
        String sourceAETitle = mppsParams != null ? mppsParams.sourceAETitle() : "MPPSSCU";
        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection((String) (mppsParams != null && mppsParams.connection() != null ? mppsParams.connection() : defaultParams.get("mpps.connection")), ""));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        return new MppsTool(host, port, aeTitle, baseDir, device, sourceAETitle, conn);
    }

    private static TestTool createQueryTool(BasicTest test, Properties defaultParams, String host, int port) {
        QueryParameters queryParams = (QueryParameters) test.getParams().get("QueryParameters");

        String aeTitle = queryParams != null && !"NULL".equalsIgnoreCase(queryParams.aeTitle()) ? queryParams.aeTitle() : defaultParams.getProperty("query.aetitle");

        String queryLevel = queryParams != null ? queryParams.queryLevel() : defaultParams.getProperty("query.level");

        String queryInformationModel = queryParams != null ? queryParams.queryInformationModel() : defaultParams.getProperty("query.informationmodel");
        boolean relational = queryParams != null ? queryParams.relational() : Boolean.valueOf(defaultParams.getProperty("query.relational"));

        String sourceDevice = queryParams != null ? queryParams.sourceDevice() : "findscu";

        String sourceAETitle = queryParams != null ? queryParams.sourceAETitle() : "FINDSCU";

        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection((String) (queryParams != null && queryParams.connection() != null ? queryParams.connection() : defaultParams.get("query.connection")), ""));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        return new QueryTool(host, port, aeTitle, queryLevel, queryInformationModel, relational, device, sourceAETitle, conn);
    }

    private static TestTool createStoreTool(BasicTest test, Properties defaultParams, String host, int port) {
        StoreParameters storeParams = (StoreParameters) test.getParams().get("StoreParameters");

        String aeTitle = storeParams != null && !"NULL".equalsIgnoreCase(storeParams.aeTitle()) ? storeParams.aeTitle() : defaultParams.getProperty("store.aetitle");

        File baseDir = storeParams != null && !"NULL".equalsIgnoreCase(storeParams.baseDirectory()) ? new File(storeParams.baseDirectory()) : new File(defaultParams.getProperty("store.directory"));

        String sourceDevice = storeParams != null ? storeParams.sourceDevice() : "storescu";

        String sourceAETitle = storeParams != null ? storeParams.sourceAETitle() : "STORESCU";

        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection((String) (storeParams != null && storeParams.connection() != null ? storeParams.connection() : defaultParams.get("store.connection")), ""));
        } catch (ConfigurationException e) {
            throw new TestToolException(e);
        }
        return new StoreTool(host, port, aeTitle, baseDir, device, sourceAETitle, conn);
    }

    private static TestTool createEchoTool(BasicTest test, Properties defaultParams, String host, int port) {
        EchoParameters echoParams = (EchoParameters) test.getParams().get("EchoParameters");

        // we use re-use some of the default from the StoreTool here
        String aeTitle = echoParams != null && !"NULL".equalsIgnoreCase(echoParams.aeTitle()) ? echoParams.aeTitle() : defaultParams.getProperty("store.aetitle");

        String sourceDevice = echoParams != null ? echoParams.sourceDevice() : "storescu";

        String sourceAETitle = echoParams != null ? echoParams.sourceAETitle() : "ECHOSCU";

        Device device;
        Connection conn;
        try {
            device = getDicomConfiguration(test).findDevice(sourceDevice);
            conn = device.connectionWithEqualsRDN(new Connection((String) (echoParams != null && echoParams.connection() != null ? echoParams.connection() : defaultParams.get("store.connection")), ""));
        } catch (ConfigurationException e) {
            throw new TestToolException(e);
        }
        return new EchoTool(host, port, aeTitle, device, sourceAETitle, conn);
    }
    
    public static ExternalDeviceToolConfig createExternalDeviceToolConfig(DicomConfiguration dicomCfg, File testStorageDir, String extDeviceName, String extDeviceAeTitle, String archiveDeviceName, String archiveDeviceAeTitle) {
        Device extDevice;
        try {
            extDevice = dicomCfg.findDevice(extDeviceName);
        } catch (ConfigurationException e) {
            throw new TestToolException(e);
        }
        
        ApplicationEntity extDeviceAe = extDevice.getApplicationEntity(extDeviceAeTitle);
        
      
        Device archiveDevice;
        try {
            archiveDevice = dicomCfg.findDevice(archiveDeviceName);
        } catch (ConfigurationException e) {
            throw new TestToolException(e);
        }
        
        ApplicationEntity archiveDeviceAE = archiveDevice.getApplicationEntity(archiveDeviceAeTitle);
        
        ExternalDeviceToolConfig qrScpConfig = new ExternalDeviceToolConfig();
        try {
            qrScpConfig.addRemoteConnection(archiveDeviceAeTitle,archiveDevice.getDefaultAE().findCompatibelConnection(extDeviceAe).getLocalConnection());
        } catch (IncompatibleConnectionException e) {
            throw new RuntimeException("Cannot find an appropriate connection of the archive for the external device tool to use",e);
        }

        qrScpConfig.device(extDevice)
            .aeTitle(extDeviceAe.getAETitle())
            .port(extDeviceAe.getConnections().iterator().next().getPort());
        
        try {
            File dicomDir = File.createTempFile("dcmext_storage_", ".dicomdir", testStorageDir);
            qrScpConfig.dicomDir(dicomDir);
        } catch(IOException e) {
            throw new TestToolException(e);
        }
        
        return qrScpConfig;
    }
    
    public static ExternalDeviceToolConfig createExternalDeviceToolConfig(BasicTest test, String extDeviceName, String extDeviceAeTitle, String archiveDeviceName, String archiveDeviceAeTitle ) {
        DicomConfiguration dicomCfg = getDicomConfiguration(test);
        File tmpStorageDir = test.createTempDirectory("EXTDEVICE").toFile();
        return createExternalDeviceToolConfig(dicomCfg, tmpStorageDir, extDeviceName, extDeviceAeTitle, archiveDeviceName, archiveDeviceAeTitle);
    }
    
    public static DicomConfiguration getDicomConfiguration(BasicTest test) {
        return test.getLocalConfig();
    }

}
