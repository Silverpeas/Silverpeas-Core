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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.util.SQLRequester.ResultLine;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the getting, on the saving, on the deletion and on the update of the events
 * in a given calendar.
 *
 * We first check the getting of an existing event works fine so that we can use afterwards the
 * getting method to get the previously saved event in order to check its persisted properties.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class CalendarEventManagementIT extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String AN_ATTRIBUTE_NAME = "location";
  private static final String AN_ATTRIBUTE_VALUE = "L'agence de Grenoble, en Isère (France)";
  private static final String USER_ID = "1";

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventManagementIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void verifyInitialData() throws Exception {
    // JPA and Basic SQL query must show that it exists no data
    assertThat(getCalendarEventTableLines(), hasSize(6));
    OperationContext.fromUser(USER_ID);
  }

  @Test
  public void getCalendarWithoutEvent() {
    Calendar calendar = Calendar.getById("ID_CAL_WITHOUT_EVENT");
    List<CalendarEventOccurrence> occurrences =
        calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences, empty());
  }

  /**
   * We test we get well all the events ordered by the component instance, then by the calendar,
   * and finally by their start date.
   */
  @Test
  public void getAllEvents() {
    try(Stream<CalendarEvent> events = Calendar.getEvents().stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(6));

      String previousComponentInstanceId = "";
      String previousCalendarId = "";
      OffsetDateTime previousStartDate = OffsetDateTime.of(1000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      for (CalendarEvent event : allEvents) {
        if (event.getCalendar().getComponentInstanceId().equals(previousComponentInstanceId)) {
          if (event.getCalendar().getId().equals(previousCalendarId)) {
              if (event.isOnAllDay()) {
                assertThat(LocalDate.from(event.getStartDate()),
                    greaterThanOrEqualTo(previousStartDate.toLocalDate()));
                previousStartDate =
                    LocalDate.from(event.getStartDate()).atStartOfDay().atOffset(ZoneOffset.UTC);
              } else {
                assertThat(OffsetDateTime.from(event.getStartDate()),
                    greaterThanOrEqualTo(previousStartDate));
                previousStartDate = OffsetDateTime.from(event.getStartDate());
              }
            } else {
            assertThat(event.getCalendar().getId(), greaterThan(previousCalendarId));
            previousCalendarId = event.getCalendar().getId();
            previousStartDate = OffsetDateTime.of(1000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
          }
        } else {
          assertThat(event.getCalendar().getComponentInstanceId(),
              greaterThan(previousComponentInstanceId));
          previousComponentInstanceId = event.getCalendar().getComponentInstanceId();
          previousCalendarId = "";
        }
      }
    }
  }

  @Test
  public void getAllEventsLinkedToUser1() {
    try (Stream<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onParticipants(User.getById(USER_ID))).stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(2));
    }
  }

  @Test
  public void getAllEventsLinkedToUser0() {
    try (Stream<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onParticipants(User.getById("0"))).stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(0));
    }
  }

  @Test
  public void getAllEventsLinkedToUser0AndUser1() {
    try (Stream<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onParticipants(User.getById("0"), User.getById(USER_ID))).stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(2));
    }
  }

  @Test
  public void getAllEventsLinkedToCalendar1() {
    try (Stream<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(Calendar.getById(CALENDAR_ID))).stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(3));
    }
  }

  @Test
  public void getAllEventsLinkedToCalendar1AndCalendar2() {
    try (Stream<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(Calendar.getById(CALENDAR_ID), Calendar.getById("ID_2")))
        .stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(4));
    }
  }

  @Test
  public void getAllEventsLinkedToCalendar1AndCalendar2AndUser1() {
    try (Stream<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(Calendar.getById(CALENDAR_ID), Calendar.getById("ID_2")))
        .filter(f -> f.onParticipants(User.getById(USER_ID)))
        .stream()) {
      List<CalendarEvent> allEvents = events.collect(Collectors.toList());
      assertThat(allEvents, hasSize(1));
    }
  }

  @Test
  public void getExistingCalendarEventByIdWhichIsNotOnAllDay() {
    Optional<CalendarEvent> mayBeEvent = Calendar.getById(CALENDAR_ID).event("ID_E_3");
    assertThat(mayBeEvent.isPresent(), is(true));

    CalendarEvent calendarEvent = mayBeEvent.get();
    assertThat(calendarEvent.getCalendar().getId(), is("ID_1"));
    assertThat(calendarEvent.isOnAllDay(), is(false));
    assertThat(calendarEvent.getStartDate(), is(OffsetDateTime.parse("2016-01-08T18:30:00Z")));
    assertThat(calendarEvent.getEndDate(), is(OffsetDateTime.parse("2016-01-22T13:38:00Z")));
    assertThat(calendarEvent.getTitle(), is("title C"));
    assertThat(calendarEvent.getDescription(), is("description C"));
    assertThat(calendarEvent.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(calendarEvent.getPriority(), is(Priority.HIGH));
    assertThat(calendarEvent.getAttributes().get("location").isPresent(), is(true));
    assertThat(calendarEvent.getAttributes().get("location").get(), is("location C"));
  }

  @Test
  public void getExistingCalendarEventByIdWhichIsOnAllDay() {
    Optional<CalendarEvent> mayBeEvent = Calendar.getById(CALENDAR_ID).event("ID_E_5");
    assertThat(mayBeEvent.isPresent(), is(true));

    CalendarEvent calendarEvent = mayBeEvent.get();
    assertThat(calendarEvent.getCalendar().getId(), is("ID_1"));
    assertThat(calendarEvent.isOnAllDay(), is(true));
    assertThat(calendarEvent.getStartDate(), is(LocalDate.parse("2016-01-09")));
    assertThat(calendarEvent.getEndDate(), is(LocalDate.parse("2016-01-10")));
    assertThat(calendarEvent.getTitle(), is("title E"));
    assertThat(calendarEvent.getDescription(), is("description E"));
    assertThat(calendarEvent.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(calendarEvent.getPriority(), is(Priority.HIGH));
    assertThat(calendarEvent.getAttributes().get("location").isPresent(), is(false));
  }

  @Test
  public void getNoOccurrencesFromNoEventsPlannedInAGivenPeriod() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences = calendar.in(Year.of(2000)).getEventOccurrences();
    assertThat(occurrences.isEmpty(), is(true));
  }

  @Test
  public void getTheSingleOccurrenceOfAPlannedEvent() {
    Calendar calendar = Calendar.getById("ID_2");
    List<CalendarEventOccurrence> occurrences =
        calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = occurrences.get(0);
    CalendarEvent event = occurrence.getCalendarEvent();
    assertThat(event.getId(), is("ID_E_2"));
    assertThat(occurrence.getStartDate(), is(event.getStartDate()));
    assertThat(occurrence.getEndDate(), is(event.getEndDate()));
  }

  @Test
  public void planANewEventOnAllDay() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    LocalDate today = LocalDate.now();
    CalendarEvent expectedEvent = CalendarEvent.on(today)
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .inLocation("Salle Chamrousse")
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE)
        .withCategories("Professional", "Meeting")
        .planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void planANewEventOnSeveralDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    LocalDate today = LocalDate.now();
    LocalDate dayAfterTomorrow = today.plusDays(2);
    CalendarEvent expectedEvent = CalendarEvent.on(Period.between(today, dayAfterTomorrow))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void planANewEventAtAGivenDateTime() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime inThreeHours = now.plusHours(3);
    CalendarEvent expectedEvent = CalendarEvent.on(Period.between(now, inThreeHours))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE)
        .planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void planningAnAlreadyPlannedEventDoesNothing() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_3").get();
    assertThat(event.isPlanned(), is(true));

    event = event.planOn(Calendar.getById("ID_2"));
    assertThat(event.isPlanned(), is(true));
    assertThat(event.getCalendar(), is(calendar));
  }

  @Test
  public void deleteAnExistingEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event("ID_E_5");
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    event.delete();
    assertThat(event.isPersisted(), is(false));

    assertThat(calendar.event("ID_E_5").isPresent(), is(false));
  }

  @Test
  public void sameAsPreviousButIntoParentTransaction() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Transaction.performInOne(() ->  {
      Optional<CalendarEvent> mayBeEvent = calendar.event("ID_E_5");
      assertThat(mayBeEvent.isPresent(), is(true));
      CalendarEvent event = mayBeEvent.get();
      event.delete();
      assertThat(event.isPersisted(), is(false));
      return null;
    });
    assertThat(calendar.event("ID_E_5").isPresent(), is(false));
  }

  @Test
  public void deleteAllExistingEventOfACalendar() throws Exception {
    List<ResultLine> beforeDeletion = getCalendarEventTableLines();

    assertThat(beforeDeletion, hasSize(6));
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event("ID_E_5");
    assertThat(mayBeEvent.isPresent(), is(true));
    assertThat(calendar.isEmpty(), is(false));
    assertThat(Calendar.getById("ID_2").isEmpty(), is(false));

    calendar.clear();

    List<ResultLine> afterDeletion = getCalendarEventTableLines();
    assertThat(afterDeletion, hasSize(3));
    assertThat(calendar.event("ID_E_5").isPresent(), is(false));
    assertThat(calendar.isEmpty(), is(true));
    assertThat(Calendar.getById("ID_2").isEmpty(), is(false));
  }

  @Test
  public void deleteANonPlannedEventDoesNothing() {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime inThreeHours = now.plusHours(3);
    CalendarEvent event = CalendarEvent.on(Period.between(now, inThreeHours))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE);
    assertThat(event.isPersisted(), is(false));

    OperationResult result = event.delete();

    assertEventIsDeleted(result);
    assertThat(event.isPersisted(), is(false));
  }

  @Test
  public void deleteTheSingleEventOccurrenceDeleteTheEventItself() throws Exception {
    Calendar calendar = Calendar.getById("ID_2");
    List<CalendarEventOccurrence> occurrences =
        calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));

    CalendarEventOccurrence occurrence = occurrences.get(0);
    CalendarEvent event = occurrence.getCalendarEvent();
    String eventIdBeforeDeletion = event.getId();

    OperationResult result = event.deleteOnly(occurrence);

    assertEventIsDeleted(result);
    assertThat(calendar.event(eventIdBeforeDeletion).isPresent(), is(false));
    assertThat(calendar.in(YearMonth.of(2016, 1)).getEventOccurrences().isEmpty(),
        is(true));

    ResultLine component =
        getCalendarComponentTableLineById(event.asCalendarComponent().getId());
    assertThat(component, nullValue());
  }

  @Test
  public void deleteAnEventShouldDeleteAllItsAttributesAndAttendees() throws Exception {
    List<ResultLine> allAttributes = getAttributesTableLinesByEventId("ID_C_1");
    List<ResultLine> allAttendees = getAttendeesTableLines();
    assertThat(allAttributes, hasSize(1));
    assertThat(allAttendees, hasSize(3));

    Calendar calendar = Calendar.getById("ID_3");
    CalendarEvent event = calendar.event("ID_E_1").get();

    OperationResult result = event.delete();

    assertEventIsDeleted(result);
    allAttributes = getAttributesTableLinesByEventId("ID_C_1");
    allAttendees = getAttendeesTableLines();
    assertThat(allAttributes, hasSize(0));
    assertThat(allAttendees, hasSize(1));
  }

  @Test
  public void updateTheDateOfAPlannedEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_3").get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.isPlanned(), is(true));
    assertThat(event.isOnAllDay(), is(false));
    assertThat(event.getSequence(), is(0L));

    LocalDate eventEndDate = ((OffsetDateTime) event.getEndDate()).toLocalDate();
    event.setPeriod(Period.between(LocalDate.parse("2016-01-12"), eventEndDate));
    OperationResult result = event.update();

    assertEventIsOnlyUpdated(result);
    event = calendar.event("ID_E_3").get();
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getSequence(), is(1L));
    assertThat(event.isOnAllDay(), is(true));
    assertThat(event.getStartDate(), is(LocalDate.parse("2016-01-12")));
    assertThat(event.getEndDate(), is(eventEndDate));
  }

  @Test
  public void updateTheCalendarOfAPlannedEvent() {
    final String testedEventId = "ID_E_3";
    final Temporal startDate = OffsetDateTime.parse("2016-01-08T18:30:00Z");
    final Temporal endDate = OffsetDateTime.parse("2016-01-22T13:38:00Z");
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(testedEventId).get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.isPlanned(), is(true));
    assertThat(event.isOnAllDay(), is(false));
    assertThat(event.getSequence(), is(0L));
    assertThat(event.getStartDate(), is(startDate));
    assertThat(event.getEndDate(), is(endDate));

    Calendar targetCalendar = Calendar.getById("ID_3");
    event.setCalendar(targetCalendar);
    OperationResult result = event.update();

    assertEventIsOnlyUpdated(result);
    event = CalendarEvent.getById(testedEventId);
    assertThat(targetCalendar.event(testedEventId).get(), is(event));
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getCalendar(), is(targetCalendar));
    assertThat(event.isPlanned(), is(true));
    assertThat(event.isOnAllDay(), is(false));
    assertThat(event.getSequence(), is(1L));
    assertThat(event.getStartDate(), is(startDate));
    assertThat(event.getEndDate(), is(endDate));
  }

  @Test
  public void updateTheCalendarOfARecurrentPlannedEvent() {
    OperationContext.fromUser("2");
    final String testedEventId = "ID_E_6";
    final Temporal startDate = OffsetDateTime.parse("2016-08-01T15:30:00Z");
    final Temporal endDate = OffsetDateTime.parse("2016-08-01T16:45:00Z");
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(testedEventId).get();
    Date lastUpdateDate = event.getLastUpdateDate();
    User lastUpdater = event.getLastUpdater();
    assertThat(event.isPlanned(), is(true));
    assertThat(event.isOnAllDay(), is(false));
    assertThat(event.getSequence(), is(0L));
    assertThat(event.getStartDate(), is(startDate));
    assertThat(event.getEndDate(), is(endDate));
    List<CalendarEventOccurrence> occurrences = event.getPersistedOccurrences();
    assertThat(occurrences, hasSize(1));
    CalendarEventOccurrence occurrence = occurrences.get(0);
    assertThat(occurrence.getSequence(), is(0L));
    assertThat(occurrence.getCalendarEvent().getSequence(), is(0L));
    final Recurrence recurrence = occurrence.getCalendarEvent().getRecurrence();
    assertThat(recurrence, notNullValue());
    assertThat(occurrence.getStartDate(), is(OffsetDateTime.parse("2016-08-02T15:30:00Z")));
    assertThat(occurrence.getEndDate(), is(OffsetDateTime.parse("2016-08-02T16:45:00Z")));

    Calendar targetCalendar = Calendar.getById("ID_3");
    event.setCalendar(targetCalendar);
    OperationResult result = event.update();

    assertEventIsOnlyUpdated(result);
    event = CalendarEvent.getById(testedEventId);
    assertThat(targetCalendar.event(testedEventId).get(), is(event));
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getLastUpdater(), not(is(lastUpdater)));
    assertThat(event.getCalendar(), is(targetCalendar));
    assertThat(event.isPlanned(), is(true));
    assertThat(event.isOnAllDay(), is(false));
    assertThat(event.getSequence(), is(0L));
    assertThat(event.getStartDate(), is(startDate));
    assertThat(event.getEndDate(), is(endDate));
    occurrences = event.getPersistedOccurrences();
    assertThat(occurrences, hasSize(1));
    occurrence = occurrences.get(0);
    assertThat(occurrence.asCalendarComponent().getCalendar(), is(targetCalendar));
    assertThat(occurrence.getSequence(), is(0L));
    assertThat(occurrence.getCalendarEvent(), is(event));
    assertThat(occurrence.getCalendarEvent().getSequence(), is(0L));
    assertThat(occurrence.getCalendarEvent().getRecurrence(), is(recurrence));
    assertThat(occurrence.getStartDate(), is(OffsetDateTime.parse("2016-08-02T15:30:00Z")));
    assertThat(occurrence.getEndDate(), is(OffsetDateTime.parse("2016-08-02T16:45:00Z")));
    assertThat(occurrence.getLastUpdateDate(), is(event.getLastUpdateDate()));
    assertThat(occurrence.getLastModifier(), is(event.getLastUpdater()));
  }

  @Test
  public void updateTheTitleOfAPlannedEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_3").get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.isPlanned(), is(true));
    assertThat(event.getSequence(), is(0L));

    final String title = "An updated title";
    event.setTitle(title);
    OperationResult result = event.update();

    assertEventIsOnlyUpdated(result);
    event = calendar.event("ID_E_3").get();
    assertThat(event.getSequence(), is(1L));
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getTitle(), is(title));
  }

  @Test
  public void updateTheCategoryOfAPlannedEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_3").get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.isPlanned(), is(true));
    assertThat(event.getSequence(), is(0L));

    final String category = "Personal";
    event.getCategories().add(category);
    OperationResult result = event.update();

    assertEventIsOnlyUpdated(result);
    event = calendar.event("ID_E_3").get();
    assertThat(event.getSequence(), is(1L));
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getCategories().contains(category), is(true));
  }

  @Test
  public void updateTheVisibilityOfAPlannedEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_3").get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.getSequence(), is(0L));
    assertThat(event.isPlanned(), is(true));
    assertThat(event.getVisibilityLevel(), not(VisibilityLevel.CONFIDENTIAL));

    event.withVisibilityLevel(VisibilityLevel.CONFIDENTIAL);
    OperationResult result = event.update();

    assertEventIsOnlyUpdated(result);
    event = calendar.event("ID_E_3").get();
    assertThat(event.getSequence(), is(1L));
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getVisibilityLevel(), is(VisibilityLevel.CONFIDENTIAL));
  }

  @Test(expected = IllegalStateException.class)
  public void updateANonPlannedEventDoesNothing() {
    LocalDate today = LocalDate.now();
    LocalDate dayAfterTomorrow = today.plusDays(2);
    CalendarEvent event = CalendarEvent.on(Period.between(today, dayAfterTomorrow))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE);
    assertThat(event.isPlanned(), is(false));

    event.update();
  }

  @Test
  public void updateTheSingleOccurrenceOfAPlannedEventModifyTheEvent() {
    Calendar calendar = Calendar.getById("ID_2");
    List<CalendarEventOccurrence> occurrences =
        calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = occurrences.get(0);
    assertThat(occurrence.getSequence(), is(0L));
    assertThat(occurrence.getCalendarEvent().getSequence(), is(0L));
    assertThat(occurrence.getCalendarEvent().getRecurrence(), nullValue());
    assertThat(occurrence.getStartDate(), is(OffsetDateTime.parse("2016-01-05T08:00:00Z")));
    assertThat(occurrence.getEndDate(), is(OffsetDateTime.parse("2016-01-21T16:50:00Z")));

    CalendarEvent event = occurrence.getCalendarEvent();
    final Period newPeriod =
        Period.between(occurrence.getStartDate(), OffsetDateTime.parse("2016-01-05T10:30:00Z"));
    occurrence.setPeriod(newPeriod);
    OperationResult result = event.updateOnly(occurrence);

    assertEventIsOnlyUpdated(result);
    occurrences = calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));
    occurrence = occurrences.get(0);
    assertThat(occurrence.getSequence(), is(1L));

    CalendarEvent updatedEvent = occurrence.getCalendarEvent();
    assertThat(updatedEvent.getSequence(), is(1L));
    assertThat(updatedEvent.getStartDate(), is(OffsetDateTime.parse("2016-01-05T08:00:00Z")));
    assertThat(updatedEvent.getEndDate(), is(OffsetDateTime.parse("2016-01-05T10:30:00Z")));
    assertThat(updatedEvent, is(event));
  }
  
}
