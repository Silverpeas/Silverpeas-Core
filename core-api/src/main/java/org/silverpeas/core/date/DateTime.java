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

import org.apache.commons.lang3.time.FastDateFormat;
import org.silverpeas.core.util.DateUtil;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A date and time.
 */
public class DateTime extends AbstractDateDatable<DateTime> {

  private static final long serialVersionUID = -2562622075317046753L;
  private TimeZone timeZone = TimeZone.getDefault();

  /**
   * Creates a new date time set at now.
   * @return now date time.
   */
  public static DateTime now() {
    return new DateTime(new java.util.Date());
  }

  /**
   * Creates a new date time from the specified parts of the time specification year month day hour
   * minute second millisecond. The hour, minute, second and millisecond parts are optional; if not
   * passed, they are set at 0. For example, the following patterns are valid:
   * <ul>
   * <li>at(2011, 5, 23, 10, 57, 23, 12) meaning in ISO 86601 2011-05-23T10:57:23.012</li>
   * <li>at(2011, 5, 23, 10, 57, 23) meaning in ISO 86601 2011-05-23T10:57:23.00</li>
   * <li>at(2011, 5, 23, 10, 57) meaning in ISO 86601 2011-05-23T10:57:00.00</li>
   * <li>at(2011, 5, 23, 10) meaning in ISO 86601 2011-05-23T10:00:00.00</li>
   * <li>at(2011, 5, 23) meaning in ISO 86601 2011-05-23T00:00:00.00</li>
   * </ul>
   * @param timeParts the different parts of the date time to set in the following order: year,
   * month, day, hour, minute, second, millisecond. The year, month, and day are mandatory whereas
   * other time parts are optional. If one optional part isn't passed, then it is set to 0.
   * @return a date time matching the specified date and time specification.
   */
  public static DateTime dateTimeAt(int... timeParts) {
    if (timeParts.length < 3) {
      throw new IllegalArgumentException("The year, month and day must be set");
    }
    Calendar calendar = Calendar.getInstance();
    calendar.set(timeParts[0], timeParts[1], timeParts[2], 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    if (timeParts.length >= 4) {
      calendar.set(Calendar.HOUR_OF_DAY, timeParts[3]);
      if (timeParts.length >= 5) {
        calendar.set(Calendar.MINUTE, timeParts[4]);
        if (timeParts.length >= 6) {
          calendar.set(Calendar.SECOND, timeParts[5]);
          if (timeParts.length >= 7) {
            calendar.set(Calendar.MILLISECOND, timeParts[6]);
          }
        }
      }
    }
    return new DateTime(calendar.getTime());
  }

  /**
   * Constructs a new date time from the specified Java date and with the host time zone as time
   * zone.
   * @param aDate the Java date from which a date time is built.
   */
  public DateTime(final java.util.Date aDate) {
    super(aDate.getTime());
  }

  /**
   * Constructs a new date time from the specified Java date and in the specified time zone.
   * @param aDate the Java date from which a date time is built.
   * @param timeZone the time zone in which this date is set.
   */
  public DateTime(final java.util.Date aDate, final TimeZone timeZone) {
    super(aDate.getTime());
    this.timeZone = timeZone;
  }

  @Override
  protected DateTime newInstanceFrom(final java.util.Date aDate) {
    return new DateTime(aDate, getTimeZone());
  }

  @Override
  public DateTime clone() {
    return newInstanceFrom(this);
  }

  @Override
  public java.util.Date asDate() {
    return new java.util.Date(getTime());
  }

  @Override
  public String toISO8601() {
    FastDateFormat formatter = FastDateFormat.getInstance(ISO_8601_PATTERN, getTimeZone());
    return formatter.format(this);
  }

  @Override
  public String toShortISO8601() {
    FastDateFormat formatter = FastDateFormat.getInstance(SHORT_ISO_8601_PATTERN, getTimeZone());
    return formatter.format(this);
  }

  @Override
  public String toICal() {
    return DateUtil.formatAsICalDate(this);
  }

  @Override
  public String toICalInUTC() {
    return DateUtil.formatAsICalUTCDate(this);
  }

  @Override
  public DateTime inTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
    return this;
  }

  @Override
  public TimeZone getTimeZone() {
    return this.timeZone;
  }

  @Override
  public boolean isBefore(DateTime anotherDatable) {
    return super.before(anotherDatable);
  }

  @Override
  public boolean isAfter(DateTime anotherDatable) {
    return super.after(anotherDatable);
  }

  @Override
  public boolean isEqualTo(DateTime anotherDatable) {
    return super.equals(anotherDatable);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    DateTime other;
    if (obj.getClass().getName().equals("java.util.Date")) {
      other = new DateTime((java.util.Date) obj);
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      other = (DateTime) obj;
    }
    return isEqualTo(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Is the date in this datetime before the specified date.
   * @param otherDate the other date.
   * @return true if the date part of this datetime is before the other date.
   */
  public boolean isBefore(Date otherDate) {
    return otherDate.isBefore(new Date(this));
  }

  /**
   * Is the date in this datetime after the specified date.
   * @param otherDate the other date.
   * @return true if the date part of this datetime is after the other date.
   */
  public boolean isAfter(Date otherDate) {
    return otherDate.isAfter(new Date(this));
  }

  /**
   * Is the date in this datetime equal to the specified date.
   * @param otherDate the other date.
   * @return true if the date part of this datetime is equal to the other date.
   */
  public boolean isEqualTo(Date otherDate) {
    return otherDate.isEqualTo(new Date(this));
  }

  /**
   * Converts this date time to a date. The time part of this date time is lost.
   * @return a date representation of this date time.
   */
  public Date toDate() {
    return new Date(this);
  }
}
