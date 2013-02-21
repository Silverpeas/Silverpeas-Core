package com.silverpeas.util.security;

import javax.crypto.Cipher;

/**
 * It is a security service for protecting content from an access in plain data.
 * The service provides the encryption and the decryption of content by using a
 * key-symmetric cryptographic algorithm for which it manages the key.
 */
public class ContentEncryptionService {

  private static final String KEY_FILE_PATH = "";

  /**
   * Updates the key to use to encrypt and to decrypt the enciphered content.
   * This key will be stored into a file and will be also encrypted by another cryptographic
   * algorithm.
   * @param key the new symmetric key in hexadecimal.
   */
  public void updateKey(String key) throws Exception {

  }

}
