/*
 *
 * ** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che.test.remotedeploy;

import org.dcm4che3.util.Base64;
import org.dcm4chee.archive.test.remoting.DeSerializer;
import org.dcm4chee.archive.test.remoting.InsiderREST;
import org.dcm4chee.archive.test.remoting.RemoteRequestJSON;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


public class PortalToServer {

    public static String REMOTE_ENDPOINT_URL = "http://localhost:8080/dcm4chee-arc-insider";

    private static InsiderREST remoteEndpoint;

    public static <T> T warp(Class<T> insiderInterface, final Class<? extends T> insiderClass) {

        Object o = Proxy.newProxyInstance(insiderInterface.getClassLoader(), new Class[]{insiderInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                RemoteRequestJSON requestJSON = new RemoteRequestJSON();

                requestJSON.methodName = method.getName();
                requestJSON.mainClassName = insiderClass.getName();
                requestJSON.classes = new HashMap<String, String>();

                // main class
                URL insiderClassResource = insiderClass.getResource(insiderClass.getSimpleName() + ".class");
                requestJSON.classes.put(insiderClass.getName(), Base64.toBase64(getBytes(insiderClassResource)));

                // inner classes
                try {
                    for (Class<?> aClass : insiderClass.getDeclaredClasses()) {

                        String[] splitClassName = aClass.getName().split("\\.");
                        String classFileName = splitClassName[splitClassName.length - 1] + ".class";
                        URL resource = insiderClass.getResource(classFileName);
                        requestJSON.classes.put(aClass.getName(), Base64.toBase64(getBytes(resource)));

                    }
                } catch (IOException e) {
                    throw new RuntimeException("trouble reading bytecode", e);
                }

                String base64resp = getRemoteEndpoint().warpAndRun(requestJSON);

                return DeSerializer.deserialize(Base64.fromBase64(base64resp));
            }
        });

        return (T) o;
    }

    private static byte[] getBytes(URL resource) throws IOException {
        URLConnection connection = resource.openConnection();
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = input.read();

        while (data != -1) {
            buffer.write(data);
            data = input.read();
        }

        input.close();

        return buffer.toByteArray();
    }

    public synchronized static InsiderREST getRemoteEndpoint() {
        if (remoteEndpoint == null) {
            // create jax-rs client
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(REMOTE_ENDPOINT_URL);
            ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
            remoteEndpoint = rtarget.proxy(InsiderREST.class);
        }
        return remoteEndpoint;
    }
}