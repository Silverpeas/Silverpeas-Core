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
package org.silverpeas.core;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This Unit test shows a simple method to fetch all implementations of an interface which is
 * dealing with typed types
 * @author Yohann Chastagnier
 */
@UnitTest
@EnableWeld
public class ApplicationServiceTest {

  @WeldSetup
  public WeldInitiator weld =
      WeldInitiator.of(TestApplicationServiceImpl1.class, TestApplicationServiceImpl2.class);

  @Inject
  @Any
  private Instance<ApplicationService<?>> anyApplicationServiceInstanceGetter;

  @Inject
  @Any
  private Provider<ApplicationService<?>> anyApplicationServiceProvider;

  @Inject
  @Any
  private Instance<ApplicationService<TestResource2>> res2ApplicationServiceInstanceGetter;

  @Inject
  @Any
  private Provider<ApplicationService<TestResource2>> res2ApplicationServiceProvider;

  @Test
  public void gettingAllImplementationsOfTypedInterface() {
    final int nbForEachInstances = 10;
    final Set<ApplicationService> instances = new HashSet<>();
    for (int i = 0; i < nbForEachInstances; i++) {
      // Two instance types:
      // - 1 simple class
      // - 1 singleton class
      anyApplicationServiceInstanceGetter.forEach(instances::add);
    }

    Map<Class, Integer> result = mapNbInstancesByTypes(instances);

    assertThat(instances, hasSize(nbForEachInstances + 1));
    assertThat(result.size(), is(2));
    assertThat(result.get(TestApplicationServiceImpl1.class), is(nbForEachInstances));
    assertThat(result.get(TestApplicationServiceImpl2.class), is(1));
  }

  @Test
  public void
  getInstanceOfTypedInterfaceWithoutPrecisingTheTypeWhereasSeveralImplementationAreProvided() {
    assertThrows(AmbiguousResolutionException.class, () -> {
      assertThat(anyApplicationServiceInstanceGetter.isAmbiguous(), is(true));
      anyApplicationServiceInstanceGetter.get();
    });
  }

  @Test
  public void getInstanceOfTypedInterfaceByQualifier() {
    assertThat(anyApplicationServiceInstanceGetter.isAmbiguous(), is(true));
    Instance<ApplicationService<?>> precised = anyApplicationServiceInstanceGetter.select(
        new AnnotationLiteral<ApplicationServiceTestQualifier>() {});
    assertThat(precised.isAmbiguous(), is(false));
  }

  @Test
  public void
  provideInstanceOfTypedInterfaceWithoutPrecisingTheTypeWhereasSeveralImplementationAreProvided() {
    assertThrows(AmbiguousResolutionException.class, () -> anyApplicationServiceProvider.get());
  }

  @Test
  public void getInstanceOfTypedInterfaceByPrecisingTheType() {
    assertThat(res2ApplicationServiceInstanceGetter.get(),
        instanceOf(TestApplicationServiceImpl2.class));
  }

  @Test
  public void provideInstanceOfTypedInterfaceByPrecisingTheType() {
    assertThat(res2ApplicationServiceProvider.get(), instanceOf(TestApplicationServiceImpl2.class));
  }

  @Test
  public void gettingAllImplementationsOfTypedInterfaceWhichItIsPrecised() {
    final int nbForEachInstances = 10;
    final Set<ApplicationService> instances = new HashSet<>();
    for (int i = 0; i < nbForEachInstances; i++) {
      // One instance types:
      // - 1 simple class
      res2ApplicationServiceInstanceGetter.forEach(instances::add);
    }

    Map<Class, Integer> result = mapNbInstancesByTypes(instances);

    assertThat(instances, hasSize(1));
    assertThat(result.size(), is(1));
    assertThat(result.get(TestApplicationServiceImpl2.class), is(1));
  }

  private Map<Class, Integer> mapNbInstancesByTypes(Collection<ApplicationService> instances) {
    Map<Class, Integer> counts = new HashMap<>();
    for (ApplicationService instance : instances) {
      Integer count = counts.get(instance.getClass());
      if (count == null) {
        count = 0;
      }
      count++;
      counts.put(instance.getClass(), count);
    }
    return counts;
  }
}