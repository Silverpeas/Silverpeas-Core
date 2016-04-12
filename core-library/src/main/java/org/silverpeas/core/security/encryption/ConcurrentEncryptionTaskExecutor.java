package org.silverpeas.core.security.encryption;

import java.util.concurrent.Semaphore;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

/**
 * Executor of concurrent encryption tasks for the ContentEncryptionService instances. The
 * concurrent encryption tasks can be ran simultaneously in several threads, the aim of the
 * ConcurrentEncryptionTaskExecutor is then to manage the concurrency over these different tasks by
 * executing them for the account of the threads they belong to with the use of a semaphore.
 * </p>
 * This class is to be used only by the ContentEncryptionService instances for their inner
 * operations.
 * </p>
 * The concurrency is done according the following policy: a non-privileged task can be ran
 * concurrently with other non-privileged tasks. When a privileged task has to be executed
 * concurrently, it waits for the completion of other tasks and then it is ran in a privileged mode,
 * that is to say it is the alone to be ran and the others tasks (privileged and non-privileged
 * ones) are blocked until it has finished its work.
 */
class ConcurrentEncryptionTaskExecutor {

  // safe value taken from the ReentrantLock API
  private static final int TOKEN_COUNT = 2147483647;
  private static final Semaphore semaphore = new Semaphore(TOKEN_COUNT);

  /**
   * Executes concurrently the specified task according to the following policy: a non-privileged
   * task can be ran concurrently with other non-privileged tasks. When a privileged task has to be
   * executed concurrently, it waits for the completion of other tasks and then it is ran in a
   * privileged mode, that is to say it is the alone to be ran and the others tasks (privileged and
   * non-privileged ones) are blocked until it has finished its work.
   *
   * @param task the task to execute.
   * @param <T> the return type of the task execution.
   * @return the result of the task execution.
   * @throws CryptoException if an error has occured while executing the encryption task.
   */
  public static <T> T execute(ConcurrentEncryptionTask task) throws CryptoException {
    int token = task.isPrivileged() ? TOKEN_COUNT : 1;
    try {
      acquireToken(token);
      return task.execute();
    } finally {
      releaseToken(token);
    }
  }

  private static synchronized void acquireToken(int token) {
    if (semaphore.availablePermits() == 0) {
      throw new IllegalStateException("The encryption is being updated: the content encryption "
          + "and decryption service is blocked");
    }
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private static synchronized void releaseToken(int token) {
    semaphore.release();
  }

  /**
   * An encryption task to execute concurrently by this executor.
   */
  public static interface ConcurrentEncryptionTask {

    /**
     * Is this task has to be ran in a privileged mode?
     *
     * @return true if this task has to be ran in a privileged mode, that is to say by blocking all
     * others concurrent tasks.
     */
    public boolean isPrivileged();

    /**
     * Executes the task.
     *
     * @param <T> the type of the result of the task execution.
     * @return the result of the task.
     * @throws CryptoException if an error occurs while executing its encryption task.
     */
    public <T> T execute() throws CryptoException;
  }
}
