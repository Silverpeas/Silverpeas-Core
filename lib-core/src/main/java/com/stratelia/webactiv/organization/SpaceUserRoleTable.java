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
 * FLOSS exception.  You should have received a copy of the text describing
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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A SpaceUserRoleTable object manages the ST_SpaceUserRole table.
 */
public class SpaceUserRoleTable extends Table {
  public SpaceUserRoleTable(OrganizationSchema organization) {
    super(organization, "ST_SpaceUserRole");
    this.organization = organization;
  }

  static final private String SPACEUSERROLE_COLUMNS =
      "id,spaceId,name,RoleName,description,isInherited";

  /**
   * Fetch the current spaceUserRole row from a resultSet.
   */
  protected SpaceUserRoleRow fetchSpaceUserRole(ResultSet rs)
      throws SQLException {
    SpaceUserRoleRow sur = new SpaceUserRoleRow();

    sur.id = rs.getInt(1);
    sur.spaceId = rs.getInt(2);
    sur.name = rs.getString(3);
    sur.roleName = rs.getString(4);
    sur.description = rs.getString(5);
    sur.isInherited = rs.getInt(6);

    return sur;
  }

  /**
   * Returns the SpaceUserRole whith the given id.
   */
  public SpaceUserRoleRow getSpaceUserRole(int id)
      throws AdminPersistenceException {
    return (SpaceUserRoleRow) getUniqueRow(SELECT_SPACEUSERROLE_BY_ID, id);
  }

  static final private String SELECT_SPACEUSERROLE_BY_ID = "select "
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole where id = ?";

  /**
   * Returns the SpaceUserRole whith the given RoleName in the given space.
   */
  public SpaceUserRoleRow getSpaceUserRole(int spaceId, String RoleName)
      throws AdminPersistenceException {
    SpaceUserRoleRow[] spaceUserRoles = (SpaceUserRoleRow[]) getRows(
        SELECT_SPACEUSERROLE_BY_ROLENAME, new int[] { spaceId },
        new String[] { RoleName }).toArray(new SpaceUserRoleRow[0]);

    if (spaceUserRoles.length == 0)
      return null;
    else if (spaceUserRoles.length == 1)
      return spaceUserRoles[0];
    else {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.getSpaceUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_SPACEUSERROLE_NAME_SPACEID_FOUND_TWICE", "space id : '"
          + spaceId + "', space userrole name: '" + RoleName + "'");
    }
  }

  static final private String SELECT_SPACEUSERROLE_BY_ROLENAME = "select "
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole where spaceId = ? and name = ?";

  /**
   * Returns all the SpaceUserRoles.
   */
  public SpaceUserRoleRow[] getAllSpaceUserRoles()
      throws AdminPersistenceException {
    return (SpaceUserRoleRow[]) getRows(SELECT_ALL_SPACEUSERROLES).toArray(
        new SpaceUserRoleRow[0]);
  }

  static final private String SELECT_ALL_SPACEUSERROLES = "select "
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole";

  /**
   * Returns all the SpaceUserRoles of a space.
   */
  public SpaceUserRoleRow[] getAllSpaceUserRolesOfSpace(int spaceId)
      throws AdminPersistenceException {
    return (SpaceUserRoleRow[]) getRows(SELECT_ALL_SPACE_USERROLES, spaceId)
        .toArray(new SpaceUserRoleRow[0]);
  }

  static final private String SELECT_ALL_SPACE_USERROLES = "select "
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole where spaceId = ?";

  /**
   * Returns all the SpaceUserRole ids of a space.
   */
  public String[] getAllSpaceUserRoleIdsOfSpace(int spaceId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_SPACE_USERROLE_IDS, spaceId).toArray(
        new String[0]);
  }

  static final private String SELECT_ALL_SPACE_USERROLE_IDS =
      "select id from ST_SpaceUserRole where spaceId = ?";

  /**
   * Returns all the direct SpaceUserRoles of user.
   */
  public SpaceUserRoleRow[] getDirectSpaceUserRolesOfUser(int userId)
      throws AdminPersistenceException {
    return (SpaceUserRoleRow[]) getRows(SELECT_USER_SPACEUSERROLES, userId)
        .toArray(new SpaceUserRoleRow[0]);
  }

  static final private String SELECT_USER_SPACEUSERROLES = "select "
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole, ST_SpaceUserRole_User_Rel"
      + " where id = spaceUserRoleId" + " and   userId = ?";

  /**
   * Returns all the direct SpaceUserRoles of a group.
   */
  public SpaceUserRoleRow[] getDirectSpaceUserRolesOfGroup(int groupId)
      throws AdminPersistenceException {
    return (SpaceUserRoleRow[]) getRows(SELECT_GROUP_SPACEUSERROLES, groupId)
        .toArray(new SpaceUserRoleRow[0]);
  }

  static final private String SELECT_GROUP_SPACEUSERROLES = "select "
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole, ST_SpaceUserRole_Group_Rel"
      + " where id = spaceUserRoleId" + " and   groupId = ?";

  /**
   * Returns the SpaceUserRole whose fields match those of the given sample SpaceUserRole fields.
   */
  public SpaceUserRoleRow[] getAllMatchingSpaceUserRoles(
      SpaceUserRoleRow sampleSpaceUserRole) throws AdminPersistenceException {
    String[] columns = new String[] { "name", "description" };
    String[] values = new String[] { sampleSpaceUserRole.name,
        sampleSpaceUserRole.description };

    return (SpaceUserRoleRow[]) getMatchingRows(SPACEUSERROLE_COLUMNS, columns,
        values).toArray(new SpaceUserRoleRow[0]);
  }

  /**
   * Inserts in the database a new spaceUserRole row.
   */
  public void createSpaceUserRole(SpaceUserRoleRow spaceUserRole)
      throws AdminPersistenceException {
    SpaceRow space = organization.space.getSpace(spaceUserRole.spaceId);
    if (space == null) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.createSpaceUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_SPACE_NOT_FOUND", "space id : '"
          + spaceUserRole.spaceId + "'");
    }

    insertRow(INSERT_SPACEUSERROLE, spaceUserRole);

    /*if (spaceUserRole.roleName.equalsIgnoreCase("manager")) {
      organization.userSet.createUserSet("M", spaceUserRole.id);
      organization.userSet.addUserSetInUserSet("M", spaceUserRole.id, "S",
          spaceUserRole.spaceId);
    } else {
      organization.userSet.createUserSet("X", spaceUserRole.id);
      organization.userSet.addUserSetInUserSet("X", spaceUserRole.id, "S",
          spaceUserRole.spaceId);
    }*/
  }

  static final private String INSERT_SPACEUSERROLE = "insert into"
      + " ST_SpaceUserRole(id,spaceId,name,RoleName,description,isInherited)"
      + " values     (? 	,?       ,?   ,?       ,?		   ,?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    SpaceUserRoleRow usr = (SpaceUserRoleRow) row;
    SilverTrace.debug("admin", "SpaceUserRoleTable.prepareInsert",
        "root.MSG_GEN_ENTER_METHOD", "usr.id = " + usr.id + ", usr.spaceId = "
        + usr.spaceId + ", usr.roleName = " + usr.roleName
        + ", usr.isInherited = " + usr.isInherited);
    if (usr.id == -1) {
      usr.id = getNextId();
    }

    insert.setInt(1, usr.id);
    insert.setInt(2, usr.spaceId);
    insert.setString(3, truncate(usr.name, 100));
    insert.setString(4, truncate(usr.roleName, 100));
    insert.setString(5, truncate(usr.description, 500));
    insert.setInt(6, usr.isInherited);
  }

  /**
   * Updates a spaceUserRole
   */
  public void updateSpaceUserRole(SpaceUserRoleRow spaceUserRole)
      throws AdminPersistenceException {
    updateRow(UPDATE_SPACEUSERROLE, spaceUserRole);
  }

  static final private String UPDATE_SPACEUSERROLE = "update ST_SpaceUserRole set"
      + " name = ?," + " description = ?" + " where id = ?";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    SpaceUserRoleRow s = (SpaceUserRoleRow) row;

    update.setString(1, truncate(s.name, 100));
    update.setString(2, truncate(s.description, 500));
    update.setInt(3, s.id);
  }

  /**
   * Delete the spaceUserRole
   */
  public void removeSpaceUserRole(int id) throws AdminPersistenceException {
    SpaceUserRoleRow spaceUserRole = getSpaceUserRole(id);
    if (spaceUserRole == null)
      return;

    UserRow[] users = organization.user.getDirectUsersOfSpaceUserRole(id);
    for (int i = 0; i < users.length; i++) {
      removeUserFromSpaceUserRole(users[i].id, id);
    }

    GroupRow[] groups = organization.group.getDirectGroupsInSpaceUserRole(id);
    for (int i = 0; i < groups.length; i++) {
      removeGroupFromSpaceUserRole(groups[i].id, id);
    }

    /*if (spaceUserRole.roleName.equalsIgnoreCase("manager"))
      organization.userSet.removeUserSet("M", id);
    else
      organization.userSet.removeUserSet("X", id);*/

    updateRelation(DELETE_SPACEUSERROLE, id);
  }

  static final private String DELETE_SPACEUSERROLE = "delete from ST_SpaceUserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   */
  public boolean isUserDirectlyInRole(int userId, int spaceUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, spaceUserRoleId };
    Integer result = getInteger(SELECT_COUNT_SPACEUSERROLE_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_SPACEUSERROLE_USER_REL =
      "select count(*) from ST_SpaceUserRole_User_Rel"
      + " where userId = ? and spaceUserRoleId = ?";

  /**
   * Add an user in a spaceUserRole.
   */
  public void addUserInSpaceUserRole(int userId, int spaceUserRoleId)
      throws AdminPersistenceException {
    if (isUserDirectlyInRole(userId, spaceUserRoleId))
      return;

    UserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.addUserInSpaceUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
          "user id : '" + userId + "'");
    }

    SpaceUserRoleRow spaceUserRole = getSpaceUserRole(spaceUserRoleId);
    if (spaceUserRole == null) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.addUserInSpaceUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_SPACEUSERROLE_NOT_FOUND",
          "space user role id : '" + spaceUserRoleId + "'");
    }

    int[] params = new int[] { spaceUserRoleId, userId };
    updateRelation(INSERT_A_SPACEUSERROLE_USER_REL, params);

    /*if (spaceUserRole.roleName.equalsIgnoreCase("manager")) {
      organization.userSet.addUserInUserSet(userId, "M", spaceUserRoleId);
    } else {
      organization.userSet.addUserInUserSet(userId, "X", spaceUserRoleId);
    }*/
  }

  static final private String INSERT_A_SPACEUSERROLE_USER_REL =
      "insert into ST_SpaceUserRole_User_Rel(spaceUserRoleId, userId) values(?,?)";

  /**
   * Removes an user from a spaceUserRole.
   */
  public void removeUserFromSpaceUserRole(int userId, int spaceUserRoleId)
      throws AdminPersistenceException {
    if (!isUserDirectlyInRole(userId, spaceUserRoleId)) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.removeUserFromSpaceUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_SPACE_USERROLE",
          "space userrole id: '" + spaceUserRoleId + "', user id: '" + userId
          + "'");
    }

    int[] params = new int[] { spaceUserRoleId, userId };
    SynchroReport.debug("SpaceUserRoleTable.removeUserFromSpaceUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " de role d'espace d'ID "
        + spaceUserRoleId + ", requête : " + DELETE_SPACEUSERROLE_USER_REL,
        null);
    updateRelation(DELETE_SPACEUSERROLE_USER_REL, params);
  }

  static final private String DELETE_SPACEUSERROLE_USER_REL =
      "delete from ST_SpaceUserRole_User_Rel where spaceUserRoleId = ? and userId = ?";

  /**
   * Tests if a group has a given role (not recursive).
   */
  public boolean isGroupDirectlyInRole(int groupId, int spaceUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, spaceUserRoleId };
    Integer result = getInteger(SELECT_COUNT_SPACEUSERROLE_GROUP_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_SPACEUSERROLE_GROUP_REL =
      "select count(*) from ST_SpaceUserRole_Group_Rel"
      + " where groupId = ? and spaceUserRoleId = ?";

  /**
   * Adds a group in a spaceUserRole.
   */
  public void addGroupInSpaceUserRole(int groupId, int spaceUserRoleId)
      throws AdminPersistenceException {
    if (isGroupDirectlyInRole(groupId, spaceUserRoleId))
      return;

    GroupRow group = organization.group.getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.addGroupInSpaceUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group id : '" + groupId + "'");
    }

    SpaceUserRoleRow spaceUserRole = getSpaceUserRole(spaceUserRoleId);
    if (spaceUserRole == null) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.addGroupInSpaceUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_SPACEUSERROLE_NOT_FOUND",
          "space userrole id : '" + spaceUserRoleId + "'");
    }

    int[] params = new int[] { spaceUserRoleId, groupId };
    updateRelation(INSERT_A_SPACEUSERROLE_GROUP_REL, params);

    /*if (spaceUserRole.roleName.equalsIgnoreCase("manager")) {
      organization.userSet.addUserSetInUserSet("G", groupId, "M",
          spaceUserRoleId);
    } else {
      organization.userSet.addUserSetInUserSet("G", groupId, "X",
          spaceUserRoleId);
    }*/
  }

  static final private String INSERT_A_SPACEUSERROLE_GROUP_REL =
      "insert into ST_SpaceUserRole_Group_Rel(spaceUserRoleId, groupId) values(?,?)";

  /**
   * Removes a group from a spaceUserRole.
   */
  public void removeGroupFromSpaceUserRole(int groupId, int spaceUserRoleId)
      throws AdminPersistenceException {
    if (!isGroupDirectlyInRole(groupId, spaceUserRoleId)) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.removeGroupFromSpaceUserRole",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_IN_SPACE_USERROLE", "space userrole id: '"
          + spaceUserRoleId + "', group id: '" + groupId + "'");
    }

    int[] params = new int[] { spaceUserRoleId, groupId };
    SynchroReport
        .debug("SpaceUserRoleTable.removeGroupFromSpaceUserRole()",
        "Retrait du groupe d'ID " + groupId + " de l'espace d'ID "
        + spaceUserRoleId + ", requête : "
        + DELETE_SPACEUSERROLE_GROUP_REL, null);
    updateRelation(DELETE_SPACEUSERROLE_GROUP_REL, params);
  }

  static final private String DELETE_SPACEUSERROLE_GROUP_REL =
      "delete from ST_SpaceUserRole_Group_Rel where spaceUserRoleId = ? and groupId = ?";

  /**
   * Fetch the current spaceUserRole row from a resultSet.
   */
  @Override
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchSpaceUserRole(rs);
  }

  private OrganizationSchema organization = null;
}