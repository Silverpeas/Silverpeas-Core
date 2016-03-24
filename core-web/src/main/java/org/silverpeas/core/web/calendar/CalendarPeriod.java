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
 * FLOSS exception. You should have recieved a copy of the text describing
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

import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;

import java.util.Date;

/**
 * User: Yohann Chastagnier
 * Date: 23/04/13
 */
public class CalendarPeriod {

  private Period period;
  private CalendarDateTime beginDate;
  private CalendarDateTime endDate;

  public static CalendarPeriod from(Date referenceDate, PeriodType periodType, String locale) {
    return CalendarPeriod.from(Period.from(referenceDate, periodType, locale), locale);
  }

  public static CalendarPeriod from(Period period, String locale) {
    return new CalendarPeriod(period, locale);
  }

  private CalendarPeriod(Period period, String locale) {
    this.period = period;
    this.beginDate = new CalendarDateTime(period.getBeginDate(), locale);
    this.endDate = new CalendarDateTime(period.getEndDate(), locale);
  }

  public PeriodType getPeriodType() {
    return period.getPeriodType();
  }

  /**
   * @return the begin date of the period.
   */
  public CalendarDateTime getBeginDate() {
    return beginDate;
  }

  /**
   * @return the end date of the period.
   */
  public CalendarDateTime getEndDate() {
    return endDate;
  }
}
