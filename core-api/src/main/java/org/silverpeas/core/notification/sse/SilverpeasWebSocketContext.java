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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.User;

import javax.websocket.Session;
import java.io.IOException;

import static org.silverpeas.core.util.JSONCodec.encodeObject;

/**
 * This is a wrap of a {@link javax.websocket.Session} instance.
 * <p>
 *   All Server Event requests performed from a WebSocket MUST be wrapped by this implementation
 *   and registered by {@link SilverpeasServerEventContextManager}.
 * </p>
 * @author silveryocha
 */
public class SilverpeasWebSocketContext extends AbstractServerEventContext<Session> {

  private final String requestURI;
  private boolean closed = false;

  /**
   * Hidden constructor.
   */
  SilverpeasWebSocketContext(final SilverpeasServerEventContextManager manager,
      final Session wrappedInstance, final String sessionId, final User user) {
    super(manager, wrappedInstance, sessionId, user);
    requestURI = getWrappedInstance().getRequestURI().toString().replaceFirst("[?].*$", "");
  }

  /**
   * Wraps the given instance. Nothing is wrapped if the given instance is a wrapped one.
   * @param session the instance to wrap.
   * @param userSessionId the identifier or the user session.
   * @param user the identifier of th user linked to the async request.
   * @return the wrapped given instance.
   */
  public static SilverpeasWebSocketContext wrap(Session session, final String userSessionId,
      User user) {
    if (session instanceof SilverpeasWebSocketContext) {
      return (SilverpeasWebSocketContext) session;
    }
    return new SilverpeasWebSocketContext(SilverpeasServerEventContextManager.get(), session,
        userSessionId, user);
  }

  /**
   * Gets the request URI behind the async context.
   * @return a request URI as string.
   */
  @Override
  public String getRequestURI() {
    return requestURI;
  }

  @Override
  public boolean isSendPossible() {
    return safeRead(() -> !closed && getWrappedInstance().isOpen());
  }

  @Override
  public void close() {
    safeWrite(() -> {
      closed = true;
      try {
        if (getWrappedInstance().isOpen()) {
          getWrappedInstance().close();
        }
      } catch (IOException e) {
        SseLogger.get().error(e);
      }
    });
    getManager().unregister(this);
  }

  @Override
  public void performEventSend(final String name, final long id, final String data)
      throws IOException {
    getWrappedInstance().getAsyncRemote()
        .sendObject(encodeObject(o -> o.put("name", name).put("id", id).put("data", data)));
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }
}
