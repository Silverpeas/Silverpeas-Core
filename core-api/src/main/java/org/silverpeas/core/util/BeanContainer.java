/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * I represents the strategy used to manage the life-cycle of the objects (aka the beans) in
 * Silverpeas. Usually, the implementation of this interface is a wrapper or an adaptor to an
 * existing IoD container (Injection Of Dependency container) such as CDI or Spring.
 *
 * All implementation of a bean management container must implement this interface.
 *
 * @author mmoquillon
 */
public interface BeanContainer {

  /**
   * Gets a bean managed in this container by its name. If no such bean exists in the bean
   * container, then an IllegalStateException exception is thrown.
   * @param name the name of the bean.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws java.lang.IllegalStateException if no bean can be found with the specified name.
   */
  <T> T getBeanByName(String name) throws IllegalStateException;

  /**
   * Gets a bean managed in this container by its type and optionally by some qualifiers.
   * If no such bean exists in the bean container, then an IllegalStateException exception is
   * thrown.
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws java.lang.IllegalStateException if no bean of the specified type and with the
   * specified qualifiers can be found.
   */
  <T> T getBeanByType(Class<T> type, Annotation... qualifiers) throws IllegalStateException;

  /**
   * Gets a bean managed in this container by its name. If no such bean exists in the bean
   * container, then an empty set is returned.
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or
   * an empty set otherwise.
   */
  <T> Set<T> getAllBeansByType(Class<T> type, Annotation... qualifiers);
}
