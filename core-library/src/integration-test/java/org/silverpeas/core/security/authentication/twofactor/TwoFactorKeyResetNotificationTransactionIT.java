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
package org.silverpeas.core.security.authentication.twofactor;

import jakarta.annotation.Resource;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.util.ServiceProvider;

import static org.junit.Assert.*;

/**
 * Verifies after-commit delivery and its new JTA transaction with the container interceptors.
 * Only the notification transport is replaced; the sender inherits the production transaction
 * boundary and is injected into the production observer by CDI.
 */
@RunWith(Arquillian.class)
public class TwoFactorKeyResetNotificationTransactionIT {
  private ResetPublisher publisher;
  private DeliveryLog deliveries;

  @Deployment
  public static Archive<?> deployment() {
    return LibCoreWarBuilder.onWarForTestClass(TwoFactorKeyResetNotificationTransactionIT.class)
        .addClasses(TwoFactorKeyReset.class, TwoFactorKeyResetNotifier.class,
            TwoFactorKeyResetNotificationSender.class, ResetPublisher.class,
            RecordingSender.class, DeliveryLog.class, Delivery.class)
        .build();
  }

  @Before
  public void prepare() {
    publisher = ServiceProvider.getService(ResetPublisher.class);
    deliveries = ServiceProvider.getService(DeliveryLog.class);
    deliveries.clear();
  }

  @Test
  public void committedResetIsDeliveredInADifferentActiveTransaction() throws Exception {
    UserTransaction transaction = transaction();
    Object resetTransaction;
    transaction.begin();
    try {
      resetTransaction = publisher.publish();
      assertTrue(deliveries.entries().isEmpty());
      transaction.commit();
    } finally {
      rollbackIfActive(transaction);
    }
    assertDeliveryInNewTransaction(resetTransaction);
  }

  @Test
  public void rolledBackResetDoesNotSendNotifications() throws Exception {
    UserTransaction transaction = transaction();
    transaction.begin();
    try {
      publisher.publish();
      assertTrue(deliveries.entries().isEmpty());
    } finally {
      transaction.rollback();
    }
    assertTrue(deliveries.entries().isEmpty());
  }

  @Test
  public void resetOutsideATransactionStillDeliversInANewTransaction() {
    assertNull(publisher.publish());
    assertDeliveryInNewTransaction(null);
  }

  private void assertDeliveryInNewTransaction(Object resetTransaction) {
    assertEquals(1, deliveries.entries().size());
    Delivery delivery = deliveries.entries().getFirst();
    assertEquals(Status.STATUS_ACTIVE, delivery.status());
    assertNotNull(delivery.transactionKey());
    assertNotEquals(resetTransaction, delivery.transactionKey());
    assertEquals(List.of("42", "43"), delivery.userIds());
  }

  private UserTransaction transaction() throws Exception {
    return InitialContext.doLookup("java:comp/UserTransaction");
  }

  private void rollbackIfActive(UserTransaction transaction) throws Exception {
    if (transaction.getStatus() == Status.STATUS_ACTIVE) {
      transaction.rollback();
    }
  }

  /** Publishes the same event as the repository within the test's caller transaction. */
  @Bean
  public static class ResetPublisher {
    @Inject
    private Event<TwoFactorKeyReset> events;
    @Resource
    private TransactionSynchronizationRegistry transactions;

    public Object publish() {
      Object key = transactions.getTransactionKey();
      events.fire(new TwoFactorKeyReset(List.of("42", "43")));
      return key;
    }
  }

  /** Replaces transport while retaining the inherited REQUIRES_NEW interception. */
  @Bean
  @Specializes
  public static class RecordingSender extends TwoFactorKeyResetNotificationSender {
    @Resource
    private TransactionSynchronizationRegistry transactions;
    @Inject
    private DeliveryLog deliveries;

    @Override
    public void send(TwoFactorKeyReset reset) {
      deliveries.add(new Delivery(transactions.getTransactionStatus(),
          transactions.getTransactionKey(), reset.userIds()));
    }
  }

  /** Keeps deliveries independently of each observer instance's dependent scope. */
  @Bean
  @Singleton
  public static class DeliveryLog {
    private final List<Delivery> entries = new ArrayList<>();

    public void add(Delivery delivery) {
      entries.add(delivery);
    }

    public List<Delivery> entries() {
      return List.copyOf(entries);
    }

    public void clear() {
      entries.clear();
    }
  }

  /** Records the actual transaction seen at delivery. */
  public record Delivery(int status, Object transactionKey, List<String> userIds) {
  }
}
