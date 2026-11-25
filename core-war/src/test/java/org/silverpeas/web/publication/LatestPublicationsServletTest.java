/*
 * Copyright (C) 2000 - 2026 Silverpeas
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package org.silverpeas.web.publication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.io.IOException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LatestPublicationsServletTest {

  private final LatestPublicationsServlet servlet = new LatestPublicationsServlet();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);

  @Test
  void rejectsAnOversizedLimit() throws Exception {
    HttpSession session = mock(HttpSession.class);
    MainSessionController controller = mock(MainSessionController.class);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT))
        .thenReturn(controller);
    when(controller.getUserId()).thenReturn("1");
    when(request.getParameter("spaceId")).thenReturn("123");
    when(request.getParameter("limit")).thenReturn("101");
    when(request.getParameter("includeSubspaces")).thenReturn("false");

    servlet.doGet(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  void returnsInternalServerErrorWhenSendingAnErrorFails() throws Exception {
    IOException cause = new IOException("Connection closed");
    doThrow(cause).when(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);

    servlet.doGet(request, response);

    verify(response).resetBuffer();
    verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  void leavesACommittedResponseIntactWhenSendingAnErrorFails() throws Exception {
    when(response.isCommitted()).thenReturn(true);
    doThrow(new IOException("Connection closed")).when(response)
        .sendError(HttpServletResponse.SC_UNAUTHORIZED);

    servlet.doGet(request, response);

    verify(response, never()).resetBuffer();
    verify(response, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }
}
