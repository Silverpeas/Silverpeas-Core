/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.model;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TicketTest {

  private Ticket newTicket(String securityCode) {
    return SimpleFileTicket.builder()
        .setSharedObjectId(1)
        .setComponentId("kmelia1")
        .setCreatorId("1")
        .setCreationDate(new Date())
        .setSecurityCode(securityCode)
        .build();
  }

  @Test
  public void noCodeAllowsAccess() {
    assertTrue(newTicket(null).checkSecurityCode(null));
  }

  @Test
  public void blankCodeAllowsAccess() {
    Ticket ticket = newTicket("");
    assertNull(ticket.getSecurityCode());
    assertTrue(ticket.checkSecurityCode(null));
  }

  @Test
  public void matchingCodeAllowsAccess() {
    assertTrue(newTicket("ABC123").checkSecurityCode("ABC123"));
  }

  @Test
  public void newCodesAreSaltedAndNotStoredInPlaintext() {
    Ticket first = newTicket("ABC123");
    Ticket second = newTicket("ABC123");
    assertNotNull(first.getSecurityCode());
    assertTrue(first.getSecurityCode().startsWith("pbkdf2-sha256$"));
    assertNotEquals("ABC123", first.getSecurityCode());
    assertNotEquals(first.getSecurityCode(), second.getSecurityCode());
    assertTrue(first.checkSecurityCode("ABC123"));
    assertFalse(first.checkSecurityCode("wrong"));
  }

  @Test
  public void legacyPlaintextCodeStillWorks() {
    Ticket ticket = newTicket(null);
    ticket.securityCode = "OLD123";
    assertTrue(ticket.checkSecurityCode("OLD123"));
    assertFalse(ticket.checkSecurityCode("wrong"));
  }

  @Test
  public void missingCodeDeniesAccess() {
    assertFalse(newTicket("ABC123").checkSecurityCode(null));
  }

  @Test
  public void wrongCodeDeniesAccess() {
    assertFalse(newTicket("ABC123").checkSecurityCode("XYZ"));
  }

  @Test
  public void setterNormalizesBlankCode() {
    Ticket ticket = newTicket("ABC123");
    ticket.setSecurityCode("");
    assertNull(ticket.getSecurityCode());
    assertTrue(ticket.checkSecurityCode(null));
  }

  @Test
  public void legacyBlankCodeAllowsAccess() {
    Ticket ticket = newTicket(null);
    ticket.securityCode = "";
    assertTrue(ticket.checkSecurityCode(null));
  }
}
