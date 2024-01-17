/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.*;

import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * This class manage one particular group. It is a base class to derive from. The child classes
 * manages the particular method to retrieve the groups' elements(groups or users)
 *
 * @author tleroi
 */
public abstract class AbstractLDAPGroup {

  private static final String LDAPGROUP_GET_GROUPS = "AbstractLDAPGroup.getGroups()";
  private static final String LDAPGROUP_TRANSLATE_GROUPS = "AbstractLDAPGroup.translateGroups()";
  LDAPSettings driverSettings = null;
  LDAPSynchroCache synchroCache = null;
  private StringBuilder synchroReport = null;
  boolean synchroInProcess = false;

  /**
   * Initialize the settings from the read ones
   *
   * @param driverSettings the settings retrieved from the property file
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
  public void beginSynchronization() {
    synchroReport = new StringBuilder();
    synchroInProcess = true;
  }

  /**
   * Called when Admin ends the synchronization
   */
  public String endSynchronization() {
    synchroInProcess = false;
    return synchroReport.toString();
  }

  /**
   * Return all groups
   *
   * @param lds the LDAP connection
   * @return all groups
   * @throws AdminException if an error occur during LDAP operations
   */
  public GroupDetail[] getAllGroups(String lds, String extraFilter) throws AdminException {
    Map<String, GroupDetail> groupsDone = new HashMap<>();

    // Get the root Groups and add them to the current list
    GroupDetail[] groupsVector = getGroups(lds, null, extraFilter);
    List<GroupDetail> groupsCurrent = new ArrayList<>(Arrays.asList(groupsVector));
    // While there is something in the current list
    while (!groupsCurrent.isEmpty()) {
      // Remove one group from the current list
      GroupDetail group = groupsCurrent.remove(groupsCurrent.size() - 1);
      String groupId = group.getSpecificId();
      // If not already treated -> call to retrieve his children
      if (!groupsDone.containsKey(groupId)) {
        // Add the group to the already treated groups
        groupsDone.put(groupId, group);
        // Retrieves his children
        groupsVector = getGroups(lds, groupId, extraFilter);
        groupsCurrent.addAll(Arrays.asList(groupsVector));
      }
    }
    return groupsDone.values().toArray(new GroupDetail[0]);
  }

  /**
   * Return all groups found in the tree that are children of parentId group or return root
   * groups if parentId is null or empty
   *
   * @param lds the LDAP connection
   * @param parentId the parent group Id to start search, if null or empty, root groups are returned
   * @return all founded groups
   * @throws AdminException if an error occur during LDAP operations
   */
  public GroupDetail[] getGroups(String lds, String parentId, String extraFilter) throws AdminException {
    List<GroupDetail> groupsReturned = new ArrayList<>();

    // Only for the same group split into several groups (ie Novell LDAP)
    List<LDAPEntry> groupMerged = new ArrayList<>();
    if (parentId == null) {
      SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
          "Recherche des groupes racine du domaine LDAP distant...");
    } else {
      SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
          "Recherche des groupes fils inclus au groupe " + parentId
              + " du domaine LDAP distant...");
    }
    LDAPEntry[] groupsFounded = getChildGroupsEntry(lds, parentId, extraFilter);

    SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS, "groupsFounded="
        + groupsFounded.length);
    GroupDetail[] groupsProcessed = new GroupDetail[groupsFounded.length];

    int i = -1;
    while (++i < groupsFounded.length) {
      int cpt = i;
      boolean groupSplit = false;
      // if there is a group after the current group
      if (i + 1 < groupsFounded.length) {
        String firstGroupSplit = groupsFounded[i].getDN();
        // if the following group has same name of the current group => this
        // group is split in several groups
        while (i + 1 < groupsFounded.length
            && firstGroupSplit.equals(groupsFounded[++cpt].getDN())) {
          groupSplit = true;
          groupMerged.add(groupsFounded[cpt - 1]);
          i++;
        }
      }
      translateGroup(lds, groupMerged, groupsFounded, groupsProcessed, i, cpt, groupSplit);

      // Add this group to the returned groups
      groupsReturned.add(groupsProcessed[i]);

      reportGroupProcessed(parentId, groupsProcessed[i], i);
    }

    if (parentId == null) {
      SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
          "Récupération de " + groupsFounded.length + " groupes racine du domaine LDAP distant");
    } else {
      SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
          "Récupération de " + groupsFounded.length + " groupes fils du groupe " + parentId);
    }

    return groupsReturned.toArray(new GroupDetail[0]);
  }

  private void reportGroupProcessed(final String parentId, final GroupDetail groupDetail,
      final int i) {
    if (groupDetail != null) {
      SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
          "groupsReturned[" + i + "]" + groupDetail.getSpecificId() + " - " +
              groupDetail.getName());

      String strTypeGroup;
      if (parentId == null) {
        strTypeGroup = "Groupe racine";
      } else {
        strTypeGroup = "Groupe fils";
      }
      if (groupDetail.getUserIds().length != 0) {
        SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
            strTypeGroup + " trouvé no : " + i + ", nom du groupe : " +
                groupDetail.getSpecificId() + ", desc. : " + groupDetail.getDescription() + ". " +
                groupDetail.getUserIds().length + " utilisateur(s) membre(s) associé(s)");
      } else {
        SynchroDomainReport.debug(LDAPGROUP_GET_GROUPS,
            strTypeGroup + " trouvé no : " + i + ", nom du groupe : " +
                groupDetail.getSpecificId() + ", desc. : " + groupDetail.getDescription());
      }
    }
  }

  private void translateGroup(final String lds, final List<LDAPEntry> groupMerged,
      final LDAPEntry[] groupsFounded, final GroupDetail[] groupsProcessed, final int i,
      final int cpt, final boolean groupSplitted) throws AdminException {
    if (groupSplitted) {
      // Merge multiple groups with same name in one
      groupMerged.add(groupsFounded[cpt - 1]);
      // Convert it into GroupDetail
      groupsProcessed[i] = translateGroups(lds, groupMerged);
      groupMerged.clear();
    } else {
      groupsProcessed[i] = translateGroup(lds, groupsFounded[i]);
    }
  }

  /**
   * Return a GroupDetail object filled with the infos of the group having ID = id NOTE : the
   * DomainID and
   * the ID are not set. THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds the LDAP connection
   * @param id the group id (most case : LDAP DN)
   * @return the group object
   * @throws AdminException if an error occur during LDAP operations or if the group is not found
   */
  public GroupDetail getGroup(String lds, String id) throws AdminException {
    return getGroupDetail(lds, id, this::getGroupEntry);
  }

  public GroupDetail getGroupByName(String lds, String name) throws AdminException {
    return getGroupDetail(lds, name, this::getGroupEntryByName);
  }

  private GroupDetail getGroupDetail(final String ldap, final String group,
      final LdapGroupSupplier ldapEntrySupplier) throws AdminException {
    LDAPEntry theEntry = null;
    try {
      theEntry = ldapEntrySupplier.get(ldap, group);
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverLogger.getLogger(this).warn(e.getMessage());
        synchroReport.append("PB getting GroupDetail : ").append(group).append("\n");
      } else {
        throw e;
      }
    }
    return translateGroup(ldap, theEntry);
  }

  /**
   * Translate a group LDAPEntry into a GroupDetail object NOTE : the GroupParentId, the DomainID
   * and the
   * ID are not set. THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param groupEntry the LDAP group object
   * @return the group object
   * @throws AdminException if an error occur during LDAP operations or if there is no groupEntry
   * object
   */
  protected GroupDetail translateGroup(String lds, LDAPEntry groupEntry) throws AdminException {
    GroupDetail groupInfos = new GroupDetail();

    if (groupEntry == null) {
      throw new AdminException(undefined("LDAP group entry"));
    }

    // We don't set : GroupParentID, DomainId and Id...
    // ------------------------------------------------
    fillGroupDetail(groupInfos, groupEntry);
    try {
      groupInfos.setUserIds(getUserIds(lds, groupEntry));
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverLogger.getLogger(this).warn(e.getMessage());
        synchroReport.append("PB getting GroupDetail's childs : ").append(groupInfos.getName()).append(
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
   * Translate several groups LDAPEntry into a GroupDetail object NOTE : the GroupParentId, the
   * DomainID
   * and the ID are not set. THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds the ldap service
   * @param groupEntries the group entries.
   * @return the group of users.
   * @throws AdminException if an error occurs.
   */
  protected GroupDetail translateGroups(String lds, List<LDAPEntry> groupEntries)
      throws AdminException {
    GroupDetail groupInfos = new GroupDetail();
    ArrayList<String> allUserIds = new ArrayList<>();

    if (groupEntries.isEmpty()) {
      throw new AdminException(undefined("LDAP group entries"));
    }

    boolean first = true;
    for (LDAPEntry groupEntry : groupEntries) {
      if (first) {
        fillGroupDetail(groupInfos, groupEntry);
        first = false;
      }
      try {
        String[] userIds = getUserIds(lds, groupEntry);
        SynchroDomainReport.info(LDAPGROUP_TRANSLATE_GROUPS,
            "Users in group: " + userIds.length);
        Collections.addAll(allUserIds, userIds);
      } catch (AdminException e) {
        if (synchroInProcess) {
          SilverLogger.getLogger(this).warn(e.getMessage());
          synchroReport.append("PB getting GroupDetail's children: ").append(groupInfos.getName()).append(
              "\n");
          SynchroDomainReport.error(LDAPGROUP_TRANSLATE_GROUPS,
              "Pb de récupération des membres utilisateurs du groupe "
                  + groupInfos.getSpecificId(), e);
        } else {
          throw e;
        }
      }
    }
    groupInfos.setUserIds(allUserIds.toArray(new String[0]));
    SynchroDomainReport.debug(LDAPGROUP_TRANSLATE_GROUPS,
        "Users in merged GroupDetail: " + groupInfos.getNbUsers());
    return groupInfos;
  }

  private void fillGroupDetail(final GroupDetail groupInfos, final LDAPEntry groupEntry) {
    groupInfos.setSpecificId(
        LDAPUtility.getFirstAttributeValue(groupEntry, driverSettings.getGroupsIdField()));
    groupInfos.setName(
        LDAPUtility.getFirstAttributeValue(groupEntry, driverSettings.getGroupsNameField()));
    groupInfos.setDescription(
        LDAPUtility.getFirstAttributeValue(groupEntry, driverSettings.getGroupsDescriptionField()));
  }

  /**
   * Gets the group's parent groups IDs THIS FUNCTION ALWAYS THROW EXCEPTION (EVEN IF A SYNCHRO IS
   * RUNNING)
   *
   * @param groupId the group's Id
   * @return the groups that contain the group
   * @throws AdminException if an error occurs
   */
  public abstract String[] getGroupMemberGroupIds(String lds, String groupId)
      throws AdminException;

  /**
   * Gets the users groups IDs THIS FUNCTION ALWAYS THROW EXCEPTION (EVEN IF A SYNCHRO IS RUNNING)
   *
   * @param userId the user's Id
   * @return the groups that contain the user
   * @throws AdminException if an error occurs.
   */
  public abstract String[] getUserMemberGroupIds(String lds, String userId)
      throws AdminException;

  /**
   * Gets the users ID that are directly in the group describes by groupEntry THIS FUNCTION ALWAYS
   * THROW EXCEPTION (EVEN IF A SYNCHRO IS RUNNING)
   *
   * @param groupEntry the group that contains users
   * @return the father's group ID or empty string if the group is at the root level
   * @throws AdminException if an error occurs.
   */
  protected abstract String[] getUserIds(String lds, LDAPEntry groupEntry)
      throws AdminException;

  /**
   * Gets a set of LDAP entries that are the child groups of a parent one THIS FUNCTION THROW
   * EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds the LDAP connection
   * @param parentId Id of the parent group
   * @return all founded child groups or root groups if parentId is equal to null or is empty
   * @throws AdminException if an error occur during LDAP operations
   */
  protected abstract LDAPEntry[] getChildGroupsEntry(String lds,
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

  protected LDAPEntry getMemberEntry(final String lds, final String memberId, final boolean isGroup)
      throws AdminException {
    final LDAPEntry memberEntry;
    if (isGroup) {
      memberEntry =
          LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
              driverSettings.getScope(), driverSettings.getGroupsIdFilter(memberId),
              driverSettings.getGroupAttributes());
    } else {
      memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
          driverSettings.getScope(), driverSettings.getUsersIdFilter(memberId),
          driverSettings.getGroupAttributes());
    }
    if (memberEntry == null) {
      throw new AdminException(unknown("LDAP group id", memberId));
    }
    return memberEntry;
  }

  protected String getSpecificId(final String lds, final LDAPEntry theEntry) throws AdminException {
    if (theEntry != null) {
      String userSpecificId =
          LDAPUtility.getFirstAttributeValue(theEntry, driverSettings.getUsersIdField());
      // Verify that the user exist in the scope
      if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
          driverSettings.getScope(), driverSettings.getUsersIdFilter(userSpecificId),
          driverSettings.getGroupAttributes()) != null) {
        return userSpecificId;
      }
    }
    return null;
  }

  @FunctionalInterface
  private interface LdapGroupSupplier {

    LDAPEntry get(final String ldap, final String group) throws AdminException;
  }
}