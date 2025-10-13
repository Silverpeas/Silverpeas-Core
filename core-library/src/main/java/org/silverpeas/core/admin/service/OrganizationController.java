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
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;

import java.util.*;

/**
 * The organization controller is the gateway for accessing the organizational resources of
 * Silverpeas: spaces, component instances, users, groups, roles, and so one.
 * It performs additional controls and treatments on these resources according to some
 * authorization and privacy rules before returning them. This is why their access have to be
 * centralized to this controller.
 * For organizational resources management or for technical transverse services (like the user
 * notification service for example), prefer to use directly the {@link Administration} service.
 */
public interface OrganizationController {

  /**
   * Gets an object satisfying the {@link OrganizationController} interface.
   * @return an {@link OrganizationController} object.
   */
  static OrganizationController get() {
    return ServiceProvider.getService(OrganizationController.class);
  }

  /**
   * Return all the spaces available in silverpeas
   * @return an array with the identifiers of the spaces
   */
  String[] getAllSpaceIds();

  /**
   * Return all the subSpaces available in silverpeas given a space id (driver format)
   * @param sSpaceId the identifier of the parent space
   * @return an array with the identifiers of the subspaces.
   */
  String[] getAllSubSpaceIds(String sSpaceId);

  /**
   * Return the spaces name corresponding to the given space ids.
   * @param asSpaceIds one or more space identifiers.
   * @return the names of the specified spaces
   */
  @SuppressWarnings("unused")
  String[] getSpaceNames(String[] asSpaceIds);

  /**
   * Return the space light corresponding to the given space id
   * @param spaceId the unique identifier of a space.
   * @return the space
   */
  SpaceInstLight getSpaceInstLightById(String spaceId);

  /**
   * Return the space instance corresponding to the given space id
   */
  SpaceInst getSpaceInstById(String sSpaceId);

  /**
   * Return the component ids available for the current user id in the given space id
   */
  String[] getAvailCompoIds(String sClientSpaceId, String sUserId);

  /**
   * Return the component ids available for the current user id
   */
  String[] getAvailCompoIds(String sUserId);

  /**
   * Return the component ids available for the current user id in the given space id
   */
  String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId);

  /**
   * Return the tuples (space id, compo id) allowed for the given user and given component name
   * @param sUserId the identifier of a user
   * @param sCompoName the name of a component
   * @return an array of tuples (space id, component instance id) of resources available by the user
   */
  CompoSpace[] getCompoForUser(String sUserId, String sCompoName);

  /**
   * Gets the available component for a given user
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   */
  List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName);

  /**
   * Gets the component instances accessible by the specified user and spawn from the specified
   * application in Silverpeas.
   * @param sUserId the unique identifier of a user in Silverpeas.
   * @param sCompoName the name of the application for which the instances have to be got.
   * @return an array with the identifier of component instances.
   */
  String[] getComponentIdsForUser(String sUserId, String sCompoName);

  /**
   * Return the compo id for the given component name
   * @param sCompoName the name of a component
   * @return an array of identifiers of instances of the given component
   */
  String[] getCompoId(String sCompoName);

  /**
   * Gets the value of the specified parameter of the given application instance.
   * @param sComponentId the unique identifier of a component instance.
   * @param parameterName the name of an application parameter.
   * @return a textual representation of the value of the parameter or an empty string if no such
   * parameter is defined for the given component instance.
   */
  String getComponentParameterValue(String sComponentId, String parameterName);

  /**
   * Gets all the component instance having the specified value for the given application
   * parameter.
   * @param param the name of an application parameter.
   * @param value the value of the application parameter.
   * @return a list of component instances.
   */
  List<ComponentInstLight> getComponentsWithParameterValue(String param, String value);

  /**
   * Gets all parameters values by component and by parameter name.
   * @param componentIds list of component identifier.
   * @param paramNames optional list of parameter name. All parameters are retrieved if it is not
   * filled or null
   * @return a map filled with couples of parameter name / value per component instance identifier.
   */
  Map<String, Map<String, String>> getParameterValuesByComponentIdThenByParamName(
      final Collection<String> componentIds, final Collection<String> paramNames);

  /**
   * Gets the component instance related to the given identifier.<br> In contrary to
   * {@link #getComponentInst(String)}, {@link #getComponentInstLight(String)} signatures, this one
   * is able to return different kinds of implementation of {@link SilverpeasComponentInstance} and
   * it is able to deal with administration cache too.<br> So, this signature is useful into
   * contexts of transverse treatments.
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
   * Return the full detail of the user with the given ldap id
   */
  UserFull getUserFull(String sUserId);

  /**
   * Return a list of full detail of users corresponding to
   */
  List<UserFull> getUserFulls(Collection<String> userIds);

  /**
   * Return the detail of the user with the given ldap id
   */
  <T extends User> T getUserDetail(String sUserId);

  /**
   * Return an array of users corresponding to the given user id array
   */
  <T extends User> T[] getUserDetails(String[] asUserIds);

  /**
   * Return all the users allowed to access the given component
   */
  <T extends User> T[] getAllUsers(String componentId);

  /**
   * Gets all the users that belong to the specified domain.
   * @param domainId the unique identifier of the domain.
   * @return an array of users objects or null if no such domain exists.
   */
  <T extends User> T[] getAllUsersInDomain(String domainId);

  /**
   * Searches the users that match the specified criteria.
   * @param criteria the criteria in searching of user details.
   * @return a slice of the list of user details matching the criteria or an empty list of no ones
   * are found. user details.
   */
  <T extends User> SilverpeasList<T> searchUsers(UserDetailsSearchCriteria criteria);

  /**
   * Gets all the user groups that belong to the specified domain.
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
   * @param orderedByType are the returned groups have to be ordered by their type? If yes, then
   * first are the usual groups and then the community ones.
   * @return a slice of the list of user groups matching the criteria or an empty list of no ones
   * are found.
   */
  <T extends Group> SilverpeasList<T> searchGroups(GroupsSearchCriteria criteria,
      boolean orderedByType);

  /**
   * Returns the total number of distinct users recursively contained in the specified group
   */
  int getAllSubUsersNumber(String sGroupId);

  /**
   * For use in userPanel : return the direct subgroups
   */
  <T extends Group> T[] getAllSubGroups(String parentGroupId);

  /**
   * Gets all the groups and subgroups that are children of the specified group.
   * @param parentGroupId the unique identifier of a group.
   * @param <T> the concrete type of the {@link Group} instances to return.
   * @return an array with all the groups that are children of the specified group.
   */
  @SuppressWarnings("unused")
  <T extends Group> T[] getRecursivelyAllSubgroups(String parentGroupId);

  /**
   * Return all the users of Silverpeas
   */
  <T extends User> T[] getAllUsers();

  /**
   * Return all the users with the given profile allowed to access the given component of the given
   * space
   */
  <T extends User> T[] getUsers(String componentId, String profile);

  /**
   * Gets the collection of silverpeas roles the given user has on the component instance
   * represented by the given identifier.<br> In contrary to
   * {@link #getUserProfiles(String, String)}, {@link #getUserProfiles(String, ProfiledObjectId)} or
   * {@link #getUserProfiles(String, String, ProfiledObjectId)} signatures, this one is able to
   * return user roles of different kinds of implementation of
   * {@link SilverpeasComponentInstance}.<br> So, this signature is useful into contexts of
   * transversal treatments.<br> BE CAREFUL, the manager role is never returned as it corresponds to
   * a space role.
   * @param componentInstanceIdentifier the identifier of the component instance.
   * @return an optional component instance.
   */
  Collection<SilverpeasRole> getUserSilverpeasRolesOn(User user,
      String componentInstanceIdentifier);

  /**
   * Gets all the profiles the specified user plays in the given component instance.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param componentId the unique identifier of a component instance.
   * @return an array with the name of all the user profiles in the given component instance. The
   * name of the profiles correspond to the roles the user plays in the component instance.
   */
  String[] getUserProfiles(String userId, String componentId);

  /**
   * Gets the profile names of given user indexed by the given component instances.
   * @param userId a user identifier as string.
   * @param componentIds list of component instance identifier as string.
   * @return a map filled with list of profile name as string by component instance identifier as
   * string.
   */
  Map<String, Set<String>> getUserProfilesByComponentId(String userId,
      Collection<String> componentIds);

  /**
   * Gets all the profiles the specified user plays in the given space.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param spaceId the unique identifier of a space.
   * @return a list with the name of all the user profiles in the given space. The
   * name of the profiles correspond to the roles the user plays in the space.
   */
  List<String> getSpaceUserProfilesBySpaceId(String userId, String spaceId);

  /**
   * Gets the profile names of given user indexed by the given space.
   * @param userId a user identifier as string.
   * @param spaceIds list of space identifier as string.
   * @return a map filled with list of profile name as string by space identifier as
   * string.
   */
  Map<String, Set<String>> getSpaceUserProfilesBySpaceIds(String userId,
      Collection<String> spaceIds);

  /**
   * Gets all the profiles the user have for the specified resource in the given component
   * instance.
   * @param userId the unique identifier of a user.
   * @param componentId the unique identifier of a component instance.
   * @param objectId the unique identifier of a resource in Silverpeas whose access is covered by
   * some user profiles.
   * @return an array with the name of all the user profiles the user play for the given resource in
   * the application instance. The name of the profiles correspond to the roles the user plays.
   */
  String[] getUserProfiles(String userId, String componentId, ProfiledObjectId objectId);

  /**
   * Gets the profile names of given user indexed by couple of given component instances and object
   * instances.
   * @param userId a user identifier as string.
   * @param componentIds list of component instance identifier as string.
   * @param profiledObjectIds if NOTHING is given, then all the rows associated to the type are
   * returned, otherwise all the rows associated to type and ids.
   * @return a map filled with list of profile name as string by couple component instance
   * identifier as string - object identifier as String.
   */
  Map<Pair<String, String>, Set<String>> getUserProfilesByComponentIdAndObjectId(String userId,
      Collection<String> componentIds, ProfiledObjectIds profiledObjectIds);

  /**
   * Gets the profile names the given user plays in the component instance for accessing the
   * resources of the given type. instances.
   * @param userId the unique identifier of a user.
   * @param componentId the unique identifier of a component instance.
   * @param profiledObjectType if NONE is given, then all the rows associated to the type are
   * returned, otherwise all the rows associated to type.
   * @return a map associating each object whose type matches the specified one with a list of
   * profile names.
   */
  Map<String, List<String>> getUserObjectProfiles(String userId, String componentId,
      ProfiledObjectType profiledObjectType);

  /**
   * Gets all the user profiles defined in the specified component instance to access the specified
   * resource.
   * @param componentId the unique identifier of a component instance.
   * @param objectId the unique identifier of a resource covered by access rights.
   * @return a list of user profiles.
   */
  List<ProfileInst> getUserProfiles(String componentId, ProfiledObjectId objectId);

  /**
   * Gets the user profile with the specified unique identifier.
   * @param profileId the unique identifier of a user profile.
   * @return the profile instance or null if no such profile exists.
   */
  ProfileInst getUserProfile(String profileId);

  /**
   * Return all administrators ids
   */
  String[] getAdministratorUserIds(String fromUserId);

  /**
   * Return the Group of the group with the given id
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
   * Return all root groups of silverpeas or null if an error occurred when getting the root
   * groups.
   */
  <T extends Group> T[] getAllRootGroups();

  /**
   * Get ALL the users that are in a group or his subgroups
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
   * Return all the spaces id manageable by given user in Silverpeas
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
   * Return all the subSpaces id available  given a space id (driver format)
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
   * Gets all the spaces at root level accessible by the specified user and containing at least one
   * instance of the specified application.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param componentName the name of an application in Silverpeas.
   * @return a list of space instances.
   */
  List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName);

  /**
   * Gets all the subspaces of the given space accessible by the specified user and containing at
   * least one instance of the specified application.
   * @param spaceId the unique identifier of a space in Silverpeas.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param componentName the name of an application in Silverpeas.
   * @return a list of space instances.
   */
  List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName);

  /**
   * Is the specified tool is available in Silverpeas?
   * </p>
   * A tool in Silverpeas is a singleton component that is dedicated to a given user. Each tool is
   * identified by a unique identifier, and it is unique to each user.
   * @param toolId the unique identifier of a tool.
   * @return true if the tool is available, false otherwise.
   */
  boolean isToolAvailable(String toolId);

  /**
   * Gets the component instance identifiers available for the specified user?
   * </p>
   * A component is an application in Silverpeas to perform some tasks and to manage some resources.
   * Each component in Silverpeas can be instantiated several times, each of them corresponding then
   * to a running application in Silverpeas, and it is uniquely identified from others instances by
   * a given identifier.
   * @param userId the unique identifier of a user.
   * @return a list of component instance identifier as string.
   */
  List<String> getAvailableComponentsByUser(String userId);

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
   */
  boolean isComponentAvailableToUser(String componentId, String userId);

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
   */
  boolean isComponentAvailableToGroup(String componentId, String groupId);

  /**
   * Is the specified component instance exists in Silverpeas?
   * @param componentId the unique identifier of a component instance.
   * @return true if a component instance exists in Silverpeas with the given identifier. False
   * otherwise.
   */
  boolean isComponentExist(String componentId);

  /**
   * Is the specified component instance manageable by the given user? The component instance is
   * manageable if the user has enough access right to manage it.
   * @param componentId the unique identifier of the component instance.
   * @param userId the unique identifier of a user.
   * @return true of the user can manage the specified component instance. False otherwise.
   */
  boolean isComponentManageable(String componentId, String userId);

  /**
   * Is the specified space is allowed to be accessed by the given user?
   * @param spaceId the unique identifier of a space
   * @param userId the unique identifier of a user
   * @return true if user is allowed to access at least to one component instance in given space,
   * false otherwise.
   */
  boolean isSpaceAvailable(String spaceId, String userId);

  /**
   * This method allows callers to perform several space availability checks for a given user.
   * <p>
   *   This is useful for treatments requiring highest performances than calling each time
   *   {@link #isSpaceAvailable(String, String)} for example.
   * </p>
   * <p>
   *   IMPORTANT: the {@link UserSpaceAvailabilityChecker} MUST not be an attribute of a
   *   singleton instance.
   * </p>
   * @param userId the unique identifier of a user
   * @return a {@link UserSpaceAvailabilityChecker} instance initialized for the given user
   * identifier.
   */
  UserSpaceAvailabilityChecker getUserSpaceAvailabilityChecker(String userId);

  /**
   * Is the specified resource protected by access rights in the given component instance is allowed
   * to be accessed by the specified user?
   * @param objectId the unique identifier of a resource protected by access rights.
   * @param componentId the unique identifier of a component instance.
   * @param userId the unique identifier of a user in Silverpeas.
   * @return true if the user can access the given resource in the component instance. False
   * otherwise.
   */
  boolean isObjectAvailableToUser(ProfiledObjectId objectId, String componentId, String userId);

  /**
   * Is the specified resource protected by access rights in the given component instance is allowed
   * to be accessed by the specified group of users?
   * @param objectId the unique identifier of a resource protected by access rights.
   * @param componentId the unique identifier of a component instance.
   * @param groupId the unique identifier of a group of users in Silverpeas.
   * @return true if the group can access the given resource in the component instance. False
   * otherwise.
   */
  boolean isObjectAvailableToGroup(ProfiledObjectId objectId, String componentId, String groupId);

  /**
   * Gets the treeview of spaces in Silverpeas available to the specified user.
   * @param userId the unique identifier of a user.
   * @return a list of space instances accessible by the user and modeling a treeview.
   */
  List<SpaceInstLight> getSpaceTreeview(String userId);

  /**
   * Gets the all the subspaces of the specified space accessible to the given user.
   * @param userId the unique identifier of a user.
   * @param spaceFatherId the unique identifier of a space.
   * @return an array with the identifiers of the subspaces.
   */
  String[] getAllowedSubSpaceIds(String userId, String spaceFatherId);

  /**
   * Gets the root space for which the specified space is a direct or indirect child. If the given
   * space is yet a root space, it returns just its instance.
   * @param spaceId the unique identifier of a space in Silverpeas.
   * @return the space instance representing the root space, parent of the given space, or null if
   * the given space doesn't exist.
   */
  SpaceInstLight getRootSpace(String spaceId);

  /**
   * Return all the users of Silverpeas
   */
  String[] getAllUsersIds();

  /**
   * Return userIds according to a list of profile names
   * @param componentId the instance id
   * @param profileNames the list which contains the profile names
   * @return a string array of user id
   */
  String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames);

  /**
   * Return userIds according to a list of profile names
   * @param componentId the instance id
   * @param profileNames the list which contains the profile names
   * @param includeRemovedUsersAndGroups users in removed state are taken into account
   * @return a string array of user id
   */
  String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames,
      final boolean includeRemovedUsersAndGroups);

  /**
   * Gets the users playing the specified roles for the given protected resource in the specified
   * component instance.
   * @param componentId the unique identifier of a component instance.
   * @param profileNames the list which contains the profile names
   * @return an array with the user identifiers.
   */
  String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId,
      List<String> profileNames);

  /**
   * Gets the users playing the specified roles for the given protected resource in the specified
   * component instance.
   * @param componentId the unique identifier of a component instance.
   * @param profileNames the list which contains the profile names
   * @param includeRemovedUsersAndGroups users in removed state are taken into account
   * @return an array with the user identifiers.
   */
  String[] getUsersIdsByRoleNames(String componentId, ProfiledObjectId objectId,
      List<String> profileNames, final boolean includeRemovedUsersAndGroups);

  /**
   * Gets the domain with the specified identifier.
   * @param domainId the unique identifier of a user domain.
   * @return a {@link Domain} instance or null if no such user domain exists.
   */
  Domain getDomain(String domainId);

  /**
   * Gets all the user domains defined in Silverpeas.
   * @return an array of {@link Domain} instances.
   */
  Domain[] getAllDomains();

  /**
   * Gets all the groups to which the specified user belongs explicitly. The parent groups to which
   * the user doesn't belong directly but only by the children group(s) it belongs to aren't taken
   * into account.
   * @param userId the unique identifier of a user.
   * @return a list of group of users to which the given user explicitly belongs.
   */
  List<GroupDetail> getDirectGroupsOfUser(String userId);

  /**
   * Gets all the groups to which the specified user belongs. All the groups are taken into account,
   * even those to which he doesn't belong directly but only by a child group.
   * @param userId the unique identifier of a user.
   * @return an array  with the identifier of all the groups the user is concerned.
   */
  String[] getAllGroupIdsOfUser(String userId);

  /**
   * Gets all the component instances in Silverpeas allowed to be accessed by the specified user.
   * @param userId the unique identifier of a user.
   * @return an array with the identifier of the component instances accessible to the given user.
   */
  @SuppressWarnings("unused")
  String[] getAllowedComponentIds(String userId);

  /**
   * Get all the users (except delete ones) that are defined in the specified domains.
   * @param domainIds a list of domain identifiers.
   * @return a list of users belonging to the given domains sorted by reverse creation order.
   */
  <T extends User> List<T> getUsersOfDomainsFromNewestToOldest(List<String> domainIds);

  /**
   * Get all the users (except delete ones) that are defined in the specified domains.
   * @param domainIds a list of domain identifiers.
   * @return a list of users belonging to the given domains sorted by the alphabetic order of their
   * name.
   */
  <T extends User> List<T> getUsersOfDomains(List<String> domainIds);

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
  boolean isAdminTool(String toolId);

  /**
   * Gets the identifier of the component instances that are both searchable and that satisfy the
   * specified criteria.
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
   */
  SpaceProfile getSpaceProfile(String spaceId, SilverpeasRole role);

  /**
   * Gets the complete treeview of spaces and component instances defined in Silverpeas.
   * @return a {@link SpaceWithSubSpacesAndComponents} instance representing the complete treeview
   * of the organizational resources defined in Silverpeas.
   * @throws AdminException if an error occurs while computing the treeview.
   */
  SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException;

  /**
   * Gets the complete treeview of spaces and component instances defined in Silverpeas accessible
   * to the specified user.
   * @param userId the unique identifier of a user in Silverpeas.
   * @return a {@link SpaceWithSubSpacesAndComponents} instance representing the complete treeview
   * of the organizational resources defined in Silverpeas and accessible to the given user.
   * @throws AdminException if an error occurs while computing the treeview.
   */
  SpaceWithSubSpacesAndComponents getFullTreeview(String userId) throws AdminException;

  /**
   * Gets the complete treeview of spaces and of instances of the specified application and that are
   * accessible to the specified user.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param componentName the name of an application in Silverpeas.
   * @return a {@link SpaceWithSubSpacesAndComponents} instance representing the complete treeview
   * accessible to the given user, with only as component instances those of the given application.
   * @throws AdminException if an error occurs while computing the treeview.
   */
  SpaceWithSubSpacesAndComponents getFullTreeviewOnComponentName(String userId,
      String componentName) throws AdminException;

  /**
   * Gets the complete treeview of spaces and component instances contained in the specified space
   * and that are accessible to the specified user.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param spaceId the unique identifier of a space.
   * @return a {@link SpaceWithSubSpacesAndComponents} instance representing the complete treeview
   * rooted to the given space and accessible to the given user.
   * @throws AdminException if an error occurs while computing the treeview.
   */
  SpaceWithSubSpacesAndComponents getFullTreeview(String userId, String spaceId)
      throws AdminException;

  /**
   * Gets the path of the specified space in the organizational tree of Silverpeas.
   * @param spaceId the unique identifier of a space.
   * @return a list of space instances ordered from the root space to the specified one, each of
   * them representing a node of the path.
   */
  @NonNull
  List<SpaceInstLight> getPathToSpace(@NonNull String spaceId);

  /**
   * Gets the path of the specified component instance in the organizational tree of Silverpeas.
   * @param componentId the unique identifier of a component instance.
   * @return a list of space instances ordered from the root space to the space to which the
   * component instance belongs directly. Each of then represents a node in the path to the given
   * component instance.
   */
  List<SpaceInstLight> getPathToComponent(String componentId);
}