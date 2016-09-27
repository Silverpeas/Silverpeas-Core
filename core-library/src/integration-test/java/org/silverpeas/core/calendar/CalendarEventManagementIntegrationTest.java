/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.rule.DbSetupRule.TableLine;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.calendar.event.CalendarEventOccurrenceReferenceData
    .fromOccurrenceId;

/**
 * Integration tests on the getting, on the saving, on the deletion and on the update of the events
 * in a given calendar.
 *
 * We first check the getting of an existing event works fine so that we can use afterwards the
 * getting method to get the previously saved event in order to check its persisted properties.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class CalendarEventManagementIntegrationTest extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String AN_ATTRIBUTE_NAME = "location";
  private static final String AN_ATTRIBUTE_VALUE = "L'agence de Grenoble, en Isère (France)";
  private static final String USER_ID = "1";

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventManagementIntegrationTest.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void verifyInitialData() throws Exception {
    // JPA and Basic SQL query must show that it exists no data
    assertThat(getCalendarEventTableLines(), hasSize(5));
  }

  @Test
  public void getExistingCalendarEventByIdWhichIsNotOnAllDay() {
    Optional<CalendarEvent> mayBeEvent = Calendar.getById(CALENDAR_ID).event("ID_E_3");
    assertThat(mayBeEvent.isPresent(), is(true));

    CalendarEvent calendarEvent = mayBeEvent.get();
    assertThat(calendarEvent.getCalendar().getId(), is("ID_1"));
    assertThat(calendarEvent.isOnAllDay(), is(false));
    assertThat(calendarEvent.getStartDateTime(),
        is(Instant.parse("2016-01-08T18:30:00Z").atOffset(ZoneOffset.UTC)));
    assertThat(calendarEvent.getEndDateTime(),
        is(Instant.parse("2016-01-22T13:38:00Z").atOffset(ZoneOffset.UTC)));
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
    assertThat(calendarEvent.getStartDateTime(),
        is(Instant.parse("2016-01-09T00:00:00Z").atOffset(ZoneOffset.UTC)));
    assertThat(calendarEvent.getEndDateTime(),
        is(Instant.parse("2016-01-09T23:59:00Z").atOffset(ZoneOffset.UTC)));
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
    assertThat(occurrence.getStartDateTime(), is(event.getStartDateTime()));
    assertThat(occurrence.getEndDateTime(), is(event.getEndDateTime()));
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
    List<TableLine> beforeDeletion = getCalendarEventTableLines();

    assertThat(beforeDeletion, hasSize(5));
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event("ID_E_5");
    assertThat(mayBeEvent.isPresent(), is(true));
    assertThat(calendar.isEmpty(), is(false));
    assertThat(Calendar.getById("ID_2").isEmpty(), is(false));

    calendar.clear();

    List<TableLine> afterDeletion = getCalendarEventTableLines();
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

    event.delete();
    assertThat(event.isPersisted(), is(false));
  }

  @Test
  public void deleteTheSingleEventOccurrenceDeleteTheEventItself() {
    Calendar calendar = Calendar.getById("ID_2");
    List<CalendarEventOccurrence> occurrences =
        calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = occurrences.get(0);
    CalendarEvent event = occurrence.getCalendarEvent();
    String eventIdBeforeDeletion = event.getId();
    event.delete(fromOccurrenceId(occurrence.getId()));

    assertThat(calendar.event(eventIdBeforeDeletion).isPresent(), is(false));
    assertThat(calendar.in(YearMonth.of(2016, 1)).getEventOccurrences().isEmpty(),
        is(true));
  }

  @Test
  public void deleteAnEventShouldDeleteAllItsAttributesAndAttendees() throws Exception {
    List<TableLine> allAttributes = getAttributesTableLinesByEventId("ID_E_1");
    List<TableLine> allAttendees = getAttendeesTableLines();
    assertThat(allAttributes, hasSize(1));
    assertThat(allAttendees, hasSize(3));

    Calendar calendar = Calendar.getById("ID_3");
    CalendarEvent event = calendar.event("ID_E_1").get();
    event.delete();

    allAttributes = getAttributesTableLinesByEventId("ID_E_1");
    allAttendees = getAttendeesTableLines();
    assertThat(allAttributes, hasSize(0));
    assertThat(allAttendees, hasSize(1));
  }

  @Test
  public void updateAPlannedEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_3").get();
    assertThat(event.isPlanned(), is(true));

    OffsetDateTime eventEndDateTime = event.getEndDateTime();
    event.setPeriod(Period.between(LocalDate.parse("2016-01-12"), eventEndDateTime.toLocalDate()));
    event.update();

    event = calendar.event("ID_E_3").get();
    assertThat(event.getStartDateTime(),
        is(LocalDate.parse("2016-01-12").atStartOfDay().atOffset(ZoneOffset.UTC)));
    assertThat(event.getEndDateTime(), is(eventEndDateTime.withHour(23).withMinute(59)));
  }

  @Test
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
    assertThat(event.isPlanned(), is(false));
  }

  @Test
  public void updateTheSingleOccurrenceOfAPlannedEventModifyTheEvent() {
    Calendar calendar = Calendar.getById("ID_2");
    List<CalendarEventOccurrence> occurrences =
        calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = occurrences.get(0);
    assertThat(occurrence.getStartDateTime(), is(OffsetDateTime.parse("2016-01-05T08:00:00Z")));
    assertThat(occurrence.getEndDateTime(), is(OffsetDateTime.parse("2016-01-21T16:50:00Z")));

    CalendarEvent event = occurrence.getCalendarEvent();
    event.setLastUpdatedBy("1");
    final Period newPeriod =
        Period.between(occurrence.getStartDateTime(), OffsetDateTime.parse("2016-01-05T10:30:00Z"));
    event.update(fromOccurrenceId(occurrence.getId()).withPeriod(newPeriod, ZoneOffset.UTC));

    occurrences = calendar.in(YearMonth.of(2016, 1)).getEventOccurrences();
    assertThat(occurrences.size(), is(1));
    occurrence = occurrences.get(0);
    CalendarEvent updatedEvent = occurrence.getCalendarEvent();
    assertThat(occurrence.getStartDateTime(), is(OffsetDateTime.parse("2016-01-05T08:00:00Z")));
    assertThat(occurrence.getEndDateTime(), is(OffsetDateTime.parse("2016-01-05T10:30:00Z")));
    assertThat(updatedEvent.getStartDateTime(), is(OffsetDateTime.parse("2016-01-05T08:00:00Z")));
    assertThat(updatedEvent.getEndDateTime(), is(OffsetDateTime.parse("2016-01-05T10:30:00Z")));
    assertThat(updatedEvent, is(event));
  }

  private void assertEventProperties(final CalendarEvent actual, final CalendarEvent expected) {
    assertThat(actual.getStartDateTime(), is(expected.getStartDateTime()));
    assertThat(actual.getEndDateTime(), is(expected.getEndDateTime()));
    assertThat(actual.isOnAllDay(), is(expected.isOnAllDay()));
    assertThat(actual.getTitle(), is(expected.getTitle()));
    assertThat(actual.getDescription(), is(expected.getDescription()));
    assertThat(actual.getLocation(), is(expected.getLocation()));
    assertThat(actual.getAttributes().isEmpty(), is(false));
    assertThat(actual.getAttributes(), is(expected.getAttributes()));
    assertThat(actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
    assertThat(actual.getAttendees(), is(expected.getAttendees()));
    assertThat(actual.getCategories(), is(expected.getCategories()));
    assertThat(actual.isRecurrent(), is(false));
  }

}
