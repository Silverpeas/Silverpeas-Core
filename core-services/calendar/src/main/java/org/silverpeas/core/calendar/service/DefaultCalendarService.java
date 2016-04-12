/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.calendar.model.Attendee;
import org.silverpeas.core.calendar.model.Category;
import org.silverpeas.core.calendar.model.HolidayDetail;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.calendar.model.ParticipationStatus;
import org.silverpeas.core.calendar.model.Schedulable;
import org.silverpeas.core.calendar.model.SchedulableCount;
import org.silverpeas.core.calendar.model.ToDoHeader;
import org.silverpeas.core.calendar.model.TodoDetail;
import org.silverpeas.core.calendar.socialnetwork.SocialInformationEvent;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Calendar service layer to manager calendars in Silverpeas
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultCalendarService implements SilverpeasCalendar, ComponentInstanceDeletion {

  @Inject
  private JournalDAO journalDAO;

  // private methods to use in all this ejb methods
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED");
    }
  }

  /**
   * methods for unresponded messages
   * @param userId the user identifier
   * @return
   */
  @Override
  public boolean hasTentativeSchedulablesForUser(String userId) {

    Connection con = getConnection();
    try {
      return getJournalDAO().hasTentativeJournalsForUser(con, userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB. hasTentativeSchedulablesForUser(String userId)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_GET_JOURNALS", "userId=" + userId, e);

    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getTentativeSchedulablesForUser(String userId) {

    Connection con = getConnection();
    try {
      return getJournalDAO().getTentativeJournalHeadersForUser(con, userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getTentativeSchedulablesForUser(String userId)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_GET_JOURNALS", "userId=" + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param userId the user identifier
   * @return
   */
  @Override
  public Collection<ToDoHeader> getNotCompletedToDosForUser(String userId) {

    Connection con = getConnection();
    try {
      return ToDoDAO.getNotCompletedToDoHeadersForUser(con, userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getNotCompletedToDosForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "userId=" + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<String> getAllToDoForUser(final String userId) {

    Connection con = getConnection();
    try {
      return ToDoDAO.getAllTodoByUser(con, userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getAllToDoForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "userId=" + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ToDoHeader> getOrganizerToDos(String organizerId) {
    Connection con = getConnection();
    try {
      return ToDoDAO.getOrganizerToDoHeaders(con, organizerId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getOrganizerToDos(String organizerId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "organizerId=" + organizerId,
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ToDoHeader> getClosedToDos(String organizerId) {
    Connection con = getConnection();
    try {
      return ToDoDAO.getClosedToDoHeaders(con, organizerId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getClosedToDos(String organizerId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "organizerId=" + organizerId,
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ToDoHeader> getExternalTodos(String spaceId, String componentId,
      String externalId) {
    Connection con = getConnection();
    try {
      return ToDoDAO.getToDoHeadersByExternalId(con, spaceId, componentId, externalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getExternalTodos(String spaceId, String componentId, String externalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS",
          "spaceId=" + spaceId + ", componentId=" + componentId + ", externalId=" + externalId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ToDoHeader getToDoHeader(String todoId) {
    Connection con = getConnection();
    try {
      return ToDoDAO.getToDoHeader(con, todoId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getToDoHeader(String todoId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO", "todoId=" + todoId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public TodoDetail getTodoDetail(String id) {
    try {
      TodoDetail detail =  TodoDetail.fromToDoHeader(getToDoHeader(id));
      List<Attendee> attendees = new ArrayList<>(getToDoAttendees(id));
      detail.setAttendees(attendees);
      return detail;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  private Collection<ToDoHeader> getToDoHeadersByInstanceId(String instanceId) {
    Connection con = getConnection();
    try {
      return ToDoDAO.getToDoHeadersByInstanceId(con, instanceId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getToDoHeadersByInstanceId(String instanceId)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_GET_TODO", "instanceId=" + instanceId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateToDo(ToDoHeader todo) {
    if (todo.getName() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    }
    if (todo.getDelegatorId() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    }
    if (todo.getEndDate() != null && todo.getStartDate() != null &&
        todo.getStartDate().after(todo.getEndDate())) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_DATE_FIN_ERROR");
    }

    Connection con = getConnection();
    try {
      ToDoDAO.updateToDo(con, todo); // SQLEXCeption

      createIndex(todo, todo.getDelegatorId()); // vide
      Collection<Attendee> attendees = getToDoAttendees(todo.getId());
      for (Attendee attendee : attendees) {
        createIndex(todo, attendee.getUserId()); // vide
      }
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.updateToDo(ToDoHeader todo)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPDATE_TODO", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String addToDo(ToDoHeader todo) {
    if (todo.getName() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    }
    if (todo.getDelegatorId() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    }
    if ((todo.getEndDate() != null) && (todo.getStartDate() != null)) {
      if (todo.getStartDate().compareTo(todo.getEndDate()) > 0) {
        throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
            "calendar.EX_DATE_FIN_ERROR");
      }
    }
    Connection con = getConnection();
    try {
      String result = ToDoDAO.addToDo(con, todo);
      todo.setId(result);
      createIndex(todo, todo.getDelegatorId());
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.addToDo(ToDoHeader todo)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CREATE_TODO", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String addToDo(TodoDetail todo) {
    try {
      ToDoHeader header = ToDoHeader.fromTodoDetail(todo);
      String id = addToDo(header);
      if (todo.getAttendees() != null) {
        for (Attendee attendee : todo.getAttendees()) {
          addToDoAttendee(id, attendee);
        }
      }
      return id;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void removeToDo(String id) {
    Connection con = getConnection();
    try {
      // get the detail for desindexation
      ToDoHeader todo = getToDoHeader(id);

      // remove attendees and associated indexes
      setToDoAttendees(id, ArrayUtil.EMPTY_STRING_ARRAY);
      ToDoDAO.removeToDo(con, id);
      try {
        removeIndex(todo, todo.getDelegatorId());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }

    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.removeToDo(String id)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_REMOVE_TODO", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void removeToDoFromExternal(String spaceId, String componentId, String externalId) {
    try {
      Collection<ToDoHeader> headers = getExternalTodos(spaceId, componentId, externalId);
      for (ToDoHeader header : headers) {
        removeToDo(header.getId());
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  public void removeAttendeeInToDoFromExternal(String componentId, String externalId,
      String userId) {
    try {
      Attendee attendee = new Attendee();
      attendee.setUserId(userId);

      Collection<ToDoHeader> headers = getExternalTodos("useless", componentId, externalId);
      for (ToDoHeader header : headers) {
        if (header != null) {
         removeToDoAttendee(header.getId(), attendee);
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * @param day
   * @param userId the user identifier
   * @param categoryId the category identifier
   * @param participation
   * @return
   */
  @Override
  public Collection<JournalHeader> getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) {
    Connection con = getConnection();
    try {
      return getJournalDAO()
          .getDayJournalHeadersForUser(con, day, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getDaySchedulablesForUser(String day,String userId,String categoryId," +
              "String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) {
    Connection con = getConnection();
    try {
      return getJournalDAO()
          .getNextJournalHeadersForUser(con, day, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getNextDaySchedulablesForUser(String day,String userId,String categoryId," +
              "String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation) {
    Connection con = getConnection();
    try {
      return getJournalDAO()
          .getPeriodJournalHeadersForUser(con, begin, end, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.CalendarEJB. getPeriodSchedulablesForUser(String begin,String end," +
              "String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<SchedulableCount> countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) {
    Connection con = getConnection();
    try {
      return getJournalDAO()
          .countMonthJournalsForUser(con, month, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.CalendarEJB.countMonthSchedulablesForUser(String month,String userId," +
              "String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * methods for journal
   */
  @Override
  public String addJournal(JournalHeader journal) {
    // verify the journal attributes are correctly set
    validateJournal(journal);
    // write in DB
    Connection con = getConnection();
    try {
      String result = getJournalDAO().addJournal(con, journal);
      journal.setId(result);
      createIndex(journal, journal.getDelegatorId());
      return result;
    } catch (SQLException se) {
      throw new CalendarRuntimeException("CalendarEJB.addJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_ADD_JOURNAL", se);
    } catch (UtilException ue) {
      throw new CalendarRuntimeException("CalendarEJB.addJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "root.EX_GET_NEXTID_FAILED", ue);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException("CalendarEJB.addJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.EX_EXCUTE_INSERT_EMPTY", ce);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateJournal(JournalHeader journal) {
    validateJournal(journal);
    Connection con = getConnection();
    try {
      getJournalDAO().updateJournal(con, journal);
      createIndex(journal, journal.getDelegatorId());
      Collection<Attendee> attendees = getJournalAttendees(journal.getId());
      for (Attendee attendee : attendees) {
        createIndex(journal, attendee.getUserId());
      }
    } catch (SQLException se) {
      throw new CalendarRuntimeException("CalendarEJB.updateJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPADATE_JOURNAL", se);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException("CalendarEJB.updateJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      DBUtil.close(con);
    }
  }

  private void validateJournal(JournalHeader journal) {
    // verify the journal attributes are correctly set
    if (journal.getName() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "name");
    }
    if (journal.getStartDate() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "startDate");
    }
    if (journal.getDelegatorId() == null) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "delegatorId");
    }
    if (journal.getStartDate().compareTo(journal.getEndDate()) > 0) {
      throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
          "calendar.EX_DATE_FIN_ERROR");
    }
    if (journal.getStartHour() != null) {
      if (journal.getEndHour() == null) {
        throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
            "calendar.EX_PARAM_NULL", "endHour");
      }
    }
    if (journal.getEndHour() != null) {
      if (journal.getStartHour() == null) {
        throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
            "calendar.EX_PARAM_NULL", "startHour");
      }
    }
    if (journal.getStartDate().equals(journal.getEndDate())) {
      if (journal.getStartHour() != null) {
        if (journal.getStartHour().compareTo(journal.getEndHour()) > 0) {
          throw new CalendarRuntimeException("calendar", SilverpeasException.ERROR,
              "calendar.EX_HOUR_FIN_ERRORR");
        }
      }
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeJournal(String journalId) {
    Connection con = getConnection();
    try {
      JournalHeader journal = getJournalHeader(journalId);
      // remove attendees and associated indexes
      setJournalAttendees(journalId, ArrayUtil.EMPTY_STRING_ARRAY);
      CategoryDAO.removeJournal(con, journalId);
      getJournalDAO().removeJournal(con, journalId);
      try {
        removeIndex(journal, journal.getDelegatorId());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    } catch (SQLException se) {
      throw new CalendarRuntimeException("CalendarEJB.removeJournal(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_REMOVE_JOURNAL", se);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException("CalendarEJB.removeJournal(String journalId)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public JournalHeader getJournalHeader(String journalId) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getJournalHeader(con, journalId);
    } catch (SQLException se) {
      throw new CalendarRuntimeException("CalendarEJB.getJournalHeader(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException("CalendarEJB.getJournalHeader(String journalId)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException("CalendarEJB.getJournalHeader(String journalId)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getExternalJournalHeadersForUser(String userId) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getOutlookJournalHeadersForUser(con, userId);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getOutlookJournalHeadersForUser(String userId)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getOutlookJournalHeadersForUser(String userId)", SilverpeasException.ERROR,
          "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException("getOutlookJournalHeadersForUser(String userId)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getExternalJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getOutlookJournalHeadersForUserAfterDate(con, userId, startDate);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate, int nbReturned) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getJournalHeadersForUserAfterDate(con, userId, startDate, nbReturned);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getJournalHeadersForUserAfterDate(String userId, Date startDate, " +
              "int nbReturned)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getJournalHeadersForUserAfterDate(String userId, Date startDate, " +
              "int nbReturned)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "getJournalHeadersForUserAfterDate(String userId, Date startDate, int nbReturned)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * methods for attendees
   */
  @Override
  public void addJournalAttendee(String journalId, Attendee attendee) {
    Connection con = getConnection();
    try {
      AttendeeDAO.addJournalAttendee(con, journalId, attendee);
      createIndex(getJournalHeader(journalId), attendee.getUserId());
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.addJournalAttendee(String journalId,Attendee attendee)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_JOURNAL_ATTENDEES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void removeJournalAttendee(String journalId, Attendee attendee) {
    Connection con = getConnection();
    try {
      AttendeeDAO.removeJournalAttendee(con, journalId, attendee);
      try {
        removeIndex(getJournalHeader(journalId), attendee.getUserId());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }

    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.removeJournalAttendee(String journalId,Attendee attendee)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_JOURNAL_ATTENDEES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Collection<Attendee> getJournalAttendees(String journalId) {
    Connection con = getConnection();
    try {
      return AttendeeDAO.getJournalAttendees(con, journalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getJournalAttendees(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL_ATTENDEES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void setJournalAttendees(String journalId, String[] userIds) {
    Collection<Attendee> current = getJournalAttendees(journalId);
    JournalHeader journalHeader = getJournalHeader(journalId);
    // search for element to remove
    for (Attendee attendee : current) {
      boolean toRemove = true;
      if (userIds != null) {
        for (String userId : userIds) {
          if (userId.equals(attendee.getUserId())) {
            toRemove = false;
          }
        }
      }
      if (toRemove) {
        removeJournalAttendee(journalId, new Attendee(attendee.getUserId()));
      }
    }
    // search for element to add
    if (userIds != null) {
      for (String userId : userIds) {
        boolean toAdd = true;
        for (Attendee attendee : current) {
          if (userId.equals(attendee.getUserId())) {
            toAdd = false;
          }
        }
        if (toAdd) {
          Attendee attendee;
          if (userId.equals(journalHeader.getDelegatorId())) {
            attendee = new Attendee(userId, ParticipationStatus.ACCEPTED);
          } else {
            attendee = new Attendee(userId);
          }
          addJournalAttendee(journalId, attendee);
        }
      }
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setJournalParticipationStatus(String journalId, String userId, String participation) {
    Attendee attendee = new Attendee(userId, participation);
    Connection con = getConnection();
    try {
      AttendeeDAO.removeJournalAttendee(con, journalId, attendee);
      AttendeeDAO.addJournalAttendee(con, journalId, attendee);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.setJournalParticipationStatus(String journalId,String userId," +
              "String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPDATE_JOURNAL", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addToDoAttendee(String todoId, Attendee attendee) {
    Connection con = getConnection();
    try {
      AttendeeDAO.addToDoAttendee(con, todoId, attendee);
      createIndex(getToDoHeader(todoId), attendee.getUserId());
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.addToDoAttendee(String todoId,Attendee attendee)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void removeToDoAttendee(String todoId, Attendee attendee) {
    Connection con = getConnection();
    try {
      AttendeeDAO.removeToDoAttendee(con, todoId, attendee);
      try {
        removeIndex(getToDoHeader(todoId), attendee.getUserId());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }

    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.removeToDoAttendee(String todoId,Attendee attendee)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Collection<Attendee> getToDoAttendees(String todoId) {
    Connection con = getConnection();
    try {
      return AttendeeDAO.getToDoAttendees(con, todoId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getToDoAttendees(String todoId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO_ATTENDEES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void setToDoAttendees(String todoId, String[] userIds) {
    Collection<Attendee> current = getToDoAttendees(todoId);
    // search for element to remove
    for (Attendee attendee : current) {
      boolean toRemove = true;
      if (userIds != null) {
        for (String userId : userIds) {
          if (userId.equals(attendee.getUserId())) {
            toRemove = false;
          }
        }
      }
      if (toRemove) {
        removeToDoAttendee(todoId, new Attendee(attendee.getUserId()));
      }
    }
    // search for element to add
    if (userIds != null) {
      for (String userId : userIds) {
        boolean toAdd = true;
        for (Attendee attendee : current) {
          if (userId.equals(attendee.getUserId())) {
            toAdd = false;
          }
        }
        if (toAdd) {
          Attendee attendee = new Attendee(userId);
          addToDoAttendee(todoId, attendee);
        }
      }
    }
  }

  /**
   * methods for categories
   */
  @Override
  public Collection<Category> getAllCategories() {
    Connection con = getConnection();
    try {
      return CategoryDAO.getAllCategories(con);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getAllCategories()",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Category getCategory(String categoryId) {
    Connection con = getConnection();
    try {
      return CategoryDAO.getCategory(con, categoryId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getCategory(String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void addJournalCategory(String journalId, String categoryId) {
    Connection con = getConnection();
    try {
      CategoryDAO.addJournalCategory(con, journalId, categoryId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.addJournalCategory(String journalId,String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_CATEGORIES", e);

    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void removeJournalCategory(String journalId, String categoryId) {
    Connection con = getConnection();
    try {
      CategoryDAO.removeJournalCategory(con, journalId, categoryId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.removeJournalCategory(String journalId,String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_CATEGORIES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Collection<Category> getJournalCategories(String journalId) {
    Connection con = getConnection();
    try {
      return CategoryDAO.getJournalCategories(con, journalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.getJournalCategories(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setJournalCategories(String journalId, String[] categoryIds) {
    Collection<Category> current = getJournalCategories(journalId);
    // search for element to remove
    for (Category category : current) {
      boolean toRemove = true;
      if (categoryIds != null) {
        for (String categoryId : categoryIds) {
          if (categoryId.equals(category.getId())) {
            toRemove = false;
          }
        }
      }
      if (toRemove) {
        removeJournalCategory(journalId, category.getId());
      }
    }
    // search for element to add
    if (categoryIds != null) {
      for (String categoryId : categoryIds) {
        boolean toAdd = true;
        for (Category category : current) {
          if (categoryId.equals(category.getId())) {
            toAdd = false;
          }
        }
        if (toAdd) {
          addJournalCategory(journalId, categoryId);
        }
      }
    }
  }

  /**
   * methode de reindexation de tous les todos
   */
  @Override
  public void indexAllTodo() {
    Connection con = null;
    ResultSet rs = null;
    String selectStatement = "select " + ToDoDAO.COLUMNNAMES + " from CalendarToDo ";
    PreparedStatement prepStmt = null;
    try {
      con = DBUtil.openConnection();
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      ToDoHeader todo;
      while (rs.next()) {
        todo = ToDoDAO.getToDoHeaderFromResultSet(rs);
        createIndex(todo, todo.getDelegatorId());
        Collection<Attendee> attendees = getToDoAttendees(todo.getId());
        for (Attendee attendee : attendees) {
          createIndex(todo, attendee.getUserId());
        }
      }

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    } finally {
      DBUtil.close(rs, prepStmt);
      DBUtil.close(con);
    }
  }

  /**
   * *******************************************************************************
   * Gestion du calendrier des jours non travailles
   * ********************************************************************************
   */
  @Override
  public List<String> getHolidayDates(String userId) {
    Connection con = getConnection();
    try {
      return HolidaysDAO.getHolidayDates(con, userId);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.getHolidayDates()",
          SilverpeasRuntimeException.ERROR, "calendar.GETTING_HOLIDAYDATES_FAILED",
          "userId = " + userId, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<String> getHolidayDates(String userId, Date beginDate, Date endDate) {
    Connection con = getConnection();
    try {
      return HolidaysDAO.getHolidayDates(con, userId, beginDate, endDate);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.getHolidayDates()",
          SilverpeasRuntimeException.ERROR, "calendar.GETTING_HOLIDAYDATES_FAILED",
          "userId = " + userId, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addHolidayDate(HolidayDetail holiday) {
    Connection con = getConnection();
    try {
      HolidaysDAO.addHolidayDate(con, holiday);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.addHolidayDate()",
          SilverpeasRuntimeException.ERROR, "calendar.ADDING_HOLIDAYDATE_FAILED",
          "date = " + holiday.getDate().toString(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addHolidayDates(List<HolidayDetail> holidayDates) {
    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        HolidaysDAO.addHolidayDate(con, holiday);
      }
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.addHolidayDates()",
          SilverpeasRuntimeException.ERROR, "calendar.ADDING_HOLIDAYDATES_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeHolidayDate(HolidayDetail holiday) {
    Connection con = getConnection();
    try {
      HolidaysDAO.removeHolidayDate(con, holiday);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.removeHolidayDate()",
          SilverpeasRuntimeException.ERROR, "calendar.REMOVING_HOLIDAYDATE_FAILED",
          "date = " + holiday.getDate().toString(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeHolidayDates(List<HolidayDetail> holidayDates) {
    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        HolidaysDAO.removeHolidayDate(con, holiday);
      }
    } catch (Exception re) {
      throw new CalendarRuntimeException("CalendarEJB.removeHolidayDates()",
          SilverpeasRuntimeException.ERROR, "calendar.REMOVING_HOLIDAYDATES_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean isHolidayDate(HolidayDetail date) {
    Connection con = getConnection();
    try {
      return HolidaysDAO.isHolidayDate(con, date);
    } catch (Exception re) {
      throw new CalendarRuntimeException("CalendarEJB.isHolidayDate()",
          SilverpeasRuntimeException.ERROR, "calendar.GETTING_HOLIDAYDATE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Reindex all journal
   */
  @Override
  public void indexAllJournal() {
    Connection con = null;
    ResultSet rs = null;
    String selectStatement = "select " + JournalDAO.COLUMNNAMES + " from CalendarJournal ";
    PreparedStatement prepStmt = null;

    try {
      con = DBUtil.openConnection();
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      JournalHeader journal;
      while (rs.next()) {

        journal = getJournalDAO().getJournalHeaderFromResultSet(rs);
        createIndex(journal, journal.getDelegatorId());
        Collection<Attendee> attendees = getJournalAttendees(journal.getId());
        for (Attendee attendee : attendees) {
          createIndex(journal, attendee.getUserId());
        }
      }

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    } finally {
      DBUtil.close(rs, prepStmt);
      DBUtil.close(con);
    }
  }

  private void createIndex(Schedulable detail, String userId) {
    try {
      FullIndexEntry indexEntry;
      if (detail instanceof ToDoHeader) {
        indexEntry = new FullIndexEntry("user@" + userId + "_todo", "todo", detail.getId());
      } else {
        indexEntry = new FullIndexEntry("user@" + userId + "_agenda", "agenda", detail.getId());
      }
      indexEntry.setTitle(detail.getName());
      indexEntry.setPreView(detail.getDescription());
      indexEntry.setCreationUser(detail.getDelegatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private void removeIndex(Schedulable detail, String userId) {
    IndexEntryKey indexEntry = getIndexEntry(detail, userId);
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  private IndexEntryKey getIndexEntry(Schedulable detail, String userId) {
    IndexEntryKey indexEntry;
    if (detail instanceof ToDoHeader) {
      indexEntry = new IndexEntryKey("user@" + userId + "_todo", "todo", detail.getId());
    } else {
      indexEntry = new IndexEntryKey("user@" + userId + "_agenda", "agenda", detail.getId());
    }
    return indexEntry;
  }

  /**
   * Method for getting the next events of userId ,the result is limited
   * @param day
   * @param userId the user identifier
   * @param classification
   * @param begin the begin date
   * @param end the end date
   * @return List<JournalHeader>
   */
  @Override
  public List<JournalHeader> getNextEventsForUser(String day, String userId, String classification,
      Date begin, Date end) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getNextEventsForUser(con, day, userId, classification, begin, end);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getNextEventsForUser(String day,String userId,String classification," +
              "int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * get Next Social Events for a given list of my Contacts . This includes all kinds of events
   * @param day
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List<SocialInformationEvent>
   */
  @Override
  public List<SocialInformationEvent> getNextEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getNextEventsForMyContacts(con, day, myId, myContactsIds, begin, end);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getNextEventsForUser(String day,String userId,String classification," +
              "int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * get Last Social Events for a given list of my Contacts . This includes all kinds of events
   * @param day
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List<SocialInformationEvent>
   */
  @Override
  public List<SocialInformationEvent> getLastEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getLastEventsForMyContacts(con, day, myId, myContactsIds, begin, end);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getNextEventsForUser(String day,String userId,String classification," +
              "int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * get the my last Events of information and number of Item and the first Index
   * @param day
   * @param myId
   * @param begin
   * @param end
   * @return List<SocialInformationEvent>
   */
  @Override
  public List<SocialInformationEvent> getMyLastEvents(String day, String myId, Date begin,
      Date end) {
    Connection con = getConnection();
    try {
      return getJournalDAO().getMyLastEvents(con, day, myId, begin, end);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarEJB.getNextEventsForUser(String day,String userId,String classification," +
              "int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is in deletion.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    Connection con = getConnection();
    try {
      Collection<ToDoHeader> todosToRemove = getToDoHeadersByInstanceId(componentInstanceId);
      for (ToDoHeader todo : todosToRemove) {
        ToDoDAO.removeToDo(con, todo.getId());
        AttendeeDAO.removeToDo(con, todo.getId());
        try {
          removeIndex(todo, todo.getDelegatorId());
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarEJB.removeToDoByInstanceId(String instanceId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CREATE_TODO", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Gets a DAO on journal objects.
   * @return a JournalDAO instance.
   */
  private JournalDAO getJournalDAO() {
    return journalDAO;
  }
}
