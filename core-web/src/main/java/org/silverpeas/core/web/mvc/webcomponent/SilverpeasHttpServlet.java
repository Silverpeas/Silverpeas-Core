/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * This servlet is the parent one of Silverpeas application.
 * It provides common HTTP servlet tools.
 * @author  Yohann Chastagnier
 */
public class SilverpeasHttpServlet extends HttpServlet {

  private static final long serialVersionUID = -2003173095753706593L;

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    try {

      // Perform the request
      super.service(request, response);

    } catch (HttpError httpError) {
      SilverLogger.getLogger(this).debug("http error " + httpError.errorCode, httpError);
      httpError.performResponse(response);
    }
  }

  /**
   * Indicates if it exists an opened user session.
   * @param request the current request.
   * @return true if it exists an opened user session, false otherwise.
   */
  protected UserSessionStatus existOpenedUserSession(final HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      final MainSessionController mainSessionCtrl =
          (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
      final boolean isDesktopControllerInSession = mainSessionCtrl != null;
      final String sessionId = isDesktopControllerInSession
          ? mainSessionCtrl.getSessionId()
          : session.getId();
      if (sessionId != null) {
        SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
        SessionInfo sessionInfo = sessionManagement.getSessionInfo(sessionId);
        final boolean valid =
            sessionInfo.isDefined() && sessionInfo != SessionInfo.AnonymousSession;
        return new UserSessionStatus(valid, isDesktopControllerInSession, sessionInfo);
      }
    }
    return new UserSessionStatus();
  }

  /**
   * Handle the sendRedirect or the forward.
   * @throws ServletException on redirect or forward error.
   * @throws IOException on redirect error.
   */
  protected void redirectOrForwardService(HttpServletRequest request, HttpServletResponse response,
      String destination) throws ServletException, IOException {
    if (destination.startsWith("http") || destination.startsWith("ftp")) {
      response.sendRedirect(response.encodeRedirectURL(destination));
    } else {
      RequestDispatcher requestDispatcher = request.getRequestDispatcher(destination);
      requestDispatcher.forward(request, response);
    }
  }

  /**
   * The precondition given in one or more of the request-header fields evaluated to false when
   * it was tested on the server. This response code allows the client to place preconditions on
   * the current resource metainformation (header field data) and thus prevent the requested
   * method from being applied to a resource other than the one intended.
   */
  protected void throwHttpPreconditionFailedError() {
    throw preconditionFailed("");
  }

  /**
   * The HTTP server understood the request, but is refusing to fulfill it. This status code is
   * commonly used when the server does not wish to reveal exactly why the request has been
   * refused, or when no other response is applicable (for example the server is an Intranet and
   * only the LAN machines are authorized to connect).
   */
  protected void throwHttpForbiddenError() {
    throw forbidden("");
  }

  /**
   * The server has not found anything matching the requested address (URI) ( not found ).
   * This means the URL you have typed or cliked on is wrong or obsolete and does not match any
   * document existing on the server (you may try to gradualy remove the URL components from the
   * right to the left to eventualy retrieve an existing path).
   */
  protected void throwHttpNotFoundError() {
    throw notFound("");
  }

  /**
   * The precondition given in one or more of the request-header fields evaluated to false when
   * it was tested on the server. This response code allows the client to place preconditions on
   * the current resource metainformation (header field data) and thus prevent the requested
   * method from being applied to a resource other than the one intended.
   */
  protected void throwHttpPreconditionFailedError(String message) {
    throw preconditionFailed(message);
  }

  /**
   * The HTTP server understood the request, but is refusing to fulfill it. This status code is
   * commonly used when the server does not wish to reveal exactly why the request has been
   * refused, or when no other response is applicable (for example the server is an Intranet and
   * only the LAN machines are authorized to connect).
   */
  protected void throwHttpForbiddenError(String message) {
    throw forbidden(message);
  }

  /**
   * The server has not found anything matching the requested address (URI) ( not found ).
   * This means the URL you have typed or cliked on is wrong or obsolete and does not match any
   * document existing on the server (you may try to gradualy remove the URL components from the
   * right to the left to eventualy retrieve an existing path).
   */
  protected void throwHttpNotFoundError(String message) {
    throw notFound(message);
  }

  /**
   * Sends an HTTP error to the client with the specified error code. Once the error is sent, the
   * HTTP response is consumed and it cannot be used anymore; so the servlet service should returns
   * directly after invoking this method.
   * @param response the HTTP response
   * @param status the error status code
   */
  protected void sendError(HttpServletResponse response, final int status) {
    sendError(response, status, null);
  }

  /**
   * Sends an HTTP error to the client with the specified error code and error message. Once the
   * error is sent, the HTTP response is consumed and it cannot be used anymore; so the servlet
   * service should returns directly after invoking this method.
   * @param response the HTTP response
   * @param status the error status code
   * @param message the message to pass in the response and giving details about the error.
   */
  protected void sendError(HttpServletResponse response, final int status, final String message) {
    try {
      if (StringUtil.isDefined(message)) {
        response.sendError(status, message);
      } else {
        response.sendError(status);
      }
    } catch (IOException e1) {
      SilverLogger.getLogger(this).error(e1);
    }
  }

  protected HttpError preconditionFailed(final String msg) {
    final HttpError error;
    if (StringUtil.isDefined(msg)) {
      error = new HttpError(HttpServletResponse.SC_PRECONDITION_FAILED, msg);
    } else {
      error = new HttpError(HttpServletResponse.SC_PRECONDITION_FAILED);
    }
    return error;
  }

  protected HttpError forbidden(final String msg) {
    final HttpError error;
    if (StringUtil.isDefined(msg)) {
      error = new HttpError(HttpServletResponse.SC_FORBIDDEN, msg);
    } else {
      error = new HttpError(HttpServletResponse.SC_FORBIDDEN);
    }
    return error;
  }

  protected HttpError notFound(final String msg) {
    final HttpError error;
    if (StringUtil.isDefined(msg)) {
      error = new HttpError(HttpServletResponse.SC_NOT_FOUND, msg);
    } else {
      error = new HttpError(HttpServletResponse.SC_NOT_FOUND);
    }
    return error;
  }

  protected static class UserSessionStatus {
    private final boolean valid;
    private final boolean fromDesktop;
    private SessionInfo info;

    private UserSessionStatus() {
      this(false, false, null);
    }

    private UserSessionStatus(final boolean valid, final boolean fromDesktop,
        final SessionInfo info) {
      this.valid = valid;
      this.fromDesktop = fromDesktop;
      this.info = info;
    }

    public boolean isValid() {
      return valid;
    }

    public boolean isFromDesktop() {
      return fromDesktop;
    }

    public SessionInfo getInfo() {
      return info;
    }
  }

  /**
   * Internal exception class management
   */
  protected class HttpError extends RuntimeException {
    private static final long serialVersionUID = -4303217388313620495L;
    private final int errorCode;
    private final String message;

    HttpError(int errorCode) {
      this.errorCode = errorCode;
      this.message = null;
    }

    HttpError(int errorCode, String message) {
      this.errorCode = errorCode;
      this.message = message;
    }

    void performResponse(HttpServletResponse response) throws IOException {
      response.sendError(errorCode, message);
    }
  }
}
