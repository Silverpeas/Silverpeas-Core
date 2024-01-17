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
import org.silverpeas.core.annotation.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.admin.persistence.SpaceUserRoleRow.fetch;

/**
 * A SpaceUserRoleTable object manages the ST_SpaceUserRole table.
 */
@Repository
public class SpaceUserRoleTable extends Table<SpaceUserRoleRow> {

  public static final String REQUEST_MSG = ", requête : ";

  SpaceUserRoleTable() {
    super("ST_SpaceUserRole");
  }

  private static final String SPACEUSERROLE_COLUMNS =
      "id,spaceId,name,RoleName,description,isInherited";

  /**
   * Returns the SpaceUserRole whith the given id.
   */
  public SpaceUserRoleRow getSpaceUserRole(int id) throws SQLException {
    return getUniqueRow(SELECT_SPACEUSERROLE_BY_ID, id);
  }

  public static final String SELECT = "select ";
  private static final String SELECT_SPACEUSERROLE_BY_ID = SELECT
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole where id = ?";

  /**
   * Returns the SpaceUserRole whith the given RoleName in the given space.
   */
  public SpaceUserRoleRow getSpaceUserRole(int spaceId, String roleName, int inherited) throws
      SQLException {
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

    throw new SQLException(
        failureOnGetting("user role " + roleName, "for space " + spaceId));
  }

  private static final String SELECT_SPACEUSERROLE_BY_ROLENAME = SELECT
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole where spaceId = ? and isInherited = ? and rolename = ?";

  /**
   * Returns all the SpaceUserRoles.
   */
  public SpaceUserRoleRow[] getAllSpaceUserRoles() throws SQLException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_ALL_SPACEUSERROLES);
    return rows.toArray(new SpaceUserRoleRow[0]);
  }

  private static final String SELECT_ALL_SPACEUSERROLES = SELECT
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole";

  /**
   * Returns all the SpaceUserRoles of a space.
   */
  public SpaceUserRoleRow[] getAllSpaceUserRolesOfSpace(int spaceId) throws
      SQLException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_ALL_SPACE_USERROLES, spaceId);
    return rows.toArray(new SpaceUserRoleRow[0]);
  }

  private static final String SELECT_ALL_SPACE_USERROLES = SELECT
      + SPACEUSERROLE_COLUMNS + " from ST_SpaceUserRole where spaceId = ?";

  /**
   * Returns all the SpaceUserRole ids of a space.
   */
  public String[] getAllSpaceUserRoleIdsOfSpace(int spaceId) throws SQLException {
    List<String> ids = getIds(SELECT_ALL_SPACE_USERROLE_IDS, spaceId);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_ALL_SPACE_USERROLE_IDS =
      "select id from ST_SpaceUserRole where spaceId = ?";

  /**
   * Returns all the direct SpaceUserRoles of user.
   */
  public SpaceUserRoleRow[] getDirectSpaceUserRolesOfUser(int userId) throws SQLException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_USER_SPACEUSERROLES, userId);
    return rows.toArray(new SpaceUserRoleRow[0]);
  }

  private static final String SELECT_USER_SPACEUSERROLES = SELECT
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole, ST_SpaceUserRole_User_Rel"
      + " where id = spaceUserRoleId" + " and   userId = ?";

  /**
   * Returns all the direct SpaceUserRoles of a group.
   */
  public SpaceUserRoleRow[] getDirectSpaceUserRolesOfGroup(int groupId) throws SQLException {
    List<SpaceUserRoleRow> rows = getRows(SELECT_GROUP_SPACEUSERROLES, groupId);
    return rows.toArray(new SpaceUserRoleRow[0]);
  }

  private static final String SELECT_GROUP_SPACEUSERROLES = SELECT
      + SPACEUSERROLE_COLUMNS
      + " from ST_SpaceUserRole, ST_SpaceUserRole_Group_Rel"
      + " where id = spaceUserRoleId" + " and   groupId = ?";

  /**
   * Returns the SpaceUserRole whose fields match those of the given sample SpaceUserRole fields.
   */
  public SpaceUserRoleRow[] getAllMatchingSpaceUserRoles(SpaceUserRoleRow sampleSpaceUserRole)
      throws SQLException {
    String[] columns = new String[] { "name", "description" };
    String[] values = new String[] { sampleSpaceUserRole.getName(), sampleSpaceUserRole.getDescription() };
    List<SpaceUserRoleRow> rows = getMatchingRows(SPACEUSERROLE_COLUMNS, columns, values);
    return rows.toArray(new SpaceUserRoleRow[0]);
  }

  /**
   * Inserts in the database a new spaceUserRole row.
   */
  public void createSpaceUserRole(SpaceUserRoleRow spaceUserRole) throws SQLException {
    SpaceRow space = OrganizationSchema.get().space().getSpace(spaceUserRole.getSpaceId());
    if (space == null) {
      throw new SQLException(unknown("space", String.valueOf(spaceUserRole.getSpaceId())));
    }

    insertRow(INSERT_SPACEUSERROLE, spaceUserRole);
  }

  private static final String INSERT_SPACEUSERROLE = "insert into"
      + " ST_SpaceUserRole(id,spaceId,name,RoleName,description,isInherited)"
      + " values (?, ?, ?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, SpaceUserRoleRow row)
      throws SQLException {
    if (!row.isIdDefined()) {
      row.setId(getNextId());
    }
    insert.setInt(1, row.getId());
    insert.setInt(2, row.getSpaceId());
    insert.setString(3, truncate(row.getName(), 100));
    insert.setString(4, truncate(row.getRoleName(), 100));
    insert.setString(5, truncate(row.getDescription(), 500));
    insert.setInt(6, row.getInheritance());
  }

  /**
   * Updates a spaceUserRole
   */
  public void updateSpaceUserRole(SpaceUserRoleRow spaceUserRole) throws SQLException {
    updateRow(UPDATE_SPACEUSERROLE, spaceUserRole);
  }

  private static final String UPDATE_SPACEUSERROLE = "update ST_SpaceUserRole set"
      + " name = ?," + " description = ?" + " where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, SpaceUserRoleRow row)
      throws SQLException {
    update.setString(1, truncate(row.getName(), 100));
    update.setString(2, truncate(row.getDescription(), 500));
    update.setInt(3, row.getId());
  }

  /**
   * Delete the spaceUserRole
   */
  public void removeSpaceUserRole(int id) throws SQLException {
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

  private static final String DELETE_SPACEUSERROLE = "delete from ST_SpaceUserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   */
  public boolean isUserDirectlyInRole(int userId, int spaceUserRoleId) throws
      SQLException {
    int[] ids = new int[] { userId, spaceUserRoleId };
    Integer result = getInteger(SELECT_COUNT_SPACEUSERROLE_USER_REL, ids);

    return result != null && result >= 1;
  }

  private static final String SELECT_COUNT_SPACEUSERROLE_USER_REL = "select count(*) from "
      + "ST_SpaceUserRole_User_Rel where userId = ? and spaceUserRoleId = ?";

  /**
   * Add an user in a spaceUserRole.
   */
  public void addUserInSpaceUserRole(int userId, int spaceUserRoleId) throws
      SQLException {
    if (isUserDirectlyInRole(userId, spaceUserRoleId)) {
      return;
    }
    checkUserExistence(userId);

    SpaceUserRoleRow spaceUserRole = getSpaceUserRole(spaceUserRoleId);
    if (spaceUserRole == null) {
      throw new SQLException(unknown("space role", String.valueOf(spaceUserRoleId)));
    }

    int[] params = new int[] { spaceUserRoleId, userId };
    updateRelation(INSERT_A_SPACEUSERROLE_USER_REL, params);
  }

  private static final String INSERT_A_SPACEUSERROLE_USER_REL =
      "insert into ST_SpaceUserRole_User_Rel(spaceUserRoleId, userId) values(?,?)";

  /**
   * Removes an user from a spaceUserRole.
   */
  public void removeUserFromSpaceUserRole(int userId, int spaceUserRoleId)
      throws SQLException {
    if (!isUserDirectlyInRole(userId, spaceUserRoleId)) {
      throw new SQLException("user " + userId + " isn't in role " + spaceUserRoleId);
    }

    int[] params = new int[] { spaceUserRoleId, userId };
    SynchroDomainReport.debug("SpaceUserRoleTable.removeUserFromSpaceUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " de role d'espace d'ID "
        + spaceUserRoleId + REQUEST_MSG + DELETE_SPACEUSERROLE_USER_REL);
    updateRelation(DELETE_SPACEUSERROLE_USER_REL, params);
  }

  private static final String DELETE_SPACEUSERROLE_USER_REL =
      "delete from ST_SpaceUserRole_User_Rel where spaceUserRoleId = ? and userId = ?";

  /**
   * Removes all users from a spaceUserRole.
   */
  public void removeAllUsersFromSpaceUserRole(int spaceUserRoleId)
      throws SQLException {
    SynchroDomainReport.debug("SpaceUserRoleTable.removeAllUsersFromSpaceUserRole()",
        "Retrait des utilisateurs du role d'espace d'ID "
        + spaceUserRoleId + REQUEST_MSG + DELETE_SPACEUSERROLE_USER_REL);
    updateRelation(DELETE_ALL_USERS_FROM_SPACEUSERROLE, spaceUserRoleId);
  }

  private static final String DELETE_ALL_USERS_FROM_SPACEUSERROLE =
      "delete from ST_SpaceUserRole_User_Rel where spaceUserRoleId = ? ";

  /**
   * Tests if a group has a given role (not recursive).
   */
  public boolean isGroupDirectlyInRole(int groupId, int spaceUserRoleId)
      throws SQLException {
    int[] ids = new int[] { groupId, spaceUserRoleId };
    Integer result = getInteger(SELECT_COUNT_SPACEUSERROLE_GROUP_REL, ids);

    return result != null && result >= 1;
  }

  private static final String SELECT_COUNT_SPACEUSERROLE_GROUP_REL =
      "select count(*) from ST_SpaceUserRole_Group_Rel"
      + " where groupId = ? and spaceUserRoleId = ?";

  /**
   * Adds a group in a spaceUserRole.
   */
  public void addGroupInSpaceUserRole(int groupId, int spaceUserRoleId)
      throws SQLException {
    if (isGroupDirectlyInRole(groupId, spaceUserRoleId)) {
      return;
    }

    checkGroupExistence(groupId);

    SpaceUserRoleRow spaceUserRole = getSpaceUserRole(spaceUserRoleId);
    if (spaceUserRole == null) {
      throw new SQLException(unknown("space role", String.valueOf(spaceUserRoleId)));
    }

    int[] params = new int[] { spaceUserRoleId, groupId };
    updateRelation(INSERT_A_SPACEUSERROLE_GROUP_REL, params);
  }

  private static final String INSERT_A_SPACEUSERROLE_GROUP_REL =
      "insert into ST_SpaceUserRole_Group_Rel(spaceUserRoleId, groupId) values(?,?)";

  /**
   * Removes a group from a spaceUserRole.
   */
  public void removeGroupFromSpaceUserRole(int groupId, int spaceUserRoleId)
      throws SQLException {
    if (!isGroupDirectlyInRole(groupId, spaceUserRoleId)) {
      throw new SQLException("group " + groupId + " isn't in role " + spaceUserRoleId);
    }

    int[] params = new int[] { spaceUserRoleId, groupId };
    SynchroDomainReport.debug("SpaceUserRoleTable.removeGroupFromSpaceUserRole()",
        "Retrait du groupe d'ID " + groupId + " de l'espace d'ID "
        + spaceUserRoleId + REQUEST_MSG
        + DELETE_SPACEUSERROLE_GROUP_REL);
    updateRelation(DELETE_SPACEUSERROLE_GROUP_REL, params);
  }

  private static final String DELETE_SPACEUSERROLE_GROUP_REL =
      "delete from ST_SpaceUserRole_Group_Rel where spaceUserRoleId = ? and groupId = ?";

  /**
   * Removes all groups from a spaceUserRole.
   */
  public void removeAllGroupsFromSpaceUserRole(int spaceUserRoleId)
      throws SQLException {
    SynchroDomainReport.debug("SpaceUserRoleTable.removeAllGroupsFromSpaceUserRole()",
        "Retrait des groupes du rôle de l'espace d'ID " + spaceUserRoleId + REQUEST_MSG
        + DELETE_SPACEUSERROLE_GROUP_REL);
    updateRelation(DELETE_ALL_GROUPS_FROM_SPACEUSERROLE, spaceUserRoleId);
  }

  private static final String DELETE_ALL_GROUPS_FROM_SPACEUSERROLE =
      "delete from ST_SpaceUserRole_Group_Rel where spaceUserRoleId = ?";

  /**
   * Fetch the current spaceUserRole row from a resultSet.
   */
  @Override
  protected SpaceUserRoleRow fetchRow(ResultSet rs) throws SQLException {
    return fetch(rs);
  }
}