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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It is a security service for protecting content from an access in plain data.
 * The service provides the encryption and the decryption of content by using a
 * symmetric-key cryptographic algorithm for which it manages the key.
 * </p>
 * This service doesn't maintain the knowledge of the contents that were encrypted; it is not of
 * its
 * responsibility. Therefore, when the encryption key is updated, it is the responsibility of the
 * content management services to inform this service the encrypted contents they manage. For
 * doing,
 * it provides them an interface to register a content provider in the form of an
 * {@link EncryptionContentIterator} object. These iterators will be then used directly by the
 * content encryption service to renew their cipher when the key is updated.
 * </p>
 * When the encryption key is updated, all of the encryption and decryption capabilities are then
 * locked. If a call is performed to one of this service's methods, an IllegalStateException is
 * thrown.
 */
@Named
public class ContentEncryptionService {

  private static final String ACTUAL_KEY_FILE_PATH =
      FileRepositoryManager.getSecurityDirPath() + ".aid_key";
  private static final String DEPRECATED_KEY_FILE_PATH =
      FileRepositoryManager.getSecurityDirPath() + ".did_key";
  private static final CipherKey CIPHER_KEY;

  private static List<EncryptionContentIterator> contentIterators =
      new CopyOnWriteArrayList<EncryptionContentIterator>();

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
   * Registers the specified iterator on some encrypted contents for which the cipher has to be
   * renewed when the encryption key is updated.
   * </p>
   * This method is dedicated to the content management service for providing to the content
   * encryption services a way to access the encrypted contents they manage in order to renew their
   * cipher when the encryption key is updated.
   * @param iterator a provider of encrypted content in the form of a
   * {@link EncryptionContentIterator} iterator.
   */
  public void registerForRenewingContentCipher(final EncryptionContentIterator iterator) {
    contentIterators.add(iterator);
  }

  /**
   * Updates the key to use to encrypt and to decrypt the enciphered content. The key must be in
   * hexadecimal otherwise an AssertionError will be thrown.
   * </p>
   * This key will be stored into a file and will be also encrypted by another cryptographic
   * algorithm whereas the previous key is saved into another file as a deprecated key. The
   * old key will be used only to decrypt the content that was encrypted with it in the goal to
   * encrypt them again with the new key.
   * </p>
   * The execution of this method will block any other call of the ContentEncryptionService
   * methods for all of its instances in order to prevent incoherent state of encrypted contents.
   * Any attempts to execute one of the ContentEncryptionService method, whereas this method is
   * running, will raise an IllegalStateException exception.
   * @param key the new symmetric key in hexadecimal.
   * @throws CryptoException if an error while updating the encryption key.
   */
  public void updateCipherKey(final String key) throws CryptoException {
    assertKeyIsInHexadecimal(key);
    ConcurrentEncryptionTaskExecutor
        .execute(new ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask() {

          @Override
          public boolean isPrivileged() {
            return true;
          }

          @Override
          public Void execute() throws CryptoException {
            try {
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

              EncryptionContentIterator[] iterators =
                  contentIterators.toArray(new EncryptionContentIterator[contentIterators.size()]);
              return CryptographicTask.renewEncryptionOf(iterators).execute();
            } catch (IOException ex) {
              throw new CryptoException("Cannot update the encryption key", ex);
            }
          }
        });
  }

  /**
   * Encrypts the specified content by using the encryption key that was set with the
   * {@link #updateCipherKey(String)} method.
   * @param contentParts either the different part of a content to encrypt or several single
   * textual
   * contents to encrypt.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   * @return an array with the different parts of the content, encrypted and in base64, in the same
   *         order they were passed as argument of this method.
   * @throws CryptoException the encryption of one of the content (or content part) failed.
   */
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
   * @param content the content to encrypt in the form of a Map instance. Each entry in the Map
   * represents a field/property of the content to encrypt.
   * @return a Map with the different field/property of the content encrypted.
   * @throws CryptoException the encryption of the content failed.
   */
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
   * This method is for encrypting in batch several and possibly different contents.
   * If there is more than one iterator on contents, each of them will be taken in charge
   * concurrently by a pool of several threads.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   * @param iterators the iterators on the contents to encrypt.
   */
  public void encryptContents(final EncryptionContentIterator... iterators) throws CryptoException {
    ConcurrentEncryptionTaskExecutor.execute(CryptographicTask.encryptionOf(iterators));
  }

  /**
   * Decrypts the specified encrypted content by using the encryption key that was set with the
   * {@link #updateCipherKey(String)} method.
   * @param encryptedContentParts either the different part of an encrypted content to decrypt or
   * several single encrypted textual contents to decrypt.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   * @return an array with the different parts of the decrypted content in the same order they were
   *         passed as argument of this method.
   * @throws CryptoException the decryption of one of the encrypted content (or content part)
   * failed.
   */
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
   * @param encryptedContent the content to decrypt in the form of a Map instance. Each entry in
   * the Map represents a field/property of the content to decrypt.
   * @return a Map with the different field/property of the content decrypted.
   * @throws CryptoException the decryption of the content failed.
   */
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
   * This method is for decrypting in batch several and possibly different encrypted contents.
   * If there is more than one iterator on contents, each of them will be taken in charge
   * concurrently by a pool of several threads.
   * </p>
   * If the encryption key is is being updated, an IllegalStateException is thrown.
   * @param iterators the iterators on the contents to decrypt.
   */
  public void decryptContents(final EncryptionContentIterator... iterators) throws CryptoException {
    ConcurrentEncryptionTaskExecutor.execute(CryptographicTask.decryptionOf(iterators));
  }

  /**
   * Renews explicitly the cipher of the contents provided by the specified iterators.
   * </p>
   * This method is mainly for encrypted contents for which the renew of their cipher has failed
   * when the encryption key has been updated.
   * </p>
   * The execution of this method will block any other call of the ContentEncryptionService
   * methods for all of its instances in order to prevent incoherent state of encrypted contents.
   * Any attempts to execute one of the ContentEncryptionService method, whereas this method is
   * running, will raise an IllegalStateException exception.
   * </p>
   * If it doesn't exist a previous encryption key required to decrypt the contents before
   * encrypting them with the actual encryption key, then nothing is performed by this method and
   * it will return silently.
   * @param iterators the iterators on the encrypted contents for which their cipher has to be
   * renewed.
   * @throws CryptoException if an error occurs while renewing the cipher of the contents with the
   * actual encryption key.
   */
  public void renewCipherOfContents(final EncryptionContentIterator... iterators)
      throws CryptoException {
    try {
      ConcurrentEncryptionTaskExecutor
          .execute(CryptographicTask.renewEncryptionOf(iterators).inPrivilegedMode());
    } catch (CryptoException ex) {
      if (ex.getCause() instanceof FileNotFoundException) {
        Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, ex.getMessage());
        return;
      }
      throw ex;
    }
  }

  /**
   * Encrypts the specified content by using the specified cipher with the specified cipher key.
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
   * @return the asked cipher key.
   * @throws CryptoException if the cipher key cannot be get.
   */
  protected static CipherKey getActualCipherKey() throws CryptoException {
    return getCipherKey(ACTUAL_KEY_FILE_PATH);
  }

  /**
   * Gets the previous cipher key that was used in the content encryption/decryption.
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

  /**
   * Gets the cipher to use to encrypt/decrypt a content.
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

}