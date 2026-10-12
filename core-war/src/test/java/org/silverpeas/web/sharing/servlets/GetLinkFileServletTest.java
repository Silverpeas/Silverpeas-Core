/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.web.sharing.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.sharing.services.SharingTicketService;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class GetLinkFileServletTest {

  private final GetLinkFileServlet servlet = new GetLinkFileServlet();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final ServletContext context = mock(ServletContext.class);
  private final SharingTicketService sharingService = mock(SharingTicketService.class);
  private final RequestDispatcher invalidTicket = mock(RequestDispatcher.class);
  private final RequestDispatcher invalidCode = mock(RequestDispatcher.class);

  @Before
  public void setUp() throws Exception {
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
  public void missingTicketShowsInvalidTicketPage() throws Exception {
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      servlet.service(request, response);
      verify(invalidTicket).forward(request, response);
      verify(invalidCode, never()).forward(request, response);
    }
  }

  @Test
  public void incorrectSecurityCodeShowsInvalidCodePage() throws Exception {
    Ticket ticket = mock(Ticket.class);
    when(sharingService.getTicket("token")).thenReturn(ticket);
    when(request.getHeader("X-Verification-Code")).thenReturn("wrong");
    when(ticket.checkSecurityCode("wrong")).thenReturn(false);
    try (MockedStatic<SharingServiceProvider> provider = mockStatic(SharingServiceProvider.class)) {
      provider.when(SharingServiceProvider::getSharingTicketService).thenReturn(sharingService);
      servlet.service(request, response);
      verify(invalidCode).forward(request, response);
      verify(invalidTicket, never()).forward(request, response);
    }
  }

  @Test
  public void expiredTicketWithCorrectCodeShowsInvalidTicketPage() throws Exception {
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
}
