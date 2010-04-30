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
import java.sql.Types;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A UserRoleTable object manages the ST_UserRole table.
 */
public class UserRoleTable extends Table {
  public UserRoleTable(OrganizationSchema organization) {
    super(organization, "ST_UserRole");
    this.organization = organization;
  }

  static final private String USERROLE_COLUMNS =
      "id,instanceId,name,roleName,description,isInherited,objectId";

  /**
   * Fetch the current userRole row from a resultSet.
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
   */
  public UserRoleRow getUserRole(int id) throws AdminPersistenceException {
    return (UserRoleRow) getUniqueRow(SELECT_USERROLE_BY_ID, id);
  }

  static final private String SELECT_USERROLE_BY_ID = "select "
      + USERROLE_COLUMNS + " from ST_UserRole where id = ?";

  /**
   * Returns the UserRole whith the given roleName in the given instance.
   */
  public UserRoleRow getUserRole(int instanceId, String roleName)
      throws AdminPersistenceException {
    UserRoleRow[] userRoles = (UserRoleRow[]) getRows(
        SELECT_USERROLE_BY_ROLENAME, new int[] { instanceId },
        new String[] { roleName }).toArray(new UserRoleRow[0]);

    if (userRoles.length == 0)
      return null;
    else if (userRoles.length == 1)
      return userRoles[0];
    else {
      throw new AdminPersistenceException("UserRoleTable.getUserRole",
          SilverpeasException.ERROR,
          "admin.EX_ERR_USERROLE_NAME_INSTANCEID_FOUND_TWICE",
          "instance id : '" + instanceId + "', userrole name: '" + roleName
          + "'");
    }
  }

  static final private String SELECT_USERROLE_BY_ROLENAME = "select "
      + USERROLE_COLUMNS
      + " from ST_UserRole where instanceId = ? and name = ? and objectId is null";

  /**
   * Returns all the UserRoles.
   */
  public UserRoleRow[] getAllUserRoles() throws AdminPersistenceException {
    return (UserRoleRow[]) getRows(SELECT_ALL_USERROLES).toArray(
        new UserRoleRow[0]);
  }

  static final private String SELECT_ALL_USERROLES = "select "
      + USERROLE_COLUMNS + " from ST_UserRole";

  /**
   * Returns all the UserRoles of an instance.
   */
  public UserRoleRow[] getAllUserRolesOfInstance(int instanceId)
      throws AdminPersistenceException {
    return (UserRoleRow[]) getRows(SELECT_ALL_INSTANCE_USERROLES, instanceId)
        .toArray(new UserRoleRow[0]);
  }

  static final private String SELECT_ALL_INSTANCE_USERROLES = "select "
      + USERROLE_COLUMNS + " from ST_UserRole where instanceId = ? ";

  /**
   * Returns all the UserRole ids of an instance.
   */
  public String[] getAllUserRoleIdsOfInstance(int instanceId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_INSTANCE_USERROLE_IDS, instanceId)
        .toArray(new String[0]);
  }

  static final private String SELECT_ALL_INSTANCE_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId is null";

  public String[] getAllObjectUserRoleIdsOfInstance(int instanceId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_INSTANCE_OBJECT_USERROLE_IDS,
        instanceId).toArray(new String[0]);
  }

  static final private String SELECT_ALL_INSTANCE_OBJECT_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId is not null";

  /**
   * Returns all the UserRole ids of an object in a given instance.
   */
  public String[] getAllUserRoleIdsOfObject(int objectId, String objectType,
      int instanceId) throws AdminPersistenceException {
    int[] params = new int[] { instanceId, objectId };
    String[] sParams = new String[] { objectType };
    return (String[]) getIds(SELECT_ALL_OBJECT_USERROLE_IDS, params, sParams)
        .toArray(new String[0]);
  }

  static final private String SELECT_ALL_OBJECT_USERROLE_IDS =
      "select id from ST_UserRole where instanceId = ? and objectId = ? and objectType = ? ";

  /**
   * Returns all the direct UserRoles of user.
   */
  public UserRoleRow[] getDirectUserRolesOfUser(int userId)
      throws AdminPersistenceException {
    return (UserRoleRow[]) getRows(SELECT_USER_USERROLES, userId).toArray(
        new UserRoleRow[0]);
  }

  static final private String SELECT_USER_USERROLES = "select "
      + USERROLE_COLUMNS + " from ST_UserRole, ST_UserRole_User_Rel"
      + " where id = userRoleId" + " and userId = ? ";

  /**
   * Returns all the direct UserRoles of a group.
   */
  public UserRoleRow[] getDirectUserRolesOfGroup(int groupId)
      throws AdminPersistenceException {
    return (UserRoleRow[]) getRows(SELECT_GROUP_USERROLES, groupId).toArray(
        new UserRoleRow[0]);
  }

  static final private String SELECT_GROUP_USERROLES = "select "
      + USERROLE_COLUMNS + " from ST_UserRole, ST_UserRole_Group_Rel"
      + " where id = userRoleId" + " and groupId = ? ";

  /**
   * Returns the UserRole whose fields match those of the given sample UserRole fields.
   */
  public UserRoleRow[] getAllMatchingUserRoles(UserRoleRow sampleUserRole)
      throws AdminPersistenceException {
    String[] columns = new String[] { "name", "description" };
    String[] values = new String[] { sampleUserRole.name,
        sampleUserRole.description };

    return (UserRoleRow[]) getMatchingRows(USERROLE_COLUMNS, columns, values)
        .toArray(new UserRoleRow[0]);
  }

  /**
   * Inserts in the database a new userRole row.
   */
  public void createUserRole(UserRoleRow userRole)
      throws AdminPersistenceException {
    ComponentInstanceRow instance = organization.instance
        .getComponentInstance(userRole.instanceId);
    if (instance == null) {
      throw new AdminPersistenceException("UserRoleTable.createUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_INSTANCE_NOT_FOUND",
          "instance id : '" + userRole.instanceId + "'");
    }

    if (userRole.objectId != -1 && !StringUtil.isDefined(userRole.objectType)) {
      throw new AdminPersistenceException("UserRoleTable.createUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_OBJECT_TYPE_NOT_SPECIFIED",
          "objectId = " + userRole.objectId);
    }

    insertRow(INSERT_USERROLE, userRole);
    /*organization.userSet.createUserSet(UserSetRow.RIGHTS, userRole.id);
    if (userRole.objectId != -1) {
      organization.userSet
          .createUserSet(userRole.objectType, userRole.objectId);
      organization.userSet.addUserSetInUserSet(UserSetRow.RIGHTS, userRole.id,
          userRole.objectType, userRole.objectId);
    } else
      organization.userSet.addUserSetInUserSet(UserSetRow.RIGHTS, userRole.id,
          ObjectType.INSTANCE, userRole.instanceId);*/
  }

  static final private String INSERT_USERROLE = "insert into"
      + " ST_UserRole(id,instanceId,name,roleName,description,isInherited,objectId,objectType)"
      + " values     (? ,?         ,?   ,?       ,?			 ,?			 ,?		  ,?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    UserRoleRow ur = (UserRoleRow) row;
    if (ur.id == -1) {
      ur.id = getNextId();
    }

    insert.setInt(1, ur.id);
    insert.setInt(2, ur.instanceId);
    insert.setString(3, truncate(ur.name, 100));
    insert.setString(4, truncate(ur.roleName, 100));
    insert.setString(5, truncate(ur.description, 500));
    insert.setInt(6, ur.isInherited);

    if (ur.objectId == -1)
      insert.setNull(7, Types.INTEGER);
    else
      insert.setInt(7, ur.objectId);

    if (!StringUtil.isDefined(ur.objectType))
      insert.setNull(8, Types.VARCHAR);
    else
      insert.setString(8, ur.objectType);
  }

  /**
   * Updates a userRole
   */
  public void updateUserRole(UserRoleRow userRole)
      throws AdminPersistenceException {
    updateRow(UPDATE_USERROLE, userRole);
  }

  static final private String UPDATE_USERROLE = "update ST_UserRole set"
      + " name = ?," + " description = ?" + " where id = ?";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    UserRoleRow s = (UserRoleRow) row;

    update.setString(1, truncate(s.name, 100));
    update.setString(2, truncate(s.description, 500));
    update.setInt(3, s.id);
  }

  /**
   * Delete the userRole
   */
  public void removeUserRole(int id) throws AdminPersistenceException {
    UserRoleRow userRole = getUserRole(id);
    if (userRole == null)
      return;

    UserRow[] users = organization.user.getDirectUsersOfUserRole(id);
    for (int i = 0; i < users.length; i++) {
      removeUserFromUserRole(users[i].id, id);
    }

    GroupRow[] groups = organization.group.getDirectGroupsInUserRole(id);
    for (int i = 0; i < groups.length; i++) {
      removeGroupFromUserRole(groups[i].id, id);
    }

    //organization.userSet.removeUserSet(UserSetRow.RIGHTS, id);
    updateRelation(DELETE_USERROLE, id);
  }

  static final private String DELETE_USERROLE = "delete from ST_UserRole where id = ?";

  /**
   * Tests if a user has a given role (not recursive).
   */
  public boolean isUserDirectlyInRole(int userId, int userRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, userRoleId };
    Integer result = getInteger(SELECT_COUNT_USERROLE_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_USERROLE_USER_REL =
      "select count(*) from ST_UserRole_User_Rel"
      + " where userId = ? and userRoleId = ?";

  /**
   * Add an user in a userRole.
   */
  public void addUserInUserRole(int userId, int userRoleId)
      throws AdminPersistenceException {
    if (isUserDirectlyInRole(userId, userRoleId))
      return;

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
    /*organization.userSet
        .addUserInUserSet(userId, UserSetRow.RIGHTS, userRoleId);*/
  }

  static final private String INSERT_A_USERROLE_USER_REL =
      "insert into ST_UserRole_User_Rel(userRoleId, userId) values(?,?)";

  /**
   * Removes an user from a userRole.
   */
  public void removeUserFromUserRole(int userId, int userRoleId)
      throws AdminPersistenceException {
    if (!isUserDirectlyInRole(userId, userRoleId)) {
      throw new AdminPersistenceException(
          "UserRoleTable.removeUserFromUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_IN_USERROLE", "userrole id: '" + userRoleId
          + "', user id: '" + userId + "'");
    }

    int[] params = new int[] { userRoleId, userId };
    SynchroReport.debug("UserRoleTable.removeUserFromUserRole()",
        "Retrait de l'utilisateur d'ID " + userId + " du role d'ID "
        + userRoleId + ", requête : " + DELETE_USERROLE_USER_REL, null);
    updateRelation(DELETE_USERROLE_USER_REL, params);
  }

  static final private String DELETE_USERROLE_USER_REL =
      "delete from ST_UserRole_User_Rel where userRoleId = ? and userId = ?";

  /**
   * Tests if a group has a given role (not recursive).
   */
  public boolean isGroupDirectlyInRole(int groupId, int userRoleId)
      throws AdminPersistenceException {
    int[] ids = new int[] { groupId, userRoleId };
    Integer result = getInteger(SELECT_COUNT_USERROLE_GROUP_REL, ids);

    SilverTrace.debug("admin", "UserRoleTable.isGroupDirectlyInRole()",
        "Le groupe d'ID " + groupId + " et le role d'ID " + userRoleId
        + " ont un nb de lien = " + result);
    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_USERROLE_GROUP_REL =
      "select count(*) from ST_UserRole_Group_Rel"
      + " where groupId = ? and userRoleId = ?";

  /**
   * Adds a group in a userRole.
   */
  public void addGroupInUserRole(int groupId, int userRoleId)
      throws AdminPersistenceException {
    if (isGroupDirectlyInRole(groupId, userRoleId))
      return;

    GroupRow group = organization.group.getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("UserRoleTable.addGroupInUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group id : '" + groupId + "'");
    }

    UserRoleRow userRole = getUserRole(userRoleId);
    if (userRole == null) {
      throw new AdminPersistenceException("UserRoleTable.addGroupInUserRole",
          SilverpeasException.ERROR, "admin.EX_ERR_USERROLE_NOT_FOUND",
          "user role id : '" + userRoleId + "'");
    }

    int[] params = new int[] { userRoleId, groupId };
    updateRelation(INSERT_A_USERROLE_GROUP_REL, params);

    /*organization.userSet.addUserSetInUserSet(ObjectType.GROUP, groupId,
        UserSetRow.RIGHTS, userRoleId);*/
  }

  static final private String INSERT_A_USERROLE_GROUP_REL =
      "insert into ST_UserRole_Group_Rel(userRoleId, groupId) values(?,?)";

  /**
   * Removes a group from a userRole.
   */
  public void removeGroupFromUserRole(int groupId, int userRoleId)
      throws AdminPersistenceException {
    if (!isGroupDirectlyInRole(groupId, userRoleId)) {
      throw new AdminPersistenceException(
          "UserRoleTable.removeGroupFromUserRole", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_IN_USERROLE", "userrole id: '" + userRoleId
          + "', group id: '" + groupId + "'");
    }

    int[] params = new int[] { userRoleId, groupId };
    SynchroReport.debug("UserRoleTable.removeGroupFromUserRole()",
        "Retrait du groupe d'ID " + groupId + " du role d'ID " + userRoleId
        + ", requête : " + DELETE_USERROLE_GROUP_REL, null);
    updateRelation(DELETE_USERROLE_GROUP_REL, params);
  }

  static final private String DELETE_USERROLE_GROUP_REL =
      "delete from ST_UserRole_Group_Rel where userRoleId = ? and groupId = ?";

  /**
   * Fetch the current userRole row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchUserRole(rs);
  }

  private OrganizationSchema organization = null;
}
