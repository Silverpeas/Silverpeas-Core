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

package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.dao.RoleDAO;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class ProfileInstManager {

  /**
   * Constructor
   */
  public ProfileInstManager() {
  }

  /**
   * Create a new Profile instance in database
   * @param profileInst
   * @param ddManager
   * @param fatherCompLocalId
   * @return
   * @throws AdminException
   */
  public String createProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager, int fatherCompLocalId)
      throws AdminException {
    try {
      // Create the spaceProfile node
      UserRoleRow newRole = makeUserRoleRow(profileInst);
      newRole.id = -1; // new profile Id is to be defined
      newRole.instanceId = fatherCompLocalId;
      ddManager.getOrganization().userRole.createUserRole(newRole);
      String sProfileNodeId = idAsString(newRole.id);

      // Update the CSpace with the links TProfile-TGroup
      for (int nI = 0; nI < profileInst.getNumGroup(); nI++) {
        ddManager.getOrganization().userRole.addGroupInUserRole(idAsInt(profileInst.getGroup(nI)),
            idAsInt(
            sProfileNodeId));
      }

      // Update the CSpace with the links TProfile-TUser
      for (int nI = 0; nI < profileInst.getNumUser(); nI++) {
        ddManager.getOrganization().userRole.addUserInUserRole(idAsInt(profileInst.getUser(nI)),
            idAsInt(
            sProfileNodeId));
      }

      return sProfileNodeId;
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.createProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_PROFILE", "profile name: '" + profileInst.getName()
          + "', father component Id: '" + fatherCompLocalId + "'", e);
    }
  }

  /**
   * Get Profileinformation from database with the given id and creates a new Profile instance
   * @param ddManager
   * @param sProfileId
   * @return
   * @throws AdminException
   */
  public ProfileInst getProfileInst(DomainDriverManager ddManager, String sProfileId)
      throws AdminException {
    ProfileInst profileInst = null;
    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      UserRoleRow userRole = ddManager.getOrganization().userRole.getUserRole(idAsInt(sProfileId));

      if (userRole != null) {
        profileInst = userRoleRow2ProfileInst(userRole);
        setUsersAndGroups(ddManager, profileInst);
      } else {
        SilverTrace.error("admin", "ProfileInstManager.getProfileInst",
            "root.EX_RECORD_NOT_FOUND", "sProfileId = " + sProfileId);
      }
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.getProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_PROFILE",
          "profile Id: '" + sProfileId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return profileInst;
  }

  private ProfileInst userRoleRow2ProfileInst(UserRoleRow userRole) {
    // Set the attributes of the profile Inst
    ProfileInst profileInst = new ProfileInst();
    profileInst.setId(Integer.toString(userRole.id));
    profileInst.setName(userRole.roleName);
    profileInst.setLabel(userRole.name);
    profileInst.setDescription(userRole.description);
    profileInst.setComponentFatherId(Integer.toString(userRole.instanceId));
    if (userRole.isInherited == 1) {
      profileInst.setInherited(true);
    }
    if (userRole.objectId > 0) {
      profileInst.setObjectId(userRole.objectId);
    }
    profileInst.setObjectType(userRole.objectType);
    return profileInst;
  }

  private void setUsersAndGroups(DomainDriverManager ddManager, ProfileInst profileInst)
      throws AdminPersistenceException {

    // Get the groups
    String[] asGroupIds =
        ddManager.getOrganization().group.getDirectGroupIdsInUserRole(idAsInt(profileInst.getId()));

    // Set the groups to the space profile
    if (asGroupIds != null) {
      for (String groupId : asGroupIds) {
        profileInst.addGroup(groupId);
      }
    }

    // Get the Users
    String[] asUsersIds = ddManager.getOrganization().user.getDirectUserIdsOfUserRole(idAsInt(
        profileInst.getId()));

    // Set the Users to the space profile
    if (asUsersIds != null) {
      for (String userId : asUsersIds) {
        profileInst.addUser(userId);
      }
    }
  }

  public ProfileInst getInheritedProfileInst(DomainDriverManager ddManager,
      int instanceLocalId, String roleName)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      UserRoleRow userRole =
          ddManager.getOrganization().userRole.getUserRole(instanceLocalId, roleName, 1);

      ProfileInst profileInst = null;
      if (userRole != null) {
        // Set the attributes of the profile Inst
        profileInst = userRoleRow2ProfileInst(userRole);
        setUsersAndGroups(ddManager, profileInst);
      }

      return profileInst;
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.getInheritedProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_PROFILE",
          "instanceId = " + instanceLocalId + ", role = " + roleName, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Deletes profile instance from Silverpeas
   * @param profileInst
   * @param ddManager
   * @throws AdminException
   */
  public void deleteProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // delete the profile node
      ddManager.getOrganization().userRole.removeUserRole(idAsInt(profileInst.getId()));
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.deleteProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_PROFILE",
          "profile Id: '" + profileInst.getId() + "'", e);
    }
  }

  /**
   * Update group role in objects of component
   * @param groupManager
   * @param ddManager
   * @param profileInst
   * @param groupId
   * @param rightAssignationMode the data is used from a copy/replace from operation...
   * @throws AdminException
   */
  private void updateGroupRoleOfComponentObjects(GroupManager groupManager,
      DomainDriverManager ddManager, ProfileInst profileInst, String groupId,
      final RightAssignationContext.MODE rightAssignationMode) throws AdminException {

    //First : update role for the group
    int componentId = Integer.parseInt(profileInst.getComponentFatherId());
    boolean groupComponentAccess =
        hasDirectRightsToComponent(ddManager, groupManager, groupId, true, componentId,
            rightAssignationMode);

    if(!groupComponentAccess) {
      //get all component object rights
      String[] componentObjectRoleIds =
          ddManager.getOrganization().userRole.getAllObjectUserRoleIdsOfInstance(componentId);

      //delete rights for this group to objects of this component
      for (String userRoleId : componentObjectRoleIds) {
        if (ddManager.getOrganization().userRole
            .isGroupDirectlyInRole(idAsInt(groupId), Integer.parseInt(userRoleId))) {
          ddManager.getOrganization().userRole
              .removeGroupFromUserRole(idAsInt(groupId), Integer.parseInt(userRoleId));
        }
      }
    }

    //Second : update role for the users of the group
    //the set of unique user id
    Set<String> users = new HashSet<String>();

    //users directly in group
    List<String> listUsersGroup = groupManager.getUsersDirectlyInGroup(groupId);
    for (String userId : listUsersGroup) {
      users.add(userId);
    }

    //users in sub groups
    List<String> listSubGroupIds = groupManager.getAllSubGroupIdsRecursively(groupId);
    for (String subGroupId : listSubGroupIds) {
      List<String> listUsersSubGroup = groupManager.getUsersDirectlyInGroup(subGroupId);
      for (String userId : listUsersSubGroup) {
        users.add(userId);
      }
    }

    //for any user : updateRoleInNodes
    for(String userId : users) {
      updateUserRoleOfComponentObjects(ddManager, groupManager, profileInst, userId,
          rightAssignationMode);
    }
  }

  /**
   * Indicates if the user has access to a component without searching into right objects managed
   * by the component.
   * @param ddManager
   * @param groupManager
   * @param resourceId
   * @param isGroupResource
   * @param instanceId
   * @param rightAssignationMode the data is used from a copy/replace from operation...
   * @return true if the user has access to the given component
   * @throws AdminException
   */
  private boolean hasDirectRightsToComponent(final DomainDriverManager ddManager,
      final GroupManager groupManager, String resourceId, boolean isGroupResource, int instanceId,
      final RightAssignationContext.MODE rightAssignationMode) throws AdminException {
    final List<String> roleIds;
    if (isGroupResource) {
      roleIds = getDirectComponentProfileIds(ddManager, null, Collections.singletonList(resourceId),
          instanceId);
    } else {
      List<String> allGroupIdsOfUser = null;
      if (!RightAssignationContext.MODE.REPLACE.equals(rightAssignationMode)) {
        allGroupIdsOfUser = groupManager.getAllGroupsOfUser(ddManager, resourceId);
      }
      roleIds = getDirectComponentProfileIds(ddManager, resourceId, allGroupIdsOfUser, instanceId);
    }
    return !roleIds.isEmpty();
  }

  /**
   * Update user role in objects of component
   * @param ddManager
   * @param groupManager
   * @param profileInst
   * @param userId
   * @param rightAssignationMode the data is used from a copy/replace from operation...
   * @throws AdminException
   * */
  private void updateUserRoleOfComponentObjects(DomainDriverManager ddManager,
      final GroupManager groupManager, ProfileInst profileInst, String userId,
      final RightAssignationContext.MODE rightAssignationMode) throws AdminException {

    int componentId = Integer.parseInt(profileInst.getComponentFatherId());
    boolean userComponentAccess =
        hasDirectRightsToComponent(ddManager, groupManager, userId, false, componentId,
            rightAssignationMode);

    if (!userComponentAccess) {
      //get all object rights of this component
      String[] componentObjectRoleIds =
          ddManager.getOrganization().userRole.getAllObjectUserRoleIdsOfInstance(componentId);

      //delete rights for this user to Nodes of this component
      for (String userRoleId : componentObjectRoleIds) {
        if (ddManager.getOrganization().userRole
            .isUserDirectlyInRole(idAsInt(userId), Integer.parseInt(userRoleId))) {
          ddManager.getOrganization().userRole
              .removeUserFromUserRole(idAsInt(userId), Integer.parseInt(userRoleId));
        }
      }
    }
  }

  /**
   * Update profile instance.
   * The method take into account the Node Rights of users or groups.
   * @param groupManager
   * @param ddManager
   * @param profileInstNew
   * @param rightAssignationMode the data is used from a copy/replace from operation. It is not a
   * nice way to handle this kind of information, but it is not possible to refactor the right
   * services.
   * @throws AdminException
   */
  public String updateProfileInst(GroupManager groupManager, DomainDriverManager ddManager,
      ProfileInst profileInstNew, final RightAssignationContext.MODE rightAssignationMode)
      throws AdminException {

    ProfileInst profileInst = getProfileInst(ddManager, profileInstNew.getId());

    ArrayList<String> alOldProfileGroup = new ArrayList<String>();
    ArrayList<String> alNewProfileGroup = new ArrayList<String>();
    ArrayList<String> alAddGroup = new ArrayList<String>();
    ArrayList<String> alRemGroup = new ArrayList<String>();
    ArrayList<String> alOldProfileUser = new ArrayList<String>();
    ArrayList<String> alNewProfileUser = new ArrayList<String>();
    ArrayList<String> alAddUser = new ArrayList<String>();
    ArrayList<String> alRemUser = new ArrayList<String>();

    try {
      // Compute the Old profile group list
      ArrayList<String> alGroup = profileInst.getAllGroups();
      for (String groupId : alGroup) {
        alOldProfileGroup.add(groupId);
      }

      // Compute the New profile group list
      alGroup = profileInstNew.getAllGroups();
      for (String groupId : alGroup) {
        alNewProfileGroup.add(groupId);
      }

      // Compute the remove group list
      for (String groupId : alOldProfileGroup) {
        if (!alNewProfileGroup.contains(groupId)) {
          alRemGroup.add(groupId);
        }
      }

      // Compute the add and stay group list
      for (String groupId : alNewProfileGroup) {
        if (!alOldProfileGroup.contains(groupId)) {
          alAddGroup.add(groupId);
        }
      }

      // Add the new Groups
      for (String groupId : alAddGroup) {
        // Create the links between the profile and the group
        ddManager.getOrganization().userRole.addGroupInUserRole(
            idAsInt(groupId), idAsInt(profileInst.getId()));
      }

      // Remove the removed groups
      for (String groupId : alRemGroup) {
        // delete the node link Profile_Group
        ddManager.getOrganization().userRole.removeGroupFromUserRole(
            idAsInt(groupId), idAsInt(profileInst.getId()));

        //update group role for objects of component
        updateGroupRoleOfComponentObjects(groupManager, ddManager, profileInst, groupId,
            rightAssignationMode);
      }

      // Compute the Old profile User list
      ArrayList<String> alUser = profileInst.getAllUsers();
      for (String userId : alUser) {
        alOldProfileUser.add(userId);
      }

      // Compute the New profile User list
      alUser = profileInstNew.getAllUsers();
      for (String userId : alUser) {
        alNewProfileUser.add(userId);
      }

      // Compute the remove User list
      for (String userId : alOldProfileUser) {
        if (!alNewProfileUser.contains(userId)) {
          alRemUser.add(userId);
        }
      }

      // Compute the add and stay User list
      for (String userId : alNewProfileUser) {
        if (!alOldProfileUser.contains(userId)) {
          alAddUser.add(userId);
        }
      }

      // Add the new Users
      for (String userId : alAddUser) {
        // Create the links between the profile and the User
        ddManager.getOrganization().userRole.addUserInUserRole(
            idAsInt(userId), idAsInt(profileInst.getId()));
      }

      // Remove the removed Users
      for (String userId : alRemUser) {
        // delete the node link Profile_User
        ddManager.getOrganization().userRole.removeUserFromUserRole(
            idAsInt(userId), idAsInt(profileInst.getId()));

        //update user role in nodes of component
        updateUserRoleOfComponentObjects(ddManager, groupManager, profileInst, userId,
            rightAssignationMode);
      }

      // update the profile node
      UserRoleRow changedUserRole = makeUserRoleRow(profileInstNew);
      changedUserRole.id = idAsInt(profileInstNew.getId());
      ddManager.getOrganization().userRole.updateUserRole(changedUserRole);

      return idAsString(changedUserRole.id);
    } catch (Exception e) {
      throw new AdminException("ProfileInstManager.updateProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_PROFILE",
          "profile Id: '" + profileInst.getId() + "'", e);
    }
  }

  /**
   * Get all the profiles Id for the given user and groups
   * @param sUserId
   * @param groupIds
   * @return
   * @throws AdminException
   */
  public String[] getProfileIdsOfUser(String sUserId, List<String> groupIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles = RoleDAO.getRoles(con, groupIds, Integer.parseInt(sUserId));
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfileIdsOfUserAndGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all the profiles Id for the given user and componentId
   * @param ddManager
   * @param sUserId
   * @param groupIds
   * @param componentId
   * @return ids
   * @throws AdminException
   */
  private List<String> getDirectComponentProfileIds(final DomainDriverManager ddManager,
      String sUserId, List<String> groupIds, int componentId) throws AdminException {
    try {
      // This connection must not been closed at the end of this method,
      // because it must be called into a transaction. It the method that has initialized the
      // transaction that will close the connection.
      Connection con = ddManager.getOrganization().getConnection();

      List<UserRoleRow> roles = RoleDAO.getRoles(con, groupIds, idAsInt(sUserId), componentId);
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds;

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfileIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    }
  }

  /**
   * Get all the component object profile Ids for the given user and/or groups
   * @param sUserId
   * @param groupIds
   * @return
   * @throws AdminException
   */
  public String[] getAllComponentObjectProfileIdsOfUser(String sUserId, List<String> groupIds)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles =
          RoleDAO.getAllComponentObjectRoles(con, groupIds, Integer.parseInt(sUserId));
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getNodeProfileIdsOfUserAndGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public String[] getProfileNamesOfUser(String sUserId, List<String> groupIds, int componentLocalId)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles =
          RoleDAO.getRoles(con, groupIds, Integer.parseInt(sUserId), componentLocalId);
      List<String> roleNames = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        if (!roleNames.contains(role.roleName)) {
          roleNames.add(role.roleName);
        }
      }

      return roleNames.toArray(new String[roleNames.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfileNamesOfUserAndGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all the profiles Id for the given group
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public String[] getProfileIdsOfGroup(String sGroupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

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
   * Get all the node profiles Id for the given group
   * @param groupId
   * @return
   * @throws AdminException
   */
  public String[] getAllComponentObjectProfileIdsOfGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles = RoleDAO.getAllComponentObjectRoles(con,
          Collections.singletonList(groupId), -1);
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getNodeProfileIdsOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Converts ProfileInst to UserRoleRow
   * @param profileInst
   * @return
   */
  private UserRoleRow makeUserRoleRow(ProfileInst profileInst) {
    UserRoleRow userRole = new UserRoleRow();

    userRole.id = idAsInt(profileInst.getId());
    userRole.roleName = profileInst.getName();
    userRole.name = profileInst.getLabel();
    userRole.description = profileInst.getDescription();
    if (profileInst.isInherited()) {
      userRole.isInherited = 1;
    }
    userRole.objectId = profileInst.getObjectId();
    userRole.objectType = profileInst.getObjectType();

    return userRole;
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