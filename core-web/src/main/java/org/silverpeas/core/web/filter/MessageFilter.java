/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.filter;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.notification.message.Message;
import org.silverpeas.core.notification.message.MessageContainer;
import org.silverpeas.core.notification.message.MessageListener;
import org.silverpeas.core.notification.message.MessageManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * This filter initializes all the necessary stuff to use easily the notification message API
 * from anywhere in the application.
 * @author Yohann Chastagnier
 */
public class MessageFilter implements Filter {
  /**
   * The HTTP header paremeter that contains the registred key of messages.
   */
  public static final String HTTP_MESSAGEKEY = "X-Silverpeas-MessageKey";

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      final HttpServletRequest httpRequest = (HttpServletRequest) request;
      final HttpServletResponse httpResponse = (HttpServletResponse) response;

      // Initializing the manager of messages associated to the current request
      String registredKey = MessageManager.initialize();
      MessageManager
          .addListener(new RequestMessageListener(httpRequest, httpResponse, registredKey));
      try {

        chain.doFilter(request, response);
        return;

      } finally {

        // Remove message container if no message registred
        if (MessageManager.getMessageContainer(registredKey).getMessages().isEmpty()) {
          MessageManager.clear(registredKey);
          httpResponse.setHeader(HTTP_MESSAGEKEY, null);
        }

        // Remove from cache the registred key of messages (if any)
        MessageManager.destroy();
      }
    }

    // Executed if rules of the filter are not satisfied
    chain.doFilter(request, response);
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    // Nothing to do.
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    // Nothing to do.
  }

  /**
   * Listener of messages.
   */
  private class RequestMessageListener implements MessageListener {
    private final HttpServletRequest httpRequest;
    private final HttpServletResponse httpResponse;
    private final String registredKeyOfMessages;

    public RequestMessageListener(final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse, final String registredKeyOfMessages) {
      this.httpRequest = httpRequest;
      this.httpResponse = httpResponse;
      this.registredKeyOfMessages = registredKeyOfMessages;
    }

    @Override
    public void beforeGetLanguage(final MessageContainer container) {
      HttpSession session = httpRequest.getSession(false);
      if (session != null) {
        MainSessionController mainSessionCtrl =
            (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
        if (mainSessionCtrl != null) {
          container.setLanguage(mainSessionCtrl.getFavoriteLanguage());
        }
      }
    }

    @Override
    public void beforeAddMessage(final MessageContainer container, final Message message) {
      // Nothing to do for now.
    }

    @Override
    public void afterMessageAdded(final MessageContainer container, final Message message) {
      httpResponse.setHeader(HTTP_MESSAGEKEY, registredKeyOfMessages);
    }
  }
}
