/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.user.model.GroupCache;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.util.StringUtil.isDefined;

@Repository
public class GroupDAO {

  private static final String GROUP_TABLE = "ST_Group";
  private static final String GROUP_USERS_TABLE = "st_group_user_rel";
  private static final String GROUP_ROLE_TABLE = "st_groupuserrole";
  private static final String GROUP_ROLE_USERS_TABLE = "st_groupuserrole_user_rel";
  private static final String GROUP_ROLE_GROUPS_TABLE = "st_groupuserrole_group_rel";
  private static final String USER_ROLE_GROUPS_TABLE = "st_userrole_group_rel";
  private static final String SPACE_ROLE_GROUP = "st_spaceuserrole_group_rel";
  private static final String GROUP_COLUMNS =
      "DISTINCT(id),specificId,domainId,superGroupId,name,description,synchroRule";
  private static final String DOMAIN_ID_CRITERION = "domainId = ?";
  private static final String GROUP_ID_CRITERION = "groupId = ?";
  private static final String ID_CRITERION = "id = ?";
  private static final String USER_ID = "userId";
  private static final String GROUP_ID = "groupId";
  private static final String SPECIFIC_ID = "specificId";
  private static final String DOMAIN_ID = "domainId";
  private static final String SUPER_GROUP_ID = "superGroupId";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String SYNCHRO_RULE = "synchroRule";
  
  @Inject
  private GroupCache groupCache;

  protected GroupDAO() {
  }

  public String saveGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    Integer superGroupId = checkSuperGroup(connection, group);

    final int nextId = DBUtil.getNextId(GROUP_TABLE);
    String specificId =
        isDefined(group.getSpecificId()) ? group.getSpecificId() : String.valueOf(nextId);
    JdbcSqlQuery.createInsertFor(GROUP_TABLE)
        .addInsertParam("id", nextId)
        .addInsertParam(SPECIFIC_ID, specificId)
        .addInsertParam(DOMAIN_ID, getDomainIdOf(group))
        .addInsertParam(SUPER_GROUP_ID, superGroupId)
        .addInsertParam(NAME, group.getName())
        .addInsertParam(DESCRIPTION, group.getDescription())
        .addInsertParam(SYNCHRO_RULE, group.getRule())
        .executeWith(connection);

    return String.valueOf(nextId);
  }

  public void addUserInGroup(final Connection connection, final String userId, final String groupId)
      throws SQLException {
    checkUserExistence(connection, userId);

    checkGroupExistence(connection, groupId);

    JdbcSqlQuery.createInsertFor(GROUP_USERS_TABLE)
        .addInsertParam(GROUP_ID, Integer.parseInt(groupId))
        .addInsertParam(USER_ID, Integer.parseInt(userId))
        .executeWith(connection);

    groupCache.removeCacheOfUser(userId);
  }

  public void addUsersInGroup(final Connection connection, final List<String> userIds,
      final String groupId) throws SQLException {
    checkGroupExistence(connection, groupId);

    for (String userId : userIds) {
      checkUserExistence(connection, userId);

      JdbcSqlQuery.createInsertFor(GROUP_USERS_TABLE)
          .addInsertParam(GROUP_ID, Integer.parseInt(groupId))
          .addInsertParam(USER_ID, Integer.parseInt(userId))
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
    UserDAO userDAO = ServiceProvider.getSingleton(UserDAO.class);
    if (!userDAO.isUserByIdExists(connection, userId)) {
      throw new SQLException(unknown("user", userId));
    }
  }

  public void updateGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    Integer superGroupId = checkSuperGroup(connection, group);

    String specificId = isDefined(group.getSpecificId()) ? group.getSpecificId() : group.getId();
    JdbcSqlQuery.createUpdateFor(GROUP_TABLE)
        .addUpdateParam(SPECIFIC_ID, specificId)
        .addUpdateParam(DOMAIN_ID, getDomainIdOf(group))
        .addUpdateParam(SUPER_GROUP_ID, superGroupId)
        .addUpdateParam(NAME, group.getName())
        .addUpdateParam(DESCRIPTION, group.getDescription())
        .addUpdateParam(SYNCHRO_RULE, isDefined(group.getRule()) ? group.getRule() : null)
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

  public void deleteGroup(final Connection connection, final GroupDetail group)
      throws SQLException {
    JdbcSqlQuery.createDeleteFor(GROUP_TABLE)
        .where(ID_CRITERION, Integer.parseInt(group.getId()))
        .executeWith(connection);
  }

  public void deleteUserInGroup(final Connection connection, final String userId,
      final String groupdId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(GROUP_USERS_TABLE)
        .where("userId = ?", Integer.parseInt(userId))
        .and(GROUP_ID_CRITERION, Integer.parseInt(groupdId))
        .executeWith(connection);
    groupCache.removeCacheOfUser(userId);
  }

  public boolean isGroupByNameExists(final Connection connection, final String name)
      throws SQLException {
    return JdbcSqlQuery.createSelect("COUNT(id)")
        .from(GROUP_TABLE)
        .where("name like ?", name)
        .executeUniqueWith(connection, rs -> rs.getInt(1)) > 0;
  }

  /**
   * Gets all the user groups available in Silverpeas whatever the user domain they belongs to.
   *
   * @param connection the connection with the data source to use.
   * @return a list of user groups.
   * @throws SQLException if an error occurs while getting the user groups from the data source.
   */
  public List<GroupDetail> getAllGroups(Connection connection) throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Gets all root groups available in Silverpeas whatever the user domain they belongs to.
   * @param connection the connection with the data source to use.
   * @return a list of user groups.
   * @throws SQLException if an error occurs while getting the user groups from the data source.
   */
  public List<GroupDetail> getAllRootGroups(Connection connection) throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where("superGroupId is null")
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the Root Groups having a given domain id.
   * @param domainId domain id
   * @return all the Root Groups having a given domain id.
   * @throws SQLException
   */
  public List<GroupDetail> getAllRootGroupsByDomainId(final Connection connection,
      final String domainId) throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and("superGroupId is null")
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the Groups having a given domain id.
   * @param connection connection to the data source.
   * @param domainId domain id
   * @return all the Groups having a given domain id.
   * @throws SQLException
   */
  public List<GroupDetail> getAllGroupsByDomainId(final Connection connection,
      final String domainId) throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .orderBy(NAME)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns the parent of the given group if any.
   * @param connection connection to the data source.
   * @param groupId a group id
   * @return the parent of the specified group or null.
   * @throws SQLException
   */
  public GroupDetail getSuperGroup(Connection connection, String groupId) throws SQLException {
    return JdbcSqlQuery.createSelect(
        "sg.id,sg.specificId,sg.domainId,sg.superGroupId,sg.name,sg.description,sg.synchroRule")
        .from(GROUP_TABLE + " sg", GROUP_TABLE + " g")
        .where("sg.id = g.superGroupId")
        .and("g.id = ?", Integer.parseInt(groupId))
        .executeUniqueWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Gets the user groups that match the specified criteria. The criteria are provided by a
   * SearchCriteriaBuilder instance that was used to create them.
   *
   * @param connection the connetion with a data source to use.
   * @param criteria a builder with which the criteria the user groups must satisfy has been built.
   * @return a list slice of user groups matching the criteria or an empty list if no such user groups are
   * found. The slice is set by the pagination criteriion. If no such criterion is provided, then it
   * is the whole list of groups matching the other criteria.
   */
  public ListSlice<GroupDetail> getGroupsByCriteria(Connection connection,
      GroupsSearchCriteria criteria) throws SQLException {
    return new SqlGroupSelectorByCriteriaBuilder(GROUP_COLUMNS)
        .build(criteria)
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  public GroupDetail getGroupBySpecificId(final Connection connection, final String domainId,
      final String specificId) throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and("specificId = ?", specificId)
        .executeUniqueWith(connection, GroupDAO::fetchGroup);
  }

  public GroupDetail getGroup(Connection con, String groupId)
      throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where(ID_CRITERION, Integer.parseInt(groupId))
        .executeUniqueWith(con, GroupDAO::fetchGroup);
  }

  public List<GroupDetail> getDirectSubGroups(Connection con, String groupId) throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where("superGroupId = ?", Integer.parseInt(groupId))
        .executeWith(con, GroupDAO::fetchGroup);
  }

  public int getNBUsersDirectlyInGroup(Connection con, String groupId) throws SQLException {
    return JdbcSqlQuery.createSelect("COUNT(userId)")
        .from(GROUP_USERS_TABLE)
        .where(GROUP_ID_CRITERION, Integer.parseInt(groupId))
        .executeUniqueWith(con, rs -> rs.getInt(1));
  }

  public List<String> getUsersDirectlyInGroup(Connection con, String groupId) throws SQLException {
    return JdbcSqlQuery.createSelect(USER_ID)
        .from(GROUP_USERS_TABLE)
        .where(GROUP_ID_CRITERION, Integer.parseInt(groupId))
        .executeWith(con, rs -> String.valueOf(rs.getInt(1)));
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
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE)
        .where("synchroRule is not null")
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the groups of a given user (not recursive).
   * @param userId user id
   * @return all the groups of a given user (not recursive).
   * @throws SQLException
   */
  public List<GroupDetail> getDirectGroupsOfUser(final Connection connection, final String userId)
      throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_COLUMNS)
        .from(GROUP_TABLE, GROUP_USERS_TABLE)
        .where("id = groupId")
        .and("userId = ?", Integer.parseInt(userId))
        .executeWith(connection, GroupDAO::fetchGroup);
  }

  /**
   * Returns all the identifiers of the groups that are in the specified user role (not recursive).
   * @param userRoleId user role id
   * @return all the group identifiers.
   * @throws SQLException
   */
  public List<String> getDirectGroupIdsByUserRole(Connection connection, String userRoleId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(GROUP_TABLE, USER_ROLE_GROUPS_TABLE)
        .where("id = groupid")
        .and("userroleid = ?", Integer.parseInt(userRoleId))
        .executeWith(connection, rs -> String.valueOf(rs.getInt(1)));
  }

  public List<String> getDirectGroupIdsBySpaceUserRole(Connection connection,
      String spaceUserRoleId) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(GROUP_TABLE, SPACE_ROLE_GROUP)
        .where("groupId = id")
        .and("spaceUserRoleId = ?", Integer.parseInt(spaceUserRoleId))
        .executeWith(connection, rs -> String.valueOf(rs.getInt(1)));
  }

  public List<String> getDirectGroupIdsByGroupUserRole(Connection connection,
      String groupUserRoleId) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(GROUP_TABLE, GROUP_ROLE_GROUPS_TABLE)
        .where("id = groupId")
        .and("groupUserRoleId = ?", Integer.parseInt(groupUserRoleId))
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public GroupDetail getGroupByGroupUserRole(Connection connection, String groupUserRoleId)
      throws SQLException {
    return JdbcSqlQuery.createSelect(
        "g.id,g.specificId,g.domainId,g.superGroupId,g.name,g.description,g.synchroRule")
        .from(GROUP_TABLE + " g", GROUP_ROLE_TABLE + " gr")
        .where("g.id = gr.groupId")
        .and("gr.id = ?", Integer.parseInt(groupUserRoleId))
        .executeUniqueWith(connection, GroupDAO::fetchGroup);
  }

  private List<String> getManageableGroupIdsByUser(Connection con, String userId)
      throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_ROLE_TABLE + ".groupid")
        .from(GROUP_ROLE_USERS_TABLE, GROUP_ROLE_TABLE)
        .where(GROUP_ROLE_USERS_TABLE + ".groupuserroleid = " + GROUP_ROLE_TABLE + ".id")
        .and(GROUP_ROLE_USERS_TABLE + ".userId = ?", Integer.parseInt(userId))
        .executeWith(con, rs -> String.valueOf(rs.getInt(1)));
  }

  private List<String> getManageableGroupIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    return JdbcSqlQuery.createSelect(GROUP_ROLE_TABLE + ".groupid")
        .from(GROUP_ROLE_GROUPS_TABLE, GROUP_ROLE_TABLE)
        .where(GROUP_ROLE_GROUPS_TABLE + ".groupuserroleid = " + GROUP_ROLE_TABLE + ".id")
        .and(GROUP_ROLE_GROUPS_TABLE + ".groupId")
        .in(groupIds.stream().map(Integer::parseInt).collect(Collectors.toList()))
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
    group.setRule(rs.getString(SYNCHRO_RULE));
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
}
