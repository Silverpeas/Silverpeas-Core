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

import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class SharingResourceTest {

  private Ticket ticket(String code) {
    return SimpleFileTicket.builder()
        .setSharedObjectId(1)
        .setComponentId("kmelia1")
        .setCreatorId("1")
        .setCreationDate(new Date())
        .setToken(UUID.randomUUID().toString())
        .setSecurityCode(code)
        .build();
  }

  private void assertNotFound(Ticket ticket) {
    try {
      SharingResource.verifySecurityCode(ticket, "ABC123");
      fail("Expected HTTP 404");
    } catch (WebApplicationException e) {
      assertEquals(404, e.getResponse().getStatus());
    }
  }

  @Test
  void missingTicketReturnsNotFound() {
    assertNotFound(null);
  }

  @Test
  void expiredTicketReturnsNotFound() {
    Ticket expired = ticket("ABC123");
    expired.setEndDate(new Date(1L));
    assertNotFound(expired);
  }

  @Test
  void validCodeIsAccepted() {
    assertTrue(SharingResource.verifySecurityCode(ticket("ABC123"), "ABC123"));
  }

  @Test
  void invalidCodeIsRejected() {
    assertFalse(SharingResource.verifySecurityCode(ticket("ABC123"), "wrong"));
  }

  @Test
  void missingCodeIsRejectedForProtectedTicket() {
    assertFalse(SharingResource.verifySecurityCode(ticket("ABC123"), null));
  }

  @Test
  void unprotectedTicketIsAccepted() {
    assertTrue(SharingResource.verifySecurityCode(ticket(null), null));
  }
}
