/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.silverpeas.core.util.ArrayUtil.EMPTY_USER_DETAIL_ARRAY;

/**
 * This object is used by all the admin jsp such as SpaceManagement, UserManagement,
 * etc... It provides access functions to query and modify the domains as well as the company
 * organization It should be used only by a client that has the administrator rights. The
 * OrganizationController extends {@link AdministrationServiceProvider]} that maintains a static
 * reference to an {@link Administration} instance. During the initialization of the Admin
 * instance, some computations require services published by the underlying IoC container. So,
 * an instance of OrganizationController is created by the IoC container and published under the
 * name 'organizationController' so that the initialization of the static Admin instance can be
 * performed correctly within the execution context of IoC container.
 */
@Singleton
public class DefaultOrganizationController implements OrganizationController {

  private static final long serialVersionUID = 3435241972671610107L;
  private static final Set<String> toolIds = new LinkedHashSet<>(5);

  @Inject
  private Administration admin;

  /**
   * Constructor declaration
   */
  public DefaultOrganizationController() {
  }

  // -------------------------------------------------------------------
  // SPACES QUERIES
  // -------------------------------------------------------------------
  @Override
  public String[] getAllSpaceIds() {
    try {
      return getAdminService().getAllSpaceIds();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId) {
    try {
      String[] asSubSpaceIds = getAdminService().getAllSubSpaceIds(sSpaceId);

      return asSubSpaceIds;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getSpaceNames(String[] asSpaceIds) {
    try {
      String[] asSpaceNames = getAdminService().getSpaceNames(asSpaceIds);
      return asSpaceNames;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(String spaceId) {
    try {
      SpaceInstLight spaceLight = getAdminService().getSpaceInstLightById(spaceId);
      return spaceLight;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String getGeneralSpaceId() {
    try {
      return getAdminService().getGeneralSpaceId();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return "";
    }
  }

  @Override
  public SpaceInst getSpaceInstById(String sSpaceId) {
    try {
      return getAdminService().getSpaceInstById(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {
    try {
      return getAdminService().getAvailCompoIds(sClientSpaceId, sUserId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAvailCompoIds(String sUserId) {
    try {
      return getAdminService().getAvailCompoIds(sUserId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) {
    try {
      String[] asCompoIds = getAdminService().getAvailCompoIdsAtRoot(sClientSpaceId,
          sUserId);

      return asCompoIds;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public Map<String, WAComponent> getAllComponents() {
    try {
      return getAdminService().getAllComponents();
    } catch (Exception e) {
      if (!(e instanceof AdminException && ((AdminException) e).isAlreadyPrinted())) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
      return new HashMap<>();
    }
  }

  @Override
  public CompoSpace[] getCompoForUser(String sUserId, String sCompoName) {
    try {
      return getAdminService().getCompoForUser(sUserId, sCompoName);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new CompoSpace[0];
    }
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName) {
    try {
      return getAdminService().getAvailComponentInstLights(userId, componentName);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new ArrayList<ComponentInstLight>();
    }

  }

  @Override
  public String[] getComponentIdsForUser(String sUserId, String sCompoName) {
    try {
      return getAdminService().getComponentIdsByNameAndUserId(sUserId, sCompoName);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getCompoId(String sCompoName) {
    try {
      return getAdminService().getCompoId(sCompoName);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String getComponentParameterValue(String sComponentId, String parameterName) {
    return getAdminService().getComponentParameterValue(sComponentId, parameterName);
  }

  // -------------------------------------------------------------------
  // COMPONENTS QUERIES
  // -------------------------------------------------------------------
  @Override
  public ComponentInst getComponentInst(String sComponentId) {
    try {
      return getAdminService().getComponentInst(sComponentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<SpaceInst> getSpacePath(String spaceId) {
    return getSpacePath(new ArrayList<SpaceInst>(), spaceId);
  }

  @Override
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
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return path;
  }

  @Override
  public ComponentInstLight getComponentInstLight(String sComponentId) {
    try {
      return getAdminService().getComponentInstLight(sComponentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  // -------------------------------------------------------------------
  // USERS QUERIES
  // -------------------------------------------------------------------
  @Override
  public int getUserDBId(String sUserId) {
    return idAsInt(sUserId);
  }

  @Override
  public String getUserDetailByDBId(int id) {
    return idAsString(id);
  }

  @Override
  public UserFull getUserFull(String sUserId) {
    try {
      return getAdminService().getUserFull(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public UserDetail getUserDetail(String sUserId) {
    try {
      UserDetail userDetail = getAdminService().getUserDetail(sUserId);

      return userDetail;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public UserDetail[] getUserDetails(String[] asUserIds) {
    try {
      return getAdminService().getUserDetails(asUserIds);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public UserDetail[] getAllUsers(String sPrefixTableName, String sComponentName) {
    try {
      if (sComponentName != null) {
        return getAdminService().getUsers(true, null, sPrefixTableName, sComponentName);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);

    }
    return null;
  }

  @Override
  public UserDetail[] getAllUsers(String componentId) {
    try {
      if (componentId != null) {
        return getAdminService().getUsers(true, null, null, componentId);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public UserDetail[] getAllUsersInDomain(String domainId) {
    try {
      if (domainId != null) {
        return getAdminService().getUsersOfDomain(domainId);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public List<UserDetail> getUsersOfDomains(List<String> domainIds) {
    try {
      return getAdminService().getUsersOfDomains(domainIds);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(List<String> domainIds) {
    try {
      return getAdminService().getUsersOfDomainsFromNewestToOldest(domainIds);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ListSlice<UserDetail> searchUsers(final UserDetailsSearchCriteria criteria) {
    try {
      return getAdminService().searchUsers(criteria);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public Group[] getAllRootGroupsInDomain(String domainId) {
    try {
      if (domainId != null) {
        return getAdminService().getRootGroupsOfDomain(domainId);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public UserDetail[] getFiltredDirectUsers(String sGroupId, String sUserLastNameFilter) {
    try {
      return getAdminService().getFiltredDirectUsers(sGroupId, sUserLastNameFilter);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd) {
    try {
      return getAdminService().searchUsers(modelUser, isAnd);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public ListSlice<Group> searchGroups(final GroupsSearchCriteria criteria) {
    try {
      return getAdminService().searchGroups(criteria);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public Group[] searchGroups(Group modelGroup, boolean isAnd) {
    try {
      return getAdminService().searchGroups(modelGroup, isAnd);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public int getAllSubUsersNumber(String sGroupId) {
    try {
      return getAdminService().getAllSubUsersNumber(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return 0;
    }
  }

  @Override
  public Group[] getAllSubGroups(String parentGroupId) {
    try {
      return getAdminService().getAllSubGroups(parentGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @Override
  public UserDetail[] getAllUsers() {
    try {
      return getAdminService().getAllUsers().toArray(new UserDetail[0]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Return all the users of Silverpeas
   */
  @Override
  public List<UserDetail> getAllUsersFromNewestToOldest() {
    try {
      return getAdminService().getAllUsersFromNewestToOldest();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public UserDetail[] getUsers(String sPrefixTableName, String sComponentName, String sProfile) {
    try {
      UserDetail[] aUserDetail = null;

      if (sPrefixTableName != null && sComponentName != null) {
        aUserDetail = getAdminService().getUsers(false, sProfile, sPrefixTableName,
            sComponentName);

      }
      return aUserDetail;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId) {
    try {
      return getAdminService().getCurrentProfiles(userId, componentId);
    } catch (Exception e) {
      if (!isToolAvailable(componentId)) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
      return null;
    }
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId, int objectId,
      ObjectType objectType) {
    try {
      return getAdminService().getProfilesByObjectAndUserId(objectId, objectType.getCode(),
          componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<ProfileInst> getUserProfiles(String componentId, String objectId, String objectType) {
    try {
      return getAdminService().getProfilesByObject(objectId, objectType, componentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public ProfileInst getUserProfile(String profileId) {
    try {
      return getAdminService().getProfileInst(profileId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] getAdministratorUserIds(String fromUserId) {
    try {
      return getAdminService().getAdministratorUserIds(fromUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  // -------------------------------------------------------------------
  // GROUPS QUERIES
  // -------------------------------------------------------------------
  @Override
  public Group getGroup(String sGroupId) {
    try {
      Group group = getAdminService().getGroup(sGroupId);

      return group;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Group[] getGroups(String[] groupsId) {
    Group[] retour = null;
    try {
      retour = getAdminService().getGroups(groupsId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return retour;
  }

  @Override
  public Group[] getAllGroups() {
    try {
      Group[] aGroup = null;
      String[] asGroupIds = getAdminService().getAllGroupIds();

      if (asGroupIds != null) {
        aGroup = getAdminService().getGroups(asGroupIds);

      }
      return aGroup;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Group[] getAllRootGroups() {
    try {
      return getAdminService().getAllRootGroups();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) {

    try {
      return getAdminService().getAllUsersOfGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_USER_DETAIL_ARRAY;
    }
  }

  @Override
  public List<String> getPathToGroup(String groupId) {

    try {
      return getAdminService().getPathToGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
  @Override
  public String[] getAllSpaceIds(String sUserId) {
    try {
      return getAdminService().getAllSpaceIds(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getUserManageableSpaceIds(String sUserId) {
    try {
      String[] asSpaceIds = getAdminService().getUserManageableSpaceIds(sUserId);

      return asSpaceIds;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllRootSpaceIds() {
    try {
      return getAdminService().getAllRootSpaceIds();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllRootSpaceIds(String sUserId) {
    try {
      return getAdminService().getAllRootSpaceIds(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId) {
    try {
      return getAdminService().getAllSubSpaceIds(sSpaceId, sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) {
    try {
      return getAdminService().getAllComponentIds(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) {
    try {
      return getAdminService().getAllComponentIdsRecur(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId, String sUserId,
      String sComponentRootName, boolean inCurrentSpace, boolean inAllSpaces) {
    try {
      return getAdminService().getAllComponentIdsRecur(sSpaceId,
          sUserId, sComponentRootName, inCurrentSpace, inAllSpaces);
    } catch (Exception e) {
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName) {
    try {
      return getAdminService().getRootSpacesContainingComponent(userId, componentName);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName) {
    try {
      return getAdminService().getSubSpacesContainingComponent(spaceId, userId, componentName);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * Is the specified tool belongs to the administration component?
   * </p>
   * The administration component (or administrative console) forms a particular component made up
   * of several tools, each of them providing an administrative feature. Each tool in the
   * administration component have the same identifier that refers in fact the administration
   * console.
   *
   * @param toolId the unique identifier of the tool.
   * @return true if the tool belongs to the administration component.
   */
  @Override
  public boolean isAdminTool(String toolId) {
    return getAdminService().isAnAdminTool(toolId);
  }

  /**
   * Is the specified tool is available in Silverpeas?
   * </p>
   * A tool in Silverpeas is a singleton component that is dedicated to a given user. Each tool is
   * identified by a unique identifier and it is unique to each user.
   *
   * @param toolId the unique identifier of a tool.
   * @return true if the tool is available, false otherwise.
   */
  @Override
  public boolean isToolAvailable(String toolId) {
    boolean isToolAvailable;
    try {
      isToolAvailable = getAvailableToolIds().contains(toolId);
    } catch (Exception e) {
      isToolAvailable = false;
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return isToolAvailable;
  }

  /**
   * Is the specified component instance available among the components instances accessibles by the
   * specified user?
   * </p>
   * A component is an application in Silverpeas to perform some tasks and to manage some resources.
   * Each component in Silverpeas can be instanciated several times, each of them corresponding then
   * to a running application in Silverpeas and it is uniquely identified from others instances by a
   * given identifier.
   *
   * @param componentId the unique identifier of a component instance.
   * @param userId the unique identifier of a user.
   * @return true if the component instance is available, false otherwise.
   */
  @Override
  public boolean isComponentAvailable(String componentId, String userId) {
    try {
      return getAdminService().isComponentAvailable(componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isComponentExist(String componentId) {
    try {
      return getAdminService().getComponentInstLight(componentId) != null;
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isComponentManageable(String componentId, String userId) {
    try {
      return getAdminService().isComponentManageable(componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isSpaceAvailable(String spaceId, String userId) {
    try {
      return getAdminService().isSpaceAvailable(userId, spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isObjectAvailable(int objectId, ObjectType objectType, String componentId,
      String userId) {
    try {
      return getAdminService().isObjectAvailable(componentId, objectId, objectType.getCode(),
          userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public List<SpaceInstLight> getSpaceTreeview(String userId) {
    try {
      return getAdminService().getUserSpaceTreeview(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  @Override
  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) {
    try {
      return getAdminService().getAllowedSubSpaceIds(userId, spaceFatherId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public SpaceInstLight getRootSpace(String spaceId) {
    try {
      return getAdminService().getRootSpace(spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------
  @Override
  public String[] getAllUsersIds() {
    try {
      return getAdminService().getAllUsersIds();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] searchUsersIds(String groupId, String componentId, String[] profileId,
      UserDetail filterUser) {
    try {
      return getAdminService().searchUsersIds(groupId, componentId, profileId, filterUser);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames) {
    try {
      ComponentInst componentInst = getComponentInst(componentId);

      List<ProfileInst> profiles = componentInst.getAllProfilesInst();
      List<String> profileIds = new ArrayList<String>();
      for (ProfileInst profileInst : profiles) {
        if (profileNames.contains(profileInst.getName())) {
          profileIds.add(profileInst.getId());
        }
      }

      if (!profileIds.isEmpty()) {
        String[] pIds = profileIds.toArray(new String[profileIds.size()]);

        return getAdminService().searchUsersIds(null, null, pIds, new UserDetail());
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, String objectId, ObjectType objectType,
      List<String> profileNames) {

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

      if (profileIds.isEmpty()) {
        return EMPTY_STRING_ARRAY;
      } // else return all users !!

      return getAdminService().searchUsersIds(null, null, profileIds.toArray(new String[profileIds.
          size()]),
          new UserDetail());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] searchGroupsIds(boolean isRootGroup, String componentId, String[] profileId,
      Group modelGroup) {
    try {
      return getAdminService().searchGroupsIds(isRootGroup, componentId, profileId, modelGroup);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Domain getDomain(String domainId) {
    try {
      return getAdminService().getDomain(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Domain[] getAllDomains() {
    try {
      return getAdminService().getAllDomains();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String[] getDirectGroupIdsOfUser(String userId) {
    try {
      return getAdminService().getDirectGroupsIdsOfUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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

  @Override
  public String[] getAllGroupIdsOfUser(String userId) {
    try {
      ArrayList<String> listRes = new ArrayList<String>();
      String[] tabGroupIds = getAdminService().getDirectGroupsIdsOfUser(userId);
      for (String groupId : tabGroupIds) {
        listRes = recursiveMajListGroupId(groupId, listRes);
      }
      return listRes.toArray(new String[listRes.size()]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public void reloadAdminCache() {
    getAdminService().reloadCache();
  }

  @Override
  public boolean isAnonymousAccessActivated() {
    return UserDetail.isAnonymousUserExist();
  }

  @Override
  public String[] getAllowedComponentIds(String userId) {
    try {
      return getAdminService().getAvailCompoIds(userId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public List<String> getSearchableComponentsByCriteria(ComponentSearchCriteria criteria) {
    List<String> componentIds = new ArrayList<String>();

    if (criteria.hasCriterionOnWorkspace()) {
      if (criteria.hasCriterionOnComponentInstance()) {
        componentIds.add(criteria.getComponentInstanceId());
      } else {
        String[] availableComponentIds = getAvailCompoIds(criteria.getWorkspaceId(),
            criteria.getUser().getId());
        for (String aComponentId : availableComponentIds) {
          if (isSearchable(aComponentId, null)) {
            componentIds.add(aComponentId);
          }
        }
      }
    } else {
      String[] availableComponentIds = getAvailCompoIds(criteria.getUser().getId());
      List<String> excludedComponentIds = getComponentsExcludedFromGlobalSearch(
          criteria.getUser().getId());
      for (String aComponentId : availableComponentIds) {
        if (isSearchable(aComponentId, excludedComponentIds)) {
          componentIds.add(aComponentId);
        }
      }
    }
    return componentIds;
  }

  private boolean isSearchable(String componentId, List<String> exclusionList) {
    if (exclusionList != null && !exclusionList.isEmpty() && exclusionList.contains(componentId)) {
      return false;
    }
    if (componentId.startsWith("silverCrawler")
        || componentId.startsWith("gallery")
        || componentId.startsWith("kmelia")) {
      boolean isPrivateSearch = "yes".equalsIgnoreCase(
          getComponentParameterValue(componentId, "privateSearch"));
      if (isPrivateSearch) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  private List<String> getComponentsExcludedFromGlobalSearch(String userId) {
    List<String> excluded = new ArrayList<String>();

    // exclude all components of all excluded spaces
    List<String> spaces = getItemsExcludedFromGlobalSearch("SpacesExcludedFromGlobalSearch");
    for (String space : spaces) {
      String[] availableComponentIds = getAvailCompoIds(space, userId);
      excluded.addAll(Arrays.asList(availableComponentIds));
    }

    // exclude components explicitly excluded
    List<String> components =
        getItemsExcludedFromGlobalSearch("ComponentsExcludedFromGlobalSearch");
    excluded.addAll(components);

    return excluded;
  }

  private List<String> getItemsExcludedFromGlobalSearch(String parameterName) {
    SettingBundle searchSettings = ResourceLocator.getSettingBundle(
        "org.silverpeas.pdcPeas.settings.pdcPeasSettings");
    List<String> items = new ArrayList<String>();
    String param = searchSettings.getString(parameterName);
    if (StringUtil.isDefined(param)) {
      String[] componentIds = param.split(",");
      items.addAll(Arrays.asList(componentIds));
    }
    return items;
  }

  private Administration getAdminService() {
    return admin;
  }

  private static Set<String> getAvailableToolIds() {
    Collection<String> propertyValues = toolIds;
    if (toolIds.isEmpty()) {
      final String availableToolIds =
          ResourceLocator.getGeneralSettingBundle().getString("availableToolIds", "");
      if (!availableToolIds.isEmpty()) {
        for (String aToolId : availableToolIds.split("[ ,;]")) {
          if (aToolId != null && !aToolId.trim().isEmpty()) {
            toolIds.add(aToolId.trim());
          }
        }
      }
    }
    return toolIds;
  }
}
