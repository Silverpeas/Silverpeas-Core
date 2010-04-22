/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.organization.GroupRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class GroupManager {
  /**
   * Constructor
   */
  public GroupManager() {
  }

  /**
   * Add a user to a group
   */
  public void addUserInGroup(DomainDriverManager ddManager, String sUserId,
      String sGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.organization.group.addUserInGroup(idAsInt(sUserId),
          idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException("GroupManager.removeUserFromGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS",
          "User Id: '" + sUserId + "' GroupId = '" + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Remove a user from a group
   */
  public void removeUserFromGroup(DomainDriverManager ddManager,
      String sUserId, String sGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.organization.group.removeUserFromGroup(idAsInt(sUserId),
          idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException("GroupManager.removeUserFromGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS",
          "User Id: '" + sUserId + "' GroupId = '" + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the direct groups id containing a user
   */
  public String[] getDirectGroupsOfUser(DomainDriverManager ddManager,
      String sUserId) throws AdminException {
    try {
      String[] groups;

      ddManager.getOrganizationSchema();
      GroupRow[] grs = ddManager.organization.group
          .getDirectGroupsOfUser(idAsInt(sUserId));
      // Convert GroupRow objects in Group Object
      groups = new String[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = idAsString(grs[nI].id);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getDirectGroupsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS",
          "User Id: '" + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the Silverpeas group id of group qualified by given specific Id and domain id
   */
  public String getGroupIdBySpecificIdAndDomainId(
      DomainDriverManager ddManager, String sSpecificId, String sDomainId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow gr = ddManager.organization.group.getGroupBySpecificId(
          idAsInt(sDomainId), sSpecificId);
      return idAsString(gr.id);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupIdBySpecificIdAndDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP",
          "group specific Id: '" + sSpecificId + "', domain Id: '" + sDomainId
          + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the groups id available in Silverpeas
   */
  public String[] getAllGroupIds(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.organization.group.getAllGroupIds();

      if (asGroupIds != null)
        return asGroupIds;
      else
        return new String[0];
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllGroupIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the root groups id available in Silverpeas
   */
  public String[] getAllRootGroupIds(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.organization.group.getAllRootGroupIds();

      if (asGroupIds != null)
        return asGroupIds;
      else
        return new String[0];
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllRootGroupIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ROOT_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the direct sub groups id of a given group
   */
  public String[] getAllSubGroupIds(DomainDriverManager ddManager,
      String superGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.organization.group
          .getDirectSubGroupIds(idAsInt(superGroupId));

      if (asGroupIds != null)
        return asGroupIds;
      else
        return new String[0];
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllSubGroupIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_CHILDREN_GROUP_IDS",
          "father group Id: '" + superGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the path from root to a given group
   */
  public List<String> getPathToGroup(DomainDriverManager ddManager, String groupId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      List<String> path = new ArrayList<String>();

      GroupRow superGroup = ddManager.organization.group
          .getSuperGroup(idAsInt(groupId));
      while (superGroup != null) {
        path.add(0, idAsString(superGroup.id));
        superGroup = ddManager.organization.group.getSuperGroup(superGroup.id);
      }

      return path;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getPathToGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PATH_TO_GROUP",
          "groupId = " + groupId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Check if the given group exists
   * @return true if a group with the given name
   */
  public boolean isGroupExist(DomainDriverManager ddManager, String sName)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // build GroupRow to search
      GroupRow searchedGroup = new GroupRow();
      searchedGroup.specificId = null;
      searchedGroup.name = sName;
      searchedGroup.description = null;

      // search for group
      GroupRow[] group = ddManager.organization.group
          .getAllMatchingGroups(searchedGroup);

      return (group.length > 0);
    } catch (Exception e) {
      throw new AdminException("GroupManager.isGroupExist",
          SilverpeasException.ERROR, "admin.EX_ERR_IS_GROUP_EXIST",
          "group name: '" + sName + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get group information with the given id from Silverpeas
   */
  public Group getGroup(DomainDriverManager ddManager, String sGroupId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow gr = ddManager.organization.group.getGroup(idAsInt(sGroupId));

      Group group = new Group();

      if (gr != null) {
        group.setId(idAsString(gr.id));
        group.setSpecificId(gr.specificId);
        group.setDomainId(idAsString(gr.domainId));
        group.setSuperGroupId(idAsString(gr.superGroupId));
        group.setName(gr.name);
        group.setDescription(gr.description);
        group.setRule(gr.rule);
      }

      // Get the father id
      /*
       * gr = ddManager.organization.group.getSuperGroup(idAsInt(sGroupId)); if(gr != null)
       * group.setSuperGroupId(idAsString(gr.id));
       */

      // Get the selected users for this group
      String[] asUsersId = ddManager.organization.user
          .getDirectUserIdsOfGroup(idAsInt(sGroupId));
      if (asUsersId != null)
        group.setUserIds(asUsersId);

      return group;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", "group Id: '"
          + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the sub groups id of a given group
   */
  public String[] getAllSubGroupIdsRecursively(DomainDriverManager ddManager,
      String superGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.organization.group
          .getAllSubGroupIds(idAsInt(superGroupId));

      if (asGroupIds != null)
        return asGroupIds;
      else
        return new String[0];
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllSubGroupIdsRecursively",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_CHILDREN_GROUP_IDS",
          "father group Id: '" + superGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get group information with the given group name
   */
  public Group getGroupByNameInDomain(DomainDriverManager ddManager,
      String sGroupName, String sDomainFatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      Group group = ddManager.getGroupByNameInDomain(sGroupName,
          sDomainFatherId);

      if (group != null) {
        String specificId = group.getSpecificId();
        GroupRow gr = ddManager.organization.group.getGroupBySpecificId(
            idAsInt(sDomainFatherId), specificId);
        if (gr != null) {
          group.setId(idAsString(gr.id));

          // Get the selected users for this group
          String[] asUsersId = ddManager.organization.user
              .getDirectUserIdsOfGroup(gr.id);
          if (asUsersId != null)
            group.setUserIds(asUsersId);
        } else
          return null;
      }

      return group;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupByNameInDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", "group Name: '"
          + sGroupName + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public Group[] getRootGroupsOfDomain(DomainDriverManager ddManager,
      String sDomainId) throws AdminException {
    GroupRow[] grs = null;
    Group[] groups = null;
    String[] asUsersId = null;

    try {
      // Get organization
      ddManager.getOrganizationSchema();

      // Get groups of domain from Silverpeas database
      grs = ddManager.organization.group
          .getAllRootGroupsOfDomain(idAsInt(sDomainId));

      // Convert GroupRow objects in Group Object
      groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = GroupRow2Group(grs[nI]);
        // Get the selected users for this group
        asUsersId = ddManager.organization.user
            .getDirectUserIdsOfGroup(idAsInt(groups[nI].getId()));
        if (asUsersId != null)
          groups[nI].setUserIds(asUsersId);
      }

      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
          "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public Group[] getSynchronizedGroups(DomainDriverManager ddManager)
      throws AdminException {
    GroupRow[] grs = null;
    Group[] groups = null;

    try {
      // Get organization
      ddManager.getOrganizationSchema();

      // Get groups of domain from Silverpeas database
      grs = ddManager.organization.group.getSynchronizedGroups();

      // Convert GroupRow objects in Group Object
      groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = GroupRow2Group(grs[nI]);
      }

      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] getRootGroupIdsOfDomain(DomainDriverManager ddManager,
      String sDomainId) throws AdminException {
    String[] groupIds = null;

    try {
      // Get organization
      ddManager.getOrganizationSchema();

      // Get groups of domain from Silverpeas database
      groupIds = ddManager.organization.group
          .getAllRootGroupIdsOfDomain(idAsInt(sDomainId));

      if (groupIds != null)
        return groupIds;
      else
        return new String[0];
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
          "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the groups of domain
   */
  public Group[] getGroupsOfDomain(DomainDriverManager ddManager,
      String sDomainId) throws AdminException {
    GroupRow[] grs = null;
    Group[] groups = null;

    try {
      // Get organization
      ddManager.getOrganizationSchema();

      SynchroReport.info("GroupManager.getGroupsOfDomain()",
          "Recherche des groupes du domaine LDAP dans la base...", null);
      // Get groups of domain from Silverpeas database
      grs = ddManager.organization.group
          .getAllGroupsOfDomain(idAsInt(sDomainId));

      // Convert GroupRow objects in Group Object
      groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = GroupRow2Group(grs[nI]);
        SynchroReport.debug("GroupManager.getGroupsOfDomain()",
            "Groupe trouvé no : " + Integer.toString(nI) + ", specificID : "
            + groups[nI].getSpecificId() + ", desc. : "
            + groups[nI].getDescription(), null);
      }
      SynchroReport.info("GroupManager.getGroupsOfDomain()",
          "Récupération de " + grs.length
          + " groupes du domaine LDAP dans la base", null);
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
          "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] searchGroupsIds(DomainDriverManager ddManager,
      boolean isRootGroup, String componentId, String[] aProfileId,
      Group modelGroup) throws AdminException {
    String[] grids = null;
    GroupRow model;
    int[] aRoleId = null;

    try {
      // Get organization
      ddManager.getOrganizationSchema();
      model = Group2GroupRow(modelGroup);
      // The Ids could be equal to -1 !!!! Put it to -2 if null
      if ((modelGroup.getId() == null) || (modelGroup.getId().length() <= 0)) {
        model.id = -2;
      }
      if ((modelGroup.getDomainId() == null)
          || (modelGroup.getDomainId().length() <= 0)) {
        model.domainId = -2;
      }
      if ((modelGroup.getSuperGroupId() == null)
          || (modelGroup.getSuperGroupId().length() <= 0)) {
        model.superGroupId = -2;
      }
      if (aProfileId != null) {
        aRoleId = new int[aProfileId.length];
        for (int i = 0; i < aProfileId.length; i++) {
          aRoleId[i] = idAsInt(aProfileId[i]);
        }
      }

      // Get groups
      grids = ddManager.organization.group.searchGroupsIds(isRootGroup,
          idAsInt(componentId), aRoleId, model);
      return grids;
    } catch (Exception e) {
      throw new AdminException("GroupManager.searchGroupsIdsInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public Group[] searchGroups(DomainDriverManager ddManager, Group modelGroup,
      boolean isAnd) throws AdminException {
    GroupRow[] grs = null;
    Group[] groups = null;
    String[] asUsersId = null;
    GroupRow model;

    try {
      // Get organization
      ddManager.getOrganizationSchema();
      model = Group2GroupRow(modelGroup);
      // The Ids could be equal to -1 !!!! Put it to -2 if null
      if ((modelGroup.getId() == null) || (modelGroup.getId().length() <= 0)) {
        model.id = -2;
      }
      if ((modelGroup.getDomainId() == null)
          || (modelGroup.getDomainId().length() <= 0)) {
        model.domainId = -2;
      }
      if ((modelGroup.getSuperGroupId() == null)
          || (modelGroup.getSuperGroupId().length() <= 0)) {
        model.superGroupId = -2;
      }

      // Get groups
      grs = ddManager.organization.group.searchGroups(model, isAnd);

      // Convert GroupRow objects in Group Object
      groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = GroupRow2Group(grs[nI]);
        // Get the selected users for this group
        asUsersId = ddManager.organization.user
            .getDirectUserIdsOfGroup(idAsInt(groups[nI].getId()));
        if (asUsersId != null)
          groups[nI].setUserIds(asUsersId);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.searchGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Add the given group in Silverpeas
   */
  public String addGroup(DomainDriverManager ddManager, Group group,
      boolean onlyInSilverpeas) throws AdminException {
    if (group == null || group.getName().length() == 0) {
      if (group != null)
        SynchroReport.error("GroupManager.addGroup()",
            "Problème lors de l'ajout du groupe " + group.getSpecificId()
            + " dans la base, ce groupe n'a pas de nom", null);
      return "";
    }

    try {
      ddManager.getOrganizationSchema();
      // Create group in specific domain (if onlyInSilverpeas is not true)
      // if domainId=-1 then group is a silverpeas organization
      if (!onlyInSilverpeas) {
        String specificId = null;
        if (group.getDomainId() != null) {
          specificId = ddManager.createGroup(group);
          group.setSpecificId(specificId);
        }
      }
      // Create the group node in Silverpeas
      GroupRow gr = Group2GroupRow(group);
      if (gr.superGroupId != -1)
        SynchroReport
            .info(
            "GroupManager.addGroup()",
            "Ajout du groupe "
            + group.getName()
            + " (père="
            + getGroup(ddManager, group.getSuperGroupId())
            .getSpecificId()
            + ") dans les tables ST_Group, ST_UserSet et maj de ST_UserSet_UserSet_Rel...",
            null);
      else
        // pas de père
        SynchroReport.info("GroupManager.addGroup()", "Ajout du groupe "
            + group.getName()
            + " (groupe racine) dans les tables ST_Group et ST_UserSet...",
            null);
      ddManager.organization.group.createGroup(gr);
      String sGroupId = idAsString(gr.id);

      // Create the links group_user in Silverpeas
      SynchroReport.info("GroupManager.addGroup()",
          "Inclusion des utilisateurs directement associés au groupe "
          + group.getName()
          + " (tables ST_Group_User_Rel et ST_UserSet_User_Rel)", null);
      String[] asUserIds = group.getUserIds();
      int nUserAdded = 0;
      for (int nI = 0; nI < asUserIds.length; nI++)
        if (asUserIds[nI] != null && asUserIds[nI].length() > 0) {
          ddManager.organization.group.addUserInGroup(idAsInt(asUserIds[nI]),
              idAsInt(sGroupId));
          nUserAdded++;
        }
      SynchroReport.info("GroupManager.addGroup()", nUserAdded
          + " utilisateurs ajoutés au groupe " + group.getName()
          + " dans la base", null);
      return sGroupId;
    } catch (Exception e) {
      SynchroReport.error("GroupManager.addGroup()",
          "problème lors de l'ajout du groupe " + group.getName() + " - "
          + e.getMessage(), null);
      throw new AdminException("GroupManager.addGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_GROUP", "group name: '"
          + group.getName() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups
   */
  public String deleteGroupById(DomainDriverManager ddManager, Group group,
      boolean onlyInSilverpeas) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // Delete group from specific domain
      // if domainId=-1 then group is a silverpeas organization
      if (group.getDomainId() != null && !onlyInSilverpeas)
        ddManager.deleteGroup(group.getId());

      // Delete the group node from Silverpeas
      ddManager.organization.group.removeGroup(idAsInt(group.getId()));// Les
      // traces
      // d'Info
      // synchro
      // y st
      // mises
      // car
      // supp
      // ss-groupes
      // possible

      return group.getId();
    } catch (Exception e) {
      SynchroReport.error("GroupManager.deleteGroupById()",
          "problème lors de la suppression du groupe " + group.getName()
          + " - " + e.getMessage(), null);
      throw new AdminException("GroupManager.deleteGroupById",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP", "group Id: '"
          + group.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /** Update the given group */
  public String updateGroup(DomainDriverManager ddManager, Group group,
      boolean onlyInSilverpeas) throws AdminException {
    ArrayList<String> alRemUsers = new ArrayList<String>();
    ArrayList<String> alAddUsers = new ArrayList<String>();

    if (group == null || group.getName().length() == 0
        || group.getId().length() == 0) {
      if (group != null)
        SynchroReport.error("GroupManager.updateGroup()",
            "Problème lors de maj du groupe " + group.getSpecificId()
            + " dans la base, ce groupe n'a pas de nom", null);
      return "";
    }

    try {
      ddManager.getOrganizationSchema();
      // Update group in specific domain
      // if domainId=null then group is a silverpeas organization
      if (group.getDomainId() != null && !onlyInSilverpeas)
        ddManager.updateGroup(group);

      // Get the group id
      String sGroupId = group.getId();

      String strInfoSycnhro = "";
      if (group.getSuperGroupId() != null)
        strInfoSycnhro =
            "Maj du groupe "
                +
                group.getName()
                +
                " (père="
                +
                getGroup(ddManager, group.getSuperGroupId()).getSpecificId()
                +
                ") dans la base (table ST_Group et éventuellement ST_UserSet_UserSet_Rel et ST_UserSet_User_Rel)...";
      else
        strInfoSycnhro =
            "Maj du groupe "
                +
                group.getName()
                +
                " (groupe racine) dans la base (table ST_Group et éventuellement ST_UserSet_UserSet_Rel et ST_UserSet_User_Rel)...";
      SynchroReport.info("GroupManager.updateGroup()", strInfoSycnhro, null);
      // Update the group node
      GroupRow gr = Group2GroupRow(group);
      ddManager.organization.group.updateGroup(gr);

      // Update the users if necessary
      SynchroReport
          .info(
              "GroupManager.updateGroup()",
              "Maj éventuelle des relations du groupe "
                  +
                  group.getName()
                  +
                  " avec les utilisateurs qui y sont directement inclus (tables ST_Group_User_Rel et ST_UserSet_User_Rel)",
              null);

      String[] asOldUsersId = ddManager.organization.user
          .getDirectUserIdsOfGroup(idAsInt(sGroupId));// ds ST_Group_User_Rel

      // Compute the remove users list
      String[] asNewUsersId = group.getUserIds();
      for (int nI = 0; nI < asOldUsersId.length; nI++) {
        boolean bFound = false;
        for (int nJ = 0; nJ < asNewUsersId.length; nJ++)
          if (asOldUsersId[nI].equals(asNewUsersId[nJ]))
            bFound = true;

        if (!bFound)
          alRemUsers.add(asOldUsersId[nI]);
      }

      // Compute the add users list
      for (int nI = 0; nI < asNewUsersId.length; nI++) {
        boolean bFound = false;
        for (int nJ = 0; nJ < asOldUsersId.length; nJ++)
          if (asNewUsersId[nI].equals(asOldUsersId[nJ]))
            bFound = true;

        if (!bFound)
          alAddUsers.add(asNewUsersId[nI]);
      }
      // Remove the users that are not in this group anymore
      for (int nI = 0; nI < alRemUsers.size(); nI++)
        ddManager.organization.group.removeUserFromGroup(
            idAsInt(alRemUsers.get(nI)), idAsInt(sGroupId));

      // Add the new users of the group
      for (int nI = 0; nI < alAddUsers.size(); nI++)
        ddManager.organization.group.addUserInGroup(idAsInt(alAddUsers
            .get(nI)), idAsInt(sGroupId));

      SynchroReport.info("GroupManager.updateGroup()", "Groupe : "
          + group.getName() + ", ajout de " + alAddUsers.size()
          + " nouveaux utilisateurs, suppression de " + alRemUsers.size()
          + " utilisateurs", null);
      return sGroupId;
    } catch (Exception e) {
      SynchroReport.error("GroupManager.updateGroup()",
          "problème lors de la maj du groupe " + group.getName() + " - "
          + e.getMessage(), null);
      throw new AdminException("GroupManager.updateGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP", "group Id: '"
          + group.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get Silverpeas admin organization
   * @Return an array of AdminGroupInst containing the organization
   */
  public AdminGroupInst[] getAdminOrganization(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // get all the admin group details
      String[] asGroupIds = this.getAllGroupIds(ddManager);
      Group[] aGroup = new Group[asGroupIds.length];
      for (int nI = 0; nI < asGroupIds.length; nI++)
        aGroup[nI] = this.getGroup(ddManager, asGroupIds[nI]);

      // ------------------------
      // Build the Organization
      // ------------------------

      // Search the root groups
      ArrayList<Integer> alRoot = new ArrayList<Integer>();
      for (int nI = 0; nI < aGroup.length; nI++) {
        if (aGroup[nI].getSuperGroupId() == null)
          alRoot.add(new Integer(nI));
      }

      // Build the AdminGroupInst
      AdminGroupInst[] aAdminGroupInst = new AdminGroupInst[alRoot.size()];
      for (int nI = 0; nI < alRoot.size(); nI++) {
        // Set the Group of the node
        aAdminGroupInst[nI] = new AdminGroupInst();
        aAdminGroupInst[nI].setGroup(aGroup[alRoot.get(nI).intValue()]);

        // Set the children group inst
        aAdminGroupInst[nI].setChildrenAdminGroupInst(this
            .getChildrenGroupInst(ddManager, aAdminGroupInst[nI].getGroup()
            .getId(), aGroup));
      }

      return aAdminGroupInst;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAdminOrganization",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ADMIN_ORGANIZATION", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the list of children groups of the given group
   */
  private ArrayList<AdminGroupInst> getChildrenGroupInst(DomainDriverManager ddManager,
      String sFatherGroupId, Group[] aGroup) {
    ArrayList<AdminGroupInst> alChildrenGroupInst = new ArrayList<AdminGroupInst>();

    // Search the children group
    for (int nI = 0; nI < aGroup.length; nI++)
      if (aGroup[nI].getSuperGroupId() != null
          && aGroup[nI].getSuperGroupId().equals(sFatherGroupId)) {
        AdminGroupInst adminGroupInst = new AdminGroupInst();
        adminGroupInst.setGroup(aGroup[nI]);
        adminGroupInst.setChildrenAdminGroupInst(this.getChildrenGroupInst(
            ddManager, adminGroupInst.getGroup().getId(), aGroup));
        alChildrenGroupInst.add(adminGroupInst);
      }

    return alChildrenGroupInst;
  }

  /**
   * Get space ids manageable by given group
   */
  public String[] getManageableSpaceIds(DomainDriverManager ddManager,
      String sGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.group
          .getManageableSpaceIds(idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException("GroupManager.getManageableSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "group Id: '"
          + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Convert GroupRow to Group
   */
  private Group GroupRow2Group(GroupRow gr) {
    Group group = new Group();

    if (gr != null) {
      group.setId(idAsString(gr.id));
      group.setSpecificId(gr.specificId);
      group.setDomainId(idAsString(gr.domainId));
      group.setSuperGroupId(idAsString(gr.superGroupId));
      group.setName(gr.name);
      group.setDescription(gr.description);
      group.setRule(gr.rule);
    }
    return group;
  }

  /**
   * Convert Group to GroupRow
   */
  private GroupRow Group2GroupRow(Group group) {
    GroupRow gr = new GroupRow();
    gr.id = idAsInt(group.getId());
    gr.specificId = group.getSpecificId();
    gr.domainId = idAsInt(group.getDomainId());
    gr.superGroupId = idAsInt(group.getSuperGroupId());
    gr.name = group.getName();
    gr.description = group.getDescription();
    gr.rule = group.getRule();

    return gr;
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    if (id == null || id.length() == 0)
      return -1; // the null id.

    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      return -1; // the null id.
    }
  }

  /**
   * Convert int Id to String Id
   */
  static private String idAsString(int id) {
    if (id == -1)
      return null;
    else
      return Integer.toString(id);
  }
}
