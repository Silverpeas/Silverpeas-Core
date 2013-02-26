package com.silverpeas.util.security;

import org.silverpeas.util.crypto.Cipher;
import org.silverpeas.util.crypto.CipherKey;
import org.silverpeas.util.crypto.CryptoException;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A cryptographic task the ContentEncryptionService instances can perform on the contents
 * provided by some iterators on content: content encryption, content decryption or content
 * encryption renewing.
 * </p>
 * This class is to be used only by the ContentEncryptionService instances for their inner
 * operations.
 * </p>
 * A cryptographic task is a concurrent one so it can be executed concurrently along other
 * cryptographic tasks.
 * </p>
 * By default, the task will be executed in non-privileged mode, that is to say that the task
 * will not block other cryptographic tasks awaiting execution.
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
   * @param contents the iterators on the contents to encrypt.
   * @return an encryption task.
   */
  public static CryptographicTask encryptionOf(EncryptionContentIterator... contents) {
    return new CryptographicTask(CryptographicTask.Type.ENCRYPTION, contents);
  }

  /**
   * Creates a task to decrypt the contents provided by the specified iterators.
   * @param contents the iterators on the contents to decrypt.
   * @return an encryption task.
   */
  public static CryptographicTask decryptionOf(EncryptionContentIterator... contents) {
    return new CryptographicTask(CryptographicTask.Type.DECRYPTION, contents);
  }

  /**
   * Creates a task to renew the encryption of the contents provided by the specified iterators.
   * @param contents the iterators on the contents for which the cipher has to be renewed.
   * @return an encryption task.
   */
  public static CryptographicTask renewEncryptionOf(EncryptionContentIterator... contents) {
    return new CryptographicTask(CryptographicTask.Type.RENEW, contents);
  }

  /**
   * The task must be executed in a privileged mode, that is to say its execution will block the
   * others cryptographic tasks awaiting execution.
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

  @Override
  public Void execute() throws CryptoException {
    final Cipher cipher = ContentEncryptionService.getCipherForContentEncryption();
    final CipherKey actualKey = ContentEncryptionService.getActualCipherKey();
    final CipherKey previousKey =
        this.task == Type.RENEW ? ContentEncryptionService.getPreviousCipherKey() : null;
    if (iterators.length > 1) {
      final ExecutorService executor = getTaskExecutor(iterators.length);
      for (final EncryptionContentIterator someContents : iterators) {
        executor.submit(new Runnable() {
          @Override
          public void run() {
            process(someContents, cipher, actualKey, previousKey);
          }
        });
      }
      executor.shutdown();
      while (!executor.isTerminated()) {
      }
    } else if (iterators.length != 0) {
      process(iterators[0], cipher, actualKey, previousKey);
    }
    return null;
  }

  private void process(EncryptionContentIterator theContents, Cipher cipher, CipherKey actualKey,
      CipherKey previousKey) {
    for (; theContents.hasNext(); ) {
      Map<String, String> content = theContents.next();
      try {
        switch (task) {
          case ENCRYPTION:
            content = ContentEncryptionService.encryptContent(content, cipher, actualKey);
            break;
          case DECRYPTION:
            content = ContentEncryptionService.decryptContent(content, cipher, actualKey);
            break;
          case RENEW:
            content = ContentEncryptionService.decryptContent(content, cipher, previousKey);
            content = ContentEncryptionService.encryptContent(content, cipher, actualKey);
            break;
        }
        theContents.update(content);
      } catch (CryptoException ex) {
        theContents.onError(content, ex);
      }
    }
  }

  CryptographicTask(Type taskType, EncryptionContentIterator... iterators) {
    this.task = taskType;
    this.iterators = iterators;
  }

  private static ExecutorService getTaskExecutor(int taskCount) {
    final int maxThreads = Runtime.getRuntime().availableProcessors();
    final int threads = Math.min(taskCount, maxThreads);
    return Executors.newFixedThreadPool(threads);
  }
}
