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

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
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
    if (spaceId == null || !spaceId.matches("[1-9][0-9]*") ||
        limit == null || !limit.matches("[1-9][0-9]*") ||
        !("true".equals(includeSubspaces) || "false".equals(includeSubspaces))) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    final int count;
    try {
      count = Integer.parseInt(limit);
      if (count > MAX_LIMIT) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
    } catch (NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    request.setAttribute("publications",
        new LatestPublicationsProvider().getPublications(spaceId, controller.getUserId(), count,
            Boolean.parseBoolean(includeSubspaces)));
    response.setContentType("text/html;charset=UTF-8");
    request.getRequestDispatcher("/WEB-INF/jsp/latestPublications.jsp").forward(request, response);
  }
}
