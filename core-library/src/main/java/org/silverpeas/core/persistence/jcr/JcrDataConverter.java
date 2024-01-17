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
package org.silverpeas.core.persistence.jcr;

import org.apache.jackrabbit.util.Text;
import org.silverpeas.core.util.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * A converter of some data handled in the JCR, such as node paths and dates.
 * It converts the data from and for the JCR world and the Silverpeas world.
 *
 * @author Emmanuel Hugonnet
 */
public class JcrDataConverter {

  /**
   * Token used to replace space in names.
   */
  public static final String SPACE_TOKEN = "__";
  /**
   * Token used in path.
   */
  public static final String PATH_SEPARATOR = "/";
  private static final String ILLEGAL_JCR_CHARACTERS_REGEXP = "[%:\\[\\]*'\"|\t\r\n]";

  private JcrDataConverter() {

  }

  /**
   * Replace all whitespace to SPACE_TOKEN.
   *
   * @param name the String o be converted.
   * @return the resulting String.
   */
  public static String convertToJcrPath(String name) {
    String coolName = name.replace(" ", SPACE_TOKEN);
    StringBuilder buffer = new StringBuilder(coolName.length() + 10);
    StringTokenizer tokenizer = new StringTokenizer(coolName, PATH_SEPARATOR, true);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (PATH_SEPARATOR.equals(token)) {
        buffer.append(token);
      } else {
        buffer.append(Text.escapeIllegalJcrChars(token));
      }
    }
    return buffer.toString();
  }

  public static String escapeIllegalJcrChars(String name) {
    return name.replaceAll(ILLEGAL_JCR_CHARACTERS_REGEXP, " ").trim();
  }

  /**
   * Replace all SPACE_TOKEN to whitespace.
   *
   * @param name the String o be converted.
   * @return the resulting String.
   */
  public static String convertFromJcrPath(String name) {
    return Text.unescapeIllegalJcrChars(name.replace(SPACE_TOKEN, " "));
  }

  /**
   * Parses the specified text encoding a date in the format yyyy/MM/dd and returns the represented
   * date.
   *
   * @param date the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException an error if the specified text doesn't encode a date.
   */
  public static Date parseDate(String date) throws ParseException {
    return DateUtil.parse(date);
  }

  /**
   * Format a Date to a String of format yyyy/MM/dd.
   *
   * @param date the date to be formatted.
   * @return the formatted String.
   */
  public static String formatDate(Date date) {
    return DateUtil.formatDate(date);
  }

  /**
   * Format a Calendar to a String of format yyyy/MM/dd.
   *
   * @param calendar the date to be formatted.
   * @return the formatted String.
   */
  public static String formatDate(Calendar calendar) {
    return DateUtil.formatDate(calendar);
  }

  /**
   * Parse a String of format HH:mm and set the corresponding hours and minutes to the specified
   * Calendar.
   *
   * @param time the String to be parsed.
   * @param calendar the calendar to be updated.
   */
  public static void setTime(Calendar calendar, String time) {
    DateUtil.setTime(calendar, time);
  }

  /**
   * Format a Date to a String of format HH:mm.
   *
   * @param date the date to be formatted.
   * @return the formatted String.
   */
  public static String formatTime(Date date) {
    return DateUtil.formatTime(date);
  }

  /**
   * Format a Calendar to a String of format HH:mm.
   *
   * @param calendar the date to be formatted.
   * @return the formatted String.
   */
  public static String formatTime(Calendar calendar) {
    return DateUtil.formatTime(calendar);
  }

  public static String formatDateForXpath(Date date) {
    return "xs:dateTime('" + getXpathFormattedDate(date) + 'T'
        + getXpathFormattedTime(date) + getTimeZone(date) + "')";
  }

  protected static String getTimeZone(Date date) {
    DateFormat xpathTimezoneFormat = new SimpleDateFormat("Z");
    String timeZone = xpathTimezoneFormat.format(date);
    return timeZone.substring(0, timeZone.length() - 2) + ':'
        + timeZone.substring(timeZone.length() - 2);
  }

  protected static String getXpathFormattedTime(Date date) {
    DateFormat xpathTimeFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
    return xpathTimeFormatter.format(date);
  }

  protected static String getXpathFormattedDate(Date date) {
    DateFormat xpathDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    return xpathDateFormatter.format(date);
  }
}
