/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.util;

import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Utility class providing useful operations on {@link String}.
 */
public class StringUtil extends StringUtils {

  public static final String EMPTY = StringUtils.EMPTY;
  public static final String NEWLINE = System.getProperty("line.separator");

  private static final String PATTERN_START = "{";
  private static final String PATTERN_END = "}";
  private static final String TRUNCATED_TEXT_SUFFIX = "...";
  private static final String EMAIL_PATTERN
      = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";
  private static final String HOUR_PATTERN = "^([0-1]?\\d|2[0-4]):([0-5]\\d)(:[0-5]\\d)?$";

  /**
   * Gets an empty string as a constant (reusable empty string).
   * @return an empty string.
   */
  public static String emptyString() {
    return EMPTY;
  }

  /**
   * <p>Case insensitive check if a CharSequence starts with a specified prefix.</p>
   *
   * <p>{@code null}s are handled without exceptions. Two {@code null}
   * references are considered to be equal. The comparison is case insensitive.</p>
   *
   * <pre>
   * StringUtils.startsWithIgnoreCase(null, null)      = true
   * StringUtils.startsWithIgnoreCase(null, "abc")     = false
   * StringUtils.startsWithIgnoreCase("abcdef", null)  = false
   * StringUtils.startsWithIgnoreCase("abcdef", "abc") = true
   * StringUtils.startsWithIgnoreCase("ABCDEF", "abc") = true
   * </pre>
   * @param str the text to check, may be null
   * @param prefix the text to find, may be null
   * @return {@code true} if the CharSequence starts with the prefix, case-insensitive, or both
   * {@code null}
   * @see java.lang.String#startsWith(String)
   */
  public static boolean startsWithIgnoreCase(final String str, final String prefix) {
    return StringUtils.startsWithIgnoreCase(str, prefix);
  }

  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * Null objects or empty strings within the array are represented by empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null, *)               = null
   * StringUtils.join([], *)                 = ""
   * StringUtils.join([null], *)             = ""
   * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
   * StringUtils.join(["a", "b", "c"], null) = "abc"
   * StringUtils.join([null, "", "a"], ';')  = ";;a"
   * </pre>
   * @param array the array of values to join together, may be null
   * @param separator the separator character to use
   * @return the joined String, {@code null} if null array input
   * @since 2.0
   */
  public static String join(final Object[] array, final char separator) {
    return StringUtils.join(array, separator);
  }

  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * A {@code null} separator is the same as an empty String (""). Null objects or empty strings
   * within the array are represented by empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null, *)                = null
   * StringUtils.join([], *)                  = ""
   * StringUtils.join([null], *)              = ""
   * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
   * StringUtils.join(["a", "b", "c"], null)  = "abc"
   * StringUtils.join(["a", "b", "c"], "")    = "abc"
   * StringUtils.join([null, "", "a"], ',')   = ",,a"
   * </pre>
   * @param array the array of values to join together, may be null
   * @param delimiter the separator character to use, null treated as ""
   * @return the joined String, {@code null} if null array input
   */
  public static String join(Object[] array, String delimiter) {
    return StringUtils.join(array, delimiter);
  }

  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "...is the time for..."</p>
   *
   * <p>Works like {@code abbreviate(String, int)}, but allows you to specify
   * a "left edge" offset.  Note that this left edge is not necessarily going to be the leftmost
   * character in the result, or the first character following the ellipses, but it will appear
   * somewhere in the result.
   *
   * <p>In no case will it return a String of length greater than
   * {@code maxWidth}.</p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *, *)                = null
   * StringUtils.abbreviate("", 0, 4)                  = ""
   * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
   * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
   * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
   * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
   * </pre>
   * @param str the String to check, may be null
   * @param offset left edge of source String
   * @param maxWidth maximum length of result String, must be at least 4
   * @return abbreviated String, {@code null} if null String input
   * @throws IllegalArgumentException if the width is too small
   */
  public static String abbreviate(final String str, final int offset, final int maxWidth) {
    return StringUtils.abbreviate(str, offset, maxWidth);
  }

  /**
   * Is the specified string is well-defined?
   * @param str a string to check.
   * @return true if the given string isn't null, nor empty and doesn't contain any space
   * characters. False otherwise. The "null" string is considered as a non-defined string.
   */
  public static boolean isDefined(String str) {
    return (str != null && !str.trim().isEmpty() &&
        !"null".equalsIgnoreCase(str));
  }

  /**
   * Is the specified string is not defined?
   * @param str the string to check.
   * @return true if the string is either null, an empty string, or a string containing only space
   * characters. False otherwise. The "null" string is considered as a non-defined string.
   * @implNote it is the reverse of the {@link StringUtil#isDefined(String)} method.
   * @see StringUtil#isDefined(String)
   */
  public static boolean isNotDefined(String str) {
    return !isDefined(str);
  }

  /**
   * Requires the specified string to be defined, otherwise an {@link AssertionError} is thrown.
   * @param str the string to check.
   */
  public static void requireDefined(final String str) {
    if (isNotDefined(str)) {
      throw new AssertionError(str + " isn't defined!");
    }
  }

  /**
   * Requires the specified string to be defined, otherwise an {@link AssertionError} is thrown with
   * the given message.
   * @param str the string to check.
   * @param message the message to pass if the string doesn't satisfy the requirement.
   */
  public static void requireDefined(final String str, final String message) {
    if (isNotDefined(str)) {
      throw new AssertionError(message);
    }
  }

  /**
   * <p>Check if a CharSequence starts with a specified prefix.</p>
   *
   * <p>{@code null}s are handled without exceptions. Two {@code null}
   * references are considered to be equal. The comparison is case sensitive.</p>
   *
   * <pre>
   * StringUtils.startsWith(null, null)      = true
   * StringUtils.startsWith(null, "abc")     = false
   * StringUtils.startsWith("abcdef", null)  = false
   * StringUtils.startsWith("abcdef", "abc") = true
   * StringUtils.startsWith("ABCDEF", "abc") = false
   * </pre>
   * @param str the CharSequence to check, may be null
   * @param prefix the prefix to find, may be null
   * @return {@code true} if the CharSequence starts with the prefix, case-sensitive, or both
   * {@code null}
   * @see java.lang.String#startsWith(String)
   */
  public static boolean startsWith(final CharSequence str, final CharSequence prefix) {
    return StringUtils.startsWith(str, prefix);
  }

  /**
   * <p>Splits the provided text into an array, separator string specified.</p>
   *
   * <p>The separator(s) will not be included in the returned String array.
   * Adjacent separators are treated as one separator.</p>
   *
   * <p>A {@code null} input String returns {@code null}.
   * A {@code null} separator splits on whitespace.</p>
   *
   * <pre>
   * StringUtils.splitByWholeSeparator(null, *)               = null
   * StringUtils.splitByWholeSeparator("", *)                 = []
   * StringUtils.splitByWholeSeparator("ab de fg", null)      = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab   de fg", null)    = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
   * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
   * </pre>
   * @param str the String to parse, may be null
   * @param separator String containing the String to be used as a delimiter, {@code null} splits on
   * whitespace
   * @return an array of parsed Strings, {@code null} if null String was input
   */
  public static String[] splitByWholeSeparator(final String str, final String separator) {
    return StringUtils.splitByWholeSeparator(str, separator);
  }

  /**
   * <p>Splits the provided text into an array, separator specified.
   * This is an alternative to using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as one separator. For more control over the split use the
   * StrTokenizer class.</p>
   *
   * <p>A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.split(null, *)         = null
   * StringUtils.split("", *)           = []
   * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
   * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
   * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
   * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
   * </pre>
   * @param str the String to parse, may be null
   * @param separatorChar the character used as the delimiter
   * @return an array of parsed Strings, {@code null} if null String input
   * @since 2.0
   */
  public static String[] split(final String str, final char separatorChar) {
    return StringUtils.split(str, separatorChar);
  }

  /**
   * <p>Splits the provided text into an array, separators specified.
   * This is an alternative to using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as one separator. For more control over the split use the
   * StrTokenizer class.</p>
   *
   * <p>A {@code null} input String returns {@code null}.
   * A {@code null} separatorChars splits on whitespace.</p>
   *
   * <pre>
   * StringUtils.split(null, *)         = null
   * StringUtils.split("", *)           = []
   * StringUtils.split("abc def", null) = ["abc", "def"]
   * StringUtils.split("abc def", " ")  = ["abc", "def"]
   * StringUtils.split("abc  def", " ") = ["abc", "def"]
   * StringUtils.split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
   * </pre>
   * @param str the String to parse, may be null
   * @param separatorChars the characters used as the delimiters, {@code null} splits on whitespace
   * @return an array of parsed Strings, {@code null} if null String input
   */
  public static String[] split(final String str, final String separatorChars) {
    return StringUtils.split(str, separatorChars);
  }

  /**
   * <p>Capitalizes a String changing the first character to title case as
   * per {@link Character#toTitleCase(int)}. No other characters are changed.</p>
   *
   * <p>A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.capitalize(null)  = null
   * StringUtils.capitalize("")    = ""
   * StringUtils.capitalize("cat") = "Cat"
   * StringUtils.capitalize("cAt") = "CAt"
   * StringUtils.capitalize("'cat'") = "'cat'"
   * </pre>
   * @param str the String to capitalize, may be null
   * @return the capitalized String, {@code null} if null String input
   * @see #uncapitalize(String)
   */
  public static String capitalize(final String str) {
    return StringUtils.capitalize(str);
  }

  /**
   * <p>Uncapitalizes a String, changing the first character to lower case as
   * per {@link Character#toLowerCase(int)}. No other characters are changed.</p>
   *
   * <p>A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.uncapitalize(null)  = null
   * StringUtils.uncapitalize("")    = ""
   * StringUtils.uncapitalize("cat") = "cat"
   * StringUtils.uncapitalize("Cat") = "cat"
   * StringUtils.uncapitalize("CAT") = "cAT"
   * </pre>
   * @param str the String to uncapitalize, may be null
   * @return the uncapitalized String, {@code null} if null String input
   * @see #capitalize(String)
   */
  public static String uncapitalize(final String str) {
    return StringUtils.uncapitalize(str);
  }

  /**
   * Normalizes the given string (which must be encoded into UTF-8) in order that the result
   * contains only unified chars.
   * <p>Indeed, according to the environment of the user, sometimes it is sent data with
   * combined characters which will make the server have a bad behavior, like throw an error on file
   * download.</p>
   * @param string the string to normalize. There is no guarantee when the string is not encoded
   * into UTF8.
   * @return the normalized string.
   */
  public static String normalize(final String string) {
    String normalized = string;
    if (normalized != null) {
      normalized = Normalizer.normalize(normalized, Normalizer.Form.NFC);
    }
    return normalized;
  }

  /**
   * Same treatment as the one of {@link #normalize(String)} but removes also the accented
   * characters.
   * @param string the string to normalize. There is no guarantee when the string is not encoded
   * into UTF8.
   * @return the normalized string.
   */
  public static String normalizeByRemovingAccent(final String string) {
    String normalized = string;
    if (normalized != null) {
      // separating all the accent marks from the characters
      normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
      // removing accent
      normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    return normalized;
  }

  /**
   * <p>Returns either the passed in String, or if the String is
   * {@code not defined}, an empty String ("").</p>
   * <p>
   * <pre>
   * StringUtil.defaultStringIfNotDefined(null)   = ""
   * StringUtil.defaultStringIfNotDefined("")     = ""
   * StringUtil.defaultStringIfNotDefined("    ") = ""
   * StringUtil.defaultStringIfNotDefined("bat")  = "bat"
   * </pre>
   * @param string the String to check, may be null, blank or filled by spaces if the input is
   * {@code not defined}, may be null, blank or filled by spaces
   * @return the passed in String, or the default if it was {@code null}
   * @see StringUtil#isNotDefined(String)
   * @see StringUtils#defaultString(String, String)
   */
  public static String defaultStringIfNotDefined(String string) {
    return defaultStringIfNotDefined(string, StringUtils.EMPTY);
  }


  /**
   * <p>Returns either the passed in String, or if the String is
   * {@code not defined}, the value of {@code defaultString}.</p>
   * <p>
   * <pre>
   * StringUtil.defaultStringIfNotDefined(null, "NULL")   = "NULL"
   * StringUtil.defaultStringIfNotDefined("", "NULL")     = "NULL"
   * StringUtil.defaultStringIfNotDefined("    ", "NULL") = "NULL"
   * StringUtil.defaultStringIfNotDefined("bat", "NULL")  = "bat"
   * </pre>
   * @param string the String to check, may be null, blank or filled by spaces
   * @param defaultString the default String to return if the input is {@code not defined}, may be
   * null, blank or filled by spaces
   * @return the passed in String, or the default if it was {@code null}
   * @see StringUtil#isNotDefined(String)
   * @see StringUtils#defaultString(String, String)
   */
  public static String defaultStringIfNotDefined(String string, String defaultString) {
    return StringUtils.defaultString((isDefined(string) ? string : null), defaultString);
  }

  /**
   * This method allows the caller to handle the case where a string is not defined in a
   * functional way.
   * <p>
   *   If the returned optional is present (so not empty), it means that the string given as
   *   parameter is defined (checked with {@link #isDefined(String)}).
   * </p>
   * <p>
   *   If the returned optional is not present (so empty), it means that the string given as
   *   parameter is not defined. In a such cas, thanks to {@link Optional}, the caller can choose
   *   the behavior to adopt.
   * </p>
   * @param string the String to check, may be null, blank or filled by spaces
   * @return optional with given string if defined
   */
  public static Optional<String> definedString(String string) {
    return ofNullable(string).filter(StringUtil::isDefined);
  }

  /**
   * Is the specified value encodes an {@link Integer}?
   * @param value the textual representation of an integer.
   * @return true if the given value represents an integer. False otherwise.
   */
  public static boolean isInteger(String value) {
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Decodes the specified textual value as an integer.
   * @param value the textual value to decode.
   * @param defaultValue the default value to use if the value doesn't encode an integer.
   * @return either the integer representation of the value or the given default value if it isn't
   * an integer representation.
   */
  public static int asInt(String value, int defaultValue) {
    int integer;
    try {
      integer = value == null ? defaultValue : Integer.parseInt(value);
    } catch (NumberFormatException e) {
      integer = defaultValue;
    }
    return integer;
  }

  /**
   * Is the specified value encodes a {@link Long}?
   * @param value the textual representation of a long integer.
   * @return true if the given value represents a long integer. False otherwise.
   */
  public static boolean isLong(String value) {
    try {
      Long.parseLong(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Decodes the specified textual value as a float real.
   * @param value the textual value to decode.
   * @return either the float real representation of the value or 0 if it isn't a float real
   * representation.
   */
  public static float asFloat(String value) {
    if (StringUtil.isFloat(value)) {
      return Float.parseFloat(value);
    } else if (value != null) {
      String charge = value.replace(',', '.');
      if (StringUtil.isFloat(charge)) {
        return Float.parseFloat(charge);
      }
    }
    return 0f;
  }

  /**
   * Is the specified value encodes a {@link Float}?
   * @param value the textual representation of a float real.
   * @return true if the given value represents a float real. False otherwise.
   */
  public static boolean isFloat(String value) {
    try {
      Float.parseFloat(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * @param text the text from which the quotes must be escaped (replaced by spaces in fact).
   * @return a String with all quotes replaced by spaces
   */
  public static String escapeQuote(String text) {
    return text.replace("'", " ");
  }

  /**
   * Replaces all quotes by spaces.
   * @param name the original filename.
   * @return a String with all quotes replaced by spaces
   */
  public static String toAcceptableFilename(String name) {
    String fileName = name;
    fileName = fileName.replace('\\', '_');
    fileName = fileName.replace('/', '_');
    fileName = fileName.replace('$', '_');
    fileName = fileName.replace('%', '_');
    fileName = fileName.replace('?', '_');
    fileName = fileName.replace(':', '_');
    fileName = fileName.replace('*', '_');
    fileName = fileName.replace('"', '_');
    fileName = fileName.replace('<', '_');
    fileName = fileName.replace('>', '_');
    fileName = fileName.replace('|', '_');
    return fileName;
  }

  /**
   * Format a string by extending the principle of the method format() of the class
   * java.text.MessageFormat to string arguments. For instance, the string '{key}' contained in the
   * original string to format will be replaced by the value corresponding to this key contained
   * into the values map.
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
          sb.append(label, 0, startIndex).append(
              value != null ? value.toString() : "");
        } else {
          sb.append(label, 0, endIndex + 1);
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
   * Replace parts of a text by a replacement string. The text to replace is specified by a regex.
   * @param source the original text
   * @param regex the regex that permits to identify parts of text to replace
   * @param replacement the replacement text
   * @return The source text modified
   */
  public static String regexReplace(String source, String regex, String replacement) {
    if (StringUtil.isNotDefined(source) || StringUtil.isNotDefined(regex)) {
      return source;
    }
    return source.replaceAll(regex, replacement);
  }

  /**
   * Validate the form of an email address.
   * <p> Returns <tt>true</tt> only if
   * <ul>
   * <li><tt>aEmailAddress</tt> can successfully construct an
   * {@link javax.mail.internet.InternetAddress}</li>
   * <li>when parsed with "@" as delimiter, <tt>aEmailAddress</tt> contains two tokens which
   * satisfy</li>
   * </ul>
   * <p>
   * The second condition arises since local email addresses, simply of the form "<tt>albert</tt>",
   * for example, are valid for {@link javax.mail.internet.InternetAddress}, but almost always
   * undesired.
   * @param aEmailAddress the address to be validated
   * @return true is the address is a valid email address - false otherwise.
   */
  public static boolean isValidEmailAddress(String aEmailAddress) {
    if (aEmailAddress == null) {
      return false;
    }
    boolean result;
    try {
      new InternetAddress(aEmailAddress);
      result = Pattern.matches(EMAIL_PATTERN, aEmailAddress);
    } catch (AddressException ex) {
      result = false;
    }
    return result;
  }

  /**
   * Is the specified text represents a time in the format expected by Silverpeas.
   * @param time the textual representation of a time.
   * @return true if the specified parameter represents a time formatted in the format expected by
   * Silverpeas.
   * @see StringUtil#HOUR_PATTERN for the expected format of the time.
   */
  public static boolean isValidHour(final String time) {
    return isDefined(time) && Pattern.matches(HOUR_PATTERN, time);
  }

  /**
   * Converts the given text into the specified charset. If the charset isn't supported, then an
   * {@link UnsupportedEncodingException} exception is thrown.
   * @param toConvert the text to convert.
   * @param encoding the charset into which the text has to be converted.
   * @return the converted text.
   */
  public static String convertToEncoding(String toConvert, String encoding) {
    try {
      return new String(toConvert.getBytes(Charset.defaultCharset()), encoding);
    } catch (UnsupportedEncodingException ex) {
      return toConvert;
    }
  }

  /**
   * Evaluate the expression and return true if expression equals "true", "yes", "y", "1" or "oui".
   * @param expression the expression to be evaluated
   * @return true if expression equals "true", "yes", "y", "1" or "oui".
   */
  public static boolean getBooleanValue(final String expression) {
    return "true".equalsIgnoreCase(expression) || "yes".equalsIgnoreCase(expression)
        || "y".equalsIgnoreCase(expression) || "oui".equalsIgnoreCase(expression)
        || "1".equalsIgnoreCase(expression);
  }

  /**
   * Indicates if two Strings are equals, managing null.
   * @param s1 the first String.
   * @param s2 the second String.
   * @return true if the two Strings are equals.
   */
  public static boolean areStringEquals(String s1, String s2) {
    if (s1 == null) {
      return s2 == null;
    }
    return s1.equals(s2);
  }

  /**
   * Encodes the specified binary data into a text of Base64 characters.
   * @param binaryData the binary data to convert in Base64-based String.
   * @return a String representation of the binary data in Base64 characters.
   */
  public static String asBase64(byte[] binaryData) {
    return Base64.getEncoder().encodeToString(binaryData);
  }

  /**
   * Decodes the specified text with Base64 characters in binary.
   * @param base64Text the text in Base64.
   * @return the binary representation of the text.
   */
  public static byte[] fromBase64(String base64Text) {
    return Base64.getDecoder().decode(base64Text);
  }

  /**
   * <p>Splits the provided text into an array, separator specified. This is an alternative to
   * using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array. Adjacent separators are treated
   * as one separator. For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.split(null, *)         = null
   * StringUtils.split("", *)           = empty list
   * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
   * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
   * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
   * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
   * </pre>
   * @param str the String to parse, may be null
   * @param separatorChar the character used as the delimiter
   * @return an array of parsed Strings, {@code null} if null String input
   */
  public static Iterable<String> splitString(String str, char separatorChar) {
    return Arrays.asList(StringUtils.split(str, separatorChar));
  }

  /**
   * Doubles the anti-slash character in a path value.
   * @param path the String representing a path.
   * @return the path corrected with the anti-slash doubled.
   */
  public static String doubleAntiSlash(String path) {
    StringBuilder res = new StringBuilder(path);
    int k = 0;
    for (int i = 0, j = 1; i < path.length(); i++, j++) {
      if (path.charAt(i) == '\\') {
        boolean hasNotAntiSlashAfter = j < path.length() && path.charAt(j) != '\\';
        boolean hasNotAntiSlashBefore = i > 0 && path.charAt(i - 1) != '\\';
        if (hasNotAntiSlashAfter && hasNotAntiSlashBefore) {
          res.insert(k + i, '\\');
          k++;
        }
      }
    }
    return res.toString();
  }

  /**
   * Is the actual value is like the expected one? Case-sensitivity isn't taken into account in the
   * like-matching.
   * @param actualValue the actual value to compare.
   * @param expectedValue the expected value the actual one has to match.
   * @return true if the actual value is like the expected one. False otherwise.
   */
  public static boolean likeIgnoreCase(final String actualValue, String expectedValue) {
    return new Like(actualValue, expectedValue, true).test();
  }

  /**
   * Is the actual value is like the expected one? Case-sensitivity of the values matters.
   * @param actualValue the actual value to compare.
   * @param expectedValue the expected value the actual one has to match.
   * @return true if the actual value is like the expected one. False otherwise.
   */
  public static boolean like(final String actualValue, String expectedValue) {
    return new Like(actualValue, expectedValue, false).test();
  }

  private static class Like {
    private final String actual;
    private final String expected;

    private Like(final String actual, final String expected, final boolean ignoreCase) {
      if (ignoreCase) {
        this.actual = defaultStringIfNotDefined(actual).toLowerCase();
        this.expected = defaultStringIfNotDefined(expected).toLowerCase();
      } else {
        this.actual = defaultStringIfNotDefined(actual);
        this.expected = defaultStringIfNotDefined(expected);
      }
    }

    boolean test() {
      final ScanContext ctx = new ScanContext();
      while (ctx.canConsumeAnotherToken()) {
        char current = ctx.currentToken();
        switch (current) {

          case '\\':
            processEscapeCard(ctx, current);
            break;

          case '_':
            ctx.next();
            ctx.matches = ctx.actualIdx <= actual.length();
            break;

          case '%':
            processWildcardAny(ctx);
            break;

          default:
            ctx.matches = ctx.actualMatches(current);
            ctx.next();
            break;
        }
      }

      return ctx.matches && ctx.isActualFullyChecked();
    }

    private void processEscapeCard(final ScanContext ctx, final char current) {
      char next = ctx.nextToken();
      boolean escape = next == '%' || next == '_';
      if (escape) {
        ctx.matches = actual.charAt(ctx.actualIdx) == next;
        ctx.expectedIdx++;
      } else {
        ctx.matches = actual.charAt(ctx.actualIdx) == current;
      }
      ctx.actualIdx++;
    }

    private void processWildcardAny(final ScanContext ctx) {
      if (!goToNextNonWildcardToken(ctx)) {
        return;
      }

      // checks any wildcard '_' matches the expected number of characters in the actual text
      // for doing we first count all of them
      int underscoreCount = 0;
      while (ctx.hasSomeToken() && ctx.currentToken() == '_') {
        underscoreCount++;
        ctx.expectedIdx++;
      }

      // if the consuming is done, the treatment ends here
      if (!ctx.hasSomeToken()) {
        // in the case of wildcard '_', the number of them should match at least the rest of the
        // actual text characters (the first others are taken by the '%' wildcard)
        if (underscoreCount > 0) {
          ctx.matches = actual.length() - ctx.actualIdx >= underscoreCount;
        }
        ctx.actualIdx = actual.length();
        return;
      }

      StringBuilder exp = new StringBuilder();
      subtractExpectedTextToNextWildcard(ctx, exp);

      // finally, checks either the number of wildcard '_' matches at least the number of the
      // tokens in the expression figuring out from the expected text between two wildcards, or the
      // expression is well contained in the actual text at an expected position.
      if (underscoreCount > 0) {
        if (exp.length() > 0) {
          ctx.actualIdx =
              actual.indexOf(exp.toString(), ctx.actualIdx + underscoreCount) + exp.length();
          ctx.matches = ctx.actualIdx >= exp.length();
        } else {
          ctx.actualIdx = ctx.actualIdx + underscoreCount;
          ctx.matches = ctx.actualIdx <= actual.length();
        }
      } else {
        ctx.actualIdx = actual.indexOf(exp.toString(), ctx.actualIdx) + exp.length();
        ctx.matches = ctx.actualIdx >= exp.length();
      }
    }

    /**
     * Moves the cursor on a token in the expected text to the next token that is not a '%'
     * wildcard.
     * @param ctx the current expected text scanning context.
     * @return true if a non-wildcard token has been found, false if the expected text has been
     * fully consumed without finding any non '%' wildcard token.
     */
    private boolean goToNextNonWildcardToken(final ScanContext ctx) {
      // skips all additional wildcard '%'
      do {
        ctx.expectedIdx++;
      } while (ctx.hasSomeToken() && ctx.currentToken() == '%');

      // if the consuming is done, the treatment ends here
      if (!ctx.hasSomeToken()) {
        ctx.actualIdx = actual.length();
        return false;
      }

      return true;
    }

    private void subtractExpectedTextToNextWildcard(final ScanContext ctx,
        StringBuilder expression) {
      char endChar = ctx.currentToken();
      while (ctx.hasSomeToken() && endChar != '_' && endChar != '%') {
        if (endChar == '\\' && ++ctx.expectedIdx < expected.length()) {
          char nextChar = ctx.currentToken();
          if (nextChar == '_' || nextChar == '%') {
            expression.append(nextChar);
            endChar = ctx.safeNextToken();
          } else if (nextChar != '\\') {
            expression.append(endChar).append(nextChar);
            endChar = ctx.safeNextToken();
          } else {
            expression.append(endChar);
            //noinspection ConstantConditions
            endChar = nextChar;
          }
        } else {
          expression.append(endChar);
          endChar = ctx.safeNextToken();
        }
      }
    }

    /**
     * Context on the scanning of the expected text against the actual one.
     */
    private class ScanContext {
      /**
       * The current character index in the actual text.
       */
      protected int actualIdx = 0;
      /**
       * The current character index in the expected text.
       */
      protected int expectedIdx = 0;
      /**
       * Is the matching between the expected and the actual texts is ok at the current state of the
       * scanning.
       */
      protected boolean matches = true;

      /**
       * Can another token in the expected text be consumed?
       * @return true if there is another token in the expected text and the matching between the
       * expected and the actual texts is always ok. False otherwise.
       */
      public boolean canConsumeAnotherToken() {
        return hasSomeToken() && matches;
      }

      /**
       * Is there again one token to consume in the expected text?
       * @return true if the expected text isn't fully consumed. False otherwise.
       */
      public boolean hasSomeToken() {
        return expectedIdx < expected.length();
      }

      /**
       * Is the specified character is at the expected position in the actual text.
       * @param token a token to check.
       * @return true if the token is at actualIdx position in the actual text.
       */
      public boolean actualMatches(final char token) {
        return actualIdx < actual.length() && actual.charAt(actualIdx) == token;
      }

      /**
       * Is the actual text fully checked with the expected text?
       * @return true if the matching of the expected text against the actual one has been complete.
       * False otherwise.
       */
      public boolean isActualFullyChecked() {
        return actualIdx == actual.length();
      }

      /**
       * Gets the current token in the expected text.
       * @return the token at the expectedIdx position in the expected text.
       */
      public char currentToken() {
        return expected.charAt(expectedIdx);
      }

      /**
       * Gets the next token in the expected text. The cursor expectedIdx of the expected text is
       * incremented before reading the token.
       * @return the token at the expectedIdx+1 position in the expected text.
       */
      public char nextToken() {
        return expected.charAt(++expectedIdx);
      }

      /**
       * Gets the next token in the expected text if there is another token to consume. The cursor
       * expectedIdx of the expected text is incremented to read the next token.
       * @return the token at the expectedIdx+1 position in the expected text if this text hasn't
       * been fully consumed. Returns '\0' otherwise.
       */
      public char safeNextToken() {
        expectedIdx++;
        return hasSomeToken() ? currentToken() : '\0';
      }

      /**
       * Increments both actualIdx and expectedIdx cursors.
       */
      public void next() {
        expectedIdx++;
        actualIdx++;
      }
    }
  }

  private StringUtil() {
  }
}
