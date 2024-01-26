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
import org.silverpeas.core.notification.sse.ServerEvent;
import org.silverpeas.core.notification.sse.SilverpeasServerEventContext;
import org.silverpeas.core.notification.sse.SilverpeasServerEventContextManager;
import org.silverpeas.core.notification.sse.SseLogger;
import org.silverpeas.kernel.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.getLastServerEventsFromId;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.registerContext;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.isCheckPreviousAsyncContextEnabled;

/**
 * Centralizing common code between the different kind of WEB access.
 * @author Yohann Chastagnier
 */
interface SilverpeasServerEventWebAccess {

  default void prepareEventsOnOpening(final WebAccessContext wac, final Long initialLastServerEventId) {
    final ServerEvent serverEvent;
    Long lastServerEventId = initialLastServerEventId;
    final String userSessionId = wac.getUserSessionId();
    if (lastServerEventId != null) {
      SseLogger.get()
          .debug(() -> format(
              "Sending emitted events since disconnection for sessionId {0} on URI {1}",
              wac.getSessionId(), wac.getRequestURI()));
      serverEvent = RetryServerEvent.createFor(userSessionId, lastServerEventId);
      final Pair<Long, List<ServerEvent>> result = getLastServerEventsFromId(lastServerEventId);
      lastServerEventId = result.getFirst();
      wac.getNotConsumedServerEvent().addAll(result.getSecond());
    } else {
      serverEvent = InitializationServerEvent.createFor(userSessionId);
    }
    wac.setLastServerEventId(lastServerEventId);
    wac.setInitialServerEvent(serverEvent);
  }

  default void send(final WebAccessContext wac, final SilverpeasServerEventContext context)
      throws IOException {
    final String sessionId = context.getSessionId();
    final User user = context.getUser();
    final ServerEvent initialServerEvent = wac.getInitialServerEvent();
    initialServerEvent.send(context, sessionId, user);
    for (ServerEvent toSendAgain : wac.getNotConsumedServerEvent()) {
      boolean sent = toSendAgain.send(context, sessionId, user);
      if (sent) {
        SseLogger.get().debug(() -> format("Send of not consumed {0}", toSendAgain));
      }
    }
    if (isCheckPreviousAsyncContextEnabled()) {
      // trying to close previous opened SSE connexion
      Optional.of(initialServerEvent)
          .filter(InitializationServerEvent.class::isInstance)
          .stream()
          .flatMap(s -> SilverpeasServerEventContextManager.get().getContextSnapshot().stream())
          .filter(c -> sessionId.equals(c.getSessionId()))
          .forEach(SilverpeasServerEventContext::closeOnPreviousCheckFailure);
    }
    registerContext(context);
  }

  class WebAccessContext {
    private final String requestUri;
    private final String sessionId;
    private final String userSessionId;
    private final User user;
    private Long lastServerEventId = null;
    private final List<ServerEvent> notConsumedServerEvent = new ArrayList<>();
    private ServerEvent initialServerEvent = null;

    WebAccessContext(final String requestUri, final String sessionId, final String userSessionId, final User user) {
      this.requestUri = requestUri;
      this.sessionId = sessionId;
      this.userSessionId = userSessionId;
      this.user = user;
    }

    public String getRequestURI() {
      return requestUri;
    }

    public String getSessionId() {
      return sessionId;
    }

    public String getUserSessionId() {
      return userSessionId;
    }

    public User getUser() {
      return user;
    }

    public Long getLastServerEventId() {
      return lastServerEventId;
    }

    public void setLastServerEventId(final Long lastServerEventId) {
      this.lastServerEventId = lastServerEventId;
    }

    public List<ServerEvent> getNotConsumedServerEvent() {
      return notConsumedServerEvent;
    }

    public ServerEvent getInitialServerEvent() {
      return initialServerEvent;
    }

    public void setInitialServerEvent(final ServerEvent initialServerEvent) {
      this.initialServerEvent = initialServerEvent;
    }
  }
}
