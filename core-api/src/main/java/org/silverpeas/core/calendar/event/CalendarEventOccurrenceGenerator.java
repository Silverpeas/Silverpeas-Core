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
package org.silverpeas.core.calendar.event;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.ServiceProvider;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import static java.time.Month.DECEMBER;

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
   * A convenient method to improve the readability of the generation method invocation.
   * @param period a period.
   * @return the period.
   */
  static Period in(final Period period) {
    return period;
  }

  /**
   * Generates the occurrences of the specified events and that occur in the year.
   * @param events the events.
   * @param year the year during which the event occur.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  default List<CalendarEventOccurrence> generateOccurrencesOf(Collection<CalendarEvent> events,
      Year year) {
    return generateOccurrencesOf(events,
        Period.between(year.atDay(1), year.atMonth(DECEMBER).atEndOfMonth()));
  }

  /**
   * Generates the occurrences of the specified events and that occur in the specified month and
   * year.
   * @param events the events.
   * @param inYearMonth the month in a given year during which the event occur.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  default List<CalendarEventOccurrence> generateOccurrencesOf(Collection<CalendarEvent> events,
      YearMonth inYearMonth) {
    return generateOccurrencesOf(events,
        Period.between(inYearMonth.atDay(1), inYearMonth.atEndOfMonth()));
  }

  /**
   * Generates the occurrences of the specified events and that occur at the specified day.
   * @param events the events.
   * @param inDay the day in which the event occur.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  default List<CalendarEventOccurrence> generateOccurrencesOf(Collection<CalendarEvent> events,
      LocalDate inDay) {
    return generateOccurrencesOf(events,
        Period.between(inDay.atStartOfDay().atOffset(ZoneOffset.UTC),
            inDay.plusDays(1).atStartOfDay().minusMinutes(1).atOffset(ZoneOffset.UTC)));
  }

  /**
   * Generates the occurrences of the specified events and that occur in the period of time.
   * @param events the events.
   * @return a set of event occurrences that occur in the specified period sorted by the date and
   * time at which they start.
   */
  List<CalendarEventOccurrence> generateOccurrencesOf(Collection<CalendarEvent> events,
      Period inPeriod);
}
