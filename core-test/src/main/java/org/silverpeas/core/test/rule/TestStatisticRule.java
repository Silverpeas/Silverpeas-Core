/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.test.rule;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
public class TestStatisticRule implements TestRule {

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
        logger.info("(Thread [" + Thread.currentThread().getId() + "]) - Starting test '" +
            description.getMethodName() +
            "'...");
        long start = System.currentTimeMillis();
        try {
          base.evaluate();
        } finally {
          long end = System.currentTimeMillis();
          logger.info("(Thread [" + Thread.currentThread().getId() + "]) - Test '" +
              description.getMethodName() +
              "' ends, with duration of " + DurationFormatUtils.formatDurationHMS(end - start));
        }
      }
    };
  }
}
