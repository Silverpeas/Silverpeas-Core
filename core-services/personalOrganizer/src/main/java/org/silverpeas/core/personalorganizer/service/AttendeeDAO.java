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
package org.silverpeas.core.personalorganizer.service;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.util.MapUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class AttendeeDAO {

  private static final String JOURNALCOLUMNNAMES = "userId, journalId, participationStatus";
  private static final String TODOCOLUMNNAMES = "userId, todoId, participationStatus";
  private static final String JOURNALTABLENAME = "CalendarJournalAttendee";
  private static final String TODOTABLENAME = "CalendarToDoAttendee";
  private static final String JOURNALIDNAME = "journalId";
  private static final String TODOIDNAME = "todoId";

  private AttendeeDAO() {
    throw new IllegalAccessError("Utility class");
  }

  private static Attendee getAttendeeFromResultSet(ResultSet rs)
      throws SQLException {
    String userId = rs.getString(1);
    String participation = rs.getString(3);
    return new Attendee(userId, participation);
  }

  static Collection<Attendee> getJournalAttendees(String journalId)
      throws SQLException {
    return getJournalAttendees(singletonList(journalId)).getOrDefault(journalId, emptyList());
  }

  static Map<String, List<Attendee>> getJournalAttendees(List<String> journalIds)
      throws SQLException {
    return getAttendees(journalIds, AttendeeDAO.JOURNALCOLUMNNAMES,
        AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  static List<Attendee> getToDoAttendees(String todoId)
      throws SQLException {
    return getToDoAttendees(singletonList(todoId)).getOrDefault(todoId, emptyList());
  }

  static Map<String, List<Attendee>> getToDoAttendees(List<String> todoIds) throws SQLException {
    return getAttendees(todoIds, AttendeeDAO.TODOCOLUMNNAMES,
        AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  private static Map<String, List<Attendee>> getAttendees(List<String> ids, String columns,
      String table, String idLabel) throws SQLException {
    return JdbcSqlQuery.executeBySplittingOn(ids, (idBatch, result)-> JdbcSqlQuery
        .createSelect(columns)
        .from(table)
        .where(idLabel).in(idBatch.stream().map(Integer::parseInt).collect(Collectors.toList()))
        .execute(r ->  {
          final String id = r.getString(2);
          final Attendee attendee = getAttendeeFromResultSet(r);
          MapUtil.putAddList(result, id, attendee);
          return null;
        }));
  }

  static void addJournalAttendee(Connection con, String journalId, Attendee attendee)
      throws SQLException {
    addAttendee(con, journalId, attendee, JOURNALCOLUMNNAMES, JOURNALTABLENAME);
  }

  static void addToDoAttendee(Connection con, String todoId, Attendee attendee)
      throws SQLException {
    addAttendee(con, todoId, attendee, TODOCOLUMNNAMES, TODOTABLENAME);
  }

  private static void addAttendee(Connection con, String id, Attendee attendee, String columns,
      String table) throws SQLException {
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

  static void removeJournalAttendee(Connection con, String journalId, Attendee attendee)
      throws SQLException {
    removeAttendee(con, journalId, attendee, JOURNALTABLENAME, JOURNALIDNAME);
  }

  static void removeToDoAttendee(Connection con, String todoId, Attendee attendee)
      throws SQLException {
    removeAttendee(con, todoId, attendee, TODOTABLENAME, TODOIDNAME);
  }

  private static void removeAttendee(Connection con, String id, Attendee attendee, String table,
      String idLabel)
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

  public static void removeJournal(String journalId)
      throws SQLException {
    removeAttendees(journalId, AttendeeDAO.JOURNALTABLENAME, AttendeeDAO.JOURNALIDNAME);
  }

  static void removeToDo(String todoId)
      throws SQLException {
    removeAttendees(todoId, AttendeeDAO.TODOTABLENAME, AttendeeDAO.TODOIDNAME);
  }

  private static void removeAttendees(String id, String table, String idLabel)
      throws SQLException {
    JdbcSqlQuery.createDeleteFor(table).where(idLabel + " = ?", Integer.parseInt(id)).execute();
  }
}
