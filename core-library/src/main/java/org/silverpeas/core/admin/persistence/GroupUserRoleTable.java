/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A GroupUserRoleTable object manages the ST_GroupUserRole table.
 */
public class GroupUserRoleTable extends Table<GroupUserRoleRow> {

  public GroupUserRoleTable(OrganizationSchema organization) {
    super(organization, "ST_GroupUserRole");
    this.organization = organization;
  }

  static final private String GROUPUSERROLE_COLUMNS = "id,groupId,roleName";

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
  public GroupUserRoleRow getGroupUserRole(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_GROUPUSERROLE_BY_ID, id);
  }

  static final private String SELECT_GROUPUSERROLE_BY_ID = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where id = ?";

  /**
   * Returns the GroupUserRole whith the given groupId.
   */
  public GroupUserRoleRow getGroupUserRoleByGroupId(int groupId) throws AdminPersistenceException {
    return getUniqueRow(SELECT_GROUPUSERROLE_BY_GROUPID, groupId);
  }

  static final private String SELECT_GROUPUSERROLE_BY_GROUPID = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where groupId = ?";

  /**
   * Inserts in the database a new groupUserRole row.
   */
  public void createGroupUserRole(GroupUserRoleRow groupUserRole) throws AdminPersistenceException {
    GroupRow group = organization.group.getGroup(groupUserRole.groupId);
    if (group == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.createGroupUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", "group id : '"
          + groupUserRole.groupId + "'");
    }

    insertRow(INSERT_GROUPUSERROLE, groupUserRole);
    /*
     * organization.userSet.createUserSet("H", groupUserRole.id);
     * organization.userSet.addUserSetInUserSet("H", groupUserRole.id, "G", groupUserRole.groupId);
     */
  }

  static final private String INSERT_GROUPUSERROLE =
      "insert into ST_GroupUserRole(id,groupId,roleName)"
      + " values     (? 	,?       ,?)";

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
  }

  /**
   * Delete the groupUserRole
   */
  public void removeGroupUserRole(int id) throws AdminPersistenceException {
    GroupUserRoleRow groupUserRole = getGroupUserRole(id);
    if (groupUserRole == null) {
      return;
    }

    UserRow[] users = organization.user.getDirectUsersOfGroupUserRole(id);
    for (UserRow user : users) {
      removeUserFromGroupUserRole(user.id, id);
    }

    GroupRow[] groups = organization.group.getDirectGroupsInGroupUserRole(id);
    for (GroupRow group : groups) {
      removeGroupFromGroupUserRole(group.id, id);
    }

    // organization.userSet.removeUserSet("H", id);
    updateRelation(DELETE_GROUPUSERROLE, id);
  }

  static final private String DELETE_GROUPUSERROLE = "delete from ST_GroupUserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   */
  private boolean isUserDirectlyInRole(int userId, int groupUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_GROUPUSERROLE_USER_REL, ids);

    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_GROUPUSERROLE_USER_REL =
      "select count(*) from ST_GroupUserRole_User_Rel"
      + " where userId = ? and groupUserRoleId = ?";

  /**
   * Add an user in a groupUserRole.
   */
  public void addUserInGroupUserRole(int userId, int groupUserRoleId) throws
      AdminPersistenceException {
    if (isUserDirectlyInRole(userId, groupUserRoleId)) {
      return;
    }

    UserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.addUserInGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
          "user id : '" + userId + "'");
    }

    GroupUserRoleRow groupUserRole = getGroupUserRole(groupUserRoleId);
    if (groupUserRole == null) {
      throw new AdminPersistenceException("GroupUserRoleTable.addUserInGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUPUSERROLE_NOT_FOUND",
          "group user role id : '" + groupUserRoleId + "'");
    }

    int[] params = new int[] { groupUserRoleId, userId };
    updateRelation(INSERT_A_GROUPUSERROLE_USER_REL, params);
  }

  static final private String INSERT_A_GROUPUSERROLE_USER_REL =
      "insert into ST_GroupUserRole_User_Rel(groupUserRoleId, userId) values(?,?)";

  /**
   * Removes an user from a groupUserRole.
   */
  public void removeUserFromGroupUserRole(int userId, int groupUserRoleId)
      throws AdminPersistenceException {
    if (!isUserDirectlyInRole(userId, groupUserRoleId)) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.removeUserFromGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_GROUP_USERROLE",
          "group userrole id: '" + groupUserRoleId + "', user id: '" + userId
          + "'");
    }

    int[] params = new int[] { groupUserRoleId, userId };
    SynchroDomainReport.debug("GroupUserRoleTable.removeUserFromGroupUserRole()",
        "Retrait de l'utilisateur d'ID " + userId
        + " de role d'egroup d'ID " + groupUserRoleId + ", requête : "
        + DELETE_GROUPUSERROLE_USER_REL);
    updateRelation(DELETE_GROUPUSERROLE_USER_REL, params);
  }

  static final private String DELETE_GROUPUSERROLE_USER_REL =
      "delete from ST_GroupUserRole_User_Rel where groupUserRoleId = ? and userId = ?";

  /**
   * Tests if a group has a given role (not recursive).
   */
  private boolean isGroupDirectlyInRole(int groupId, int groupUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_GROUPUSERROLE_GROUP_REL, ids);

    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_GROUPUSERROLE_GROUP_REL =
      "select count(*) from ST_GroupUserRole_Group_Rel"
      + " where groupId = ? and groupUserRoleId = ?";

  /**
   * Adds a group in a groupUserRole.
   */
  public void addGroupInGroupUserRole(int groupId, int groupUserRoleId)
      throws AdminPersistenceException {
    if (isGroupDirectlyInRole(groupId, groupUserRoleId)) {
      return;
    }

    GroupRow group = organization.group.getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.addGroupInGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group id : '" + groupId + "'");
    }

    GroupUserRoleRow groupUserRole = getGroupUserRole(groupUserRoleId);
    if (groupUserRole == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.addGroupInGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUPUSERROLE_NOT_FOUND",
          "group userrole id : '" + groupUserRoleId + "'");
    }

    int[] params = new int[] { groupUserRoleId, groupId };
    updateRelation(INSERT_A_GROUPUSERROLE_GROUP_REL, params);

    /*
     * organization.userSet .addUserSetInUserSet("G", groupId, "H", groupUserRoleId);
     */
  }

  static final private String INSERT_A_GROUPUSERROLE_GROUP_REL =
      "insert into ST_GroupUserRole_Group_Rel(groupUserRoleId, groupId) values(?,?)";

  /**
   * Removes a group from a groupUserRole.
   */
  public void removeGroupFromGroupUserRole(int groupId, int groupUserRoleId)
      throws AdminPersistenceException {
    if (!isGroupDirectlyInRole(groupId, groupUserRoleId)) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.removeGroupFromGroupUserRole",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_IN_GROUP_USERROLE", "group userrole id: '"
          + groupUserRoleId + "', group id: '" + groupId + "'");
    }

    int[] params = new int[] { groupUserRoleId, groupId };
    SynchroDomainReport.debug("GroupUserRoleTable.removeGroupFromGroupUserRole()",
        "Retrait du groupe d'ID " + groupId + " de l'espace d'ID "
        + groupUserRoleId + ", requête : "
        + DELETE_GROUPUSERROLE_GROUP_REL);
    updateRelation(DELETE_GROUPUSERROLE_GROUP_REL, params);
  }

  static final private String DELETE_GROUPUSERROLE_GROUP_REL =
      "delete from ST_GroupUserRole_Group_Rel where groupUserRoleId = ? and groupId = ?";

  /**
   * Fetch the current groupUserRole row from a resultSet.
   */
  @Override
  protected GroupUserRoleRow fetchRow(ResultSet rs) throws SQLException {
    return fetchGroupUserRole(rs);
  }

  private OrganizationSchema organization = null;
}
