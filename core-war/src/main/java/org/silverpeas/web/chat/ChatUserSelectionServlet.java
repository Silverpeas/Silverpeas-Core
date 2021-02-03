/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.chat;

import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasHttpServlet;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.web.mvc.controller.MainSessionController
    .MAIN_SESSION_CONTROLLER_ATT;

/**
 * Servlet listening for selection of chat user. It prepares the shareable selection object and
 * passed the control to the user selection panel.
 * @author mmoquillon
 */
public class ChatUserSelectionServlet extends SilverpeasHttpServlet {

  @Inject
  private SynchronizerTokenService tokenService;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    MainSessionController sessionController = getMainSessionController(request);
    if (sessionController != null) {
      Selection selection = sessionController.getSelection();
      selection.resetAll();
      selection.setHostPath(null);
      selection.setMultiSelect(false);
      selection.setHtmlFormName("chat_selected_user");
      selection.setHtmlFormElementId("userId");
      selection.setHtmlFormElementName("userName");
      selection.setElementSelectable(true);
      selection.setSetSelectable(false);
      request.setAttribute("SELECTION", selection);

      Token token = tokenService.getSessionToken(request);
      request.setAttribute(SynchronizerTokenService.SESSION_TOKEN_KEY, token.getValue());

      RequestDispatcher requestDispatcher =
          getServletConfig().getServletContext().getRequestDispatcher(selection.getSelectionURL());
      requestDispatcher.forward(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private MainSessionController getMainSessionController(final HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      return (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
    }
    return null;
  }
}
