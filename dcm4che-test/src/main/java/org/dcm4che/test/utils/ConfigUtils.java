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

package org.dcm4che.test.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.dcm4che.test.common.BasicTest;

/**
 * Utility methods to manipulate the server-side configuration from within
 * tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class ConfigUtils {

    /**
     * Restore the configuration to its initial state.
     * 
     * @param test
     *            the test
     */
    public static void restoreConfig(BasicTest test)
    {
        String baseURL = test.getDefaultProperties().getProperty("remoteConn.url");
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(baseURL + "/dcm4chee-arc-test/config-restore/");
        Response rsp = target.request().build("GET").invoke();

        if (rsp.getStatus() != 200) {
            String s = rsp.readEntity(String.class);
            throw new RuntimeException("Config restore failed:\n" + (s != null ? s : rsp));
        }
    }

}
