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

import org.apache.ecs.html.U;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.test.CalendarWarBuilder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static java.time.DayOfWeek.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.calendar.Recurrence.NO_RECURRENCE_COUNT;
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
    Optional<CalendarEvent> mayBeEvent = Calendar.getById(CALENDAR_ID).getEvents().get("ID_E_5");
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
  public void saveADailyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(2, DAY))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAWeeklyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(3, WEEK))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAMonthlyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(4, MONTH))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAYearlyEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(1, YEAR))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveARecurringEventWithExceptionDates() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(1, WEEK)
            .excludeEventOccurrencesStartingAt(today.plusWeeks(2), today.plusWeeks(5)))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveARecurringEventWithExceptionDateTimes() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = aTimelyEvent()
        .recur(Recurrence.every(1, WEEK)
            .excludeEventOccurrencesStartingAt(now.plusWeeks(2), now.plusWeeks(5)))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAWeeklyEventOnFirstSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = aTimelyEvent()
        .recur(Recurrence.every(2, WEEK).on(MONDAY, FRIDAY))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAMonthlyEventOnAllSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = aTimelyEvent()
        .recur(Recurrence.every(1, MONTH).on(MONDAY, FRIDAY))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveAMonthlyEventOnSomeSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = aTimelyEvent()
        .recur(Recurrence.every(1, MONTH)
            .on(DayOfWeekOccurrence.nth(1, MONDAY), DayOfWeekOccurrence.nth(3, FRIDAY)))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveARecurringEventEndingAtGivenDate() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(3, WEEK).upTo(today.plusWeeks(12)))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void createARecurringEventEndingAtGivenDateTime() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(2, WEEK).upTo(now.plusWeeks(8)))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void createARecurringEventEndingAfterGivenOccurrencesCount() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent expectedEvent = anAllDayEvent()
        .recur(Recurrence.every(1, WEEK).upTo(10))
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
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
