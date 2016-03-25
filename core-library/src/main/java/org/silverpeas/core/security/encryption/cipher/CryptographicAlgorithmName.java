package org.silverpeas.core.security.encryption.cipher;

/**
 * A name of a a cryptographic algorithm (or cipher) supported in Silverpeas.
 */
public enum CryptographicAlgorithmName {

  /**
   * The AES-256 cipher.
   */
  AES,
  /**
   * The Blowfish cipher.
   */
  Blowfish,
  /**
   * The CAST5 (CAST-128) cipher.
   */
  CAST5,
  /**
   * The Cryptographic Message Syntax (CMS) based on the syntax of PKCS#7. More than just a cipher,
   * it is an asymmetric-keys and certificate based enciphering process to encrypt and to decrypt
   * digital messages exchanged between two interlocutors.
   */
  CMS;
}
