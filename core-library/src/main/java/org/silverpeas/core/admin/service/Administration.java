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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
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
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.Map;

/**
 * This interface defines all the required services to manage administration data (domains, spaces,
 * components, users, groups,...).
 * @author Yohann Chastagnier
 */
public interface Administration {
  String ADMIN_COMPONENT_ID = "ADMIN";
  String basketSuffix = " (Restauré)";

  public static Administration get() {
    return ServiceProvider.getService(Administration.class);
  }

  void reloadCache();

  // -------------------------------------------------------------------------
  // Start Server actions
  // -------------------------------------------------------------------------
  void startServer();

  /**
   * Get Enterprise space id.
   * @return The general space id
   */
  String getGeneralSpaceId();

  void createSpaceIndex(int spaceId);

  void createSpaceIndex(SpaceInstLight spaceInst);

  void deleteSpaceIndex(SpaceInst spaceInst);

  /**
   * add a space instance in database
   * @param userId Id of user who add the space
   * @param spaceInst SpaceInst object containing information about the space to be created
   * @return the created space id
   */
  String addSpaceInst(String userId, SpaceInst spaceInst) throws AdminException;

  /**
   * Delete the given space The delete is apply recursively to the sub-spaces
   * @param userId Id of user who deletes the space
   * @param spaceId Id of the space to be deleted
   * @param definitive
   * @return the deleted space id
   * @throws AdminException
   */
  String deleteSpaceInstById(String userId, String spaceId, boolean definitive)
      throws AdminException;

  /**
   * Delete the given space if it's not the general space The delete is apply recursively to the
   * sub-spaces
   * @param userId Id of user who deletes the space
   * @param spaceId Id of the space to be deleted
   * @param startNewTransaction Flag : must be true at first call to initialize transaction, then
   * false for recurrents calls
   * @param definitive
   * @return the deleted space id
   * @throws AdminException
   */
  String deleteSpaceInstById(String userId, String spaceId, boolean startNewTransaction,
      boolean definitive) throws AdminException;

  /**
   * @param spaceId
   * @throws AdminException
   */
  void restoreSpaceFromBasket(String spaceId) throws AdminException;

  /**
   * Get the space instance with the given space id.
   * @param spaceId client space id
   * @return Space information as SpaceInst object.
   * @throws AdminException
   */
  SpaceInst getSpaceInstById(String spaceId) throws AdminException;

  /**
   * @param userId
   * @return
   * @throws AdminException
   */
  SpaceInst getPersonalSpace(String userId) throws AdminException;

  /**
   * Get all the subspaces Ids available in Silverpeas given a domainFatherId (client id format)
   * @param domainFatherId Id of the father space
   * @return an array of String containing the ids of spaces that are child of given space.
   * @throws AdminException
   */
  String[] getAllSubSpaceIds(String domainFatherId) throws AdminException;

  /**
   * Updates the space (with the given name) with the given space Updates only the node
   * @param spaceInstNew SpaceInst object containing new information for space to be updated
   * @return the updated space id.
   * @throws AdminException
   */
  String updateSpaceInst(SpaceInst spaceInstNew) throws AdminException;

  /**
   * @param spaceId
   * @param orderNum
   * @throws AdminException
   */
  void updateSpaceOrderNum(String spaceId, int orderNum) throws AdminException;

  /**
   * Tests if a space with given space id exists.
   * @param spaceId if of space to be tested
   * @return true if the given space instance name is an existing space
   */
  boolean isSpaceInstExist(String spaceId) throws AdminException;

  /**
   * Return all the root spaces Ids available in Silverpeas.
   * @return all the root spaces Ids available in Silverpeas.
   * @throws AdminException
   */
  String[] getAllRootSpaceIds() throws AdminException;

  /**
   * Retrieve spaces from root to component
   * @param componentId the target component
   * @return a List of SpaceInstLight
   * @throws AdminException
   */
  List<SpaceInstLight> getPathToComponent(String componentId) throws AdminException;

  /**
   * Retrieve spaces from root to space identified by spaceId
   * @param spaceId the target space
   * @param includeTarget
   * @return a List of SpaceInstLight
   * @throws AdminException
   */
  List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget) throws AdminException;

  /**
   * Return the all the spaces Ids available in Silverpeas.
   * @return the all the spaces Ids available in Silverpeas.
   * @throws AdminException
   */
  String[] getAllSpaceIds() throws AdminException;

  /**
   * Returns all spaces which has been removed but not definitely deleted.
   * @return a List of SpaceInstLight
   * @throws AdminException
   */
  List<SpaceInstLight> getRemovedSpaces() throws AdminException;

  /**
   * Returns all components which has been removed but not definitely deleted.
   * @return a List of ComponentInstLight
   * @throws AdminException
   */
  List<ComponentInstLight> getRemovedComponents() throws AdminException;

  /**
   * Return the the spaces name corresponding to the given space ids
   * @param asClientSpaceIds
   * @return
   * @throws AdminException
   */
  String[] getSpaceNames(String[] asClientSpaceIds) throws AdminException;

  Map<String, SpaceTemplate> getAllSpaceTemplates();

  SpaceInst getSpaceInstFromTemplate(String templateName);

  /**
   * Return all the components of silverpeas read in the xmlComponent directory.
   * @return all the components of silverpeas read in the xmlComponent directory.
   */
  Map<String, WAComponent> getAllComponents();

  /**
   * Return the component Inst corresponding to the given ID
   * @param sClientComponentId
   * @return the component Inst corresponding to the given ID
   * @throws AdminException
   */
  ComponentInst getComponentInst(String sClientComponentId) throws AdminException;

  /**
   * Return the component Inst Light corresponding to the given ID
   * @param componentId
   * @return the component Inst Light corresponding to the given ID
   * @throws AdminException
   */
  ComponentInstLight getComponentInstLight(String componentId) throws AdminException;

  /**
   * Get the parameters for the given component.
   * @param componentId
   * @return the parameters for the given component.
   */
  List<Parameter> getComponentParameters(String componentId);

  /**
   * Return the value of the parameter for the given component and the given name of parameter
   * @param componentId
   * @param parameterName
   * @return the value of the parameter for the given component and the given name of parameter
   */
  String getComponentParameterValue(String componentId, String parameterName);

  void restoreComponentFromBasket(String componentId) throws AdminException;

  /**
   * Create the index for the specified component.
   * @param componentId
   */
  void createComponentIndex(String componentId);

  /**
   * Create the index for the specified component.
   * @param componentInst
   */
  void createComponentIndex(ComponentInstLight componentInst);

  String addComponentInst(String sUserId, ComponentInst componentInst)
      throws AdminException, QuotaException;

  /**
   * Add the given component instance in Silverpeas.
   * @param userId
   * @param componentInst
   * @param startNewTransaction
   * @return
   * @throws AdminException
   */
  String addComponentInst(String userId, ComponentInst componentInst, boolean startNewTransaction)
      throws AdminException, QuotaException;

  /**
   * Delete the specified component.
   * @param userId
   * @param componentId
   * @param definitive
   * @return
   * @throws AdminException
   */
  String deleteComponentInst(String userId, String componentId, boolean definitive)
      throws AdminException;

  /**
   * @param componentId
   * @param orderNum
   * @throws AdminException
   */
  void updateComponentOrderNum(String componentId, int orderNum) throws AdminException;

  /**
   * Update the given component in Silverpeas.
   * @param component
   * @return
   * @throws AdminException
   */
  String updateComponentInst(ComponentInst component) throws AdminException;

  /**
   * Set space profiles to a subspace. There is no persistance. The subspace object is enriched.
   * @param subSpace the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException
   */
  void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space)
      throws AdminException;

  void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space, boolean persist,
      boolean startNewTransaction) throws AdminException;

  void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space) throws AdminException;

  /**
   * Set space profile to a component. There is persistance.
   * @param component the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException
   */
  void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space,
      boolean startNewTransaction) throws AdminException;

  void moveSpace(String spaceId, String fatherId) throws AdminException;

  /**
   * Move the given component in Silverpeas.
   * @param spaceId
   * @param componentId
   * @param idComponentBefore
   * @param componentInsts
   * @throws AdminException
   */
  void moveComponentInst(String spaceId, String componentId, String idComponentBefore,
      ComponentInst[] componentInsts) throws AdminException;

  void setComponentPlace(String componentId, String idComponentBefore,
      ComponentInst[] m_BrothersComponents) throws AdminException;

  String getRequestRouter(String sComponentName);

  /**
   * Get all the profiles name available for the given component.
   * @param sComponentName
   * @return
   * @throws AdminException
   */
  String[] getAllProfilesNames(String sComponentName);

  /**
   * Get the profile label from its name.
   * @param sComponentName
   * @param sProfileName
   * @return
   * @throws AdminException
   */
  String getProfileLabelfromName(String sComponentName, String sProfileName, String lang);

  /**
   * Get the profile instance corresponding to the given id
   * @param sProfileId
   * @return
   * @throws AdminException
   */
  ProfileInst getProfileInst(String sProfileId) throws AdminException;

  List<ProfileInst> getProfilesByObject(String objectId, String objectType, String componentId)
      throws AdminException;

  String[] getProfilesByObjectAndUserId(int objectId, String objectType, String componentId,
      String userId) throws AdminException;

  boolean isObjectAvailable(String componentId, int objectId, String objectType, String userId)
      throws AdminException;

  String addProfileInst(ProfileInst profileInst) throws AdminException;

  String addProfileInst(ProfileInst profileInst, String userId) throws AdminException;

  String deleteProfileInst(String sProfileId, String userId) throws AdminException;

  String updateProfileInst(ProfileInst profileInstNew) throws AdminException;

  String updateProfileInst(ProfileInst profileInstNew, String userId) throws AdminException;

  /**
   * Get the space profile instance corresponding to the given ID
   * @param spaceProfileId
   * @return
   * @throws AdminException
   */
  SpaceProfileInst getSpaceProfileInst(String spaceProfileId) throws AdminException;

  String addSpaceProfileInst(SpaceProfileInst spaceProfile, String userId) throws AdminException;

  String deleteSpaceProfileInst(String sSpaceProfileId, String userId) throws AdminException;

  String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId)
      throws AdminException;

  String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId,
      boolean startNewTransaction) throws AdminException;

  /**
   * Get the group names corresponding to the given group ids.
   * @param groupIds
   * @return
   * @throws AdminException
   */
  String[] getGroupNames(String[] groupIds) throws AdminException;

  /**
   * Get the group name corresponding to the given group id.
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  String getGroupName(String sGroupId) throws AdminException;

  /**
   * Get the all the groups ids available in Silverpeas.
   * @return
   * @throws AdminException
   */
  String[] getAllGroupIds() throws AdminException;

  /**
   * Tests if group exists in Silverpeas.
   * @param groupName
   * @return true if a group with the given name
   * @throws AdminException
   */
  boolean isGroupExist(String groupName) throws AdminException;

  /**
   * Get group information with the given id
   * @param groupId
   * @return
   * @throws AdminException
   */
  Group getGroup(String groupId) throws AdminException;

  List<String> getPathToGroup(String groupId) throws AdminException;

  /**
   * Get group information with the given group name.
   * @param groupName
   * @param domainFatherId
   * @return
   * @throws AdminException
   */
  Group getGroupByNameInDomain(String groupName, String domainFatherId) throws AdminException;

  /**
   * Get groups information with the given ids.
   * @param asGroupId
   * @return
   * @throws AdminException
   */
  Group[] getGroups(String[] asGroupId) throws AdminException;

  /**
   * Add the given group in Silverpeas.
   * @param group
   * @return
   * @throws AdminException
   */
  String addGroup(Group group) throws AdminException;

  /**
   * Add the given group in Silverpeas.
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  String addGroup(Group group, boolean onlyInSilverpeas) throws AdminException;

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups.
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  String deleteGroupById(String sGroupId) throws AdminException;

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups.
   * @param sGroupId
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  String deleteGroupById(String sGroupId, boolean onlyInSilverpeas) throws AdminException;

  /**
   * Update the given group in Silverpeas and specific.
   * @param group
   * @return
   * @throws AdminException
   */
  String updateGroup(Group group) throws AdminException;

  /**
   * Update the given group in Silverpeas and specific
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  String updateGroup(Group group, boolean onlyInSilverpeas) throws AdminException;

  void removeUserFromGroup(String sUserId, String sGroupId) throws AdminException;

  void addUserInGroup(String sUserId, String sGroupId) throws AdminException;

  /**
   * Get Silverpeas organization
   */
  AdminGroupInst[] getAdminOrganization() throws AdminException;

  /**
   * Gets the set of Ids denoting the direct subgroups of a given group
   * @param groupId The ID of the parent group
   * @return the Ids as an array of <code>String</code>.
   */
  String[] getAllSubGroupIds(String groupId) throws AdminException;

  String[] getAllSubGroupIdsRecursively(String groupId) throws AdminException;

  /**
   * Gets the set of Ids denoting the groups without any parent.
   * @return the Ids as an array of <code>String</code>.
   */
  String[] getAllRootGroupIds() throws AdminException;

  /**
   * Gets all root user groups in Silverpeas. A root group is the group of users without any other
   * parent group.
   * @return an array of user groups.
   * @throws AdminException if an error occurs whil getting the
   * root user groups.
   */
  Group[] getAllRootGroups() throws AdminException;

  /**
   * Get the group profile instance corresponding to the given ID
   */
  GroupProfileInst getGroupProfileInst(String groupId) throws AdminException;

  String addGroupProfileInst(GroupProfileInst spaceProfileInst) throws AdminException;

  /**
   * Add the space profile instance from Silverpeas
   */
  String addGroupProfileInst(GroupProfileInst groupProfileInst, boolean startNewTransaction)
      throws AdminException;

  String deleteGroupProfileInst(String groupId) throws AdminException;

  /**
   * Delete the given space profile from Silverpeas
   */
  String deleteGroupProfileInst(String groupId, boolean startNewTransaction) throws AdminException;

  /**
   * Update the given space profile in Silverpeas
   */
  String updateGroupProfileInst(GroupProfileInst groupProfileInstNew) throws AdminException;

  /**
   * @throws AdminException
   */
  void indexAllGroups() throws AdminException;

  /**
   * @param domainId
   * @throws AdminException
   */
  void indexGroups(String domainId) throws AdminException;

  /**
   * Get all the users Ids available in Silverpeas
   */
  String[] getAllUsersIds() throws AdminException;

  /**
   * Get the user detail corresponding to the given user Id
   * @param sUserId the user id.
   * @return the user detail corresponding to the given user Id
   * @throws AdminException
   */
  UserDetail getUserDetail(String sUserId) throws AdminException;

  /**
   * Get the user details corresponding to the given user Ids.
   * @param userIds
   * @return the user details corresponding to the given user Ids.
   * @throws AdminException
   */
  UserDetail[] getUserDetails(String[] userIds);

  /**
   * Get all users (except delete ones) from all domains.
   * @return the user details from all domains sort by alphabetical order
   * @throws AdminException
   */
  List<UserDetail> getAllUsers() throws AdminException;

  /**
   * Get all users (except delete ones) from all domains.
   * @return the user details from all domains sort by reverse creation order
   * @throws AdminException
   */
  List<UserDetail> getAllUsersFromNewestToOldest() throws AdminException;

  /**
   * Checks if an existing user already have the given email
   * @param email email to check
   * @return true if at least one user with given email is found
   * @throws AdminException
   */
  boolean isEmailExisting(String email) throws AdminException;

  /**
   * Get the user Id corresponding to Domain/Login
   * @param sLogin
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  String getUserIdByLoginAndDomain(String sLogin, String sDomainId) throws AdminException;

  /**
   * @param authenticationKey The authentication key.
   * @return The user id corresponding to the authentication key.
   * @throws Exception
   */
  String getUserIdByAuthenticationKey(String authenticationKey) throws Exception;

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   * @param sUserId
   * @return
   * @throws AdminException
   */
  UserFull getUserFull(String sUserId) throws AdminException;

  UserFull getUserFull(String domainId, String specificId) throws Exception;

  /**
   * Add the given user in Silverpeas and specific domain.
   * @param userDetail
   * @return the new user id.
   * @throws AdminException
   */
  String addUser(UserDetail userDetail) throws AdminException;

  /**
   * Add the given user in Silverpeas and specific domain
   * @param userDetail user to add
   * @param addOnlyInSilverpeas true if user must not be added in distant datasource (used by
   * synchronization tools)
   * @return id of created user
   */
  String addUser(UserDetail userDetail, boolean addOnlyInSilverpeas) throws AdminException;

  void migrateUser(UserDetail userDetail, String targetDomainId) throws AdminException;

  /**
   * Blocks the user represented by the given identifier.
   * @param userId
   * @throws AdminException
   */
  void blockUser(String userId) throws AdminException;

  /**
   * Unblock the user represented by the given identifier.
   * @param userId
   * @throws AdminException
   */
  void unblockUser(String userId) throws AdminException;

  /**
   * Deactivates the user represented by the given identifier.
   *
   * @param userId
   * @throws AdminException
   */
  void deactivateUser(String userId) throws AdminException;

  /**
   * Activate the user represented by the given identifier.
   *
   * @param userId
   * @throws AdminException
   */
  void activateUser(String userId) throws AdminException;

  /**
   * Updates the acceptance date of a user from its id.
   * @param userId
   * @throws AdminException
   */
  void userAcceptsTermsOfService(String userId) throws AdminException;

  /**
   * Delete the given user from silverpeas and specific domain
   */
  String deleteUser(String sUserId) throws AdminException;

  /**
   * Delete the given user from silverpeas and specific domain
   */
  String deleteUser(String sUserId, boolean onlyInSilverpeas) throws AdminException;

  /**
   * Update the given user (ONLY IN SILVERPEAS)
   */
  String updateUser(UserDetail user) throws AdminException;

  /**
   * Update the given user in Silverpeas and specific domain
   */
  String updateUserFull(UserFull user) throws AdminException;

  /**
   * Converts driver space id to client space id
   */
  String getClientSpaceId(String sDriverSpaceId);

  /**
   * Converts driver space ids to client space ids
   */
  String[] getClientSpaceIds(String[] asDriverSpaceIds) throws Exception;

  /**
   * Create a new domain
   */
  String getNextDomainId() throws AdminException;

  /**
   * Create a new domain
   */
  String addDomain(Domain theDomain) throws AdminException;

  /**
   * Update a domain
   */
  String updateDomain(Domain domain) throws AdminException;

  /**
   * Remove a domain
   */
  String removeDomain(String domainId) throws AdminException;

  /**
   * Get all domains
   */
  Domain[] getAllDomains() throws AdminException;

  /**
   * Get all domain ids for the specified login.
   */
  List<String> getAllDomainIdsForLogin(String login) throws AdminException;

  /**
   * Get a domain with given id
   */
  Domain getDomain(String domainId) throws AdminException;

  /**
   * Get a domain with given id
   */
  long getDomainActions(String domainId) throws AdminException;

  Group[] getRootGroupsOfDomain(String domainId) throws AdminException;

  Group[] getSynchronizedGroups() throws AdminException;

  String[] getRootGroupIdsOfDomain(String domainId) throws AdminException;

  UserDetail[] getAllUsersOfGroup(String groupId) throws AdminException;

  UserDetail[] getUsersOfDomain(String domainId) throws AdminException;

  /**
   * Get all users (except delete ones) from specified domains.
   * @return the user details from specified domains sort by alphabetical order
   * @throws AdminException
   */
  List<UserDetail> getUsersOfDomains(List<String> domainIds) throws AdminException;

  /**
   * Get all users (except delete ones) from specified domains.
   * @return the user details from specified domains sort by reverse creation order
   * @throws AdminException
   */
  List<UserDetail> getUsersOfDomainsFromNewestToOldest(List<String> domainIds)
      throws AdminException;

  String[] getUserIdsOfDomain(String domainId) throws AdminException;

  /**
   * Get the user id for the given login password
   */
  String identify(String sKey, String sSessionId, boolean isAppInMaintenance) throws AdminException;

  /**
   * Get the user id for the given login password
   */
  String identify(String sKey, String sSessionId, boolean isAppInMaintenance, boolean removeKey)
      throws AdminException;

  // ---------------------------------------------------------------------------------------------
  // QUERY FUNCTIONS
  // ---------------------------------------------------------------------------------------------
  String[] getDirectGroupsIdsOfUser(String userId) throws AdminException;

  UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd) throws AdminException;

  Group[] searchGroups(Group modelGroup, boolean isAnd) throws AdminException;

  /**
   * Get the spaces ids allowed for the given user Id
   */
  String[] getUserSpaceIds(String sUserId) throws AdminException;

  /**
   * Get the root spaces ids allowed for the given user Id
   */
  String[] getUserRootSpaceIds(String sUserId) throws AdminException;

  String[] getUserSubSpaceIds(String sUserId, String spaceId) throws AdminException;

  /**
   * This method permit to know if given space is allowed to given user.
   * @param userId
   * @param spaceId
   * @return true if user is allowed to access to one component (at least) in given space, false
   * otherwise.
   * @throws AdminException
   */
  boolean isSpaceAvailable(String userId, String spaceId) throws AdminException;

  /**
   * Get subspaces of a given space available to a user.
   * @param userId
   * @param spaceId
   * @return a list of SpaceInstLight
   * @throws AdminException
   * @author neysseri
   */
  List<SpaceInstLight> getSubSpacesOfUser(String userId, String spaceId) throws AdminException;

  List<SpaceInstLight> getSubSpaces(String spaceId) throws AdminException;

  /**
   * Get components of a given space (and subspaces) available to a user.
   * @param userId
   * @param spaceId
   * @return a list of ComponentInstLight
   * @throws AdminException
   * @author neysseri
   */
  List<ComponentInstLight> getAvailCompoInSpace(String userId, String spaceId)
      throws AdminException;

  Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId) throws AdminException;

  /**
   * Get all spaces available to a user. N levels compliant. Infos of each space are in
   * SpaceInstLight object.
   * @param userId
   * @return an ordered list of SpaceInstLight. Built according a depth-first algorithm.
   * @throws Exception
   * @author neysseri
   */
  List<SpaceInstLight> getUserSpaceTreeview(String userId) throws Exception;

  String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) throws AdminException;

  /**
   * Get the space instance light (only spaceid, fatherId and name) with the given space id
   * @param sClientSpaceId client space id (as WAxx)
   * @return Space information as SpaceInstLight object
   */
  SpaceInstLight getSpaceInstLightById(String sClientSpaceId) throws AdminException;

  /**
   * Return the higher space according to a subspace (N level compliant)
   * @param spaceId the subspace id
   * @return a SpaceInstLight object
   * @throws AdminException
   */
  SpaceInstLight getRootSpace(String spaceId) throws AdminException;

  /**
   * Get the spaces ids manageable by given group Id
   */
  String[] getGroupManageableSpaceIds(String sGroupId) throws AdminException;

  /**
   * Get the spaces ids manageable by given user Id
   */
  String[] getUserManageableSpaceIds(String sUserId) throws AdminException;

  /**
   * Get the spaces roots ids manageable by given user Id
   */
  String[] getUserManageableSpaceRootIds(String sUserId) throws AdminException;

  /**
   * Get the sub space ids manageable by given user Id in given space
   */
  String[] getUserManageableSubSpaceIds(String sUserId, String sParentSpaceId)
      throws AdminException;

  List<String> getUserManageableGroupIds(String sUserId) throws AdminException;

  /**
   * Get the component ids allowed for the given user Id in the given space
   */
  String[] getAvailCompoIds(String sClientSpaceId, String sUserId) throws AdminException;

  /**
   * Is the specified tool belongs to the administration component?
   * </p>
   * The administration component (or administrive console) forms a particular component made up of
   * several tools, each of them providing an administrative feature. Each tool in the
   * administration component have the same identifier that refers in fact the administration
   * console.
   * @param toolId the unique identifier of the tool.
   * @return true if the tool belongs to the administration component.
   */
  boolean isAnAdminTool(String toolId);

  /**
   * Is the specified component instance available among the components instances accessibles by
   * the
   * specified user?
   * </p>
   * A component is an application in Silverpeas to perform some tasks and to manage some
   * resources.
   * Each component in Silverpeas can be instanciated several times, each of them corresponding
   * then
   * to a running application in Silverpeas and it is uniquely identified from others instances by
   * a
   * given identifier.
   * @param componentId the unique identifier of a component instance.
   * @param userId the unique identifier of a user.
   * @return true if the component instance is available, false otherwise.
   */
  boolean isComponentAvailable(String componentId, String userId) throws AdminException;

  boolean isComponentManageable(String componentId, String userId) throws AdminException;

  /**
   * Get ids of components allowed to user in given space (not in subspaces)
   * @return an array of componentId (kmelia12, hyperlink145...)
   * @throws AdminException
   */
  String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) throws AdminException;

  /**
   * Get the componentIds allowed for the given user Id in the given space and the
   * componentNameRoot
   * @param sClientSpaceId
   * @param sUserId
   * @param componentNameRoot
   * @return ArrayList of componentIds
   * @author dlesimple
   */
  List<String> getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId,
      String componentNameRoot) throws AdminException;

  /**
   * Get the component ids allowed for the given user Id.
   * @param userId
   */
  String[] getAvailCompoIds(String userId) throws AdminException;

  /**
   * Get the driver component ids allowed for the given user Id in the given space
   */
  String[] getAvailDriverCompoIds(String sClientSpaceId, String sUserId) throws AdminException;

  String[] getComponentIdsByNameAndUserId(String sUserId, String sComponentName)
      throws AdminException;

  /**
   * gets the available component for a given user
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   * @throws AdminException
   */
  List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName)
      throws AdminException;

  /**
   * This method returns all root spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
   * @param userId
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException
   */
  List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName)
      throws AdminException;

  /**
   * This method returns all sub spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
   * @param userId
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException
   */
  List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName) throws AdminException;

  /**
   * Get the tuples (space id, compo id) allowed for the given user and given component name
   */
  CompoSpace[] getCompoForUser(String sUserId, String sComponentName) throws AdminException;

  /**
   * Return the compo id for the given component name
   */
  String[] getCompoId(String sComponentName) throws AdminException;

  /**
   * Get all the profiles Id for the given user
   */
  String[] getProfileIds(String sUserId) throws AdminException;

  /**
   * Get all the profiles Id for the given group
   */
  String[] getProfileIdsOfGroup(String sGroupId) throws AdminException;

  /**
   * Get the profile names of the given user for the given component
   */
  String[] getCurrentProfiles(String sUserId, ComponentInst componentInst);

  /**
   * Get the profile names of the given user for the given component
   */
  String[] getCurrentProfiles(String sUserId, String componentId) throws AdminException;

  /**
   * if bAllProfiles = true, return all the user details for the given space and given component if
   * bAllProfiles = false, return the user details only for the given profile for the given space
   * and given component
   */
  UserDetail[] getUsers(boolean bAllProfiles, String sProfile, String sClientSpaceId,
      String sClientComponentId) throws AdminException;

  /**
   * For use in userPanel : return the direct sub-groups
   */
  Group[] getAllSubGroups(String parentGroupId) throws AdminException;

  /**
   * For use in userPanel : return the users that are direct child of a given group
   */
  UserDetail[] getFiltredDirectUsers(String sGroupId, String sUserLastNameFilter)
      throws AdminException;

  /**
   * For use in userPanel : return the total number of users recursively contained in a group
   */
  int getAllSubUsersNumber(String sGroupId) throws AdminException;

  /**
   * this method gets number user in domain. If domain id is null, it returns number user of all
   * domain
   */
  int getUsersNumberOfDomain(String domainId) throws AdminException;

  /**
   * Get the Ids of the administrators
   */
  String[] getAdministratorUserIds(String fromUserId) throws AdminException;

  /**
   * Get administrator Email
   * @return String
   */
  String getAdministratorEmail();

  /**
   * Get the administrator email
   */
  String getDAPIGeneralAdminId();

  // -------------------------------------------------------------------
  // RE-INDEXATION
  // -------------------------------------------------------------------
  String[] getAllSpaceIds(String sUserId) throws Exception;

  /**
   * Return all the root spaces Id available in webactiv
   */
  String[] getAllRootSpaceIds(String sUserId) throws Exception;

  /**
   * Return all the subSpaces Id available in webactiv given a space id (driver format)
   */
  String[] getAllSubSpaceIds(String sSpaceId, String sUserId) throws Exception;

  /**
   * Return all the components Id in the subspaces available in webactiv given a space id
   */
  String[] getAllComponentIds(String sSpaceId) throws Exception;

  /**
   * Return all the componentIds recursively in the subspaces available in webactiv given a space
   * id
   */
  String[] getAllComponentIdsRecur(String sSpaceId) throws Exception;

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces or in
   * Silverpeas) available in silverpeas given a userId and a componentNameRoot
   * @param sSpaceId
   * @param sUserId
   * @param componentNameRoot
   * @param inCurrentSpace
   * @param inAllSpaces
   * @return Array of componentsIds
   * @author dlesimple
   */
  String[] getAllComponentIdsRecur(String sSpaceId, String sUserId, String componentNameRoot,
      boolean inCurrentSpace, boolean inAllSpaces) throws Exception;

  void synchronizeGroupByRule(String groupId, boolean scheduledMode) throws AdminException;

  /**
   *
   */
  String synchronizeGroup(String groupId, boolean recurs) throws Exception;

  /**
   *
   */
  String synchronizeImportGroup(String domainId, String groupKey, String askedParentId,
      boolean recurs, boolean isIdKey) throws Exception;

  /**
   *
   */
  String synchronizeRemoveGroup(String groupId) throws Exception;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  String synchronizeUser(String userId, boolean recurs) throws Exception;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  String synchronizeImportUserByLogin(String domainId, String userLogin, boolean recurs)
      throws Exception;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  String synchronizeImportUser(String domainId, String specificId, boolean recurs) throws Exception;

  List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId, String language)
      throws Exception;

  UserDetail[] searchUsers(String domainId, Map<String, String> query) throws Exception;

  /**
   * Synchronize Users and groups between cache and domain's datastore.
   * @param userId
   * @return
   * @throws Exception
   */
  String synchronizeRemoveUser(String userId) throws Exception;

  String synchronizeSilverpeasWithDomain(String sDomainId) throws Exception;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  String synchronizeSilverpeasWithDomain(String sDomainId, boolean threaded) throws AdminException;

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------
  String[] searchUsersIds(String sGroupId, String componentId, String[] profileIds,
      UserDetail modelUser) throws AdminException;

  ListSlice<UserDetail> searchUsers(UserDetailsSearchCriteria searchCriteria) throws AdminException;

  ListSlice<Group> searchGroups(GroupsSearchCriteria searchCriteria) throws AdminException;

  String[] searchGroupsIds(boolean isRootGroup, String componentId, String[] profileId,
      Group modelGroup) throws AdminException;

  // -------------------------------------------------------------------------
  // For DB connection reset
  // -------------------------------------------------------------------------
  void resetAllDBConnections(boolean isScheduled) throws AdminException;

  // -------------------------------------------------------------------------
  // Node profile management
  // -------------------------------------------------------------------------
  void indexAllUsers() throws AdminException;

  void indexUsers(String domainId) throws AdminException;

  String copyAndPasteComponent(PasteDetail pasteDetail) throws AdminException, QuotaException;

  String copyAndPasteSpace(PasteDetail pasteDetail) throws AdminException, QuotaException;

  /*
   * Assign rights of a user to a user
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceUserId : the user id of the source user
   * @param targetUserId : the user id of the target user
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  void assignRightsFromUserToUser(RightAssignationContext.MODE operationMode, String sourceUserId,
      String targetUserId, boolean nodeAssignRights, String authorId) throws AdminException;

  /*
   * Assign rights of a user to a group
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceUserId : the user id of the source user
   * @param targetGroupId : the group id of the target group
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  void assignRightsFromUserToGroup(RightAssignationContext.MODE operationMode, String sourceUserId,
      String targetGroupId, boolean nodeAssignRights, String authorId) throws AdminException;

  /*
   * Assign rights of a group to a user
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceGroupId : the group id of the source group
   * @param targetUserId : the user id of the target user
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  void assignRightsFromGroupToUser(RightAssignationContext.MODE operationMode, String sourceGroupId,
      String targetUserId, boolean nodeAssignRights, String authorId) throws AdminException;

  /*
   * Assign rights of a group to a group
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceGroupId : the group id of the source group
   * @param targetGroupId : the group id of the target group
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  void assignRightsFromGroupToGroup(RightAssignationContext.MODE operationMode,
      String sourceGroupId, String targetGroupId, boolean nodeAssignRights, String authorId)
      throws AdminException;

  /**
   * Is the specified user a manager of the specified domain?
   * @param userId the user identifier.
   * @param domainId the domain identifier.
   * @return true if user identified by given userId is the manager of given domain identifier.
   */
  boolean isDomainManagerUser(String userId, String domainId);
}
