/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption;

import java.util.Map;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

/**
 * It is a security service for protecting content from an access in plain data. The service
 * provides the encryption and the decryption of content by using a symmetric-key cryptographic
 * algorithm for which it manages the key.
 * </p>
 * The implementation of this service must be accessible within an IoC container under the name
 * "contentEncryptionService".
 * </p>
 * This service doesn't maintain the knowledge of the contents that were encrypted; it is not of its
 * responsibility. Therefore, when the encryption key is updated, it is the responsibility of the
 * content management services to provide this service the encrypted contents they manage. For
 * doing, it provides them an interface to register a content provider in the form of an
 * {@link EncryptionContentIterator} object. These iterators will be then used directly by the
 * content encryption service to renew their cipher when the key is updated.
 * </p>
 * When the encryption key is updated, all of the encryption and decryption capabilities are then
 * locked. If a call is performed to one of this service's methods, an IllegalStateException is
 * thrown.
 *
 * @author mmoquillon
 */
public interface ContentEncryptionService {

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
  String[] decryptContent(final String... encryptedContentParts) throws CryptoException;

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
  Map<String, String> decryptContent(final Map<String, String> encryptedContent)
      throws CryptoException;

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
  void decryptContents(final EncryptionContentIterator... iterators)
      throws CryptoException;

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
  String[] encryptContent(final String... contentParts) throws CryptoException;

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
  Map<String, String> encryptContent(final Map<String, String> content) throws CryptoException;

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
  void encryptContents(final EncryptionContentIterator... iterators) throws CryptoException;

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
  void registerForRenewingContentCipher(final EncryptionContentIterator iterator);

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
   * @throws CipherKeyUpdateException if the replace of the cipher key has failed.
   * @throws CryptoException if an error while renewing the cipher of the encrypted contents with
   * the new cipher key.
   */
  void renewCipherOfContents(final EncryptionContentIterator... iterators) throws
      CipherKeyUpdateException, CryptoException;

  /**
   * Updates the key to use to encrypt and to decrypt the enciphered content. The key must be in
   * hexadecimal otherwise an AssertionError will be thrown. If no previous key existed, then the
   * cipher key will be created with this specified one and it will be used to encrypt and to
   * decrypt at the demand the content in Silverpeas.
   * </p>
   * The update of the key triggers automatically the renew of the cipher of the encrypted contents
   * in Silverpeas with the new cipher key. If one of the cipher renew of one of the encrypted
   * content failed, the key update is rolled-back (the key isn't updated) and a
   * {@link CryptoException} is thrown.
   * </p>
   * The execution of this method will block any other call of the DefaultContentEncryptionService
   * methods for all of its instances in order to prevent incoherent state of encrypted contents.
   * Any attempts to execute one of the DefaultContentEncryptionService method, whereas this method
   * is running, will raise an IllegalStateException exception.
   *
   * @param key the new symmetric key in hexadecimal.
   * @throws CipherKeyUpdateException if the update of the cipher key has failed.
   * @throws CryptoException if an error while renewing the cipher of the encrypted contents with
   * the new cipher key.
   */
  void updateCipherKey(final String key) throws CipherKeyUpdateException, CryptoException;

  /**
   * Checks if a key is defined and so if content can be encrypted
   * @return true if the key exist and it is valid
   */
  boolean isCipherKeyDefined();
}