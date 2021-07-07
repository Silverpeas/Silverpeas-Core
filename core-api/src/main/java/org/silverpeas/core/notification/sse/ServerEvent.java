/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * An event implying data the server has to push to client without the client requesting
 * Silverpeas. This event is called {@code Server Event}.<br>
 * Three elements compose a server event:
 * <ul>
 * <li>{@code id}, an identifier that identifies the event</li>
 * <li>{@code name}, the name of the event. It can be taken like the name of a bus. This name
 * is used by client to add listeners. For example, il the name of an event is {@code
 * USER_SESSION} the client (in JavaScript) can listen to by coding
 * <pre>
 *     new EventSource(...).addEventListener('USER_SESSION', function(serverEvent){
 *       ...
 *     })
 *   </pre>
 * </li>
 * <li>{@code data}, textual data (but it is not forbidden to render JSON structure as string)
 * </li>
 * </ul>
 * @author Yohann Chastagnier
 */
public interface ServerEvent {

  int CLIENT_RETRY = 5000;

  /**
   * Gets the identifier of the server event.<br>
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
   * Gets the sub type the event if any.
   * @return the sub type of the event if any, empty string otherwise.
   */
  default String subType() {
    return EMPTY;
  }

  /**
   * The data to send.
   * @param receiverSessionId the identifier of the receiver session.
   * @param receiver the user that will receive the event in its WEB client.
   * @return the data as string.
   */
  String getData(final String receiverSessionId, final User receiver);

  /**
   * Indicates if the given receiver (behind the session) is concerned by the event.
   * If not, the event is not sent.
   * @param receiverSessionId the identifier of the receiver session.
   * @param receiver the user that will receive the event in its WEB client.
   * @return true if given receiver is concerned, false otherwise.
   */
  default boolean isConcerned(final String receiverSessionId, final User receiver) {
    return true;
  }

  /**
   * Sends the event by using the given response and taking into account the receiver linked to.
   * <br>
   * If {@link #isConcerned(String, User)} indicates that the given receiver is not concerned, nothing is
   * sent.
   * @param request the request from which the communication has been opened.
   * @param response the response on which the event will be pushed.
   * @param receiverSessionId the identifier of the receiver session.
   * @param receiver the receiver instance.
   * @return true if send has been performed, false otherwise.
   * @throws IOException if the sending fails.
   */
  default boolean send(final HttpServletRequest request, HttpServletResponse response,
      final String receiverSessionId, final User receiver) throws IOException {
    if (!isConcerned(receiverSessionId, receiver)) {
      return false;
    }

    final String eventName = defaultStringIfNotDefined(getName().asString());
    final String eventData = defaultStringIfNotDefined(getData(receiverSessionId, receiver));
    final int capacity = 100 + eventName.length() + eventData.length();
    StringBuilder sb = new StringBuilder(capacity);
    sb.append("retry: ").append(CLIENT_RETRY);
    sb.append("\nid: ").append(getId());
    if (StringUtil.isDefined(eventName)) {
      sb.append("\nevent: ").append(eventName);
    }
    sb.append("\ndata: ");
    if (StringUtil.isDefined(eventData)) {
      for (int i = 0; i < eventData.length(); i++) {
        char currentChar = eventData.charAt(i);
        if (currentChar == '\n') {
          sb.append("\ndata: ");
        } else {
          sb.append(currentChar);
        }
      }
    }
    sb.append("\n\n");
    final PrintWriter writer = response.getWriter();
    writer.append(sb.toString());
    writer.flush();
    return true;
  }

  @FunctionalInterface
  interface ServerEventName {
    String asString();
  }
}
