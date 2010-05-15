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

package com.silverpeas.notation.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

public class NotationDAO {

  private static final int INITIAL_CAPACITY = 100;

  public static final String TABLE_NAME = "SB_Notation_Notation";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_INSTANCEID = "instanceId";
  public static final String COLUMN_EXTERNALID = "externalId";
  public static final String COLUMN_EXTERNALTYPE = "externalType";
  public static final String COLUMN_AUTHOR = "author";
  public static final String COLUMN_NOTE = "note";
  public static final String COLUMNS = COLUMN_ID + ", " + COLUMN_INSTANCEID
      + ", " + COLUMN_EXTERNALID + ", " + COLUMN_EXTERNALTYPE + ", "
      + COLUMN_AUTHOR + ", " + COLUMN_NOTE;

  private static final String QUERY_CREATE_NOTATION = "INSERT INTO "
      + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)";

  private static final String QUERY_UPDATE_NOTATION = "UPDATE " + TABLE_NAME
      + " SET " + COLUMN_NOTE + " = ?" + " WHERE " + COLUMN_INSTANCEID + " = ?"
      + " AND " + COLUMN_EXTERNALID + " = ?" + " AND " + COLUMN_EXTERNALTYPE
      + " = ?" + " AND " + COLUMN_AUTHOR + " = ?";

  private static final String QUERY_DELETE_NOTATION = "DELETE FROM "
      + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ?" + " AND "
      + COLUMN_EXTERNALID + " = ?" + " AND " + COLUMN_EXTERNALTYPE + " = ?";

  private static final String QUERY_GET_NOTATIONS = "SELECT " + COLUMNS
      + " FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ?"
      + " AND " + COLUMN_EXTERNALID + " = ?" + " AND " + COLUMN_EXTERNALTYPE
      + " = ?";

  private static final String QUERY_COUNT_NOTATIONS = "SELECT COUNT("
      + COLUMN_ID + ") FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID
      + " = ?" + " AND " + COLUMN_EXTERNALID + " = ?" + " AND "
      + COLUMN_EXTERNALTYPE + " = ?";

  private static final String QUERY_HAS_USER_NOTATION = "SELECT " + COLUMN_ID
      + " FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ?"
      + " AND " + COLUMN_EXTERNALID + " = ?" + " AND " + COLUMN_EXTERNALTYPE
      + " = ?" + " AND " + COLUMN_AUTHOR + " = ?";

  private NotationDAO() {
  }

  public static void createNotation(Connection con, NotationPK pk, int note)
      throws SQLException {
    int newId = 0;
    try {
      newId = DBUtil.getNextId(TABLE_NAME, COLUMN_ID);
    } catch (Exception e) {
      SilverTrace.warn("notation", "NotationDAO.createNotation",
          "root.EX_PK_GENERATION_FAILED", e);
    }

    PreparedStatement prepStmt = con.prepareStatement(QUERY_CREATE_NOTATION);
    try {
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, pk.getInstanceId());
      prepStmt.setString(3, pk.getId());
      prepStmt.setInt(4, pk.getType());
      prepStmt.setString(5, pk.getUserId());
      prepStmt.setInt(6, note);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateNotation(Connection con, NotationPK pk, int note)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_UPDATE_NOTATION);
    try {
      prepStmt.setInt(1, note);
      prepStmt.setString(2, pk.getInstanceId());
      prepStmt.setString(3, pk.getId());
      prepStmt.setInt(4, pk.getType());
      prepStmt.setString(5, pk.getUserId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteNotation(Connection con, NotationPK pk)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_DELETE_NOTATION);
    try {
      prepStmt.setString(1, pk.getInstanceId());
      prepStmt.setString(2, pk.getId());
      prepStmt.setInt(3, pk.getType());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static Collection<Notation> getNotations(Connection con, NotationPK pk)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_GET_NOTATIONS);
    prepStmt.setString(1, pk.getInstanceId());
    prepStmt.setString(2, pk.getId());
    prepStmt.setInt(3, pk.getType());
    ResultSet rs = null;

    List<Notation> notations = new ArrayList<Notation>(INITIAL_CAPACITY);
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        notations.add(resultSet2Notation(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return notations;
  }

  public static int countNotations(Connection con, NotationPK pk)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_COUNT_NOTATIONS);
    prepStmt.setString(1, pk.getInstanceId());
    prepStmt.setString(2, pk.getId());
    prepStmt.setInt(3, pk.getType());
    ResultSet rs = null;

    try {
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return 0;
  }

  public static boolean hasUserNotation(Connection con, NotationPK pk)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_HAS_USER_NOTATION);
    prepStmt.setString(1, pk.getInstanceId());
    prepStmt.setString(2, pk.getId());
    prepStmt.setInt(3, pk.getType());
    prepStmt.setString(4, pk.getUserId());
    ResultSet rs = null;

    try {
      rs = prepStmt.executeQuery();
      return (rs.next());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<NotationPK> getNotationPKs(Connection con, NotationPK pk)
      throws SQLException {
    String instanceId = pk.getInstanceId();
    int externalType = pk.getType();

    StringBuffer query = new StringBuffer().append("SELECT ").append(
        COLUMN_EXTERNALID).append(", ").append(COLUMN_EXTERNALTYPE).append(
        " FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(COLUMN_INSTANCEID).append(" = ?");
    if (externalType != Notation.TYPE_UNDEFINED) {
      query.append(" AND ").append(COLUMN_EXTERNALTYPE).append(" = ?");
    }

    PreparedStatement prepStmt = con.prepareStatement(query.toString());
    int index = 1;
    prepStmt.setString(index++, instanceId);
    if (externalType != Notation.TYPE_UNDEFINED) {
      prepStmt.setInt(index++, externalType);
    }

    List<NotationPK> notationPKs = new ArrayList<NotationPK>(INITIAL_CAPACITY);
    ResultSet rs = null;
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        NotationPK notationPK = new NotationPK(rs.getString(COLUMN_EXTERNALID),
            instanceId, rs.getInt(COLUMN_EXTERNALTYPE));
        if (!notationPKs.contains(notationPK)) {
          notationPKs.add(notationPK);
        }
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return notationPKs;
  }

  private static Notation resultSet2Notation(ResultSet rs) throws SQLException {
    return new Notation(rs.getInt(COLUMN_ID), rs.getString(COLUMN_INSTANCEID),
        rs.getString(COLUMN_EXTERNALID), rs.getInt(COLUMN_EXTERNALTYPE), rs
        .getString(COLUMN_AUTHOR), rs.getInt(COLUMN_NOTE));
  }

}