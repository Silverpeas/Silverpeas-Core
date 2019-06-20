/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * It is an abstract implementation of a {@link ServerEvent}. It defines the common properties all
 * the concrete events should have. A concrete resource event can extend this class to inherit the
 * basic properties without to implement them by itself.<br>
 * This abstraction handles the identifier of the event which is auto-generated. The start of
 * identifier counting is reset after each server start or restart
 * @author Yohann Chastagnier
 */
public abstract class AbstractServerEvent implements ServerEvent {

  private static long idCounter = 0;
  private long id = -1;
  private String data = StringUtil.EMPTY;
  private BiFunction<String, User, String> dynamicData = null;

  /**
   * Gets the Event Source URI on which the event is handled.<br>
   * If empty, then the event must be sent on all Event Sources.
   * @return an URI as string.
   */
  public List<String> getEventSourceURIs() {
    return Collections.emptyList();
  }

  @Override
  public Long getId() {
    if (id == -1) {
      id = idCounter++;
    }
    return id;
  }

  @Override
  public String getData(final String receiverSessionId, final User receiver) {
    return dynamicData == null ? data : dynamicData.apply(receiverSessionId, receiver);
  }

  @Override
  public boolean send(final HttpServletRequest request, final HttpServletResponse response,
      final String receiverSessionId, final User receiver) throws IOException {
    List<String> eventSourceURIs = getEventSourceURIs();
    boolean aimedEventSource = eventSourceURIs.isEmpty();
    if (!aimedEventSource) {
      for (String eventSourceURI : eventSourceURIs) {
        if (request.getRequestURI().endsWith(eventSourceURI)) {
          aimedEventSource = true;
          break;
        }
      }
    }
    return aimedEventSource &&
        ServerEvent.super.send(request, response, receiverSessionId, receiver);
  }

  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this, SHORT_PREFIX_STYLE);
    tsb.append("id", getId());
    tsb.append("name", getName().asString());
    return tsb.toString();
  }

  /**
   * Sets the specified data.<br>
   * Given data are ignored if {@link #withData(BiFunction)} has been called with non null
   * functional
   * interface.
   * @param <T> a subtype of AbstractServerEvent
   * @param data the data the event must return to WEB client.
   * @return the instance itself.
   */
  @SuppressWarnings("unchecked")
  protected <T extends AbstractServerEvent> T withData(String data) {
    this.data = data;
    return (T) this;
  }

  /**
   * Sets a functional interface which will produced the data as string by taking into account a
   * given {@link User} which is the current user for which the server event will be send. @param
   * dynamicData functional interface which will be played at each call of {@link
   * ServerEvent#send(HttpServletRequest, HttpServletResponse, String, User)} method. The functional
   * interface provides one
   * parameter: {@link User}, the user for which the server event will be sent. It produces the
   * data to send.<br>
   * If a functional interface is given, data eventually set by {@link #withData(String)} are
   * ignored.
   * @param <T> a subtype of AbstractServerEvent
   * @param dynamicData the data the vent must return to WEB client.
   * @return the instance itself.
   */
  @SuppressWarnings("unchecked")
  protected <T extends AbstractServerEvent> T withData(
      BiFunction<String, User, String> dynamicData) {
    this.dynamicData = dynamicData;
    return (T) this;
  }
}
