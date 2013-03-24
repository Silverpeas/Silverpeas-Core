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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
   This class is derived from Sha512Crypt.

   Created: 18 December 2007

   Java Port By: James Ratcliff, falazar@arlut.utexas.edu

   This class implements the new generation, scalable, SHA512-based
   Unix 'crypt' algorithm developed by a group of engineers from Red
   Hat, Sun, IBM, and HP for common use in the Unix and Linux
   /etc/shadow files.

   The Linux glibc library (starting at version 2.7) includes support
   for validating passwords hashed using this algorithm.

   The algorithm itself was released into the Public Domain by Ulrich
   Drepper <drepper@redhat.com>.  A discussion of the rationale and
   development of this algorithm is at

   http://people.redhat.com/drepper/sha-crypt.html

   and the specification and a sample C language implementation is at

   http://people.redhat.com/drepper/SHA-crypt.txt

   This Java Port is

     Copyright (c) 2008-2012 The University of Texas at Austin.

     All rights reserved.

     Redistribution and use in source and binary form are permitted
     provided that distributions retain this entire copyright notice
     and comment. Neither the name of the University nor the names of
     its contributors may be used to endorse or promote products
     derived from this software without specific prior written
     permission. THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY
     EXPRESS OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE
     IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
     PARTICULAR PURPOSE.

*/
package org.silverpeas.authentication.encryption;


import java.security.MessageDigest;
import java.text.MessageFormat;

/**
 * A variation of the SHA-512 algorithm (Secure Hash Algorithm) as used in current Unix systems for
 * hashing the passwords.
 * <p/>
 * This version uses salting and stretching to perturb the algorithm in different ways, and hence
 * to be less vulnerable to attacks. It computes a base64-encoded digest of 123 characters at
 * maximum from a salt and an unencrypted password; the SHA-512 encrypted password in the digest is
 * fixed at 86 characters.
 * <p/>
 * The UnixSHA512Encryption class is derived from the Sha512Crypt one that is a Java port of the
 * the new generation, scalable, SHA-512-based Unix 'crypt' algorithm developed by a group of
 * engineers from Red Hat, Sun, IBM, and HP for common use in Unix and Linux. The port was written
 * by <a href="ftp://ftp.arlut.utexas.edu/java_hashes/">the University of Texas at Austin</a>.
 * <p/>
 * The Linux glibc library (starting at version 2.7) includes support for validating passwords
 * hashed using this algorithm.
 * <p/>
 * The algorithm itself was released into the Public Domain by Ulrich Drepper
 * &lt;drepper@redhat.com&gt;. A discussion of the rationale and development of this algorithm is
 * at
 * <a href="http://people.redhat.com/drepper/sha-crypt.html">
 * http://people.redhat.com/drepper/sha-crypt.html</a>
 * and the specification and a sample C language implementation is at
 * <a href="http://people.redhat.com/drepper/SHA-crypt.txt">
 * http://people.redhat.com/drepper/SHA-crypt.txt</a>
 */
public class UnixSHA512Encryption implements PasswordEncryption {

  /**
   * Encrypts the specified password by using a random salt (or no salt for some weakness
   * algorithms).
   * @param password the password to encrypt.
   * @return a digest of the password.
   */
  @Override
  public String encrypt(final String password) {
    return encrypt(password, null, 0);
  }

  /**
   * Encrypts the specified password by using the specified salt. If the salt is null or empty,
   * then
   * a random salt is computed.
   * @param password the password to encrypt.
   * @param salt the salt to use to generate more entropy in the encryption of the password.
   * @return a digest of the password.
   */
  @Override
  public String encrypt(final String password, final byte[] salt) {
    String saltAsStr = (salt == null || salt.length == 0 ? null: new String(salt));
    return encrypt(password, saltAsStr, 0);
  }

  /**
   * Checks the specified password matches the specified digest.
   * <p/>
   * @param password an unencrypted password.
   * @param digest a digest of a password with which the specified password has to be matched.
   * @throws AssertionError if the digest wasn't computed from the specified password.
   */
  @Override
  public void check(final String password, final String digest) throws AssertionError {
    String encryptedPassword = encrypt(password, getSaltUsedInDigest(digest));
    if (!encryptedPassword.equals(digest)) {
      throw new AssertionError(MessageFormat.format(BAD_PASSWORD_MESSAGE, password, digest));
    }
  }

  /**
   * Gets the salt that was used to compute the specified digest.
   * <p/>
   * According to the cryptographic algorithm that computed the digest, the salt used in the
   * encryption can be retrieved from the digest itself. In the case the salt cannot be determine,
   * an empty one is then returned.
   * <p/>
   * If the digest cannot be analysed by this encryption then an IllegalArgumentException exception
   * is thrown.
   * @param digest the digest from which the salt has to be get.
   * @return the salt or nothing (an empty salt) if it cannot be get from the digest.
   */
  @Override
  public byte[] getSaltUsedInDigest(final String digest) {
    String salt = "";
    if (verifyHashTextFormat(digest)) {
      String[] parts = digest.split("\\$");
      if (parts.length >= 3) {
        salt = parts[2];
        if (salt.startsWith(sha512_rounds_prefix)) {
          salt = parts[3];
        }
      }
    }
    return salt.getBytes();
  }

  /**
   * Does this encryption understand the specified digest?
   * An encryption understands usually the digest it has itself generated. This method is for
   * knowing the encryption that has computed a given digest.
   * @param digest the digest to analyse.
   * @return true if the specified digest was computed by this encryption, false if it doesn't
   *         understand it (either the encryption hasn't generated the digest or it cannot analyse
   *         it).
   */
  @Override
  public boolean doUnderstandDigest(final String digest) {
    return verifyHashTextFormat(digest);
  }

  static private final String sha512_salt_prefix = "$6$";
  static private final String sha512_rounds_prefix = "rounds=";
  static private final int SALT_LEN_MAX = 16;
  static private final int ROUNDS_DEFAULT = 5000;
  static private final int ROUNDS_MIN = 1000;
  static private final int ROUNDS_MAX = 999999999;
  static private final String SALTCHARS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  static private final String itoa64 =
      "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  static private MessageDigest getSHA512() {
    try {
      return MessageDigest.getInstance("SHA-512");
    } catch (java.security.NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Generates a SHA-512 encrypted password hash from a plaintext password and a salt.
   * <p/>
   * <p>The resulting string will be in the form '$6$&lt;rounds=n&gt;$&lt;salt&gt;$&lt;hashed
   * mess&gt;</p>
   * @param keyStr Plaintext password
   * @param saltStr An encoded salt/rounds which will be consulted to determine the salt
   * and round count, if not null. The salt can contains the algorithm identifier (id est $6$) and
   * in that case this part is removed.
   * @param roundsCount If this value is not 0, this many rounds will
   * used to generate the hash text.
   * @return The SHA-512 Unix Crypt hash text for the keyStr
   */

  private static final String encrypt(String keyStr, String saltStr, int roundsCount) {
    MessageDigest ctx = getSHA512();
    MessageDigest alt_ctx = getSHA512();

    byte[] alt_result;
    byte[] temp_result;
    byte[] p_bytes;
    byte[] s_bytes;
    int cnt, cnt2;
    int rounds = ROUNDS_DEFAULT; // Default number of rounds.
    StringBuilder buffer;
    boolean include_round_count = false;

      /* -- */

    if (saltStr != null) {
      if (saltStr.startsWith(sha512_salt_prefix)) {
        saltStr = saltStr.substring(sha512_salt_prefix.length());
      }

      if (saltStr.startsWith(sha512_rounds_prefix)) {
        String num = saltStr.substring(sha512_rounds_prefix.length(), saltStr.indexOf('$'));
        int srounds = Integer.valueOf(num).intValue();
        saltStr = saltStr.substring(saltStr.indexOf('$') + 1);
        rounds = Math.max(ROUNDS_MIN, Math.min(srounds, ROUNDS_MAX));
        include_round_count = true;
      }

      // gnu libc's crypt(3) implementation allows the salt to end
      // in $ which is then ignored.

      if (saltStr.endsWith("$")) {
        saltStr = saltStr.substring(0, saltStr.length() - 1);
      }

      if (saltStr.length() > SALT_LEN_MAX) {
        saltStr = saltStr.substring(0, SALT_LEN_MAX);
      }
    } else {
      java.util.Random randgen = new java.util.Random();
      StringBuilder saltBuf = new StringBuilder();

      while (saltBuf.length() < 16) {
        int index = (int) (randgen.nextFloat() * SALTCHARS.length());
        saltBuf.append(SALTCHARS.substring(index, index + 1));
      }

      saltStr = saltBuf.toString();
    }

    if (roundsCount != 0) {
      rounds = Math.max(ROUNDS_MIN, Math.min(roundsCount, ROUNDS_MAX));
    }

    byte[] key = keyStr.getBytes();
    byte[] salt = saltStr.getBytes();

    ctx.reset();
    ctx.update(key, 0, key.length);
    ctx.update(salt, 0, salt.length);

    alt_ctx.reset();
    alt_ctx.update(key, 0, key.length);
    alt_ctx.update(salt, 0, salt.length);
    alt_ctx.update(key, 0, key.length);

    alt_result = alt_ctx.digest();

    for (cnt = key.length; cnt > 64; cnt -= 64) {
      ctx.update(alt_result, 0, 64);
    }

    ctx.update(alt_result, 0, cnt);

    for (cnt = key.length; cnt > 0; cnt >>= 1) {
      if ((cnt & 1) != 0) {
        ctx.update(alt_result, 0, 64);
      } else {
        ctx.update(key, 0, key.length);
      }
    }

    alt_result = ctx.digest();

    alt_ctx.reset();

    for (cnt = 0; cnt < key.length; ++cnt) {
      alt_ctx.update(key, 0, key.length);
    }

    temp_result = alt_ctx.digest();

    p_bytes = new byte[key.length];

    for (cnt2 = 0, cnt = p_bytes.length; cnt >= 64; cnt -= 64) {
      System.arraycopy(temp_result, 0, p_bytes, cnt2, 64);
      cnt2 += 64;
    }

    System.arraycopy(temp_result, 0, p_bytes, cnt2, cnt);

    alt_ctx.reset();

    for (cnt = 0; cnt < 16 + (alt_result[0] & 0xFF); ++cnt) {
      alt_ctx.update(salt, 0, salt.length);
    }

    temp_result = alt_ctx.digest();

    s_bytes = new byte[salt.length];

    for (cnt2 = 0, cnt = s_bytes.length; cnt >= 64; cnt -= 64) {
      System.arraycopy(temp_result, 0, s_bytes, cnt2, 64);
      cnt2 += 64;
    }

    System.arraycopy(temp_result, 0, s_bytes, cnt2, cnt);

      /* Repeatedly run the collected hash value through SHA512 to burn
         CPU cycles.  */

    for (cnt = 0; cnt < rounds; ++cnt) {
      ctx.reset();

      if ((cnt & 1) != 0) {
        ctx.update(p_bytes, 0, key.length);
      } else {
        ctx.update(alt_result, 0, 64);
      }

      if (cnt % 3 != 0) {
        ctx.update(s_bytes, 0, salt.length);
      }

      if (cnt % 7 != 0) {
        ctx.update(p_bytes, 0, key.length);
      }

      if ((cnt & 1) != 0) {
        ctx.update(alt_result, 0, 64);
      } else {
        ctx.update(p_bytes, 0, key.length);
      }

      alt_result = ctx.digest();
    }

    buffer = new StringBuilder(sha512_salt_prefix);

    if (include_round_count || rounds != ROUNDS_DEFAULT) {
      buffer.append(sha512_rounds_prefix);
      buffer.append(rounds);
      buffer.append("$");
    }

    buffer.append(saltStr);
    buffer.append("$");

    buffer.append(b64_from_24bit(alt_result[0], alt_result[21], alt_result[42], 4));
    buffer.append(b64_from_24bit(alt_result[22], alt_result[43], alt_result[1], 4));
    buffer.append(b64_from_24bit(alt_result[44], alt_result[2], alt_result[23], 4));
    buffer.append(b64_from_24bit(alt_result[3], alt_result[24], alt_result[45], 4));
    buffer.append(b64_from_24bit(alt_result[25], alt_result[46], alt_result[4], 4));
    buffer.append(b64_from_24bit(alt_result[47], alt_result[5], alt_result[26], 4));
    buffer.append(b64_from_24bit(alt_result[6], alt_result[27], alt_result[48], 4));
    buffer.append(b64_from_24bit(alt_result[28], alt_result[49], alt_result[7], 4));
    buffer.append(b64_from_24bit(alt_result[50], alt_result[8], alt_result[29], 4));
    buffer.append(b64_from_24bit(alt_result[9], alt_result[30], alt_result[51], 4));
    buffer.append(b64_from_24bit(alt_result[31], alt_result[52], alt_result[10], 4));
    buffer.append(b64_from_24bit(alt_result[53], alt_result[11], alt_result[32], 4));
    buffer.append(b64_from_24bit(alt_result[12], alt_result[33], alt_result[54], 4));
    buffer.append(b64_from_24bit(alt_result[34], alt_result[55], alt_result[13], 4));
    buffer.append(b64_from_24bit(alt_result[56], alt_result[14], alt_result[35], 4));
    buffer.append(b64_from_24bit(alt_result[15], alt_result[36], alt_result[57], 4));
    buffer.append(b64_from_24bit(alt_result[37], alt_result[58], alt_result[16], 4));
    buffer.append(b64_from_24bit(alt_result[59], alt_result[17], alt_result[38], 4));
    buffer.append(b64_from_24bit(alt_result[18], alt_result[39], alt_result[60], 4));
    buffer.append(b64_from_24bit(alt_result[40], alt_result[61], alt_result[19], 4));
    buffer.append(b64_from_24bit(alt_result[62], alt_result[20], alt_result[41], 4));
    buffer.append(b64_from_24bit((byte) 0x00, (byte) 0x00, alt_result[63], 2));

      /* Clear the buffer for the intermediate result so that people
         attaching to processes or reading core dumps cannot get any
         information. */

    ctx.reset();

    return buffer.toString();
  }

  private static final String b64_from_24bit(byte B2, byte B1, byte B0, int size) {
    int v = ((((int) B2) & 0xFF) << 16) | ((((int) B1) & 0xFF) << 8) | ((int) B0 & 0xff);

    StringBuilder result = new StringBuilder();

    while (--size >= 0) {
      result.append(itoa64.charAt((v & 0x3f)));
      v >>>= 6;
    }

    return result.toString();
  }

  /**
   * <p>Returns true if sha512CryptText is a valid Sha512Crypt hashtext,
   * false if not.</p>
   */
  private static final boolean verifyHashTextFormat(String sha512CryptText) {
    if (!sha512CryptText.startsWith(sha512_salt_prefix)) {
      return false;
    }

    sha512CryptText = sha512CryptText.substring(sha512_salt_prefix.length());

    if (sha512CryptText.startsWith(sha512_rounds_prefix)) {
      String num =
          sha512CryptText.substring(sha512_rounds_prefix.length(), sha512CryptText.indexOf('$'));

      try {
        Integer.valueOf(num).intValue();
      } catch (NumberFormatException ex) {
        return false;
      }

      sha512CryptText = sha512CryptText.substring(sha512CryptText.indexOf('$') + 1);
    }

    if (sha512CryptText.indexOf('$') > (SALT_LEN_MAX + 1)) {
      return false;
    }

    sha512CryptText = sha512CryptText.substring(sha512CryptText.indexOf('$') + 1);

    for (int i = 0; i < sha512CryptText.length(); i++) {
      if (itoa64.indexOf(sha512CryptText.charAt(i)) == -1) {
        return false;
      }
    }

    return true;
  }
}
