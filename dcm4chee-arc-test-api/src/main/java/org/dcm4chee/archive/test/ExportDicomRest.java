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

package org.dcm4chee.archive.test;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Restful Wrapper for ExportDicom API. Currently only used for testing.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@Path("/exportDicom")
public interface ExportDicomRest {

    /**
     * Sends all instances for each of the studies to the destination AE.
     *
     * @param destinationAET    destinatione AE title
     * @param studyInstanceUIDs list of Study Instance UIDs
     */
    @POST
    @Path("studies")
    void exportStudies(@FormParam("destinationAET") String destinationAET,
                       @FormParam("studyUID") List<String> studyInstanceUIDs);

    /**
     * Sends the instances to a destination AE.
     *
     * @param destinationAET destination AE title
     * @param instanceUIDs   list of SOP Instance UIDs
     */
    @POST
    @Path("instances")
    void exportInstances(@FormParam("destinationAET") String destinationAET,
                         @FormParam("instanceUID") List<String> instanceUIDs);

    /**
     * Sends the instances to a destination AE.
     *
     * @param destinationAET destination AE title
     * @param studyInstanceUIDs list of Study Instance UIDs
     * @param keyObjectDocumentCodes codes (including designator, and optionally version) of key object documents
     */
    @POST
    @Path("keyImages")
    void exportKeyImages(@FormParam("destinationAET")String destinationAET,
                         @FormParam("studyUID") List<String> studyInstanceUIDs,
                         @FormParam("keyObjectCodes") List<String> keyObjectDocumentCodes);
}
