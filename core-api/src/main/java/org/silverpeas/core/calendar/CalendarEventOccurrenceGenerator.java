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

import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.ServiceProvider;

import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

/**
 * A generator of occurrences of {@link CalendarEvent} instances that will occur in a given period
 * of time.
 * @author mmoquillon
 */
public interface CalendarEventOccurrenceGenerator {

  /**
   * Gets an instance of the default implementation of this generator.
   * @return a generator of event occurrences.
   */
  static CalendarEventOccurrenceGenerator get() {
    return ServiceProvider.getService(CalendarEventOccurrenceGenerator.class);
  }

  /**
   * Generates the actual occurrences of the calendar events that occur in the specified window of
   * time. The occurrences that were modified and hence persisted are taken in charge.
   *
   * This method doesn't require to be implemented.
   * @param timeWindow the time window in which the events occur.
   * @return a set of event occurrences that occur in the specified window of time sorted by the
   * datetime at which they start.
   */
  default List<CalendarEventOccurrence> generateOccurrencesIn(CalendarTimeWindow timeWindow) {
    return generateOccurrencesOf(timeWindow.getEvents(), Period
        .between(timeWindow.getStartDate().atStartOfDay().atOffset(ZoneOffset.UTC),
            timeWindow.getEndDate().plusDays(1).atStartOfDay().minusMinutes(1)
                .atOffset(ZoneOffset.UTC)));
  }

  /**
   * Generates the actual occurrences of the specified events and that occur in the period of time.
   * The occurrences that were modified and hence persisted are taken in charge.
   *
   * This method doesn't require to be implemented.
   * @param events the events.
   * @param inPeriod the period of time the instances of the events occur.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  default List<CalendarEventOccurrence> generateOccurrencesOf(Collection<CalendarEvent> events,
      Period inPeriod) {
    List<CalendarEventOccurrence> occurrences = computeOccurrencesOf(events, inPeriod);
    List<CalendarEventOccurrence> modified = CalendarEventOccurrenceRepository.get().getAll();
    modified.forEach(o -> {
      int idx = occurrences.indexOf(o);
      occurrences.set(idx, o);
    });
    return occurrences;
  }

  /**
   * Computes the occurrences of the specified events from their recurrence rule, their date and
   * times, and for the specified period of time. This method is used by the generation methods.
   * <p>
   * The computation doesn't take into account the occurrences that were modified; to consider an
   * actual set of occurrences of the specified events in the given period, you have to consider
   * one of the following methods:
   * <p>
   * <ul>
   * <li>
   * {@link CalendarEventOccurrenceGenerator#generateOccurrencesIn(CalendarTimeWindow)}
   * </li>
   * <li>
   * {@link CalendarEventOccurrenceGenerator#generateOccurrencesOf(Collection, Period)}
   * </li>
   * </ul>
   * @param events the events to consider.
   * @param period the period of time the instances of the events occur.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  List<CalendarEventOccurrence> computeOccurrencesOf(Collection<CalendarEvent> events,
      Period period);
}
