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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SilverCryptFactorySymetric {
  public static final String ALGORITHM = "Blowfish";
  // Singleton pour gèrer une seule Map de trousseaux de clés
  private static SilverCryptFactorySymetric factory = null;
  private static Cipher cipher = null;
  private SilverCryptKeysSymetric silverCryptKeysSymetric = null;

  private SilverCryptFactorySymetric() {
  }

  static {
    try {
      cipher = Cipher.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    }
  }

  public static SilverCryptFactorySymetric getInstance() {
    if (factory == null)
      factory = new SilverCryptFactorySymetric();

    return factory;
  }

  public synchronized byte[] goCrypting(String stringUnCrypted) throws CryptageException {
    return goCrypting(stringUnCrypted, null);
  }

  public synchronized byte[] goCrypting(String stringUnCrypted, SilverCryptKeysSymetric symetricKeys)
      throws CryptageException {
    SilverTrace.info("util", "SilverCryptFactorySymetric.goCrypting",
        "root.MSG_GEN_ENTER_METHOD", "stringUnCrypted = " + stringUnCrypted);
    // String crypted = "";
    byte[] cipherText = null;
    try {
      byte[] cipherBytes = stringUnCrypted.getBytes();
      // encrypt using the key and the plaintext
      if (symetricKeys == null)
        symetricKeys = this.getSymetricKeys();
      cipher.init(Cipher.ENCRYPT_MODE, symetricKeys.getKey());
      SilverTrace.debug("util", "SilverCryptFactorySymetric.goCrypting",
          "root.MSG_GEN_PARAM_VALUE", "After init");
      cipherText = cipher.doFinal(cipherBytes);
    } catch (Exception e) {
      throw new CryptageException("SilverCryptFactory.goCrypting",
          SilverpeasException.ERROR, "util.CRYPT_FAILED", e);
    }
    SilverTrace.info("util", "SilverCryptFactorySymetric.goCrypting",
        "root.MSG_GEN_EXIT_METHOD", "cipherText = " + cipherText);
    // return crypted;
    return cipherText;
  }

  public synchronized String goUnCrypting(byte[] cipherBytes) throws CryptageException {
    return goUnCrypting(cipherBytes, null);
  }

  public synchronized String goUnCrypting(byte[] cipherBytes, SilverCryptKeysSymetric symetricKeys)
      throws CryptageException {
    SilverTrace.info("util", "SilverCryptFactorySymetric.goUnCrypting",
        "root.MSG_GEN_ENTER_METHOD");
    String uncrypted = "";
    try {
      if (symetricKeys == null)
        symetricKeys = this.getSymetricKeys();
      cipher.init(Cipher.DECRYPT_MODE, symetricKeys.getKey());

      SilverTrace.debug("util", "SilverCryptFactorySymetric.goUnCrypting",
          "root.MSG_GEN_PARAM_VALUE", "After init");

      byte[] newPlainText = cipher.doFinal(cipherBytes);
      uncrypted = new String(newPlainText, "UTF8");

    } catch (Exception e) {
      throw new CryptageException("SilverCryptFactory.goUnCrypting",
          SilverpeasException.ERROR, "util.UNCRYPT_FAILED", e);
    }
    SilverTrace.info("util", "SilverCryptFactorySymetric.goUnCrypting",
        "root.MSG_GEN_EXIT_METHOD", "uncrypted = " + uncrypted);
    return uncrypted;
  }

  public SilverCryptKeysSymetric getSymetricKeys() throws CryptageException {
    // récupération du trousseau de clé!
    if (silverCryptKeysSymetric == null) {
      silverCryptKeysSymetric = new SilverCryptKeysSymetric();
    }
    return silverCryptKeysSymetric;
  }
}