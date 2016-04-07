/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manage one particular group. It is a base class to derive from. The child classes
 * manages the particular method to retreive the groups' elements(groups or users)
 *
 * @author tleroi
 */
abstract public class AbstractLDAPGroup {

  LDAPSettings driverSettings = null;
  LDAPSynchroCache synchroCache = null;
  private StringBuffer synchroReport = null;
  boolean synchroInProcess = false;

  /**
   * Initialize the settings from the read ones
   *
   * @param driverSettings the settings retreived from the property file
   */
  public void init(LDAPSettings driverSettings, LDAPSynchroCache synchroCache) {
    this.driverSettings = driverSettings;
    this.synchroCache = synchroCache;
  }

  AbstractLDAPGroup append(CharSequence message) {
    synchroReport.append(message);
    return this;
  }

  /**
   * Called when Admin starts the synchronization
   */
  public void beginSynchronization() throws Exception {
    synchroReport = new StringBuffer();
    synchroInProcess = true;
  }

  /**
   * Called when Admin ends the synchronization
   */
  public String endSynchronization() throws Exception {
    synchroInProcess = false;
    return synchroReport.toString();
  }

  public Group[] getAllChangedGroups(String lds, String extraFilter)
      throws AdminException {
    return getAllGroups(lds, extraFilter);
  }

  /**
   * Return all groups
   *
   * @param lds the LDAP connection
   * @return all groups
   * @throws AdminException if an error occur during LDAP operations
   */
  public Group[] getAllGroups(String lds, String extraFilter) throws AdminException {
    Map<String, Group> groupsDone = new HashMap<>();
    List<Group> groupsCurrent = new ArrayList<>();

    // Get the root Groups and add them to the current list
    Group[] groupsVector = getGroups(lds, null, extraFilter);
    groupsCurrent.addAll(Arrays.asList(groupsVector));
    // While there is something in the current list
    while (groupsCurrent.size() > 0) {
      // Remove one group from the current list
      Group group = groupsCurrent.remove(groupsCurrent.size() - 1);
      String groupId = group.getSpecificId();
      // If not already treated -> call to retreive his childs
      if (groupsDone.get(groupId) == null) {
        // Add the group to the already treated groups
        groupsDone.put(groupId, group);
        // Retreives his childs
        groupsVector = getGroups(lds, groupId, extraFilter);
        groupsCurrent.addAll(Arrays.asList(groupsVector));
      }
    }
    return groupsDone.values().toArray(new Group[groupsDone.size()]);
  }

  /**
   * Return all groups found in the tree that are childs of parentId group or return root groups if
   * parentId is null or empty
   *
   * @param lds the LDAP connection
   * @param parentId the parent group Id to start search, if null or empty, root groups are returned
   * @return all founded groups
   * @throws AdminException if an error occur during LDAP operations
   */
  public Group[] getGroups(String lds, String parentId, String extraFilter) throws AdminException {
    List<Group> groupsReturned = new ArrayList<>();

    // Only for the same group splitted into several groups (ie Novell LDAP)
    List<LDAPEntry> groupMerged = new ArrayList<>();

    int i;
    if (parentId == null) {
      SynchroDomainReport.info("AbstractLDAPGroup.getGroups()",
          "Recherche des groupes racine du domaine LDAP distant...");
    } else {
      SynchroDomainReport.info("AbstractLDAPGroup.getGroups()",
          "Recherche des groupes fils inclus au groupe " + parentId
          + " du domaine LDAP distant...");
    }
    LDAPEntry[] groupsFounded = getChildGroupsEntry(lds, parentId, extraFilter);

    SynchroDomainReport.info("AbstractLDAPGroup.getGroups()", "groupsFounded="
        + groupsFounded.length);
    Group[] groupsProcessed = new Group[groupsFounded.length];

    for (i = 0; i < groupsFounded.length; i++) {
      int cpt = i;
      boolean groupSplitted = false;
      // if there is a group after the current group
      if (i + 1 < groupsFounded.length) {
        String firstGroupSplitted = groupsFounded[i].getDN();
        // if the following group has same name of the current group => this
        // group is splitted in several groups
        while (i + 1 < groupsFounded.length
            && firstGroupSplitted.equals(groupsFounded[++cpt].getDN())) {
          groupSplitted = true;
          groupMerged.add(groupsFounded[cpt - 1]);
          i++;
        }
      }
      if (groupSplitted) {
        // Merge multiple groups with same name in one
        groupMerged.add(groupsFounded[cpt - 1]);
        // Convert it into Group
        groupsProcessed[i] = translateGroups(lds, groupMerged);
        groupMerged.clear();
      } else {
        groupsProcessed[i] = translateGroup(lds, groupsFounded[i]);
      }

      // Add this group to the returned groups
      groupsReturned.add(groupsProcessed[i]);

      if (groupsProcessed[i] != null) {
        SynchroDomainReport.warn("AbstractLDAPGroup.getGroups()",
            "groupsReturned[i]" + groupsProcessed[i].getId() + " - "
            + groupsProcessed[i].getName());
      }

      String StrTypeGroup;
      if (parentId == null) {
        StrTypeGroup = "Groupe racine";
      } else {
        StrTypeGroup = "Groupe fils";
      }
      if (groupsProcessed[i].getUserIds().length != 0) {
        SynchroDomainReport.debug("AbstractLDAPGroup.getGroups()", StrTypeGroup
            + " trouvé no : " + Integer.toString(i) + ", nom du groupe : "
            + groupsProcessed[i].getSpecificId() + ", desc. : "
            + groupsProcessed[i].getDescription() + ". "
            + groupsProcessed[i].getUserIds().length
            + " utilisateur(s) membre(s) associé(s)");
      } else {
        SynchroDomainReport.debug("AbstractLDAPGroup.getGroups()", StrTypeGroup
            + " trouvé no : " + Integer.toString(i) + ", nom du groupe : "
            + groupsProcessed[i].getSpecificId() + ", desc. : "
            + groupsProcessed[i].getDescription());
      }

      groupsProcessed[i].traceGroup();
    }

    if (parentId == null) {
      SynchroDomainReport.info("AbstractLDAPGroup.getGroups()", "Récupération de "
          + groupsFounded.length + " groupes racine du domaine LDAP distant");
    } else {
      SynchroDomainReport.info("AbstractLDAPGroup.getGroups()", "Récupération de "
          + groupsFounded.length + " groupes fils du groupe " + parentId);
    }

    return groupsReturned.toArray(new Group[groupsReturned.size()]);
  }

  /**
   * Return a Group object filled with the infos of the group having ID = id NOTE : the DomainID and
   * the ID are not set. THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds the LDAP connection
   * @param id the group id (most case : LDAP DN)
   * @return the group object
   * @throws AdminException if an error occur during LDAP operations or if the group is not found
   */
  public Group getGroup(String lds, String id) throws AdminException {
    LDAPEntry theEntry = null;
    try {
      theEntry = getGroupEntry(lds, id);
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverTrace.warn("admin", "AbstractLDAPGroup.getGroup",
            "admin.EX_ERR_GET_GROUP", "GroupId=" + id, e);
        synchroReport.append("PB getting Group : ").append(id).append("\n");
      } else {
        throw e;
      }
    }
    return translateGroup(lds, theEntry);
  }

  public Group getGroupByName(String lds, String name) throws AdminException {
    LDAPEntry theEntry = null;
    try {
      theEntry = getGroupEntryByName(lds, name);
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverTrace.warn("admin", "AbstractLDAPGroup.getGroupByName",
            "admin.EX_ERR_GET_GROUP", "GroupId=" + name, e);
        synchroReport.append("PB getting Group : ").append(name).append("\n");
      } else {
        throw e;
      }
    }
    return translateGroup(lds, theEntry);
  }

  public AbstractLDAPTimeStamp getMaxTimeStamp(String lds, String minTimeStamp)
      throws AdminException {
    AbstractLDAPTimeStamp theTimeStamp = driverSettings.newLDAPTimeStamp(minTimeStamp);
    theTimeStamp.initFromServer(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.
        getGroupsFullFilter(),
        driverSettings.getGroupsNameField());
    return theTimeStamp;
  }

  /**
   * Translate a group LDAPEntry into a Group object NOTE : the GroupParentId, the DomainID and the
   * ID are not set. THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param groupEntry the LDAP group object
   * @return the group object
   * @throws AdminException if an error occur during LDAP operations or if there is no groupEntry
   * object
   */
  protected Group translateGroup(String lds, LDAPEntry groupEntry) throws AdminException {
    Group groupInfos = new Group();

    if (groupEntry == null) {
      throw new AdminException("AbstractLDAPGroup.translateGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GROUP_ENTRY_ISNULL");
    }

    // We don't set : GroupParentID, DomainId and Id...
    // ------------------------------------------------
    groupInfos.setSpecificId(LDAPUtility.getFirstAttributeValue(groupEntry,
        driverSettings.getGroupsIdField()));
    groupInfos.setName(LDAPUtility.getFirstAttributeValue(groupEntry,
        driverSettings.getGroupsNameField()));
    groupInfos.setDescription(LDAPUtility.getFirstAttributeValue(groupEntry,
        driverSettings.getGroupsDescriptionField()));
    try {
      groupInfos.setUserIds(getUserIds(lds, groupEntry));
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverTrace.warn("admin", "AbstractLDAPGroup.translateGroup",
            "admin.EX_ERR_CHILD_USERS", "Group=" + groupInfos.getName(), e);
        synchroReport.append("PB getting Group's childs : ").append(groupInfos.getName()).append(
            "\n");
        SynchroDomainReport.error("AbstractLDAPGroup.translateGroup()",
            "Pb de récupération des membres utilisateurs du groupe "
            + groupInfos.getSpecificId(), e);
      } else {
        throw e;
      }
    }
    return groupInfos;
  }

  /**
   * Translate several groups LDAPEntry into a Group object NOTE : the GroupParentId, the DomainID
   * and the ID are not set. THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds
   * @param groupEntries
   * @return
   * @throws AdminException
   */
  protected Group translateGroups(String lds, List<LDAPEntry> groupEntries)
      throws AdminException {
    Group groupInfos = new Group();
    ArrayList<String> allUserIds = new ArrayList<>();

    if (groupEntries.isEmpty()) {
      throw new AdminException("AbstractLDAPGroup.translateGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GROUP_ENTRY_ISNULL");
    }

    boolean first = true;
    for (LDAPEntry groupEntry : groupEntries) {
      if (first) {
        groupInfos.setSpecificId(LDAPUtility.getFirstAttributeValue(groupEntry,
            driverSettings.getGroupsIdField()));
        groupInfos.setName(LDAPUtility.getFirstAttributeValue(groupEntry,
            driverSettings.getGroupsNameField()));
        groupInfos.setDescription(LDAPUtility.getFirstAttributeValue(
            groupEntry, driverSettings.getGroupsDescriptionField()));
        first = false;
      }
      try {
        String[] userIds = getUserIds(lds, groupEntry);
        SynchroDomainReport.warn("AbstractLDAPGroup.translateGroups()",
            "Users in group: " + userIds.length);
        Collections.addAll(allUserIds, userIds);
      } catch (AdminException e) {
        if (synchroInProcess) {
          SilverTrace.warn("admin", "AbstractLDAPGroup.translateGroups",
              "admin.EX_ERR_CHILD_USERS", "Group=" + groupInfos.getName(), e);
          synchroReport.append("PB getting Group's childs : ").append(groupInfos.getName()).append(
              "\n");
          SynchroDomainReport.error("AbstractLDAPGroup.translateGroups()",
              "Pb de récupération des membres utilisateurs du groupe "
              + groupInfos.getSpecificId(), e);
        } else {
          throw e;
        }
      }
    }
    groupInfos.setUserIds(allUserIds.toArray(new String[allUserIds.size()]));
    SynchroDomainReport.warn("AbstractLDAPGroup.translateGroups()",
        "Users in merged Group: " + groupInfos.getNbUsers());
    return groupInfos;
  }

  /**
   * return the group's parent groups IDs THIS FUNCTION ALWAYS THROW EXCEPTION (EVEN IF A SYNCHRO IS
   * RUNNING)
   *
   * @param groupId the group's Id
   * @return the groups that contain the group
   * @throws AdminException
   */
  abstract public String[] getGroupMemberGroupIds(String lds, String groupId)
      throws AdminException;

  /**
   * return the users groups IDs THIS FUNCTION ALWAYS THROW EXCEPTION (EVEN IF A SYNCHRO IS RUNNING)
   *
   * @param userId the user's Id
   * @return the groups that contain the user
   * @throws AdminException
   */
  abstract public String[] getUserMemberGroupIds(String lds, String userId)
      throws AdminException;

  /**
   * return the users ID that are directly in the group discribes by groupEntry THIS FUNCTION ALWAYS
   * THROW EXCEPTION (EVEN IF A SYNCHRO IS RUNNING)
   *
   * @param groupEntry the group that contains users
   * @return the father's group ID or empty string if the group is at the root level
   * @throws AdminException
   */
  abstract protected String[] getUserIds(String lds, LDAPEntry groupEntry)
      throws AdminException;

  /**
   * Return a set of LDAP entries that are the child groups of a parent one THIS FUNCTION THROW
   * EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds the LDAP connection
   * @param parentId Id of the parent group
   * @return all founded child groups or root groups if parentId is equal to null or is empty
   * @throws AdminException if an error occur during LDAP operations
   */
  abstract protected LDAPEntry[] getChildGroupsEntry(String lds,
      String parentId, String extraFilter) throws AdminException;

  /**
   * Return the LDAP entry of the specified group Id THIS FUNCTION ALWAYS THROW EXCEPTION (EVEN IF A
   * SYNCHRO IS RUNNING)
   *
   * @param lds the LDAP connection
   * @param groupId group's Id
   * @return group's entry
   * @throws AdminException if an error occur during LDAP operations
   */
  protected LDAPEntry getGroupEntry(String lds, String groupId)
      throws AdminException {

    return LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
        driverSettings.getScope(),
        driverSettings.getGroupsIdFilter(groupId), driverSettings.getGroupAttributes());
  }

  protected LDAPEntry getGroupEntryByName(String lds, String groupName)
      throws AdminException {

    return LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
        driverSettings.getScope(),
        driverSettings.getGroupsNameFilter(groupName), driverSettings.getGroupAttributes());
  }
}