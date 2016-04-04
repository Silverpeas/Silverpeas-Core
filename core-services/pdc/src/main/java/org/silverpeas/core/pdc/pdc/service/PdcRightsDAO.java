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

package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author inra
 */
public class PdcRightsDAO {

  public static ArrayList<String> getUserIds(Connection con, String axisId,
      String valueId) throws SQLException {
    ArrayList<String> listUsersIds = new ArrayList<String>();
    String query = "select userId from sb_pdc_user_rights where valueid = ? and axisId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(valueId));
      prepStmt.setInt(2, Integer.parseInt(axisId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        listUsersIds.add(Integer.toString(rs.getInt(1)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listUsersIds;
  }

  public static boolean isUserManager(Connection con, String userId)
      throws SQLException {
    String query = "select userId from sb_pdc_user_rights where userid = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      return (rs.next());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static boolean isGroupManager(Connection con, String[] groupIds)
      throws SQLException {
    if (groupIds == null || groupIds.length == 0)
      return false;

    StringBuilder clauseIN = new StringBuilder("(");
    boolean firstGroup = true;
    for (String groupId : groupIds) {
      if (!firstGroup) {
        clauseIN.append(",");
        firstGroup = false;
      }

      clauseIN.append(groupId);
    }
    clauseIN.append(")");

    String query = "select groupId from sb_pdc_group_rights where groupid IN "
        + clauseIN.toString();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(query);
      rs = prepStmt.executeQuery();
      return (rs.next());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static ArrayList<String> getGroupIds(Connection con, String axisId,
      String valueId) throws SQLException {
    ArrayList<String> listGroupsIds = new ArrayList<String>();
    String query = "select groupid from sb_pdc_group_rights where valueid = ? and axisId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(valueId));
      prepStmt.setInt(2, Integer.parseInt(axisId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        listGroupsIds.add(Integer.toString(rs.getInt(1)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listGroupsIds;
  }

  public static void deleteRights(Connection con, String axisId, String valueId)
      throws SQLException {
    deleteUserRights(con, axisId, valueId);
    deleteGroupRights(con, axisId, valueId);
  }

  private static void deleteUserRights(Connection con, String axisId,
      String valueId) throws SQLException {
    String query = "delete from sb_pdc_user_rights where valueid = ? and axisId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(valueId));
      prepStmt.setInt(2, Integer.parseInt(axisId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static void deleteGroupRights(Connection con, String axisId,
      String valueId) throws SQLException {
    String query = "delete from sb_pdc_group_rights where valueid = ? and axisId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(valueId));
      prepStmt.setInt(2, Integer.parseInt(axisId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void insertUserId(Connection con, String axisId,
      String valueId, String uid) throws SQLException {
    String query = "insert into sb_pdc_user_rights values (?, ?, ?)";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(axisId));
      prepStmt.setInt(2, Integer.parseInt(valueId));
      prepStmt.setInt(3, Integer.parseInt(uid));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void insertGroupId(Connection con, String axisId,
      String valueId, String gid) throws SQLException {
    String query = "insert into sb_pdc_group_rights values (?, ?, ?)";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(axisId));
      prepStmt.setInt(2, Integer.parseInt(valueId));
      prepStmt.setInt(3, Integer.parseInt(gid));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Lors de la supression d'un user, on supprime les droits associes
   * @param userId
   * @throws SQLException
   */
  public static void deleteManager(Connection con, String userId)
      throws SQLException {
    String query = "delete from sb_pdc_user_rights where userId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(userId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteGroupManager(Connection con, String groupId)
      throws SQLException {
    String query = "delete from sb_pdc_group_rights where groupId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(groupId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteAxisRights(Connection con, String axisId)
      throws SQLException {
    String query = "delete from sb_pdc_user_rights where axisId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(axisId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    query = "delete from sb_pdc_group_rights where axisId = ? ";

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(axisId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}