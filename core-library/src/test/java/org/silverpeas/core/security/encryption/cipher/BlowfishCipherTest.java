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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.Charsets;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests on the Blowfish cipher as implemented in Silverpeas.
 */
@EnableSilverTestEnv
public class BlowfishCipherTest {

  private static final String KNOWN_PLAIN_TEXT = "Il était une fois un joli petit pois tout argenté";
  private Cipher blowfish;
  private CipherKey key;

  public BlowfishCipherTest() {
  }

  @BeforeEach
  public void setUp() throws Exception {
    blowfish = initBlowhfishCipher();
    key = initCipherKey();
  }

  @Test
  public void testGetAlgorithmName() throws Exception {
    assertThat(blowfish.getAlgorithmName(), is(CryptographicAlgorithmName.Blowfish));
  }

  @Test
  public void testEncrypt() throws Exception {
    byte[] encryptedText = blowfish.encrypt(KNOWN_PLAIN_TEXT, key);
    byte[] expectedEncryptedText = encryptTextWithKey(KNOWN_PLAIN_TEXT, key.getRawKey());
    assertThat(encryptedText, is(expectedEncryptedText));
  }

  @Test
  public void testDecrypt() throws Exception {
    byte[] expectedEncryptedText = encryptTextWithKey(KNOWN_PLAIN_TEXT, key.getRawKey());
    String decryptedText = blowfish.decrypt(expectedEncryptedText, key);
    assertThat(decryptedText, is(KNOWN_PLAIN_TEXT));
  }

  private Cipher initBlowhfishCipher() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher cipher = cipherFactory.getCipher(CryptographicAlgorithmName.Blowfish);
    assertNotNull(cipher);
    return cipher;
  }

  private CipherKey initCipherKey() throws Exception {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
    SecretKey secretKey = keyGenerator.generateKey();
    return CipherKey.aKeyFromBinary(secretKey.getEncoded());
  }

  private byte[] encryptTextWithKey(String text, byte[] encodedKey)
      throws Exception {
    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("Blowfish");
    BlowfishKey blowfishKey = new BlowfishKey(encodedKey);
    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, blowfishKey);
    return cipher.doFinal(text.getBytes(Charsets.UTF_8));
  }
}
