package org.silverpeas.core.security.encryption;

import org.silverpeas.core.util.EncodingUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Named;
import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherFactory;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.security.encryption.cipher.CryptographicAlgorithmName;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * It is the default implementation of the {@link ContentEncryptionService} interface in Silverpeas.
 * </p>
 * This implementation manages the encryption of the content with the AES-256 cipher and it stores
 * the cipher key into a file after encrypting it with another cryptographic algorithm, CAST-128 (a
 * CAST5 cipher), in order to protect it. The two keys are set together in the key file which is
 * located in an hidden directory. The key file is hidden and readonly.
 * </p>
 * It manages the cipher key by maintaining both the actual cipher key used to encrypt and decrypt
 * the content and the previous one so that the cipher of some old contents can be renewed with the
 * new key after decrypting them with the old key.
 * </p>
 * This implementation used two additional classes to perform its task: the
 * {@link ConcurrentEncryptionTaskExecutor} class to ensure the execution of the different methods
 * are done by following the concurrency policy expected by the {@link ContentEncryptionService}
 * interface, and the {@link CryptographicTask} class to represent a encryption or a decryption of
 * contents provided by some content iterators (they implement the {@link EncryptionContentIterator}
 * interface).
 */
@Named("contentEncryptionService")
public class DefaultContentEncryptionService implements ContentEncryptionService {

  private static final String ACTUAL_KEY_FILE_PATH =
      FileRepositoryManager.getSecurityDirPath() + ".aid_key";
  private static final String DEPRECATED_KEY_FILE_PATH =
      FileRepositoryManager.getSecurityDirPath() + ".did_key";
  private static final String KEY_SEP = " ";
  private static List<EncryptionContentIterator> contentIterators =
      new CopyOnWriteArrayList<EncryptionContentIterator>();

  protected DefaultContentEncryptionService() {
  }

  /**
   * Registers the specified iterator on some encrypted contents for which the cipher has to be
   * renewed when the encryption key is updated.
   * </p>
   * This method is dedicated to the content management service for providing to the content
   * encryption services a way to access the encrypted contents they manage in order to renew their
   * cipher when the encryption key is updated.
   *
   * @param iterator a provider of encrypted content in the form of a
   * {@link EncryptionContentIterator} iterator.
   */
  @Override
  public void registerForRenewingContentCipher(final EncryptionContentIterator iterator) {
    contentIterators.add(new EncryptionContentIteratorWrapper(iterator));
  }

  /**
   * Updates the key to use to encrypt and to decrypt the enciphered content. The key must be in
   * hexadecimal and sized in 256 bits otherwise an AssertionError will be thrown. If no previous
   * key existed, then the cipher key will be created with the specified one and it will be used to
   * encrypt and to decrypt at the demand the content in Silverpeas.
   * </p>
   * The update of the key triggers automatically the renew of the cipher of the encrypted contents
   * in Silverpeas with the new cipher key. If one of the cipher renew of one of the encrypted
   * content failed, the key update is rolled-back (the key isn't updated).
   * </p>
   * The execution of this method will block any other call of the DefaultContentEncryptionService
   * methods for all of its instances in order to prevent incoherent state of encrypted contents.
   * Any attempts to execute one of the DefaultContentEncryptionService method, whereas this method
   * is running, will raise an IllegalStateException exception.
   *
   * @param key the new symmetric key in hexadecimal.
   * @throws CipherKeyUpdateException if the replace of the cipher key has failed.
   * @throws CryptoException if an error while renewing the cipher of the encrypted contents with
   * the new cipher key.
   */
  @Override
  public void updateCipherKey(final String key) throws CipherKeyUpdateException, CryptoException {
    assertKeyIsInHexadecimal(key);
    assertKeyIsIn256Bits(key);
    ConcurrentEncryptionTaskExecutor
        .execute(new ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask() {
      @Override
      public boolean isPrivileged() {
        return true;
      }

      @Override
      public Void execute() throws CryptoException {
        File backupedKeyFile = null;
        File backupedDeprecatedKeyFile = null;
        boolean restore = false;
        try {
          boolean renewContentCiphers = false;
          File keyFile = new File(ACTUAL_KEY_FILE_PATH);
          if (keyFile.exists()) {
            renewContentCiphers = true;
            backupedKeyFile = new File(ACTUAL_KEY_FILE_PATH + ".backup");
            FileUtil.copyFile(keyFile, backupedKeyFile);
            File deprecatedKeyFile = new File(DEPRECATED_KEY_FILE_PATH);
            if (deprecatedKeyFile.exists()) {
              backupedDeprecatedKeyFile = new File(DEPRECATED_KEY_FILE_PATH + ".backup");
              FileUtil.moveFile(deprecatedKeyFile, backupedDeprecatedKeyFile);
            }
            FileUtil.moveFile(keyFile, deprecatedKeyFile);
            setHidden(DEPRECATED_KEY_FILE_PATH);
          }

          Cipher cipher = getCipherForKeyEncryption();
          CipherKey encryptionKey = cipher.generateCipherKey();
          byte[] encryptedKey = cipher.encrypt(key, encryptionKey);
          String encryptedContent = StringUtil.asBase64(encryptionKey.getRawKey()) + KEY_SEP
              + StringUtil.asBase64(encryptedKey);
          FileUtil.writeFile(keyFile, new StringReader(encryptedContent));
          keyFile.setReadOnly();
          setHidden(ACTUAL_KEY_FILE_PATH);

          if (renewContentCiphers) {
            EncryptionContentIterator[] iterators =
                contentIterators.toArray(new EncryptionContentIterator[contentIterators.size()]);
            CryptographicTask.renewEncryptionOf(iterators).execute();
          }
          return null;
        } catch (IOException ex) {
          restore = true;
          throw new CipherKeyUpdateException("Cannot update the encryption key", ex);
        } catch (CipherRenewingException ex) {
          restore = true;
          throw new CryptoException(ex.getMessage(), ex);
        } finally {
          try {
            if (backupedKeyFile != null) {
              if (restore) {
                File keyFile = new File(ACTUAL_KEY_FILE_PATH);
                FileUtil.copyFile(backupedKeyFile, keyFile);
                keyFile.setReadOnly();
                setHidden(ACTUAL_KEY_FILE_PATH);
              }
              FileUtil.forceDeletion(backupedKeyFile);
            }
            if (backupedDeprecatedKeyFile != null) {
              if (restore) {
                File keyFile = new File(DEPRECATED_KEY_FILE_PATH);
                keyFile.delete();
                FileUtil.copyFile(backupedDeprecatedKeyFile, keyFile);
                keyFile.setReadOnly();
                setHidden(DEPRECATED_KEY_FILE_PATH);
              }
              FileUtil.forceDeletion(backupedDeprecatedKeyFile);
            }
          } catch (IOException ex) {
            SilverLogger.getLogger(this).error(ex.getMessage(), ex);
          }
        }
      }
    });
  }

  /**
   * Encrypts the specified content by using the encryption key that was set with the
   * {@link #updateCipherKey(String)} method.
   *
   * @param contentParts either the different part of a content to encrypt or several single textual
   * contents to encrypt.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   * @return an array with the different parts of the content, encrypted and in base64, in the same
   * order they were passed as argument of this method.
   * @throws CryptoException the encryption of one of the content (or content part) failed.
   */
  @Override
  public String[] encryptContent(final String... contentParts) throws CryptoException {
    return ConcurrentEncryptionTaskExecutor
        .execute(new ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask() {
      @Override
      public boolean isPrivileged() {
        return false;
      }

      @Override
      public String[] execute() throws CryptoException {
        CipherKey key = getActualCipherKey();
        Cipher cipher = getCipherForContentEncryption();
        String[] encryptedContents = new String[contentParts.length];
        for (int i = 0; i < contentParts.length; i++) {
          if (contentParts[i] != null) {
            byte[] theEncryptedContent = cipher.encrypt(contentParts[i], key);
            encryptedContents[i] = StringUtil.asBase64(theEncryptedContent);
          }
        }
        return encryptedContents;
      }
    });
  }

  /**
   * Encrypts the specified content by using the encryption key that was set with the
   * {@link #updateCipherKey(String)} method.
   * </p>
   * The content is here in the form of a Map instance in which each entry represents a field or a
   * property of the content. The method returns also a Map with, for each entry, the field or the
   * property encrypted and in base64.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   *
   * @param content the content to encrypt in the form of a Map instance. Each entry in the Map
   * represents a field/property of the content to encrypt.
   * @return a Map with the different field/property of the content encrypted.
   * @throws CryptoException the encryption of the content failed.
   */
  @Override
  public Map<String, String> encryptContent(final Map<String, String> content)
      throws CryptoException {
    return ConcurrentEncryptionTaskExecutor
        .execute(new ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask() {
      @Override
      public boolean isPrivileged() {
        return false;
      }

      @Override
      public Map<String, String> execute() throws CryptoException {
        CipherKey key = getActualCipherKey();
        Cipher cipher = getCipherForContentEncryption();
        return encryptContent(content, cipher, key);
      }
    });
  }

  /**
   * Encrypts the contents provided by the specified iterators.
   * </p>
   * This method is for encrypting in batch several and possibly different contents. If there is
   * more than one iterator on contents, each of them will be taken in charge concurrently by a pool
   * of several threads.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   *
   * @param iterators the iterators on the contents to encrypt.
   */
  @Override
  public void encryptContents(final EncryptionContentIterator... iterators) throws CryptoException {
    ConcurrentEncryptionTaskExecutor.execute(CryptographicTask.encryptionOf(iterators));
  }

  /**
   * Decrypts the specified encrypted content by using the encryption key that was set with the
   * {@link #updateCipherKey(String)} method.
   *
   * @param encryptedContentParts either the different part of an encrypted content to decrypt or
   * several single encrypted textual contents to decrypt.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   * @return an array with the different parts of the decrypted content in the same order they were
   * passed as argument of this method.
   * @throws CryptoException the decryption of one of the encrypted content (or content part)
   * failed.
   */
  @Override
  public String[] decryptContent(final String... encryptedContentParts) throws CryptoException {
    return ConcurrentEncryptionTaskExecutor
        .execute(new ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask() {
      @Override
      public boolean isPrivileged() {
        return false;
      }

      @Override
      public String[] execute() throws CryptoException {
        CipherKey key = getActualCipherKey();
        Cipher cipher = getCipherForContentEncryption();
        String[] contents = new String[encryptedContentParts.length];
        for (int i = 0; i < encryptedContentParts.length; i++) {
          if (encryptedContentParts[i] != null) {
            contents[i] = cipher.decrypt(StringUtil.fromBase64(encryptedContentParts[i]), key);
          }
        }
        return contents;
      }
    });
  }

  /**
   * Decrypts the specified encrypted content by using the encryption key that was set with the
   * {@link #updateCipherKey(String)} method.
   * </p>
   * The encrypted content is here in the form of a Map instance in which each entry represents a
   * field or a property of the encrypted content. The method returns also a Map with, for each
   * entry, the field or the property decrypted.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   *
   * @param encryptedContent the content to decrypt in the form of a Map instance. Each entry in the
   * Map represents a field/property of the content to decrypt.
   * @return a Map with the different field/property of the content decrypted.
   * @throws CryptoException the decryption of the content failed.
   */
  @Override
  public Map<String, String> decryptContent(final Map<String, String> encryptedContent)
      throws CryptoException {
    return ConcurrentEncryptionTaskExecutor
        .execute(new ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask() {
      @Override
      public boolean isPrivileged() {
        return false;
      }

      @Override
      public Map<String, String> execute() throws CryptoException {
        CipherKey key = getActualCipherKey();
        Cipher cipher = getCipherForContentEncryption();
        return decryptContent(encryptedContent, cipher, key);
      }
    });
  }

  /**
   * Decrypts the encrypted contents provided by the specified iterators.
   * <p/>
   * This method is for decrypting in batch several and possibly different encrypted contents. If
   * there is more than one iterator on contents, each of them will be taken in charge concurrently
   * by a pool of several threads.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   *
   * @param iterators the iterators on the contents to decrypt.
   */
  @Override
  public void decryptContents(final EncryptionContentIterator... iterators) throws CryptoException {
    ConcurrentEncryptionTaskExecutor.execute(CryptographicTask.decryptionOf(iterators));
  }

  /**
   * Renews explicitly the cipher of the contents provided by the specified iterators.
   * </p>
   * This method is mainly for encrypted contents for which the renew of their cipher has failed
   * when the encryption key has been updated.
   * </p>
   * The execution of this method will block any other call of the DefaultContentEncryptionService
   * methods for all of its instances in order to prevent incoherent state of encrypted contents.
   * Any attempts to execute one of the DefaultContentEncryptionService method, whereas this method
   * is running, will raise an IllegalStateException exception.
   * </p>
   * If it doesn't exist a previous encryption key required to decrypt the contents before
   * encrypting them with the actual encryption key, then nothing is performed by this method and it
   * will return silently.
   *
   * @param iterators the iterators on the encrypted contents for which their cipher has to be
   * renewed.
   * @throws CryptoException if an error occurs while renewing the cipher of the contents with the
   * actual encryption key.
   */
  @Override
  public void renewCipherOfContents(final EncryptionContentIterator... iterators)
      throws CryptoException {
    try {
      ConcurrentEncryptionTaskExecutor
          .execute(CryptographicTask.renewEncryptionOf(iterators).inPrivilegedMode());
    } catch (CryptoException ex) {
      if (ex.getCause() instanceof FileNotFoundException) {
        SilverLogger.getLogger(this).warn(ex.getMessage());
        return;
      }
      throw ex;
    }
  }

  /**
   * Encrypts the specified content by using the specified cipher with the specified cipher key.
   *
   * @param content the content to encrypt in the form of a {@link Map} in which each entry is a
   * property or a field of the content.
   * @param cipher the cipher to encrypt the content.
   * @param key the cipher key.
   * @return the encrypted content.
   * @throws CryptoException if an error occurs while encrypting the content.
   */
  protected static Map<String, String> encryptContent(Map<String, String> content, Cipher cipher,
      CipherKey key) throws CryptoException {
    Map<String, String> encryptedContents = new HashMap<String, String>(content.size());
    for (Map.Entry<String, String> aContent : content.entrySet()) {
      if (aContent.getValue() != null) {
        byte[] theEncryptedContent = cipher.encrypt(aContent.getValue(), key);
        encryptedContents.put(aContent.getKey(), StringUtil.asBase64(theEncryptedContent));
      }
    }
    return encryptedContents;
  }

  /**
   * Decrypts the specified content by using the specified cipher with the specified cipher key.
   *
   * @param encryptedContent the encrypted content to decrypt in the form of a {@link Map} in which
   * each entry is a property or a field of the content.
   * @param cipher the cipher to decrypt the content.
   * @param key the cipher key.
   * @return the decrypted content.
   * @throws CryptoException if an error occurs while decrypting the content.
   */
  protected static Map<String, String> decryptContent(Map<String, String> encryptedContent,
      Cipher cipher, CipherKey key) throws CryptoException {
    Map<String, String> content = new HashMap<String, String>(encryptedContent.size());
    for (Map.Entry<String, String> anEncryptedContent : encryptedContent.entrySet()) {
      if (anEncryptedContent.getValue() != null) {
        String aContent = cipher.decrypt(StringUtil.fromBase64(anEncryptedContent.getValue()), key);
        content.put(anEncryptedContent.getKey(), aContent);
      }
    }
    return content;
  }

  /**
   * Gets the actual cipher key to use in the content encryption/decryption.
   *
   * @return the asked cipher key.
   * @throws CryptoException if the cipher key cannot be get.
   */
  protected static CipherKey getActualCipherKey() throws CryptoException {
    return getCipherKey(ACTUAL_KEY_FILE_PATH);
  }

  /**
   * Gets the previous cipher key that was used in the content encryption/decryption.
   *
   * @return the asked cipher key.
   * @throws CryptoException if the cipher key cannot be get.
   */
  protected static CipherKey getPreviousCipherKey() throws CryptoException {
    return getCipherKey(DEPRECATED_KEY_FILE_PATH);
  }

  private static CipherKey getCipherKey(String keyFilePath) throws CryptoException {
    String key = null;
    try {
      File keyFile = new File(keyFilePath);
      String[] keys = FileUtil.readFileToString(keyFile).split(KEY_SEP);
      Cipher cipher = getCipherForKeyEncryption();
      CipherKey encryptionKey = CipherKey.aKeyFromBase64Text(keys[0]);
      key = cipher.decrypt(StringUtil.fromBase64(keys[1]), encryptionKey);
      return CipherKey.aKeyFromHexText(key);
    } catch (IOException ex) {
      throw new CryptoException("Cannot get the encryption key", ex);
    } catch (ParseException ex) {
      throw new CryptoException("Hum... the key isn't in hexadecimal: '" + key + "'", ex);
    }
  }

  private static void assertKeyIsInHexadecimal(String key) {
    try {
      EncodingUtil.fromHex(key);
    } catch (ParseException ex) {
      throw new AssertionError("The encryption key '" + key + "' must be in hexadecimal");
    }
  }

  private static void assertKeyIsIn256Bits(String key) {
    if (key.length() != 64) {
      throw new AssertionError("The encryption key '" + key + "' must be in 256 bits");
    }
  }

  /**
   * Gets the cipher to use to encrypt/decrypt a content.
   *
   * @return the cipher used in the content encryption.
   */
  protected static Cipher getCipherForContentEncryption() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    return cipherFactory.getCipher(CryptographicAlgorithmName.AES);
  }

  private static Cipher getCipherForKeyEncryption() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    return cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
  }

  private static void setHidden(String file) {
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      try {
        Runtime.getRuntime().exec("attrib +H " + file);
      } catch (IOException ex) {
        SilverLogger.getLogger(DefaultContentEncryptionService.class).warn(ex.getMessage());
      }
    }
  }

  @Override
  public boolean isCipherKeyDefined() {
    try {
      getActualCipherKey();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Wrapper of a {@link EncryptionContentIterator} instance in the goal of controlling the
   * execution flow of the underlying iterator by catching any RuntimeException or Throwable.
   * </p>
   * Mainly to be used in the cipher key update.
   */
  private static class EncryptionContentIteratorWrapper implements EncryptionContentIterator {

    private final EncryptionContentIterator wrapped;

    public EncryptionContentIteratorWrapper(EncryptionContentIterator iterator) {
      wrapped = iterator;
    }

    @Override
    public Map<String, String> next() {
      return wrapped.next();
    }

    @Override
    public boolean hasNext() {
      return wrapped.hasNext();
    }

    @Override
    public void update(
        Map<String, String> updatedContent) {
      wrapped.update(updatedContent);
    }

    @Override
    public void onError(Map<String, String> content, CryptoException ex) {
      try {
        wrapped.onError(content, ex);
      } catch (Throwable t) {
      }
      throw new CipherRenewingException(ex);
    }

    @Override
    public void remove() {
      wrapped.remove();
    }

    @Override
    public void init() {
      wrapped.init();
    }
  }
}