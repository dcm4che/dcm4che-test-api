/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che.test.remote;

import org.dcm4che3.net.Device;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class WaprUnitSampleTest {


    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    Device d;


    @Test
    @Ignore
    public void testWarp() throws Exception {

        WarpGate gate = WarpUnit.createGate(WarpUnit.makeURL("10.231.162.21", "8080"), WaprUnitSampleTest.class);

        String closure = "BOOO!";

        String entity = "CODE";

        // this stuff is run on the server
        String res = gate.warp(() -> {

            System.out.println("I'm inside! and I can see stuff here: e.g. the device name is " + d.getDeviceName());
            System.out.println("Guess what, I am able to see the closure from the client: " + closure);


            System.out.println("I can do lots of stuff! Let's see how many of '" + entity + "' we have: " + em.createNativeQuery("select count(*) from " + entity).getSingleResult());

            return "And you can see this ret value in the client!";

        });


        gate.warp(() -> System.out.println(d.getDeviceName()));


    }
}
