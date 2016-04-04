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
package org.silverpeas.core.security.authentication.password;

/**
 * Encryption of a user password or passphrase by using a cryptographic one-way hash algorithm.
 * <p/>
 * While the message authentication and the integrity checking requires a hash function that matters
 * in speed and in efficiency (a different digest for inputs altered even a little), the hashing of
 * the passwords or passphrases require robustness and future-proof against strong attacks that take
 * advantage of a hardware more and more powerful and of the password's lifetime.
 * <p/>
 * Usually, the hash functions used in the encryption of a password is based on a standard and well
 * known cryptographic algorithm: MD5, SHA-1, SHA-256, etc. Unfortunately, these functions don't
 * suit well for encrypting password for the reasons explained above; they suffer of the
 * recognizability and of the speed problems. This is why techniques like the salting (random
 * sequence of bytes which is added to the hash function) and the stretching (iteration of the hash
 * function many times) should be used to address the weakness of these one-way hash functions.
 * (These techniques are usually used by the Unix system in their variations of the above algorithms
 * to encrypt the user passwords.)
 * Another and better solution is to use an adaptive key derivations functions to encrypt the
 * passwords or the passphrases as they generate more entropy in the digest computation.
 *
 * @author mmoquillon
 */
public interface PasswordEncryption {

  /**
   * A format message for the {@link PasswordEncryption#check(String, String)} method when the
   * password doesn't match the digest. It serves as a template for the error message to be carried
   * by the AssertionError error.
   */
  String BAD_PASSWORD_MESSAGE = "The password \"{0}\" doesn''t match the digest \"{1}\"";

  /**
   * Encrypts the specified password by using a random salt (or no salt for some weakness
   * algorithms).
   *
   * @param password the password to encrypt.
   * @return a digest of the password.
   */
  String encrypt(String password);

  /**
   * Encrypts the specified password by using the specified salt. If the salt is null or empty, then
   * a random salt is computed.
   *
   * @param password the password to encrypt.
   * @param salt the salt to use to generate more entropy in the encryption of the password.
   * @return a digest of the password.
   */
  String encrypt(String password, byte[] salt);

  /**
   * Checks the specified password matches the specified digest.
   * <p/>
   *
   * @param password an unencrypted password.
   * @param digest a digest of a password with which the specified password has to be matched.
   * @throws AssertionError if the digest wasn't computed from the specified password.
   */
  void check(String password, String digest) throws AssertionError;

  /**
   * Gets the salt that was used to compute the specified digest.
   * <p/>
   * According to the cryptographic algorithm that computed the digest, the salt used in the
   * encryption can be retrieved from the digest itself. In the case the salt cannot be determine,
   * an empty one is then returned.
   * <p/>
   * If the digest cannot be analysed by this encryption then an IllegalArgumentException exception
   * is thrown.
   *
   * @param digest the digest from which the salt has to be get.
   * @return the salt or nothing (an empty salt) if it cannot be get from the digest.
   */
  byte[] getSaltUsedInDigest(String digest);

  /**
   * Does this encryption understand the specified digest?
   * An encryption understands usually the digest it has itself generated. This method is for
   * knowing the encryption that has computed a given digest.
   * @param digest the digest to analyse.
   * @return true if the specified digest was computed by this encryption, false if it doesn't
   * understand it (either the encryption hasn't generated the digest or it cannot analyse it).
   */
  boolean doUnderstandDigest(String digest);
}
