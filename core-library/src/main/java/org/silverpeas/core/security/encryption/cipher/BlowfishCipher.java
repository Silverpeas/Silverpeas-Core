/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption.cipher;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.Charsets;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/**
 * Blowfish is a keyed, symmetric block cipher, designed in 1993 by Bruce Schneier and included in a
 * large number of cipher suites and encryption products. Blowfish provides a good encryption rate
 * in software and no effective cryptanalysis of it has been found to date. However, the Advanced
 * Encryption Standard now receives more attention.
 *
 * Blowfish was one of the first secure block ciphers not subject to any patents and therefore
 * freely available for anyone to use. This benefit has contributed to its popularity in
 * cryptographic software.
 *
 * This implementation wraps the Blowfish cipher provided in the Java Cryptography API and it
 * performs the redundant operations in the encryption and in the decryption.
 */
public class BlowfishCipher implements Cipher {

  private final javax.crypto.Cipher cipher;
  private BlowfishKey blowfishKey = null;

  protected BlowfishCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
    blowfishKey = new BlowfishKey();
    cipher = javax.crypto.Cipher.getInstance(CryptographicAlgorithmName.Blowfish.name());
  }

  /**
   * Gets the name of the algorithm of the cipher.
   *
   * @return the algorithm name.
   */
  @Override
  public CryptographicAlgorithmName getAlgorithmName() {
    return CryptographicAlgorithmName.Blowfish;
  }

  @Override
  public byte[] encrypt(String data, CipherKey keyCode) throws CryptoException {

    byte[] cipherText;
    try {
      byte[] cipherBytes = data.getBytes();
      BlowfishKey key;
      if (keyCode == null) {
        key = this.getSymmetricKey();
      } else {
        key = new BlowfishKey(keyCode.getRawKey());
      }
      synchronized (cipher) {
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        cipherText = cipher.doFinal(cipherBytes);
      }
    } catch (Exception e) {
      throw new CryptoException(CryptoException.ENCRYPTION_FAILURE, e);
    }

    return cipherText;
  }

  @Override
  public String decrypt(byte[] cipher, CipherKey keyCode) throws CryptoException {
    SilverTrace
        .info("util", "BlowfishCipher.decrypt", "root.MSG_GEN_ENTER_METHOD");
    String uncrypted;
    byte[] newPlainText;
    try {
      BlowfishKey key;
      if (keyCode == null) {
        key = this.getSymmetricKey();
      } else {
        key = new BlowfishKey(keyCode.getRawKey());
      }
      synchronized (cipher) {
        this.cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
        newPlainText = this.cipher.doFinal(cipher);
      }
      uncrypted = new String(newPlainText, Charsets.UTF_8);

    } catch (Exception e) {
      throw new CryptoException(CryptoException.DECRYPTION_FAILURE, e);
    }

    return uncrypted;
  }

  private BlowfishKey getSymmetricKey() {
    return blowfishKey;
  }

  @Override
  public CipherKey generateCipherKey() throws CryptoException {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(CryptographicAlgorithmName.Blowfish.
          name());
      SecretKey key = keyGenerator.generateKey();
      return CipherKey.aKeyFromBinary(key.getEncoded());
    } catch (NoSuchAlgorithmException ex) {
      throw new CryptoException(CryptoException.KEY_GENERATION_FAILURE, ex);
    }
  }
}
