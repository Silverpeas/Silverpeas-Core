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
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines all the required services to manage administration data (domains, spaces,
 * components, users, groups,...).
 * @author Yohann Chastagnier
 */
public interface Administration {

  class Constants {
    public static final String ADMIN_COMPONENT_ID = "ADMIN";
    public static final String BASKET_SUFFIX = " (Restauré)";

    private Constants() {

    }
  }

  static Administration get() {
    return ServiceProvider.getSingleton(Administration.class);
  }

  void reloadCache();

  // -------------------------------------------------------------------------
  // Start Server actions
  // -------------------------------------------------------------------------
  void initSynchronization();

  void createSpaceIndex(int spaceId);

  void createSpaceIndex(SpaceInstLight spaceInst);

  void deleteSpaceIndex(SpaceInst spaceInst);

  void deleteAllSpaceIndexes();

  /**
   * add a space instance in database
   * @param userId Id of user who add the space
   * @param spaceInst SpaceInst object containing information about the space to be created
   * @return the created space id
   */
  String addSpaceInst(String userId, SpaceInst spaceInst) throws AdminException;

  /**
   * Delete the given space. It is applied recursively to the sub-spaces
   * @param userId Id of user who deletes the space
   * @param spaceId Id of the space to be deleted
   * @param definitive is the deletion is definitive?
   * @return the deleted space id
   * @throws AdminException if an error occurs while deleting the space.
   */
  String deleteSpaceInstById(String userId, String spaceId, boolean definitive)
      throws AdminException;

  /**
   * Restores the space from the basket.
   * @param spaceId the unique identifier of the space to restore.
   * @throws AdminException if an error occurs while restoring the space.
   */
  void restoreSpaceFromBasket(String spaceId) throws AdminException;

  /**
   * Get the space instance with the given space id.
   * @param spaceId client space id
   * @return Space information as SpaceInst object.
   * @throws AdminException if an error occurs while getting the space.
   */
  SpaceInst getSpaceInstById(String spaceId) throws AdminException;

  SpaceInst getPersonalSpace(String userId) throws AdminException;

  /**
   * Get all the subspaces Ids available in Silverpeas given a domainFatherId (client id format)
   * @param domainFatherId Id of the father space
   * @return an array of String containing the ids of spaces that are child of given space.
   * @throws AdminException if an error occurs all the subspaces.
   */
  String[] getAllSubSpaceIds(String domainFatherId) throws AdminException;

  /**
   * Updates the space (with the given name) with the given space Updates only the node
   * @param spaceInstNew SpaceInst object containing new information for space to be updated
   * @return the updated space id.
   * @throws AdminException if an error occurs while updating the space.
   */
  String updateSpaceInst(SpaceInst spaceInstNew) throws AdminException;

  void updateSpaceOrderNum(String spaceId, int orderNum) throws AdminException;

  /**
   * Return all the root spaces Ids available in Silverpeas.
   * @return all the root spaces Ids available in Silverpeas.
   * @throws AdminException if an error occurs
   */
  String[] getAllRootSpaceIds() throws AdminException;

  /**
   * Retrieve spaces from root to component
   * @param componentId the target component
   * @return a List of SpaceInstLight
   * @throws AdminException if an error occurs
   */
  List<SpaceInstLight> getPathToComponent(String componentId) throws AdminException;

  /**
   * Retrieve all the spaces that are parent to the given space up to the root space.
   * @param spaceId the target space
   * @param includeTarget is the target space should be included with the returned spaces.
   * @return an ordered list of space, from the root space down to the targeted space.
   * @throws AdminException if an error occurs
   */
  List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget) throws AdminException;

  /**
   * Return the all the spaces Ids available in Silverpeas.
   * @return the all the spaces Ids available in Silverpeas.
   * @throws AdminException if an error occurs
   */
  String[] getAllSpaceIds() throws AdminException;

  /**
   * Returns all spaces which has been removed but not definitely deleted.
   * @return a List of SpaceInstLight
   * @throws AdminException if an error occurs
   */
  List<SpaceInstLight> getRemovedSpaces() throws AdminException;

  /**
   * Returns all components which has been removed but not definitely deleted.
   * @return a List of ComponentInstLight
   * @throws AdminException if an error occurs
   */
  List<ComponentInstLight> getRemovedComponents() throws AdminException;

  /**
   * Return the spaces name corresponding to the given space ids
   * @param asClientSpaceIds the global space identifiers
   * @return the name of the specified spaces
   * @throws AdminException if an error occurs
   */
  String[] getSpaceNames(String[] asClientSpaceIds) throws AdminException;

  /**
   * Return all the components of silverpeas read in the xmlComponent directory.
   * @return all the components of silverpeas read in the xmlComponent directory.
   */
  Map<String, WAComponent> getAllWAComponents();

  /**
   * Gets the component instance related to the given identifier.<br> In contrary to
   * {@link #getComponentInst(String)}, {@link #getComponentInstLight(String)} signatures, this one
   * is able to return different kinds of implementation of {@link SilverpeasComponentInstance}:
   * <ul>
   *   <li>{@link PersonalComponentInstance}</li>
   *   <li>{@link ComponentInstLight}</li>
   * </ul>
   * So, this signature is useful into contexts of transversal treatments.
   * @param componentInstanceIdentifier the identifier of the requested component instance.
   * @return an optional component instance.
   */
  SilverpeasComponentInstance getComponentInstance(String componentInstanceIdentifier)
      throws AdminException;

  /**
   * Return the component Inst corresponding to the given ID
   * @param sClientComponentId the global component instance identifier.
   * @return the component Inst corresponding to the given ID
   * @throws AdminException if an error occurs
   */
  ComponentInst getComponentInst(String sClientComponentId) throws AdminException;

  /**
   * Return the component Inst Light corresponding to the given ID
   * @param componentId the component instance identifier
   * @return the component Inst Light corresponding to the given ID
   * @throws AdminException if an error occurs
   */
  ComponentInstLight getComponentInstLight(String componentId) throws AdminException;

  /**
   * Return the value of the parameter for the given component and the given name of parameter
   * @param componentId the component instance identifier
   * @param parameterName the name of the parameter
   * @return the value of the parameter for the given component and the given name of parameter
   */
  String getComponentParameterValue(String componentId, String parameterName);

  /**
   * Gets all parameters values by component and by parameter name.
   * @param componentIds list of component identifier.
   * @param paramNames optional list of parameter name. All parameters are retrieved if it is not
   * filled or null
   * @return a map filled with couples of parameter name / value per component instance identifier.
   */
  Map<String, Map<String, String>> getParameterValuesByComponentIdThenByParamName(
      final Collection<String> componentIds, final Collection<String> paramNames);

  List<ComponentInstLight> getComponentsWithParameter(String paramName, String paramValue);

  void restoreComponentFromBasket(String componentId) throws AdminException;

  /**
   * Create the index for the specified component.
   * @param componentId the unique identifier of a component instance
   */
  void createComponentIndex(String componentId);

  /**
   * Create the index for the specified component.
   * @param componentInst a component instance
   */
  void createComponentIndex(SilverpeasComponentInstance componentInst);

  void deleteAllComponentIndexes();

  String addComponentInst(String sUserId, ComponentInst componentInst)
      throws AdminException, QuotaException;

  /**
   * Delete the specified component.
   * @param userId the unique identifier of a user
   * @param componentId the unique identifier of a component instance
   * @param definitive is the deletion definitive?
   * @return the identifier of the deleted component instance.
   * @throws AdminException if an error occurs
   */
  String deleteComponentInst(String userId, String componentId, boolean definitive)
      throws AdminException;

  /**
   * Update the given component in Silverpeas.
   * @param component the component instance.
   * @return the unique identifier of the updated component instance.
   * @throws AdminException if an error occurs
   */
  String updateComponentInst(ComponentInst component) throws AdminException;

  /**
   * Set space profiles to a subspace. There is no persistance. The subspace object is enriched.
   * @param subSpace the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException if an error occurs
   */
  void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space)
      throws AdminException;

  void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space, boolean persist)
      throws AdminException;

  void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space) throws AdminException;

  void moveSpace(String spaceId, String fatherId) throws AdminException;

  /**
   * Move the given component in Silverpeas.
   * @param spaceId the unique identifier of the targeted space.
   * @param componentId the unique identifier of the component.
   * @param idComponentBefore the unique identifier of the component placed before the new position
   * of the component in the targeted space.
   * @param componentInsts the list of component instances in the targeted spaces.
   * @throws AdminException if an error occurs
   */
  void moveComponentInst(String spaceId, String componentId, String idComponentBefore,
      ComponentInst[] componentInsts) throws AdminException;

  void setComponentPlace(String componentId, String idComponentBefore,
      ComponentInst[] brothersComponents) throws AdminException;

  String getRequestRouter(String sComponentName);

  /**
   * Get all the profiles name available for the given component.
   * @param sComponentName the unique identifier of the component instance.
   * @return an array of role profiles defined for the given component instance.
   */
  String[] getAllProfilesNames(String sComponentName);

  /**
   * Get the profile label from its name.
   * @param sComponentName the unique identifier of the component instance.
   * @param sProfileName the name of the role profile
   * @return the label of the profile
   */
  String getProfileLabelFromName(String sComponentName, String sProfileName, String lang);

  /**
   * Get the profile instance corresponding to the given id
   * @param sProfileId the unique identifier of the profile.
   * @return the role profile
   * @throws AdminException if an error occurs
   */
  ProfileInst getProfileInst(String sProfileId) throws AdminException;

  /**
   * Gets all the profiles of the specified user in the given component instance. A profile is a
   * mapping between a role and the privileges the role provides to a user in a given space or
   * applications in Silverpeas. Both inherited and explicit profiles of the given component
   * instance are taken into account, either through a group of users or by the user himself.
   * @param userId the unique identifier of a user.
   * @param componentId the unique identifier of a component instance in Silverpeas.
   * @return a list with the profiles the user has in the given component instance.
   */
  List<ProfileInst> getAllProfiles(String userId, String componentId);

  List<ProfileInst> getProfilesByObject(ProfiledObjectId objectRef, String componentId)
      throws AdminException;

  String[] getProfilesByObjectAndGroupId(ProfiledObjectId objectRef, String componentId,
      String groupId) throws AdminException;

  String[] getProfilesByObjectAndUserId(ProfiledObjectId objectRef, String componentId,
      String userId) throws AdminException;

  /**
   * Gets the profile names of given user indexed by couple of given component instances and object
   * instances.
   * @param profiledObjectIds if NOTHING is given, then all the rows associated to the type are
   * returned, otherwise all the rows associated to type and ids.
   * @param componentIds list of component instance identifier as string.
   * @param userId a user identifier as string.
   * @return a map filled with list of profile name as string by couple component instance
   * identifier as string - object identifier as integer.
   */
  Map<Pair<String, String>, Set<String>> getUserProfilesByComponentIdAndObjectId(
      ProfiledObjectIds profiledObjectIds, Collection<String> componentIds, String userId)
      throws AdminException;

  Map<String, List<String>> getProfilesByObjectTypeAndUserId(ProfiledObjectType profiledObjectType,
      String componentId, String userId) throws AdminException;

  boolean isObjectAvailableToUser(String componentId, ProfiledObjectId objectRef, String userId)
      throws AdminException;

  boolean isObjectAvailableToGroup(String componentId, ProfiledObjectId objectRef, String groupId)
      throws AdminException;

  String addProfileInst(ProfileInst profileInst) throws AdminException;

  String addProfileInst(ProfileInst profileInst, String userId) throws AdminException;

  String deleteProfileInst(String sProfileId, String userId) throws AdminException;

  String updateProfileInst(ProfileInst profileInstNew) throws AdminException;

  String updateProfileInst(ProfileInst profileInstNew, String userId) throws AdminException;

  /**
   * Get the space profile instance corresponding to the given ID
   * @param spaceProfileId the unique profile identifier.
   * @return the profile instance.
   * @throws AdminException if an error occurs
   */
  SpaceProfileInst getSpaceProfileInst(String spaceProfileId) throws AdminException;

  String addSpaceProfileInst(SpaceProfileInst spaceProfile, String userId) throws AdminException;

  void deleteSpaceProfileInst(String sSpaceProfileId, String userId) throws AdminException;

  String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId)
      throws AdminException;


  /**
   * Get the group name corresponding to the given group id.
   * @param sGroupId the unique identifier of a group.
   * @return the name of the group.
   * @throws AdminException if an error occurs
   */
  String getGroupName(String sGroupId) throws AdminException;

  /**
   * Get the all the {@link GroupState#VALID} groups available in Silverpeas.
   * @return a list of available user groups in Silverpeas.
   * @throws AdminException if an error occurs
   */
  List<GroupDetail> getAllGroups() throws AdminException;

  /**
   * Tests if {@link GroupState#VALID} group exists in Silverpeas.
   * @param groupName the name of a group
   * @return true if a group with the given name
   * @throws AdminException if an error occurs
   */
  boolean isGroupExist(String groupName) throws AdminException;

  /**
   * Get group information with the given id
   * @param groupId the unique identifier of a group
   * @return a user group instance.
   * @throws AdminException if an error occurs
   */
  GroupDetail getGroup(String groupId) throws AdminException;

  List<String> getPathToGroup(String groupId) throws AdminException;

  /**
   * Get group information with the given group name.
   * @param groupName the name of the group.
   * @param domainFatherId the identifier of a user domain.
   * @return a user group instance.
   * @throws AdminException if an error occurs
   */
  GroupDetail getGroupByNameInDomain(String groupName, String domainFatherId) throws AdminException;

  /**
   * Get groups information with the given ids.
   * @param asGroupId one or more group identifiers.
   * @return the group instances of the specified identifiers.
   * @throws AdminException if an error occurs
   */
  GroupDetail[] getGroups(String[] asGroupId) throws AdminException;

  /**
   * Add the given group in Silverpeas.
   * @param group a user group.
   * @return the identifier of the new group.
   * @throws AdminException if an error occurs
   */
  String addGroup(GroupDetail group) throws AdminException;

  /**
   * Add the given group in Silverpeas.
   * @param group a user group.
   * @param onlyInSilverpeas performs the operation only in Silverpeas
   * @return the identifier of the new group
   * @throws AdminException if an error occurs
   */
  String addGroup(GroupDetail group, boolean onlyInSilverpeas) throws AdminException;

  /**
   * Restores the given group from silverpeas and specific domain
   * @param groupId the group identifier
   * @return all the group instance which have been restored from the given one.
   * @throws AdminException if an error occurs
   */
  List<GroupDetail> restoreGroup(String groupId) throws AdminException;

  /**
   * Removes the given group from silverpeas and specific domain
   * @param groupId the group identifier
   * @return all the group instance which have been removed from the given one.
   * @throws AdminException if an error occurs
   */
  List<GroupDetail> removeGroup(String groupId) throws AdminException;

  /**
   * Delete the group with the given Id. The deletion is applied recursively to the subgroups.
   * @param sGroupId the unique identifier of a group
   * @return the identifier of the deleted group.
   * @throws AdminException if an error occurs
   */
  List<GroupDetail> deleteGroupById(String sGroupId) throws AdminException;

  /**
   * Delete the group with the given id. The deletion is applied recursively to the subgroups.
   * @param sGroupId the unique identifier of a group.
   * @param onlyInSilverpeas performs the operation only in Silverpeas.
   * @return the identifier of the deleted group.
   * @throws AdminException if an error occurs.
   */
  List<GroupDetail> deleteGroupById(String sGroupId, boolean onlyInSilverpeas)
      throws AdminException;

  /**
   * Update the given group in Silverpeas and specific.
   * @param group the group to update
   * @return the unique identifier of the updated group
   * @throws AdminException if an error occurs
   */
  String updateGroup(GroupDetail group) throws AdminException;

  /**
   * Update the given group in Silverpeas and specific
   * @param group the group to update
   * @param onlyInSilverpeas performs the operation only in Silverpeas
   * @return the unique identifier of the updated group
   * @throws AdminException if an error occurs
   */
  String updateGroup(GroupDetail group, boolean onlyInSilverpeas) throws AdminException;

  void removeUserFromGroup(String sUserId, String sGroupId) throws AdminException;

  void addUserInGroup(String sUserId, String sGroupId) throws AdminException;

  /**
   * Gets all {@link GroupState#VALID} root groups in Silverpeas. A root group is the group of users
   * without any other parent group.
   * @return a list of user groups.
   * @throws AdminException if an error occurs whil getting the root user groups.
   */
  List<GroupDetail> getAllRootGroups() throws AdminException;

  /**
   * Get the group profile instance corresponding to the given ID
   */
  GroupProfileInst getGroupProfileInst(String groupId) throws AdminException;

  void addGroupProfileInst(GroupProfileInst spaceProfileInst) throws AdminException;

  void deleteGroupProfileInst(String groupId) throws AdminException;

  void updateGroupProfileInst(GroupProfileInst groupProfileInstNew) throws AdminException;

  void indexAllGroups() throws AdminException;

  void indexGroups(String domainId) throws AdminException;

  String[] getAllUsersIds() throws AdminException;

  /**
   * Get the user detail corresponding to the given user Id
   * @param sUserId the user id.
   * @return the user detail corresponding to the given user Id
   * @throws AdminException if an error occurs
   */
  UserDetail getUserDetail(String sUserId) throws AdminException;

  /**
   * Get the user details corresponding to the given user Ids.
   * @param userIds one or more user identifiers
   * @return the user details corresponding to the given user Ids.
   */
  UserDetail[] getUserDetails(String[] userIds);

  /**
   * Get all users (except delete ones) from all domains.
   * @return the user details from all domains sort by alphabetical order
   * @throws AdminException if an error occurs
   */
  List<UserDetail> getAllUsers() throws AdminException;

  /**
   * Get all users (except delete ones) from all domains.
   * @return the user details from all domains sort by reverse creation order
   * @throws AdminException if an error occurs
   */
  List<UserDetail> getAllUsersFromNewestToOldest() throws AdminException;

  /**
   * Checks if an existing user already have the given email
   * @param email email to check
   * @return true if at least one user with given email is found
   * @throws AdminException if an error occurs
   */
  boolean isEmailExisting(String email) throws AdminException;

  /**
   * Get the user id corresponding to Domain/Login (ignoring the login case)
   * @param sLogin the user login
   * @param sDomainId the domain of the user
   * @return the unique identifier of the user
   * @throws AdminException if an error occurs
   */
  String getUserIdByLoginAndDomain(String sLogin, String sDomainId) throws AdminException;

  /**
   * @param authenticationKey The authentication key.
   * @return The user id corresponding to the authentication key.
   * @throws AdminException if an error occurs
   */
  String getUserIdByAuthenticationKey(String authenticationKey) throws AdminException;

  /**
   * Get full information about the user with the given unique identifier (only info in cache table)
   * from its domain.
   * @param sUserId the unique identifier of the user to get.
   * @return a {@link UserFull} instance.
   * @throws AdminException if an error occurs while getting the user.
   */
  UserFull getUserFull(String sUserId) throws AdminException;

  /**
   * Gets full information about users corresponding to given unique identifiers (only info in cache
   * table) from its domain.
   * @param userIds the unique identifiers of user to get.
   * @return list of {@link UserFull} instance.
   * @throws AdminException if an error occurs while getting the user.
   */
  List<UserFull> getUserFulls(Collection<String> userIds) throws AdminException;

  UserFull getUserFull(String domainId, String specificId) throws AdminException;

  /**
   * Add the given user in Silverpeas and specific domain.
   * @param userDetail a user
   * @return the new user id.
   * @throws AdminException if an error occurs
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
   * @param userId the unique identifier of the user
   * @throws AdminException if an error occurs
   */
  void blockUser(String userId) throws AdminException;

  /**
   * Unblock the user represented by the given identifier.
   * @param userId the unique identifier of the user
   * @throws AdminException if an error occurs
   */
  void unblockUser(String userId) throws AdminException;

  /**
   * Deactivates the user represented by the given identifier.
   * @param userId the unique identifier of the user
   * @throws AdminException if an error occurs
   */
  void deactivateUser(String userId) throws AdminException;

  /**
   * Activate the user represented by the given identifier.
   * @param userId the unique identifier of the user
   * @throws AdminException if an error occurs
   */
  void activateUser(String userId) throws AdminException;

  /**
   * Sets some data of the specified user as or not sensitive information. The data to be
   * potentially sensitive are defined in the user domain to which the user belongs.
   * @param userId the unique identifier of the user.
   * @param sensitive a boolean indicating whether the data potentially sensitive are actually
   * sensitive or not.
   * @throws AdminException if the setting fails.
   */
  void setUserSensitiveData(String userId, boolean sensitive) throws AdminException;

  /**
   * Updates the acceptance date of a user from its id.
   * @param userId the unique identifier of the user
   * @throws AdminException if an error occurs
   */
  void userAcceptsTermsOfService(String userId) throws AdminException;

  /**
   * Restores the given user from silverpeas and specific domain
   * @param sUserId the user identifier
   * @return the user identifier
   * @throws AdminException if an error occurs
   */
  String restoreUser(String sUserId) throws AdminException;

  /**
   * Removes the given user from silverpeas and specific domain
   * @param sUserId the user identifier
   * @return the user identifier
   * @throws AdminException if an error occurs
   */
  String removeUser(String sUserId) throws AdminException;

  /**
   * Delete the given user from silverpeas and specific domain
   * @param sUserId the user identifier
   * @return the user identifier
   * @throws AdminException if an error occurs
   */
  String deleteUser(String sUserId) throws AdminException;

  /**
   * Delete the given user from silverpeas and specific domain
   * @param sUserId the user identifier
   * @param onlyInSilverpeas performs the operation only in Silverpeas
   * @return the user identifier
   * @throws AdminException if an error occurs
   */
  String deleteUser(String sUserId, boolean onlyInSilverpeas) throws AdminException;

  /**
   * Update the given user (ONLY IN SILVERPEAS)
   * @param user the user
   * @return the user identifier
   * @throws AdminException if an error occurs
   */
  String updateUser(UserDetail user) throws AdminException;

  /**
   * Update the given user in Silverpeas and specific domain
   * @param user the user
   * @return the user identifier
   * @throws AdminException if an error occurs
   */
  String updateUserFull(UserFull user) throws AdminException;

  /**
   * Converts driver space id to client space id
   * @param sDriverSpaceId the local space identifier
   * @return the global space identifier
   */
  String getClientSpaceId(String sDriverSpaceId);

  /**
   * Converts driver space ids to client space ids
   * @param asDriverSpaceIds one or more local space identifiers
   * @return the global identifier of the specified spaces.
   */
  String[] getClientSpaceIds(String[] asDriverSpaceIds);

  String getNextDomainId() throws AdminException;

  /**
   * Create a new domain
   * @param theDomain the user domain to create.
   * @return the unique identifier of the domain
   */
  String addDomain(Domain theDomain) throws AdminException;

  /**
   * Update a domain
   * @param domain the domain to update
   * @return the unique identifier of the updated domain.
   */
  String updateDomain(Domain domain) throws AdminException;

  /**
   * Remove a domain
   * @param domainId the unique identifier of a domain
   * @return the unique identifier of the deleted domain
   */
  String removeDomain(String domainId) throws AdminException;

  /**
   * Get all domains
   * @return an array with all the user domains in Silverpeas
   */
  Domain[] getAllDomains() throws AdminException;

  /**
   * Get a domain with given id
   * @param domainId the unique domain identifier
   * @return the user domain
   */
  Domain getDomain(String domainId) throws AdminException;

  long getDomainActions(String domainId) throws AdminException;

  /**
   * Gets all {@link GroupState#VALID} groups at the root of a domain.
   * @param domainId identifier of a domain.
   * @return an array of {@link GroupDetail} instance.
   * @throws AdminException on any technical error.
   */
  GroupDetail[] getRootGroupsOfDomain(String domainId) throws AdminException;

  List<GroupDetail> getSynchronizedGroups() throws AdminException;

  /**
   * Gets all users of a group, including these of {@link GroupState#VALID} subgroups.
   * @param groupId the identifier of the group from which users are retrieved.
   * @return an array of {@link UserDetail} instance.
   * @throws AdminException on any technical error.
   */
  UserDetail[] getAllUsersOfGroup(String groupId) throws AdminException;

  UserDetail[] getUsersOfDomain(String domainId) throws AdminException;

  /**
   * Get all users (except delete ones) from specified domains.
   * @param domainIds a list of domain identifiers
   * @return the user details from specified domains sort by alphabetical order
   * @throws AdminException if an error occurs
   */
  List<UserDetail> getUsersOfDomains(List<String> domainIds) throws AdminException;

  /**
   * Get all users (except delete ones) from specified domains.
   * @param domainIds a list of domain identifiers.
   * @return the user details from specified domains sort by reverse creation order
   * @throws AdminException if an error occurs
   */
  List<UserDetail> getUsersOfDomainsFromNewestToOldest(List<String> domainIds)
      throws AdminException;

  String[] getUserIdsOfDomain(String domainId) throws AdminException;

  String identify(String sKey, String sSessionId, boolean isAppInMaintenance) throws AdminException;

  String identify(String sKey, String sSessionId, boolean isAppInMaintenance, boolean removeKey)
      throws AdminException;

  // ---------------------------------------------------------------------------------------------
  // QUERY FUNCTIONS
  // ---------------------------------------------------------------------------------------------
  List<GroupDetail> getDirectGroupsOfUser(String userId) throws AdminException;

  /**
   * Get the spaces ids allowed for the given user
   * @param sUserId the unique identifier of a user
   * @return the unique identifier of the allowed spaces.
   * @throws AdminException if an error occurs
   */
  String[] getUserSpaceIds(String sUserId) throws AdminException;

  /**
   * Get the root spaces ids allowed for the given user
   * @param sUserId the unique identifier of a user
   * @return the unique identifier of the allowed root spaces.
   * @throws AdminException if an error occurs
   */
  String[] getUserRootSpaceIds(String sUserId) throws AdminException;

  /**
   * Get the subspaces ids in the specified space that are allowed for the given user
   * @param sUserId the unique identifier of a user
   * @param spaceId the unique identifier of the space parent.
   * @return the unique identifier of the allowed root spaces.
   * @throws AdminException if an error occurs
   */
  String[] getUserSubSpaceIds(String sUserId, String spaceId) throws AdminException;

  /**
   * This method permit knowing if the given space is allowed to the given user.
   * @param userId the unique identifier of a user
   * @param spaceId the unique identifier of a space
   * @return true if user is allowed to access to one component (at least) in given space, false
   * otherwise.
   * @throws AdminException if an error occurs
   */
  boolean isSpaceAvailable(String userId, String spaceId) throws AdminException;

  /**
   * This method allows callers to perform several space availability checks for a given user.
   * <p>
   * This is useful for treatments requiring highest performances than calling each time
   * {@link #isSpaceAvailable(String, String)} for example.
   * </p>
   * <p>
   * IMPORTANT: the {@link UserSpaceAvailabilityChecker} MUST not be an attribute of a singleton
   * instance.
   * </p>
   * @param userId the unique identifier of a user
   * @return a {@link UserSpaceAvailabilityChecker} instance initialized for the given user
   * identifier.
   * @throws AdminException if an error occurs
   */
  UserSpaceAvailabilityChecker getUserSpaceAvailabilityChecker(String userId) throws AdminException;

  List<SpaceInstLight> getSubSpaces(String spaceId) throws AdminException;

  /**
   * Get components of a given space (and subspaces) available to a user.
   * @param userId the unique identifier of a user
   * @param spaceId the unique identifier of a space
   * @return a list of ComponentInstLight
   * @throws AdminException if an error occurs
   * @author neysseri
   */
  List<ComponentInstLight> getAvailCompoInSpace(String userId, String spaceId)
      throws AdminException;

  /**
   * Get all spaces available to a user. N levels compliant. Infos of each space are in
   * SpaceInstLight object.
   * @param userId the unique identifier of a user
   * @return an ordered list of SpaceInstLight. Built according a depth-first algorithm.
   * @throws AdminException if an error occurs
   * @author neysseri
   */
  List<SpaceInstLight> getUserSpaceTreeview(String userId) throws AdminException;

  String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) throws AdminException;

  /**
   * Get the space instance light (only space id, father id and name) with the given space id
   * @param sClientSpaceId client space id (as WAxx)
   * @return Space information as SpaceInstLight object
   * @throws AdminException if an error occurs
   */
  SpaceInstLight getSpaceInstLightById(String sClientSpaceId) throws AdminException;

  /**
   * Return the higher space according to a subspace (N level compliant)
   * @param spaceId the subspace id
   * @return a SpaceInstLight object
   * @throws AdminException if an error occurs
   */
  SpaceInstLight getRootSpace(String spaceId) throws AdminException;

  /**
   * Get all the spaces ids manageable by given group id.
   * <p>
   *   It means the direct space ids the group is indicated to and all the sub spaces ids by
   *   inheritance.
   * </p>
   * @param sGroupId the unique identifier of a group
   * @return an array of space identifiers.
   * @throws AdminException if an error occurs
   */
  String[] getGroupManageableSpaceIds(String sGroupId) throws AdminException;

  /**
   * Get all the spaces ids manageable by given user id.
   * <p>
   *   It means the direct space ids the user is indicated to and all the sub spaces ids by
   *   inheritance.
   * </p>
   * @param sUserId the unique identifier of a user
   * @return an array of space identifiers
   * @throws AdminException if an error occurs
   */
  String[] getUserManageableSpaceIds(String sUserId) throws AdminException;

  /**
   * Get the subspace manageable by given user id in given space
   * @param sUserId the unique identifier of a user
   * @param sParentSpaceId the identifier of the parent space
   * @return an array of space identifiers
   * @throws AdminException if an error occurs
   */
  String[] getUserManageableSubSpaceIds(String sUserId, String sParentSpaceId)
      throws AdminException;

  /**
   * Gets the space profile instance which provides all user and group identifiers through simple
   * methods.
   * @param spaceId the identifier of aimed space.
   * @param role the aimed technical role name.
   * @return the {@link SpaceProfile} instance.
   * @throws AdminException on a technical error.
   */
  SpaceProfile getSpaceProfile(String spaceId, SilverpeasRole role) throws AdminException;

  List<String> getUserManageableGroupIds(String sUserId) throws AdminException;

  /**
   * Get the component ids allowed for the given user id in the given space
   * @param sClientSpaceId the global space identifier
   * @param sUserId the unique identifier of a user
   * @return an array of component instance identifiers
   * @throws AdminException if an error occurs
   */
  String[] getAvailCompoIds(String sClientSpaceId, String sUserId) throws AdminException;

  /**
   * Is the specified tool belongs to the administration component?
   * </p>
   * The administration component (or administrative console) forms a particular component made up
   * of several tools, each of them providing an administrative feature. Each tool in the
   * administration component have the same identifier that refers in fact the administration
   * console.
   * @param toolId the unique identifier of the tool.
   * @return true if the tool belongs to the administration component.
   */
  boolean isAnAdminTool(String toolId);

  /**
   * Gets the component instance identifiers available for the specified user?
   * </p>
   * A component is an application in Silverpeas to perform some tasks and to manage some resources.
   * Each component in Silverpeas can be instantiated several times, each of them corresponding then
   * to a running application in Silverpeas, and it is uniquely identified from others instances by
   * a given identifier.
   * @param userId the unique identifier of a user.
   * @return a list of component instance identifier as string.
   * @throws AdminException if an error occurs
   */
  List<String> getAvailableComponentsByUser(String userId) throws AdminException;

  /**
   * Is the specified component instance available among the components instances accessible by the
   * specified user?
   * </p>
   * A component is an application in Silverpeas to perform some tasks and to manage some resources.
   * Each component in Silverpeas can be instantiated several times, each of them corresponding then
   * to a running application in Silverpeas, and it is uniquely identified from others instances by
   * a given identifier.
   * @param componentId the unique identifier of a component instance.
   * @param userId the unique identifier of a user.
   * @return true if the component instance is available, false otherwise.
   * @throws AdminException if an error occurs
   */
  boolean isComponentAvailableToUser(String componentId, String userId) throws AdminException;

  /**
   * Is the specified component instance available among the components instances accessible by the
   * specified group of users?
   * </p>
   * A component is an application in Silverpeas to perform some tasks and to manage some resources.
   * Each component in Silverpeas can be instantiated several times, each of them corresponding then
   * to a running application in Silverpeas, and it is uniquely identified from others instances by
   * a given identifier.
   * @param componentId the unique identifier of a component instance.
   * @param groupId the unique identifier of a group of users.
   * @return true if the component instance is available, false otherwise.
   * @throws AdminException if an error occurs
   */
  boolean isComponentAvailableToGroup(String componentId, String groupId) throws AdminException;

  /**
   * Is the specified component instance manageable by the given user? The component instance is
   * manageable if the user has enough access right to manage it.
   * @param componentId the unique identifier of the component instance.
   * @param userId the unique identifier of a user.
   * @return true of the user can manage the specified component instance. False otherwise.
   * @throws AdminException if an error occurs while checking the access right of the user.
   */
  boolean isComponentManageable(String componentId, String userId) throws AdminException;

  /**
   * Get ids of components allowed to user in given space (not in subspaces)
   * @param sClientSpaceId the identifier of a space
   * @param sUserId the unique identifier of a user
   * @return an array of componentId (kmelia12, hyperlink145...)
   * @throws AdminException if an error occurs
   */
  String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId) throws AdminException;

  /**
   * Get the componentIds allowed for the given user Id in the given space and the
   * componentNameRoot
   * @param sClientSpaceId the unique identifier of a space
   * @param sUserId the unique identifier of a user
   * @param componentNameRoot the root name of the component
   * @return a list of component ids
   * @throws AdminException if an error occurs
   * @author dlesimple
   */
  List<String> getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId,
      String componentNameRoot) throws AdminException;

  /**
   * Get the component ids allowed for the given user id.
   * @param userId the unique identifier of a user
   * @throws AdminException if an error occurs
   */
  String[] getAvailCompoIds(String userId) throws AdminException;

  String[] getComponentIdsByNameAndUserId(String sUserId, String sComponentName)
      throws AdminException;

  /**
   * Gets the available component for a given user
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   * @throws AdminException if an error occurs
   */
  List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName)
      throws AdminException;

  /**
   * This method returns all root spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
   * @param userId the unique identifier of a user
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException if an error occurs
   */
  List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName)
      throws AdminException;

  /**
   * This method returns all sub spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
   * @param userId the unique identifier of a user
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException if an error occurs
   */
  List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName) throws AdminException;

  /**
   * Get the tuples (space id, compo id) allowed for the given user and given component name
   * @param sUserId the user identifier
   * @param sComponentName the name of a component
   * @return an array of tuples (space id, component instance id)
   * @throws AdminException if an error occurs
   */
  CompoSpace[] getCompoForUser(String sUserId, String sComponentName) throws AdminException;

  /**
   * Return the compo id for the given component name
   * @param sComponentName a name of a component
   * @return an array with the identifier of the instances of the specified component
   * @throws AdminException if an error occurs
   */
  String[] getCompoId(String sComponentName) throws AdminException;

  /**
   * Get all the profile ids for the given user
   * @param sUserId the unique identifier of a user
   * @return an array with the identifier of all profiles of the user
   * @throws AdminException if an error occurs
   */
  String[] getProfileIds(String sUserId) throws AdminException;

  /**
   * Get all the profile ids for the given group
   * @param sGroupId the unique identifier of a group
   * @return an array with the identifier of all profiles of the group
   * @throws AdminException if an error occurs
   */
  String[] getProfileIdsOfGroup(String sGroupId) throws AdminException;

  /**
   * Get the profile names of the given user for the given component instance
   * @param sUserId a unique identifier of a user
   * @param componentInst a component instance
   * @return an array of all the name of the profiles in which the given user is for the given
   * component instance
   */
  String[] getCurrentProfiles(String sUserId, ComponentInst componentInst);

  /**
   * Get the profile names of the given user for the given component instance
   * @param sUserId a unique identifier of a user
   * @param componentId the unique identifier of a component instance
   * @return an array of all the name of the profiles in which the given user is for the given
   * component instance
   * @throws AdminException if an error occurs
   */
  String[] getCurrentProfiles(String sUserId, String componentId) throws AdminException;

  /**
   * Gets the profile names of given user indexed by the given component instances.
   * @param userId a user identifier as string.
   * @param componentIds list of component instance identifier as string.
   * @return a map filled with list of profile name as string by component instance identifier as
   * string.
   * @throws AdminException if an error occurs
   */
  Map<String, Set<String>> getUserProfilesByComponentId(String userId,
      Collection<String> componentIds) throws AdminException;

  /**
   * Get the profile names of the given user for the given space.
   * @param userId a unique identifier of a user.
   * @param spaceId the unique identifier of a space.
   * @return an list of all the name of the profiles in which the given user is for the given space.
   * @throws AdminException if an error occurs
   */
  List<String> getSpaceUserProfilesBySpaceId(String userId, String spaceId) throws AdminException;

  /**
   * Gets the space profile names of given user indexed by the given spaces.
   * @param userId a user identifier as string.
   * @param spaceIds list of space identifier as string.
   * @return a map filled with list of profile name as string by space identifier as string.
   * @throws AdminException if an error occurs
   */
  Map<String, Set<String>> getSpaceUserProfilesBySpaceIds(String userId,
      Collection<String> spaceIds) throws AdminException;

  /**
   * Gets all the users in the given profile for the given component instance in the given space.
   * @param bAllProfiles if bAllProfiles = true, return all the user details for the given space and
   * given component if bAllProfiles = false, return the user details only for the given profile for
   * the given space and given component
   * @param sProfile the identifier of a profile
   * @param sClientComponentId the unique identifier of a component instance.
   * @throws AdminException if an error occurs
   */
  UserDetail[] getUsers(boolean bAllProfiles, String sProfile, String sClientComponentId) throws AdminException;

  /**
   * For use in userPanel : return the direct {@link GroupState#VALID} subgroups.
   * @param parentGroupId the unique identifier of the parent group
   * @return an array with all the groups children of the specified group
   * @throws AdminException if an error occurs
   */
  GroupDetail[] getAllSubGroups(String parentGroupId) throws AdminException;

  /**
   * For use in userPanel: return recursively the direct {@link GroupState#VALID} subgroups of the
   * specified group.
   * @param parentGroupId the unique identifier of the parent group
   * @return an array with all the groups children of the specified group
   * @throws AdminException if an error occurs
   */
  GroupDetail[] getRecursivelyAllSubGroups(String parentGroupId) throws AdminException;

  /**
   * For use in userPanel: return the users that are direct child of a given group
   * @param sGroupId the unique identifier of the group
   * @param sUserLastNameFilter filter on the user last name.
   * @return an array with all the users in the given group
   * @throws AdminException if an error occurs
   */
  UserDetail[] getFilteredDirectUsers(String sGroupId, String sUserLastNameFilter)
      throws AdminException;

  /**
   * For use in userPanel: return the total number of users recursively contained in a group
   * @param sGroupId the unique identifier of a group
   * @return the number of users in the given group and its subgroups.
   * @throws AdminException if an error occurs
   */
  int getAllSubUsersNumber(String sGroupId) throws AdminException;

  /**
   * Get the identifiers of the administrators accessible by the given user.
   * @param fromUserId the identifier of a user
   * @return an array with the identifier of all the administrators that can be contacted by the
   * given user
   * @throws AdminException if an error occurs
   */
  String[] getAdministratorUserIds(String fromUserId) throws AdminException;

  /**
   * Gets the email of Silverpeas. This email is dedicated to be used when Silverpeas sends emails
   * to users.
   * @return a non-reply address email with which Silverpeas sent emails to users.
   */
  String getSilverpeasEmail();

  /**
   * Gets the name of Silverpeas to use when it sends notifications to users.
   * @return the name to use when the system (Silverpeas) sends notifications to users.
   */
  String getSilverpeasName();

  // -------------------------------------------------------------------
  // RE-INDEXATION
  // -------------------------------------------------------------------
  String[] getAllSpaceIds(String sUserId) throws AdminException;

  String[] getAllRootSpaceIds(String sUserId) throws AdminException;

  String[] getAllSubSpaceIds(String sSpaceId, String sUserId) throws AdminException;

  /**
   * Returns all the component identifiers of the space represented by the given identifier.
   * <p>Component instance of sub spaces are not retrieved.</p>
   * <p>It returns also ids of {@link SilverpeasPersonalComponentInstance} instances.</p>
   * @param sSpaceId the unique identifier of a space
   * @return the identifiers of the component instances in the given space.
   * @throws AdminException if an error occurs
   */
  String[] getAllComponentIds(String sSpaceId) throws AdminException;

  /**
   * Returns all the component identifiers of the space, and its sub spaces, represented by the
   * given identifier.
   * <p>It returns also ids of {@link SilverpeasPersonalComponentInstance} instances.</p>
   * @param sSpaceId the unique identifier of a space
   * @return an array with the identifier of all the component instances in the given space and its
   * subspaces.
   * @throws AdminException if an error occurs
   */
  String[] getAllComponentIdsRecur(String sSpaceId) throws AdminException;

  void synchronizeGroupByRule(String groupId, boolean scheduledMode) throws AdminException;

  String synchronizeGroup(String groupId, boolean recurs) throws AdminException;

  String synchronizeImportGroup(String domainId, String groupKey, String askedParentId,
      boolean recurs, boolean isIdKey) throws AdminException;

  String synchronizeRemoveGroup(String groupId) throws AdminException;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   * @param userId the identifier of the user performing the synchronization
   * @param recurs the synchronization should be recursive?
   * @return the identifier of the user invoking this method
   * @throws AdminException if an error occurs
   */
  String synchronizeUser(String userId, boolean recurs) throws AdminException;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   * @param domainId the identifier of the domain concerned by the import
   * @param userLogin the login of a user in the given domain
   * @param recurs the synchronization should be recursive?
   * @return the identifier of the user invoking this method
   * @throws AdminException if an error occurs
   */
  String synchronizeImportUserByLogin(String domainId, String userLogin, boolean recurs)
      throws AdminException;

  /**
   * Synchronize Users and groups between cache and domain's datastore
   * @param domainId the identifier of the domain concerned by the import
   * @param specificId the identifier of the user specific to the given domain
   * @param recurs the synchronization should be recursive?
   * @return the identifier of the user invoking this method
   * @throws AdminException if an error occurs
   */
  String synchronizeImportUser(String domainId, String specificId, boolean recurs)
      throws AdminException;

  List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId, String language)
      throws AdminException;

  UserDetail[] searchUsers(String domainId, Map<String, String> query) throws AdminException;

  String synchronizeRemoveUser(String userId) throws AdminException;

  String synchronizeSilverpeasWithDomain(String sDomainId) throws AdminException;

  String synchronizeSilverpeasWithDomain(String sDomainId, boolean threaded) throws AdminException;

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------

  List<String> searchUserIdsByProfile(final List<String> profileIds,
      final boolean includeRemovedUsersAndGroups) throws AdminException;

  SilverpeasList<UserDetail> searchUsers(UserDetailsSearchCriteria searchCriteria)
      throws AdminException;

  SilverpeasList<GroupDetail> searchGroups(GroupsSearchCriteria searchCriteria)
      throws AdminException;

  // -------------------------------------------------------------------------
  // Node profile management
  // -------------------------------------------------------------------------
  void indexAllUsers() throws AdminException;

  void indexUsers(String domainId) throws AdminException;

  String copyAndPasteComponent(PasteDetail pasteDetail) throws AdminException, QuotaException;

  String copyAndPasteSpace(PasteDetail pasteDetail) throws AdminException, QuotaException;

  /**
   * Assign rights of a user to a user
   * @param operationMode value of {@link RightAssignationContext.MODE}
   * @param sourceUserId the user id of the source user
   * @param targetUserId the user id of the target user
   * @param nodeAssignRights true if you want also to apply the operation to the rights on nodes
   * @param authorId the userId of the author of this action
   * @throws AdminException if an error occurs
   */
  void assignRightsFromUserToUser(RightAssignationContext.MODE operationMode, String sourceUserId,
      String targetUserId, boolean nodeAssignRights, String authorId) throws AdminException;

  /**
   * Assign rights of a user to a group
   * @param operationMode value of {@link RightAssignationContext.MODE}
   * @param sourceUserId the user id of the source user
   * @param targetGroupId the group id of the target group
   * @param nodeAssignRights true if you want also to apply the operation to the rights on nodes
   * @param authorId the userId of the author of this action
   * @throws AdminException if an error occurs
   */
  void assignRightsFromUserToGroup(RightAssignationContext.MODE operationMode, String sourceUserId,
      String targetGroupId, boolean nodeAssignRights, String authorId) throws AdminException;

  /**
   * Assign rights of a group to a user
   * @param operationMode value of {@link RightAssignationContext.MODE}
   * @param sourceGroupId the group id of the source group
   * @param targetUserId the user id of the target user
   * @param nodeAssignRights true if you want also to apply the operation to the rights on nodes
   * @param authorId the userId of the author of this action
   * @throws AdminException if an error occurs
   */
  void assignRightsFromGroupToUser(RightAssignationContext.MODE operationMode, String sourceGroupId,
      String targetUserId, boolean nodeAssignRights, String authorId) throws AdminException;

  /**
   * Assign rights of a group to a group
   * @param operationMode value of {@link RightAssignationContext.MODE}
   * @param sourceGroupId the group id of the source group
   * @param targetGroupId the group id of the target group
   * @param nodeAssignRights true if you want also to apply the operation to the rights on nodes
   * @param authorId the userId of the author of this action
   * @throws AdminException if an error occurs
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

  SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException;

  SpaceWithSubSpacesAndComponents getAllowedFullTreeview(String userId) throws AdminException;

  SpaceWithSubSpacesAndComponents getAllowedFullTreeviewOnComponentName(String userId,
      String componentName) throws AdminException;

  SpaceWithSubSpacesAndComponents getAllowedFullTreeview(String userId, String spaceId)
      throws AdminException;

  /**
   * Gets all the groups that were removed in the specified domains. If no domains are specified,
   * then all the domains are taken into account.
   * @param domainIds the unique identifiers of the domains.
   * @return a list of groups or an empty list if there is no removed groups in the specified
   * domains.
   * @throws AdminException if an error occurs
   */
  List<GroupDetail> getRemovedGroups(String... domainIds) throws AdminException;

  /**
   * Gets all the users that were removed in the specified domains. If no domains are specified,
   * then all the domains are taken into account.
   * @param domainIds the unique identifiers of the domains.
   * @return a list of users or an empty list if there is no removed users in the specified domains.
   * @throws AdminException if an error occurs
   */
  List<UserDetail> getRemovedUsers(String... domainIds) throws AdminException;

  /**
   * Gets all the users that were deleted in the specified domains and that weren't blanked. If no
   * domains are specified, then all the domains are taken into account.
   * @param domainIds the unique identifiers of the domains.
   * @return a list of users or an empty list if there is no deleted users in the specified domains.
   * @throws AdminException if an error occurs
   */
  List<UserDetail> getNonBlankedDeletedUsers(String... domainIds) throws AdminException;

  /**
   * Gets all the users in the specified domains having sensitive data.
   * @param domainIds the unique identifiers of the domains.
   * @return a list of users or an empty list if there is no users with sensitive data in the
   * specified domains.
   * @throws AdminException if an error occurs.
   */
  List<UserDetail> getUsersWithSensitiveData(final String... domainIds) throws AdminException;

  /**
   * Blanks the specified users in the specified domain. The users have to be deleted in Silverpeas,
   * otherwise an {@link AdminException} exception is thrown.
   * @param targetDomainId the unique identifier of the domain.
   * @param userIds a list of unique identifiers of deleted users in the specified domain.
   * @throws AdminException if an error occurs while blanking the deleted users.
   */
  void blankDeletedUsers(String targetDomainId, List<String> userIds) throws AdminException;

  /**
   * Disables the privacy of the data marked as potentially sensitive for the specified users in
   * the given domain. The data that can be sensitive are defined in the user domain of the users.
   * This is why users from different domains can have different sensitive data. By default,
   * those data aren't sensitive, but their privacy can be enabled and disabled per user.
   * @param domainId the unique identifier of a user domain.
   * @param userIds a list of unique identifiers of users in the given domain for which the
   * sensitivity of their data should be disabled.
   * @throws AdminException if an error occurs while disabling the sensitivity of the data for
   * the users in the given user domain.
   */
  void disableDataSensitivity(final String domainId,
      final List<String> userIds) throws AdminException;
}
