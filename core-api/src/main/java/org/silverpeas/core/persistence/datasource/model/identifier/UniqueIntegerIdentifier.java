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
package org.silverpeas.core.persistence.datasource.model.identifier;

import jakarta.persistence.Embeddable;

/**
 * Unique identifier as an integer.
 *
 * @author ebonnet
 */
@Embeddable
public class UniqueIntegerIdentifier extends AutoIncrementableIdentifier<Integer> {

  private static final long serialVersionUID = 8570844400186460258L;

  public static UniqueIntegerIdentifier from(String value) {
    return new UniqueIntegerIdentifier().setFromString(value);
  }

  public static UniqueIntegerIdentifier from(int value) {
    var id = new UniqueIntegerIdentifier();
    id.setId(value);
    return id;
  }

  @Override
  public UniqueIntegerIdentifier setFromString(final String id) {
    setId(Integer.valueOf(id));
    return this;
  }

  /**
   * Generates a new numeric identifier encoded in 32 bits.
   *
   * @param parameters the name of the SQL table in which are stored the entities and the name of
   * the SQL column in the SQL table that stores the identifier values.
   * @return a new numeric identifier encoded in 32 bits.
   */
  @Override
  public UniqueIntegerIdentifier generateNewValue(String... parameters) {
    setId(nextNewValue(parameters));
    return this;
  }
}
