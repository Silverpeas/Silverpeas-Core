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

package org.silverpeas.core.calendar.service;

import org.silverpeas.core.calendar.model.Attendee;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AttendeeDAO {

  public static final String JOURNALCOLUMNNAMES = "userId, journalId, participationStatus";
  public static final String TODOCOLUMNNAMES = "userId, todoId, participationStatus";
  public static final String JOURNALTABLENAME = "CalendarJournalAttendee";
  public static final String TODOTABLENAME = "CalendarToDoAttendee";
  public static final String JOURNALIDNAME = "journalId";
  public static final String TODOIDNAME = "todoId";

  public static Attendee getAttendeeFromResultSet(ResultSet rs)
      throws SQLException {
    String userId = rs.getString(1);
    String participation = rs.getString(3);
    Attendee result = new Attendee(userId, participation);
    return result;
  }

  public static Collection<Attendee> getJournalAttendees(Connection con, String journalId)
      throws SQLException {
    return getAttendees(con, journalId, AttendeeDAO.JOURNALCOLUMNNAMES,
        AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  public static Collection<Attendee> getToDoAttendees(Connection con, String todoId)
      throws SQLException {
    return getAttendees(con, todoId, AttendeeDAO.TODOCOLUMNNAMES,
        AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  public static Collection<Attendee> getAttendees(Connection con, String id,
      String columns, String table, String idLabel) throws SQLException {
    String selectStatement = "select " + columns + " from " + table + " "
        + "where " + idLabel + " = " + id;

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Attendee> list = new ArrayList<Attendee>();
    try {
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Attendee attendee = getAttendeeFromResultSet(rs);
        list.add(attendee);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static void addJournalAttendee(Connection con, String journalId,
      Attendee attendee) throws SQLException {
    addAttendee(con, journalId, attendee, AttendeeDAO.JOURNALCOLUMNNAMES,
        AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  public static void addToDoAttendee(Connection con, String todoId,
      Attendee attendee) throws SQLException {
    addAttendee(con, todoId, attendee, AttendeeDAO.TODOCOLUMNNAMES,
        AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  public static void addAttendee(Connection con, String id, Attendee attendee,
      String columns, String table, String idLabel) throws SQLException {
    String insertStatement = "insert into " + table + " (" + columns + ") "
        + " values (?, ?, ?)";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, attendee.getUserId());
      prepStmt.setInt(2, Integer.parseInt(id));
      prepStmt.setString(3, attendee.getParticipationStatus().getString());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeJournalAttendee(Connection con, String journalId,
      Attendee attendee) throws SQLException {
    removeAttendee(con, journalId, attendee, AttendeeDAO.JOURNALCOLUMNNAMES,
        AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  public static void removeToDoAttendee(Connection con, String todoId,
      Attendee attendee) throws SQLException {
    removeAttendee(con, todoId, attendee, AttendeeDAO.TODOCOLUMNNAMES,
        AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  public static void removeAttendee(Connection con, String id,
      Attendee attendee, String columns, String table, String idLabel)
      throws SQLException {
    PreparedStatement prepStmt = null;

    try {
      String statement = "delete from " + table + " " + "where " + idLabel
          + " = ? and userId = ?";
      prepStmt = con.prepareStatement(statement);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.setString(2, attendee.getUserId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeJournal(Connection con, String journalId)
      throws SQLException {
    removeAttendees(con, journalId, AttendeeDAO.JOURNALCOLUMNNAMES,
        AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  public static void removeToDo(Connection con, String todoId)
      throws SQLException {
    removeAttendees(con, todoId, AttendeeDAO.TODOCOLUMNNAMES,
        AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  public static void removeAttendees(Connection con, String id, String columns,
      String table, String idLabel) throws SQLException {
    String statement = "delete from " + table + " " + "where " + idLabel
        + " = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(statement);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}
