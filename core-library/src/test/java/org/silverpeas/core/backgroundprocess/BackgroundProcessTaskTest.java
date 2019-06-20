/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.backgroundprocess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.logging.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
@ExtendWith(LoggerExtension.class)
@LoggerLevel(Level.DEBUG)
@Execution(ExecutionMode.SAME_THREAD)
@TestManagedBeans(BackgroundProcessTask.class)
public class BackgroundProcessTaskTest {

  private final static List<AbstractBackgroundProcessRequest> processedRequest =
      synchronizedList(new ArrayList<>());

  @BeforeEach
  public void setup() {
    BackgroundProcessLogger.initLogger();
    BackgroundProcessTask.synchronizedContexts.clear();
    processedRequest.clear();
  }

  @AfterEach
  public void clean() {
    await().pollInterval(1, TimeUnit.SECONDS).until(() -> true);
  }

  @Test
  public void pushOneRandomRequestShouldWork() {
    final SimpleProcessRequest4Test request = new SimpleProcessRequest4Test();
    push(request);

    waitEndOfBackgroundProcessTask();

    assertThat(processedRequest.size(), is(1));
    assertThat(processedRequest.get(0).getUniqueId(), is(request.getUniqueId()));
  }

  @Test
  public void pushLotOfRandomRequestsShouldWork() {
    final List<AbstractBackgroundProcessRequest> requests = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      requests.add(new SimpleProcessRequest4Test());
    }

    requests.forEach(this::push);

    waitEndOfBackgroundProcessTask();

    assertIds(requests);
  }

  @Test
  public void pushUniqueVeryShortRequestsShouldWork() {
    final List<DurationProcessRequest4Test> requests = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      requests.add(new DurationProcessRequest4Test(i, "ID", 0));
    }

    requests.forEach((r) -> {
      push(r);
      await().pollInterval(5, TimeUnit.MILLISECONDS).until(() -> true);
    });

    waitEndOfBackgroundProcessTask();

    assertIds(singletonList(requests.get(0)));
    assertNums(singletonList(requests.get(0)));
  }

  @Test
  public void pushUniqueShortRequestsShouldWork() {
    final List<DurationProcessRequest4Test> requests = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      requests.add(new DurationProcessRequest4Test(i, "ID", 5));
    }

    requests.forEach((r) -> {
      push(r);
      await().pollInterval(10, TimeUnit.MILLISECONDS).until(() -> true);
    });

    waitEndOfBackgroundProcessTask();

    assertIds(singletonList(requests.get(0)));
    assertNums(singletonList(requests.get(0)));
  }

  @Test
  public void pushUniqueLongRequestsShouldWork() {
    final List<DurationProcessRequest4Test> requests = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      requests.add(new DurationProcessRequest4Test(i, "ID", 100));
    }

    requests.forEach((r) -> {
      push(r);
      await().pollInterval(5, TimeUnit.MILLISECONDS).until(() -> true);
    });

    waitEndOfBackgroundProcessTask();

    assertIds(singletonList(requests.get(0)));
    assertNums(singletonList(requests.get(0)));
  }

  @Test
  public void pushUniqueVeryLongRequestsShouldWork() {
    DurationProcessRequest4Test r1 = new DurationProcessRequest4Test(0, "ID", 2000);
    DurationProcessRequest4Test r2 = new DurationProcessRequest4Test(1, "ID", 10);

    push(r1);
    await().pollInterval(200, TimeUnit.MILLISECONDS).until(() -> true);
    push(r2);

    waitEndOfBackgroundProcessTask();

    assertIds(singletonList(r1));
    assertNums(singletonList(r1));
  }

  @Test
  public void pushUniqueVeryLongRequestsAfterNoMoreValidShouldWork() {
    DurationProcessRequest4Test r1 = new DurationProcessRequest4Test(0, "ID", 2000);
    DurationProcessRequest4Test r2 = new DurationProcessRequest4Test(1, "ID", 10);

    push(r1);
    await().timeout(20, TimeUnit.SECONDS).pollInterval(15000, TimeUnit.MILLISECONDS).until(() -> true);
    push(r2);

    waitEndOfBackgroundProcessTask();

    assertIds(asList(r1, r2));
    assertNums(asList(r1, r2));
  }

  @Test
  public void replaceUniqueRequestsShouldWork() {
    DurationProcessRequest4Test r1 = new DurationProcessRequest4Test(0, "DUMMY", 1000);
    DurationProcessRequest4Test r2 = new DurationProcessRequest4Test(1, "ID", 10);
    DurationProcessRequest4Test r3 = new DurationProcessRequest4Test(2, "ID", 10);
    DurationProcessRequest4Test r4 = new DurationProcessRequest4Test(3, "ID", 10);

    push(r1);
    push(r2);
    push(r3);
    await().pollInterval(5, TimeUnit.SECONDS).until(() -> true);
    push(r4);

    waitEndOfBackgroundProcessTask();

    assertIds(asList(r1, r2));
    assertNums(asList(r1, r2));
  }

  @Test
  public void bigMeltingShouldWork() {
    final List<DurationProcessRequest4Test> requestsT1 = new ArrayList<>();
    final List<SimpleProcessRequest4Test> requestsT2 = new ArrayList<>();
    final List<DurationProcessRequest4Test> requestsT3 = new ArrayList<>();
    final List<SimpleProcessRequest4Test> requestsT4 = new ArrayList<>();
    final Thread t1 = new Thread(() -> {
      for (int i = 0; i < 100; i++) {
        requestsT1.add(new DurationProcessRequest4Test(i, "__ID", 100));
      }
      await().pollInterval(2, TimeUnit.SECONDS).until(() -> true);
      requestsT1.forEach((r) -> {
        push(r);
        await().pollInterval(4, TimeUnit.MILLISECONDS).until(() -> true);
      });
    });
    final Thread t2 = new Thread(() -> {
      for (int i = 0; i < 100; i++) {
        requestsT2.add(new SimpleProcessRequest4Test());
      }
      requestsT2.forEach((r) -> {
        push(r);
        await().pollInterval(9, TimeUnit.MILLISECONDS).until(() -> true);
      });
    });
    final Thread t3 = new Thread(() -> {
      for (int i = 0; i < 100; i++) {
        requestsT3.add(new DurationProcessRequest4Test(26000 + i, "__ID", 1));
      }
      requestsT3.forEach((r) -> {
        push(r);
        await().pollInterval(5, TimeUnit.MILLISECONDS).until(() -> true);
      });
    });
    final Thread t4 = new Thread(() -> {
      for (int i = 0; i < 100; i++) {
        requestsT4.add(new SimpleProcessRequest4Test());
      }
      requestsT4.forEach((r) -> {
        push(r);
        await().pollInterval(2, TimeUnit.MILLISECONDS).until(() -> true);
      });
    });

    List<Thread> threads = asList(t1, t2, t3, t4);
    threads.forEach(Thread::start);
    threads.forEach(t -> {
      try {
        t.join(60000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    waitEndOfBackgroundProcessTask();

    List<AbstractBackgroundProcessRequest> allExpected = new ArrayList<>(requestsT2);
    allExpected.addAll(requestsT4);
    allExpected.add(requestsT3.get(0));
    assertIds(allExpected);
    DurationProcessRequest4Test durationRequest =
        (DurationProcessRequest4Test) processedRequest.stream()
            .filter(r -> r instanceof DurationProcessRequest4Test).findFirst().get();
    assertThat(durationRequest.num, is(26000));
  }

  private void assertIds(final List<AbstractBackgroundProcessRequest> expected) {
    final String expectedResult = expected.stream()
        .map(AbstractBackgroundProcessRequest::getUniqueId)
        .sorted()
        .collect(Collectors.joining(","));

    final String resultIds = processedRequest.stream()
        .map(AbstractBackgroundProcessRequest::getUniqueId)
        .sorted()
        .collect(Collectors.joining(","));

    assertThat(resultIds, is(expectedResult));
  }

  private void assertNums(final List<DurationProcessRequest4Test> expected) {
    final String expectedResult = expected.stream()
        .map(r -> "" + r.num)
        .sorted()
        .collect(Collectors.joining(","));

    final String resultIds = processedRequest.stream()
        .map(r -> (DurationProcessRequest4Test) r)
        .map(r -> String.valueOf(r.num))
        .sorted()
        .collect(Collectors.joining(","));

    assertThat(resultIds, is(expectedResult));
  }

  private void waitEndOfBackgroundProcessTask() {
    await()
        .timeout(5, TimeUnit.MINUTES)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .until(() -> !RequestTaskManager.isTaskRunning(BackgroundProcessTask.class));
  }

  private void push(AbstractBackgroundProcessRequest request) {
    BackgroundProcessTask.push(request);
  }

  private static class SimpleProcessRequest4Test extends AbstractBackgroundProcessRequest {

    @Override
    protected void process() {
      await().pollInterval(5, TimeUnit.MILLISECONDS).until(() -> true);
      processedRequest.add(this);
    }
  }

  private static class DurationProcessRequest4Test extends AbstractBackgroundProcessRequest {
    private final int num;
    private final int duration;
    protected DurationProcessRequest4Test(int num, final String uniqueId, int duration) {
      super(uniqueId, BackgroundProcessTask.LOCK_DURATION.TEN_SECONDS);
      this.num = num;
      this.duration = duration;
    }

    @Override
    protected void process() {
      await().pollInterval(duration, TimeUnit.MILLISECONDS).until(() -> true);
      processedRequest.add(this);
    }
  }
}