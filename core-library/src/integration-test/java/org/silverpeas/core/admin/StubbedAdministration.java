/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.admin;

import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.space.SpaceAndChildren;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.space.model.SpaceTemplate;
import org.silverpeas.core.admin.user.model.AdminGroupInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.util.ListSlice;

import javax.inject.Singleton;
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
  public void startServer() {

  }

  @Override
  public String getGeneralSpaceId() {
    return null;
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
  public String addSpaceInst(final String userId, final SpaceInst spaceInst) throws AdminException {
    return null;
  }

  @Override
  public String deleteSpaceInstById(final String userId, final String spaceId,
      final boolean definitive) throws AdminException {
    return null;
  }

  @Override
  public String deleteSpaceInstById(final String userId, final String spaceId,
      final boolean startNewTransaction, final boolean definitive) throws AdminException {
    return null;
  }

  @Override
  public void restoreSpaceFromBasket(final String spaceId) throws AdminException {

  }

  @Override
  public SpaceInst getSpaceInstById(final String spaceId) throws AdminException {
    return null;
  }

  @Override
  public SpaceInst getPersonalSpace(final String userId) throws AdminException {
    return null;
  }

  @Override
  public String[] getAllSubSpaceIds(final String domainFatherId) throws AdminException {
    return new String[0];
  }

  @Override
  public String updateSpaceInst(final SpaceInst spaceInstNew) throws AdminException {
    return null;
  }

  @Override
  public void updateSpaceOrderNum(final String spaceId, final int orderNum) throws AdminException {

  }

  @Override
  public boolean isSpaceInstExist(final String spaceId) throws AdminException {
    return false;
  }

  @Override
  public String[] getAllRootSpaceIds() throws AdminException {
    return new String[0];
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(final String componentId) throws AdminException {
    return null;
  }

  @Override
  public List<SpaceInstLight> getPathToSpace(final String spaceId, final boolean includeTarget)
      throws AdminException {
    return null;
  }

  @Override
  public String[] getAllSpaceIds() throws AdminException {
    return new String[0];
  }

  @Override
  public List<SpaceInstLight> getRemovedSpaces() throws AdminException {
    return null;
  }

  @Override
  public List<ComponentInstLight> getRemovedComponents() throws AdminException {
    return null;
  }

  @Override
  public String[] getSpaceNames(final String[] asClientSpaceIds) throws AdminException {
    return new String[0];
  }

  @Override
  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return null;
  }

  @Override
  public SpaceInst getSpaceInstFromTemplate(final String templateName) {
    return null;
  }

  @Override
  public Map<String, WAComponent> getAllComponents() {
    return null;
  }

  @Override
  public ComponentInst getComponentInst(final String sClientComponentId) throws AdminException {
    return null;
  }

  @Override
  public ComponentInstLight getComponentInstLight(final String componentId) throws AdminException {
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
  public void restoreComponentFromBasket(final String componentId) throws AdminException {

  }

  @Override
  public void createComponentIndex(final String componentId) {

  }

  @Override
  public void createComponentIndex(final ComponentInstLight componentInst) {

  }

  @Override
  public String addComponentInst(final String sUserId, final ComponentInst componentInst)
      throws AdminException, QuotaException {
    return null;
  }

  @Override
  public String addComponentInst(final String userId, final ComponentInst componentInst,
      final boolean startNewTransaction) throws AdminException, QuotaException {
    return null;
  }

  @Override
  public String deleteComponentInst(final String userId, final String componentId,
      final boolean definitive) throws AdminException {
    return null;
  }

  @Override
  public void updateComponentOrderNum(final String componentId, final int orderNum)
      throws AdminException {

  }

  @Override
  public String updateComponentInst(final ComponentInst component) throws AdminException {
    return null;
  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space)
      throws AdminException {

  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space,
      final boolean persist, final boolean startNewTransaction) throws AdminException {

  }

  @Override
  public void setSpaceProfilesToComponent(final ComponentInst component, final SpaceInst space)
      throws AdminException {

  }

  @Override
  public void setSpaceProfilesToComponent(final ComponentInst component, final SpaceInst space,
      final boolean startNewTransaction) throws AdminException {

  }

  @Override
  public void moveSpace(final String spaceId, final String fatherId) throws AdminException {

  }

  @Override
  public void moveComponentInst(final String spaceId, final String componentId,
      final String idComponentBefore, final ComponentInst[] componentInsts) throws AdminException {

  }

  @Override
  public void setComponentPlace(final String componentId, final String idComponentBefore,
      final ComponentInst[] m_BrothersComponents) throws AdminException {

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
  public ProfileInst getProfileInst(final String sProfileId) throws AdminException {
    return null;
  }

  @Override
  public List<ProfileInst> getProfilesByObject(final String objectId, final String objectType,
      final String componentId) throws AdminException {
    return null;
  }

  @Override
  public String[] getProfilesByObjectAndUserId(final int objectId, final String objectType,
      final String componentId, final String userId) throws AdminException {
    return new String[0];
  }

  @Override
  public boolean isObjectAvailable(final String componentId, final int objectId,
      final String objectType, final String userId) throws AdminException {
    return false;
  }

  @Override
  public String addProfileInst(final ProfileInst profileInst) throws AdminException {
    return null;
  }

  @Override
  public String addProfileInst(final ProfileInst profileInst, final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public String deleteProfileInst(final String sProfileId, final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public String updateProfileInst(final ProfileInst profileInstNew) throws AdminException {
    return null;
  }

  @Override
  public String updateProfileInst(final ProfileInst profileInstNew, final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public SpaceProfileInst getSpaceProfileInst(final String spaceProfileId) throws AdminException {
    return null;
  }

  @Override
  public String addSpaceProfileInst(final SpaceProfileInst spaceProfile, final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public String deleteSpaceProfileInst(final String sSpaceProfileId, final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public String updateSpaceProfileInst(final SpaceProfileInst newSpaceProfile, final String userId)
      throws AdminException {
    return null;
  }

  @Override
  public String updateSpaceProfileInst(final SpaceProfileInst newSpaceProfile, final String userId,
      final boolean startNewTransaction) throws AdminException {
    return null;
  }

  @Override
  public String[] getGroupNames(final String[] groupIds) throws AdminException {
    return new String[0];
  }

  @Override
  public String getGroupName(final String sGroupId) throws AdminException {
    return null;
  }

  @Override
  public String[] getAllGroupIds() throws AdminException {
    return new String[0];
  }

  @Override
  public boolean isGroupExist(final String groupName) throws AdminException {
    return false;
  }

  @Override
  public Group getGroup(final String groupId) throws AdminException {
    return null;
  }

  @Override
  public List<String> getPathToGroup(final String groupId) throws AdminException {
    return null;
  }

  @Override
  public Group getGroupByNameInDomain(final String groupName, final String domainFatherId)
      throws AdminException {
    return null;
  }

  @Override
  public Group[] getGroups(final String[] asGroupId) throws AdminException {
    return new Group[0];
  }

  @Override
  public String addGroup(final Group group) throws AdminException {
    return null;
  }

  @Override
  public String addGroup(final Group group, final boolean onlyInSilverpeas) throws AdminException {
    return null;
  }

  @Override
  public String deleteGroupById(final String sGroupId) throws AdminException {
    return null;
  }

  @Override
  public String deleteGroupById(final String sGroupId, final boolean onlyInSilverpeas)
      throws AdminException {
    return null;
  }

  @Override
  public String updateGroup(final Group group) throws AdminException {
    return null;
  }

  @Override
  public String updateGroup(final Group group, final boolean onlyInSilverpeas)
      throws AdminException {
    return null;
  }

  @Override
  public void removeUserFromGroup(final String sUserId, final String sGroupId)
      throws AdminException {

  }

  @Override
  public void addUserInGroup(final String sUserId, final String sGroupId) throws AdminException {

  }

  @Override
  public AdminGroupInst[] getAdminOrganization() throws AdminException {
    return new AdminGroupInst[0];
  }

  @Override
  public String[] getAllSubGroupIds(final String groupId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getAllSubGroupIdsRecursively(final String groupId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getAllRootGroupIds() throws AdminException {
    return new String[0];
  }

  @Override
  public Group[] getAllRootGroups() throws AdminException {
    return new Group[0];
  }

  @Override
  public GroupProfileInst getGroupProfileInst(final String groupId) throws AdminException {
    return null;
  }

  @Override
  public String addGroupProfileInst(final GroupProfileInst spaceProfileInst) throws AdminException {
    return null;
  }

  @Override
  public String addGroupProfileInst(final GroupProfileInst groupProfileInst,
      final boolean startNewTransaction) throws AdminException {
    return null;
  }

  @Override
  public String deleteGroupProfileInst(final String groupId) throws AdminException {
    return null;
  }

  @Override
  public String deleteGroupProfileInst(final String groupId, final boolean startNewTransaction)
      throws AdminException {
    return null;
  }

  @Override
  public String updateGroupProfileInst(final GroupProfileInst groupProfileInstNew)
      throws AdminException {
    return null;
  }

  @Override
  public void indexAllGroups() throws AdminException {

  }

  @Override
  public void indexGroups(final String domainId) throws AdminException {

  }

  @Override
  public String[] getAllUsersIds() throws AdminException {
    return new String[0];
  }

  @Override
  public UserDetail getUserDetail(final String sUserId) throws AdminException {
    return null;
  }

  @Override
  public UserDetail[] getUserDetails(final String[] userIds) {
    return new UserDetail[0];
  }

  @Override
  public List<UserDetail> getAllUsers() throws AdminException {
    return null;
  }

  @Override
  public List<UserDetail> getAllUsersFromNewestToOldest() throws AdminException {
    return null;
  }

  @Override
  public boolean isEmailExisting(final String email) throws AdminException {
    return false;
  }

  @Override
  public String getUserIdByLoginAndDomain(final String sLogin, final String sDomainId)
      throws AdminException {
    return null;
  }

  @Override
  public String getUserIdByAuthenticationKey(final String authenticationKey) throws Exception {
    return null;
  }

  @Override
  public UserFull getUserFull(final String sUserId) throws AdminException {
    return null;
  }

  @Override
  public UserFull getUserFull(final String domainId, final String specificId) throws Exception {
    return null;
  }

  @Override
  public String addUser(final UserDetail userDetail) throws AdminException {
    return null;
  }

  @Override
  public String addUser(final UserDetail userDetail, final boolean addOnlyInSilverpeas)
      throws AdminException {
    return null;
  }

  @Override
  public void migrateUser(final UserDetail userDetail, final String targetDomainId)
      throws AdminException {

  }

  @Override
  public void blockUser(final String userId) throws AdminException {

  }

  @Override
  public void unblockUser(final String userId) throws AdminException {

  }

  @Override
  public void deactivateUser(final String userId) throws AdminException {

  }

  @Override
  public void activateUser(final String userId) throws AdminException {

  }

  @Override
  public void userAcceptsTermsOfService(final String userId) throws AdminException {

  }

  @Override
  public String deleteUser(final String sUserId) throws AdminException {
    return null;
  }

  @Override
  public String deleteUser(final String sUserId, final boolean onlyInSilverpeas)
      throws AdminException {
    return null;
  }

  @Override
  public String updateUser(final UserDetail user) throws AdminException {
    return null;
  }

  @Override
  public String updateUserFull(final UserFull user) throws AdminException {
    return null;
  }

  @Override
  public String getClientSpaceId(final String sDriverSpaceId) {
    return null;
  }

  @Override
  public String[] getClientSpaceIds(final String[] asDriverSpaceIds) throws Exception {
    return new String[0];
  }

  @Override
  public String getNextDomainId() throws AdminException {
    return null;
  }

  @Override
  public String addDomain(final Domain theDomain) throws AdminException {
    return null;
  }

  @Override
  public String updateDomain(final Domain domain) throws AdminException {
    return null;
  }

  @Override
  public String removeDomain(final String domainId) throws AdminException {
    return null;
  }

  @Override
  public Domain[] getAllDomains() throws AdminException {
    return new Domain[0];
  }

  @Override
  public List<String> getAllDomainIdsForLogin(final String login) throws AdminException {
    return null;
  }

  @Override
  public Domain getDomain(final String domainId) throws AdminException {
    return null;
  }

  @Override
  public long getDomainActions(final String domainId) throws AdminException {
    return 0;
  }

  @Override
  public Group[] getRootGroupsOfDomain(final String domainId) throws AdminException {
    return new Group[0];
  }

  @Override
  public Group[] getSynchronizedGroups() throws AdminException {
    return new Group[0];
  }

  @Override
  public String[] getRootGroupIdsOfDomain(final String domainId) throws AdminException {
    return new String[0];
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(final String groupId) throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsersOfDomain(final String domainId) throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public List<UserDetail> getUsersOfDomains(final List<String> domainIds) throws AdminException {
    return null;
  }

  @Override
  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(final List<String> domainIds)
      throws AdminException {
    return null;
  }

  @Override
  public String[] getUserIdsOfDomain(final String domainId) throws AdminException {
    return new String[0];
  }

  @Override
  public String identify(final String sKey, final String sSessionId,
      final boolean isAppInMaintenance) throws AdminException {
    return null;
  }

  @Override
  public String identify(final String sKey, final String sSessionId,
      final boolean isAppInMaintenance, final boolean removeKey) throws AdminException {
    return null;
  }

  @Override
  public String[] getDirectGroupsIdsOfUser(final String userId) throws AdminException {
    return new String[0];
  }

  @Override
  public UserDetail[] searchUsers(final UserDetail modelUser, final boolean isAnd)
      throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public Group[] searchGroups(final Group modelGroup, final boolean isAnd) throws AdminException {
    return new Group[0];
  }

  @Override
  public String[] getUserSpaceIds(final String sUserId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getUserRootSpaceIds(final String sUserId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getUserSubSpaceIds(final String sUserId, final String spaceId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public boolean isSpaceAvailable(final String userId, final String spaceId) throws AdminException {
    return false;
  }

  @Override
  public List<SpaceInstLight> getSubSpacesOfUser(final String userId, final String spaceId)
      throws AdminException {
    return null;
  }

  @Override
  public List<SpaceInstLight> getSubSpaces(final String spaceId) throws AdminException {
    return null;
  }

  @Override
  public List<ComponentInstLight> getAvailCompoInSpace(final String userId, final String spaceId)
      throws AdminException {
    return null;
  }

  @Override
  public Map<String, SpaceAndChildren> getTreeView(final String userId, final String spaceId)
      throws AdminException {
    return null;
  }

  @Override
  public List<SpaceInstLight> getUserSpaceTreeview(final String userId) throws Exception {
    return null;
  }

  @Override
  public String[] getAllowedSubSpaceIds(final String userId, final String spaceFatherId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(final String sClientSpaceId) throws AdminException {
    return null;
  }

  @Override
  public SpaceInstLight getRootSpace(final String spaceId) throws AdminException {
    return null;
  }

  @Override
  public String[] getGroupManageableSpaceIds(final String sGroupId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSpaceIds(final String sUserId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSpaceRootIds(final String sUserId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getUserManageableSubSpaceIds(final String sUserId, final String sParentSpaceId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public List<String> getUserManageableGroupIds(final String sUserId) throws AdminException {
    return null;
  }

  @Override
  public String[] getAvailCompoIds(final String sClientSpaceId, final String sUserId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public boolean isAnAdminTool(final String toolId) {
    return false;
  }

  @Override
  public boolean isComponentAvailable(final String componentId, final String userId)
      throws AdminException {
    return false;
  }

  @Override
  public boolean isComponentManageable(final String componentId, final String userId)
      throws AdminException {
    return false;
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(final String sClientSpaceId, final String sUserId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public List<String> getAvailCompoIdsAtRoot(final String sClientSpaceId, final String sUserId,
      final String componentNameRoot) throws AdminException {
    return null;
  }

  @Override
  public String[] getAvailCompoIds(final String userId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getAvailDriverCompoIds(final String sClientSpaceId, final String sUserId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getComponentIdsByNameAndUserId(final String sUserId, final String sComponentName)
      throws AdminException {
    return new String[0];
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(final String userId,
      final String componentName) throws AdminException {
    return null;
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(final String userId,
      final String componentName) throws AdminException {
    return null;
  }

  @Override
  public List<SpaceInstLight> getSubSpacesContainingComponent(final String spaceId,
      final String userId, final String componentName) throws AdminException {
    return null;
  }

  @Override
  public CompoSpace[] getCompoForUser(final String sUserId, final String sComponentName)
      throws AdminException {
    return new CompoSpace[0];
  }

  @Override
  public String[] getCompoId(final String sComponentName) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getProfileIds(final String sUserId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getProfileIdsOfGroup(final String sGroupId) throws AdminException {
    return new String[0];
  }

  @Override
  public String[] getCurrentProfiles(final String sUserId, final ComponentInst componentInst) {
    return new String[0];
  }

  @Override
  public String[] getCurrentProfiles(final String sUserId, final String componentId)
      throws AdminException {
    return new String[0];
  }

  @Override
  public UserDetail[] getUsers(final boolean bAllProfiles, final String sProfile,
      final String sClientSpaceId, final String sClientComponentId) throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public Group[] getAllSubGroups(final String parentGroupId) throws AdminException {
    return new Group[0];
  }

  @Override
  public UserDetail[] getFiltredDirectUsers(final String sGroupId, final String sUserLastNameFilter)
      throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public int getAllSubUsersNumber(final String sGroupId) throws AdminException {
    return 0;
  }

  @Override
  public int getUsersNumberOfDomain(final String domainId) throws AdminException {
    return 0;
  }

  @Override
  public String[] getAdministratorUserIds(final String fromUserId) throws AdminException {
    return new String[0];
  }

  @Override
  public String getAdministratorEmail() {
    return null;
  }

  @Override
  public String getDAPIGeneralAdminId() {
    return null;
  }

  @Override
  public String[] getAllSpaceIds(final String sUserId) throws Exception {
    return new String[0];
  }

  @Override
  public String[] getAllRootSpaceIds(final String sUserId) throws Exception {
    return new String[0];
  }

  @Override
  public String[] getAllSubSpaceIds(final String sSpaceId, final String sUserId) throws Exception {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIds(final String sSpaceId) throws Exception {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIdsRecur(final String sSpaceId) throws Exception {
    return new String[0];
  }

  @Override
  public String[] getAllComponentIdsRecur(final String sSpaceId, final String sUserId,
      final String componentNameRoot, final boolean inCurrentSpace, final boolean inAllSpaces)
      throws Exception {
    return new String[0];
  }

  @Override
  public void synchronizeGroupByRule(final String groupId, final boolean scheduledMode)
      throws AdminException {

  }

  @Override
  public String synchronizeGroup(final String groupId, final boolean recurs) throws Exception {
    return null;
  }

  @Override
  public String synchronizeImportGroup(final String domainId, final String groupKey,
      final String askedParentId, final boolean recurs, final boolean isIdKey) throws Exception {
    return null;
  }

  @Override
  public String synchronizeRemoveGroup(final String groupId) throws Exception {
    return null;
  }

  @Override
  public String synchronizeUser(final String userId, final boolean recurs) throws Exception {
    return null;
  }

  @Override
  public String synchronizeImportUserByLogin(final String domainId, final String userLogin,
      final boolean recurs) throws Exception {
    return null;
  }

  @Override
  public String synchronizeImportUser(final String domainId, final String specificId,
      final boolean recurs) throws Exception {
    return null;
  }

  @Override
  public List<DomainProperty> getSpecificPropertiesToImportUsers(final String domainId,
      final String language) throws Exception {
    return null;
  }

  @Override
  public UserDetail[] searchUsers(final String domainId, final Map<String, String> query)
      throws Exception {
    return new UserDetail[0];
  }

  @Override
  public String synchronizeRemoveUser(final String userId) throws Exception {
    return null;
  }

  @Override
  public String synchronizeSilverpeasWithDomain(final String sDomainId) throws Exception {
    return null;
  }

  @Override
  public String synchronizeSilverpeasWithDomain(final String sDomainId, final boolean threaded)
      throws AdminException {
    return null;
  }

  @Override
  public String[] searchUsersIds(final String sGroupId, final String componentId,
      final String[] profileIds, final UserDetail modelUser) throws AdminException {
    return new String[0];
  }

  @Override
  public ListSlice<UserDetail> searchUsers(final UserDetailsSearchCriteria searchCriteria)
      throws AdminException {
    return null;
  }

  @Override
  public ListSlice<Group> searchGroups(final GroupsSearchCriteria searchCriteria)
      throws AdminException {
    return null;
  }

  @Override
  public String[] searchGroupsIds(final boolean isRootGroup, final String componentId,
      final String[] profileId, final Group modelGroup) throws AdminException {
    return new String[0];
  }

  @Override
  public void resetAllDBConnections(final boolean isScheduled) throws AdminException {

  }

  @Override
  public void indexAllUsers() throws AdminException {

  }

  @Override
  public void indexUsers(final String domainId) throws AdminException {

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
      final String authorId) throws AdminException {

  }

  @Override
  public void assignRightsFromUserToGroup(final RightAssignationContext.MODE operationMode,
      final String sourceUserId, final String targetGroupId, final boolean nodeAssignRights,
      final String authorId) throws AdminException {

  }

  @Override
  public void assignRightsFromGroupToUser(final RightAssignationContext.MODE operationMode,
      final String sourceGroupId, final String targetUserId, final boolean nodeAssignRights,
      final String authorId) throws AdminException {

  }

  @Override
  public void assignRightsFromGroupToGroup(final RightAssignationContext.MODE operationMode,
      final String sourceGroupId, final String targetGroupId, final boolean nodeAssignRights,
      final String authorId) throws AdminException {

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
}
