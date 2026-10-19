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
package org.silverpeas.core.webapi.attachment;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.security.ShareableAccessControl;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.sharing.services.SharingTicketService;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class SharedAttachmentResourceTest {

  @Test
  void zipChecksCodeOnceAndChecksAccessForEveryFile() {
    assertZipAccess(true);
  }

  @Test
  void zipRejectsAFileOutsideTheShare() {
    assertZipAccess(false);
  }

  private void assertZipAccess(boolean secondReadable) {
    SharedAttachmentResource resource = spy(new SharedAttachmentResource());
    HttpRequest request = mock(HttpRequest.class);
    doReturn(request).when(resource).getHttpRequest();
    String token = UUID.randomUUID().toString();
    doReturn(token).when(resource).getToken();
    when(request.getHeader("X-Verification-Code")).thenReturn("correct");
    Ticket ticket = mock(Ticket.class);
    when(ticket.isValid()).thenReturn(true);
    when(ticket.getSecurityCode()).thenReturn("protected");
    when(ticket.getToken()).thenReturn(token);
    when(ticket.checkSecurityCode("correct")).thenReturn(true);
    ShareableAccessControl access = mock(ShareableAccessControl.class);
    when(ticket.getAccessControl()).thenReturn(access);
    when(access.isReadable(any())).thenReturn(true, secondReadable);
    SharingTicketService sharing = mock(SharingTicketService.class);
    when(sharing.getTicket(token)).thenReturn(ticket);
    AttachmentService attachments = mock(AttachmentService.class);
    SimpleDocument first = mock(SimpleDocument.class);
    SimpleDocument second = mock(SimpleDocument.class);
    when(attachments.searchDocumentById(any(SimpleDocumentPK.class), isNull()))
        .thenReturn(first, second);
    when(first.getLastPublicVersion()).thenReturn(first);
    when(second.getLastPublicVersion()).thenReturn(second);
    try (MockedStatic<SharingServiceProvider> sharingProvider =
             mockStatic(SharingServiceProvider.class);
         MockedStatic<AttachmentServiceProvider> attachmentProvider =
             mockStatic(AttachmentServiceProvider.class)) {
      sharingProvider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharing);
      attachmentProvider.when(AttachmentServiceProvider::getAttachmentService).thenReturn(attachments);
      if (secondReadable) {
        try (Response response = resource.zipFiles("1,2")) {
          assertEquals(200, response.getStatus());
        }
      } else {
        WebApplicationException error = assertThrows(WebApplicationException.class,
            () -> resource.zipFiles("1,2"));
        assertEquals(401, error.getResponse().getStatus());
      }
      verify(ticket, times(1)).checkSecurityCode("correct");
      verify(sharing, times(1)).getTicket(token);
      verify(access, times(2)).isReadable(any());
    }
  }
}
