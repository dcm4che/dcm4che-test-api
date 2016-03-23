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
 * Portions created by the Initial Developer are Copyright (C) 2013
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
package org.dcm4che.test.clean;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;


/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

public class CleanArchiveEJB implements CleanArchive {

    private static final String[] DELETE_ARR_QUERIES = {
            "DELETE FROM arr_part_obj",
            "DELETE FROM arr_active_part",
            "DELETE FROM arr_audit_record",
            "DELETE FROM arr_code"};

    private static final String[] DELETE_QUERIES = {
            "DELETE FROM study_on_stg_sys",
            "DELETE FROM study_avail_on_system",
            "DELETE FROM rel_instance_location", "DELETE FROM location",
            "DELETE FROM content_item", "DELETE FROM verify_observer",
            "DELETE FROM mpps", "DELETE FROM archiving_task",
            "DELETE FROM sps_station_aet", "DELETE FROM mwl_item",
            "DELETE FROM ext_retrieve_location",
            "DELETE FROM ext_proxy_location",
            "DELETE FROM instance", "DELETE FROM series_query_attrs",
            "DELETE FROM series_req", "DELETE FROM series",
            "DELETE FROM study_query_attrs", "DELETE FROM rel_study_pcode",
            "DELETE FROM study", "DELETE FROM rel_linked_patient_id",
            "DELETE FROM patient_id", "DELETE FROM id_issuer",
            "DELETE FROM patient", "DELETE FROM soundex_code",
            "DELETE FROM person_name", "DELETE FROM instance_history",
            "DELETE FROM series_history", "DELETE FROM study_history",
            "DELETE FROM action_history", "DELETE FROM update_history",
            "DELETE FROM code", "DELETE FROM dicomattrs",
            "DELETE FROM study_update_session"};

    @PersistenceContext(unitName = "dcm4chee-arc-cleanup")
    EntityManager em;

    @Inject
    UserTransaction utx;

    public String clearDB() throws Exception {
        utx.begin();
        try {
            for (String queryStr : DELETE_ARR_QUERIES) {
                Query query = em.createNativeQuery(queryStr);
                query.executeUpdate();
            }
            utx.commit();
        } catch (Throwable ignore) {
            utx.rollback();
        }

        utx.begin();
        try {
            for (String queryStr : DELETE_QUERIES) {
                Query query = em.createNativeQuery(queryStr);
                query.executeUpdate();
            }
            utx.commit();
            return "Successfully Cleaned Database";
        } catch (Exception e) {
            utx.rollback();
            throw e;
        }

    }
}
