/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.admin.migration;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.admin.service.OrganizationController;

import java.util.ArrayList;
import java.util.HashMap;

public class DomainSP2LDAPBatch {
  private AdminController adminController = ServiceProvider.getService(AdminController.class);
  public static final String DOMAIN_SILVERPEAS_ID = "0";

  public OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  public AdminController getAdminController() {
    return adminController;
  }

  public DomainSP2LDAPBatch() {
  }

  /**
   * Process migration
   * @param domainLDAP_Id
   * @return ArrayList of 2 HashMap (list1:users processed, list2: users not processed)
   * @throws Exception
   */
  public ArrayList<HashMap<String, UserDetail>> processMigration(String domainLDAP_Id)
      throws Exception {
    SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()", "root.MSG_ENTER_METHOD");
    AdminController adminController = getAdminController();

    ArrayList<HashMap<String, UserDetail>> returnListLDAPUsers =
        new ArrayList<HashMap<String, UserDetail>>();

    HashMap<String, UserDetail> processedUsers = new HashMap<String, UserDetail>();
    HashMap<String, UserDetail> notProcessedSPUsers = new HashMap<String, UserDetail>();
    HashMap<String, UserDetail> listLDAPUsers = new HashMap<String, UserDetail>();

    try {
      // get all users from ldap
      String[] listLDAPUsersIds = adminController.getUserIdsOfDomain(domainLDAP_Id);
      SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()",
          "Utilisateurs du domaine Silverpeas=" + listLDAPUsersIds.length);
      if (listLDAPUsersIds.length == 0)
        return returnListLDAPUsers;

      for (String listLDAPUsersId : listLDAPUsersIds) {
        UserDetail userDetailLDAP = getOrganisationController().getUserDetail(listLDAPUsersId);
        String keyName =
            (userDetailLDAP.getFirstName() + userDetailLDAP.getLastName()).toLowerCase();
        listLDAPUsers.put(keyName, userDetailLDAP);
      }

      // get all users from domainSilverpeas
      String[] listSilverpeasUsersIds = adminController.getUserIdsOfDomain(DOMAIN_SILVERPEAS_ID);
      boolean processGroups = false;
      for (String listSilverpeasUsersId : listSilverpeasUsersIds) {
        UserDetail userDetail = getOrganisationController().getUserDetail(listSilverpeasUsersId);
        String keyName = (userDetail.getFirstName() + userDetail.getLastName()).toLowerCase();
        // user to migrate
        if (listLDAPUsers.containsKey(keyName)) {
          // Delete ldap user entry
          UserDetail userDetailLDAP = listLDAPUsers.get(keyName);

          adminController.synchronizeRemoveUser(userDetailLDAP.getId());

          // update silverpeas user entry (withe ldap infos)
          userDetail.setSpecificId(userDetailLDAP.getSpecificId());
          userDetail.setDomainId(userDetailLDAP.getDomainId());
          adminController.updateUser(userDetail);
          // Users processed
          processedUsers.put(keyName, userDetail);
          processGroups = true;
        } else {
          notProcessedSPUsers.put(keyName, userDetail);
        }
      }

      if (processGroups) {
        // Move groups from domainSP to mixtDomain
        Group[] groups = getOrganisationController().getAllGroups();
        SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()",
            "DEBUT Migration des groupes du domaine SP vers le domaine mixte...");
        for (Group group : groups) {
          boolean processGroup = false;

          if (DOMAIN_SILVERPEAS_ID.equals(group.getDomainId())) {
            UserDetail[] usersOfGroup = getAdminController().getAllUsersOfGroup(group.getId());
            for (int j = 0; j < usersOfGroup.length && !processGroup; j++) {
              UserDetail userDetail = usersOfGroup[j];
              String userKeyName =
                  (userDetail.getFirstName() + userDetail.getLastName()).toLowerCase();
              if (processedUsers.containsKey(userKeyName)) {
                processGroup = true;
              }
            }

            if (processGroup) {
              int nextId = DBUtil.getNextId("ST_GROUP", "specificId");
              group.setSpecificId(Integer.toString(nextId));
              group.setDomainId(null);
              adminController.updateGroup(group);
              SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()",
                  "- Groupe " + group.getName() + " avec " + group.getUserIds().length +
                  " utilisateurs d&eacute;plac&eacute;s dans le domaine Mixte");
            }
          }
        }
        SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()",
            "FIN Migration des groupes du domaine SP vers le domaine mixte...");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new AdminException("DomainSP2LDAPBatch.processMigration()",
          SilverpeasException.ERROR,
          "Erreur lors de la migration des comptes Silverpeas", e);
    }
    SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()",
        "DEBUT Synchronisation post migration du domaine " + domainLDAP_Id);
    SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()", getAdminController()
        .synchronizeSilverpeasWithDomain(domainLDAP_Id));
    SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()",
        "FIN Synchronisation post migration du domaine " + domainLDAP_Id);
    returnListLDAPUsers.add(processedUsers);
    returnListLDAPUsers.add(notProcessedSPUsers);
    returnListLDAPUsers.add(listLDAPUsers);
    SynchroDomainReport.info("DomainSP2LDAPBatch.processMigration()", "root.MSG_EXIT_METHOD");
    SynchroDomainReport.setReportLevel(null);
    return returnListLDAPUsers;
  }

  /**
   * Get all Domains
   * @return
   * @throws AdminException
   */
  public Domain[] getDomains() throws AdminException {
    return getOrganisationController().getAllDomains();
  }

  public int getNbUsers(String domainId) {
    return getAdminController().getUserIdsOfDomain(domainId).length;
  }
}
