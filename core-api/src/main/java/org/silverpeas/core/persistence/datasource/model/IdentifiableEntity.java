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
package org.silverpeas.core.persistence.datasource.model;

import java.io.Serializable;

/**
 * An identifiable entity is a serializable business entity that is uniquely identifiable over the
 * time and over different runtime. All entities to be persisted should implement this interface.
 * The persistence of an identifiable entity should be ensures by a
 * {@link org.silverpeas.core.persistence.datasource.repository.EntityRepository}.
 *
 * Because an identifiable entity is a business object, it is strongly recommended to expose the
 * business operations related to the entity itself directly within its interface. In the
 * same way, we recommend to perform repository related operations through the entity itself.
 * @author: ebonnet
 */
public interface IdentifiableEntity extends Serializable {
  /**
   * Gets the unique identifier of this entity.
   * @return the entity unique identifier in the form of a string.
   */
  String getId();

  /**
   * Indicates if the entity is persisted.
   * @return true if the entity is stored in a data source, false otherwise.
   */
  boolean isPersisted();

}
