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

package org.silverpeas.core.date;

import org.silverpeas.core.util.DateUtil;

import java.util.Calendar;
import java.util.TimeZone;

import static java.util.Calendar.*;

/**
 * A date on a day of a month and in year. The time isn't represented here. Please refers to
 * DateTime to models also the time.
 */
public class Date extends AbstractDateDatable<Date> {

  private static final long serialVersionUID = 7970735205076340522L;
  private TimeZone timeZone = TimeZone.getDefault();

  /**
   * Creates a date set to today.
   * @return today date.
   */
  public static Date today() {
    Calendar today = getInstance();
    return dateOn(today.get(YEAR), today.get(MONTH) + 1, today.get(DAY_OF_MONTH));
  }

  /**
   * Creates a date set to tomorrow.
   * @return tomorrow date.
   */
  public static Date tomorrow() {
    Calendar tomorrow = getInstance();
    tomorrow.add(DAY_OF_MONTH, 1);
    return dateOn(tomorrow.get(YEAR), tomorrow.get(MONTH) + 1, tomorrow.get(DAY_OF_MONTH));
  }

  /**
   * Creates a date set to yesterday.
   * @return yesterday date.
   */
  public static Date yesterday() {
    Calendar yesterday = getInstance();
    yesterday.add(DAY_OF_MONTH, -1);
    return dateOn(yesterday.get(YEAR), yesterday.get(MONTH) + 1, yesterday.get(DAY_OF_MONTH));
  }

  /**
   * Creates a date on the specified year in the specified month (1 to 12) and in the specified day
   * (1 to 31).
   * @param year the year.
   * @param month the month (1 for january, 12 for december).
   * @param dayOfMonth the day of month: 1 to 28,29,30,31 according to the month.
   * @return the date corresponding to the specified date information.
   */
  public static Date dateOn(int year, int month, int dayOfMonth) {
    Calendar date = getInstance();
    date.clear();
    date.set(DAY_OF_MONTH, dayOfMonth);
    date.set(MONTH, month - 1);
    date.set(YEAR, year);
    return new Date(date.getTime());
  }

  /**
   * Constructs a new date from the specified Java date.
   * @param aDate the Java date from which this Silverpeas date is built.
   */
  public Date(final java.util.Date aDate) {
    super(aDate.getTime());
  }

  @Override
  protected Date newInstanceFrom(final java.util.Date aDate) {
    return new Date(aDate);
  }

  @Override
  public Date clone() {
    return newInstanceFrom(this);
  }

  @Override
  public java.util.Date asDate() {
    return new java.util.Date(getTime());
  }

  /**
   * The time zone for a date has no meaning.
   * @param timeZone the time zone to set to this date.?
   */
  @Override
  public Date inTimeZone(final TimeZone timeZone) {
    this.timeZone = (TimeZone) timeZone.clone();
    return this;
  }

  @Override
  public String toISO8601() {
    return DateUtil.formatAsISO8601Day(this);
  }

  @Override
  public String toShortISO8601() {
    return DateUtil.formatAsISO8601Day(this);
  }

  @Override
  public String toICal() {
    return DateUtil.formatAsICalDay(this);
  }

  @Override
  public String toICalInUTC() {
    return toICal();
  }

  /**
   * The time zone has no meaning for a date.
   * @return the time zone in which this date is set.
   */
  @Override
  public TimeZone getTimeZone() {
    return (TimeZone) timeZone.clone();
  }

  /**
   * The next date to this one.
   * @return the day after this one.
   */
  public Date next() {
    Calendar tomorrow = getInstance();
    tomorrow.setTime(this);
    tomorrow.add(DAY_OF_MONTH, 1);
    return new Date(tomorrow.getTime());
  }

  /**
   * The previous date to this one.
   * @return the day preceding this one.
   */
  public Date previous() {
    Calendar yesterday = getInstance();
    yesterday.setTime(this);
    yesterday.add(DAY_OF_MONTH, -1);
    return new Date(yesterday.getTime());
  }

  @Override
  public boolean isBefore(Date anotherDatable) {
    Calendar self = getInstance();
    self.setTime(this);
    Calendar other = getInstance();
    other.setTime(anotherDatable);
    return self.get(YEAR) < other.get(YEAR) ||
        (isInSameYear(self, other) && self.get(MONTH) < other.get(MONTH)) ||
        (isInSameMonthInYear(self, other) && self.get(DAY_OF_MONTH) < other.get(DAY_OF_MONTH));
  }

  @Override
  public boolean isAfter(Date anotherDatable) {
    Calendar self = getInstance();
    self.setTime(this);
    Calendar other = getInstance();
    other.setTime(anotherDatable);
    return self.get(YEAR) > other.get(YEAR) ||
        (isInSameYear(self, other) && self.get(MONTH) > other.get(MONTH)) ||
        (isInSameMonthInYear(self, other) && self.get(DAY_OF_MONTH) > other.get(DAY_OF_MONTH));
  }

  @Override
  public boolean isEqualTo(Date anotherDatable) {
    Calendar self = getInstance();
    self.setTime(this);
    Calendar other = getInstance();
    other.setTime(anotherDatable);
    return isInSameMonthInYear(self, other) && self.get(DAY_OF_MONTH) == other.get(DAY_OF_MONTH);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    Date other;
    if (obj.getClass().getName().equals("java.util.Date")) {
      other = new Date((java.util.Date) obj);
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      other = (Date) obj;
    }
    return isEqualTo(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Is the two specified calendar dates are in both a date in the same year?
   * @param date1 the date1 to compare with.
   * @param date2 the date2 to compare with.
   * @return true if the two calendar dates are both a date in the same year, false otherwise.
   */
  private boolean isInSameYear(Calendar date1, Calendar date2) {
    return date1.get(YEAR) == date2.get(YEAR);
  }

  /**
   * Is the two specified calendar dates are in both a date in the same month in year?
   * @param date1 the date1 to compare with.
   * @param date2 the date2 to compare with.
   * @return true if the two calendar dates are both a date in the same month in the same year,
   *         false otherwise.
   */
  private boolean isInSameMonthInYear(Calendar date1, Calendar date2) {
    return date1.get(YEAR) == date2.get(YEAR) && date1.get(MONTH) == date2.get(MONTH);
  }
}
