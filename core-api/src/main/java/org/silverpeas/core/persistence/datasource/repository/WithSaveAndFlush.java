/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource.repository;

import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;

/**
 * This interface is dedicated to repositories that want to support a way to both save the entity
 * into the persistence context and then to flush it to the underlying data source. Usually, a
 * such operation isn't required as at the end of the transaction the persistence context in
 * Silverpeas is synchronized with the underlying data sources. Nevertheless, in some context, it
 * could be necessary to explicit flush the persistence context with a more global transaction
 * in order the entity to be ready to be used for others transactions.
 * @author mmoquillon
 */
public interface WithSaveAndFlush<T extends IdentifiableEntity> {

  /**
   * Saves the specified entity and then flushes the persistence context into the underlying data
   * sources.
   * @param entity the entity to save.
   * @return the saved entity.
   */
  T saveAndFlush(final T entity);
}
