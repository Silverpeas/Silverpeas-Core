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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.persistence.GroupUserRoleRow;
import org.silverpeas.core.admin.persistence.GroupUserRoleTable;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.SpaceUserRoleRow;
import org.silverpeas.core.admin.persistence.SpaceUserRoleTable;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.admin.persistence.UserRoleTable;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.dao.GroupDAO;
import org.silverpeas.core.admin.user.dao.GroupSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.dao.SearchCriteriaDAOFactory;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.notification.GroupEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.admin.domain.model.Domain.MIXED_DOMAIN_ID;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.likeIgnoreCase;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class GroupManager {

  public static final String GROUP = "group";
  public static final String GROUP_MANAGER_GET_GROUPS_OF_DOMAIN =
      "GroupManager.getGroupsOfDomain()";
  public static final String GROUP_MANAGER_ADD_GROUP = "GroupManager.addGroup()";
  public static final String GROUP_MANAGER_UPDATE_GROUP = "GroupManager.updateGroup()";
  public static final String IN_GROUP = "in group ";
  public static final String GROUP_MANAGER_DELETE_GROUP = "GroupManager.deleteGroup()";
  public static final String REMOVING_MESSAGE = "Suppression de ";
  public static final String IN_SILVERPEAS_MESSAGE = " dans la base";
  @Inject
  private GroupDAO groupDao;
  @Inject
  private UserDAO userDao;
  @Inject
  private GroupEventNotifier notifier;
  @Inject
  private OrganizationSchema organizationSchema;
  @Inject
  private DomainDriverManager domainDriverManager;

  protected GroupManager() {
  }

  public static GroupManager get() {
    return ServiceProvider.getService(GroupManager.class);
  }

  /**
   * Gets the groups that match the specified criteria.
   *
   * @param criteria the criteria in searching of user groups.
   * @return a slice of the list of user groups matching the criteria or an empty list of no ones are found.
   * @throws AdminException if an error occurs while getting the user groups.
   */
  public SilverpeasList<GroupDetail> getGroupsMatchingCriteria(final GroupSearchCriteriaForDAO criteria) throws
          AdminException {
    try (Connection connection = DBUtil.openConnection()){
      final GroupCriteriaFilter filter = new GroupCriteriaFilter(connection, criteria, groupDao);
      final SilverpeasList<GroupDetail> groups = filter.getFilteredGroups();
      String[] domainIdConstraint = new String[0];
      for (final String domainId : criteria.getCriterionOnDomainIds()) {
        if (!MIXED_DOMAIN_ID.equals(domainId)) {
          domainIdConstraint = new String[]{domainId};
          break;
        }
      }
      final SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
      for (final GroupDetail group : groups) {
        final List<String> groupIds = filter.getAllSubGroups(group.getId())
            .stream()
            .map(GroupDetail::getId)
            .collect(Collectors.toList());
        groupIds.add(group.getId());
        final UserSearchCriteriaForDAO criteriaOnUsers = factory.getUserSearchCriteriaDAO();
        final UserState[] criterionOnUserStatesToExclude = criteria.getCriterionOnUserStatesToExclude();
        final int userCount = userDao.getUserCountByCriteria(connection, criteriaOnUsers.
            onDomainIds(domainIdConstraint).
            onGroupIds(groupIds.toArray(new String[0])).
            onUserStatesToExclude(criterionOnUserStatesToExclude));
        group.setTotalNbUsers(userCount);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("Fail to search groups matching criteria", e);
    }
  }

  /**
   * Gets the total number of users in the specified group, that is to say the number of distinct
   * users in the specified group and in its subgroups.
   * @param domainId the unique identifier to which the group belong.
   * @param groupId the unique identifier of the group.
   * @return the total users count in the specified group.
   * @throws AdminException if an error occurs while computing the user count.
   */
  public int getTotalUserCountInGroup(String domainId, String groupId) throws AdminException {
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();
      SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
      List<String> groupIds = getAllSubGroupIdsRecursively(groupId);
      groupIds.add(groupId);
      UserSearchCriteriaForDAO criteriaOnUsers = factory.getUserSearchCriteriaDAO();
      return userDao.getUserCountByCriteria(connection, criteriaOnUsers.
          onDomainIds(domainId).
          onGroupIds(groupIds.toArray(new String[groupIds.size()])));
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Add a user to a group
   *
   * @param sUserId
   * @param sGroupId
   * @throws AdminException
   */
  public void addUserInGroup(String sUserId, String sGroupId) throws
          AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      groupDao.addUserInGroup(connection, sUserId, sGroupId);
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user " + sUserId, IN_GROUP + sGroupId), e);
    }
  }

  public void addUsersInGroup(final List<String> userIds, final String groupId)
      throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      groupDao.addUsersInGroup(connection, userIds, groupId);
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Remove a user from a group
   *
   * @param sUserId
   * @param sGroupId
   * @throws AdminException
   */
  public void removeUserFromGroup(String sUserId, String sGroupId)
          throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      SynchroDomainReport.debug("GroupManager.removeUserFromGroup()",
          "Retrait de l'utilisateur d'ID " + sUserId + " du groupe d'ID " + sGroupId);
      groupDao.deleteUserInGroup(connection, sUserId, sGroupId);
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user " + sUserId, IN_GROUP + sGroupId), e);
    }
  }

  public void removeUsersFromGroup(final List<String> userIds, final String groupId)
      throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      for (String userId : userIds) {
        groupDao.deleteUserInGroup(connection, userId, groupId);
      }
    } catch (SQLException e) {
      throw new AdminException(
          failureOnDeleting("users " + userIds.stream().collect(Collectors.joining("n")),
              IN_GROUP + groupId), e);
    }
  }

  public List<String> getDirectGroupIdsInSpaceRole(final String spaceRoleId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return groupDao.getDirectGroupIdsBySpaceUserRole(connection, spaceRoleId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("groups in space role", spaceRoleId), e);
    }
  }

  /**
   * Get the direct groups id containing a user. Groups that the user is linked to by
   * transitivity are not returned.
   * @param sUserId
   * @return
   * @throws AdminException
   */
  public List<GroupDetail> getDirectGroupsOfUser(String sUserId) throws
          AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      final List<GroupDetail> groups = groupDao.getDirectGroupsOfUser(connection, sUserId);
      return setDirectUsersOfGroups(groups);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("direct groups of user", sUserId), e);
    }
  }

  /**
   * Get all group ids containing a user. So, groups that the user is linked to by
   * transitivity are returned too (recursive treatment).
   * @param userId
   * @return
   * @throws AdminException
   */
  public List<String> getAllGroupsOfUser(String userId) throws AdminException {
    Set<String> allGroupsOfUser = new HashSet<>();

    List<GroupDetail> directGroups = getDirectGroupsOfUser(userId);
    for (GroupDetail group : directGroups) {
      if (group != null) {
        allGroupsOfUser.add(group.getId());
        while (group != null && StringUtil.isDefined(group.getSuperGroupId())) {
          group = getGroupDetail(group.getSuperGroupId());
          if (group != null) {
            allGroupsOfUser.add(group.getId());
          }
        }
      }
    }

    return new ArrayList<>(allGroupsOfUser);
  }

  /**
   * Get the Silverpeas group id of group qualified by given specific Id and domain id
   *
   * @param sSpecificId
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String getGroupIdBySpecificIdAndDomainId(String sSpecificId,
          String sDomainId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      GroupDetail gr = groupDao.getGroupBySpecificId(connection, sDomainId, sSpecificId);
      return gr.getId();
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("groups by specific id and domain", sSpecificId + "/" + sDomainId), e);
    }
  }

  public List<GroupDetail> getAllGroups() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      final List<GroupDetail> groups = groupDao.getAllGroups(connection);
      return setDirectUsersOfGroups(groups);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("all", "groups"), e);
    }
  }

  /**
   * Gets all the root user groups in Silverpeas.
   *
   * @return a list of root user groups.
   * @throws AdminException if an error occurs while getting the root groups.
   */
  public List<GroupDetail> getAllRootGroups() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<GroupDetail> groups = groupDao.getAllRootGroups(connection);
      return setDirectUsersOfGroups(groups);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all root groups", ""), e);
    }
  }

  public List<GroupDetail> getSubGroups(final String groupId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      final List<GroupDetail> groups = groupDao.getDirectSubGroups(connection, groupId);
      return setDirectUsersOfGroups(groups);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("all subgroups of group", groupId), e);
    }
  }

  public List<GroupDetail> getRecursivelySubGroups(final String groupId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      final List<GroupDetail> groups = getRecursivelySubGroups(connection, groupId);
      return setDirectUsersOfGroups(groups);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("recursively all subgroups of group", groupId), e);
    }
  }

  private List<GroupDetail> getRecursivelySubGroups(final Connection connection,
      final String groupId) throws SQLException {
    List<GroupDetail> subGroupsFlatTree = new ArrayList<>();
    List<GroupDetail> groups = groupDao.getDirectSubGroups(connection, groupId);
    for (GroupDetail group : groups) {
      subGroupsFlatTree.add(group);
      subGroupsFlatTree.addAll(getRecursivelySubGroups(connection, group.getId()));
    }
    return subGroupsFlatTree;
  }

  /**
   * Get the path from root to a given group
   *
   * @param groupId
   * @return
   * @throws AdminException
   */
  public List<String> getPathToGroup(String groupId) throws
          AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<String> path = new ArrayList<>();
      GroupDetail superGroup = groupDao.getSuperGroup(connection, groupId);
      while (superGroup != null) {
        path.add(0, superGroup.getId());
        superGroup = groupDao.getSuperGroup(connection, superGroup.getId());
      }

      return path;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("path to group", groupId), e);
    }
  }

  /**
   * /**
   * Check if the given group exists
   *
   * @param sName
   * @return true if a group with the given name
   * @throws AdminException
   */
  public boolean isGroupExist(String sName) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return groupDao.isGroupByNameExists(connection, sName);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups matching name", sName), e);
    }
  }

  /**
   * Gets the group detail instance from group DAO without decoration data.
   * @param groupId the aimed group id.
   * @return the group detail instance without decoration data.
   * @throws AdminException
   */
  private GroupDetail getGroupDetail(String groupId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return groupDao.getGroup(connection, groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(GROUP, groupId), e);
    }
  }

  /**
   * Get group information with the given id from Silverpeas
   *
   * @param groupId
   * @return
   * @throws AdminException
   */
  public GroupDetail getGroup(String groupId) throws AdminException {
    GroupDetail group = getGroupDetail(groupId);
    if (group != null) {
      try {
        setDirectUsersOfGroup(group);
      } catch (SQLException e) {
        throw new AdminException(failureOnGetting("failure getting direct users of group", groupId),
            e);
      }
    }
    return group;
  }

  /**
   * Get the all the sub groups id of a given group
   *
   * @param superGroupId
   * @return
   * @throws AdminException
   */
  public List<String> getAllSubGroupIdsRecursively(String superGroupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      return getSubGroupIds(con, superGroupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("recursively all subgroups of group", superGroupId),
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<String> getSubGroupIds(Connection con, String groupId) throws SQLException {
    List<String> groupIds = new ArrayList<>();
    List<GroupDetail> groups = groupDao.getDirectSubGroups(con, groupId);
    for (GroupDetail group : groups) {
      groupIds.add(group.getId());
      groupIds.addAll(getSubGroupIds(con, group.getId()));
    }
    return groupIds;
  }

  /**
   * Get group information with the given group name
   *
   * @param sGroupName
   * @param sDomainFatherId
   * @return
   * @throws AdminException
   */
  public GroupDetail getGroupByNameInDomain(String sGroupName,
          String sDomainFatherId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      GroupDetail group = domainDriverManager.getGroupByNameInDomain(sGroupName, sDomainFatherId);

      if (group != null) {
        String specificId = group.getSpecificId();
        GroupDetail gr = groupDao.getGroupBySpecificId(connection, sDomainFatherId, specificId);
        if (gr != null) {
          group.setId(gr.getId());
          // Get the selected users for this group
          setDirectUsersOfGroup(group);
        } else {
          return null;
        }
      }
      return group;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("group by name", sGroupName), e);
    }
  }

  /**
   *
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public GroupDetail[] getRootGroupsOfDomain(String sDomainId) throws
          AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<GroupDetail> groups = groupDao.getAllRootGroupsByDomainId(connection, sDomainId);
      setDirectUsersOfGroups(groups);
      return groups.toArray(new GroupDetail[groups.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root groups in domain", sDomainId), e);
    }
  }

  public List<GroupDetail> getSynchronizedGroups() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      // Get groups of domain from Silverpeas database
      final List<GroupDetail> groups = groupDao.getSynchronizedGroups(connection);
      return setDirectUsersOfGroups(groups);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("synchronized groups", ""), e);
    }
  }

  /**
   * Get the groups of domain
   *
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public GroupDetail[] getGroupsOfDomain(String sDomainId) throws
          AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      // Get organization
      SynchroDomainReport.debug(GROUP_MANAGER_GET_GROUPS_OF_DOMAIN,
              "Recherche des groupes du domaine dans la base...");
      // Get groups of domain from Silverpeas database
      List<GroupDetail> grs = groupDao.getAllGroupsByDomainId(connection, sDomainId);
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.size()];
      for (int nI = 0; nI < grs.size(); nI++) {
        groups[nI] = grs.get(nI);
        SynchroDomainReport.debug(
            GROUP_MANAGER_GET_GROUPS_OF_DOMAIN, "Groupe trouvé no : " + Integer.
                toString(nI) + ", specificID : " + groups[nI].getSpecificId() + ", desc. : "
                + groups[nI].getDescription());
      }
      SynchroDomainReport.debug(GROUP_MANAGER_GET_GROUPS_OF_DOMAIN,
          "Récupération de " + grs.size() + " groupes du domaine dans la base");
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("groups in domain", sDomainId), e);
    }
  }

  public List<String> getDirectGroupIdsInRole(String roleId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return groupDao.getDirectGroupIdsByUserRole(connection, roleId);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("groups in profile ", roleId), e);
    }
  }

  /**
   * Add the given group in Silverpeas
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String addGroup(GroupDetail group, boolean onlyInSilverpeas) throws AdminException {
    if (group == null || !StringUtil.isDefined(group.getName())) {
      if (group != null) {
        SynchroDomainReport.error(GROUP_MANAGER_ADD_GROUP, "Problème lors de l'ajout du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }

    try (Connection connection = DBUtil.openConnection()) {
      // Create group in specific domain (if onlyInSilverpeas is not true)
      // if domainId=-1 then group is a silverpeas organization
      if (!onlyInSilverpeas) {
        String specificId;
        if (group.getDomainId() != null) {
          specificId = domainDriverManager.createGroup(group);
          group.setSpecificId(specificId);
        }
      }
      // Create the group node in Silverpeas
      if (StringUtil.isDefined(group.getSuperGroupId())) {
        SynchroDomainReport.debug(GROUP_MANAGER_ADD_GROUP, "Ajout du groupe " + group.getName()
                + " (père=" + getGroupDetail(group.getSuperGroupId()).getSpecificId()
                + ") dans la table ST_Group");
      } else {
        SynchroDomainReport.debug(GROUP_MANAGER_ADD_GROUP, "Ajout du groupe " + group.getName()
                + " (groupe racine) dans la table ST_Group...");
      }
      String groupId = groupDao.saveGroup(connection, group);
      group.setId(groupId);
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, group);

      // index group information
      domainDriverManager.indexGroup(group);

      // Create the links group_user in Silverpeas
      SynchroDomainReport.debug(GROUP_MANAGER_ADD_GROUP,
              "Inclusion des utilisateurs directement associés au groupe " + group.getName()
              + " (table ST_Group_User_Rel)");
      String[] asUserIds = group.getUserIds();
      int nUserAdded = 0;
      for (String asUserId : asUserIds) {
        if (StringUtil.isDefined(asUserId)) {
          groupDao.addUserInGroup(connection, asUserId, groupId);
          nUserAdded++;
        }
      }
      SynchroDomainReport.info(
          GROUP_MANAGER_ADD_GROUP, nUserAdded + " utilisateurs ajoutés au groupe "
              + group.getName() + IN_SILVERPEAS_MESSAGE);
      return groupId;
    } catch (SQLException e) {
      SynchroDomainReport.error(GROUP_MANAGER_ADD_GROUP, "problème lors de l'ajout du groupe "
              + group.getName() + " - " + e.getMessage(), null);
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String deleteGroup(GroupDetail group,
          boolean onlyInSilverpeas) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.deleteGroup(group.getId());
      }
      // Delete the group node from Silverpeas
      deleteGroup(connection, group);
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, group);

      // Delete index of group information
      domainDriverManager.unindexGroup(group.getId());

      return group.getId();
    } catch (SQLException e) {
      SynchroDomainReport.error(GROUP_MANAGER_DELETE_GROUP,
              "problème lors de la suppression du groupe " + group.getName()
              + " - " + e.getMessage(), null);
      throw new AdminException(failureOnDeleting(GROUP, group.getId()), e);
    }
  }

  private void deleteGroup(final Connection connection, final GroupDetail group)
      throws SQLException, AdminException {
    int groupId = idAsInt(group.getId());

    SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
        "Suppression du groupe " + group.getName() + " dans la base...");

    // remove the group from each role where it's used.
    UserRoleTable userRoleTable = OrganizationSchema.get().userRole();
    UserRoleRow[] roles = userRoleTable.getDirectUserRolesOfGroup(groupId);
    SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
        REMOVING_MESSAGE + group.getName() + " des rôles dans la base");
    for (UserRoleRow role : roles) {
      userRoleTable.removeGroupFromUserRole(groupId, role.getId());
    }

    // remove the group from each space role where it's used.
    SpaceUserRoleTable spaceUserRoleTable = OrganizationSchema.get().spaceUserRole();
    SpaceUserRoleRow[] spaceRoles = spaceUserRoleTable.getDirectSpaceUserRolesOfGroup(groupId);
    SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
        REMOVING_MESSAGE + group.getName() + " comme manager d'espace dans la base");
    for (SpaceUserRoleRow spaceRole : spaceRoles) {
      spaceUserRoleTable.removeGroupFromSpaceUserRole(groupId, spaceRole.id);
    }

    // remove the group from each group role where it's used.
    GroupUserRoleTable groupUserRoleTable = OrganizationSchema.get().groupUserRole();
    GroupUserRoleRow[] groupRoles = groupUserRoleTable.getDirectGroupUserRolesOfGroup(groupId);
    SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
        REMOVING_MESSAGE + group.getName() + " comme manager de groupe dans la base");
    for (GroupUserRoleRow groupRole : groupRoles) {
      groupUserRoleTable.removeGroupFromGroupUserRole(groupId, groupRole.id);
    }

    // remove the group user role.
    GroupUserRoleRow groupRole = groupUserRoleTable.getGroupUserRoleByGroupId(groupId);
    if (groupRole != null) {
      SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
          REMOVING_MESSAGE + group.getName() + " des rôles de groupe dans la base");
      groupUserRoleTable.removeGroupUserRole(groupRole.id);
    }

    // remove all the subgroups
    List<GroupDetail> subgroups = groupDao.getDirectSubGroups(connection, group.getId());
    if (!subgroups.isEmpty()) {
      SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
          "Suppression des groupes fils de " + group.getName() + IN_SILVERPEAS_MESSAGE);
      for (GroupDetail subgroup : subgroups) {
        deleteGroup(connection, subgroup);
      }
    }
    // remove from the group any user.
    final List<String> userIds = userDao.getDirectUserIdsInGroup(connection, group.getId(), true);
    for (String userId : userIds) {
      removeUserFromGroup(userId, group.getId());
    }

    SynchroDomainReport.info(GROUP_MANAGER_DELETE_GROUP,
        REMOVING_MESSAGE + userIds.size() + " utilisateurs inclus directement dans le groupe " +
            group.getName() + IN_SILVERPEAS_MESSAGE);

    SynchroDomainReport.debug(GROUP_MANAGER_DELETE_GROUP,
        REMOVING_MESSAGE + group.getName() + " (ID=" + group.getName() + ")");
    groupDao.deleteGroup(connection, group);
  }

  /**
   * Update the given group
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String updateGroup(GroupDetail group, boolean onlyInSilverpeas)
          throws AdminException {
    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
            group.getId())) {
      if (group != null) {
        SynchroDomainReport.error(GROUP_MANAGER_UPDATE_GROUP, "Problème lors de maj du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }
    try (Connection connection = DBUtil.openConnection()) {
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.updateGroup(group);
      }
      // Get the group id
      String sGroupId = group.getId();
      String strInfoSycnhro;
      if (group.getSuperGroupId() != null) {
        strInfoSycnhro = "Maj du groupe " + group.getName() + " (père=" + getGroupDetail(group.
                getSuperGroupId()).getSpecificId() + ") dans la base (table ST_Group)...";
      } else {
        strInfoSycnhro = "Maj du groupe " + group.getName()
                + " (groupe racine) dans la base (table ST_Group)...";
      }
      SynchroDomainReport.debug(GROUP_MANAGER_UPDATE_GROUP, strInfoSycnhro);
      // Update the group
      groupDao.updateGroup(connection, group);

      // index group information
      domainDriverManager.indexGroup(group);

      // Update the users if necessary
      SynchroDomainReport.debug(GROUP_MANAGER_UPDATE_GROUP, "Maj éventuelle des relations du groupe "
              + group.getName()
              + " avec les utilisateurs qui y sont directement inclus (tables ST_Group_User_Rel)");

      List<String> asOldUsersId = userDao.getDirectUserIdsInGroup(connection, sGroupId, false);

      // Compute the remove users list
      List<String> asNewUsersId = Arrays.asList(group.getUserIds());
      long removedUsers =
          removeUsersNoMoreInGroup(connection, sGroupId, asOldUsersId, asNewUsersId);
      long addedUsers = addNewUsersInGroup(connection, sGroupId, asOldUsersId, asNewUsersId);

      SynchroDomainReport.info(GROUP_MANAGER_UPDATE_GROUP,
          "Groupe : " + group.getName() + ", ajout de " + addedUsers +
              " nouveaux utilisateurs, suppression de " + removedUsers
              + " utilisateurs");
      return sGroupId;
    } catch (Exception e) {
      SynchroDomainReport.error(GROUP_MANAGER_UPDATE_GROUP,
              "problème lors de la maj du groupe " + group.getName() + " - "
              + e.getMessage(), null);
      throw new AdminException(failureOnUpdate(GROUP, group.getId()), e);
    }
  }

  private long addNewUsersInGroup(final Connection connection, final String sGroupId,
      final List<String> asOldUsersId, final List<String> asNewUsersId) throws SQLException {
    long addedUsers = 0;
    for (String userId : asNewUsersId) {
      if (!asOldUsersId.contains(userId)) {
        SynchroDomainReport.debug(GROUP_MANAGER_UPDATE_GROUP,
            "Ajout de l'utilisateur d'ID " + userId + " dans le groupe d'ID " + sGroupId);
        groupDao.addUserInGroup(connection, userId, sGroupId);
        addedUsers++;
      }
    }
    return addedUsers;
  }

  private long removeUsersNoMoreInGroup(final Connection connection, final String sGroupId,
      final List<String> asOldUsersId, final List<String> asNewUsersId) throws SQLException {
    long removedUsers = 0;
    for (String userId : asOldUsersId) {
      if (!asNewUsersId.contains(userId)) {
        SynchroDomainReport.debug(GROUP_MANAGER_UPDATE_GROUP,
            "Suppression de l'utilisateur d'ID " + userId + " du groupe d'ID " + sGroupId);
        groupDao.deleteUserInGroup(connection, userId, sGroupId);
        removedUsers++;
      }
    }
    return removedUsers;
  }

  public List<String> getManageableGroupIds(String userId, List<String> groupIds)
          throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return groupDao.getManageableGroupIds(con, userId, groupIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("groups manageable by user", userId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public int getNBUsersDirectlyInGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return groupDao.getNBUsersDirectlyInGroup(con, groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user count in group", groupId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<String> getUsersDirectlyInGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return groupDao.getUsersDirectlyInGroup(con, groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in group", groupId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<GroupDetail> setDirectUsersOfGroups(final List<GroupDetail> groups)
      throws SQLException {
    final Map<String, List<String>> usersByGroup;
    try (Connection connection = DBUtil.openConnection()) {
      usersByGroup = userDao.getDirectUserIdsByGroup(connection,
          groups.stream().map(GroupDetail::getId).collect(Collectors.toList()), false);
    }
    for (GroupDetail group : groups) {
      final List<String> userIds = usersByGroup.getOrDefault(group.getId(), emptyList());
      group.setUserIds(userIds.toArray(new String[0]));
    }
    return groups;
  }

  private void setDirectUsersOfGroup(final GroupDetail group) throws SQLException {
    setDirectUsersOfGroups(singletonList(group));
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    return StringUtil.asInt(id, -1);
  }

  private static class GroupCriteriaFilter {
    private final Connection connection;
    private final GroupSearchCriteriaForDAO criteria;
    private final GroupDAO groupDao;
    private final String nameFilter;
    private final PaginationPage paginationPage;
    private final boolean childrenRequired;
    private final boolean logicalNameFiltering;
    private final Map<String, List<GroupDetail>> subGroupsOfGroupsCache = new LinkedHashMap<>();

    GroupCriteriaFilter(final Connection connection, final GroupSearchCriteriaForDAO criteria,
        final GroupDAO groupDao) {
      this.connection = connection;
      this.criteria = criteria;
      this.groupDao = groupDao;
      this.nameFilter = criteria.getCriterionOnName();
      this.paginationPage = criteria.getPagination();
      this.childrenRequired = criteria.childrenRequired();
      this.logicalNameFiltering = childrenRequired && isDefined(nameFilter);
      if (childrenRequired) {
        criteria.clearPagination();
      }
      if (logicalNameFiltering) {
        criteria.clearOnName();
      }
    }

    SilverpeasList<GroupDetail> getFilteredGroups() throws SQLException {
      List<GroupDetail> groups = groupDao.getGroupsByCriteria(connection, criteria);
      if (childrenRequired) {
        final List<GroupDetail> allSubGroups = new LinkedList<>();
        final Iterator<GroupDetail> it = groups.iterator();
        while(it.hasNext()) {
          final GroupDetail group = it.next();
          if (logicalNameFiltering && !likeIgnoreCase(group.getName(), nameFilter)) {
            it.remove();
          }
          final List<GroupDetail> subGroups = getAllSubGroups(group.getId());
          if (logicalNameFiltering) {
            subGroups.removeIf(g -> !likeIgnoreCase(g.getName(), nameFilter));
          }
          allSubGroups.addAll(subGroups);
        }
        groups.addAll(allSubGroups);
        groups = groups
            .stream()
            .distinct()
            .sorted(Comparator.comparing(GroupDetail::getName))
            .collect(Collectors.toList());
        if (paginationPage != null) {
          groups = paginationPage.getPaginatedListFrom(groups);
        }
      }
      return SilverpeasList.wrap(groups);
    }

    List<GroupDetail> getAllSubGroups(String fromGroupId) {
      return getSubGroups(fromGroupId);
    }

    private List<GroupDetail> getSubGroups(String groupId) {
      final List<GroupDetail> groups = new ArrayList<>();
      final List<GroupDetail> directSubGroups = getDirectSubGroups(groupId);
      for (final GroupDetail group : directSubGroups) {
        groups.add(group);
        groups.addAll(getSubGroups(group.getId()));
      }
      return groups;
    }

    private List<GroupDetail> getDirectSubGroups(final String groupId) {
      return subGroupsOfGroupsCache.computeIfAbsent(groupId, i -> {
        try {
          return groupDao.getDirectSubGroups(connection, groupId);
        } catch (SQLException e) {
          throw new SilverpeasRuntimeException(e);
        }
      });
    }
  }
}