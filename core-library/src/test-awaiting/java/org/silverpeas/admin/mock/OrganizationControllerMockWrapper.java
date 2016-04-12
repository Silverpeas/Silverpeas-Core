/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.admin.mock;

import com.silverpeas.admin.components.WAComponent;
import com.stratelia.webactiv.beans.admin.*;
import org.silverpeas.util.ListSlice;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * A wrapper around an OrganizationController mock for testing purpose.
 * It is managed by the IoC container and it plays the role of an OrganizationController instance
 * for the business objects involved in a test. For doing, it delegates the invoked methods to
 * the wrapped mock.
 * You can get the wrapped mock for registering some behaviours an OrganizationController instance
 * should have in the tests.
 */
@Named("organizationController")
public class OrganizationControllerMockWrapper extends DefaultOrganizationController {

  private static final long serialVersionUID = 2449731617524868440L;
  private DefaultOrganizationController mock;

  public OrganizationControllerMockWrapper() {
    mock = mock(DefaultOrganizationController.class);
  }

  /**
   * Gets the mock of the OrganizationController class wrapped by this instance.
   * @return an OrganizationController mock.
   */
  public OrganizationController getMock() {
    return mock;
  }

  @Override
  public String[] searchUsersIds(String groupId, String componentId, String[] profileId,
          UserDetail filterUser) {
    return mock.searchUsersIds(groupId, componentId, profileId, filterUser);
  }

  @Override
  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd) {
    return mock.searchUsers(modelUser, isAnd);
  }

  @Override
  public  ListSlice<UserDetail> searchUsers(UserDetailsSearchCriteria criteria) {
    return mock.searchUsers(criteria);
  }

  @Override
  public  ListSlice<Group> searchGroups(GroupsSearchCriteria criteria) {
    return mock.searchGroups(criteria);
  }

  @Override
  public String[] searchGroupsIds(boolean isRootGroup, String componentId, String[] profileId,
          Group modelGroup) {
    return mock.searchGroupsIds(isRootGroup, componentId, profileId, modelGroup);
  }

  @Override
  public Group[] searchGroups(Group modelGroup, boolean isAnd) {
    return mock.searchGroups(modelGroup, isAnd);
  }

  @Override
  public void reloadAdminCache() {
    mock.reloadAdminCache();
  }

  @Override
  public boolean isSpaceAvailable(String spaceId, String userId) {
    return mock.isSpaceAvailable(spaceId, userId);
  }

  @Override
  public boolean isObjectAvailable(int objectId, ObjectType objectType, String componentId,
          String userId) {
    return mock.isObjectAvailable(objectId, objectType, componentId, userId);
  }

  @Override
  public boolean isComponentManageable(String componentId, String userId) {
    return mock.isComponentManageable(componentId, userId);
  }

  @Override
  public boolean isComponentExist(String componentId) {
    return mock.isComponentExist(componentId);
  }

  @Override
  public boolean isComponentAvailable(String componentId, String userId) {
    return mock.isComponentAvailable(componentId, userId);
  }

  @Override
  public boolean isAnonymousAccessActivated() {
    return mock.isAnonymousAccessActivated();
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId, String objectId, ObjectType objectType,
          List<String> profileNames) {
    return mock.getUsersIdsByRoleNames(componentId, objectId, objectType, profileNames);
  }

  @Override
  public String[] getUsersIdsByRoleNames(String componentId,
          List<String> profileNames) {
    return mock.getUsersIdsByRoleNames(componentId, profileNames);
  }

  @Override
  public UserDetail[] getUsers(String sPrefixTableName, String sComponentName, String sProfile) {
    return mock.getUsers(sPrefixTableName, sComponentName, sProfile);
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId, int objectId,
          ObjectType objectType) {
    return mock.getUserProfiles(userId, componentId, objectId, objectType);
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId) {
    return mock.getUserProfiles(userId, componentId);
  }

  @Override
  public ProfileInst getUserProfile(String profileId) {
    return mock.getUserProfile(profileId);
  }

  @Override
  public String[] getUserManageableSpaceIds(String sUserId) {
    return mock.getUserManageableSpaceIds(sUserId);
  }

  @Override
  public UserFull getUserFull(String sUserId) {
    return mock.getUserFull(sUserId);
  }

  @Override
  public UserDetail[] getUserDetails(String[] asUserIds) {
    return mock.getUserDetails(asUserIds);
  }

  @Override
  public String getUserDetailByDBId(int id) {
    return mock.getUserDetailByDBId(id);
  }

  @Override
  public UserDetail getUserDetail(String sUserId) {
    return mock.getUserDetail(sUserId);
  }

  @Override
  public int getUserDBId(String sUserId) {
    return mock.getUserDBId(sUserId);
  }

  @Override
  public List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
          String componentName) {
    return mock.getSubSpacesContainingComponent(spaceId, userId, componentName);
  }

  @Override
  public List<SpaceInstLight> getSpaceTreeview(String userId) {
    return mock.getSpaceTreeview(userId);
  }

  @Override
  public List<SpaceInst> getSpacePathToComponent(String componentId) {
    return mock.getSpacePathToComponent(componentId);
  }

  @Override
  public List<SpaceInst> getSpacePath(String spaceId) {
    return mock.getSpacePath(spaceId);
  }

  @Override
  public String[] getSpaceNames(String[] asSpaceIds) {
    return mock.getSpaceNames(asSpaceIds);
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(String spaceId) {
    return mock.getSpaceInstLightById(spaceId);
  }

  @Override
  public SpaceInst getSpaceInstById(String sSpaceId) {
    return mock.getSpaceInstById(sSpaceId);
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName) {
    return mock.getRootSpacesContainingComponent(userId, componentName);
  }

  @Override
  public SpaceInstLight getRootSpace(String spaceId) {
    return mock.getRootSpace(spaceId);
  }

  @Override
  public List<String> getPathToGroup(String groupId) {
    return mock.getPathToGroup(groupId);
  }

  @Override
  public Group[] getGroups(String[] groupsId) {
    return mock.getGroups(groupsId);
  }

  @Override
  public Group getGroup(String sGroupId) {
    return mock.getGroup(sGroupId);
  }

  @Override
  public String getGeneralSpaceId() {
    return mock.getGeneralSpaceId();
  }

  @Override
  public UserDetail[] getFiltredDirectUsers(String sGroupId, String sUserLastNameFilter) {
    return mock.getFiltredDirectUsers(sGroupId, sUserLastNameFilter);
  }

  @Override
  public Domain getDomain(String domainId) {
    return mock.getDomain(domainId);
  }

  @Override
  public String[] getDirectGroupIdsOfUser(String userId) {
    return mock.getDirectGroupIdsOfUser(userId);
  }

  @Override
  public String getComponentParameterValue(String sComponentId, String parameterName) {
    return mock.getComponentParameterValue(sComponentId, parameterName);
  }

  @Override
  public ComponentInstLight getComponentInstLight(String sComponentId) {
    return mock.getComponentInstLight(sComponentId);
  }

  @Override
  public ComponentInst getComponentInst(String sComponentId) {
    return mock.getComponentInst(sComponentId);
  }

  @Override
  public String[] getComponentIdsForUser(String sUserId, String sCompoName) {
    return mock.getComponentIdsForUser(sUserId, sCompoName);
  }

  @Override
  public String[] getCompoId(String sCompoName) {
    return mock.getCompoId(sCompoName);
  }

  @Override
  public CompoSpace[] getCompoForUser(String sUserId, String sCompoName) {
    return mock.getCompoForUser(sUserId, sCompoName);
  }

  @Override
  public String[] getAvailDriverCompoIds(String sClientSpaceId, String sUserId) {
    return mock.getAvailDriverCompoIds(sClientSpaceId, sUserId);
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName) {
    return mock.getAvailComponentInstLights(userId, componentName);
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) {
    return mock.getAvailCompoIdsAtRoot(sClientSpaceId, sUserId);
  }

  @Override
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {
    return mock.getAvailCompoIds(sClientSpaceId, sUserId);
  }

  @Override
  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) {
    return mock.getAllowedSubSpaceIds(userId, spaceFatherId);
  }

  @Override
  public String[] getAllowedComponentIds(String userId) {
    return mock.getAllowedComponentIds(userId);
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) {
    return mock.getAllUsersOfGroup(groupId);
  }

  @Override
  public UserDetail[] getAllUsersInDomain(String domainId) {
    return mock.getAllUsersInDomain(domainId);
  }

  @Override
  public String[] getAllUsersIds() {
    return mock.getAllUsersIds();
  }

  @Override
  public UserDetail[] getAllUsers() {
    return mock.getAllUsers();
  }

  @Override
  public UserDetail[] getAllUsers(String componentId) {
    return mock.getAllUsers(componentId);
  }

  @Override
  public int getAllSubUsersNumber(String sGroupId) {
    return mock.getAllSubUsersNumber(sGroupId);
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId) {
    return mock.getAllSubSpaceIds(sSpaceId, sUserId);
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId) {
    return mock.getAllSubSpaceIds(sSpaceId);
  }

  @Override
  public Group[] getAllSubGroups(String parentGroupId) {
    return mock.getAllSubGroups(parentGroupId);
  }

  @Override
  public String[] getAllSpaceIds(String sUserId) {
    return mock.getAllSpaceIds(sUserId);
  }

  @Override
  public String[] getAllSpaceIds() {
    return mock.getAllSpaceIds();
  }

  @Override
  public String[] getAllRootSpaceIds(String sUserId) {
    return mock.getAllRootSpaceIds(sUserId);
  }

  @Override
  public String[] getAllRootSpaceIds() {
    return mock.getAllRootSpaceIds();
  }

  @Override
  public Group[] getAllRootGroupsInDomain(String domainId) {
    return mock.getAllRootGroupsInDomain(domainId);
  }

  @Override
  public Group[] getAllRootGroups() {
    return mock.getAllRootGroups();
  }

  @Override
  public Group[] getAllGroups() {
    return mock.getAllGroups();
  }

  @Override
  public String[] getAllGroupIdsOfUser(String userId) {
    return mock.getAllGroupIdsOfUser(userId);
  }

  @Override
  public Domain[] getAllDomains() {
    return mock.getAllDomains();
  }

  @Override
  public Map<String, String> getAllComponentsNames() {
    return mock.getAllComponentsNames();
  }

  @Override
  public Map<String, WAComponent> getAllComponents() {
    return mock.getAllComponents();
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId, String sUserId, String sComponentRootName,
          boolean inCurrentSpace, boolean inAllSpaces) {
    return mock.getAllComponentIdsRecur(sSpaceId, sUserId, sComponentRootName, inCurrentSpace,
            inAllSpaces);
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) {
    return mock.getAllComponentIdsRecur(sSpaceId);
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) {
    return mock.getAllComponentIds(sSpaceId);
  }

  @Override
  public String[] getAdministratorUserIds(String fromUserId) {
    return mock.getAdministratorUserIds(fromUserId);
  }

  @Override
  public boolean isToolAvailable(String toolId) {
    return mock.isToolAvailable(toolId);
  }
}
