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

package org.dcm4che.test.tool;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.storescu.StoreSCU;

/**
 * C-ECHO SCU tool for tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class EchoTool implements TestTool {

    private final String host;
    private final int port;
    private final String aeTitle;
    private final Device sourceDevice;
    private final Connection sourceConnection;
    private final String sourceAETitle;
    
    public EchoTool(String host, int port, String aeTitle, Device sourceDevice, String sourceAETitle, Connection sourceConnection) {
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.sourceDevice = sourceDevice;
        this.sourceAETitle = sourceAETitle;
        this.sourceConnection = sourceConnection;
    }

    public Attributes echo() throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {

        sourceDevice.setInstalled(true);
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        sourceDevice.addApplicationEntity(ae);
        ae.addConnection(sourceConnection);

        // we are using the StoreSCU for doing the C-ECHO (because there is no SCU which only supports C-ECHO at the moment)
        StoreSCU main = new StoreSCU(ae);

        // configure connection params
        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);
        main.getRemoteConnection().setTlsCipherSuites(sourceConnection.getTlsCipherSuites());
        main.getRemoteConnection().setTlsProtocols(sourceConnection.getTlsProtocols());

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        sourceDevice.setExecutor(executorService);
        sourceDevice.setScheduledExecutor(scheduledExecutorService);

        Attributes responseDataset;
        try {
            main.open();

            responseDataset = main.echo();
        } finally {
            main.close();
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }

        return responseDataset;
    }

    @Override
    public void init(TestResult result) {
        // not needed
    }

    @Override
    public TestResult getResult() {
        return null; // not needed
    }

}
