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
package org.silverpeas.core.webapi.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.SilverpeasWebSocketContext;
import org.silverpeas.core.notification.sse.SseLogger;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.web.token.SilverpeasWebTokenService;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Collections;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterBySessionId;
import static org.silverpeas.core.notification.sse.SilverpeasWebSocketContext.wrap;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.getWebSocketSendTimeout;
import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;

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
   * Gets the name of the parameter representing the token generated for current authenticated
   * user and passed in the URI of the WebSocket endpoint.
   * <p>
   *   For example:
   *   With the {@code @ServerEndpoint("/ws/{token}/sse/common")} declaration, "token" MUST be
   *   returned by this method.
   * </p>
   * @return a string representing the parameter name for the token.
   */
  protected abstract String getTokenParameterName();

  /**
   * This method is called one time only, when {@link #onOpen(Session)} is executed.
   * @param wsSession a WebSocket session instance.
   * @return a {@link SessionInfo} instance.
   */
  protected SessionInfo getSessionInfo(final Session wsSession) {
    return ofNullable(wsSession.getPathParameters().get(getTokenParameterName()))
        .flatMap(SilverpeasWebTokenService.get()::consumeIdentifierBy)
        .map(getSessionManagement()::getSessionInfo)
        .orElse(SessionInfo.NoneSession);
  }

  @OnOpen
  public void onOpen(final Session wsSession) throws IOException {
    final SilverLogger silverLogger = SseLogger.get();
    final String requestURI = wsSession.getRequestURI().toString();
    final String wsSessionId = wsSession.getId();
    final SessionInfo spSessionInfo = getSessionInfo(wsSession);
    if (!spSessionInfo.isDefined() || spSessionInfo.isAnonymous()) {
      silverLogger.debug(
          "Asking for SSE websocket communication from an unhandled session (sessionId={0}) on URI {1}",
          wsSessionId, requestURI);
      wsSession.close();
      return;
    }
    wsSession.getAsyncRemote().setSendTimeout(getWebSocketSendTimeout());
    final User sessionUser = spSessionInfo.getUser();
    final String userSessionId = spSessionInfo.getSessionId();
    silverLogger.debug("Asking for SSE websocket communication (sessionUser={0}) on URI {1} (SessionId={2})",
        sessionUser.getId(), requestURI, userSessionId);
    // Preparing initial response
    final Long lastServerEventId = wsSession.getRequestParameterMap()
        .getOrDefault(LAST_EVENT_ID_PARAM, Collections.emptyList())
        .stream()
        .filter(StringUtil::isLong)
        .map(Long::parseLong)
        .findFirst()
        .orElse(null);
    final WebAccessContext wac = new WebAccessContext(requestURI, wsSessionId, userSessionId, sessionUser);
    prepareEventsOnOpening(wac, lastServerEventId);
    // Send initial events
    final SilverpeasWebSocketContext websocketContext = wrap(wsSession, userSessionId, sessionUser);
    try {
      send(wac, websocketContext);
      silverLogger.debug("{0} SSE websockets currently opened (from session opening)",
          wsSession.getOpenSessions().size());
    } catch (IOException | IllegalStateException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @OnClose
  public void onClose(Session wsSession) {
    final SilverLogger silverLogger = SseLogger.get();
    unregisterBySessionId(wsSession.getId());
    silverLogger.debug("{0} SSE websockets currently opened (from session closing)",
        wsSession.getOpenSessions().size() - 1);
  }

  @OnError
  public void onError(Session wsSession, Throwable throwable) {
    SseLogger.get().warn(format("Error {1} with session {0} ", wsSession.getId(), throwable));
  }

  @OnMessage
  public void onMessage(Session wsSession, String message) {
    SseLogger.get().warn(format("Message {1} received with session {0} ", wsSession.getId(), message));
  }
}
