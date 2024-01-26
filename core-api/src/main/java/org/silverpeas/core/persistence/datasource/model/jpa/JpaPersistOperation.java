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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.PersistOperation;
import org.silverpeas.core.persistence.datasource.PersistenceOperation;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.util.ArgumentAssertion;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author mmoquillon
 */
@PersistOperation
@Technical
@JPA
@Bean
public class JpaPersistOperation extends PersistenceOperation {

  private final List<SilverpeasJpaEntity<?, ?>> entities = new ArrayList<>();

  /**
   * Applying information of the context to the given entity on a persist operation.
   * @param entity an entity.
   */
  @Override
  protected void applyTechnicalDataTo(Entity<?, ?> entity) {
    String errorMessage = "the user identifier must exist when performing persist operation";
    User user = OperationContext.getFromCache().getUser();
    ArgumentAssertion.assertTrue(Transaction.isTransactionActive(),
        "A transaction must be active when performing persist operation");
    ArgumentAssertion.assertNotNull(user, errorMessage);
    ArgumentAssertion.assertDefined(user.getId(), errorMessage);
    if (entity instanceof SilverpeasJpaEntity) {
      SilverpeasJpaEntity<?, ?> jpaEntity = (SilverpeasJpaEntity<?, ?>) entity;
      Optional<SilverpeasJpaEntity<?, ?>> optional = find(jpaEntity);
      if (optional.isPresent()) {
        if (optional.get() != jpaEntity) {
          jpaEntity.setCreator(optional.get().getCreator())
              .setCreationDate(optional.get().getCreationDate());
        }
        if (jpaEntity.getLastUpdater() != null && jpaEntity.getLastUpdateDate() != null) {
          jpaEntity.setLastUpdater(optional.get().getLastUpdater())
              .setLastUpdateDate(optional.get().getLastUpdateDate());
        } else {
          jpaEntity.setLastUpdater(optional.get().getCreator())
              .setLastUpdateDate(optional.get().getCreationDate());
        }
      } else {
        Timestamp now = new Timestamp(new Date().getTime());
        jpaEntity.setCreator(user).setCreationDate(now);
        jpaEntity.setLastUpdater(user).setLastUpdateDate(now);
      }
    } else {
      Timestamp now = new Timestamp(new Date().getTime());
      entity.createdBy(user, now);
      entity.updatedBy(user, now);
    }
  }

  /**
   * Indicates the creation properties of the specified entity was set explicitly. Useful when this
   * information is lost when the entity is persisted with its counterpart in the persistence
   * context.
   * @param entity the entity for which the update properties were set.
   */
  @Override
  public void setManuallyTechnicalDataFor(final Entity<?, ?> entity, final User creator,
      final Date creationDate) {
    if (entity instanceof SilverpeasJpaEntity) {
      SilverpeasJpaEntity<?, ?> jpaEntity = (SilverpeasJpaEntity<?, ?>) entity;
      jpaEntity.setCreator(creator).setCreationDate(creationDate);
      if (find(jpaEntity).isEmpty()) {
        this.entities.add(jpaEntity);
      }
    }
  }

  private Optional<SilverpeasJpaEntity<?, ?>> find(final Entity<?, ?> entity) {
    return this.entities.stream().filter(e -> e.equals(entity))
        .findFirst();
  }

  @Override
  public void clear(final Entity<?, ?> entity) {
    if (!StringUtil.isDefined(entity.getId())) {
      entities.removeIf(
          e -> e.getClass().equals(entity.getClass()) && StringUtil.isDefined(e.getId()));
    }
  }
}
  