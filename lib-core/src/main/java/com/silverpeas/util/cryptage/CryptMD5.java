/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.util.cryptage;

import com.google.common.io.Closeables;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The MD5 algorithm (Message Digest 5).
 * <p/>
 * It is a one-way hashing function that compute a 128-bit digest of a message (generally a
 * file). It is not for encrypting password but for generating a digest of a content in order to
 * sign it and to ensure its integrity.
 * <p/>
 * A first flaw (possibility to create collisions at the demand) was discovered in 1996. In
 * 2004, a chinese team broke it by discovering full collisions. Since, it was replaced first by
 * the SHA-1 algorithm and now the SHA-2 algorithms are used.
 */
public class CryptMD5 {

  /**
   * Encrypts the specified text in MD5.
   * @param original the text to encrypt
   * @return the digest of the text in a 32-bits hexadecimal.
   */
  public static String encrypt(String original) throws UtilException {

    byte[] uniqueKey = original.getBytes();

    // on récupère un objet qui permettra de crypter la chaine
    byte[] hash;
    try {
      hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
    } catch (NoSuchAlgorithmException e) {
      // TODO utiliser le CryptageException
      // throw new CryptageException
      throw new UtilException("CryptMD5.encrypt()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    }

    StringBuilder hashString = new StringBuilder();
    String hex;
    for (byte aHash : hash) {
      hex = Integer.toHexString(aHash);
      if (hex.length() == 1) {
        hashString.append('0');
        hashString.append(hex.charAt(hex.length() - 1));
      } else {
        hashString.append(hex.substring(hex.length() - 2));
      }
    }
    return hashString.toString();
  }

  /*
   * Compute the MD5 hash of a file.
   * @param file: the file to be MD5 hashed.
   * @return the MD5 hash as a String.
   */
  public static String encrypt(File file) throws UtilException {
    MessageDigest digest;
    FileInputStream fileInputStream = null;
    try {
      digest = MessageDigest.getInstance("MD5");
      fileInputStream = new FileInputStream(file);
      byte[] buffer = new byte[8];
      int c = 0;
      while ((c = fileInputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, c);
      }
      byte[] signature = digest.digest();
      return getHexString(signature);
    } catch (NoSuchAlgorithmException e) {
      throw new UtilException("CryptMD5.encrypt()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    } catch (IOException e) {
      throw new UtilException("CryptMD5.encrypt()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    } finally {
      if (fileInputStream != null) {
        Closeables.closeQuietly(fileInputStream);
      }
    }
  }

  protected static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2',
      (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
      (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
      (byte) 'f' };

  protected static String getHexString(byte[] rawData) throws UnsupportedEncodingException {
    byte[] hex = new byte[2 * rawData.length];
    int index = 0;
    for (byte raw : rawData) {
      int v = raw & 0xFF;
      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
    }
    return new String(hex, "ASCII");
  }
}