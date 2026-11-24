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
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableSilverTestEnv(context = JEETestContext.class)
class Argon2idEncryptionTest {

  private final Argon2idEncryption encryption = new Argon2idEncryption();

  @Test
  void roundTripAndRandomSalt() {
    String first = encryption.encrypt("Secret15599!");
    String second = encryption.encrypt("Secret15599!");
    assertNotEquals(first, second);
    assertTrue(first.startsWith("$argon2id$v=19$"));
    assertTrue(encryption.doUnderstandDigest(first));
    encryption.check("Secret15599!", first);
    encryption.check("Secret15599!", second);
  }

  @Test
  void explicitSaltProducesDeterministicDigest() {
    byte[] salt = "0123456789abcdef".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    String first = encryption.encrypt("Secret15599!", salt);
    assertTrue(first.startsWith("$argon2id$"));
    assertArrayEquals(salt, encryption.getSaltUsedInDigest(first));
    assertEquals(first, encryption.encrypt("Secret15599!", salt));
  }

  @Test
  void acceptsImportedHashWithEightByteSalt() {
    String digest = "$argon2id$v=19$m=19456,t=2,p=1$MTIzNDU2Nzg$"
        + "vUXEAWfR0du+T68wyb4pQPw7fXicOxdGJaztAnHsyQU";
    encryption.check("Secret15599!", digest);
  }

  @Test
  void explicitSaltMustFitStoredDigest() {
    String digest = encryption.encrypt("Secret15599!", new byte[64]);
    encryption.check("Secret15599!", digest);
    assertThrows(IllegalArgumentException.class,
        () -> encryption.encrypt("Secret15599!", new byte[65]));
  }

  @Test
  void invalidPasswordDoesNotLeakSecrets() {
    String digest = encryption.encrypt("Secret15599!");
    AssertionError error = assertThrows(AssertionError.class,
        () -> encryption.check("Wrong15599!", digest));
    assertFalse(error.getMessage().contains("Wrong15599!"));
    assertFalse(error.getMessage().contains(digest));
  }

  @Test
  void malformedDigestIsRejectedWithoutEchoingIt() {
    String malformed = "$argon2id$v=19$m=999999999,t=2,p=1$c2FsdA$aGFzaA";
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> encryption.check("Secret15599!", malformed));
    assertFalse(error.getMessage().contains(malformed));
  }
  @Test
  void digestFitsSqlDomainPasswordColumn() {
    String digest = encryption.encrypt("Secret15599!");
    assertEquals(97, digest.length());
    assertTrue(digest.length() <= 123, "DomainSP_User.password is VARCHAR(123)");
  }

  @Test
  void unicodePasswordCanBeVerified() {
    String password = "Mot de passe éà漢字🔒";
    String digest = encryption.encrypt(password);
    encryption.check(password, digest);
    AssertionError expected = assertThrows(AssertionError.class,
        () -> encryption.check(password + "!", digest));
    assertEquals("The password does not match the stored digest", expected.getMessage());
  }

  @Test
  void malformedOrExcessiveParametersAreRejected() {
    String valid = encryption.encrypt("Secret15599!");
    String excessiveMemory = valid.replace("m=19456", "m=999999999");
    String unsupportedVersion = valid.replace("v=19", "v=16");
    for (String malformed : new String[]{excessiveMemory, unsupportedVersion}) {
      IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
          () -> encryption.check("Secret15599!", malformed));
      assertFalse(expected.getMessage().contains(malformed));
    }
  }

}
