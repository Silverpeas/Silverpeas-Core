/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.user.dao.RoleDAO;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class ProfileInstManager {

  public static final String PROFILES_OF_USER = "profiles of user";
  public static final String PROFILE = "profile";
  @Inject
  private OrganizationSchema organizationSchema;

  /**
   * Constructor
   */
  protected ProfileInstManager() {
  }

  /**
   * Create a new Profile instance in database
   * @param profileInst
   * @param fatherCompLocalId
   * @return
   * @throws AdminException
   */
  public String createProfileInst(ProfileInst profileInst, int fatherCompLocalId)
      throws AdminException {
    try {
      // Create the spaceProfile node
      UserRoleRow newRole = makeUserRoleRow(profileInst);
      newRole.id = -1; // new profile Id is to be defined
      newRole.instanceId = fatherCompLocalId;
      organizationSchema.userRole().createUserRole(newRole);
      String sProfileNodeId = idAsString(newRole.id);

      // Update the CSpace with the links TProfile-TGroup
      for (int nI = 0; nI < profileInst.getNumGroup(); nI++) {
        organizationSchema.userRole().addGroupInUserRole(idAsInt(profileInst.getGroup(nI)),
            idAsInt(
            sProfileNodeId));
      }

      // Update the CSpace with the links TProfile-TUser
      for (int nI = 0; nI < profileInst.getNumUser(); nI++) {
        organizationSchema.userRole().addUserInUserRole(idAsInt(profileInst.getUser(nI)),
            idAsInt(
            sProfileNodeId));
      }

      return sProfileNodeId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(PROFILE, profileInst.getName()), e);
    }
  }

  /**
   * Get Profile information from database with the given id and creates a new Profile instance
   * @param sProfileId
   * @return
   * @throws AdminException
   */
  public ProfileInst getProfileInst(String sProfileId)
      throws AdminException {
    ProfileInst profileInst = null;
    try {
      // Load the profile detail
      UserRoleRow userRole = organizationSchema.userRole().getUserRole(idAsInt(sProfileId));

      if (userRole != null) {
        profileInst = userRoleRow2ProfileInst(userRole);
        setUsersAndGroups(profileInst);
      } else {
        SilverLogger.getLogger(this).error("User profile {0} not found", sProfileId);
      }
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(PROFILE, sProfileId), e);
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

  private void setUsersAndGroups(ProfileInst profileInst) throws AdminException {
    // Get the groups
    List<String> asGroupIds = GroupManager.get().getDirectGroupIdsInRole(profileInst.getId());
    asGroupIds.forEach(profileInst::addGroup);

    // Get the Users
    List<String> userIds = UserManager.get().getDirectUserIdsInRole(profileInst.getId());
    userIds.forEach(profileInst::addUser);
  }

  public ProfileInst getInheritedProfileInst(int instanceLocalId, String roleName)
      throws AdminException {
    try {
      // Load the profile detail
      UserRoleRow userRole =
          organizationSchema.userRole().getUserRole(instanceLocalId, roleName, 1);

      ProfileInst profileInst = null;
      if (userRole != null) {
        // Set the attributes of the profile Inst
        profileInst = userRoleRow2ProfileInst(userRole);
        setUsersAndGroups(profileInst);
      }

      return profileInst;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("inherited profile", roleName), e);
    }
  }

  /**
   * Deletes profile instance from Silverpeas
   * @param profileInst
   * @throws AdminException
   */
  public void deleteProfileInst(ProfileInst profileInst) throws AdminException {
    try {
      // delete the profile node
      organizationSchema.userRole().removeUserRole(idAsInt(profileInst.getId()));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(PROFILE, profileInst.getId()), e);
    }
  }

  /**
   * Update group role in objects of component
   * @param groupManager
   * @param profileInst
   * @param groupId
   * @param rightAssignationMode the data is used from a copy/replace from operation...
   * @throws AdminException
   */
  private void updateGroupRoleOfComponentObjects(GroupManager groupManager, ProfileInst profileInst,
      String groupId, final RightAssignationContext.MODE rightAssignationMode)
      throws AdminException, SQLException {

    //First : update role for the group
    int componentId = Integer.parseInt(profileInst.getComponentFatherId());
    boolean groupComponentAccess =
        hasDirectRightsToComponent(groupManager, groupId, true, componentId,
            rightAssignationMode);

    if(!groupComponentAccess) {
      //get all component object rights
      String[] componentObjectRoleIds =
          organizationSchema.userRole().getAllObjectUserRoleIdsOfInstance(componentId);

      //delete rights for this group to objects of this component
      for (String userRoleId : componentObjectRoleIds) {
        if (organizationSchema.userRole()
            .isGroupDirectlyInRole(idAsInt(groupId), Integer.parseInt(userRoleId))) {
          organizationSchema.userRole()
              .removeGroupFromUserRole(idAsInt(groupId), Integer.parseInt(userRoleId));
        }
      }
    }

    //Second : update role for the users of the group
    //the set of unique user id
    Set<String> users = new HashSet<>();

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
      updateUserRoleOfComponentObjects(groupManager, profileInst, userId,
          rightAssignationMode);
    }
  }

  /**
   * Indicates if the user has access to a component without searching into right objects managed
   * by the component.
   * @param groupManager
   * @param resourceId
   * @param isGroupResource
   * @param instanceId
   * @param rightAssignationMode the data is used from a copy/replace from operation...
   * @return true if the user has access to the given component
   * @throws AdminException
   */
  private boolean hasDirectRightsToComponent(final GroupManager groupManager, String resourceId,
      boolean isGroupResource, int instanceId,
      final RightAssignationContext.MODE rightAssignationMode) throws AdminException {
    final List<String> roleIds;
    if (isGroupResource) {
      roleIds = getDirectComponentProfileIds(null, Collections.singletonList(resourceId),
          instanceId);
    } else {
      List<String> allGroupIdsOfUser = null;
      if (!RightAssignationContext.MODE.REPLACE.equals(rightAssignationMode)) {
        allGroupIdsOfUser = groupManager.getAllGroupsOfUser(resourceId);
      }
      roleIds = getDirectComponentProfileIds(resourceId, allGroupIdsOfUser, instanceId);
    }
    return !roleIds.isEmpty();
  }

  /**
   * Update user role in objects of component
   * @param groupManager
   * @param profileInst
   * @param userId
   * @param rightAssignationMode the data is used from a copy/replace from operation...
   * @throws AdminException
   * */
  private void updateUserRoleOfComponentObjects(final GroupManager groupManager,
      ProfileInst profileInst, String userId,
      final RightAssignationContext.MODE rightAssignationMode) throws AdminException, SQLException {

    int componentId = Integer.parseInt(profileInst.getComponentFatherId());
    boolean userComponentAccess =
        hasDirectRightsToComponent(groupManager, userId, false, componentId,
            rightAssignationMode);

    if (!userComponentAccess) {
      //get all object rights of this component
      String[] componentObjectRoleIds =
          organizationSchema.userRole().getAllObjectUserRoleIdsOfInstance(componentId);

      //delete rights for this user to Nodes of this component
      for (String userRoleId : componentObjectRoleIds) {
        if (organizationSchema.userRole()
            .isUserDirectlyInRole(idAsInt(userId), Integer.parseInt(userRoleId))) {
          organizationSchema.userRole()
              .removeUserFromUserRole(idAsInt(userId), Integer.parseInt(userRoleId));
        }
      }
    }
  }

  /**
   * Update profile instance.
   * The method take into account the Node Rights of users or groups.
   * @param groupManager
   * @param profileInstNew
   * @param rightAssignationMode the data is used from a copy/replace from operation. It is not a
   * nice way to handle this kind of information, but it is not possible to refactor the right
   * services.
   * @throws AdminException
   */
  public String updateProfileInst(GroupManager groupManager, ProfileInst profileInstNew,
      final RightAssignationContext.MODE rightAssignationMode)
      throws AdminException {
    ProfileInst profileInst = getProfileInst(profileInstNew.getId());

    try {
      // the groups in the previous state of the profile
      List<String> alOldProfileGroup = profileInst.getAllGroups();

      // the groups in the current state of the profile
      List<String> alNewProfileGroup = profileInstNew.getAllGroups();

      // Add the new Groups
      for (String groupId : alNewProfileGroup) {
        if (!alOldProfileGroup.contains(groupId)) {
          // Create the links between the profile and the group
          organizationSchema.userRole().addGroupInUserRole(
              idAsInt(groupId), idAsInt(profileInst.getId()));
        }
      }

      // Remove from the profile the groups that are no more in the new state of the profile
      for (String groupId : alOldProfileGroup) {
        if (!alNewProfileGroup.contains(groupId)) {
          // delete the link between the profile and the group
          organizationSchema.userRole().removeGroupFromUserRole(
              idAsInt(groupId), idAsInt(profileInst.getId()));

          //update group role for objects of component
          updateGroupRoleOfComponentObjects(groupManager, profileInst, groupId,
              rightAssignationMode);
        }
      }

      // the users in the previous state of the profile
      List<String> alOldProfileUser = profileInst.getAllUsers();

      // the users in the new state of the profile
      List<String> alNewProfileUser = profileInstNew.getAllUsers();

      // Add the new Users
      for (String userId : alNewProfileUser) {
        if (!alOldProfileUser.contains(userId)) {
          // Create the links between the profile and the user
          organizationSchema.userRole().addUserInUserRole(
              idAsInt(userId), idAsInt(profileInst.getId()));
        }
      }

      // Remove from the profile the users that are no more in the new state of the profile
      for (String userId : alOldProfileUser) {
        if (!alNewProfileUser.contains(userId)) {
          // delete the link between the profile and the user
          organizationSchema.userRole().removeUserFromUserRole(
              idAsInt(userId), idAsInt(profileInst.getId()));

          //update user role in nodes of component
          updateUserRoleOfComponentObjects(groupManager, profileInst, userId,
              rightAssignationMode);
        }
      }

      // update the profile node
      UserRoleRow changedUserRole = makeUserRoleRow(profileInstNew);
      changedUserRole.id = idAsInt(profileInstNew.getId());
      organizationSchema.userRole().updateUserRole(changedUserRole);

      return idAsString(changedUserRole.id);
    } catch (SQLException e) {
      throw new AdminException(failureOnUpdate(PROFILE, profileInst.getId()), e);
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
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting(PROFILES_OF_USER, sUserId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all the profiles Id for the given user and componentId
   * @param sUserId
   * @param groupIds
   * @param componentId
   * @return ids
   * @throws AdminException
   */
  private List<String> getDirectComponentProfileIds(String sUserId, List<String> groupIds,
      int componentId) throws AdminException {
    try (Connection con = DBUtil.openConnection()) {
      List<UserRoleRow> roles = RoleDAO.getRoles(con, groupIds, idAsInt(sUserId), componentId);
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds;

    } catch (Exception e) {
      throw new AdminException(failureOnGetting(PROFILES_OF_USER, sUserId), e);
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
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting(PROFILES_OF_USER, sUserId), e);
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
      List<String> roleNames = new ArrayList<>();

      for (UserRoleRow role : roles) {
        if (!roleNames.contains(role.roleName)) {
          roleNames.add(role.roleName);
        }
      }

      return roleNames.toArray(new String[roleNames.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting(PROFILES_OF_USER, sUserId), e);
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

      List<String> groupIds = new ArrayList<>();
      groupIds.add(sGroupId);
      List<UserRoleRow> roles = RoleDAO.getRoles(con, groupIds, -1);
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("profiles of group", sGroupId), e);
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
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("profiles of group", groupId), e);
    } finally {
      DBUtil.close(con);
    }
  }

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
  private static String idAsString(int id) {
    return Integer.toString(id);
  }
}