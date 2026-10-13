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
import org.silverpeas.core.security.authentication.twofactor.TwoFactorSecretEncryptionIterator;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestedBean;
import java.time.Instant;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorSecretRepositoryTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TwoFactorSecretRepositoryTest(
      @TestManagedMock EntityManagerProvider provider,
      @TestManagedMock ContentEncryptionService encryptionService) {
    this.provider = provider;
    this.encryptionService = encryptionService;
    this.authenticationRepository = new TwoFactorAuthenticationRepositoryImpl(provider, encryptionService);
    this.repository = new TwoFactorSecretRepository(provider);
  }

  @TestManagedBean
  private EntityManagerProvider provider;
  @TestManagedBean
  private ContentEncryptionService encryptionService;
  @TestedBean
  private TwoFactorAuthenticationRepositoryImpl authenticationRepository;
  @TestedBean
  private TwoFactorSecretRepository repository;
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
  void shouldRenewEverySecretWithoutChangingAuthenticationMetadata() {
    Instant lastUse = Instant.parse("2026-10-13T08:00:00Z");
    for (int owner : new int[]{42, 43}) {
      authenticationRepository.save(TwoFactorAuthentication.builder(owner).secret("secret" + owner)
          .status(TwoFactorAuthentication.Status.ENABLED).lastUsedAt(lastUse)
          .failedAttempts(2).lockedUntil(lastUse.plusSeconds(300)).build());
    }
    var before = authenticationRepository.get(42).orElseThrow();
    TwoFactorSecretEncryptionIterator iterator = new TwoFactorSecretEncryptionIterator(repository);
    iterator.init();
    int renewed = 0;
    while (iterator.hasNext()) {
      String ciphertext = iterator.next().get("secret");
      assertTrue(ciphertext.startsWith("old:"));
      iterator.update(Map.of("secret", "new:" + ciphertext.substring(4)));
      renewed++;
    }
    assertEquals(2, renewed);
    var stored = repository.getEncryptedSecrets();
    assertTrue(stored.stream().allMatch(a -> ((String) a[1]).startsWith("new:")));
    assertEquals(before, authenticationRepository.get(42).orElseThrow());
    assertEquals("secret43", authenticationRepository.get(43).orElseThrow().getSecret());
    // The same iterator must reload current ciphertext on the next key renewal.
    iterator.init();
    while (iterator.hasNext()) {
      assertTrue(iterator.next().get("secret").startsWith("new:"));
    }
  }
}
