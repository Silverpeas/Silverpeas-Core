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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.model.Entity;

import java.util.Date;

/**
 * A persistence operation. It defines the CRUD operations performed into a datasource.
 * @author mmoquillon
 */
public abstract class PersistenceOperation {

  /**
   * Applies to the specified entity a computation consisting to set its technical
   * data before being serialized to a datasource.
   * @param entity an entity.
   */
  protected abstract void applyTechnicalDataTo(final Entity<?, ?> entity);

  /**
   * Sets manually the specified technical data of the specified entity.
   * @param entity the entity.
   * @param user the user for which the persistence operation will be performed.
   * @param date the date at which the persistence operation will be considered as performed.
   */
  protected abstract void setManuallyTechnicalDataFor(final Entity<?, ?> entity, final User user,
      final Date date);

  /**
   * Clears the specified entity for the current persistence operation. Any technical data manually
   * set will be lost.
   * @param entity the entity.
   */
  protected abstract void clear(final Entity<?, ?> entity);
}
