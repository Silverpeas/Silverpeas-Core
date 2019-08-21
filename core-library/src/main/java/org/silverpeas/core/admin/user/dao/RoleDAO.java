/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.jetbrains.annotations.NotNull;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RoleDAO {

  private static final String USERROLE_COLUMNS =
      "id,instanceId,name,roleName,description,isInherited,objectId,objecttype";
  private static final String FROM_ST_USERROLE_AND_ST_USERROLE_GROUP_REL =
      " from st_userrole r, st_userrole_group_rel gr";
  private static final String WHERE_ID_EQUALS_USERROLEID = " where r.id=gr.userroleid";
  private static final String AND_GROUP_ID_IN = " and gr.groupId IN (";


  private static String list2String(List<String> ids) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i != 0) {
        str.append(",");
      }
      str.append(ids.get(i));
    }
    return str.toString();
  }

  private static final String SELECT = "select ";
  private static final String FROM_ST_USERROLE_AND_ST_USERROLE_USER_REL =
      " from st_userrole r, st_userrole_user_rel ur";
  private static final String WHERE_ID_EQUALS_TO_USERROLEID = " where r.id=ur.userroleid";
  private static final String AND_USER_ID_EQUALS_TO_GIVEN_VALUE = " and ur.userId = ? ";
  private static final String QUERY_ALL_USER_ROLES =
      SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_USER_REL +
          WHERE_ID_EQUALS_TO_USERROLEID + " and r.objectId is null " +
          AND_USER_ID_EQUALS_TO_GIVEN_VALUE;

  private List<UserRoleRow> getRoles(Connection con, int userId) throws SQLException {
    return getUserRoleRows(con, QUERY_ALL_USER_ROLES, userId);
  }

  private List<UserRoleRow> getUserRoleRows(final Connection con, final String queryAllUserRoles,
      final int userId) throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(queryAllUserRoles)) {
      stmt.setInt(1, userId);
      try (final ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          roles.add(UserRoleRow.fetch(rs));
        }
      }
      return roles;
    }
  }


  /**
   * Gets several groups and/or one user component object roles (not the direct component roles).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con connection with the database.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @return a list of {@link UserRoleRow} instances.
   * @throws SQLException if an error occurs.
   */
  public List<UserRoleRow> getAllComponentObjectRoles(Connection con, List<String> groupIds,
      int userId) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (groupIds != null && !groupIds.isEmpty()) {
      roles.addAll(getAllComponentObjectRolesForGroups(con, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getAllComponentObjectRolesForUser(con, userId));
    }
    return roles;
  }

  private static final String QUERY_ALL_USER_COMPONENT_OBJECT_ROLES =
      SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_USER_REL +
          WHERE_ID_EQUALS_TO_USERROLEID + " and r.objectId is not null " +
          AND_USER_ID_EQUALS_TO_GIVEN_VALUE;

  private List<UserRoleRow> getAllComponentObjectRolesForUser(Connection con, int userId)
      throws SQLException {
    return getUserRoleRows(con, QUERY_ALL_USER_COMPONENT_OBJECT_ROLES, userId);
  }

  private List<UserRoleRow> getRoles(Connection con, List<String> groupIds) throws SQLException {
    final String query = SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_GROUP_REL +
        WHERE_ID_EQUALS_USERROLEID + " and r.objectId is null " + AND_GROUP_ID_IN +
        list2String(groupIds) + ")";

    return getUserRoleRows(con, query);
  }

  @NotNull
  private List<UserRoleRow> getUserRoleRows(final Connection con, final String query)
      throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(query);
         final ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        roles.add(UserRoleRow.fetch(rs));
      }
    }
    return roles;
  }

  private List<UserRoleRow> getAllComponentObjectRolesForGroups(Connection con, List<String> groupIds) throws SQLException {
    String query = SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_GROUP_REL +
        WHERE_ID_EQUALS_USERROLEID + " and r.objectId is not null " + AND_GROUP_ID_IN +
        list2String(groupIds) + ")";

    return getUserRoleRows(con, query);
  }

  public List<UserRoleRow> getRoles(Connection con, List<String> groupIds, int userId)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (groupIds != null && !groupIds.isEmpty()) {
      roles.addAll(getRoles(con, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getRoles(con, userId));
    }
    return roles;
  }

  /**
   * Gets several groups and/or one user roles for the given identifier of component instance
   * (not of objects managed by the component).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con the connection with the database.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @param instanceId the component instance identifier.
   * @return a list of {@link UserRoleRow} instances.
   * @throws SQLException if an error occurs.
   */
  public List<UserRoleRow> getRoles(Connection con, List<String> groupIds, int userId, int instanceId)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (groupIds != null && !groupIds.isEmpty()) {
      roles.addAll(getRoles(con, -1, null, instanceId, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getRoles(con, instanceId, userId));
    }
    return roles;
  }

  /**
   * Gets several groups and/or one user roles for objects of the given identifier of component
   * instance (not directly those of of the component).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con a connection with the database.
   * @param objectId if -1 is given, then all the rows associated to the type are returned
   * @param objectType the type of the object on which the roles are related.
   * @param instanceId the component instance identifier.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @return a list of {@link UserRoleRow} instances.
   * @throws SQLException if an error occurs.
   */
  public List<UserRoleRow> getRoles(Connection con, int objectId, String objectType, int instanceId, List<String> groupIds, int userId) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (groupIds != null && !groupIds.isEmpty()) {
      roles.addAll(getRoles(con, objectId, objectType, instanceId, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getRoles(con, objectId, objectType, instanceId, userId));
    }
    return roles;
  }

  private List<UserRoleRow> getRoles(Connection con, int objectId, String objectType, int instanceId, List<String> groupIds)
      throws SQLException {
    String queryAllAvailableComponentIds =
        SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_GROUP_REL +
            WHERE_ID_EQUALS_USERROLEID;
    if (objectId != -1) {
      queryAllAvailableComponentIds += " and r.objectId = " + objectId;
    }
    if (StringUtil.isDefined(objectType)) {
      queryAllAvailableComponentIds += " and r.objectType = '" + objectType + "'";
    } else {
      queryAllAvailableComponentIds += " and r.objectType is null";
    }
    queryAllAvailableComponentIds +=
        " and r.instanceId = " + instanceId + AND_GROUP_ID_IN + list2String(groupIds) + ")";

    return getUserRoleRows(con, queryAllAvailableComponentIds);
  }

  private static final String QUERY_ALL_USER_ROLES_ON_OBJECT =
      SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_USER_REL +
          WHERE_ID_EQUALS_TO_USERROLEID + " and r.objectId = ? " + " and r.objectType = ? " +
          " and r.instanceId = ? " + AND_USER_ID_EQUALS_TO_GIVEN_VALUE;

  /**
   * Gets the roles associated to the given user about the object represented by an id and a
   * type.<br>
   * When the value {@code -1} is given of the objectId parameter, all the object of given type are
   * aimed.
   * @return a list of user roles.
   * @throws SQLException if an error occurs
   */
  private List<UserRoleRow> getRoles(Connection con, int objectId, String objectType,
      int instanceId, int userId) throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();

    final String sqlQuery;
    if (objectId != -1) {
      sqlQuery = QUERY_ALL_USER_ROLES_ON_OBJECT;
    } else {
      sqlQuery = QUERY_ALL_USER_ROLES_ON_OBJECT.replace(" and r.objectId = ?", "");
    }
    try (final PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
      int index = 0;
      if (objectId != -1) {
        stmt.setInt(++index, objectId);
      }
      stmt.setString(++index, objectType);
      stmt.setInt(++index, instanceId);
      stmt.setInt(++index, userId);

      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          roles.add(UserRoleRow.fetch(rs));
        }
      }
    }
    return roles;
  }

  private static final String QUERY_ALL_USER_ROLES_ON_COMPONENT =
      SELECT + USERROLE_COLUMNS + FROM_ST_USERROLE_AND_ST_USERROLE_USER_REL +
          WHERE_ID_EQUALS_TO_USERROLEID + " and r.instanceId = ? " + " and r.objecttype is null " +
          AND_USER_ID_EQUALS_TO_GIVEN_VALUE;

  private List<UserRoleRow> getRoles(Connection con, int instanceId, int userId)
      throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(QUERY_ALL_USER_ROLES_ON_COMPONENT)) {
      stmt.setInt(1, instanceId);
      stmt.setInt(2, userId);
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          roles.add(UserRoleRow.fetch(rs));
        }
      }
    }
    return roles;
  }
}