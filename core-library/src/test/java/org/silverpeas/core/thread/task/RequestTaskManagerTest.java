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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.thread.task;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.thread.task.RequestTaskManager.RequestTaskMonitor;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
class RequestTaskManagerTest {

  @TestedBean
  private RequestTaskManager taskManager;

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
    taskManager.shutdownAllTasks();
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
  void usingNormallyShouldWork() {
    assertThat(counter, is(0));
    final int nbRequests = 100;
    for (int i = 0; i < nbRequests; i++) {
      TestRequestTask.newRandomSleepRequest();
      if (Math.random() > 0.7) {
        await(50);
      }
    }
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAtEndOfTest(TestRequestTask.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(nbRequests));
  }

  @Test
  void usingLimitedQueueShouldWork() {
    assertThat(counter, is(0));
    final int nbRequests = 100;
    for (int i = 0; i < nbRequests; i++) {
      TestRequestTaskWithLimit.newRandomSleepRequest();
      if (Math.random() > 0.7) {
        await(50);
      }
    }
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(nbRequests));
  }

  @Test
  void firstRequestKillsTaskButRestartedOnNewRequestPush() {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAfterFirstInit(TestRequestTaskWithLimit.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    await(500);

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

  @Test
  void notFirstRequestKillsTaskButRestartedOnNewRequestPush() {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAfterFirstInit(TestRequestTaskWithLimit.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.ThreadKillTestRequest());
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));
    await(500);

    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.requestList.size(), is(8));
    assertThat(getCounter(), is(0));

    // Starting the thread by adding a new request
    TestRequestTaskWithLimit.newRandomSleepRequest();

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(8));
  }

  @Test
  void newRequestPushWakeUpTheTaskBeforeAcquiringAccessToAddTheRequestIntoQueue() throws Exception {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAfterFirstInit(TestRequestTaskWithLimit.class);

    // Setting manually the queue
    monitor.requestList.add(new TestRequestTaskWithLimit.SleepTestRequest(0));

    // Acquiring all access
    monitor.acquireAccess();
    monitor.acquireAccess();
    await(500);

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
  void requestPushedDuringAfterNoMoreRequestMustBeHandledBySameTask() {

    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithAfterNoMoreRequestLongTreatment.newRandomSleepRequest();
    await(800);

    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor =
        (RequestTaskMonitor<?
            extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
            TestRequestTask.TestProcessContext>) taskManager
            .getTasks()
            .get(TestRequestTaskWithAfterNoMoreRequestLongTreatment.class);
    Future<?> taskInstanceOnAfterNoMoreRequest = monitor.task;
    assertThat(monitor.isTaskRunning(), is(true));
    assertThat(monitor.taskWatcher.isDone(), is(false));
    assertThat(monitor.taskWatcher.isCancelled(), is(false));
    assertThat(monitor.requestList.size(), is(0));
    assertThat(getCounter(), is(1));
    assertThat(getAfterNoMoreRequestCounter(), is(1));

    // Adding a new request
    TestRequestTaskWithAfterNoMoreRequestLongTreatment.newSleepRequest(20);
    Future<?> taskInstanceAfterEndOfTreatment = monitor.task;

    waitForTaskEndingAtEndOfTest(TestRequestTaskWithAfterNoMoreRequestLongTreatment.class);
    assertThat(taskInstanceOnAfterNoMoreRequest, is(taskInstanceAfterEndOfTreatment));
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(2));
  }

  @Test
  void onlyRequestKillingTaskMustAlsoBeConsumed() {
    // Firstly initializing the monitor by pushing a request
    TestRequestTask.newEmptyRequest();
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAfterFirstInit(TestRequestTask.class);

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
  void onlyRequestKillingTaskMusAlsoBeConsumedWithLimitedQueue() {
    // Firstly initializing the monitor by pushing a request
    TestRequestTaskWithLimit.newEmptyRequest();
    await(600);
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor =
        (RequestTaskMonitor<?
            extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
            TestRequestTask.TestProcessContext>) taskManager
            .getTasks()
            .get(TestRequestTaskWithLimit.class);
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
  void bigMelting() {
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
        await(50);
      }
    }
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor = waitForTaskEndingAtEndOfTest(TestRequestTask.class);
    waitForTaskEndingAtEndOfTest(TestRequestTaskWithLimit.class);
    assertThat(monitor, notNullValue());
    assertThatThreadsAreStoppedAndMonitorsAreCleanedAndQueuesAreConsummed(monitor);
    assertThat(counter, is(nbSleepRequests));
  }

  @SuppressWarnings("unchecked")
  private RequestTaskMonitor<?
      extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
      TestRequestTask.TestProcessContext> waitForTaskEndingAfterFirstInit(
      final Class<?> testClass) {
    await(200);
    final RequestTaskMonitor<?
        extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor =
        (RequestTaskMonitor<?
            extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
            TestRequestTask.TestProcessContext>) taskManager
            .getTasks()
            .get(testClass);
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
      final RequestTaskMonitor<?
          extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
          TestRequestTask.TestProcessContext> monitor) {
    assertThat(monitor.isTaskRunning(), is(false));
    assertThat(monitor.task, nullValue());
    assertThat(monitor.taskWatcher, nullValue());
    assertThat(monitor.requestList.size(), is(0));
  }

  @SuppressWarnings("unchecked")
  private RequestTaskMonitor<?
      extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
      TestRequestTask.TestProcessContext> waitForTaskEndingAtEndOfTest(
      final Class<?> testClass) {
    await(200);
    RequestTaskMonitor<? extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
        TestRequestTask.TestProcessContext>
        monitor =
        (RequestTaskMonitor<?
            extends AbstractRequestTask.Request<TestRequestTask.TestProcessContext>,
            TestRequestTask.TestProcessContext>) taskManager
            .getTasks()
            .get(testClass);
    int nbTry = 0;
    while (nbTry < 20) {
      await(10);
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

  private void await(long timeInMilliSecondes) {
    Awaitility.await().pollInterval(timeInMilliSecondes, TimeUnit.MILLISECONDS).until(() -> true);
  }
}