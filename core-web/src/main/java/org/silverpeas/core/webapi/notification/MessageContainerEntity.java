/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.notification;

import org.silverpeas.core.notification.message.Message;
import org.silverpeas.core.notification.message.MessageContainer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageContainerEntity {

  @XmlElement
  private Collection<MessageEntity> messages;

  /**
   * Creates a new message container entity from the specified message container.
   * @param messageContainer
   * @return the entity representing the specified message container.
   */
  public static MessageContainerEntity createFrom(final MessageContainer messageContainer) {
    MessageContainerEntity entity = new MessageContainerEntity();
    if (messageContainer != null) {
      for (Message message : messageContainer.getMessages()) {
        entity.addMessage(MessageEntity.createFrom(message));
      }
    }
    return entity;
  }

  /**
   * Default hidden constructor.
   */
  protected MessageContainerEntity() {
    messages = new LinkedList<MessageEntity>();
  }

  protected MessageContainerEntity addMessage(MessageEntity message) {
    if (message != null) {
      messages.add(message);
    }
    return this;
  }

  protected Collection<MessageEntity> getMessages() {
    return messages;
  }
}
