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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationController extends java.io.Serializable {

  static OrganizationController get() {
    return ServiceProvider.getService(OrganizationController.class);
  }

  /**
   * Return all the spaces Id available in silverpeas
   *
   * @return
   */
  String[] getAllSpaceIds();

  /**
   * Return all the subSpaces Id available in silverpeas given a space id (driver format)
   *
   * @param sSpaceId
   * @return
   */
  String[] getAllSubSpaceIds(String sSpaceId);

  /**
   * Return the the spaces name corresponding to the given space ids.
   *
   * @param asSpaceIds
   * @return
   */
  String[] getSpaceNames(String[] asSpaceIds);

  /**
   * Return the space light corresponding to the given space id
   *
   * @param spaceId
   * @return
   */
  SpaceInstLight getSpaceInstLightById(String spaceId);

  /**
   * Return the space Instance corresponding to the given space id
   */
  SpaceInst getSpaceInstById(String sSpaceId);

  /**
   * Return the component ids available for the current user Id in the given space id
   */
  String[] getAvailCompoIds(String sClientSpaceId, String sUserId);

  /**
   * Return the component ids available for the current user Id
   */
  String[] getAvailCompoIds(String sUserId);

  /**
   * Return the component ids available for the cuurent user Id in the given space id
   */
  String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId);

  /**
   * Return the tuples (space id, compo id) allowed for the given user and given component name
   *
   * @param sUserId
   * @param sCompoName
   * @return
   */
  CompoSpace[] getCompoForUser(String sUserId, String sCompoName);

  /**
   * gets the available component for a given user
   *
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   */
  List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName);

  String[] getComponentIdsForUser(String sUserId, String sCompoName);

  /**
   * Return the compo id for the given component name
   *
   * @param sCompoName
   * @return
   */
  String[] getCompoId(String sCompoName);

  String getComponentParameterValue(String sComponentId, String parameterName);

  List<ComponentInstLight> getComponentsWithParameterValue(String param, String value);

  /**
   * Gets the component instance related to the given identifier.<br>
   * In contrary to {@link #getComponentInst(String)}, {@link #getComponentInstLight(String)}
   * signatures, this one is able to return different kinds of implementation of {@link
   * SilverpeasComponentInstance}.<br>
   * So, this signature is useful into contexts of transverse treatments.
   * @param componentInstanceIdentifier the identifier of the requested component instance.
   * @return an optional component instance.
   */
  Optional<SilverpeasComponentInstance> getComponentInstance(String componentInstanceIdentifier);

  /**
   * Return the component Instance corresponding to the given component id
   */
  ComponentInst getComponentInst(String sComponentId);

  /**
   * Return the component Instance Light corresponding to the given component id
   */
  ComponentInstLight getComponentInstLight(String sComponentId);

  /**
   * Return the full detail of the user with the given ldap Id
   */
  UserFull getUserFull(String sUserId);

  /**
   * Return the detail of the user with the given ldap Id
   */
  <T extends User> T getUserDetail(String sUserId);

  /**
   * Return an array of users corresponding to the given user Id array
   */
  <T extends User> T[] getUserDetails(String[] asUserIds);

  /**
   * @deprecated use getAllUsers(String componentId) Return all the users allowed to access the
   * given component of the given space
   */
  @Deprecated
  <T extends User> T[] getAllUsers(String sPrefixTableName, String sComponentName);

  /**
   * Return all the users allowed to acces the given component
   */
  <T extends User> T[] getAllUsers(String componentId);

  /**
   * Gets all the users that belong to the specified domain.
   *
   * @param domainId the unique identifier of the domain.
   * @return an array of users objects or null if no such domain exists.
   */
  <T extends User> T[] getAllUsersInDomain(String domainId);

  /**
   * Searches the users that match the specified criteria.
   *
   * @param criteria the criteria in searching of user details.
   * @return a slice of the list of user details matching the criteria or an empty list of no ones
   * are found.
   * @throws AdminException if an error occurs while getting the
   * user details.
   */
  <T extends User> ListSlice<T> searchUsers(UserDetailsSearchCriteria criteria);

  /**
   * Gets all the user groups that belong to the specified domain.
   *
   * @param domainId the unique identifier of the domain.
   * @return an array of Group objects or null if no such domain exists.
   */
  <T extends Group> T[] getAllRootGroupsInDomain(String domainId);

  /**
   * For use in userPanel : return the users that are direct child of a given group
   */
  <T extends User> T[] getFilteredDirectUsers(String sGroupId, String sUserLastNameFilter);

  /**
   * Searches the groups that match the specified criteria.
   *
   * @param criteria the criteria in searching of user groups.
   * @return a slice of the list of user groups matching the criteria or an empty list of no ones
   * are found.
   * @throws AdminException if an error occurs while getting the
   * user groups.
   */
  <T extends Group> SilverpeasList<T> searchGroups(GroupsSearchCriteria criteria);

  /**
   * Returns the total number of distinct users recursively contained in the specified group
   */
  int getAllSubUsersNumber(String sGroupId);

  /**
   * For use in userPanel : return the direct sub-groups
   */
  <T extends Group> T[] getAllSubGroups(String parentGroupId);

  /**
   * Gets all the groups and sub groups that are children of the specified group.
   * @param parentGroupId the unique identifier of a group.
   * @param <T> the concrete type of the {@link Group} instances to return.
   * @return an array with all the groups that are children of the specified group.
   */
  <T extends Group> T[] getRecursivelyAllSubgroups(String parentGroupId);

  /**
   * Return all the users of Silverpeas
   */
  <T extends User> T[] getAllUsers();

  /**
   * Return all the users with the given profile allowed to access the given component of the given
   * space
   */
  <T extends User> T[] getUsers(String sPrefixTableName, String sComponentName, String sProfile);

  /**
   * Gets the collection of silverpeas roles the given user has on the component instance
   * represented by the given identifier.<br>
   * In contrary to {@link #getUserProfiles(String, String)},
   * {@link #getUserProfiles(String, ProfiledObjectId)} or
   * {@link #getUserProfiles(String, String, ProfiledObjectId)}
   * signatures, this one is able to return user roles of different kinds of implementation of
   * {@link SilverpeasComponentInstance}.<br>
   * So, this signature is useful into contexts of transversal treatments.<br>
   * BE CAREFUL, the manager role is never returned as it corresponds to a space role.
   * @param componentInstanceIdentifier the identifier of the component instance.
   * @return an optional component instance.
   */
  Collection<SilverpeasRole> getUserSilverpeasRolesOn(User user,
      String componentInstanceIdentifier);

  String[] getUserProfiles(String userId, String componentId);

  String[] getUserProfiles(String userId, String componentId, ProfiledObjectId objectId);

  Map<Integer, List<String>> getUserObjectProfiles(String userId, String componentId,
      ProfiledObjectType objectType);

  List<ProfileInst> getUserProfiles(String componentId, ProfiledObjectId objectId);

  ProfileInst getUserProfile(String profileId);

  /**
   * Return all administrators ids
   */
  String[] getAdministratorUserIds(String fromUserId);

  /**
   * Return the Group of the group with the given Id
   */
  <T extends Group> T getGroup(String sGroupId);

  /**
   * Return all groups specified by the groupsIds
   */
  <T extends Group> T[] getGroups(String[] groupsId);

  /**
   * Return all the groups of silverpeas
   */
  <T extends Group> T[] getAllGroups();

  /**
   * Return all root groups of silverpeas or null if an error occured when getting the root groups.
   */
  <T extends Group> T[] getAllRootGroups();

  /**
   * Get ALL the users that are in a group or his sub groups
   */
  <T extends User> T[] getAllUsersOfGroup(String groupId);

  /**
   * Get path to Group
   */
  List<String> getPathToGroup(String groupId);

  // -------------------------------------------------------------------
  // RE-INDEXATION
  // -------------------------------------------------------------------
  String[] getAllSpaceIds(String sUserId);

  /**
   * Return all the spaces Id manageable by given user in Silverpeas
   */
  String[] getUserManageableSpaceIds(String sUserId);

  /**
   * Return all the root spaceIds
   */
  String[] getAllRootSpaceIds();

  /**
   * Return all the root spaceIds available for the user sUserId
   */
  String[] getAllRootSpaceIds(String sUserId);

  /**
   * Return all the subSpaces Id available in webactiv given a space id (driver format)
   */
  String[] getAllSubSpaceIds(String sSpaceId, String sUserId);

  /**
   * Returns all the component identifiers of the space represented by the given identifier.
   * <p>Component instance of sub spaces are not retrieved.</p>
   * <p>It returns also ids of {@link SilverpeasPersonalComponentInstance} instances.</p>
   */
  String[] getAllComponentIds(String sSpaceId);

  /**
   * Returns all the component identifiers of the space, and its sub spaces, represented by the
   * given identifier.
   * <p>It returns also ids of {@link SilverpeasPersonalComponentInstance} instances.</p>
   */
  String[] getAllComponentIdsRecur(String sSpaceId);

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces or all
   * silverpeas) available in silverpeas given a userId and a componentNameRoot
   *
   * @author dlesimple
   * @deprecated please use {@link #getAllComponentIds(String)} or
   * {@link #getAllComponentIdsRecur(String)}
   * @param sSpaceId
   * @param sUserId
   * @param sComponentRootName
   * @param inCurrentSpace
   * @param inAllSpaces
   * @return Array of componentsIds
   */
  @Deprecated
  String[] getAllComponentIdsRecur(String sSpaceId, String sUserId, String sComponentRootName,
      boolean inCurrentSpace, boolean inAllSpaces);

  List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName);

  List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName);

  boolean isToolAvailable(String toolId);

  /**
   * Is the specified component instance available to the given user?
   * @param componentId the unique identifier of a component instance.
   * @param userId the unique identifier of a user.
   * @return true if the user can access the given component instance. False otherwise.
   * @deprecated Replaced by {@link OrganizationController#isComponentAvailableToUser(String, String)}
   */
  @Deprecated
  boolean isComponentAvailable(String componentId, String userId);

  boolean isComponentAvailableToUser(String componentId, String userId);

  boolean isComponentAvailableToGroup(String componentId, String groupId);

  boolean isComponentExist(String componentId);

  boolean isComponentManageable(String componentId, String userId);

  boolean isSpaceAvailable(String spaceId, String userId);

  /**
   * Is the specified profiled object in the given component instance available to the specified
   * user?
   * @param objectId A reference to the access right profiled object.
   * @param componentId the unique identifier of the component instance in which is defined the
   * above object.
   * @param userId the unique identifier of the user.
   * @return true if the user can access the object defined in the given component instance. False
   * otherwise.
   * @deprecated Use instead {@link OrganizationController#isObjectAvailableToUser(ProfiledObjectId, String, String)}
   */
  @Deprecated
  boolean isObjectAvailable(ProfiledObjectId objectId, String componentId, String userId);

  boolean isObjectAvailableToUser(ProfiledObjectId objectId, String componentId, String userId);

  boolean isObjectAvailableToGroup(ProfiledObjectId objectId, String componentId, String groupId);

  List<SpaceInstLight> getSpaceTreeview(String userId);

  String[] getAllowedSubSpaceIds(String userId, String spaceFatherId);

  SpaceInstLight getRootSpace(String spaceId);

  /**
   * Return all the users of Silverpeas
   */
  String[] getAllUsersIds();

  /**
   * Return userIds according to a list of profile names
   *
   * @param componentId the instance id
   * @param profileNames the list which contains the profile names
   * @return a string array of user id
   */
  String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames);

  String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId, List<String> profileNames);

  /**
   * Get a domain with given id
   *
   * @param domainId
   * @return
   */
  Domain getDomain(String domainId);

  /**
   * Get all domains
   *
   * @return
   */
  Domain[] getAllDomains();

  List<GroupDetail> getDirectGroupsOfUser(String userId);

  String[] getAllGroupIdsOfUser(String userId);

  /**
   * Is the anonymous access is activated for the running Silverpeas? When the anonymous access is
   * activated, then a specific user for anonymous access should be set; all anonym accesses to the
   * running Silverpeas are done with this user profile.
   *
   * @return true if the anonym access is activated, false otherwise.
   */
  boolean isAnonymousAccessActivated();

  String[] getAllowedComponentIds(String userId);

  <T extends User> List<T> getAllUsersFromNewestToOldest();

  <T extends User> List<T> getUsersOfDomainsFromNewestToOldest(List<String> domainIds);

  <T extends User> List<T> getUsersOfDomains(List<String> domainIds);

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
  boolean isAdminTool(String toolId);

  /**
   * Gets the identifier of the component instances that are both searchable and that satisfy the
   * specified criteria.
   *
   * @param criteria the criteria on the component instances to get.
   * @return a list of component instance identifiers.
   */
  List<String> getSearchableComponentsByCriteria(ComponentSearchCriteria criteria);

  /**
   * Gets the space profile instance which provides all user and group identifiers through simple
   * methods.
   * @param spaceId the identifier of aimed space.
   * @param role the aimed technical role name.
   * @return the {@link SpaceProfile} instance.
   * @throws AdminException on a technical error.
   */
  SpaceProfile getSpaceProfile(String spaceId, SilverpeasRole role);

  SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException;

  SpaceWithSubSpacesAndComponents getFullTreeview(String userId) throws AdminException;

  SpaceWithSubSpacesAndComponents getFullTreeview(String userId, String spaceId)
      throws AdminException;

  List<SpaceInstLight> getPathToSpace(String spaceId);

  List<SpaceInstLight> getPathToComponent(String componentId);
}