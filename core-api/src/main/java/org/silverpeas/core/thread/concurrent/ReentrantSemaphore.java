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

package org.silverpeas.core.thread.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Management of a Reentrant {@link Semaphore}.
 * <p>
 * When a thread is calling several times the {@link #acquire()} method, only the first call is
 * acquiring effectively a permits.<br/>
 * The advantage of this behavior is not getting a self thread blocking in case of several
 * semaphore permission requests.
 * </p>
 * @author silveryocha
 * @see Semaphore
 */
public class ReentrantSemaphore {
  private final Semaphore semaphore;
  private final Map<Thread, Integer> lockers = new ConcurrentHashMap<>();

  /**
   * Creates a reentrant {@code Semaphore} with the given number of permits.
   * <p>
   * The semaphore will guarantee first-in first-out granting of permits under contention.
   * </p>
   * @param permits the initial number of permits available. This value may be zero or negative,
   * in which case the semaphore is ignored and no thread blocking is performed.
   */
  public ReentrantSemaphore(int permits) {
    semaphore = permits > 0 ? new Semaphore(permits, true) : null;
  }

  /**
   * Acquires a permit from this semaphore, blocking until one is
   * available, or the thread is {@linkplain Thread#interrupt interrupted}.
   *
   * <p>If a thread has already acquired successfully then it does not acquire again and it is not
   * blocked</p>
   *
   * <p>Acquires a permit, if one is available and returns immediately,
   * reducing the number of available permits by one.
   *
   * <p>If no permit is available then the current thread becomes
   * disabled for thread scheduling purposes and lies dormant until
   * one of two things happens:
   * <ul>
   * <li>Some other thread invokes the {@link #release} method for this
   * semaphore and the current thread is next to be assigned a permit; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread.
   * </ul>
   *
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting
   * for a permit,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   * @throws InterruptedException if the current thread is interrupted
   */
  public void acquire() throws InterruptedException {
    if (semaphore != null) {
      Integer nb = lockers.get(Thread.currentThread());
      if (nb == null) {
        semaphore.acquire();
        nb = 0;
      }
      lockers.put(Thread.currentThread(), nb + 1);
    }
  }

  /**
   * Releases a permit, returning it to the semaphore.
   *
   * <p>Releases a permit, increasing the number of available permits by
   * one.  If any threads are trying to acquire a permit, then one is
   * selected and given the permit that was just released.  That thread
   * is (re)enabled for thread scheduling purposes.
   *
   * <p>There is no requirement that a thread that releases a permit must
   * have acquired that permit by calling {@link #acquire}.
   * Correct usage of a semaphore is established by programming convention
   * in the application.
   */
  public void release() {
    if (semaphore != null) {
      final Integer nb = lockers.get(Thread.currentThread());
      if (nb != null) {
        if (nb == 1) {
          semaphore.release();
          lockers.remove(Thread.currentThread());
        } else {
          lockers.put(Thread.currentThread(), nb - 1);
        }
      }
    }
  }
}
