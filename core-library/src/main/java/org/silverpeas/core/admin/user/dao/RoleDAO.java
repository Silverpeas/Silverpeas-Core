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

package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

  static final private String USERROLE_COLUMNS =
      "id,instanceId,name,roleName,description,isInherited,objectId,objecttype";

  public RoleDAO() {

  }

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

  private final static String queryAllUserRoles = "select " + USERROLE_COLUMNS
      + " from st_userrole r, st_userrole_user_rel ur"
      + " where r.id=ur.userroleid"
      + " and r.objectId is null "
      + " and ur.userId = ? ";

  private static List<UserRoleRow> getRoles(Connection con, int userId)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(queryAllUserRoles);
      stmt.setInt(1, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }


  /**
   * Gets several groups and/or one user component object roles (not the direct component roles).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con
   * @param groupIds
   * @param userId
   * @return
   * @throws SQLException
   */
  public static List<UserRoleRow> getAllComponentObjectRoles(Connection con, List<String> groupIds,
      int userId) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();
    if (groupIds != null && groupIds.size() > 0) {
      roles.addAll(getAllComponentObjectRolesForGroups(con, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getAllComponentObjectRolesForUser(con, userId));
    }
    return roles;
  }

  private final static String queryAllUserComponentObjectRoles =
      "select " + USERROLE_COLUMNS + " from st_userrole r, st_userrole_user_rel ur"
      + " where r.id=ur.userroleid"
      + " and r.objectId is not null "
      + " and ur.userId = ? ";

  private static List<UserRoleRow> getAllComponentObjectRolesForUser(Connection con, int userId)
  throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(queryAllUserComponentObjectRoles);
      stmt.setInt(1, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static List<UserRoleRow> getRoles(Connection con, List<String> groupIds)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    String query = "select " + USERROLE_COLUMNS
        + " from st_userrole r, st_userrole_group_rel gr"
        + " where r.id=gr.userroleid"
        + " and r.objectId is null "
        + " and gr.groupId IN (" + list2String(groupIds) + ")";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static List<UserRoleRow> getAllComponentObjectRolesForGroups(Connection con,
      List<String> groupIds) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    String query = "select " + USERROLE_COLUMNS
        + " from st_userrole r, st_userrole_group_rel gr"
        + " where r.id=gr.userroleid"
        + " and r.objectId is not null "
        + " and gr.groupId IN (" + list2String(groupIds) + ")";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<UserRoleRow> getRoles(Connection con, List<String> groupIds, int userId)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();
    if (groupIds != null && groupIds.size() > 0) {
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
   * @param con
   * @param groupIds
   * @param userId
   * @param instanceId
   * @return
   * @throws SQLException
   */
  public static List<UserRoleRow> getRoles(Connection con, List<String> groupIds, int userId,
      int instanceId)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();
    if (groupIds != null && groupIds.size() > 0) {
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
   * @param con
   * @param objectId
   * @param objectType
   * @param instanceId
   * @param groupIds
   * @param userId
   * @return
   * @throws SQLException
   */
  public static List<UserRoleRow> getRoles(Connection con, int objectId, String objectType,
      int instanceId, List<String> groupIds, int userId) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();
    if (groupIds != null && groupIds.size() > 0) {
      roles.addAll(getRoles(con, objectId, objectType, instanceId, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getRoles(con, objectId, objectType, instanceId, userId));
    }
    return roles;
  }

  private static List<UserRoleRow> getRoles(Connection con, int objectId, String objectType,
      int instanceId, List<String> groupIds)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    String queryAllAvailableComponentIds = "select " + USERROLE_COLUMNS
        + " from st_userrole r, st_userrole_group_rel gr"
        + " where r.id=gr.userroleid";
    if (objectId != -1) {
      queryAllAvailableComponentIds += " and r.objectId = " + objectId;
    }
    if (StringUtil.isDefined(objectType)) {
      queryAllAvailableComponentIds += " and r.objectType = '" + objectType + "'";
    } else {
      queryAllAvailableComponentIds += " and r.objectType is null";
    }
    queryAllAvailableComponentIds += " and r.instanceId = " + instanceId
        + " and gr.groupId IN (" + list2String(groupIds) + ")";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();

      rs = stmt.executeQuery(queryAllAvailableComponentIds);

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Fetch the current userRole row from a resultSet.
   */
  private static UserRoleRow fetchUserRole(ResultSet rs) throws SQLException {
    UserRoleRow ur = new UserRoleRow();

    ur.id = rs.getInt(1);
    ur.instanceId = rs.getInt(2);
    ur.name = rs.getString(3);
    ur.roleName = rs.getString(4);
    ur.description = rs.getString(5);
    ur.isInherited = rs.getInt(6);
    ur.objectId = rs.getInt(7);
    ur.objectType = rs.getString(8);

    return ur;
  }

  private final static String queryAllUserRolesOnObject = "select " + USERROLE_COLUMNS
      + " from st_userrole r, st_userrole_user_rel ur"
      + " where r.id=ur.userroleid"
      + " and r.objectId = ? "
      + " and r.objectType = ? "
      + " and r.instanceId = ? "
      + " and ur.userId = ? ";

  private static List<UserRoleRow> getRoles(Connection con, int objectId, String objectType,
      int instanceId, int userId) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(queryAllUserRolesOnObject);
      stmt.setInt(1, objectId);
      stmt.setString(2, objectType);
      stmt.setInt(3, instanceId);
      stmt.setInt(4, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String queryAllUserRolesOnComponent = "select " + USERROLE_COLUMNS
      + " from st_userrole r, st_userrole_user_rel ur"
      + " where r.id=ur.userroleid"
      + " and r.instanceId = ? "
      + " and r.objecttype is null "
      + " and ur.userId = ? ";

  private static List<UserRoleRow> getRoles(Connection con, int instanceId, int userId)
  throws SQLException {
    List<UserRoleRow> roles = new ArrayList<UserRoleRow>();

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(queryAllUserRolesOnComponent);
      stmt.setInt(1, instanceId);
      stmt.setInt(2, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        roles.add(fetchUserRole(rs));
      }

      return roles;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}