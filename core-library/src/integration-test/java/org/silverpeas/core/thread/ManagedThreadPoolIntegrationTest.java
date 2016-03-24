/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.thread;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.time.TimeData;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static java.lang.String.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig.maxThreadPoolSizeOf;
import static org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig.timeoutOf;

@RunWith(Arquillian.class)
public class ManagedThreadPoolIntegrationTest {

  private final static long OFFSET_TIME = 350;
  private final static long SLEEP_TIME_OF_1_SECOND = 1000;
  private final static long MAX_DURATION_TIME_OF_PARALLEL_EXEC = 2000;
  private final static long SHORT_TIMEOUT = SLEEP_TIME_OF_1_SECOND / 2;
  private final static long LARGE_TIMEOUT = SLEEP_TIME_OF_1_SECOND * 5;

  private ThreadEndTag threadEndTag;

  @Rule
  public TestName testName = new TestName();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(ManagedThreadPoolIntegrationTest.class)
        .addCommonBasicUtilities().addSilverpeasExceptionBases().build();
  }

  @Before
  public void setup() {
    threadEndTag = new ThreadEndTag();
  }

  @Test
  public void invokeRunnables() throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    TimeData duration = executeInvokeRunnableTest(
        () -> ManagedThreadPool.invoke(runnables.toArray(new Runnable[5])));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(100L));
    Thread.sleep(100);
    log("Verifying that no processes is ended...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
    long waitingFor = (SLEEP_TIME_OF_1_SECOND + OFFSET_TIME);
    log("Waiting for {0}ms before verifying again end of runnable processes...",
        valueOf(waitingFor));
    Thread.sleep(waitingFor);
    log("Verifying that all processes are ended...");
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(runnables.size()));
    log("... OK");
  }

  @Test
  public void invokeRunnablesAndAwaitTermination() throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    TimeData duration =
        executeInvokeRunnableTest(() -> ManagedThreadPool.invokeAndAwaitTermination(runnables));
    log("Verifying that 5 processes of 1s are ended after {0}ms but before {1}ms...",
        valueOf(SLEEP_TIME_OF_1_SECOND), valueOf(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(runnables.size()));
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SLEEP_TIME_OF_1_SECOND));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    log("... OK");
  }

  @Test
  public void invokeRunnablesAndAwaitTerminationWithShortTimeout() throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    TimeData duration = executeInvokeRunnableTest(
        () -> ManagedThreadPool.invokeAndAwaitTermination(runnables, timeoutOf(SHORT_TIMEOUT)));
    long expectedLargestDuration = (SHORT_TIMEOUT + OFFSET_TIME);
    log("Verifying that the timeout has been performed after {0}ms but before {1}ms...",
        valueOf(SHORT_TIMEOUT), valueOf(expectedLargestDuration));
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SHORT_TIMEOUT));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(expectedLargestDuration));
    log("... OK");
    Thread.sleep(SLEEP_TIME_OF_1_SECOND);
    log("Verifying that all processes have not been killed despite the effective timeout...");
    assertThat(threadEndTag.getThreadIdCalls(), not(empty()));
    log("... OK");
  }

  @Test
  public void invokeRunnablesAndAwaitTerminationWithShortTimeoutAndKillingRunningThreads()
      throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    TimeData duration = executeInvokeRunnableTest(() -> ManagedThreadPool
        .invokeAndAwaitTermination(runnables, timeoutOf(SHORT_TIMEOUT).killThreadsAfterTimeout()));
    long expectedLargestDuration = (SHORT_TIMEOUT + OFFSET_TIME);
    log("Verifying that the timeout has been performed after {0}ms but before {1}ms...",
        valueOf(SHORT_TIMEOUT), valueOf(expectedLargestDuration));
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SHORT_TIMEOUT));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(expectedLargestDuration));
    log("... OK");
    Thread.sleep(SLEEP_TIME_OF_1_SECOND);
    log("Verifying that all processes have been killed after the effective timeout...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
  }

  @Test
  public void
  invokeRunnablesAndAwaitTerminationWithShortTimeoutAndKillingRunningThreadsBut2ThreadsOkOn7()
      throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    runnables.addAll(initializeRunnables(100, 200));
    TimeData duration = executeInvokeRunnableTest(() -> ManagedThreadPool
        .invokeAndAwaitTermination(runnables, timeoutOf(SHORT_TIMEOUT).killThreadsAfterTimeout()));
    long expectedLargestDuration = (SHORT_TIMEOUT + OFFSET_TIME);
    log("Verifying that the timeout has been performed after {0}ms but before {1}ms...",
        valueOf(SHORT_TIMEOUT), valueOf(expectedLargestDuration));
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SHORT_TIMEOUT));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(expectedLargestDuration));
    log("... OK");
    log("Verifying that 2 processes have been done before the effective timeout...");
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(2));
    log("... OK");
    Thread.sleep(SLEEP_TIME_OF_1_SECOND);
    log("Verifying that all other processes have been killed after the effective timeout...");
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(2));
    log("... OK");
  }

  @Test
  public void invokeRunnablesAndAwaitTerminationWithLargeTimeout() throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    TimeData duration = executeInvokeRunnableTest(
        () -> ManagedThreadPool.invokeAndAwaitTermination(runnables, timeoutOf(LARGE_TIMEOUT)));
    log("Verifying that the timeout has not been performed after {0}ms but before {1}ms...",
        valueOf(SLEEP_TIME_OF_1_SECOND), valueOf(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(runnables.size()));
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SLEEP_TIME_OF_1_SECOND));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    log("... OK");
  }

  @Test
  public void invokeRunnablesAndAwaitTerminationWithLargeTimeoutAndKillingRunningThreads()
      throws Exception {
    final List<TestRunnable> runnables = fiveRunnablesOf1SecondOfTreatment();
    TimeData duration = executeInvokeRunnableTest(() -> ManagedThreadPool
        .invokeAndAwaitTermination(runnables, timeoutOf(LARGE_TIMEOUT).killThreadsAfterTimeout()));
    log("Verifying that the timeout has not been performed after {0}ms but before {1}ms...",
        valueOf(SLEEP_TIME_OF_1_SECOND), valueOf(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(runnables.size()));
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SLEEP_TIME_OF_1_SECOND));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    log("... OK");
  }

  @Test
  public void invokeCallable() throws Exception {
    final TestCallable callable = initializeCallables(SLEEP_TIME_OF_1_SECOND).get(0);
    Pair<TimeData, List<Future<Long>>> result = executeInvokeCallableTest(
        () -> Collections.singletonList(ManagedThreadPool.invoke(callable)));
    log("Verifying that caller of the invoke method get back the hand immediately...");
    TimeData duration = result.getLeft();
    List<Future<Long>> futures = result.getRight();
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(100L));
    assertThat(futures, hasSize(1));
    Thread.sleep(100);
    log("Verifying that no processes is ended...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
    long getResultTime = System.currentTimeMillis();
    log("Getting future result at {0} with result {1}...", valueOf(getResultTime),
        valueOf(futures.get(0).get()));
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(1));
    log("... OK");
  }

  @Test
  public void invokeCallables() throws Exception {
    final List<TestCallable> callables = fiveCallablesOf1SecondOfTreatment();
    Pair<TimeData, List<Future<Long>>> result =
        executeInvokeCallableTest(() -> ManagedThreadPool.invoke(callables));
    log("Verifying that caller of the invoke method get back the hand immediately...");
    TimeData duration = result.getLeft();
    List<Future<Long>> futures = result.getRight();
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(100L));
    assertThat(futures, hasSize(5));
    Thread.sleep(100);
    log("Verifying that no processes is ended...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
    long getResultTime = System.currentTimeMillis();
    log("Getting future result at {0}...", valueOf(getResultTime));
    for (Future<Long> future : futures) {
      log("\tprocess ended at {0}", valueOf(future.get()));
      assertThat(future.get() - getResultTime, lessThan(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    }
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(callables.size()));
    log("... OK");
  }

  @Test
  public void invokeCallablesAndSpecifyPoolSize() throws Exception {
    final List<TestCallable> callables = fiveCallablesOf1SecondOfTreatment();
    final List<TestCallable> callablesWithPoolSize = fiveCallablesOf1SecondOfTreatment();

    log("[without pool size] Processing the invocation...");
    long beforeTime = System.currentTimeMillis();
    Pair<TimeData, List<Future<Long>>> result =
        executeInvokeCallableTest(() -> ManagedThreadPool.invoke(callables));
    List<Future<Long>> futures = result.getRight();
    for (Future<Long> future : futures) {
      log("[without pool size]\tprocess ended at {0}", valueOf(future.get()));
    }
    long afterTime = System.currentTimeMillis();
    TimeData duration = UnitUtil.getTimeData(afterTime - beforeTime);
    log("[without pool size] Invocation duration of {0}ms", valueOf(duration.getTimeAsLong()));

    log("[with pool size of 1] Processing the invocation...");
    long beforeTimeWithPoolSize = System.currentTimeMillis();
    Pair<TimeData, List<Future<Long>>> resultWithPoolSize = executeInvokeCallableTest(
        () -> ManagedThreadPool.invoke(callablesWithPoolSize, maxThreadPoolSizeOf(1)));
    List<Future<Long>> futuresWithPoolSize = resultWithPoolSize.getRight();
    for (Future<Long> future : futuresWithPoolSize) {
      log("[with pool size of 1]\tprocess ended at {0}", valueOf(future.get()));
    }
    long afterTimeWithPoolSize = System.currentTimeMillis();
    TimeData durationWithPoolSize =
        UnitUtil.getTimeData(afterTimeWithPoolSize - beforeTimeWithPoolSize);
    log("[with pool size of 1] Invocation duration of {0}ms",
        valueOf(durationWithPoolSize.getTimeAsLong()));

    assertThat(threadEndTag.getThreadIdCalls(),
        hasSize(callables.size() + callablesWithPoolSize.size()));
    assertThat(durationWithPoolSize.getTimeAsLong(),
        greaterThan((duration.getTimeAsLong() * (callablesWithPoolSize.size() - 1))));
    log("... OK");
  }

  @Test
  public void invokeCallablesWithShortTimeout() throws Exception {
    final List<TestCallable> callables = fiveCallablesOf1SecondOfTreatment();
    Pair<TimeData, List<Future<Long>>> result = executeInvokeCallableTest(
        () -> ManagedThreadPool.invoke(callables, timeoutOf(SHORT_TIMEOUT)));
    long expectedLargestDuration = (SHORT_TIMEOUT + OFFSET_TIME);
    log("Verifying that the timeout has not been performed after {0}ms but before {1}ms...",
        valueOf(SHORT_TIMEOUT), valueOf(expectedLargestDuration));
    TimeData duration = result.getLeft();
    List<Future<Long>> futures = result.getRight();
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SHORT_TIMEOUT));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(expectedLargestDuration));
    assertThat(futures, hasSize(5));
    log("Verifying that no processes is ended...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
    long getResultTime = System.currentTimeMillis();
    log("Getting future result at {0}...", valueOf(getResultTime));
    for (Future<Long> future : futures) {
      log("\tprocess ended at {0}", valueOf(future.get()));
      assertThat(future.get() - getResultTime, lessThan(MAX_DURATION_TIME_OF_PARALLEL_EXEC));
    }
    assertThat(threadEndTag.getThreadIdCalls(), hasSize(callables.size()));
    log("... OK");
  }

  @Test
  public void invokeCallablesWithShortTimeoutDemonstrationByNotCallingOfGetMethodOfFuture()
      throws Exception {
    final List<TestCallable> callables = fiveCallablesOf1SecondOfTreatment();
    Pair<TimeData, List<Future<Long>>> result = executeInvokeCallableTest(
        () -> ManagedThreadPool.invoke(callables, timeoutOf(SHORT_TIMEOUT)));
    long expectedLargestDuration = (SHORT_TIMEOUT + OFFSET_TIME);
    log("Verifying that the timeout has not been performed after {0}ms but before {1}ms...",
        valueOf(SHORT_TIMEOUT), valueOf(expectedLargestDuration));
    TimeData duration = result.getLeft();
    List<Future<Long>> futures = result.getRight();
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SHORT_TIMEOUT));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(expectedLargestDuration));
    assertThat(futures, hasSize(5));
    log("Verifying that no processes is ended...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
    Thread.sleep(SLEEP_TIME_OF_1_SECOND);
    log("Verifying that all processes have not been killed after the effective timeout...");
    assertThat(threadEndTag.getThreadIdCalls(), not(empty()));
    log("... OK");
  }

  @Test
  public void invokeCallablesWithShortTimeoutAndKillingRunningThreads() throws Exception {
    final List<TestCallable> callables = fiveCallablesOf1SecondOfTreatment();
    Pair<TimeData, List<Future<Long>>> result = executeInvokeCallableTest(() -> ManagedThreadPool
        .invoke(callables, timeoutOf(SHORT_TIMEOUT).killThreadsAfterTimeout()));
    long expectedLargestDuration = (SHORT_TIMEOUT + OFFSET_TIME);
    log("Verifying that the timeout has not been performed after {0}ms but before {1}ms...",
        valueOf(SHORT_TIMEOUT), valueOf(expectedLargestDuration));
    TimeData duration = result.getLeft();
    List<Future<Long>> futures = result.getRight();
    assertThat(duration.getTimeAsLong(), greaterThanOrEqualTo(SHORT_TIMEOUT));
    assertThat(duration.getTimeAsLong(), lessThanOrEqualTo(expectedLargestDuration));
    assertThat(futures, hasSize(5));
    log("Verifying that no processes is ended...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
    Thread.sleep(SLEEP_TIME_OF_1_SECOND);
    log("Verifying that all processes have been killed after the effective timeout...");
    assertThat(threadEndTag.getThreadIdCalls(), empty());
    log("... OK");
  }

  /**
   * Common initialization.
   * @return list of 5 TestRunnable of 1 second of treatments.
   */
  private List<TestRunnable> fiveRunnablesOf1SecondOfTreatment() {
    return initializeRunnables(SLEEP_TIME_OF_1_SECOND, SLEEP_TIME_OF_1_SECOND + 1,
        SLEEP_TIME_OF_1_SECOND + 2, SLEEP_TIME_OF_1_SECOND + 3, SLEEP_TIME_OF_1_SECOND + 4);
  }

  /**
   * Common initialization.
   * @return list of 5 TestCallable of 1 second of treatments.
   */
  private List<TestCallable> fiveCallablesOf1SecondOfTreatment() {
    return initializeCallables(SLEEP_TIME_OF_1_SECOND, SLEEP_TIME_OF_1_SECOND + 1,
        SLEEP_TIME_OF_1_SECOND + 2, SLEEP_TIME_OF_1_SECOND + 3, SLEEP_TIME_OF_1_SECOND + 4);
  }

  /**
   * Method to execute the invoke and get the duration of it invoke execution.
   * @param test the invoke to perform.
   * @return the duration.
   */
  private TimeData executeInvokeRunnableTest(InvokeRunnableTest test) throws Exception {
    long beforeTime = System.currentTimeMillis();
    test.execute();
    long afterTime = System.currentTimeMillis();
    TimeData duration = UnitUtil.getTimeData(afterTime - beforeTime);
    log("Invocation duration of {0}ms", valueOf(duration.getTimeAsLong()));
    return duration;
  }

  /**
   * Method to execute the invoke and get the duration of it invoke execution.
   * @param test the invoke to perform.
   * @return the duration.
   */
  private Pair<TimeData, List<Future<Long>>> executeInvokeCallableTest(InvokeCallableTest test)
      throws Exception {
    long beforeTime = System.currentTimeMillis();
    List<Future<Long>> futures = test.execute();
    long afterTime = System.currentTimeMillis();
    TimeData duration = UnitUtil.getTimeData(afterTime - beforeTime);
    log("Invocation duration of {0}ms", valueOf(duration.getTimeAsLong()));
    return Pair.of(duration, futures);
  }

  /**
   * Logging easily.
   */
  private synchronized void log(String message, Object... arguments) {
    Logger.getLogger(ManagedThreadPoolTest.class.getSimpleName())
        .info(MessageFormat.format(testName.getMethodName() + " - " + message, arguments));
  }

  /**
   * Gets a list of runnable from a list of sleep times.
   * @param sleepTimes the sleep time list.
   * @return the list of runnable.
   */
  private List<TestRunnable> initializeRunnables(long... sleepTimes) {
    List<TestRunnable> runnables = new ArrayList<>();
    for (Long sleepTime : sleepTimes) {
      runnables.add(new TestRunnable(threadEndTag, sleepTime));
    }
    return runnables;
  }

  /**
   * Gets a list of runnable from a list of sleep times.
   * @param sleepTimes the sleep time list.
   * @return the list of runnable.
   */
  private List<TestCallable> initializeCallables(long... sleepTimes) {
    List<TestCallable> runnables = new ArrayList<>();
    for (Long sleepTime : sleepTimes) {
      runnables.add(new TestCallable(threadEndTag, sleepTime));
    }
    return runnables;
  }

  /**
   * Runnable for test.
   */
  private static class TestRunnable implements Runnable {

    private final ThreadEndTag threadEndTag;
    private final long sleepTime;

    private TestRunnable(final ThreadEndTag threadEndTag, final long sleepTime) {
      this.threadEndTag = threadEndTag;
      this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(sleepTime);
        threadEndTag.mark();
      } catch (InterruptedException ignore) {
      }
    }
  }

  /**
   * Callable for test.
   * The return value is the current time in milliseconds.
   */
  private static class TestCallable implements Callable<Long> {

    private final ThreadEndTag threadEndTag;
    private final long sleepTime;

    private TestCallable(final ThreadEndTag threadEndTag, final long sleepTime) {
      this.threadEndTag = threadEndTag;
      this.sleepTime = sleepTime;
    }

    @Override
    public Long call() throws Exception {
      try {
        Thread.sleep(sleepTime);
        threadEndTag.mark();
      } catch (InterruptedException ignore) {
      }
      return System.currentTimeMillis();
    }
  }

  /**
   * A class to permits for callables or runnables to mark their end of treatment.
   */
  private static class ThreadEndTag {
    List<String> threadIdCalls = new ArrayList<>();

    public synchronized void mark() {
      threadIdCalls.add("ID_" + Thread.currentThread().getId());
    }

    @SuppressWarnings("unchecked")
    public synchronized List<String> getThreadIdCalls() {
      return new ArrayList<>(threadIdCalls);
    }
  }

  @FunctionalInterface
  private static interface InvokeRunnableTest {
    void execute() throws Exception;
  }

  @FunctionalInterface
  private static interface InvokeCallableTest {
    List<Future<Long>> execute() throws Exception;
  }
}