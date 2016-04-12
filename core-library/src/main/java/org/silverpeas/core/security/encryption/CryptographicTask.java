package org.silverpeas.core.security.encryption;

import java.util.Map;
import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

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
class CryptographicTask implements ConcurrentEncryptionTaskExecutor.ConcurrentEncryptionTask {

  private static enum Type {

    ENCRYPTION,
    DECRYPTION,
    RENEW;
  }
  private final Type task;
  private EncryptionContentIterator[] iterators;
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
   * chipher renew. If an error occurs while performing the cryptographic function on a given
   * content, the exception describing the error is passed to the iterator and the task is stopped.
   *
   * @param <T> the type of the result of the task execution.
   * @return the result of the task.
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
      for (; theContents.hasNext();) {
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
    } catch (Throwable ex) {
      theContents.onError(content, new CryptoException(ex.getMessage(), ex));
    }
  }

  CryptographicTask(Type taskType, EncryptionContentIterator... iterators) {
    this.task = taskType;
    this.iterators = iterators;
  }
}
