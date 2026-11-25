package org.silverpeas.web.publication;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.publication.LatestPublicationsProvider;

import java.io.IOException;

@WebServlet("/LatestPublications")
public class LatestPublicationsServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final int MAX_LIMIT = 100;
  private static final String POSITIVE_INTEGER = "[1-9]\\d*";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    try {
      response.setHeader("Cache-Control", "no-store");
      var session = request.getSession(false);
      var controller = session == null ? null : (MainSessionController)
          session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      if (controller == null || controller.getUserId() == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      String spaceId = request.getParameter("spaceId");
      String limit = request.getParameter("limit");
      String includeSubspaces = request.getParameter("includeSubspaces");
      if (spaceId == null || !spaceId.matches(POSITIVE_INTEGER) ||
          limit == null || !limit.matches(POSITIVE_INTEGER) ||
          !("true".equals(includeSubspaces) || "false".equals(includeSubspaces))) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      final int count;
      try {
        count = Integer.parseInt(limit);
      } catch (NumberFormatException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      if (count > MAX_LIMIT) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      request.setAttribute("publications",
          new LatestPublicationsProvider().getPublications(spaceId, controller.getUserId(), count,
              Boolean.parseBoolean(includeSubspaces)));
      response.setContentType("text/html;charset=UTF-8");
      request.getRequestDispatcher("/WEB-INF/jsp/latestPublications.jsp").forward(request, response);
    } catch (IOException | ServletException e) {
      throw new ServletException("Unable to send the latest publications response", e);
    }
  }
}
