/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.*;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.*;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.MANAGER;

@Service
@Singleton
public class DefaultOrganizationController implements OrganizationController {

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
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId) {
    try {
      return getAdminService().getAllSubSpaceIds(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getSpaceNames(String[] asSpaceIds) {
    try {
      return getAdminService().getSpaceNames(asSpaceIds);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
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
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAvailCompoIds(String sUserId) {
    try {
      return getAdminService().getAvailCompoIds(sUserId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) {
    try {
      return getAdminService().getAvailCompoIdsAtRoot(sClientSpaceId, sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
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
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getCompoId(String sCompoName) {
    try {
      return getAdminService().getCompoId(sCompoName);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
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

  @Override
  public Map<String, Map<String, String>> getParameterValuesByComponentIdThenByParamName(
      final Collection<String> componentIds, final Collection<String> paramNames) {
    return getAdminService().getParameterValuesByComponentIdThenByParamName(componentIds,
        paramNames);
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
      User requester = User.getCurrentRequester();
      return removeSensitiveData(getAdminService().getUserFull(sUserId), requester);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<UserFull> getUserFulls(final Collection<String> userIds) {
    try {
      return removeSensitiveData(getAdminService().getUserFulls(userIds));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return emptyList();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail getUserDetail(String sUserId) {
    try {
      User requester = User.getCurrentRequester();
      return removeSensitiveData(getAdminService().getUserDetail(sUserId), requester);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getUserDetails(String[] asUserIds) {
    try {
      return removeSensitiveData(getAdminService().getUserDetails(asUserIds));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new UserDetail[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getAllUsers(String componentId) {
    try {
      if (componentId != null) {
        return removeSensitiveData(getAdminService().getUsers(true, null, componentId));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return new UserDetail[0];
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getAllUsersInDomain(String domainId) {
    try {
      if (domainId != null) {
        return removeSensitiveData(getAdminService().getUsersOfDomain(domainId));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return new UserDetail[0];
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends User> List<T> getUsersOfDomains(List<String> domainIds) {
    try {
      return (List<T>) removeSensitiveData(getAdminService().getUsersOfDomains(domainIds));
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends User> List<T> getUsersOfDomainsFromNewestToOldest(List<String> domainIds) {
    try {
      return (List<T>) removeSensitiveData(
          getAdminService().getUsersOfDomainsFromNewestToOldest(domainIds));
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends User> SilverpeasList<T> searchUsers(final UserDetailsSearchCriteria criteria) {
    try {
      final SilverpeasList<UserDetail> userDetails = SilverpeasList.wrap(
          removeSensitiveData(getAdminService().searchUsers(criteria)));
      return (SilverpeasList<T>) userDetails;
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return new ListSlice<>(Collections.emptyList());
  }

  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getFilteredDirectUsers(String sGroupId, String sUserLastNameFilter) {
    try {
      return removeSensitiveData(
          getAdminService().getFilteredDirectUsers(sGroupId, sUserLastNameFilter));
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
    return new SilverpeasArrayList<>();
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

  @SuppressWarnings("unchecked")
  @Override
  public Group[] getAllSubGroups(String parentGroupId) {
    try {
      return getAdminService().getAllSubGroups(parentGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Group[] getRecursivelyAllSubgroups(final String parentGroupId) {
    try {
      return getAdminService().getRecursivelyAllSubGroups(parentGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getAllUsers() {
    try {
      return removeSensitiveData(getAdminService().getAllUsers().toArray(new UserDetail[0]));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new UserDetail[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getUsers(String componentId, String profile) {
    try {
      UserDetail[] aUserDetail = null;
      if (componentId != null) {
        aUserDetail = removeSensitiveData(getAdminService().getUsers(false, profile, componentId));
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
        SilverpeasRole.fromStrings(getUserProfiles(user.getId(), componentInstanceIdentifier));
    silverpeasRoles.remove(MANAGER);
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
  public Map<String, Set<String>> getUserProfilesByComponentId(final String userId,
      final Collection<String> componentIds) {
    try {
      return getAdminService().getUserProfilesByComponentId(userId, componentIds);
    } catch (Exception e) {
      if (componentIds.stream().anyMatch(i -> !isToolAvailable(i))) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
      return emptyMap();
    }
  }

  @Override
  public List<String> getSpaceUserProfilesBySpaceId(String userId, String spaceId) {
    try {
      return getAdminService().getSpaceUserProfilesBySpaceId(userId, spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return emptyList();
    }
  }

  @Override
  public Map<String, Set<String>> getSpaceUserProfilesBySpaceIds(final String userId,
      final Collection<String> spaceIds) {
    try {
      return getAdminService().getSpaceUserProfilesBySpaceIds(userId, spaceIds);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return emptyMap();
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
  public Map<Pair<String, String>, Set<String>> getUserProfilesByComponentIdAndObjectId(
      final String userId, final Collection<String> componentIds,
      final ProfiledObjectIds profiledObjectIds) {
    try {
      return getAdminService()
          .getUserProfilesByComponentIdAndObjectId(profiledObjectIds, componentIds, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return emptyMap();
    }
  }

  @Override
  public Map<String, List<String>> getUserObjectProfiles(final String userId,
      final String componentId, final ProfiledObjectType profiledObjectType) {
    try {
      return getAdminService().getProfilesByObjectTypeAndUserId(profiledObjectType, componentId,
          userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return Collections.emptyMap();
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
      return ArrayUtil.emptyStringArray();
    }
  }

  // -------------------------------------------------------------------
  // GROUPS QUERIES
  // -------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Group getGroup(String sGroupId) {
    try {
      return getAdminService().getGroup(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Group[] getGroups(String[] groupsId) {
    Group[] groups = null;
    try {
      groups = getAdminService().getGroups(groupsId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return groups;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Group[] getAllGroups() {
    try {
      List<GroupDetail> groups = getAdminService().getAllGroups();
      return groups.toArray(new Group[0]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Group[] getAllRootGroups() {
    try {
      List<GroupDetail> groups = getAdminService().getAllRootGroups();
      return groups.toArray(new Group[0]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) {

    try {
      return removeSensitiveData(getAdminService().getAllUsersOfGroup(groupId));
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
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getUserManageableSpaceIds(String sUserId) {
    try {
      return getAdminService().getUserManageableSpaceIds(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllRootSpaceIds() {
    try {
      return getAdminService().getAllRootSpaceIds();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllRootSpaceIds(String sUserId) {
    try {
      return getAdminService().getAllRootSpaceIds(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId) {
    try {
      return getAdminService().getAllSubSpaceIds(sSpaceId, sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) {
    try {
      return getAdminService().getAllComponentIds(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) {
    try {
      return getAdminService().getAllComponentIdsRecur(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId,
      String componentName) {
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

  @Override
  public boolean isToolAvailable(String toolId) {
    return ToolInstance.from(toolId).isPresent();
  }

  @Override
  public List<String> getAvailableComponentsByUser(final String userId) {
    try {
      return getAdminService().getAvailableComponentsByUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

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
  public UserSpaceAvailabilityChecker getUserSpaceAvailabilityChecker(final String userId) {
    try {
      return getAdminService().getUserSpaceAvailabilityChecker(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new UserSpaceAvailabilityChecker(userId);
    }
  }

  @Override
  public boolean isObjectAvailableToUser(ProfiledObjectId objectId, String componentId,
      String userId) {
    try {
      return getAdminService().isObjectAvailableToUser(componentId, objectId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isObjectAvailableToGroup(ProfiledObjectId objectId, String componentId,
      String groupId) {
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
      return ArrayUtil.emptyStringArray();
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
    return getUsersIdsByRoleNames(componentId, profileNames, false);
  }

  @Override
  public String[] getUsersIdsByRoleNames(final String componentId, final List<String> profileNames,
      final boolean includeRemovedUsersAndGroups) {
    try {
      List<String> userIds;
      ComponentInst componentInst = getComponentInst(componentId);

      List<ProfileInst> profiles = componentInst.getAllProfilesInst();
      List<String> profileIds = profiles.stream()
          .filter(p -> profileNames.contains(p.getName()))
          .map(ProfileInst::getId)
          .collect(Collectors.toList());

      if (profileIds.isEmpty()) {
        return ArrayUtil.emptyStringArray();
      }

      userIds = getAdminService().searchUserIdsByProfile(profileIds, includeRemovedUsersAndGroups);
      return userIds.toArray(new String[0]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new String[0];
    }
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId,
      List<String> profileNames) {
    return getUsersIdsByRoleNames(componentId, objectId, profileNames, false);
  }

  @Override
  public String[] getUsersIdsByRoleNames(final String componentId, final ProfiledObjectId objectId,
      final List<String> profileNames, final boolean includeRemovedUsersAndGroups) {
    try {
      List<ProfileInst> profiles = getAdminService().getProfilesByObject(objectId, componentId);
      List<String> profileIds = new ArrayList<>();
      for (ProfileInst profile : profiles) {
        if (profileNames.contains(profile.getName())) {
          profileIds.add(profile.getId());
        }
      }

      if (profileIds.isEmpty()) {
        return ArrayUtil.emptyStringArray();
      } // else return all users !!

      List<String> userIds = getAdminService().searchUserIdsByProfile(profileIds,
          includeRemovedUsersAndGroups);
      return userIds.toArray(new String[0]);
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
      return listRes.toArray(new String[0]);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getAllowedComponentIds(String userId) {
    try {
      return getAdminService().getAvailCompoIds(userId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public List<String> getSearchableComponentsByCriteria(ComponentSearchCriteria criteria) {
    final List<String> componentIds;
    final String userId = criteria.getUser().getId();
    if (criteria.hasCriterionOnWorkspace()) {
      if (criteria.hasCriterionOnComponentInstances()) {
        componentIds = new ArrayList<>(criteria.getComponentInstanceIds());
      } else {
        final String[] availableComponentIds = getAvailCompoIds(criteria.getWorkspaceId(), userId);
        componentIds = extractSearchableComponents(availableComponentIds, userId);
      }
    } else {
      final String[] availableComponentIds = getAvailCompoIds(userId);
      componentIds = extractSearchableComponents(availableComponentIds, userId);
    }
    return componentIds;
  }

  private List<String> extractSearchableComponents(String[] availableComponentIds, String userId) {
    final Set<String> excludedComponentIds = getComponentsExcludedFromGlobalSearch(userId);
    return Stream.of(availableComponentIds)
        .filter(c -> !excludedComponentIds.contains(c))
        .collect(Collectors.toList());
  }

  private Set<String> getComponentsExcludedFromGlobalSearch(String userId) {
    final Set<String> excluded = new HashSet<>();

    // exclude all components of all excluded spaces
    final List<String> spaces = getItemsExcludedFromGlobalSearch("SpacesExcludedFromGlobalSearch");
    for (final String space : spaces) {
      String[] availableComponentIds = getAvailCompoIds(space, userId);
      excluded.addAll(Arrays.asList(availableComponentIds));
    }

    // exclude components explicitly excluded
    final List<String> components =
        getItemsExcludedFromGlobalSearch("ComponentsExcludedFromGlobalSearch");
    excluded.addAll(components);

    // exclude components (from instance parameter 'privateSearch')
    getAdminService().getComponentsWithParameter("privateSearch", "yes")
        .forEach(c -> excluded.add(c.getId()));

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
  public SpaceWithSubSpacesAndComponents getFullTreeviewOnComponentName(final String userId,
      final String componentName) throws AdminException {
    return getAdminService().getAllowedFullTreeviewOnComponentName(userId, componentName);
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(String userId, String spaceId)
      throws AdminException {
    return getAdminService().getAllowedFullTreeview(userId, spaceId);
  }

  @Override
  @NonNull
  public List<SpaceInstLight> getPathToSpace(@NonNull String spaceId) {
    return getPathToSpace(new ArrayList<>(), spaceId);
  }

  @NonNull
  private List<SpaceInstLight> getPathToSpace(@NonNull List<SpaceInstLight> path,
      @NonNull String spaceId) {
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

  private UserDetail[] removeSensitiveData(final UserDetail[] userDetails) {
    List<UserDetail> results = removeSensitiveData(Arrays.asList(userDetails));
    return results.toArray(new UserDetail[0]);
  }

  private <T extends UserDetail> List<T> removeSensitiveData(final List<T> users) {
    User requester = User.getCurrentRequester();
    if (!users.isEmpty() && requester != null) {
      users.replaceAll(u -> removeSensitiveData(u, requester));
    }
    return users;
  }

  private <T extends UserDetail> T removeSensitiveData(T user, final User requester) {
    if (user != null && requester != null && !requester.getId().equals(user.getId()) &&
        hasNotDomainAdminRights(requester, user.getDomainId())) {
      return removeSensitiveDataForNonAdmin(user);
    }
    return user;
  }

  private <T extends UserDetail> T removeSensitiveDataForNonAdmin(T user) {
    if (user.hasSensitiveData()) {
      user.setEmailAddress("");
      if (user instanceof UserFull) {
        UserFull userFull = (UserFull) user;
        String[] properties = userFull.getPropertiesNames();
        Stream.of(properties)
            .map(userFull::getProperty)
            .filter(DomainProperty::isSensitive)
            .forEach(p -> userFull.setValue(p.getName(), null));
      }
    }
    return user;
  }

  private boolean hasNotDomainAdminRights(User user, String domainId) {
    return !((user.isAccessAdmin() ||
        (user.isAccessDomainManager() && user.getDomainId().equals(domainId))) &&
        (!user.isDomainRestricted() || !user.isDomainAdminRestricted()));
  }
}
