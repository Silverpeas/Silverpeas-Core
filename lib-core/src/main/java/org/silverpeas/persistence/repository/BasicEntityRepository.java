package org.silverpeas.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.silverpeas.persistence.model.IdentifiableEntity;

/**
 * @author: ebonnet
 */
public interface BasicEntityRepository<ENTITY extends IdentifiableEntity<ENTITY, ENTITY_IDENTIFIER_TYPE>, ENTITY_IDENTIFIER_TYPE> {
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
}
