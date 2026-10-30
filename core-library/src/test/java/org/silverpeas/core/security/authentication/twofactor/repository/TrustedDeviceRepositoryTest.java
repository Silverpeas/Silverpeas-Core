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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TrustedDeviceRepositoryTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TrustedDeviceRepositoryTest(
      @TestManagedMock EntityManagerProvider provider) {
    this.provider = provider;
    this.repository = new TrustedDeviceRepository(provider);
  }

  @TestManagedBean
  private EntityManagerProvider provider;
  @TestedBean
  private TrustedDeviceRepository repository;
  private TwoFactorJpaTestDatabase database;
  private static final Instant NOW = Instant.parse("2026-10-13T08:00:00Z");

  @BeforeEach
  void setUp() throws Exception {
    database = new TwoFactorJpaTestDatabase();
    when(provider.getEntityManager()).thenReturn(database.entityManager());
  }

  @AfterEach
  void tearDown() {
    database.close();
  }

  @Test
  void shouldRotateOnceForOwnerAndPreserveAbsoluteExpiry() {
    Instant expiry = NOW.plusSeconds(3600);
    repository.save(42, "old", NOW, expiry, "browser");
    assertFalse(repository.rotate(43, "old", "other", NOW, "browser"));
    assertTrue(repository.rotate(42, "old", "new", NOW.plusSeconds(1), "browser"));
    assertFalse(repository.rotate(42, "old", "replay", NOW.plusSeconds(2), "browser"));
    assertEquals(expiry, repository.getExpiration(42, "new").orElseThrow());
    assertTrue(repository.getExpiration(42, "old").isEmpty());
    assertTrue(repository.getExpiration(43, "new").isEmpty());
  }

  @Test
  void shouldRejectAndRemoveExpiredTokenIncludingExactExpiry() {
    repository.save(42, "expired", NOW, NOW, null);
    assertFalse(repository.rotate(42, "expired", "new", NOW, null));
    assertTrue(repository.getExpiration(42, "expired").isEmpty());
    assertFalse(repository.rotate(42, "unknown", "new", NOW, null));
  }

  @Test
  void shouldRevokeOnlyOwnersDevices() {
    repository.save(42, "a", NOW, NOW.plusSeconds(100), null);
    repository.save(43, "b", NOW, NOW.plusSeconds(100), null);
    repository.deleteAll(42);
    assertTrue(repository.getExpiration(42, "a").isEmpty());
    assertTrue(repository.getExpiration(43, "b").isPresent());
  }

  @Test
  void concurrentRotationShouldHaveExactlyOneWinner() throws Exception {
    repository.save(42, "old", NOW, NOW.plusSeconds(100), null);
    database.entityManager().getTransaction().commit();
    CountDownLatch start = new CountDownLatch(1);
    try (var workers = Executors.newFixedThreadPool(2)) {
      var task = (java.util.concurrent.Callable<Boolean>) () -> {
        try (var em = database.newEntityManager()) {
          EntityManagerProvider threadProvider = mock(EntityManagerProvider.class);
          when(threadProvider.getEntityManager()).thenReturn(em);
          var threadRepository = new TrustedDeviceRepository(threadProvider);
          em.getTransaction().begin();
          assertTrue(start.await(10, TimeUnit.SECONDS));
          boolean rotated = threadRepository.rotate(42, "old", "new", NOW, null);
          em.getTransaction().commit();
          return rotated;
        }
      };
      var first = workers.submit(task);
      var second = workers.submit(task);
      start.countDown();
      assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
    }
  }
}
