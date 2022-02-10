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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.PersistenceOperation;
import org.silverpeas.core.persistence.datasource.UpdateOperation;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.util.ArgumentAssertion;
import org.silverpeas.core.util.StringUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author mmoquillon
 */
@UpdateOperation
@Technical
@JPA
@Bean
public class JpaUpdateOperation extends PersistenceOperation {

  private List<SilverpeasJpaEntity> entities = new ArrayList<>();

  /**
   * Applying information of the context to the given entity on a update operation.
   * @param entity an entity.
   */
  protected void applyTechnicalDataTo(Entity entity) {
    String errorMessage = "the user identifier must exist when performing update operation";
    User user = OperationContext.getFromCache().getUser();
    ArgumentAssertion.assertTrue(Transaction.isTransactionActive(),
        "A transaction must be active when performing update operation");
    ArgumentAssertion.assertNotNull(user, errorMessage);
    ArgumentAssertion.assertDefined(user.getId(), errorMessage);
    if (entity instanceof SilverpeasJpaEntity) {
      SilverpeasJpaEntity jpaEntity = (SilverpeasJpaEntity) entity;
      Optional<SilverpeasJpaEntity> optional = find(jpaEntity);
      if (optional.isPresent()) {
        if (optional.get() != jpaEntity) {
          jpaEntity.setLastUpdater(optional.get().getLastUpdater())
              .setLastUpdateDate(optional.get().getLastUpdateDate());
        }
      } else {
        jpaEntity.setLastUpdater(user).setLastUpdateDate(new Timestamp(new Date().getTime()));
      }
    } else {
      entity.updatedBy(user, new Timestamp(new Date().getTime()));
    }
  }

  public void setManuallyTechnicalDataFor(final Entity entity, final User updater,
      final Date updateDate) {
    if (entity instanceof SilverpeasJpaEntity) {
      SilverpeasJpaEntity jpaEntity = (SilverpeasJpaEntity) entity;
      JpaEntityReflection.setUpdateData(jpaEntity, updater, updateDate);
      if (!find(jpaEntity).isPresent()) {
        this.entities.add(jpaEntity);
      }
    }
  }

  private Optional<SilverpeasJpaEntity> find(final Entity entity) {
    return this.entities.stream().filter(e -> StringUtil.isDefined(e.getId()) && e.equals(entity))
        .findFirst();
  }

  public void clear(final Entity entity) {
    entities.removeIf(
        e -> e.getClass().equals(entity.getClass()) && StringUtil.isDefined(e.getId()) &&
            e.getId().equals(entity.getId()));
  }
}
  