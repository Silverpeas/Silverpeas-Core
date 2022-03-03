/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.security.encryption.cipher.CryptoException;

import java.util.concurrent.Semaphore;

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
   * @throws CryptoException if an error has occurred while executing the encryption task.
   */
  public static <T> T execute(ConcurrentEncryptionTask task)
      throws CryptoException {
    try {
      acquireMutex();
      return task.execute();
    } finally {
      releaseMutex();
    }
  }

  private static synchronized void acquireMutex() {
    if (semaphore.availablePermits() == 0) {
      throw new IllegalStateException("The encryption is being updated: the content encryption "
          + "and decryption service is blocked");
    }
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private static synchronized void releaseMutex() {
    semaphore.release();
  }

  /**
   * An encryption task to execute concurrently by this executor.
   */
  public interface ConcurrentEncryptionTask {

    /**
     * Is this task has to be ran in a privileged mode?
     *
     * @return true if this task has to be ran in a privileged mode, that is to say by blocking all
     * others concurrent tasks.
     */
    boolean isPrivileged();

    /**
     * Executes the task.
     *
     * @param <T> the type of the result of the task execution.
     * @return the result of the task.
     * @throws CryptoException if an error occurs while executing its encryption task.
     */
    <T> T execute() throws CryptoException;
  }
}
