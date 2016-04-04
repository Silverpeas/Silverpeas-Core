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

package org.silverpeas.core.calendar;

import net.fortuna.ical4j.model.WeekDay;

import java.util.Calendar;
import java.util.Date;

/**
 * The days of week.
 */
public enum DayOfWeek {
  MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

  /**
   * Gets the corresponding ICal4J day of week.
   * @return
   */
  public WeekDay toICal4J() {
    switch (this) {
      case MONDAY:
        return WeekDay.MO;
      case TUESDAY:
        return WeekDay.TU;
      case WEDNESDAY:
        return WeekDay.WE;
      case THURSDAY:
        return WeekDay.TH;
      case FRIDAY:
        return WeekDay.FR;
      case SATURDAY:
        return WeekDay.SA;
      case SUNDAY:
        return WeekDay.SU;
    }
    return null;
  }

  /**
   * Gets the Silverpeas Day Of Week from a {@link Date}
   * @param aDate
   * @return
   */
  public static DayOfWeek fromDate(Date aDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(aDate);
    return fromDayOfWeekNumber(calendar.get(Calendar.DAY_OF_WEEK));
  }

  /**
   * Gets the Silverpeas Day Of Week from the number of the day in week normalized by {@link
   * Calendar}.
   * @param dayOfWeekNumber
   * @return
   */
  public static DayOfWeek fromDayOfWeekNumber(int dayOfWeekNumber) {
    switch (dayOfWeekNumber) {
      case java.util.Calendar.MONDAY:
        return MONDAY;
      case java.util.Calendar.TUESDAY:
        return TUESDAY;
      case java.util.Calendar.WEDNESDAY:
        return WEDNESDAY;
      case java.util.Calendar.THURSDAY:
        return THURSDAY;
      case java.util.Calendar.FRIDAY:
        return FRIDAY;
      case java.util.Calendar.SATURDAY:
        return SATURDAY;
      case java.util.Calendar.SUNDAY:
        return SUNDAY;
    }
    return null;
  }
}
