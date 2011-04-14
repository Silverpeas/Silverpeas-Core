/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.webactiv.beans.admin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.organization.UserRoleRow;
import com.stratelia.webactiv.util.DBUtil;

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

  public static List<UserRoleRow> getRoles(Connection con, int objectId,
      String objectType, int instanceId, List<String> groupIds, int userId) throws SQLException {
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
        + " where r.id=gr.userroleid"
        + " and r.objectId = " + objectId
        + " and r.objectType = '" + objectType + "'"
        + " and r.instanceId = " + instanceId
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

  private final static String queryUserIdsOfRole = "select userid"
      + " from st_userrole_user_rel"
      + " where userroleid = ? ";

  public static List<String> getUserIdsOfRole(Connection con, int roleId) throws SQLException {
    List<String> ids = new ArrayList<String>();

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(queryUserIdsOfRole);
      stmt.setInt(1, roleId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}