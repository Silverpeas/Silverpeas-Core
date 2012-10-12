/**
* Copyright (C) 2000 - 2011 Silverpeas
*
* This program is free software: you can redistribute it and/or modify it under the terms of the
* GNU Affero General Public License as published by the Free Software Foundation, either version 3
* of the License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
* redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
* applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
* text describing the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with this program.
* If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.util;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.silverpeas.util.i18n.I18NHelper;
import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.CharEncoding;

public class StringUtil extends StringUtils {

  private static final char[] PUNCTUATION = new char[]{'&', '\"', '\'', '{', '(', '[', '-', '|', '`',
    '_', '\\', '^', '@', ')', ']', '=', '+', '}', '?', ',', '.', ';', '/', ':', '!', '§',
    '%', '*', '$', '£', '€', '©', '²', '°', '¤'};
  private static final String PATTERN_START = "{";
  private static final String PATTERN_END = "}";
  private static final String TRUNCATED_TEXT_SUFFIX = "...";
  private static final String EMAIL_PATTERN =
    "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";

  public static boolean isDefined(String parameter) {
    return (parameter != null && parameter.trim().length() > 0 && !"null".equalsIgnoreCase(parameter));
  }

  public static boolean isInteger(String id) {
    try {
      Integer.parseInt(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean isLong(String id) {
    try {
      Long.parseLong(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static float convertFloat(String value) {
    if (StringUtil.isFloat(value)) {
      return Float.valueOf(value);
    } else if (value != null) {
      String charge = value.replace(',', '.');
      if (StringUtil.isFloat(charge)) {
        return Float.valueOf(charge);
      }
    }
    return 0f;
  }

  public static boolean isFloat(String id) {
    try {
      Float.parseFloat(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
* @param text
* @return a String with all quotes replaced by spaces
*/
  public static String escapeQuote(String text) {
    return text.replaceAll("'", " ");
  }

  /**
* Replaces
*
* @param name
* @return a String with all quotes replaced by spaces
*/
  public static String toAcceptableFilename(String name) {
    String fileName = name;
    fileName = fileName.replace('\\', '_');
    fileName = fileName.replace('/', '_');
    fileName = fileName.replace('$', '_');
    fileName = fileName.replace('%', '_');
    fileName = fileName.replace('?', '_');
    return fileName;
  }

  /**
* Format a string by extending the principle of the the method format() of the class
* java.text.MessageFormat to string arguments. For instance, the string '{key}' contained in the
* original string to format will be replaced by the value corresponding to this key contained
* into the values map.
*
* @param label The string to format
* @param values The values to insert into the string
* @return The formatted string, filled with values of the map.
*/
  public static String format(String label, Map<String, ?> values) {
    StringBuilder sb = new StringBuilder();
    int startIndex = label.indexOf(PATTERN_START);
    int endIndex;
    String patternKey;
    Object value;
    while (startIndex != -1) {
      endIndex = label.indexOf(PATTERN_END, startIndex);
      if (endIndex != -1) {
        patternKey = label.substring(startIndex + 1, endIndex);
        if (values.containsKey(patternKey)) {
          value = values.get(patternKey);
          sb.append(label.substring(0, startIndex)).append(
            value != null ? value.toString() : "");
        } else {
          sb.append(label.substring(0, endIndex + 1));
        }
        label = label.substring(endIndex + 1);
        startIndex = label.indexOf(PATTERN_START);
      } else {
        sb.append(label);
        label = "";
        startIndex = -1;
      }
    }
    sb.append(label);
    return sb.toString();
  }

  /**
* @param text The string to truncate if its size is greater than the maximum length given as
* parameter.
* @param maxLength The maximum length required.
* @return The truncated string followed by '...' if needed. Returns the string itself if its
* length is smaller than the required maximum length.
*/
  public static String truncate(String text, int maxLength) {
    if (text == null || text.length() <= maxLength) {
      return text;
    } else if (maxLength <= 3) {
      return TRUNCATED_TEXT_SUFFIX;
    } else {
      return text.substring(0, maxLength - 3) + TRUNCATED_TEXT_SUFFIX;
    }
  }

  /**
* Validate the form of an email address. <P> Return <tt>true</tt> only if <ul> <li>
* <tt>aEmailAddress</tt> can successfully construct an {@link javax.mail.internet.InternetAddress}
* <li>when parsed with "@" as delimiter, <tt>aEmailAddress</tt> contains two tokens which satisfy {@link hirondelle.web4j.util.Util#textHasContent}.
* </ul> <P> The second condition arises since local email addresses, simply of the form
* "<tt>albert</tt>", for example, are valid for {@link javax.mail.internet.InternetAddress}, but
* almost always undesired.
*
* @param aEmailAddress the address to be validated
* @return true is the address is a valid email address - false otherwise.
*/
  public static boolean isValidEmailAddress(String aEmailAddress) {
    if (aEmailAddress == null) {
      return false;
    }
    boolean result = true;
    try {
      new InternetAddress(aEmailAddress);
      result = Pattern.matches(EMAIL_PATTERN, aEmailAddress);
    } catch (AddressException ex) {
      result = false;
    }
    return result;
  }

  public static String convertToEncoding(String toConvert, String encoding) {
    try {
      return new String(toConvert.getBytes(Charset.defaultCharset()), encoding);
    } catch (UnsupportedEncodingException ex) {
      return toConvert;
    }
  }

  /**
* Evaluate the expression and return true if expression equals "true", "yes", "y", "1" or "oui".
*
* @param expression the expression to be evaluated
* @return true if expression equals "true", "yes", "y", "1" or "oui".
*/
  public static boolean getBooleanValue(final String expression) {
    return "true".equalsIgnoreCase(expression) || "yes".equalsIgnoreCase(expression)
      || "y".equalsIgnoreCase(expression) || "oui".equalsIgnoreCase(expression)
      || "1".equalsIgnoreCase(expression);
  }

  /**
* Method for trying to detect encoding
*
* @param data some data to try to detect the encoding.
* @param declaredEncoding expected encoding.
* @return
*/
  public static String detectEncoding(byte[] data, String declaredEncoding) {
    CharsetDetector detector = new CharsetDetector();
    if (!StringUtil.isDefined(declaredEncoding)) {
      detector.setDeclaredEncoding("ISO-8859-1");
    } else {
      detector.setDeclaredEncoding(declaredEncoding);
    }
    detector.setText(data);
    CharsetMatch detectedEnc = detector.detect();
    return detectedEnc.getName();
  }

  /**
* Method for trying to detect encoding
*
* @param data some data to try to detect the encoding.
* @param declaredEncoding expected encoding.
* @return
*/
  public static String detectStringEncoding(byte[] data, String declaredEncoding) throws
    UnsupportedEncodingException {
    if (data != null) {
      String value = new String(data, declaredEncoding);
      if (!checkEncoding(value)) {
        Set<String> supportedEncodings;
        if (CharEncoding.UTF_8.equals(declaredEncoding)) {
          supportedEncodings = StringUtil.detectMaybeEncoding(data, CharEncoding.ISO_8859_1);
        } else {
          supportedEncodings = StringUtil.detectMaybeEncoding(data, CharEncoding.UTF_8);
        }
        return reencode(data, supportedEncodings, declaredEncoding);
      }
    }
    return declaredEncoding;
  }

  private static boolean checkEncoding(String value) throws UnsupportedEncodingException {
    if (value != null) {
      char[] chars = value.toCharArray();
      for (char currentChar : chars) {
        if (!Character.isLetterOrDigit(currentChar) && !Character.isWhitespace(currentChar)
          && !ArrayUtil.contains(PUNCTUATION, currentChar)) {
          return false;
        }
      }
    }
    return true;

  }

  private static String reencode(byte[] data, Set<String> encodings, String declaredEncoding) throws UnsupportedEncodingException {
    if(!encodings.isEmpty()) {
      String encoding = encodings.iterator().next();
      String value = new String(data, encoding);
      if (!checkEncoding(value)) {
        encodings.remove(encoding);
        return reencode(data, encodings, declaredEncoding);
      }
      return encoding;
    }
    return declaredEncoding;
  }

  /**
* Method for trying to detect encoding
*
* @param data some data to try to detect the encoding.
* @param declaredEncoding expected encoding.
* @return
*/
  public static Set<String> detectMaybeEncoding(byte[] data, String declaredEncoding) {
    CharsetDetector detector = new CharsetDetector();
    if (!StringUtil.isDefined(declaredEncoding)) {
      detector.setDeclaredEncoding("ISO-8859-1");
    } else {
      detector.setDeclaredEncoding(declaredEncoding);
    }
    detector.setText(data);
    CharsetMatch[] detectedEnc = detector.detectAll();
    Set<String> encodings = new LinkedHashSet<String>(detectedEnc.length);
    for (CharsetMatch detectedEncoding : detectedEnc) {
      encodings.add(detectedEncoding.getName());
    }
    return encodings;
  }

  /**
* Indicates if two Strings are equals, managing null.
*
* @param s1 the first String.
* @param s2 the second String.
* @return true ifthe two Strings are equals.
*/
  public static boolean areStringEquals(String s1, String s2) {
    if (s1 == null) {
      return s2 == null;
    }
    return s1.equals(s2);
  }

  /**
* Parse a String into a float using the default locale.
*
* @param value the string to be parsed into a float.
* @return the float value.
* @throws ParseException
*/
  public static float floatValue(String value) throws ParseException {
    return floatValue(value, I18NHelper.defaultLanguage);
  }

  /**
* Parse a String into a float using the specified locale.
*
* @param value the string to be parsed into a float
* @param language the language for defining the locale
* @return the float value.
* @throws ParseException
*/
  public static float floatValue(String value, String language) throws ParseException {
    String lang = language;
    if (!StringUtil.isDefined(language)) {
      lang = I18NHelper.defaultLanguage;
    }
    NumberFormat numberFormat = NumberFormat.getInstance(new Locale(lang));
    return numberFormat.parse(value).floatValue();
  }

  private StringUtil() {
  }
}