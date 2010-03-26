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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import com.stratelia.webactiv.organization.GroupRow;
import com.stratelia.webactiv.organization.GroupUserRoleRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class GroupProfileInstManager {
  /**
   * Constructor
   */
  public GroupProfileInstManager() {

  }

  /**
   * Create a new group profile instance in database
   */
  public String createGroupProfileInst(GroupProfileInst groupProfileInst,
      DomainDriverManager DDManager, String sgroupId) throws AdminException {
    try {
      // Create the groupProfile node
      GroupUserRoleRow newRole = makeGroupUserRoleRow(groupProfileInst);
      newRole.groupId = idAsInt(sgroupId);
      DDManager.organization.groupUserRole.createGroupUserRole(newRole);
      String sProfileId = idAsString(newRole.id);

      for (int nI = 0; nI < groupProfileInst.getNumGroup(); nI++)
        DDManager.organization.groupUserRole.addGroupInGroupUserRole(
            idAsInt(groupProfileInst.getGroup(nI)), idAsInt(sProfileId));

      for (int nI = 0; nI < groupProfileInst.getNumUser(); nI++)
        DDManager.organization.groupUserRole.addUserInGroupUserRole(
            idAsInt(groupProfileInst.getUser(nI)), idAsInt(sProfileId));

      return sProfileId;
    } catch (Exception e) {
      throw new AdminException("GroupProfileInstManager.addGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE_PROFILE",
          "space profile name: '" + groupProfileInst.getName() + "'", e);
    }
  }

  /**
   * Get Space profile information with given id and creates a new GroupProfileInst
   */
  public GroupProfileInst getGroupProfileInst(DomainDriverManager ddManager,
      String sProfileId, String sGroupId) throws AdminException {
    if (sGroupId == null) {
      try {
        ddManager.getOrganizationSchema();
        GroupRow group = ddManager.organization.group
            .getGroupOfGroupUserRole(idAsInt(sProfileId));
        if (group == null)
          group = new GroupRow();
        sGroupId = idAsString(group.id);
      } catch (Exception e) {
        throw new AdminException("GroupProfileInstManager.getGroupProfileInst",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_PROFILE",
            "space profile Id: '" + sProfileId + "', groupId: '" + sGroupId
            + "'", e);
      } finally {
        ddManager.releaseOrganizationSchema();
      }
    }

    GroupProfileInst groupProfileInst = new GroupProfileInst();
    groupProfileInst.removeAllGroups();
    groupProfileInst.removeAllUsers();
    this.setGroupProfileInst(groupProfileInst, ddManager, sProfileId, sGroupId);

    return groupProfileInst;
  }

  /**
   * get information for given id and store it in the given GroupProfileInst object
   */
  public void setGroupProfileInst(GroupProfileInst groupProfileInst,
      DomainDriverManager ddManager, String sProfileId, String sGroupId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      GroupUserRoleRow groupUserRole = ddManager.organization.groupUserRole
          .getGroupUserRoleByGroupId(idAsInt(sGroupId));

      groupProfileInst.setGroupId(sGroupId);

      if (groupUserRole != null) {
        // Set the attributes of the space profile Inst
        groupProfileInst.setId(Integer.toString(groupUserRole.id));
        groupProfileInst.setName(groupUserRole.roleName);

        sProfileId = groupProfileInst.getId();

        // Get the groups
        String[] asGroupIds = ddManager.organization.group
            .getDirectGroupIdsInGroupUserRole(idAsInt(sProfileId));

        // Set the groups to the space profile
        for (int nI = 0; asGroupIds != null && nI < asGroupIds.length; nI++)
          groupProfileInst.addGroup(asGroupIds[nI]);

        // Get the Users
        String[] asUsersIds = ddManager.organization.user
            .getDirectUserIdsOfGroupUserRole(idAsInt(sProfileId));

        // Set the Users to the space profile
        for (int nI = 0; asUsersIds != null && nI < asUsersIds.length; nI++)
          groupProfileInst.addUser(asUsersIds[nI]);
      }
    } catch (Exception e) {
      throw new AdminException("GroupProfileInstManager.setGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_SPACE_PROFILE",
          "space profile Id: '" + sProfileId + "', groupId = '" + sGroupId
          + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Deletes group profile instance from Silverpeas
   */
  public void deleteGroupProfileInst(GroupProfileInst groupProfileInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      for (int nI = 0; nI < groupProfileInst.getNumGroup(); nI++)
        ddManager.organization.groupUserRole.removeGroupFromGroupUserRole(
            idAsInt(groupProfileInst.getGroup(nI)), idAsInt(groupProfileInst
            .getId()));

      for (int nI = 0; nI < groupProfileInst.getNumUser(); nI++)
        ddManager.organization.groupUserRole.removeUserFromGroupUserRole(
            idAsInt(groupProfileInst.getUser(nI)), idAsInt(groupProfileInst
            .getId()));

      // delete the groupProfile node
      ddManager.organization.groupUserRole
          .removeGroupUserRole(idAsInt(groupProfileInst.getId()));
    } catch (Exception e) {
      throw new AdminException(
          "GroupProfileInstManager.deleteGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACEPROFILE",
          "space profile Id: '" + groupProfileInst.getId() + "'", e);
    }
  }

  /**
   * Updates group profile instance
   */
  public String updateGroupProfileInst(GroupProfileInst groupProfileInst,
      DomainDriverManager ddManager, GroupProfileInst groupProfileInstNew)
      throws AdminException {
    ArrayList<String> alOldProfileGroup = new ArrayList<String>();
    ArrayList<String> alNewProfileGroup = new ArrayList<String>();
    ArrayList<String> alAddGroup = new ArrayList<String>();
    ArrayList<String> alRemGroup = new ArrayList<String>();
    ArrayList<String> alStayGroup = new ArrayList<String>();
    ArrayList<String> alOldProfileUser = new ArrayList<String>();
    ArrayList<String> alNewProfileUser = new ArrayList<String>();
    ArrayList<String> alAddUser = new ArrayList<String>();
    ArrayList<String> alRemUser = new ArrayList<String>();
    ArrayList<String> alStayUser = new ArrayList<String>();

    try {
      // Compute the Old spaceProfile group list
      ArrayList<String> alGroup = groupProfileInst.getAllGroups();
      for (int nI = 0; nI < alGroup.size(); nI++)
        alOldProfileGroup.add(alGroup.get(nI));

      // Compute the New spaceProfile group list
      alGroup = groupProfileInstNew.getAllGroups();
      for (int nI = 0; nI < alGroup.size(); nI++)
        alNewProfileGroup.add(alGroup.get(nI));

      // Compute the remove group list
      for (int nI = 0; nI < alOldProfileGroup.size(); nI++)
        if (alNewProfileGroup.indexOf(alOldProfileGroup.get(nI)) == -1)
          alRemGroup.add(alOldProfileGroup.get(nI));

      // Compute the add and stay group list
      for (int nI = 0; nI < alNewProfileGroup.size(); nI++)
        if (alOldProfileGroup.indexOf(alNewProfileGroup.get(nI)) == -1)
          alAddGroup.add(alNewProfileGroup.get(nI));
        else
          alStayGroup.add(alNewProfileGroup.get(nI));

      // Add the new Groups
      for (int nI = 0; nI < alAddGroup.size(); nI++) {
        // Create the links between the spaceProfile and the group
        ddManager.organization.groupUserRole.addGroupInGroupUserRole(
            idAsInt(alAddGroup.get(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      // Remove the removed groups
      for (int nI = 0; nI < alRemGroup.size(); nI++) {
        // delete the node link SpaceProfile_Group
        ddManager.organization.groupUserRole.removeGroupFromGroupUserRole(
            idAsInt(alRemGroup.get(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      // Compute the Old spaceProfile User list
      ArrayList<String> alUser = groupProfileInst.getAllUsers();
      for (int nI = 0; nI < alUser.size(); nI++)
        alOldProfileUser.add(alUser.get(nI));

      // Compute the New spaceProfile User list
      alUser = groupProfileInstNew.getAllUsers();
      for (int nI = 0; nI < alUser.size(); nI++)
        alNewProfileUser.add(alUser.get(nI));

      // Compute the remove User list
      for (int nI = 0; nI < alOldProfileUser.size(); nI++)
        if (alNewProfileUser.indexOf(alOldProfileUser.get(nI)) == -1)
          alRemUser.add(alOldProfileUser.get(nI));

      // Compute the add and stay User list
      for (int nI = 0; nI < alNewProfileUser.size(); nI++)
        if (alOldProfileUser.indexOf(alNewProfileUser.get(nI)) == -1)
          alAddUser.add(alNewProfileUser.get(nI));
        else
          alStayUser.add(alNewProfileUser.get(nI));

      // Add the new Users
      for (int nI = 0; nI < alAddUser.size(); nI++) {
        // Create the links between the spaceProfile and the User
        ddManager.organization.groupUserRole.addUserInGroupUserRole(
            idAsInt(alAddUser.get(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      // Remove the removed Users
      for (int nI = 0; nI < alRemUser.size(); nI++) {
        // delete the node link SpaceProfile_User
        ddManager.organization.groupUserRole.removeUserFromGroupUserRole(
            idAsInt(alRemUser.get(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      // update the spaceProfile node
      /*
       * GroupUserRoleRow changedSpaceUserRole = makeGroupUserRoleRow(groupProfileInstNew);
       * changedSpaceUserRole.id = idAsInt(groupProfileInstNew.getId());
       * ddManager.organization.groupUserRole .updateSpaceUserRole(changedSpaceUserRole);
       */

      return groupProfileInst.getId();
    } catch (Exception e) {
      throw new AdminException(
          "GroupProfileInstManager.updateGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACEPROFILE",
          "space profile Id: '" + groupProfileInst.getId() + "'", e);
    }
  }

  /**
   * Get all the group profiles Id for the given user
   */
  public String[] getGroupProfileIdsOfUser(DomainDriverManager ddManager,
      String sUserId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.groupUserRole
          .getAllGroupUserRoleIdsOfUser(idAsInt(sUserId));
    } catch (Exception e) {
      throw new AdminException(
          "GroupProfileInstManager.getSpaceProfileIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_SPACEPROFILES",
          "user Id: '" + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Converts GroupProfileInst to GroupUserRoleRow
   */
  private GroupUserRoleRow makeGroupUserRoleRow(
      GroupProfileInst groupProfileInst) {
    GroupUserRoleRow groupUserRole = new GroupUserRoleRow();

    groupUserRole.id = idAsInt(groupProfileInst.getId());
    groupUserRole.roleName = groupProfileInst.getName();

    return groupUserRole;
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
    return Integer.toString(id);
  }
}
