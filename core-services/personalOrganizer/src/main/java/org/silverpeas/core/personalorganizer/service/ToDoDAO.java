/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.personalorganizer.service;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.personalorganizer.model.ToDoHeader;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

public class ToDoDAO {

  static final String COLUMNNAMES =
      "id, name, delegatorId, description, priority, classification, startDay, "
          + "startHour, endDay, endHour, percentCompleted, completedDay, duration, spaceId, componentId, externalId";
  private static final String TODOCOLUMNNAMES =
      "CalendarToDo.id, CalendarToDo.name, CalendarToDo.delegatorId, CalendarToDo.description, "
          + "CalendarToDo.priority, CalendarToDo.classification, CalendarToDo.startDay, "
          + "CalendarToDo.startHour, CalendarToDo.endDay, CalendarToDo.endHour, CalendarToDo.percentCompleted, "
          + "CalendarToDo.completedDay, CalendarToDo.duration, CalendarToDo.spaceId, CalendarToDo.componentId, "
          + "CalendarToDo.externalId";
  private static final String DISTINCT_CLAUSE = "DISTINCT ";
  private static final String TO_DO_TABLE = "CalendarToDo";
  private static final String TO_DO_ATTENDEE_TABLE = "CalendarToDoAttendee";
  private static final String TO_DO_JOINING_TO_DO_ATTENDEE =
      "CalendarToDo.id = CalendarToDoAttendee.todoId";
  private static final String ORDER_BY_COL = ", lower(name) ln";
  private static final String ORDER_BY_CLAUSE = "ln";
  private static final String USER_ID_CRITERION = "userId = ?";

  private ToDoDAO() {
    throw new IllegalAccessError("Utility class");
  }

  public static String addToDo(Connection con, ToDoHeader toDo) throws SQLException {
    String insertStatement = "insert into CalendarToDo (" + COLUMNNAMES + ") "
        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement prepStmt = null;

    int id;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      id = DBUtil.getNextId(TO_DO_TABLE, "id");

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

  static void updateToDo(Connection con, ToDoHeader toDo) throws SQLException {
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

  static void removeToDo(String id) throws SQLException {
    JdbcSqlQuery.deleteFrom(TO_DO_TABLE).where("id = ?", Integer.parseInt(id)).execute();
  }

  static SilverpeasList<ToDoHeader> getNotCompletedToDoHeadersForUser(String userId)
      throws SQLException {
    return JdbcSqlQuery
        .select(DISTINCT_CLAUSE + TODOCOLUMNNAMES + ORDER_BY_COL)
        .from(TO_DO_TABLE)
        .join(TO_DO_ATTENDEE_TABLE).on(TO_DO_JOINING_TO_DO_ATTENDEE)
        .where(USER_ID_CRITERION, userId)
        .and("completedDay IS NULL")
        .orderBy(ORDER_BY_CLAUSE)
        .execute(ToDoDAO::getToDoHeaderFromResultSet);
  }

  static SilverpeasList<ToDoHeader> getOrganizerToDoHeaders(String organizerId)
      throws SQLException {
    return JdbcSqlQuery
        .select(TODOCOLUMNNAMES + ORDER_BY_COL)
        .from(TO_DO_TABLE)
        .where("delegatorId = ?", organizerId)
        .and("completedDay IS NULL")
        .orderBy(ORDER_BY_CLAUSE)
        .execute(ToDoDAO::getToDoHeaderFromResultSet);
  }

  static SilverpeasList<ToDoHeader> getClosedToDoHeaders(String organizerId) throws SQLException {
    return JdbcSqlQuery
        .select("*")
        .from("(")

        .addSqlPart("SELECT " + TODOCOLUMNNAMES + ORDER_BY_COL)
        .from(TO_DO_TABLE)
        .join(TO_DO_ATTENDEE_TABLE).on(TO_DO_JOINING_TO_DO_ATTENDEE)
        .where(USER_ID_CRITERION, organizerId)
        .and("completedDay IS NOT NULL")

        .union()

        .addSqlPart("SELECT " + TODOCOLUMNNAMES + ORDER_BY_COL)
        .from(TO_DO_TABLE)
        .where("delegatorId = ?", organizerId)
        .and("completedDay IS NOT NULL")

        .addSqlPart(") u")
        .orderBy(ORDER_BY_CLAUSE)
        .execute(ToDoDAO::getToDoHeaderFromResultSet);
  }

  static SilverpeasList<ToDoHeader> getToDoHeadersByExternalId(String componentId,
      String externalId) throws SQLException {
    return JdbcSqlQuery
        .select(DISTINCT_CLAUSE + TODOCOLUMNNAMES + ORDER_BY_COL)
        .from(TO_DO_TABLE)
        .where("componentId = ?", componentId)
        .and("externalId like ?", externalId)
        .orderBy("startDay, " + ORDER_BY_CLAUSE)
        .execute(ToDoDAO::getToDoHeaderFromResultSet);
  }

  /**
   * Gets the to do header details about a component instance.
   * @param componentId an identifier of component instance.
   * @throws SQLException on SQL error
   * @return a list of to do header details
   */
  static SilverpeasList<ToDoHeader> getToDoHeadersByInstanceId(final String componentId)
      throws SQLException {
    return JdbcSqlQuery
        .select(TODOCOLUMNNAMES + ORDER_BY_COL)
        .from(TO_DO_TABLE)
        .where("componentId = ?", componentId)
        .orderBy(ORDER_BY_CLAUSE)
        .execute(ToDoDAO::getToDoHeaderFromResultSet);
  }

  static ToDoHeader getToDoHeaderFromResultSet(ResultSet rs) throws SQLException {
    try {
      int i = 1;
      String id = String.valueOf(rs.getInt(i++));
      String name = rs.getString(i++);
      String delegatorId = rs.getString(i++);
      ToDoHeader toDo = new ToDoHeader(id, name, delegatorId);
      toDo.setDescription(rs.getString(i++));
      toDo.getPriority().setValue(rs.getInt(i++));
      toDo.getClassification().setString(rs.getString(i++));
      toDo.setStartDay(rs.getString(i++));
      toDo.setStartHour(rs.getString(i++));
      toDo.setEndDay(rs.getString(i++));
      toDo.setEndHour(rs.getString(i++));
      toDo.setPercentCompleted(rs.getInt(i++));
      toDo.setCompletedDay(rs.getString(i++));
      toDo.setDuration(rs.getInt(i++));
      toDo.setSpaceId(rs.getString(i++));
      toDo.setComponentId(rs.getString(i++));
      toDo.setExternalId(rs.getString(i));
      return toDo;
    } catch (ParseException e) {
      SilverLogger.getLogger(ToDoDAO.class).warn(e);
      return null;
    }
  }

  /**
   * Gets to do header details from an identifier.
   * @param toDoId identifier of to do.
   * @throws SQLException on SQL error.
   * @return a to do details.
   */
  static ToDoHeader getToDoHeader(String toDoId) throws SQLException {
    return JdbcSqlQuery
        .select(COLUMNNAMES)
        .from(TO_DO_TABLE)
        .where("id = ?", Integer.parseInt(toDoId))
        .executeUnique(ToDoDAO::getToDoHeaderFromResultSet);
  }

  /**
   * Gets all identifier of to do linked to a user.
   * @param userId the identifier of a user.
   * @throws SQLException on SQL error.
   * @return a list of to do identifier.
   */
  static SilverpeasList<String> getAllTodoByUser(String userId) throws SQLException {
    return JdbcSqlQuery
        .select(DISTINCT_CLAUSE + "(" + TO_DO_TABLE + ".id)")
        .from(TO_DO_TABLE)
        .join(TO_DO_ATTENDEE_TABLE).on(TO_DO_JOINING_TO_DO_ATTENDEE)
        .where(USER_ID_CRITERION, userId)
        .execute(r -> r.getString(1));
  }
}
