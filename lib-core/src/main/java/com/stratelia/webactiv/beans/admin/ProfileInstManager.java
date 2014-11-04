/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.beans.admin;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.dao.RoleDAO;
import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.organization.UserRoleRow;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
   * @param sFatherCompoId
   * @return
   * @throws AdminException
   */
  public String createProfileInst(ProfileInst profileInst,
      DomainDriverManager ddManager, String sFatherCompoId)
      throws AdminException {
    try {
      // Create the spaceProfile node
      UserRoleRow newRole = makeUserRoleRow(profileInst);
      newRole.id = -1; // new profile Id is to be defined
      newRole.instanceId = idAsInt(sFatherCompoId);
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
          + "', father component Id: '" + sFatherCompoId + "'", e);
    }
  }

  /**
   * Get Profileinformation from database with the given id and creates a new Profile instance
   * @param ddManager
   * @param sProfileId
   * @param sFatherId
   * @return
   * @throws AdminException
   */
  public ProfileInst getProfileInst(DomainDriverManager ddManager, String sProfileId,
      String sFatherId) throws AdminException {
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
          "profile Id: '" + sProfileId + "', father component Id: '"
          + sFatherId + "'", e);
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
      String instanceId, String roleName)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      UserRoleRow userRole =
          ddManager.getOrganization().userRole.getUserRole(idAsInt(instanceId), roleName, 1);

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
          "instanceId = " + instanceId + ", role = " + roleName, e);
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
   * true if the group has access to the given component
   * @param ddManager
   * @param groupId
   * @param instanceId
   * @throws AdminException 
   */
  private boolean hasGroupRightsToComponent(DomainDriverManager ddManager, String groupId, int instanceId) 
      throws AdminException {
    String[] roleIds = getProfileIdsOfGroup(groupId, instanceId);
    
    //it is executed in a transaction, so we must ignore one response which corresponds to roleId
    int size = roleIds.length;
    if(size >= 1) {
      size --;
    }
    
    if(size > 0) {
      return true;
    }
    return false;
  }
  
  /**
   * Update group role in nodes of component
   * @param groupManager
   * @param ddManager
   * @param profileInst
   * @param groupeId
   * @throws AdminException
   * @throws AdminPersistenceException
   */
  private void updateGroupRoleInNodes(GroupManager groupManager, DomainDriverManager ddManager, ProfileInst profileInst,
      String groupId) throws AdminException, AdminPersistenceException {
    
    //First : update role for the group
    int componentId = Integer.parseInt(profileInst.getComponentFatherId());
    boolean groupComponentAccess = hasGroupRightsToComponent(ddManager, groupId, componentId);
    
    if(!groupComponentAccess) {
      //get all rights for this group to Nodes of this component
      String[] tabUserRoleIds = ddManager.getOrganization().userRole.getAllObjectUserRoleIdsOfInstance(componentId);
      
      //delete rights for this group to Nodes of this component
      for (String userRoleId : tabUserRoleIds) {
        if(ddManager.getOrganization().userRole.isGroupDirectlyInRole(idAsInt(groupId), Integer.parseInt(userRoleId))) {
          ddManager.getOrganization().userRole.removeGroupFromUserRole(idAsInt(groupId), Integer.parseInt(userRoleId));
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
      updateUserRoleInNodes(ddManager, profileInst, userId);
    }
  }
  
  /**
   * true if the user has access to the given component
   * @param ddManager
   * @param userId
   * @param instanceId
   * @throws AdminException 
   */
  private boolean hasUserRightsToComponent(DomainDriverManager ddManager, String userId, int instanceId) 
      throws AdminException {
    List<String> roleIds = getProfileIdsOfUser(userId, instanceId);
    
    //it is executed in a transaction, so we must ignore one response which corresponds to roleId
    int size = roleIds.size();
    if(size >= 1) {
      size --;
    }
    
    if(size > 0) {
      return true;
    }
    return false;
  }
  
  /**
   * Update user role in nodes of component
   * @param ddManager
   * @param profileInst
   * @param userId
   * @throws AdminException
   * @throws AdminPersistenceException
   */
  private void updateUserRoleInNodes(DomainDriverManager ddManager, ProfileInst profileInst,
      String userId) throws AdminException, AdminPersistenceException {
    
    int componentId = Integer.parseInt(profileInst.getComponentFatherId());
    boolean userComponentAccess = hasUserRightsToComponent(ddManager, userId, componentId);
    
    if(!userComponentAccess) {
      //get all rights for this user to Nodes of this component
      String[] tabUserRoleIds = ddManager.getOrganization().userRole.getAllObjectUserRoleIdsOfInstance(componentId);
      
      //delete rights for this user to Nodes of this component
      for (String userRoleId : tabUserRoleIds) {
        if(ddManager.getOrganization().userRole.isUserDirectlyInRole(idAsInt(userId), Integer.parseInt(userRoleId))) {
          ddManager.getOrganization().userRole.removeUserFromUserRole(idAsInt(userId), Integer.parseInt(userRoleId));
        }
      }
    }
  }

  /**
   * Update profile instance
   * @param ddManager
   * @param profileInst
   * @throws AdminException
   */
  public String updateProfileInst(GroupManager groupManager, DomainDriverManager ddManager, ProfileInst profileInstNew)
      throws AdminException {

    ProfileInst profileInst = getProfileInst(ddManager, profileInstNew.getId(), null);

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
        } else {
          alStayGroup.add(groupId);
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
        
        //update user role in nodes of component
        updateGroupRoleInNodes(groupManager, ddManager, profileInst, groupId);
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
        } else {
          alStayUser.add(userId);
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
        updateUserRoleInNodes(ddManager, profileInst, userId);
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
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

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
   * @param sUserId
   * @param componentId
   * @return ids
   * @throws AdminException
   */
  private List<String> getProfileIdsOfUser(String sUserId, int componentId)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<UserRoleRow> roles = RoleDAO.getRoles(con, componentId, idAsInt(sUserId));
      List<String> roleIds = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds;

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfileIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }
  
  /**
   * Get all the node profiles Id for the given user and groups
   * @param sUserId
   * @param groupIds
   * @return
   * @throws AdminException
   */
  public String[] getNodeProfileIdsOfUser(String sUserId, List<String> groupIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<UserRoleRow> roles = RoleDAO.getNodeRoles(con, groupIds, Integer.parseInt(sUserId));
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

  public String[] getProfileNamesOfUser(String sUserId, List<String> groupIds, int componentId)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<UserRoleRow> roles =
          RoleDAO.getRoles(con, groupIds, Integer.parseInt(sUserId), componentId);
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
   * Get all the profiles Id for the given group and componentId
   * @param groupId
   * @param componentId
   * @return ids
   * @throws AdminException
   */
  private String[] getProfileIdsOfGroup(String groupId, int componentId)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<UserRoleRow> roles = RoleDAO.getRolesByGroup(con, componentId, idAsInt(groupId));
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
   * Get all the profiles Id for the given group
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public String[] getProfileIdsOfGroup(String sGroupId) throws AdminException {
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
   * Get all the node profiles Id for the given group
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public String[] getNodeProfileIdsOfGroup(String sGroupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      List<String> groupIds = new ArrayList<String>();
      groupIds.add(sGroupId);
      List<UserRoleRow> roles = RoleDAO.getNodeRoles(con, groupIds, -1);
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