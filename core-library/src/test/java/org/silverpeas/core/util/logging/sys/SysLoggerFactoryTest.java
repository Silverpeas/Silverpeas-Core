/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.logging.sys;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.logging.SilverLoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit test on the SysLoggerFactory implementation of LoggerFactory.
 * @author miguel
 */
public class SysLoggerFactoryTest {

  private static String LOGGER_NAMESPACE = "silverpeas.test";

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);

  @Before
  public void initEnvVariables() {
    SystemWrapper.get()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectory.getResourceTestDirFile().getPath());
  }

  @Test
  public void getALogger() {
    SilverLoggerFactory loggerFactory = new SysLoggerFactory();
    SilverLogger logger = loggerFactory.getLogger(LOGGER_NAMESPACE);
    assertThat(logger, is(notNullValue()));
    assertThat(logger instanceof SysLogger, is(true));
    assertThat(logger.getNamespace(), is(LOGGER_NAMESPACE));
  }

  @Test
  public void getDifferentLoggersFromDifferentThreads() throws Exception {
    final int maxThreads = 100;
    ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    final Set<SilverLogger> loggers = new HashSet<>(maxThreads);
    SilverLoggerFactory loggerFactory = new SysLoggerFactory();

    for (int i = 0; i < maxThreads; i++) {
      final int nb = i;
      executor.execute(() -> {
        loggers.add(loggerFactory.getLogger(LOGGER_NAMESPACE + nb));
      });
    }
    executor.shutdown();
    do {
      executor.awaitTermination(5, TimeUnit.MILLISECONDS);
    } while (!executor.isTerminated());
    assertThat(loggers.size(), is(maxThreads));
  }

  @Test
  public void getTheSameLoggerFromDifferentThreads() throws Exception {
    final int maxThreads = 100;
    ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    final Set<SilverLogger> loggers = new HashSet<>(maxThreads);
    SilverLoggerFactory loggerFactory = new SysLoggerFactory();

    for (int i = 0; i < maxThreads; i++) {
      executor.execute(() -> loggers.add(loggerFactory.getLogger(LOGGER_NAMESPACE)));
    }
    executor.shutdown();
    do {
      executor.awaitTermination(5, TimeUnit.MILLISECONDS);
    } while (!executor.isTerminated());
    assertThat(loggers.size(), is(1));
  }
}
