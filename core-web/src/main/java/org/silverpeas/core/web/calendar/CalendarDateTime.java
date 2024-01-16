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
package org.silverpeas.core.web.calendar;

import org.silverpeas.core.util.DateUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CalendarDateTime extends CalendarDay {

  private int hours;
  private int minutes;
  private int seconds;
  private int milliseconds;

  /**
   * Constructs a new CalendarDay instance from the specified date.
   * @param date the date of the day.
   * @param locale the locale.
   */
  protected CalendarDateTime(final Date date, final String locale) {
    super(date, locale);
    Calendar calendar = DateUtil.convert(date);
    hours = calendar.get(Calendar.HOUR_OF_DAY);
    minutes = calendar.get(Calendar.MINUTE);
    seconds = calendar.get(Calendar.SECOND);
    milliseconds = calendar.get(Calendar.MILLISECOND);
  }

  /**
   * Gets the hours of the datetime (from 0 to 23).
   * @return the hours of the datetime.
   */
  public int getHours() {
    return hours;
  }

  /**
   * Gets the minutes of the datetime (from 0 to 59).
   * @return the minutes of the datetime.
   */
  public int getMinutes() {
    return minutes;
  }

  /**
   * Gets the seconds of the datetime (from 0 to 59).
   * @return the seconds of the datetime.
   */
  public int getSeconds() {
    return seconds;
  }

  /**
   * Gets the milliseconds of the datetime (from 0 to 999).
   * @return the milliseconds of the datetime.
   */
  public int getMilliseconds() {
    return milliseconds;
  }

  /**
   * Gets this datetime as a Date instance.
   * @return the Date representation of this datetime.
   */
  public Date getDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, getYear());
    calendar.set(Calendar.MONTH, getMonth());
    calendar.set(Calendar.DAY_OF_MONTH, getDayOfMonth());
    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, seconds);
    calendar.set(Calendar.MILLISECOND, milliseconds);
    return calendar.getTime();
  }
}
