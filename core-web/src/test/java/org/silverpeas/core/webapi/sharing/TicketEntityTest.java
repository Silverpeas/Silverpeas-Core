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
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.sharing.model.SimpleFileTicket;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.model.TicketDetail;
import org.silverpeas.core.sharing.model.TicketFactory;
import org.mockito.MockedStatic;

import java.net.URI;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TicketEntityTest {

  private TicketEntity newTicketEntity(String code) {
    TicketEntity entity = new TicketEntity();
    entity.setComponentId("kmelia1");
    entity.setSharedObjectId(12);
    entity.setSharedObjectType(Ticket.FILE_TYPE);
    entity.securityCode = code;
    return entity;
  }

  private Ticket createTicket(TicketEntity entity) throws Exception {
    UserDetail user = mock(UserDetail.class);
    when(user.getId()).thenReturn("42");
    try (MockedStatic<TicketFactory> factory = mockStatic(TicketFactory.class)) {
      factory.when(() -> TicketFactory.aTicket(any(TicketDetail.class)))
          .thenAnswer(invocation -> SimpleFileTicket.builder(
              invocation.getArgument(0, TicketDetail.class)).build());
      return entity.toTicket(user);
    }
  }

  @Test
  void securityCodeIsHashedWhenCreatingTicket() throws Exception {
    Ticket ticket = createTicket(newTicketEntity("ABC123"));
    assertNotNull(ticket);
    assertNotEquals("ABC123", ticket.getSecurityCode());
    assertTrue(ticket.checkSecurityCode("ABC123"));
    assertFalse(ticket.checkSecurityCode("incorrect"));
  }

  @Test
  void noSecurityCodeCreatesUnprotectedTicket() throws Exception {
    Ticket ticket = createTicket(newTicketEntity(null));
    assertNotNull(ticket);
    assertTrue(ticket.checkSecurityCode(null));
  }

  @Test
  void responseEntityNeverExposesSecurityCode() {
    Ticket ticket = SimpleFileTicket.builder()
        .setSharedObjectId(12)
        .setComponentId("kmelia1")
        .setCreatorId("42")
        .setCreationDate(new Date())
        .setSecurityCode("ABC123")
        .build();

    TicketEntity entity = TicketEntity.fromTicket(ticket, URI.create("https://example.org/sharing"));
    assertNull(entity.securityCode);
  }
}
