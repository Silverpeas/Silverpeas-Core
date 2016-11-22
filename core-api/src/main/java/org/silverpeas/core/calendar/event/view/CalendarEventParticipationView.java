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
package org.silverpeas.core.calendar.event.view;

import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.event.InternalAttendee;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A view in which the occurrences of calendar events are grouped by their participants.
 * A participant is either an author or an attendee for an event.
 * The events occurrences are grouped by participant and for each participant the occurrences are
 * sorted by calendar identifier and by date time.
 * @author mmoquillon
 */
public class CalendarEventParticipationView implements CalendarEventView<String> {

  @Override
  public Map<String, List<CalendarEventOccurrence>> apply(
      final List<CalendarEventOccurrence> occurrences) {
    Map<String, List<CalendarEventOccurrence>> view = new HashMap<>();
    for (CalendarEventOccurrence occurrence : occurrences) {
      add(view, occurrence.getCalendarEvent().getCreatedBy(), occurrence);
      occurrence.getCalendarEvent()
          .getAttendees()
          .stream()
          .filter(a -> a instanceof InternalAttendee)
          .forEach(a -> add(view, a.getId(), occurrence));
    }
    return view;
  }

  private void add(Map<String, List<CalendarEventOccurrence>> view, String participant,
      CalendarEventOccurrence occurrence) {
    List<CalendarEventOccurrence> occurrences =
        view.computeIfAbsent(participant, u -> new LinkedList<>());
    occurrences.add(occurrence);
    Collections.sort(occurrences, (o1, o2) -> {
      int c = o1.getCalendarEvent()
          .getCalendar()
          .getId()
          .compareTo(o2.getCalendarEvent().getCalendar().getId());
      return c == 0 ? o1.getStartDateTime().compareTo(o2.getStartDateTime()) : c;
    });
  }

}
  