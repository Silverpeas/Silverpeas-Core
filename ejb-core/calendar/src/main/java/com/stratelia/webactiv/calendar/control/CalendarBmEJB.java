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
// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.calendar.control;

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
  public boolean hasTentativeSchedulablesForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.hasTentativeSchedulablesForUser(userId)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      boolean result = JournalDAO.hasTentativeJournalsForUser(con, userId);
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

  public Collection getTentativeSchedulablesForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getTentativeSchedulablesForUser(userId)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      Collection result = JournalDAO.getTentativeJournalHeadersForUser(con,
          userId);
      return result;
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
  public Collection getNotCompletedToDosForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getNotCompletedToDosForUser(userId)",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      Collection result = ToDoDAO
          .getNotCompletedToDoHeadersForUser(con, userId);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNotCompletedToDosForUser(String userId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS", "userId="
              + userId, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getOrganizerToDos(String organizerId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getOrganizerToDos(organizerId)",
        "root.MSG_GEN_ENTER_METHOD", "organizerId=" + organizerId);
    Connection con = getConnection();
    try {
      Collection result = ToDoDAO.getOrganizerToDoHeaders(con, organizerId);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getOrganizerToDos(String organizerId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS",
          "organizerId=" + organizerId, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getClosedToDos(String organizerId) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.getClosedToDos(organizerId)",
        "root.MSG_GEN_ENTER_METHOD", "organizerId=" + organizerId);
    Connection con = getConnection();
    try {
      Collection result = ToDoDAO.getClosedToDoHeaders(con, organizerId);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getClosedToDos(String organizerId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODOS",
          "organizerId=" + organizerId, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getExternalTodos(String spaceId, String componentId,
      String externalId) throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "getExternalTodos(String spaceId, String componentId, String externalId)",
            "root.MSG_GEN_ENTER_METHOD", "space=" + spaceId + ", component="
                + componentId + ", externalId=" + externalId);
    Connection con = getConnection();
    try {
      Collection result = ToDoDAO.getToDoHeadersByExternalId(con, spaceId,
          componentId, externalId);
      return result;
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

  public ToDoHeader getToDoHeader(String todoId) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.getToDoHeader(String todoId)",
        "root.MSG_GEN_ENTER_METHOD", "todoId=" + todoId);
    Connection con = getConnection();
    try {
      ToDoHeader todo = ToDoDAO.getToDoHeader(con, todoId);
      return todo;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getToDoHeader(String todoId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO", "todoId="
              + todoId, e);
    } finally {
      freeConnection(con);
    }
  }

  private Collection getToDoHeadersByInstanceId(String instanceId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getToDoHeadersByInstanceId(String instanceId)",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    try {
      Collection todos = ToDoDAO.getToDoHeadersByInstanceId(con, instanceId);
      return todos;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getToDoHeadersByInstanceId(String instanceId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO",
          "instanceId=" + instanceId, e);
    } finally {
      freeConnection(con);
    }
  }

  public void updateToDo(ToDoHeader todo) throws RemoteException,
      CalendarException {
    SilverTrace.info("calendar", "CalendarBmEJB. updateToDo(ToDoHeader todo)",
        "root.MSG_GEN_ENTER_METHOD");

    if (todo.getName() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    if (todo.getDelegatorId() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    if ((todo.getEndDate() != null) && (todo.getStartDate() != null))
      if (todo.getStartDate().compareTo(todo.getEndDate()) > 0)
        throw new CalendarException("calendar", SilverpeasException.ERROR,
            "calendar.EX_DATE_FIN_ERROR");

    Connection con = getConnection();
    try {
      ToDoDAO.updateToDo(con, todo); // SQLEXCeption

      // try {
      createIndex(todo, todo.getDelegatorId()); // vide
      Collection attendees = getToDoAttendees(todo.getId()); // remoteException
      for (Iterator i = attendees.iterator(); i.hasNext();) {
        Attendee attendee = (Attendee) i.next();
        createIndex(todo, attendee.getUserId()); // vide
      }
      // }
      // catch (Exception e) {
      // SilverTrace.warn("calendar",
      // "CalendarBmEJB.updateToDo(ToDoHeader todo)",
      // "root.EX_INDEX_FAILED","",e);
      // }
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.updateToDo(ToDoHeader todo)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_UPDATE_TODO", e);
    } finally {
      freeConnection(con);
    }
  }

  public String addToDo(ToDoHeader todo) throws RemoteException,
      CalendarException {
    SilverTrace.info("calendar", "CalendarBmEJB. addToDo(ToDoHeader todo)",
        "root.MSG_GEN_ENTER_METHOD");
    if (todo.getName() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    if (todo.getDelegatorId() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL");
    if ((todo.getEndDate() != null) && (todo.getStartDate() != null))
      if (todo.getStartDate().compareTo(todo.getEndDate()) > 0)
        throw new CalendarException("calendar", SilverpeasException.ERROR,
            "calendar.EX_DATE_FIN_ERROR");

    Connection con = getConnection();
    try {
      String result = ToDoDAO.addToDo(con, todo);
      todo.setId(result);
      // try {
      createIndex(todo, todo.getDelegatorId());
      // } catch (Exception e) {
      // SilverTrace.warn("calendar",
      // "CalendarBmEJB.updateToDo(ToDoHeader todo)",
      // "root.EX_INDEX_FAILED","",e);
      // }
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addToDo(ToDoHeader todo)", SilverpeasException.ERROR,
          "calendar.MSG_CANT_CREATE_TODO", e);
    } finally {
      freeConnection(con);
    }
  }

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

  public void removeToDoByInstanceId(String instanceId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB. removeToDoByInstanceId(String instanceId)",
        "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    Connection con = getConnection();
    try {
      ArrayList todosToRemove = (ArrayList) getToDoHeadersByInstanceId(instanceId);
      if (todosToRemove != null) {
        ToDoHeader todo = null;
        for (int i = 0; i < todosToRemove.size(); i++) {
          todo = (ToDoHeader) todosToRemove.get(i);
          ToDoDAO.removeToDo(con, todo.getId());
          try {
            removeIndex(todo, todo.getDelegatorId());
          } catch (Exception e) {
            SilverTrace.warn("calendar",
                "CalendarBmEJB.removeToDoByInstanceId(String instanceId)",
                "root.EX_INDEX_FAILED", "", e);
          }
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

  public Collection getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB. removeToDo(String id)",
        "root.MSG_GEN_ENTER_METHOD", "day=" + day + "userId=" + userId
            + "categoryId=" + categoryId + "participation=" + participation);
    Connection con = getConnection();
    try {
      Collection result = JournalDAO.getDayJournalHeadersForUser(con, day,
          userId, categoryId, participation);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getDaySchedulablesForUser(String day,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getNextDaySchedulablesForUser(String id)",
        "root.MSG_GEN_ENTER_METHOD", "day=" + day + "userId=" + userId
            + "categoryId=" + categoryId + "participation=" + participation);
    Connection con = getConnection();
    try {
      Collection result = JournalDAO.getNextJournalHeadersForUser(con, day,
          userId, categoryId, participation);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getNextDaySchedulablesForUser(String day,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation)
      throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "CalendarBmEJB. getPeriodSchedulablesForUser(String begin,String end,String userId,String categoryId,String participation)",
            "root.MSG_GEN_ENTER_METHOD", "begin=" + begin + ", end=" + end
                + ", userId=" + userId + ", categoryId=" + categoryId
                + ", participation=" + participation);
    Connection con = getConnection();
    try {
      Collection result = JournalDAO.getPeriodJournalHeadersForUser(con, begin,
          end, userId, categoryId, participation);
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.CalendarBmEJB. getPeriodSchedulablesForUser(String begin,String end,String userId,String categoryId,String participation)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNALS", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "CalendarBmEJB.countMonthSchedulablesForUser(String month,String userId,String categoryId,String participation)",
            "root.MSG_GEN_ENTER_METHOD", "month=" + month + ", userId="
                + userId + ", categoryId=" + categoryId + ", participation="
                + participation);
    Connection con = getConnection();
    try {
      Collection result = JournalDAO.countMonthJournalsForUser(con, month,
          userId, categoryId, participation);
      return result;
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

  public String addJournal(JournalHeader journal) throws RemoteException,
      CalendarException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.addJournal(JournalHeader journal)",
        "root.MSG_GEN_ENTER_METHOD");
    // verify the journal attributes are correctly set
    if (journal.getName() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "name");
    if (journal.getStartDate() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "startDate");
    if (journal.getDelegatorId() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "delegatorId");
    if (journal.getStartDate().compareTo(journal.getEndDate()) > 0)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_DATE_FIN_ERROR");
    if (journal.getStartHour() != null)
      if (journal.getEndHour() == null)
        throw new CalendarException("calendar", SilverpeasException.ERROR,
            "calendar.EX_PARAM_NULL", "endHour");
    if (journal.getEndHour() != null)
      if (journal.getStartHour() == null)
        throw new CalendarException("calendar", SilverpeasException.ERROR,
            "calendar.EX_PARAM_NULL", "startHour");
    if (journal.getStartDate().compareTo(journal.getEndDate()) == 0)
      if (journal.getStartHour() != null)
        if (journal.getStartHour().compareTo(journal.getEndHour()) > 0)
          throw new CalendarException("calendar", SilverpeasException.ERROR,
              "calendar.EX_HOUR_FIN_ERRORR");

    // write in DB
    Connection con = getConnection();
    try {
      String result = JournalDAO.addJournal(con, journal);
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

  public void updateJournal(JournalHeader journal) throws RemoteException,
      CalendarException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.updateJournal(JournalHeader journal)",
        "root.MSG_GEN_ENTER_METHOD");
    // verify the journal attributes are correctly set
    if (journal.getName() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "name");
    if (journal.getStartDate() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "startDate");
    if (journal.getDelegatorId() == null)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_PARAM_NULL", "delegatorId");
    if (journal.getStartDate().compareTo(journal.getEndDate()) > 0)
      throw new CalendarException("calendar", SilverpeasException.ERROR,
          "calendar.EX_DATE_FIN_ERROR");
    if (journal.getStartHour() != null)
      if (journal.getEndHour() == null)
        throw new CalendarException("calendar", SilverpeasException.ERROR,
            "calendar.EX_PARAM_NULL", "endHour");
    if (journal.getEndHour() != null)
      if (journal.getStartHour() == null)
        throw new CalendarException("calendar", SilverpeasException.ERROR,
            "calendar.EX_PARAM_NULL", "startHour");
    if (journal.getStartDate().compareTo(journal.getEndDate()) == 0)
      if (journal.getStartHour() != null)
        if (journal.getStartHour().compareTo(journal.getEndHour()) > 0)
          throw new CalendarException("calendar", SilverpeasException.ERROR,
              "calendar.EX_HOUR_FIN_ERRORR");

    Connection con = getConnection();
    try {
      JournalDAO.updateJournal(con, journal);
      // try {
      createIndex(journal, journal.getDelegatorId());
      Collection attendees = getJournalAttendees(journal.getId());
      for (Iterator i = attendees.iterator(); i.hasNext();) {
        Attendee attendee = (Attendee) i.next();
        createIndex(journal, attendee.getUserId());
      }
      // } catch (Exception e) {
      // SilverTrace.warn("calendar",
      // "CalendarBmEJB.updateJournal(JournalHeader journal)",
      // "root.EX_INDEX_FAILED","",e);
      // }
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
      JournalDAO.removeJournal(con, journalId);
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

  /*
   * public JournalHeader getJournalHeader(String journalId) throws
   * RemoteException { SilverTrace.info("calendar",
   * "CalendarBmEJB.getJournalHeader(String journalId)"
   * ,"root.MSG_GEN_ENTER_METHOD","journalId="+journalId); Connection con =
   * getConnection(); try { JournalHeader journal =
   * JournalDAO.getJournalHeader(con, journalId); return journal; } catch
   * (Exception e) { throw new
   * CalendarRuntimeException("CalendarBmEJB.getJournalHeader(String journalId)"
   * ,SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL",e); } finally {
   * freeConnection(con); } }
   */
  public JournalHeader getJournalHeader(String journalId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getJournalHeader(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      JournalHeader journal = JournalDAO.getJournalHeader(con, journalId);
      return journal;
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

  public Collection getExternalJournalHeadersForUser(String userId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getOutlookJournalHeadersForUser(String userId)",
        "root.MSG_GEN_ENTER_METHOD", "outlookId=" + userId);
    Connection con = getConnection();
    try {
      Collection journals = JournalDAO.getOutlookJournalHeadersForUser(con,
          userId);
      return journals;
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

  public Collection getExternalJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate) throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "CalendarBmEJB.getOutlookJournalHeadersForUserAfterDate(String userId, Date startDate)",
            "root.MSG_GEN_ENTER_METHOD", "outlookId=" + userId);
    Connection con = getConnection();
    try {
      Collection journals = JournalDAO
          .getOutlookJournalHeadersForUserAfterDate(con, userId, startDate);
      return journals;
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

  public Collection getJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate, int nbReturned) throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "CalendarBmEJB.getJournalHeadersForUserAfterDate(String userId, Date startDate, int nbReturned)",
            "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    Connection con = getConnection();
    try {
      Collection journals = JournalDAO.getJournalHeadersForUserAfterDate(con,
          userId, startDate, nbReturned);
      return journals;
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
  public void addJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.addJournalAttendee()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.addJournalAttendee(con, journalId, attendee);
      // try {
      createIndex(getJournalHeader(journalId), attendee.getUserId());
      // } catch (Exception e) {
      // SilverTrace.warn("calendar", "CalendarBmEJB.addJournalAttendee()",
      // "root.EX_INDEX_FAILED","",e);
      // }
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addJournalAttendee(String journalId,Attendee attendee)",
          SilverpeasException.ERROR,
          "calendar.MSG_CANT_CHANGE_JOURNAL_ATTENDEES", e);
    } finally {
      freeConnection(con);
    }

  }

  public void removeJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "CalendarBmEJB.removeJournalAttendee(String journalId,Attendee attendee)",
            "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.removeJournalAttendee(con, journalId, attendee);
      try {
        removeIndex(getJournalHeader(journalId), attendee.getUserId());
      } catch (Exception e) {
        SilverTrace
            .warn(
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

  public Collection getJournalAttendees(String journalId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getJournalAttendees(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      Collection attendees = AttendeeDAO.getJournalAttendees(con, journalId);
      return attendees;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalAttendees(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_JOURNAL_ATTENDEES",
          e);
    } finally {
      freeConnection(con);
    }

  }

  public void setJournalAttendees(String journalId, String[] userIds)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.setJournalAttendees(String journalId,String[] userIds)",
        "root.MSG_GEN_ENTER_METHOD");
    Collection current = getJournalAttendees(journalId);
    JournalHeader journalHeader = getJournalHeader(journalId);
    // search for element to remove
    Iterator j = current.iterator();
    while (j.hasNext()) {
      Attendee attendee = (Attendee) j.next();
      boolean toRemove = true;
      if (userIds != null)
        for (int i = 0; i < userIds.length; i++) {
          if (userIds[i].equals(attendee.getUserId()))
            toRemove = false;
        }
      if (toRemove)
        removeJournalAttendee(journalId, new Attendee(attendee.getUserId()));
    }
    // search for element to add
    if (userIds != null)
      for (int i = 0; i < userIds.length; i++) {
        boolean toAdd = true;
        Iterator k = current.iterator();
        while (k.hasNext()) {
          Attendee attendee = (Attendee) k.next();
          if (userIds[i].equals(attendee.getUserId()))
            toAdd = false;
        }
        if (toAdd) {
          Attendee attendee;
          if (userIds[i].equals(journalHeader.getDelegatorId()))
            attendee = new Attendee(userIds[i], ParticipationStatus.ACCEPTED);
          else
            attendee = new Attendee(userIds[i]);
          addJournalAttendee(journalId, attendee);
        }
      }
  }

  public void setJournalParticipationStatus(String journalId, String userId,
      String participation) throws RemoteException {
    SilverTrace
        .info(
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

  public void addToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.addToDoAttendee(String todoId,Attendee attendee)",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      AttendeeDAO.addToDoAttendee(con, todoId, attendee);
      // try {
      createIndex(getToDoHeader(todoId), attendee.getUserId());
      // } catch (Exception e) {
      // SilverTrace.warn("calendar",
      // "CalendarBmEJB.addToDoAttendee(String todoId,Attendee attendee)",
      // "root.EX_INDEX_FAILED","",e);
      // }
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.addToDoAttendee(String todoId,Attendee attendee)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES",
          e);
    } finally {
      freeConnection(con);
    }

  }

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
        SilverTrace
            .warn(
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

  public Collection getToDoAttendees(String todoId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getToDoAttendees(String todoId)",
        "root.MSG_GEN_ENTER_METHOD", "todoId=" + todoId);
    Connection con = getConnection();
    try {
      Collection attendees = AttendeeDAO.getToDoAttendees(con, todoId);
      return attendees;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getToDoAttendees(String todoId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_TODO_ATTENDEES", e);
    } finally {
      freeConnection(con);
    }

  }

  public void setToDoAttendees(String todoId, String[] userIds)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.setToDoAttendees(String todoId,String[] userIds)",
        "root.MSG_GEN_ENTER_METHOD");
    Collection current = getToDoAttendees(todoId);

    // search for element to remove
    Iterator j = current.iterator();
    while (j.hasNext()) {
      Attendee attendee = (Attendee) j.next();
      boolean toRemove = true;
      if (userIds != null)
        for (int i = 0; i < userIds.length; i++) {
          if (userIds[i].equals(attendee.getUserId()))
            toRemove = false;
        }
      if (toRemove)
        removeToDoAttendee(todoId, new Attendee(attendee.getUserId()));
    }
    // search for element to add
    if (userIds != null)
      for (int i = 0; i < userIds.length; i++) {
        boolean toAdd = true;
        Iterator k = current.iterator();
        while (k.hasNext()) {
          Attendee attendee = (Attendee) k.next();
          if (userIds[i].equals(attendee.getUserId()))
            toAdd = false;
        }
        if (toAdd) {
          Attendee attendee;
          /*
           * if (userIds[i].equals(todoHeader.getDelegatorId())) attendee = new
           * Attendee(journalId, userIds[i], ParticipationStatus.ACCEPTED); else
           */
          attendee = new Attendee(userIds[i]);
          addToDoAttendee(todoId, attendee);
        }
      }
  }

  /**
   * methods for categories
   */
  public Collection getAllCategories() throws RemoteException {
    SilverTrace.info("calendar", "CalendarBmEJB.getAllCategories()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      Collection categories = CategoryDAO.getAllCategories(con);
      return categories;
    } catch (Exception e) {
      throw new CalendarRuntimeException("CalendarBmEJB.getAllCategories()",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

  public Category getCategory(String categoryId) throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getCategory(String categoryId)",
        "root.MSG_GEN_ENTER_METHOD", "categoryId=" + categoryId);
    Connection con = getConnection();
    try {
      Category category = CategoryDAO.getCategory(con, categoryId);
      return category;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getCategory(String categoryId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

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

  public void removeJournalCategory(String journalId, String categoryId)
      throws RemoteException {
    SilverTrace
        .info(
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

  public Collection getJournalCategories(String journalId)
      throws RemoteException {
    SilverTrace.info("calendar",
        "CalendarBmEJB.getJournalCategories(String journalId)",
        "root.MSG_GEN_ENTER_METHOD", "journalId=" + journalId);
    Connection con = getConnection();
    try {
      Collection categories = CategoryDAO.getJournalCategories(con, journalId);
      return categories;
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "CalendarBmEJB.getJournalCategories(String journalId)",
          SilverpeasException.ERROR, "calendar.MSG_CANT_GET_CATEGORIES", e);
    } finally {
      freeConnection(con);
    }

  }

  public void setJournalCategories(String journalId, String[] categoryIds)
      throws RemoteException {
    SilverTrace
        .info(
            "calendar",
            "CalendarBmEJB.setJournalCategories(String journalId,String[] categoryIds)",
            "root.MSG_GEN_ENTER_METHOD");
    Collection current = getJournalCategories(journalId);
    // search for element to remove
    Iterator j = current.iterator();
    while (j.hasNext()) {
      Category category = (Category) j.next();
      boolean toRemove = true;
      if (categoryIds != null)
        for (int i = 0; i < categoryIds.length; i++) {
          if (categoryIds[i].equals(category.getId()))
            toRemove = false;
        }
      if (toRemove)
        removeJournalCategory(journalId, category.getId());
    }
    // search for element to add
    if (categoryIds != null)
      for (int i = 0; i < categoryIds.length; i++) {
        boolean toAdd = true;
        Iterator k = current.iterator();
        while (k.hasNext()) {
          Category category = (Category) k.next();
          if (categoryIds[i].equals(category.getId()))
            toAdd = false;
        }
        if (toAdd)
          addJournalCategory(journalId, categoryIds[i]);
      }
  }

  /**
   * methode de reindexation de tous les todos
   */
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
        Collection attendees = getToDoAttendees(todo.getId());
        for (Iterator i = attendees.iterator(); i.hasNext();) {
          Attendee attendee = (Attendee) i.next();
          createIndex(todo, attendee.getUserId());
        }
      }

    } catch (Exception se) {
      SilverTrace.warn("calendar", "CalendarBmEJB.indexAllTodo()",
          "root.EX_INDEX_FAILED", "", se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null)
          con.close();
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
  public List getHolidayDates(String userId) throws RemoteException {
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

  public List getHolidayDates(String userId, Date beginDate, Date endDate)
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

  public void addHolidayDates(List holidayDates) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.addHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDates.size()="
            + holidayDates.size());
    Connection con = getConnection();
    try {
      HolidayDetail holiday = null;
      for (int h = 0; h < holidayDates.size(); h++) {
        holiday = (HolidayDetail) holidayDates.get(h);
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

  public void removeHolidayDates(List holidayDates) throws RemoteException {
    SilverTrace.info("calendar", "calendarBmEJB.removeHolidayDates()",
        "root.MSG_GEN_ENTER_METHOD", "holidayDates.size()="
            + holidayDates.size());
    Connection con = getConnection();
    try {
      HolidayDetail holiday = null;
      for (int h = 0; h < holidayDates.size(); h++) {
        holiday = (HolidayDetail) holidayDates.get(h);
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

        journal = JournalDAO.getJournalHeaderFromResultSet(rs);
        createIndex(journal, journal.getDelegatorId());
        Collection attendees = getJournalAttendees(journal.getId());
        for (Iterator i = attendees.iterator(); i.hasNext();) {
          Attendee attendee = (Attendee) i.next();
          createIndex(journal, attendee.getUserId());
        }
      }

    } catch (Exception se) {
      SilverTrace.warn("calendar", "CalendarBmEJB.indexAllJournal()",
          "root.EX_INDEX_FAILED", "", se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null)
          con.close();
      } catch (SQLException se) {
        SilverTrace.warn("calendar", "CalendarBmEJB.indexAllJournal()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", se);
      }

    }

  }

  private void createIndex(Schedulable detail, String userId) {
    try {
      FullIndexEntry indexEntry = null;
      if (detail instanceof ToDoHeader)
        indexEntry = new FullIndexEntry("user@" + userId + "_todo", "todo",
            detail.getId());
      else
        indexEntry = new FullIndexEntry("user@" + userId + "_agenda", "agenda",
            detail.getId());
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
    if (detail instanceof ToDoHeader)
      indexEntry = new IndexEntryPK("user@" + userId + "_todo", "todo", detail
          .getId());
    else
      indexEntry = new IndexEntryPK("user@" + userId + "_agenda", "agenda",
          detail.getId());
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
  public void ejbRemove() {
    // Debug.debug(700, "CalendarBmEJB.ejbRemove()", "enter", null, null);
  }

  // public void ejbActivate() throws javax.ejb.EJBException,
  // java.rmi.RemoteException
  public void ejbActivate() {
    // Debug.debug(700, "CalendarBmEJB.ejbActivate()", "enter", null, null);
  }

  // public void ejbPassivate() throws javax.ejb.EJBException,
  // java.rmi.RemoteException
  public void ejbPassivate() {
    // Debug.debug(700, "CalendarBmEJB.ejbPassivate()", "enter", null, null);
  }

  // public void setSessionContext(final javax.ejb.SessionContext p1) throws
  // javax.ejb.EJBException, java.rmi.RemoteException
  public void setSessionContext(final javax.ejb.SessionContext p1)
      throws javax.ejb.EJBException, java.rmi.RemoteException {
    // Debug.debug(700, "CalendarBmEJB.setSessionContext()", "enter", null,
    // null);
  }
}
