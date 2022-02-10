/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption.cipher;

/**
 * The Advanced Encryption Standard (AES) is a specification for the encryption of electronic data
 * established by the U.S. National Institute of Standards and Technology (NIST) in 2001. It has
 * been adopted by the U.S. government and is now used worldwide. It supersedes the Data Encryption
 * Standard (DES), which was published in 1977.
 * <p>
 * AES is based on a design principle known as a substitution-permutation network, and is fast in
 * both software and hardware. Unlike its predecessor DES, AES does not use a Feistel network. AES
 * is a variant of Rijndael which has a fixed block size of 128 bits, and a key size of 128, 192,
 * or 256 bits. By contrast, the Rijndael specification per se is specified with block and key
 * sizes
 * that may be any multiple of 32 bits, both with a minimum of 128 and a maximum of 256 bits.
 * <p>
 * This implementation provides the encryption and the decryption of text by using the AES
 * algorithm with which the CBC (Cipher Block Chaining) operation mode is applied. With CBC, a
 * random Initialization Vector (IV) is used in order to produce the ciphertext unique; for a same
 * plain text, a different ciphertext can be computed. The resulting encrypted data is then made up
 * of the ciphertext and of the IV used in the encryption. By this way, a different AES cipher
 * implementation cannot decrypt the encrypted data that was computed by this implementation and it
 * cannot decrypt the ciphertext coming from another AES cipher implementation without having the
 * IV combined with the ciphertext.
 * <p>
 * The cryptographic tasks in this implementation are based upon the AES cipher provided in the
 * Java Cryptography API. It supports both AES-128, AES-192, and AES-256, depending on the the size
 * of the key passed as argument in the encryption and in the decryption functions. In order to use
 * keys of size above of 128 bits, the Java Cryptography Extension (JCE) Unlimited Strength
 * Jurisdiction Policy Files are required. For each of these size of the symmetric key, a given
 * number of repetitions of transformation rounds that convert the plaintext into the ciphertext is
 * performed:
 * <ul>
 * <li>10 cycles of repetition for 128-bit keys (AES-128)</li>
 * <li>12 cycles of repetition for 192-bit keys (AES-192)</li>
 * <li>14 cycles of repetition for 256-bit keys (AES-256)</li>
 * </ul>
 * Any other sizes will raise a CryptoException exception.
 */
public class AESCipher extends BlockCipherWithPadding {


  protected AESCipher() {
  }

  /**
   * Gets the name of the algorithm of the cipher.
   * @return the algorithm name.
   */
  @Override
  public CryptographicAlgorithmName getAlgorithmName() {
    return CryptographicAlgorithmName.AES;
  }
}
