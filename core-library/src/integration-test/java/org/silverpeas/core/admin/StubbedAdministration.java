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
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.SilverpeasList;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class StubbedAdministration implements Administration {

  private StubbedAdministration() {
    // Hidden constructor
  }

  @Override
  public void reloadCache() {

  }

  @Override
  public void initSynchronization() {

  }

  @Override
  public void createSpaceIndex(final int spaceId) {

  }

  @Override
  public void createSpaceIndex(final SpaceInstLight spaceInst) {

  }

  @Override
  public void deleteSpaceIndex(final SpaceInst spaceInst) {

  }

  @Override
  public void deleteAllSpaceIndexes() {

  }

  @Override
  public String addSpaceInst(final String userId, final SpaceInst spaceInst) {
    return null;
  }

  @Override
  public String deleteSpaceInstById(final String userId, final String spaceId,
      final boolean definitive) {
    return null;
  }

  @Override
  public void restoreSpaceFromBasket(final String spaceId) {

  }

  @Override
  public SpaceInst getSpaceInstById(final String spaceId) {
    return null;
  }

  @Override
  public SpaceInst getPersonalSpace(final String userId) {
    return null;
  }

  @Override
  public String[] getAllSubSpaceIds(final String domainFatherId) {
    return new String[0];
  }

  @Override
  public String updateSpaceInst(final SpaceInst spaceInstNew) {
    return null;
  }

  @Override
  public void updateSpaceOrderNum(final String spaceId, final int orderNum) {

  }

  @Override
  public boolean isSpaceInstExist(final String spaceId) {
    return false;
  }

  @Override
  public String[] getAllRootSpaceIds() {
    return new String[0];
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(final String componentId) {
    return null;
  }

  @Override
  public List<SpaceInstLight> getPathToSpace(final String spaceId, final boolean includeTarget) {
    return null;
  }

  @Override
  public String[] getAllSpaceIds() {
    return new String[0];
  }

  @Override
  public List<SpaceInstLight> getRemovedSpaces() {
    return null;
  }

  @Override
  public List<ComponentInstLight> getRemovedComponents() {
    return null;
  }

  @Override
  public String[] getSpaceNames(final String[] asClientSpaceIds) {
    return new String[0];
  }

  @Override
  public Map<String, WAComponent> getAllWAComponents() {
    return null;
  }

  @Override
  public ComponentInst getComponentInst(final String sClientComponentId) {
    return null;
  }

  @Override
  public SilverpeasComponentInstance getComponentInstance(
      final String componentInstanceIdentifier) {
    return null;
  }

  @Override
  public ComponentInstLight getComponentInstLight(final String componentId) {
    return null;
  }

  @Override
  public List<Parameter> getComponentParameters(final String componentId) {
    return null;
  }

  @Override
  public String getComponentParameterValue(final String componentId, final String parameterName) {
    return null;
  }

  @Override
  public List<ComponentInstLight> getComponentsWithParameter(final String paramName,
      final String paramValue) {
    return null;
  }

  @Override
  public void restoreComponentFromBasket(final String componentId) {

  }

  @Override
  public void createComponentIndex(final String componentId) {

  }

  @Override
  public void createComponentIndex(final SilverpeasComponentInstance componentInst) {

  }

  @Override
  public void deleteAllComponentIndexes() {

  }

  @Override
  public String addComponentInst(final String sUserId, final ComponentInst componentInst)
      throws AdminException, QuotaException {
    return null;
  }

  @Override
  public String deleteComponentInst(final String userId, final String componentId,
      final boolean definitive) {
    return null;
  }

  @Override
  public String updateComponentInst(final ComponentInst component) {
    return null;
  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space) {

  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space,
      final boolean persist, final boolean startNewTransaction) {

  }

  @Override
  public void setSpaceProfilesToComponent(final ComponentInst component, final SpaceInst space) {

  }


  @Override
  public void moveSpace(final String spaceId, final String fatherId) {

  }

  @Override
  public void moveComponentInst(final String spaceId, final String componentId,
      final String idComponentBefore, final ComponentInst[] componentInsts) {

  }

  @Override
  public void setComponentPlace(final String componentId, final String idComponentBefore,
      final ComponentInst[] brothersComponents) {

  }

  @Override
  public String getRequestRouter(final String sComponentName) {
    return null;
  }

  @Override
  public String[] getAllProfilesNames(final String sComponentName) {
    return new String[0];
  }

  @Override
  public String getProfileLabelfromName(final String sComponentName, final String sProfileName,
      final String lang) {
    return null;
  }

  @Override
  public ProfileInst getProfileInst(final String sProfileId) {
    return null;
  }

  @Override
  public List<ProfileInst> getProfilesByObject(final ProfiledObjectId objectRef, final String componentId) {
    return null;
  }

  @Override
  public String[] getProfilesByObjectAndUserId(final ProfiledObjectId objectRef, final String componentId, final String userId) {
    return new String[0];
  }

  @Override
  public String[] getProfilesByObjectAndGroupId(final ProfiledObjectId objectRef, final String componentId, final String groupId) throws AdminException {
    return new String[0];
  }

  @Override
  public boolean isComponentAvailableToGroup(final String componentId, final String groupId)
      throws AdminException {
    return false;
  }

  @Override
  public Map<Integer, List<String>> getProfilesByObjectTypeAndUserId(final String objectType,
      final String componentId, final String userId) {
    return null;
  }

  @Override
  public boolean isObjectAvailableToUser(final String componentId, final ProfiledObjectId objectRef,
      final String userId) {
    return false;
  }

  @Override
  public boolean isObjectAvailableToGroup(final String componentId,
      final ProfiledObjectId objectRef, final String groupId) throws AdminException {
    return false;
  }

  @Override
  public String addProfileInst(final ProfileInst profileInst) {
    return null;
  }

  @Override
  public String addProfileInst(final ProfileInst profileInst, final String userId) {
    return null;
  }

  @Override
  public String deleteProfileInst(final String sProfileId, final String userId) {
    return null;
  }

  @Override
  public String updateProfileInst(final ProfileInst profileInstNew) {
    return null;
  }

  @Override
  public String updateProfileInst(final ProfileInst profileInstNew, final String userId) {
    return null;
  }

  @Override
  public SpaceProfileInst getSpaceProfileInst(final String spaceProfileId) {
    return null;
  }

  @Override
  public String addSpaceProfileInst(final SpaceProfileInst spaceProfile, final String userId) {
    return null;
  }

  @Override
  public String deleteSpaceProfileInst(final String sSpaceProfileId, final String userId) {
    return null;
  }

  @Override
  public String updateSpaceProfileInst(final SpaceProfileInst newSpaceProfile,
      final String userId) {
    return null;
  }

  @Override
  public String[] getGroupNames(final String[] groupIds) {
    return new String[0];
  }

  @Override
  public String getGroupName(final String sGroupId) {
    return null;
  }

  @Override
  public List<GroupDetail> getAllGroups() {
    return Collections.emptyList();
  }

  @Override
  public boolean isGroupExist(final String groupName) {
    return false;
  }

  @Override
  public GroupDetail getGroup(final String groupId) {
    return null;
  }

  @Override
  public List<String> getPathToGroup(final String groupId) {
    return null;
  }

  @Override
  public GroupDetail getGroupByNameInDomain(final String groupName, final String domainFatherId) {
    return null;
  }

  @Override
  public GroupDetail[] getGroups(final String[] asGroupId) {
    return new GroupDetail[0];
  }

  @Override
  public String addGroup(final GroupDetail group) {
    return null;
  }

  @Override
  public String addGroup(final GroupDetail group, final boolean onlyInSilverpeas) {
    return null;
  }

  @Override
  public String deleteGroupById(final String sGroupId) {
    return null;
  }

  @Override
  public String deleteGroupById(final String sGroupId, final boolean onlyInSilverpeas) {
    return null;
  }

  @Override
  public String updateGroup(final GroupDetail group) {
    return null;
  }

  @Override
  public String updateGroup(final GroupDetail group, final boolean onlyInSilverpeas) {
    return null;
  }

  @Override
  public void removeUserFromGroup(final String sUserId, final String sGroupId) {

  }

  @Override
  public void addUserInGroup(final String sUserId, final String sGroupId) {

  }

  @Override
  public List<GroupDetail> getAllRootGroups() {
    return Collections.emptyList();
  }

  @Override
  public GroupProfileInst getGroupProfileInst(final String groupId) {
    return null;
  }

  @Override
  public String addGroupProfileInst(final GroupProfileInst spaceProfileInst) {
    return null;
  }

  @Override
  public String deleteGroupProfileInst(final String groupId) {
    return null;
  }

  @Override
  public String updateGroupProfileInst(final GroupProfileInst groupProfileInstNew) {
    return null;
  }

  @Override
  public void indexAllGroups() {

  }

  @Override
  public void indexGroups(final String domainId) {

  }

  @Override
  public String[] getAllUsersIds() {
    return new String[0];
  }

  @Override
  public UserDetail getUserDetail(final String sUserId) {
    return null;
  }

  @Override
  public UserDetail[] getUserDetails(final String[] userIds) {
    return new UserDetail[0];
  }

  @Override
  public List<UserDetail> getAllUsers() {
    return null;
  }

  @Override
  public List<UserDetail> getAllUsersFromNewestToOldest() {
    return null;
  }

  @Override
  public boolean isEmailExisting(final String email) {
    return false;
  }

  @Override
  public String getUserIdByLoginAndDomain(final String sLogin, final String sDomainId) {
    return null;
  }

  @Override
  public String getUserIdByAuthenticationKey(final String authenticationKey) {
    return null;
  }

  @Override
  public UserFull getUserFull(final String sUserId) {
    return null;
  }

  @Override
  public UserFull getUserFull(final String domainId, final String specificId) {
    return null;
  }

  @Override
  public String addUser(final UserDetail userDetail) {
    return null;
  }

  @Override
  public String addUser(final UserDetail userDetail, final boolean addOnlyInSilverpeas) {
    return null;
  }

  @Override
  public void migrateUser(final UserDetail userDetail, final String targetDomainId) {

  }

  @Override
  public void blockUser(final String userId) {

  }

  @Override
  public void unblockUser(final String userId) {

  }

  @Override
  public void deactivateUser(final String userId) {

  }

  @Override
  public void activateUser(final String userId) {

  }

  @Override
  public void userAcceptsTermsOfService(final String userId) {

  }

  @Override
  public String restoreUser(final String sUserId) throws AdminException {
    return null;
  }

  @Override
  public String removeUser(final String sUserId) throws AdminException {
    return null;
  }

  @Override
  public String deleteUser(final String sUserId) {
    return null;
  }

  @Override
  public String deleteUser(final String sUserId, final boolean onlyInSilverpeas) {
    return null;
  }

  @Override
  public String updateUser(final UserDetail user) {
    return null;
  }

  @Override
  public String updateUserFull(final UserFull user) {
    return null;
  }

  @Override
  public String getClientSpaceId(final String sDriverSpaceId) {
    return null;
  }

  @Override
  public String[] getClientSpaceIds(final String[] asDriverSpaceIds) {
    return new String[0];
  }

  @Override
  public String getNextDomainId() {
    return null;
  }

  @Override
  public String addDomain(final Domain theDomain) {
    return null;
  }

  @Override
  public String updateDomain(final Domain domain) {
    return null;
  }

  @Override
  public String removeDomain(final String domainId) {
    return null;
  }

  @Override
  public Domain[] getAllDomains() {
    return new Domain[0];
  }

  @Override
  public List<String> getAllDomainIdsForLogin(final String login) {
    return null;
  }

  @Override
  public Domain getDomain(final String domainId) {
    return null;
  }

  @Override
  public long getDomainActions(final String domainId) {
    return 0;
  }

  @Override
  public GroupDetail[] getRootGroupsOfDomain(final String domainId) {
    return new GroupDetail[0];
  }

  @Override
  public List<GroupDetail> getSynchronizedGroups() {
    return Collections.emptyList();
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(final String groupId) {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsersOfDomain(final String domainId) {
    return new UserDetail[0];
  }

  @Override
  public List<UserDetail> getUsersOfDomains(final List<String> domainIds) {
    return null;
  }

  @Override
  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(final List<String> domainIds) {
    return null;
  }

  @Override
  public String[] getUserIdsOfDomain(final String domainId) {
    return new String[0];
  }

  @Override
  public String identify(final String sKey, final String sSessionId,
      final boolean isAppInMaintenance) {
    return null;
  }

  @Override
  public String identify(final String sKey, final String sSessionId,
      final boolean isAppInMaintenance, final boolean removeKey) {
    return null;
  }

  @Override
  public List<GroupDetail> getDirectGroupsOfUser(final String userId) {
    return Collections.emptyList();
  }

  @Override
  public String[] getUserSpaceIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getUserRootSpaceIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getUserSubSpaceIds(final String sUserId, final String spaceId) {
    return new String[0];
  }

  @Override
  public boolean isSpaceAvailable(final String userId, final String spaceId) {
    return false;
  }

  @Override
  public List<SpaceInstLight> getSubSpaces(final String spaceId) {
    return null;
  }

  @Override
  public List<ComponentInstLight> getAvailCompoInSpace(final String userId, final String spaceId) {
    return null;
  }

  @Override
  public List<SpaceInstLight> getUserSpaceTreeview(final String userId) {
    return null;
  }

  @Override
  public String[] getAllowedSubSpaceIds(final String userId, final String spaceFatherId) {
    return new String[0];
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(final String sClientSpaceId) {
    return null;
  }

  @Override
  public SpaceInstLight getRootSpace(final String spaceId) {
    return null;
  }

  @Override
  public String[] getGroupManageableSpaceIds(final String sGroupId) {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSpaceIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSpaceRootIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSubSpaceIds(final String sUserId, final String sParentSpaceId) {
    return new String[0];
  }

  @Override
  public SpaceProfile getSpaceProfile(final String spaceId, final SilverpeasRole role) {
    return null;
  }

  @Override
  public List<String> getUserManageableGroupIds(final String sUserId) {
    return null;
  }

  @Override
  public String[] getAvailCompoIds(final String sClientSpaceId, final String sUserId) {
    return new String[0];
  }

  @Override
  public boolean isAnAdminTool(final String toolId) {
    return false;
  }

  @Override
  public boolean isComponentAvailableToUser(final String componentId, final String userId) {
    return false;
  }

  @Override
  public boolean isComponentManageable(final String componentId, final String userId) {
    return false;
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(final String sClientSpaceId, final String sUserId) {
    return new String[0];
  }

  @Override
  public List<String> getAvailCompoIdsAtRoot(final String sClientSpaceId, final String sUserId,
      final String componentNameRoot) {
    return null;
  }

  @Override
  public String[] getAvailCompoIds(final String userId) {
    return new String[0];
  }

  @Override
  public String[] getComponentIdsByNameAndUserId(final String sUserId,
      final String sComponentName) {
    return new String[0];
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(final String userId,
      final String componentName) {
    return null;
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
  public CompoSpace[] getCompoForUser(final String sUserId, final String sComponentName) {
    return new CompoSpace[0];
  }

  @Override
  public String[] getCompoId(final String sComponentName) {
    return new String[0];
  }

  @Override
  public String[] getProfileIds(final String sUserId) {
    return new String[0];
  }

  @Override
  public String[] getProfileIdsOfGroup(final String sGroupId) {
    return new String[0];
  }

  @Override
  public String[] getCurrentProfiles(final String sUserId, final ComponentInst componentInst) {
    return new String[0];
  }

  @Override
  public String[] getCurrentProfiles(final String sUserId, final String componentId) {
    return new String[0];
  }

  @Override
  public UserDetail[] getUsers(final boolean bAllProfiles, final String sProfile,
      final String sClientSpaceId, final String sClientComponentId) {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail[] getAllSubGroups(final String parentGroupId) {
    return new GroupDetail[0];
  }

  @Override
  public GroupDetail[] getRecursivelyAllSubGroups(String parentGroupId) {
    return new GroupDetail[0];
  }

  @Override
  public UserDetail[] getFiltredDirectUsers(final String sGroupId,
      final String sUserLastNameFilter) {
    return new UserDetail[0];
  }

  @Override
  public int getAllSubUsersNumber(final String sGroupId) {
    return 0;
  }

  @Override
  public int getUsersNumberOfDomain(final String domainId) {
    return 0;
  }

  @Override
  public String[] getAdministratorUserIds(final String fromUserId) {
    return new String[0];
  }

  @Override
  public String getSilverpeasEmail() {
    return null;
  }

  @Override
  public String getSilverpeasName() {
    return null;
  }

  @Override
  public String getDAPIGeneralAdminId() {
    return null;
  }

  @Override
  public String[] getAllSpaceIds(final String sUserId) {
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
      final String componentNameRoot, final boolean inCurrentSpace, final boolean inAllSpaces) {
    return new String[0];
  }

  @Override
  public void synchronizeGroupByRule(final String groupId, final boolean scheduledMode) {

  }

  @Override
  public String synchronizeGroup(final String groupId, final boolean recurs) {
    return null;
  }

  @Override
  public String synchronizeImportGroup(final String domainId, final String groupKey,
      final String askedParentId, final boolean recurs, final boolean isIdKey) {
    return null;
  }

  @Override
  public String synchronizeRemoveGroup(final String groupId) {
    return null;
  }

  @Override
  public String synchronizeUser(final String userId, final boolean recurs) {
    return null;
  }

  @Override
  public String synchronizeImportUserByLogin(final String domainId, final String userLogin,
      final boolean recurs) {
    return null;
  }

  @Override
  public String synchronizeImportUser(final String domainId, final String specificId,
      final boolean recurs) {
    return null;
  }

  @Override
  public List<DomainProperty> getSpecificPropertiesToImportUsers(final String domainId,
      final String language) {
    return null;
  }

  @Override
  public UserDetail[] searchUsers(final String domainId, final Map<String, String> query) {
    return new UserDetail[0];
  }

  @Override
  public String synchronizeRemoveUser(final String userId) {
    return null;
  }

  @Override
  public String synchronizeSilverpeasWithDomain(final String sDomainId) {
    return null;
  }

  @Override
  public String synchronizeSilverpeasWithDomain(final String sDomainId, final boolean threaded) {
    return null;
  }

  @Override
  public List<String> searchUserIdsByProfile(final List<String> profileIds) {
    return Collections.emptyList();
  }

  @Override
  public ListSlice<UserDetail> searchUsers(final UserDetailsSearchCriteria searchCriteria) {
    return null;
  }

  @Override
  public SilverpeasList<GroupDetail> searchGroups(final GroupsSearchCriteria searchCriteria) {
    return null;
  }

  @Override
  public void indexAllUsers() {

  }

  @Override
  public void indexUsers(final String domainId) {

  }

  @Override
  public String copyAndPasteComponent(final PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    return null;
  }

  @Override
  public String copyAndPasteSpace(final PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    return null;
  }

  @Override
  public void assignRightsFromUserToUser(final RightAssignationContext.MODE operationMode,
      final String sourceUserId, final String targetUserId, final boolean nodeAssignRights,
      final String authorId) {

  }

  @Override
  public void assignRightsFromUserToGroup(final RightAssignationContext.MODE operationMode,
      final String sourceUserId, final String targetGroupId, final boolean nodeAssignRights,
      final String authorId) {

  }

  @Override
  public void assignRightsFromGroupToUser(final RightAssignationContext.MODE operationMode,
      final String sourceGroupId, final String targetUserId, final boolean nodeAssignRights,
      final String authorId) {

  }

  @Override
  public void assignRightsFromGroupToGroup(final RightAssignationContext.MODE operationMode,
      final String sourceGroupId, final String targetGroupId, final boolean nodeAssignRights,
      final String authorId) {

  }

  /**
   * Is the specified user a manager of the specified domain?
   * @param userId the user identifier.
   * @param domainId the domain identifier.
   * @return true if user identified by given userId is the manager of given domain identifier.
   */
  @Override
  public boolean isDomainManagerUser(final String userId, final String domainId) {
    return false;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getAllowedFullTreeview(final String userId) {
    return null;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getAllowedFullTreeview(final String userId,
      final String spaceId) {
    return null;
  }

  @Override
  public List<UserDetail> getRemovedUsers(final String... domainIds) throws AdminException {
    return null;
  }

  @Override
  public List<UserDetail> getNonBlankedDeletedUsers(final String... domainIds) {
    return null;
  }

  @Override
  public void blankDeletedUsers(final String targetDomainId, final List<String> userIds) {
  }
}