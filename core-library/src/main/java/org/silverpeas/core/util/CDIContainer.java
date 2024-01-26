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
package org.silverpeas.core.util;

import org.silverpeas.kernel.BeanContainer;
import org.silverpeas.kernel.exception.ExpectationViolationException;
import org.silverpeas.kernel.exception.MultipleCandidateException;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the bean container above the CDI framework. It is loaded by Java SPI and should
 * be accessed only through the {@link org.silverpeas.kernel.ManagedBeanProvider} single instance.
 *
 * @author mmoquillon
 */
public class CDIContainer implements BeanContainer {

  @SuppressWarnings("unchecked")
  @Override
  public <T> Optional<T> getBeanByName(final String name) throws IllegalStateException {
    BeanManager beanManager = CDI.current().getBeanManager();
    try {
      //noinspection RedundantCast,rawtypes
      Bean<T> bean = beanManager.resolve((Set) beanManager.getBeans(name));
      if (bean == null) {
        return Optional.empty();
      }
      CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
      Type type = bean.getTypes()
          .stream()
          .findFirst()
          .orElseThrow(() -> new ExpectationViolationException("The bean " + name +
              " doesn't satisfy any managed type"));

      return Optional.ofNullable((T) beanManager.getReference(bean, type, ctx));
    } catch (AmbiguousResolutionException e) {
      throw new MultipleCandidateException(e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      // the figured out type of the bean is incorrect. This shouldn't happen as it is the one
      // CDI provides us for the fetched bean.
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Optional<T> getBeanByType(final Class<T> type, Annotation... qualifiers)
      throws IllegalStateException {
    BeanManager beanManager = CDI.current().getBeanManager();
    try {
      //noinspection RedundantCast,rawtypes
      Bean<T> bean = beanManager.resolve((Set) beanManager.getBeans(type, qualifiers));
      if (bean == null) {
        return Optional.empty();
      }
      CreationalContext<T> ctx = beanManager.createCreationalContext(bean);

      return Optional.ofNullable((T) beanManager.getReference(bean, type, ctx));
    } catch (AmbiguousResolutionException e) {
      throw new MultipleCandidateException(e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      // if the annotation isn't a qualifier (the type of the bean should be the one we
      // are asking)
      throw new ExpectationViolationException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getAllBeansByType(final Class<T> type, Annotation... qualifiers) {
    BeanManager beanManager = CDI.current().getBeanManager();
    try {
    return beanManager.getBeans(type, qualifiers).stream().map(bean -> {
          CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
          return (T) beanManager.getReference(bean, type, ctx);
        })
        .collect(Collectors.toSet());
    } catch (IllegalArgumentException e) {
      // if the annotation isn't a qualifier
      throw new ExpectationViolationException(e.getMessage(), e);
    }
  }
}
