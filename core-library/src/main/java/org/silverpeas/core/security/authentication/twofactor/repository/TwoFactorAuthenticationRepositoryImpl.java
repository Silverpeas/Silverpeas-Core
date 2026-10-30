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
import org.silverpeas.core.security.authentication.twofactor.MissingTwoFactorEncryptionKeyException;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import java.util.Optional;

/**
 * JPA repository of two-factor configurations. Only encrypted secrets are persisted.
 */
@Repository
@Transactional(Transactional.TxType.SUPPORTS)
public class TwoFactorAuthenticationRepositoryImpl implements TwoFactorAuthenticationRepository {
  private static final String USER_ID_PARAMETER = "userId";

  private final EntityManagerProvider entityManagerProvider;
  private final ContentEncryptionService encryptionService;

  @Inject
  protected TwoFactorAuthenticationRepositoryImpl(final EntityManagerProvider entityManagerProvider,
      final ContentEncryptionService encryptionService) {
    this.entityManagerProvider = entityManagerProvider;
    this.encryptionService = encryptionService;
  }

  @Override
  public Optional<TwoFactorAuthentication> get(final int userId) {
    // Projection reads the database after bulk updates without returning stale managed entities.
    return entityManager().createQuery("select a.secret, a.status, a.createdAt, a.updatedAt, " +
        "a.lastUsedAt, a.failedAttempts, a.lockedUntil from TwoFactorAuthenticationEntity a " +
        "where a.userId = :userId", Object[].class).setParameter(USER_ID_PARAMETER, userId)
        .getResultStream().findFirst().map(a -> TwoFactorAuthentication.builder(userId)
            .secret(decryptSecret((String) a[0]))
            .status(TwoFactorAuthentication.Status.valueOf((String) a[1]))
            .createdAt((Instant) a[2]).updatedAt((Instant) a[3]).lastUsedAt((Instant) a[4])
            .failedAttempts((Integer) a[5]).lockedUntil((Instant) a[6]).build());
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void save(final TwoFactorAuthentication authentication) {
    String encrypted = encryptSecret(authentication.getSecret());
    Instant now = Instant.now();
    EntityManager em = entityManager();
    int updated = em.createQuery("update TwoFactorAuthenticationEntity a set a.secret = :secret, " +
        "a.status = :status, a.updatedAt = :now, a.lastUsedAt = :lastUsedAt, " +
        "a.failedAttempts = :attempts, a.lockedUntil = :lockedUntil where a.userId = :userId")
        .setParameter("secret", encrypted).setParameter("status", authentication.getStatus().name())
        .setParameter("now", now).setParameter("lastUsedAt", authentication.getLastUsedAt())
        .setParameter("attempts", authentication.getFailedAttempts())
        .setParameter("lockedUntil", authentication.getLockedUntil())
        .setParameter(USER_ID_PARAMETER, authentication.getUserId()).executeUpdate();
    if (updated == 0) {
      TwoFactorAuthenticationEntity entity = new TwoFactorAuthenticationEntity();
      entity.userId = authentication.getUserId();
      entity.secret = encrypted;
      entity.status = authentication.getStatus().name();
      entity.createdAt = now;
      entity.updatedAt = now;
      entity.lastUsedAt = authentication.getLastUsedAt();
      entity.failedAttempts = authentication.getFailedAttempts();
      entity.lockedUntil = authentication.getLockedUntil();
      em.persist(entity);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void delete(final int userId) {
    entityManager().createQuery("delete from TwoFactorAuthenticationEntity a where a.userId = :userId")
        .setParameter(USER_ID_PARAMETER, userId).executeUpdate();
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateLastUsedAt(final int userId, final Instant lastUsedAt) {
    entityManager().createQuery("update TwoFactorAuthenticationEntity a set " +
        "a.lastUsedAt = :lastUsedAt, a.updatedAt = :now where a.userId = :userId")
        .setParameter("lastUsedAt", lastUsedAt).setParameter("now", Instant.now())
        .setParameter(USER_ID_PARAMETER, userId).executeUpdate();
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateFailedAttempts(final int userId, final int failedAttempts,
      final Instant lockedUntil) {
    entityManager().createQuery("update TwoFactorAuthenticationEntity a set " +
        "a.failedAttempts = :attempts, a.lockedUntil = :lockedUntil, a.updatedAt = :now " +
        "where a.userId = :userId").setParameter("attempts", failedAttempts)
        .setParameter("lockedUntil", lockedUntil).setParameter("now", Instant.now())
        .setParameter(USER_ID_PARAMETER, userId).executeUpdate();
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void resetFailedAttempts(final int userId) {
    updateFailedAttempts(userId, 0, null);
  }

  private String encryptSecret(final String secret) {
    if (!encryptionService.isCipherKeyDefined()) {
      throw new MissingTwoFactorEncryptionKeyException();
    }
    try {
      return encryptionService.encryptContent(secret)[0];
    } catch (CryptoException e) {
      throw new IllegalStateException("Unable to encrypt the two-factor authentication secret", e);
    }
  }

  private String decryptSecret(final String secret) {
    try {
      return encryptionService.decryptContent(secret)[0];
    } catch (CryptoException e) {
      throw new IllegalStateException("Unable to decrypt the two-factor authentication secret", e);
    }
  }

  private EntityManager entityManager() {
    return entityManagerProvider.getEntityManager();
  }
}
