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

import org.junit.Test;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.event.CalendarEventOccurrenceGenerator;
import org.silverpeas.core.calendar.event.ICal4JCalendarEventOccurrenceGenerator;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.*;
import static java.time.Month.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.date.TimeUnit.MONTH;
import static org.silverpeas.core.date.TimeUnit.WEEK;

/**
 * Unit tests on the generation of event occurrences between two given date times.
 * @author mmoquillon
 */
public class CalendarEventOccurrenceGenerationTest {

  private static final String EVENT_TITLE = "an event title";
  private static final String EVENT_DESCRIPTION = "a short event description";
  private static final String ATTR_TEST_ID = "TEST_EVENT_ID";
  private static final List<CalendarEvent> eventsForTest = getCalendarEventsForTest();

  private CalendarEventOccurrenceGenerator generator = new ICal4JCalendarEventOccurrenceGenerator();

  @Test
  public void nothingDoneWithAnEmptyListOfEvents() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(Collections.emptyList(), in(Year.of(2016)));
    assertThat(occurrences.isEmpty(), is(true));
  }

  @Test
  public void noOccurrencesIfNoEventInTheGivenPeriod() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(eventsForTest, in(YearMonth.of(2016, 1)));
    assertThat(occurrences.isEmpty(), is(true));
  }

  @Test
  public void countEventOccurrencesInYear() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(eventsForTest, in(Year.of(2016)));
    assertThat(occurrences.isEmpty(), is(false));
    assertThat(occurrences.size(), is(103));

    // compute the occurrence count both per month and per event
    int[] occurrenceCountPerMonth = new int[12];
    int[] occurrenceCountPerEvent = new int[6];
    occurrences.stream().forEach(o -> {
      occurrenceCountPerMonth[o.getStartDateTime().getMonth().ordinal()] += 1;
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
    assertThat(occurrenceCountPerMonth[SEPTEMBER.ordinal()], is(18));
    assertThat(occurrenceCountPerMonth[OCTOBER.ordinal()], is(17));
    assertThat(occurrenceCountPerMonth[NOVEMBER.ordinal()], is(17));
    assertThat(occurrenceCountPerMonth[DECEMBER.ordinal()], is(12));

    // check now the count of occurrences per event is ok
    assertThat(occurrenceCountPerEvent[Integer.parseInt("1") - 1], is(1));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("2") - 1], is(1));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("3") - 1], is(42));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("4") - 1], is(1));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("5") - 1], is(46));
    assertThat(occurrenceCountPerEvent[Integer.parseInt("6") - 1], is(12));
  }

  @Test
  public void countEventOccurrencesInMay() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(eventsForTest, in(YearMonth.of(2016, Month.MAY)));
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
  public void countEventOccurrencesInJuly() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(eventsForTest, in(YearMonth.of(2016, Month.JULY)));
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
  public void countEventOccurrencesInAGivenPeriod() {
    List<CalendarEventOccurrence> occurrences = generator.generateOccurrencesOf(eventsForTest,
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
  public void dateOfEventOccurrencesInJuly() {
    List<CalendarEventOccurrence> occurrences =
        generator.generateOccurrencesOf(eventsForTest, in(YearMonth.of(2016, Month.JULY)));
    assertThat(occurrences.size(), is(4));
    // first occurrence
    Iterator<CalendarEventOccurrence> iterator = occurrences.iterator();
    CalendarEventOccurrence occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("3"));
    assertThat(occurrence.getStartDateTime(), is(dateTime(2016, 7, 1, 9, 0)));
    assertThat(occurrence.getEndDateTime(), is(dateTime(2016, 7, 1, 9, 15)));
    // second occurrence
    occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("3"));
    assertThat(occurrence.getStartDateTime(), is(dateTime(2016, 7, 8, 9, 0)));
    assertThat(occurrence.getEndDateTime(), is(dateTime(2016, 7, 8, 9, 15)));
    // third occurrence
    occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("4"));
    assertThat(occurrence.getStartDateTime(), is(dateTime(2016, 7, 11, 0, 0)));
    assertThat(occurrence.getEndDateTime(), is(dateTime(2016, 7, 22, 23, 59)));
    // fourth occurrence
    occurrence = iterator.next();
    assertThat(occurrence.getCalendarEvent().getAttributes().get(ATTR_TEST_ID).get(), is("3"));
    assertThat(occurrence.getStartDateTime(), is(dateTime(2016, 7, 29, 9, 0)));
    assertThat(occurrence.getEndDateTime(), is(dateTime(2016, 7, 29, 9, 15)));
  }

  private static List<CalendarEvent> getCalendarEventsForTest() {
    List<CalendarEvent> events = new ArrayList<>();
    /* event 1 on Thursday 2016-08-11 */
    events.add(CalendarEvent.on(date(2016, 8, 11))
        .withTitle(EVENT_TITLE + " 1")
        .withDescription(EVENT_DESCRIPTION + " 1")
        .withAttribute(ATTR_TEST_ID, "1"));
    /* event 2 at Friday 2016-05-20 15h00 - 15h35 */
    events.add(CalendarEvent.on(
        Period.between(dateTime(2016, 5, 20, 15, 0), dateTime(2016, 5, 20, 15, 35)))
        .withTitle(EVENT_TITLE + " 2")
        .withDescription(EVENT_DESCRIPTION + " 2")
        .withAttribute(ATTR_TEST_ID, "2"));
    /* event 3 at 09h00 - 09h15 every Fridays from 2016-03-04 excluding
       Friday 2016-07-15 and Friday 2016-07-22 */
    events.add(
        CalendarEvent.on(Period.between(dateTime(2016, 3, 4, 9, 0), dateTime(2016, 3, 4, 9, 15)))
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
        CalendarEvent.on(Period.between(dateTime(2016, 9, 1, 10, 0), dateTime(2016, 9, 1, 11, 0)))
            .withTitle(EVENT_TITLE + " 5")
            .withDescription(EVENT_DESCRIPTION + " 5")
            .withAttribute(ATTR_TEST_ID, "5")
            .recur(Recurrence.every(WEEK)
                .on(MONDAY, TUESDAY, WEDNESDAY)
                .upTo(dateTime(2016, 12, 20, 10, 0))
                .excludeEventOccurrencesStartingAt(date(2016, 11, 30), date(2016, 12, 12))));
    /* event 6 at 08h00 - 09h00 every month on all Thursdays and on the third Friday
       from Thursday 2016-04-28 to Friday 2016-07-01 */
    events.add(
        CalendarEvent.on(Period.between(dateTime(2016, 4, 28, 8, 0), dateTime(2016, 4, 28, 9, 0)))
            .withTitle(EVENT_TITLE + " 6")
            .withDescription(EVENT_DESCRIPTION + " 6")
            .withAttribute(ATTR_TEST_ID, "6")
            .recur(Recurrence.every(MONTH)
                .on(DayOfWeekOccurrence.all(THURSDAY), DayOfWeekOccurrence.nth(3, FRIDAY))
                .upTo(date(2016, 6, 30))));
    return events;
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

  private static OffsetDateTime dateTime(int year, int month, int day, int hour, int minute) {
    return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
  }
}
