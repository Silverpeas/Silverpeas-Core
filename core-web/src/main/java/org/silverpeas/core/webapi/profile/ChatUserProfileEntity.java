/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.chat.servers.ChatServer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The profile of a user that is registered into a chat service.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ChatUserProfileEntity extends UserProfileEntity {

  private final ChatUser chatUser;

  /**
   * Decorates the specified user details with the required WEB exposition features.
   *
   * @param user the user details to decorate.
   * @return a web entity representing the profile of a user.
   */
  public static ChatUserProfileEntity fromUser(final ChatUser user) {
    return new ChatUserProfileEntity(user);
  }

  private ChatUserProfileEntity(final ChatUser user) {
    super(user);
    this.chatUser = user;
  }

  /**
   * Gets the identifier of the user in the chat service.
   * @return the chat identifier of the user.
   */
  @XmlElement
  public String getChatId() {
    return this.chatUser.getChatId();
  }

  /**
   * Is the chat service is enabled? It is a short hand to check the chat service is enabled in
   * the Javascript code in the web client side.
   * @return true if the chat service is enabled and this user can chat with others users in
   * Silverpeas.
   */
  @XmlElement
  public boolean isChatEnabled() {
    return ChatServer.isEnabled();
  }
}
