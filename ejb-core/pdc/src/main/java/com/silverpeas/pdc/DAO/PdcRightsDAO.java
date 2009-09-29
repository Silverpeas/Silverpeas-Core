package com.silverpeas.pdc.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.stratelia.webactiv.util.DBUtil;

/**
 * @author inra
 * 
 */
public class PdcRightsDAO {

  public static ArrayList getUserIds(Connection con, String axisId,
      String valueId) throws SQLException {
    ArrayList listUsersIds = new ArrayList();
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

    StringBuffer clauseIN = new StringBuffer("(");
    boolean firstGroup = true;
    for (int i = 0; i < groupIds.length; i++) {
      if (!firstGroup) {
        clauseIN.append(",");
        firstGroup = false;
      }

      clauseIN.append(groupIds[i]);
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

  public static ArrayList getGroupIds(Connection con, String axisId,
      String valueId) throws SQLException {
    ArrayList listGroupsIds = new ArrayList();
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
   * Lors de la supression d'un user, on supprime les droits associés
   * 
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