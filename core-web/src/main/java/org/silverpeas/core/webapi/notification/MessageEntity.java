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
import org.silverpeas.core.notification.message.MessageType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageEntity {

  /* Type of the content */
  @XmlElement
  private MessageType type;

  /* The content */
  @XmlElement
  private String content;

  /* The content */
  @XmlElement
  private long displayLiveTime;

  /**
   * Creates a new content entity
   * @param message
   * @return the entity representing the specified content.
   */
  public static MessageEntity createFrom(Message message) {
    return new MessageEntity(message);
  }

  /**
   * Default hidden constructor.
   * @param message
   */
  private MessageEntity(Message message) {
    this.type = message.getType();
    this.content = message.getContent();
    this.displayLiveTime = message.getDisplayLiveTime();
  }

  protected MessageEntity() {
  }

  protected MessageType getType() {
    return type;
  }

  protected String getContent() {
    return content;
  }

  protected long getDisplayLiveTime() {
    return displayLiveTime;
  }
}
