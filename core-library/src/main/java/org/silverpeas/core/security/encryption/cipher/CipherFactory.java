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
package org.silverpeas.core.security.encryption.cipher;

import java.util.EnumMap;
import java.util.Map;

import static org.silverpeas.core.util.logging.SilverLogger.*;

/**
 * A factory of the ciphers supported by Silverpeas.
 */
public class CipherFactory {

  private static CipherFactory instance;
  private final Map<CryptographicAlgorithmName, Cipher> ciphers =
      new EnumMap<>(CryptographicAlgorithmName.class);

  private CipherFactory() {
    // we load all the ciphers supported by the Silverpeas Cryptography API
    try {
      ciphers.put(CryptographicAlgorithmName.BLOWFISH, new BlowfishCipher());
    } catch (Exception ex) {
      getLogger(CipherFactory.class).error(ex.getMessage(), ex);
    }
    ciphers.put(CryptographicAlgorithmName.CMS, new CMSCipher());
    ciphers.put(CryptographicAlgorithmName.CAST5, new CAST5Cipher());
    ciphers.put(CryptographicAlgorithmName.AES, new AESCipher());
  }

  public static synchronized CipherFactory getFactory() {
    if (instance == null) {
      instance = new CipherFactory();
    }
    return instance;
  }

  /**
   * Gets the cipher identified by the specified cryptographic algorithm name.
   * @param cipherName a name of a cryptographic cipher.
   * @return the cipher that matches the specified name or null if no such cipher exists with the
   * given algorithm name.
   */
  public Cipher getCipher(CryptographicAlgorithmName cipherName) {
    return ciphers.get(cipherName);
  }
}
