/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

//
// GCALDaemon is an OS-independent Java program that offers two-way
// synchronization between Google Calendar and various iCalalendar (RFC 2445)
// compatible calendar applications (Sunbird, Rainlendar, iCal, Lightning, etc).
//
// Apache License
// Version 2.0, January 2004
// http://www.apache.org/licenses/
// 
// Project home:
// http://gcaldaemon.sourceforge.net
//
package com.silverpeas.ical;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;

/**
 * Common String utilities (formatters, converters, etc). Created: Jan 03, 2007 12:50:56 PM
 * @author Andras Berkes
 */
public final class StringUtils {

  private static final byte[] BASE64_CHARS = new byte[256];
  private final static char[] ALPHABETS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
      .toCharArray();

  public static final String US_ASCII = "US-ASCII";
  public static final String ISO_8859_1 = "ISO-8859-1";
  public static final String ISO_8859_2 = "ISO-8859-2";
  public static final String UTF_8 = "UTF-8";

  private static final HashMap charsets = new HashMap();

  static {
    for (int i = 0; i < 256; i++)
      BASE64_CHARS[i] = -1;
    for (int i = 'A'; i <= 'Z'; i++)
      BASE64_CHARS[i] = (byte) (i - 'A');
    for (int i = 'a'; i <= 'z'; i++)
      BASE64_CHARS[i] = (byte) (26 + i - 'a');
    for (int i = '0'; i <= '9'; i++)
      BASE64_CHARS[i] = (byte) (52 + i - '0');
    BASE64_CHARS['+'] = 62;
    BASE64_CHARS['/'] = 63;
    try {
      charsets.put(ISO_8859_1, Charset.forName(ISO_8859_1));
      charsets.put(ISO_8859_2, Charset.forName(ISO_8859_2));
      charsets.put(UTF_8, Charset.forName(UTF_8));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static final Charset getCharset(String encoding) {
    Charset charset = (Charset) charsets.get(encoding);
    if (charset == null) {
      charset = Charset.forName(encoding);
      charsets.put(encoding, charset);
    }
    return charset;
  }

  public static final byte[] encodeString(String string, String encoding)
      throws CharacterCodingException {
    return encodeArray(string.toCharArray(), encoding);
  }

  static final byte[] encodeArray(char[] chars, String encoding)
      throws CharacterCodingException {
    if (encoding.equals(US_ASCII)) {
      byte[] array = new byte[chars.length];
      for (int i = 0; i < array.length; i++) {
        array[i] = (byte) chars[i];
      }
      return array;
    }
    ByteBuffer buffer = getEncoder(encoding).encode(CharBuffer.wrap(chars));
    byte[] array = new byte[buffer.limit()];
    System.arraycopy(buffer.array(), 0, array, 0, array.length);
    return array;
  }

  public static final CharsetEncoder getEncoder(String encoding) {
    return getCharset(encoding).newEncoder();
  }

  public static final String decodeToString(byte[] bytes, String encoding)
      throws UnsupportedEncodingException {
    return new String(decodeToArray(bytes, encoding));
  }

  static final char[] decodeToArray(byte[] bytes, String encoding)
      throws UnsupportedEncodingException {
    if (encoding.equals(US_ASCII)) {
      char[] array = new char[bytes.length];
      for (int i = 0; i < array.length; i++) {
        array[i] = (char) bytes[i];
      }
      return array;
    }
    try {
      CharBuffer buffer = getDecoder(encoding).decode(ByteBuffer.wrap(bytes));
      char[] array = new char[buffer.limit()];
      System.arraycopy(buffer.array(), 0, array, 0, array.length);
      return array;
    } catch (Exception nioException) {
      return (new String(bytes, encoding)).toCharArray();
    }
  }

  public static final String decodePassword(String encodedPassword)
      throws Exception {
    StringBuilder buffer = new StringBuilder(encodedPassword.substring(3));
    return decodeBASE64(buffer.reverse().toString().replace('$', '=')).trim();
  }

  static final CharsetDecoder getDecoder(String encoding) {
    return getCharset(encoding).newDecoder();
  }

  public static final long stringToLong(String string)
      throws NumberFormatException {
    StringBuffer buffer = new StringBuffer(string.toLowerCase());
    long unit = resolveUnit(buffer);
    long value = Long.parseLong(buffer.toString().trim());
    if (unit != 1) {
      value *= unit;
    }
    return value;
  }

  private static final long resolveUnit(StringBuffer buffer) {
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

  // --- FAST BASE64 PASSWORD ENCODER/DECODER ---

  /**
   * Decodes a BASE64-encoded string.
   * @param string BASE64 string
   * @return String the decoded bytes
   */
  public static final String decodeBASE64(String string) throws Exception {
    char[] data = string.toCharArray();
    int tempLen = data.length;
    for (char aData : data) {
      if ((aData > 255) || BASE64_CHARS[aData] < 0) {
        --tempLen;
      }
    }
    int len = (tempLen / 4) * 3;
    if ((tempLen % 4) == 3) {
      len += 2;
    }
    if ((tempLen % 4) == 2) {
      len += 1;
    }
    byte[] bytes = new byte[len];
    int shift = 0;
    int accum = 0;
    int index = 0;
    int value;
    for (char aData : data) {
      value = (aData > 255) ? -1 : BASE64_CHARS[aData];
      if (value >= 0) {
        accum <<= 6;
        shift += 6;
        accum |= value;
        if (shift >= 8) {
          shift -= 8;
          bytes[index++] = (byte) ((accum >> shift) & 0xff);
        }
      }
    }
    return decodeToString(bytes, UTF_8);
  }

  /**
   * Encode the input bytes into BASE64 format.
   * @param data - byte array to encode
   * @return encoded string
   */
  public static final String encodeBASE64(byte[] data) throws Exception {
    char[] chars = new char[((data.length + 2) / 3) * 4];
    for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
      boolean quad = false;
      boolean trip = false;
      int val = (0xFF & data[i]);
      val <<= 8;
      if ((i + 1) < data.length) {
        val |= (0xFF & data[i + 1]);
        trip = true;
      }
      val <<= 8;
      if ((i + 2) < data.length) {
        val |= (0xFF & data[i + 2]);
        quad = true;
      }
      chars[index + 3] = ALPHABETS[(quad ? (val & 0x3F) : 64)];
      val >>= 6;
      chars[index + 2] = ALPHABETS[(trip ? (val & 0x3F) : 64)];
      val >>= 6;
      chars[index + 1] = ALPHABETS[val & 0x3F];
      val >>= 6;
      chars[index] = ALPHABETS[val & 0x3F];
    }
    return new String(chars);
  }

}
