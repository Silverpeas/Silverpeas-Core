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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.logging.sys;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.logging.SilverLoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test on the SysLoggerFactory implementation of LoggerFactory.
 * @author miguel
 */
@EnableSilverTestEnv
public class SysLoggerFactoryTest {

  private static String LOGGER_NAMESPACE = "silverpeas.test";

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
    final int maxThreads = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    SilverLoggerFactory loggerFactory = new SysLoggerFactory();
    Map<String, SilverLogger> cachedLoggers = getCachedLoggers(loggerFactory);

    // explicit garbage collecting to avoid its running while performing the core of this test.
    System.gc();
    await().atMost(1, TimeUnit.SECONDS).until(cachedLoggers::isEmpty);

    for (int i = 0; i < maxThreads; i++) {
      final int nb = i;
      executor.execute(() -> loggerFactory.getLogger(LOGGER_NAMESPACE + nb));
    }
    executor.shutdown();
    do {
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
    } while (!executor.isTerminated());
    assertThat(cachedLoggers.size(), is(maxThreads));
  }

  @Test
  public void getTheSameLoggerFromDifferentThreads() throws Exception {
    final int maxThreads = Runtime.getRuntime().availableProcessors() + 1;
    ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    SilverLoggerFactory loggerFactory = new SysLoggerFactory();
    Map<String, SilverLogger> cachedLoggers = getCachedLoggers(loggerFactory);

    // explicit garbage collecting to avoid its running while performing the core of this test.
    System.gc();
    await().atMost(10, TimeUnit.SECONDS).until(cachedLoggers::isEmpty);

    for (int i = 0; i < maxThreads; i++) {
      executor.execute(() -> loggerFactory.getLogger(LOGGER_NAMESPACE));
    }
    executor.shutdown();
    do {
      executor.awaitTermination(100, TimeUnit.MILLISECONDS);
    } while (!executor.isTerminated());
    assertThat(cachedLoggers.size(), is(1));
  }

  @Test
  public void getTheSameLoggerBetweenOneGc() throws Exception {
    SilverLoggerFactory loggerFactory = new SysLoggerFactory();
    Map<String, SilverLogger> cachedLoggers = getCachedLoggers(loggerFactory);

    // explicit garbage collecting to avoid its running while performing the core of this test.
    System.gc();
    await().atMost(10, TimeUnit.SECONDS).until(cachedLoggers::isEmpty);

    SilverLogger logger1 = loggerFactory.getLogger(LOGGER_NAMESPACE);
    assertThat(cachedLoggers.size(), is(1));

    System.gc();
    await().atMost(10, TimeUnit.SECONDS).until(cachedLoggers::isEmpty);

    SilverLogger logger2 = loggerFactory.getLogger(LOGGER_NAMESPACE);
    assertThat(cachedLoggers.size(), is(1));
    assertThat(logger1, not(logger2));
  }

  @SuppressWarnings("unchecked")
  private Map<String, SilverLogger> getCachedLoggers(final SilverLoggerFactory loggerFactory)
      throws NoSuchFieldException, IllegalAccessException {
    Field loggers = loggerFactory.getClass().getDeclaredField("loggers");
    loggers.setAccessible(true);
    return (Map<String, SilverLogger>) loggers.get(loggerFactory);
  }

  private static class LoggerKey {
    private String namespace;

    public LoggerKey(final String namespace) {
      this.namespace = namespace;
    }

    public String getNamespace() {
      return this.namespace;
    }
  }
}
