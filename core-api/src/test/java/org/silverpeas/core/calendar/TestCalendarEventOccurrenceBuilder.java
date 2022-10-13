/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.test.TestUserProvider.aUser;

/**
 * A builder of a list of event occurrences dedicated for unit tests.
 * @author mmoquillon
 */
public class TestCalendarEventOccurrenceBuilder {

  private static final String EVENT_TITLE = "an event title";
  private static final String EVENT_DESCRIPTION = "a short event description";

  /**
   * Builds a list of different occurrences of events.
   * @return a list of calendar event occurrences.
   */
  public static List<CalendarEventOccurrence> build() {
    List<CalendarEventOccurrence> occurrences = new ArrayList<>();
    final OffsetDateTime now = OffsetDateTime.now();

    Calendar calendar = mock(Calendar.class);
    when(calendar.getId()).thenReturn("ID_1");
    when(calendar.getComponentInstanceId()).thenReturn("Kal32");

    CalendarEvent event = CalendarEvent.on(Period.between(now, now.plusHours(2)))
        .createdBy(aUser("1"))
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttendee(aUser("0"))
        .withAttendee(aUser("2"))
        .withAttendee("titi@chez-les-duponts.fr")
        .recur(Recurrence.every(1, TimeUnit.DAY).until(3));
    event.setCalendar(calendar);
    occurrences.add(new CalendarEventOccurrence(event, now, now.plusHours(2)));
    occurrences.add(
        new CalendarEventOccurrence(event, now.plusDays(1), now.plusDays(1).plusHours(2)));
    occurrences.add(
        new CalendarEventOccurrence(event, now.plusDays(2), now.plusDays(2).plusHours(2)));

    calendar = mock(Calendar.class);
    when(calendar.getId()).thenReturn("ID_2");
    when(calendar.getComponentInstanceId()).thenReturn("Kal32");
    event = CalendarEvent.on(now.toLocalDate())
        .createdBy(aUser("0"))
        .withAttendee("toto@chez-les-duponts.fr")
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
    event.setCalendar(calendar);
    occurrences.add(new CalendarEventOccurrence(event, event.getStartDate(), event.getEndDate()));

    calendar = mock(Calendar.class);
    when(calendar.getId()).thenReturn("ID_3");
    when(calendar.getComponentInstanceId()).thenReturn("Kal12");
    event = CalendarEvent.on(Period.between(now, now.plusHours(3)))
        .createdBy(aUser("0"))
        .withAttendee(aUser("1"))
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
    event.setCalendar(calendar);
    occurrences.add(new CalendarEventOccurrence(event, event.getStartDate(), event.getEndDate()));

    return occurrences;
  }

}
  