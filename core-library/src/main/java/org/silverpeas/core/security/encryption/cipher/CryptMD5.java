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
package org.silverpeas.core.security.encryption.cipher;

import org.apache.commons.codec.binary.Hex;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.Charsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The MD5 algorithm (Message Digest 5).
 * <p>
 * It is a one-way hashing function that compute a 128-bit digest of a message (generally a file).
 * It is not for encrypting password but for generating a digest of a content in order to sign it
 * and to ensure its integrity.
 * <p>
 * A first flaw (possibility to create collisions at the demand) was discovered in 1996. In
 * 2004, a chinese team broke it by discovering full collisions. Since, it was replaced first by
 * the SHA-1 algorithm and now the SHA-2 algorithms are used (SHA-256, SHA-512).
 */
public class CryptMD5 {

  private CryptMD5() {

  }

  /**
   * Encrypts the specified text in MD5.
   *
   * @param original the text to encrypt
   * @return the digest of the text in a 32-bits hexadecimal.
   * @throws SilverpeasRuntimeException if an error occurs.
   */
  public static String encrypt(String original) throws SilverpeasRuntimeException {
    byte[] uniqueKey = original.getBytes(Charsets.UTF_8);
    try {
      byte[] hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
      return Hex.encodeHexString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new SilverpeasRuntimeException(e);
    }

  }

  /**
   * Computes the MD5 hash of a file.
   * @param file the file to be MD5 hashed.
   * @return the MD5 hash as a String.
   * @throws SilverpeasRuntimeException if an error occurs.
   */
  public static String encrypt(File file) throws SilverpeasRuntimeException {
    try(InputStream fileInputStream = new FileInputStream(file)) {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8];
      int c = 0;
      while ((c = fileInputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, c);
      }
      byte[] signature = digest.digest();
      return Hex.encodeHexString(signature);
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}
