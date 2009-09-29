/* 
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License 
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file  
 * at legal/CDDLv1.0.txt.                                                           
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */

/**
 * The <code>Base64</code> class provides an implementation of the Base64
 * algorithm.
 */

package com.silverpeas.portlets.context.window.impl;

class Base64 {

  private static byte[] DecodeMap;
  private static byte[] theMap = { (byte) 'A', (byte) 'B', (byte) 'C',
      (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I',
      (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O',
      (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
      (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a',
      (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
      (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm',
      (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's',
      (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y',
      (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
      (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+',
      (byte) '/' };

  // the Base64 mapping

  static {
    DecodeMap = new byte[128];
    for (int i = 0; i < theMap.length; i++) {
      DecodeMap[theMap[i]] = (byte) i;
    }
  }

  /**
   * Convert the String into an array of bytes then send the bytes through the
   * encoding algorithim. Return the encoded data as a String object.
   * 
   * @param theData
   *          the data that is to be Base64 encoded.
   * @return String the base64 encoded data as a String object.
   */
  public static String encode(String theData) {

    if (theData == null) {
      return null;
    }

    byte[] theBytes = theData.getBytes();
    theData = new String(encode(theBytes));

    return theData;
  }

  /**
   * This is where the actual encoding takes place. returns a byte array of the
   * encoded bytes.
   * 
   * @param theBytes
   *          a byte array that is to be Base64 encoded.
   * @return byte[] the Base64 encoded data.
   */
  public static byte[] encode(byte[] theBytes) {
    int i = 0, place = (-1);
    byte byte1, byte2, byte3;
    byte[] encodedBytes = new byte[((theBytes.length + 2) / 3) * 4];

    while ((i + 2) < theBytes.length) {
      byte1 = theBytes[i];
      byte2 = theBytes[++i];
      byte3 = theBytes[++i];

      encodedBytes[++place] = theMap[(byte1 >>> 2) & 0x3F];
      encodedBytes[++place] = theMap[((byte1 << 4) & 0x30)
          + ((byte2 >>> 4) & 0xf)];
      encodedBytes[++place] = theMap[((byte2 << 2) & 0x3c)
          + ((byte3 >>> 6) & 0x3)];
      encodedBytes[++place] = theMap[byte3 & 0x3F];
      ++i;
    }

    if ((theBytes.length - 1 - i) == 0) {
      byte1 = theBytes[i];
      byte2 = 0;
      byte3 = 0;

      encodedBytes[++place] = theMap[(byte1 >>> 2) & 0x3F];
      encodedBytes[++place] = theMap[((byte1 << 4) & 0x30)
          + ((byte2 >>> 4) & 0xf)];
      encodedBytes[++place] = (byte) '=';
      encodedBytes[++place] = (byte) '=';
    } else if ((theBytes.length - 1 - i) == 1) {
      byte1 = theBytes[i];
      byte2 = theBytes[i + 1];
      byte3 = 0;

      encodedBytes[++place] = theMap[(byte1 >>> 2) & 0x3F];
      encodedBytes[++place] = theMap[((byte1 << 4) & 0x30)
          + ((byte2 >>> 4) & 0xf)];
      encodedBytes[++place] = theMap[((byte2 << 2) & 0x3c)
          + ((byte3 >>> 6) & 0x3)];
      encodedBytes[++place] = (byte) '=';
    }

    // add padding if necessary;
    while ((++place) < encodedBytes.length) {
      encodedBytes[place] = (byte) '=';
    }

    return encodedBytes;
  }

  /**
   * This is where the base 64 decoding is done.
   * 
   * @param encData
   *          the Base64 encoded data that is to be decoded.
   * @return byte[] the decoded data.
   */
  public static byte[] decode(byte[] encData) {
    if (encData == null) {
      return null;
    }
    if (encData.length == 0) {
      return new byte[0];
    }

    int tail = encData.length;
    while (encData[tail - 1] == '=') {
      tail--;
    }

    byte decData[] = new byte[tail - encData.length / 4];

    // ascii printable to 0-63 conversion
    for (int idx = 0; idx < encData.length; idx++) {
      encData[idx] = DecodeMap[encData[idx]];
    }

    // 4-byte to 3-byte conversion
    int sidx, didx;
    for (sidx = 0, didx = 0; didx < decData.length - 2; sidx += 4, didx += 3) {
      decData[didx] = (byte) (((encData[sidx] << 2) & 255) | ((encData[sidx + 1] >>> 4) & 003));
      decData[didx + 1] = (byte) (((encData[sidx + 1] << 4) & 255) | ((encData[sidx + 2] >>> 2) & 017));
      decData[didx + 2] = (byte) (((encData[sidx + 2] << 6) & 255) | (encData[sidx + 3] & 077));

    }

    if (didx < decData.length) {
      decData[didx] = (byte) (((encData[sidx] << 2) & 255) | ((encData[sidx + 1] >>> 4) & 003));
    }
    if (++didx < decData.length) {
      decData[didx] = (byte) (((encData[sidx + 1] << 4) & 255) | ((encData[sidx + 2] >>> 2) & 017));
    }

    return decData;
  }
}