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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.thread.concurrent;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
class ReentrantSemaphoreTest {

  private long lastLogTime = -1;

  @Test
  void testSimpleNoSemaphore() throws InterruptedException {
    final ReentrantSemaphore rs = new ReentrantSemaphore(0);
    final InstanceCtx ctx = new InstanceCtx(rs);
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
    rs.acquire();
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
    rs.acquire();
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
    rs.release();
    rs.release();
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
  }

  @Test
  void testSimpleAcquireAndRelease() throws InterruptedException {
    final ReentrantSemaphore rs = new ReentrantSemaphore(1);
    final InstanceCtx ctx = new InstanceCtx(rs);
    assertThat(ctx.semaphore.availablePermits(), is(1));
    assertThat(ctx.lockers.size(), is(0));
    rs.acquire();
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(1));
    final Integer nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, is(1));
    rs.release();
    assertThat(ctx.semaphore.availablePermits(), is(1));
    assertThat(ctx.lockers.size(), is(0));
  }

  @Test
  void testAcquireAndReleaseSeveralTimes() throws InterruptedException {
    final ReentrantSemaphore rs = new ReentrantSemaphore(1);
    final InstanceCtx ctx = new InstanceCtx(rs);
    assertThat(ctx.semaphore.availablePermits(), is(1));
    assertThat(ctx.lockers.size(), is(0));
    rs.acquire();
    rs.acquire();
    rs.acquire();
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(1));
    Integer nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, is(3));
    rs.release();
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(1));
    nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, is(2));
    rs.release();
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(1));
    nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, is(1));
    rs.release();
    assertThat(ctx.semaphore.availablePermits(), is(1));
    assertThat(ctx.lockers.size(), is(0));
  }

  @Test
  void testTwoThreadsAcquiringWithOneBlockedByTheOtherOne() throws InterruptedException {
    final ReentrantSemaphore rs = new ReentrantSemaphore(1);
    final InstanceCtx ctx = new InstanceCtx(rs);
    final CountDownLatch latch = new CountDownLatch(1);
    assertThat(ctx.semaphore.availablePermits(), is(1));
    assertThat(ctx.lockers.size(), is(0));
    final Thread thread1 = new Thread(() -> {
      try {
        rs.acquire();
        latch.await();
        rs.release();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    thread1.start();
    await().pollDelay(100, MILLISECONDS).until(() -> true);
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(1));
    Integer nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, nullValue());
    nbThreadAcquiring = ctx.lockers.get(thread1);
    assertThat(nbThreadAcquiring, is(1));
    // BEGIN THREAD 2 WHICH WILL BE BLOCKED
    final Mutable<Boolean> thread2Update = Mutable.of(false);
    final Mutable<Boolean> thread2FinallyPerformed = Mutable.of(false);
    final Thread thread2 = new Thread(() -> {
      try {
        rs.acquire();
        thread2Update.set(true);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        rs.release();
        thread2FinallyPerformed.set(true);
      }
    });
    thread2.start();
    long start = System.currentTimeMillis();
    await().pollDelay(100, MILLISECONDS).until(() -> true);
    assertThatThread1IsBlockingThread2(ctx, thread1, thread2Update, thread2);
    assertThat(thread2FinallyPerformed.get(), is(false));
    thread2.join(400);
    if (!thread2.isInterrupted()) {
      thread2.interrupt();
    }
    long end = System.currentTimeMillis();
    assertThat(end - start, greaterThanOrEqualTo(500L));
    await().pollDelay(100, MILLISECONDS).until(() -> true);
    assertThat(thread2FinallyPerformed.get(), is(true));
    assertThatThread1IsBlockingThread2(ctx, thread1, thread2Update, thread2);
    // END OF THREAD 2 MANAGEMENT
    latch.countDown();
    thread1.join(1000);
    assertThat(ctx.semaphore.availablePermits(), is(1));
    assertThat(ctx.lockers.size(), is(0));
  }

  private void assertThatThread1IsBlockingThread2(final InstanceCtx ctx, final Thread thread1,
      final Mutable<Boolean> thread2Update, final Thread thread2) {
    Integer nbThreadAcquiring;
    assertThat(thread2Update.get(), is(false));
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(1));
    nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, nullValue());
    nbThreadAcquiring = ctx.lockers.get(thread1);
    assertThat(nbThreadAcquiring, is(1));
    nbThreadAcquiring = ctx.lockers.get(thread2);
    assertThat(nbThreadAcquiring, nullValue());
  }

  @Test
  void testLotOfThread() throws InterruptedException {
    final int semaphorePermits = 5;
    final int nbThreads = 1000;
    final ReentrantSemaphore rs = new ReentrantSemaphore(semaphorePermits);
    final InstanceCtx ctx = new InstanceCtx(rs);
    final CountDownLatch latchStep1 = new CountDownLatch(1);
    final CountDownLatch latchStep2 = new CountDownLatch(1);
    final List<Thread> threads = new ArrayList<>(nbThreads);
    final AtomicInteger performed = new AtomicInteger(0);
    IntStream.range(0, nbThreads).forEach(i -> {
      threads.add(new Thread(() -> {
        try {
          latchStep1.await();
          rs.acquire();
          log(String.format("perform n°%s", performed.incrementAndGet()));
          rs.acquire();
          rs.acquire();
          rs.acquire();
          rs.release();
          rs.release();
          latchStep2.await();
          rs.release();
          rs.release();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }));
    });

    log("STARTING THREADS...");
    Collections.shuffle(threads);
    for (Thread thread : threads) {
      thread.start();
    }
    log(threads.size() + " THREADS STARTED");
    log("WAITING 1s...");
    await().pollDelay(1, SECONDS).until(() -> true);
    assertThat(ctx.semaphore.availablePermits(), is(semaphorePermits));
    assertThat(ctx.lockers.size(), is(0));
    latchStep1.countDown();

    log(threads.size() + " THREADS RELEASED");
    log("WAITING 1s...");
    await().pollDelay(1, SECONDS).until(() -> true);

    assertThat(performed.getAcquire(), is(semaphorePermits));
    assertThat(ctx.semaphore.availablePermits(), is(0));
    assertThat(ctx.lockers.size(), is(semaphorePermits));
    Integer nbThreadAcquiring = ctx.lockers.get(Thread.currentThread());
    assertThat(nbThreadAcquiring, nullValue());
    ctx.lockers.forEach((t, i) -> {
      assertThat(i, is(2));
    });
    latchStep2.countDown();

    log("WAITING ENDING OF THREADS...");
    for (Thread thread : threads) {
      thread.join(60000);
    }
    log(threads.size() + " THREADS STOPPED");
    assertThat(ctx.semaphore.availablePermits(), is(semaphorePermits));
    assertThat(ctx.lockers.size(), is(0));
    assertThat(performed.getAcquire(), is(nbThreads));
  }

  @Test
  void testLotOfThreadButNoSemaphore() throws InterruptedException {
    final int nbThreads = 1000;
    final ReentrantSemaphore rs = new ReentrantSemaphore(0);
    final InstanceCtx ctx = new InstanceCtx(rs);
    final CountDownLatch latchStep1 = new CountDownLatch(1);
    final CountDownLatch latchStep2 = new CountDownLatch(1);
    final List<Thread> threads = new ArrayList<>(nbThreads);
    final AtomicInteger performed = new AtomicInteger(0);
    IntStream.range(0, nbThreads).forEach(i -> {
      threads.add(new Thread(() -> {
        try {
          latchStep1.await();
          rs.acquire();
          log(String.format("perform n°%s", performed.incrementAndGet()));
          rs.acquire();
          rs.acquire();
          rs.acquire();
          rs.release();
          rs.release();
          latchStep2.await();
          rs.release();
          rs.release();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }));
    });

    log("STARTING THREADS...");
    Collections.shuffle(threads);
    for (Thread thread : threads) {
      thread.start();
    }
    log(threads.size() + " THREADS STARTED");
    log("WAITING 1s...");
    await().pollDelay(1, SECONDS).until(() -> true);
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
    latchStep1.countDown();

    log(threads.size() + " THREADS RELEASED");
    log("WAITING 1s...");
    await().pollDelay(1, SECONDS).until(() -> true);

    assertThat(performed.getAcquire(), is(nbThreads));
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
    latchStep2.countDown();

    log("WAITING ENDING OF THREADS...");
    for (Thread thread : threads) {
      thread.join(60000);
    }
    log(threads.size() + " THREADS STOPPED");
    assertThat(performed.getAcquire(), is(nbThreads));
    assertThat(ctx.semaphore, nullValue());
    assertThat(ctx.lockers.size(), is(0));
  }

  private static class InstanceCtx {
    private final Semaphore semaphore;
    private final Map<Thread, Integer> lockers;

    @SuppressWarnings("unchecked")
    private InstanceCtx(final ReentrantSemaphore rs) {
      try {
        semaphore = (Semaphore) readDeclaredField(rs, "semaphore", true);
        lockers = (Map<Thread, Integer>) readDeclaredField(rs, "lockers", true);
      } catch (IllegalAccessException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }

  /*
  Tool methods
   */

  private void log(String message) {
    long currentTime = System.currentTimeMillis();
    if (lastLogTime < 0) {
      lastLogTime = currentTime;
    }
    System.out.println(
        StringUtil.leftPad(String.valueOf(currentTime - lastLogTime), 6, " ") + " ms -> " +
            message);
  }
}