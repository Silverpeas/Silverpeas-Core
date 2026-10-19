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
