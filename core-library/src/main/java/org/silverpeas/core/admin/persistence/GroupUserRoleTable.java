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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.user.dao.GroupDAO;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * A GroupUserRoleTable object manages the ST_GroupUserRole table.
 */
@Repository
public class GroupUserRoleTable extends Table<GroupUserRoleRow> {

  private static final String SELECT = "select ";
  private static final String SELECT_COUNT_GROUPUSERROLE_USER_REL =
      "select count(*) from ST_GroupUserRole_User_Rel"
          + " where userId = ? and groupUserRoleId = ?";
  private static final String GROUPUSERROLE_COLUMNS = "id,ST_GroupUserRole.groupId,roleName";
  private static final String SELECT_GROUPUSERROLE_BY_ID = SELECT
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where id = ?";
  private static final String SELECT_GROUPUSERROLE_BY_GROUPID = SELECT
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where groupId = ?";
  private static final String SELECT_USER_GROUPUSERROLES = SELECT + GROUPUSERROLE_COLUMNS +
      " from ST_GroupUserRole, ST_GroupUserRole_User_Rel where id = groupUserRoleId  and " +
      "ST_GroupUserRole_User_Rel.userId = ?";
  private static final String SELECT_GROUP_GROUPUSERROLES = SELECT + GROUPUSERROLE_COLUMNS +
      " from ST_GroupUserRole, ST_GroupUserRole_Group_Rel where id = groupUserRoleId  and " +
      "ST_GroupUserRole_Group_Rel.groupId = ?";
  private static final String INSERT_GROUPUSERROLE =
      "insert into ST_GroupUserRole(id,groupId,roleName) values (?,?,?)";
  private static final String DELETE_GROUPUSERROLE = "delete from ST_GroupUserRole where id = ?";
  private static final String INSERT_A_GROUPUSERROLE_USER_REL =
      "insert into ST_GroupUserRole_User_Rel(groupUserRoleId, userId) values(?,?)";
  private static final String DELETE_GROUPUSERROLE_USER_REL =
      "delete from ST_GroupUserRole_User_Rel where groupUserRoleId = ? and userId = ?";
  private static final String SELECT_COUNT_GROUPUSERROLE_GROUP_REL =
      "select count(*) from ST_GroupUserRole_Group_Rel"
          + " where groupId = ? and groupUserRoleId = ?";
  private static final String INSERT_A_GROUPUSERROLE_GROUP_REL =
      "insert into ST_GroupUserRole_Group_Rel(groupUserRoleId, groupId) values(?,?)";
  private static final String DELETE_GROUPUSERROLE_GROUP_REL =
      "delete from ST_GroupUserRole_Group_Rel where groupUserRoleId = ? and groupId = ?";

  @Inject
  private GroupDAO groupDAO;
  @Inject
  private UserDAO userDAO;

  GroupUserRoleTable() {
    super("ST_GroupUserRole");
  }

  /**
   * Fetch the current groupUserRole row from a resultSet.
   */
  protected GroupUserRoleRow fetchGroupUserRole(ResultSet rs) throws SQLException {
    GroupUserRoleRow sur = new GroupUserRoleRow();

    sur.id = rs.getInt(1);
    sur.groupId = rs.getInt(2);
    sur.roleName = rs.getString(3);
    return sur;
  }

  /**
   * Returns the GroupUserRole whith the given id.
   */
  public GroupUserRoleRow getGroupUserRole(int id) throws SQLException {
    return getUniqueRow(SELECT_GROUPUSERROLE_BY_ID, id);
  }

  /**
   * Returns the GroupUserRole whith the given groupId.
   */
  public GroupUserRoleRow getGroupUserRoleByGroupId(int groupId) throws SQLException {
    return getUniqueRow(SELECT_GROUPUSERROLE_BY_GROUPID, groupId);
  }

  /**
   * Returns all the direct GroupUserRoles of user.
   */
  public GroupUserRoleRow[] getDirectGroupUserRolesOfUser(int userId) throws SQLException {
    List<GroupUserRoleRow> rows = getRows(SELECT_USER_GROUPUSERROLES, userId);
    return rows.toArray(new GroupUserRoleRow[rows.size()]);
  }

  /**
   * Returns all the direct GroupUserRoles of a group.
   */
  public GroupUserRoleRow[] getDirectGroupUserRolesOfGroup(int groupId) throws SQLException {
    List<GroupUserRoleRow> rows = getRows(SELECT_GROUP_GROUPUSERROLES, groupId);
    return rows.toArray(new GroupUserRoleRow[rows.size()]);
  }

  /**
   * Inserts in the database a new groupUserRole row.
   */
  public void createGroupUserRole(GroupUserRoleRow groupUserRole) throws SQLException {
    checkGroupExistence(groupUserRole.groupId);
    insertRow(INSERT_GROUPUSERROLE, groupUserRole);
  }

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, GroupUserRoleRow usr)
      throws SQLException {
    if (usr.id == -1) {
      usr.id = getNextId();
    }

    insert.setInt(1, usr.id);
    insert.setInt(2, usr.groupId);
    if (!StringUtil.isDefined(usr.roleName)) {
      // column "rolename" is not null
      usr.roleName = "useless";
    }
    insert.setString(3, usr.roleName);
  }

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, GroupUserRoleRow row)
      throws SQLException {
    // nothing to do here
  }

  /**
   * Delete the groupUserRole
   */
  public void removeGroupUserRole(int id) throws SQLException {
    GroupUserRoleRow groupUserRole = getGroupUserRole(id);
    if (groupUserRole == null) {
      return;
    }

    try (Connection connection = DBUtil.openConnection()) {
      List<String> userIds =
          userDAO.getDirectUserIdsByGroupUserRole(connection, String.valueOf(id), true);
      for (String userId : userIds) {
        removeUserFromGroupUserRole(Integer.parseInt(userId), id);
      }

      List<String> groupIds =
          groupDAO.getDirectGroupIdsByGroupUserRole(connection, String.valueOf(id));
      for (String groupId : groupIds) {
        removeGroupFromGroupUserRole(Integer.valueOf(groupId), id);
      }


      updateRelation(DELETE_GROUPUSERROLE, id);
    }

  }

  /**
   * Tests if a user has a given role (not recursive).
   */
  private boolean isUserDirectlyInRole(int userId, int groupUserRoleId)
      throws SQLException {
    int[] ids = new int[] { userId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_GROUPUSERROLE_USER_REL, ids);

    return result != null && result >= 1;
  }

  /**
   * Add an user in a groupUserRole.
   */
  public void addUserInGroupUserRole(int userId, int groupUserRoleId) throws
      SQLException {
    if (isUserDirectlyInRole(userId, groupUserRoleId)) {
      return;
    }

    checkUserExistence(userId);

    GroupUserRoleRow groupUserRole = getGroupUserRole(groupUserRoleId);
    if (groupUserRole == null) {
      throw new SQLException(unknown("group role", String.valueOf(groupUserRoleId)));
    }

    int[] params = new int[] { groupUserRoleId, userId };
    updateRelation(INSERT_A_GROUPUSERROLE_USER_REL, params);
  }

  /**
   * Removes an user from a groupUserRole.
   */
  public void removeUserFromGroupUserRole(int userId, int groupUserRoleId)
      throws SQLException {
    if (!isUserDirectlyInRole(userId, groupUserRoleId)) {
      throw new SQLException(
          "user " + userId + " isn't in group role " + groupUserRoleId);
    }

    int[] params = new int[] { groupUserRoleId, userId };
    SynchroDomainReport.debug("GroupUserRoleTable.removeUserFromGroupUserRole()",
        "Retrait de l'utilisateur d'ID " + userId
        + " de role d'egroup d'ID " + groupUserRoleId + ", requête : "
        + DELETE_GROUPUSERROLE_USER_REL);
    updateRelation(DELETE_GROUPUSERROLE_USER_REL, params);
  }

  /**
   * Tests if a group has a given role (not recursive).
   */
  private boolean isGroupDirectlyInRole(int groupId, int groupUserRoleId)
      throws SQLException {
    int[] ids = new int[] { groupId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_GROUPUSERROLE_GROUP_REL, ids);

    return result != null && result >= 1;
  }

  /**
   * Adds a group in a groupUserRole.
   */
  public void addGroupInGroupUserRole(int groupId, int groupUserRoleId)
      throws SQLException {
    if (isGroupDirectlyInRole(groupId, groupUserRoleId)) {
      return;
    }

    checkGroupExistence(groupId);

    GroupUserRoleRow groupUserRole = getGroupUserRole(groupUserRoleId);
    if (groupUserRole == null) {
      throw new SQLException(unknown("group role", String.valueOf(groupUserRoleId)));
    }

    int[] params = new int[] { groupUserRoleId, groupId };
    updateRelation(INSERT_A_GROUPUSERROLE_GROUP_REL, params);
  }

  /**
   * Removes a group from a groupUserRole.
   */
  public void removeGroupFromGroupUserRole(int groupId, int groupUserRoleId)
      throws SQLException {
    if (!isGroupDirectlyInRole(groupId, groupUserRoleId)) {
      throw new SQLException(
          "The group " + groupId + " isn't in group role " + groupUserRoleId);
    }

    int[] params = new int[] { groupUserRoleId, groupId };
    SynchroDomainReport.debug("GroupUserRoleTable.removeGroupFromGroupUserRole()",
        "Retrait du groupe d'ID " + groupId + " de l'espace d'ID "
        + groupUserRoleId + ", requête : "
        + DELETE_GROUPUSERROLE_GROUP_REL);
    updateRelation(DELETE_GROUPUSERROLE_GROUP_REL, params);
  }

  /**
   * Fetch the current groupUserRole row from a resultSet.
   */
  @Override
  protected GroupUserRoleRow fetchRow(ResultSet rs) throws SQLException {
    return fetchGroupUserRole(rs);
  }

}
