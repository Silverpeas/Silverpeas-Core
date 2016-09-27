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

package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.function.Function;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * An event implying data the server has to push to client without the client requesting
 * Silverpeas. This event is called {@code Server Event}.<br/>
 * Three elements compose a server event:
 * <ul>
 *   <li>{@code id}, an identifier that identifies the event</li>
 *   <li>{@code name}, the name of the event. It can be taken like the name of a bus. This name
 *   is used by client to add listeners. For example, il the name of an event is {@code
 *   USER_SESSION} the client (in JavaScript) can listen to by coding
 *   <pre>
 *     new EventSource(...).addEventListener('USER_SESSION', function(serverEvent){
 *       ...
 *     })
 *   </pre>
 *   </li>
 *   <li>{@code data}, textual data (but it is not forbidden to render JSON structure as string)
 *   </li>
 * </ul>
 * @author Yohann Chastagnier
 */
public interface ServerEvent {

  int CLIENT_RETRY = 5000;

  /**
   * Gets the identifier of the server event.<br/>
   * This identifier is unique during the lifetime of the server. After the server is restarted, the
   * counter starts again to zero.
   * @return the name as unique {@link ServerEventName} instance.
   */
  Long getId();

  /**
   * Gets the name of the server event.
   * @return the name as unique {@link ServerEventName} instance.
   */
  ServerEventName getName();

  /**
   * The data to send.
   * @return the data as string.
   * @param receiver the receiver eventually used by dynamic data producers (see
   * {@link AbstractServerEvent#withData(Function)}).
   */
  String getData(final User receiver);

  /**
   * Indicates if the given receiver is concerned by the event. If not, the event is not sent.
   * @param receiver a potential receiver.
   * @return true if given receiver is concerned, false otherwise.
   */
  default boolean isConcerned(User receiver) {
    return true;
  }

  /**
   * Sends the event by using the given response and taking into account the receiver linked to
   * .<br/>
   * If {@link #isConcerned(User)} indicates that the given receiver is not concerned, nothing is
   * sent.
   * @param request
   * @param response the response on which the event will be pushed.
   * @param receiver the user concerned. The {@link User} instance is given to the
 * {@link #getData(User)} method in order to produce dynamic data according to the receiver.
   */
  default void send(final HttpServletRequest request, HttpServletResponse response, final User receiver) throws IOException {
    if (!isConcerned(receiver)) {
      return;
    }

    // Configuring the response
    response.setContentType("text/event-stream");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Connection", "keep-alive");
    response.setCharacterEncoding("UTF-8");

    final String eventName = defaultStringIfNotDefined(getName().asString());
    final String eventData = defaultStringIfNotDefined(getData(receiver));
    final int capacity = 100 + eventName.length() + eventData.length();
    StringBuilder sb = new StringBuilder(capacity);
    sb.append("retry: ").append(CLIENT_RETRY);
    sb.append("\nid: ").append(getId());
    if (StringUtil.isDefined(eventName)) {
      sb.append("\nevent: ").append(eventName);
    }
    if (StringUtil.isDefined(eventData)) {
      sb.append("\ndata: ");
      for (int i = 0; i < eventData.length(); i++) {
        char currentChar = eventData.charAt(i);
        switch (currentChar) {
          case '\n':
            sb.append("\ndata: ");
            break;
          default:
            sb.append(currentChar);
        }
      }
    }
    sb.append("\n\n");
    response.getWriter().append(sb.toString());
    response.flushBuffer();
  }

  @FunctionalInterface
  interface ServerEventName {
    String asString();
  }
}
