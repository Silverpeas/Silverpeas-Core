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

package com.stratelia.webactiv.calendar.control;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.calendar.model.HolidayDetail;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.SchedulableCount;
import com.stratelia.webactiv.calendar.model.ToDoHeader;

public interface CalendarBmBusinessSkeleton {

  /**
   * getDaySchedulablesForUser() for a particular user returns all the events scheduled on a
   * particular day. This includes all kinds of events
   */
  public Collection<JournalHeader> getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException;

  /**
   * getNextDaySchedulablesForUser() for a particular user returns the next events scheduled. This
   * includes all kinds of events
   */
  public Collection<JournalHeader> getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException;

  /**
   * getPeriodSchedulablesForUser() for a particular user returns all the events scheduled during a
   * particular period. This includes all kinds of events
   */
  public Collection<JournalHeader> getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation)
      throws RemoteException;

  /**
   * countMonthSchedulablesForUser() for a particular user, counts the number of schedules for each
   * day in the month
   */
  public Collection<SchedulableCount> countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) throws RemoteException;

  /**
   * methods for tentative schedules (not yet accepted or declined events)
   */
  public boolean hasTentativeSchedulablesForUser(String userId)
      throws RemoteException;

  public Collection<JournalHeader> getTentativeSchedulablesForUser(String userId)
      throws RemoteException;

  public Collection<ToDoHeader> getNotCompletedToDosForUser(String userId)
      throws RemoteException;

  public Collection<ToDoHeader> getOrganizerToDos(String organizerId)
      throws RemoteException;

  public Collection<ToDoHeader> getClosedToDos(String organizerId) throws RemoteException;

  public Collection<ToDoHeader> getExternalTodos(String spaceId, String componentId,
      String externalId) throws RemoteException;

  /**
   * addJournal() add a journal entry in the database
   */
  public String addJournal(JournalHeader journal) throws RemoteException;

  /**
   * addToDo() add a todo entry in the database
   */
  public String addToDo(ToDoHeader todo) throws RemoteException;

  /**
   * updateJournal() update the journal entry, specified by the id, in the database
   */
  public void updateJournal(JournalHeader journal) throws RemoteException;

  /**
   * updateToDo() update the todo entry, specified by the id, in the database
   */
  /*
   * public void updateToDo(ToDoHeader todo) throws RemoteException, CreateException;
   */
  public void updateToDo(ToDoHeader todo) throws RemoteException;

  /**
   * removeJournal() remove the journal entry specified by the id
   */
  public void removeJournal(String journalId) throws RemoteException;

  /**
   * removeToDo() remove the todo entry specified by the id
   */
  public void removeToDo(String id) throws RemoteException;

  /**
   * removeToDoByInstanceId remove all todo of the specified instance
   */
  public void removeToDoByInstanceId(String instanceId) throws RemoteException;

  /**
   * getJournalHeader() returns the journalHeader represented by the journalId
   */
  public JournalHeader getJournalHeader(String journalId)
      throws RemoteException;

  /**
   * getOutlookJournalHeadersForUser() returns the journalHeaders for user represented by the userId
   */
  public Collection<JournalHeader> getExternalJournalHeadersForUser(String userId)
      throws RemoteException;

  /**
   * getExternalJournalHeadersForUserAfterDate() returns the journalHeaders for user represented by
   * the userId for which start date after given date
   */
  public Collection<JournalHeader> getExternalJournalHeadersForUserAfterDate(String userId,
      Date startDate) throws RemoteException;

  /**
   * getJournalHeadersForUserAfterDate() returns the journalHeaders for user represented by the
   * userId for which start date after given date
   */
  public Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userId,
      Date startDate, int nbReturned) throws RemoteException;

  /**
   * getToDoHeader() returns the ToDoHeader represented by the todoId
   */
  public ToDoHeader getToDoHeader(String todoId) throws RemoteException;

  /**
   * methods for attendees
   */
  public void addJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException;

  public void removeJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException;

  public Collection<Attendee> getJournalAttendees(String journalId)
      throws RemoteException;

  public void setJournalAttendees(String journalId, String[] userIds)
      throws RemoteException;

  public void setJournalParticipationStatus(String journalId, String userId,
      String participation) throws RemoteException;

  public void addToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException;

  public void removeToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException;

  public Collection<Attendee> getToDoAttendees(String todoId) throws RemoteException;

  public void setToDoAttendees(String todoId, String[] userIds)
      throws RemoteException;

  /**
   * methods for categories
   */
  public Collection<Category> getAllCategories() throws RemoteException;

  public Category getCategory(String categoryId) throws RemoteException;

  public Collection<Category> getJournalCategories(String journalId)
      throws RemoteException;

  public void addJournalCategory(String journalId, String categoryId)
      throws RemoteException;

  public void removeJournalCategory(String journalId, String categoryId)
      throws RemoteException;

  public void setJournalCategories(String journalId, String[] categoryIds)
      throws RemoteException;

  // methods for reindexation
  public void indexAllTodo() throws RemoteException;

  public void indexAllJournal() throws RemoteException;

  // Gestion des jours non travailles
  public boolean isHolidayDate(HolidayDetail date) throws RemoteException;

  public List<String> getHolidayDates(String userId) throws RemoteException;

  public List<String> getHolidayDates(String userId, Date beginDate, Date endDate)
      throws RemoteException;

  public void addHolidayDate(HolidayDetail holiday) throws RemoteException;

  public void addHolidayDates(List<HolidayDetail> holidayDates) throws RemoteException;

  public void removeHolidayDate(HolidayDetail holiday) throws RemoteException;

  public void removeHolidayDates(List<HolidayDetail> holidayDates) throws RemoteException;

}
