/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.SilverpeasWebSocketContext;
import org.silverpeas.core.notification.sse.SseLogger;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Collections;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterBySessionId;
import static org.silverpeas.core.notification.sse.SilverpeasWebSocketContext.wrap;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.getWebSocketSendTimeout;

/**
 * This abstraction defines the behavior the Servlets in charge of responding to EventSource
 * JavaScript API must have.<br>
 * Finally, adding a new URI to handle EventSource needs to extends this class and to parametrize
 * the mapping! (see {@link CommonServerSentEventServlet} as an example)
 * @author Yohann Chastagnier
 */
public abstract class SilverpeasServerEventWebSocket
    implements SilverpeasServerEventWebAccess {

  private static final String LAST_EVENT_ID_PARAM = "Last-Event-ID";

  /**
   * This method is called one time only, when {@link #onOpen(Session)} is executed.
   * @param session a WebSocket session instance.
   * @return a {@link SessionInfo} instance.
   */
  protected abstract SessionInfo getSessionInfo(final Session session);

  @OnOpen
  public void onOpen(final Session session) throws IOException {
    final SilverLogger silverLogger = SseLogger.get();
    final String requestURI = session.getRequestURI().toString();
    final String sessionId = session.getId();
    final SessionInfo spSessionInfo = getSessionInfo(session);
    if (!spSessionInfo.isDefined() || spSessionInfo.isAnonymous()) {
      silverLogger.debug(
          "Asking for SSE websocket communication from an unhandled session (sessionId={0}) on URI {1}",
          sessionId, requestURI);
      session.close();
      return;
    }
    session.getAsyncRemote().setSendTimeout(getWebSocketSendTimeout());
    final User sessionUser = spSessionInfo.getUser();
    final String userSessionId = spSessionInfo.getSessionId();
    silverLogger.debug("Asking for SSE websocket communication (sessionUser={0}) on URI {1} (SessionId={2})",
        sessionUser.getId(), requestURI, userSessionId);
    // Preparing initial response
    final Long lastServerEventId = session.getRequestParameterMap()
        .getOrDefault(LAST_EVENT_ID_PARAM, Collections.emptyList())
        .stream()
        .filter(StringUtil::isLong)
        .map(Long::parseLong)
        .findFirst()
        .orElse(null);
    final WebAccessContext wac = new WebAccessContext(requestURI, sessionId, userSessionId, sessionUser);
    prepareEventsOnOpening(wac, lastServerEventId);
    // Send initial events
    final SilverpeasWebSocketContext websocketContext = wrap(session, userSessionId, sessionUser);
    try {
      send(wac, websocketContext);
      silverLogger.debug("{0} SSE websockets currently opened (from session opening)",
          session.getOpenSessions().size());
    } catch (IOException | IllegalStateException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @OnClose
  public void onClose(Session session) {
    final SilverLogger silverLogger = SseLogger.get();
    unregisterBySessionId(session.getId());
    silverLogger.debug("{0} SSE websockets currently opened (from session closing)",
        session.getOpenSessions().size() - 1);
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    SseLogger.get().warn(format("Error {1} with session {0} ", session.getId(), throwable));
  }

  @OnMessage
  public void onMessage(Session session, String message) {
    SseLogger.get().warn(format("Message {1} received with session {0} ", session.getId(), message));
  }
}
