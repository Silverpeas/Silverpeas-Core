/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;

/**
 * Listen user modifications to clone them in Chat server
 *
 * @author remipassmoilesel
 */
@Service
public class ChatUserEventListener extends CDIResourceEventListener<UserEvent> {

  @Inject
  @DefaultChatServer
  private ChatServer server;

  @Override
  public void onCreation(final UserEvent event) {
    UserDetail detail = event.getTransition().getAfter();
    if (server.isAllowed(detail)) {
      server.createUser(detail);
      logger.debug("Chat account have been created for user {0}", detail.getId());
    } else {
      logger.debug("No chat account created for user {0}: " +
              "the user isn't allowed to use the chat service", detail.getId());
    }
  }

  @Override
  public void onDeletion(final UserEvent event) {
    UserDetail detail = event.getTransition().getBefore();
    if (server.isUserExisting(detail)) {
      server.deleteUser(detail);
      logger.debug("Chat account have been deleted for user {0}", detail.getId());
    } else {
      logger.debug("No chat account deleted for user {0}: " +
              "no such account in the chat server", detail.getId());
    }
  }

  @Override
  public boolean isEnabled() {
    return ChatServer.isEnabled();
  }
}
