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
//TODO : reporter dans CVS (done)
package com.stratelia.webactiv.calendar.model;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.calendar.socialNetwork.SocialInformationEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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

  public String addJournal(Connection con, JournalHeader journal)
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
      if (prepStmt.executeUpdate() == 0) {
        throw new CalendarException(
            "JournalDAO.Connection con,  addJournal(Connection con, JournalHeader journal)",
            SilverpeasException.ERROR, "calendar.EX_EXCUTE_INSERT_EMPTY");
      }
    } finally {
      DBUtil.close(prepStmt);
    }
    return String.valueOf(id);
  }

  public void updateJournal(Connection con, JournalHeader journal)
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
      if (prepStmt.executeUpdate() == 0) {
        throw new CalendarException(
            "JournalDAO.Connection con,  updateJournal(Connection con, JournalHeader journal)",
            SilverpeasException.ERROR, "calendar.EX_EXCUTE_UPDATE_EMPTY");
      }
    } finally {
      DBUtil.close(prepStmt);
    }

  }

  public void removeJournal(Connection con, String id)
      throws SQLException, CalendarException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(DELETE_JOURNAL);
      prepStmt.setInt(1, new Integer(id).intValue());
      if (prepStmt.executeUpdate() == 0) {
        throw new CalendarException(
            "JournalDAO.Connection con,  removeJournal(Connection con, JournalHeader journal)",
            SilverpeasException.ERROR, "calendar.EX_EXCUTE_DELETE_EMPTY");
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public boolean hasTentativeJournalsForUser(Connection con,
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

  public Collection<JournalHeader> getTentativeJournalHeadersForUser(Connection con,
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

  private PreparedStatement getTentativePreparedStatement(
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

  private Collection<JournalHeader> getJournalHeadersForUser(Connection con,
      String day, String userId, String categoryId, String participation,
      String comparator) throws SQLException, java.text.ParseException {
    StringBuilder selectStatement = new StringBuilder();
    selectStatement.append("select distinct ").append(
        JournalDAO.JOURNALCOLUMNNAMES).append(
        " from CalendarJournal, CalendarJournalAttendee ");
    if (categoryId != null) {
      selectStatement.append(", CalendarJournalCategory ");
    }

    selectStatement.append(" where (CalendarJournal.id = CalendarJournalAttendee.journalId) ");
    selectStatement.append(" and (userId = '").append(userId).append("'");
    selectStatement.append(" and participationStatus = '").append(participation).append("') ");
    if (categoryId != null) {
      selectStatement.append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
      selectStatement.append(" and (CalendarJournalCategory.categoryId = '").append(categoryId).
          append("') ");
    }

    selectStatement.append(" and ((startDay ").append(comparator).append(" '").append(day).append(
        "') or (startDay <= '").append(day).append(
        "' and endDay >= '").append(day).append("')) ");

    if (participation.equals(ParticipationStatus.ACCEPTED)) {
      selectStatement.append("union ").append("select distinct ").append(
          JournalDAO.JOURNALCOLUMNNAMES).append(" from CalendarJournal ");
      if (categoryId != null) {
        selectStatement.append(", CalendarJournalCategory ");
      }

      selectStatement.append(" where (delegatorId = '").append(userId).append(
          "') ");

      if (categoryId != null) {
        selectStatement.append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
        selectStatement.append(" and (CalendarJournalCategory.categoryId = '").append(categoryId).
            append("') ");
      }

      selectStatement.append(" and ((startDay ").append(comparator).append(" '").append(day).append(
          "') or (startDay <= '").append(day).append("' and endDay >= '").append(day).append("')) ");
    }

    selectStatement.append(" order by 7 , 8 "); // Modif PHiL -> Interbase

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      list = new ArrayList<JournalHeader>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public Collection<JournalHeader> getDayJournalHeadersForUser(Connection con,
      String day, String userId, String categoryId, String participation)
      throws SQLException, java.text.ParseException {
    return getJournalHeadersForUser(con, day, userId, categoryId,
        participation, "=");
  }

  public Collection<JournalHeader> getNextJournalHeadersForUser(Connection con,
      String day, String userId, String categoryId, String participation)
      throws SQLException, java.text.ParseException {
    return getJournalHeadersForUser(con, day, userId, categoryId,
        participation, ">=");
  }

  /**
   * get next JournalHeader for this user accordint to
   * the type of data base used(PostgreSQL,Oracle,MMS)
   * @param con
   * @param day
   * @param userId
   * @param classification
   * @param limit
   * @param offset
   * @return
   * @throws SQLException
   * @throws java.text.ParseException
   */
  public List<JournalHeader> getNextEventsForUser(Connection con,
      String day, String userId, String classification, int limit, int offset)
      throws SQLException, java.text.ParseException {

    String databaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (databaseProductName.toUpperCase().contains("POSTGRESQL")) {
      return getNextCalendarJournalForUser_PostgreSQL(con, day, userId, classification, limit,
          offset);
    } else if (databaseProductName.toUpperCase().contains("ORACLE")) {
      return getNextCalendarJournalForUser_Oracle(con, day, userId, classification, limit,
          offset);
    }
    return getNextCalendarJournalForUser_MMS(con, day, userId, classification, limit,
        offset);
  }

  public Collection<SchedulableCount> countMonthJournalsForUser(Connection con,
      String month, String userId, String categoryId, String participation)
      throws SQLException {
    StringBuilder selectStatement = new StringBuilder(200);
    String theDay = "";
    selectStatement.append(
        "select count(distinct CalendarJournal.id), ? from CalendarJournal, CalendarJournalAttendee ");
    if (categoryId != null) {
      selectStatement.append(", CalendarJournalCategory ");
    }

    selectStatement.append("where (CalendarJournal.id = CalendarJournalAttendee.journalId) ");
    selectStatement.append("and (userId = ").append(userId);
    selectStatement.append(" and participationStatus = '").append(participation).append("')");
    selectStatement.append(" and ((startDay = ?) or ((startDay <= ?) and (endDay >= ?))) ");
    if (categoryId != null) {
      selectStatement.append(" and (CalendarJournal.id = CalendarJournalCategory.journalId)");
      selectStatement.append(" and (CalendarJournalCategory.categoryId = '").append(categoryId).
          append("') ");
    }
    selectStatement.append("group by ?");

    if (participation.equals(ParticipationStatus.ACCEPTED)) {
      selectStatement.append(
          "union select count(distinct CalendarJournal.id), ? from CalendarJournal ");
      if (categoryId != null) {
        selectStatement.append(", CalendarJournalCategory ");
      }
      selectStatement.append("where (delegatorId = '").append(userId).append(
          "')");
      selectStatement.append(" and ((startDay = ?) or ((startDay <= ?) and (endDay >= ?)))");
      if (categoryId != null) {
        selectStatement.append(" and (CalendarJournal.id = CalendarJournalCategory.journalId)");
        selectStatement.append(" and (CalendarJournalCategory.categoryId = '").append(categoryId).
            append("') ");
      }
      selectStatement.append("group by ?");
    }
    List<SchedulableCount> list = new ArrayList<SchedulableCount>();
    int number;
    String date = "";

    PreparedStatement prepStmt = null;

    try {
      ResultSet rs = null;
      prepStmt = con.prepareStatement(selectStatement.toString());
      for (int day = 1; day == 31; day++) {
        if (day < 10) {
          theDay = month + "0" + String.valueOf(day);
        } else {
          theDay = month + String.valueOf(day);
        }
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

  public Collection<JournalHeader> getPeriodJournalHeadersForUser(Connection con,
      String begin, String end, String userId, String categoryId,
      String participation) throws SQLException, java.text.ParseException {

    StringBuilder selectStatement = new StringBuilder(200);
    selectStatement.append("select distinct ").append(JournalDAO.COLUMNNAMES).append(
        " from CalendarJournal, CalendarJournalAttendee ");
    if (categoryId != null) {
      selectStatement.append(", CalendarJournalCategory ");
    }

    selectStatement.append(" where (CalendarJournal.id = CalendarJournalAttendee.journalId) ");
    selectStatement.append(" and (userId = '").append(userId).append("' ");
    selectStatement.append(" and participationStatus = '").append(participation).append("') ");

    if (categoryId != null) {
      selectStatement.append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
      selectStatement.append(" and (categoryId = '").append(categoryId).append(
          "') ");
    }

    selectStatement.append(" and ( (startDay >= '").append(begin).append(
        "' and startDay <= '").append(end).append("')");
    selectStatement.append(" or (endDay >= '").append(begin).append(
        "' and endDay <= '").append(end).append("')");
    selectStatement.append(" or ('").append(begin).append("' >= startDay and '").append(begin).
        append("' <= endDay) ");
    selectStatement.append(" or ('").append(end).append("' >= startDay and '").append(end).append(
        "' <= endDay) ) ");

    if (participation.equals(ParticipationStatus.ACCEPTED)) {
      selectStatement.append(" union select distinct ").append(
          JournalDAO.COLUMNNAMES).append(" from CalendarJournal ");
      if (categoryId != null) {
        selectStatement.append(", CalendarJournalCategory ");
      }

      selectStatement.append("where (delegatorId = '").append(userId).append(
          "') ");

      if (categoryId != null) {
        selectStatement.append(" and (CalendarJournal.id = CalendarJournalCategory.journalId) ");
        selectStatement.append(" and (categoryId = '").append(categoryId).append("') ");
      }
      selectStatement.append(" and ( (startDay >= '").append(begin).append(
          "' and startDay <= '").append(end).append("')");
      selectStatement.append(" or (endDay >= '").append(begin).append(
          "' and endDay <= '").append(end).append("')");
      selectStatement.append(" or ('").append(begin).append(
          "' >= startDay and '").append(begin).append("' <= endDay) ");
      selectStatement.append(" or ('").append(end).append("' >= startDay and '").append(end).append(
          "' <= endDay) ) ");
    }
    selectStatement.append(" order by 7 , 8 ");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      list = new ArrayList<JournalHeader>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public JournalHeader getJournalHeaderFromResultSet(ResultSet rs)
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

  public JournalHeader getJournalHeader(Connection con, String journalId)
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
      if (rs.next()) {
        journal = getJournalHeaderFromResultSet(rs);
      } else {
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

  public Collection<JournalHeader> getOutlookJournalHeadersForUser(Connection con,
      String userId) throws SQLException, CalendarException,
      java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal "
        + "where delegatorId = ? and externalId is not null";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    Collection<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<JournalHeader>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public Collection<JournalHeader> getOutlookJournalHeadersForUserAfterDate(
      Connection con, String userId, java.util.Date startDate)
      throws SQLException, CalendarException, java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal "
        + "where delegatorId = ? and startDay >= ? and externalId is not null";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    Collection<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, format.format(startDate));
      rs = prepStmt.executeQuery();
      list = new ArrayList<JournalHeader>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public Collection<JournalHeader> getJournalHeadersForUserAfterDate(Connection con,
      String userId, java.util.Date startDate, int nbReturned)
      throws SQLException, CalendarException, java.text.ParseException {

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal " + "where delegatorId = ? "
        + "and ((startDay >= ?) or (startDay <= ? and endDay >= ?))"
        + " order by startDay, startHour";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String startDateString = DateUtil.date2SQLDate(startDate);
    Collection<JournalHeader> list = null;
    try {
      int count = 0;
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, startDateString);
      prepStmt.setString(3, startDateString);
      prepStmt.setString(4, startDateString);
      rs = prepStmt.executeQuery();
      list = new ArrayList<JournalHeader>();
      while (rs.next() && nbReturned != count) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
        count++;
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * get next JournalHeader for this user when data base is PostgreSQL
   * @param con
   * @param day
   * @param userId
   * @param limit
   * @param offset
   * @return List<JournalHeader>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<JournalHeader> getNextCalendarJournalForUser_PostgreSQL(Connection con,
      String day, String userId, String classification, int limit, int offset) throws SQLException,
      java.text.ParseException {
    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where  delegatorId = ? and endDay >= ? ";
    int classificationIndex = 2;
    int limitIndex = 3;
    if (StringUtil.isDefined(classification)) {
      selectNextEvents += " and classification = ? ";
      classificationIndex++;
      limitIndex++;
    }
    selectNextEvents += " order by CalendarJournal.startDay, CalendarJournal.startHour ";

    if (limit > 0) {
      selectNextEvents += "  limit  ?  offset  ? ";
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, day);
      if (classificationIndex == 3)//Classification param not null
      {
        prepStmt.setString(classificationIndex, classification);
      }
      if (limit > 0)//limit para >0 do the search with limit the result
      {
        prepStmt.setInt(limitIndex, limit);
        prepStmt.setInt(limitIndex + 1, offset);
      }
      rs = prepStmt.executeQuery();
      list = new ArrayList<JournalHeader>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get next JournalHeader for this user when data base is Oracle
   * @param con
   * @param day
   * @param userId
   * @param limit
   * @param offset
   * @return List<JournalHeader>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<JournalHeader> getNextCalendarJournalForUser_Oracle(Connection con,
      String day, String userId, String classification, int limit, int offset) throws SQLException,
      java.text.ParseException {
    String selectNextEventsOracle = " select * from (ROWNUM num , table_oracle.* from (";
    String selectNextEvents = " select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where  delegatorId = ? and endDay >= ? ";
    int classificationIndex = 2, limitIndex = 3;
    if (StringUtil.isDefined(classification)) {
      selectNextEvents += " and classification = ? ";
      classificationIndex++;
      limitIndex++;
    }
    selectNextEvents += " order by CalendarJournal.startDay, CalendarJournal.startHour ";

    selectNextEventsOracle += " " + selectNextEvents;
    if (limit > 0) {
      selectNextEventsOracle += "  ) table_oracle) where num between ? and ? ";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEventsOracle);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, day);
      if (classificationIndex == 3)//Classification para not null
      {
        prepStmt.setString(classificationIndex, classification);
      }
      if (limit > 0)//limit para >0 do the search with limit the result
      {
        prepStmt.setInt(limitIndex, limit);
        prepStmt.setInt(limitIndex + 1, offset + 1);//firstIndex=offset+1;
      }

      rs = prepStmt.executeQuery();

      list = new ArrayList<JournalHeader>();

      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(journal);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get next JournalHeader for this user when data base is MMS
   * @param con
   * @param day
   * @param userId
   * @param classification
   * @param limit
   * @param offset
   * @return List<JournalHeader>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<JournalHeader> getNextCalendarJournalForUser_MMS(Connection con,
      String day, String userId, String classification, int limit, int offset) throws SQLException,
      java.text.ParseException {
    String SelectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where  delegatorId = ? and endDay >= ? ";
    int classificationIndex = 2;
    if (StringUtil.isDefined(classification)) {
      SelectNextEvents += " and classification = ? ";
      classificationIndex++;
    }
    SelectNextEvents += " order by CalendarJournal.startDay, CalendarJournal.startHour ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<JournalHeader> list = null;
    try {
      prepStmt = con.prepareStatement(SelectNextEvents);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, day);
      if (classificationIndex == 3)//Classification para not null
      {
        prepStmt.setString(classificationIndex, classification);
      }
      rs = prepStmt.executeQuery();

      list = new ArrayList<JournalHeader>();
      int index = 0;
      while (rs.next()) {
        // limit the searche
        if (index >= offset && index < limit + offset) {
          JournalHeader journal = getJournalHeaderFromResultSet(rs);
          list.add(journal);
        }
        index++;
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get Next Social Events for a given list of my Contacts accordint to
   * the type of data base used(PostgreSQL,Oracle,MMS) .
   * This includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws ParseException
   */
  public List<SocialInformationEvent> getNextEventsForMyContacts(Connection con, String day,
      String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws SQLException,
      ParseException {
    String databaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (databaseProductName.contains("POSTGRESQL")) {
      return getNextEventsForMyContacts_PostgreSQL(con, day, myId, myContactsIds, numberOfElement,
          firstIndex);
    } else if (databaseProductName.toUpperCase().contains("ORACLE")) {
      return getNextEventsForMyContacts_Oracle(con, day, myId, myContactsIds, numberOfElement,
          firstIndex);
    }
    return getNextEventsForMyContacts_MSS(con, day, myId, myContactsIds, numberOfElement, firstIndex);
  }

  private static String listToSqlString(List<String> list) {
    String result = "";
    if (list == null || list.isEmpty()) {
      return "''";
    }
    int size = list.size();
    int i = 0;
    for (String var : list) {
      i++;
      result += "'" + var + "'";
      if (i != size) {
        result += ",";
      }
    }
    return result;
  }

  /**
   * get Next Social Events for a given list of my Contacts when data base is PostgreSQL. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<SocialInformationEvent> getNextEventsForMyContacts_PostgreSQL(Connection con,
      String day, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws
      SQLException,
      java.text.ParseException {
    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay >= ? and delegatorId in(" + listToSqlString(myContactsIds) + ") order by CalendarJournal.startDay, CalendarJournal.startHour ";
    System.out.println("selectNextEvents=" + selectNextEvents);
    if (numberOfElement > 0) {
      selectNextEvents += "  limit  ?  offset  ?";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      if (numberOfElement > 0) {
        prepStmt.setInt(2, numberOfElement);
        prepStmt.setInt(3, firstIndex);
      }

      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(new SocialInformationEvent(journal, journal.getId().equals(myId)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get Next Social Events for a given list of my Contacts when data base is oracle. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<SocialInformationEvent> getNextEventsForMyContacts_Oracle(Connection con,
      String day, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws
      SQLException,
      java.text.ParseException {

    String selectNextEvents = "select * from (ROWNUM num , table_oracle.* from (select distinct "
        + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay >= ? and delegatorId in(" + listToSqlString(myContactsIds) + ") order by CalendarJournal.startDay, CalendarJournal.startHour  ) table_oracle) ";

    if (numberOfElement > 0) {
      selectNextEvents += " where num between ? and ? ";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      if (numberOfElement > 0) {
        prepStmt.setInt(2, numberOfElement);
        prepStmt.setInt(3, firstIndex);
      }
      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(new SocialInformationEvent(journal, journal.getId().equals(myId)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get Next Social Events for a given list of my Contacts when data base is MMS. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  public List<SocialInformationEvent> getNextEventsForMyContacts_MSS(Connection con,
      String day, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws
      SQLException,
      java.text.ParseException {

    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay >= ? and delegatorId in(" + listToSqlString(myContactsIds) + ") order by CalendarJournal.startDay, CalendarJournal.startHour ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      int index = 0;
      while (rs.next()) {
        // limit the searche
        if ((index >= firstIndex && index < numberOfElement + firstIndex) || numberOfElement <= 0) {
          JournalHeader journal = getJournalHeaderFromResultSet(rs);
          list.add(new SocialInformationEvent(journal, journal.getId().equals(myId)));
        }
        index++;
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get Last Social Events for a given list of my Contacts accordint to
   * the type of data base used(PostgreSQL,Oracle,MMS) .
   * This includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws ParseException
   */
  public List<SocialInformationEvent> getLastEventsForMyContacts(Connection con, String day,
      String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws SQLException,
      ParseException {
    String databaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (databaseProductName.contains("POSTGRESQL")) {
      return getLastEventsForMyContacts_PostgreSQL(con, day, myId, myContactsIds, numberOfElement,
          firstIndex);
    } else if (databaseProductName.toUpperCase().contains("ORACLE")) {
      return getLastEventsForMyContacts_Oracle(con, day, myId, myContactsIds, numberOfElement,
          firstIndex);
    }
    return getLastEventsForMyContacts_MSS(con, day, myId, myContactsIds, numberOfElement, firstIndex);
  }

  /**
   * get Last Social Events for a given list of my Contacts when data base is PostgreSQL. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<SocialInformationEvent> getLastEventsForMyContacts_PostgreSQL(Connection con,
      String day, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws
      SQLException,
      java.text.ParseException {
    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay < ? and delegatorId in(" + listToSqlString(myContactsIds) + ") order by CalendarJournal.startDay desc, CalendarJournal.startHour desc ";
    if (numberOfElement > 0) {
      selectNextEvents += "  limit  ?  offset  ?";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      if (numberOfElement > 0) {
        prepStmt.setInt(2, numberOfElement);
        prepStmt.setInt(3, firstIndex);
      }

      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(new SocialInformationEvent(journal, journal.getId().equals(myId)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get Last Social Events for a given list of my Contacts when data base is Oracle. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<SocialInformationEvent> getLastEventsForMyContacts_Oracle(Connection con,
      String day, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws
      SQLException,
      java.text.ParseException {

    String selectNextEvents = "select * from (ROWNUM num , table_oracle.* from (select distinct "
        + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay < ? and delegatorId in(" + listToSqlString(myContactsIds) + ") order by CalendarJournal.startDay desc , CalendarJournal.startHour desc  ) table_oracle) ";

    if (numberOfElement > 0) {
      selectNextEvents += " where num between ? and ? ";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      if (numberOfElement > 0) {
        prepStmt.setInt(2, numberOfElement);
        prepStmt.setInt(3, firstIndex);
      }
      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(new SocialInformationEvent(journal, journal.getId().equals(myId)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get Last Social Events for a given list of my Contacts when data base is MMS. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  public List<SocialInformationEvent> getLastEventsForMyContacts_MSS(Connection con,
      String day, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws
      SQLException,
      java.text.ParseException {

    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay < ? and delegatorId in(" + listToSqlString(myContactsIds) + ") order by CalendarJournal.startDay desc, CalendarJournal.startHour desc ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      int index = 0;
      while (rs.next()) {
        // limit the searche
        if ((index >= firstIndex && index < numberOfElement + firstIndex) || numberOfElement <= 0) {
          JournalHeader journal = getJournalHeaderFromResultSet(rs);
          list.add(new SocialInformationEvent(journal, journal.getId().equals(myId)));
        }
        index++;
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get my Last Social Events accordint to
   * the type of data base used(PostgreSQL,Oracle,MMS) .
   * This includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws ParseException
   */
  public List<SocialInformationEvent> getMyLastEvents(Connection con, String day, String myId,
      int numberOfElement, int firstIndex) throws SQLException,
      ParseException {
    String databaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (databaseProductName.contains("POSTGRESQL")) {
      return getMyLastEvents_PostgreSQL(con, day, myId, numberOfElement,
          firstIndex);
    } else if (databaseProductName.toUpperCase().contains("ORACLE")) {
      return getMyLastEvents_Oracle(con, day, myId, numberOfElement, firstIndex);
    }
    return getMyLastEvents_MSS(con, day, myId, numberOfElement, firstIndex);
  }

  /**
   * get Last Social Events for a given list of my Contacts when data base is PostgreSQL. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<SocialInformationEvent> getMyLastEvents_PostgreSQL(Connection con,
      String day, String myId, int numberOfElement, int firstIndex) throws
      SQLException,
      java.text.ParseException {
    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay < ? and delegatorId = ? order by CalendarJournal.startDay desc, CalendarJournal.startHour desc ";
    if (numberOfElement > 0) {
      selectNextEvents += "  limit  ?  offset  ?";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      prepStmt.setString(2, myId);
      if (numberOfElement > 0) {
        prepStmt.setInt(3, numberOfElement);
        prepStmt.setInt(4, firstIndex);
      }

      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(new SocialInformationEvent(journal));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * get my Last Social Events  when data base is Oracle. This
   * includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SQLException
   * @throws java.text.ParseException
   */
  private List<SocialInformationEvent> getMyLastEvents_Oracle(Connection con,
      String day, String myId, int numberOfElement, int firstIndex) throws
      SQLException,
      java.text.ParseException {

    String selectNextEvents = "select * from (ROWNUM num , table_oracle.* from (select distinct "
        + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay < ? and delegatorId = ? order by CalendarJournal.startDay desc , CalendarJournal.startHour desc  ) table_oracle) ";

    if (numberOfElement > 0) {
      selectNextEvents += " where num between ? and ? ";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      prepStmt.setString(2, myId);
      if (numberOfElement > 0) {
        prepStmt.setInt(3, numberOfElement);
        prepStmt.setInt(4, firstIndex);
      }
      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      while (rs.next()) {
        JournalHeader journal = getJournalHeaderFromResultSet(rs);
        list.add(new SocialInformationEvent(journal));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   *  get my Last Social Events  when data base is MMS.
   * This includes all kinds of events
   * @param con
   * @param day
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SQLException
   * @throws java.text.ParseException
   */
  public  List<SocialInformationEvent> getMyLastEvents_MSS(Connection con,
      String day, String myId, int numberOfElement, int firstIndex) throws
      SQLException,
      java.text.ParseException {

    String selectNextEvents = "select distinct " + JournalDAO.JOURNALCOLUMNNAMES + " from CalendarJournal "
        + " where endDay < ? and delegatorId = ? order by CalendarJournal.startDay desc, CalendarJournal.startHour desc ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationEvent> list = null;
    try {
      prepStmt = con.prepareStatement(selectNextEvents);
      prepStmt.setString(1, day);
      prepStmt.setString(2, myId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<SocialInformationEvent>();
      int index = 0;
      while (rs.next()) {
        // limit the searche
        if ((index >= firstIndex && index < numberOfElement + firstIndex) || numberOfElement <= 0) {
          JournalHeader journal = getJournalHeaderFromResultSet(rs);
          list.add(new SocialInformationEvent(journal));
        }
        index++;
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }
}
