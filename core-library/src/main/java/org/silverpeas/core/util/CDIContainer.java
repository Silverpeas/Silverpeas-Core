/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the bean container above the CDI framework. This implementation can be found
 * either by the Service Loader API of Java or by CDI itself. The later must be avoided and
 * should be used only in the Silverpeas Core API implementation for only some peculiar reasons.
 * @author mmoquillon
 */
public class CDIContainer implements BeanContainer {

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getBeanByName(final String name) throws IllegalStateException {
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<T> bean = beanManager.resolve((Set) beanManager.getBeans(name));
    if (bean == null) {
      throw new IllegalStateException("Cannot find an instance of name " + name);
    }
    CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
    Type type = bean.getTypes()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "The bean " + name + " doesn't satisfy any managed type"));

    return (T) beanManager.getReference(bean, type, ctx);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getBeanByType(final Class<T> type, Annotation... qualifiers)
      throws IllegalStateException {
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<T> bean = beanManager.resolve((Set) beanManager.getBeans(type, qualifiers));
    if (bean == null) {
      throw new IllegalStateException("Cannot find an instance of type " + type.getName());
    }
    CreationalContext<T> ctx = beanManager.createCreationalContext(bean);

    return (T) beanManager.getReference(bean, type, ctx);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getAllBeansByType(final Class<T> type, Annotation... qualifiers) {
    BeanManager beanManager = CDI.current().getBeanManager();
    return beanManager.getBeans(type, qualifiers).stream().map(bean -> {
          CreationalContext ctx = beanManager.createCreationalContext(bean);
          return (T) beanManager.getReference(bean, type, ctx);
        })
        .collect(Collectors.toSet());
  }
}
