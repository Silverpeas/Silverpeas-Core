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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.sse;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.notification.sse.behavior.SendEveryAmountOfTime;
import org.silverpeas.core.notification.sse.behavior.StoreLastOnly;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.ScheduledJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.SettingBundleStub;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Built for development purpose.
 * @author Yohann Chastagnier
 */
@EnableAutoWeld
@AddBeanClasses(DefaultServerEventNotifier.class)
@AddPackages({AbstractServerEventDispatcherTaskTest.class, RequestTaskManager.class})
class ServerEventDispatcherTaskLoadTest extends AbstractServerEventDispatcherTaskTest {

  private final static int NB_REG_THREAD = 10;
  private final static int NB_REG_BY_THREAD = 100;
  private final static int NB_EVT_THREAD = 10;
  private final static int NB_EVT_BY_THREAD = 300;

  @RegisterExtension
  static SettingBundleStub settings = new SettingBundleStub(
      "org.silverpeas.notificationManager.settings.notificationManagerSettings");

  @TestManagedBean
  private VolatileScheduler4Test volatileScheduler4Test;

  @Inject
  TestServerEventBucket bucket;

  @Inject
  DefaultServerEventNotifier defaultServerEventNotifier;

  @BeforeEach
  @AfterEach
  public void bucketSetup() throws Exception {
    bucket.empty();
    volatileScheduler4Test.shutdown();
  }

  @Test
  void empty() {
    assertThat(true, is(true));
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithIgnoreStoringEvent() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadIgnoreStoring::new)
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithIgnoreStoringEventByUsingTheMostAsPossibleProcessorsOnSend() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadIgnoreStoring::new)
        .andUsingThreadPool()
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithNormalEvent() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadNormal::new)
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithNormalEventByUsingTheMostAsPossibleProcessorsOnSend() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadNormal::new)
        .andUsingThreadPool()
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithStoreLastOnlyEvent() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadStoreLastOnly::new)
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithStoreLastOnlyEventByUsingTheMostAsPossibleProcessorsOnSend() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadStoreLastOnly::new)
        .andUsingThreadPool()
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithSendAveryAmountEvent() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadSendAveryAmount::new)
        .execute();
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  @Disabled("for development purpose")
  void loadSseWithSendAveryAmountEventByUsingTheMostAsPossibleProcessorsOnSend() {
    new TestBuilder(this)
        .supplyingEvent(TestServerEventLoadSendAveryAmount::new)
        .andUsingThreadPool()
        .execute();
  }

  private SilverpeasAsyncContext registerAsyncContext(final String sessionId) {
    final SilverpeasAsyncContext mockedAsyncContext;
    try {
      mockedAsyncContext = newMockedAsyncContext(sessionId);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    return mockedAsyncContext;
  }

  private static class TestServerEventLoadNormal extends AbstractServerEventTest {

    private final static ServerEvent.ServerEventName EVENT_NAME = () -> "EVENT_LOAD_NORMAL";

    @Override
    public ServerEvent.ServerEventName getName() {
      return EVENT_NAME;
    }
  }

  private static class TestServerEventLoadStoreLastOnly extends AbstractServerEventTest
      implements StoreLastOnly {

    private final static ServerEvent.ServerEventName EVENT_NAME = () -> "EVENT_LOAD_STORE_LAST_ONLY";

    @Override
    public ServerEvent.ServerEventName getName() {
      return EVENT_NAME;
    }
  }

  private static class TestServerEventLoadSendAveryAmount extends AbstractServerEventTest
      implements SendEveryAmountOfTime {

    private final static ServerEvent.ServerEventName EVENT_NAME = () -> "EVENT_LOAD_SEND_EVERY_AMOUNT";
    private boolean marked = false;

    @Override
    public ServerEvent.ServerEventName getName() {
      return EVENT_NAME;
    }

    @Override
    public boolean hasWaitingFor() {
      return marked;
    }

    @Override
    public void markAsWaitingFor() {
      marked = true;
    }
  }

  private static class TestServerEventLoadIgnoreStoring extends AbstractServerEventTest
      implements IgnoreStoring {

    private final static ServerEvent.ServerEventName EVENT_NAME = () -> "EVENT_LOAD_IGNORE_STORING";

    @Override
    public ServerEvent.ServerEventName getName() {
      return EVENT_NAME;
    }
  }

  private static class TestBuilder {
    final private ServerEventDispatcherTaskLoadTest test;

    private Supplier<AbstractServerEvent> eventSupplier = () -> null;

    private TestBuilder(final ServerEventDispatcherTaskLoadTest test) {
      this.test = test;
      settings.put("notification.sse.send.thread.pool.max", "0");
      settings.put("notification.sse.EVENT_LOAD_SEND_EVERY_AMOUNT.send.every", "1");
    }

    TestBuilder andUsingThreadPool() {
      settings.put("notification.sse.send.thread.pool.max", "8");
      return this;
    }

    TestBuilder supplyingEvent(final Supplier<AbstractServerEvent> eventSupplier) {
      this.eventSupplier = eventSupplier;
      return this;
    }

    void execute() {
      final AbstractServerEvent event = eventSupplier.get();
      if (event == null) {
        throw new IllegalStateException("Event supplier must provide an event");
      }
      final int nbSseRequests = NB_REG_THREAD * NB_REG_BY_THREAD;
      final SilverLogger logger = SilverLogger.getLogger(this);
      final List<SilverpeasAsyncContext> contexts = Collections.synchronizedList(new ArrayList<>(nbSseRequests));
      final int nbEvents = NB_EVT_THREAD * NB_EVT_BY_THREAD;
      final Mutable<Integer> expectedNbEventIntoStore = Mutable.of(nbEvents);
      if (event instanceof IgnoreStoring) {
        expectedNbEventIntoStore.set(0);
      }
      if (event instanceof StoreLastOnly) {
        expectedNbEventIntoStore.set(1);
      }
      final Mutable<Boolean> greaterThanAboutNbCallsOfWriterByContext = Mutable.of(true);
      final Mutable<Integer> expectedNbCallsOfWriterByContext = Mutable.of(NB_EVT_THREAD * (NB_EVT_BY_THREAD / 2));
      if (event instanceof StoreLastOnly) {
        greaterThanAboutNbCallsOfWriterByContext.set(false);
        expectedNbCallsOfWriterByContext.set(NB_EVT_THREAD * 4);
      }
      final List<Thread> contextThreads = IntStream.range(0, NB_REG_THREAD)
          .mapToObj(i -> new Thread(() -> IntStream.range(0, NB_REG_BY_THREAD).forEach(j -> {
            final String sessionId = "SESSION_" +  (j + (NB_REG_BY_THREAD * i));
            contexts.add(test.registerAsyncContext(sessionId));
          })))
          .collect(Collectors.toList());
      final List<Thread> newEventThreads = IntStream.range(0, NB_EVT_THREAD)
          .mapToObj(i -> new Thread(() -> {
            IntStream.range(0, NB_EVT_BY_THREAD).forEach(j -> {
              with().pollInterval(20, TimeUnit.MILLISECONDS)
                  .await()
                  .untilTrue(new AtomicBoolean(true));
              test.defaultServerEventNotifier.notify(eventSupplier.get());
            });
          }))
          .collect(Collectors.toList());
      final long start = System.currentTimeMillis();
      starts(contextThreads);
      starts(newEventThreads);
      ends(contextThreads);
      ends(newEventThreads);
      final long middle = System.currentTimeMillis();
      with().pollInterval(1, TimeUnit.SECONDS)
          .await()
          .timeout(1, TimeUnit.MINUTES)
          .until(() -> !RequestTaskManager.get().isTaskRunning(ServerEventDispatcherTask.class));
      final long end = System.currentTimeMillis();
      final long testPeriod = end - start;
      logger.info("Threads have run into {0}", formatDurationHMS(middle - start));
      logger.info("Waiting end of treatment during {0}", formatDurationHMS(end - middle));
      logger.info("Total {0}", formatDurationHMS(end - start));
      test.afterSomeTimesCheck(() -> {
        logger.info("Nb registered context: {0}", String.valueOf(contexts.size()));
        logger.info("Nb registered into store: {0}", String.valueOf(test.getStoredServerEvents().size()));
        assertThat(contexts, hasSize(nbSseRequests));
        test.bucket.getServerEventsByListener().forEach((k, v) -> assertThat(v, hasSize(nbEvents)));
        for (SilverpeasAsyncContext asyncContext : contexts) {
          SilverpeasAsyncContext4Test context4Test = (SilverpeasAsyncContext4Test) asyncContext;
          final int nbCalls = context4Test.getResponse().getWriter().getAppendedValues().size();
          if (greaterThanAboutNbCallsOfWriterByContext.get()) {
            assertThat("asyncContext " + asyncContext.getSessionId(), nbCalls,
                greaterThanOrEqualTo(expectedNbCallsOfWriterByContext.get()));
          } else {
            assertThat("asyncContext " + asyncContext.getSessionId(), nbCalls,
                lessThanOrEqualTo(expectedNbCallsOfWriterByContext.get()));
          }
        }
        assertThat(test.getStoredServerEvents(), hasSize(expectedNbEventIntoStore.get()));
        assertThat(testPeriod, lessThanOrEqualTo(40000L));
        if (event instanceof SendEveryAmountOfTime) {
          assertThat(test.contextsByEventType.size(), is(1));
        } else {
          assertThat(test.contextsByEventType.size(), is(0));
        }
      });
    }

    private void starts(final List<Thread> threads) {
      for (Thread thread : threads) {
        thread.start();
      }
    }

    private void ends(final List<Thread> threads) {
      for (Thread thread : threads) {
        try {
          thread.join(60000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private static class VolatileScheduler4Test implements Scheduler {

    private ScheduledExecutorService scheduledExecutor;

    protected VolatileScheduler4Test() {
    }

    @Override
    public ScheduledJob scheduleJob(final String jobName, final JobTrigger trigger,
        final SchedulerEventListener listener) throws SchedulerException {
      return null;
    }

    @Override
    public ScheduledJob scheduleJob(final Job theJob, final JobTrigger trigger,
        final SchedulerEventListener listener) throws SchedulerException {
      return null;
    }

    @Override
    public ScheduledJob scheduleJob(final Job theJob, final JobTrigger trigger)
        throws SchedulerException {
      final int seconds = NotificationManagerSettings.sendEveryAmountOfSecondsFor(
          TestServerEventLoadSendAveryAmount.EVENT_NAME);
      if (seconds > 0) {
        if (scheduledExecutor != null) {
          shutdown();
        }
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> {
          try {
            theJob.execute(null);
            SilverLogger.getLogger(this).info("Send performed every " + seconds + " seconds");
          } catch (SilverpeasException e) {
            throw new SilverpeasRuntimeException(e);
          }
        }, seconds, seconds, TimeUnit.SECONDS);
      }
      return null;
    }

    @Override
    public void unscheduleJob(final String jobName) throws SchedulerException {
      shutdown();
    }

    @Override
    public boolean isJobScheduled(final String jobName) {
      return false;
    }

    @Override
    public Optional<ScheduledJob> getScheduledJob(final String jobName) {
      return Optional.empty();
    }

    @Override
    public void shutdown() throws SchedulerException {
      if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
        scheduledExecutor.shutdownNow();
      }
    }
  }
}