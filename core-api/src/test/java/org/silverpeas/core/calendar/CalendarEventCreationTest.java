/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An event is created for a period of time but it is effective only when setting on the timeline
 * by adding it to a given calendar.
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
    assertThat(event.getStartDate(), is(today));
    assertThat(event.getEndDate(), is(today.plusDays(1)));
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
    assertThat(event.getStartDate(), is(today));
    assertThat(event.getEndDate(), is(dayAfterTomorrow));
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
    assertThat(event.getStartDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(event.getEndDate(), is(inThreeHours.withOffsetSameInstant(ZoneOffset.UTC)));
    assertTitleAndDescriptionOf(event);
    assertDefaultValuesOf(event);
  }

  @Test
  public void createADefaultNewEventWithEndDateBeforeStartDate() {
    assertThrows(IllegalArgumentException.class, () -> {
      LocalDate now = LocalDate.now();
      LocalDate yesterday = now.minusDays(1);
      CalendarEvent.on(Period.between(now, yesterday));
    });
  }

  @Test
  public void createADefaultNewEventWithEndDateTimeBeforeStartDateTime() {
    assertThrows(IllegalArgumentException.class, () -> {
      OffsetDateTime now = OffsetDateTime.now();
      OffsetDateTime yesterday = now.minusDays(1);
      CalendarEvent.on(Period.between(now, yesterday));
    });
  }

  private void assertDefaultValuesOf(CalendarEvent event) {
    assertThat(event.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(event.getAttendees().isEmpty(), is(true));
    assertThat(event.getCategories().isEmpty(), is(true));
    assertThat(event.getRecurrence(), is(Recurrence.NO_RECURRENCE));
  }

  private void assertTitleAndDescriptionOf(CalendarEvent event) {
    assertThat(event.getTitle(), is(EVENT_TITLE));
    assertThat(event.getDescription(), is(EVENT_DESCRIPTION));
  }
}
