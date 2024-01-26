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
package org.silverpeas.core.util.logging;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.LoggerReaderRule;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.kernel.logging.Level;
import org.silverpeas.kernel.util.SystemWrapper;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.text.MessageFormat.format;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Integration test on the use of the Log annotation.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class LogAnnotationIT {

  @Rule
  public LoggerReaderRule loggerReaderRule = new LoggerReaderRule();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  private AnAnnotatedObject anAnnotatedObject;
  @Inject
  private AnAnnotatedObjectWithAMessage anAnnotatedObjectWithAMessage;
  @Inject
  private AnObjectWithAnnotatedMethods anObjectWithAnnotatedMethods;

  TestContext context = new TestContext();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(LogAnnotationIT.class)
        .addAdministrationFeatures()
        .addAsResource("org/silverpeas/util/logging/")
        .build();
  }

  @Before
  public void SetUpTestContext() {
    context.setLoggerLevel(Level.DEBUG);
    SystemWrapper.getInstance()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectoryRule.getBuildDirFile().getAbsolutePath());
  }

  @Test
  public void invokeMethodOfALogAnnotatedObject() {
    anAnnotatedObject.doSomething();
    assertThatLogContainsTheDefaultRecordsFor("AnAnnotatedObject", "doSomething()");
  }

  @Test
  public void invokeMethodWithParametersOfALogAnnotatedObject() {
    Date today = new Date();
    anAnnotatedObject.doSomething("foo", 42.0, today);
    String msg = "doSomething(foo, 42.0, " + today + ")";
    assertThatLogContainsTheDefaultRecordsFor("AnAnnotatedObject", msg);
  }

  @Test
  public void invokeMethodOfALogAnnotatedObjectWithAMessage() {
    anAnnotatedObjectWithAMessage.doSomething();
    assertThatLogContainsTheExpectedRecordWith("I love to do anything for you", 1);
  }

  @Test
  public void invokeALogAnnotatedMethod() {
    anObjectWithAnnotatedMethods.doSomething();
    assertThatLogContainsTheDefaultRecordsFor("AnObjectWithAnnotatedMethods", "doSomething()");
  }

  @Test
  public void invokeALogAnnotatedMethodWithParameters() {
    Date today = new Date();
    anObjectWithAnnotatedMethods.doSomething("foo", 42.0, today);
    String msg = "doSomething(foo, 42.0, " + today + ")";
    assertThatLogContainsTheDefaultRecordsFor("AnObjectWithAnnotatedMethods", msg);
  }

  @Test
  public void invokeALogAnnotatedMethodWithAMessage() {
    anObjectWithAnnotatedMethods.doAnotherThing();
    assertThatLogContainsTheExpectedRecordWith("I love to do anything for you", 1);
  }

  @Test
  public void invokeALogAnnotatedMethodWithAMessageExpectingParameters() {
    Date today = new Date();
    anObjectWithAnnotatedMethods.doAnotherThing("foo", 42.0, today);
    String msg = "I'd like to do foo 42.0 times for you at " + today;
    assertThatLogContainsTheExpectedRecordWith(msg, 2);
  }

  private void assertThatLogContainsTheDefaultRecordsFor(String clazz, String method) {
    String record1 =
        MessageFormat.format(LogAnnotationProcessor.SYSTEM_DEFAULT_BEFORE_PATTERN, clazz, method);
    String record2 =
        MessageFormat.format(LogAnnotationProcessor.USER_DEFAULT_BEFORE_PATTERN, "Toto Rabbit",
            100, clazz, method);
    try {
      // the log file can contains more than these two records as the tests can be ran several
      // times.
      await().pollDelay(1, TimeUnit.SECONDS)
          .atMost(2, TimeUnit.SECONDS)
          .untilTrue(new AtomicBoolean(true));
      final List<String> lines = IOUtils.readLines(loggerReaderRule.getReader());
      assertThat(format("Searching {0} or {1} into \n{2}", record1, record2, lines),
          lines.stream().filter(line -> line.contains(record1) || line.contains(record2)).count(),
          is(greaterThanOrEqualTo(2L)));
    } catch (UncheckedIOException e) {
      fail(e.getMessage());
    }
  }

  private void assertThatLogContainsTheExpectedRecordWith(String message, long recordsCount) {
    String record = format(LogAnnotationProcessor.SYSTEM_BEFORE_PATTERN, message);
    try {
      // the log file can contains more than this record as the tests can be ran several
      // times.
      await().pollDelay(1, TimeUnit.SECONDS)
          .atMost(2, TimeUnit.SECONDS)
          .untilTrue(new AtomicBoolean(true));
      final List<String> lines = IOUtils.readLines(loggerReaderRule.getReader());
      assertThat(format("Searching {0} into \n{1}", record, lines),
          lines.stream().filter(line -> line.contains(record)).count(),
          is(greaterThanOrEqualTo(recordsCount)));
    } catch (UncheckedIOException e) {
      fail(e.getMessage());
    }
  }
}
