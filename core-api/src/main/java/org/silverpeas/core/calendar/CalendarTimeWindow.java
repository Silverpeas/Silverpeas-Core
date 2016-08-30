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
 * FLOSS exception. You should have received a copy of the text describing
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

import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.event.PlannedEventOccurrences;
import org.silverpeas.core.util.logging.SilverLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static java.time.Month.DECEMBER;

/**
 * This class represents a time window into which occurrences of events will be computed.
 * @author Yohann Chastagnier
 */
public class CalendarTimeWindow {

  private static Method peoInMethod;
  private static Method peoGetMethod;

  private final Calendar calendar;
  private final LocalDate startDate, endDate;
  private PlannedEventOccurrences occurrences;

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
    initCollectionOfPlannedEventOccurrences();
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the list of occurrences of the planned events in this collection.
   * @return a list of planned event occurrences.
   */
  @SuppressWarnings("unchecked")
  public List<CalendarEventOccurrence> getEventOccurrences() {
    List<CalendarEventOccurrence> result = null;
    try {
      if (peoInMethod == null) {
        peoInMethod =
            PlannedEventOccurrences.class.getDeclaredMethod("in", CalendarTimeWindow.class);
        peoInMethod.setAccessible(true);
      }
      result = (List<CalendarEventOccurrence>) peoInMethod.invoke(occurrences, this);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return result;
  }

  private void initCollectionOfPlannedEventOccurrences() {
    try {
      if (peoGetMethod == null) {
        peoGetMethod = PlannedEventOccurrences.class.getDeclaredMethod("get");
        peoGetMethod.setAccessible(true);
      }
      this.occurrences = (PlannedEventOccurrences) peoGetMethod.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }
}
