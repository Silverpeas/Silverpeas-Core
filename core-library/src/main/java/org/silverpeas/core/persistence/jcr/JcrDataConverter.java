/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jcr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.DateUtil;

/**
 * A converter of some data handled in the JCR, such as node paths and dates.
 * It converts the data from and fro the JCR world and the Silverpeas wrld.
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
  private static final String OPENING_BRACKET = "[";
  private static final String CLOSING_BRACKET = "]";

  /**
   * Encodes the JCR path to a Xpath compatible path.
   *
   * @param path the JCR path to be encoded for Xpath.
   * @return the corresponding xpath.
   */
  public static final String encodeJcrPath(String path) {
    return ISO9075.encodePath(convertToJcrPath(path));
  }

  /**
   * Replace all whitespace to SPACE_TOKEN.
   *
   * @param name the String o be converted.
   * @return the resulting String.
   */
  public static String convertToJcrPath(String name) {
    String coolName = name.replaceAll(" ", SPACE_TOKEN);
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
    return StringUtil.escapeQuote(name).replace(OPENING_BRACKET, " ").replace(CLOSING_BRACKET, " ");
  }

  /**
   * Replace all %39 with the char'
   *
   * @param text
   * @return a String with all %39 replaced by quotes
   */
  public static String unescapeQuote(String text) {
    return text.replaceAll("%" + ((int) ('\'')), "'");
  }

  /**
   * Replace all SPACE_TOKEN to whitespace.
   *
   * @param name the String o be converted.
   * @return the resulting String.
   */
  public static String convertFromJcrPath(String name) {
    return Text.unescapeIllegalJcrChars(name.replaceAll(SPACE_TOKEN, " "));
  }

  /**
   * Parse a String of format yyyy/MM/dd and return the corresponding Date.
   *
   * @param date the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
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
   * @param calend the date to be formatted.
   * @return the formatted String.
   */
  public static String formatDate(Calendar calend) {
    return DateUtil.formatDate(calend);
  }

  /**
   * Parse a String of format HH:mm and set the corresponding hours and minutes to the specified
   * Calendar.
   *
   * @param time the String to be parsed.
   * @param calend the calendar to be updated.
   */
  public static void setTime(Calendar calend, String time) {
    DateUtil.setTime(calend, time);
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
   * @param calend the date to be formatted.
   * @return the formatted String.
   */
  public static String formatTime(Calendar calend) {
    return DateUtil.formatTime(calend);
  }

  public static String formatDateForXpath(Date date) {
    return "xs:dateTime('" + getXpathFormattedDate(date) + 'T'
        + getXpathFormattedTime(date) + getTimeZone(date) + "')";
  }

  public static String formatCalendarForXpath(Calendar date) {
    return "xs:dateTime('" + getXpathFormattedDate(date.getTime()) + 'T'
        + getXpathFormattedTime(date.getTime()) + getTimeZone(date.getTime())
        + "')";
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
