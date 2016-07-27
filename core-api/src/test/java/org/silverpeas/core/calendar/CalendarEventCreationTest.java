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

import org.junit.Test;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * An event belongs to a calendar and it is by the calendar an event can be created.
 * @author mmoquillon
 */
public class CalendarEventCreationTest {

  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";

  /**
   * Creates a defaulted new event on a given date.
   * By default, the event is public and has no occurrences.
   */
  @Test
  public void createADefaultNewEventOnAllDay() {
    LocalDate today = LocalDate.now();
    CalendarEvent event =
        CalendarEvent.on(today).withTitle(EVENT_TITLE).withDescription(EVENT_DESCRIPTION);
    assertThat(event.getStartDateTime(), is(today.atStartOfDay().atOffset(ZoneOffset.UTC)));
    assertThat(event.getEndDateTime(), is(today.atTime(23, 59).atOffset(ZoneOffset.UTC)));
    assertTitleAndDescriptionOf(event);
    assertDefaultValuesOf(event);
  }

  @Test
  public void createADefaultNewEventOnSeveralDays() {
    LocalDate today = LocalDate.now();
    LocalDate dayAfterTomorrow = today.plusDays(2);
    CalendarEvent event = CalendarEvent.on(Period.between(today, dayAfterTomorrow))
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
    assertThat(event.getStartDateTime(), is(today.atStartOfDay().atOffset(ZoneOffset.UTC)));
    assertThat(event.getEndDateTime(), is(dayAfterTomorrow.atTime(23, 59).atOffset(ZoneOffset.UTC)));
    assertTitleAndDescriptionOf(event);
    assertDefaultValuesOf(event);
  }

  @Test
  public void createADefaultNewEventAtAGivenDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime inThreeHours = now.plusHours(3);
    CalendarEvent event = CalendarEvent.on(Period.between(now, inThreeHours))
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
    assertThat(event.getStartDateTime(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(event.getEndDateTime(), is(inThreeHours.withOffsetSameInstant(ZoneOffset.UTC)));
    assertTitleAndDescriptionOf(event);
    assertDefaultValuesOf(event);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createADefaultNewEventWithEndDateBeforeStartDate() {
    LocalDate now = LocalDate.now();
    LocalDate yesterday = now.minusDays(1);
    CalendarEvent.on(Period.between(now, yesterday));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createADefaultNewEventWithEndDateTimeBeforeStartDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime yesterday = now.minusDays(1);
    CalendarEvent.on(Period.between(now, yesterday));
  }

  private void assertDefaultValuesOf(CalendarEvent event) {
    assertThat(event.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(event.getAttendees().isEmpty(), is(true));
    assertThat(event.getCategories().isEmpty(), is(true));
    assertThat(event.getRecurrence(), is(CalendarEventRecurrence.NO_RECURRENCE));
  }

  private void assertTitleAndDescriptionOf(CalendarEvent event) {
    assertThat(event.getTitle(), is(EVENT_TITLE));
    assertThat(event.getDescription(), is(EVENT_DESCRIPTION));
  }
}
