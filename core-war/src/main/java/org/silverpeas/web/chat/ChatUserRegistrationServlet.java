/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.chat;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chat.ChatUsersRegistration;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.builder.AbstractUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifMediaType;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.MissingResourceException;

/**
 * Registers all the Silverpeas users in the remote chat server.
 * @author mmoquillon
 */
public class ChatUserRegistrationServlet extends HttpServlet {

  private static final String CHAT_I18N_BUNDLE = "org.silverpeas.chat.multilang.chat";
  private static final String REGISTRATION_STATUS = "chat.registration.status";
  private static final String SUCCESS_MESSAGE = "chat.registration.ok";
  private static final String FAILURE_MESSAGE = "chat.registration.nok";
  private static final String REGISTRATION_ACCEPTED = "chat.registration.ongoing";

  @Inject
  private ChatUsersRegistration registration;
  @Resource
  private ManagedExecutorService executor;
  private LocalizationBundle messages = ResourceLocator.getLocalizationBundle(CHAT_I18N_BUNDLE);

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    final User requester = User.getCurrentRequester();
    executor.execute(() -> {
      try {
        registerAllUsers();
        notify(requester, messages.getString(SUCCESS_MESSAGE));
      } catch (MissingResourceException e) {
        throw e;
      } catch (Exception e) {
        SilverLogger.getLogger(ChatUserRegistrationServlet.class).error(e);
        notify(requester, messages.getString(FAILURE_MESSAGE));
      }
    });

    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    PrintWriter body = resp.getWriter();
    body.println(messages.getString(REGISTRATION_ACCEPTED));
    body.flush();
  }

  private void registerAllUsers() {
    UserDetail.getAll().stream()
        .filter(user -> !user.isAccessGuest() && !user.isAnonymous())
        .forEach(user -> registration.registerUser(user));
  }

  private void notify(final User user, final String message) {
    UserNotification notification = new RegistrationStatusNotificationBuilder(
        messages.getString(REGISTRATION_STATUS),
        message,
        user).build();
    notification.send(NotifMediaType.DEFAULT);
  }

  private static class RegistrationStatusNotificationBuilder extends AbstractUserNotificationBuilder {

    private final User requester;

    public RegistrationStatusNotificationBuilder(final String title, final String content,
        final User requester) {
      super(title, content);
      this.requester = requester;
    }

    @Override
    protected boolean isUserSubscriptionNotificationEnabled() {
      return false;
    }

    @Override
    protected NotifAction getAction() {
      return NotifAction.REPORT;
    }

    @Override
    protected String getComponentInstanceId() {
      return null;
    }

    @Override
    protected String getSender() {
      return this.requester.getId();
    }

    @Override
    protected Collection<String> getUserIdsToNotify() {
      return Arrays.asList(this.requester.getId());
    }

    @Override
    protected void performBuild() {
      // nothing specific to build
    }

    @Override
    protected boolean isSendImmediatly() {
      return true;
    }
  }
}
