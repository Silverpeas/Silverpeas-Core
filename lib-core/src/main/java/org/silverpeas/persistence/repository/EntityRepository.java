/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.persistence.repository;

import org.silverpeas.persistence.model.Entity;

import java.util.Collection;
import java.util.List;

/*
 * This interface must be implemented by all repositories handling Silverpeas entities that have
 * to be persisted.
 *
 * It provides common entity query methods.
 *
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
public interface EntityRepository<ENTITY extends Entity<ENTITY, ENTITY_IDENTIFIER_TYPE>,
    ENTITY_IDENTIFIER_TYPE> {

  /**
   * Gets all persisted entities.
   * (It is recommended to not use this method on huge persitent containers)
   * @return
   */
  List<ENTITY> getAll();

  /**
   * Gets a persisted entity by its id.
   * @return
   */
  ENTITY getById(String id);

  /**
   * Gets persisted entities by their ids.
   * @return
   */
  List<ENTITY> getById(String... ids);

  /**
   * Gets persisted entities by their ids.
   * @return
   */
  List<ENTITY> getById(Collection<String> ids);

  /**
   * Gets a persisted entity by its id.
   * @return
   */
  ENTITY getByIdentifier(ENTITY_IDENTIFIER_TYPE id);

  /**
   * Gets persisted entities by their ids.
   * @return
   */
  List<ENTITY> getByIdentifier(ENTITY_IDENTIFIER_TYPE... ids);

  /**
   * Gets persisted entities by their ids.
   * @return
   */
  List<ENTITY> getByIdentifier(Collection<ENTITY_IDENTIFIER_TYPE> ids);

  /**
   * Persists entity : create (if id is null or empty) or update.
   * @param context the context of the save operation (containing the user saver for example).
   * @param entity the entity to save.
   * @return the created or updated entity.
   */
  ENTITY save(OperationContext context, ENTITY entity);

  /**
   * Persists entities : create (if id is null or empty) or update.
   * @param context the context of the save operation (containing the user saver for example).
   * @param entities the entities to save.
   * @return
   */
  List<ENTITY> save(OperationContext context, ENTITY... entities);

  /**
   * Persists entities : create (if id is null or empty) or update.
   * @param context the context of the save operation (containing the user saver for example).
   * @param entities the entities to save.
   * @return
   */
  List<ENTITY> save(OperationContext context, List<ENTITY> entities);

  /**
   * Deletes entities.
   * @return
   */
  void delete(ENTITY... entity);

  /**
   * Deletes entities.
   * @return
   */
  void delete(List<ENTITY> entities);

  /**
   * Deletes entities by their ids.
   * @param ids
   * @return number of deleted entities.
   */
  long deleteById(final String... ids);

  /**
   * Deletes entities by their ids.
   * @param ids
   * @return number of deleted entities.
   */
  long deleteById(final Collection<String> ids);

  /**
   * Deletes entities by their ids.
   * @param ids
   * @return number of deleted entities.
   */
  long deleteByIdentifier(final ENTITY_IDENTIFIER_TYPE... ids);

  /**
   * Deletes entities by their ids.
   * @param ids
   * @return number of deleted entities.
   */
  long deleteByIdentifier(final Collection<ENTITY_IDENTIFIER_TYPE> ids);
}
