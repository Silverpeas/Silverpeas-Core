/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.security.authentication.password.encryption;


import org.apache.commons.codec.digest.Crypt;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Charsets;

import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A variation of the SHA-512 algorithm (Secure Hash Algorithm) as used in current Unix systems for
 * hashing the passwords.
 * <p/>
 * This version uses salting and stretching to perturb the algorithm in different ways, and hence
 * to be less vulnerable to attacks. It computes a base64-encoded digest of 123 characters at
 * maximum from a salt and an unencrypted password; the SHA-512 encrypted password in the digest is
 * fixed at 86 characters.
 * <p/>
 * The UnixSHA512Encryption class is based upon the the new generation, scalable, SHA-512-based
 * Unix 'crypt' algorithm developed by a group of engineers from Red Hat, Sun, IBM, and HP for
 * common use in Unix and Linux.
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
@Singleton
public class UnixSHA512Encryption implements PasswordEncryption {

  static private final String SALTCHARS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890/.";
  static private final String ENCRYPTION_METHOD_ID = "$6$";

  /**
   * Encrypts the specified password by using a random salt (or no salt for some weakness
   * algorithms).
   * @param password the password to encrypt.
   * @return a digest of the password.
   */
  @Override
  public String encrypt(final String password) {
    String salt = computeRandomSalt();
    return Crypt.crypt(password, salt);
  }

  /**
   * Encrypts the specified password by using the specified salt. If the salt is null or empty,
   * then a random salt is computed.
   * @param password the password to encrypt.
   * @param salt the salt to use to generate more entropy in the encryption of the password.
   * @return a digest of the password.
   */
  @Override
  public String encrypt(final String password, final byte[] salt) {
    String enrichedSalt = (ArrayUtil.isEmpty(salt)? computeRandomSalt():setUpSalt(salt));
    return Crypt.crypt(password, enrichedSalt);
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
    if (doUnderstandDigest(digest)) {
      Pattern pattern = Pattern.compile("\\$[a-zA-Z0-9/.]{0,16}\\$");
      Matcher matcher = pattern.matcher(digest);
      matcher.find(1); // the first matching is the crypto id ($6$) so pass to the next
      salt = matcher.group();
      salt = salt.substring(1, salt.length() - 1);
    }
    return salt.getBytes(Charsets.UTF_8);
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
    return digest.matches("\\$6\\$(rounds=[0-9]+\\$)?[a-zA-Z0-9/.]{0,16}\\$[a-zA-Z0-9/.]{86}");
  }

  private static final String computeRandomSalt() {
    java.util.Random random = new java.util.Random();
    StringBuilder saltBuf = new StringBuilder(ENCRYPTION_METHOD_ID);

    while (saltBuf.length() < 16) {
      int index = (int) (random.nextFloat() * SALTCHARS.length());
      saltBuf.append(SALTCHARS.substring(index, index + 1));
    }

    return saltBuf.toString();
  }

  private static final String setUpSalt(byte[] salt) {
    return ENCRYPTION_METHOD_ID + new String(salt, Charsets.UTF_8);
  }
}
