/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.calendar.ical4j.ICal4JCalendarEventOccurrenceGenerator;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.*;
import static java.time.Month.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.date.TimeUnit.MONTH;
import static org.silverpeas.core.date.TimeUnit.WEEK;

/**
 * Unit tests on the generation of event occurrences between two given date times.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({JpaPersistOperation.class, JpaUpdateOperation.class})
class CalendarEventOccurrenceGenerationTest {

  private static final String EVENT_TITLE = "an event title";
  private static final String EVENT_DESCRIPTION = "a short event description";
  private static final String ATTR_TEST_ID = "TEST_EVENT_ID";
  private static final ZoneId PARIS_ZONE_ID = ZoneId.of("Europe/Paris");
  private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

  private final CalendarEventOccurrenceGenerator generator =
      new ICal4JCalendarEventOccurrenceGenerator(new ICal4JDateCodec(),
          new ICal4JRecurrenceCodec(new ICal4JDateCodec()));

  @BeforeEach
  public void mockCalendarOccurrenceRepository(
      @TestManagedMock CalendarEventOccurrenceRepository repository,
      @TestManagedMock OrganizationController organizationController) {
    when(organizationController.getUserDetail(anyString())).thenAnswer(a -> {
      String id = a.getArgument(0);
      UserDetail user = new UserDetail();
      user.setId(id);
      return user;
    });
    when(repository.getAll(anyCollection(), any(Period.class))).thenReturn(Collections.emptyList());

    OperationContext.fromUser("0");
  }

  @Test
  void nothingDoneWithAnEmptyListOfEvents() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(Collections.emptyList(), in(Year.of(2016)));
    assertThat(occurrences.isEmpty(), is(true));
  }

  @Test
  void noOccurrencesIfNoEventInTheGivenPeriod() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(calendarEventsForTest(), in(YearMonth.of(2016, 1)));
    assertThat(occurrences.isEmpty(), is(true));
  }

  @Test
  void countEventOccurrencesInYear() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(calendarEventsForTest(), in(Year.of(2016)));
    assertThat(occurrences.isEmpty(), is(false));
    assertThat(occurrences.size(), is(102));

    // compute the occurrence count both per month and per event
    int[] occurrenceCountPerMonth = new int[12];
    int[] occurrenceCountPerEvent = new int[6];
    occurrences.forEach(o -> {
      occurrenceCountPerMonth[o.getStartDate().get(ChronoField.MONTH_OF_YEAR) - 1] += 1;
      occurrenceCountPerEvent[
          Integer.parseInt(o.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get()) - 1] += 1;
    });
    // check now the count of occurrences per month is ok
    assertThat(occurrenceCountPerMonth[JANUARY.ordinal()], is(0));
    assertThat(occurrenceCountPerMonth[FEBRUARY.ordinal()], is(0));
    assertThat(occurrenceCountPerMonth[MARCH.ordinal()], is(4));
    assertThat(occurrenceCountPerMonth[APRIL.ordinal()], is(6));
    assertThat(occurrenceCountPerMonth[MAY.ordinal()], is(10));
    assertThat(occurrenceCountPerMonth[JUNE.ordinal()], is(10));
    assertThat(occurrenceCountPerMonth[JULY.ordinal()], is(4));
    assertThat(occurrenceCountPerMonth[AUGUST.ordinal()], is(5));
    assertThat(occurrenceCountPerMonth[SEPTEMBER.ordinal()], is(17));
    assertThat(occurrenceCountPerMonth[OCTOBER.ordinal()], is(17));
    assertThat(occurrenceCountPerMonth[NOVEMBER.ordinal()], is(17));
    assertThat(occurrenceCountPerMonth[DECEMBER.ordinal()], is(12));

    // check now the count of occurrences per event is ok
    assertThat(occurrenceCountPerEvent[Integer.parseInt("1") - 1], is(1));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("2") - 1], is(1));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("3") - 1], is(42));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("4") - 1], is(1));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("5") - 1], is(45));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("6") - 1], is(12));
  }

  @Test
  void countEventOccurrencesInMay() {
    List<CalendarEventOccurrence> occurrences = generator
        .generateOccurrencesOf(calendarEventsForTest(), in(YearMonth.of(2016, Month.MAY)));
    assertThat(occurrences.isEmpty(), is(false));
    assertThat(occurrences.size(), is(10));
    List<String> allEventIds = occurrences.stream()
        .map(o -> o.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get())
        .collect(Collectors.toList());
    assertThat(allEventIds.stream()
        .distinct()
        .allMatch(id -> id.equals("2") || id.equals("3") || id.equals("6")), is(true));
    assertThat(allEventIds.stream().filter(id -> id.equals("2")).count(), is(1L));
    assertThat(allEventIds.stream().filter(id -> id.equals("3")).count(), is(4L));
    assertThat(allEventIds.stream().filter(id -> id.equals("6")).count(), is(5L));
  }

  @Test
  void countEventOccurrencesInJuly() {
    List<CalendarEventOccurrence> occurrences = generator
        .generateOccurrencesOf(calendarEventsForTest(), in(YearMonth.of(2016, Month.JULY)));
    assertThat(occurrences.isEmpty(), is(false));
    assertThat(occurrences.size(), is(4));
    List<String> allEventIds = occurrences.stream()
        .map(o -> o.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get())
        .collect(Collectors.toList());
    assertThat(allEventIds.stream().distinct().allMatch(id -> id.equals("3") || id.equals("4")),
        is(true));
    assertThat(allEventIds.stream().filter(id -> id.equals("3")).count(), is(3L));
    assertThat(allEventIds.stream().filter(id -> id.equals("4")).count(), is(1L));
  }

  @Test
  void countEventOccurrencesInAGivenPeriod() {
    List<CalendarEventOccurrence> occurrences = generator
        .generateOccurrencesOf(calendarEventsForTest(),
            Period.between(date(2016, 8, 8), date(2016, 8, 14)));
    assertThat(occurrences.isEmpty(), is(false));
    assertThat(occurrences.size(), is(2));
    List<String> allEventIds = occurrences.stream()
        .map(o -> o.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get())
        .collect(Collectors.toList());
    assertThat(allEventIds.stream()
        .distinct()
        .allMatch(id -> id.equals("1") || id.equals("3")), is(true));
    assertThat(allEventIds.stream().filter(id -> id.equals("1")).count(), is(1L));
    assertThat(allEventIds.stream().filter(id -> id.equals("3")).count(), is(1L));
  }

  @Test
  void dateOfEventOccurrencesInJuly() {
    List<CalendarEventOccurrence> occurrences = generator
        .generateOccurrencesOf(calendarEventsForTest(), in(YearMonth.of(2016, Month.JULY)));
    assertThat(occurrences.size(), is(4));
    // first occurrence
    Iterator<CalendarEventOccurrence> iterator = occurrences.iterator();
    CalendarEventOccurrence occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("3"));
    assertThat(occurrence.getStartDate(), is(dateTimeInUTC(2016, 7, 1, 9, 0)));
    assertThat(occurrence.getEndDate(), is(dateTimeInUTC(2016, 7, 1, 9, 15)));
    // second occurrence
    occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("3"));
    assertThat(occurrence.getStartDate(), is(dateTimeInUTC(2016, 7, 8, 9, 0)));
    assertThat(occurrence.getEndDate(), is(dateTimeInUTC(2016, 7, 8, 9, 15)));
    // third occurrence
    occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("4"));
    assertThat(occurrence.getStartDate(), is(date(2016, 7, 11)));
    assertThat(occurrence.getEndDate(), is(date(2016, 7, 22)));
    // fourth occurrence
    occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("3"));
    assertThat(occurrence.getStartDate(), is(dateTimeInUTC(2016, 7, 29, 9, 0)));
    assertThat(occurrence.getEndDate(), is(dateTimeInUTC(2016, 7, 29, 9, 15)));
  }

  @Test
  void nextOccurrenceAboutNonRecurrentOneDayEventShouldWork() {
    CalendarEvent event =
        calendarEventForTest(Period.between(date(2017, 12, 12), date(2017, 12, 12)));
    ZonedDateTime from = ZonedDateTime.parse("2017-12-12T00:00:00+01:00");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());

    from = ZonedDateTime.parse("2017-12-12T00:00:00-01:00");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());

    from = ZonedDateTime.parse("2017-12-11T23:59:59+10:00");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 13)));

    from = ZonedDateTime.parse("2017-12-11T23:59:59Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 13)));

    from = ZonedDateTime.parse("2017-12-12T00:00:00Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutNonRecurrentSeveralDayEventShouldWork() {
    CalendarEvent event =
        calendarEventForTest(Period.between(date(2017, 12, 12), date(2017, 12, 15)));
    ZonedDateTime from = ZonedDateTime.parse("2017-12-12T00:00:00+01:00");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());

    from = ZonedDateTime.parse("2017-12-12T00:00:00-01:00");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());

    from = ZonedDateTime.parse("2017-12-11T23:59:59+12:00");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 15)));

    from = ZonedDateTime.parse("2017-12-11T23:59:59Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 15)));

    from = ZonedDateTime.parse("2017-12-12T00:00:00Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutNonRecurrentHourEventOnOneDayShouldWork() {
    CalendarEvent event =
        calendarEventForTest(Period.between(
            dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 12, 14, 45)));
    ZonedDateTime from = ZonedDateTime.parse("2017-12-12T13:30:00+01:00");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 12, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:29:59Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 12, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:30:00Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutNonRecurrentHugeHourEventOnOneDayShouldWork() {
    CalendarEvent event =
        calendarEventForTest(Period.between(
            dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 15, 14, 45)));
    ZonedDateTime from = ZonedDateTime.parse("2017-12-12T13:30:00+01:00[Europe/Paris]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 15, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:29:59Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 15, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:30:00Z");
    result = generator.generateNextOccurrenceOf(event, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentOneDayEventShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(date(2017, 12, 12), date(2017, 12, 12)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 13)));

    from = ZonedDateTime.parse("2017-12-11T23:59:59Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 13)));

    from = ZonedDateTime.parse("2017-12-12T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 13)));
    assertThat(result.getEndDate(), is(date(2017, 12, 14)));

    from = ZonedDateTime.parse("2017-12-13T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 14)));
    assertThat(result.getEndDate(), is(date(2017, 12, 15)));

    from = ZonedDateTime.parse("2017-12-14T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentOneDayEventWithExceptionShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(date(2017, 12, 12), date(2017, 12, 12)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3)
                .excludeEventOccurrencesStartingAt(date(2017, 12, 12), date(2017, 12, 14)));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 13)));
    assertThat(result.getEndDate(), is(date(2017, 12, 14)));

    from = ZonedDateTime.parse("2017-12-13T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentSeveralDayEventShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(date(2017, 12, 12), date(2017, 12, 15)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 15)));

    from = ZonedDateTime.parse("2017-12-11T23:59:59Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 12)));
    assertThat(result.getEndDate(), is(date(2017, 12, 15)));

    from = ZonedDateTime.parse("2017-12-12T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 13)));
    assertThat(result.getEndDate(), is(date(2017, 12, 16)));

    from = ZonedDateTime.parse("2017-12-13T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 14)));
    assertThat(result.getEndDate(), is(date(2017, 12, 17)));

    from = ZonedDateTime.parse("2017-12-14T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentSeveralDayEventWithExceptionShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(date(2017, 12, 12), date(2017, 12, 15)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3)
                .excludeEventOccurrencesStartingAt(date(2017, 12, 12), date(2017, 12, 14)));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(date(2017, 12, 13)));
    assertThat(result.getEndDate(), is(date(2017, 12, 16)));

    from = ZonedDateTime.parse("2017-12-13T00:00:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentHourEventShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(
            dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 12, 14, 45)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 12, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:29:59Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 12, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 13, 14, 45)));

    from = ZonedDateTime.parse("2017-12-13T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 14, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 14, 14, 45)));

    from = ZonedDateTime.parse("2017-12-14T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentHourEventStartingOnSummerShouldWork() {
    final OffsetDateTime startDateTimeOnParis = dateTimeOnParis(2017, 7, 11, 23, 0);
    final OffsetDateTime endDateTimeOnParis = dateTimeOnParis(2017, 7, 12, 0, 45);
    assertThat(startDateTimeOnParis.withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 7, 11, 21, 0)));
    assertThat(endDateTimeOnParis.withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 7, 11, 22, 45)));
    assertThat(dateTimeOnParis(2017, 12, 11, 23, 0).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 12, 11, 22, 0)));
    assertThat(dateTimeOnParis(2017, 12, 12, 0, 45).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 12, 11, 23, 45)));
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(startDateTimeOnParis, endDateTimeOnParis), PARIS_ZONE_ID)
            .recur(Recurrence
                .every(1, TimeUnit.MONTH)
                .until(10));
    ZonedDateTime from = ZonedDateTime.parse("2017-12-11T21:59:59-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 1, 11, 22, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 1, 11, 23, 45)));

    from = ZonedDateTime.parse("2017-12-11T22:00:00-01:00[Atlantic/Azores]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 1, 11, 22, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 1, 11, 23, 45)));

    from = ZonedDateTime.parse("2017-12-11T22:59:59+00:00[UTC]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 1, 11, 22, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 1, 11, 23, 45)));

    from = ZonedDateTime.parse("2017-12-11T23:00:00+00:00[UTC]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 1, 11, 22, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 1, 11, 23, 45)));

    from = ZonedDateTime.parse("2017-12-11T22:59:59+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 11, 22, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 11, 23, 45)));

    from = ZonedDateTime.parse("2017-12-11T23:00:00+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 1, 11, 22, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 1, 11, 23, 45)));
  }

  @Test
  void nextOccurrenceAboutRecurrentHourEventStartingOnSummerAndNowAboutHourChangingShouldWork() {
    final OffsetDateTime startDateTimeOnParis = dateTimeOnParis(2017, 7, 29, 3, 0);
    final OffsetDateTime endDateTimeOnParis = dateTimeOnParis(2017, 7, 29, 4, 30);
    assertThat(startDateTimeOnParis.withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 7, 29, 1, 0)));
    assertThat(endDateTimeOnParis.withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 7, 29, 2, 30)));
    assertThat(dateTimeOnParis(2017, 10, 28, 23, 59).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 10, 28, 21, 59)));
    assertThat(dateTimeOnParis(2017, 10, 29, 0, 0).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 10, 28, 22, 0)));
    assertThat(dateTimeOnParis(2017, 10, 29, 2, 0).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 10, 29, 0, 0)));
    assertThat(dateTimeOnParis(2017, 10, 29, 2, 59).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 10, 29, 0, 59)));
    assertThat(dateTimeOnParis(2017, 10, 29, 3, 0).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2017, 10, 29, 2, 0)));
    assertThat(dateTimeOnParis(2018, 3, 25, 2, 59).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2018, 3, 25, 1, 59)));
    assertThat(dateTimeOnParis(2018, 3, 25, 3, 0).withOffsetSameInstant(ZoneOffset.UTC), is(
        dateTimeInUTC(2018, 3, 25, 1, 0)));
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(startDateTimeOnParis, endDateTimeOnParis), PARIS_ZONE_ID)
            .recur(Recurrence
                .every(1, TimeUnit.MONTH)
                .until(100));
    ZonedDateTime from = ZonedDateTime.parse("2017-10-29T00:59:59-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 10, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 10, 29, 3, 30)));

    from = ZonedDateTime.parse("2017-10-29T01:00:00-01:00[Atlantic/Azores]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 11, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 11, 29, 3, 30)));

    from = ZonedDateTime.parse("2017-10-29T01:59:59+00:00[UTC]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 10, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 10, 29, 3, 30)));

    from = ZonedDateTime.parse("2017-10-29T02:00:00+00:00[UTC]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 11, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 11, 29, 3, 30)));

    from = ZonedDateTime.parse("2017-10-29T02:59:59+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 10, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 10, 29, 3, 30)));

    from = ZonedDateTime.parse("2017-10-29T03:00:00+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 11, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 11, 29, 3, 30)));

    from = ZonedDateTime.parse("2018-01-29T02:59:59+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 1, 29, 2, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 1, 29, 3, 30)));

    from = ZonedDateTime.parse("2018-01-29T03:00:00+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 3, 29, 1, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 3, 29, 2, 30)));

    from = ZonedDateTime.parse("2018-02-28T02:59:59+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 3, 29, 1, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 3, 29, 2, 30)));

    from = ZonedDateTime.parse("2018-02-28T03:00:00+01:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 3, 29, 1, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 3, 29, 2, 30)));

    from = ZonedDateTime.parse("2018-03-29T02:59:59+02:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 3, 29, 1, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 3, 29, 2, 30)));

    from = ZonedDateTime.parse("2018-03-29T03:00:00+02:00[Europe/Paris]");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2018, 4, 29, 1, 0)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2018, 4, 29, 2, 30)));
  }

  @Test
  void nextOccurrenceAboutRecurrentHourEventWithExceptionShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(
            dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 12, 14, 45)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3)
                .excludeEventOccurrencesStartingAt(dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 14, 13, 30)));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 13, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:29:59Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 13, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 13, 14, 45)));

    from = ZonedDateTime.parse("2017-12-13T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentHugeHourEventShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(
            dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 15, 14, 45)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 15, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:29:59Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 12, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 15, 14, 45)));

    from = ZonedDateTime.parse("2017-12-12T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 16, 14, 45)));

    from = ZonedDateTime.parse("2017-12-13T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 14, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 17, 14, 45)));

    from = ZonedDateTime.parse("2017-12-14T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  @Test
  void nextOccurrenceAboutRecurrentHugeHourEventWithExceptionShouldWork() {
    CalendarEvent recurrentEvent =
        calendarEventForTest(Period.between(
            dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 15, 14, 45)))
            .recur(Recurrence
                .every(1, TimeUnit.DAY)
                .until(3)
                .excludeEventOccurrencesStartingAt(dateTimeInUTC(2017, 12, 12, 13, 30), dateTimeInUTC(2017, 12, 14, 13, 30)));
    ZonedDateTime from = ZonedDateTime.parse("2000-01-01T11:11:11-01:00[Atlantic/Azores]");
    CalendarEventOccurrence result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 16, 14, 45)));

    from = ZonedDateTime.parse("2017-12-13T13:29:59Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, notNullValue());
    assertThat(result.getStartDate(), is(dateTimeInUTC(2017, 12, 13, 13, 30)));
    assertThat(result.getEndDate(), is(dateTimeInUTC(2017, 12, 16, 14, 45)));

    from = ZonedDateTime.parse("2017-12-13T13:30:00Z");
    result = generator.generateNextOccurrenceOf(recurrentEvent, from);
    assertThat(result, nullValue());
  }

  private static List<CalendarEvent> calendarEventsForTest() {
    List<CalendarEvent> events = new ArrayList<>();
    /* event 1 on Thursday 2016-08-11 */
    events.add(CalendarEvent.on(date(2016, 8, 11))
        .withTitle(EVENT_TITLE + " 1")
        .withDescription(EVENT_DESCRIPTION + " 1")
        .withAttribute(ATTR_TEST_ID, "1"));
    /* event 2 at Friday 2016-05-20 15h00 - 15h35 */
    events.add(CalendarEvent.on(
        Period.between(dateTimeInUTC(2016, 5, 20, 15, 0), dateTimeInUTC(2016, 5, 20, 15, 35)))
        .withTitle(EVENT_TITLE + " 2")
        .withDescription(EVENT_DESCRIPTION + " 2")
        .withAttribute(ATTR_TEST_ID, "2"));
    /* event 3 at 09h00 - 09h15 every Fridays from 2016-03-04 excluding
       Friday 2016-07-15 and Friday 2016-07-22 */
    events.add(
        CalendarEvent.on(Period.between(
            dateTimeInUTC(2016, 3, 4, 9, 0), dateTimeInUTC(2016, 3, 4, 9, 15)))
            .withTitle(EVENT_TITLE + " 3")
            .withDescription(EVENT_DESCRIPTION + " 3")
            .withAttribute(ATTR_TEST_ID, "3")
            .recur(Recurrence.every(WEEK)
                .on(FRIDAY)
                .excludeEventOccurrencesStartingAt(date(2016, 7, 15), date(2016, 7, 22))));
    /* event 4 from Monday 2016-07-11 to Friday 2016-07-22 */
    events.add(CalendarEvent.on(Period.between(date(2016, 7, 11), date(2016, 7, 22)))
        .withTitle(EVENT_TITLE + " 4")
        .withDescription(EVENT_DESCRIPTION + " 4")
        .withAttribute(ATTR_TEST_ID, "4"));
    /* event 5 at 10h00 - 11h00 every Monday, Tuesday and Wednesday from Thursday 2016-09-01 to
       Tuesday 2016-12-20 excluding Wednesday 2016-11-30 and Monday 2016-12-12 */
    events.add(
        CalendarEvent.on(Period.between(
            dateTimeInUTC(2016, 9, 1, 10, 0), dateTimeInUTC(2016, 9, 1, 11, 0)))
            .withTitle(EVENT_TITLE + " 5")
            .withDescription(EVENT_DESCRIPTION + " 5")
            .withAttribute(ATTR_TEST_ID, "5")
            .recur(Recurrence.every(WEEK)
                .on(MONDAY, TUESDAY, WEDNESDAY)
                .until(dateTimeInUTC(2016, 12, 20, 10, 0))
                .excludeEventOccurrencesStartingAt(date(2016, 11, 30), date(2016, 12, 12))));
    /* event 6 at 08h00 - 09h00 every month on all Thursdays and on the third Friday
       from Thursday 2016-04-28 to Friday 2016-07-01 */
    events.add(
        CalendarEvent.on(Period.between(
            dateTimeInUTC(2016, 4, 28, 8, 0), dateTimeInUTC(2016, 4, 28, 9, 0)))
            .withTitle(EVENT_TITLE + " 6")
            .withDescription(EVENT_DESCRIPTION + " 6")
            .withAttribute(ATTR_TEST_ID, "6")
            .recur(Recurrence.every(MONTH)
                .on(DayOfWeekOccurrence.all(THURSDAY), DayOfWeekOccurrence.nth(3, FRIDAY))
                .until(date(2016, 6, 30))));

    Calendar calendar = new Calendar();
    calendar.setZoneId(UTC_ZONE_ID);
    for (CalendarEvent event : events) {
      try {
        FieldUtils.writeDeclaredField(event.asCalendarComponent(), "calendar", calendar, true);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return events;
  }

  private CalendarEvent calendarEventForTest(Period period) {
    return calendarEventForTest(period, UTC_ZONE_ID);
  }

  private CalendarEvent calendarEventForTest(Period period, ZoneId calendarZoneId) {
    CalendarEvent event = CalendarEvent
        .on(period)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
    Calendar calendar = new Calendar();
    calendar.setZoneId(calendarZoneId);
    try {
      FieldUtils.writeDeclaredField(event.asCalendarComponent(), "calendar", calendar, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return event;
  }


  private Period in(Year year) {
    return Period.between(year.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC),
        year.atMonth(DECEMBER)
            .atEndOfMonth()
            .plusDays(1)
            .atStartOfDay()
            .minusMinutes(1)
            .atOffset(ZoneOffset.UTC));
  }

  private Period in(YearMonth yearMonth) {
    return Period.between(yearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC),
        yearMonth.atEndOfMonth()
            .plusDays(1)
            .atStartOfDay()
            .minusMinutes(1)
            .atOffset(ZoneOffset.UTC));
  }


  private static LocalDate date(int year, int month, int day) {
    return LocalDate.of(year, month, day);
  }

  private static OffsetDateTime dateTimeInUTC(int year, int month, int day, int hour, int minute) {
    return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
  }

  private static OffsetDateTime dateTimeOnParis(int year, int month, int day, int hour, int minute) {
    return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, PARIS_ZONE_ID).toOffsetDateTime();
  }
}
