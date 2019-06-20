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

import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.ServiceProvider;

import java.time.ZonedDateTime;
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
   * time.
   *
   * The occurrences are computed from specified window of time that implies a set of events planned
   * in this period.
   *
   * This method doesn't require to be implemented.
   * @param timeWindow the time window in which the events occur.
   * @return a set of event occurrences that occur in the specified window of time sorted by the
   * datetime at which they start.
   */
  default List<CalendarEventOccurrence> generateOccurrencesIn(CalendarTimeWindow timeWindow) {
    return generateOccurrencesOf(timeWindow.getEvents(), timeWindow.getPeriod());
  }

  /**
   * Generates the actual occurrences of the specified events and that occur in the period of time.
   *
   * The occurrences are computed from the recurrence rule of the specified events, from the date
   * and times at which the events start, and for the specified period of time.
   *
   * This method require to be implemented.
   * @param events the events.
   * @param inPeriod the period of time the instances of the events occur.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  List<CalendarEventOccurrence> generateOccurrencesOf(List<CalendarEvent> events, Period inPeriod);

  /**
   * Counts the number of occurrences of the specified event in the given period. If the period is
   * null, then the period over which the event recurs is taken into account.
   *
   * This method is a faster way to compute the occurrence count of an event by considering only
   * its recurrence rule and by not generating any occurrences.
   * @param event an event.
   * @param inPeriod the period of time the instances of the events occur. It can be null, in this
   * case the recurrence period is taken in the computation.
   * @return the number of occurrences of the event occurring in the specified period or -1 if
   * the event isn't yet planned or {@link Long#MAX_VALUE} if there an unlimited number of
   * occurrences (endless recurrence).
   */
  long countOccurrencesOf(final CalendarEvent event, Period inPeriod);

  /**
   * Generates the next occurrence of the specified event since the given date time.
   *
   * The next occurrence is computed from the recurrence rule of the specified event, from the date
   * and times at which the events start, from the specified date, and from recurrence exceptions.
   *
   * This method requires to be implemented.
   * @param event an event.
   * @param since the date time since which the next occurrence must be computed. No occurrence
   * occurring at this exact given date time (with a precision of one minute) isn't taken into
   * account.
   * @return the next occurrence of the given event, null if not {@link CalendarEventOccurrence}
   * can be computed.
   */
  CalendarEventOccurrence generateNextOccurrenceOf(CalendarEvent event, ZonedDateTime since);
}
