package org.silverpeas.core.security.encryption.cipher;

import org.silverpeas.core.util.EncodingUtil;
import org.silverpeas.core.util.StringUtil;

import java.io.File;
import java.text.ParseException;

/**
 * A key used in a cryptographic algorithm to encrypt a plain text or to decrypt a cipher text. This
 * key can be either symmetric or asymmetric, it can be provided as such or in a file. This class is
 * a wrapper of the actual representation of the cipher key so that it can be passed to the ciphers
 * in the Silverpeas Cryptography API in different forms, according to the ciphers expectation.
 */
public class CipherKey {

  private byte[] key;
  private String keyFilePath;

  private CipherKey(byte[] aKey) {
    this.key = aKey.clone();
  }

  private CipherKey(String akeyFilePath) {
    this.keyFilePath = akeyFilePath;
  }

  /**
   * Constructs a new cipher key from the specified hexadecimal representation of the key.
   *
   * @param hexKey the text with hexadecimal-based characters.
   * @return the cipher key.
   */
  public static CipherKey aKeyFromHexText(String hexKey) throws ParseException {
    return new CipherKey(EncodingUtil.fromHex(hexKey));
  }

  /**
   * Constructs a new cipher key from the specified base64 representation of the key.
   *
   * @param base64Key the text of the key in Base64.
   * @return the cipher key.
   */
  public static CipherKey aKeyFromBase64Text(String base64Key) {
    return new CipherKey(StringUtil.fromBase64(base64Key));
  }

  /**
   * Constructs a new cipher key from the specified binary representation of the key.
   *
   * @param binaryKey the key in binary.
   * @return the cipher key.
   */
  public static CipherKey aKeyFromBinary(byte[] binaryKey) {
    return new CipherKey(binaryKey);
  }

  /**
   * Constructs a new cipher key from the path of the file in which is stored the key.
   *
   * @param path the path of the key file.
   * @return a cipher key.
   */
  public static CipherKey aKeyFromFilePath(String path) {
    return new CipherKey(path);
  }

  /**
   * Is the key in a file?
   *
   * @return true if the key is in a file and the file exists, false otherwise.
   */
  public boolean isInFile() {
    if (StringUtil.isDefined(keyFilePath)) {
      File keyFile = new File(keyFilePath);
      return keyFile.exists() && keyFile.isFile();
    }
    return false;

  }

  /**
   * Is this cipher key represents a key in itself and not the storage in which the key is?
   *
   * @return true if this cipher key represents a raw key.
   */
  public boolean isRaw() {
    return key != null;
  }

  /**
   * Gets the raw representation of this cipher key.
   *
   * @return the key in binaries.
   */
  public byte[] getRawKey() {
    return key;
  }

  /**
   * Gets the path of the file that stores the key.
   *
   * @return the path of the key file or null if this key isn't stored in a file.
   */
  public String getKeyFilePath() {
    return keyFilePath;
  }
}
