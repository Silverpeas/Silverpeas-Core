package org.silverpeas.core.security.encryption.cipher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wallet of PKS#12 key files. It is an in-memory storage of PKS12KeyStore instances for users
 * in order to be taken into account by some cryptographic processes to exchange between them
 * digital data.
 * <p/>
 * It is a singleton that manages all the PKS#12 key stores for Silverpeas.
 */
public class PKS12KeyStoreWallet {

  private static final PKS12KeyStoreWallet instance = new PKS12KeyStoreWallet();
  private static final Map<String, PKS12KeyStore> keyMap =
      new ConcurrentHashMap<String, PKS12KeyStore>();

  /**
   * Gets the single wallet instance.
   * @return the single instance of the key store wallet.
   */
  public static final PKS12KeyStoreWallet getInstance() {
    return instance;
  }

  private PKS12KeyStoreWallet() {
  }

  /**
   * Adds the PKS#12 keys contained in the specified file that is protected with the specified
   * password. The keys are added in the form of a PKS#12 key store.
   * @param keyFilePath the path of the key store file.
   * @param password the password that protects the key store file.
   * @throws CryptoException if an error occurs while adding the keys into this wallet.
   */
  public void addKeyStore(String keyFilePath, String password) throws CryptoException {
    try {
      PKS12KeyStore silverkeys = new PKS12KeyStore(keyFilePath, password);
      keyMap.put(keyFilePath, silverkeys);

    } catch (Exception e) {
      throw new CryptoException("Cannot create a PKS#12 key store from the file '" + keyFilePath +
          "'", e);
    }
  }

  /**
   * Gets the PKS#12 key store identified by the specified key store file and loaded in this wallet.
   * @param keyFilePath the path of the file from which the PKS#12 key store was loaded into this
   * wallet.
   * @return the PKS#12 key store from this wallet.
   * @throws CryptoException if no such PKS#12 key store exists in this wallet.
   */
  public PKS12KeyStore getKeyStore(String keyFilePath) throws CryptoException {
    PKS12KeyStore keyStore = keyMap.get(keyFilePath);
    if (keyStore != null) {
      return keyStore;
    } else {
      throw new CryptoException("PKS#12 key store not found");
    }
  }
}
