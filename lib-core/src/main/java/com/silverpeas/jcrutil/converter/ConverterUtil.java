package com.silverpeas.jcrutil.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;

/**
 * Utility class for path and date conversions between JCR and Slverpeas Pojo.
 * 
 * @author Emmanuel Hugonnet
 * @version $revision$
 */
public class ConverterUtil {

  /**
   * Token used to replace space in names.
   */
  public static final String SPACE_TOKEN = "__";

  /**
   * Token used in path.
   */
  public static final String PATH_SEPARATOR = "/";

  /**
   * Format and parse times.
   */
  protected static DateFormat getTimeFormat() {
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    timeFormatter.setLenient(false);
    return timeFormatter;
  }

  /**
   * Format and parse dates.
   */
  protected static DateFormat getDateFormat() {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
    dateFormatter.setLenient(false);
    return dateFormatter;
  }

  /**
   * Encodes the JCR path to a Xpath compatible path.
   * 
   * @param path
   *          the JCR path to be encoded for Xpath.
   * @return the corresponding xpath.
   */
  public static final String encodeJcrPath(String path) {
    return ISO9075.encodePath(convertToJcrPath(path));
  }

  /**
   * Replace all whitespace to SPACE_TOKEN.
   * 
   * @param name
   *          the String o be converted.
   * @return the resulting String.
   */
  public static String convertToJcrPath(String name) {
    String coolName = name.replaceAll(" ", SPACE_TOKEN);
    StringBuffer buffer = new StringBuffer(coolName.length() + 10);
    StringTokenizer tokenizer = new StringTokenizer(coolName, PATH_SEPARATOR,
        true);
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

  /**
   * Replace all "'" chars with %39
   * 
   * @param text
   * @return a String with all quotes replaced by %39
   */
  public static String escapeQuote(String text) {
    return text.replaceAll("'", "%" + ((int) ('\'')));
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
   * @param name
   *          the String o be converted.
   * @return the resulting String.
   */
  public static String convertFromJcrPath(String name) {
    return Text.unescapeIllegalJcrChars(name.replaceAll(SPACE_TOKEN, " "));
  }

  /**
   * Parse a String of format yyyy/MM/dd and return the corresponding Date.
   * 
   * @param date
   *          the String to be parsed.
   * @return the corresponding date.
   * @throws ParseException
   */
  public static Date parseDate(String date) throws ParseException {
    if (date == null) {
      return null;
    }
    Calendar result = Calendar.getInstance();
    result.setTime(getDateFormat().parse(date));
    result.set(Calendar.HOUR_OF_DAY, 0);
    result.set(Calendar.MINUTE, 0);
    result.set(Calendar.SECOND, 0);
    result.set(Calendar.MILLISECOND, 0);
    return result.getTime();
  }

  /**
   * Format a Date to a String of format yyyy/MM/dd.
   * 
   * @param date
   *          the date to be formatted.
   * @return the formatted String.
   * @throws ParseException
   */
  public static String formatDate(Date date) {
    if (date == null) {
      return null;
    }
    return getDateFormat().format(date);
  }

  /**
   * Format a Calendar to a String of format yyyy/MM/dd.
   * 
   * @param calend
   *          the date to be formatted.
   * @return the formatted String.
   * @throws ParseException
   */
  public static String formatDate(Calendar calend) {
    if (calend == null) {
      return null;
    }
    return getDateFormat().format(calend.getTime());
  }

  /**
   * Parse a String of format HH:mm and set the corresponding hours and minutes
   * to the specified Calendar.
   * 
   * @param time
   *          the String to be parsed.
   * @param the
   *          calendar to be updated.
   * @throws ParseException
   */
  public static void setTime(Calendar calend, String time) {
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    if (time != null) {
      try {
        Calendar result = Calendar.getInstance();
        result.setTime(getTimeFormat().parse(time));
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
   * 
   * @param date
   *          the date to be formatted.
   * @return the formatted String.
   * @throws ParseException
   */
  public static String formatTime(Date date) {
    if (date == null) {
      return null;
    }
    return getTimeFormat().format(date);
  }

  /**
   * Format a Calendar to a String of format HH:mm.
   * 
   * @param calend
   *          the date to be formatted.
   * @return the formatted String.
   * @throws ParseException
   */
  public static String formatTime(Calendar calend) {
    if (calend == null) {
      return null;
    }
    return getTimeFormat().format(calend.getTime());
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
