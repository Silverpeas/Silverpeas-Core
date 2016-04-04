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

import org.silverpeas.core.security.authentication.password.encryption.UnixSHA512Encryption;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Set;

/**
 * Factory of password encryption objects implementing a given algorithm. It wraps the concrete
 * implementation of the <code>PasswordEncryption</code> interface used for encrypting a password
 * according to a chosen algorithm.
 * <p/>
 * This factory provides all of the available password encryption supported by Silverpeas,
 * nevertheless it returns only the main encryption used by default in Silverpeas (the one that is
 * considered as the more robust and secure) with the <code>getDefaultPasswordEncryption()</code>
 * method. Getting others encryption can be done in order to work with passwords encrypted with
 * old (and then deprecated) algorithms with the <code>getPasswordEncryption(digest)</code>
 * method.
 * @author mmoquillon
 */
public class PasswordEncryptionProvider {

  /**
   * Gets the password encryption that is used by default in Silverpeas to encrypt the user
   * passwords and to check them.
   * @return the current default password encryption.
   */
  public static PasswordEncryption getDefaultPasswordEncryption() {
    return ServiceProvider.getService(UnixSHA512Encryption.class);
  }

  /**
   * Gets the encryption that has computed the specified digest.
   * <p/>
   * As digests in password encryption are usually made up of an encryption algorithm identifier,
   * the factory can then find the algorithm that matches the specified digest. If the digest
   * doesn't contain any algorithm identifier, then the UnixDES is returned (yet it is the only one
   * supported by Silverpeas that doesn't generate an algorithm identifier in the digest). In the
   * case the identifier in the digest isn't known, then a exception is thrown.
   * @param digest the digest from which the password encryption has be found.
   * @return the password encryption that has computed the specified digest.
   * @throws IllegalArgumentException if the digest was not computed by any of the password
   * encryption supported in Silverpeas.
   */
  public static PasswordEncryption getPasswordEncryption(String digest)
      throws IllegalArgumentException {
    Set<PasswordEncryption> availableEncrypts =
        ServiceProvider.getAllServices(PasswordEncryption.class);
    PasswordEncryption encryption = availableEncrypts.stream()
        .filter(encrypt -> encrypt.doUnderstandDigest(digest))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Digest '" + digest +
            "' not understand by any of the available encryption in Silverpeas"));
    return encryption;
  }
}
