/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * DateUtil is an helper class for date manipulation.
 * @author squere
 */
public class DateUtil {

  private static final long millisPerHour = (long) 60 * (long) 60 * (long) 1000;
  private static final long millisPerMinute = (long) 60 * (long) 1000;
  private static Map<String, FastDateFormat> outputFormatters =
      new HashMap<String, FastDateFormat>();
  private static Map<String, SimpleDateFormat> inputParsers =
      new HashMap<String, SimpleDateFormat>();
  /**
   * Format and parse dates.
   */
  public static final SimpleDateFormat DATE_PARSER;
  public static final FastDateFormat DATE_FORMATTER;
  public static final SimpleDateFormat DATETIME_PARSER;
  public static final FastDateFormat DATETIME_FORMATTER;
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
  }

  /**
   * Display the date in a language specific standard format.
   * @parameter date The date to convert
   * @parameter language The current user's language
   * @return A String representation of the date in the language specific format.
   */
  public static String dateToString(Date date, String language) {
    if (date == null) {
      return "";
    }
    FastDateFormat format = getDateOutputFormat(language);
    return format.format(date);
  }

  public static String getOutputDate(Date date, String language) {
    String result = "";
    if (date == null) {
      return result;
    }
    FastDateFormat formatter = getOutputFormatter(language);
    return formatter.format(date);
  }

  public static String getOutputDate(String dateDB, String language)
      throws ParseException {
    if (StringUtil.isDefined(dateDB)) {
      return "";
    }
    Date date = parse(dateDB);
    return getOutputDate(date, language);
  }

  public static String getOutputDateAndHour(String dateDB, String language) throws ParseException {
    if (StringUtil.isDefined(dateDB)) {
      return "";
    }
    Date date = parseDateTime(dateDB);
    return getOutputDateAndHour(date, language);
  }

  public static String getOutputDateAndHour(Date date, String language) {
    if (date == null) {
      return "";
    }
    FastDateFormat formatter = FastDateFormat.getInstance(getMultilangProperties(
        language).getString("dateOutputFormat")
        + " " + getMultilangProperties(language).getString("hourOutputFormat"));
    return formatter.format(date);
  }

  public static String getInputDate(Date date, String language) {
    if (date == null) {
      return "";
    }
    SimpleDateFormat parser = getInputFormatter(language);
    synchronized (parser) {
      return parser.format(date);
    }
  }

  public static String getInputDate(String dateDB, String language)
      throws ParseException {
    if (dateDB == null || "".equals(dateDB) || "null".equals(dateDB)) {
      return "";
    } else {
      Date date = parse(dateDB);
      return getInputDate(date, language);
    }
  }

  /**
   * Parse the date in a language specific standard format.
   * @parameter string The String to convert in Date
   * @parameter language The current user's language
   * @throws java.text.ParseException if the input String is null, empty, or just in an incorrect
   * format.
   * @return A Date representation of the String in the language specific format.
   */
  public static Date stringToDate(String string, String language)
      throws ParseException {
    SimpleDateFormat format = getDateInputFormat(language);

    try {
      return format.parse(string);
    } catch (ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  public static Date stringToDate(String date, String hour, String language)
      throws ParseException {
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

  public static Date getDate(Date date, String hour) {
    if (date == null) {
      return null;
    }

    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, extractHour(hour));
    calendar.set(Calendar.MINUTE, extractMinutes(hour));

    return calendar.getTime();

  }

  public static int extractHour(String hour) {
    if (!StringUtil.isDefined(hour)) {
      return 0;
    }

    if (hour.indexOf(":") != -1) {
      return Integer.parseInt(hour.substring(0, hour.indexOf(":")));
    } else if (hour.indexOf("h") != -1) {
      return Integer.parseInt(hour.substring(0, hour.indexOf("h")));
    } else {
      return 0;
    }
  }

  public static int extractMinutes(String hour) {
    if (!StringUtil.isDefined(hour)) {
      return 0;
    }

    if (hour.indexOf(":") != -1) {
      return Integer.parseInt(hour.substring(hour.indexOf(":") + 1));
    } else if (hour.indexOf("h") != -1) {
      return Integer.parseInt(hour.substring(hour.indexOf("h") + 1));
    } else {
      return 0;
    }
  }

  /**
   * Get the date language specific standard output format.
   * @parameter lang The current user's language
   * @return A SimpleDateFormat initialized with the language specific output format.
   */
  public static FastDateFormat getDateOutputFormat(String lang) {
    return FastDateFormat.getInstance(getMultilangProperties(lang).getString("dateOutputFormat"));
  }

  /**
   * Get the date language specific standard input format.
   * @parameter language The current user's language
   * @return A SimpleDateFormat initialized with the language specific input format.
   */
  public static SimpleDateFormat getDateInputFormat(String language) {
    return new SimpleDateFormat(getMultilangProperties(language).getString("dateInputFormat"));
  }

  /**
   * Get the date language specific standard input format.
   * @parameter lang The current user's language
   * @return A SimpleDateFormat initialized with the language specific input format.
   */
  public static SimpleDateFormat getDateAndHourInputFormat(String lang) {
    return new SimpleDateFormat(getMultilangProperties(lang).getString("dateInputFormat") + " "
        + getMultilangProperties(lang).getString("hourOutputFormat"));
  }

  /**
   * Get all date multilang properties. This properties contains day and month labels, common date
   * format, and week first day.
   * @param language The current user's language.
   * @return The ResourceLocator containing all date multilang properties
   * @author squere
   */
  public static ResourceLocator getMultilangProperties(String language) {
    return new ResourceLocator(
        "com.stratelia.webactiv.util.date.multilang.date", language);
  }

  /**
   * Parse a special String into a Date.
   * @param date (String) the format of this date must be yyyy/MM/dd
   * @return a java object Date
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
    Calendar cDate1 = Calendar.getInstance();
    cDate1.setTime(date1);
    Calendar cDate2 = Calendar.getInstance();
    cDate2.setTime(date2);
    if (cDate1.get(Calendar.YEAR) != cDate2.get(Calendar.YEAR)) {
      return false;
    } else {
      if (cDate1.get(Calendar.MONTH) != cDate2.get(Calendar.MONTH)) {
        return false;
      } else {
        if (cDate1.get(Calendar.DATE) != cDate2.get(Calendar.DATE)) {
          return false;
        }
      }
    }
    return true;
  }

  public static String today2SQLDate() {
    return date2SQLDate(new Date());
  }

  public static String date2SQLDate(Date date) {
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

  public static String formatDuration(long duration) {
    long hourDuration = duration / millisPerHour;
    long minuteDuration = (duration % millisPerHour) / millisPerMinute;
    long secondDuration = ((duration % millisPerHour) % millisPerMinute) / 1000;

    String dHour = Long.toString(hourDuration);
    String dMinute = Long.toString(minuteDuration);
    String dSecond = Long.toString(secondDuration);
    String result = "";
    if (hourDuration < 10) {
      dHour = "0" + dHour;
    }
    if (hourDuration > 0) {
      result = dHour + "h";
    }
    if (hourDuration > 0 && minuteDuration < 10) {
      dMinute = "0" + dMinute;
    }
    if (hourDuration > 0) {
      result += dMinute + "m";
    } else if (hourDuration <= 0 && minuteDuration > 0) {
      result += dMinute + "m";
    }
    if (result.length() > 0 && secondDuration < 10) {
      dSecond = "0" + dSecond;
    }
    return result += dSecond + "s";
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
    SimpleDateFormat formatter = (SimpleDateFormat) inputParsers.get(language);
    if (formatter == null) {
      formatter = getDateInputFormat(language);
      inputParsers.put(language, formatter);
    }
    return formatter;
  }

  public static String getFormattedTime(Date date) {
    String time = formatTime(date);
    SilverTrace.debug("util", "DateUtil.getFormattedTime(Date)", "Time = "
        + time);
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
    Calendar result = Calendar.getInstance();
    synchronized (DATE_PARSER) {
      result.setTime(DATE_PARSER.parse(date));
    }
    result.set(Calendar.HOUR_OF_DAY, 0);
    result.set(Calendar.MINUTE, 0);
    result.set(Calendar.SECOND, 0);
    result.set(Calendar.MILLISECOND, 0);
    return result.getTime();
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
    Calendar result = Calendar.getInstance();
    synchronized (DATETIME_PARSER) {
      result.setTime(DATETIME_PARSER.parse(date));
    }
    return result.getTime();
  }

  /**
   * Parse a String of format yyyy/MM/dd hh:mm and return the corresponding Date.
   * @param date the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
   */
  public static Date parseTime(String time) throws ParseException {
    if (time == null) {
      return null;
    }
    Calendar result = Calendar.getInstance();
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
    Calendar result = Calendar.getInstance();
    synchronized (DATE_PARSER) {
      result.setTime(DATE_PARSER.parse(date));
    }
    result.set(Calendar.HOUR_OF_DAY, 0);
    result.set(Calendar.MINUTE, 0);
    result.set(Calendar.SECOND, 0);
    result.set(Calendar.MILLISECOND, 0);
    return result;
  }

  /**
   * Format a Date to a String of format yyyy/MM/dd.
   * @param date the date to be formatted.
   * @return the formatted String.
   * @throws ParseException
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
   * @throws ParseException
   */
  public static String formatDate(Calendar calend) {
    if (calend == null) {
      return null;
    }
    return DATE_FORMATTER.format(calend.getTime());
  }

  /**
   * Parse a String of format HH:mm and set the corresponding hours and minutes to the specified
   * Calendar.
   * @param time the String to be parsed.
   * @param the calendar to be updated.
   * @throws ParseException
   */
  public static void setTime(Calendar calend, String time) {
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    if (time != null) {
      try {
        Calendar result = Calendar.getInstance();
        synchronized (TIME_PARSER) {
          result.setTime(TIME_PARSER.parse(time));
        }
        calend.set(Calendar.HOUR_OF_DAY, result.get(Calendar.HOUR_OF_DAY));
        calend.set(Calendar.MINUTE, result.get(Calendar.MINUTE));
        return;
      } catch (ParseException pex) {
      }
    }
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
  }

  /**
   * Format a Date to a String of format HH:mm.
   * @param date the date to be formatted.
   * @return the formatted String.
   * @throws ParseException
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
   * @throws ParseException
   */
  public static String formatTime(Calendar calend) {
    if (calend == null) {
      return null;
    }
    return TIME_FORMATTER.format(calend.getTime());
  }
}
