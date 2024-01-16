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
package org.silverpeas.core.util;

import org.silverpeas.core.SilverpeasRuntimeException;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.Objects;

/**
 * URL-encoding utility for each URL part according to the RFC specs.
 * The original code, renamed to URLEncoder to match its responsibility, is available on :
 * https://github.com/resteasy/Resteasy/blob/master/jaxrs/security/resteasy-oauth/src/main/java
 * /org/jboss/resteasy/auth/oauth/URLUtils.java
 * <a href="http://www.ietf.org/rfc/rfc3986.txt">IETF RFC 3986</a>}
 * @author stephane@epardaud.fr
 */
public class URLEncoder {

  /**
   * gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
   */
  public static final BitSet GEN_DELIMS = new BitSet(7);
  /**
   * sub-delims = "!" / "$" / "&amp;" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
   */
  public static final BitSet SUB_DELIMS = new BitSet(11);
  /**
   * reserved = gen-delims | sub-delims
   */
  public static final BitSet RESERVED = new BitSet(2);
  /**
   * lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n"
   * | "o" | "p" | "q" |
   * "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
   */
  public static final BitSet LOW_ALPHA = new BitSet(26);
  /**
   * upalpha = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" |
   * "O" | "P" | "Q" |
   * "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
   */
  public static final BitSet UP_ALPHA = new BitSet(26);
  /**
   * alpha = lowalpha | upalpha
   */
  public static final BitSet ALPHA = new BitSet(52);
  /**
   * digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
   */
  public static final BitSet DIGIT = new BitSet(10);
  /**
   * alphanum = alpha | digit
   */
  public static final BitSet ALPHANUM = new BitSet(62);
  /**
   * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
   */
  public static final BitSet UNRESERVED = new BitSet(66);
  /**
   * pchar = unreserved | escaped | sub-delims | ":" | "@"
   * <p>
   * Note: we don't allow escaped here since we will escape it ourselves, so we don't want to
   * allow them in the
   * unescaped sequences
   */
  public static final BitSet PCHAR = new BitSet(80);
  /**
   * path_segment = pchar &lt;without&gt; ";"
   */
  public static final BitSet PATH_SEGMENT = new BitSet();
  /**
   * path_param_name = pchar &lt;without&gt; ";" | "="
   */
  public static final BitSet PATH_PARAM_NAME = new BitSet();
  /**
   * path_param_value = pchar &lt;without&gt; ";"
   */
  public static final BitSet PATH_PARAM_VALUE = new BitSet();
  /**
   * query = pchar / "/" / "?"
   */
  public static final BitSet QUERY = new BitSet();
  /**
   * fragment = pchar / "/" / "?"
   */
  public static final BitSet FRAGMENT = new BitSet();

  private URLEncoder() {
  }

  /**
   * Encodes a string to be a valid path parameter name, which means it can contain PCHAR* without
   * "=" or ";". Uses
   * UTF-8.
   */
  public static String encodePathParamName(final String pathParamName) {
    try {
      return encodePart(pathParamName, Charsets.UTF_8.toString(), PATH_PARAM_NAME);
    } catch (final UnsupportedEncodingException e) {
      // should not happen
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Encodes a string to be a valid path parameter value, which means it can contain PCHAR*
   * without ";". Uses UTF-8.
   */
  public static String encodePathParamValue(final String pathParamValue) {
    try {
      return encodePart(pathParamValue, Charsets.UTF_8.toString(), PATH_PARAM_VALUE);
    } catch (final UnsupportedEncodingException e) {
      // should not happen
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Encodes a string to be a valid query, which means it can contain PCHAR* | "?" | "/" without
   * "=" | "&amp;" | "+". Uses
   * UTF-8.
   */
  public static String encodeQueryNameOrValue(final String queryNameOrValue) {
    try {
      return encodePart(queryNameOrValue, Charsets.UTF_8.toString(), QUERY);
    } catch (final UnsupportedEncodingException e) {
      // should not happen
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Encodes a string to be a valid query with no parenthesis, which means it can contain PCHAR* |
   * "?" | "/" without
   * "=" | "&amp;" | "+" | "(" | ")". It strips parenthesis. Uses UTF-8.
   */
  public static String encodeQueryNameOrValueNoParen(final String queryNameOrValueNoParen) {
    Objects.requireNonNull(queryNameOrValueNoParen);
    try {
      String query = encodePart(queryNameOrValueNoParen, Charsets.UTF_8.toString(), QUERY);
      query = query.replace("(", "");
      return query.replace(")", "");
    } catch (final UnsupportedEncodingException e) {
      // should not happen
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Encodes a string to be a valid path segment, which means it can contain PCHAR* only (do not
   * put path parameters or
   * they will be escaped. Uses UTF-8.
   */
  public static String encodePathSegment(final String pathSegment) {
    try {
      return encodePart(pathSegment, Charsets.UTF_8.toString(), PATH_SEGMENT);
    } catch (final UnsupportedEncodingException e) {
      // should not happen
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Encodes a string to be a valid URI part, with the given characters allowed. The rest will be
   * encoded.
   * @throws UnsupportedEncodingException
   */
  public static String encodePart(final String part, final String charset, final BitSet allowed)
      throws UnsupportedEncodingException {
    if (part == null) {
      return null;
    }
    // start at *3 for the worst case when everything is %encoded on one byte
    final StringBuilder encoded = new StringBuilder(part.length() * 3);
    final char[] toEncode = part.toCharArray();
    for (final char c : toEncode) {
      if (allowed.get(c)) {
        encoded.append(c);
      } else {
        final byte[] bytes = String.valueOf(c).getBytes(charset);
        for (final byte b : bytes) {
          // make it unsigned
          final int u8 = b & 0xFF;
          encoded.append(String.format("%%%1$02X", u8));
        }
      }
    }
    return encoded.toString();
  }

  static {
    GEN_DELIMS.set(':');
    GEN_DELIMS.set('/');
    GEN_DELIMS.set('?');
    GEN_DELIMS.set('#');
    GEN_DELIMS.set('[');
    GEN_DELIMS.set(']');
    GEN_DELIMS.set('@');
  }

  static {
    SUB_DELIMS.set('!');
    SUB_DELIMS.set('$');
    SUB_DELIMS.set('&');
    SUB_DELIMS.set('\'');
    SUB_DELIMS.set('(');
    SUB_DELIMS.set(')');
    SUB_DELIMS.set('*');
    SUB_DELIMS.set('+');
    SUB_DELIMS.set(',');
    SUB_DELIMS.set(';');
    SUB_DELIMS.set('=');
  }

  static {
    RESERVED.or(GEN_DELIMS);
    RESERVED.or(SUB_DELIMS);
  }

  static {
    LOW_ALPHA.set('a');
    LOW_ALPHA.set('b');
    LOW_ALPHA.set('c');
    LOW_ALPHA.set('d');
    LOW_ALPHA.set('e');
    LOW_ALPHA.set('f');
    LOW_ALPHA.set('g');
    LOW_ALPHA.set('h');
    LOW_ALPHA.set('i');
    LOW_ALPHA.set('j');
    LOW_ALPHA.set('k');
    LOW_ALPHA.set('l');
    LOW_ALPHA.set('m');
    LOW_ALPHA.set('n');
    LOW_ALPHA.set('o');
    LOW_ALPHA.set('p');
    LOW_ALPHA.set('q');
    LOW_ALPHA.set('r');
    LOW_ALPHA.set('s');
    LOW_ALPHA.set('t');
    LOW_ALPHA.set('u');
    LOW_ALPHA.set('v');
    LOW_ALPHA.set('w');
    LOW_ALPHA.set('x');
    LOW_ALPHA.set('y');
    LOW_ALPHA.set('z');
  }

  static {
    UP_ALPHA.set('A');
    UP_ALPHA.set('B');
    UP_ALPHA.set('C');
    UP_ALPHA.set('D');
    UP_ALPHA.set('E');
    UP_ALPHA.set('F');
    UP_ALPHA.set('G');
    UP_ALPHA.set('H');
    UP_ALPHA.set('I');
    UP_ALPHA.set('J');
    UP_ALPHA.set('K');
    UP_ALPHA.set('L');
    UP_ALPHA.set('M');
    UP_ALPHA.set('N');
    UP_ALPHA.set('O');
    UP_ALPHA.set('P');
    UP_ALPHA.set('Q');
    UP_ALPHA.set('R');
    UP_ALPHA.set('S');
    UP_ALPHA.set('T');
    UP_ALPHA.set('U');
    UP_ALPHA.set('V');
    UP_ALPHA.set('W');
    UP_ALPHA.set('X');
    UP_ALPHA.set('Y');
    UP_ALPHA.set('Z');
  }

  static {
    ALPHA.or(LOW_ALPHA);
    ALPHA.or(UP_ALPHA);
  }

  static {
    DIGIT.set('0');
    DIGIT.set('1');
    DIGIT.set('2');
    DIGIT.set('3');
    DIGIT.set('4');
    DIGIT.set('5');
    DIGIT.set('6');
    DIGIT.set('7');
    DIGIT.set('8');
    DIGIT.set('9');
  }

  static {
    ALPHANUM.or(ALPHA);
    ALPHANUM.or(DIGIT);
  }

  static {
    UNRESERVED.or(ALPHA);
    UNRESERVED.or(DIGIT);
    UNRESERVED.set('-');
    UNRESERVED.set('.');
    UNRESERVED.set('_');
    UNRESERVED.set('~');
  }

  static {
    PCHAR.or(UNRESERVED);
    PCHAR.or(SUB_DELIMS);
    PCHAR.set(':');
    PCHAR.set('@');
  }

  static {
    PATH_SEGMENT.or(PCHAR);
    // deviate from the RFC in order to disallow the path param separator
    PATH_SEGMENT.clear(';');
  }

  static {
    PATH_PARAM_NAME.or(PCHAR);
    // deviate from the RFC in order to disallow the path param separators
    PATH_PARAM_NAME.clear(';');
    PATH_PARAM_NAME.clear('=');
  }

  static {
    PATH_PARAM_VALUE.or(PCHAR);
    // deviate from the RFC in order to disallow the path param separator
    PATH_PARAM_VALUE.clear(';');
  }

  static {
    QUERY.or(PCHAR);
    QUERY.set('/');
    QUERY.set('?');
    // deviate from the RFC to disallow separators such as "=", "@" and the famous "+" which is treated as a space
    // when decoding
    QUERY.clear('=');
    QUERY.clear('&');
    QUERY.clear('+');
  }

  static {
    FRAGMENT.or(PCHAR);
    FRAGMENT.set('/');
    FRAGMENT.set('?');
  }
}
