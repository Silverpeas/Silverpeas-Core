/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.calendar.control;

import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.HolidayDetail;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.SchedulableCount;
import com.stratelia.webactiv.calendar.model.ToDoHeader;
import com.stratelia.webactiv.calendar.socialnetwork.SocialInformationEvent;

import javax.ejb.Local;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Local
public interface SilverpeasCalendar {

  /**
   * getDaySchedulablesForUser() for a particular user returns all the events scheduled on a
   * particular day. This includes all kinds of events
   */
  public Collection<JournalHeader> getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) ;

  /**
   * getNextDaySchedulablesForUser() for a particular user returns the next events scheduled. This
   * includes all kinds of events
   */
  public Collection<JournalHeader> getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) ;

  /**
   * getNextEventForUser for a particular user returns the next events scheduled. This includes all
   * kinds of events
   */
  public List<JournalHeader> getNextEventsForUser(String day, String userId,
      String classification, Date begin, Date end) ;

  /**
   * get Next Social Events for a given list of my Contacts returns the next events
   * (SocialInformationEvent). This includes all kinds of events
   */
  public List<SocialInformationEvent> getNextEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end) ;

  /**
   * get Last Social Events for a given list of my Contacts returns the next events
   * (SocialInformationEvent). This includes all kinds of events
   */
  public List<SocialInformationEvent> getLastEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end) ;

  /**
   * get the my last Events of information and number of Item and the first Index includes all kinds
   * of events
   * @return: List <SocialInformation>
   * @param :String day,String myId , int numberOfElement, int firstIndex
   */
  public List<SocialInformationEvent> getMyLastEvents(String day, String myId,
      Date begin, Date end) ;

  /**
   * getPeriodSchedulablesForUser() for a particular user returns all the events scheduled during a
   * particular period. This includes all kinds of events
   */
  public Collection<JournalHeader> getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation)
      ;

  /**
   * countMonthSchedulablesForUser() for a particular user, counts the number of schedules for each
   * day in the month
   */
  public Collection<SchedulableCount> countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) ;

  /**
   * methods for tentative schedules (not yet accepted or declined events)
   */
  public boolean hasTentativeSchedulablesForUser(String userId)
      ;

  public Collection<JournalHeader> getTentativeSchedulablesForUser(String userId)
      ;

  public Collection<ToDoHeader> getNotCompletedToDosForUser(String userId)
      ;

  public Collection<ToDoHeader> getOrganizerToDos(String organizerId)
      ;

  public Collection<ToDoHeader> getClosedToDos(String organizerId) ;

  public Collection<ToDoHeader> getExternalTodos(String spaceId, String componentId,
      String externalId) ;

  /**
   * addJournal() add a journal entry in the database
   */
  public String addJournal(JournalHeader journal) ;

  /**
   * addToDo() add a todo entry in the database
   */
  public String addToDo(ToDoHeader todo) ;

  /**
   * updateJournal() update the journal entry, specified by the id, in the database
   */
  public void updateJournal(JournalHeader journal) ;

  /**
   * updateToDo() update the todo entry, specified by the id, in the database
   */
  /*
   * public void updateToDo(ToDoHeader todo) , CreateException;
   */
  public void updateToDo(ToDoHeader todo) ;

  /**
   * removeJournal() remove the journal entry specified by the id
   */
  public void removeJournal(String journalId) ;

  /**
   * removeToDo() remove the todo entry specified by the id
   */
  public void removeToDo(String id) ;

  /**
   * removeToDoByInstanceId remove all todo of the specified instance
   */
  public void removeToDoByInstanceId(String instanceId) ;

  /**
   * getJournalHeader() returns the journalHeader represented by the journalId
   */
  public JournalHeader getJournalHeader(String journalId)
      ;

  /**
   * getOutlookJournalHeadersForUser() returns the journalHeaders for user represented by the userId
   */
  public Collection<JournalHeader> getExternalJournalHeadersForUser(String userId)
      ;

  /**
   * getExternalJournalHeadersForUserAfterDate() returns the journalHeaders for user represented by
   * the userId for which start date after given date
   */
  public Collection<JournalHeader> getExternalJournalHeadersForUserAfterDate(String userId,
      Date startDate) ;

  /**
   * getJournalHeadersForUserAfterDate() returns the journalHeaders for user represented by the
   * userId for which start date after given date
   */
  public Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userId,
      Date startDate, int nbReturned) ;

  /**
   * getToDoHeader() returns the ToDoHeader represented by the todoId
   */
  public ToDoHeader getToDoHeader(String todoId) ;

  /**
   * methods for attendees
   */
  public void addJournalAttendee(String journalId, Attendee attendee)
      ;

  public void removeJournalAttendee(String journalId, Attendee attendee)
      ;

  public Collection<Attendee> getJournalAttendees(String journalId)
      ;

  public void setJournalAttendees(String journalId, String[] userIds)
      ;

  public void setJournalParticipationStatus(String journalId, String userId,
      String participation) ;

  public void addToDoAttendee(String todoId, Attendee attendee)
      ;

  public void removeToDoAttendee(String todoId, Attendee attendee)
      ;

  public Collection<Attendee> getToDoAttendees(String todoId) ;

  public void setToDoAttendees(String todoId, String[] userIds)
      ;

  /**
   * methods for categories
   */
  public Collection<Category> getAllCategories() ;

  public Category getCategory(String categoryId) ;

  public Collection<Category> getJournalCategories(String journalId)
      ;

  public void addJournalCategory(String journalId, String categoryId)
      ;

  public void removeJournalCategory(String journalId, String categoryId)
      ;

  public void setJournalCategories(String journalId, String[] categoryIds)
      ;

  // methods for reindexation
  public void indexAllTodo() ;

  public void indexAllJournal() ;

  // Gestion des jours non travailles
  public boolean isHolidayDate(HolidayDetail date) ;

  public List<String> getHolidayDates(String userId) ;

  public List<String> getHolidayDates(String userId, Date beginDate, Date endDate)
      ;

  public void addHolidayDate(HolidayDetail holiday) ;

  public void addHolidayDates(List<HolidayDetail> holidayDates) ;

  public void removeHolidayDate(HolidayDetail holiday) ;

  public void removeHolidayDates(List<HolidayDetail> holidayDates) ;
}
