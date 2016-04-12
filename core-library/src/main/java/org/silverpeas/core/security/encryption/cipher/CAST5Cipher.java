package org.silverpeas.core.security.encryption.cipher;

/**
 * CAST-128 (alternatively CAST5) is a block cipher created in 1996 by Carlisle Adams and Stafford
 * Tavares. It is a 12- or 16-round Feistel network with a 64-bit block size and a key size of
 * between 40 to 128 bits (but only in 8-bit increments). The full 16 rounds are used when the key
 * size is longer than 80 bits. All the algorithms herein are from the Internet RFC's RFC2144 -
 * CAST5 (64bit block, 40-128bit key).
 */
public class CAST5Cipher extends BlockCipherWithPadding {

  /**
   * Gets the name of the algorithm of the cipher.
   * @return the algorithm name.
   */
  @Override
  public CryptographicAlgorithmName getAlgorithmName() {
    return CryptographicAlgorithmName.CAST5;
  }
}
