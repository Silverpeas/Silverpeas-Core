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
 * @author mmoquillon
 */
public class DefaultContributionModel implements ContributionModel {

  private final Contribution contribution;

  DefaultContributionModel(final Contribution contribution) {
    this.contribution = contribution;
  }

  @Override
  public FilterByType filterByType(final String property) {
    return new FilterByType(getProperty(property));
  }

  @Override
  public <T> T getProperty(final String property) {
    return getByReflection(property);
  }

  /**
   * Gets by reflection the value of the specified property of the underlying modelled contribution.
   * @param property the property to get.
   * @param <T> the type of the property value.
   * @return the value of the property
   */
  protected <T> T getByReflection(final String property) {
    String propName = property;
    try {
      if (Character.isLowerCase(property.charAt(0))) {
        propName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
      }
      Method method = contribution.getClass().getDeclaredMethod(propName);
      return (T) method.invoke(contribution);
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
}
  