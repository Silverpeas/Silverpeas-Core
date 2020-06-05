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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.persistence.datasource.repository.WithSaveAndFlush;

import javax.persistence.Query;

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
 * @param <E> specify the class name of the entity which is handled by the repository entity.
 * @author ebonnet
 */
public class BasicJpaEntityRepository<E extends BasicJpaEntity>
    extends AbstractJpaEntityRepository<E> implements WithSaveAndFlush<E> {

  @Override
  public E saveAndFlush(final E entity) {
    E curEntity = save(entity);
    flush();
    return curEntity;
  }

  @Override
  public long deleteByComponentInstanceId(final String instanceId) {
    Query deleteQuery = getEntityManager().createQuery(
        "delete from " + getEntityClass().getName() + " a where a.instanceId = :instanceId");
    return newNamedParameters().add("instanceId", instanceId).applyTo(deleteQuery).executeUpdate();
  }

}
