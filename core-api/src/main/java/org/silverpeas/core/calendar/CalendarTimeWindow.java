/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class represents a window of time within which calendar events can occur. Some constraints
 * can be done to filter the events in the window of time by one or several properties.
 * @author Yohann Chastagnier
 */
public class CalendarTimeWindow {

  private CalendarEventFilter filter = new CalendarEventFilter();
  private final LocalDate startDate;
  private final LocalDate endDate;
  private List<CalendarEvent> events;

  CalendarTimeWindow(final LocalDate startDate, final LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Filters the calendar events occurring in this window of time.
   * @param filterConsumer a function accepting a {@link CalendarEventFilter} instance to set
   * the different filtering criteria.
   * @return itself.
   */
  public CalendarTimeWindow filter(Consumer<CalendarEventFilter> filterConsumer) {
    filterConsumer.accept(this.filter);
    return this;
  }

  /**
   * Gets the date at which this window of time starts.
   * @return the inclusive start date of this window of time.
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * Gets the date at which this window of time ends.
   * @return the exclusive end date of this window of time.
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the temporal period covered by this window of time.
   * @return the period covered by this window of time.
   */
  public Period getPeriod() {
    return Period.between(getStartDate(), getEndDate());
  }

  /**
   * Gets the list of occurrences of events that occur in this window of time, once the filtering
   * applied. If no filters were previously defined, then the occurrences of all the events
   * occurring in this window of time will be returned.
   * @return a list of event occurrences.
   */
  public List<CalendarEventOccurrence> getEventOccurrences() {
    return CalendarEventOccurrence.getOccurrencesIn(this);
  }

  /**
   * Gets all the events that have at least one occurrence in this window of time, once the
   * filtering applied. If no filters were previously defined, then all the events that occur in
   * this window of time will be returned.
   * @return a list of events that occur in this window of time.
   */
  public List<CalendarEvent> getEvents() {
    if (events == null) {
      events = CalendarEventRepository.get()
          .getAllBetween(filter, startDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
              endDate.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
    return events;
  }
}
