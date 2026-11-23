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

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Argon2id password hashing using the PHC string representation.
 */
@Bean
public class Argon2idEncryption implements PasswordEncryption {

  private static final int MEMORY_KIB = 19456;
  private static final int ITERATIONS = 2;
  private static final int PARALLELISM = 1;
  private static final int SALT_LENGTH = 16;
  private static final int MIN_STORED_SALT_LENGTH = 8;
  private static final int MAX_SALT_LENGTH = 64;
  private static final int HASH_LENGTH = 32;
  private static final int MAX_MEMORY_KIB = 262144;
  private static final int MAX_ITERATIONS = 10;
  private static final int MAX_PARALLELISM = 4;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Pattern PHC = Pattern.compile(
      "^\\$argon2id\\$v=19\\$m=([0-9]+),t=([0-9]+),p=([0-9]+)\\$([A-Za-z0-9+/]+)\\$([A-Za-z0-9+/]+)$");

  @Override
  public String encrypt(String password) {
    byte[] salt = new byte[SALT_LENGTH];
    RANDOM.nextBytes(salt);
    return encrypt(password, salt);
  }

  @Override
  public String encrypt(String password, byte[] salt) {
    if (salt == null || salt.length == 0) {
      return encrypt(password);
    }
    if (salt.length < SALT_LENGTH || salt.length > MAX_SALT_LENGTH) {
      throw new IllegalArgumentException("Invalid Argon2id salt length");
    }
    byte[] hash = derive(password, salt, MEMORY_KIB, ITERATIONS, PARALLELISM, HASH_LENGTH);
    Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
    return "$argon2id$v=19$m=" + MEMORY_KIB + ",t=" + ITERATIONS + ",p=" + PARALLELISM
        + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
  }

  @Override
  public void check(String password, String digest) throws AssertionError {
    ParsedDigest parsed = parse(digest);
    byte[] computed = derive(password, parsed.salt(), parsed.memory(), parsed.iterations(),
        parsed.parallelism(), parsed.hash().length);
    if (!MessageDigest.isEqual(computed, parsed.hash())) {
      throw new AssertionError(BAD_PASSWORD_MESSAGE);
    }
  }

  @Override
  public byte[] getSaltUsedInDigest(String digest) {
    return parse(digest).salt();
  }

  @Override
  public boolean doUnderstandDigest(String digest) {
    return digest != null && digest.startsWith("$argon2id$");
  }

  private static byte[] derive(String password, byte[] salt, int memory, int iterations,
      int parallelism, int length) {
    Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withMemoryAsKB(memory)
        .withIterations(iterations)
        .withParallelism(parallelism)
        .withSalt(salt)
        .build();
    Argon2BytesGenerator generator = new Argon2BytesGenerator();
    generator.init(parameters);
    byte[] result = new byte[length];
    generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), result);
    return result;
  }

  private static ParsedDigest parse(String digest) {
    Matcher matcher = PHC.matcher(digest == null ? "" : digest);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid Argon2id digest format");
    }
    try {
      int memory = Integer.parseInt(matcher.group(1));
      int iterations = Integer.parseInt(matcher.group(2));
      int parallelism = Integer.parseInt(matcher.group(3));
      if (memory < 8 * parallelism || memory > MAX_MEMORY_KIB ||
          iterations < 1 || iterations > MAX_ITERATIONS ||
          parallelism < 1 || parallelism > MAX_PARALLELISM) {
        throw new IllegalArgumentException("Invalid Argon2id parameters");
      }
      Base64.Decoder decoder = Base64.getDecoder();
      byte[] salt = decoder.decode(matcher.group(4));
      byte[] hash = decoder.decode(matcher.group(5));
      if (salt.length < MIN_STORED_SALT_LENGTH || salt.length > MAX_SALT_LENGTH ||
          hash.length < 16 || hash.length > 64) {
        throw new IllegalArgumentException("Invalid Argon2id digest length");
      }
      return new ParsedDigest(memory, iterations, parallelism, salt, hash);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid Argon2id digest", ex);
    }
  }

  private record ParsedDigest(int memory, int iterations, int parallelism, byte[] salt,
                              byte[] hash) {
  }
}
