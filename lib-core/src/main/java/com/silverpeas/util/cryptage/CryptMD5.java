/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.util.cryptage;

import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class CryptMD5 {

  /*
   * Encode la chaine passée en paramètre avec l’algorithme MD5
   * @param original : la chaine à encoder
   * @return la valeur (string) hexadécimale sur 32 bits
   */
  public static String crypt(String original) throws UtilException {

    byte[] uniqueKey = original.getBytes();

    // on récupère un objet qui permettra de crypter la chaine
    byte[] hash;
    try {
      hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
    } catch (NoSuchAlgorithmException e) {
      // TODO utiliser le CryptageException
      // throw new CryptageException
      throw new UtilException("CryptMD5.crypt()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    }

    StringBuilder hashString = new StringBuilder();
    String hex;
    for (int i = 0; i < hash.length; ++i) {
      hex = Integer.toHexString(hash[i]);

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
  public static String hash(File file) throws UtilException {
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
      throw new UtilException("CryptMD5.hash()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    } catch (IOException e) {
      throw new UtilException("CryptMD5.hash()", SilverpeasException.ERROR,
          "root.EX_NO_MESSAGE", e);
    } finally {
      if (fileInputStream != null) {
        Closeables.closeQuietly(fileInputStream);
      }
    }
  }

  static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2',
      (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
      (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
      (byte) 'f' };

  protected static String getHexString(byte[] raw)
      throws UnsupportedEncodingException {
    byte[] hex = new byte[2 * raw.length];
    int index = 0;

    for (int i = 0; i < raw.length; i++) {
      int v = raw[i] & 0xFF;
      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
    }
    return new String(hex, "ASCII");
  }
}