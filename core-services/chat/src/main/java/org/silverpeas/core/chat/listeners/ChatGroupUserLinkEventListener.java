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
import org.silverpeas.core.chat.ChatUsersRegistration;
import org.silverpeas.core.notification.system.CDIAfterSuccessfulTransactionResourceEventListener;

import javax.inject.Inject;

/**
 * Listen for the deletion of a group of users. If the deleted group is the one for which the users
 * are allowed to access the chat server, and in the condition the users don't belong to another
 * group through which they can have access the chat server, then the account of those users in the
 * chat server is deleted.
 * @author mmoquillon
 */
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
}
  