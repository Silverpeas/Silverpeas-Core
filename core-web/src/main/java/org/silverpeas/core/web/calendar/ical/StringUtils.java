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

// Based on code from
// GCALDaemon is an OS-independent Java program that offers two-way
// synchronization between Google SilverpeasCalendar and various iCalalendar (RFC 2445)
// compatible calendar applications (Sunbird, Rainlendar, iCal, Lightning, etc).
// Project home:
// http://gcaldaemon.sourceforge.net
//
package org.silverpeas.core.web.calendar.ical;

import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.util.Charsets;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * Common String utilities (formatters, converters, etc).
 */
public final class StringUtils {

  public static byte[] encodeString(String string, Charset encoding)
      throws CharacterCodingException {
    return encodeArray(string.toCharArray(), encoding);
  }
  public static byte[] encodeString(String string, String encoding)
      throws CharacterCodingException {
    return encodeString(string, Charsets.toCharset(encoding));
  }

  static byte[] encodeArray(char[] chars, Charset encoding)
      throws CharacterCodingException {
    if (CharEncoding.US_ASCII.equals(encoding.name())) {
      byte[] array = new byte[chars.length];
      for (int i = 0; i < array.length; i++) {
        array[i] = (byte) chars[i];
      }
      return array;
    }
    ByteBuffer buffer = encoding.newEncoder().encode(CharBuffer.wrap(chars));
    byte[] array = new byte[buffer.limit()];
    System.arraycopy(buffer.array(), 0, array, 0, array.length);
    return array;
  }

  public static String decodeToString(byte[] bytes, String encoding) {
    return new String(decodeToArray(bytes, Charsets.toCharset(encoding)));
  }


  public static String decodeToString(byte[] bytes, Charset encoding) {
    return new String(decodeToArray(bytes, encoding));
  }

  static char[] decodeToArray(byte[] bytes, Charset encoding) {
    if (CharEncoding.US_ASCII.equals(encoding.name())) {
      char[] array = new char[bytes.length];
      for (int i = 0; i < array.length; i++) {
        array[i] = (char) bytes[i];
      }
      return array;
    }
    try {
      CharBuffer buffer = encoding.newDecoder().decode(ByteBuffer.wrap(bytes));
      char[] array = new char[buffer.limit()];
      System.arraycopy(buffer.array(), 0, array, 0, array.length);
      return array;
    } catch (Exception nioException) {
      return (new String(bytes, encoding)).toCharArray();
    }
  }

  public static String decodePassword(String encodedPassword) throws Exception {
    StringBuilder buffer = new StringBuilder(encodedPassword.substring(3));
    return decodeBASE64(buffer.reverse().toString().replace('$', '=')).trim();
  }
  public static long stringToLong(String string) throws NumberFormatException {
    StringBuffer buffer = new StringBuffer(string.toLowerCase());
    long unit = resolveUnit(buffer);
    long value = Long.parseLong(buffer.toString().trim());
    if (unit != 1) {
      value *= unit;
    }
    return value;
  }

  private static long resolveUnit(StringBuffer buffer) {
    long unit = 1;
    int i = -1;
    for (;;) {
      i = buffer.indexOf("msec", 0);
      if (i != -1) {
        break;
      }
      i = buffer.indexOf("mill", 0);
      if (i != -1) {
        break;
      }
      i = buffer.indexOf("sec", 0);
      if (i != -1) {
        unit = 1000L;
        break;
      }
      i = buffer.indexOf("min", 0);
      if (i != -1) {
        unit = 1000L * 60;
        break;
      }
      i = buffer.indexOf("hour", 0);
      if (i != -1) {
        unit = 1000L * 60 * 60;
        break;
      }
      i = buffer.indexOf("day", 0);
      if (i != -1) {
        unit = 1000L * 60 * 60 * 24;
        break;
      }
      i = buffer.indexOf("week", 0);
      if (i != -1) {
        unit = 1000L * 60 * 60 * 24 * 7;
        break;
      }
      i = buffer.indexOf("month", 0);
      if (i != -1) {
        unit = 1000L * 60 * 60 * 24 * 30;
        break;
      }
      i = buffer.indexOf("year", 0);
      if (i != -1) {
        unit = 1000L * 60 * 60 * 24 * 365;
        break;
      }
      i = buffer.indexOf("kbyte", 0);
      if (i != -1) {
        unit = 1024L;
        break;
      }
      i = buffer.indexOf("mbyte", 0);
      if (i != -1) {
        unit = 1024L * 1024;
        break;
      }
      i = buffer.indexOf("gbyte", 0);
      if (i != -1) {
        unit = 1024L * 1024 * 1024;
        break;
      }
      i = buffer.indexOf("tbyte", 0);
      if (i != -1) {
        unit = 1024L * 1024 * 1024 * 1024;
        break;
      }
      i = buffer.indexOf("byte", 0);
      break;
    }
    if (i != -1) {
      buffer.setLength(i);
    }
    return unit;
  }

  /**
   * Decodes a BASE64-encoded string.
   * @param string BASE64 string
   * @return String the decoded bytes
   */
  public static String decodeBASE64(String string) {
    return decodeToString(DatatypeConverter.parseBase64Binary(string), CharEncoding.UTF_8);
  }

  /**
   * Encode the input bytes into BASE64 format.
   * @param data - byte array to encode
   * @return encoded string
   */
  public static String encodeBASE64(byte[] data) {
    return DatatypeConverter.printBase64Binary(data);
  }

}