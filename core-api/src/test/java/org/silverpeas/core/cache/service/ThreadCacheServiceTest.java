/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.cache.service;

import org.junit.Test;

import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public class ThreadCacheServiceTest {
  private static final Logger logger = Logger.getAnonymousLogger();
  private static final ThreadCacheService service = new ThreadCacheService();
  private static final String Object1 = "";
  private static final Object Object2 = new Object();

  private static void log(String message) {
    logger.info("Thread id = " + Thread.currentThread().getId() + " - " + message);
  }

  @Test
  public void testClear() throws InterruptedException {
    final TestPerformer testPerformer = new TestPerformer() {
      @Override
      public void perform() {
        log("testClear - start");
        assertThat(service.getCache().size(), is(0));
        service.add(Object1);
        service.add(Object2);
        assertThat(service.getCache().size(), is(2));
        service.clear();
        assertThat(service.getCache().size(), is(0));
        log("testClear - end");
      }
    };
    assertTest(testPerformer);
  }

  @Test
  public void testGet() throws InterruptedException {
    final TestPerformer testPerformer = new TestPerformer() {
      @Override
      public void perform() {
        log("testGet - start");
        assertThat(service.getCache().size(), is(0));
        String uniqueKey1 = service.add(Object1);
        assertThat(service.get("dummy"), nullValue());
        assertThat(service.get(uniqueKey1), is((Object) Object1));
        assertThat(service.get(uniqueKey1, Object.class), is((Object) Object1));
        assertThat(service.get(uniqueKey1, String.class), is(Object1));
        assertThat(service.get(uniqueKey1, Number.class), nullValue());
        log("testGet - end");
      }
    };
    assertTest(testPerformer);
  }

  @Test
  public void testAdd() throws InterruptedException {
    final TestPerformer testPerformer = new TestPerformer() {
      @Override
      public void perform() {
        log("testAdd - start");
        assertThat(service.getCache().size(), is(0));
        String uniqueKey1 = service.add(Object1);
        String uniqueKey2 = service.add(Object2);
        assertThat(uniqueKey1, notNullValue());
        assertThat(uniqueKey2, notNullValue());
        assertThat(uniqueKey2, not(is(uniqueKey1)));
        assertThat(service.getCache().size(), is(2));
        log("testAdd - end");
      }
    };
    assertTest(testPerformer);
  }

  @Test
  public void testPut() throws InterruptedException {
    final TestPerformer testPerformer = new TestPerformer() {
      @Override
      public void perform() {
        log("testPut - start");
        assertThat(service.getCache().size(), is(0));
        service.put("A", Object1);
        service.put("B", Object2);
        assertThat(service.getCache().size(), is(2));
        log("testPut - end");
      }
    };
    assertTest(testPerformer);
  }

  @Test
  public void testPutWithSameKey() throws InterruptedException {
    final TestPerformer testPerformer = new TestPerformer() {
      @Override
      public void perform() {
        log("testPutWithSameKey - start");
        assertThat(service.getCache().size(), is(0));
        service.put("A", Object1);
        service.put("A", Object2);
        assertThat(service.getCache().size(), is(1));
        log("testPutWithSameKey - end");
      }
    };
    assertTest(testPerformer);
  }

  @Test
  public void testRemove() throws InterruptedException {
    final TestPerformer testPerformer = new TestPerformer() {
      @Override
      public void perform() {
        log("testRemove - start");
        assertThat(service.getCache().size(), is(0));
        String uniqueKey1 = service.add(Object1);
        String uniqueKey2 = service.add(Object2);
        assertThat(service.getCache().size(), is(2));
        service.remove("lkjlkj");
        assertThat(service.getCache().size(), is(2));
        service.remove(uniqueKey1, Number.class);
        assertThat(service.getCache().size(), is(2));
        service.remove(uniqueKey1, Object.class);
        assertThat(service.getCache().size(), is(1));
        service.remove(uniqueKey2);
        assertThat(service.getCache().size(), is(0));
        log("testRemove - end");
      }
    };
    assertTest(testPerformer);
  }

  private void assertTest(TestPerformer test) throws InterruptedException {
    log("BEGIN SAME TREATMENT WITH TWO THREADS DIFFERED");
    try {
      RunnableTest runnableTest1 = new RunnableTest(test);
      RunnableTest runnableTest2 = new RunnableTest(test);
      new Thread(runnableTest1).start();
      Thread.sleep(100);
      new Thread(runnableTest2).start();
      Thread.sleep(200);
      assertThat(runnableTest1.isTestPassed(), is(true));
      assertThat(runnableTest2.isTestPassed(), is(true));
    } finally {
      log("END SAME TREATMENT WITH TWO THREADS DIFFERED");
    }
    log("BEGIN SAME TREATMENT WITH TWO THREADS NOT DIFFERED");
    try {
      RunnableTest runnableTest1 = new RunnableTest(test);
      RunnableTest runnableTest2 = new RunnableTest(test);
      new Thread(runnableTest1).start();
      new Thread(runnableTest2).start();
      Thread.sleep(200);
      assertThat(runnableTest1.isTestPassed(), is(true));
      assertThat(runnableTest2.isTestPassed(), is(true));
    } finally {
      log("END SAME TREATMENT WITH TWO THREADS NOT DIFFERED");
    }
  }

  private interface TestPerformer {
    void perform();
  }

  private class RunnableTest implements Runnable {

    private final TestPerformer test;
    private boolean testPassed = false;


    protected RunnableTest(final TestPerformer test) {
      this.test = test;
    }

    @Override
    public void run() {
      test.perform();
      testPassed = true;
    }

    private boolean isTestPassed() {
      return testPassed;
    }
  }
}
