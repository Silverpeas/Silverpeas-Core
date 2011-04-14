/*
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.calendar;

import com.stratelia.webactiv.util.DateUtil;
import java.util.TimeZone;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * A date and time.
 */
public class DateTime extends java.util.Date implements Datable<DateTime>, Cloneable {
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
   * @param timeZone  the time zone in which this date is set.
   */
  public DateTime(final java.util.Date aDate, final TimeZone timeZone) {
    super(aDate.getTime());
    this.timeZone = timeZone;
  }

  @Override
  public DateTime clone() {
    return new DateTime(new java.util.Date(getTime()), getTimeZone());
  }

  @Override
  public java.util.Date asDate() {
    return new java.util.Date(getTime());
  }

  @Override
  public String toISO8601() {
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
}
