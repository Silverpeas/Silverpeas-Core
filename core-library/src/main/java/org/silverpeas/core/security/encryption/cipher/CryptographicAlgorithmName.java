/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

/**
 * A name of a a cryptographic algorithm (or cipher) supported in Silverpeas.
 */
public enum CryptographicAlgorithmName {

  /**
   * The AES-256 cipher.
   */
  AES("AES"),
  /**
   * The Blowfish cipher.
   */
  BLOWFISH("Blowfish"),
  /**
   * The CAST5 (CAST-128) cipher.
   */
  CAST5("CAST5"),
  /**
   * The Cryptographic Message Syntax (CMS) based on the syntax of PKCS#7. More than just a cipher,
   * it is an asymmetric-keys and certificate based enciphering process to encrypt and to decrypt
   * digital messages exchanged between two interlocutors.
   */
  CMS("CMS");

  private final String id;

  /**
   * The identifier of the cryptographic algorithm as expected by the {@link javax.crypto.Cipher}
   * standard Java object.
   * @param algoID the identifier of the algorithm in the Java Crypto API.
   */
  CryptographicAlgorithmName(final String algoID) {
    this.id = algoID;
  }

  /**
   * Gets the unique identifier of this algorithm in the Java Crypto API.
   * @return the identifier of this algorithm.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the unique identifier of this algorithm in the Java Crypto API.
   * @return the identifier of this algorithm.
   */
  @Override
  public String toString() {
    return getId();
  }
}
