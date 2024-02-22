/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption;

import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

import java.util.Map;

/**
 * A cryptographic task the DefaultContentEncryptionService instances can perform on the contents
 * provided by some iterators on content: content encryption, content decryption or content
 * encryption renewing.
 * </p>
 * This class is to be used only by the DefaultContentEncryptionService instances for their inner
 * operations.
 * </p>
 * A cryptographic task is a concurrent one so it can be executed concurrently along other
 * cryptographic tasks.
 * </p>
 * By default, the task will be executed in non-privileged mode, that is to say that the task will
 * not block other cryptographic tasks awaiting execution.
 */
class CryptographicTask implements ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask<Void> {

  private enum Type {
    ENCRYPTION,
    DECRYPTION,
    RENEW
  }

  private final Type task;
  private final EncryptionContentIterator[] iterators;
  private boolean privileged = false;

  /**
   * Creates a task to encrypt the contents provided by the specified iterators.
   *
   * @param contents the iterators on the contents to encrypt.
   * @return an encryption task.
   */
  public static CryptographicTask encryptionOf(EncryptionContentIterator... contents) {
    return new CryptographicTask(CryptographicTask.Type.ENCRYPTION, contents);
  }

  /**
   * Creates a task to decrypt the contents provided by the specified iterators.
   *
   * @param contents the iterators on the contents to decrypt.
   * @return an encryption task.
   */
  public static CryptographicTask decryptionOf(EncryptionContentIterator... contents) {
    return new CryptographicTask(CryptographicTask.Type.DECRYPTION, contents);
  }

  /**
   * Creates a task to renew the encryption of the contents provided by the specified iterators.
   *
   * @param contents the iterators on the contents for which the cipher has to be renewed.
   * @return an encryption task.
   */
  public static CryptographicTask renewEncryptionOf(EncryptionContentIterator... contents) {
    return new CryptographicTask(CryptographicTask.Type.RENEW, contents);
  }

  /**
   * The task must be executed in a privileged mode, that is to say its execution will block the
   * others cryptographic tasks awaiting execution.
   *
   * @return itself.
   */
  public CryptographicTask inPrivilegedMode() {
    this.privileged = true;
    return this;
  }

  @Override
  public boolean isPrivileged() {
    return privileged;
  }

  /**
   * Executes a cryptographic task on the contents provided by some
   * {@link EncryptionContentIterator} iterators among the encryption, the decryption and the
   * cipher renew. If an error occurs while performing the cryptographic function on a given
   * content, the exception describing the error is passed to the iterator and the task is stopped.
   *
   * @return nothing
   * @throws CryptoException if an error occurs while executing its encryption task.
   */
  @Override
  public Void execute() throws CryptoException {
    final Cipher cipher = DefaultContentEncryptionService.getCipherForContentEncryption();
    final CipherKey actualKey = DefaultContentEncryptionService.getActualCipherKey();
    final CipherKey previousKey =
        this.task == Type.RENEW ? DefaultContentEncryptionService.getPreviousCipherKey() : null;
    for (EncryptionContentIterator encryptionContentIterator : iterators) {
      process(encryptionContentIterator, cipher, actualKey, previousKey);
    }
    return null;
  }

  private void process(EncryptionContentIterator theContents, Cipher cipher, CipherKey actualKey,
      CipherKey previousKey) {
    theContents.init();
    Map<String, String> content = null;
    try {
      while (theContents.hasNext()) {
        content = theContents.next();
        switch (task) {
          case ENCRYPTION:
            content = DefaultContentEncryptionService.encryptContent(content, cipher, actualKey);
            break;
          case DECRYPTION:
            content = DefaultContentEncryptionService.decryptContent(content, cipher, actualKey);
            break;
          case RENEW:
            content = DefaultContentEncryptionService.decryptContent(content, cipher, previousKey);
            content = DefaultContentEncryptionService.encryptContent(content, cipher, actualKey);
            break;
        }
        theContents.update(content);
      }
    } catch (Exception ex) {
      theContents.onError(content, new CryptoException(ex.getMessage(), ex));
    }
  }

  CryptographicTask(Type taskType, EncryptionContentIterator... iterators) {
    this.task = taskType;
    this.iterators = iterators;
  }
}
