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
package org.silverpeas.core.calendar.view;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.InternalAttendee;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.DECLINED;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.REQUIRED;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * A view in which the occurrences of calendar events are grouped by their participants.
 * A participant is either an author or an attendee for an event.
 * The events occurrences are grouped by participant and for each participant the occurrences are
 * sorted by calendar identifier and by datetime.
 * @author mmoquillon
 */
public class CalendarEventInternalParticipationView implements CalendarEventView<String> {

  private final Set<String> filterOnParticipantIds;

  public CalendarEventInternalParticipationView() {
    this(Collections.emptySet());
  }

  public CalendarEventInternalParticipationView(Collection<User> filterOnParticipants) {
    this.filterOnParticipantIds =
        filterOnParticipants.stream().map(User::getId).collect(Collectors.toSet());
  }

  @Override
  public Map<String, List<CalendarEventOccurrence>> apply(
      final List<CalendarEventOccurrence> occurrences) {
    Map<String, List<CalendarEventOccurrence>> view = new HashMap<>();
    for (CalendarEventOccurrence occurrence : occurrences) {
      final String createdBy = occurrence.getCalendarEvent().getCreator().getId();
      if (filterOnParticipantIds.isEmpty() || filterOnParticipantIds.contains(createdBy)) {
        add(view, createdBy, occurrence);
      }
      occurrence.getAttendees()
          .stream()
          .filter(a -> a instanceof InternalAttendee)
          .filter(a -> filterOnParticipantIds.isEmpty() ||
                 (!a.getId().equals(createdBy) && filterOnParticipantIds.contains(a.getId())))
          .filter(a -> REQUIRED == a.getPresenceStatus() || DECLINED != a.getParticipationStatus())
          .forEach(a -> add(view, a.getId(), occurrence));
    }
    view.values().forEach(userOccurrences -> userOccurrences.sort(Comparator.comparing(
        (CalendarEventOccurrence o) -> o.getCalendarEvent().getCalendar().getId())
        .thenComparing(o -> asOffsetDateTime(o.getStartDate()))));
    return view;
  }

  private void add(Map<String, List<CalendarEventOccurrence>> view, String participant,
      CalendarEventOccurrence occurrence) {
    List<CalendarEventOccurrence> occurrences =
        view.computeIfAbsent(participant, u -> new LinkedList<>());
    occurrences.add(occurrence);
  }

}
  