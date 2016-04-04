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

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.calendar.model.ToDoHeader;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ToDoDAO {

  public static final String COLUMNNAMES =
      "id, name, delegatorId, description, priority, classification, startDay, "
          + "startHour, endDay, endHour, percentCompleted, completedDay, duration, spaceId, componentId, externalId";
  private static final String TODOCOLUMNNAMES =
      "CalendarToDo.id, CalendarToDo.name, CalendarToDo.delegatorId, CalendarToDo.description, "
          + "CalendarToDo.priority, CalendarToDo.classification, CalendarToDo.startDay, "
          + "CalendarToDo.startHour, CalendarToDo.endDay, CalendarToDo.endHour, CalendarToDo.percentCompleted, "
          + "CalendarToDo.completedDay, CalendarToDo.duration, CalendarToDo.spaceId, CalendarToDo.componentId, "
          + "CalendarToDo.externalId";

  public static String addToDo(Connection con, ToDoHeader toDo) throws SQLException, UtilException {
    String insertStatement = "insert into CalendarToDo (" + COLUMNNAMES + ") "
        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement prepStmt = null;

    int id;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      id = DBUtil.getNextId("CalendarToDo", "id");

      prepStmt.setInt(1, id);
      prepStmt.setString(2, toDo.getName());
      prepStmt.setString(3, toDo.getDelegatorId());
      prepStmt.setString(4, toDo.getDescription());
      prepStmt.setInt(5, toDo.getPriority().getValue());
      prepStmt.setString(6, toDo.getClassification().getString());
      prepStmt.setString(7, toDo.getStartDay());
      prepStmt.setString(8, toDo.getStartHour());
      prepStmt.setString(9, toDo.getEndDay());
      prepStmt.setString(10, toDo.getEndHour());
      prepStmt.setInt(11, toDo.getPercentCompleted());
      prepStmt.setString(12, toDo.getCompletedDay());
      prepStmt.setInt(13, toDo.getDuration());
      prepStmt.setString(14, toDo.getSpaceId());
      prepStmt.setString(15, toDo.getComponentId());
      prepStmt.setString(16, toDo.getExternalId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return String.valueOf(id);
  }

  public static void updateToDo(Connection con, ToDoHeader toDo) throws SQLException {
    String insertStatement = "update CalendarToDo "
        + " set name = ?, delegatorId = ?, description = ?, "
        + "priority = ?, classification = ?, "
        + "startDay = ?, startHour = ?, endDay = ?, endHour = ?, "
        + "percentCompleted = ?, completedDay = ?, " + "duration = ?, "
        + "spaceId = ?, componentId = ? , externalId = ? " + "where id = ?";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, toDo.getName());
      prepStmt.setString(2, toDo.getDelegatorId());
      prepStmt.setString(3, toDo.getDescription());
      prepStmt.setInt(4, toDo.getPriority().getValue());
      prepStmt.setString(5, toDo.getClassification().getString());
      prepStmt.setString(6, toDo.getStartDay());
      prepStmt.setString(7, toDo.getStartHour());
      prepStmt.setString(8, toDo.getEndDay());
      prepStmt.setString(9, toDo.getEndHour());
      prepStmt.setInt(10, toDo.getPercentCompleted());
      prepStmt.setString(11, toDo.getCompletedDay());
      prepStmt.setInt(12, toDo.getDuration());
      prepStmt.setString(13, toDo.getSpaceId());
      prepStmt.setString(14, toDo.getComponentId());
      prepStmt.setString(15, toDo.getExternalId());
      prepStmt.setInt(16, Integer.parseInt(toDo.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

  }

  public static void removeToDo(Connection con, String id) throws SQLException {
    String statement = "DELETE FROM CalendarToDo WHERE id = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(statement);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeToDoByInstanceId(Connection con, String instanceId)
      throws SQLException {
    String statement = "DELETE FROM CalendarToDo WHERE componentId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(statement);
      prepStmt.setString(1, instanceId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static Collection<ToDoHeader> getNotCompletedToDoHeadersForUser(Connection con,
      String userId) throws SQLException, CalendarException {
    String selectStatement = "select distinct " + ToDoDAO.TODOCOLUMNNAMES
        + ", lower(name) " + " from CalendarToDo, CalendarToDoAttendee "
        + " WHERE (userId = ?) " + " and (completedDay IS NULL)"
        + " and (CalendarToDo.id = CalendarToDoAttendee.todoId) "
        + " order by lower(name)";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<ToDoHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<ToDoHeader>();
      while (rs.next()) {
        ToDoHeader toDo = getToDoHeaderFromResultSet(rs);
        list.add(toDo);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ToDoHeader> getOrganizerToDoHeaders(Connection con,
      String organizerId) throws SQLException, CalendarException {
    String selectStatement = "select " + ToDoDAO.TODOCOLUMNNAMES
        + ", lower(name) " + " from CalendarToDo "
        + " WHERE (delegatorId = ?) " + " and (completedDay IS NULL)"
        + " order by lower(name)";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<ToDoHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, organizerId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<ToDoHeader>();
      while (rs.next()) {
        ToDoHeader toDo = getToDoHeaderFromResultSet(rs);
        list.add(toDo);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ToDoHeader> getClosedToDoHeaders(Connection con,
      String organizerId) throws SQLException, CalendarException {
    String selectStatement = "select distinct " + ToDoDAO.TODOCOLUMNNAMES
        + ", lower(name) " + " from CalendarToDo, CalendarToDoAttendee "
        + " WHERE CalendarToDo.id = CalendarToDoAttendee.todoId "
        + " AND (userId = ? ) " + " AND (completedDay IS NOT NULL) " + "UNION "
        + "select distinct " + ToDoDAO.TODOCOLUMNNAMES + ", lower(name) "
        + " from CalendarToDo " + " WHERE (delegatorId = ? ) "
        + " AND (completedDay IS NOT NULL) " + " order by 17";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<ToDoHeader> list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, organizerId);
      prepStmt.setString(2, organizerId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<ToDoHeader>();
      while (rs.next()) {
        ToDoHeader toDo = getToDoHeaderFromResultSet(rs);
        list.add(toDo);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ToDoHeader> getToDoHeadersByExternalId(Connection con,
      String spaceId, String componentId, String externalId)
      throws SQLException, CalendarException {
    String selectStatement = "select distinct " + ToDoDAO.TODOCOLUMNNAMES
        + ", lower(name) " + " from CalendarToDo "
        + " WHERE (externalId like ?) " + " and (componentId = ?)"
        + " order by startDay, lower(name)";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, externalId);
      prepStmt.setString(2, componentId);

      rs = prepStmt.executeQuery();
      List<ToDoHeader> list = new ArrayList<ToDoHeader>();
      while (rs.next()) {
        ToDoHeader toDo = getToDoHeaderFromResultSet(rs);
        list.add(toDo);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * @param con
   * @param componentId
   * @return
   * @throws SQLException
   * @throws CalendarException
   */
  public static Collection<ToDoHeader> getToDoHeadersByInstanceId(final Connection con,
      final String componentId) throws SQLException, CalendarException {
    String selectStatement = "select " + ToDoDAO.TODOCOLUMNNAMES
        + ", lower(name) " + " from CalendarToDo " + " WHERE componentId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<ToDoHeader> list = new ArrayList<ToDoHeader>();
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, componentId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ToDoHeader toDo = getToDoHeaderFromResultSet(rs);
        list.add(toDo);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static ToDoHeader getToDoHeaderFromResultSet(ResultSet rs) throws SQLException,
      CalendarException {
    try {
      String id = String.valueOf(rs.getInt(1));
      String name = rs.getString(2);
      String delegatorId = rs.getString(3);
      ToDoHeader toDo = new ToDoHeader(id, name, delegatorId);
      toDo.setDescription(rs.getString(4));
      toDo.getPriority().setValue(rs.getInt(5));
      toDo.getClassification().setString(rs.getString(6));
      toDo.setStartDay(rs.getString(7));
      toDo.setStartHour(rs.getString(8));
      toDo.setEndDay(rs.getString(9));
      toDo.setEndHour(rs.getString(10));
      toDo.setPercentCompleted(rs.getInt(11));
      toDo.setCompletedDay(rs.getString(12));
      toDo.setDuration(rs.getInt(13));
      toDo.setSpaceId(rs.getString(14));
      toDo.setComponentId(rs.getString(15));
      toDo.setExternalId(rs.getString(16));
      return toDo;
    } catch (ParseException e) {
      SilverTrace.warn("calendar", "ToDoDAO.getToDoHeaderFromResultSet(ResultSet rs)",
          "calendar_MSG_calendar_MSG_CANT_GET_TODO", "return => ToDO=null");
      return null;
    }
  }

  public static ToDoHeader getToDoHeader(Connection con, String toDoId)
      throws SQLException, CalendarException {
    String selectStatement = "SELECT " + ToDoDAO.COLUMNNAMES + " FROM CalendarToDo WHERE id = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    ToDoHeader toDo;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(toDoId));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        toDo = getToDoHeaderFromResultSet(rs);
      } else {
        throw new CalendarException("ToDoDAO.getToDoHeader.Connection con, String journalId",
            SilverpeasException.ERROR, " toDoId=" + toDoId);
      }
      return toDo;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static List<String> getAllTodoByUser(Connection con, String userId) throws SQLException {
    List<String> taskIds = new ArrayList<String>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String selectStatement = "SELECT DISTINCT(ctd.id) FROM CalendarToDo ctd, " +
        "CalendarToDoAttendee  ctda WHERE ctda.userid = ? " +
        "AND (ctd.id = ctda.todoId)";
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        taskIds.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return taskIds;
  }
}
