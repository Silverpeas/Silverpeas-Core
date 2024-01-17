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

import java.io.IOException;

/**
 * Definition of a Server Event context.
 * <p>
 *   Servent Event can be requested from different kinds of WEB context:
 *   <ul>
 *     <li>SSE, Server Sent Event, which is using HTTP protocol</li>
 *     <li>WebSocket, which is using the websocket one. Technically, on client side, a WebSocket
 *     MUST be created to mimic an EventSource behavior.</li>
 *   </ul>
 * </p>
 * <p>
 *   This interface allows to handle these contexts by Silverpeas's Server Event API, regardless
 *   the WEB context.
 * </p>
 * @author silveryocha
 */
public interface SilverpeasServerEventContext {

  /**
   * Gets the request URI from which the context has been initialized (HTTP or WEBSOCKET).
   * @return a string representing a URI.
   */
  String getRequestURI();

  /**
   * Gets the session identifier linked to the context.
   * <p>
   *   Be careful, according to the WEB context, the session identifier is not necessarily the
   *   JSESSIONID.
   * </p>
   * @return a session identifier as string.
   */
  String getSessionId();

  /**
   * Gets the user identifier linked to the context.
   * @return a user identifier as string.
   */
  User getUser();

  /**
   * Sets the last server event identifier known before a network breakdown.
   * @param lastServerEventId a long representing a unique identifier (from server starting).
   */
  void setLastServerEventId(Long lastServerEventId);

  /**
   * Gets the last server event identifier known before a network breakdown.
   * @return an identifier as string.
   */
  Long getLastServerEventId();

  /**
   * Closes the context.
   * <p>
   *   It will be removed from the management context handled by
   *   {@link SilverpeasServerEventContextManager}.
   * </p>
   */
  void close();

  /**
   * Indicates if the sending is yet possible.
   * @return true if possible, false otherwise.
   */
  boolean isSendPossible();

  /**
   * Sends the given using elements of the context.
   * <p>
   *   If sending has not been done, then context is removed from the context manager.
   * </p>
   * @param name the name of the event.
   * @param id the unique identifier of the event (from the start of the server).
   * @param data the data sent with the event.
   * @return true if send has been done, false otherwise.
   */
  boolean sendEvent(String name, final long id, String data) throws IOException;

  /**
   * Performs a check on the context and close it if a failure is detected.
   * <p>
   *   By default, noting is performed.
   * </p>
   */
  default void closeOnPreviousCheckFailure() {
  }

  /**
   * Sends 'heartbeat' event if it has been indicated to the context that it MUST have theis
   * behavior.
   * <p>
   *   This is useful for old WEB browser implementations which do not implement entirely server
   *   event API.
   * </p>
   * <p>
   *   By default, noting is performed.
   * </p>
   */
  default void sendHeartbeatIfEnabled() {
  }
}
