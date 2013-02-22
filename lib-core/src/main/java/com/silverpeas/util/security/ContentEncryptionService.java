package com.silverpeas.util.security;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherFactory;
import org.silverpeas.util.crypto.CipherKey;
import org.silverpeas.util.crypto.CryptoException;
import org.silverpeas.util.crypto.CryptographicAlgorithmName;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * It is a security service for protecting content from an access in plain data.
 * The service provides the encryption and the decryption of content by using a
 * key-symmetric cryptographic algorithm for which it manages the key.
 */
@Named
public class ContentEncryptionService {

  protected static final String ACTUAL_KEY_FILE_PATH =
      FileRepositoryManager.getSecurityDirPath() + ".aid_key";
  protected static final String DEPRECATED_KEY_FILE_PATH =
      FileRepositoryManager.getSecurityDirPath() + ".did_key";
  protected static final CipherKey CIPHER_KEY;

  static {
    try {
      CIPHER_KEY = CipherKey.aKeyFromHexText("06277d1ce530c94bd9a13a72a58342be");
    } catch (ParseException e) {
      throw new RuntimeException("Cannot create the cryptographic key!", e);
    }
  }

  protected ContentEncryptionService() {

  }

  /**
   * Updates the key to use to encrypt and to decrypt the enciphered content. The key must be in
   * hexadecimal otherwise an AssertionError will be thrown.
   * <p/>
   * This key will be stored into a file and will be also encrypted by another cryptographic
   * algorithm whereas the previous key is saved into another file as a deprecated key. The
   * old key will be used only to decrypt the content that was encrypted with it in the goal to
   * encrypt them again with the new key.
   * @param key the new symmetric key in hexadecimal.
   */
  public void updateEncryptionKey(String key) throws CryptoException, IOException {
    assertKeyIsInHexadecimal(key);
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    if (keyFile.exists()) {
      File deprecatedKeyFile = new File(DEPRECATED_KEY_FILE_PATH);
      if (deprecatedKeyFile.exists()) {
        deprecatedKeyFile.setWritable(true);
      }
      FileUtil.moveFile(keyFile, deprecatedKeyFile);
    }

    Cipher cipher = getCipherForKeyEncryption();
    byte[] encryptedKey = cipher.encrypt(key, CIPHER_KEY);
    FileUtil.writeFile(keyFile, new StringReader(StringUtil.asBase64(encryptedKey)));
    keyFile.setReadOnly();
  }

  /**
   * Encrypts the specified content by using the encryption key that was set with the
   * {@link #updateEncryptionKey(String)} method.
   * @param contents either the different part of a content to encrypt or several single textual
   * contents to encrypt.
   * @return an array with the different parts of the content, encrypted and in base64, in the same
   *         order they were passed as argument of this method.
   * @throws CryptoException the encryption of one of the content (or content part) failed.
   */
  public String[] encryptContent(final String... contents) throws CryptoException {
    CipherKey key = getEncryptionKey();
    Cipher cipher = getCipherForContentEncryption();
    String[] encryptedContents = new String[contents.length];
    for (int i = 0; i < contents.length; i++) {
      if (contents[i] != null) {
        byte[] theEncryptedContent = cipher.encrypt(contents[i], key);
        encryptedContents[i] = StringUtil.asBase64(theEncryptedContent);
      }
    }
    return encryptedContents;
  }

  /**
   * Encrypts the specified content by using the encryption key that was set with the
   * {@link #updateEncryptionKey(String)} method.
   * </p>
   * The content is here in the form of a Map instance in which each entry represents a field or a
   * property of the content. The method returns also a Map with, for each entry, the field or the
   * property encrypted and in base64.
   * @param content the content to encrypt in the form of a Map instance. Each entry in the Map
   * represents a field/property of the content to encrypt.
   * @return a Map with the different field/property of the content encrypted.
   * @throws CryptoException the encryption of the content failed.
   */
  public Map<String, String> encryptContent(final Map<String, String> content)
      throws CryptoException {
    CipherKey key = getEncryptionKey();
    Cipher cipher = getCipherForContentEncryption();
    return encryptContent(content, cipher, key);
  }

  /**
   * Encrypts the contents provided by the specified providers.
   * <p/>
   * This method is for encrypting in batch several and possibly different contents.
   * If there is more than one provider, each of them will be taken in charge concurrently by a pool
   * of several threads.
   * @param providers the providers of contents to encrypt.
   */
  public void encryptContents(final ContentProvider... providers) throws CryptoException {
    CipherKey key = getEncryptionKey();
    Cipher cipher = getCipherForContentEncryption();
    if (providers.length > 1) {
      final int threads = Math.min(providers.length, 5); // 5 is computed empirically in an i7
      ExecutorService executor = Executors.newFixedThreadPool(threads);
      for (final ContentProvider aProvider : providers) {
        CryptgraphicTask task =
            new CryptgraphicTask(CryptgraphicTask.ENCRYPTION)
                .withContentProvider(aProvider)
                .withCipherAndKey(cipher, key);
        executor.submit(task);
      }
      executor.shutdown();
      while (!executor.isTerminated()) {
      }
    } else {
      new CryptgraphicTask(CryptgraphicTask.ENCRYPTION)
          .withContentProvider(providers[0])
          .withCipherAndKey(cipher, key).run();
    }
  }

  /**
   * Decrypts the specified encrypted content by using the encryption key that was set with the
   * {@link #updateEncryptionKey(String)} method.
   * @param encryptedContents either the different part of an encrypted content to decrypt or
   *  several single encrypted textual contents to decrypt.
   * @return an array with the different parts of the decrypted content in the same order they were
   * passed as argument of this method.
   * @throws CryptoException the decryption of one of the encrypted content (or content part)
   * failed.
   */
  public String[] decryptContent(final String... encryptedContents) throws CryptoException {
    CipherKey key = getEncryptionKey();
    Cipher cipher = getCipherForContentEncryption();
    String[] contents = new String[encryptedContents.length];
    for (int i = 0; i < encryptedContents.length; i++) {
      if (encryptedContents[i] != null) {
        contents[i] = cipher.decrypt(StringUtil.fromBase64(encryptedContents[i]), key);
      }
    }
    return contents;
  }

  /**
   * Decrypts the specified encrypted content by using the encryption key that was set with the
   * {@link #updateEncryptionKey(String)} method.
   * </p>
   * The encrypted content is here in the form of a Map instance in which each entry represents a
   * field or a property of the encrypted content. The method returns also a Map with, for each
   * entry, the field or the property decrypted.
   * @param encryptedContent the content to decrypt in the form of a Map instance. Each entry in
   * the Map represents a field/property of the content to decrypt.
   * @return a Map with the different field/property of the content decrypted.
   * @throws CryptoException the decryption of the content failed.
   */
  public Map<String, String> decryptContent(final Map<String, String> encryptedContent)
      throws CryptoException {
    CipherKey key = getEncryptionKey();
    Cipher cipher = getCipherForContentEncryption();
    return decryptContent(encryptedContent, cipher, key);
  }

  /**
   * Decrypts the encrypted contents provided by the specified providers.
   * <p/>
   * This method is for decrypting in batch several and possibly different encrypted contents.
   * If there is more than one provider, each of them will be taken in charge by a different
   * thread.
   * @param providers the providers of contents to decrypt.
   */
  public void decryptContents(final ContentProvider... providers) throws CryptoException {
    CipherKey key = getEncryptionKey();
    Cipher cipher = getCipherForContentEncryption();
    if (providers.length > 1) {
      final int threads = Math.min(providers.length, 5); // 5 is computed empirically in an i7
      ExecutorService executor = Executors.newFixedThreadPool(threads);
      for (final ContentProvider aProvider : providers) {
        CryptgraphicTask task =
            new CryptgraphicTask(CryptgraphicTask.DECRYPTION)
                .withContentProvider(aProvider)
                .withCipherAndKey(cipher, key);
        executor.submit(task);
      }
      executor.shutdown();
      while (!executor.isTerminated()) {
      }
    } else {
      new CryptgraphicTask(CryptgraphicTask.DECRYPTION)
          .withContentProvider(providers[0])
          .withCipherAndKey(cipher, key).run();
    }
  }


  private CipherKey getEncryptionKey() throws CryptoException {
    String key = null;
    try {
      File keyFile = new File(ACTUAL_KEY_FILE_PATH);
      String encryptedKey = FileUtil.readFileToString(keyFile);
      Cipher cipher = getCipherForKeyEncryption();
      key = cipher.decrypt(StringUtil.fromBase64(encryptedKey), CIPHER_KEY);
      return CipherKey.aKeyFromHexText(key);
    } catch (IOException ex) {
      throw new CryptoException("Cannot get the encryption key", ex);
    } catch (ParseException ex) {
      throw new CryptoException("Hum... the key isn't in hexadecimal: '" + key + "'", ex);
    }
  }

  private static void assertKeyIsInHexadecimal(String key) {
    try {
      StringUtil.fromHex(key);
    } catch (ParseException ex) {
      throw new AssertionError("The encryption key '" + key + "' must be in hexadecimal");
    }
  }

  private static Cipher getCipherForContentEncryption() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    return cipherFactory.getCipher(CryptographicAlgorithmName.AES);
  }

  private static Cipher getCipherForKeyEncryption() {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    return cipherFactory.getCipher(CryptographicAlgorithmName.CAST5);
  }

  private static Map<String, String> encryptContent(Map<String, String> content, Cipher cipher,
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

  private static Map<String, String> decryptContent(Map<String, String> encryptedContent,
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

  private class CryptgraphicTask implements Runnable {

    public static final byte ENCRYPTION = 0;
    public static final byte DECRYPTION = 1;

    private byte task;
    private ContentProvider provider;
    private Cipher cipher;
    private CipherKey cipherKey;

    public CryptgraphicTask(byte taskType) {
      this.task = taskType;
    }

    public CryptgraphicTask withContentProvider(ContentProvider provider) {
      this.provider = provider;
      return this;
    }

    public CryptgraphicTask withCipherAndKey(Cipher cipher, CipherKey key) {
      this.cipher = cipher;
      this.cipherKey = key;
      return this;
    }

    @Override
    public void run() {
      for (; provider.hasNextContent(); ) {
        Map<String, String> content = provider.getContent();
        try {
          if (task == ENCRYPTION) {
            provider.setUpdatedContent(encryptContent(content, cipher, cipherKey));
          } else {
            provider.setUpdatedContent(decryptContent(content, cipher, cipherKey));
          }
        } catch (CryptoException ex) {
          provider.onError(content, ex);
        }
      }
    }
  }
}
