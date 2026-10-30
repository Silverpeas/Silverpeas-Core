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
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestedBean;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorAuthenticationRepositoryTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TwoFactorAuthenticationRepositoryTest(
      @TestManagedMock EntityManagerProvider provider,
      @TestManagedMock ContentEncryptionService encryptionService) {
    this.provider = provider;
    this.encryptionService = encryptionService;
    this.repository = new TwoFactorAuthenticationRepositoryImpl(provider, encryptionService);
  }

  @TestManagedBean
  private EntityManagerProvider provider;
  @TestManagedBean
  private ContentEncryptionService encryptionService;
  @TestedBean
  private TwoFactorAuthenticationRepositoryImpl repository;
  private TwoFactorJpaTestDatabase database;
  private static final Instant NOW = Instant.parse("2026-10-13T08:00:00Z");

  @BeforeEach
  void setUp() throws Exception {
    database = new TwoFactorJpaTestDatabase();
    when(provider.getEntityManager()).thenReturn(database.entityManager());
    when(encryptionService.isCipherKeyDefined()).thenReturn(true);
    when(encryptionService.encryptContent(anyString())).thenAnswer(i ->
        new String[]{"encrypted:" + i.getArgument(0)});
    when(encryptionService.decryptContent(anyString())).thenAnswer(i ->
        new String[]{((String) i.getArgument(0)).substring("encrypted:".length())});
  }

  @AfterEach
  void tearDown() {
    database.close();
  }

  @Test
  void shouldSaveEncryptedSecretAndReadDecryptedConfiguration() {
    assertTrue(repository.get(42).isEmpty());
    repository.save(authentication(42, "secret"));
    var stored = repository.get(42).orElseThrow();
    assertEquals("secret", stored.getSecret());
    assertEquals(TwoFactorAuthentication.Status.PENDING, stored.getStatus());
    assertEquals(0, stored.getFailedAttempts());
    assertNull(stored.getLockedUntil());
    assertEquals("encrypted:secret", database.entityManager().createQuery(
        "select a.secret from TwoFactorAuthenticationEntity a", String.class).getSingleResult());
  }

  @Test
  void shouldPreserveCreationTimeWhenReplacingConfiguration() {
    repository.save(authentication(42, "first"));
    Instant created = repository.get(42).orElseThrow().getCreatedAt();
    repository.save(authentication(42, "second").toBuilder()
        .status(TwoFactorAuthentication.Status.ENABLED).failedAttempts(2)
        .lockedUntil(NOW.plusSeconds(300)).build());
    var stored = repository.get(42).orElseThrow();
    assertEquals(created, stored.getCreatedAt());
    assertEquals("second", stored.getSecret());
    assertTrue(stored.isEnabled());
    assertEquals(2, stored.getFailedAttempts());
    assertEquals(NOW.plusSeconds(300), stored.getLockedUntil());
  }

  @Test
  void shouldUpdateAndResetAttemptsAndLastUseOnlyForOwner() {
    repository.save(authentication(42, "a"));
    repository.save(authentication(43, "b"));
    repository.updateFailedAttempts(42, 5, NOW.plusSeconds(300));
    assertEquals(5, repository.get(42).orElseThrow().getFailedAttempts());
    assertEquals(NOW.plusSeconds(300), repository.get(42).orElseThrow().getLockedUntil());
    assertEquals(0, repository.get(43).orElseThrow().getFailedAttempts());
    repository.resetFailedAttempts(42);
    repository.updateLastUsedAt(42, NOW);
    var stored = repository.get(42).orElseThrow();
    assertEquals(0, stored.getFailedAttempts());
    assertNull(stored.getLockedUntil());
    assertEquals(NOW, stored.getLastUsedAt());
    assertNull(repository.get(43).orElseThrow().getLastUsedAt());
  }

  @Test
  void shouldDeleteOnlyOwnersConfiguration() {
    repository.save(authentication(42, "a"));
    repository.save(authentication(43, "b"));
    repository.delete(42);
    assertTrue(repository.get(42).isEmpty());
    assertTrue(repository.get(43).isPresent());
  }

  @Test
  void shouldRejectMissingKeyWithoutInstallingOne() throws Exception {
    when(encryptionService.isCipherKeyDefined()).thenReturn(false);
    var authentication = authentication(42, "secret");
    assertThrows(IllegalStateException.class, () -> repository.save(authentication));
    verify(encryptionService, never()).updateCipherKey(anyString());
    assertTrue(repository.get(42).isEmpty());
  }

  @Test
  void shouldPropagateEncryptionFailureWithoutPersistingSecret() throws Exception {
    when(encryptionService.encryptContent("secret")).thenThrow(new CryptoException("failure"));
    var authentication = authentication(42, "secret");
    assertThrows(IllegalStateException.class, () -> repository.save(authentication));
    assertTrue(repository.get(42).isEmpty());
  }

  private TwoFactorAuthentication authentication(int owner, String secret) {
    return TwoFactorAuthentication.builder(owner).secret(secret)
        .status(TwoFactorAuthentication.Status.PENDING).build();
  }
}
