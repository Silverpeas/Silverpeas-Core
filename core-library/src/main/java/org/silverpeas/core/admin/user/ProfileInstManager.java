/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.ComponentInstManager;
import org.silverpeas.core.admin.user.dao.RoleDAO;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class ProfileInstManager {

  public static final String PROFILES_OF_USER = "profiles of user";
  public static final String PROFILE = "profile";
  @Inject
  private OrganizationSchema organizationSchema;
  @Inject
  private RoleDAO roleDAO;

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
      UserRoleRow newRole = UserRoleRow.makeFrom(profileInst);
      newRole.unsetId(); // new profile Id is to be defined
      newRole.setInstanceId(fatherCompLocalId);
      organizationSchema.userRole().createUserRole(newRole);
      String sProfileNodeId = idAsString(newRole.getId());

      // Update the CSpace with the links TProfile-TGroup
      for(String groupId : profileInst.getAllGroups()) {
        organizationSchema.userRole().addGroupInUserRole(idAsInt(groupId), idAsInt(sProfileNodeId));
      }

      // Update the CSpace with the links TProfile-TUser
      for(String userId : profileInst.getAllUsers()) {
        organizationSchema.userRole().addUserInUserRole(idAsInt(userId), idAsInt(sProfileNodeId));
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
        setComponentInstanceId(profileInst);
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
    profileInst.setId(Integer.toString(userRole.getId()));
    profileInst.setName(userRole.getRoleName());
    profileInst.setLabel(userRole.getName());
    profileInst.setDescription(userRole.getDescription());
    profileInst.setComponentFatherId(Integer.toString(userRole.getInstanceId()));
    if (userRole.getInheritance() == 1) {
      profileInst.setInherited(true);
    }
    if (userRole.getObjectId() > 0) {
      String oid = String.valueOf(userRole.getObjectId());
      ProfiledObjectType otype = ProfiledObjectType.fromCode(userRole.getObjectType());
      profileInst.setObjectId(new ProfiledObjectId(otype, oid));
    }
    return profileInst;
  }

  private void setUsersAndGroups(ProfileInst profileInst) throws AdminException {
    // Get the groups
    List<String> groupIds = GroupManager.get().getDirectGroupIdsInRole(profileInst.getId());
    profileInst.setGroups(groupIds);

    // Get the Users
    List<String> userIds = UserManager.get().getDirectUserIdsInRole(profileInst.getId());
    profileInst.setUsers(userIds);
  }

  private void setComponentInstanceId(final ProfileInst profileInst) throws AdminException {
    int localId = Integer.parseInt(profileInst.getComponentFatherId());
    ComponentInstLight instLight = ComponentInstManager.get().getComponentInstLight(localId);
    profileInst.setComponentFatherId(instLight.getId());
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
        setComponentInstanceId(profileInst);
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
   * Update profile instance.
   * The method take into account the Node Rights of users or groups.
   * @param profileInstNew
   * @throws AdminException
   */
  public String updateProfileInst(ProfileInst profileInstNew) throws AdminException {
    ProfileInst profileInst = getProfileInst(profileInstNew.getId());

    try {
      // the groups in the previous state of the profile
      List<String> alOldProfileGroup = profileInst.getAllGroups();

      // the groups in the current state of the profile
      List<String> alNewProfileGroup = profileInstNew.getAllGroups();
      updateProfileGroups(profileInst, alOldProfileGroup, alNewProfileGroup);

      // the users in the previous state of the profile
      List<String> alOldProfileUser = profileInst.getAllUsers();

      // the users in the new state of the profile
      List<String> alNewProfileUser = profileInstNew.getAllUsers();
      updateProfileUsers(profileInst, alOldProfileUser, alNewProfileUser);

      // update the profile node
      UserRoleRow changedUserRole = UserRoleRow.makeFrom(profileInstNew);
      organizationSchema.userRole().updateUserRole(changedUserRole);

      return idAsString(changedUserRole.getId());
    } catch (SQLException e) {
      throw new AdminException(failureOnUpdate(PROFILE, profileInst.getId()), e);
    }
  }

  private void updateProfileUsers(final ProfileInst profileInst,
      final List<String> alOldProfileUser, final List<String> alNewProfileUser)
      throws SQLException {
    // Add the new Users
    for (String userId : alNewProfileUser) {
      if (!alOldProfileUser.contains(userId)) {
        // Create the links between the profile and the user
        organizationSchema.userRole()
            .addUserInUserRole(idAsInt(userId), idAsInt(profileInst.getId()));
      }
    }

    // Remove from the profile the users that are no more in the new state of the profile
    for (String userId : alOldProfileUser) {
      if (!alNewProfileUser.contains(userId)) {
        // delete the link between the profile and the user
        organizationSchema.userRole().removeUserFromUserRole(idAsInt(userId), idAsInt(profileInst.getId()));
      }
    }
  }

  private void updateProfileGroups(final ProfileInst profileInst,
      final List<String> alOldProfileGroup, final List<String> alNewProfileGroup)
      throws SQLException {
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
        }
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

      List<UserRoleRow> roles = roleDAO.getRoles(con, groupIds, Integer.parseInt(sUserId));
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.getId()));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting(PROFILES_OF_USER, sUserId), e);
    } finally {
      DBUtil.close(con);
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
          roleDAO.getAllComponentObjectRoles(con, groupIds, Integer.parseInt(sUserId));
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.getId()));
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
          roleDAO.getRoles(con, groupIds, Integer.parseInt(sUserId), componentLocalId);
      List<String> roleNames = new ArrayList<>();

      for (UserRoleRow role : roles) {
        if (!roleNames.contains(role.getRoleName())) {
          roleNames.add(role.getRoleName());
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
      List<UserRoleRow> roles = roleDAO.getRoles(con, groupIds, -1);
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.getId()));
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

      List<UserRoleRow> roles = roleDAO.getAllComponentObjectRoles(con,
          Collections.singletonList(groupId), -1);
      List<String> roleIds = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.getId()));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("profiles of group", groupId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    return StringUtil.asInt(id, -1);
  }

  /**
   * Convert int Id to String Id
   */
  private static String idAsString(int id) {
    return Integer.toString(id);
  }
}