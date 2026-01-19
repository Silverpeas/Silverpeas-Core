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
package org.silverpeas.core.test.stub;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.*;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents;
import org.silverpeas.core.admin.service.UserSpaceAvailabilityChecker;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;

import java.util.*;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

/**
 * Stubbed {@link OrganizationController} implementation to be used in integration tests without
 * having to import all the dependencies of the default {@link OrganizationController} controller.
 *
 * @author Yohann Chastagnier
 */
@Service
@Alternative
@Priority(APPLICATION + 10)
public class StubbedOrganizationController implements OrganizationController {

  @SuppressWarnings("unchecked")
  @Override
  public User getUserDetail(final String sUserId) {
    if (StringUtil.isDefined(sUserId)) {
      return UserImpl.builder(sUserId)
          .build();
    }
    return null;
  }

  @Override
  public String[] getAllSpaceIds() {
    return new String[0];
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId) {
    return new String[0];
  }

  @Override
  public String[] getSpaceNames(String[] asSpaceIds) {
    return new String[0];
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(String spaceId) {
    return null;
  }

  @Override
  public SpaceInst getSpaceInstById(String sSpaceId) {
    return null;
  }

  @Override
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAvailCompoIds(String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) {
    return new String[0];
  }

  @Override
  public CompoSpace[] getCompoForUser(String sUserId, String sCompoName) {
    return new CompoSpace[0];
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName) {
    return List.of();
  }

  @Override
  public String[] getComponentIdsForUser(String sUserId, String sCompoName) {
    return new String[0];
  }

  @Override
  public String[] getCompoId(String sCompoName) {
    return new String[0];
  }

  @Override
  public String getComponentParameterValue(String sComponentId, String parameterName) {
    return "";
  }

  @Override
  public List<ComponentInstLight> getComponentsWithParameterValue(String param, String value) {
    return List.of();
  }

  @Override
  public Map<String, Map<String, String>> getParameterValuesByComponentIdThenByParamName(Collection<String> componentIds, Collection<String> paramNames) {
    return Map.of();
  }

  @Override
  public Optional<SilverpeasComponentInstance> getComponentInstance(String componentInstanceIdentifier) {
    var identity = SilverpeasComponentInstance.getIdentity(componentInstanceIdentifier);
    ComponentInst inst = new ComponentInst();
    inst.setLocalId(identity.getInstanceLocalId());
    inst.setName(identity.getComponentName());
    return Optional.of(inst);
  }

  @Override
  public ComponentInst getComponentInst(String sComponentId) {
    var identity = SilverpeasComponentInstance.getIdentity(sComponentId);
    ComponentInst inst = new ComponentInst();
    inst.setLocalId(identity.getInstanceLocalId());
    inst.setName(identity.getComponentName());
    return inst;
  }

  @Override
  public ComponentInstLight getComponentInstLight(String sComponentId) {
    var identity = SilverpeasComponentInstance.getIdentity(sComponentId);
    ComponentInstLight inst = new ComponentInstLight();
    inst.setLocalId(identity.getInstanceLocalId());
    inst.setName(identity.getComponentName());
    return inst;
  }

  @Override
  public UserFull getUserFull(String sUserId) {
    return null;
  }

  @Override
  public List<UserFull> getUserFulls(Collection<String> userIds) {
    return List.of();
  }

  @Override
  public <T extends User> T[] getUserDetails(String[] asUserIds) {
    return null;
  }

  @Override
  public <T extends User> T[] getAllUsers(String componentId) {
    return null;
  }

  @Override
  public <T extends User> T[] getAllUsersInDomain(String domainId) {
    return null;
  }

  @Override
  public <T extends User> SilverpeasList<T> searchUsers(UserDetailsSearchCriteria criteria) {
    return null;
  }

  @Override
  public <T extends Group> T[] getAllRootGroupsInDomain(String domainId) {
    return null;
  }

  @Override
  public <T extends User> T[] getFilteredDirectUsers(String sGroupId, String sUserLastNameFilter) {
    return null;
  }

  @Override
  public <T extends Group> SilverpeasList<T> searchGroups(GroupsSearchCriteria criteria,
      boolean orderedByType) {
    return null;
  }

  @Override
  public int getAllSubUsersNumber(String sGroupId) {
    return 0;
  }

  @Override
  public <T extends Group> T[] getAllSubGroups(String parentGroupId) {
    return null;
  }

  @Override
  public <T extends Group> T[] getRecursivelyAllSubgroups(String parentGroupId) {
    return null;
  }

  @Override
  public <T extends User> T[] getAllUsers() {
    return null;
  }

  @Override
  public <T extends User> T[] getUsers(String componentId, String profile) {
    return null;
  }

  @Override
  public Collection<SilverpeasRole> getUserSilverpeasRolesOn(User user,
      String componentInstanceIdentifier) {
    return List.of();
  }

  @Override
  public String[] getUserProfiles(final String userId, final String componentId) {
    return new String[0];
  }

  @Override
  public Map<String, Set<String>> getUserProfilesByComponentId(String userId,
      Collection<String> componentIds) {
    return Map.of();
  }

  @Override
  public List<String> getSpaceUserProfilesBySpaceId(String userId, String spaceId) {
    return List.of();
  }

  @Override
  public Map<String, Set<String>> getSpaceUserProfilesBySpaceIds(String userId,
      Collection<String> spaceIds) {
    return Map.of();
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId, ProfiledObjectId objectId) {
    return new String[0];
  }

  @Override
  public Map<Pair<String, String>, Set<String>> getUserProfilesByComponentIdAndObjectId(String userId, Collection<String> componentIds, ProfiledObjectIds profiledObjectIds) {
    return Map.of();
  }

  @Override
  public Map<String, List<String>> getUserObjectProfiles(String userId, String componentId,
      ProfiledObjectType profiledObjectType) {
    return Map.of();
  }

  @Override
  public List<ProfileInst> getUserProfiles(String componentId, ProfiledObjectId objectId) {
    return List.of();
  }

  @Override
  public ProfileInst getUserProfile(String profileId) {
    return null;
  }

  @Override
  public String[] getAdministratorUserIds(String fromUserId) {
    return new String[0];
  }

  @Override
  public <T extends Group> T getGroup(String sGroupId) {
    return null;
  }

  @Override
  public <T extends Group> T[] getGroups(String[] groupsId) {
    return null;
  }

  @Override
  public <T extends Group> T[] getAllGroups() {
    return null;
  }

  @Override
  public <T extends Group> T[] getAllRootGroups() {
    return null;
  }

  @Override
  public <T extends User> T[] getAllUsersOfGroup(String groupId) {
    return null;
  }

  @Override
  public List<String> getPathToGroup(String groupId) {
    return List.of();
  }

  @Override
  public String[] getAllSpaceIds(String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSpaceIds(String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAllRootSpaceIds() {
    return new String[0];
  }

  @Override
  public String[] getAllRootSpaceIds(String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) {
    return new String[0];
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId,
      String componentName) {
    return List.of();
  }

  @Override
  public List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName) {
    return List.of();
  }

  @Override
  public boolean isToolAvailable(String toolId) {
    return false;
  }

  @Override
  public List<String> getAvailableComponentsByUser(String userId) {
    return List.of();
  }

  @Override
  public boolean isComponentAvailableToUser(String componentId, String userId) {
    return false;
  }

  @Override
  public boolean isComponentAvailableToGroup(String componentId, String groupId) {
    return false;
  }

  @Override
  public boolean isComponentExist(String componentId) {
    return false;
  }

  @Override
  public boolean isComponentManageable(String componentId, String userId) {
    return false;
  }

  @Override
  public boolean isSpaceAvailable(String spaceId, String userId) {
    return false;
  }

  @Override
  public UserSpaceAvailabilityChecker getUserSpaceAvailabilityChecker(String userId) {
    return null;
  }

  @Override
  public boolean isObjectAvailableToUser(ProfiledObjectId objectId, String componentId,
      String userId) {
    return false;
  }

  @Override
  public boolean isObjectAvailableToGroup(ProfiledObjectId objectId, String componentId,
      String groupId) {
    return false;
  }

  @Override
  public List<SpaceInstLight> getSpaceTreeview(String userId) {
    return List.of();
  }

  @Override
  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) {
    return new String[0];
  }

  @Override
  public SpaceInstLight getRootSpace(String spaceId) {
    return null;
  }

  @Override
  public String[] getAllUsersIds() {
    return new String[0];
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames) {
    return new String[0];
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames,
      boolean includeRemovedUsersAndGroups) {
    return new String[0];
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId,
      List<String> profileNames) {
    return new String[0];
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId,
      List<String> profileNames, boolean includeRemovedUsersAndGroups) {
    return new String[0];
  }

  @Override
  public Domain getDomain(String domainId) {
    return null;
  }

  @Override
  public Domain[] getAllDomains() {
    return new Domain[0];
  }

  @Override
  public List<GroupDetail> getDirectGroupsOfUser(String userId) {
    return List.of();
  }

  @Override
  public String[] getAllGroupIdsOfUser(String userId) {
    return new String[0];
  }

  @Override
  public String[] getAllowedComponentIds(String userId) {
    return new String[0];
  }

  @Override
  public <T extends User> List<T> getUsersOfDomainsFromNewestToOldest(List<String> domainIds) {
    return List.of();
  }

  @Override
  public <T extends User> List<T> getUsersOfDomains(List<String> domainIds) {
    return List.of();
  }

  @Override
  public boolean isAdminTool(String toolId) {
    return false;
  }

  @Override
  public List<String> getSearchableComponentsByCriteria(ComponentSearchCriteria criteria) {
    return List.of();
  }

  @Override
  public SpaceProfile getSpaceProfile(String spaceId, SilverpeasRole role) {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview() {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(String userId) {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeviewOnComponentName(String userId,
      String componentName) {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(String userId, String spaceId) {
    return null;
  }

  @Override
  @NonNull
  public List<SpaceInstLight> getPathToSpace(@NonNull String spaceId) {
    return List.of();
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(String componentId) {
    return List.of();
  }
}
