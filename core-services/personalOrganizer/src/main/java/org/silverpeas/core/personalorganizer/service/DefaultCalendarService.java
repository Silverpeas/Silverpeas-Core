/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.Category;
import org.silverpeas.core.personalorganizer.model.HolidayDetail;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.model.ParticipationStatus;
import org.silverpeas.core.personalorganizer.model.Schedulable;
import org.silverpeas.core.personalorganizer.model.SchedulableCount;
import org.silverpeas.core.personalorganizer.model.ToDoHeader;
import org.silverpeas.core.personalorganizer.model.TodoDetail;
import org.silverpeas.core.personalorganizer.socialnetwork.SocialInformationEvent;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Calendar service layer to manager calendars in Silverpeas
 */
@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultCalendarService implements SilverpeasCalendar, ComponentInstanceDeletion {

  private static final String CANNOT_GET_JOURNALS_OF_USER = "Cannot get journals of user ";
  private static final String NO_SUCH_JOURNAL = "No such journal ";
  private static final String ERROR_WHILE_PARSING_THE_DATES_OF_A_JOURNAL =
      "Error while parsing the dates of a journal";
  private static final String IDX_USER_PREFIX = "user@";
  private static final String CANNOT_GET_JOURNALS = "Cannot get journals";
  @Inject
  private JournalDAO journalDAO;

  // private methods to use in all this ejb methods
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot open connection");
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, e);

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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param userId the user identifier
   * @return
   */
  @Override
  public SilverpeasList<ToDoHeader> getNotCompletedToDosForUser(String userId) {
    try {
      return ToDoDAO.getNotCompletedToDoHeadersForUser(userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get todos of user " + userId, e);
    }
  }

  @Override
  public List<String> getAllToDoForUser(final String userId) {
    try {
      return ToDoDAO.getAllTodoByUser(userId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get todos of user " + userId, e);
    }
  }

  @Override
  public SilverpeasList<ToDoHeader> getOrganizerToDos(String organizerId) {
    try {
      return ToDoDAO.getOrganizerToDoHeaders(organizerId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get journals from organizer " + organizerId, e);
    }
  }

  @Override
  public SilverpeasList<ToDoHeader> getClosedToDos(String organizerId) {
    try {
      return ToDoDAO.getClosedToDoHeaders(organizerId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get journals from organizer " + organizerId, e);
    }
  }

  @Override
  public Collection<ToDoHeader> getExternalTodos(String spaceId, String componentId,
      String externalId) {
    try {
      return ToDoDAO.getToDoHeadersByExternalId(componentId, externalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException(
          "Cannot get journals in space " + spaceId + ", application " + componentId +
              ", and external resource " + externalId, e);
    }
  }

  @Override
  public ToDoHeader getToDoHeader(String todoId) {
    try {
      return ToDoDAO.getToDoHeader(todoId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get the todo " + todoId, e);
    }
  }

  @Override
  public TodoDetail getTodoDetail(String id) {
    try {
      TodoDetail detail = TodoDetail.fromToDoHeader(getToDoHeader(id));
      List<Attendee> attendees = new ArrayList<>(getToDoAttendees(id));
      detail.setAttendees(attendees);
      return detail;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  private Collection<ToDoHeader> getToDoHeadersByInstanceId(String instanceId) {
    try {
      return ToDoDAO.getToDoHeadersByInstanceId(instanceId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get todos in application " + instanceId, e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateToDo(ToDoHeader todo) {
    checkTodo(todo);

    Connection con = getConnection();
    try {
      ToDoDAO.updateToDo(con, todo); // SQLEXCeption

      createIndex(todo, todo.getDelegatorId()); // vide
      Collection<Attendee> attendees = getToDoAttendees(todo.getId());
      for (Attendee attendee : attendees) {
        createIndex(todo, attendee.getUserId()); // vide
      }
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot update the todo " + todo.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void checkTodo(final ToDoHeader todo) {
    if (todo.getName() == null) {
      throw new CalendarRuntimeException("Todo's name is null!");
    }
    if (todo.getDelegatorId() == null) {
      throw new CalendarRuntimeException("Todo's delegator is null!");
    }
    if (todo.getEndDate() != null && todo.getStartDate() != null &&
        todo.getStartDate().after(todo.getEndDate())) {
      throw new CalendarRuntimeException("The todo period is incorrect!");
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String addToDo(ToDoHeader todo) {
    checkTodo(todo);
    Connection con = getConnection();
    try {
      String result = ToDoDAO.addToDo(con, todo);
      todo.setId(result);
      createIndex(todo, todo.getDelegatorId());
      return result;
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot create new todo", e);
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
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeToDo(String id) {
    // get the detail for desindexation
    ToDoHeader todo = getToDoHeader(id);
    try {
      // remove attendees and associated indexes
      setToDoAttendees(id, ArrayUtil.emptyStringArray());
      ToDoDAO.removeToDo(id);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot delete todo " + id, e);
    }
    removeIndex(todo, todo.getDelegatorId());
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
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

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, e);
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, e);
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, e);
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, e);
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
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot add journal", e);
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
      throw new CalendarRuntimeException("Cannot update journal " + journal.getId(), se);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(NO_SUCH_JOURNAL + journal.getId(), ce);
    } finally {
      DBUtil.close(con);
    }
  }

  private void validateJournal(JournalHeader journal) {
    // verify the journal attributes are correctly set
    if (journal.getName() == null) {
      throw new CalendarRuntimeException("The journal's name is null!");
    }
    if (journal.getStartDate() == null) {
      throw new CalendarRuntimeException("The journal's starting date is null!");
    }
    if (journal.getDelegatorId() == null) {
      throw new CalendarRuntimeException("The journal's delegator is null!");
    }
    if (journal.getStartDate().after(journal.getEndDate())) {
      throw new CalendarRuntimeException("The journal's period is invalid!");
    }
    if (journal.getStartHour() != null && journal.getEndHour() == null) {
      throw new CalendarRuntimeException("The journal doesn't end!");
    }
    if (journal.getEndHour() != null && journal.getStartHour() == null) {
      throw new CalendarRuntimeException("The journal starting hour isn't defined!");
    }
    if (journal.getStartDate().equals(journal.getEndDate()) && journal.getStartHour() != null &&
        journal.getStartHour().compareTo(journal.getEndHour()) > 0) {
      throw new CalendarRuntimeException("The journal period is invalid!");
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeJournal(String journalId) {
    Connection con = getConnection();
    try {
      JournalHeader journal = getJournalHeader(journalId);
      // remove attendees and associated indexes
      setJournalAttendees(journalId, ArrayUtil.emptyStringArray());
      CategoryDAO.removeJournal(con, journalId);
      getJournalDAO().removeJournal(con, journalId);
      removeIndex(journal, journal.getDelegatorId());
    } catch (SQLException se) {
      throw new CalendarRuntimeException("Cannot remove journal " + journalId, se);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(NO_SUCH_JOURNAL + journalId, ce);
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
      throw new CalendarRuntimeException("Cannot get journal " + journalId, se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException("Cannot parse dates of journal " + journalId, pe);
    } catch (CalendarException ce) {
      throw new CalendarRuntimeException(NO_SUCH_JOURNAL + journalId, ce);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<JournalHeader> getExternalJournalHeadersForUser(String userId) {
    try (final Connection con = getConnection()) {
      return getJournalDAO().getOutlookJournalHeadersForUser(con, userId);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(ERROR_WHILE_PARSING_THE_DATES_OF_A_JOURNAL, pe);
    }
  }

  @Override
  public Collection<JournalHeader> getExternalJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate) {
    try (final Connection con = getConnection()) {
      return getJournalDAO().getOutlookJournalHeadersForUserAfterDate(con, userId, startDate);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(ERROR_WHILE_PARSING_THE_DATES_OF_A_JOURNAL, pe);
    }
  }

  @Override
  public Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userId,
      java.util.Date startDate, int nbReturned) {
    try (final Connection con = getConnection()) {
      return getJournalDAO().getJournalHeadersForUserAfterDate(con, userId, startDate, nbReturned);
    } catch (SQLException se) {
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS_OF_USER + userId, se);
    } catch (java.text.ParseException pe) {
      throw new CalendarRuntimeException(ERROR_WHILE_PARSING_THE_DATES_OF_A_JOURNAL, pe);
    }
  }

  /**
   * methods for attendees
   */
  @Override
  public void addJournalAttendee(String journalId, Attendee attendee) {
    try (final Connection con = getConnection()) {
      AttendeeDAO.addJournalAttendee(con, journalId, attendee);
      createIndex(getJournalHeader(journalId), attendee.getUserId());
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot add attendee to the journal " + journalId, e);
    }

  }

  @Override
  public void removeJournalAttendee(String journalId, Attendee attendee) {
    try (final Connection con = getConnection()) {
      AttendeeDAO.removeJournalAttendee(con, journalId, attendee);
      removeIndex(getJournalHeader(journalId), attendee.getUserId());

    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot remove attendee to the journal " + journalId, e);
    }

  }

  @Override
  public Collection<Attendee> getJournalAttendees(String journalId) {
    try {
      return AttendeeDAO.getJournalAttendees(journalId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get attendees to the journal " + journalId, e);
    }
  }

  @Override
  public void setJournalAttendees(String journalId, String[] userIds) {
    Collection<Attendee> current = getJournalAttendees(journalId);
    JournalHeader journalHeader = getJournalHeader(journalId);
    final List<String> usersToAdd =
        userIds != null ? Arrays.asList(userIds) : Collections.emptyList();
    // search for element to remove
    current.stream()
        .filter(a -> !usersToAdd.contains(a.getUserId()))
        .forEach(a -> removeJournalAttendee(journalId, new Attendee(a.getUserId())));

    // search for element to add
    List<String> attendees = current.stream().map(Attendee::getUserId).collect(Collectors.toList());
    usersToAdd.stream()
        .filter(u -> attendees.stream().noneMatch(a -> a.equals(u)))
        .forEach(u -> {
          final Attendee attendee;
          if (u.equals(journalHeader.getDelegatorId())) {
            attendee = new Attendee(u, ParticipationStatus.ACCEPTED);
          } else {
            attendee = new Attendee(u);
          }
          addJournalAttendee(journalId, attendee);
        });
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
      throw new CalendarRuntimeException("Cannot update the journal " + journalId, e);
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
      throw new CalendarRuntimeException("Cannot add attendee to the todo " + todoId, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeToDoAttendee(String todoId, Attendee attendee) {
    Connection con = getConnection();
    try {
      AttendeeDAO.removeToDoAttendee(con, todoId, attendee);
      removeIndex(getToDoHeader(todoId), attendee.getUserId());

    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot remove attendee to the todo " + todoId, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public List<Attendee> getToDoAttendees(String todoId) {
    try {
      return AttendeeDAO.getToDoAttendees(todoId);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot get attendees to the todo " + todoId, e);
    }
  }

  @Override
  public Map<String, List<Attendee>> getToDoAttendees(final Collection<String> todoIds) {
    try {
      return AttendeeDAO.getToDoAttendees(todoIds);
    } catch (Exception e) {
      throw new CalendarRuntimeException("Cannot add attendees to several todos", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setToDoAttendees(String todoId, String[] userIds) {
    Collection<Attendee> current = getToDoAttendees(todoId);
    final List<String> usersToAdd =
        userIds != null ? Arrays.asList(userIds) : Collections.emptyList();
    // search for element to remove
    current.stream()
        .filter(a -> !usersToAdd.contains(a.getUserId()))
        .forEach(a -> removeToDoAttendee(todoId, new Attendee(a.getUserId())));

    // search for element to add
    List<String> attendees = current.stream().map(Attendee::getUserId).collect(Collectors.toList());
    usersToAdd.stream()
        .filter(u -> attendees.stream().noneMatch(a -> a.equals(u)))
        .forEach(u -> {
          Attendee attendee = new Attendee(u);
          addToDoAttendee(todoId, attendee);
        });
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
      throw new CalendarRuntimeException("Cannot get all the categories", e);
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
      throw new CalendarRuntimeException("Cannot get category " + categoryId, e);
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
          "Cannot add journal " + journalId + " in category " + categoryId, e);

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
          "Cannot remove journal " + journalId + " from category " + categoryId, e);
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
      throw new CalendarRuntimeException("Cannot get all the categories of journal " + journalId,
          e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setJournalCategories(String journalId, String[] categoryIds) {
    Collection<Category> current = getJournalCategories(journalId);
    final List<String> categoriesToAdd =
        categoryIds != null ? Arrays.asList(categoryIds) : Collections.emptyList();
    // search for element to remove
    current.stream()
        .filter(c -> !categoriesToAdd.contains(c.getId()))
        .forEach(c -> removeJournalCategory(journalId, c.getId()));

    // search for element to add
    List<String> categories = current.stream().map(Category::getId).collect(Collectors.toList());
    categoriesToAdd.stream()
        .filter(c1 -> categories.stream().noneMatch(c2 -> c2.equals(c1)))
        .forEach(c -> addJournalCategory(journalId, c));
  }

  @Override
  public void indexAllTodo() {
    final List<ToDoHeader> todos = new ArrayList<>(JdbcSqlQuery.SPLIT_BATCH);
    final List<String> todoIds = new ArrayList<>(JdbcSqlQuery.SPLIT_BATCH);
    final Consumer<List<String>> indexationProcess = l -> {
      final Map<String, List<Attendee>> attendeeMapping = getToDoAttendees(todoIds);
      for (final ToDoHeader todo : todos) {
        createIndex(todo, todo.getDelegatorId());
        final Collection<Attendee> attendees =
            attendeeMapping.getOrDefault(todo.getId(), emptyList());
        for (Attendee attendee : attendees) {
          createIndex(todo, attendee.getUserId());
        }
      }
    };
    try {
      JdbcSqlQuery.select(ToDoDAO.COLUMNNAMES).from("CalendarToDo").execute(rs -> {
        final ToDoHeader todo = ToDoDAO.getToDoHeaderFromResultSet(rs);
        todos.add(todo);
        todoIds.add(todo.getId());
        if (todoIds.size() == JdbcSqlQuery.SPLIT_BATCH) {
          indexationProcess.accept(todoIds);
          // Cleaning
          todos.clear();
          todoIds.clear();
        }
        return null;
      });

      // Indexing the last to do
      indexationProcess.accept(todoIds);

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
      throw new CalendarRuntimeException("Cannot get holiday dates of user " + userId, re);
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
      throw new CalendarRuntimeException("Cannot get holiday dates of user " + userId, re);
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
      throw new CalendarRuntimeException("Cannot add holiday date", re);
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
      throw new CalendarRuntimeException("Cannot add holiday dates", re);
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
      throw new CalendarRuntimeException("Cannot remove holiday date", re);
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
      throw new CalendarRuntimeException("Cannot remove holiday dates", re);
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
      throw new CalendarRuntimeException("Cannot get holiday date", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Reindex all journal
   */
  @Override
  public void indexAllJournal() {
    final String selectStatement = "select " + JournalDAO.COLUMNNAMES + " from CalendarJournal";
    try (final Connection con = DBUtil.openConnection();
         final Statement prepStmt = con.createStatement();
         final ResultSet rs = prepStmt.executeQuery(selectStatement)) {
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
    }
  }

  private void createIndex(Schedulable detail, String userId) {
    try {
      FullIndexEntry indexEntry;
      if (detail instanceof ToDoHeader) {
        indexEntry = new FullIndexEntry(new IndexEntryKey(IDX_USER_PREFIX + userId + "_todo",
            "todo", detail.getId()));
      } else {
        indexEntry =
            new FullIndexEntry(new IndexEntryKey(IDX_USER_PREFIX + userId + "_agenda", "agenda",
                detail.getId()));
      }
      indexEntry.setTitle(detail.getName());
      indexEntry.setPreview(detail.getDescription());
      indexEntry.setCreationUser(detail.getDelegatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private void removeIndex(Schedulable detail, String userId) {
    try {
      IndexEntryKey indexEntry = getIndexEntry(detail, userId);
      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private IndexEntryKey getIndexEntry(Schedulable detail, String userId) {
    IndexEntryKey indexEntry;
    if (detail instanceof ToDoHeader) {
      indexEntry = new IndexEntryKey(IDX_USER_PREFIX + userId + "_todo", "todo", detail.getId());
    } else {
      indexEntry =
          new IndexEntryKey(IDX_USER_PREFIX + userId + "_agenda", "agenda", detail.getId());
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS, e);
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS, e);
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS, e);
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
      throw new CalendarRuntimeException(CANNOT_GET_JOURNALS, e);
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
    Collection<ToDoHeader> todosToRemove = getToDoHeadersByInstanceId(componentInstanceId);
    for (ToDoHeader todo : todosToRemove) {
      try {
        ToDoDAO.removeToDo(todo.getId());
        AttendeeDAO.removeToDo(todo.getId());
      } catch (Exception e) {
        throw new CalendarRuntimeException("Cannot delete todos", e);
      }
      removeIndex(todo, todo.getDelegatorId());
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
