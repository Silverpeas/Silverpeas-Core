/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.test.unit;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the injection of an {@link Instance} of managed beans by the
 * {@link SimpleJEEDependencyResolver}, both through a field and through a parameter of the
 * injection constructor.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SimpleJEEDependencyResolverTest {

  @TestManagedMock
  private Dependency dependency;

  @TestedBean
  private BeanWithInstances bean;

  @Test
  void anInstanceInjectedThroughAFieldProvidesTheManagedBean() {
    assertThat(bean.fieldInstance.get(), is(dependency));
  }

  @Test
  void anInstanceInjectedThroughTheConstructorProvidesTheManagedBean() {
    assertThat(bean.constructorInstance.get(), is(dependency));
  }

  @Test
  void anInstanceInjectedThroughTheConstructorIsSatisfied() {
    assertThat(bean.constructorInstance.isUnsatisfied(), is(false));
  }

  /**
   * A dependency of the bean.
   */
  public interface Dependency {
  }

  /**
   * A bean getting an {@link Instance} of its dependency through a field and through its
   * constructor.
   */
  public static class BeanWithInstances {

    @Inject
    private Instance<Dependency> fieldInstance;

    private final Instance<Dependency> constructorInstance;

    @Inject
    BeanWithInstances(final Instance<Dependency> constructorInstance) {
      this.constructorInstance = constructorInstance;
    }
  }
}
