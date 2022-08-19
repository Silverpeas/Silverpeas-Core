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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.management.interceptor;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.LoggerReaderRule;
import org.silverpeas.core.util.logging.Level;

import javax.ejb.EJBException;
import javax.inject.Inject;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class SimulationActionInterceptorIT {

  @Rule
  public LoggerReaderRule loggerReaderRule = new LoggerReaderRule();

  @Inject
  private EjbService ejbService;

  @Inject
  private EjbService ejbServiceSameInstance;

  @Inject
  private SimpleService simpleService;

  private final SimulationActionTestFileCheck checkTest = new SimulationActionTestFileCheck();
  private final SimulationActionTestDummyFileElementConverter converter = new SimulationActionTestDummyFileElementConverter();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SimulationActionInterceptorIT.class)
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addAdministrationFeatures()
        .addAsResource("org/silverpeas/util/logging/")
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.process");
        }).build();
  }

  @Before
  public void setup() {
    checkTest.init();
    converter.init();
  }

  @After
  public void tearDown() {
    checkTest.release();
    converter.release();
  }

  @Test
  public void thatEjbProxyInstances() {
    // That is just to validate that it is so easy to create an EJB today.
    assertThat(ejbService, not(sameInstance(ejbServiceSameInstance)));
  }

  @Test(expected = EJBException.class)
  public void interceptorIsHandledForEjbServicesOnDeleteMethodWithMissingActionAnnotation() {
    ejbService.delete(new InterceptorTestFile("FromEJB"), new ResourceReference("id", "instanceId"));
  }

  @Test
  public void interceptorIsHandledForEjbServicesOnMoveMethodWithMissingSourceAnnotation() {
    ejbService.move(new ResourceReference("id", "instanceId"), new ResourceReference("id", "instanceId"));
    assertCheckNotCalled();
    assertThatLogContainsTheMessage(Level.WARNING, "Intercepted method " +
            "'move', but SourcePK, SourceObject or TargetPK annotations are missing on " +
        "parameter specifications...");
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultEjbService@move called");
  }

  @Test
  public void interceptorIsHandledForSimpleServicesOnDeleteMethodWithMissingTargetAnnotation() {
    simpleService.delete(new InterceptorTestFile("FromService"), new ResourceReference("id", "instanceId"));
    assertCheckNotCalled();
    assertThatLogContainsTheMessage(Level.WARNING, "Intercepted method " +
            "'delete', but SourcePK, SourceObject or TargetPK annotations are missing on " +
            "parameter specifications...");
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultSimpleService@delete called");
  }

  @Test
  public void interceptorIsHandledForEjbServicesOnCreateMethod() {
    ejbService.create(new InterceptorTestFile("FromEJB"), new ResourceReference("id", "instanceId"));
    assertCheckCalled();
    assertThatLogContainsTheMessage(Level.INFO, "InterceptorTest@DefaultEjbService@create called");
  }

  @Test
  public void interceptorIsHandledForSimpleServicesOnCreateMethod() {
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
      assertThat(IOUtils.readLines(loggerReaderRule.getReader()).stream()
          .filter(line -> line.contains(message))
          .count(), is(greaterThanOrEqualTo(1L)));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
