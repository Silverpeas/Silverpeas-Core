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

/** 
 *
 * @author  nchaix
 * @version 
 */

package com.stratelia.webactiv.beans.admin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.dao.RoleDAO;
import com.stratelia.webactiv.organization.ComponentInstanceRow;
import com.stratelia.webactiv.organization.UserRoleRow;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ProfileInstManager extends Object {
  /**
   * Constructor
   */
  public ProfileInstManager() {
  }

  /**
   * Create a new Profile instance in database
   */
  public String createProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager, String sFatherCompoId)
      throws AdminException {
    try {
      // Create the spaceProfile node
      UserRoleRow newRole = makeUserRoleRow(profileInst);
      newRole.instanceId = idAsInt(sFatherCompoId);
      ddManager.organization.userRole.createUserRole(newRole);
      String sProfileNodeId = idAsString(newRole.id);

      // Update the CSpace with the links TProfile-TGroup
      for (int nI = 0; nI < profileInst.getNumGroup(); nI++)
        ddManager.organization.userRole.addGroupInUserRole(idAsInt(profileInst
            .getGroup(nI)), idAsInt(sProfileNodeId));

      // Update the CSpace with the links TProfile-TUser
      for (int nI = 0; nI < profileInst.getNumUser(); nI++)
        ddManager.organization.userRole.addUserInUserRole(idAsInt(profileInst
            .getUser(nI)), idAsInt(sProfileNodeId));

      return sProfileNodeId;
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.createProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_PROFILE",
          "profile name: '" + profileInst.getName()
          + "', father component Id: '" + sFatherCompoId + "'", e);
    }
  }

  /**
   * Get Profileinformation from database with the given id and creates a new Profile instance
   */
  public ProfileInst getProfileInst(DomainDriverManager ddManager,
      String sProfileId, String sFatherId) throws AdminException {
    if (sFatherId == null) {
      try {
        ddManager.getOrganizationSchema();
        ComponentInstanceRow instance = ddManager.organization.instance
            .getComponentInstanceOfUserRole(idAsInt(sProfileId));
        if (instance == null)
          instance = new ComponentInstanceRow();
        sFatherId = idAsString(instance.id);
      } catch (Exception e) {
        throw new AdminException("ProfileInstManager.getProfileInst",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE",
            "profile Id: '" + sProfileId + "', father component Id: '"
            + sFatherId + "'", e);
      } finally {
        ddManager.releaseOrganizationSchema();
      }
    }

    ProfileInst profileInst = new ProfileInst();
    setProfileInst(profileInst, ddManager, sProfileId, sFatherId);

    return profileInst;
  }

  /**
   * Set Profile information with the given id
   */
  public void setProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager, String sProfileId, String sFatherId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      UserRoleRow userRole = ddManager.organization.userRole
          .getUserRole(idAsInt(sProfileId));

      if (userRole != null) {
        // Set the attributes of the profile Inst
        profileInst.setId(sProfileId);
        profileInst.setName(userRole.roleName);
        profileInst.setLabel(userRole.name);
        profileInst.setDescription(userRole.description);
        profileInst.setComponentFatherId(sFatherId);
        if (userRole.isInherited == 1)
          profileInst.setInherited(true);
        profileInst.setObjectId(userRole.objectId);

        // Get the groups
        String[] asGroupIds = ddManager.organization.group
            .getDirectGroupIdsInUserRole(idAsInt(sProfileId));

        // Set the groups to the profile
        for (int nI = 0; asGroupIds != null && nI < asGroupIds.length; nI++)
          profileInst.addGroup(asGroupIds[nI]);

        // Get the Users
        String[] asUsersIds = ddManager.organization.user
            .getDirectUserIdsOfUserRole(idAsInt(sProfileId));

        // Set the Users to the profile
        for (int nI = 0; asUsersIds != null && nI < asUsersIds.length; nI++)
          profileInst.addUser(asUsersIds[nI]);
      } else {
        SilverTrace.error("admin", "ProfileInstManager.setProfileInst",
            "root.EX_RECORD_NOT_FOUND", "sProfileId = " + sProfileId);
      }
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.setProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_PROFILE",
          "profile Id: '" + sProfileId + "', father component Id: '"
          + sFatherId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Deletes profile instance from Silverpeas
   */
  public void deleteProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // delete the profile node
      ddManager.organization.userRole.removeUserRole(idAsInt(profileInst.getId()));
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.deleteProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_PROFILE",
          "profile Id: '" + profileInst.getId() + "'", e);
    }
  }

  public String updateProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager, ProfileInst profileInstNew)
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
      // Compute the Old profile group list
      ArrayList<String> alGroup = profileInst.getAllGroups();
      for (int nI = 0; nI < alGroup.size(); nI++)
        alOldProfileGroup.add(alGroup.get(nI));

      // Compute the New profile group list
      alGroup = profileInstNew.getAllGroups();
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
        // Create the links between the profile and the group
        ddManager.organization.userRole.addGroupInUserRole(
            idAsInt(alAddGroup.get(nI)), idAsInt(profileInst.getId()));
      }

      // Remove the removed groups
      for (int nI = 0; nI < alRemGroup.size(); nI++) {
        // delete the node link Profile_Group
        ddManager.organization.userRole.removeGroupFromUserRole(
            idAsInt(alRemGroup.get(nI)), idAsInt(profileInst.getId()));
      }

      // Compute the Old profile User list
      ArrayList<String> alUser = profileInst.getAllUsers();
      for (int nI = 0; nI < alUser.size(); nI++)
        alOldProfileUser.add(alUser.get(nI));

      // Compute the New profile User list
      alUser = profileInstNew.getAllUsers();
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
        // Create the links between the profile and the User
        ddManager.organization.userRole.addUserInUserRole(
            idAsInt(alAddUser.get(nI)), idAsInt(profileInst.getId()));
      }

      // Remove the removed Users
      for (int nI = 0; nI < alRemUser.size(); nI++) {
        // delete the node link Profile_User
        ddManager.organization.userRole.removeUserFromUserRole(
            idAsInt(alRemUser.get(nI)), idAsInt(profileInst.getId()));
      }

      // update the profile node
      UserRoleRow changedUserRole = makeUserRoleRow(profileInstNew);
      changedUserRole.id = idAsInt(profileInstNew.getId());
      ddManager.organization.userRole.updateUserRole(changedUserRole);

      return idAsString(changedUserRole.id);
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.updateProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_PROFILE",
          "profile Id: '" + profileInst.getId() + "'", e);
    }
  }

  /**
   * Get all the profiles Id for the given user
   */
  public String[] getProfileIdsOfUser(String sUserId, List<String> groupIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<UserRoleRow> roles = RoleDAO.getRoles(con, groupIds, Integer.parseInt(sUserId));
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getUserProfileNames",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all the profiles Id for the given group
   */
  public String[] getProfileIdsOfGroup(DomainDriverManager ddManager,
      String sGroupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<String> groupIds = new ArrayList<String>();
      groupIds.add(sGroupId);
      List<UserRoleRow> roles = RoleDAO.getRoles(con, groupIds, -1);
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfileIdsOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Converts ProfileInst to UserRoleRow
   */
  private UserRoleRow makeUserRoleRow(ProfileInst profileInst) {
    UserRoleRow userRole = new UserRoleRow();

    userRole.id = idAsInt(profileInst.getId());
    userRole.roleName = profileInst.getName();
    userRole.name = profileInst.getLabel();
    userRole.description = profileInst.getDescription();
    if (profileInst.isInherited())
      userRole.isInherited = 1;
    userRole.objectId = profileInst.getObjectId();
    userRole.objectType = profileInst.getObjectType();

    return userRole;
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