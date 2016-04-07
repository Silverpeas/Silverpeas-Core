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
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * A UserRoleTable object manages the ST_UserRole table.
 */
public class UserRoleTable extends Table<UserRoleRow> {

  public UserRoleTable(OrganizationSchema organization) {
    super(organization, "ST_UserRole");
    this.organization = organization;
  }

  static final private String USERROLE_COLUMNS =
      "id,instanceId,name,roleName,description,isInherited,objectId";

  /**
   * Fetch the current userRole row from a resultSet.
   * @param rs
   * @return the current userRole row from a resultSet.
   * @throws SQLException
   */
  protected UserRoleRow fetchUserRole(ResultSet rs) throws SQLException {
    UserRoleRow ur = new UserRoleRow();

    ur.id = rs.getInt(1);
    ur.instanceId = rs.getInt(2);
    ur.name = rs.getString(3);
    ur.roleName = rs.getString(4);
    ur.description = rs.getString(5);
    ur.isInherited = rs.getInt(6);
    ur.objectId = rs.getInt(7);

    return ur;
  }

  /**
   * Returns the UserRole whith the given id.
   * @param id
   * @return the UserRole whith the given id.
   * @throws AdminPersistenceException
   */
  public UserRoleRow getUserRole(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_USERROLE_BY_ID, id);
  }

  static final private String SELECT_USERROLE_BY_ID = "select "
      + USERROLE_COLUMNS + " from ST_UserRole where id = ?";

  /**
   * Returns the UserRole whith the given roleName in the given instance.
   * @param instanceId
   * @param roleName
   * @return the UserRole whith the given roleName in the given instance. the UserRole whith the
   * given roleName in the given instance.
   * @throws AdminPersistenceException
   */
  public UserRoleRow getUserRole(int instanceId, String roleName, int inherited)
      throws AdminPersistenceException {
    List<Object> params = new ArrayList<>(3);
    params.add(instanceId);
    params.add(inherited);
    params.add(roleName);
    List<UserRoleRow> userRoles = getRows(SELECT_USERROLE_BY_ROLENAME, params);

    if (userRoles.isEmpty()) {
      return null;
    }

    if (userRoles.size() >= 2) {
      SilverTrace.error("admin", "UserRoleTable.getUserRole", "root.MSG_GEN_PARAM_VALUE", "# = " +
          userRoles.size() + ", instanceId = " + instanceId + ", roleName = " + roleName);
    }
    return userRoles.get(0);
  }

  static final private String SELECT_USERROLE_BY_ROLENAME =
      "select "
          +
          USERROLE_COLUMNS
          +
          " from ST_UserRole where instanceId = ? and isInherited = ? and rolename = ? and objectId is null";

  /**
   * Returns all the UserRoles of an instance.
   * @param instanceId
   * @return all the UserRoles of an instance.
   * @throws AdminPersistenceException
   */
  public UserRoleRow[] getAllUserRolesOfInstance(int instanceId) throws AdminPersistenceException {
    List<UserRoleRow> rows = getRows(SELECT_ALL_INSTANCE_USERROLES, instanceId);
    return rows.toArray(new UserRoleRow[rows.size()]);
  }

  static final private String SELECT_ALL_INSTANCE_USERROLES = "select "
      + USERROLE_COLUMNS + " from ST_UserRole where instanceId = ? ";

  /**
   * Returns all the UserRole ids of an instance.
   * @param instanceId
   * @return all the UserRole ids of an instance.
   * @throws AdminPersistenceException
   */
  public String[] getAllUserRoleIdsOfInstance(int instanceId) throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_INSTANCE_USERROLE_IDS, instanceId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_INSTANCE_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId is null";

  public String[] getAllObjectUserRoleIdsOfInstance(int instanceId)
      throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_INSTANCE_OBJECT_USERROLE_IDS, instanceId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_INSTANCE_OBJECT_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId is not null";

  /**
   * Returns all the UserRole ids of an object in a given instance.
   * @param objectId
   * @param objectType
   * @param instanceId
   * @return all the UserRole ids of an object in a given instance.
   * @throws AdminPersistenceException
   */
  public String[] getAllUserRoleIdsOfObject(int objectId, String objectType, int instanceId) throws
      AdminPersistenceException {
    List<Object> params = new ArrayList<>(3);
    params.add(instanceId);
    params.add(objectId);
    params.add(objectType);
    List<String> ids = getIds(SELECT_ALL_OBJECT_USERROLE_IDS, params);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_OBJECT_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId = ? and objectType = ? ";

  /**
   * Returns all the direct UserRoles of user.
   * @param userId
   * @return all the direct UserRoles of user.
   * @throws AdminPersistenceException
   */
  public UserRoleRow[] getDirectUserRolesOfUser(int userId) throws AdminPersistenceException {
    List<UserRoleRow> rows = getRows(SELECT_USER_USERROLES, userId);
    return rows.toArray(new UserRoleRow[rows.size()]);
  }

  static final private String SELECT_USER_USERROLES = "select "
      + USERROLE_COLUMNS + " from ST_UserRole, ST_UserRole_User_Rel"
      + " where id = userRoleId" + " and userId = ? ";

  /**
   * Returns all the direct UserRoles of a group.
   * @param groupId
   * @return all the direct UserRoles of a group.
   * @throws AdminPersistenceException
   */
  public UserRoleRow[] getDirectUserRolesOfGroup(int groupId) throws AdminPersistenceException {
    List<UserRoleRow> rows = getRows(SELECT_GROUP_USERROLES, groupId);
    return rows.toArray(new UserRoleRow[rows.size()]);
  }

  static final private String SELECT_GROUP_USERROLES = "select " + USERROLE_COLUMNS
      + " from ST_UserRole, ST_UserRole_Group_Rel where id = userRoleId" + " and groupId = ? ";

  /**
   * Inserts in the database a new userRole row.
   * @param userRole
   * @throws AdminPersistenceException
   */
  public void createUserRole(UserRoleRow userRole) throws AdminPersistenceException {
    ComponentInstanceRow instance = organization.instance.getComponentInstance(userRole.instanceId);
    if (instance == null) {
      throw new AdminPersistenceException("UserRoleTable.createUserRole",
          SilverpeasException.ERROR,
          "admin.EX_ERR_INSTANCE_NOT_FOUND", "instance id : '" + userRole.instanceId + "'");
    }

    if (userRole.objectId != -1 && !StringUtil.isDefined(userRole.objectType)) {
      throw new AdminPersistenceException("UserRoleTable.createUserRole",
          SilverpeasException.ERROR,
          "admin.EX_ERR_OBJECT_TYPE_NOT_SPECIFIED", "objectId = " + userRole.objectId);
    }
    insertRow(INSERT_USERROLE, userRole);
  }

  static final private String INSERT_USERROLE = "insert into"
      + " ST_UserRole(id,instanceId,name,roleName,description,isInherited,objectId,objectType)"
      + " values     (? ,?         ,?   ,?       ,?			 ,?			 ,?		  ,?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, UserRoleRow row) throws
      SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }
    insert.setInt(1, row.id);
    insert.setInt(2, row.instanceId);
    insert.setString(3, truncate(row.name, 100));
    insert.setString(4, truncate(row.roleName, 100));
    insert.setString(5, truncate(row.description, 500));
    insert.setInt(6, row.isInherited);
    if (row.objectId == -1) {
      insert.setNull(7, Types.INTEGER);
    } else {
      insert.setInt(7, row.objectId);
    }
    if (!StringUtil.isDefined(row.objectType)) {
      insert.setNull(8, Types.VARCHAR);
    } else {
      insert.setString(8, row.objectType);
    }
  }

  /**
   * Update a user role.
   * @param userRole
   * @throws AdminPersistenceException
   */
  public void updateUserRole(UserRoleRow userRole) throws AdminPersistenceException {
    updateRow(UPDATE_USERROLE, userRole);
  }

  static final private String UPDATE_USERROLE = "update ST_UserRole set"
      + " name = ?, description = ? where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, UserRoleRow row) throws
      SQLException {
    update.setString(1, truncate(row.name, 100));
    update.setString(2, truncate(row.description, 500));
    update.setInt(3, row.id);
  }

  /**
   * Delete the userRole
   * @param id
   * @throws AdminPersistenceException
   */
  public void removeUserRole(int id) throws AdminPersistenceException {
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

  static final private String DELETE_USERROLE = "delete from ST_UserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   * @param userId
   * @param userRoleId
   * @return
   * @throws AdminPersistenceException
   */
  public boolean isUserDirectlyInRole(int userId, int userRoleId) throws AdminPersistenceException {
    int[] ids = new int[] { userId, userRoleId };
    Integer result = getInteger(SELECT_COUNT_USERROLE_USER_REL, ids);
    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_USERROLE_USER_REL =
      "select count(*) from ST_UserRole_User_Rel"
      + " where userId = ? and userRoleId = ?";

  /**
   * Add an user in a userRole.
   * @param userId
   * @param userRoleId
   * @throws AdminPersistenceException
   */
  public void addUserInUserRole(int userId, int userRoleId) throws AdminPersistenceException {
    if (isUserDirectlyInRole(userId, userRoleId)) {
      return;
    }
    UserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException("UserRoleTable.addUserInUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
          "user id : '" + userId + "'");
    }

    UserRoleRow userRole = getUserRole(userRoleId);
    if (userRole == null) {
      throw new AdminPersistenceException("UserRoleTable.addUserInUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USERROLE_NOT_FOUND",
          "user role id : '" + userRoleId + "'");
    }

    int[] params = new int[] { userRoleId, userId };
    updateRelation(INSERT_A_USERROLE_USER_REL, params);
  }

  static final private String INSERT_A_USERROLE_USER_REL =
      "insert into ST_UserRole_User_Rel(userRoleId, userId) values(?,?)";

  /**
   * Removes an user from a userRole.
   * @param userId
   * @param userRoleId
   * @throws AdminPersistenceException
   */
  public void removeUserFromUserRole(int userId, int userRoleId) throws AdminPersistenceException {
    if (!isUserDirectlyInRole(userId, userRoleId)) {
      throw new AdminPersistenceException(
          "UserRoleTable.removeUserFromUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_IN_USERROLE", "userrole id: '" + userRoleId
          + "', user id: '" + userId + "'");
    }
    int[] params = new int[] { userRoleId, userId };
    SynchroDomainReport.debug("UserRoleTable.removeUserFromUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " du role d'ID "
        + userRoleId + ", requête : " + DELETE_USERROLE_USER_REL);
    updateRelation(DELETE_USERROLE_USER_REL, params);
  }

  static final private String DELETE_USERROLE_USER_REL =
      "delete from ST_UserRole_User_Rel where userRoleId = ? and userId = ?";

  /**
   * Removes all users from a userRole.
   * @param userRoleId
   */
  public void removeAllUsersFromUserRole(int userRoleId) throws AdminPersistenceException {
    SynchroDomainReport.debug("UserRoleTable.removeAllUsersFromUserRole()",
        "Retrait des utilisateurs du role d'ID " + userRoleId + ", requête : "
        + DELETE_USERROLE_USER_REL);
    updateRelation(DELETE_ALL_USERS_FROM_USERROLE, userRoleId);
  }

  static final private String DELETE_ALL_USERS_FROM_USERROLE =
      "delete from ST_UserRole_User_Rel where userRoleId = ? ";

  /**
   * Removes all groups from a userRole.
   * @param userRoleId
   * @throws AdminPersistenceException
   */
  public void removeAllGroupsFromUserRole(int userRoleId) throws AdminPersistenceException {

    SynchroDomainReport.debug("UserRoleTable.removeAllGroupsFromUserRole()",
        "Retrait des groupes du role d'ID " + userRoleId + ", requête : "
        + DELETE_USERROLE_USER_REL);
    updateRelation(DELETE_ALL_GROUPS_FROM_USERROLE, userRoleId);
  }

  static final private String DELETE_ALL_GROUPS_FROM_USERROLE =
      "delete from ST_UserRole_Group_Rel where userRoleId = ? ";

  /**
   * Tests if a group has a given role (not recursive).
   * @param groupId
   * @param userRoleId
   * @return
   * @throws AdminPersistenceException
   */
  public boolean isGroupDirectlyInRole(int groupId, int userRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, userRoleId };
    Integer result = getInteger(SELECT_COUNT_USERROLE_GROUP_REL, ids);

    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_USERROLE_GROUP_REL =
      "select count(*) from ST_UserRole_Group_Rel where groupId = ? and userRoleId = ?";

  /**
   * Adds a group in a userRole.
   * @param groupId
   * @param userRoleId
   * @throws AdminPersistenceException
   */
  public void addGroupInUserRole(int groupId, int userRoleId) throws AdminPersistenceException {
    if (isGroupDirectlyInRole(groupId, userRoleId)) {
      return;
    }

    GroupRow group = organization.group.getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("UserRoleTable.addGroupInUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND", "group id : '" + groupId + "'");
    }

    UserRoleRow userRole = getUserRole(userRoleId);
    if (userRole == null) {
      throw new AdminPersistenceException("UserRoleTable.addGroupInUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USERROLE_NOT_FOUND",
          "user role id : '" + userRoleId + "'");
    }
    int[] params = new int[] { userRoleId, groupId };
    updateRelation(INSERT_A_USERROLE_GROUP_REL, params);

  }

  static final private String INSERT_A_USERROLE_GROUP_REL =
      "insert into ST_UserRole_Group_Rel(userRoleId, groupId) values(?,?)";

  /**
   * Removes a group from a userRole.
   * @param groupId
   * @param userRoleId
   * @throws AdminPersistenceException
   */
  public void removeGroupFromUserRole(int groupId, int userRoleId) throws AdminPersistenceException {
    if (!isGroupDirectlyInRole(groupId, userRoleId)) {
      throw new AdminPersistenceException("UserRoleTable.removeGroupFromUserRole",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_IN_USERROLE",
          "userrole id: '" + userRoleId + "', group id: '" + groupId + "'");
    }

    int[] params = new int[] { userRoleId, groupId };
    SynchroDomainReport.debug("UserRoleTable.removeGroupFromUserRole()",
        "Retrait du groupe d'ID " + groupId + " du role d'ID " + userRoleId
        + ", requête : " + DELETE_USERROLE_GROUP_REL);
    updateRelation(DELETE_USERROLE_GROUP_REL, params);
  }

  static final private String DELETE_USERROLE_GROUP_REL =
      "delete from ST_UserRole_Group_Rel where userRoleId = ? and groupId = ?";

  /**
   * Fetch the current userRole row from a resultSet.
   */
  @Override
  protected UserRoleRow fetchRow(ResultSet rs) throws SQLException {
    return fetchUserRole(rs);
  }

  private OrganizationSchema organization = null;
}
