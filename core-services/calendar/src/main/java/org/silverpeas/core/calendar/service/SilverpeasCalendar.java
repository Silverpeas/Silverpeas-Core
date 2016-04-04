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

import org.silverpeas.core.calendar.model.Attendee;
import org.silverpeas.core.calendar.model.Category;
import org.silverpeas.core.calendar.model.HolidayDetail;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.calendar.model.SchedulableCount;
import org.silverpeas.core.calendar.model.ToDoHeader;
import org.silverpeas.core.calendar.model.TodoDetail;
import org.silverpeas.core.calendar.socialnetwork.SocialInformationEvent;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface SilverpeasCalendar {

  /**
   * getDaySchedulablesForUser() for a particular user returns all the events scheduled on a
   * particular day. This includes all kinds of events
   */
  Collection<JournalHeader> getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation);

  /**
   * getNextDaySchedulablesForUser() for a particular user returns the next events scheduled. This
   * includes all kinds of events
   */
  Collection<JournalHeader> getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation);

  /**
   * getNextEventForUser for a particular user returns the next events scheduled. This includes all
   * kinds of events
   */
  List<JournalHeader> getNextEventsForUser(String day, String userId, String classification,
      Date begin, Date end);

  /**
   * get Next Social Events for a given list of my Contacts returns the next events
   * (SocialInformationEvent). This includes all kinds of events
   */
  List<SocialInformationEvent> getNextEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end);

  /**
   * get Last Social Events for a given list of my Contacts returns the next events
   * (SocialInformationEvent). This includes all kinds of events
   */
  List<SocialInformationEvent> getLastEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end);

  /**
   * get the my last Events of information and number of Item and the first Index includes all
   * kinds
   * of events
   */
  List<SocialInformationEvent> getMyLastEvents(String day, String myId, Date begin,
      Date end);

  /**
   * getPeriodSchedulablesForUser() for a particular user returns all the events scheduled during a
   * particular period. This includes all kinds of events
   */
  Collection<JournalHeader> getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation);

  /**
   * countMonthSchedulablesForUser() for a particular user, counts the number of schedules for each
   * day in the month
   */
  Collection<SchedulableCount> countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation);

  /**
   * methods for tentative schedules (not yet accepted or declined events)
   */
  boolean hasTentativeSchedulablesForUser(String userId);

  Collection<JournalHeader> getTentativeSchedulablesForUser(String userId);

  Collection<ToDoHeader> getNotCompletedToDosForUser(String userId);

  List<String> getAllToDoForUser(String userId);

  Collection<ToDoHeader> getOrganizerToDos(String organizerId);

  Collection<ToDoHeader> getClosedToDos(String organizerId);

  Collection<ToDoHeader> getExternalTodos(String spaceId, String componentId,
      String externalId);

  /**
   * addJournal() add a journal entry in the database
   */
  String addJournal(JournalHeader journal);

  /**
   * addToDo() add a todo entry in the database
   */
  String addToDo(ToDoHeader todo);

  String addToDo(TodoDetail todo);

  /**
   * updateJournal() update the journal entry, specified by the id, in the database
   */
  void updateJournal(JournalHeader journal);

  /**
   * updateToDo() update the todo entry, specified by the id, in the database
   */
  /*
   *  void updateToDo(ToDoHeader todo) , CreateException;
   */
  void updateToDo(ToDoHeader todo);

  /**
   * removeJournal() remove the journal entry specified by the id
   */
  void removeJournal(String journalId);

  /**
   * removeToDo() remove the todo entry specified by the id
   */
  void removeToDo(String id);

  void removeToDoFromExternal(String spaceId, String componentId, String externalId);

  void removeAttendeeInToDoFromExternal(String componentId, String externalId, String userId);

  /**
   * getJournalHeader() returns the journalHeader represented by the journalId
   */
  JournalHeader getJournalHeader(String journalId);

  /**
   * getOutlookJournalHeadersForUser() returns the journalHeaders for user represented by the
   * userId
   */
  Collection<JournalHeader> getExternalJournalHeadersForUser(String userId);

  /**
   * getExternalJournalHeadersForUserAfterDate() returns the journalHeaders for user represented by
   * the userId for which start date after given date
   */
  Collection<JournalHeader> getExternalJournalHeadersForUserAfterDate(String userId,
      Date startDate);

  /**
   * getJournalHeadersForUserAfterDate() returns the journalHeaders for user represented by the
   * userId for which start date after given date
   */
  Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userId, Date startDate,
      int nbReturned);

  /**
   * getToDoHeader() returns the ToDoHeader represented by the todoId
   */
  ToDoHeader getToDoHeader(String todoId);

  TodoDetail getTodoDetail(String todoId);

  /**
   * methods for attendees
   */
  void addJournalAttendee(String journalId, Attendee attendee);

  void removeJournalAttendee(String journalId, Attendee attendee);

  Collection<Attendee> getJournalAttendees(String journalId);

  void setJournalAttendees(String journalId, String[] userIds);

  void setJournalParticipationStatus(String journalId, String userId, String participation);

  void addToDoAttendee(String todoId, Attendee attendee);

  void removeToDoAttendee(String todoId, Attendee attendee);

  Collection<Attendee> getToDoAttendees(String todoId);

  void setToDoAttendees(String todoId, String[] userIds);

  /**
   * methods for categories
   */
  Collection<Category> getAllCategories();

  Category getCategory(String categoryId);

  Collection<Category> getJournalCategories(String journalId);

  void addJournalCategory(String journalId, String categoryId);

  void removeJournalCategory(String journalId, String categoryId);

  void setJournalCategories(String journalId, String[] categoryIds);

  // methods for reindexation
  void indexAllTodo();

  void indexAllJournal();

  // Gestion des jours non travailles
  boolean isHolidayDate(HolidayDetail date);

  List<String> getHolidayDates(String userId);

  List<String> getHolidayDates(String userId, Date beginDate, Date endDate);

  void addHolidayDate(HolidayDetail holiday);

  void addHolidayDates(List<HolidayDetail> holidayDates);

  void removeHolidayDate(HolidayDetail holiday);

  void removeHolidayDates(List<HolidayDetail> holidayDates);
}
