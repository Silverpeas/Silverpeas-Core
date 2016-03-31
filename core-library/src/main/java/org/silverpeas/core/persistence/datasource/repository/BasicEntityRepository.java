package org.silverpeas.core.persistence.datasource.repository;

import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;

import java.util.Collection;
import java.util.List;

/**
 * It represents the repositories taken in charge the persistence context of the business
 * entities in Silverpeas. These entities can be a contribution, a contribution's content or
 * a transverse application bean (user, domain, ...) managed within the Silverpeas Portal.
 * <p>
 *   This interface is dedicated to be implemented by abstract repositories that providing each an
 *   implementation of the persistence technology used to manage the persistence of the entities
 *   in a data source.
 * </p>
 * @param <ENTITY> specify the class name of the entity which is handled by the repository
 * manager.
 * @param <ENTITY_IDENTIFIER_TYPE> the identifier class name used by {@link ENTITY} for its primary
 * key definition.
 * @author: ebonnet
 */
public interface BasicEntityRepository<ENTITY extends IdentifiableEntity, ENTITY_IDENTIFIER_TYPE> {
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
   * Persists entity : create (if id is null or empty) or update.
   * @param entity the entity to save.
   * @return the created or updated entity.
   */
  ENTITY save(ENTITY entity);

  /**
   * Persists and flush entity : create (if id is null or empty) or update.
   * @param entity the entity to save.
   * @return the created or updated entity.
   */
  ENTITY saveAndFlush(ENTITY entity);


  /**
   * Persists entities : create (if id is null or empty) or update.
   * @param entities the entities to save.
   * @return
   */
  List<ENTITY> save(ENTITY... entities);

  /**
   * Persists entities : create (if id is null or empty) or update.
   * @param entities the entities to save.
   * @return
   */
  List<ENTITY> save(List<ENTITY> entities);

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
   * Deletes all entities belonging to the specified component instance.
   * @param instanceId the unique instance identifier.
   * @return the number of deleted entities.
   */
  default long deleteByComponentInstanceId(final String instanceId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Synchronizes the persistence context to the underlying data source. Within a transactional
   * context, the persistence context is directly put to the data source but will be effective
   * only when the transaction will be committed. The consequence of the synchronization within
   * a transaction context is the persistence context is then validated by the data source. Making
   * it work, the data source has to support the transactions.
   * <p/>
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
  boolean contains(ENTITY entity);
}
