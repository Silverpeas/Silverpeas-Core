package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.component.model.WAComponent;

import java.util.List;
import java.util.Map;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ServiceProvider;

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
   * Method declaration
   *
   * @return
   * @see
   */
  String getGeneralSpaceId();

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
   * Return all the components of silverpeas read in the xmlComponent directory
   */
  Map<String, WAComponent> getAllComponents();

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

  /**
   * Return the component Instance corresponding to the given component id
   */
  ComponentInst getComponentInst(String sComponentId);

  /**
   * @param spaceId - id of the space or subSpace
   * @return a List of SpaceInst ordered from root to subSpace
   * @throws Exception
   */
  List<SpaceInst> getSpacePath(String spaceId);

  List<SpaceInst> getSpacePathToComponent(String componentId);

  /**
   * Return the component Instance Light corresponding to the given component id
   */
  ComponentInstLight getComponentInstLight(String sComponentId);

  /**
   * Return the database id of the user with the given ldap Id
   */
  int getUserDBId(String sUserId);

  /**
   * Return the ldapId of the user with the given db Id
   */
  String getUserDetailByDBId(int id);

  /**
   * Return the UserDetail of the user with the given ldap Id
   */
  UserFull getUserFull(String sUserId);

  /**
   * Return the UserDetail of the user with the given ldap Id
   */
  UserDetail getUserDetail(String sUserId);

  /**
   * Return an array of UserDetail corresponding to the given user Id array
   */
  UserDetail[] getUserDetails(String[] asUserIds);

  /**
   * @deprecated use getAllUsers(String componentId) Return all the users allowed to acces the given
   * component of the given space
   */
  UserDetail[] getAllUsers(String sPrefixTableName, String sComponentName);

  /**
   * Return all the users allowed to acces the given component
   */
  UserDetail[] getAllUsers(String componentId);

  /**
   * Gets all the users that belong to the specified domain.
   *
   * @param domainId the unique identifier of the domain.
   * @return an array of UserDetail objects or null if no such domain exists.
   */
  UserDetail[] getAllUsersInDomain(String domainId);

  /**
   * Searches the users that match the specified criteria.
   *
   * @param criteria the criteria in searching of user details.
   * @return a slice of the list of user details matching the criteria or an empty list of no ones
   * are found.
   * @throws AdminException if an error occurs while getting the
   * user details.
   */
  ListSlice<UserDetail> searchUsers(UserDetailsSearchCriteria criteria);

  /**
   * Gets all the user groups that belong to the specified domain.
   *
   * @param domainId the unique identifier of the domain.
   * @return an array of Group objects or null if no such domain exists.
   */
  Group[] getAllRootGroupsInDomain(String domainId);

  /**
   * For use in userPanel : return the users that are direct child of a given group
   */
  UserDetail[] getFiltredDirectUsers(String sGroupId, String sUserLastNameFilter);

  /**
   * Return an array of UserDetail corresponding to the founded users
   */
  UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd);

  /**
   * Searches the groups that match the specified criteria.
   *
   * @param criteria the criteria in searching of user groups.
   * @return a slice of the list of user groups matching the criteria or an empty list of no ones
   * are found.
   * @throws AdminException if an error occurs while getting the
   * user groups.
   */
  ListSlice<Group> searchGroups(GroupsSearchCriteria criteria);

  /**
   * Return an array of Group corresponding to the founded groups
   */
  Group[] searchGroups(Group modelGroup, boolean isAnd);

  /**
   * Returns the total number of distinct users recursively contained in the specified group
   */
  int getAllSubUsersNumber(String sGroupId);

  /**
   * For use in userPanel : return the direct sub-groups
   */
  Group[] getAllSubGroups(String parentGroupId);

  /**
   * Return all the users of Silverpeas
   */
  UserDetail[] getAllUsers();

  /**
   * Return all the users with the given profile allowed to access the given component of the given
   * space
   */
  UserDetail[] getUsers(String sPrefixTableName, String sComponentName, String sProfile);

  String[] getUserProfiles(String userId, String componentId);

  String[] getUserProfiles(String userId, String componentId, int objectId, ObjectType objectType);

  List<ProfileInst> getUserProfiles(String componentId, String objectId, String objectType);

  ProfileInst getUserProfile(String profileId);

  /**
   * Return all administrators ids
   */
  String[] getAdministratorUserIds(String fromUserId);

  /**
   * Return the Group of the group with the given Id
   */
  Group getGroup(String sGroupId);

  /**
   * Return all groups specified by the groupsIds
   */
  Group[] getGroups(String[] groupsId);

  /**
   * Return all the groups of silverpeas
   */
  Group[] getAllGroups();

  /**
   * Return all root groups of silverpeas or null if an error occured when getting the root groups.
   */
  Group[] getAllRootGroups();

  /**
   * Get ALL the users that are in a group or his sub groups
   */
  UserDetail[] getAllUsersOfGroup(String groupId);

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
   * Return all the components Id available in webactiv given a space id (driver format)
   */
  String[] getAllComponentIds(String sSpaceId);

  /**
   * Return all the components Id recursively available in webactiv given a space id (driver format)
   */
  String[] getAllComponentIdsRecur(String sSpaceId);

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces or all
   * silverpeas) available in silverpeas given a userId and a componentNameRoot
   *
   * @author dlesimple
   * @param sSpaceId
   * @param sUserId
   * @param sComponentRootName
   * @param inCurrentSpace
   * @param inAllSpaces
   * @return Array of componentsIds
   */
  String[] getAllComponentIdsRecur(String sSpaceId, String sUserId, String sComponentRootName,
      boolean inCurrentSpace, boolean inAllSpaces);

  List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName);

  List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName);

  boolean isToolAvailable(String toolId);

  boolean isComponentAvailable(String componentId, String userId);

  boolean isComponentExist(String componentId);

  boolean isComponentManageable(String componentId, String userId);

  boolean isSpaceAvailable(String spaceId, String userId);

  boolean isObjectAvailable(int objectId, ObjectType objectType, String componentId, String userId);

  List<SpaceInstLight> getSpaceTreeview(String userId);

  String[] getAllowedSubSpaceIds(String userId, String spaceFatherId);

  SpaceInstLight getRootSpace(String spaceId);

  /**
   * Return all the users of Silverpeas
   */
  String[] getAllUsersIds();

  /**
   *
   * Return all the users of Silverpeas
   *
   * @param groupId
   * @param componentId
   * @param profileId
   * @param filterUser
   * @return
   */
  String[] searchUsersIds(String groupId, String componentId, String[] profileId,
      UserDetail filterUser);

  /**
   * Return userIds according to a list of profile names
   *
   * @param componentId the instance id
   * @param profileNames the list which contains the profile names
   * @return a string array of user id
   */
  String[] getUsersIdsByRoleNames(String componentId, List<String> profileNames);

  String[] getUsersIdsByRoleNames(String componentId, String objectId, ObjectType objectType,
      List<String> profileNames);

  String[] searchGroupsIds(boolean isRootGroup, String componentId, String[] profileId,
      Group modelGroup);

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

  String[] getDirectGroupIdsOfUser(String userId);

  String[] getAllGroupIdsOfUser(String userId);

  void reloadAdminCache();

  /**
   * Is the anonymous access is activated for the running Silverpeas? When the anonymous access is
   * activated, then a specific user for anonymous access should be set; all anonym accesses to the
   * running Silverpeas are done with this user profile.
   *
   * @return true if the anonym access is activated, false otherwise.
   */
  boolean isAnonymousAccessActivated();

  String[] getAllowedComponentIds(String userId);

  public List<UserDetail> getAllUsersFromNewestToOldest();

  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(
      List<String> domainIds);

  public List<UserDetail> getUsersOfDomains(
      List<String> domainIds);

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
}
