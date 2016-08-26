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
 * FLOSS exception. You should have received a copy of the text describing
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
import org.silverpeas.core.test.CalendarWarBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static java.time.DayOfWeek.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.silverpeas.core.date.TimeUnit.*;

/**
 * Integration tests on the getting, on the saving, on the deletion and on the update of the
 * recurrent events in a given calendar.
 * <p>
 * We first check the getting of an existing recurrent event works fine so that we can use
 * afterwards the getting method to get the previously saved event in order to check its persisted
 * properties.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class RecurrentCalendarEventManagementIntegrationTest extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String USER_ID = "1";

  private LocalDate today = LocalDate.now();
  private OffsetDateTime now = OffsetDateTime.now();

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(
        RecurrentCalendarEventManagementIntegrationTest.class)
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
  public void getExistingRecurrentCalendarEventById() {
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
    assertThat(calendarEvent.getAttributes().isEmpty(), is(true));
    assertThat(calendarEvent.isRecurrent(), is(true));
    assertThat(calendarEvent.getRecurrence(), is(Recurrence.every(1, WEEK)
        .upTo(8)
        .on(SATURDAY)
        .excludeEventOccurrencesStartingAt(LocalDate.parse("2016-01-16"),
            LocalDate.parse("2016-01-30"))));
  }

  @Test
  public void getOccurrencesFromARecurrentEventInAGivenPeriod() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
            .getEventOccurrences();
    assertThat(occurrences.size(), is(5));

    final OffsetDateTime exceptionDate =
        LocalDate.of(2016, 1, 30).atStartOfDay().atOffset(ZoneOffset.UTC);
    int step = 0;
    for (int i = 0; i < occurrences.size(); i++) {
      CalendarEventOccurrence occurrence = occurrences.get(i);
      OffsetDateTime startDateTime =
          LocalDate.of(2016, 1, 23).atStartOfDay().atOffset(ZoneOffset.UTC).plusWeeks(i + step);
      if (startDateTime.isEqual(exceptionDate)) {
        startDateTime = exceptionDate.plusWeeks(1);
        step++;
      }
      OffsetDateTime endDateTime =
          LocalDate.of(2016, 1, 23).atTime(23, 59).atOffset(ZoneOffset.UTC).plusWeeks(i + step);

      assertThat(occurrence.getCalendarEvent().getId(), is("ID_E_5"));
      assertThat(occurrence.getStartDateTime(), is(startDateTime));
      assertThat(occurrence.getEndDateTime(), is(endDateTime));
    }
  }

  @Test
  public void saveADailyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent().recur(Recurrence.every(2, DAY)).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAWeeklyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent().recur(Recurrence.every(3, WEEK)).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAMonthlyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent =
        anAllDayEvent().recur(Recurrence.every(4, MONTH)).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAYearlyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent().recur(Recurrence.every(1, YEAR)).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveARecurringEventWithExceptionDates() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(1, WEEK)
            .excludeEventOccurrencesStartingAt(today.plusWeeks(2), today.plusWeeks(5)))
        .planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveARecurringEventWithExceptionDateTimes() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = aTimelyEvent()
        .recur(Recurrence.every(1, WEEK)
            .excludeEventOccurrencesStartingAt(now.plusWeeks(2), now.plusWeeks(5)))
        .planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAWeeklyEventOnFirstSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent =
        aTimelyEvent().recur(Recurrence.every(2, WEEK).on(MONDAY, FRIDAY)).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAMonthlyEventOnAllSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent =
        aTimelyEvent().recur(Recurrence.every(1, MONTH).on(MONDAY, FRIDAY)).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAMonthlyEventOnSomeSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = aTimelyEvent()
        .recur(Recurrence.every(1, MONTH)
            .on(DayOfWeekOccurrence.nth(1, MONDAY), DayOfWeekOccurrence.nth(3, FRIDAY)))
        .planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveARecurringEventEndingAtGivenDate() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent =
        anAllDayEvent().recur(Recurrence.every(3, WEEK).upTo(today.plusWeeks(12))).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void createARecurringEventEndingAtGivenDateTime() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent =
        anAllDayEvent().recur(Recurrence.every(2, WEEK).upTo(now.plusWeeks(8))).planOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.event(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void deleteAnOccurrenceAmongSeveralOneOfARecurrentEventAddAnExceptionDate() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
            .getEventOccurrences();
    assertThat(occurrences.size(), is(5));
    CalendarEventOccurrence occurrence = occurrences.get(0);
    occurrence.delete();

    Optional<CalendarEvent> mayBeEvent = calendar.event(occurrence.getCalendarEvent().getId());
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    assertThat(event.getRecurrence().getExceptionDates().contains(occurrence.getStartDateTime()),
        is(true));

    occurrences = calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
        .getEventOccurrences();
    assertThat(occurrences.size(), is(4));
    assertThat(occurrences.get(0).getStartDateTime(),
        is(occurrence.getStartDateTime().plusWeeks(2)));
  }

  @Test
  public void deleteAllTheOccurrencesOfARecurrentEventDeleteTheEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 9), LocalDate.of(2016, 3, 5)).getEventOccurrences();
    assertThat(occurrences.isEmpty(), is(false));
    occurrences.forEach(CalendarEventOccurrence::delete);

    occurrences =
        calendar.between(LocalDate.of(2016, 1, 9), LocalDate.of(2016, 3, 5)).getEventOccurrences();
    assertThat(occurrences.isEmpty(), is(true));
    assertThat(calendar.event("ID_E_5").isPresent(), is(false));
  }

  @Test
  public void updateRecurrenceOfAnEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_5").get();
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, WEEK)));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(8));

    event.setLastUpdatedBy("1");
    event.recur(Recurrence.every(1, DAY).upTo(5));
    event.update();

    event = calendar.event("ID_E_5").get();
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, DAY)));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(5));
  }

  @Test
  public void updateOnlyOneOccurrenceOfAnEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
            .getEventOccurrences();
    CalendarEventOccurrence occurrence = occurrences.get(0);
    assertThat(occurrences.size(), is(5));
    assertThat(occurrence.getStartDateTime(), is(OffsetDateTime.parse("2016-01-23T00:00Z")));
    assertThat(occurrence.getEndDateTime(), is(OffsetDateTime.parse("2016-01-23T23:59Z")));

    CalendarEvent previousEvent = occurrence.getCalendarEvent();
    occurrence.setDay(LocalDate.of(2016, 1, 24));
    occurrence.getCalendarEvent().setLastUpdatedBy("1");
    occurrence.update();

    occurrences = calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
        .getEventOccurrences();
    CalendarEventOccurrence updatedOccurrence = occurrences.get(0);
    assertThat(occurrences.size(), is(5));
    assertThat(updatedOccurrence.getStartDateTime(), is(OffsetDateTime.parse("2016-01-24T00:00Z")));
    assertThat(updatedOccurrence.getEndDateTime(), is(OffsetDateTime.parse("2016-01-24T23:59Z")));
    assertThat(updatedOccurrence.getCalendarEvent(), is(occurrence.getCalendarEvent()));
    assertThat(updatedOccurrence.getCalendarEvent(), not(previousEvent));
    assertThat(updatedOccurrence.getCalendarEvent().isRecurrent(), is(false));
  }

  private CalendarEvent anAllDayEvent() {
    return CalendarEvent.on(today)
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
  }

  private CalendarEvent aTimelyEvent() {
    return CalendarEvent.on(Period.between(now, now.plusHours(2)))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
  }

  private void assertEventProperties(final CalendarEvent actual, final CalendarEvent expected) {
    assertThat(actual.getStartDateTime(), is(expected.getStartDateTime()));
    assertThat(actual.getEndDateTime(), is(expected.getEndDateTime()));
    assertThat(actual.isOnAllDay(), is(expected.isOnAllDay()));
    assertThat(actual.getTitle(), is(expected.getTitle()));
    assertThat(actual.getDescription(), is(expected.getDescription()));
    assertThat(actual.getAttributes().isEmpty(), is(true));
    assertThat(actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
    assertThat(actual.getAttendees(), is(expected.getAttendees()));
    assertThat(actual.getCategories(), is(expected.getCategories()));
    assertThat(actual.isRecurrent(), is(true));
    assertThat(actual.getRecurrence(), is(expected.getRecurrence()));
  }

}
