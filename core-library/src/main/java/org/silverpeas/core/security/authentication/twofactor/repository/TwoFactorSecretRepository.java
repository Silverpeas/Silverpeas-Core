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
import jakarta.enterprise.event.Event;
import jakarta.persistence.LockModeType;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorKeyReset;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import java.util.List;

/**
 * Access to stored ciphertext for the platform cipher-key renewal lifecycle.
 * No decryption is performed while the encryption service holds its renewal lock.
 */
@Repository
@Transactional(Transactional.TxType.SUPPORTS)
public class TwoFactorSecretRepository {
  private static final String USER_ID_PARAMETER = "userId";
  private final EntityManagerProvider provider;
  private final Event<TwoFactorKeyReset> resetEvents;

  /**
   * Creates the ciphertext repository and its transactional reset-event publisher.
   * @param provider the entity manager provider.
   * @param resetEvents events delivered to notification observers after commit.
   */
  @Inject
  protected TwoFactorSecretRepository(final EntityManagerProvider provider,
      final Event<TwoFactorKeyReset> resetEvents) {
    this.provider = provider;
    this.resetEvents = resetEvents;
  }

  /**
   * Reads the encrypted secrets to be processed by the platform encryption service.
   * @return pairs of user identifier and stored ciphertext.
   */
  public List<Object[]> getEncryptedSecrets() {
    return provider.getEntityManager().createQuery(
        "select a.userId, a.secret from TwoFactorAuthenticationEntity a", Object[].class)
        .getResultList();
  }

  /**
   * Replaces ciphertext without changing the authentication configuration.
   * @param userId the owner of the secret.
   * @param ciphertext the newly encrypted secret.
   */
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateEncryptedSecret(final int userId, final String ciphertext) {
    provider.getEntityManager().createQuery(
        "update TwoFactorAuthenticationEntity a set a.secret = :secret where a.userId = :userId")
        .setParameter("secret", ciphertext).setParameter(USER_ID_PARAMETER, userId).executeUpdate();
  }

  /**
   * Invalidates all enrollments and associated credentials after a key loss or change.
   * Row locks serialize competing resets so enabled users are notified once.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void resetAll() {
    var configurations = provider.getEntityManager().createQuery(
        "select a from TwoFactorAuthenticationEntity a", TwoFactorAuthenticationEntity.class)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    reset(configurations);
  }

  /**
   * Invalidates a configuration whose secret cannot be decrypted with the available key.
   * @param userId the owner of the unreadable secret.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void resetUser(final int userId) {
    var configurations = provider.getEntityManager().createQuery(
        "select a from TwoFactorAuthenticationEntity a where a.userId = :userId",
        TwoFactorAuthenticationEntity.class).setParameter(USER_ID_PARAMETER, userId)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    reset(configurations);
  }

  private void reset(final List<TwoFactorAuthenticationEntity> configurations) {
    var enabledUsers = configurations.stream().filter(a -> "ENABLED".equals(a.status))
        .map(a -> String.valueOf(a.userId)).toList();
    var em = provider.getEntityManager();
    for (var configuration : configurations) {
      em.createQuery("delete from RecoveryCodeEntity r where r.userId = :userId")
          .setParameter(USER_ID_PARAMETER, configuration.userId).executeUpdate();
      em.createQuery("delete from TrustedDeviceEntity d where d.userId = :userId")
          .setParameter(USER_ID_PARAMETER, configuration.userId).executeUpdate();
      em.remove(configuration);
    }
    em.flush();
    if (!enabledUsers.isEmpty()) {
      resetEvents.fire(new TwoFactorKeyReset(enabledUsers));
    }
  }

}
