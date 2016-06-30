/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.SilverpeasAsyncContext;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.Math.min;
import static java.text.MessageFormat.format;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.registerAsyncContext;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask
    .sendLastServerEventsFromId;
import static org.silverpeas.core.notification.sse.SilverpeasAsyncContext.wrap;

/**
 * This abstraction defines the behavior the Servlets in charge of responding to EventSource
 * JavaScript API must have.<br/>
 * Finally, adding a new URI to handle EventSource needs to extends this class and to parametrize
 * the mapping! (see {@link CommonServerSentEventServlet} as an example)
 * @author Yohann Chastagnier
 */
public abstract class SilverpeasServerSentEventServlet extends SilverpeasAuthenticatedHttpServlet {

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {

    final String requestURI = request.getRequestURI();
    final User sessionUser = getMainSessionController(request).getCurrentUserDetail();

    if (!"text/event-stream".equals(request.getHeader("Accept"))) {
      final String errorMessage =
          "Server Sent Servlet accepts only 'test/event-stream' requests ({0})";
      SilverLogger.getLogger(this).error(errorMessage, requestURI);
      throwHttpForbiddenError(errorMessage);
    }

    SilverLogger.getLogger(this)
        .debug("Asking for SSE communication (sessionUser={0}) on URI {1}", sessionUser.getId(),
            requestURI);

    // An initial response
    Long lastServerEventId = null;
    try {
      lastServerEventId = Long.valueOf(request.getHeader("Last-Event-ID"));
    } catch (NumberFormatException ignore) {
    }
    if (lastServerEventId != null) {
      SilverLogger.getLogger(this).debug(
          () -> format("Sending emitted events since disconnection for sessionId {0} on URI {1}",
              request.getSession(false).getId(), requestURI));
      RetryServerEvent.createFor(sessionUser, lastServerEventId)
          .send(request, response, sessionUser);
      lastServerEventId =
          sendLastServerEventsFromId(request, response, lastServerEventId, sessionUser);
    } else {
      InitializationServerEvent.createFor(sessionUser).send(request, response, sessionUser);
    }

    // Start Async processing
    final SilverpeasAsyncContext asyncContext = wrap(request.startAsync(), sessionUser);
    final int timeout = min((request.getSession(false).getMaxInactiveInterval() * 1000), 180000);
    asyncContext.setTimeout(timeout);
    asyncContext.setLastServerEventId(lastServerEventId);
    registerAsyncContext(asyncContext);
  }
}
