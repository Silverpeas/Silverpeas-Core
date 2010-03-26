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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
//TODO : reporter dans CVS (done)
package com.stratelia.webactiv.calendar.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.text.SimpleDateFormat;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.calendar.control.CalendarException;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import java.util.List;

public class JournalDAO {

  public static final String COLUMNNAMES =
      "id, name, delegatorId, description, priority, classification, startDay, startHour, endDay, endHour, externalId";
  private static final String JOURNALCOLUMNNAMES =
      "CalendarJournal.id, CalendarJournal.name, CalendarJournal.delegatorId, CalendarJournal.description, CalendarJournal.priority, "
          + " CalendarJournal.classification, CalendarJournal.startDay, CalendarJournal.startHour, CalendarJournal.endDay, CalendarJournal.endHour, CalendarJournal.externalId";

  private static final String INSERT_JOURNAL = "INSERT INTO CalendarJournal ("
      + COLUMNNAMES + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE_JOURNAL = "UPDATE CalendarJournal SET name = ?, "
      + "delegatorId = ?, description = ?, priority = ?, classification = ?, "
      + "startDay = ?, startHour = ?, endDay = ?, endHour = ?, externalId = ? WHERE id = ?";
  private static final String DELETE_JOURNAL = "DELETE FROM CalendarJournal WHERE id = ?";

  public static String addJournal(Connection con, JournalHeader journal)
      throws SQLException, UtilException, CalendarException {
    PreparedStatement prepStmt = null;
    int id = 0;
    try {
      prepStmt = con.prepareStatement(INSERT_JOURNAL);
      id = DBUtil.getNextId("CalendarJournal", "id");
      prepStmt.setInt(1, id);
      prepStmt.setString(2, journal.getName());
      prepStmt.setString(3, journal.getDelegatorId());
      prepStmt.setString(4, journal.getDescription());
      prepStmt.setInt(5, journal.getPriority().getValue());
      prepStmt.setString(6, journal.getClassification().getString());
      prepStmt.setString(7, journal.getStartDay());
      prepStmt.setString(8, journal.getStartHour());
      prepStmt.setString(9, journal.getEndDay());
      prepStmt.setString(10, journal.getEndHour());
      prepStmt.setString(11, journal.getExternalId());
      if (prepStmt.executeUpdate() == 0)
        throw new CalendarException(
            "JournalDAO.Connection con,  addJournal(Connection con, JournalHeader journal)",
            SilverpeasException.ERROR, "calendar.EX_EXCUTE_INSERT_EMPTY");
    } finally {
      DBUtil.close(prepStmt);
    }
    return String.valueOf(id);
  }

  public static void updateJournal(Connection con, JournalHeader journal)
      throws SQLException, CalendarException {
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(UPDATE_JOURNAL);
      prepStmt.setString(1, journal.getName());
      prepStmt.setString(2, journal.getDelegatorId());
      prepStmt.setString(3, journal.getDescription());
      prepStmt.setInt(4, journal.getPriority().getValue());
      prepStmt.setString(5, journal.getClassification().getString());
      prepStmt.setString(6, journal.getStartDay());
      prepStmt.setString(7, journal.getStartHour());
      prepStmt.setString(8, journal.getEndDay());
      prepStmt.setString(9, journal.getEndHour());
      prepStmt.setString(10, journal.getExternalId());
      prepStmt.setInt(11, new Integer(journal.getId()).intValue());
      if (prepStmt.executeUpdate() == 0)
        throw new CalendarException(
            "JournalDAO.Connection con,  updateJournal(Connection con, JournalHeader journal)",
            SilverpeasException.ERROR, "calendar.EX_EXCUTE_UPDATE_EMPTY");
    } finally {
      DBUtil.close(prepStmt);
    }

  }

  public static void removeJournal(Connection con, String id)
      throws SQLException, CalendarException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(DELETE_JOURNAL);
      prepStmt.setInt(1, new Integer(id).intValue());
      if (prepStmt.executeUpdate() == 0)
        throw new CalendarException(
            "JournalDAO.Connection con,  removeJournal(Connection con, JournalHeader journal)",
            SilverpeasException.ERROR, "calendar.EX_EXCUTE_DELETE_EMPTY");
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static boolean hasTentativeJournalsForUser(Connection con,
      String userId) throws SQLException, java.text.ParseException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = getTentativePreparedStatement(con, userId);
      rs = prepStmt.executeQuery();
      return rs.next();
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection getTentativeJournalHeadersForUser(Connection con,
      String userId) throws SQLException, java.text.ParseException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<JournalHeader> list = new ArrayList<JournalHeader>();
    try {
      prepStmt = getTentativePreparedStatement(con, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  private static PreparedStatement getTentativePreparedStatement(
      Connection con, String userId) throws SQLException {
    String selectStatement = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES
        + " from CalendarJournal, CalendarJournalAttendee "
        + " WHERE (CalendarJournal.id = CalendarJournalAttendee.journalId) "
        + " and (CalendarJournalAttendee.participationStatus = ?) "
        + " and (userId = ?) " + " order by startDay, startHour";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);
    prepStmt.setString(1, ParticipationStatus.TENTATIVE);
    prepStmt.setString(2, userId);
    return prepStmt;
  }

  private static Collection getJournalHeadersForUser(Connection con,
      String day, String userId, String categoryId, String participation,
      String comparator) throws SQLException, java.text.ParseException {
    StringBuffer selectStatement = new StringBuffer();
    selectStatement.append("select distinct ").append(
        JournalDAO.JOURNALCOLUMNNAMES).append(
        " from CalendarJournal, CalendarJournalAttendee ");
    if (categoryId != null)
      selectStatement.append(", CalendarJournalCategory ");

    selectStatement
        .append(" where (CalendarJournal.id = CalendarJournalAttendee.journalId) ");
    selectStatement.append(" and (userId = '").append(userId).append("'");
    selectStatement.append(" and participationStatus = '")
        .append(participation).append("') ");
    if (categoryId != null) {
      selectStatement
          .append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
      selectStatement.append(" and (CalendarJournalCategory.categoryId = '")
          .append(categoryId).append("') ");
    }

    selectStatement.append(" and ((startDay ").append(comparator).append(" '")
        .append(day).append("') or (startDay <= '").append(day).append(
        "' and endDay >= '").append(day).append("')) ");

    if (participation.equals(ParticipationStatus.ACCEPTED)) {
      selectStatement.append("union ").append("select distinct ").append(
          JournalDAO.JOURNALCOLUMNNAMES).append(" from CalendarJournal ");
      if (categoryId != null)
        selectStatement.append(", CalendarJournalCategory ");

      selectStatement.append(" where (delegatorId = '").append(userId).append(
          "') ");

      if (categoryId != null) {
        selectStatement
            .append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
        selectStatement.append(" and (CalendarJournalCategory.categoryId = '")
            .append(categoryId).append("') ");
      }

      selectStatement.append(" and ((startDay ").append(comparator)
          .append(" '").append(day).append("') or (startDay <= '").append(day)
          .append("' and endDay >= '").append(day).append("')) ");
    }

    // selectStatement += " order by startDay, startHour";
    selectStatement.append(" order by 7 , 8 "); // Modif PHiL -> Interbase

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    ArrayList list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static Collection getDayJournalHeadersForUser(Connection con,
      String day, String userId, String categoryId, String participation)
      throws SQLException, java.text.ParseException {
    return getJournalHeadersForUser(con, day, userId, categoryId,
        participation, "=");
  }

  public static Collection getNextJournalHeadersForUser(Connection con,
      String day, String userId, String categoryId, String participation)
      throws SQLException, java.text.ParseException {
    return getJournalHeadersForUser(con, day, userId, categoryId,
        participation, ">=");
  }

  public static Collection countMonthJournalsForUser(Connection con,
      String month, String userId, String categoryId, String participation)
      throws SQLException {
    StringBuffer selectStatement = new StringBuffer(200);
    String theDay = "";
    selectStatement
        .append("select count(distinct CalendarJournal.id), ? from CalendarJournal, CalendarJournalAttendee ");
    if (categoryId != null)
      selectStatement.append(", CalendarJournalCategory ");

    selectStatement
        .append("where (CalendarJournal.id = CalendarJournalAttendee.journalId) ");
    selectStatement.append("and (userId = ").append(userId);
    selectStatement.append(" and participationStatus = '")
        .append(participation).append("')");
    selectStatement
        .append(" and ((startDay = ?) or ((startDay <= ?) and (endDay >= ?))) ");
    if (categoryId != null) {
      selectStatement
          .append(" and (CalendarJournal.id = CalendarJournalCategory.journalId)");
      selectStatement.append(" and (CalendarJournalCategory.categoryId = '")
          .append(categoryId).append("') ");
    }
    selectStatement.append("group by ?");

    if (participation.equals(ParticipationStatus.ACCEPTED)) {
      selectStatement
          .append("union select count(distinct CalendarJournal.id), ? from CalendarJournal ");
      if (categoryId != null)
        selectStatement.append(", CalendarJournalCategory ");
      selectStatement.append("where (delegatorId = '").append(userId).append(
          "')");
      selectStatement
          .append(" and ((startDay = ?) or ((startDay <= ?) and (endDay >= ?)))");
      if (categoryId != null) {
        selectStatement
            .append(" and (CalendarJournal.id = CalendarJournalCategory.journalId)");
        selectStatement.append(" and (CalendarJournalCategory.categoryId = '")
            .append(categoryId).append("') ");
      }
      selectStatement.append("group by ?");
    }
    ArrayList list = new ArrayList();
    int number;
    String date = "";

    PreparedStatement prepStmt = null;

    try {
      ResultSet rs = null;
      prepStmt = con.prepareStatement(selectStatement.toString());
      for (int day = 1; day == 31; day++) {
        if (day < 10)
          theDay = month + "0" + String.valueOf(day);
        else
          theDay = month + String.valueOf(day);
        prepStmt.setString(1, theDay);
        prepStmt.setString(2, theDay);
        prepStmt.setString(3, theDay);
        prepStmt.setString(4, theDay);
        prepStmt.setString(5, theDay);
        prepStmt.setString(6, theDay);
        prepStmt.setString(7, theDay);
        prepStmt.setString(8, theDay);
        prepStmt.setString(9, theDay);
        prepStmt.setString(10, theDay);
        rs = prepStmt.executeQuery();

        while (rs.next()) {
          number = rs.getInt(1);
          date = rs.getString(2);
          SchedulableCount count = new SchedulableCount(number, date);
          list.add(count);
        }
        DBUtil.close(rs);
      }
    } finally {
      DBUtil.close(prepStmt);
    }
    return list;
  }

  public static Collection getPeriodJournalHeadersForUser(Connection con,
      String begin, String end, String userId, String categoryId,
      String participation) throws SQLException, java.text.ParseException {

    StringBuffer selectStatement = new StringBuffer(200);
    selectStatement.append("select distinct ").append(JournalDAO.COLUMNNAMES)
        .append(" from CalendarJournal, CalendarJournalAttendee ");
    if (categoryId != null)
      selectStatement.append(", CalendarJournalCategory ");

    selectStatement
        .append(" where (CalendarJournal.id = CalendarJournalAttendee.journalId) ");
    selectStatement.append(" and (userId = '").append(userId).append("' ");
    selectStatement.append(" and participationStatus = '")
        .append(participation).append("') ");

    if (categoryId != null) {
      selectStatement
          .append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
      selectStatement.append(" and (categoryId = '").append(categoryId).append(
          "') ");
    }

    selectStatement.append(" and ( (startDay >= '").append(begin).append(
        "' and startDay <= '").append(end).append("')");
    selectStatement.append(" or (endDay >= '").append(begin).append(
        "' and endDay <= '").append(end).append("')");
    selectStatement.append(" or ('").append(begin)
        .append("' >= startDay and '").append(begin).append("' <= endDay) ");
    selectStatement.append(" or ('").append(end).append("' >= startDay and '")
        .append(end).append("' <= endDay) ) ");

    if (participation.equals(ParticipationStatus.ACCEPTED)) {
      selectStatement.append(" union select distinct ").append(
          JournalDAO.COLUMNNAMES).append(" from CalendarJournal ");
      if (categoryId != null)
        selectStatement.append(", CalendarJournalCategory ");

      selectStatement.append("where (delegatorId = '").append(userId).append(
          "') ");

      if (categoryId != null) {
        selectStatement
            .append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
        selectStatement.append(" and (categoryId = '").append(categoryId)
            .append("') ");
      }
      selectStatement.append(" and ( (startDay >= '").append(begin).append(
          "' and startDay <= '").append(end).append("')");
      selectStatement.append(" or (endDay >= '").append(begin).append(
          "' and endDay <= '").append(end).append("')");
      selectStatement.append(" or ('").append(begin).append(
          "' >= startDay and '").append(begin).append("' <= endDay) ");
      selectStatement.append(" or ('").append(end)
          .append("' >= startDay and '").append(end).append("' <= endDay) ) ");
    }
    selectStatement.append(" order by 7 , 8 ");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static JournalHeader getJournalHeaderFromResultSet(ResultSet rs)
      throws SQLException, java.text.ParseException {
    String id = "" + rs.getInt(1);
    String name = rs.getString(2);
    String delegatorId = rs.getString(3);
    JournalHeader journal = new JournalHeader(id, name, delegatorId);
    journal.setDescription(rs.getString(4));
    try {
      journal.getPriority().setValue(rs.getInt(5));
    } catch (Exception e) {
      SilverTrace.warn("calendar",
          "JournalDAO.getJournalHeaderFromResultSet(ResultSet rs)",
          "calendar_MSG_NOT_GET_PRIORITY");
    }
    journal.getClassification().setString(rs.getString(6));
    journal.setStartDay(rs.getString(7));
    journal.setStartHour(rs.getString(8));
    journal.setEndDay(rs.getString(9));
    journal.setEndHour(rs.getString(10));
    journal.setExternalId(rs.getString(11));
    return journal;
  }

  public static JournalHeader getJournalHeader(Connection con, String journalId)
      throws SQLException, CalendarException, java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal " + "where id = ?";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    JournalHeader journal;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(journalId).intValue());
      rs = prepStmt.executeQuery();
      if (rs.next())
        journal = getJournalHeaderFromResultSet(rs);
      else {
        throw new CalendarException(
            "JournalDAO.Connection con, String journalId",
            SilverpeasException.ERROR, "calendar.EX_RS_EMPTY", "journalId="
            + journalId);
      }
      return journal;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection getOutlookJournalHeadersForUser(Connection con,
      String userId) throws SQLException, CalendarException,
      java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal "
        + "where delegatorId = ? and externalId is not null";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    Collection list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection getOutlookJournalHeadersForUserAfterDate(
      Connection con, String userId, java.util.Date startDate)
      throws SQLException, CalendarException, java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal "
        + "where delegatorId = ? and startDay >= ? and externalId is not null";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    Collection list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, format.format(startDate));
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection getJournalHeadersForUserAfterDate(Connection con,
      String userId, java.util.Date startDate, int nbReturned)
      throws SQLException, CalendarException, java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal " + "where delegatorId = ? "
        + "and ((startDay >= ?) or (startDay <= ? and endDay >= ?))"
        + " order by startDay, startHour";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String startDateString = DateUtil.date2SQLDate(startDate);
    Collection list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, startDateString);
      prepStmt.setString(3, startDateString);
      prepStmt.setString(4, startDateString);
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next() && nbReturned != 0) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
        nbReturned--;
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}
