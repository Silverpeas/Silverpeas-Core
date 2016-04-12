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
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A SpaceUserRoleTable object manages the ST_SpaceUserRole table.
 */
public class SpaceUserRoleTable extends Table<SpaceUserRoleRow> {

  public SpaceUserRoleTable(OrganizationSchema organization) {
    super(organization, "ST_SpaceUserRole");
    this.organization = organization;
  }

  static final private String SPACEUSERROLE_COLUMNS =
      "id,spaceId,name,RoleName,description,isInherited";

  /**
   * Fetch the current spaceUserRole row from a resultSet.
   */
  protected SpaceUserRoleRow fetchSpaceUserRole(ResultSet rs) throws SQLException {
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
  public SpaceUserRoleRow getSpaceUserRole(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_SPACEUSERROLE_BY_ID, id);
  }

  static final private String SELECT_SPACEUSERROLE_BY_ID = "select "
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole where id = ?";

  /**
   * Returns the SpaceUserRole whith the given RoleName in the given space.
   */
  public SpaceUserRoleRow getSpaceUserRole(int spaceId, String roleName, int inherited) throws
      AdminPersistenceException {
    List<Object> params = new ArrayList<>(3);
    params.add(spaceId);
    params.add(inherited);
    params.add(roleName);
    List<SpaceUserRoleRow> spaceUserRoles = getRows(SELECT_SPACEUSERROLE_BY_ROLENAME, params);

    if (spaceUserRoles.isEmpty()) {
      return null;
    } else if (spaceUserRoles.size() == 1) {
      return spaceUserRoles.get(0);
    }

    throw new AdminPersistenceException(
        "SpaceUserRoleTable.getSpaceUserRole", SilverpeasException.ERROR,
        "admin.EX_ERR_SPACEUSERROLE_NAME_SPACEID_FOUND_TWICE", "space id : '"
        + spaceId + "', space userrole name: '" + roleName + "'");
  }

  static final private String SELECT_SPACEUSERROLE_BY_ROLENAME = "select "
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole where spaceId = ? and isInherited = ? and rolename = ?";

  /**
   * Returns all the SpaceUserRoles.
   */
  public SpaceUserRoleRow[] getAllSpaceUserRoles() throws AdminPersistenceException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_ALL_SPACEUSERROLES);
    return rows.toArray(new SpaceUserRoleRow[rows.size()]);
  }

  static final private String SELECT_ALL_SPACEUSERROLES = "select "
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole";

  /**
   * Returns all the SpaceUserRoles of a space.
   */
  public SpaceUserRoleRow[] getAllSpaceUserRolesOfSpace(int spaceId) throws
      AdminPersistenceException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_ALL_SPACE_USERROLES, spaceId);
    return rows.toArray(new SpaceUserRoleRow[rows.size()]);
  }

  static final private String SELECT_ALL_SPACE_USERROLES = "select "
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole where spaceId = ?";

  /**
   * Returns all the SpaceUserRole ids of a space.
   */
  public String[] getAllSpaceUserRoleIdsOfSpace(int spaceId) throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_SPACE_USERROLE_IDS, spaceId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_SPACE_USERROLE_IDS =
      "select id from ST_SpaceUserRole where spaceId = ?";

  /**
   * Returns all the direct SpaceUserRoles of user.
   */
  public SpaceUserRoleRow[] getDirectSpaceUserRolesOfUser(int userId) throws
      AdminPersistenceException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_USER_SPACEUSERROLES, userId);
    return rows.toArray(new SpaceUserRoleRow[rows.size()]);
  }

  static final private String SELECT_USER_SPACEUSERROLES = "select "
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole, ST_SpaceUserRole_User_Rel"
      + " where id = spaceUserRoleId" + " and   userId = ?";

  /**
   * Returns all the direct SpaceUserRoles of a group.
   */
  public SpaceUserRoleRow[] getDirectSpaceUserRolesOfGroup(int groupId) throws
      AdminPersistenceException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_GROUP_SPACEUSERROLES, groupId);
    return rows.toArray(new SpaceUserRoleRow[rows.size()]);
  }

  static final private String SELECT_GROUP_SPACEUSERROLES = "select "
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole, ST_SpaceUserRole_Group_Rel"
      + " where id = spaceUserRoleId" + " and   groupId = ?";

  /**
   * Returns the SpaceUserRole whose fields match those of the given sample SpaceUserRole fields.
   */
  public SpaceUserRoleRow[] getAllMatchingSpaceUserRoles(SpaceUserRoleRow sampleSpaceUserRole)
      throws AdminPersistenceException {
    String[] columns = new String[] { "name", "description" };
    String[] values = new String[] { sampleSpaceUserRole.name, sampleSpaceUserRole.description };
    List<SpaceUserRoleRow> rows = getMatchingRows(SPACEUSERROLE_COLUMNS, columns, values);
    return rows.toArray(new SpaceUserRoleRow[rows.size()]);
  }

  /**
   * Inserts in the database a new spaceUserRole row.
   */
  public void createSpaceUserRole(SpaceUserRoleRow spaceUserRole) throws AdminPersistenceException {
    SpaceRow space = organization.space.getSpace(spaceUserRole.spaceId);
    if (space == null) {
      throw new AdminPersistenceException(
          "SpaceUserRoleTable.createSpaceUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_SPACE_NOT_FOUND", "space id : '"
          + spaceUserRole.spaceId + "'");
    }

    insertRow(INSERT_SPACEUSERROLE, spaceUserRole);
  }

  static final private String INSERT_SPACEUSERROLE = "insert into"
      + " ST_SpaceUserRole(id,spaceId,name,RoleName,description,isInherited)"
      + " values (?, ?, ?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, SpaceUserRoleRow row)
      throws SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }

    insert.setInt(1, row.id);
    insert.setInt(2, row.spaceId);
    insert.setString(3, truncate(row.name, 100));
    insert.setString(4, truncate(row.roleName, 100));
    insert.setString(5, truncate(row.description, 500));
    insert.setInt(6, row.isInherited);
  }

  /**
   * Updates a spaceUserRole
   */
  public void updateSpaceUserRole(SpaceUserRoleRow spaceUserRole) throws AdminPersistenceException {
    updateRow(UPDATE_SPACEUSERROLE, spaceUserRole);
  }

  static final private String UPDATE_SPACEUSERROLE = "update ST_SpaceUserRole set"
      + " name = ?," + " description = ?" + " where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, SpaceUserRoleRow row)
      throws SQLException {
    update.setString(1, truncate(row.name, 100));
    update.setString(2, truncate(row.description, 500));
    update.setInt(3, row.id);
  }

  /**
   * Delete the spaceUserRole
   */
  public void removeSpaceUserRole(int id) throws AdminPersistenceException {
    SpaceUserRoleRow spaceUserRole = getSpaceUserRole(id);
    if (spaceUserRole == null) {
      return;
    }
    // delete all users attached to profile
    removeAllUsersFromSpaceUserRole(id);
    // delete all groups attached to profile
    removeAllGroupsFromSpaceUserRole(id);
    updateRelation(DELETE_SPACEUSERROLE, id);
  }

  static final private String DELETE_SPACEUSERROLE = "delete from ST_SpaceUserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   */
  public boolean isUserDirectlyInRole(int userId, int spaceUserRoleId) throws
      AdminPersistenceException {
    int[] ids = new int[] { userId, spaceUserRoleId };
    Integer result = getInteger(SELECT_COUNT_SPACEUSERROLE_USER_REL, ids);

    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_SPACEUSERROLE_USER_REL = "select count(*) from "
      + "ST_SpaceUserRole_User_Rel where userId = ? and spaceUserRoleId = ?";

  /**
   * Add an user in a spaceUserRole.
   */
  public void addUserInSpaceUserRole(int userId, int spaceUserRoleId) throws
      AdminPersistenceException {
    if (isUserDirectlyInRole(userId, spaceUserRoleId)) {
      return;
    }
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
    SynchroDomainReport.debug("SpaceUserRoleTable.removeUserFromSpaceUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " de role d'espace d'ID "
        + spaceUserRoleId + ", requête : " + DELETE_SPACEUSERROLE_USER_REL);
    updateRelation(DELETE_SPACEUSERROLE_USER_REL, params);
  }

  static final private String DELETE_SPACEUSERROLE_USER_REL =
      "delete from ST_SpaceUserRole_User_Rel where spaceUserRoleId = ? and userId = ?";

  /**
   * Removes all users from a spaceUserRole.
   */
  public void removeAllUsersFromSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    SynchroDomainReport.debug("SpaceUserRoleTable.removeAllUsersFromSpaceUserRole()",
        "Retrait des utilisateurs du role d'espace d'ID "
        + spaceUserRoleId + ", requête : " + DELETE_SPACEUSERROLE_USER_REL);
    updateRelation(DELETE_ALL_USERS_FROM_SPACEUSERROLE, spaceUserRoleId);
  }

  static final private String DELETE_ALL_USERS_FROM_SPACEUSERROLE =
      "delete from ST_SpaceUserRole_User_Rel where spaceUserRoleId = ? ";

  /**
   * Tests if a group has a given role (not recursive).
   */
  public boolean isGroupDirectlyInRole(int groupId, int spaceUserRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, spaceUserRoleId };
    Integer result = getInteger(SELECT_COUNT_SPACEUSERROLE_GROUP_REL, ids);

    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_SPACEUSERROLE_GROUP_REL =
      "select count(*) from ST_SpaceUserRole_Group_Rel"
      + " where groupId = ? and spaceUserRoleId = ?";

  /**
   * Adds a group in a spaceUserRole.
   */
  public void addGroupInSpaceUserRole(int groupId, int spaceUserRoleId)
      throws AdminPersistenceException {
    if (isGroupDirectlyInRole(groupId, spaceUserRoleId)) {
      return;
    }

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
    SynchroDomainReport.debug("SpaceUserRoleTable.removeGroupFromSpaceUserRole()",
        "Retrait du groupe d'ID " + groupId + " de l'espace d'ID "
        + spaceUserRoleId + ", requête : "
        + DELETE_SPACEUSERROLE_GROUP_REL);
    updateRelation(DELETE_SPACEUSERROLE_GROUP_REL, params);
  }

  static final private String DELETE_SPACEUSERROLE_GROUP_REL =
      "delete from ST_SpaceUserRole_Group_Rel where spaceUserRoleId = ? and groupId = ?";

  /**
   * Removes all groups from a spaceUserRole.
   */
  public void removeAllGroupsFromSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    SynchroDomainReport.debug("SpaceUserRoleTable.removeAllGroupsFromSpaceUserRole()",
        "Retrait des groupes du rôle de l'espace d'ID " + spaceUserRoleId + ", requête : "
        + DELETE_SPACEUSERROLE_GROUP_REL);
    updateRelation(DELETE_ALL_GROUPS_FROM_SPACEUSERROLE, spaceUserRoleId);
  }

  static final private String DELETE_ALL_GROUPS_FROM_SPACEUSERROLE =
      "delete from ST_SpaceUserRole_Group_Rel where spaceUserRoleId = ?";

  /**
   * Fetch the current spaceUserRole row from a resultSet.
   */
  @Override
  protected SpaceUserRoleRow fetchRow(ResultSet rs) throws SQLException {
    return fetchSpaceUserRole(rs);
  }

  private OrganizationSchema organization = null;
}