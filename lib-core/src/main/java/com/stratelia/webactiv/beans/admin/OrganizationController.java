/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

/*
 * @author Norbert CHAIX
 * @version 1.0
 * date 14/09/2000
 */
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.admin.components.WAComponent;
import static com.silverpeas.util.ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

import static com.stratelia.webactiv.beans.admin.AdminReference.getAdminService;
import java.util.*;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * This objet is used by all the admin jsp such as SpaceManagement, UserManagement, etc... It
 * provides access functions to query and modify the domains as well as the company organization It
 * should be used only by a client that has the administrator rights.
 *
 * The OrganizationController extends AdminReference that maintains a static references to an Admin
 * instance. During the initialization of the Admin instance, some computations require services
 * published by the underlying IoC container. So, an instance of OrganizationController is created
 * by the IoC container and published under the name 'organizationController' so that the
 * initialization of the static Admin instance can be performed correctly within the execution
 * context of IoC container.
 */
public class OrganizationController implements java.io.Serializable {

  private static final long serialVersionUID = 3435241972671610107L;

  /**
   * Constructor declaration
   */
  public OrganizationController() {
  }

  // -------------------------------------------------------------------
  // SPACES QUERIES
  // -------------------------------------------------------------------
  /**
   * Return all the spaces Id available in silverpeas
   *
   * @return
   */
  public String[] getAllSpaceIds() {
    try {
      return getAdminService().getAllSpaceIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSpaceIds",
          "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
      return new String[0];
    }
  }

  /**
   * Return all the subSpaces Id available in silverpeas given a space id (driver format)
   */
  public String[] getAllSubSpaceIds(String sSpaceId) {
    try {
      String[] asSubSpaceIds = getAdminService().getAllSubSpaceIds(sSpaceId);

      return asSubSpaceIds;
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSubSpaceIds",
          "admin.MSG_ERR_GET_SUBSPACE_IDS", "father space id: '" + sSpaceId +
           "'", e);
      return new String[0];
    }
  }

  /**
   * Return the the spaces name corresponding to the given space ids.
   *
   * @param asSpaceIds
   * @return
   */
  public String[] getSpaceNames(String[] asSpaceIds) {
    try {
      String[] asSpaceNames = getAdminService().getSpaceNames(asSpaceIds);

      return asSpaceNames;
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getSpaceNames",
          "admin.MSG_ERR_GET_SPACE_NAMES", e);
      return new String[0];
    }
  }

  /**
   * Return the space light corresponding to the given space id
   *
   * @param spaceId
   * @return
   */
  public SpaceInstLight getSpaceInstLightById(String spaceId) {
    try {
      SpaceInstLight spaceLight = getAdminService().getSpaceInstLightById(spaceId);
      return spaceLight;
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getSpaceInstLightById",
          "admin.MSG_ERR_GET_SPACE_BY_ID", "spaceId=" + spaceId, e);
      return null;
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getGeneralSpaceId() {
    try {
      return getAdminService().getGeneralSpaceId();
    } catch (Exception e) {
      SilverTrace.fatal("admin", "OrganizationController.getGeneralSpaceId",
          "admin.MSG_FATAL_GET_GENERAL_SPACE_ID", e);
      return "";
    }
  }

  /**
   * Return the space Instance corresponding to the given space id
   */
  public SpaceInst getSpaceInstById(String sSpaceId) {
    try {
      return getAdminService().getSpaceInstById(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getSpaceInstById",
          "admin.MSG_ERR_GET_SPACE", "space Id: '" + sSpaceId + "'", e);
      return null;
    }
  }

  /**
   * Return the component ids available for the cuurent user Id in the given space id
   */
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {
    try {
      return getAdminService().getAvailCompoIds(sClientSpaceId, sUserId);
    } catch (AdminException e) {
      SilverTrace.error("admin", "OrganizationController.getAvailCompoIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "space Id: '" +
           sClientSpaceId + "', user Id: '" + sUserId + "'", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return the component ids available for the cuurent user Id in the given space id
   */
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) {
    try {
      String[] asCompoIds = getAdminService().getAvailCompoIdsAtRoot(sClientSpaceId,
          sUserId);

      return asCompoIds;
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getAvailCompoIdsAtRoot",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "space Id: '" +
           sClientSpaceId + "', user Id: '" + sUserId + "'", e);
      return new String[0];
    }
  }

  /**
   * Return all the components of silverpeas read in the xmlComponent directory
   */
  public Map<String, WAComponent> getAllComponents() {
    try {
      return getAdminService().getAllComponents();
    } catch (Exception e) {
      if (!(e instanceof AdminException && ((AdminException) e).isAlreadyPrinted())) {
        SilverTrace.error("admin", "OrganizationController.getAvailDriverCompoIds",
            "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", null, e);
      }
      return new HashMap<String, WAComponent>();
    }
  }

  /**
   * Return all the components names available in webactiv
   */
  public Map<String, String> getAllComponentsNames() {
    try {
      return getAdminService().getAllComponentsNames();
    } catch (Exception e) {
      if (!(e instanceof AdminException && ((AdminException) e).isAlreadyPrinted())) {
        SilverTrace.error("admin",
            "OrganizationController.getAvailDriverCompoIds",
            "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", null, e);
      }
      return new HashMap<String, String>();
    }
  }

  /**
   * Return the driver component ids available for the cuurent user Id in the given space id
   */
  public String[] getAvailDriverCompoIds(String sClientSpaceId, String sUserId) {
    try {
      return getAdminService().getAvailDriverCompoIds(sClientSpaceId, sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAvailDriverCompoIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "space Id: '" +
           sClientSpaceId + "', user Id: '" + sUserId + "'", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return the tuples (space id, compo id) allowed for the given user and given component name
   *
   * @param sUserId
   * @param sCompoName
   * @return
   */
  public CompoSpace[] getCompoForUser(String sUserId, String sCompoName) {
    try {
      return getAdminService().getCompoForUser(sUserId, sCompoName);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getCompoForUser",
          "admin.MSG_ERR_GET_USER_AVAILABLE_INSTANCES_OF_COMPONENT",
          "user Id : '" + sUserId + "', component name: '" + sCompoName + "'", e);
      return new CompoSpace[0];
    }
  }

  /**
   * gets the available component for a given user
   *
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   */
  public List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName) {
    try {
      return getAdminService().getAvailComponentInstLights(userId, componentName);
    } catch (AdminException e) {
      SilverTrace.error("admin",
          "getAvailComponentInstLights", "admin.MSG_ERR_GET_USER_AVAILABLE_INSTANCES_OF_COMPONENT",
          "user Id : '" + userId + "', component name: '" + componentName + "'", e);
      return new ArrayList<ComponentInstLight>();
    }

  }

  public String[] getComponentIdsForUser(String sUserId, String sCompoName) {
    try {
      return getAdminService().getComponentIdsByNameAndUserId(sUserId, sCompoName);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getComponentIdsForUser",
          "admin.MSG_ERR_GET_USER_AVAILABLE_INSTANCES_OF_COMPONENT",
          "user Id : '" + sUserId + "', component name: '" + sCompoName + "'", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return the compo id for the given component name
   *
   * @param sCompoName
   * @return
   */
  public String[] getCompoId(String sCompoName) {
    try {
      return getAdminService().getCompoId(sCompoName);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getCompoId",
          "admin.MSG_ERR_GET_AVAILABLE_INSTANCES_OF_COMPONENT",
          "component name: '" + sCompoName + "'", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  public String getComponentParameterValue(String sComponentId,
      String parameterName) {
    return getAdminService().getComponentParameterValue(sComponentId, parameterName);
  }
  // -------------------------------------------------------------------
  // COMPONENTS QUERIES
  // -------------------------------------------------------------------

  /**
   * Return the component Instance corresponding to the given component id
   */
  public ComponentInst getComponentInst(String sComponentId) {
    try {
      return getAdminService().getComponentInst(sComponentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getComponentInst",
          "admin.MSG_ERR_GET_COMPONENT", "component Id : '" + sComponentId +
           "'", e);
      return null;
    }
  }

  /**
   * @param spaceId - id of the space or subSpace
   * @return a List of SpaceInst ordered from root to subSpace
   * @throws Exception
   */
  public List<SpaceInst> getSpacePath(String spaceId) {
    return getSpacePath(new ArrayList<SpaceInst>(), spaceId);
  }

  public List<SpaceInst> getSpacePathToComponent(String componentId) {
    ComponentInstLight componentInstLight = getComponentInstLight(componentId);
    if (componentInstLight != null) {
      return getSpacePath(componentInstLight.getDomainFatherId());
    }
    return new ArrayList<SpaceInst>();
  }

  private List<SpaceInst> getSpacePath(List<SpaceInst> path, String spaceId) {
    try {
      SpaceInst spaceInst = getAdminService().getSpaceInstById(spaceId);
      if (spaceInst != null) {
        path.add(0, spaceInst);
        if (!spaceInst.isRoot()) {
          path = getSpacePath(path, spaceInst.getDomainFatherId());
        }
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getSpacePath",
          "admin.MSG_ERR_GET_SPACE", "spaceId = '" + spaceId + "'", e);
    }
    return path;
  }

  /**
   * Return the component Instance Light corresponding to the given component id
   */
  public ComponentInstLight getComponentInstLight(String sComponentId) {
    try {
      return getAdminService().getComponentInstLight(sComponentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getComponentInstLight",
          "admin.MSG_ERR_GET_COMPONENT", "component Id : '" + sComponentId +
           "'", e);
      return null;
    }
  }

  // -------------------------------------------------------------------
  // USERS QUERIES
  // -------------------------------------------------------------------
  /**
   * Return the database id of the user with the given ldap Id
   */
  public int getUserDBId(String sUserId) {
    return idAsInt(sUserId);
  }

  /**
   * Return the ldapId of the user with the given db Id
   */
  public String getUserDetailByDBId(int id) {
    return idAsString(id);
  }

  /**
   * Return the UserDetail of the user with the given ldap Id
   */
  public UserFull getUserFull(String sUserId) {
    try {
      return getAdminService().getUserFull(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getUserFull",
          "admin.EX_ERR_GET_USER_DETAIL", "user Id : '" + sUserId + "'", e);
      return null;
    }
  }

  /**
   * Return the UserDetail of the user with the given ldap Id
   */
  public UserDetail getUserDetail(String sUserId) {
    try {
      UserDetail userDetail = getAdminService().getUserDetail(sUserId);

      return userDetail;
    } catch (Exception e) {
      SilverTrace.warn("admin", "OrganizationController.getUserDetail",
          "admin.EX_ERR_GET_USER_DETAIL", "user Id : '" + sUserId + "'", e);
      return null;
    }
  }

  /**
   * Return an array of UserDetail corresponding to the given user Id array
   */
  public UserDetail[] getUserDetails(String[] asUserIds) {
    try {
      return getAdminService().getUserDetails(asUserIds);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getUserDetails",
          "admin.EX_ERR_GET_USER_DETAILS", e);
      return null;
    }
  }

  /**
   * @deprecated use getAllUsers(String componentId) Return all the users allowed to acces the given
   * component of the given space
   */
  public UserDetail[] getAllUsers(String sPrefixTableName, String sComponentName) {
    try {
      if (sComponentName != null) {
        return getAdminService().getUsers(true, null, sPrefixTableName, sComponentName);
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsers",
          "admin.MSG_ERR_GET_USERS_FOR_PROFILE_AND_COMPONENT", "space Id: '" +
           sPrefixTableName + "' component Id: '" + sComponentName, e);

    }
    return null;
  }

  /**
   * Return all the users allowed to acces the given component
   */
  public UserDetail[] getAllUsers(String componentId) {
    try {
      if (componentId != null) {
        return getAdminService().getUsers(true, null, null, componentId);
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsers",
          "admin.MSG_ERR_GET_USERS_FOR_PROFILE_AND_COMPONENT", "componentId: '" + componentId, e);
    }
    return null;
  }
  
  /**
   * Gets all the users that belong to the specified domain.
   * @param domainId the unique identifier of the domain.
   * @return an array of UserDetail objects or null if no such domain exists.
   */
  public UserDetail[] getAllUsersInDomain(String domainId) {
    try {
      if (domainId != null) {
        return getAdminService().getUsersOfDomain(domainId);
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsersInDomain",
          "admin.EX_ERR_GET_USER_DETAILS", "domainId: '" + domainId, e);
    }
    return null;
  }
  
   /**
   * Searches the users that match the specified criteria.
   * @param criteria the criteria in searching of user details.
   * @return an array of user details matching the criteria or an empty array of no ones are found.
   * @throws AdminException if an error occurs while getting the user details.
   */
  public UserDetail[] searchUsers(final SearchCriteria criteria) {
    try {
      return getAdminService().searchUsers(criteria);
    } catch(AdminException ex) {
      SilverTrace.error("admin", "OrganizationController.getUsersMatchingCriteria",
          "admin.EX_ERR_GET_USER_DETAILS", "criteria: '" + criteria.toString(), ex);
    }
    return null;
  }
  
  /**
   * Gets all the user groups that belong to the specified domain.
   * @param domainId the unique identifier of the domain.
   * @return an array of Group objects or null if no such domain exists.
   */
  public Group[] getAllRootGroupsInDomain(String domainId) {
    try {
      if (domainId != null) {
        return getAdminService().getRootGroupsOfDomain(domainId);
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsersInDomain",
          "admin.EX_ERR_GET_USER_DETAILS", "domainId: '" + domainId, e);
    }
    return null;
  }

  /**
   * For use in userPanel : return the users that are direct child of a given group
   */
  public UserDetail[] getFiltredDirectUsers(String sGroupId,
      String sUserLastNameFilter) {
    try {
      return getAdminService().getFiltredDirectUsers(sGroupId, sUserLastNameFilter);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getFiltredDirectUsers",
          "admin.EX_ERR_GET_USER_DETAILS", e);
      return null;
    }
  }

  /**
   * Return an array of UserDetail corresponding to the founded users
   */
  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd) {
    try {
      return getAdminService().searchUsers(modelUser, isAnd);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.searchUsers",
          "admin.EX_ERR_GET_USER_DETAILS", e);
      return null;
    }
  }

  /**
   * Return an array of Group corresponding to the founded groups
   */
  public Group[] searchGroups(Group modelGroup, boolean isAnd) {
    try {
      return getAdminService().searchGroups(modelGroup, isAnd);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.searchGroups",
          "admin.EX_ERR_GET_USER_DETAILS", e);
      return null;
    }
  }

  /**
   * For use in userPanel : return the total number of users recursivly contained in a group
   */
  public int getAllSubUsersNumber(String sGroupId) {
    try {
      return getAdminService().getAllSubUsersNumber(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSubUsersNumber",
          "admin.EX_ERR_GET_USER_DETAILS", e);
      return 0;
    }
  }

  /**
   * For use in userPanel : return the direct sub-groups
   */
  public Group[] getAllSubGroups(String parentGroupId) {
    try {
      return getAdminService().getAllSubGroups(parentGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSubGroups",
          "admin.EX_ERR_GET_USER_DETAILS", e);
      return new Group[0];
    }
  }

  /**
   * Return all the users of Silverpeas
   */
  public UserDetail[] getAllUsers() {
    try {
      UserDetail[] aUserDetail = null;
      String[] asUserIds = getAdminService().getAllUsersIds();
      if (asUserIds != null) {
        aUserDetail = getAdminService().getUserDetails(asUserIds);

      }
      return aUserDetail;
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsers",
          "admin.MSG_ERR_GET_ALL_USERS", e);
      return null;
    }
  }

  /**
   * Return all the users with the given profile allowed to access the given component of the given
   * space
   */
  public UserDetail[] getUsers(String sPrefixTableName, String sComponentName,
      String sProfile) {
    try {
      UserDetail[] aUserDetail = null;

      if (sPrefixTableName != null && sComponentName != null) {
        aUserDetail = getAdminService().getUsers(false, sProfile, sPrefixTableName,
            sComponentName);

      }
      return aUserDetail;
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getUsers",
          "admin.MSG_ERR_GET_USERS_FOR_PROFILE_AND_COMPONENT", "profile: '" +
           sProfile + "', space Id: '" + sPrefixTableName +
           "' component Id: '" + sComponentName, e);
      return null;
    }
  }

  public String[] getUserProfiles(String userId, String componentId) {
    try {
      return getAdminService().getCurrentProfiles(userId, componentId);
    } catch (Exception e) {
      if (!isToolAvailable(componentId)) {
        SilverTrace.error("admin", "OrganizationController.getUserProfiles",
            "admin.MSG_ERR_GET_PROFILES_FOR_USER_AND_COMPONENT", "userId: '" +
                userId + "', componentId: '" + componentId + "'", e);
      }
      return null;
    }
  }

  public String[] getUserProfiles(String userId, String componentId,
      int objectId, ObjectType objectType) {
    try {
      return getAdminService().getProfilesByObjectAndUserId(objectId, objectType.getCode(),
          componentId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getUserProfiles",
          "admin.MSG_ERR_GET_PROFILES_FOR_USER_AND_COMPONENT", "userId = " +
           userId + ", componentId = " + componentId + ", objectId = " +
           objectId, e);
      return null;
    }
  }
  
  public ProfileInst getUserProfile(String profileId) {
    try {
      return getAdminService().getProfileInst(profileId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getUserProfile",
          "admin.MSG_ERR_GET_PROFILE", "profileId: '" +
           profileId, e);
      return null;
    }
  }

  /**
   * Return all administrators ids
   */
  public String[] getAdministratorUserIds(String fromUserId) {
    try {
      return getAdminService().getAdministratorUserIds(fromUserId);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getAdministratorUserIds",
          "admin.MSG_ERR_GET_ALL_ADMIN_IDS", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  // -------------------------------------------------------------------
  // GROUPS QUERIES
  // -------------------------------------------------------------------
  /**
   * Return the Group of the group with the given Id
   */
  public Group getGroup(String sGroupId) {
    try {
      Group group = getAdminService().getGroup(sGroupId);

      return group;
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getGroup",
          "admin.EX_ERR_GET_GROUP", "group Id : '" + sGroupId + "'", e);
      return null;
    }
  }

  /**
   * Return all groups specified by the groupsIds
   */
  public Group[] getGroups(String[] groupsId) {
    Group[] retour = null;
    try {
      retour = getAdminService().getGroups(groupsId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getGroups",
          "admin.EX_ERR_GET_GROUP", "", e);
    }
    return retour;
  }

  /**
   * Return all the groups of silverpeas
   */
  public Group[] getAllGroups() {
    try {
      Group[] aGroup = null;
      String[] asGroupIds = getAdminService().getAllGroupIds();

      if (asGroupIds != null) {
        aGroup = getAdminService().getGroups(asGroupIds);

      }
      return aGroup;
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllGroups",
          "admin.MSG_ERR_GET_ALL_GROUPS", e);
      return null;
    }
  }

  /**
   * Return all root groups of silverpeas or null if an error occured when getting the root groups.
   */
  public Group[] getAllRootGroups() {
    try {
      return getAdminService().getAllRootGroups();
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllRootGroups",
          "admin.MSG_ERR_GET_ALL_GROUPS", e);
      return null;
    }
  }

  /**
   * Get ALL the users that are in a group or his sub groups
   */
  public UserDetail[] getAllUsersOfGroup(String groupId) {
    SilverTrace.info("admin", "OrganizationController.getAllUsersOfGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    try {
      return getAdminService().getAllUsersOfGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsersOfGroup",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return EMPTY_USER_DETAIL_ARRAY;
    }
  }

  /**
   * Get path to Group
   */
  public List<String> getPathToGroup(String groupId) {
    SilverTrace.info("admin", "OrganizationController.getPathToGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    try {
      return getAdminService().getPathToGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getPathToGroup",
          "admin.EX_ERR_GET_ALL_GROUPS", e);
      return new ArrayList<String>();
    }
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

  // -------------------------------------------------------------------
  // RE-INDEXATION
  // -------------------------------------------------------------------
  public String[] getAllSpaceIds(String sUserId) {
    try {
      return getAdminService().getAllSpaceIds(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSpaceIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SPACE_IDS", "user Id: '" + sUserId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the spaces Id manageable by given user in Silverpeas
   */
  public String[] getUserManageableSpaceIds(String sUserId) {
    try {
      String[] asSpaceIds = getAdminService().getUserManageableSpaceIds(sUserId);

      return asSpaceIds;
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getUserManageableSpaceIds",
          "admin.MSG_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id: '" + sUserId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the root spaceIds
   */
  public String[] getAllRootSpaceIds() {
    try {
      return getAdminService().getAllRootSpaceIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSpaceIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SPACE_IDS", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the root spaceIds available for the user sUserId
   */
  public String[] getAllRootSpaceIds(String sUserId) {
    try {
      return getAdminService().getAllRootSpaceIds(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSpaceIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SPACE_IDS", "user Id: '" + sUserId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the subSpaces Id available in webactiv given a space id (driver format)
   */
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId) {
    try {
      return getAdminService().getAllSubSpaceIds(sSpaceId, sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllSubSpaceIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SUBSPACE_IDS", "user Id: '" +
           sUserId + "', father space id: '" + sSpaceId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the components Id available in webactiv given a space id (driver format)
   */
  public String[] getAllComponentIds(String sSpaceId) {
    try {
      return getAdminService().getAllComponentIds(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllComponentIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "space id=" + sSpaceId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the components Id recursively available in webactiv given a space id (driver format)
   */
  public String[] getAllComponentIdsRecur(String sSpaceId) {
    try {
      return getAdminService().getAllComponentIdsRecur(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllComponentIdsRecur",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "spaceId = " + sSpaceId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces or all
   * silverpeas) available in silverpeas given a userId and a componentNameRoot
   *
   * @author dlesimple
   * @param sSpaceId
   * @param sUserId
   * @param sComponentRootName
   * @param inCurrentSpace
   * @param inAllSpaces
   * @return Array of componentsIds
   */
  public String[] getAllComponentIdsRecur(String sSpaceId, String sUserId,
      String sComponentRootName, boolean inCurrentSpace, boolean inAllSpaces) {
    try {
      return getAdminService().getAllComponentIdsRecur(sSpaceId,
          sUserId, sComponentRootName, inCurrentSpace, inAllSpaces);
    } catch (Exception e) {
      return EMPTY_STRING_ARRAY;
    }
  }

  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName) {
    try {
      return getAdminService().getRootSpacesContainingComponent(userId, componentName);
    } catch (AdminException e) {
      SilverTrace.error("admin",
          "OrganizationController.getRootSpacesContainingComponent",
          "admin.MSG_ERR_GET_ROOT_SPACES", "userId = " + userId + ", componentName = " +
           componentName, e);
      return new ArrayList<SpaceInstLight>();
    }
  }

  public List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName) {
    try {
      return getAdminService().getSubSpacesContainingComponent(spaceId, userId, componentName);
    } catch (AdminException e) {
      SilverTrace.error("admin",
          "OrganizationController.getSubSpacesContainingComponent",
          "admin.MSG_ERR_GET_SUB_SPACES", "spaceId = " + spaceId + ", userId = " + userId +
           ", componentName = " + componentName, e);
      return new ArrayList<SpaceInstLight>();
    }
  }

  public boolean isToolAvailable(String toolId) {
    boolean isToolAvailable = false;
    try {
      isToolAvailable =
          GeneralPropertiesManager.getStringCollection("availableToolIds").contains(toolId);
    } catch (Exception e) {
      isToolAvailable = false;
      SilverTrace.error("admin", "OrganizationController.isToolAvailable",
          "admin.MSG_ERR_GET_AVAILABLE_TOOL_IDS", "toolId: '" + toolId + "'");
    }
    return isToolAvailable;
  }

  public boolean isComponentAvailable(String componentId, String userId) {
    try {
      return getAdminService().isComponentAvailable(componentId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.isComponentAvailable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id: '" +
           userId + "', componentId: '" + componentId + "'", e);
      return false;
    }
  }

  public boolean isComponentExist(String componentId) {
    try {
      return getAdminService().getComponentInstLight(componentId) != null;
    } catch (AdminException ex) {
      SilverTrace.error("admin", "OrganizationController.isComponentExist",
          "admin.EX_ERR_GET_COMPONENT", "componentId: '" + componentId + "'", ex);
      return false;
    }
  }

  public boolean isComponentManageable(String componentId, String userId) {
    try {
      return getAdminService().isComponentManageable(componentId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.isComponentManageable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id: '" +
           userId + "', componentId: '" + componentId + "'", e);
      return false;
    }
  }

  public boolean isSpaceAvailable(String spaceId, String userId) {
    try {
      return getAdminService().isSpaceAvailable(userId, spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.isSpaceAvailable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SPACE_IDS", "user Id: '" + userId +
           "', spaceId: '" + spaceId + "'", e);
      return false;
    }
  }

  public boolean isObjectAvailable(int objectId, ObjectType objectType,
      String componentId, String userId) {
    try {
      return getAdminService().isObjectAvailable(componentId, objectId, objectType.getCode(), userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.isObjectAvailable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_OBJECT", "userId = " + userId +
           ", componentId = " + componentId + ", objectId = " + objectId, e);
      return false;
    }
  }

  public List<SpaceInstLight> getSpaceTreeview(String userId) {
    try {
      return getAdminService().getUserSpaceTreeview(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getSpaceTreeview",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SPACES", "user Id = " + userId, e);
      return new ArrayList<SpaceInstLight>();
    }
  }

  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) {
    try {
      return getAdminService().getAllowedSubSpaceIds(userId, spaceFatherId);
    } catch (AdminException e) {
      SilverTrace.error("admin", "OrganizationController.getSpaceTreeview",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SUBSPACE_IDS", "user Id = " + userId + ", spaceId = " +
           spaceFatherId, e);
      return EMPTY_STRING_ARRAY;
    }
  }

  public SpaceInstLight getRootSpace(String spaceId) {
    try {
      return getAdminService().getRootSpace(spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getRootSpace",
          "admin.MSG_ERR_GET_USER_AVAILABLE_SPACE", "spaceId = " + spaceId, e);
      return null;
    }
  }

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------
  /**
   * Return all the users of Silverpeas
   */
  public String[] getAllUsersIds() {
    try {
      return getAdminService().getAllUsersIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllUsersIds",
          "admin.MSG_ERR_GET_ALL_USERS", e);
      return null;
    }
  }

  /**
   * Return all the users of Silverpeas
   */
  public String[] searchUsersIds(String groupId, String componentId,
      String[] profileId, UserDetail filterUser) {
    try {
      return getAdminService().searchUsersIds(groupId, componentId, profileId, filterUser);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.searchUsersIds",
          "admin.MSG_ERR_GET_ALL_USERS", e);
      return null;
    }
  }

  /**
   * Return userIds according to a list of profile names
   *
   * @param componentId the instance id
   * @param profileNames the list which contains the profile names
   * @return a string array of user id
   */
  public String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames) {
    try {
      ComponentInst componentInst = getComponentInst(componentId);

      List<ProfileInst> profiles = componentInst.getAllProfilesInst();
      List<String> profileIds = new ArrayList<String>();
      for (ProfileInst profileInst : profiles) {
        if (profileNames.contains(profileInst.getName())) {
          profileIds.add(profileInst.getId());
          SilverTrace.info("admin",
              "OrganizationController.getUsersIdsByRoleNames",
              "root.MSG_GEN_PARAM_VALUE", "profileName = " +
               profileInst.getName() + ", profileId = " +
               profileInst.getId());
        }
      }

      if (!profileIds.isEmpty()) {
        String[] pIds = profileIds.toArray(new String[profileIds.size()]);
        SilverTrace.info("admin", "OrganizationController.getUsersIdsByRoleNames",
            "root.MSG_GEN_PARAM_VALUE", "pIds = " + Arrays.toString(pIds));
        return getAdminService().searchUsersIds(null, null, pIds, new UserDetail());
      }
      return new String[0];
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getUsersIdsByRoleNames",
          "admin.MSG_ERR_GET_ALL_USERS", e);
      return null;
    }
  }

  public String[] getUsersIdsByRoleNames(String componentId, String objectId,
      ObjectType objectType, List<String> profileNames) {
    SilverTrace.info("admin", "OrganizationController.getUsersIdsByRoleNames",
        "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId +
         ", objectId = " + objectId);
    try {
      List<ProfileInst> profiles = getAdminService().getProfilesByObject(objectId, objectType.
          getCode(),
          componentId);
      List<String> profileIds = new ArrayList<String>();
      for (ProfileInst profile : profiles) {
        if (profile != null && profileNames.contains(profile.getName())) {
          profileIds.add(profile.getId());
        }
      }

      SilverTrace.info("admin",
          "OrganizationController.getUsersIdsByRoleNames",
          "root.MSG_GEN_PARAM_VALUE", "profileIds = " + profileIds.toString());

      if (profileIds.isEmpty()) {
        return EMPTY_STRING_ARRAY;
      } // else return all users !!

      return getAdminService().searchUsersIds(null, null, profileIds.toArray(new String[profileIds.
          size()]),
          new UserDetail());
    } catch (Exception e) {
      SilverTrace.error("admin",
          "OrganizationController.getUsersIdsByRoleNames",
          "admin.MSG_ERR_GET_ALL_USERS", e);
      return null;
    }
  }

  public String[] searchGroupsIds(boolean isRootGroup, String componentId,
      String[] profileId, Group modelGroup) {
    try {
      return getAdminService().searchGroupsIds(isRootGroup, componentId, profileId, modelGroup);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.searchGroupsIds",
          "admin.MSG_ERR_GET_ALL_USERS", e);
      return null;
    }
  }

  /**
   * Get a domain with given id
   */
  public Domain getDomain(String domainId) {
    try {
      return getAdminService().getDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getDomain", "admin.EX_ERR_GET_DOMAIN", e);
      return null;
    }
  }

  /**
   * Get all domains
   */
  public Domain[] getAllDomains() {
    try {
      return getAdminService().getAllDomains();
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllDomain", "admin.EX_ERR_GET_DOMAIN", e);
      return null;
    }
  }

  public String[] getDirectGroupIdsOfUser(String userId) {
    try {
      return getAdminService().getDirectGroupsIdsOfUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getDirectGroupIdsOfUser",
          "admin.MSG_ERR_GET_ALL_GROUPS", e);
      return EMPTY_STRING_ARRAY;
    }

  }

  private ArrayList<String> recursiveMajListGroupId(String idGroup, ArrayList<String> listRes) {
    Group group = getGroup(idGroup);
    if (group.getSuperGroupId() != null) {
      listRes = recursiveMajListGroupId(group.getSuperGroupId(), listRes);
    }
    listRes.add(idGroup);
    return listRes;
  }

  public String[] getAllGroupIdsOfUser(String userId) {
    try {
      ArrayList<String> listRes = new ArrayList<String>();
      String[] tabGroupIds = getAdminService().getDirectGroupsIdsOfUser(userId);
      for (String groupId : tabGroupIds) {
        listRes = recursiveMajListGroupId(groupId, listRes);
      }
      return listRes.toArray(new String[listRes.size()]);
    } catch (Exception e) {
      SilverTrace.error("admin", "OrganizationController.getAllGroupIdsOfUser",
          "admin.MSG_ERR_GET_ALL_GROUPS", e);
      return EMPTY_STRING_ARRAY;
    }
  }

  public void reloadAdminCache() {
    getAdminService().reloadCache();
  }

  /**
   * Is the anonymous access is activated for the running Silverpeas? When the anonymous access is
   * activated, then a specific user for anonymous access should be set; all anonym accesses to the
   * running Silverpeas are done with this user profile.
   *
   * @return true if the anonym access is activated, false otherwise.
   */
  public boolean isAnonymousAccessActivated() {
    return UserDetail.isAnonymousUserExist();
  }

  public String[] getAllowedComponentIds(String userId) {
    try {
      return getAdminService().getAvailCompoIds(userId);
    } catch (AdminException e) {
      SilverTrace.error("admin", "OrganizationController.getAllowedComponentIds",
          "admin.MSG_ERR_GET_AVAILABLE_COMPONENTIDS", e);
      return EMPTY_STRING_ARRAY;
    }
  }
}
