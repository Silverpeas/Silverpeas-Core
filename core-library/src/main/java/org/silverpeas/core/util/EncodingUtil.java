/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;

public class EncodingUtil extends StringUtils {

  private static final char[] PUNCTUATION =
      new char[]{'&', '\"', '\'', '{', '(', '[', '-', '|', '`', '_', '\\', '^', '@', ')', ']', '=',
          '+', '}', '?', ',', '.', ';', '/', ':', '!', '§', '%', '*', '$', '£', '€', '©', '²', '°',
          '¤'};

  /**
   * Method for trying to detect encoding.
   * @param data some data to try to detect the encoding.
   * @param declaredEncoding expected encoding.
   * @return the detected encoding.
   */
  public static String detectStringEncoding(byte[] data, String declaredEncoding)
      throws UnsupportedEncodingException {
    if (data != null) {
      String value = new String(data, declaredEncoding);
      if (hasEncodingToBeChecked(value)) {
        return findBestEncoding(data, declaredEncoding);
      }
    }
    return declaredEncoding;
  }

  /**
   * If the value contains one character which is neither an alphanumeric, neither a whitespace
   * neither a punctuation character, then the encoding has to be checked
   * @param value the value to verify.
   * @return true if the encoding of the given value has to be checked.
   */
  private static boolean hasEncodingToBeChecked(String value) {
    if (value != null) {
      char[] chars = value.toCharArray();
      for (char currentChar : chars) {
        if (!Character.isLetterOrDigit(currentChar) && !Character.isWhitespace(currentChar) &&
            !ArrayUtils.contains(PUNCTUATION, currentChar)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param data some data to try to detect the encoding.
   * @param declaredEncoding expected encoding.
   * @return the best encoding or the one declared if the encoding could not have to be guessed.
   * @throws UnsupportedEncodingException
   */
  private static String findBestEncoding(byte[] data, String declaredEncoding)
      throws UnsupportedEncodingException {
    final Set<String> supportedEncodings;
    if (CharEncoding.UTF_8.equals(declaredEncoding)) {
      supportedEncodings = detectMaybeEncoding(data, CharEncoding.ISO_8859_1);
    } else {
      supportedEncodings = detectMaybeEncoding(data, CharEncoding.UTF_8);
    }
    for (String encoding : supportedEncodings) {
      String encodedData = new String(data, encoding);
      if (!hasEncodingToBeChecked(encodedData)) {
        return encoding;
      }
    }
    return declaredEncoding;
  }

  /**
   * Method for trying to detect encoding
   * @param data some data to try to detect the encoding.
   * @param declaredEncoding expected encoding.
   * @return the possible encodings.
   */
  private static Set<String> detectMaybeEncoding(byte[] data, String declaredEncoding) {
    final CharsetDetector detector = new CharsetDetector();
    if (!StringUtil.isDefined(declaredEncoding)) {
      detector.setDeclaredEncoding(CharEncoding.ISO_8859_1);
    } else {
      detector.setDeclaredEncoding(declaredEncoding);
    }
    detector.setText(data);
    CharsetMatch[] detectedEnc = detector.detectAll();
    Set<String> encodings = new LinkedHashSet<>(detectedEnc.length);
    for (CharsetMatch detectedEncoding : detectedEnc) {
      encodings.add(detectedEncoding.getName());
    }
    return encodings;
  }

  /**
   * Decodes the specified text with hexadecimal values in bytes of those same values. The text is
   * considered to be in the UTF-8 charset.
   *
   * @param hexText the text with hexadecimal-based characters.
   * @return the binary representation of the text.
   * @throws ParseException if an odd number or illegal of characters is supplied.
   */
  public static byte[] fromHex(String hexText) throws ParseException {
    try {
      return Hex.decodeHex(hexText.toCharArray());
    } catch (Exception ex) {
      throw new ParseException(ex.getMessage(), -1);
    }
  }

  /**
   * Encodes the specified binary data into a String representing the hexadecimal values of each
   * byte in order. The String is in the UTF-8 charset.
   *
   * @param binaryData the binary data to concert in hexadecimal-based String.
   * @return a String representation of the binary data in Hexadecimal characters.
   */
  public static String asHex(byte[] binaryData) {
    return Hex.encodeHexString(binaryData);
  }

  private EncodingUtil() {
  }
}
