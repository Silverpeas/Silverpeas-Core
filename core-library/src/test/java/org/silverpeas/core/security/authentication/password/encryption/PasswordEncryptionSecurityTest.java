/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.security.authentication.password.encryption;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for credential disclosure and password salt generation.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PasswordEncryptionSecurityTest {

  private static final String PASSWORD = "Test15599!Secret2026";
  private static final String WRONG_PASSWORD = "Wrong15599!Secret2026";

  @Test
  void sha512CryptDoesNotExposeCredentialsOnMismatch() {
    assertMismatchDoesNotExposeCredentials(new UnixSHA512Encryption());
  }

  @Test
  void md5CryptDoesNotExposeCredentialsOnMismatch() {
    assertMismatchDoesNotExposeCredentials(new UnixMD5Encryption());
  }

  @Test
  void desCryptDoesNotExposeCredentialsOnMismatch() {
    assertMismatchDoesNotExposeCredentials(new UnixDESEncryption());
  }

  @Test
  void sha512CryptGeneratesDifferentSalts() {
    assertDifferentSalts(new UnixSHA512Encryption(), true);
  }

  @Test
  void md5CryptGeneratesDifferentSalts() {
    assertDifferentSalts(new UnixMD5Encryption(), true);
  }

  @Test
  void desCryptGeneratesDifferentSalts() {
    assertDifferentSalts(new UnixDESEncryption(), false);
  }

  private static void assertMismatchDoesNotExposeCredentials(PasswordEncryption encryption) {
    String digest = encryption.encrypt(PASSWORD);
    encryption.check(PASSWORD, digest);
    AssertionError error = assertThrows(AssertionError.class,
        () -> encryption.check(WRONG_PASSWORD, digest));
    String message = error.getMessage();
    assertEquals(PasswordEncryption.BAD_PASSWORD_MESSAGE, message);
    assertFalse(message.contains(PASSWORD));
    assertFalse(message.contains(WRONG_PASSWORD));
    assertFalse(message.contains(digest));
  }

  private static void assertDifferentSalts(PasswordEncryption encryption,
      boolean verifyDigestRecognition) {
    Set<String> digests = new HashSet<>();
    for (int i = 0; i < 12; i++) {
      String digest = encryption.encrypt(PASSWORD);
      digests.add(digest);
      if (verifyDigestRecognition) {
        assertTrue(encryption.doUnderstandDigest(digest));
      }
      encryption.check(PASSWORD, digest);
    }
    // A legacy DES salt has only 12 bits: collisions are possible even with SecureRandom.
    assertTrue(digests.size() > 1, "Random salts should produce multiple distinct digests");
  }
}
