/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.silverpeas.core.test.rule.LoggerReaderRule;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.toto.ABasicClass;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.silverpeas.core.util.StringUtil.like;

/**
 * Integration test on the use of the Logger annotation.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class LoggerAnnotationIT {

  @Rule
  public LoggerReaderRule loggerReaderRule = new LoggerReaderRule();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Inject
  private ClassWithLoggerAnnotation object1;

  @Inject
  private ABasicClass object2;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(LoggerAnnotationIT.class)
        .addCommonBasicUtilities()
        .addCommonUserBeans()
        .addPackages(true, "org.silverpeas.core.util.toto")
        .addAsResource("org/silverpeas/util/logging/")
        .build();
  }

  @Before
  public void SetUpTestContext() {
    commonAPI4Test.setLoggerLevel(Level.DEBUG);
    SystemWrapper.get()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectoryRule.getBuildDirFile().getAbsolutePath());

  }

  @Test
  public void invokeMethodOfALoggerAnnotatedObject() {
    final String message = "Hello world!";
    object1.logMeAMessage(message);
    assertThatLogContainsTheMessage(message);
  }

  @Test
  public void invokeMethodOfAnObjectInALoggerAnnotatedPackage() {
    final String message = "Prout prout @ home";
    object2.logMeAMessage(message);
    assertThatLogContainsTheMessage(message);
  }

  private void assertThatLogContainsTheMessage(final String message) {
    final String record = "% INFO % " + message;
    try {
      // the log file can contains more than this record as the tests can be ran several
      // times.
      final List<String> lines = IOUtils.readLines(loggerReaderRule.getReader());
      assertThat(format("Searching {0} into \n{1}", record, lines),
          lines.stream().filter(line -> like(line, record)).count(),
          is(greaterThanOrEqualTo(1L)));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
