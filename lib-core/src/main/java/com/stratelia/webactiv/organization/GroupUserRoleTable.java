/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A GroupUserRoleTable object manages the ST_GroupUserRole table.
 */
public class GroupUserRoleTable extends Table {
  public GroupUserRoleTable(OrganizationSchema organization) {
    super(organization, "ST_GroupUserRole");
    this.organization = organization;
  }

  static final private String GROUPUSERROLE_COLUMNS = "id,groupId,roleName";

  /**
   * Fetch the current groupUserRole row from a resultSet.
   */
  protected GroupUserRoleRow fetchGroupUserRole(ResultSet rs)
      throws SQLException {
    GroupUserRoleRow sur = new GroupUserRoleRow();

    sur.id = rs.getInt(1);
    sur.groupId = rs.getInt(2);
    sur.roleName = rs.getString(3);

    return sur;
  }

  /**
   * Returns the GroupUserRole whith the given id.
   */
  public GroupUserRoleRow getGroupUserRole(int id)
      throws AdminPersistenceException {
    return (GroupUserRoleRow) getUniqueRow(SELECT_GROUPUSERROLE_BY_ID, id);
  }

  static final private String SELECT_GROUPUSERROLE_BY_ID = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where id = ?";

  /**
   * Returns the GroupUserRole whith the given groupId.
   */
  public GroupUserRoleRow getGroupUserRoleByGroupId(int groupId)
      throws AdminPersistenceException {
    return (GroupUserRoleRow) getUniqueRow(SELECT_GROUPUSERROLE_BY_GROUPID,
        groupId);
  }

  static final private String SELECT_GROUPUSERROLE_BY_GROUPID = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where groupId = ?";

  /**
   * Returns the GroupUserRole whith the given RoleName in the given group.
   */
  public GroupUserRoleRow getGroupUserRole(int groupId, String roleName)
      throws AdminPersistenceException {
    GroupUserRoleRow[] groupUserRoles = (GroupUserRoleRow[]) getRows(
        SELECT_GROUPUSERROLE_BY_ROLENAME, new int[] { groupId },
        new String[] { roleName }).toArray(new GroupUserRoleRow[0]);

    if (groupUserRoles.length == 0)
      return null;
    else if (groupUserRoles.length == 1)
      return groupUserRoles[0];
    else {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.getGroupUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUPUSERROLE_NAME_GROUPID_FOUND_TWICE", "group id : '"
              + groupId + "', group userrole name: '" + roleName + "'");
    }
  }

  static final private String SELECT_GROUPUSERROLE_BY_ROLENAME = "select "
      + GROUPUSERROLE_COLUMNS
      + " from ST_GroupUserRole where groupId = ? and roleName = ?";

  /**
   * Returns all the GroupUserRoles.
   */
  public GroupUserRoleRow[] getAllGroupUserRoles()
      throws AdminPersistenceException {
    return (GroupUserRoleRow[]) getRows(SELECT_ALL_GROUPUSERROLES).toArray(
        new GroupUserRoleRow[0]);
  }

  static final private String SELECT_ALL_GROUPUSERROLES = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole";

  /**
   * Returns all the GroupUserRoles of a group.
   */
  public GroupUserRoleRow[] getAllGroupUserRolesOfGroup(int groupId)
      throws AdminPersistenceException {
    return (GroupUserRoleRow[]) getRows(SELECT_ALL_GROUP_USERROLES, groupId)
        .toArray(new GroupUserRoleRow[0]);
  }

  static final private String SELECT_ALL_GROUP_USERROLES = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole where groupId = ?";

  /**
   * Returns all the GroupUserRole ids of a group.
   */
  public String[] getAllGroupUserRoleIdsOfGroup(int groupId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_GROUP_USERROLE_IDS, groupId).toArray(
        new String[0]);
  }

  static final private String SELECT_ALL_GROUP_USERROLE_IDS = "select id from ST_GroupUserRole where groupId = ?";

  /**
   * Returns all the GroupUserRoles of an user.
   */
  public GroupUserRoleRow[] getAllGroupUserRolesOfUser(int userId)
      throws AdminPersistenceException {
    return (GroupUserRoleRow[]) getRows(SELECT_ALL_USER_GROUPUSERROLES, userId)
        .toArray(new GroupUserRoleRow[0]);
  }

  static final private String SELECT_ALL_USER_GROUPUSERROLES = "select "
      + GROUPUSERROLE_COLUMNS + " from ST_GroupUserRole,ST_UserSet_User_Rel"
      + " where id=userSetId and userSetType='H' and userId=?";

  /**
   * Returns all the GroupUserRole ids of an user.
   */
  public String[] getAllGroupUserRoleIdsOfUser(int userId)
      throws AdminPersistenceException {
    return (String[]) getRows(SELECT_ALL_USER_GROUPUSERROLE_IDS, userId)
        .toArray(new String[0]);
  }

  static final private String SELECT_ALL_USER_GROUPUSERROLE_IDS = "select id from ST_GroupUserRole,ST_UserSet_User_Rel"
      + " where id=userSetId and userSetType='H' and userId=?";

  /**
   * Returns all the groupUserRole of a given user for a given group.
   */
  public GroupUserRoleRow[] getAllGroupUserRolesOfUserOfGroup(int userId,
      int groupId) throws AdminPersistenceException {
    int[] params = new int[] { groupId, userId };
    return (GroupUserRoleRow[]) getRows(
        SELECT_ALL_USER_GROUPUSERROLES_IN_GROUP, params).toArray(
        new GroupUserRoleRow[0]);
  }

  static final private String SELECT_ALL_USER_GROUPUSERROLES_IN_GROUP = "select "
      + GROUPUSERROLE_COLUMNS
      + " from ST_GroupUserRole,ST_UserSet_User_Rel"
      + " where id=userSetId and groupId=?"
      + " and userSetType='H' and userId=?";

  /**
   * Returns all the direct GroupUserRoles of user.
   */
  public GroupUserRoleRow[] getDirectGroupUserRolesOfUser(int userId)
      throws AdminPersistenceException {
    return (GroupUserRoleRow[]) getRows(SELECT_USER_GROUPUSERROLES, userId)
        .toArray(new GroupUserRoleRow[0]);
  }

  static final private String SELECT_USER_GROUPUSERROLES = "select "
      + GROUPUSERROLE_COLUMNS
      + " from ST_GroupUserRole, ST_GroupUserRole_User_Rel"
      + " where id = groupUserRoleId" + " and   userId = ?";

  /**
   * Returns all the direct GroupUserRoles of a group.
   */
  public GroupUserRoleRow[] getDirectGroupUserRolesOfGroup(int groupId)
      throws AdminPersistenceException {
    return (GroupUserRoleRow[]) getRows(SELECT_GROUP_GROUPUSERROLES, groupId)
        .toArray(new GroupUserRoleRow[0]);
  }

  static final private String SELECT_GROUP_GROUPUSERROLES = "select "
      + GROUPUSERROLE_COLUMNS
      + " from ST_GroupUserRole, ST_GroupUserRole_Group_Rel"
      + " where id = groupUserRoleId" + " and   groupId = ?";

  /**
   * Returns the GroupUserRole whose fields match those of the given sample
   * GroupUserRole fields.
   */
  /*
   * public GroupUserRoleRow[] getAllMatchingGroupUserRoles(GroupUserRoleRow
   * sampleGroupUserRole) throws AdminPersistenceException { String[] columns =
   * new String[] { "name", "description"}; String[] values = new String[] {
   * sampleGroupUserRole.name, sampleGroupUserRole.description};
   *
   * return (GroupUserRoleRow[]) getMatchingRows(GROUPUSERROLE_COLUMNS, columns,
   * values) .toArray(new GroupUserRoleRow[0]); }
   */

  /**
   * Inserts in the database a new groupUserRole row.
   */
  public void createGroupUserRole(GroupUserRoleRow groupUserRole)
      throws AdminPersistenceException {
    GroupRow group = organization.group.getGroup(groupUserRole.groupId);
    if (group == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.createGroupUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", "group id : '"
              + groupUserRole.groupId + "'");
    }

    insertRow(INSERT_GROUPUSERROLE, groupUserRole);
    organization.userSet.createUserSet("H", groupUserRole.id);
    organization.userSet.addUserSetInUserSet("H", groupUserRole.id, "G",
        groupUserRole.groupId);
  }

  static final private String INSERT_GROUPUSERROLE = "insert into"
      + " ST_GroupUserRole(id,groupId,roleName)"
      + " values     (? 	,?       ,?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    GroupUserRoleRow usr = (GroupUserRoleRow) row;
    if (usr.id == -1) {
      usr.id = getNextId();
    }

    insert.setInt(1, usr.id);
    insert.setInt(2, usr.groupId);
    insert.setString(3, usr.roleName);
  }

  /**
   * Updates a groupUserRole
   */
  /*
   * public void updateGroupUserRole(GroupUserRoleRow groupUserRole) throws
   * AdminPersistenceException { updateRow(UPDATE_GROUPUSERROLE, groupUserRole);
   * }
   *
   * static final private String UPDATE_GROUPUSERROLE =
   * "update ST_GroupUserRole set" + " name = ?," + " description = ?" +
   * " where id = ?";
   */

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    /*
     * GroupUserRoleRow s = (GroupUserRoleRow) row;
     *
     * update.setString(1, truncate(s.name,100)); update.setString(2,
     * truncate(s.description,500)); update.setInt(3, s.id);
     */
  }

  /**
   * Delete the groupUserRole
   */
  public void removeGroupUserRole(int id) throws AdminPersistenceException {
    GroupUserRoleRow groupUserRole = getGroupUserRole(id);
    if (groupUserRole == null)
      return;

    UserRow[] users = organization.user.getDirectUsersOfGroupUserRole(id);
    for (int i = 0; i < users.length; i++) {
      removeUserFromGroupUserRole(users[i].id, id);
    }

    GroupRow[] groups = organization.group.getDirectGroupsInGroupUserRole(id);
    for (int i = 0; i < groups.length; i++) {
      removeGroupFromGroupUserRole(groups[i].id, id);
    }

    organization.userSet.removeUserSet("H", id);
    updateRelation(DELETE_GROUPUSERROLE, id);
  }

  static final private String DELETE_GROUPUSERROLE = "delete from ST_GroupUserRole where id = ?";

  /**
   * Tests if a user has a given role (recursive).
   */
  public boolean isUserInRole(int userId, int groupUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_USERSET_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_USERSET_USER_REL = "select linksCount from ST_UserSet_User_Rel"
      + " where userId = ? and userSetId = ? and userSetType='H'";

  /**
   * Tests if a user has a given role (not recursive).
   */
  public boolean isUserDirectlyInRole(int userId, int groupUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_GROUPUSERROLE_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_GROUPUSERROLE_USER_REL = "select count(*) from ST_GroupUserRole_User_Rel"
      + " where userId = ? and groupUserRoleId = ?";

  /**
   * Add an user in a groupUserRole.
   */
  public void addUserInGroupUserRole(int userId, int groupUserRoleId)
      throws AdminPersistenceException {
    if (isUserDirectlyInRole(userId, groupUserRoleId))
      return;

    UserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.addUserInGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
          "user id : '" + userId + "'");
    }

    GroupUserRoleRow groupUserRole = getGroupUserRole(groupUserRoleId);
    if (groupUserRole == null) {
      throw new AdminPersistenceException(
          "GroupUserRoleTable.addUserInGroupUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUPUSERROLE_NOT_FOUND",
          "group user role id : '" + groupUserRoleId + "'");
    }

    int[] params = new int[] { groupUserRoleId, userId };
    updateRelation(INSERT_A_GROUPUSERROLE_USER_REL, params);
    organization.userSet.addUserInUserSet(userId, "H", groupUserRoleId);
  }

  static final private String INSERT_A_GROUPUSERROLE_USER_REL = "insert into ST_GroupUserRole_User_Rel(groupUserRoleId, userId) values(?,?)";

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

    SynchroReport.debug("GroupUserRoleTable.removeUserFromGroupUserRole()",
        "Retrait (ou décrément) de la relation entre l'utilisateur d'ID "
            + userId + " et l'egroup d'ID " + groupUserRoleId
            + " dans la table ST_UserSet_User_Rel", null);
    organization.userSet.removeUserFromUserSet(userId, "H", groupUserRoleId);

    int[] params = new int[] { groupUserRoleId, userId };
    SynchroReport.debug("GroupUserRoleTable.removeUserFromGroupUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " de role d'egroup d'ID "
            + groupUserRoleId + ", requête : " + DELETE_GROUPUSERROLE_USER_REL,
        null);
    updateRelation(DELETE_GROUPUSERROLE_USER_REL, params);
  }

  static final private String DELETE_GROUPUSERROLE_USER_REL = "delete from ST_GroupUserRole_User_Rel where groupUserRoleId = ? and userId = ?";

  /**
   * Tests if a group has a given role (recursive).
   */
  public boolean isGroupInRole(int groupId, int groupUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_USERSET_GROUP_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_USERSET_GROUP_REL = "select linksCount from ST_UserSet_UserSet_Rel"
      + " where subSetType='G' and subSetId=?"
      + " and   superSetType='H' and superSetId = ?";

  /**
   * Tests if a group has a given role (not recursive).
   */
  public boolean isGroupDirectlyInRole(int groupId, int groupUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, groupUserRoleId };
    Integer result = getInteger(SELECT_COUNT_GROUPUSERROLE_GROUP_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_GROUPUSERROLE_GROUP_REL = "select count(*) from ST_GroupUserRole_Group_Rel"
      + " where groupId = ? and groupUserRoleId = ?";

  /**
   * Adds a group in a groupUserRole.
   */
  public void addGroupInGroupUserRole(int groupId, int groupUserRoleId)
      throws AdminPersistenceException {
    if (isGroupDirectlyInRole(groupId, groupUserRoleId))
      return;

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

    organization.userSet
        .addUserSetInUserSet("G", groupId, "H", groupUserRoleId);
  }

  static final private String INSERT_A_GROUPUSERROLE_GROUP_REL = "insert into ST_GroupUserRole_Group_Rel(groupUserRoleId, groupId) values(?,?)";

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

    SynchroReport.debug("GroupUserRoleTable.removeGroupFromGroupUserRole()",
        "Suppression (ou décrément) des relations liant le groupe d'ID "
            + groupId + " et l'egroup d'ID " + groupUserRoleId
            + " dans les tables ST_UserSet_UserSet_Rel et ST_UserSet_User_Rel",
        null);
    organization.userSet.removeUserSetFromUserSet("G", groupId, "H",
        groupUserRoleId);

    int[] params = new int[] { groupUserRoleId, groupId };
    SynchroReport
        .debug("GroupUserRoleTable.removeGroupFromGroupUserRole()",
            "Retrait du groupe d'ID " + groupId + " de l'espace d'ID "
                + groupUserRoleId + ", requête : "
                + DELETE_GROUPUSERROLE_GROUP_REL, null);
    updateRelation(DELETE_GROUPUSERROLE_GROUP_REL, params);
  }

  static final private String DELETE_GROUPUSERROLE_GROUP_REL = "delete from ST_GroupUserRole_Group_Rel where groupUserRoleId = ? and groupId = ?";

  /**
   * Fetch the current groupUserRole row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchGroupUserRole(rs);
  }

  private OrganizationSchema organization = null;
}
