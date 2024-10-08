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
package org.silverpeas.core.chat;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.util.List;

/**
 * Registration of a user in the remote Chat server.
 *
 * @author mmoquillon
 */
@Service
public class ChatUsersRegistration {

  @Inject
  @DefaultChatServer
  private ChatServer chatServer;
  @Inject
  private RelationShipService relationShipService;
  private final SilverLogger logger = SilverLogger.getLogger(this);

  /**
   * Is the chat service enabled in Silverpeas?
   *
   * @return true if the chat service is enabled and then the users can be registered into the chat
   * service. False otherwise.
   */
  public boolean isChatServiceEnabled() {
    return ChatServer.isEnabled();
  }

  /**
   * Is the specified user already registered into the remote chat server?
   *
   * @param user the user to check the existence.
   * @return true if the user has an account in the remote chat server, false otherwise.
   * @throws ChatServerException a runtime exception if an error occurs while communicating with the
   * remote chat server.
   */
  public boolean isAlreadyRegistered(final User user) {
    return chatServer.isUserExisting(user);
  }

  /**
   * Registers the specified user into the remote chat server. If the user is already registered
   * into the remote chat server, then nothing is performed. If the chat service isn't enabled then
   * nothing is performed. If the Silverpeas domain of the user isn't mapped to a chat domain, then
   * nothing is performed. If the specified user hasn't the right to access the chat service (id est
   * if he doesn't belong to a group allowed to access the chat service in the case this feature is
   * enabled), then nothing is performed.
   * <p>
   * With the user registration, his relationships are also browsed in order to create each of them
   * into the remote chat server. If a user targeted by a relationship hasn't yet an account in the
   * chat server, then he's registered before creating the relationship in the server.
   *
   * @param user the user to register if not yet done.
   * @throws ChatServerException a runtime exception if the registration fails.
   */
  public void registerUser(final User user) {
    if (!isChatServiceAllowed(user)) {
      logger.debug("The user {0} isn't allowed to access the chat service",
          user.getDisplayedName());
    } else if (isAlreadyRegistered(user)) {
      logger.debug("The user {0} is already registered to access the chat service",
          user.getDisplayedName());
    } else {
      logger.debug("Register user {0}", user.getDisplayedName());
      chatServer.createUser(user);
      final List<String> contactIds =
          relationShipService.getMyContactsIds(Integer.parseInt(user.getId()));
      contactIds.stream().map(User::getById)
          .filter(this::isChatServiceAllowed)
          .forEach(c -> {
            registerUser(c);
            logger.debug("Register relationship {0} - {1}", user.getDisplayedName(),
                c.getDisplayedName());
            chatServer.createRelationShip(user, c);
          });
    }
  }

  /**
   * Unregisters the specified user from the remote chat server. If the chat service isn't enabled
   * then nothing is done. If the user isn't registered into the chat service then nothing is done.
   *
   * @param user the user to unregister.
   * @throws ChatServerException a runtime exception if the user cannot be unregistered.
   * @apiNote This is a shortcut of the {@link ChatServer#deleteUser(User)} method but by checking
   * the chat service is enabled and the user has an account on the remote chat service.
   */
  public void unregisterUser(final User user) {
    if (isChatServiceEnabled() && isAlreadyRegistered(user)) {
      logger.debug("Unregister user {0}", user.getDisplayedName());
      chatServer.deleteUser(user);
    }
  }

  private boolean isChatServiceAllowed(final User user) {
    return chatServer.isAllowed(user);
  }
}
