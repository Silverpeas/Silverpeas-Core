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
package org.silverpeas.core.chat.servers;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

/**
 * This interface represents a Chat server. An implementation of this interface has to implement the
 * access to a concrete chat server. Configuration available in
 * <code>Silverpeas-Core/core-configuration/src/main/config/
 * properties/org/silverpeas/chat/settings/chat.properties</code>
 * @author remipassmoilesel
 */
public interface ChatServer {

  static ChatServer get() {
    return ServiceProvider.getService(ChatServer.class, DefaultChatServer.Literal.INSTANCE);
  }

  /**
   * Gets the settings on the chat service. These settings provide the endpoint definitions as well
   * all the required parameters to communicate correctly with the remote chat server.
   * @return a bundle of settings on the chat service.
   */
  static ChatSettings getChatSettings() {
    return ChatSettings.get();
  }

  /**
   * Is a chat server enabled? A chat server is enabled if there is a chat server defined for
   * Silverpeas and the chat service is explicitly enabled. The definition of a chat server and the
   * activation of the chat service are both done through the properties file
   * {@code org/silverpeas/chat/settings/chat.properties}.
   * @return true if both the chat service is enabled and a chat server is defined in the Silverpeas
   * configuration. False otherwise.
   */
  static boolean isEnabled() {
    return getChatSettings().isChatEnabled();
  }

  /**
   * Creates an account in the chat server for the specified user. The user login in lower case
   * (without any domain part if any) is used as the chat login and the API token is used as
   * password. Be caution with email addresses used as login because they contain a domain part and
   * domain parts are not supported in login by chat servers. Before creating the account, all
   * domain part or so such interpreted, are first removed from the user login.
   * @param user a Silverpeas user.
   * @throws ChatServerException if an error occurs while creating the user in the chat server.
   */
  void createUser(final User user);

  /**
   * Deletes in the chat server the account of the specified user.
   * @param user a Silverpeas user.
   * @throws ChatServerException if an error occurs while deleting the user in the chat server.
   */
  void deleteUser(final User user);

  /**
   * Creates a relationship between the two specified user in the chat server. If the relationship
   * already exists, does nothing.
   * @param user1 a Silverpeas user.
   * @param user2 another Silverpeas user.
   * @throws ChatServerException if an error occurs while creating a relationship between the two
   * users in the chat server.
   */
  void createRelationShip(final User user1, final User user2);

  /**
   * Deletes the relationship existing between the two specified user in the chat server.
   * @param user1 a Silverpeas user.
   * @param user2 another Silverpeas user.
   * @throws ChatServerException if an error occurs while deleting a relationship between the two
   * users in the chat server.
   */
  void deleteRelationShip(final User user1, final User user2);

  /**
   * Is the specified user has already an account in the chat server?
   * @param user a Silverpeas user.
   * @return true if the user has an account in the chat server, false otherwise.
   * @throws ChatServerException if an error occurs while communicating with the chat server.
   */
  boolean isUserExisting(final User user);

  /**
   * Is the specified Silverpeas domain is supported by the chat server? The domain is supported if
   * it exists a mapping between it and a setting in the chat server. In that case, any users in the
   * domain can be registered and retrieved in the chat server.
   * @param domainId the unique identifier of a user domain in Silverpeas.
   * @return true if the domain is supported by the chat server.
   */
  default boolean isUserDomainSupported(final String domainId) {
    final ChatSettings settings = getChatSettings();
    return StringUtil.isDefined(settings.getMappedXmppDomain(domainId));
  }

  /**
   * Is the specified user is allowed to access the chat service? A guest user and the anonymous
   * user isn't allowed to access the chat service. For others constrains on the access, please
   * consult the documentation of {@link ChatUser#isChatEnabled()} about the conditions a user has
   * to satisfy to allow his access on the chat server.
   * @param user a user in Silverpeas
   * @return true if the user is allowed to access the chat server, false otherwise.
   */
  default boolean isAllowed(final User user) {
    ChatUser chatUser = ChatUser.fromUser(user);
    // take into account the case of the anonymous user account creation: it should be a guest.
    return !user.isAnonymous() && !user.isAccessGuest() && chatUser.isChatEnabled();
  }

}
