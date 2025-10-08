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
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.GroupCache;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

@Repository
public class GroupDAO {

  private static final String GROUP_TABLE = "ST_Group";
  private static final String GROUP_USERS_TABLE = "st_group_user_rel";
  private static final String GROUP_ROLE_TABLE = "st_groupuserrole";
  private static final String GROUP_ROLE_USERS_TABLE = "st_groupuserrole_user_rel";
  private static final String GROUP_ROLE_GROUPS_TABLE = "st_groupuserrole_group_rel";
  private static final String USER_ROLE_GROUPS_TABLE = "st_userrole_group_rel";
  private static final String SPACE_ROLE_GROUP = "st_spaceuserrole_group_rel";
  private static final String GROUP_COLUMNS_PATTERN =
      "DISTINCT({0}id),{0}specificId,{0}domainId,{0}instanceId,{0}superGroupId,{0}name," +
          "{0}description,{0}synchroRule,{0}creationDate,{0}saveDate,{0}state,{0}stateSaveDate";
  private static final String GROUP_COLUMNS = format(GROUP_COLUMNS_PATTERN, EMPTY);
  private static final String SEARCH_GROUP_COLUMNS = format(GROUP_COLUMNS_PATTERN, "g.");
  private static final String DOMAIN_ID_CRITERION = "domainId = ?";
  private static final String GROUP_ID_CRITERION = "groupId = ?";
  private static final String STATE_CRITERION = "state = ?";
  private static final String ID_CRITERION = "id = ?";
  private static final String USER_ID = "userId";
  private static final String GROUP_ID = "groupId";
  private static final String GROUP_ID_ATTR = ".groupid";
  private static final String SPECIFIC_ID = "specificId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String DOMAIN_ID = "domainId";
  private static final String SUPER_GROUP_ID = "superGroupId";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String SYNCHRO_RULE = "synchroRule";
  private static final String CREATION_DATE = "creationDate";
  private static final String SAVE_DATE = "saveDate";
  private static final String STATE = "state";
  private static final String STATE_SAVE_DATE = "stateSaveDate";

  @Inject
  private GroupCache groupCache;

  protected GroupDAO() {
  }

  public String addGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    final Integer superGroupId = checkSuperGroup(connection, group);
    final int nextId = DBUtil.getNextId(GROUP_TABLE);
    final String specificId = isDefined(group.getSpecificId()) ?
        group.getSpecificId() :
        String.valueOf(nextId);
    final Instant now = new Date().toInstant();
    if(GroupState.UNKNOWN.equals(group.getState())) {
      group.setState(GroupState.VALID);
    }
    JdbcSqlQuery.insertInto(GROUP_TABLE)
        .withInsertParam("id", nextId)
        .withInsertParam(SPECIFIC_ID, specificId)
        .withInsertParam(INSTANCE_ID, group.getInstanceId())
        .withInsertParam(DOMAIN_ID, getDomainIdOf(group))
        .withInsertParam(SUPER_GROUP_ID, superGroupId)
        .withInsertParam(NAME, group.getName())
        .withInsertParam(DESCRIPTION, group.getDescription())
        .withInsertParam(SYNCHRO_RULE, group.getRule())
        .withInsertParam(CREATION_DATE, now)
        .withInsertParam(SAVE_DATE, now)
        .withInsertParam(STATE, group.getState())
        .withInsertParam(STATE_SAVE_DATE, now)
        .executeWith(connection);
    return String.valueOf(nextId);
  }

  public Optional<GroupDetail> restoreGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    final Date now = new Date();
    final Instant nowI = now.toInstant();
    GroupDetail restored = null;
    final long nbRestored = JdbcSqlQuery.update(GROUP_TABLE)
        .withUpdateParam(STATE, GroupState.VALID)
        .withUpdateParam(STATE_SAVE_DATE, nowI)
        .withUpdateParam(SAVE_DATE, nowI)
        .where(ID_CRITERION, Integer.parseInt(group.getId()))
        .and(STATE).in(GroupState.REMOVED)
        .executeWith(connection);
    if (nbRestored > 0) {
      groupCache.clearCache();
      restored = new GroupDetail(group);
      restored.setSaveDate(now);
      restored.setState(GroupState.VALID);
      restored.setStateSaveDate(now);
    }
    return ofNullable(restored);
  }

  public Optional<GroupDetail> removeGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    final Date now = new Date();
    final Instant nowI = now.toInstant();
    GroupDetail removed = null;
    final long nbRemoved = JdbcSqlQuery.update(GROUP_TABLE)
        .withUpdateParam(STATE, GroupState.REMOVED)
        .withUpdateParam(STATE_SAVE_DATE, nowI)
        .withUpdateParam(SAVE_DATE, nowI)
        .where(ID_CRITERION, Integer.parseInt(group.getId()))
        .and(STATE).notIn(GroupState.REMOVED)
        .executeWith(connection);
    if (nbRemoved > 0) {
      groupCache.clearCache();
      removed = new GroupDetail(group);
      removed.setSaveDate(now);
      removed.setState(GroupState.REMOVED);
      removed.setStateSaveDate(now);
    }
    return ofNullable(removed);
  }

  public long deleteGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    final long nbDeleted = JdbcSqlQuery.deleteFrom(GROUP_TABLE)
        .where(ID_CRITERION, Integer.parseInt(group.getId()))
        .executeWith(connection);
    if (nbDeleted > 0) {
      groupCache.clearCache();
    }
    return nbDeleted;
  }

  /**
   * Gets all the groups that were removed in the specified domains.
   * @param connection a connection to the data source.
   * @param domainIds zero, one or more unique identifiers of Silverpeas domains. If no domains
   * are passed, then all the domains are taken by the request.
   * @return a list of group details.
   * @throws SQLException if an error while requesting the groups.
   */
  public List<GroupDetail> getRemovedGroups(final Connection connection, final String... domainIds)
      throws SQLException {
    Objects.requireNonNull(connection);
    Objects.requireNonNull(domainIds);
    final JdbcSqlQuery query = JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(STATE_CRITERION, GroupState.REMOVED);
    final List<Integer> requestedDomainIds =
        Stream.of(domainIds).map(Integer::parseInt).collect(toList());
    if (!requestedDomainIds.isEmpty()) {
      query.and(DOMAIN_ID).in(requestedDomainIds);
    }
    return query.executeWith(connection, GroupDAO::fetchGroup);
  }

  public void addUserInGroup(final Connection connection, final String userId, final String groupId)
      throws SQLException {
    checkUserExistence(connection, userId);

    checkGroupExistence(connection, groupId);

    JdbcSqlQuery.insertInto(GROUP_USERS_TABLE)
        .withInsertParam(GROUP_ID, Integer.parseInt(groupId))
        .withInsertParam(USER_ID, Integer.parseInt(userId))
        .executeWith(connection);

    groupCache.removeCacheOfUser(userId);
  }

  public void addUsersInGroup(final Connection connection, final List<String> userIds,
      final String groupId) throws SQLException {
    checkGroupExistence(connection, groupId);

    for (String userId : userIds) {
      checkUserExistence(connection, userId);

      JdbcSqlQuery.insertInto(GROUP_USERS_TABLE)
          .withInsertParam(GROUP_ID, Integer.parseInt(groupId))
          .withInsertParam(USER_ID, Integer.parseInt(userId))
          .executeWith(connection);

      groupCache.removeCacheOfUser(userId);
    }
  }

  private void checkGroupExistence(final Connection connection, final String groupId)
      throws SQLException {
    if (getGroup(connection, groupId) == null) {
      throw new SQLException(unknown("group", groupId));
    }
  }

  private void checkUserExistence(final Connection connection, final String userId)
      throws SQLException {
    UserDAO userDAO = ServiceProvider.getService(UserDAO.class);
    if (!userDAO.isUserByIdExists(connection, userId)) {
      throw new SQLException(unknown("user", userId));
    }
  }

  public void updateGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    final Instant now = new Date().toInstant();
    final Integer superGroupId = checkSuperGroup(connection, group);
    String specificId = isDefined(group.getSpecificId()) ? group.getSpecificId() : group.getId();
    JdbcSqlQuery.update(GROUP_TABLE)
        .withUpdateParam(SPECIFIC_ID, specificId)
        .withUpdateParam(DOMAIN_ID, getDomainIdOf(group))
        .withUpdateParam(SUPER_GROUP_ID, superGroupId)
        .withUpdateParam(NAME, group.getName())
        .withUpdateParam(DESCRIPTION, group.getDescription())
        .withUpdateParam(SYNCHRO_RULE, isDefined(group.getRule()) ? group.getRule() : null)
        .withUpdateParam(SAVE_DATE, now)
        .withUpdateParam(STATE, group.getState())
        .withUpdateParam(STATE_SAVE_DATE, toInstance(group.getStateSaveDate()))
        .where(ID_CRITERION, Integer.parseInt(group.getId()))
        .executeWith(connection);
  }

  private Integer checkSuperGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    Integer superGroupId = null;
    if (isDefined(group.getSuperGroupId())) {
      superGroupId = Integer.parseInt(group.getSuperGroupId());
      if (superGroupId >= 0 && getGroup(connection, group.getSuperGroupId()) == null) {
        throw new SQLException(unknown("parent group", superGroupId));
      }
    }
    return superGroupId;
  }

  public void deleteUserInGroup(final Connection connection, final String userId,
      final String groupdId) throws SQLException {
    JdbcSqlQuery.deleteFrom(GROUP_USERS_TABLE)
        .where("userId = ?", Integer.parseInt(userId))
        .and(GROUP_ID_CRITERION, Integer.parseInt(groupdId))
        .executeWith(connection);
    groupCache.removeCacheOfUser(userId);
  }

  public boolean isGroupByNameExists(final Connection connection, final String name)
      throws SQLException {
    return JdbcSqlQuery.select("COUNT(id)")
        .from(GROUP_TABLE)
        .where("name like ?", name)
        .and(STATE).notIn(GroupState.REMOVED)
        .executeUniqueWith(connection, rs -> rs.getInt(1)) > 0;
  }

  /**
   * Gets all {@link GroupState#VALID}  groups available in Silverpeas whatever the user domain
   * they belongs to.
   * @param connection the connection with the data source to use.
   * @return a list of user groups.
   * @throws SQLException if an error occurs while getting the user groups from the data source.
   */
  public List<GroupDetail> getAllGroups(Connection connection) throws SQLException {
    return JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(STATE).notIn(GroupState.REMOVED)
        .andNull(INSTANCE_ID)
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Gets all {@link GroupState#VALID} root groups available in Silverpeas whatever the user
   * domain they belongs to.
   * @param connection the connection with the data source to use.
   * @return a list of user groups.
   * @throws SQLException if an error occurs while getting the user groups from the data source.
   */
  public List<GroupDetail> getAllRootGroups(Connection connection) throws SQLException {
    return JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where("superGroupId is null")
        .and(STATE).notIn(GroupState.REMOVED)
        .andNull(INSTANCE_ID)
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the Root Groups having a given domain id.
   * @param domainId domain id
   * @return all the Root Groups having a given domain id.
   * @throws SQLException if an error occurs
   */
  public List<GroupDetail> getAllRootGroupsByDomainId(final Connection connection,
      final String domainId) throws SQLException {
    return JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and("superGroupId is null")
        .and(STATE).notIn(GroupState.REMOVED)
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the Groups having a given domain id.
   * @param connection connection to the data source.
   * @param domainId domain id
   * @param includeRemoved true to include REMOVED from the result, false otherwise
   * @return all the Groups having a given domain id.
   * @throws SQLException if an error occurs
   */
  public List<GroupDetail> getAllGroupsByDomainId(final Connection connection,
      final String domainId, final boolean includeRemoved) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId));
    if (!includeRemoved) {
      query.and(STATE).notIn(GroupState.REMOVED);
    }
    return query
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns the parent of the given group if any.
   * @param connection connection to the data source.
   * @param groupId a group id
   * @return the parent of the specified group or null.
   * @throws SQLException if an error occurs
   */
  public GroupDetail getSuperGroup(Connection connection, String groupId) throws SQLException {
    return JdbcSqlQuery.select(format(GROUP_COLUMNS_PATTERN, "sg."))
        .from(GROUP_TABLE + " sg", GROUP_TABLE + " g")
        .where("sg.id = g.superGroupId")
        .and("g.id = ?", Integer.parseInt(groupId))
        .executeUniqueWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Gets the user groups that match the specified criteria. The criteria are provided by a
   * SearchCriteriaBuilder instance that was used to create them.
   * <p>
   *   IMPORTANT: returned groups are {@link GroupState#VALID} ones.
   * </p>
   * @param connection the connection with a data source to use.
   * @param criteria a builder with which the criteria the user groups must satisfy has been built.
   * @return a list slice of user groups matching the criteria or an empty list if no such user groups are
   * found. The slice is set by the pagination criterion. If no such criterion is provided, then it
   * is the whole list of groups matching the other criteria.
   */
  public ListSlice<GroupDetail> getGroupsByCriteria(Connection connection,
      GroupsSearchCriteria criteria) throws SQLException {
    return new SqlGroupSelectorByCriteriaBuilder(SEARCH_GROUP_COLUMNS)
        .build(criteria)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  public Map<String, Set<String>> getRolesByGroupsMappingWith(Connection connection,
      final List<String> groupIds, final String[] profileIds) throws SQLException {
    final Map<String, Set<String>> rolesByGroup = new HashMap<>(groupIds.size());
    JdbcSqlQuery.executeBySplittingOn(groupIds, (idBatch, ignore) ->
        JdbcSqlQuery.select("urgr.groupid, ur.rolename")
            .from(USER_ROLE_GROUPS_TABLE + " urgr")
            .join("st_userrole ur").on("ur.id = urgr.userroleid")
            .where("urgr.groupid").in(idBatch.stream().map(Integer::parseInt).collect(toList()))
            .and("ur.id").in(Stream.of(profileIds).map(Integer::parseInt).collect(toList()))
            .executeWith(connection, r -> {
              final String groupId = Integer.toString(r.getInt(1));
              final Set<String> roles = rolesByGroup.computeIfAbsent(groupId, k -> new HashSet<>());
              roles.add(r.getString(2));
              return null;
            }));
    return rolesByGroup;
  }

  public GroupDetail getGroupBySpecificId(final Connection connection, final String domainId,
      final String specificId) throws SQLException {
    return JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and("specificId = ?", specificId)
        .executeUniqueWith(connection, GroupDAO::fetchGroup);
  }

  public GroupDetail getGroup(Connection con, String groupId)
      throws SQLException {
    return JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(ID_CRITERION, Integer.parseInt(groupId))
        .executeUniqueWith(con, GroupDAO::fetchGroup);
  }

  public List<GroupDetail> getDirectSubGroups(Connection con, String groupId,
      final boolean includeRemoved) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where("superGroupId = ?", Integer.parseInt(groupId));
    if (!includeRemoved) {
      query.and(STATE).notIn(GroupState.REMOVED);
    }
    return query.executeWith(con, GroupDAO::fetchGroup);
  }

  public List<String> getManageableGroupIds(Connection con, String userId,
      List<String> groupIds) throws SQLException {
    List<String> manageableGroupIds = new ArrayList<>();
    if (isDefined(userId)) {
      manageableGroupIds.addAll(getManageableGroupIdsByUser(con, userId));
    }
    if (groupIds != null && !groupIds.isEmpty()) {
      manageableGroupIds.addAll(getManageableGroupIdsByGroups(con, groupIds));
    }
    return manageableGroupIds;
  }

  public List<GroupDetail> getSynchronizedGroups(final Connection connection) throws SQLException {
    return JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where("synchroRule is not null")
        .and(STATE).notIn(GroupState.REMOVED)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the groups of a given user (not recursive).
   * @param userId user id
   * @param includeRemoved true to include REMOVED from the result, false otherwise
   * @return all the groups of a given user (not recursive).
   * @throws SQLException on technical error with database.
   */
  public List<GroupDetail> getDirectGroupsOfUser(final Connection connection, final String userId,
      final boolean includeRemoved)
      throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.select(GROUP_COLUMNS)
        .from(GROUP_TABLE, GROUP_USERS_TABLE)
        .where("id = groupId")
        .and("userId = ?", Integer.parseInt(userId));
    if (!includeRemoved) {
      query.and(STATE).notIn(GroupState.REMOVED);
    }
    return query.executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the identifiers of the groups that are in the specified user role (not recursive).
   * @param userRoleId user role id
   * @param includeRemoved true to take into account removed groups, false otherwise.
   * @return all the group identifiers.
   * @throws SQLException on technical error with database.
   */
  public List<String> getDirectGroupIdsByUserRole(Connection connection, String userRoleId,
      final boolean includeRemoved)
      throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.select("id")
        .from(GROUP_TABLE, USER_ROLE_GROUPS_TABLE)
        .where("id = groupid")
        .and("userroleid = ?", Integer.parseInt(userRoleId));
    if (!includeRemoved) {
      query.and(STATE).notIn(GroupState.REMOVED);
    }
    return query.executeWith(connection, rs -> String.valueOf(rs.getInt(1)));
  }

  public List<String> getDirectGroupIdsBySpaceUserRole(Connection connection,
      String spaceUserRoleId, final boolean includeRemoved) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.select("id")
        .from(GROUP_TABLE, SPACE_ROLE_GROUP)
        .where("groupId = id")
        .and("spaceUserRoleId = ?", Integer.parseInt(spaceUserRoleId));
    if (!includeRemoved) {
      query.and(STATE).notIn(GroupState.REMOVED);
    }
    return query.executeWith(connection, rs -> String.valueOf(rs.getInt(1)));
  }

  public List<String> getDirectGroupIdsByGroupUserRole(Connection connection,
      String groupUserRoleId, final boolean includeRemoved) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.select("id")
        .from(GROUP_TABLE, GROUP_ROLE_GROUPS_TABLE)
        .where("id = groupId")
        .and("groupUserRoleId = ?", Integer.parseInt(groupUserRoleId));
    if (!includeRemoved) {
      query.and(STATE).notIn(GroupState.REMOVED);
    }
    return query.executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  private List<String> getManageableGroupIdsByUser(Connection con, String userId)
      throws SQLException {
    return JdbcSqlQuery.select(GROUP_ROLE_TABLE + GROUP_ID_ATTR)
        .from(GROUP_ROLE_USERS_TABLE, GROUP_ROLE_TABLE, GROUP_TABLE)
        .where(GROUP_ROLE_USERS_TABLE + ".groupuserroleid = " + GROUP_ROLE_TABLE + ".id")
        .and(GROUP_TABLE + ".id = " + GROUP_ROLE_TABLE + GROUP_ID_ATTR)
        .and(GROUP_ROLE_USERS_TABLE + ".userId = ?", Integer.parseInt(userId))
        .and(STATE).notIn(GroupState.REMOVED)
        .executeWith(con, rs -> String.valueOf(rs.getInt(1)));
  }

  private List<String> getManageableGroupIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    return JdbcSqlQuery.select(GROUP_ROLE_TABLE + GROUP_ID_ATTR)
        .from(GROUP_ROLE_GROUPS_TABLE, GROUP_ROLE_TABLE, GROUP_TABLE)
        .where(GROUP_ROLE_GROUPS_TABLE + ".groupuserroleid = " + GROUP_ROLE_TABLE + ".id")
        .and(GROUP_TABLE + ".id = " + GROUP_ROLE_TABLE + GROUP_ID_ATTR)
        .and(GROUP_ROLE_GROUPS_TABLE + GROUP_ID_ATTR)
        .in(groupIds.stream().map(Integer::parseInt).collect(toList()))
        .and(STATE).notIn(GroupState.REMOVED)
        .executeWith(con, rs -> String.valueOf(rs.getInt(1)));
  }

  /**
   * Fetch the current group row from a resultSet.
   */
  private static GroupDetail fetchGroup(ResultSet rs) throws SQLException {
    GroupDetail group = new GroupDetail();
    group.setId(Integer.toString(rs.getInt("id")));
    group.setSpecificId(rs.getString(SPECIFIC_ID));
    if ("-1".equals(group.getSpecificId())) {
      group.setSpecificId(null);
    }
    final int domainId = rs.getInt(DOMAIN_ID);
    if (domainId != -1) {
      // The domain is not the MIXED one
      group.setDomainId(Integer.toString(domainId));
    }
    group.setSuperGroupId(Integer.toString(rs.getInt(SUPER_GROUP_ID)));
    if (rs.wasNull()) {
      group.setSuperGroupId(null);
    }
    group.setName(rs.getString(NAME));
    group.setDescription(rs.getString(DESCRIPTION));
    group.setInstanceId(rs.getString(INSTANCE_ID));
    group.setRule(rs.getString(SYNCHRO_RULE));
    group.setCreationDate(rs.getTimestamp(CREATION_DATE));
    group.setSaveDate(rs.getTimestamp(SAVE_DATE));
    group.setState(GroupState.from(rs.getString(STATE)));
    group.setStateSaveDate(rs.getTimestamp(STATE_SAVE_DATE));
    return group;
  }

  private int getDomainIdOf(final GroupDetail group) {
    final String domainId = group.getDomainId();
    if (StringUtil.isNotDefined(domainId)) {
      // Case of MIXED domain
      return -1;
    }
    return Integer.parseInt(domainId);
  }

  private Instant toInstance(final Date aDate) {
    return aDate == null ? null : aDate.toInstant();
  }
}
