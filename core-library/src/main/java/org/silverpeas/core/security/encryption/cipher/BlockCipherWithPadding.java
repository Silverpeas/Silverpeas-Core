package org.silverpeas.core.security.encryption.cipher;

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Charsets;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * In cryptography, a block cipher is a deterministic algorithm operating on fixed-length groups of
 * bits, called blocks, with an unvarying transformation that is specified by a symmetric key. Block
 * ciphers are important elementary components in the design of many cryptographic protocols, and
 * are widely used to implement encryption of bulk data.
 *
 * A block cipher by itself allows encryption only of a single data block of the cipher's block
 * length. For a variable-length message, the data must first be partitioned into separate cipher
 * blocks. In the simplest case, known as the electronic codebook (ECB) mode, a message is first
 * split into separate blocks of the cipher's block size (possibly extending the last block with
 * padding bits), and then each block is encrypted and decrypted independently. However, such a
 * naive method is generally insecure because equal plaintext blocks will always generate equal
 * ciphertext blocks (for the same key), so patterns in the plaintext message become evident in the
 * ciphertext output. To overcome this limitation, several so-called block cipher modes of operation
 * have been designed and specified in national recommendations such as NIST 800-38A and BSI
 * TR-02102 and international standards such as ISO/IEC 10116. The general concept is to use
 * randomization of the plaintext data based on an additional input value, frequently called an
 * initialization vector (IV), to create what is termed probabilistic encryption. In the popular
 * cipher block chaining (CBC) mode, for encryption to be secure the initialization vector passed
 * along with the plaintext message must be a random or pseudo-random value, which is added in an
 * exclusive-or manner to the first plaintext block before it is being encrypted. The resultant
 * ciphertext block is then used as the new initialization vector for the next plaintext block. In
 * the cipher feedback (CFB) mode, which emulates a self-synchronizing stream cipher, the
 * initialization vector is first encrypted and then added to the plaintext block. The output
 * feedback (OFB) mode repeatedly encrypts the initialization vector to create a key stream for the
 * emulation of a synchronous stream cipher. The newer counter (CTR) mode similarly creates a key
 * stream, but has the advantage of only needing unique and not (pseudo-)random values as
 * initialization vectors; the needed randomness is derived internally by using the initialization
 * vector as a block counter and encrypting this counter for each block.
 *
 * Some modes such as the CBC mode only operate on complete plaintext blocks. Simply extending the
 * last block of a message with zero-bits is insufficient since it does not allow a receiver to
 * easily distinguish messages that differ only in the amount of padding bits. More importantly,
 * such a simple solution gives rise to very efficient padding oracle attacks. A suitable padding
 * scheme is therefore needed to extend the last plaintext block to the cipher's block size. While
 * many popular schemes described in standards and in the literature have been shown to be
 * vulnerable to padding oracle attacks, a solution which adds a one-bit and then extends the last
 * block with zero-bits, standardized as "padding method 2" in ISO/IEC 9797-1, has been proven
 * secure against these attacks.
 *
 * This class is the base one of all block ciphers which use a padding scheme to complete the data
 * to encrypt when it is not divisible into blocks of expected size. All the subclasses will use the
 * CBC operation mode with the PKCS#5 padding scheme.
 *
 * The encrypted data computed by this cipher is a combination of both the ciphertext and the
 * initialization vector (IV) used in the encryption. So this block cipher implementation can
 * retrieve both the ciphertext to decrypt and the IV that was used in the encryption and that is
 * required by the decryption. This characteristic is important because the encrypted data cannot
 * therefore be directly decrypted by another implementation of the same algorithm, even it uses the
 * same operation mode and padding scheme, and this implementation cannot anymore to decrypt a
 * ciphertext coming from another implementation. Nevertheless, to facilitate the
 * encryption/decryption of a ciphertext between two implementation of the same cryptographic
 * algorithm, this class provides two methods to combine or to extract the IV and the ciphertext
 * to/from an encrypted data.
 */
public abstract class BlockCipherWithPadding implements Cipher {

  /**
   * The cryptographic mode of operation to use with the AES algorithm.
   */
  private static final String OPERATION_MODE = "CBC";
  /**
   * The padding scheme to use to pad the message in order to satisfy the length required by the
   * above cryptographic mode of operation.
   */
  private static final String PADDING_SCHEME = "PKCS5Padding";
  /**
   * The JCE provider of the underlying algorithm implementation used by this cipher. Currently, as
   * some of the block ciphers are not all provided by the default JCE provider, we use those
   * provided by the Legion of the Bouncy Castle.
   */
  private static final String SILVERPEAS_JCE_PROVIDER = "BC";
  /**
   * The pattern of the transformation to apply in the encryption and in the decryption. This
   * pattern satisfies the transformation template as defined in the Cryptography Java API.
   */
  private static final String TRANSFORMATION_PATTERN = "{0}/" + OPERATION_MODE + "/"
      + PADDING_SCHEME;

  protected BlockCipherWithPadding() {
  }

  /**
   * An helper method to produce a unique encrypted data by combining the specified ciphertext and
   * the IV (Initialization Vector) used in the ciphertext computation. This method is for using
   * this AES cipher implementation to decrypt ciphertexts that were computed by another AES cipher
   * implementation (only if they use the same mode of cryptographic operation).
   *
   * @param cipherText the ciphertext produced by an AES cipher.
   * @param iv the IV used in the ciphertext computation.
   * @return the resulting encrypted data understandable by this AES cipher implementation.
   */
  public static byte[] combineEncryptionData(byte[] cipherText, byte[] iv) {
    if (iv != null) {
      return ArrayUtil.addAll(iv, cipherText);
    } else {
      return cipherText;
    }
  }

  /**
   * A helper method to retrieve both the ciphertext and the IV (Initialization Vector) from the
   * encrypted data that was produced by the specified block cipher instance. This method consists
   * in extracting the necessary information to other implementations of AES encryption can decrypt
   * the ciphertext (only if they use the same mode of cryptographic operation).
   *
   * @param encryptedData the encrypted data computed by this AES cipher implementation.
   * @return an array with both the ciphertext (at index 0) and the IV that was used in the
   * ciphertext computation (at index 1).
   * @throws CryptoException if the extraction of the ciphertext and of the IV failed.
   */
  public static byte[][] extractEncryptionData(byte[] encryptedData, BlockCipherWithPadding cipher)
      throws CryptoException {
    try {
      javax.crypto.Cipher jceCipher = javax.crypto.Cipher.getInstance(cipher.getTransformation(),
          SILVERPEAS_JCE_PROVIDER);
      int blockSize = jceCipher.getBlockSize();
      byte[][] data = new byte[2][];
      data[1] = ArrayUtil.subarray(encryptedData, 0, blockSize);
      data[0] = ArrayUtil.subarray(encryptedData, blockSize, encryptedData.length);
      return data;
    } catch (Exception ex) {
      throw new CryptoException("The extraction of the ciphertext and of the IV from the "
          + "specified encrypted data failed!", ex);
    }
  }

  /**
   * Gets the name of the algorithm of the cipher.
   *
   * @return the algorithm name.
   */
  @Override
  public abstract CryptographicAlgorithmName getAlgorithmName();

  /**
   * Encrypts the specified data by using the specified cryptographic key.
   * <p/>
   * The String objects handled by the encryption is done according the UTF-8 charset.
   *
   * @param data the data to encode.
   * @param keyCode the key to use in the encryption.
   * @return the encrypted data in bytes.
   */
  @Override
  public byte[] encrypt(final String data, final CipherKey keyCode) throws CryptoException {
    try {
      assertKeyIsBinary(keyCode);
      byte[] keyRaw = keyCode.getRawKey();
      SecretKeySpec keySpec = new SecretKeySpec(keyRaw, getAlgorithmName().name());
      javax.crypto.Cipher cipher = javax.crypto.Cipher.
          getInstance(getTransformation(), SILVERPEAS_JCE_PROVIDER);
      cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec);
      AlgorithmParameters params = cipher.getParameters();
      byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
      byte[] cipherText = cipher.doFinal(data.getBytes(Charsets.UTF_8));
      return combineEncryptionData(cipherText, iv);
    } catch (Exception ex) {
      throw new CryptoException(CryptoException.ENCRYPTION_FAILURE, ex);
    }
  }

  /**
   * Decrypt the specified code or cipher by using the specified cryptographic key.
   * <p/>
   * The String objects handled by the encryption is done according the UTF-8 charset.
   *
   * @param encryptedData the data in bytes encrypted by this cipher.
   * @param keyCode the key to use in the decryption.
   * @return the decrypted data.
   */
  @Override
  public String decrypt(final byte[] encryptedData, final CipherKey keyCode)
      throws CryptoException {
    try {
      assertKeyIsBinary(keyCode);
      byte[] keyRaw = keyCode.getRawKey();
      byte[][] encryptionData = extractEncryptionData(encryptedData, this);
      byte[] cipherText = encryptionData[0];
      byte[] iv = encryptionData[1];
      SecretKeySpec keySpec = new SecretKeySpec(keyRaw, getAlgorithmName().name());
      javax.crypto.Cipher cipher = javax.crypto.Cipher.
          getInstance(getTransformation(), SILVERPEAS_JCE_PROVIDER);
      cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
      byte[] decryptedData = cipher.doFinal(cipherText);
      return new String(decryptedData, Charsets.UTF_8);
    } catch (Exception ex) {
      throw new CryptoException(CryptoException.DECRYPTION_FAILURE, ex);
    }
  }

  @Override
  public CipherKey generateCipherKey() throws CryptoException {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(getAlgorithmName().name(),
          SILVERPEAS_JCE_PROVIDER);
      SecretKey key = keyGenerator.generateKey();
      return CipherKey.aKeyFromBinary(key.getEncoded());
    } catch (Exception ex) {
      throw new CryptoException(CryptoException.KEY_GENERATION_FAILURE, ex);
    }
  }

  private void assertKeyIsBinary(CipherKey key) throws InvalidKeyException {
    if (!key.isRaw()) {
      throw new InvalidKeyException("Invalid key format for this " + getAlgorithmName().name()
          + " cipher!");
    }
  }

  private String getTransformation() {
    return MessageFormat.format(TRANSFORMATION_PATTERN, getAlgorithmName().name());
  }
}
