/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.security.authentication.twofactor.repository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import java.time.Instant;
import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;
import java.util.List;

/**
 * JPA repository of recovery codes. Consumption is an atomic, user-scoped update.
 */
@Repository
@Transactional(Transactional.TxType.SUPPORTS)
public class RecoveryCodeRepositoryImpl implements RecoveryCodeRepository {

  private final EntityManagerProvider entityManagerProvider;

  @Inject
  protected RecoveryCodeRepositoryImpl(final EntityManagerProvider entityManagerProvider) {
    this.entityManagerProvider = entityManagerProvider;
  }

  @Override
  public List<RecoveryCode> getUnused(final int userId) {
    return entityManager().createQuery(
        "select c from RecoveryCodeEntity c where c.userId = :userId and c.used = false",
        RecoveryCodeEntity.class).setParameter("userId", userId).getResultList().stream()
        .map(c -> RecoveryCode.builder(c.userId).id(c.id).hash(c.hash).used(c.used)
            .createdAt(c.createdAt).usedAt(c.usedAt).build()).toList();
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void save(final RecoveryCode code) {
    RecoveryCodeEntity entity = new RecoveryCodeEntity();
    entity.userId = code.getUserId();
    entity.hash = code.getHash();
    entity.used = code.isUsed();
    entity.createdAt = code.getCreatedAt();
    entity.usedAt = code.getUsedAt();
    entityManager().persist(entity);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public boolean consume(final int userId, final String hash, final Instant usedAt) {
    return entityManager().createQuery("update RecoveryCodeEntity c set c.used = true, " +
        "c.usedAt = :usedAt where c.userId = :userId and c.hash = :hash and c.used = false")
        .setParameter("userId", userId).setParameter("hash", hash)
        .setParameter("usedAt", usedAt).executeUpdate() == 1;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteAll(final int userId) {
    entityManager().createQuery("delete from RecoveryCodeEntity c where c.userId = :userId")
        .setParameter("userId", userId).executeUpdate();
  }

  private EntityManager entityManager() {
    return entityManagerProvider.getEntityManager();
  }
}
