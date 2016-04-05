/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.util;

import org.junit.Rule;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.ExecutionAttempts.Job;
import static org.silverpeas.core.util.ExecutionAttempts.retry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests the retry mechanism.
 */
public class ExecutionAttemptsTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  /**
   * Test of retry method, of class ExecutionAttempts.
   */
  @Test
  public void retryNothingWhenAllWorksFine() throws Exception {
    retry(0, new Job() {

      @Override
      public void execute() throws Exception {
        assertThat(invoked(), is(true));
      }
    });
    assertThat(exectutedCorrectly(), is(true));
  }

  @Test(expected = RuntimeException.class)
  public void retryNothingWithNoAttemptsWhenAFailureIsOccuring() throws Exception {
    try {
      retry(0, new Job() {

        @Override
        public void execute() throws Exception {
          if (++attempts == 1) {
            throw new RuntimeException();
          }
        }
      });
    } finally {
      assertThat(attempts, is(1));
    }
  }

  @Test(expected = RuntimeException.class)
  public void retrySeveralTimesWhenAFailureIsOccuring() throws Exception {
    try {
      retry(3, new Job() {

        @Override
        public void execute() throws Exception {
          if (++attempts <= 3) {
            throw new RuntimeException();
          }
        }
      });
    } finally {
      assertThat(attempts, is(3));
    }
  }

  @Test
  public void retryOnlyOnceWhenAFailureOccuredWithinTheFirstInvocation() throws Exception {
    try {
      retry(3, new Job() {

        @Override
        public void execute() throws Exception {
          if (++attempts == 1) {
            throw new RuntimeException();
          }
          assertThat(invoked(), is(true));
        }
      });
    } finally {
      assertThat(attempts, is(2));
    }
  }

  protected static boolean exectutedCorrectly() {
    return true;
  }

  protected static boolean invoked() {
    return true;
  }
  private int attempts = 0;
}
