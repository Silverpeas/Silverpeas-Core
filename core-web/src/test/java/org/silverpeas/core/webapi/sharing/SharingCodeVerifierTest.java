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
package org.silverpeas.core.webapi.sharing;

import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.sharing.model.SimpleFileTicket;
import org.silverpeas.core.sharing.model.Ticket;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class SharingCodeVerifierTest {

  private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
  private static final int MAX_ATTEMPTS = 5;

  private Ticket ticket(String code) {
    return SimpleFileTicket.builder().setSharedObjectId(1).setComponentId("kmelia1")
        .setCreatorId("1").setCreationDate(new Date())
        .setToken(UUID.randomUUID().toString()).setSecurityCode(code).build();
  }

  private boolean verify(Ticket ticket, String code, long now) {
    return SharingCodeVerifier.verify(ticket, code, now, MAX_ATTEMPTS,
        15 * MINUTE, 15 * MINUTE);
  }

  private void assertLocked(Ticket ticket, String code, long now) {
    try {
      verify(ticket, code, now);
      fail("Expected HTTP 429");
    } catch (WebApplicationException e) {
      assertEquals(429, e.getResponse().getStatus());
    }
  }

  @Test
  void correctAndIncorrectCodes() {
    Ticket ticket = ticket("ABC123");
    assertFalse(verify(ticket, "wrong", 1000));
    assertTrue(verify(ticket, "ABC123", 1001));
  }

  @Test
  void fifthFailureLocksEvenCorrectCodes() {
    Ticket ticket = ticket("ABC123");
    for (int i = 0; i < 4; i++) {
      assertFalse(verify(ticket, "wrong", 1000 + i));
    }
    assertLocked(ticket, "wrong", 1004);
    assertLocked(ticket, "ABC123", 1005);
  }

  @Test
  void failuresExpireOutsideAttemptWindow() {
    Ticket ticket = ticket("ABC123");
    for (int i = 0; i < 4; i++) {
      assertFalse(verify(ticket, "wrong", 1000 + i));
    }
    assertFalse(verify(ticket, "wrong", 1000 + 16 * MINUTE));
    assertTrue(verify(ticket, "ABC123", 1001 + 16 * MINUTE));
  }

  @Test
  void lockExpiresAndCounterResets() {
    Ticket ticket = ticket("ABC123");
    for (int i = 0; i < 4; i++) {
      assertFalse(verify(ticket, "wrong", 1000 + i));
    }
    assertLocked(ticket, "wrong", 1004);
    assertFalse(verify(ticket, "wrong", 1004 + 15 * MINUTE));
    assertTrue(verify(ticket, "ABC123", 1005 + 15 * MINUTE));
  }

  @Test
  void successfulVerificationClearsPreviousFailures() {
    Ticket ticket = ticket("ABC123");
    for (int i = 0; i < 4; i++) {
      assertFalse(verify(ticket, "wrong", 1000 + i));
    }
    assertTrue(verify(ticket, "ABC123", 1005));
    for (int i = 0; i < 4; i++) {
      assertFalse(verify(ticket, "wrong", 1010 + i));
    }
  }

  @Test
  void ticketsWithoutCodesDoNotGetLocked() {
    Ticket ticket = ticket(null);
    for (int i = 0; i < 10; i++) {
      assertTrue(verify(ticket, null, 1000 + i));
    }
  }
  @Test
  void cleanupRemovesExpiredFailures() {
    Ticket ticket = ticket("ABC123");
    assertFalse(verify(ticket, "wrong", 1000));
    assertTrue(SharingCodeVerifier.isTracking(ticket.getToken()));
    SharingCodeVerifier.cleanup(1000 + 16 * MINUTE, 15 * MINUTE);
    assertFalse(SharingCodeVerifier.isTracking(ticket.getToken()));
  }

  @Test
  void cleanupPreservesActiveLockUntilItExpires() {
    Ticket ticket = ticket("ABC123");
    for (int i = 0; i < 4; i++) {
      assertFalse(verify(ticket, "wrong", 1000 + i));
    }
    assertLocked(ticket, "wrong", 1004);
    SharingCodeVerifier.cleanup(1004 + MINUTE, 15 * MINUTE);
    assertLocked(ticket, "ABC123", 1004 + MINUTE);
    SharingCodeVerifier.cleanup(1004 + 16 * MINUTE, 15 * MINUTE);
    assertTrue(verify(ticket, "ABC123", 1004 + 16 * MINUTE));
  }
}
