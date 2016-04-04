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

package org.silverpeas.core.util;

import org.silverpeas.core.date.Datable;
import org.silverpeas.core.date.DateTime;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.silverpeas.core.util.time.TimeUnit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static java.util.Calendar.*;

/**
 * DateUtil is an helper class for date manipulation.
 * @author squere
 */
public class DateUtil {
  public static final Date MINIMUM_DATE = java.sql.Date.valueOf("1900-01-01");
  public static final Date MAXIMUM_DATE = java.sql.Date.valueOf("2999-12-31");

  private static final long millisPerHour = 60l * 60l * 1000l;
  private static final long millisPerMinute = 60l * 1000l;
  private static Map<String, FastDateFormat> outputFormatters = new HashMap<>(5);
  private static Map<String, SimpleDateFormat> inputParsers = new HashMap<>(5);
  /**
   * Format and parse dates.
   */
  public static final SimpleDateFormat DATE_PARSER;
  public static final FastDateFormat DATE_FORMATTER;
  public static final SimpleDateFormat DATETIME_PARSER;
  public static final FastDateFormat DATETIME_FORMATTER;
  public static final FastDateFormat ISO8601DATE_FORMATTER;
  public static final FastDateFormat ISO8601DAY_FORMATTER;
  public static final FastDateFormat ICALDAY_FORMATTER;
  public static final FastDateFormat ICALDATE_FORMATTER;
  public static final FastDateFormat ICALUTCDATE_FORMATTER;
  public static final FastDateFormat ISO8601_FORMATTER;
  /**
   * Format and parse dates.
   */
  public static final SimpleDateFormat TIME_PARSER;
  public static final FastDateFormat TIME_FORMATTER;

  static {
    DATE_PARSER = new SimpleDateFormat("yyyy/MM/dd");
    DATE_PARSER.setLenient(false);
    DATE_FORMATTER = FastDateFormat.getInstance("yyyy/MM/dd");
    DATETIME_PARSER = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    DATETIME_FORMATTER = FastDateFormat.getInstance("yyyy/MM/dd HH:mm");
    TIME_PARSER = new SimpleDateFormat("HH:mm");
    TIME_PARSER.setLenient(false);
    TIME_FORMATTER = FastDateFormat.getInstance("HH:mm");
    ISO8601DATE_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm");
    ISO8601DAY_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd");
    ICALDAY_FORMATTER = FastDateFormat.getInstance("yyyyMMdd");
    ICALDATE_FORMATTER = FastDateFormat.getInstance("yyyyMMdd'T'HHmmss");
    ICALUTCDATE_FORMATTER =
        FastDateFormat.getInstance("yyyyMMdd'T'HHmmss'Z'", TimeZone.getTimeZone("UTC"));
    ISO8601_FORMATTER =
        FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"));
  }

  /**
   * Display the date in a language specific standard format.
   * @param date the date to convert.
   * @param language The current user's language
   * @return A String representation of the date in the language specific format.
   */
  public static String dateToString(Date date, String language) {
    if (isNotDefined(date)) {
      return "";
    }
    FastDateFormat format = getDateOutputFormat(language);
    return format.format(date);
  }

  public static String getOutputDate(Date date, String language) {
    if (isNotDefined(date)) {
      return "";
    }
    FastDateFormat formatter = getOutputFormatter(language);
    return formatter.format(date);
  }

  public static String getOutputDate(String dateDB, String language) throws ParseException {
    if (!StringUtil.isDefined(dateDB)) {
      return "";
    }
    Date date = parse(dateDB);
    return getOutputDate(date, language);
  }

  public static String getOutputHour(Date date, String language) {
    if (isNotDefined(date)) {
      return "";
    }
    FastDateFormat formatter = getHourOutputFormat(language);
    return formatter.format(date);
  }

  public static String getOutputHour(String dateDB, String language) throws ParseException {
    if (!StringUtil.isDefined(dateDB)) {
      return "";
    }
    Date date = parse(dateDB);
    return getOutputHour(date, language);
  }

  public static String getOutputDateAndHour(String dateDB, String language) throws ParseException {
    if (!StringUtil.isDefined(dateDB)) {
      return "";
    }
    Date date = parseDateTime(dateDB);
    return getOutputDateAndHour(date, language);
  }

  public static String getOutputDateAndHour(Date date, String language) {
    if (isNotDefined(date)) {
      return "";
    }
    if (DateUtils.getFragmentInMilliseconds(date, Calendar.DAY_OF_MONTH) == 0L) {
      // this case is useful on data recovery
      // avoiding to display an useless information about hour (00:00) when given date have an hour
      // like 0:00:00.000
      return getOutputDate(date, language);
    }
    FastDateFormat formatter = FastDateFormat.getInstance(
        getLocalizedProperties(language).getString("dateOutputFormat") + " " + getLocalizedProperties(
            language).getString(
        "hourOutputFormat"));
    return formatter.format(date);
  }

  public static String getInputDate(Date date, String language) {
    if (isNotDefined(date)) {
      return "";
    }
    SimpleDateFormat parser = getInputFormatter(language);
    synchronized (parser) {
      return parser.format(date);
    }
  }

  private static boolean isNotDefined(Date date) {
    return (date == null) || new org.silverpeas.core.date.Date(date).isNotDefined();
  }

  public static String getInputDate(String dateDB, String language) throws ParseException {
    if (!StringUtil.isDefined(dateDB)) {
      return "";
    }
    Date date = parse(dateDB);
    return getInputDate(date, language);
  }

  /**
   * Parse the date in a language specific standard format.
   * @param string The String to convert in Date
   * @param language The current user's language
   * @return A Date representation of the String in the language specific format.
   * @throws java.text.ParseException if the input String is null, empty, or just in an incorrect
   * format.
   */
  public static Date stringToDate(String string, String language) throws ParseException {
    SimpleDateFormat format = getDateInputFormat(language);
    try {
      return format.parse(string);
    } catch (ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  public static Date stringToDate(String date, String hour, String language) throws ParseException {
    try {
      SimpleDateFormat format;
      if (hour == null || "".equals(hour.trim())) {
        format = getDateInputFormat(language);
        return format.parse(date);
      }
      format = getDateAndHourInputFormat(language);
      return format.parse(date + " " + hour);
    } catch (Exception e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  /**
   * Gets the current date and hour.
   * @return
   */
  public static Date getNow() {
    return Calendar.getInstance().getTime();
  }

  /**
   * Gets the current date with reseted hour.
   * @return
   */
  public static Date getDate() {
    return resetHour(getNow());
  }

  public static Date getDate(Date date, String hour) {
    if (date == null) {
      return null;
    }
    Calendar calendar = getInstance();
    calendar.setTime(date);
    calendar.set(HOUR_OF_DAY, extractHour(hour));
    calendar.set(MINUTE, extractMinutes(hour));
    return calendar.getTime();

  }

  public static int extractHour(String hour) {
    if (!StringUtil.isDefined(hour)) {
      return 0;
    }
    if (hour.indexOf(':') != -1) {
      return Integer.parseInt(hour.substring(0, hour.indexOf(':')));
    } else if (hour.indexOf('h') != -1) {
      return Integer.parseInt(hour.substring(0, hour.indexOf('h')));
    } else {
      return 0;
    }
  }

  public static int extractMinutes(String hour) {
    if (!StringUtil.isDefined(hour)) {
      return 0;
    }
    if (hour.indexOf(':') != -1) {
      return Integer.parseInt(hour.substring(hour.indexOf(':') + 1));
    } else if (hour.indexOf('h') != -1) {
      return Integer.parseInt(hour.substring(hour.indexOf('h') + 1));
    } else {
      return 0;
    }
  }

  /**
   * Get the date language specific standard output format.
   * @param lang The current user's language
   * @return A SimpleDateFormat initialized with the language specific output format.
   */
  public static FastDateFormat getDateOutputFormat(String lang) {
    return FastDateFormat.getInstance(getLocalizedProperties(lang).getString("dateOutputFormat"));
  }

  /**
   * Get the date language specific standard input format.
   * @param language The current user's language
   * @return A SimpleDateFormat initialized with the language specific input format.
   */
  public static SimpleDateFormat getDateInputFormat(String language) {
    return new SimpleDateFormat(getLocalizedProperties(language).getString("dateInputFormat"));
  }

  /**
   * Get the hour (from a date) language specific standard output format.
   * @param lang The current user's language
   * @return A SimpleDateFormat initialized with the language specific output format.
   */
  public static FastDateFormat getHourOutputFormat(String lang) {
    return FastDateFormat.getInstance(getLocalizedProperties(lang).getString("hourOutputFormat"));
  }

  /**
   * Get the hour (from a date) language specific standard input format.
   * @param language The current user's language
   * @return A SimpleDateFormat initialized with the language specific input format.
   */
  public static SimpleDateFormat getHourInputFormat(String language) {
    return new SimpleDateFormat(getLocalizedProperties(language).getString("hourInputFormat"));
  }

  /**
   * Get the date language specific standard input format.
   * @param lang The current user's language
   * @return A SimpleDateFormat initialized with the language specific input format.
   */
  public static SimpleDateFormat getDateAndHourInputFormat(String lang) {
    return new SimpleDateFormat(getLocalizedProperties(lang).getString("dateInputFormat") + " " +
        getLocalizedProperties(lang).getString("hourOutputFormat"));
  }

  /**
   * Get all the localized date's properties. These properties contain day and month labels,
   * common date format, and week first day.
   * @param locale the locale to use.
   * @return The localization bundle containing all date localized date's properties.
   * @author squere
   */
  public static LocalizationBundle getLocalizedProperties(String locale) {
    return ResourceLocator.getLocalizationBundle("org.silverpeas.util.date.multilang.date",
        locale);
  }

  /**
   * Parse a special String into a Date.
   * @param date (String) the format of this date must be yyyy/MM/dd
   * @return a java object Date
   * @throws ParseException
   */
  public static Date parse(String date) throws ParseException {
    synchronized (DATE_PARSER) {
      return DATE_PARSER.parse(date);
    }
  }

  /**
   * Parse a special String into a Date.
   * @param date (String) the format of this date must be yyyy/MM/dd
   * @param format (String) the whished format in according to the date parameter
   * @return a java object Date
   * @throws ParseException
   */
  public static Date parse(String date, String format) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.parse(date);
  }

  /**
   * Test if two dates are equal
   * @param date1 first date
   * @param date2 second date
   * @return true if both dates defined the same date
   */
  public static boolean datesAreEqual(Date date1, Date date2) {
    return compareTo(date1, date2) == 0;
  }

  /**
   * Compare 2 dates on year/month/day only.
   * @param date1
   * @param date2
   * @return int
   */
  public static int compareTo(Date date1, Date date2) {
    return compareTo(date1, date2, true);
  }

  /**
   * Compare 2 dates.
   * @param date1
   * @param aDate2
   * @return int
   */
  public static int compareTo(Date date1, Date aDate2, boolean aForceResetHour) {
    Calendar myCal1 = DateUtil.convert(date1);
    if (aForceResetHour) {
      resetHour(myCal1);
    }

    Calendar myCal2 = DateUtil.convert(aDate2);
    if (aForceResetHour) {
      resetHour(myCal2);
    }

    return myCal1.getTime().compareTo(myCal2.getTime());
  }

  public static String today2SQLDate() {
    return date2SQLDate(new Date());
  }

  /**
   * @param date the date to transform
   * @return a String representing the given date in a yyyy/MM/dd format or null if given date is
   *         null
   */
  public static String date2SQLDate(Date date) {
    if (date == null) {
      return null;
    }
    return DATE_FORMATTER.format(date);
  }

  public static String date2SQLDate(String date, String language) throws ParseException {
    String result = null;
    Date oDate = null;
    if (StringUtil.isDefined(date)) {
      oDate = stringToDate(date, language);
    }
    if (oDate != null) {
      result = DateUtil.date2SQLDate(oDate);
    }
    return result;
  }

  /**
   * Returns the first date of month according to given date (ex. 26/08/2010)
   * @param date any date
   * @return first date of month in sql format (ie. 2010/08/01)
   */
  public static String firstDayOfMonth2SQLDate(Date date) {
    String sqlDate = DATE_FORMATTER.format(date);
    sqlDate = sqlDate.substring(0, sqlDate.length() - 2) + "01";
    return sqlDate;
  }

  public static String formatDuration(long duration) {
    long hourDuration = duration / millisPerHour;
    long minuteDuration = (duration % millisPerHour) / millisPerMinute;
    long secondDuration = ((duration % millisPerHour) % millisPerMinute) / 1000;
    StringBuilder result = new StringBuilder(10);
    if (hourDuration > 0) {
      if (hourDuration < 10) {
        result.append('0');
      }
      result.append(hourDuration).append('h');
      if (minuteDuration < 10) {
        result.append('0');
      }
      result.append(minuteDuration).append('m');
    } else if (hourDuration <= 0 && minuteDuration > 0) {
      result.append(minuteDuration).append('m');
    }
    if (result.length() > 0 && secondDuration < 10) {
      result.append('0');
    }
    return result.append(secondDuration).append('s').toString();
  }

  private static FastDateFormat getOutputFormatter(String language) {
    FastDateFormat formatter = outputFormatters.get(language);
    if (formatter == null) {
      formatter = getDateOutputFormat(language);
      outputFormatters.put(language, formatter);
    }
    return formatter;
  }

  private static SimpleDateFormat getInputFormatter(String language) {
    SimpleDateFormat formatter = inputParsers.get(language);
    if (formatter == null) {
      formatter = getDateInputFormat(language);
      inputParsers.put(language, formatter);
    }
    return formatter;
  }

  public static String getFormattedTime(Date date) {
    String time = formatTime(date);
    return time;
  }

  /**
   * Parse a String of format yyyy/MM/dd and return the corresponding Date.
   * @param date the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
   */
  public static Date parseDate(String date) throws ParseException {
    if (date == null) {
      return null;
    }
    Calendar result = getInstance();
    synchronized (DATE_PARSER) {
      result.setTime(DATE_PARSER.parse(date));
    }
    return resetHour(result).getTime();
  }

  /**
   * Reset hour of a date (00:00:00.000)
   * @param date
   * @return
   */
  public static Calendar resetHour(Calendar date) {
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);
    return date;
  }

  /**
   * Reset hour of a date (00:00:00.000)
   * @param date
   * @return
   */
  public static Date resetHour(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Parse a String of format yyyy/MM/dd hh:mm and return the corresponding Date.
   * @param date the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
   */
  public static Date parseDateTime(String date) throws ParseException {
    if (date == null) {
      return null;
    }
    Calendar result = getInstance();
    synchronized (DATETIME_PARSER) {
      result.setTime(DATETIME_PARSER.parse(date));
    }
    return result.getTime();
  }

  /**
   * Parse a String of format yyyy/MM/dd hh:mm and return the corresponding Date.
   * @param time the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
   */
  public static Date parseTime(String time) throws ParseException {
    if (time == null) {
      return null;
    }
    Calendar result = getInstance();
    synchronized (TIME_PARSER) {
      result.setTime(TIME_PARSER.parse(time));
    }
    return result.getTime();
  }

  /**
   * Parse a String of format yyyy/MM/dd and return the corresponding Date.
   * @param date the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
   */
  public static Calendar parseCalendar(String date) throws ParseException {
    if (date == null) {
      return null;
    }
    Calendar result = getInstance();
    synchronized (DATE_PARSER) {
      result.setTime(DATE_PARSER.parse(date));
    }
    return resetHour(result);
  }

  /**
   * Format a Date to a String of format yyyy/MM/dd.
   * @param date the date to be formatted.
   * @return the formatted String.
   */
  public static String formatDate(Date date) {
    if (date == null) {
      return null;
    }
    return DATE_FORMATTER.format(date);
  }

  /**
   * Format a Calendar to a String of format yyyy/MM/dd.
   * @param calend the date to be formatted.
   * @return the formatted String.
   */
  public static String formatDate(Calendar calend) {
    if (calend == null) {
      return null;
    }
    return DATE_FORMATTER.format(calend.getTime());
  }

  /**
   * Formats the specified date according to the specified date pattern.
   * @param date the date to format into a string.
   * @param pattern the pattern to apply in the format.
   * @return a string representation of the specified date and in the specified pattern.
   */
  public static String formatDate(final Date date, String pattern) {
    SimpleDateFormat formater = new SimpleDateFormat(pattern);
    return formater.format(date);
  }

  /**
   * Parse a String of format HH:mm and set the corresponding hours and minutes to the specified
   * Calendar.
   * @param time the String to be parsed.
   * @param calend the calendar to be updated.
   */
  public static void setTime(Calendar calend, String time) {
    calend.set(SECOND, 0);
    calend.set(MILLISECOND, 0);
    if (time != null) {
      try {
        Calendar result = Calendar.getInstance();
        synchronized (TIME_PARSER) {
          result.setTime(TIME_PARSER.parse(time));
        }
        calend.set(HOUR_OF_DAY, result.get(HOUR_OF_DAY));
        calend.set(MINUTE, result.get(MINUTE));
        return;
      } catch (ParseException pex) {
      }
    }
    calend.set(HOUR_OF_DAY, 0);
    calend.set(MINUTE, 0);
  }

  /**
   * Format a Date to a String of format HH:mm.
   * @param date the date to be formatted.
   * @return the formatted String.
   */
  public static String formatTime(Date date) {
    if (date == null) {
      return null;
    }
    return TIME_FORMATTER.format(date);
  }

  /**
   * Format a Calendar to a String of format HH:mm.
   * @param calend the date to be formatted.
   * @return the formatted String.
   */
  public static String formatTime(Calendar calend) {
    if (calend == null) {
      return null;
    }
    return TIME_FORMATTER.format(calend.getTime());
  }

  /**
   * Formats the specified date according to the ISO 8601 format.
   * @param date the date to format.
   * @return a String representation of the date in one of the ISO 8601 format (down to the minute
   *         and without the UTC offset).
   */
  public static String formatAsISO8601Date(final Date date) {
    return ISO8601DATE_FORMATTER.format(date);
  }

  /**
   * Formats the specified date according to the short ISO 8601 format (only the day date is
   * rendered).
   * @param date the date to format.
   * @return a String representation of the date in one of the short ISO 8601 format (yyyy-MM-dd).
   */
  public static String formatAsISO8601Day(final Date date) {
    return ISO8601DAY_FORMATTER.format(date);
  }

  /**
   * Formats the specified date according to the ISO 8601 format of the iCal format (in the
   * timezone
   * of the date).
   * @param date the date to format.
   * @return a String representation of the date the ISO 8601 format of the iCal format (down to
   *         the
   *         second).
   */
  public static String formatAsICalDate(final Date date) {
    return ICALDATE_FORMATTER.format(date);
  }

  /**
   * Formats the specified date according to the ISO 8601 format of the iCal format (in UTC).
   * @param date the date to format.
   * @return a String representation of the date the ISO 8601 format of the iCal format (down to
   *         the
   *         second in UTC).
   */
  public static String formatAsICalUTCDate(final Date date) {
    return ICALUTCDATE_FORMATTER.format(date);
  }

  /**
   * Formats the specified date according to the short ISO 8601 format (only the day date is
   * rendered) used in the iCal format.
   * @param date the date to format.
   * @return a String representation of the date in one of the short ISO 8601 format (yyyyMMdd).
   */
  public static String formatAsICalDay(final Date date) {
    return ICALDAY_FORMATTER.format(date);
  }

  /**
   * Parses the specified ISO 8601 formatted date and returns it as a Date instance.
   * @param date the date to parse (must satisfy one of the following pattern yyyy-MM-ddTHH:mm or
   * yyyy-MM-dd).
   * @return a date object, resulting of the parsing.
   * @throws ParseException if the specified date is not in the one of the expected formats.
   */
  public static Date parseISO8601Date(final String date) throws ParseException {
    return DateUtils
        .parseDate(date, ISO8601DATE_FORMATTER.getPattern(), ISO8601DAY_FORMATTER.getPattern());
  }

  /**
   * Converts the specified date as a Datable object with the time set or not.
   * @param aDate a Java date to convert.
   * @param withTime the time in the Java date has to be taken into account.
   * @return a Datable object.
   */
  public static Datable<?> asDatable(final java.util.Date aDate, boolean withTime) {
    Datable<?> datable;
    if (withTime) {
      datable = new DateTime(aDate);
    } else {
      datable = new org.silverpeas.core.date.Date(aDate);
    }
    return datable;
  }

  /**
   * Compute the date of the first day in the year of the specified date.
   * @param date the specified date.
   * @return a date for the first day of the year of the specified date.
   */
  public static Date getFirstDateOfYear(Date date) {
    Calendar calendar = getInstance();
    calendar.setTime(date);
    calendar.set(MONTH, Calendar.JANUARY);
    calendar.set(DAY_OF_MONTH, 1);
    calendar.set(HOUR_OF_DAY, 0);
    calendar.set(MINUTE, 0);
    calendar.set(SECOND, 0);
    calendar.set(MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Compute the date of the last day in the year of the specified date.
   * @param date the specified date.
   * @return a date for the last day of the year of the specified date.
   */
  public static Date getEndDateOfYear(Date date) {
    Calendar calendar = getInstance();
    calendar.setTime(date);
    calendar.set(MONTH, Calendar.DECEMBER);
    calendar.set(HOUR_OF_DAY, 23);
    calendar.set(MINUTE, 59);
    calendar.set(SECOND, 59);
    calendar.set(MILLISECOND, 999);
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1);
    calendar.add(Calendar.DATE, -1);
    return calendar.getTime();
  }

  /**
   * Compute the date of the first day in the month of the specified date.
   * @param date the specified date.
   * @return a date for the first day of the month of the specified date.
   */
  public static Date getFirstDateOfMonth(Date date) {
    Calendar calendar = getInstance();
    calendar.setTime(date);
    calendar.set(DAY_OF_MONTH, 1);
    calendar.set(HOUR_OF_DAY, 0);
    calendar.set(MINUTE, 0);
    calendar.set(SECOND, 0);
    calendar.set(MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Compute the date of the last day in the month of the specified date.
   * @param date the specified date.
   * @return a date for the last day of the month of the specified date.
   */
  public static Date getEndDateOfMonth(Date date) {
    Calendar calendar = getInstance();
    calendar.setTime(date);
    calendar.set(HOUR_OF_DAY, 23);
    calendar.set(MINUTE, 59);
    calendar.set(SECOND, 59);
    calendar.set(MILLISECOND, 999);
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1);
    calendar.add(Calendar.DATE, -1);
    return calendar.getTime();
  }

  /**
   * Compute the date of the first day in the week of the specified date.
   * @param date the specified date.
   * @param language the locale to take into account (fr for the french locale (fr_FR) for
   * example).
   * @return a date for the first day of the week of the specified date.
   */
  public static Date getFirstDateOfWeek(Date date, String language) {
    Locale locale = getLocale(language);
    Calendar calendar = (locale != null ? getInstance(locale) : getInstance());
    calendar.setTime(date);
    calendar.set(DAY_OF_WEEK, getFirstDayOfWeek(language));
    calendar.set(HOUR_OF_DAY, 0);
    calendar.set(MINUTE, 0);
    calendar.set(SECOND, 0);
    calendar.set(MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Compute the date of the last day in the week of the specified date.
   * @param date the specified date.
   * @param language the locale to take into account (fr for the french locale (fr_FR) for
   * example).
   * @return a date for the last day of the week of the specified date.
   */
  public static Date getEndDateOfWeek(Date date, String language) {
    Locale locale = getLocale(language);
    Calendar calendar = (locale != null ? getInstance(locale) : getInstance());
    calendar.setTime(date);
    calendar.set(DAY_OF_WEEK, getFirstDayOfWeek(language));
    calendar.set(HOUR_OF_DAY, 23);
    calendar.set(MINUTE, 59);
    calendar.set(SECOND, 59);
    calendar.set(MILLISECOND, 999);
    calendar.add(Calendar.DAY_OF_MONTH, 6);
    return calendar.getTime();
  }

  /**
   * Sets the locale of this calendar view. According to the locale, some calendar properties will
   * be set (for example, the first day of the week).
   * @param locale the locale to take into account (fr for the french locale (fr_FR) for example).
   */
  private static Locale getLocale(final String locale) {
    if (StringUtil.isDefined(locale)) {
      return new Locale(locale);
    }
    return null;
  }

  /**
   * Gets the first day of weeks of the calendar with 1 meaning for sunday, 2 meaning for monday,
   * and so on. The first day of weeks depends on the locale; the first day of weeks is monday for
   * french whereas it is for sunday for US.
   * @param language the locale to take into account (fr for the french locale (fr_FR) for
   * example).
   * @return the first day of week.
   */
  public static int getFirstDayOfWeek(String language) {
    Calendar calendar;
    Locale locale = getLocale(language);
    if (locale == null) {
      calendar = Calendar.getInstance();
    } else {
      calendar = Calendar.getInstance(locale);
    }
    return calendar.getFirstDayOfWeek();
  }

  /**
   * Get last hour, minute, second, millisecond of the specified date
   * @param curDate the specified date
   * @return a date at last hour, minute, second and millisecond of the specified date
   */
  public static Date getEndOfDay(Date curDate) {
    if (curDate != null) {
      Calendar cal = getInstance();
      cal.setTime(curDate);
      cal.set(HOUR_OF_DAY, 23);
      cal.set(MINUTE, 59);
      cal.set(SECOND, 59);
      cal.set(MILLISECOND, 999);
      return cal.getTime();
    }
    return null;
  }

  /**
   * Get first hour, minute, second, millisecond of the specified date
   * @param curDate the specified date
   * @return a date at last hour, minute, second and millisecond of the specified date
   */
  public static Date getBeginOfDay(Date curDate) {
    if (curDate != null) {
      Calendar cal = getInstance();
      cal.setTime(curDate);
      cal.set(HOUR_OF_DAY, 0);
      cal.set(MINUTE, 0);
      cal.set(SECOND, 0);
      cal.set(MILLISECOND, 0);
      return cal.getTime();
    }
    return null;
  }

  /**
   * Set the first hour, minute, second, millisecond of the specified calendar to 0.
   * @param calendar the specified calendar.
   */
  public static void setAtBeginOfDay(Calendar calendar) {
    if (calendar != null) {
      calendar.set(HOUR_OF_DAY, 0);
      calendar.set(MINUTE, 0);
      calendar.set(SECOND, 0);
      calendar.set(MILLISECOND, 0);
    }
  }

  /**
   * Compute a new date by adding the specified number of days without couting week-ends.
   * @param calendar
   * @param nbDay
   */
  public static void addDaysExceptWeekEnds(Calendar calendar, int nbDay) {
    int nb = 0;
    while (nb < nbDay) {
      calendar.add(DATE, 1);
      if (calendar.get(DAY_OF_WEEK) != SATURDAY && calendar.get(DAY_OF_WEEK) != SUNDAY) {
        nb += 1;
      }
    }
  }

  /**
   * Return the day index of the week from a given date.
   * This index is that of Calendar monday, tuesday, ...
   * Use Calendar.MONDAY, Calendar.TUESDAY, ...
   * @param curDate
   * @return String
   */
  public static int getDayNumberInWeek(Date curDate) {
    return DateUtil.convert(curDate).get(DAY_OF_WEEK);
  }

  /**
   * Get the number of days between two dates.
   * @param date1
   * @param date2
   * @return int
   */
  public static int getDayNumberBetween(Date date1, Date date2) {
    return UnitUtil.getTimeData(date2.getTime() - date1.getTime()).getTimeConverted(TimeUnit.DAY)
        .intValue();
  }

  /**
   * Convert Date to Calendar
   * @param curDate
   * @return Calendar
   */
  public static Calendar convert(Date curDate) {
    return convert(curDate, null);
  }

  /**
   * Convert Date to Calendar
   * @param curDate
   * @param language the locale to take into account (fr for the french locale (fr_FR) for
   * example).
   * @return Calendar
   */
  public static Calendar convert(Date curDate, String language) {
    Locale locale = getLocale(language);
    Calendar cal = (locale != null ? getInstance(locale) : getInstance());
    cal.setTime(curDate);
    return cal;
  }

  private DateUtil() {
  }
}
