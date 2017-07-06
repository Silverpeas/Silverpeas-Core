/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource.model;

import org.silverpeas.core.ResourceIdentifier;

import java.io.Serializable;

/**
 * All Silverpeas entities must use this interface for their identifier.
 * By this way, all entities have a typed identifier that only the entity knows.
 * @author Yohann Chastagnier
 */
public interface EntityIdentifier extends ResourceIdentifier, Serializable {

  /**
   * Sets the id value from a String.
   * @param id the String value of the identifier.
   * @return the entity identifier instance.
   */
  EntityIdentifier fromString(String id);

  /**
   * Generate a new ID.
   * "Auto-Increment" identifiers must implement this method.
   * @param tableName the name of the database table for which the next identifier value has to be
   * determined.
   * @param tableColumnIdName the name of the identifier column from which the next identifier value
   * has to be determined.
   * @return the entity identifier instance.
   */
  EntityIdentifier generateNewId(final String tableName, final String tableColumnIdName);
}
