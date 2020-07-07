/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.test.rule;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * @author Yohann Chastagnier
 */
public class TestStatisticRule implements TestRule {

  private static final Object MUTEX = new Object();
  private final Map<Long, Description> currentDescriptions = new ConcurrentHashMap<>();
  private Logger logger;

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        synchronized (MUTEX) {
          if (logger == null) {
            logger = Logger.getLogger(this.getClass().getSimpleName());
          }
        }
        final long threadId = Thread.currentThread().getId();
        currentDescriptions.putIfAbsent(threadId, description);
        log("starting test...");
        long start = System.currentTimeMillis();
        try {
          base.evaluate();
        } finally {
          log("...ending test", start, System.currentTimeMillis());
          currentDescriptions.remove(threadId);
        }
      }
    };
  }

  public void log(final String message) {
    final long threadId = Thread.currentThread().getId();
    final Description currentDescription = getCurrentDescription(threadId);
    if (currentDescription != null) {
      final String methodName = currentDescription.getMethodName();
      if (methodName != null) {
        logger.log(Level.INFO, "(Thread [{0}]) - Test method ''{1}'' - {2}",
            new String[]{String.valueOf(threadId), methodName, message});
      }
    }
  }

  public void log(final String prefixMessage, final long start, final long end) {
    log(format("{0} (duration of {1})",
        prefixMessage, DurationFormatUtils.formatDurationHMS(end - start)));
  }

  private Description getCurrentDescription(final long threadId) {
    return currentDescriptions.get(threadId);
  }
}
