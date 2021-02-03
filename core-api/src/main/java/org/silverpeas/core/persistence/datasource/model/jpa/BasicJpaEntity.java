/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;

import javax.persistence.MappedSuperclass;

/**
 * This abstract class must be extended by all basic JPA entities that don't fit to the persistence
 * rules of the Silverpeas Persistence API. Usually it means to be used for the migration of
 * entities from the old persistence way (J2EE BMP) to JPA.
 *
 * Please be careful into the child entity classes about the use of @PrePersist and @PreUpdate
 * annotations. In most of cases you don't need to use them, but to override {@link
 * BasicJpaEntity#performBeforePersist} or {@link BasicJpaEntity#performBeforeUpdate} methods
 * without forgetting to invoke the super call.
 * @param <E> the class name of the represented entity.
 * @param <I> the unique identifier class used by the entity to identify it
 * uniquely in the persistence context.
 * @author ebonnet
 */
@MappedSuperclass
public abstract class BasicJpaEntity<E extends IdentifiableEntity, I extends EntityIdentifier>
    extends AbstractJpaEntity<E, I> {

  private static final long serialVersionUID = 3955905287437500278L;

  @Override
  protected void performBeforePersist() {
  }

  @Override
  protected void performBeforeUpdate() {
  }

  @Override
  protected void performBeforeRemove() {
  }
}
