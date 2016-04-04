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

package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.persistence.GroupRow;
import org.silverpeas.core.admin.persistence.GroupUserRoleRow;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.ArrayList;

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
      DDManager.getOrganization().groupUserRole.createGroupUserRole(newRole);
      String sProfileId = idAsString(newRole.id);

      for (int nI = 0; nI < groupProfileInst.getNumGroup(); nI++) {
        DDManager.getOrganization().groupUserRole.addGroupInGroupUserRole(
            idAsInt(groupProfileInst.getGroup(nI)), idAsInt(sProfileId));
      }

      for (int nI = 0; nI < groupProfileInst.getNumUser(); nI++) {
        DDManager.getOrganization().groupUserRole.addUserInGroupUserRole(
            idAsInt(groupProfileInst.getUser(nI)), idAsInt(sProfileId));
      }

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
        GroupRow group = ddManager.getOrganization().group
            .getGroupOfGroupUserRole(idAsInt(sProfileId));
        if (group == null) {
          group = new GroupRow();
        }
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
      GroupUserRoleRow groupUserRole = ddManager.getOrganization().groupUserRole
          .getGroupUserRoleByGroupId(idAsInt(sGroupId));

      groupProfileInst.setGroupId(sGroupId);

      if (groupUserRole != null) {
        // Set the attributes of the space profile Inst
        groupProfileInst.setId(Integer.toString(groupUserRole.id));
        groupProfileInst.setName(groupUserRole.roleName);

        sProfileId = groupProfileInst.getId();

        // Get the groups
        String[] asGroupIds = ddManager.getOrganization().group
            .getDirectGroupIdsInGroupUserRole(idAsInt(sProfileId));

        // Set the groups to the space profile
        for (int nI = 0; asGroupIds != null && nI < asGroupIds.length; nI++) {
          groupProfileInst.addGroup(asGroupIds[nI]);
        }

        // Get the Users
        String[] asUsersIds = ddManager.getOrganization().user
            .getDirectUserIdsOfGroupUserRole(idAsInt(sProfileId));

        // Set the Users to the space profile
        for (int nI = 0; asUsersIds != null && nI < asUsersIds.length; nI++) {
          groupProfileInst.addUser(asUsersIds[nI]);
        }
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
      for (int nI = 0; nI < groupProfileInst.getNumGroup(); nI++) {
        ddManager.getOrganization().groupUserRole.removeGroupFromGroupUserRole(
            idAsInt(groupProfileInst.getGroup(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      for (int nI = 0; nI < groupProfileInst.getNumUser(); nI++) {
        ddManager.getOrganization().groupUserRole.removeUserFromGroupUserRole(
            idAsInt(groupProfileInst.getUser(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      // delete the groupProfile node
      ddManager.getOrganization().groupUserRole
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
      for (String anAlGroup : alGroup) {
        alOldProfileGroup.add(anAlGroup);
      }

      // Compute the New spaceProfile group list
      alGroup = groupProfileInstNew.getAllGroups();
      for (String anAlGroup : alGroup) {
        alNewProfileGroup.add(anAlGroup);
      }

      // Compute the remove group list
      for (String anAlOldProfileGroup : alOldProfileGroup) {
        if (!alNewProfileGroup.contains(anAlOldProfileGroup)) {
          alRemGroup.add(anAlOldProfileGroup);
        }
      }

      // Compute the add and stay group list
      for (String anAlNewProfileGroup : alNewProfileGroup) {
        if (!alOldProfileGroup.contains(anAlNewProfileGroup)) {
          alAddGroup.add(anAlNewProfileGroup);
        } else {
          alStayGroup.add(anAlNewProfileGroup);
        }
      }

      // Add the new Groups
      for (String anAlAddGroup : alAddGroup) {
        // Create the links between the spaceProfile and the group
        ddManager.getOrganization().groupUserRole.addGroupInGroupUserRole(
            idAsInt(anAlAddGroup), idAsInt(groupProfileInst
            .getId()));
      }

      // Remove the removed groups
      for (String anAlRemGroup : alRemGroup) {
        // delete the node link SpaceProfile_Group
        ddManager.getOrganization().groupUserRole.removeGroupFromGroupUserRole(
            idAsInt(anAlRemGroup), idAsInt(groupProfileInst.getId()));
      }

      // Compute the Old spaceProfile User list
      ArrayList<String> alUser = groupProfileInst.getAllUsers();
      for (String anAlUser : alUser) {
        alOldProfileUser.add(anAlUser);
      }

      // Compute the New spaceProfile User list
      alUser = groupProfileInstNew.getAllUsers();
      for (String anAlUser : alUser) {
        alNewProfileUser.add(anAlUser);
      }

      // Compute the remove User list
      for (String anAlOldProfileUser : alOldProfileUser) {
        if (!alNewProfileUser.contains(anAlOldProfileUser)) {
          alRemUser.add(anAlOldProfileUser);
        }
      }

      // Compute the add and stay User list
      for (String anAlNewProfileUser : alNewProfileUser) {
        if (!alOldProfileUser.contains(anAlNewProfileUser)) {
          alAddUser.add(anAlNewProfileUser);
        } else {
          alStayUser.add(anAlNewProfileUser);
        }
      }

      // Add the new Users
      for (String anAlAddUser : alAddUser) {
        // Create the links between the spaceProfile and the User
        ddManager.getOrganization().groupUserRole.addUserInGroupUserRole(idAsInt(anAlAddUser),
            idAsInt(groupProfileInst.getId()));
      }

      // Remove the removed Users
      for (String anAlRemUser : alRemUser) {
        // delete the node link SpaceProfile_User
        ddManager.getOrganization().groupUserRole.removeUserFromGroupUserRole(
            idAsInt(anAlRemUser), idAsInt(groupProfileInst.getId()));
      }
      return groupProfileInst.getId();
    } catch (Exception e) {
      throw new AdminException("GroupProfileInstManager.updateGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACEPROFILE",
          "space profile Id: '" + groupProfileInst.getId() + "'", e);
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
    return Integer.toString(id);
  }
}
