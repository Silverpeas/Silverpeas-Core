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

package org.silverpeas.core.web.authentication;

/**
 * Custom encryption algorithm of the user credentials before storing them in cookies.
 */
public class CookieCredentialEncryption implements CredentialEncryption {
  public CookieCredentialEncryption() {
  }

  /**
   * Simple encode for cookie value
   */
  public String encode(String stringToEncode) {

    StringBuilder hashString = new StringBuilder();
    char[] uniqueKey = stringToEncode.toCharArray();
    for (char anUniqueKey : uniqueKey) {
      String carInt = Integer.toString(anUniqueKey * 3);
      String lg = Integer.toString(carInt.length());
      hashString.append(lg).append(carInt);
    }
    hashString.reverse();

    return hashString.toString();
  }

  /**
   * Simple decode for cookie value
   * @param encodedText : the text to decode.
   */
  public String decode(String encodedText) {

    int pos = 0;
    String reverseEncodedText = new StringBuffer(encodedText).reverse()
        .toString();
    StringBuilder hashString = new StringBuilder();
    for (int i = 0; i + pos < reverseEncodedText.length(); i++) {
      int lg = Integer.parseInt(reverseEncodedText.substring(i + pos, i + pos + 1));
      String car = reverseEncodedText.substring(i + pos + 1, i + pos + 1 + lg);
      pos = pos + lg;
      hashString.append((char) (Integer.parseInt(car) / 3));
    }

    return hashString.toString();
  }

  /**
   * XOR Decrypt for credential parameters (login and password)
   * @param str : String to decode
   * @param key : key for decoding
   * @param extraCrypt : add simple encode (see decode(String))
   */
  public String decode(String str, String key, boolean extraCrypt) {
    //
    String asciiChar_string = "";
    for (int i = 0; i < key.length(); i++) {
      int asciiCode = key.charAt(i);
      asciiChar_string += asciiCode;
    }
    String prand = asciiChar_string;

    int sPos = new Double(Math.floor(prand.length() / 5)).intValue();
    StringBuilder stringMult = new StringBuilder();
    stringMult.append(prand.charAt(sPos)).append(prand.charAt(sPos * 2))
        .append(prand.charAt(sPos * 3)).append(prand.charAt(sPos * 4)).append(
        prand.charAt(sPos * 5));

    int mult = Integer.parseInt(stringMult.toString());

    int incr = Math.round(key.length() / 2);
    double modu = Math.pow(2, 127) - 1;
    int salt = Integer.parseInt(str.substring(str.length() - 8, str.length()),
        16);

    str = str.substring(0, str.length() - 8);
    prand += salt;
    double prandInt = Double.parseDouble(prand);
    prandInt = (mult * prandInt + incr) % modu;

    int dec_chrInt;

    StringBuilder hashString = new StringBuilder();
    for (int i = 0; i < str.length(); i += 2) {
      dec_chrInt = Integer.parseInt(str.substring(i, i + 2), 16)
          ^ new Double(Math.floor((prandInt / modu) * 255)).intValue();
      hashString.append((char) dec_chrInt);
      prandInt = (mult * prandInt + incr) % modu;
    }
    String decStr = hashString.toString();
    String decStrFinal = decStr;
    if (extraCrypt) {
      decStrFinal = decode(decStr);
    }
    return decStrFinal;
  }
}