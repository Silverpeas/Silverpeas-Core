package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.persistence.datasource.repository.WithSaveAndFlush;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;

import javax.persistence.Query;
import java.util.List;

/**
 * It represents the repositories taken in charge the persistence of the business entities in
 * Silverpeas in a basic way, that is to say without any of the added features from the new
 * Silverpeas persistence mechanism (creation and update date, ...). Theses entities were already
 * yet here before the new Silverpeas Persistence API and they were managed according to the SQL
 * old school way (J2EE BMP way). The repository is here to manage such entities in order to
 * facilitate the migration from the BMP approach to the new JPA one.
 *
 * The managed entities can be a contribution, a contribution's content or
 * a transverse application bean (user, domain, ...) whose the persistence was managed in the old
 * SQL way.
 *
 * This interface is dedicated to be implemented by abstract repositories that providing each an
 * implementation of the persistence technology used to manage the persistence of the entities
 * in a data source.
 * @param <E> specify the class name of the entity which is handled by the repository
 * entity.
 * @author ebonnet
 */
public class BasicJpaEntityRepository<E extends IdentifiableEntity>
    extends AbstractJpaEntityRepository<E> implements WithSaveAndFlush<E> {

  @Override
  public E saveAndFlush(final E entity) {
    E curEntity = save(entity);
    flush();
    return curEntity;
  }

  @Override
  public SilverpeasList<E> save(final List<E> entities) {
    SilverpeasList<E> savedEntities = new SilverpeasArrayList<>(entities.size());
    for (E entity : entities) {
      if (entity.isPersisted()) {
        savedEntities.add(getEntityManager().merge(entity));
      } else {
        getEntityManager().persist(entity);
        savedEntities.add(entity);
      }
    }
    return savedEntities;
  }

  @Override
  public long deleteByComponentInstanceId(final String instanceId) {
    Query deleteQuery = getEntityManager().createQuery(
        "delete from " + getEntityClass().getName() + " a where a.instanceId = :instanceId");
    return newNamedParameters().add("instanceId", instanceId).applyTo(deleteQuery).executeUpdate();
  }

}
