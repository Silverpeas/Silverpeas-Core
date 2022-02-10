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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.chat.listeners;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.ChatUsersRegistration;
import org.silverpeas.core.security.authentication.UserAuthenticationListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This listener handles the chat initialization.<br>
 * Just after a successful user authentication, doing some stuffs about the chat.
 */
@Service
@Singleton
public class ChatUserAuthenticationListener implements UserAuthenticationListener {

  private static final String CHAT_ATTRIBUTE = "Silverpeas.Chat";

  private final ChatUsersRegistration chatUsersRegistration;

  @Inject
  public ChatUserAuthenticationListener(final ChatUsersRegistration chatUsersRegistration) {
    this.chatUsersRegistration = chatUsersRegistration;
  }

  @Override
  public String firstHomepageAccessAfterAuthentication(HttpServletRequest request, User user,
      String finalURL) {
      HttpSession session = request.getSession();
    session.setAttribute(CHAT_ATTRIBUTE,
        chatUsersRegistration.isChatServiceEnabled() && user != null && !user.isAnonymous() &&
            !user.isAccessGuest());
    return null;
  }
}
