package org.silverpeas.web.chat;

import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
public class ChatUserSelectionServlet extends HttpServlet {

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
