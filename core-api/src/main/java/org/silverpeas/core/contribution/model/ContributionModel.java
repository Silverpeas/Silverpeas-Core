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

import org.silverpeas.core.util.filter.Filter;

import java.time.temporal.Temporal;
import java.util.Date;

/**
 * Model of a contribution. A model is an object that is the business abstraction of the concrete
 * type of a contribution. Through the model, we can access the business properties specific to that
 * concrete type without having it to be explicitly known.
 * @author mmoquillon
 */
public interface ContributionModel {

  /**
   * Applies a filter on the value of the specified business property.
   * <p>
   * This method is useful to apply a specific treatment according to the type of the property
   * (in the case the property can be of different type or the property represents a more generic
   * business concept that can have different types according to the business context).
   * </p>
   * @param property the name of a business property of the represented contribution.
   * @return a filter by the type of the property.
   */
  Filter<Class<?>, Object> filterByType(final String property);

  /**
   * Gets the {@link Temporal} value of the specified business property.
   * @param property the name of the business property of the represented contribution.
   * @return a {@link Temporal} instance.
   */
  Temporal getTemporal(final String property);

  /**
   * Gets the {@link Date} value of the specified business property.
   * @param property the name of the business property of the represented contribution.
   * @return a {@link Date} instance.
   */
  Date getDate(final String property);
}
