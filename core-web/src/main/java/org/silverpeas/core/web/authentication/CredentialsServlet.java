/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.core.web.authentication.credentials.*;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.Defined;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller tier for credential management (called by MandatoryQuestionChecker)
 *
 * @author Ludovic Bertin
 */
public class CredentialsServlet extends HttpServlet implements HttpFunctionHandlerRegistering {

  private static final long serialVersionUID = -7586840606648226466L;
  private final Map<String, HttpFunctionHandler> handlers = new HashMap<>(20);
  private final List<String> preProcessingByPassers = new ArrayList<>();

  @Inject
  private Instance<CredentialsFunctionHandler> handlerInstances;

  @PostConstruct
  void loadHandlers() {
    handlerInstances.forEach(h -> h.registerWith(this));
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      CredentialsProcessContext context = new CredentialsProcessContext(request);
      HttpFunctionHandler handler = handlers.get(context.getFunction());
      if (handler != null) {
        preHandlerProcessing(request, context);

        if (!context.isProcessed()) {
          String nextPage = handler.doAction(request);
          context.setDestination(nextPage);
        }

        postHandlerProcessing(request, response, context);
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
            "command not found : " + context.getFunction());
      }
    } catch (ServletException | IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void preHandlerProcessing(HttpServletRequest request, CredentialsProcessContext context) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier((User) null)
        .clearSession(request);
    if (StringUtil.isDefined(login) && StringUtil.isDefined(domainId) &&
        !preProcessingByPassers.contains(context.getFunction())) {
      // Verify that the user can login
      UserCanLoginVerifier userStateVerifier = AuthenticationUserVerifierFactory
          .getUserCanLoginVerifier(getAuthenticationCredential(login, domainId));
      context.setUser(userStateVerifier.getUser());
      context.setDestination(checkUserState(userStateVerifier));
    }
  }

  private void postHandlerProcessing(HttpServletRequest request, HttpServletResponse response,
      CredentialsProcessContext context) throws IOException, ServletException {
    String destinationPage = context.getDestination();
    if (destinationPage.startsWith("http")) {
      AuthenticationUserVerifierFactory
          .getUserCanTryAgainToLoginVerifier(context.getUser())
          .clearCache();
      response.sendRedirect(response.encodeRedirectURL(destinationPage));
    } else {
      RequestDispatcher dispatcher = request.getRequestDispatcher(destinationPage);
      dispatcher.forward(request, response);
    }
  }

  private String checkUserState(final UserCanLoginVerifier userStateVerifier) {
    try {
      userStateVerifier.verify();
    } catch (AuthenticationException e) {
      SilverLogger.getLogger(this).debug(e.getMessage(), e);
      return userStateVerifier.getErrorDestination();
    }
    return "";
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  private AuthenticationCredential getAuthenticationCredential(@Defined final String login,
      @Defined final String domain) {
    try {
      return AuthenticationCredential.newWithAsLogin(login).withAsDomainId(domain);
    } catch (AuthenticationException e) {
      // shouldn't be thrown
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public void register(HttpFunctionHandler handler, boolean bypassPreHandleProcessing) {
    handlers.put(handler.getFunction(), handler);
    if (bypassPreHandleProcessing) {
      preProcessingByPassers.add(handler.getFunction());
    }
  }

  private static class CredentialsProcessContext {
    private String destination;
    private User user;
    private final String function;

    private CredentialsProcessContext(HttpServletRequest request) {
      this.function = getFunction(request);
    }

    public String getFunction() {
      return function;
    }

    public boolean isProcessed() {
      return StringUtil.isDefined(destination);
    }

    public String getDestination() {
      return destination;
    }

    public void setDestination(String destination) {
      this.destination = destination;
    }

    public User getUser() {
      return user;
    }

    public void setUser(User user) {
      this.user = user;
    }

    /**
     * Retrieves function from path info.
     *
     * @param request HTTP request
     * @return the function as a String
     */
    private static String getFunction(HttpServletRequest request) {
      String function = "Error";
      String pathInfo = request.getPathInfo();
      if (pathInfo != null) {
        // remove first '/'
        pathInfo = pathInfo.substring(1);
        function = pathInfo.substring(pathInfo.indexOf('/') + 1);
      }
      return function;
    }
  }
}
