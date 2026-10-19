/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.web.sharing.servlets;

import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.model.SimpleFileTicket;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.security.ShareableResource;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.sharing.services.SharingTicketService;

import java.util.Collections;

import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class GetLinkFileServletTest {

  private final GetLinkFileServlet servlet = new GetLinkFileServlet();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final ServletContext context = mock(ServletContext.class);
  private final SharingTicketService sharingService = mock(SharingTicketService.class);
  private final RequestDispatcher invalidTicket = mock(RequestDispatcher.class);
  private final RequestDispatcher invalidCode = mock(RequestDispatcher.class);

  @BeforeEach
  void setUp() throws Exception {
    ServletConfig config = mock(ServletConfig.class);
    when(config.getServletContext()).thenReturn(context);
    servlet.init(config);
    when(request.getRequestURI()).thenReturn("/silverpeas/LinkFile/Key/token/file.pdf");
    when(request.getContextPath()).thenReturn("/silverpeas");
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameterMap()).thenReturn(Collections.emptyMap());
    when(context.getRequestDispatcher("/sharing/jsp/invalidTicket.jsp"))
        .thenReturn(invalidTicket);
    when(context.getRequestDispatcher("/sharing/jsp/invalidSecuriyCode.jsp"))
        .thenReturn(invalidCode);
  }

  @Test
  void missingTicketShowsInvalidTicketPage() throws Exception {
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      servlet.service(request, response);
      verify(invalidTicket).forward(request, response);
      verify(invalidCode, never()).forward(request, response);
    }
  }

  @Test
  void incorrectSecurityCodeReturnsForbidden() throws Exception {
    Ticket ticket = mock(Ticket.class);
    when(sharingService.getTicket("token")).thenReturn(ticket);
    when(request.getHeader("X-Verification-Code")).thenReturn("wrong");
    when(ticket.isValid()).thenReturn(true);
    when(ticket.getSecurityCode()).thenReturn("protected");
    when(ticket.getToken()).thenReturn(java.util.UUID.randomUUID().toString());
    when(ticket.checkSecurityCode("wrong")).thenReturn(false);
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      servlet.service(request, response);
      verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
      verify(invalidCode, never()).forward(request, response);
      verify(invalidTicket, never()).forward(request, response);
    }
  }

  @Test
  void expiredTicketWithCorrectCodeShowsInvalidTicketPage() throws Exception {
    Ticket ticket = mock(Ticket.class);
    when(sharingService.getTicket("token")).thenReturn(ticket);
    when(request.getHeader("X-Verification-Code")).thenReturn("correct");
    when(ticket.checkSecurityCode("correct")).thenReturn(true);
    when(ticket.isValid()).thenReturn(false);
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      servlet.service(request, response);
      verify(invalidTicket).forward(request, response);
      verify(invalidCode, never()).forward(request, response);
    }
  }
  @Test
  void missingCodeReturnsUnauthorized() throws Exception {
    Ticket ticket = mock(Ticket.class);
    when(sharingService.getTicket("token")).thenReturn(ticket);
    when(ticket.isValid()).thenReturn(true);
    when(ticket.getSecurityCode()).thenReturn("protected");
    when(ticket.getToken()).thenReturn(java.util.UUID.randomUUID().toString());
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      servlet.service(request, response);
      verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  @Test
  void repeatedFailuresLockEvenCorrectCode() throws Exception {
    Ticket ticket = mock(Ticket.class);
    when(sharingService.getTicket("token")).thenReturn(ticket);
    when(ticket.isValid()).thenReturn(true);
    when(ticket.getSecurityCode()).thenReturn("protected");
    when(ticket.getToken()).thenReturn(java.util.UUID.randomUUID().toString());
    when(request.getHeader("X-Verification-Code")).thenReturn("wrong");
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      for (int i = 0; i < 5; i++) {
        servlet.service(request, response);
      }
      when(request.getHeader("X-Verification-Code")).thenReturn("correct");
      when(ticket.checkSecurityCode("correct")).thenReturn(true);
      servlet.service(request, response);
      verify(response, times(4)).sendError(HttpServletResponse.SC_FORBIDDEN);
      verify(response, times(2)).sendError(429);
      verify(ticket, never()).checkSecurityCode("correct");
      verifyNoInteractions(invalidCode);
    }
  }

  private void assertSuccessfulDownload(String requiredCode, String contentType) throws Exception {
    Path file = Files.createTempFile("silverpeas-sharing-", ".txt");
    byte[] content = "shared content".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    try (ByteArrayOutputStream downloaded = new ByteArrayOutputStream()) {
      Files.write(file, content);
      SimpleFileTicket ticket = mock(SimpleFileTicket.class);
      @SuppressWarnings("unchecked")
      ShareableResource<SimpleDocument> resource = mock(ShareableResource.class);
      SimpleDocument document = mock(SimpleDocument.class);
      when(sharingService.getTicket("token")).thenReturn(ticket);
      when(ticket.isValid()).thenReturn(true);
      when(ticket.getSecurityCode()).thenReturn(requiredCode);
      when(ticket.getToken()).thenReturn(java.util.UUID.randomUUID().toString());
      when(ticket.checkSecurityCode(requiredCode)).thenReturn(true);
      when(ticket.getResource()).thenReturn(resource);
      when(resource.getAccessedObject()).thenReturn(document);
      when(document.getAttachmentPath()).thenReturn(file.toString());
      when(document.getContentType()).thenReturn(contentType);
      when(document.getFilename()).thenReturn("shared.txt");
      when(document.getSize()).thenReturn((long) content.length);
      when(request.getHeader("X-Verification-Code")).thenReturn(requiredCode);
      when(request.getRemoteAddr()).thenReturn("127.0.0.1");
      when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
        @Override
        void write(int value) {
          downloaded.write(value);
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        void setWriteListener(WriteListener listener) {
          // This synchronous test stream does not support asynchronous writes.
        }
      });

      try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
        provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
        servlet.service(request, response);
      }

      assertArrayEquals(content, downloaded.toByteArray());
      verify(response).setContentType(contentType);
      verify(response).setHeader("Content-Length", String.valueOf(content.length));
      verify(response).setHeader("Content-Disposition", "inline; filename=\"shared.txt\"");
      ArgumentCaptor<DownloadDetail> download = ArgumentCaptor.forClass(DownloadDetail.class);
      verify(sharingService).addDownload(download.capture());
      assertEquals("127.0.0.1", download.getValue().getUserIP());
      verify(invalidTicket, never()).forward(request, response);
      verify(invalidCode, never()).forward(request, response);
    } finally {
      Files.deleteIfExists(file);
    }
  }

  @Test
  void downloadWithoutSecurityCodeSucceedsAndIsCounted() throws Exception {
    assertSuccessfulDownload(null, "text/plain");
  }

  @Test
  void downloadWithValidSecurityCodeSucceedsAndIsCounted() throws Exception {
    assertSuccessfulDownload("correct", "text/plain");
  }

  @Test
  void protectedHtmlFileCanBeDownloaded() throws Exception {
    assertSuccessfulDownload("correct", "text/html");
  }

}
