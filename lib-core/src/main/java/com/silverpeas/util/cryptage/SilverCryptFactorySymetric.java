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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class SilverCryptFactorySymetric {

  public static final String ALGORITHM = "Blowfish";
  /**
   * Singleton managing the keyring.
   **/
  private static SilverCryptFactorySymetric factory = null;
  private static Cipher cipher = null;
  private SilverCryptKeysSymetric silverCryptKeysSymetric = null;

  private SilverCryptFactorySymetric() {
  }

  static {
    try {
      cipher = Cipher.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      SilverTrace.error("util", "SilverCryptFactorySymetric.NoSuchAlgorithm",
          "root.MSG_GEN_PARAM_VALUE", "In init", e);
    } catch (NoSuchPaddingException e) {
      SilverTrace.error("util", "SilverCryptFactorySymetric.NoSuchPadding",
          "root.MSG_GEN_PARAM_VALUE", "In init", e);
    }
  }

  public static SilverCryptFactorySymetric getInstance() {
    synchronized (SilverCryptFactorySymetric.class) {
      if (factory == null) {
        factory = new SilverCryptFactorySymetric();
      }
    }
    return factory;
  }

  public synchronized byte[] goCrypting(String stringUnCrypted) throws CryptageException {
    return goCrypting(stringUnCrypted, null);
  }

  public synchronized byte[] goCrypting(String stringUnCrypted, SilverCryptKeysSymetric symetricKeys)
      throws CryptageException {
    SilverTrace.info("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_ENTER_METHOD",
        "stringUnCrypted = " + stringUnCrypted);
    byte[] cipherText = null;
    try {
      byte[] cipherBytes = stringUnCrypted.getBytes();
      SilverCryptKeysSymetric keys = symetricKeys;
      if (symetricKeys == null) {
        keys = this.getSymetricKeys();
      }
      cipher.init(Cipher.ENCRYPT_MODE, keys.getKey());
      SilverTrace.debug("util", "SilverCryptFactorySymetric.goCrypting",
          "root.MSG_GEN_PARAM_VALUE",
          "After init");
      cipherText = cipher.doFinal(cipherBytes);
    } catch (Exception e) {
      throw new CryptageException("SilverCryptFactory.goCrypting", SilverpeasException.ERROR,
          "util.CRYPT_FAILED", e);
    }
    SilverTrace.info("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_EXIT_METHOD",
        "cipherText = " + Arrays.toString(cipherText));
    return cipherText;
  }

  public synchronized String goUnCrypting(byte[] cipherBytes) throws CryptageException {
    return goUnCrypting(cipherBytes, null);
  }

  public synchronized String goUnCrypting(byte[] cipherBytes, SilverCryptKeysSymetric symetricKeys)
      throws CryptageException {
    SilverTrace
        .info("util", "SilverCryptFactorySymetric.goUnCrypting", "root.MSG_GEN_ENTER_METHOD");
    String uncrypted = "";
    try {
      SilverCryptKeysSymetric keys = symetricKeys;
      if (symetricKeys == null) {
        keys = this.getSymetricKeys();
      }
      cipher.init(Cipher.DECRYPT_MODE, keys.getKey());
      SilverTrace.debug("util", "SilverCryptFactorySymetric.goUnCrypting",
          "root.MSG_GEN_PARAM_VALUE", "After init");

      byte[] newPlainText = cipher.doFinal(cipherBytes);
      uncrypted = new String(newPlainText, "UTF8");

    } catch (Exception e) {
      throw new CryptageException("SilverCryptFactory.goUnCrypting", SilverpeasException.ERROR,
          "util.UNCRYPT_FAILED", e);
    }
    SilverTrace.info("util", "SilverCryptFactorySymetric.goUnCrypting",
        "root.MSG_GEN_EXIT_METHOD", "uncrypted = " + uncrypted);
    return uncrypted;
  }

  /**
   * Returns the keyring.
   * @return the keyring.
   * @throws CryptageException
   */
  public SilverCryptKeysSymetric getSymetricKeys() throws CryptageException {
    if (silverCryptKeysSymetric == null) {
      silverCryptKeysSymetric = new SilverCryptKeysSymetric();
    }
    return silverCryptKeysSymetric;
  }
}
