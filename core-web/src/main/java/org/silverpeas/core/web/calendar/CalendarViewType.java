/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.calendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.silverpeas.core.date.period.PeriodType;

/**
 * It defines the type of view mode of a calendar.
 */
public enum CalendarViewType {

  /**
   * The calendar view is yearly.
   */
  YEARLY(PeriodType.year, "year"),
  /**
   * The calendar view is monthly.
   */
  MONTHLY(PeriodType.month, "month"),
  /**
   * The calendar view is weekly.
   */
  WEEKLY(PeriodType.week, "agendaWeek"),
  /**
   * The calendar view is daily.
   */
  DAILY(PeriodType.day, "agendaDay"),
  /**
   * The calendar view is on the next events.
   */
  NEXT_EVENTS(PeriodType.unknown, "nextevents");

  private PeriodType periodeType;

  /**
   * Converts this view type in a string representation.
   * The value of the string depends on the calendar view rendering engine. It should be a value
   * that matches the view mode supported by the underlying calendar renderer.
   * @return
   */
  @Override
  public String toString() {
    return calendarView;
  }

  /**
   * Is this view type is a yearly one.
   * @return true if this view type is for a yearly one, false otherwise.
   */
  public boolean isYearlyView() {
    return this == YEARLY;
  }

  /**
   * Is this view type is a monthly one.
   * @return true if this view type is for a monthly one, false otherwise.
   */
  public boolean isMonthlyView() {
    return this == MONTHLY;
  }

  /**
   * Is this view type is a weekly one.
   * @return true if this view type is for a weekly one, false otherwise.
   */
  public boolean isWeeklyView() {
    return this == WEEKLY;
  }

  /**
   * Is this view type is a daily one.
   * @return true if this view type is for a daily one, false otherwise.
   */
  public boolean isDailyView() {
    return this == DAILY;
  }


  /**
   * Is this view type is a on the next events.
   * @return true if this view type is on the next events one, false otherwise.
   */
  public boolean isNextEventsView() {
    return this == NEXT_EVENTS;
  }

  /**
   * Gets the name of this enum.
   * @see CalendarViewType#name()
   * @return the enum name.
   */
  @JsonValue
  public String getName() {
    return name();
  }

  public PeriodType getPeriodeType() {
    return periodeType;
  }

  /**
   * Constructs a view type with the specified view mode.
   * @param periodeType the type of the period represented by the view
   * @param viewMode the view mode as defined in the underlying calendar renderer.
   */
  private CalendarViewType(PeriodType periodeType, final String viewMode) {
    this.periodeType = periodeType;
    this.calendarView = viewMode;
  }
  private String calendarView;

  @JsonCreator
  public static CalendarViewType from(String name) {
    if (name != null) {
      for (CalendarViewType viewType : CalendarViewType.values()) {
        if (name.equalsIgnoreCase(viewType.name())) {
          return viewType;
        }
      }
    }
    return null;
  }
}
