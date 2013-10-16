package org.silverpeas.util.crypto;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory of the ciphers supported by Silverpeas.
 */
public class CipherFactory {

  private static final CipherFactory instance = new CipherFactory();
  private static final Map<CryptographicAlgorithmName, Cipher> ciphers =
      new EnumMap<CryptographicAlgorithmName, Cipher>(CryptographicAlgorithmName.class);

  // we load all the ciphers supported by the Silverpeas Cryptography API
  static {
    try {
      ciphers.put(CryptographicAlgorithmName.Blowfish, new BlowfishCipher());
    } catch (Exception ex) {
      Logger.getLogger(CipherFactory.class.getSimpleName()).log(Level.SEVERE, ex.getMessage());
    }
    ciphers.put(CryptographicAlgorithmName.CMS, new CMSCipher());
    ciphers.put(CryptographicAlgorithmName.CAST5, new CAST5Cipher());
    ciphers.put(CryptographicAlgorithmName.AES, new AESCipher());
  }

  public static CipherFactory getFactory() {
    return instance;
  }

  /**
   * Gets the cipher identified by the specified cryptographic algorithm name.
   * @param cipherName a name of a cryptographic cipher.
   * @return the cipher that matches the specified name or null if no such cipher exists with the
   * given algorithm name.
   */
  public Cipher getCipher(CryptographicAlgorithmName cipherName) {
    return ciphers.get(cipherName);
  }
}
