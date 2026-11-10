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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import jakarta.enterprise.event.Event;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorKeyReset;
import java.util.List;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorSecretEncryptionIterator;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestedBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorSecretRepositoryTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TwoFactorSecretRepositoryTest(
      @TestManagedMock EntityManagerProvider provider,
      @TestManagedMock ContentEncryptionService encryptionService,
      @TestManagedMock Event<TwoFactorKeyReset> resetEvents) {
    this.provider = provider;
    this.encryptionService = encryptionService;
    this.resetEvents = resetEvents;
    this.repository = new TwoFactorSecretRepository(provider, resetEvents);
    this.authenticationRepository = new TwoFactorAuthenticationRepositoryImpl(provider, encryptionService, repository);
  }

  @TestManagedBean
  private EntityManagerProvider provider;
  @TestManagedBean
  private ContentEncryptionService encryptionService;
  @TestedBean
  private TwoFactorAuthenticationRepositoryImpl authenticationRepository;
  @TestedBean
  private TwoFactorSecretRepository repository;
  @TestManagedBean
  private Event<TwoFactorKeyReset> resetEvents;
  private TwoFactorJpaTestDatabase database;

  @BeforeEach
  void setUp() throws Exception {
    database = new TwoFactorJpaTestDatabase();
    when(provider.getEntityManager()).thenReturn(database.entityManager());
    when(encryptionService.isCipherKeyDefined()).thenReturn(true);
    when(encryptionService.encryptContent(anyString())).thenAnswer(i ->
        new String[]{"old:" + i.getArgument(0)});
    when(encryptionService.decryptContent(anyString())).thenAnswer(i ->
        new String[]{((String) i.getArgument(0)).substring(4)});
  }

  @AfterEach
  void tearDown() {
    database.close();
  }

  @Test
  void shouldResetCredentialsAndNotifyOnlyEnabledUsersAfterSuccessfulKeyChange() {
    for (int owner : new int[]{42, 43}) {
      authenticationRepository.save(TwoFactorAuthentication.builder(owner).secret("secret" + owner)
          .status(owner == 42 ? TwoFactorAuthentication.Status.ENABLED : TwoFactorAuthentication.Status.PENDING)
          .build());
      var em = database.entityManager();
      em.createNativeQuery("insert into ST_User_2FA_Recovery (userId, hash, createdAt) " +
          "values (:userId, 'recovery-hash', CURRENT_TIMESTAMP)")
          .setParameter("userId", owner).executeUpdate();
      em.createNativeQuery("insert into ST_User_2FA_Trusted_Device " +
          "(userId, tokenHash, createdAt, expiresAt) values " +
          "(:userId, 'trusted-hash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
          .setParameter("userId", owner).executeUpdate();
    }
    TwoFactorSecretEncryptionIterator iterator = new TwoFactorSecretEncryptionIterator(repository);
    iterator.init();
    assertFalse(iterator.hasNext());
    assertEquals(2, repository.getEncryptedSecrets().size());
    verifyNoInteractions(resetEvents);

    iterator.onEncryptionKeyChanged();
    assertTrue(repository.getEncryptedSecrets().isEmpty());
    assertEquals(0L, database.entityManager().createQuery(
        "select count(r) from RecoveryCodeEntity r", Long.class).getSingleResult());
    assertEquals(0L, database.entityManager().createQuery(
        "select count(d) from TrustedDeviceEntity d", Long.class).getSingleResult());
    verify(resetEvents).fire(new TwoFactorKeyReset(List.of("42")));

    iterator.onEncryptionKeyChanged();
    verifyNoMoreInteractions(resetEvents);
  }

  @Test
  void shouldResetOnlyTheUserWhoseSecretIsUnreadable() {
    for (int owner : new int[]{42, 43}) {
      authenticationRepository.save(TwoFactorAuthentication.builder(owner).secret("secret" + owner)
          .status(TwoFactorAuthentication.Status.ENABLED).build());
    }
    repository.resetUser(42);
    assertTrue(authenticationRepository.get(42).isEmpty());
    assertTrue(authenticationRepository.get(43).orElseThrow().isEnabled());
    verify(resetEvents).fire(new TwoFactorKeyReset(List.of("42")));
  }

}
