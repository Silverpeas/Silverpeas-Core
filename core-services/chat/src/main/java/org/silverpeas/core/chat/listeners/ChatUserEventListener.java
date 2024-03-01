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
package org.silverpeas.core.chat.listeners;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Inject;

/**
 * Listen user modifications to clone them in Chat server
 * @author remipassmoilesel
 */
@Technical
@Service
public class ChatUserEventListener extends CDIResourceEventListener<UserEvent> {

  @Inject
  @DefaultChatServer
  private ChatServer server;

  /**
   * A new user has been created in Silverpeas, then creates his account in the remote chat
   * service. No account should be existing for this user in the chat service, otherwise an error
   * is raised and the user account creation in Silverpeas fails.
   * @param event the event on the creation of a resource.
   */
  @Override
  public void onCreation(final UserEvent event) {
    User user = event.getTransition().getAfter();
    if (server.isAllowed(user)) {
      server.createUser(user);
      logger.debug("Chat account have been created for user {0}", user.getId());
    } else {
      logger.debug("No chat account created for user {0}: " +
          "the user isn't allowed to use the chat service", user.getId());
    }
  }

  /**
   * An existing user in Silverpeas has been deleted, then the account of the deleted user in
   * the remote chat service is also deleted.
   * @param event the event on the deletion of a resource.
   */
  @Override
  public void onDeletion(final UserEvent event) {
    User user = event.getTransition().getBefore();
    if (server.isUserExisting(user)) {
      server.deleteUser(user);
      logger.debug("Chat account have been deleted for user {0}", user.getId());
    } else {
      logger.debug("No chat account deleted for user {0}: " +
          "no such account in the chat server", user.getId());
    }
  }

  @Override
  public boolean isEnabled() {
    return ChatServer.isEnabled();
  }
}
