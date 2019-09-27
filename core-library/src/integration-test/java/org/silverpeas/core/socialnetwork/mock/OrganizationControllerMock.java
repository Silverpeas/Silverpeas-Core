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
package org.silverpeas.core.socialnetwork.mock;


import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.SilverpeasList;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.mockito.Mockito.mock;

/**
 * @author Yohann Chastagnier
 */
@Alternative
@Priority(APPLICATION + 10)
@Named("organizationController")
public class OrganizationControllerMock implements OrganizationController {
  private static final long serialVersionUID = 1L;

  private final OrganizationController mock = mock(OrganizationController.class);

  @Override
  public String[] getAllSpaceIds() {
    return new String[0];
  }

  @Override
  public String[] getAllSubSpaceIds(final String sSpaceId) {
    return new String[0];
  }

  @Override
  public String[] getSpaceNames(final String[] asSpaceIds) {
    return new String[0];
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(final String spaceId) {
    return null;
  }

  @Override
  public SpaceInst getSpaceInstById(final String sSpaceId) {
    return null;
  }

  @Override
  public String[] getAvailCompoIds(final String sClientSpaceId, final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAvailCompoIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(final String sClientSpaceId, final String sUserId) {
    return new String[0];
  }

  @Override
  public CompoSpace[] getCompoForUser(final String sUserId, final String sCompoName) {
    return new CompoSpace[0];
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(final String userId,
      final String componentName) {
    return null;
  }

  @Override
  public String[] getComponentIdsForUser(final String sUserId, final String sCompoName) {
    return new String[0];
  }

  @Override
  public String[] getCompoId(final String sCompoName) {
    return new String[0];
  }

  @Override
  public String getComponentParameterValue(final String sComponentId, final String parameterName) {
    return null;
  }

  @Override
  public List<ComponentInstLight> getComponentsWithParameterValue(String paramName,
      String paramValue) {
    return null;
  }

  @Override
  public Optional<SilverpeasComponentInstance> getComponentInstance(
      final String componentInstanceIdentifier) {
    return null;
  }

  @Override
  public ComponentInst getComponentInst(final String sComponentId) {
    return null;
  }

  @Override
  public ComponentInstLight getComponentInstLight(final String sComponentId) {
    return null;
  }

  @Override
  public UserFull getUserFull(final String sUserId) {
    return null;
  }

  @Override
  public UserDetail getUserDetail(final String sUserId) {
    UserDetail user = new UserDetail();
    String userId = sUserId;
    if ("11".equals(userId) || "13".equals(userId)) {
      user.setState(UserState.VALID);
    } else if ("12".equals(userId)) {
      user.setState(UserState.DELETED);
    } else {
      user = null;
    }
    return user;
  }

  @Override
  public UserDetail[] getUserDetails(final String[] asUserIds) {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getAllUsers(final String sPrefixTableName, final String sComponentName) {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getAllUsers(final String componentId) {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getAllUsersInDomain(final String domainId) {
    return new UserDetail[0];
  }

  @Override
  public ListSlice<UserDetail> searchUsers(final UserDetailsSearchCriteria criteria) {
    return null;
  }

  @Override
  public Group[] getAllRootGroupsInDomain(final String domainId) {
    return new Group[0];
  }

  @Override
  public UserDetail[] getFilteredDirectUsers(final String sGroupId,
      final String sUserLastNameFilter) {
    return new UserDetail[0];
  }

  @Override
  public SilverpeasList<Group> searchGroups(final GroupsSearchCriteria criteria) {
    return null;
  }

  @Override
  public int getAllSubUsersNumber(final String sGroupId) {
    return 0;
  }

  @Override
  public Group[] getAllSubGroups(final String parentGroupId) {
    return new Group[0];
  }

  @Override
  public Group[] getRecursivelyAllSubgroups(final String parentGroupId) {
    return new Group[0];
  }

  @Override
  public UserDetail[] getAllUsers() {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsers(final String sPrefixTableName, final String sComponentName,
      final String sProfile) {
    return new UserDetail[0];
  }

  @Override
  public Collection<SilverpeasRole> getUserSilverpeasRolesOn(final User user,
      final String componentInstanceIdentifier) {
    return Collections.emptyList();
  }

  @Override
  public String[] getUserProfiles(final String userId, final String componentId) {
    return new String[0];
  }

  @Override
  public String[] getUserProfiles(final String userId, final String componentId,
      final ProfiledObjectId objectId) {
    return new String[0];
  }

  @Override
  public Map<Integer, List<String>> getUserObjectProfiles(final String userId,
      final String componentId, final ProfiledObjectType objectType) {
    return null;
  }

  @Override
  public List<ProfileInst> getUserProfiles(final String componentId,
      final ProfiledObjectId objectId) {
    return null;
  }

  @Override
  public ProfileInst getUserProfile(final String profileId) {
    return null;
  }

  @Override
  public String[] getAdministratorUserIds(final String fromUserId) {
    return new String[0];
  }

  @Override
  public Group getGroup(final String sGroupId) {
    return null;
  }

  @Override
  public Group[] getGroups(final String[] groupsId) {
    return new Group[0];
  }

  @Override
  public Group[] getAllGroups() {
    return new Group[0];
  }

  @Override
  public Group[] getAllRootGroups() {
    return new Group[0];
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(final String groupId) {
    return new UserDetail[0];
  }

  @Override
  public List<String> getPathToGroup(final String groupId) {
    return null;
  }

  @Override
  public String[] getAllSpaceIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSpaceIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAllRootSpaceIds() {
    return new String[0];
  }

  @Override
  public String[] getAllRootSpaceIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAllSubSpaceIds(final String sSpaceId, final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIds(final String sSpaceId) {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIdsRecur(final String sSpaceId) {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIdsRecur(final String sSpaceId, final String sUserId,
      final String sComponentRootName, final boolean inCurrentSpace, final boolean inAllSpaces) {
    return new String[0];
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(final String userId,
      final String componentName) {
    return null;
  }

  @Override
  public List<SpaceInstLight> getSubSpacesContainingComponent(final String spaceId,
      final String userId, final String componentName) {
    return null;
  }

  @Override
  public boolean isToolAvailable(final String toolId) {
    return false;
  }

  @Override
  public boolean isComponentAvailable(final String componentId, final String userId) {
    return false;
  }

  @Override
  public boolean isComponentExist(final String componentId) {
    return false;
  }

  @Override
  public boolean isComponentManageable(final String componentId, final String userId) {
    return false;
  }

  @Override
  public boolean isSpaceAvailable(final String spaceId, final String userId) {
    return false;
  }

  @Override
  public boolean isObjectAvailable(final ProfiledObjectId objectId, final String componentId, final String userId) {
    return false;
  }

  @Override
  public List<SpaceInstLight> getSpaceTreeview(final String userId) {
    return null;
  }

  @Override
  public String[] getAllowedSubSpaceIds(final String userId, final String spaceFatherId) {
    return new String[0];
  }

  @Override
  public SpaceInstLight getRootSpace(final String spaceId) {
    return null;
  }

  @Override
  public String[] getAllUsersIds() {
    return new String[0];
  }

  @Override
  public String[] getUsersIdsByRoleNames(final String componentId,
      final List<String> profileNames) {
    return new String[0];
  }

  @Override
  public String[] getUsersIdsByRoleNames(final String componentId, final ProfiledObjectId objectId,
      final List<String> profileNames) {
    return new String[0];
  }

  @Override
  public Domain getDomain(final String domainId) {
    return null;
  }

  @Override
  public Domain[] getAllDomains() {
    return new Domain[0];
  }

  @Override
  public List<GroupDetail> getDirectGroupsOfUser(final String userId) {
    return Collections.emptyList();
  }

  @Override
  public String[] getAllGroupIdsOfUser(final String userId) {
    return new String[0];
  }

  @Override
  public boolean isAnonymousAccessActivated() {
    return false;
  }

  @Override
  public String[] getAllowedComponentIds(final String userId) {
    return new String[0];
  }

  @Override
  public List<UserDetail> getAllUsersFromNewestToOldest() {
    return null;
  }

  @Override
  public <T extends User> List<T> getUsersOfDomainsFromNewestToOldest(final List<String> domainIds) {
    return null;
  }

  @Override
  public <T extends User> List<T> getUsersOfDomains(final List<String> domainIds) {
    return null;
  }

  @Override
  public boolean isAdminTool(final String toolId) {
    return false;
  }

  @Override
  public List<String> getSearchableComponentsByCriteria(final ComponentSearchCriteria criteria) {
    return null;
  }

  @Override
  public SpaceProfile getSpaceProfile(final String spaceId, final SilverpeasRole role) {
    return null;
  }

  public OrganizationController getMock() {
    return mock;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview(final String userId, final String spaceId)
      throws AdminException {
    return null;
  }

  @Override
  public List<SpaceInstLight> getPathToSpace(final String spaceId) {
    return null;
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(final String componentId) {
    return null;
  }
}