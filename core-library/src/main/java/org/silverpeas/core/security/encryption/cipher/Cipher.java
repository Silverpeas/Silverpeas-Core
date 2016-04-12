package org.silverpeas.core.security.encryption.cipher;

/**
 * In cryptography, a cipher (or cypher) is an algorithm for encrypting or decrypting data.
 * The encryption is the operation that converts information from plain text into code or cipher.
 * The decryption is the operation that recovers information in plain text from a code or cipher.
 * All implementation of a given cipher in Silverpeas must implement this interface.
 * <p/>
 * When using a cipher the original information is known as plaintext, and the encrypted form as
 * ciphertext. The ciphertext message contains all the information of the plaintext message, but is
 * not in a format readable by a human or computer without the proper mechanism to decrypt it.
 * The operation of a cipher usually depends on a piece of auxiliary information, called a key.
 * A key must be selected before using a cipher to encrypt a message. Without knowledge of the key,
 * it should be difficult, if not nearly impossible, to decrypt the resulting ciphertext into
 * readable plaintext.
 * <p/>
 * ciphers can be categorized in several ways:
 * <ul>
 * <li>By whether they work on blocks of symbols usually of a fixed size (block ciphers), or on a
 * continuous stream of symbols (stream ciphers).</li>
 * <li>By whether the same key is used for both encryption and decryption (symmetric key
 * algorithms), or if a different key is used for each (asymmetric key algorithms). If the
 * algorithm is symmetric, the key must be known to the recipient and sender and to no one else.
 * If the algorithm is an asymmetric one, the enciphering key is different from, but closely
 * related to, the deciphering key. If one key cannot be deduced from the other, the asymmetric
 * key algorithm has the public/private key property and one of the keys may be made public
 * without loss of confidentiality.</li>
 * </ul>
 */
public interface Cipher {

  /**
   * Gets the name of the algorithm of the cipher.
   * @return the algorithm name.
   */
  CryptographicAlgorithmName getAlgorithmName();

  /**
   * Encrypts the specified data by using the specified cryptographic key.
   *
   * The String objects handled by the encryption is done according the UTF-8 charset.
   * @param data the data to encode.
   * @param keyCode the key to use in the encryption.
   * @return the encrypted data in bytes.
   * @throws CryptoException if an error has occurred in the data encryption.
   */
  byte[] encrypt(String data, CipherKey keyCode) throws CryptoException;

  /**
   * Decrypt the specified code or cipher by using the specified cryptographic key.
   *
   * The String objects handled by the encryption is done according the UTF-8 charset.
   * @param encryptedData the data in bytes encrypted by this cipher.
   * @param keyCode the key to use in the decryption.
   * @return the decrypted data.
   * @throws CryptoException if an error has occurred in the data decryption.
   */
  String decrypt(byte[] encryptedData, CipherKey keyCode) throws CryptoException;

  /**
   * Generates randomly a cipher key that can be used in the encryption and in the decryption of
   * data with this cipher.
   * @return a computed key that can be used with this cipher in the encryption and in the
   * decryption of data.
   * @throws CryptoException if an error has occurred in the key generation.
   */
  CipherKey generateCipherKey() throws CryptoException;
}
