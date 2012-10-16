/**
* Copyright (C) 2000 - 2012 Silverpeas
*
* This program is free software: you can redistribute it and/or modify it under the terms of the
* GNU Affero General Public License as published by the Free Software Foundation, either version 3
* of the License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
* redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
* applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
* text describing the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with this program.
* If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.dao.GroupDAO;
import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.organization.GroupRow;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupManager {

  /**
* Constructor
*/
  public GroupManager() {
  }

  /**
* Add a user to a group
*
* @param ddManager
* @param sUserId
* @param sGroupId
* @throws AdminException
*/
  public void addUserInGroup(DomainDriverManager ddManager, String sUserId, String sGroupId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().group.addUserInGroup(idAsInt(sUserId), idAsInt(sGroupId));
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
*
* @param ddManager
* @param sUserId
* @param sGroupId
* @throws AdminException
*/
  public void removeUserFromGroup(DomainDriverManager ddManager, String sUserId, String sGroupId)
          throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().group.removeUserFromGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException("GroupManager.removeUserFromGroup", SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_GROUPS", "User Id: '" + sUserId + "' GroupId = '" + sGroupId
              + "'",
              e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
* Get the direct groups id containing a user
*
* @param ddManager
* @param sUserId
* @return
* @throws AdminException
*/
  public String[] getDirectGroupsOfUser(DomainDriverManager ddManager, String sUserId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow[] grs = ddManager.getOrganization().group.getDirectGroupsOfUser(idAsInt(sUserId));
      // Convert GroupRow objects in Group Object
      String[] groups = new String[grs.length];
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
*
* @param ddManager
* @param sSpecificId
* @param sDomainId
* @return
* @throws AdminException
*/
  public String getGroupIdBySpecificIdAndDomainId(DomainDriverManager ddManager, String sSpecificId,
          String sDomainId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow gr = ddManager.getOrganization().group.getGroupBySpecificId(
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
*
* @param ddManager
* @return
* @throws AdminException
*/
  public String[] getAllGroupIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getAllGroupIds();

      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
* Get the all the root groups id available in Silverpeas
*
* @param ddManager
* @return
* @throws AdminException
*/
  public String[] getAllRootGroupIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getAllRootGroupIds();
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllRootGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ROOT_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
* Gets all the root user groups in Silverpeas.
*
* @param ddManager the manager of domain drivers in use in Silverpeas.
* @return an array of root user groups.
* @throws AdminException if an error occurs while getting the root groups.
*/
  public Group[] getAllRootGroups(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow[] rows = ddManager.getOrganization().group.getAllRootGroups();
      Group[] rootGroups;
      if (rows != null) {
        rootGroups = new Group[rows.length];
        for (int i = 0; i < rows.length; i++) {
          rootGroups[i] = groupRow2Group(rows[i]);
          setDirectUsersOfGroup(ddManager, rootGroups[i]);
        }
      } else {
        rootGroups = new Group[0];
      }
      return rootGroups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllRootGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ROOT_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
* Get the all the direct sub groups id of a given group
*
* @param ddManager
* @param superGroupId
* @return
* @throws AdminException
*/
  public String[] getAllSubGroupIds(DomainDriverManager ddManager, String superGroupId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getDirectSubGroupIds(idAsInt(
              superGroupId));
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
*
* @param ddManager
* @param groupId
* @return
* @throws AdminException
*/
  public List<String> getPathToGroup(DomainDriverManager ddManager, String groupId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      List<String> path = new ArrayList<String>();
      GroupRow superGroup = ddManager.getOrganization().group.getSuperGroup(idAsInt(groupId));
      while (superGroup != null) {
        path.add(0, idAsString(superGroup.id));
        superGroup = ddManager.getOrganization().group.getSuperGroup(superGroup.id);
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
* /**
* Check if the given group exists
*
* @param ddManager
* @param sName
* @return true if a group with the given name
* @throws AdminException
*/
  public boolean isGroupExist(DomainDriverManager ddManager, String sName) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // build GroupRow to search
      GroupRow searchedGroup = new GroupRow();
      searchedGroup.specificId = null;
      searchedGroup.name = sName;
      searchedGroup.description = null;

      // search for group
      GroupRow[] group = ddManager.getOrganization().group.getAllMatchingGroups(searchedGroup);

      return (group.length > 0);
    } catch (Exception e) {
      throw new AdminException("GroupManager.isGroupExist",
              SilverpeasException.ERROR, "admin.EX_ERR_IS_GROUP_EXIST",
              "group name: '" + sName + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public Group getGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);
      Group group = GroupDAO.getGroup(con, groupId);
      return group;

    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
* Get group information with the given id from Silverpeas
*
* @param ddManager
* @param sGroupId
* @return
* @throws AdminException
*/
  public Group getGroup(DomainDriverManager ddManager, String sGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow gr = ddManager.getOrganization().group.getGroup(idAsInt(sGroupId));

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
      // Get the selected users for this group
      setDirectUsersOfGroup(ddManager, group);

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
*
* @param superGroupId
* @return
* @throws AdminException
*/
  public List<String> getAllSubGroupIdsRecursively(String superGroupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);
      return getSubGroupIds(con, superGroupId);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllSubGroupIdsRecursively",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_CHILDREN_GROUP_IDS",
              "father group Id: '" + superGroupId + "'", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<String> getSubGroupIds(Connection con, String groupId) throws SQLException {
    List<String> groupIds = new ArrayList<String>();
    List<Group> groups = GroupDAO.getSubGroups(con, groupId);
    for (Group group : groups) {
      groupIds.add(group.getId());
      groupIds.addAll(getSubGroupIds(con, group.getId()));
    }
    return groupIds;
  }

  /**
* Get group information with the given group name
*
* @param ddManager
* @param sGroupName
* @param sDomainFatherId
* @return
* @throws AdminException
*/
  public Group getGroupByNameInDomain(DomainDriverManager ddManager, String sGroupName,
          String sDomainFatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      Group group = ddManager.getGroupByNameInDomain(sGroupName, sDomainFatherId);

      if (group != null) {
        String specificId = group.getSpecificId();
        GroupRow gr = ddManager.getOrganization().group.getGroupBySpecificId(
                idAsInt(sDomainFatherId),
                specificId);
        if (gr != null) {
          group.setId(idAsString(gr.id));
          // Get the selected users for this group
          setDirectUsersOfGroup(ddManager, group);
        } else {
          return null;
        }
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

  /**
*
* @param ddManager
* @param sDomainId
* @return
* @throws AdminException
*/
  public Group[] getRootGroupsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      // Get groups of domain from Silverpeas database
      GroupRow[] grs =
              ddManager.getOrganization().group.getAllRootGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        // Get the selected users for this group
        setDirectUsersOfGroup(ddManager, groups[nI]);
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

  /**
*
* @param ddManager
* @return
* @throws AdminException
*/
  public Group[] getSynchronizedGroups(DomainDriverManager ddManager) throws AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = ddManager.getOrganization().group.getSynchronizedGroups();
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupsOfDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
*
* @param ddManager
* @param sDomainId
* @return
* @throws AdminException
*/
  public String[] getRootGroupIdsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      // Get groups of domain from Silverpeas database
      String[] groupIds = ddManager.getOrganization().group.getAllRootGroupIdsOfDomain(idAsInt(
              sDomainId));
      if (groupIds != null) {
        return groupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
*
* @param ddManager
* @param sDomainId
* @return
* @throws AdminException
*/
  public Group[] getGroupsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      SynchroReport.info("GroupManager.getGroupsOfDomain()",
              "Recherche des groupes du domaine LDAP dans la base...", null);
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = ddManager.getOrganization().group.getAllGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        SynchroReport.debug("GroupManager.getGroupsOfDomain()", "Groupe trouvé no : " + Integer.
                toString(nI) + ", specificID : " + groups[nI].getSpecificId() + ", desc. : "
                + groups[nI].getDescription(), null);
      }
      SynchroReport.info("GroupManager.getGroupsOfDomain()",
              "Récupération de " + grs.length + " groupes du domaine LDAP dans la base", null);
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupsOfDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
              "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] searchGroupsIds(DomainDriverManager ddManager, boolean isRootGroup,
          String componentId, String[] aProfileId, Group modelGroup) throws AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      GroupRow model = group2GroupRow(modelGroup);
      // The Ids could be equal to -1 !!!! Put it to -2 if null
      if (!StringUtil.isDefined(modelGroup.getId())) {
        model.id = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getDomainId())) {
        model.domainId = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getSuperGroupId())) {
        model.superGroupId = -2;
      }

      int[] aRoleId = null;
      if (aProfileId != null) {
        aRoleId = new int[aProfileId.length];
        for (int i = 0; i < aProfileId.length; i++) {
          aRoleId[i] = idAsInt(aProfileId[i]);
        }
      }
      // Get groups
      return ddManager.getOrganization().group.searchGroupsIds(isRootGroup,
              idAsInt(componentId), aRoleId, model);
    } catch (Exception e) {
      throw new AdminException("GroupManager.searchGroupsIdsInGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
*
* @param ddManager
* @param modelGroup
* @param isAnd
* @return
* @throws AdminException
*/
  public Group[] searchGroups(DomainDriverManager ddManager, Group modelGroup, boolean isAnd) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      GroupRow model = group2GroupRow(modelGroup);
      // The Ids could be equal to -1 !!!! Put it to -2 if null
      if (!StringUtil.isDefined((modelGroup.getId()))) {
        model.id = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getDomainId())) {
        model.domainId = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getSuperGroupId())) {
        model.superGroupId = -2;
      }
      // Get groups
      GroupRow[] grs = ddManager.getOrganization().group.searchGroups(model, isAnd);
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        // Get the selected users for this group
        setDirectUsersOfGroup(ddManager, groups[nI]);
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
*
* @param ddManager
* @param group
* @param onlyInSilverpeas
* @return
* @throws AdminException
*/
  public String addGroup(DomainDriverManager ddManager, Group group, boolean onlyInSilverpeas)
          throws AdminException {
    if (group == null || !StringUtil.isDefined(group.getName())) {
      if (group != null) {
        SynchroReport.error("GroupManager.addGroup()", "Problème lors de l'ajout du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }

    try {
      ddManager.getOrganizationSchema();
      // Create group in specific domain (if onlyInSilverpeas is not true)
      // if domainId=-1 then group is a silverpeas organization
      if (!onlyInSilverpeas) {
        String specificId;
        if (group.getDomainId() != null) {
          specificId = ddManager.createGroup(group);
          group.setSpecificId(specificId);
        }
      }
      // Create the group node in Silverpeas
      GroupRow gr = group2GroupRow(group);
      if (gr.superGroupId != -1) {
        SynchroReport.info("GroupManager.addGroup()", "Ajout du groupe " + group.getName()
                + " (père=" + getGroup(ddManager, group.getSuperGroupId()).getSpecificId()
                + ") dans la table ST_Group", null);
      } else { // pas de père
        SynchroReport.info("GroupManager.addGroup()", "Ajout du groupe " + group.getName()
                + " (groupe racine) dans la table ST_Group...", null);
      }
      ddManager.getOrganization().group.createGroup(gr);
      String sGroupId = idAsString(gr.id);

      // index group information
      ddManager.indexGroup(gr);

      // Create the links group_user in Silverpeas
      SynchroReport.info("GroupManager.addGroup()",
              "Inclusion des utilisateurs directement associés au groupe " + group.getName()
              + " (table ST_Group_User_Rel)", null);
      String[] asUserIds = group.getUserIds();
      int nUserAdded = 0;
      for (String asUserId : asUserIds) {
        if (StringUtil.isDefined(asUserId)) {
          ddManager.getOrganization().group.addUserInGroup(idAsInt(asUserId), idAsInt(sGroupId));
          nUserAdded++;
        }
      }
      SynchroReport.info("GroupManager.addGroup()", nUserAdded + " utilisateurs ajoutés au groupe "
              + group.getName() + " dans la base", null);
      return sGroupId;
    } catch (Exception e) {
      SynchroReport.error("GroupManager.addGroup()", "problème lors de l'ajout du groupe "
              + group.getName() + " - " + e.getMessage(), null);
      throw new AdminException("GroupManager.addGroup", SilverpeasException.ERROR,
              "admin.EX_ERR_ADD_GROUP", "group name: '" + group.getName() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
* Delete the group with the given Id The delete is apply recursively to the sub-groups
*
* @param ddManager
* @param group
* @param onlyInSilverpeas
* @return
* @throws AdminException
*/
  public String deleteGroupById(DomainDriverManager ddManager, Group group,
          boolean onlyInSilverpeas) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        ddManager.deleteGroup(group.getId());
      }
      // Delete the group node from Silverpeas
      ddManager.getOrganization().group.removeGroup(idAsInt(group.getId()));

      // Delete index of group information
      ddManager.unindexGroup(group.getId());

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

  /**
* Update the given group
*
* @param ddManager
* @param group
* @param onlyInSilverpeas
* @return
* @throws AdminException
*/
  public String updateGroup(DomainDriverManager ddManager, Group group, boolean onlyInSilverpeas)
          throws AdminException {
    ArrayList<String> alRemUsers = new ArrayList<String>();
    ArrayList<String> alAddUsers = new ArrayList<String>();

    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
            group.getId())) {
      if (group != null) {
        SynchroReport.error("GroupManager.updateGroup()", "Problème lors de maj du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }
    try {
      ddManager.getOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        ddManager.updateGroup(group);
      }
      // Get the group id
      String sGroupId = group.getId();
      String strInfoSycnhro;
      if (group.getSuperGroupId() != null) {
        strInfoSycnhro = "Maj du groupe " + group.getName() + " (père=" + getGroup(ddManager, group.
                getSuperGroupId()).getSpecificId() + ") dans la base (table ST_Group)...";
      } else {
        strInfoSycnhro = "Maj du groupe " + group.getName()
                + " (groupe racine) dans la base (table ST_Group)...";
      }
      SynchroReport.info("GroupManager.updateGroup()", strInfoSycnhro, null);
      // Update the group node
      GroupRow gr = group2GroupRow(group);
      ddManager.getOrganization().group.updateGroup(gr);

      // index group information
      ddManager.indexGroup(gr);

      // Update the users if necessary
      SynchroReport.info("GroupManager.updateGroup()", "Maj éventuelle des relations du groupe "
              + group.getName()
              + " avec les utilisateurs qui y sont directement inclus (tables ST_Group_User_Rel)",
              null);
      String[] asOldUsersId = ddManager.getOrganization().user.getDirectUserIdsOfGroup(idAsInt(
              sGroupId));

      // Compute the remove users list
      String[] asNewUsersId = group.getUserIds();
      for (String anAsOldUsersId : asOldUsersId) {
        boolean bFound = false;
        for (String anAsNewUsersId : asNewUsersId) {
          if (anAsOldUsersId.equals(anAsNewUsersId)) {
            bFound = true;
          }
        }

        if (!bFound) {
          alRemUsers.add(anAsOldUsersId);
        }
      }

      // Compute the add users list
      for (String anAsNewUsersId : asNewUsersId) {
        boolean bFound = false;
        for (String anAsOldUsersId : asOldUsersId) {
          if (anAsNewUsersId.equals(anAsOldUsersId)) {
            bFound = true;
          }
        }

        if (!bFound) {
          alAddUsers.add(anAsNewUsersId);
        }
      }
      // Remove the users that are not in this group anymore
      for (String alRemUser : alRemUsers) {
        ddManager.getOrganization().group.removeUserFromGroup(
                idAsInt(alRemUser), idAsInt(sGroupId));
      }

      // Add the new users of the group
      for (String alAddUser : alAddUsers) {
        ddManager.getOrganization().group.addUserInGroup(idAsInt(alAddUser), idAsInt(sGroupId));
      }

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
*
* @param ddManager
* @return an array of AdminGroupInst containing the organization
* @throws AdminException
*/
  public AdminGroupInst[] getAdminOrganization(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = this.getAllGroupIds(ddManager);
      Group[] aGroup = new Group[asGroupIds.length];
      for (int nI = 0; nI < asGroupIds.length; nI++) {
        aGroup[nI] = this.getGroup(ddManager, asGroupIds[nI]);
      }
      // Search the root groups
      ArrayList<Integer> alRoot = new ArrayList<Integer>();
      for (int nI = 0; nI < aGroup.length; nI++) {
        if (aGroup[nI].getSuperGroupId() == null) {
          alRoot.add(nI);
        }
      }
      AdminGroupInst[] aAdminGroupInst = new AdminGroupInst[alRoot.size()];
      for (int nI = 0; nI < alRoot.size(); nI++) {
        aAdminGroupInst[nI] = new AdminGroupInst();
        aAdminGroupInst[nI].setGroup(aGroup[alRoot.get(nI)]);
        aAdminGroupInst[nI].setChildrenAdminGroupInst(this.getChildrenGroupInst(ddManager,
                aAdminGroupInst[nI].getGroup().getId(), aGroup));
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
    for (Group anAGroup : aGroup) {
      if (anAGroup.getSuperGroupId() != null && anAGroup.getSuperGroupId().equals(sFatherGroupId)) {
        AdminGroupInst adminGroupInst = new AdminGroupInst();
        adminGroupInst.setGroup(anAGroup);
        adminGroupInst.setChildrenAdminGroupInst(this.getChildrenGroupInst(ddManager,
                adminGroupInst.getGroup().getId(), aGroup));
        alChildrenGroupInst.add(adminGroupInst);
      }
    }

    return alChildrenGroupInst;
  }

  public List<String> getManageableGroupIds(String userId, List<String> groupIds)
          throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      return GroupDAO.getManageableGroupIds(con, userId, groupIds);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getManageableGroupIds",
              SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_MANAGEABLE_GROUP_IDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public int getNBUsersDirectlyInGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      return GroupDAO.getNBUsersDirectlyInGroup(con, groupId);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getNBUsersDirectlyInGroup",
              SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_OF_GROUP", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setDirectUsersOfGroup(final DomainDriverManager ddManager, final Group group) throws
          AdminPersistenceException {
    String[] asUsersId = ddManager.getOrganization().user.getDirectUserIdsOfGroup(idAsInt(group.
            getId()));
    if (asUsersId != null) {
      group.setUserIds(asUsersId);
    }
  }

  /**
* Convert GroupRow to Group
*/
  private Group groupRow2Group(GroupRow gr) {
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
  private GroupRow group2GroupRow(Group group) {
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
    if (id == null || id.length() == 0) {
      return -1; // the null id.
    }
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
    if (id == -1) {
      return null;
    } else {
      return Integer.toString(id);
    }
  }
}