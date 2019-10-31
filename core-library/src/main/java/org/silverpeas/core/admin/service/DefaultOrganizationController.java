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
/*
 * @author Norbert CHAIX
 * @version 1.0
 * date 14/09/2000
 */
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.Manager;
import static org.silverpeas.core.util.ArrayUtil.EMPTY_STRING_ARRAY;

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
      return getAdminService().getAllSubSpaceIds(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getSpaceNames(String[] asSpaceIds) {
    try {
      return getAdminService().getSpaceNames(asSpaceIds);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(String spaceId) {
    try {
      return getAdminService().getSpaceInstLightById(spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
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
     return getAdminService().getAvailCompoIdsAtRoot(sClientSpaceId, sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
      return new ArrayList<>();
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

  @Override
  public List<ComponentInstLight> getComponentsWithParameterValue(String param, String value) {
    return getAdminService().getComponentsWithParameter(param, value);
  }

  // -------------------------------------------------------------------
  // COMPONENTS QUERIES
  // -------------------------------------------------------------------

  @Override
  public Optional<SilverpeasComponentInstance> getComponentInstance(
      final String componentInstanceIdentifier) {
    try {
      if (StringUtil.isDefined(componentInstanceIdentifier)) {
        return Optional
            .ofNullable(getAdminService().getComponentInstance(componentInstanceIdentifier));
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return Optional.empty();
    }
  }

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
      return getAdminService().getUserDetail(sUserId);
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
      return new UserDetail[0];
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
    return new UserDetail[0];
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
    return new UserDetail[0];
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
    return new UserDetail[0];
  }

  @Override
  public <T extends User> List<T> getUsersOfDomains(List<String> domainIds) {
    try {
      return (List<T>) getAdminService().getUsersOfDomains(domainIds);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public <T extends User> List<T> getUsersOfDomainsFromNewestToOldest(List<String> domainIds) {
    try {
      return (List<T>) getAdminService().getUsersOfDomainsFromNewestToOldest(domainIds);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public <T extends User> ListSlice<T> searchUsers(final UserDetailsSearchCriteria criteria) {
    try {
      return (ListSlice<T>) getAdminService().searchUsers(criteria);
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
    return new Group[0];
  }

  @Override
  public UserDetail[] getFilteredDirectUsers(String sGroupId, String sUserLastNameFilter) {
    try {
      return getAdminService().getFiltredDirectUsers(sGroupId, sUserLastNameFilter);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new UserDetail[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Group> SilverpeasList<T> searchGroups(final GroupsSearchCriteria criteria) {
    try {
      return (SilverpeasList<T>) getAdminService().searchGroups(criteria);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return null;
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
  public Group[] getRecursivelyAllSubgroups(final String parentGroupId) {
    try {
      return getAdminService().getRecursivelyAllSubGroups(parentGroupId);
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
      return new UserDetail[0];
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
      return Collections.emptyList();
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
      return new UserDetail[0];
    }
  }

  @Override
  public Collection<SilverpeasRole> getUserSilverpeasRolesOn(final User user,
      final String componentInstanceIdentifier) {
    Optional<PersonalComponentInstance> personalComponentInstance =
        PersonalComponentInstance.from(componentInstanceIdentifier);
    if (personalComponentInstance.isPresent()) {
      return personalComponentInstance.get().getSilverpeasRolesFor(user);
    }
    Set<SilverpeasRole> silverpeasRoles =
        SilverpeasRole.from(getUserProfiles(user.getId(), componentInstanceIdentifier));
    silverpeasRoles.remove(Manager);
    return silverpeasRoles;
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId) {
    try {
      return getAdminService().getCurrentProfiles(userId, componentId);
    } catch (Exception e) {
      if (!isToolAvailable(componentId)) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
      return new String[0];
    }
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId, ProfiledObjectId objectId) {
    try {
      return getAdminService().getProfilesByObjectAndUserId(objectId, componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
    }
  }

  @Override
  public Map<Integer, List<String>> getUserObjectProfiles(final String userId,
      final String componentId, final ProfiledObjectType objectType) {
    try {
      return getAdminService().getProfilesByObjectTypeAndUserId(objectType.getCode(),
          componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<ProfileInst> getUserProfiles(String componentId, ProfiledObjectId objectId) {
    try {
      return getAdminService().getProfilesByObject(objectId, componentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return Collections.emptyList();
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
      return getAdminService().getGroup(sGroupId);
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
      List<GroupDetail> groups = getAdminService().getAllGroups();
      return groups.toArray(new Group[groups.size()]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @Override
  public Group[] getAllRootGroups() {
    try {
      List<GroupDetail> groups = getAdminService().getAllRootGroups();
      return groups.toArray(new Group[groups.size()]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) {

    try {
      return getAdminService().getAllUsersOfGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new UserDetail[0];
    }
  }

  @Override
  public List<String> getPathToGroup(String groupId) {

    try {
      return getAdminService().getPathToGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new ArrayList<>();
    }
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
      return getAdminService().getUserManageableSpaceIds(sUserId);
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

  @Override
  public boolean isComponentAvailable(final String componentId, final String userId) {
    return isComponentAvailableToUser(componentId, userId);
  }

  /**
   * Is the specified component instance available among the components instances accessible by the
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
  public boolean isComponentAvailableToUser(String componentId, String userId) {
    try {
      return getAdminService().isComponentAvailableToUser(componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isComponentAvailableToGroup(String componentId, String groupId) {
    try {
      return getAdminService().isComponentAvailableToGroup(componentId, groupId);
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
  public boolean isObjectAvailableToUser(ProfiledObjectId objectId, String componentId, String userId) {
    try {
      return getAdminService().isObjectAvailableToUser(componentId, objectId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isObjectAvailableToGroup(ProfiledObjectId objectId, String componentId, String groupId) {
    try {
      return getAdminService().isObjectAvailableToGroup(componentId, objectId, groupId);
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
      return new String[0];
    }
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames) {
    try {
      List<String> userIds;
      ComponentInst componentInst = getComponentInst(componentId);

      List<ProfileInst> profiles = componentInst.getAllProfilesInst();
      List<String> profileIds = profiles.stream()
          .filter(p -> profileNames.contains(p.getName()))
          .map(ProfileInst::getId)
          .collect(Collectors.toList());

      if (profileIds.isEmpty()) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }

      userIds = getAdminService().searchUserIdsByProfile(profileIds);
      return userIds.toArray(new String[userIds.size()]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
    }
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId, List<String> profileNames) {

    try {
      List<ProfileInst> profiles = getAdminService().getProfilesByObject(objectId, componentId);
      List<String> profileIds = new ArrayList<>();
      for (ProfileInst profile : profiles) {
        if (profile != null && profileNames.contains(profile.getName())) {
          profileIds.add(profile.getId());
        }
      }

      if (profileIds.isEmpty()) {
        return EMPTY_STRING_ARRAY;
      } // else return all users !!

      List<String> userIds = getAdminService().searchUserIdsByProfile(profileIds);
      return userIds.toArray(new String[userIds.size()]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
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
      return new Domain[0];
    }
  }

  @Override
  public List<GroupDetail> getDirectGroupsOfUser(String userId) {
    try {
      return getAdminService().getDirectGroupsOfUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return Collections.emptyList();
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
      ArrayList<String> listRes = new ArrayList<>();
      List<GroupDetail> groups = getAdminService().getDirectGroupsOfUser(userId);
      for (GroupDetail group : groups) {
        listRes = recursiveMajListGroupId(group.getId(), listRes);
      }
      return listRes.toArray(new String[listRes.size()]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return EMPTY_STRING_ARRAY;
    }
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
    final List<String> componentIds;
    if (criteria.hasCriterionOnWorkspace()) {
      if (criteria.hasCriterionOnComponentInstances()) {
        componentIds = new ArrayList<>();
        componentIds.addAll(criteria.getComponentInstanceIds());
      } else {
        String[] availableComponentIds = getAvailCompoIds(criteria.getWorkspaceId(),
            criteria.getUser().getId());
        componentIds = Stream.of(availableComponentIds)
            .filter(c -> isSearchable(c, null))
            .collect(Collectors.toList());
      }
    } else {
      String[] availableComponentIds = getAvailCompoIds(criteria.getUser().getId());
      List<String> excludedComponentIds = getComponentsExcludedFromGlobalSearch(
          criteria.getUser().getId());
      componentIds = Stream.of(availableComponentIds)
          .filter(c -> isSearchable(c, excludedComponentIds))
          .collect(Collectors.toList());
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
      return !isPrivateSearch;
    } else {
      return true;
    }
  }

  private List<String> getComponentsExcludedFromGlobalSearch(String userId) {
    List<String> excluded = new ArrayList<>();

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
    List<String> items = new ArrayList<>();
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

  @Override
  public SpaceProfile getSpaceProfile(String spaceId, SilverpeasRole role) {
    try {
      return getAdminService().getSpaceProfile(spaceId, role);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException {
    return getAdminService().getFullTreeview();
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(String userId) throws AdminException {
    return getAdminService().getAllowedFullTreeview(userId);
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(String userId, String spaceId)
      throws AdminException {
    return getAdminService().getAllowedFullTreeview(userId, spaceId);
  }

  @Override
  public List<SpaceInstLight> getPathToSpace(String spaceId) {
    return getPathToSpace(new ArrayList<SpaceInstLight>(), spaceId);
  }

  private List<SpaceInstLight> getPathToSpace(List<SpaceInstLight> path, String spaceId) {
    try {
      SpaceInstLight spaceInst = getAdminService().getSpaceInstLightById(spaceId);
      if (spaceInst != null) {
        path.add(0, spaceInst);
        if (!spaceInst.isRoot()) {
          path = getPathToSpace(path, spaceInst.getFatherId());
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return path;
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(String componentId) {
    Optional<SilverpeasComponentInstance> componentInstance = getComponentInstance(componentId);
    if (componentInstance.isPresent() && !componentInstance.get().isPersonal()) {
      return getPathToSpace(componentInstance.get().getSpaceId());
    }
    return new ArrayList<>();
  }
}