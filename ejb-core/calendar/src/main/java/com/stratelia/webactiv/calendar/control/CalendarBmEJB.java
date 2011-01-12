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
// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.calendar.control;

import com.stratelia.webactiv.calendar.socialNetwork.SocialInformationEvent;
import java.util.*;
import java.util.Date;

import javax.ejb.*;

import java.sql.*;
import java.rmi.RemoteException;

import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.calendar.model.*;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class CalendarBmEJB implements CalendarBmBusinessSkeleton, SessionBean {

  private static final long serialVersionUID = -7683983529322401293L;
  private JournalDAO journalDAO;

  // private methods to use in all this ejb methods
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.CALENDAR_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarBmEJB.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED");
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException e) {
        throw new CalendarRuntimeException(
            "CalendarBmEJB.freeConnection(Connection con)",
            SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED");
      }
    }
  }

  /**
   * methods for unresponded messages
   */
  @Override
  public boolean hasTentativeSchedulablesForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.hasTentativeSchedulablesForUser(userId)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      boolean result = getJournalDAO().hasTentativeJournalsForUser(con, userId);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB. hasTentativeSchedulablesForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS",
          "userId=" + userId, e);

    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<JournalHeader> getTentativeSchedulablesForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getTentativeSchedulablesForUser(userId)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      return getJournalDAO().getTentativeJournalHeadersForUser(con, userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getTentativeSchedulablesForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS",
          "userId=" + userId, e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * methods for todo object
   */
  @Override
  public Collection<ToDoHeader> getNotCompletedToDosForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getNotCompletedToDosForUser(userId)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      return ToDoDAO.getNotCompletedToDoHeadersForUser(con, userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNotCompletedToDosForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "userId="
          + userId, e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<ToDoHeader> getOrganizerToDos(String organizerId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getOrganizerToDos(organizerId)",
        "root.MSG_GEN_ENTER_METHOD", "organizerId=" + organizerId);
    Connection con = getConnection();
    try {
      return ToDoDAO.getOrganizerToDoHeaders(con, organizerId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getOrganizerToDos(String organizerId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS",
          "organizerId=" + organizerId, e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<ToDoHeader> getClosedToDos(String organizerId) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.getClosedToDos(organizerId)",
        "root.MSG_GEN_ENTER_METHOD", "organizerId=" + organizerId);
    Connection con = getConnection();
    try {
      return ToDoDAO.getClosedToDoHeaders(con, organizerId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getClosedToDos(String organizerId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS",
          "organizerId=" + organizerId, e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<ToDoHeader> getExternalTodos(String spaceId, String componentId,
      String externalId) throws RemoteException {
    SilverTrace.info(
        "calendar",
        "getExternalTodos(String spaceId, String componentId, String externalId)",
        "root.MSG_GEN_ENTER_METHOD", "space=" + spaceId + ", component="
        + componentId + ", externalId=" + externalId);
    Connection con = getConnection();
    try {
      return ToDoDAO.getToDoHeadersByExternalId(con, spaceId, componentId, externalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getExternalTodos(String spaceId, String componentId, String externalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "spaceId="
          + spaceId + ", componentId=" + componentId + ", externalId="
          + externalId, e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public ToDoHeader getToDoHeader(String todoId) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.getToDoHeader(String todoId)",
        "root.MSG_GEN_ENTER_METHOD", "todoId=" + todoId);
    Connection con = getConnection();
    try {
      return ToDoDAO.getToDoHeader(con, todoId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getToDoHeader(String todoId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO", "todoId="
          + todoId, e);
    } finally {
      freeConnection(con);
    }
  }

  private Collection<ToDoHeader> getToDoHeadersByInstanceId(String instanceId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getToDoHeadersByInstanceId(String instanceId)",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    try {
      return ToDoDAO.getToDoHeadersByInstanceId(con, instanceId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getToDoHeadersByInstanceId(String instanceId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO",
          "instanceId=" + instanceId, e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void updateToDo(ToDoHeader todo) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB. updateToDo(ToDoHeader todo)",
        "root.MSG_GEN_ENTER_METHOD");

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
      ToDoDAO.updateToDo(con, todo); // SQLEXCeption

      createIndex(todo, todo.getDelegatorId()); // vide
      Collection<Attendee> attendees = getToDoAttendees(todo.getId());
      for (Attendee attendee : attendees) {
        createIndex(todo, attendee.getUserId()); // vide
      }
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.updateToDo(ToDoHeader todo)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPDATE_TODO", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public String addToDo(ToDoHeader todo) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB. addToDo(ToDoHeader todo)",
        "root.MSG_GEN_ENTER_METHOD");
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
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addToDo(ToDoHeader todo)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_CREATE_TODO", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void removeToDo(String id) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB. removeToDo(String id)",
        "root.MSG_GEN_ENTER_METHOD", "id=" + id);
    Connection con = getConnection();
    try {
      // get the detail for desindexation
      ToDoHeader todo = getToDoHeader(id);

      // remove attendees and associated indexes
      setToDoAttendees(id, new String[0]);
      ToDoDAO.removeToDo(con, id);
      try {
        removeIndex(todo, todo.getDelegatorId());
      } catch (Exception e) {
        SilverTrace.warn("calendar", "CalendarBmEJB.removeToDo(String id)",
            "root.EX_INDEX_FAILED", "", e);
      }

    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarBmEJB.removeToDo(String id)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CREATE_TODO", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void removeToDoByInstanceId(String instanceId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB. removeToDoByInstanceId(String instanceId)",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    try {
      Collection<ToDoHeader> todosToRemove = getToDoHeadersByInstanceId(instanceId);
      for (ToDoHeader todo : todosToRemove) {
        ToDoDAO.removeToDo(con, todo.getId());
        try {
          removeIndex(todo, todo.getDelegatorId());
        } catch (Exception e) {
          SilverTrace.warn("calendar",
              "CalendarBmEJB.removeToDoByInstanceId(String instanceId)",
              "root.EX_INDEX_FAILED", "", e);
        }
      }
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.removeToDoByInstanceId(String instanceId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CREATE_TODO", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * methods for all schedules type
   */
  @Override
  public Collection<JournalHeader> getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB. removeToDo(String id)",
        "root.MSG_GEN_ENTER_METHOD", "day=" + day + "userId=" + userId
        + "categoryId=" + categoryId + "participation=" + participation);
    Connection con = getConnection();
    try {
      return getJournalDAO().getDayJournalHeadersForUser(con, day, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getDaySchedulablesForUser(String day,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<JournalHeader> getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getNextDaySchedulablesForUser(String id)",
        "root.MSG_GEN_ENTER_METHOD", "day=" + day + "userId=" + userId
        + "categoryId=" + categoryId + "participation=" + participation);
    Connection con = getConnection();
    try {
      return getJournalDAO().getNextJournalHeadersForUser(con, day, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNextDaySchedulablesForUser(String day,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<JournalHeader> getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation)
      throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB. getPeriodSchedulablesForUser(String begin,String end,String userId,String categoryId,String participation)",
        "root.MSG_GEN_ENTER_METHOD", "begin=" + begin + ", end=" + end
        + ", userId=" + userId + ", categoryId=" + categoryId
        + ", participation=" + participation);
    Connection con = getConnection();
    try {
      return getJournalDAO().getPeriodJournalHeadersForUser(con, begin, end, userId, categoryId,
          participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.CalendarBmEJB. getPeriodSchedulablesForUser(String begin,String end,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<SchedulableCount> countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.countMonthSchedulablesForUser(String month,String userId,String categoryId,String participation)",
        "root.MSG_GEN_ENTER_METHOD", "month=" + month + ", userId="
        + userId + ", categoryId=" + categoryId + ", participation="
        + participation);
    Connection con = getConnection();
    try {
      return getJournalDAO().countMonthJournalsForUser(con, month, userId, categoryId, participation);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.CalendarBmEJB.countMonthSchedulablesForUser(String month,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * methods for journal
   */
  @Override
  public String addJournal(JournalHeader journal) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.addJournal(JournalHeader journal)",
        "root.MSG_GEN_ENTER_METHOD");
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
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_ADD_JOURNAL", se);
    } catch (UtilException ue) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "root.EX_GET_NEXTID_FAILED", ue);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.EX_EXCUTE_INSERT_EMPTY", ce);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void updateJournal(JournalHeader journal) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.updateJournal(JournalHeader journal)",
        "root.MSG_GEN_ENTER_METHOD");
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
      throw new CalendarRuntimeException(
          "CalendarBmEJB.updateJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPADATE_JOURNAL", se);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.updateJournal(JournalHeader journal)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      freeConnection(con);
    }
  }


  private void validateJournal(JournalHeader journal){
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
  public void removeJournal(String journalId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.removeJournal(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      JournalHeader journal = getJournalHeader(journalId);
      // remove attendees and associated indexes
      setJournalAttendees(journalId, new String[0]);
      CategoryDAO.removeJournal(con, journalId);
      getJournalDAO().removeJournal(con, journalId);
      try {
        removeIndex(journal, journal.getDelegatorId());
      } catch (Exception e) {
        SilverTrace.warn("calendar",
            "CalendarBmEJB.removeJournal(String journalId)",
            "root.EX_INDEX_FAILED", "", e);
      }
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.removeJournal(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_REMOVE_JOURNAL", se);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.removeJournal(String journalId)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public JournalHeader getJournalHeader(String journalId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getJournalHeader(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      return getJournalDAO().getJournalHeader(con, journalId);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalHeader(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalHeader(String journalId)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalHeader(String journalId)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<JournalHeader> getExternalJournalHeadersForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getOutlookJournalHeadersForUser(String userId)",
        "root.MSG_GEN_ENTER_METHOD", "outlookId=" + userId);
    Connection con = getConnection();
    try {
      return getJournalDAO().getOutlookJournalHeadersForUser(con, userId);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getOutlookJournalHeadersForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getOutlookJournalHeadersForUser(String userId)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "getOutlookJournalHeadersForUser(String userId)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<JournalHeader> getExternalJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate) throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
        "root.MSG_GEN_ENTER_METHOD", "outlookId=" + userId);
    Connection con = getConnection();
    try {
      return getJournalDAO().getOutlookJournalHeadersForUserAfterDate(con, userId, startDate);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate, int nbReturned) throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.getJournalHeadersForUserAfterDate(String userId, Date startDate, int nbReturned)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      return getJournalDAO().getJournalHeadersForUserAfterDate(con, userId, startDate, nbReturned);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalHeadersForUserAfterDate(String userId, Date startDate, int nbReturned)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL", se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalHeadersForUserAfterDate(String userId, Date startDate, int nbReturned)",
          SilverpeasException.ERROR, "calendar.EX_ERROR_PARSING_DATE", pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(
          "getJournalHeadersForUserAfterDate(String userId, Date startDate, int nbReturned)",
          SilverpeasException.ERROR, "calendar.EX_NOT_FOUND_JOURNAL", ce);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * methods for attendees
   */
  @Override
  public void addJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.addJournalAttendee()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.addJournalAttendee(con, journalId, attendee);
      createIndex(getJournalHeader(journalId), attendee.getUserId());
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addJournalAttendee(String journalId,Attendee attendee)",
          SilverpeasException.ERROR,
          "calendar.MSG_CANT_CHANGE_JOURNAL_ATTENDEES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void removeJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.removeJournalAttendee(String journalId,Attendee attendee)",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.removeJournalAttendee(con, journalId, attendee);
      try {
        removeIndex(getJournalHeader(journalId), attendee.getUserId());
      } catch (Exception e) {
        SilverTrace.warn(
            "calendar",
            "CalendarBmEJB.removeJournalAttendee(String journalId,Attendee attendee)",
            "root.EX_INDEX_FAILED", "", e);
      }

    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.removeJournalAttendee(String journalId,Attendee attendee)",
          SilverpeasException.ERROR,
          "calendar.MSG_CANT_CHANGE_JOURNAL_ATTENDEES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public Collection<Attendee> getJournalAttendees(String journalId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getJournalAttendees(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      return AttendeeDAO.getJournalAttendees(con, journalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalAttendees(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL_ATTENDEES",
          e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void setJournalAttendees(String journalId, String[] userIds)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.setJournalAttendees(String journalId,String[] userIds)",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<Attendee> current = getJournalAttendees(journalId);
    JournalHeader journalHeader = getJournalHeader(journalId);
    // search for element to remove
    for (Attendee attendee : current) {
      boolean toRemove = true;
      if (userIds != null) {
        for (int i = 0; i < userIds.length; i++) {
          if (userIds[i].equals(attendee.getUserId())) {
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
      for (int i = 0; i < userIds.length; i++) {
        boolean toAdd = true;
        for (Attendee attendee : current) {
          if (userIds[i].equals(attendee.getUserId())) {
            toAdd = false;
          }
        }
        if (toAdd) {
          Attendee attendee;
          if (userIds[i].equals(journalHeader.getDelegatorId())) {
            attendee = new Attendee(userIds[i], ParticipationStatus.ACCEPTED);
          } else {
            attendee = new Attendee(userIds[i]);
          }
          addJournalAttendee(journalId, attendee);
        }
      }
    }
  }

  @Override
  public void setJournalParticipationStatus(String journalId, String userId,
      String participation) throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.setJournalParticipationStatus(String journalId,String userId,String participation)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId + ", userId="
        + userId + ", participation=" + participation);
    Attendee attendee = new Attendee(userId, participation);
    Connection con = getConnection();
    try {
      AttendeeDAO.removeJournalAttendee(con, journalId, attendee);
      AttendeeDAO.addJournalAttendee(con, journalId, attendee);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.setJournalParticipationStatus(String journalId,String userId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPDATE_JOURNAL", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void addToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.addToDoAttendee(String todoId,Attendee attendee)",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.addToDoAttendee(con, todoId, attendee);
      createIndex(getToDoHeader(todoId), attendee.getUserId());
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addToDoAttendee(String todoId,Attendee attendee)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES",
          e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void removeToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.removeToDoAttendee(String todoId,Attendee attendee)",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.removeToDoAttendee(con, todoId, attendee);
      try {
        removeIndex(getToDoHeader(todoId), attendee.getUserId());
      } catch (Exception e) {
        SilverTrace.warn(
            "calendar",
            "CalendarBmEJB.removeToDoAttendee(String todoId,Attendee attendee)",
            "root.EX_INDEX_FAILED", "", e);
      }

    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.removeToDoAttendee(String todoId,Attendee attendee)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES",
          e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public Collection<Attendee> getToDoAttendees(String todoId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getToDoAttendees(String todoId)",
        "root.MSG_GEN_ENTER_METHOD", "todoId=" + todoId);
    Connection con = getConnection();
    try {
      return AttendeeDAO.getToDoAttendees(con, todoId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getToDoAttendees(String todoId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO_ATTENDEES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void setToDoAttendees(String todoId, String[] userIds)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.setToDoAttendees(String todoId,String[] userIds)",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<Attendee> current = getToDoAttendees(todoId);

    // search for element to remove
    for (Attendee attendee : current) {
      boolean toRemove = true;
      if (userIds != null) {
        for (int i = 0; i < userIds.length; i++) {
          if (userIds[i].equals(attendee.getUserId())) {
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
      for (int i = 0; i < userIds.length; i++) {
        boolean toAdd = true;
        for (Attendee attendee : current) {
          if (userIds[i].equals(attendee.getUserId())) {
            toAdd = false;
          }
        }
        if (toAdd) {
          Attendee attendee = new Attendee(userIds[i]);
          addToDoAttendee(todoId, attendee);
        }
      }
    }
  }

  /**
   * methods for categories
   */
  @Override
  public Collection<Category> getAllCategories() throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.getAllCategories()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      return CategoryDAO.getAllCategories(con);
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarBmEJB.getAllCategories()",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public Category getCategory(String categoryId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getCategory(String categoryId)",
        "root.MSG_GEN_ENTER_METHOD", "categoryId=" + categoryId);
    Connection con = getConnection();
    try {
      return CategoryDAO.getCategory(con, categoryId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getCategory(String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void addJournalCategory(String journalId, String categoryId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.addJournalCategory(String journalId,String categoryId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId + ", categoryId="
        + categoryId);
    Connection con = getConnection();
    try {
      CategoryDAO.addJournalCategory(con, journalId, categoryId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addJournalCategory(String journalId,String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_CATEGORIES", e);

    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void removeJournalCategory(String journalId, String categoryId)
      throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.removeJournalCategory(String journalId,String categoryId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId
        + ", categoryId=" + categoryId);
    Connection con = getConnection();
    try {
      CategoryDAO.removeJournalCategory(con, journalId, categoryId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.removeJournalCategory(String journalId,String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public Collection<Category> getJournalCategories(String journalId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getJournalCategories(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      return CategoryDAO.getJournalCategories(con, journalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalCategories(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

  @Override
  public void setJournalCategories(String journalId, String[] categoryIds)
      throws RemoteException {
    SilverTrace.info(
        "calendar",
        "CalendarBmEJB.setJournalCategories(String journalId,String[] categoryIds)",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<Category> current = getJournalCategories(journalId);
    // search for element to remove
    for (Category category : current) {
      boolean toRemove = true;
      if (categoryIds != null) {
        for (int i = 0; i < categoryIds.length; i++) {
          if (categoryIds[i].equals(category.getId())) {
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
      for (int i = 0; i < categoryIds.length; i++) {
        boolean toAdd = true;
        for (Category category : current) {
          if (categoryIds[i].equals(category.getId())) {
            toAdd = false;
          }
        }
        if (toAdd) {
          addJournalCategory(journalId, categoryIds[i]);
        }
      }
    }
  }

  /**
   * methode de reindexation de tous les todos
   */
  @Override
  public void indexAllTodo() throws RemoteException {
    Connection con = null;

    ResultSet rs = null;

    String selectStatement = "select " + ToDoDAO.COLUMNNAMES
        + " from CalendarToDo ";
    PreparedStatement prepStmt = null;

    try {
      con = DBUtil.makeConnection(JNDINames.CALENDAR_DATASOURCE);
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

    } catch (Exception se) {
      SilverTrace.warn("calendar", "CalendarBmEJB.indexAllTodo()",
          "root.EX_INDEX_FAILED", "", se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null) {
          con.close();
        }
      } catch (SQLException se) {
        SilverTrace.warn("calendar", "CalendarBmEJB.indexAllTodo()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", se);
      }

    }

  }

  /**********************************************************************************/
  /**
   * Gestion du calendrier des jours non travailles /
   **********************************************************************************/
  @Override
  public List<String> getHolidayDates(String userId) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.getHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      return HolidaysDAO.getHolidayDates(con, userId);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.getHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "calendar.GETTING_HOLIDAYDATES_FAILED", "userId = " + userId, re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public List<String> getHolidayDates(String userId, Date beginDate, Date endDate)
      throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.getHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      return HolidaysDAO.getHolidayDates(con, userId, beginDate, endDate);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.getHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "calendar.GETTING_HOLIDAYDATES_FAILED", "userId = " + userId, re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void addHolidayDate(HolidayDetail holiday) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.addHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDate="
        + holiday.getDate().toString());
    Connection con = getConnection();
    try {
      HolidaysDAO.addHolidayDate(con, holiday);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.addHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "calendar.ADDING_HOLIDAYDATE_FAILED", "date = "
          + holiday.getDate().toString(), re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void addHolidayDates(List<HolidayDetail> holidayDates) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.addHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDates.size()="
        + holidayDates.size());
    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        HolidaysDAO.addHolidayDate(con, holiday);
      }
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.addHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "calendar.ADDING_HOLIDAYDATES_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void removeHolidayDate(HolidayDetail holiday) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.removeHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDate="
        + holiday.getDate().toString());
    Connection con = getConnection();
    try {
      HolidaysDAO.removeHolidayDate(con, holiday);
    } catch (Exception re) {
      throw new CalendarRuntimeException("calendarBmEJB.removeHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "calendar.REMOVING_HOLIDAYDATE_FAILED", "date = "
          + holiday.getDate().toString(), re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void removeHolidayDates(List<HolidayDetail> holidayDates) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.removeHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDates.size()="
        + holidayDates.size());
    Connection con = getConnection();
    try {
      for (HolidayDetail holiday : holidayDates) {
        HolidaysDAO.removeHolidayDate(con, holiday);
      }
    } catch (Exception re) {
      throw new CalendarRuntimeException("CalendarBmEJB.removeHolidayDates()",
          SilverpeasRuntimeException.ERROR,
          "calendar.REMOVING_HOLIDAYDATES_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public boolean isHolidayDate(HolidayDetail date) throws RemoteException {
    Connection con = getConnection();
    try {
      return HolidaysDAO.isHolidayDate(con, date);
    } catch (Exception re) {
      throw new CalendarRuntimeException("CalendarBmEJB.isHolidayDate()",
          SilverpeasRuntimeException.ERROR,
          "calendar.GETTING_HOLIDAYDATE_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * methode de reindexation de tous les journeaux
   */
  @Override
  public void indexAllJournal() throws RemoteException {
    Connection con = null;

    ResultSet rs = null;

    String selectStatement = "select " + JournalDAO.COLUMNNAMES
        + " from CalendarJournal ";
    PreparedStatement prepStmt = null;

    try {
      con = DBUtil.makeConnection(JNDINames.CALENDAR_DATASOURCE);
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

    } catch (Exception se) {
      SilverTrace.warn("calendar", "CalendarBmEJB.indexAllJournal()",
          "root.EX_INDEX_FAILED", "", se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null) {
          con.close();
        }
      } catch (SQLException se) {
        SilverTrace.warn("calendar", "CalendarBmEJB.indexAllJournal()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", se);
      }

    }

  }

  private void createIndex(Schedulable detail, String userId) {
    try {
      FullIndexEntry indexEntry = null;
      if (detail instanceof ToDoHeader) {
        indexEntry = new FullIndexEntry("user@" + userId + "_todo", "todo",
            detail.getId());
      } else {
        indexEntry = new FullIndexEntry("user@" + userId + "_agenda", "agenda",
            detail.getId());
      }
      indexEntry.setTitle(detail.getName());
      indexEntry.setPreView(detail.getDescription());
      indexEntry.setCreationUser(detail.getDelegatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("calendar",
          "CalendarBmEJB.createIndex(Schedulable detail, String userId)",
          "root.EX_INDEX_FAILED",
          "id=" + detail.getId() + ", userId=" + userId, e);
    }
  }

  private void removeIndex(Schedulable detail, String userId) {
    IndexEntryPK indexEntry = getIndexEntry(detail, userId);

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  private IndexEntryPK getIndexEntry(Schedulable detail, String userId) {
    IndexEntryPK indexEntry = null;
    if (detail instanceof ToDoHeader) {
      indexEntry = new IndexEntryPK("user@" + userId + "_todo", "todo", detail.getId());
    } else {
      indexEntry = new IndexEntryPK("user@" + userId + "_agenda", "agenda",
          detail.getId());
    }
    return indexEntry;
  }

  /**
   * ejb methods
   */
  // public void ejbCreate() throws CreateException
  public void ejbCreate() {
    // Debug.debug(700, "CalendarBmEJB.ejbCreate()", "enter", null, null);
  }

  // public void ejbRemove() throws javax.ejb.EJBException,
  // java.rmi.RemoteException
  @Override
  public void ejbRemove() {
    // Debug.debug(700, "CalendarBmEJB.ejbRemove()", "enter", null, null);
  }

  // public void ejbActivate() throws javax.ejb.EJBException,
  // java.rmi.RemoteException
  @Override
  public void ejbActivate() {
    // Debug.debug(700, "CalendarBmEJB.ejbActivate()", "enter", null, null);
  }

  // public void ejbPassivate() throws javax.ejb.EJBException,
  // java.rmi.RemoteException
  @Override
  public void ejbPassivate() {
    // Debug.debug(700, "CalendarBmEJB.ejbPassivate()", "enter", null, null);
  }

  // public void setSessionContext(final javax.ejb.SessionContext p1) throws
  // javax.ejb.EJBException, java.rmi.RemoteException
  @Override
  public void setSessionContext(final javax.ejb.SessionContext p1)
      throws javax.ejb.EJBException, java.rmi.RemoteException {
    // Debug.debug(700, "CalendarBmEJB.setSessionContext()", "enter", null,
    // null);
  }

  /**
   * Method for getting the next events of userId ,the result is limited
   * @param day
   * @param userId
   * @param classification
   * @param limit
   * @param offset
   * @return List<JournalHeader>
   * @throws RemoteException
   */
  @Override
  public List<JournalHeader> getNextEventsForUser(String day, String userId, String classification,
      int limit, int offset) throws RemoteException {

    SilverTrace.info("calendar",
        "CalendarBmEJB.getNextEventsForUser(String id)",
        "root.MSG_GEN_ENTER_METHOD", "day=" + day + "userId=" + userId
        + "Classification=" + classification + "limit=" + limit + "offset=" + offset);
    Connection con = getConnection();
    try {
      return getJournalDAO().getNextEventsForUser(con, day, userId, classification, limit, offset);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNextEventsForUser(String day,String userId,String classification,int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * get Next Social Events for a given list of my Contacts . This includes all kinds of events
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws RemoteException
   */
  @Override
  public List<SocialInformationEvent> getNextEventsForMyContacts(String day,String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws RemoteException {
    Connection con = getConnection();
    try {
      return getJournalDAO().getNextEventsForMyContacts(con,day, myId, myContactsIds, numberOfElement, firstIndex);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNextEventsForUser(String day,String userId,String classification,int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }
  /**
   * get Last Social Events for a given list of my Contacts . This includes all kinds of events
   * @param day
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws RemoteException
   */
  @Override
  public List<SocialInformationEvent> getLastEventsForMyContacts(String day,String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws RemoteException {
    Connection con = getConnection();
    try {
      return getJournalDAO().getLastEventsForMyContacts(con,day, myId, myContactsIds, numberOfElement, firstIndex);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNextEventsForUser(String day,String userId,String classification,int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }
  /**
   * get the my last Events  of information and number of Item and the first Index
   * @param day
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationEvent>
   * @throws RemoteException
   */
   @Override
  public List<SocialInformationEvent> getMyLastEvents(String day, String myId,
       int numberOfElement, int firstIndex) throws RemoteException
  {
    Connection con = getConnection();
    try {
      return getJournalDAO().getMyLastEvents(con,day, myId, numberOfElement, firstIndex);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNextEventsForUser(String day,String userId,String classification,int limit, int offset)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }

  }

   /**
    * Gets a DAO on journal objects.
    * @return a JournalDAO instance.
    */
   private JournalDAO getJournalDAO() {
     if (journalDAO == null) {
       journalDAO = new JournalDAO();
     }
     return journalDAO;
   }
}
