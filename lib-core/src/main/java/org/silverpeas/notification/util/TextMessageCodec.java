/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.notification.util;

import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.util.JSONCodec;

/**
 * A codec to encode and decode an event fro and from a text representation dedicated to be
 * transmitted into a text stream. This codec is dedicated to be mainly used in the JMS
 * communication.
 * @author mmoquillon
 */
public class TextMessageCodec {

  /**
   * Encodes the specified event into a text representation ready to be transmitted within a text
   * stream.
   * @param event the event to encode in text message.
   * @return a text message.
   */
  public static final String encode(ResourceEvent event) {
    return JSONCodec.encode(event);
  }

  /**
   * Decodes the specified text representation of an event to a
   * {@code org.silverpeas.notification.ResourceEvent} instance.
   * @param textMessage the text message in which is serialized a resource event.
   * @param eventType the class from which the event should be instanciated during the decoding.
   * @param <T> the type of the resource event.
   * @return a {@code org.silverpeas.notification.ResourceEvent} instance.
   */
  public static final <T extends ResourceEvent> T decode(String textMessage, Class<T> eventType) {
    return JSONCodec.decode(textMessage, eventType);
  }
}
