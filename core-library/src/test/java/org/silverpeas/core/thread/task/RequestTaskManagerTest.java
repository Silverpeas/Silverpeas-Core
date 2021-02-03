/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.thread.task;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.thread.task.RequestTaskManager.RequestTaskMonitor;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
@ExtendWith(LoggerExtension.class)
@LoggerLevel(Level.DEBUG)
@TestManagedBeans({TestRequestTask.class, TestRequestTaskWithLimit.class,
    TestRequestTaskWithAfterNoMoreRequestLongTreatment.class})
public class RequestTaskManagerTest {

  private static int counter = 0;
  private static int afterNoMoreRequestCounter = 0;

  private static synchronized int getCounter() {
    return counter;
  }

  private static synchronized int getAfterNoMoreRequestCounter() {
    return afterNoMoreRequestCounter;
  }

  static synchronized void incrementCounter() {
    counter++;
  }

  static synchronized void incrementAfterNoMoreRequestCounter() {
    afterNoMoreRequestCounter++;
  }

  @BeforeEach
  @AfterEach
  public synchronized void clean() {
    RequestTaskManager.tasks.clear();
  }

  @BeforeEach
  public synchronized void resetCounter() {
    counter = 0;
  }

  @BeforeEach
  public synchronized void resetAfterNoMoreRequestCounter() {
    afterNoMoreRequestCounter = 0;
  }

  @Test
  public void usingNormallyShouldWork()
      throws ExecutionException, InterruptedException, TimeoutException {
    assertThat(counter, is(0));
    final int nbRequests = 100;
    for (int i = 0; i < nbRequests; i++) {
      TestRequestTask.newRandomSleepRequest();
      if (Math.random() > 0.7) {
        Thread.sleep(50);
      }
    }
    RequestTaskMonitor monitor = waitForTaskEndingAtEndOfTest(TestRequestTask.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(nbRequests));
  }

  @Test
  public void usingLimitedQueueShouldWork()
      throws ExecutionException, InterruptedException, TimeoutException {
    assertThat(counter, is(0));
    final int nbRequests = 100;
    for (int i = 0; i < nbRequests; i++) {
      TestRequestTaskWithLimit.newRandomSleepRequest();
      if (Math.random() > 0.7) {
        Thread.sleep(50);
      }
    }
    RequestTaskMonitor monitor = waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(nbRequests));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void firstRequestKillsTaskButRestartedOnNewRequestPush()
      throws ExecutionException, InterruptedException, TimeoutException {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    RequestTaskMonitor monitor = waitForTaskEndingAfterFirstInit(TestRequestTaskWithLimit.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    Thread.sleep(500);

    assertThat(counter, is(0));
    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.task, nullValue());
    assertThat(monitor.taskWatcher, nullValue());
    assertThat(monitor.requestList.size(), is(4));
    assertThat(getCounter(), is(0));

    // Starting the thread by adding a new request
    TestRequestTaskWithLimit.newRandomSleepRequest();

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(4));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void notFirstRequestKillsTaskButRestartedOnNewRequestPush()
      throws ExecutionException, InterruptedException, TimeoutException {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    RequestTaskMonitor monitor = waitForTaskEndingAfterFirstInit(TestRequestTaskWithLimit.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    Thread.sleep(500);

    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.requestList.size(), is(8));
    assertThat(getCounter(), is(0));

    // Starting the thread by adding a new request
    TestRequestTaskWithLimit.newRandomSleepRequest();

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(8));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void newRequestPushWakeUpTheTaskBeforeAcquiringAccessToAddTheRequestIntoQueue()
      throws Exception {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    RequestTaskMonitor monitor = waitForTaskEndingAfterFirstInit(TestRequestTaskWithLimit.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));

    // Acquiring all access
    monitor.acquireAccess();
    monitor.acquireAccess();
    Thread.sleep(500);

    Semaphore semaphore = (Semaphore) FieldUtils.readDeclaredField(monitor, "queueSemaphore", true);
    assertThat(semaphore.availablePermits(), is(0));
    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.task, nullValue());
    assertThat(monitor.taskWatcher, nullValue());
    assertThat(monitor.requestList.size(), is(1));
    assertThat(getCounter(), is(0));

    // Starting the thread by adding a new request whereas the semaphore has no more available
    // permits.
    TestRequestTaskWithLimit.newRandomSleepRequest();

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void requestPushedDuringAfterNoMoreRequestMustBeHandledBySameTask() throws Exception {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithAfterNoMoreRequestLongTreatment.newRandomSleepRequest();
    Thread.sleep(800);

    RequestTaskMonitor monitor =
        RequestTaskManager.tasks.get(TestRequestTaskWithAfterNoMoreRequestLongTreatment.class);
    Future taskInstanceOnAfterNoMoreRequest = monitor.task;
    assertThat(monitor.isTaskRunning(), is(true));
    assertThat(monitor.taskWatcher.isDone(), is(false));
    assertThat(monitor.taskWatcher.isCancelled(), is(false));
    assertThat(monitor.requestList.size(), is(0));
    assertThat(getCounter(), is(1));
    assertThat(getAfterNoMoreRequestCounter(), is(1));

    // Adding a new request
    TestRequestTaskWithAfterNoMoreRequestLongTreatment.newSleepRequest(20);
    Future taskInstanceAfterEndOfTreatment = monitor.task;

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithAfterNoMoreRequestLongTreatment.class);
    assertThat(taskInstanceOnAfterNoMoreRequest, is(taskInstanceAfterEndOfTreatment));
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void onlyRequestKillingTaskMustAlsoBeConsumed()
      throws ExecutionException, InterruptedException, TimeoutException {
    // Firstly initializing the monitor by pushing a request
    TestRequestTask.newEmptyRequest();
    RequestTaskMonitor monitor = waitForTaskEndingAfterFirstInit(TestRequestTask.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTask.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTask.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTask.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTask.ThreadKillTestRequest());

    // Starting the thread by adding a new request
    TestRequestTask.newThreadKillRequest();

    waitForTaskEndingAtEndOfTest(TestRequestTask.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void onlyRequestKillingTaskMusAlsoBeConsumedWithLimitedQueue()
      throws ExecutionException, InterruptedException, TimeoutException {
    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    Thread.sleep(600);
    RequestTaskMonitor monitor = RequestTaskManager.tasks.get(TestRequestTaskWithLimit.class);
    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.requestList.size(), is(0));
    assertThat(counter, is(1));

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());

    // Starting the thread by adding a new request
    TestRequestTaskWithLimit.newThreadKillRequest();

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
  }

  @Test
  public void bigMelting() throws ExecutionException, InterruptedException, TimeoutException {
    assertThat(counter, is(0));
    final int nbRequests = 100;
    int nbSleepRequests = 0;
    for (int i = 0; i < nbRequests; i++) {
      if (Math.random() < 0.8) {
        TestRequestTask.newRandomSleepRequest();
        TestRequestTaskWithLimit.newRandomSleepRequest();
        nbSleepRequests += 2;
      } else {
        TestRequestTaskWithLimit.newThreadKillRequest();
        TestRequestTask.newThreadKillRequest();
      }
      if (Math.random() > 0.7) {
        Thread.sleep(50);
      }
    }
    RequestTaskMonitor monitor = waitForTaskEndingAtEndOfTest(TestRequestTask.class);
    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThat(monitor, notNullValue());
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(nbSleepRequests));
  }

  @SuppressWarnings("unchecked")
  private RequestTaskMonitor waitForTaskEndingAfterFirstInit(final Class testClass)
      throws InterruptedException, ExecutionException {
    Thread.sleep(200);
    final RequestTaskMonitor monitor = RequestTaskManager.tasks.get(testClass);
    // Waiting the end of the current task
    final Future<Void> taskWatcher = monitor.taskWatcher;
    final Future<Void> currentTask = monitor.task;
    if (currentTask != null) {
      try {
        currentTask.get();
        taskWatcher.get();
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    // Checking status
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(1));

    // Resetting counter
    resetCounter();
    return monitor;
  }

  private void assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(
      final RequestTaskMonitor monitor) {
    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.task, nullValue());
    assertThat(monitor.taskWatcher, nullValue());
    assertThat(monitor.requestList.size(), is(0));
  }

  private RequestTaskMonitor waitForTaskEndingAtEndOfTest(final Class testClass)
      throws InterruptedException {
    Thread.sleep(200);
    RequestTaskMonitor monitor = RequestTaskManager.tasks.get(testClass);
    int nbTry = 0;
    while (nbTry < 20) {
      Thread.sleep(10);
      if (monitor.isTaskRunning()) {
        nbTry = 0;
      }
      // Waiting the end of the current task
      try {
        monitor.task.get();
      } catch (Exception ignore) {
      }
      try {
        monitor.taskWatcher.get();
      } catch (Exception ignore) {
      }
      nbTry++;
    }
    // Checking status
    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.requestList.size(), is(0));
    return monitor;
  }
}