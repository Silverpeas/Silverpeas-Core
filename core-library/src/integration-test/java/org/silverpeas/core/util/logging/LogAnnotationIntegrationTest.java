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
package org.silverpeas.core.util.logging;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Integration test on the use of the Log annotation.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class LogAnnotationIntegrationTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  private AnAnnotatedObject anAnnotatedObject;
  @Inject
  private AnAnnotatedObjectWithAMessage anAnnotatedObjectWithAMessage;
  @Inject
  private AnObjectWithAnnotatedMethods anObjectWithAnnotatedMethods;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(LogAnnotationIntegrationTest.class)
        .addCommonBasicUtilities()
        .addCommonUserBeans()
        .build();
  }

  @Test
  public void invokeMethodOfALogAnnotatedObject() throws InterruptedException {
    anAnnotatedObject.doSomething();
    assertThatLogContainsTheDefaultRecordsFor("AnAnnotatedObject", "doSomething");
  }

  @Test
  public void invokeMethodOfALogAnnotatedObjectWithAMessage() throws InterruptedException {
    anAnnotatedObjectWithAMessage.doSomething();
    assertThatLogContainsTheExpectedRecordWith("I love to do anything for you");
  }

  @Test
  public void invokeALogAnnotatedMethod() throws InterruptedException {
    anObjectWithAnnotatedMethods.doSomething();
    assertThatLogContainsTheDefaultRecordsFor("AnObjectWithAnnotatedMethods", "doSomething");
  }

  @Test
  public void invokeALogAnnotatedMethodWithAMessage() throws InterruptedException {
    anObjectWithAnnotatedMethods.doAnotherThing();
    assertThatLogContainsTheExpectedRecordWith("I love to do anything for you");
  }

  private void assertThatLogContainsTheDefaultRecordsFor(String clazz, String method) {
    String record1 =
        MessageFormat.format(LogAnnotationProcessor.SYSTEM_DEFAULT_BEFORE_PATTERN, clazz, method);
    String record2 =
        MessageFormat.format(LogAnnotationProcessor.USER_DEFAULT_BEFORE_PATTERN, clazz, method,
            100);
    try {
      // the log file can contains more than these two records as the tests can be ran several
      // times.
      assertThat(Files.lines(getLogFile())
          .filter(line -> line.contains(record1) || line.contains(record2))
          .count(), is(greaterThanOrEqualTo(2l)));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  private void assertThatLogContainsTheExpectedRecordWith(String message) {
    String record =
        MessageFormat.format(LogAnnotationProcessor.SYSTEM_PATTERN, message);
    try {
      // the log file can contains more than this record as the tests can be ran several
      // times.
      assertThat(Files.lines(getLogFile()).filter(line -> line.contains(record))
          .count(), is(greaterThanOrEqualTo(1l)));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  private Path getLogFile() {
    return Paths.get(mavenTargetDirectoryRule.getWildflyHomeFile().getPath(), "standalone", "log",
        "server.log");
  }
}
