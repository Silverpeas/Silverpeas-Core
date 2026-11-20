/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.silverpeas.core.security.authentication.password.encryption;

import org.junit.Test;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.fail;

/**
 * Regression tests for credential disclosure and password salt generation.
 */
public class PasswordEncryptionSecurityTest {

  private static final String PASSWORD = "Test15599!Secret2026";
  private static final String WRONG_PASSWORD = "Wrong15599!Secret2026";

  @Test
  public void sha512CryptDoesNotExposeCredentialsOnMismatch() {
    assertMismatchDoesNotExposeCredentials(new UnixSHA512Encryption());
  }

  @Test
  public void md5CryptDoesNotExposeCredentialsOnMismatch() {
    assertMismatchDoesNotExposeCredentials(new UnixMD5Encryption());
  }

  @Test
  public void desCryptDoesNotExposeCredentialsOnMismatch() {
    assertMismatchDoesNotExposeCredentials(new UnixDESEncryption());
  }

  @Test
  public void sha512CryptGeneratesDifferentSalts() {
    assertDifferentSalts(new UnixSHA512Encryption(), true);
  }

  @Test
  public void md5CryptGeneratesDifferentSalts() {
    assertDifferentSalts(new UnixMD5Encryption(), true);
  }

  @Test
  public void desCryptGeneratesDifferentSalts() {
    assertDifferentSalts(new UnixDESEncryption(), false);
  }

  private static void assertMismatchDoesNotExposeCredentials(PasswordEncryption encryption) {
    String digest = encryption.encrypt(PASSWORD);
    encryption.check(PASSWORD, digest);
    try {
      encryption.check(WRONG_PASSWORD, digest);
      fail("An incorrect password must be rejected");
    } catch (AssertionError error) {
      String message = error.getMessage();
      assertEquals(PasswordEncryption.BAD_PASSWORD_MESSAGE, message);
      assertFalse(message.contains(PASSWORD));
      assertFalse(message.contains(WRONG_PASSWORD));
      assertFalse(message.contains(digest));
    }
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
    assertTrue("Random salts should produce multiple distinct digests", digests.size() > 1);
  }
}
