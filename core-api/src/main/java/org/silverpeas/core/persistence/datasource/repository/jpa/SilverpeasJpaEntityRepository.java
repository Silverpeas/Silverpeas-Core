/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.Query;

/**
 * A repository of Silverpeas {@link org.silverpeas.core.persistence.datasource.model.Entity}
 * objects using JPA as persistence backend.
 *
 * This repository takes in charge the creation and the update properties of the entity from the
 * requester (whether there is a user behind the repository operation).
 *
 * All repositories that using JPA for managing the persistence of their entities that satisfy
 * the {@link org.silverpeas.core.persistence.datasource.model.Entity} interface should extends
 * this repository and provides the business operations related to the persistence of their
 * entities. If the different parts of an entity are persisted into several data source
 * beside a SQL-based one, then this repository should be used within a delegation of JPA related
 * operations.
 * @param <E> the class name of the entity which is handled by the repository.
 * @author Yohann Chastagnier
 */
public class SilverpeasJpaEntityRepository<E extends SilverpeasJpaEntity<E, ?>>
    extends AbstractJpaEntityRepository<E> {

  /**
   * Deletes all entities belonging to the specified component instance.
   * @param componentInstanceId the unique instance identifier.
   * @return the number of deleted entities.
   */
  @Override
  public long deleteByComponentInstanceId(final String componentInstanceId) {
    Query deleteQuery = getEntityManager().createQuery(
        "delete from " + getEntityClass().getName() + " a where a.componentInstanceId = :id");
    return newNamedParameters().add("id", componentInstanceId).applyTo(deleteQuery).executeUpdate();
  }

}
