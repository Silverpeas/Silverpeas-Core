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
import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EnableSilverTestEnv(context = JEETestContext.class)
class RecoveryCodeRepositoryTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  RecoveryCodeRepositoryTest(
      @TestManagedMock EntityManagerProvider provider) {
    this.provider = provider;
    this.repository = new RecoveryCodeRepositoryImpl(provider);
  }

  @TestManagedBean
  private EntityManagerProvider provider;
  @TestedBean
  private RecoveryCodeRepositoryImpl repository;
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
  void shouldListOnlyUnusedCodesOfOwner() {
    repository.save(code(42, "unused", false));
    repository.save(code(42, "used", true));
    repository.save(code(43, "other", false));
    var codes = repository.getUnused(42);
    assertEquals(1, codes.size());
    assertTrue(codes.getFirst().getId() > 0);
    assertEquals("unused", codes.getFirst().getHash());
    assertEquals(NOW, codes.getFirst().getCreatedAt());
  }

  @Test
  void shouldConsumeOnlyOnceAndOnlyForOwner() {
    repository.save(code(43, "hash", false));
    assertFalse(repository.consume(42, "hash", NOW));
    assertEquals(1, repository.getUnused(43).size());
    assertTrue(repository.consume(43, "hash", NOW));
    assertFalse(repository.consume(43, "hash", NOW.plusSeconds(1)));
    assertTrue(repository.getUnused(43).isEmpty());
    var stored = database.entityManager().createQuery(
        "select c.usedAt from RecoveryCodeEntity c where c.userId = 43", Instant.class)
        .getSingleResult();
    assertEquals(NOW, stored);
    assertFalse(repository.consume(43, "unknown", NOW));
  }

  @Test
  void shouldNotChangeAlreadyUsedCode() {
    repository.save(code(42, "hash", true));
    assertFalse(repository.consume(42, "hash", NOW.plusSeconds(10)));
    assertEquals(NOW, database.entityManager().createQuery(
        "select c.usedAt from RecoveryCodeEntity c", Instant.class).getSingleResult());
  }

  @Test
  void shouldDeleteOnlyOwnersCodes() {
    repository.save(code(42, "a", false));
    repository.save(code(43, "b", false));
    repository.deleteAll(42);
    assertTrue(repository.getUnused(42).isEmpty());
    assertEquals(1, repository.getUnused(43).size());
  }

  @Test
  void shouldRollBackConsumption() {
    repository.save(code(42, "hash", false));
    var em = database.entityManager();
    em.getTransaction().commit();
    em.getTransaction().begin();
    assertTrue(repository.consume(42, "hash", NOW));
    em.getTransaction().rollback();
    em.clear();
    em.getTransaction().begin();
    assertTrue(repository.consume(42, "hash", NOW));
  }

  @Test
  void concurrentConsumptionShouldHaveExactlyOneWinner() throws Exception {
    repository.save(code(42, "hash", false));
    database.entityManager().getTransaction().commit();
    CountDownLatch start = new CountDownLatch(1);
    try (var workers = Executors.newFixedThreadPool(2)) {
      var task = (java.util.concurrent.Callable<Boolean>) () -> {
        try (var em = database.newEntityManager()) {
          EntityManagerProvider threadProvider = mock(EntityManagerProvider.class);
          when(threadProvider.getEntityManager()).thenReturn(em);
          var threadRepository = new RecoveryCodeRepositoryImpl(threadProvider);
          em.getTransaction().begin();
          assertTrue(start.await(10, TimeUnit.SECONDS));
          boolean consumed = threadRepository.consume(42, "hash", NOW);
          em.getTransaction().commit();
          return consumed;
        }
      };
      var first = workers.submit(task);
      var second = workers.submit(task);
      start.countDown();
      assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
    }
  }

  private RecoveryCode code(int owner, String hash, boolean used) {
    return RecoveryCode.builder(owner).hash(hash).used(used).createdAt(NOW)
        .usedAt(used ? NOW : null).build();
  }
}
