/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import java.util.regex.Pattern;

public class StringUtil extends StringUtils {

  public static final String EMPTY = StringUtils.EMPTY;
  public static final String NEWLINE = System.getProperty("line.separator");

  private static final String PATTERN_START = "{";
  private static final String PATTERN_END = "}";
  private static final String TRUNCATED_TEXT_SUFFIX = "...";
  private static final String EMAIL_PATTERN
      = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";
  private static final String HOUR_PATTERN = "^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$";


  public static String emptyString() {
    return EMPTY;
  }

  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * Null objects or empty strings within the array are represented by
   * empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null, *)               = null
   * StringUtils.join([], *)                 = ""
   * StringUtils.join([null], *)             = ""
   * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
   * StringUtils.join(["a", "b", "c"], null) = "abc"
   * StringUtils.join([null, "", "a"], ';')  = ";;a"
   * </pre>
   *
   * @param array  the array of values to join together, may be null
   * @param separator  the separator character to use
   * @return the joined String, {@code null} if null array input
   * @since 2.0
   */
  public static String join(final Object[] array, final char separator) {
    return StringUtils.join(array, separator);
  }

  public static boolean isDefined(String parameter) {
    return (parameter != null && !parameter.trim().isEmpty() && !"null".equalsIgnoreCase(parameter));
  }

  public static boolean isNotDefined(String parameter) {
    return !isDefined(parameter);
  }

  public static void requireDefined(final String name) {
    if (isNotDefined(name)) {
      throw new AssertionError(name + " isn't defined!");
    }
  }

  public static String requireDefined(final String object, final String message) {
    if (isNotDefined(object)) {
      throw new AssertionError(message);
    }
    return object;
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
   *
   * @see java.lang.String#startsWith(String)
   * @param str  the CharSequence to check, may be null
   * @param prefix the prefix to find, may be null
   * @return {@code true} if the CharSequence starts with the prefix, case sensitive, or
   *  both {@code null}
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
   *
   * @param str  the String to parse, may be null
   * @param separator  String containing the String to be used as a delimiter,
   *  {@code null} splits on whitespace
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
   * Adjacent separators are treated as one separator.
   * For more control over the split use the StrTokenizer class.</p>
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
   *
   * @param str  the String to parse, may be null
   * @param separatorChar  the character used as the delimiter
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
   * Adjacent separators are treated as one separator.
   * For more control over the split use the StrTokenizer class.</p>
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
   *
   * @param str  the String to parse, may be null
   * @param separatorChars  the characters used as the delimiters,
   *  {@code null} splits on whitespace
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
   *
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
   *
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
   * combined characters which will make the server have a bad behavior, like throw an error on
   * file download.</p>
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
      // separating all of the accent marks from the characters
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
   * @param string the String to check, may be null, blank or filled by spaces
   * if the input is {@code not defined}, may be null, blank or filled by spaces
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
   * @param defaultString the default String to return
   * if the input is {@code not defined}, may be null, blank or filled by spaces
   * @return the passed in String, or the default if it was {@code null}
   * @see StringUtil#isNotDefined(String)
   * @see StringUtils#defaultString(String, String)
   */
  public static String defaultStringIfNotDefined(String string, String defaultString) {
    return StringUtils.defaultString((isDefined(string) ? string : null), defaultString);
  }

  public static boolean isInteger(String value) {
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static int asInt(String value, int defaultValue) {
    int integer;
    try {
      integer = value == null ? defaultValue : Integer.parseInt(value);
    } catch (NumberFormatException e) {
      integer = defaultValue;
    }
    return integer;
  }

  public static boolean isLong(String value) {
    try {
      Long.parseLong(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

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

  public static boolean isFloat(String value) {
    try {
      Float.parseFloat(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * @param text the from which the quotes must be escaped (replaced by spaces in fact).
   * @return a String with all quotes replaced by spaces
   */
  public static String escapeQuote(String text) {
    return text.replace("'", " ");
  }

  /**
   * Replaces
   *
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
   * Replace parts of a text by an one replacement string. The text to replace is specified by a
   * regex.
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

  public static boolean isValidHour(final String time) {
    return isDefined(time) && Pattern.matches(HOUR_PATTERN, time);
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
   * Encodes the specified binary data into a text of Base64 characters.
   *
   * @param binaryData the binary data to convert in Base64-based String.
   * @return a String representation of the binary data in Base64 characters.
   */
  public static String asBase64(byte[] binaryData) {
    return Base64.getEncoder().encodeToString(binaryData);
  }

  /**
   * Decodes the specified text with Base64 characters in binary.
   *
   * @param base64Text the text in Base64.
   * @return the binary representation of the text.
   */
  public static byte[] fromBase64(String base64Text) {
    return Base64.getDecoder().decode(base64Text);
  }

  /**
   * <p>Splits the provided text into an array, using whitespace as the separator. Whitespace is
   * defined by {@link Character#isWhitespace(char)}.</p>
   *
   * <p>The separator is not included in the returned String array. Adjacent separators are treated
   * as one separator. For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.split(null)       = null
   * StringUtils.split("")         = empty list
   * StringUtils.split("abc def")  = ["abc", "def"]
   * StringUtils.split("abc  def") = ["abc", "def"]
   * StringUtils.split(" abc ")    = ["abc"]
   * </pre>
   *
   * @param str the String to parse, may be null
   * @return an array of parsed Strings, {@code null} if null String input
   */
  public static Iterable<String> splitString(String str) {
    return Arrays.asList(StringUtils.split(str));
  }

  /**
   * <p>Splits the provided text into an array, separator specified. This is an alternative to using
   * StringTokenizer.</p>
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
   *
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
          res.insert(k+i, '\\');
          k++;
        }
      }
    }
    return res.toString();
  }

  public static boolean likeIgnoreCase(final String actualValue, String expectedValue) {
    return new Like(actualValue, expectedValue, true).test();
  }

  public static boolean like(final String actualValue, String expectedValue) {
    return new Like(actualValue, expectedValue, false).test();
  }

  private static class Like {
    private final String actual;
    private final String expected;
    private String currentActual;
    private int tokenIndex;

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
      currentActual = actual;
      tokenIndex = 0;
      boolean like = true;
      boolean mustStart = true;
      String currentToken = nextExpectedToken();
      while(like && currentToken != null) {
        if (currentToken.isEmpty()) {
          mustStart = false;
          tokenIndex++;
        } else {
          like = verifyToken(currentToken, mustStart);
          mustStart = true;
        }
        currentToken = nextExpectedToken();
      }
      return like && (!mustStart || currentActual.isEmpty());
    }

    private boolean verifyToken(final String token, final boolean mustStart) {
      final String escapedToken = token.replace("\\%", "%");
      final int currentIndex = currentActual.indexOf(escapedToken);
      final int nextActualIndex = currentIndex + escapedToken.length();
      currentActual = nextActualIndex < currentActual.length()
          ? currentActual.substring(nextActualIndex)
          : "";
      tokenIndex += token.length();
      return mustStart ? currentIndex == 0 : currentIndex >= 0;
    }

    private String nextExpectedToken() {
      if (tokenIndex >= expected.length()) {
        return null;
      }
      int index = expected.indexOf('%', tokenIndex);
      boolean found = false;
      while(!found) {
        if (index < 0) {
          index = expected.length();
          found = true;
        } else if (index > 0 && expected.charAt(index - 1) != '\\') {
          found = true;
        } else if (index == 0) {
          found = true;
        } else {
          index = expected.indexOf('%', index + 1);
        }
      }
      return expected.substring(tokenIndex, index);
    }
  }

  private StringUtil() {
  }
}
