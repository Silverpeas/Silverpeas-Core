package com.stratelia.webactiv.calendar.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.DBUtil;

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

  public static Collection getJournalAttendees(Connection con, String journalId)
      throws SQLException {
    return getAttendees(con, journalId, AttendeeDAO.JOURNALCOLUMNNAMES,
        AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  public static Collection getToDoAttendees(Connection con, String todoId)
      throws SQLException {
    return getAttendees(con, todoId, AttendeeDAO.TODOCOLUMNNAMES,
        AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  public static Collection getAttendees(Connection con, String id,
      String columns, String table, String idLabel) throws SQLException {
    String selectStatement = "select " + columns + " from " + table + " "
        + "where " + idLabel + " = " + id;

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList list = new ArrayList();
    try {
      prepStmt = con.prepareStatement(selectStatement);
      // prepStmt.setInt(1, new Integer(id).intValue());
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
      prepStmt.setInt(2, new Integer(id).intValue());
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
      prepStmt.setInt(1, new Integer(id).intValue());
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
      prepStmt.setInt(1, new Integer(id).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}
