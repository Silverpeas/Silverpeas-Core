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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Argon2idEncryptionTest {

  private final Argon2idEncryption encryption = new Argon2idEncryption();

  @Test
  public void roundTripAndRandomSalt() {
    String first = encryption.encrypt("Secret15599!");
    String second = encryption.encrypt("Secret15599!");
    assertNotEquals(first, second);
    assertTrue(first.startsWith("$argon2id$v=19$"));
    assertTrue(encryption.doUnderstandDigest(first));
    encryption.check("Secret15599!", first);
    encryption.check("Secret15599!", second);
  }

  @Test
  public void explicitSaltProducesDeterministicDigest() {
    byte[] salt = "0123456789abcdef".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    String first = encryption.encrypt("Secret15599!", salt);
    assertTrue(first.startsWith("$argon2id$"));
    assertArrayEquals(salt, encryption.getSaltUsedInDigest(first));
    assertTrue(first.equals(encryption.encrypt("Secret15599!", salt)));
  }

  @Test
  public void invalidPasswordDoesNotLeakSecrets() {
    String digest = encryption.encrypt("Secret15599!");
    try {
      encryption.check("Wrong15599!", digest);
      fail("Invalid password accepted");
    } catch (AssertionError error) {
      assertFalse(error.getMessage().contains("Wrong15599!"));
      assertFalse(error.getMessage().contains(digest));
    }
  }

  @Test
  public void malformedDigestIsRejectedWithoutEchoingIt() {
    String malformed = "$argon2id$v=19$m=999999999,t=2,p=1$c2FsdA$aGFzaA";
    try {
      encryption.check("Secret15599!", malformed);
      fail("Malformed digest accepted");
    } catch (IllegalArgumentException error) {
      assertFalse(error.getMessage().contains(malformed));
    }
  }
}
