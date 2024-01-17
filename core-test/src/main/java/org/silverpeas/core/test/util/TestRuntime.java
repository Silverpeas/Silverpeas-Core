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

package org.silverpeas.core.test.util;

import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class providing features relative to the runtime of a test.
 * @author mmoquillon
 */
public class TestRuntime {

  private TestRuntime() {
  }

  /**
   * Suspends the execution of the test until a given amount of time.
   * @param duration the duration of the execution suspension.
   * @param unit the unit of time of the duration
   */
  public static void awaitUntil(long duration, TimeUnit unit) {
    Awaitility.await().pollDelay(duration, unit).untilTrue(new AtomicBoolean(true));
  }

  /**
   * Suspends the execution of the test until a given amount of time.
   * @param duration the duration of the execution suspension.
   */
  public static void awaitUntil(final Duration duration) {
    Awaitility.await().pollDelay(duration).untilTrue(new AtomicBoolean(true));
  }
}
  