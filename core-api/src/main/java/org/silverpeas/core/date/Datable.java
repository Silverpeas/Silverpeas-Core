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
import org.silverpeas.core.util.time.TimeData;

import java.util.TimeZone;

/**
 * A datable object is an object that represents a date in Silverpeas, whatever it is. It can be a
 * simple date (day in month in year) or a more complete one like a date time. Dates and datetimes
 * are differents types as they don't have any instances in common, but they are part of the same
 * types class. As Java doesn't support the trait concept from which a polymorphic familly of types
 * can be modeled, the datable class is here introduced by a Java interface with a generic type.
 * Datable can be then considered as a type generator that satisfy a bound constraint (a fix point).
 * @param <T> A datable type this class should generate.
 */
public interface Datable<T extends Datable<? super T>> extends Cloneable {

  /**
   * The pattern for the short ISO 8601 date representation. The date is at the accuracy to the
   * minute and the time zone is specified as defined in the RFC 822 (indicated by the Z symbol).
   */
  static final String SHORT_ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mmZ";

  /**
   * The pattern for the ISO 8601 date representation. The date is at the accuracy to the second and
   * the time zone is specified as defined in the RFC 822 (indicated by the Z symbol).
   */
  static final String ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

  /**
   * The pattern for the iCal date representation in the current time zone of the datable.
   */
  static final String ICAL_PATTERN = "yyyyMMdd'T'HHmmss";

  /**
   * The pattern for the iCal date representation in UTC. The UTC set of the date is indicated here
   * by the Z character.
   */
  static final String ICAL_UTC_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

  /**
   * Clones itself.
   * @return a clone to this datable.
   */
  T clone();

  /**
   * Gets the java Date representation of this datable.
   * @return a Date representation of this datable.
   */
  java.util.Date asDate();

  /**
   * Is this datable strictly before in time the specified another one?
   * @param anotherDatable the another datable to which this is compared.
   * @return true if this datable is strictly before the another one.
   */
  boolean isBefore(final T anotherDatable);

  /**
   * Is this datable strictly after in time the specified another one?
   * @param anotherDatable the another datable to which this is compared.
   * @return true if this datable is strictly after the another one.
   */
  boolean isAfter(final T anotherDatable);

  /**
   * Is this datable equal in time to the specified another one?
   * @param anotherDatable the another datable to which this is compared.
   * @return true if this datable is equal with the another one.
   */
  boolean isEqualTo(final T anotherDatable);

  /**
   * Sets the time zone this datable is defined for. If no time zone is set, then the default one is
   * considered (the timezone of the host). The time zone can have no meaning with some datable
   * types like for example simple date (day, month, and year).
   * @param timeZone the time zone of this datable.
   * @return itself.
   */
  T inTimeZone(final TimeZone timeZone);

  /**
   * Gets the time zone for which this datable is defined.
   * @return the time zone of this datable.
   */
  TimeZone getTimeZone();

  /**
   * Gets an ISO 8601 textual representation of this datable by taking into account of its
   * underlying timezone. For date and time, the representation is at the accuracy to the second.
   * The ISO 8601 format in which the date is returned is one of the more common in the Web, that is
   * with the date and time separators (the hyphen for dates and the double-points for times):
   * yyyy-MM-dd'T'HH:mm:ss where yyyy means the year in 4 digits, MM means the month in year in two
   * digits, dd means the day in month in two digits, HH means the hour (24-o'clock) in two digits,
   * mm means the minute in two digits, and ss the second in two digits.
   * @return the ISO 8601 textual representation of this datable.
   */
  String toISO8601();

  /**
   * Gets an ISO 8601 textual representation of this datable by taking into account of its
   * underlying timezone. For date and time, the representation is at the accuracy to the minute.
   * The ISO 8601 format in which the date is returned is one of the more common in the Web, that is
   * with the date and time separators (the hyphen for dates and the double-points for times):
   * yyyy-MM-dd'T'HH:mm:ss where yyyy means the year in 4 digits, MM means the month in year in two
   * digits, dd means the day in month in two digits, HH means the hour (24-o'clock) in two digits,
   * and mm means the minute in two digits.
   * @return a short ISO 8601 textual representation of this datable.
   */
  String toShortISO8601();

  /**
   * Gets the ISO 8601 textual representation of this date as it is in the iCal specification. The
   * returned iCal date representation is in this datable's time zone. In the iCal specification,
   * dates are represented in one of the ISO 8601 format, that is the complete format without any
   * date and time separators (the hyphen character for dates and the double-points for times). In
   * the iCal specification, no time zone information is added to the ISO 8601 representation of the
   * date, nevertheless when the date is indicated in UTC, the 'Z' marker must be set in the date as
   * required by the ISO 8601 standard.
   * @return the iCal textual representation of this datable and in the timezone of this datable.
   */
  String toICal();

  /**
   * Gets the ISO 8601 textual representation of this date as it is in the iCal specification. The
   * returned iCal date representation is in explicitly set in UTC. In the iCal specification, dates
   * are represented in one of the ISO 8601 format, that is the complete format without any date and
   * time separators (the hyphen character for dates and the double-points for times). In the iCal
   * specification, no time zone information is added to the ISO 8601 representation of the date,
   * nevertheless when the date is indicated in UTC, the 'Z' marker must be set in the date as
   * required by the ISO 8601 standard.
   * @return the iCal textual representation of this datable and in UTC.
   */
  String toICalInUTC();

  /**
   * Computes first hour, minute, second, millisecond from the datable instance.
   * @return a date at last hour, minute, second and millisecond of the datable instance.
   */
  T getBeginOfDay();

  /**
   * Computes first hour, minute, second, millisecond from the datable instance.
   * @return a date at last hour, minute, second and millisecond of the datable instance.
   */
  T getEndOfDay();

  /**
   * Compute the first hour, minute, second, millisecond from the datable instance.
   * @return a date at last hour, minute, second and millisecond of the datable instance.
   */
  T getBeginOfWeek();

  /**
   * Compute the date of the first day in the week from the datable instance.
   * @return a date for the first day of the week of the datable instance.
   */
  T getEndOfWeek();

  /**
   * Compute the first hour, minute, second, millisecond from the datable instance and a given
   * locale.
   * @return a date at last hour, minute, second and millisecond of the datable instance.
   */
  T getBeginOfWeek(String locale);

  /**
   * Compute the date of the first day in the week from the datable instance and a given locale.
   * @return a date for the first day of the week of the datable instance.
   */
  T getEndOfWeek(String locale);

  /**
   * Compute the first hour, minute, second, millisecond from the datable instance.
   * @return a date at last hour, minute, second and millisecond of the datable instance.
   */
  T getBeginOfMonth();

  /**
   * Compute the date of the first day in the month from the datable instance.
   * @return a date for the first day of the month of the datable instance.
   */
  T getEndOfMonth();

  /**
   * Compute the first hour, minute, second, millisecond from the datable instance.
   * @return a date at last hour, minute, second and millisecond of the datable instance.
   */
  T getBeginOfYear();

  /**
   * Compute the date of the first day in the year from the datable instance.
   * @return a date for the first day of the year of the datable instance.
   */
  T getEndOfYear();

  /**
   * Compute the time between the datable instance and another one.
   * @return the time between the datable instance and another one represented by {@link TimeData}.
   */
  TimeData getTimeDataTo(T anotherDatable);

  /**
   * Adds a number of years to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addYears(int amount);

  /**
   * Adds a number of months to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addMonths(int amount);

  /**
   * Adds a number of weeks to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addWeeks(int amount);

  /**
   * Adds a number of days to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addDays(int amount);

  /**
   * Adds a number of hours to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addHours(int amount);

  /**
   * Adds a number of minutes to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addMinutes(int amount);

  /**
   * Adds a number of seconds to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addSeconds(int amount);

  /**
   * Adds a number of milliseconds to the datable instance returning a new one.
   * The original {@code Datable} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Datable} with the amount added
   */
  T addMilliseconds(int amount);

  /**
   * Indicates is the date is different from {@link DateUtil#MINIMUM_DATE} or
   * {@link DateUtil#MAXIMUM_DATE}.
   * @return true if the date is not equal to undifined one, false otherwise.
   */
  boolean isDefined();

  /**
   * Indicates the opposite of {@link #isDefined()}
   * @return
   */
  boolean isNotDefined();
}
