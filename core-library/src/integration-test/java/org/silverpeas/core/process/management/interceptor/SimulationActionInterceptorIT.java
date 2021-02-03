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
package org.silverpeas.core.process.management.interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.test.util.SilverProperties;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.Level;

import javax.ejb.EJBException;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class SimulationActionInterceptorIT {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  private EjbService ejbService;

  @Inject
  private EjbService ejbServiceSameInstance;

  @Inject
  private SimpleService simpleService;

  private static Path logFile;

  static {
    final SilverProperties props =
        MavenTargetDirectoryRule.loadPropertiesForTestClass(SimulationActionInterceptorIT.class);
    final File wildflyHome = MavenTargetDirectoryRule.getWildflyHomeFile(props);
    logFile = Paths.get(wildflyHome.getPath(), "standalone", "log", "server.log");
  }

  private SimulationActionTestFileCheck checkTest = new SimulationActionTestFileCheck();
  private SimulationActionTestDummyFileElementConverter converter =
      new SimulationActionTestDummyFileElementConverter();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SimulationActionInterceptorIT.class)
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addCommonUserBeans().addAsResource("org/silverpeas/util/logging/")
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.process");
        }).build();
  }

  @Before
  public void setup() {
    SystemWrapper.get()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectoryRule.getBuildDirFile().getAbsolutePath());
    checkTest.init();
    converter.init();
  }

  @After
  public void tearDown() {
    checkTest.release();
    converter.release();
  }

  @AfterClass
  public static void clearLogs() throws IOException {
    Files.deleteIfExists(getLogFile());
  }


  @Test
  public void testThatEjbProxyInstances() {
    // That is just to validate that it is so easy to create an EJB today.
    assertThat(ejbService, not(sameInstance(ejbServiceSameInstance)));
  }

  @Test(expected = EJBException.class)
  public void testInterceptorIsHandledForEjbServicesOnDeleteMethodWithMissingActionAnnotation() {
    ejbService.delete(new InterceptorTestFile("FromEJB"), new ResourceReference("id", "instanceId"));
  }

  @Test
  public void testInterceptorIsHandledForEjbServicesOnMoveMethodWithMissingSourceAnnotation() {
    ejbService.move(new ResourceReference("id", "instanceId"), new ResourceReference("id", "instanceId"));
    assertCheckNotCalled();
    assertThatLogContainsTheMessage(Level.WARNING, "Intercepted method " +
            "'move', but SourcePK, SourceObject or TargetPK annotations are missing on " +
        "parameter specifications...");
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultEjbService@move called");
  }

  @Test
  public void testInterceptorIsHandledForSimpleServicesOnDeleteMethodWithMissingTargetAnnotation() {
    simpleService.delete(new InterceptorTestFile("FromService"), new ResourceReference("id", "instanceId"));
    assertCheckNotCalled();
    assertThatLogContainsTheMessage(Level.WARNING, "Intercepted method " +
            "'delete', but SourcePK, SourceObject or TargetPK annotations are missing on " +
            "parameter specifications...");
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultSimpleService@delete called");
  }

  @Test
  public void testInterceptorIsHandledForEjbServicesOnCreateMethod() {
    ejbService.create(new InterceptorTestFile("FromEJB"), new ResourceReference("id", "instanceId"));
    assertCheckCalled();
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultEjbService@create called");
  }

  @Test
  public void testInterceptorIsHandledForSimpleServicesOnCreateMethod() {
    simpleService.create(new InterceptorTestFile("FromSimple"), new ResourceReference("id", "instanceId"));
    assertCheckCalled();
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultSimpleService@create called");
  }

  private void assertCheckNotCalled() {
    assertThat("Interceptor must not be performed...", checkTest.getCallCount(), is(0));
  }

  private void assertCheckCalled() {
    assertThat("Interceptor has not been performed...", checkTest.getCallCount(), is(1));
  }

  private void assertThatLogContainsTheMessage(final Level level, final String message) {
    try {
      // the log file can contains more than this record as the tests can be ran several
      // times.
      assertThat(Files.lines(getLogFile())
          .filter(line -> line.contains(level.name()) && line.contains(message))
          .count(), is(greaterThanOrEqualTo(1l)));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  private static Path getLogFile() {
    return logFile;
  }
}
