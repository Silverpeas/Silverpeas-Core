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
 * "http://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import static java.time.Month.DECEMBER;

/**
 * This class represents a window of time of a calendar within which events can occur.
 * @author Yohann Chastagnier
 */
public class CalendarTimeWindow {

  private final Calendar calendar;
  private final LocalDate startDate, endDate;
  private final List<CalendarEvent> events;

  CalendarTimeWindow(final Calendar calendar, final Year year) {
    this(calendar, year.atDay(1), year.atMonth(DECEMBER).atEndOfMonth());
  }

  CalendarTimeWindow(final Calendar calendar, final YearMonth yearMonth) {
    this(calendar, yearMonth.atDay(1), yearMonth.atEndOfMonth());
  }

  CalendarTimeWindow(final Calendar calendar, final LocalDate day) {
    this(calendar, day, day);
  }

  CalendarTimeWindow(final Calendar calendar, final LocalDate startDate, final LocalDate endDate) {
    this.calendar = calendar;
    this.startDate = startDate;
    this.endDate = endDate;
    this.events = CalendarEventRepository.get()
        .getAllBetween(this.calendar, startDate.atStartOfDay().atOffset(ZoneOffset.UTC),
            endDate.plusDays(1).atStartOfDay().minusMinutes(1).atOffset(ZoneOffset.UTC));
  }

  /**
   * Gets the calendar on which this window of time is opened.
   * @return the calendar concerned by this window of time.
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Gets the date at which this window of time starts.
   * @return the start date of this window of time.
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * Gets the date at which this window of time ends.
   * @return the end date of this window of time.
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the list of occurrences of events that occur in this window of time.
   * @return a list of event occurrences.
   */
  public List<CalendarEventOccurrence> getEventOccurrences() {
    return CalendarEventOccurrence.getOccurrencesIn(this);
  }

  /**
   * Gets all the events that have at least one occurrence in this window of time.
   * @return a list of events that occur in this window of time.
   */
  public List<CalendarEvent> getEvents() {
    return events;
  }
}
