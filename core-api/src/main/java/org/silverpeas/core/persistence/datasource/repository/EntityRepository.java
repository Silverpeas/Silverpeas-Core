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
package org.silverpeas.core.persistence.datasource.repository;

import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A repository storing the entities managed or handled by Silverpeas. All entities that are
 * stored into such a repository are persisted over the runtime and then can be retrieved later.
 * <p>
 * When  an entity is stored into a repository, its unique identifier is then no more its local
 * and temporary OID (Object IDentifier) that is usually the address of its location in the
 * memory but a unique and permanent identifier that is usually its identifier in the persistence
 * context backed by the repository. This last identifier is then set when the entity is stored
 * into the repository.
 * </p>
 * <p>
 * The repository isn't just a simple DAO (Data Access Object) to access the whole or some of the
 * entities properties but it is a business object from which entities are persisted and retrieved.
 * That means the persistence of the entities should be handled only through business operations
 * provided by the repository, each of them having a specific business meaning, and these operations
 * should return the entity in its completeness. The fetching of some of the entity's relationships
 * should be done through the repositories corresponding to the entities related by the
 * relationships.
 * </p>
 * @author mmoquillon
 */
public interface EntityRepository<T extends IdentifiableEntity> {

  /**
   * Gets all the persisted entities.
   * WARNING: we don't recommend to use this method with a lot of persisted entities.
   * @return a list of all the persisted entities.
   */
  SilverpeasList<T> getAll();

  /**
   * Gets a persisted entity by its unique identifier.
   * @param id a unique identifier.
   * @return the entity with the specified entity identifier or null if no such entity exist in
   * the data source.
   */
  T getById(String id);

  /**
   * Gets persisted entities by their ids.
   * @param ids the unique identifiers of the entity to get.
   * @return a list of entities.
   */
  default SilverpeasList<T> getById(String... ids) {
    return getById(Arrays.asList(ids));
  }

  /**
   * Gets persisted entities by their ids.
   * @param ids the unique identifiers of the entity to get.
   * @return a list of entities.
   */
  SilverpeasList<T> getById(Collection<String> ids);

  /**
   * Finds all the entities that match the specified criteria.
   * @param criteria the criteria constraining the query and for which the entities to list have to
   * satisfy.
   * @return a list of entities matching the specified criteria. If a pagination criterion is
   * defined in the criteria, then the returned list is a {@link PaginationList} instance.
   */
  SilverpeasList<T> findByCriteria(final QueryCriteria criteria);

  /**
   * Persists entity : create (if id is null or empty) or update.
   * @param entity the entity to save.
   * @return the created or updated entity.
   */
  default T save(T entity) {
    return save(Arrays.asList(entity)).get(0);
  }

  /**
   * Persists entities : create (if id is null or empty) or update.
   * @param entities the entities to save.
   * @return the created or updated entity.
   */
  default SilverpeasList<T> save(T... entities) {
    return save(Arrays.asList(entities));
  }

  /**
   * Persists entities : create (if id is null or empty) or update.
   * @param entities the entities to save.
   * @return the created or updated entity.
   */
  SilverpeasList<T> save(List<T> entities);

  /**
   * Deletes entities.
   * @param entity the entity/entities to delete.
   */
  default void delete(T... entity) {
    delete(Arrays.asList(entity));
  }

  /**
   * Deletes entities.
   * @param entities the entities to delete.
   */
  void delete(List<T> entities);

  /**
   * Deletes entities by their ids.
   * @param ids the identifiers of the entities to delete.
   * @return number of deleted entities.
   */
  default long deleteById(final String... ids) {
    return deleteById(Arrays.asList(ids));
  }

  /**
   * Deletes entities by their ids.
   * @param ids the identifiers of the entities to delete.
   * @return number of deleted entities.
   */
  long deleteById(final Collection<String> ids);

  /**
   * Deletes all entities belonging to the specified component instance.
   * @param componentInstanceId the unique component instance identifier.
   * @return the number of deleted entities.
   */
  default long deleteByComponentInstanceId(final String componentInstanceId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Synchronizes the persistence context to the underlying data source. Within a transactional
   * context, the persistence context is directly put to the data source but will be effective
   * only when the transaction will be committed. The consequence of the synchronization within
   * a transaction context is the persistence context is then validated by the data source. Making
   * it work, the data source has to support the transactions.
   * <p>
   * Warning, the behavior of this method is implementation-dependent. According to the type of
   * the repository or of the underlying data source, the flush can not to be working.
   */
  void flush();

  /**
   * Does this repository contains the specified entity? It contains the entity if its persistence
   * context is taken in charge by the instances of the repository class.
   * @param entity an entity.
   * @return true if the specified entity exists in the persistence context backed by this
   * repository, false otherwise.
   */
  boolean contains(T entity);
}
