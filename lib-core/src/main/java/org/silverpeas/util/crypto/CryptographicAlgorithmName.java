package org.silverpeas.util.crypto;

/**
 * A name of a a cryptographic algorithm (or cipher) supported in Silverpeas.
 */
public enum CryptographicAlgorithmName {

  /**
   * The Blowfish cipher.
   */
  Blowfish,
  /**
   * The Cryptographic Message Syntax (CMS) based on the syntax of PKCS#7. More than just a cipher,
   * it is an asymmetric-keys and certificate based enciphering process to encrypt and to decrypt
   * messages exchanged between two interlocutors.
   */
  CMS;
}
