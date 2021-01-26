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
package org.silverpeas.web.chat;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chat.ChatUsersRegistration;
import org.silverpeas.core.notification.user.SimpleUserNotification;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.MissingResourceException;

/**
 * Registers all the Silverpeas users in the remote chat server.
 * @author mmoquillon
 */
public class ChatUserRegistrationServlet extends SilverpeasAuthenticatedHttpServlet {

  private static final String CHAT_I18N_BUNDLE = "org.silverpeas.chat.multilang.chat";
  private static final String REGISTRATION_STATUS = "chat.registration.status";
  private static final String SUCCESS_MESSAGE = "chat.registration.ok";
  private static final String FAILURE_MESSAGE = "chat.registration.nok";
  private static final String REGISTRATION_ACCEPTED = "chat.registration.ongoing";

  @Inject
  private ChatUsersRegistration registration;

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
    final User requester = User.getCurrentRequester();
    final LocalizationBundle messages =
        getLocalizedBundle(requester.getUserPreferences().getLanguage());
    ManagedThreadPool.getPool().invoke(() -> {
      try {
        registerAllUsers();
        notify(requester, SUCCESS_MESSAGE);
      } catch (MissingResourceException e) {
        SilverLogger.getLogger(this).error(e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (Exception e) {
        SilverLogger.getLogger(ChatUserRegistrationServlet.class).error(e);
        notify(requester, FAILURE_MESSAGE);
      }
    });
    WebMessager.getInstance().addInfo(messages.getString(REGISTRATION_ACCEPTED));
  }

  private void registerAllUsers() {
    UserDetail.getAll().stream()
        .filter(user -> !user.isAccessGuest() && !user.isAnonymous())
        .forEach(user -> registration.registerUser(user));
  }

  private void notify(final User receiver, final String messageKey) {
    SimpleUserNotification.fromSystem()
        .withTitle(l -> getLocalizedBundle(l).getString(REGISTRATION_STATUS))
        .andMessage(l -> getLocalizedBundle(l).getString(messageKey))
        .toUsers(receiver)
        .send();
  }

  private LocalizationBundle getLocalizedBundle(final String language) {
    return ResourceLocator.getLocalizationBundle(CHAT_I18N_BUNDLE, language);
  }
}
