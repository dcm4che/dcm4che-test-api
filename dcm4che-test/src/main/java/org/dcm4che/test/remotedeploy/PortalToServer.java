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

    /**
     * Creates a proxy of type <code>insiderInterface</code> that will do the following when one of its methods is called:
     * <ul>
     *     <li>Collect all the bytecodes of the <code>insiderClass</code> itself and any inner classes</li>
     *     <li>Send them to the server, along with the info of which method has been called</li>
     *     <li>On the server,  inside the main EAR of the archive:<ul>
     *
     *         <li>Feed the received bytecodes to a classloader</li>
     *         <li>Create a bean of class <code>insiderClass</code></li>
     *         <li>Run CDI injection upon this bean</li>
     *         <li>Execute the method, which the user called on the proxy, upon the newly created bean on the server</li>
     *         <li>Serialize the response, respond with it to the caller</li>
     *     </ul></li>
     *     <li> Return the received response as a return value of the proxy/
     *          throw an exception that was thrown during execution of the class method
     *     </li>
     *
     * </ul>

     * @param insiderInterface An interface that is used to create a proxy. It should contain the method that you would like to run on the server.
     * @param insiderClass A class that will be executed on the server. The implementation can use any injections/resources that are available for the classes in the EAR.
     *                     The class must not reference any other classes that are not expected to be accessible in the deployment already.
     *                     Anonymous classes are not allowed for now.
     *                     Inner classes (1 lvl) are allowed.
     * @param warpInterface
     * @return A proxy that allows to execute <code>insiderClass</code>'s methods on the server
     */
    public static <T> T warp(final Class<T> insiderInterface, final Class<? extends T> insiderClass, final boolean warpInterface) {

        Object o = Proxy.newProxyInstance(insiderInterface.getClassLoader(), new Class[]{insiderInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                RemoteRequestJSON requestJSON = new RemoteRequestJSON();

                requestJSON.methodName = method.getName();
                requestJSON.mainClassName = insiderClass.getName();
                requestJSON.args = Base64.toBase64(DeSerializer.serialize(args));
                requestJSON.classes = new HashMap<String, String>();

                String insiderClassResourceName = getClassResourceName(insiderClass);
                URL insiderClassResource = insiderClass.getResource(insiderClassResourceName);
                requestJSON.classes.put(insiderClass.getName(), Base64.toBase64(getBytes(insiderClassResource)));
                
                if (warpInterface) {
                    String insiderInterfaceResourceName = getClassResourceName(insiderInterface);
                    URL insiderInterfaceResource = insiderInterface.getResource(insiderInterfaceResourceName);
                    requestJSON.classes.put(insiderInterface.getName(), Base64.toBase64(getBytes(insiderInterfaceResource)));
                }

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

                Object returned = DeSerializer.deserialize(Base64.fromBase64(base64resp));

                if (returned instanceof Exception)
                    throw (Throwable) returned;

                return returned;
            }
        });

        return (T) o;
    }
    
    public static <T> T warp(final Class<T> insiderInterface, final Class<? extends T> insiderClass) {
        return warp(insiderInterface, insiderClass, false);
    }
    
    private static String getClassResourceName(Class<?> clazz) {
        StringBuffer classResourceName = new StringBuffer();
        Class<?> declaringClass = clazz.getDeclaringClass();
        if(declaringClass != null) {
            classResourceName.append(declaringClass.getSimpleName()).append("$");
        }
        
        classResourceName.append(clazz.getSimpleName()).append(".class");
        
        return classResourceName.toString();
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

    private synchronized static InsiderREST getRemoteEndpoint() {
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