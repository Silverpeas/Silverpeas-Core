/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contribution.model;

import org.silverpeas.core.util.filter.FilterByType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The default implementation of the {@link ContributionModel} interface. In this implementation,
 * the properties defined in the model are expected to be a declared property of the underlying
 * concrete type of the modelled contribution and as such they are get by reflections.
 * @param <C> the type of linked {@link Contribution} instance.
 * @author mmoquillon
 */
public class DefaultContributionModel<C extends Contribution> implements ContributionModel {

  private final C contribution;

  protected DefaultContributionModel(final C contribution) {
    this.contribution = contribution;
  }

  @Override
  public FilterByType filterByType(final String property, final Object... parameters) {
    return new FilterByType(getProperty(property, parameters));
  }

  @Override
  public <T> T getProperty(final String property, final Object... parameters) {
    return getByReflection(property);
  }

  /**
   * Gets by reflection the value of the specified property of the underlying modelled contribution.
   * @param <T> the type of the property value.
   * @param property the property to get.
   * @param parameters some parameters useful for property value computation.
   * @return the value of the property
   */
  @SuppressWarnings("unchecked")
  protected <T> T getByReflection(final String property, final Object... parameters) {
    String propName = property;
    try {
      if (Character.isLowerCase(property.charAt(0))) {
        propName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
      }
      Method method = contribution.getClass().getDeclaredMethod(propName);
      return (T) method.invoke(contribution, parameters);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      if (Character.isUpperCase(property.charAt(0))) {
        propName = Character.toLowerCase(property.charAt(0)) + property.substring(1);
      }
      try {
        Field field = contribution.getClass().getDeclaredField(propName);
        field.setAccessible(true);
        return (T) field.get(contribution);
      } catch (NoSuchFieldException | IllegalAccessException e1) {
        throw new NoSuchPropertyException(
            "The property " + property + " isn't supported by the contribution " +
                contribution.getClass().getSimpleName());
      }
    }
  }

  /**
   * Gets the linked {@link Contribution} instance.
   * @return a {@link Contribution} instance.
   */
  protected C getContribution() {
    return contribution;
  }
}
  