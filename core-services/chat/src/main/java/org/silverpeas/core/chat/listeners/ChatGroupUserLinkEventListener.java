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
 * FLOSS exception. You should have received a copy of the text describing
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
import org.silverpeas.core.admin.user.notification.GroupUserLinkEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.chat.ChatUsersRegistration;
import org.silverpeas.core.notification.system.CDIAfterSuccessfulTransactionResourceEventListener;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Inject;

/**
 * Listens for adding or removing of users in user groups. When a user is in a group for which the
 * chat service is enabled, then he's also registered into the remote chat service (in the case he's
 * not yet registered). In counterpart, if the user is removed from a group for which the chat
 * service is enabled, then, and only if he doesn't belong to another group for which the chat
 * service is enabled, he's automatically unregistered from the chat service.
 *
 * @author mmoquillon
 */
@Technical
@Service
public class ChatGroupUserLinkEventListener extends
    CDIAfterSuccessfulTransactionResourceEventListener<GroupUserLinkEvent> {

  @Inject
  private ChatUsersRegistration registration;

  @Override
  public void onCreation(final GroupUserLinkEvent event) {
    final String userId = event.getTransition().getAfter().getUserId();
    final User user = User.getById(userId);
    registration.registerUser(user);
  }

  @Override
  public void onDeletion(GroupUserLinkEvent event) {
    String userId = event.getTransition().getBefore().getUserId();
    ChatUser user = ChatUser.fromUser(User.getById(userId));

    if (!user.isChatEnabled()) {
      registration.unregisterUser(user);
    }
  }
}
  