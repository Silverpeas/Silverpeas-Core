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
import org.silverpeas.core.calendar.CalendarEvent.CalendarEventModificationResult;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static java.time.DayOfWeek.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.calendar.CalendarEventOccurrenceReference
    .fromOccurrenceId;
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

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

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
    assertThat(calendarEvent.getStartDate(), is(LocalDate.parse("2016-01-09")));
    assertThat(calendarEvent.getEndDate(), is(LocalDate.parse("2016-01-09")));
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
      LocalDate startDate = LocalDate.of(2016, 1, 23).plusWeeks(i + step);
      if (startDate.isEqual(exceptionDate.toLocalDate())) {
        startDate = exceptionDate.toLocalDate().plusWeeks(1);
        step++;
      }
      LocalDate endDate = LocalDate.of(2016, 1, 23).plusWeeks(i + step);

      assertThat(occurrence.getCalendarEvent().getId(), is("ID_E_5"));
      assertThat(occurrence.getStartDate(), is(startDate));
      assertThat(occurrence.getEndDate(), is(endDate));
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
    CalendarEvent occurrenceEvent = occurrences.get(0).getCalendarEvent();
    ModificationResult result = occurrenceEvent.delete(fromOccurrenceId(occurrence.getId()));
    assertThat(result, notNullValue());
    assertThat(result.isUpdatedEvent(), is(true));
    assertThat(result.isCreatedEvent(), is(false));

    Optional<CalendarEvent> mayBeEvent = calendar.event(occurrence.getCalendarEvent().getId());
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    assertThat(event.getRecurrence().getExceptionDates()
        .contains(Period.asOffsetDateTime(occurrence.getStartDate())), is(true));

    occurrences = calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
        .getEventOccurrences();
    assertThat(occurrences.size(), is(4));
    assertThat(occurrences.get(0).getStartDate(),
        is(occurrence.getStartDate().plus(2, ChronoUnit.WEEKS)));
  }

  @Test
  public void deleteFromAnOccurrenceAmongSeveralOneOfARecurrentEventAddAnExceptionDate() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
            .getEventOccurrences();
    assertThat(occurrences.size(), is(5));
    CalendarEventOccurrence occurrence = occurrences.get(2);
    final CalendarEvent occurrenceEvent = occurrence.getCalendarEvent();
    assertThat(occurrenceEvent.getRecurrence().getExceptionDates(), hasSize(2));
    ModificationResult result = occurrenceEvent.deleteFrom(fromOccurrenceId(occurrence.getId()));
    assertThat(result, notNullValue());
    assertThat(result.isUpdatedEvent(), is(true));
    assertThat(result.isCreatedEvent(), is(false));

    Optional<CalendarEvent> mayBeEvent = calendar.event(occurrenceEvent.getId());
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    assertThat(event, is(occurrenceEvent));
    assertThat(event, is(result.getUpdatedEvent()));
    assertThat(event.getRecurrence().getExceptionDates(), hasSize(2));

    occurrences = calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
        .getEventOccurrences();
    assertThat(occurrences.size(), is(2));
    assertThat(result.getUpdatedEvent(), is(occurrenceEvent));
  }

  @Test
  public void deleteAllTheOccurrencesOfARecurrentEventDeleteTheEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 9), LocalDate.of(2016, 3, 5)).getEventOccurrences();
    assertThat(occurrences.size(), greaterThan(4));
    final CalendarEventOccurrence occurrence = occurrences.get(4);
    final CalendarEvent occurrenceEvent = occurrence.getCalendarEvent();
    ModificationResult result = occurrenceEvent.delete();
    assertThat(result, notNullValue());
    assertThat(result.isUpdatedEvent(), is(false));
    assertThat(result.isCreatedEvent(), is(false));

    occurrences =
        calendar.between(LocalDate.of(2016, 1, 9), LocalDate.of(2016, 3, 5)).getEventOccurrences();
    assertThat(occurrences, hasSize(1));
    assertThat(calendar.event("ID_E_5").isPresent(), is(false));
  }

  @Test
  public void deleteAnEventShouldDeleteItsRecurrenceRule() throws Exception {
    List<DbSetupRule.TableLine> allRecurrences = getRecurrenceTableLines();
    List<DbSetupRule.TableLine> allRecurrenceDayOfWeeks = getRecurrenceDayOfWeekTableLines();
    List<DbSetupRule.TableLine> allRecurrenceExceptions = getRecurrenceExceptionTableLines();

    assertThat(allRecurrences.isEmpty(), is(false));
    assertThat(allRecurrenceDayOfWeeks.isEmpty(), is(false));
    assertThat(allRecurrenceExceptions.isEmpty(), is(false));

    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_5").get();
    event.delete();

    allRecurrences = getRecurrenceTableLines();
    allRecurrenceDayOfWeeks = getRecurrenceDayOfWeekTableLines();
    allRecurrenceExceptions = getRecurrenceExceptionTableLines();
    assertThat(allRecurrences.isEmpty(), is(true));
    assertThat(allRecurrenceDayOfWeeks.isEmpty(), is(true));
    assertThat(allRecurrenceExceptions.isEmpty(), is(true));
  }

  @Test
  public void updateRecurrenceOfAnEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event("ID_E_5").get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.getSequence(), is(0l));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, WEEK)));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(8));

    event.asCalendarComponent().setLastUpdatedBy("1");
    event.recur(Recurrence.every(1, DAY).upTo(5));
    event.update();

    event = calendar.event("ID_E_5").get();
    assertThat(event.getSequence(), is(1l));
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
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
    assertThat(occurrence.getSequence(), is(0l));
    assertThat(occurrence.getCalendarEvent().getId(), is("ID_E_5"));
    assertThat(occurrence.getCalendarEvent().isOnAllDay(), is(true));
    assertThat(occurrence.getStartDate(), is(LocalDate.parse("2016-01-23")));
    assertThat(occurrence.getEndDate(), is(LocalDate.parse("2016-01-23")));
    assertThat(occurrence.getCalendarEvent().getRecurrence().getExceptionDates(),
        containsInAnyOrder(OffsetDateTime.parse("2016-01-30T00:00Z"),
            OffsetDateTime.parse("2016-01-16T00:00Z")));

    CalendarEvent previousEvent = occurrence.getCalendarEvent();
    occurrence.setDay(LocalDate.of(2016, 1, 24));
    previousEvent.asCalendarComponent().setLastUpdatedBy("1");
    CalendarEventModificationResult result = previousEvent.update(
        fromOccurrenceId(occurrence.getId()).withPeriod(occurrence.getPeriod()));
    assertThat(result, notNullValue());
    assertThat(result.isUpdatedEvent(), is(true));
    assertThat(result.isCreatedEvent(), is(true));

    occurrences = calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
        .getEventOccurrences();
    CalendarEventOccurrence createdOccurrence = occurrences.get(0);
    assertThat(occurrences.size(), is(5));
    assertThat(result.getUpdatedEvent(), is(occurrence.getCalendarEvent()));
    assertThat(result.getUpdatedEvent().getRecurrence().getExceptionDates(), hasSize(3));
    assertThat(result.getCreatedEvent(), is(createdOccurrence.getCalendarEvent()));
    assertThat(result.getCreatedEvent().isRecurrent(), is(false));
    assertThat(result.getUpdatedEvent(), not(is(result.getCreatedEvent())));
    assertThat(createdOccurrence.getStartDate(), is(LocalDate.parse("2016-01-24")));
    assertThat(createdOccurrence.getEndDate(), is(LocalDate.parse("2016-01-24")));
    assertThat(createdOccurrence.getCalendarEvent(), not(is(occurrence.getCalendarEvent())));
    assertThat(createdOccurrence.getCalendarEvent(), not(is(previousEvent)));
    assertThat(createdOccurrence.getCalendarEvent().isRecurrent(), is(false));
  }

  @Test
  public void updateFromAnOccurrenceOfAnEvent() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
     List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
            .getEventOccurrences();
    assertThat(occurrences.size(), is(5));
    CalendarEventOccurrence occurrence = occurrences.get(2);

    CalendarEvent event = occurrence.getCalendarEvent();
    assertThat(event.getId(), is("ID_E_5"));
    assertThat(event.getTitle(), is("title E"));
    event.setTitle("UPDATED TITLE");
    ModificationResult result = event.updateFrom(
        fromOccurrenceId(occurrence.getId()).withPeriod(occurrence.getPeriod()));
    assertThat(result, notNullValue());
    assertThat(result.isUpdatedEvent(), is(true));
    assertThat(result.isCreatedEvent(), is(true));

    occurrences = calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 27))
        .getEventOccurrences();
    assertThat(occurrences.size(), is(5));
    CalendarEventOccurrence lastOccurrenceOfPreviousEvent = occurrences.get(1);
    CalendarEventOccurrence updatedOccurrence = occurrences.get(2);
    assertThat(lastOccurrenceOfPreviousEvent.getCalendarEvent().getId(),
        not(is(updatedOccurrence.getCalendarEvent().getId())));
    assertThat(lastOccurrenceOfPreviousEvent.getCalendarEvent().getTitle(), is("title E"));
    assertThat(updatedOccurrence.getCalendarEvent().getTitle(), is("UPDATED TITLE"));
    assertThat(result.getUpdatedEvent().getId(),
        is(lastOccurrenceOfPreviousEvent.getCalendarEvent().getId()));
    assertThat(result.getCreatedEvent().getId(), is(updatedOccurrence.getCalendarEvent().getId()));
    assertThat(result.getUpdatedEvent(), not(result.getCreatedEvent()));
    assertThat(lastOccurrenceOfPreviousEvent.getCalendarEvent().isRecurrent(), is(true));
    assertThat(updatedOccurrence.getCalendarEvent().isRecurrent(), is(true));
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
    assertThat(actual.getStartDate(), is(expected.getStartDate()));
    assertThat(actual.getEndDate(), is(expected.getEndDate()));
    assertThat(actual.isOnAllDay(), is(expected.isOnAllDay()));
    assertThat(actual.getTitle(), is(expected.getTitle()));
    assertThat(actual.getDescription(), is(expected.getDescription()));
    assertThat(actual.getLocation(), is(expected.getLocation()));
    assertThat(actual.getAttributes().isEmpty(), is(true));
    assertThat(actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
    assertThat(actual.getAttendees(), is(expected.getAttendees()));
    assertThat(actual.getCategories(), is(expected.getCategories()));
    assertThat(actual.isRecurrent(), is(true));
    assertThat(actual.getRecurrence(), is(expected.getRecurrence()));
  }

  protected List<DbSetupRule.TableLine> getRecurrenceTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from SB_Cal_Recurrence"));
  }

  protected List<DbSetupRule.TableLine> getRecurrenceDayOfWeekTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from SB_Cal_Recurrence_DayOfWeek"));
  }

  protected List<DbSetupRule.TableLine> getRecurrenceExceptionTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from SB_Cal_Recurrence_Exception"));
  }
}
