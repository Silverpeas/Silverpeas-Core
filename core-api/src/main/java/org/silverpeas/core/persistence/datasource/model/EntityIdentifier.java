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

import org.silverpeas.core.ResourceIdentifier;

import java.io.Serializable;

/**
 * All Silverpeas entities must use this interface for their identifier. By this way, all entities
 * have a typed identifier that only the entity knows.
 *
 * @author Yohann Chastagnier
 */
public interface EntityIdentifier extends ResourceIdentifier, Serializable,
    Comparable<EntityIdentifier> {

  /**
   * Sets the identifier's value from the given String representation.
   *
   * @param id the encoded value of the identifier.
   * @return itself valued with the encoded value.
   */
  EntityIdentifier setFromString(String id);

  /**
   * Generates a new unique value for this entity identifier if and only it isn't yet valued.
   * "Auto-Increment" identifiers must implement this method.
   *
   * @param parameters the parameters required in the generation of the new identifier. Those
   * depends on the kind of entity identifier and they must be documented in the concrete class.
   * @return the valued identifier.
   */
  EntityIdentifier generateNewValue(String... parameters);

  /**
   * Is this identifier null? In other terms, is this identifier valued?
   * @return true if this identifier has no value. False otherwise.
   */
  boolean isNull();

  /**
   * Compares this identifier of entity with the specified one. The comparing is done by their
   * String representation.
   *
   * @param o another entity identifier.
   * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
   * or greater than the specified object.
   * @see Comparable#compareTo(Object)
   */
  @Override
  default int compareTo(final EntityIdentifier o) {
    return asString().compareTo(o.asString());
  }
}
