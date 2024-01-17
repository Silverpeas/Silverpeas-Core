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
package org.silverpeas.core.contribution.model;

import org.silverpeas.core.util.filter.Filter;

/**
 * Model of a contribution. A model is an object representing the business abstraction of a concrete
 * type of contribution. It provides an interface to access in a more generic way the different
 * business properties of the represented contribution without having to be explicitly known.
 * Business properties mean here the whole interface describing the behaviour of the object.
 * @author mmoquillon
 */
public interface ContributionModel {

  /**
   * Applies a filter on the type of the value of the specified business property. If the property
   * value is null, then no filtering is applied.
   * <p>
   * This method is useful to apply a specific treatment according to the type of the property
   * (in the case the property can be of different type or the property represents a more generic
   * business concept that can have different types according to the business context).
   * </p>
   * @param property the name of a business property of the represented contribution.
   * @param parameters some parameters useful for property value computation.
   * @return a filter by the type of the property.
   */
  Filter<Class<?>, Object> filterByType(final String property, final Object... parameters);

  /**
   * Gets the value of the specified business property. The property can accept zero, one or more
   * parameters.
   * @param property the name of the business property of the represented contribution.
   * @param parameters optionally some parameters required to select the correct value of the
   * property
   * @param <T> the expected type of the property value.
   * @return the value of the asked business property.
   */
  <T> T getProperty(final String property, final Object... parameters);

}
