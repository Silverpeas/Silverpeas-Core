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
package org.silverpeas.core.calendar;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.test.CalendarWarBuilder;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This integration test is on the filtering of the occurrences of calendar events according to some
 * peculiar criteria. The occurrences can be for events planned on different calendars and for
 * which participate some given users.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarEventOccurrenceFilteringIntegrationTest extends BaseCalendarTest {

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(
        CalendarEventOccurrenceFilteringIntegrationTest.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  /**
   * Whatever the calendars, get the occurrences of events in which participate some given users
   * and in a given period of time.
   * <p>
   * Input: users that participate to some events and a period of time without any occurrences of
   * events.
   * Output: no occurrences.
   */
  @Test
  public void getNoOccurrencesWithGivenParticipantsInAnEmptyPeriod() {
    List<CalendarEventOccurrence> occurrences =
        Calendar.getTimeWindowBetween(LocalDate.now(), LocalDate.now().plusWeeks(1))
            .filter(f -> f.onParticipants(User.getById("0"), User.getById("1"), User.getById("2")))
            .getEventOccurrences();
    assertThat(occurrences.isEmpty(), is(true));
  }

  /**
   * Whatever the calendars, get the occurrences of events in which participate some given users
   * and in a given period of time.
   * <p>
   * Input: users that don't participate to the events in the given a period of time.
   * Output: no occurrences.
   */
  @Test
  public void getNoOccurrencesWithNotMatchingParticipants() {
    List<CalendarEventOccurrence> occurrences =
        Calendar.getTimeWindowBetween(LocalDate.of(2016, 1, 8), LocalDate.of(2016, 2, 27))
            .filter(f -> f.onParticipants(User.getById("2")))
            .getEventOccurrences();
    assertThat(occurrences.isEmpty(), is(true));
  }

  /**
   * Whatever the calendars, get the occurrences of events in which participate some given users
   * and in a given period of time.
   * <p>
   * Input: the author of the events that occur in the given a period of time.
   * Output: the expected occurrences.
   */
  @Test
  public void getOccurrencesForAGivenAuthor() {
    List<CalendarEventOccurrence> occurrences =
        Calendar.getTimeWindowBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 8))
            .filter(f -> f.onParticipants(User.getById("0")))
            .getEventOccurrences();

    assertThat(occurrences.size(), is(3));
    assertThat(occurrences.get(0).getCalendarEvent().getId(), is("ID_E_2"));
    assertThat(occurrences.get(1).getCalendarEvent().getId(), is("ID_E_1"));
    assertThat(occurrences.get(2).getCalendarEvent().getId(), is("ID_E_3"));
  }

  /**
   * Whatever the calendars, get the occurrences of events in which participate some given users
   * and in a given period of time.
   * <p>
   * Input: an attendee in some events that occur in the given a period of time.
   * Output: the expected occurrences.
   */
  @Test
  public void getOccurrencesForAGivenAttendee() {
    List<CalendarEventOccurrence> occurrences =
        Calendar.getTimeWindowBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 8))
            .filter(f -> f.onParticipants(User.getById("1")))
            .getEventOccurrences();

    assertThat(occurrences.size(), is(2));
    assertThat(occurrences.get(0).getCalendarEvent().getId(), is("ID_E_4"));
  }

  /**
   * Whatever the calendars, get the occurrences of events in which participate some given users
   * and in a given period of time.
   * <p>
   * Input: an author in some both non-recurring and recurring events that occur in a given a
   * period of time.
   * Output: the expected occurrences.
   */
  @Test
  public void getOccurrencesFromRecurrentEventsWithGivenParticipants() {
    List<CalendarEventOccurrence> occurrences =
        Calendar.getTimeWindowBetween(LocalDate.of(2016, 1, 9), LocalDate.of(2016, 1, 23))
            .filter(f -> f.onParticipants(User.getById("0")))
            .getEventOccurrences();

    assertThat(occurrences.size(), is(5));
    assertThat(occurrences.get(0).getCalendarEvent().getId(), is("ID_E_2"));
    assertThat(occurrences.get(1).getCalendarEvent().getId(), is("ID_E_1"));
    assertThat(occurrences.get(2).getCalendarEvent().getId(), is("ID_E_3"));
    assertThat(occurrences.get(3).getCalendarEvent().getId(), is("ID_E_5"));
    assertThat(occurrences.get(4).getCalendarEvent().getId(), is("ID_E_5"));
    assertThat(occurrences.get(3).getStartDate(), is(LocalDate.of(2016, 1, 9)));
    assertThat(occurrences.get(4).getStartDate(), is(LocalDate.of(2016, 1, 23)));
  }

  /**
   * Get the occurrences of events that were planned on some calendars in a given period of time.
   * <p>
   * Input: two calendars.
   * Output: the expected occurrences.
   */
  @Test
  public void getOccurrencesFromSeveralCalendars() {
    List<CalendarEventOccurrence> occurrences =
        Calendar.getTimeWindowBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 8))
            .filter(f -> f.onCalendar(Calendar.getById("ID_3"), Calendar.getById("ID_2")))
            .getEventOccurrences();

    assertThat(occurrences.size(), is(3));
    assertThat(occurrences.get(0).getCalendarEvent().getId(), is("ID_E_4"));
    assertThat(occurrences.get(1).getCalendarEvent().getId(), is("ID_E_2"));
    assertThat(occurrences.get(2).getCalendarEvent().getId(), is("ID_E_1"));
  }
}
  