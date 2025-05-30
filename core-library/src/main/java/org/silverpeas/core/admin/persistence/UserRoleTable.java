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
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * A UserRoleTable object manages the ST_UserRole table.
 */
@Repository
public class UserRoleTable extends Table<UserRoleRow> {

  public static final String REQUEST_MSG = ", requête : ";

  public UserRoleTable() {
    super("ST_UserRole");
  }

  private static final String USERROLE_COLUMNS =
      "id,instanceId,name,roleName,description,isInherited,objectId,objectType";

  /**
   * Returns the UserRole with the given id.
   * @param id the unique identifier of the user role.
   * @return the UserRole with the given id.
   * @throws SQLException if the query fails.
   */
  public UserRoleRow getUserRole(int id) throws SQLException {
    return getUniqueRow(SELECT_USERROLE_BY_ID, id);
  }

  public static final String SELECT = "select ";
  private static final String SELECT_USERROLE_BY_ID = SELECT
      + USERROLE_COLUMNS + " from ST_UserRole where id = ?";

  /**
   * Returns the UserRole with the given roleName in the given instance.
   * @param instanceId the local identifier of a component instance.
   * @param roleName the name of the user role.
   * @return the UserRole with the given roleName in the given instance. the UserRole whith the
   * given roleName in the given instance.
   * @throws SQLException if the query fails.
   */
  public UserRoleRow getUserRole(int instanceId, String roleName, int inherited)
      throws SQLException {
    List<Object> params = new ArrayList<>(3);
    params.add(instanceId);
    params.add(inherited);
    params.add(roleName);
    List<UserRoleRow> userRoles = getRows(SELECT_USERROLE_BY_ROLENAME, params);

    if (userRoles.isEmpty()) {
      return null;
    }

    if (userRoles.size() >= 2) {
      SilverLogger.getLogger(this).error("Error more than 2 user roles found!");
    }
    return userRoles.get(0);
  }

  private static final String SELECT_USERROLE_BY_ROLENAME =
      SELECT
          +
          USERROLE_COLUMNS
          +
          " from ST_UserRole where instanceId = ? and isInherited = ? and rolename = ? and objectId is null";

  /**
   * Returns all the UserRoles of an instance.
   * @param instanceId the local identifier of a component instance.
   * @return all the UserRoles of an instance.
   * @throws SQLException the the query fails.
   */
  public UserRoleRow[] getAllUserRolesOfInstance(int instanceId) throws SQLException {
    List<UserRoleRow> rows = getRows(SELECT_ALL_INSTANCE_USERROLES, instanceId);
    return rows.toArray(new UserRoleRow[0]);
  }

  private static final String SELECT_ALL_INSTANCE_USERROLES = SELECT
      + USERROLE_COLUMNS + " from ST_UserRole where instanceId = ? ";

  /**
   * Returns all the UserRole ids of an instance.
   * @param instanceId the local identifier of a component instance.
   * @return all the UserRole ids of an instance.
   * @throws SQLException if the query fails.
   */
  public String[] getAllUserRoleIdsOfInstance(int instanceId) throws SQLException {
    List<String> ids = getIds(SELECT_ALL_INSTANCE_USERROLE_IDS, instanceId);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_ALL_INSTANCE_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId is null";

  public String[] getAllObjectUserRoleIdsOfInstance(int instanceId)
      throws SQLException {
    List<String> ids = getIds(SELECT_ALL_INSTANCE_OBJECT_USERROLE_IDS, instanceId);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_ALL_INSTANCE_OBJECT_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId is not null";

  /**
   * Returns all the UserRole ids of an object in a given instance.
   * @param objectId the unique identifier of the an contribution managed in the given component
   * instance.
   * @param objectType the type of the contribution.
   * @param instanceId the local identifier of the component instance
   * @return all the UserRole ids of an object in a given instance.
   * @throws SQLException if the query fails.
   */
  public String[] getAllUserRoleIdsOfObject(int objectId, String objectType, int instanceId) throws
      SQLException {
    List<Object> params = new ArrayList<>(3);
    params.add(instanceId);
    params.add(objectId);
    params.add(objectType);
    List<String> ids = getIds(SELECT_ALL_OBJECT_USERROLE_IDS, params);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_ALL_OBJECT_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId = ? and objectType = ? ";

  /**
   * Returns all the direct UserRoles of user.
   * @param userId the unique identifier of a user
   * @return all the direct UserRoles of user.
   * @throws SQLException if the query fails.
   */
  public UserRoleRow[] getDirectUserRolesOfUser(int userId) throws SQLException {
    List<UserRoleRow> rows = getRows(SELECT_USER_USERROLES, userId);
    return rows.toArray(new UserRoleRow[0]);
  }

  private static final String SELECT_USER_USERROLES = SELECT
      + USERROLE_COLUMNS + " from ST_UserRole, ST_UserRole_User_Rel"
      + " where id = userRoleId" + " and userId = ? ";

  /**
   * Returns all the direct UserRoles of a group.
   * @param groupId the unique identifier of a user group.
   * @return all the direct UserRoles of a group.
   * @throws SQLException if the query fails.
   */
  public UserRoleRow[] getDirectUserRolesOfGroup(int groupId) throws SQLException {
    List<UserRoleRow> rows = getRows(SELECT_GROUP_USERROLES, groupId);
    return rows.toArray(new UserRoleRow[0]);
  }

  private static final String SELECT_GROUP_USERROLES = SELECT + USERROLE_COLUMNS
      + " from ST_UserRole, ST_UserRole_Group_Rel where id = userRoleId" + " and groupId = ? ";

  /**
   * Inserts in the database a new userRole row.
   * @param userRole the user role to insert
   * @throws SQLException if the insertion fails
   */
  public void createUserRole(UserRoleRow userRole) throws SQLException {
    if (userRole.isInstanceIdNotDefined()) {
      throw new SQLException(
          unknown("component instance", String.valueOf(userRole.getInstanceId())));
    }

    if (userRole.isObjectIdDefined() && !userRole.isObjectTypeDefined()) {
      throw new SQLException(undefined("user role type"));
    }
    insertRow(INSERT_USERROLE, userRole);
  }

  private static final String INSERT_USERROLE = "insert into"
      + " ST_UserRole(id,instanceId,name,roleName,description,isInherited,objectId,objectType)"
      + " values (?, ?, ?, ?, ?, ? ,? ,?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, UserRoleRow row) throws
      SQLException {
    if (!row.isIdDefined()) {
      row.setId(getNextId());
    }
    insert.setInt(1, row.getId());
    insert.setInt(2, row.getInstanceId());
    insert.setString(3, truncate(row.getName(), 100));
    insert.setString(4, truncate(row.getRoleName(), 100));
    insert.setString(5, truncate(row.getDescription(), 500));
    insert.setInt(6, row.getInheritance());
    if (row.isObjectIdDefined()) {
      insert.setInt(7, row.getObjectId());
    } else {
      insert.setNull(7, Types.INTEGER);
    }
    if (row.isObjectTypeDefined()) {
      insert.setString(8, row.getObjectType());
    } else {
      insert.setNull(8, Types.VARCHAR);
    }
  }

  /**
   * Update a user role.
   * @param userRole the user role with the updated data.
   * @throws SQLException if the update fails.
   */
  public void updateUserRole(UserRoleRow userRole) throws SQLException {
    updateRow(UPDATE_USERROLE, userRole);
  }

  private static final String UPDATE_USERROLE = "update ST_UserRole set"
      + " name = ?, description = ? where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, UserRoleRow row) throws
      SQLException {
    update.setString(1, truncate(row.getName(), 100));
    update.setString(2, truncate(row.getDescription(), 500));
    update.setInt(3, row.getId());
  }

  /**
   * Delete the userRole
   * @param id the unique identifier of the user role to delete.
   * @throws SQLException if the deletion fails.
   */
  public void removeUserRole(int id) throws SQLException {
    UserRoleRow userRole = getUserRole(id);
    if (userRole == null) {
      return;
    }
    // delete all groups attached to profile
    removeAllGroupsFromUserRole(id);
    // delete all users attached to profile
    removeAllUsersFromUserRole(id);
    updateRelation(DELETE_USERROLE, id);
  }

  private static final String DELETE_USERROLE = "delete from ST_UserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   * @param userId the unique identifier of a user.
   * @param userRoleId the unique identifier of the user role
   * @return true if the user plays the specified role. False otherwise.
   * @throws SQLException if the query fails.
   */
  private boolean isUserDirectlyInRole(int userId, int userRoleId) throws SQLException {
    int[] ids = new int[] { userId, userRoleId };
    Integer result = getInteger(SELECT_COUNT_USERROLE_USER_REL, ids);
    return result != null && result >= 1;
  }

  private static final String SELECT_COUNT_USERROLE_USER_REL =
      "select count(*) from ST_UserRole_User_Rel"
      + " where userId = ? and userRoleId = ?";

  /**
   * Add an user in a userRole.
   * @param userId the unique identifier of a user.
   * @param userRoleId the unique identifier of a user role.
   * @throws SQLException if the adding fails.
   */
  public void addUserInUserRole(int userId, int userRoleId) throws SQLException {
    if (isUserDirectlyInRole(userId, userRoleId)) {
      return;
    }
    checkUserExistence(userId);

    UserRoleRow userRole = getUserRole(userRoleId);
    if (userRole == null) {
      throw new SQLException(unknown("user role", String.valueOf(userRoleId)));
    }

    int[] params = new int[] { userRoleId, userId };
    updateRelation(INSERT_A_USERROLE_USER_REL, params);
  }

  private static final String INSERT_A_USERROLE_USER_REL =
      "insert into ST_UserRole_User_Rel(userRoleId, userId) values(?,?)";

  /**
   * Removes an user from a userRole.
   * @param userId the unique identifier of a user.
   * @param userRoleId the unique identifier of a user role.
   * @throws SQLException if the removing fails.
   */
  public void removeUserFromUserRole(int userId, int userRoleId) throws SQLException {
    int[] params = new int[] { userRoleId, userId };
    SynchroDomainReport.debug("UserRoleTable.removeUserFromUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " du role d'ID "
        + userRoleId + REQUEST_MSG + DELETE_USERROLE_USER_REL);
    updateRelation(DELETE_USERROLE_USER_REL, params);
  }

  private static final String DELETE_USERROLE_USER_REL =
      "delete from ST_UserRole_User_Rel where userRoleId = ? and userId = ?";

  /**
   * Removes all users from a userRole.
   * @param userRoleId the unique identifier of a user role.
   */
  public void removeAllUsersFromUserRole(int userRoleId) throws SQLException {
    SynchroDomainReport.debug("UserRoleTable.removeAllUsersFromUserRole()",
        "Retrait des utilisateurs du role d'ID " + userRoleId + REQUEST_MSG
        + DELETE_USERROLE_USER_REL);
    updateRelation(DELETE_ALL_USERS_FROM_USERROLE, userRoleId);
  }

  private static final String DELETE_ALL_USERS_FROM_USERROLE =
      "delete from ST_UserRole_User_Rel where userRoleId = ? ";

  /**
   * Removes all groups from a userRole.
   * @param userRoleId the unique identifier of a user role.
   * @throws SQLException if the removing fails.
   */
  public void removeAllGroupsFromUserRole(int userRoleId) throws SQLException {

    SynchroDomainReport.debug("UserRoleTable.removeAllGroupsFromUserRole()",
        "Retrait des groupes du role d'ID " + userRoleId + REQUEST_MSG
        + DELETE_USERROLE_USER_REL);
    updateRelation(DELETE_ALL_GROUPS_FROM_USERROLE, userRoleId);
  }

  private static final String DELETE_ALL_GROUPS_FROM_USERROLE =
      "delete from ST_UserRole_Group_Rel where userRoleId = ? ";

  /**
   * Tests if a group has a given role (not recursive).
   * @param groupId the unique identifier of a user group.
   * @param userRoleId the unique identifier of a user role.
   * @return true if the group plays the specified role.
   * @throws SQLException if the query fails.
   */
  private boolean isGroupDirectlyInRole(int groupId, int userRoleId) throws SQLException {
    int[] ids = new int[] { groupId, userRoleId };
    Integer result = getInteger(SELECT_COUNT_USERROLE_GROUP_REL, ids);

    return result != null && result >= 1;
  }

  private static final String SELECT_COUNT_USERROLE_GROUP_REL =
      "select count(*) from ST_UserRole_Group_Rel where groupId = ? and userRoleId = ?";

  /**
   * Adds a group in a userRole.
   * @param groupId the unique identifier of a user group.
   * @param userRoleId the unique identifier of a user role.
   * @throws SQLException if the adding fails.
   */
  public void addGroupInUserRole(int groupId, int userRoleId) throws SQLException {
    if (isGroupDirectlyInRole(groupId, userRoleId)) {
      return;
    }

    checkGroupExistence(groupId);

    UserRoleRow userRole = getUserRole(userRoleId);
    if (userRole == null) {
      throw new SQLException(unknown("role", String.valueOf(userRoleId)));
    }
    int[] params = new int[] { userRoleId, groupId };
    updateRelation(INSERT_A_USERROLE_GROUP_REL, params);

  }

  private static final String INSERT_A_USERROLE_GROUP_REL =
      "insert into ST_UserRole_Group_Rel(userRoleId, groupId) values(?,?)";

  /**
   * Removes a group from a userRole.
   * @param groupId the unique identifier of a user group.
   * @param userRoleId the unique identifier of a user role.
   * @throws SQLException if the removing fails.
   */
  public void removeGroupFromUserRole(int groupId, int userRoleId) throws SQLException {
    int[] params = new int[] { userRoleId, groupId };
    SynchroDomainReport.debug("UserRoleTable.removeGroupFromUserRole()",
        "Retrait du groupe d'ID " + groupId + " du role d'ID " + userRoleId
        + REQUEST_MSG + DELETE_USERROLE_GROUP_REL);
    updateRelation(DELETE_USERROLE_GROUP_REL, params);
  }

  private static final String DELETE_USERROLE_GROUP_REL =
      "delete from ST_UserRole_Group_Rel where userRoleId = ? and groupId = ?";

  /**
   * Fetch the current userRole row from a resultSet.
   */
  @Override
  protected UserRoleRow fetchRow(ResultSet rs) throws SQLException {
    return UserRoleRow.fetch(rs);
  }
}
