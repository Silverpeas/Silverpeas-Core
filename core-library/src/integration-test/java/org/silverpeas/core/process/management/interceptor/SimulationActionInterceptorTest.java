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

package org.silverpeas.core.process.management.interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.util.log.TestSilverpeasTrace;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.ForeignPK;

import javax.ejb.EJBException;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class SimulationActionInterceptorTest {

  @Inject
  private EjbService ejbService;

  @Inject
  private EjbService ejbServiceSameInstance;

  @Inject
  private SimpleService simpleService;

  private SimulationActionTestFileCheck checkTest = new SimulationActionTestFileCheck();
  private SimulationActionTestDummyFileElementConverter converter =
      new SimulationActionTestDummyFileElementConverter();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SimulationActionInterceptorTest.class)
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addCommonUserBeans()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.process");
        }).build();
  }

  @Before
  public void setup() {
    TestSilverpeasTrace.clean();
    checkTest.init();
    converter.init();
  }

  @After
  public void tearDown() {
    checkTest.release();
    converter.release();
  }


  @Test
  public void testThatEjbProxyInstances() {
    // That is just to validate that it is so easy to create an EJB today.
    assertThat(ejbService, not(sameInstance(ejbServiceSameInstance)));
  }

  @Test(expected = EJBException.class)
  public void testInterceptorIsHandledForEjbServicesOnDeleteMethodWithMissingActionAnnotation() {
    ejbService.delete(new InterceptorTestFile("FromEJB"), new ForeignPK("id", "instanceId"));
  }

  @Test
  public void testInterceptorIsHandledForEjbServicesOnMoveMethodWithMissingSourceAnnotation() {
    ejbService.move(new ForeignPK("id", "instanceId"), new ForeignPK("id", "instanceId"));
    assertCheckNotCalled();
    assertThat(TestSilverpeasTrace.getWarnMessages(), contains(
        "Process@SimulationActionProcessAnnotationInterceptor.intercept()@intercepted method " +
            "'move', but SourcePK, SourceObject or TargetPK annotations are missing on " +
            "parameter specifications..."));
    assertThat(TestSilverpeasTrace.getInfoMessages(),
        contains("InterceptorTest@DefaultEjbService@move called"));
  }

  @Test
  public void testInterceptorIsHandledForSimpleServicesOnDeleteMethodWithMissingTargetAnnotation() {
    simpleService.delete(new InterceptorTestFile("FromService"), new ForeignPK("id", "instanceId"));
    assertCheckNotCalled();
    assertThat(TestSilverpeasTrace.getWarnMessages(), contains(
        "Process@SimulationActionProcessAnnotationInterceptor.intercept()@intercepted method " +
            "'delete', but SourcePK, SourceObject or TargetPK annotations are missing on " +
            "parameter " +
            "specifications..."));
    assertThat(TestSilverpeasTrace.getInfoMessages(),
        contains("InterceptorTest@DefaultSimpleService@delete called"));
  }

  @Test
  public void testInterceptorIsHandledForEjbServicesOnCreateMethod() {
    ejbService.create(new InterceptorTestFile("FromEJB"), new ForeignPK("id", "instanceId"));
    assertCheckCalled();
    assertThat(TestSilverpeasTrace.getInfoMessages(),
        hasItems("InterceptorTest@DefaultEjbService@create called"));
  }

  @Test
  public void testInterceptorIsHandledForSimpleServicesOnCreateMethod() {
    simpleService.create(new InterceptorTestFile("FromSimple"), new ForeignPK("id", "instanceId"));
    assertCheckCalled();
    assertThat(TestSilverpeasTrace.getInfoMessages(),
        hasItem("InterceptorTest@DefaultSimpleService@create called"));
  }

  private void assertCheckNotCalled() {
    assertThat("Interceptor must not be performed...", checkTest.getCallCount(), is(0));
  }

  private void assertCheckCalled() {
    assertThat(TestSilverpeasTrace.getFatalMessages(), empty());
    assertThat(TestSilverpeasTrace.getErrorMessages(), empty());
    assertThat("Interceptor has not been performed...", checkTest.getCallCount(), is(1));
  }
}
