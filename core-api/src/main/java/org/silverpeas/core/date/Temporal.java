/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.date;

import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.time.Duration;

import java.util.TimeZone;

/**
 * WARNING: All the deprecated classes in this package contain failure in their handling of date
 * times (bad use of Timezone, etc.)
 *
 * A temporal object is an object that represents an instant in a time line in Silverpeas. This
 * instant can be a date (day in month in year), a time or a datetime.
 * <p>
 * Date, time, and datetime are different types as they don't have any instances in common, but
 * they are part of the same types class. As Java doesn't support the class type concept from which
 * a polymorphic family of types can be modeled, the temporal class is here introduced by a Java
 * interface with a generic type. Temporal can be then considered as a type generator that satisfies
 * a bound constraint (a fix point).
 * @param <T> A temporal type this class should generate.
 * @deprecated Use the java.time API
 */
@Deprecated
public interface Temporal<T extends Temporal<? super T>> extends Cloneable {

  /**
   * The pattern for the short ISO 8601 date representation. The date is at the accuracy to the
   * minute and the time zone is specified as defined in the RFC 822 (indicated by the Z symbol).
   */
  String SHORT_ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mmZ";

  /**
   * The pattern for the ISO 8601 date representation. The date is at the accuracy to the second and
   * the time zone is specified as defined in the RFC 822 (indicated by the Z symbol).
   */
  String ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

  /**
   * The pattern for the iCal date representation in the current time zone of the temporal.
   */
  String ICAL_PATTERN = "yyyyMMdd'T'HHmmss";

  /**
   * The pattern for the iCal date representation in UTC. The UTC set of the date is indicated here
   * by the Z character.
   */
  String ICAL_UTC_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

  /**
   * Clones itself.
   * @return a clone to this temporal.
   */
  T clone();

  /**
   * Gets the java Date representation of this temporal.
   * @return a Date representation of this temporal.
   */
  java.util.Date asDate();

  /**
   * Is this temporal strictly before in time the specified another one?
   * @param anotherDatable the another temporal to which this is compared.
   * @return true if this temporal is strictly before the another one.
   */
  boolean isBefore(final T anotherDatable);

  /**
   * Is this temporal strictly after in time the specified another one?
   * @param anotherDatable the another temporal to which this is compared.
   * @return true if this temporal is strictly after the another one.
   */
  boolean isAfter(final T anotherDatable);

  /**
   * Is this temporal equal in time to the specified another one?
   * @param anotherDatable the another temporal to which this is compared.
   * @return true if this temporal is equal with the another one.
   */
  boolean isEqualTo(final T anotherDatable);

  /**
   * Sets the time zone this temporal is defined for. If no time zone is set, then the default one is
   * considered (the timezone of the host). The time zone can have no meaning with some temporal
   * types like for example simple date (day, month, and year).
   * @param timeZone the time zone of this temporal.
   * @return itself.
   */
  T inTimeZone(final TimeZone timeZone);

  /**
   * Gets the time zone for which this temporal is defined.
   * @return the time zone of this temporal.
   */
  TimeZone getTimeZone();

  /**
   * Is this temporal supports the time unit?
   * @return true if this temporal supports the time unit. Time and datetime should return true
   * here whereas date should return false.
   */
  boolean isTimeSupported();

  /**
   * Gets an ISO 8601 textual representation of this temporal by taking into account of its
   * underlying timezone. For datetime, the representation is at the accuracy to the second.
   * The ISO 8601 format in which the date is returned is one of the more common in the Web, that is
   * with the datetime separators (the hyphen for dates and the double-points for times):
   * yyyy-MM-dd'T'HH:mm:ss where yyyy means the year in 4 digits, MM means the month in year in two
   * digits, dd means the day in month in two digits, HH means the hour (24-o'clock) in two digits,
   * mm means the minute in two digits, and ss the second in two digits.
   * @return the ISO 8601 textual representation of this temporal.
   */
  String toISO8601();

  /**
   * Gets an ISO 8601 textual representation of this temporal by taking into account of its
   * underlying timezone. For datetime, the representation is at the accuracy to the minute.
   * The ISO 8601 format in which the date is returned is one of the more common in the Web, that is
   * with the datetime separators (the hyphen for dates and the double-points for times):
   * yyyy-MM-dd'T'HH:mm:ss where yyyy means the year in 4 digits, MM means the month in year in two
   * digits, dd means the day in month in two digits, HH means the hour (24-o'clock) in two digits,
   * and mm means the minute in two digits.
   * @return a short ISO 8601 textual representation of this temporal.
   */
  String toShortISO8601();

  /**
   * Gets the ISO 8601 textual representation of this date as it is in the iCal specification. The
   * returned iCal date representation is in this temporal's time zone. In the iCal specification,
   * dates are represented in one of the ISO 8601 format, that is the complete format without any
   * datetime separators (the hyphen character for dates and the double-points for times). In
   * the iCal specification, no time zone information is added to the ISO 8601 representation of the
   * date, nevertheless when the date is indicated in UTC, the 'Z' marker must be set in the date as
   * required by the ISO 8601 standard.
   * @return the iCal textual representation of this temporal and in the timezone of this temporal.
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
   * @return the iCal textual representation of this temporal and in UTC.
   */
  String toICalInUTC();

  /**
   * Computes first hour, minute, second, millisecond from the temporal instance.
   * @return a date at last hour, minute, second and millisecond of the temporal instance.
   */
  T getBeginOfDay();

  /**
   * Computes first hour, minute, second, millisecond from the temporal instance.
   * @return a date at last hour, minute, second and millisecond of the temporal instance.
   */
  T getEndOfDay();

  /**
   * Compute the first hour, minute, second, millisecond from the temporal instance.
   * @return a date at last hour, minute, second and millisecond of the temporal instance.
   */
  T getBeginOfWeek();

  /**
   * Compute the date of the first day in the week from the temporal instance.
   * @return a date for the first day of the week of the temporal instance.
   */
  T getEndOfWeek();

  /**
   * Compute the first hour, minute, second, millisecond from the temporal instance and a given
   * locale.
   * @param locale the locale
   * @return a date at last hour, minute, second and millisecond of the temporal instance.
   */
  T getBeginOfWeek(String locale);

  /**
   * Compute the date of the first day in the week from the temporal instance and a given locale.
   * @param locale the locale
   * @return a date for the first day of the week of the temporal instance.
   */
  T getEndOfWeek(String locale);

  /**
   * Compute the first hour, minute, second, millisecond from the temporal instance.
   * @return a date at last hour, minute, second and millisecond of the temporal instance.
   */
  T getBeginOfMonth();

  /**
   * Compute the date of the first day in the month from the temporal instance.
   * @return a date for the first day of the month of the temporal instance.
   */
  T getEndOfMonth();

  /**
   * Compute the first hour, minute, second, millisecond from the temporal instance.
   * @return a date at last hour, minute, second and millisecond of the temporal instance.
   */
  T getBeginOfYear();

  /**
   * Compute the date of the first day in the year from the temporal instance.
   * @return a date for the first day of the year of the temporal instance.
   */
  T getEndOfYear();

  /**
   * Compute the time between the temporal instance and another one.
   * @param anotherDatable another date
   * @return the time between the temporal instance and another one represented by {@link Duration}.
   */
  Duration getTimeDataTo(T anotherDatable);

  /**
   * Adds a number of years to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addYears(int amount);

  /**
   * Adds a number of months to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addMonths(int amount);

  /**
   * Adds a number of weeks to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addWeeks(int amount);

  /**
   * Adds a number of days to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addDays(int amount);

  /**
   * Adds a number of hours to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addHours(int amount);

  /**
   * Adds a number of minutes to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addMinutes(int amount);

  /**
   * Adds a number of seconds to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addSeconds(int amount);

  /**
   * Adds a number of milliseconds to the temporal instance returning a new one.
   * The original {@code Temporal} is unchanged.
   *
   * @param amount  the amount to add, may be negative
   * @return the new {@code Temporal} with the amount added
   */
  T addMilliseconds(int amount);

  /**
   * Indicates is the date is different from {@link DateUtil#MINIMUM_DATE} or
   * {@link DateUtil#MAXIMUM_DATE}.
   * @return true if the date is not equal to undefined one, false otherwise.
   */
  boolean isDefined();

  /**
   * Indicates the opposite of {@link #isDefined()}
   * @return true if the date is undefined, false otherwise.
   */
  boolean isNotDefined();
}
