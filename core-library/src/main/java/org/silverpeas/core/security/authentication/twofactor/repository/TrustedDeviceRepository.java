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
import java.util.Optional;

/**
 * JPA persistence of trusted-device tokens with conditional atomic rotation.
 */
@Repository
@Transactional(Transactional.TxType.SUPPORTS)
public class TrustedDeviceRepository {
  private static final String USER_ID_PARAMETER = "userId";

  private final EntityManagerProvider entityManagerProvider;

  @Inject
  protected TrustedDeviceRepository(final EntityManagerProvider entityManagerProvider) {
    this.entityManagerProvider = entityManagerProvider;
  }

  /**
   * Persists a token hash and its absolute expiration.
   * @param userId the owner.
   * @param tokenHash the hash, never the browser token.
   * @param now the creation time.
   * @param expiresAt the expiration time.
   * @param userAgent the browser description.
   */
  @Transactional(Transactional.TxType.REQUIRED)
  public void save(final int userId, final String tokenHash, final Instant now,
      final Instant expiresAt, final String userAgent) {
    TrustedDeviceEntity entity = new TrustedDeviceEntity();
    entity.userId = userId;
    entity.tokenHash = tokenHash;
    entity.createdAt = now;
    entity.expiresAt = expiresAt;
    entity.lastUsedAt = now;
    entity.userAgent = userAgent;
    entityManager().persist(entity);
  }

  /**
   * Replaces a valid token hash once, preserving its absolute expiration.
   * @param userId the owner.
   * @param hash the current token hash.
   * @param newHash the replacement token hash.
   * @param now the validation time.
   * @param userAgent the browser description.
   * @return true if exactly one valid token was rotated.
   */
  @Transactional(Transactional.TxType.REQUIRED)
  public boolean rotate(final int userId, final String hash, final String newHash,
      final Instant now, final String userAgent) {
    EntityManager em = entityManager();
    em.createQuery("delete from TrustedDeviceEntity d where d.userId = :userId " +
        "and d.tokenHash = :hash and d.expiresAt <= :now")
        .setParameter(USER_ID_PARAMETER, userId).setParameter("hash", hash)
        .setParameter("now", now).executeUpdate();
    return em.createQuery("update TrustedDeviceEntity d set d.tokenHash = :newHash, " +
        "d.lastUsedAt = :now, d.userAgent = :userAgent where d.userId = :userId " +
        "and d.tokenHash = :hash and d.expiresAt > :now")
        .setParameter("newHash", newHash).setParameter("now", now)
        .setParameter("userAgent", userAgent).setParameter(USER_ID_PARAMETER, userId)
        .setParameter("hash", hash).executeUpdate() == 1;
  }

  /**
   * Gets a token's absolute expiration.
   * @param userId the owner.
   * @param hash the token hash.
   * @return the expiration, if this token belongs to the user.
   */
  public Optional<Instant> getExpiration(final int userId, final String hash) {
    return entityManager().createQuery("select d.expiresAt from TrustedDeviceEntity d " +
        "where d.userId = :userId and d.tokenHash = :hash", Instant.class)
        .setParameter(USER_ID_PARAMETER, userId).setParameter("hash", hash).getResultStream().findFirst();
  }

  /**
   * Revokes all trusted devices belonging to a user.
   * @param userId the owner.
   */
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteAll(final int userId) {
    entityManager().createQuery("delete from TrustedDeviceEntity d where d.userId = :userId")
        .setParameter(USER_ID_PARAMETER, userId).executeUpdate();
  }

  private EntityManager entityManager() {
    return entityManagerProvider.getEntityManager();
  }
}
