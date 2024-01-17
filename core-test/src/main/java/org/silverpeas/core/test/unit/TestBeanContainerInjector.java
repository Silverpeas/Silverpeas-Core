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
package org.silverpeas.core.test.unit;

import org.mockito.internal.util.MockUtil;

import java.lang.annotation.Annotation;

import static org.mockito.Mockito.when;

/**
 * Utility class to ease the programmatic injection of a bean into the bean container used
 * in the unit tests and only in unit tests.
 * @author mmoquillon
 */
public class TestBeanContainerInjector {

  private TestBeanContainerInjector() {
  }

  @SuppressWarnings("unchecked")
  public static <T> T inject(T bean, Annotation... qualifiers) {
    final Class<T> clazz;
    if (MockUtil.isMock(bean) || MockUtil.isSpy(bean)) {
      clazz = (Class<T>) MockUtil.getMockHandler(bean).getMockSettings().getTypeToMock();
    } else {
      clazz = (Class<T>) bean.getClass();
    }
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(clazz, qualifiers)).thenReturn(
        bean);
    if (!clazz.isInterface()) {
      Class<T>[] interfaces = (Class<T>[]) clazz.getInterfaces();
      for (Class<T> anInterface : interfaces) {
        when(TestBeanContainer.getMockedBeanContainer()
            .getBeanByType(anInterface, qualifiers)).thenReturn(bean);
      }
      if (clazz.getSimpleName().endsWith("4Test") &&
          clazz.getGenericSuperclass() instanceof Class) {
        when(TestBeanContainer.getMockedBeanContainer()
            .getBeanByType((Class<T>) clazz.getGenericSuperclass(), qualifiers)).thenReturn(bean);
      }
    }
    return bean;
  }
}
