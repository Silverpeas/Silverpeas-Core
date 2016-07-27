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
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * A recurrent event is created on a period of time that is recurred regularly on the timeline
 * according to a given recurrence (or frequency). The recurrence can have some exceptions
 * explicitly defined. The event is effective only when it is added in a given calendar which will
 * be in charge of the computation of the occurrences of the event according to its recurrence
 * rules (this will be covered by another unit test).
 * @author mmoquillon
 */
public class RecurrentCalendarEventCreationTest {

  private static final String EVENT_TITLE = "an event title";
  private static final String EVENT_DESCRIPTION = "a short event description";

  @Test
  public void createADailyEvent() {
    final LocalDate today = LocalDate.now();
    CalendarEvent event = CalendarEvent.on(today)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .recur(CalendarEventRecurrence.every(2, TimeUnit.DAY));
    assertThat(event.getStartDateTime(), is(today.atStartOfDay().atOffset(ZoneOffset.UTC)));
    assertThat(event.getEndDateTime(), is(today.atTime(23, 59).atOffset(ZoneOffset.UTC)));
    assertDefaultValuesOf(event);
    assertTitleAndDescriptionOf(event);
  }

  private void assertDefaultValuesOf(CalendarEvent event) {
    assertThat(event.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(event.getAttendees().isEmpty(), is(true));
    assertThat(event.getCategories().isEmpty(), is(true));
  }

  private void assertTitleAndDescriptionOf(CalendarEvent event) {
    assertThat(event.getTitle(), is(EVENT_TITLE));
    assertThat(event.getDescription(), is(EVENT_DESCRIPTION));
  }
}
