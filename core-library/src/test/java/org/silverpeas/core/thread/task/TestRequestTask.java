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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.thread.task;

import org.silverpeas.core.util.logging.SilverLogger;

/**
 * @author silveryocha
 */
public class TestRequestTask extends AbstractRequestTask<TestRequestTask.TestProcessContext> {

  protected static SilverLogger getLogger() {
    return SilverLogger.getLogger(TestRequestTask.class);
  }

  static void newEmptyRequest() {
    RequestTaskManager.push(TestRequestTask.class, new SleepTestRequest(0));
  }

  static void newRandomSleepRequest() {
    RequestTaskManager.push(TestRequestTask.class, new RandomSleepTestRequest());
  }

  static void newThreadKillRequest() {
    RequestTaskManager.push(TestRequestTask.class, new ThreadKillTestRequest());
  }

  static class TestProcessContext implements AbstractRequestTask.ProcessContext {
    TestProcessContext() {
      getLogger().debug("initializing the process context ({0})", getClass().getSimpleName());
    }
  }

  static class SleepTestRequest implements AbstractRequestTask.Request<TestProcessContext> {
    private final int sleep;

    SleepTestRequest(final int sleep) {
      this.sleep = sleep;
    }

    public void process(TestProcessContext context) throws InterruptedException {
      if (sleep > 0) {
        getLogger().debug("sleeping for {0}ms ({1})", sleep, getClass().getSimpleName());
        Thread.sleep(sleep);
      }
      RequestTaskManagerTest.incrementCounter();
    }
  }

  static class RandomSleepTestRequest extends SleepTestRequest {
    RandomSleepTestRequest() {
      super((int) (Math.random() * 10));
    }
  }

  static class ThreadKillTestRequest implements AbstractRequestTask.Request<TestProcessContext> {

    @Override
    public void process(TestProcessContext context) throws InterruptedException {
      getLogger().debug("killing thread into 10ms");
      Thread.sleep(10);
      throw new Error();
    }
  }
}
