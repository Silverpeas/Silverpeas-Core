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
package org.silverpeas.web.notificationuser.servlets;

import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.notificationuser.control.UserNotificationSessionController;

import javax.servlet.annotation.WebServlet;
import java.util.Enumeration;

import static org.silverpeas.core.admin.user.model.User.getCurrentRequester;

@WebServlet()
public class UserNotificationRequestRouter
    extends ComponentRequestRouter<UserNotificationSessionController> {

  private static final long serialVersionUID = -5858231857279380747L;
  private static final String RECIPIENT_EDITION_PARAM = "recipientEdition";
  private static final String SIMPLE_DETAILS_PARAM = "simpleDetailsWhenRecipientTotalExceed";
  private static final String RECIPIENT_USERS = NotificationContext.RECIPIENT_USERS;
  private static final String RECIPIENT_GROUPS = NotificationContext.RECIPIENT_GROUPS;
  private static final String MESSAGE_TITLE = "title";
  private static final String MAIN_FUNCTION = "Main";
  private static final String SENDING_FUNCTION = "SendNotif";
  private static final String RELEASE_FUNCTION = "ClearNotif";

  @Override
  public UserNotificationSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new UserNotificationSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getSessionControlBeanName() {
    return "userNotification";
  }

  /**
   * Compute a destination page.
   * @param function The entering request function (ex : "Main.jsp")
   * @param nuSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, UserNotificationSessionController nuSC,
      HttpRequest request) {
    String destination;
    try {
      request.setCharacterEncoding("UTF-8");
      if (function.startsWith(MAIN_FUNCTION)) {
        final NotificationContext context = getNotificationContext(request);
        UserNotification notification = nuSC.prepareNotification(context);
        final String title = notification.getNotificationMetaData().getTitle(nuSC.getLanguage());
        request.setAttribute(MESSAGE_TITLE, title);
        String recipientUsers = request.getParameter(RECIPIENT_USERS);
        String recipientGroups = request.getParameter(RECIPIENT_GROUPS);
        if (recipientUsers != null || recipientGroups != null) {
          request.setAttribute(RECIPIENT_USERS, nuSC.getUsersFrom(recipientUsers));
          request.setAttribute(RECIPIENT_GROUPS, nuSC.getGroupsFrom(recipientGroups));
        }
        request.setAttribute(NotificationContext.COMPONENT_ID, context.getComponentId());
        request.setAttribute(NotificationContext.RESOURCE_ID, context.getResourceId());
        final String param = request.getParameter(RECIPIENT_EDITION_PARAM);
        final boolean areRecipientsEditable;
        if (StringUtil.isDefined(param)) {
          areRecipientsEditable = StringUtil.getBooleanValue(param);
        } else {
          areRecipientsEditable = true;
        }
        SettingBundle settings = ResourceLocator
            .getSettingBundle("org.silverpeas.notificationManager.settings.notificationManagerSettings");
        request.setAttribute(SIMPLE_DETAILS_PARAM, settings.getInteger("notif.manual.ui.simpleDetails.whenRecipientTotalExceed", 15));
        request.setAttribute(RECIPIENT_EDITION_PARAM, areRecipientsEditable);
        destination = "/userNotification/jsp/notificationSender.jsp";
      } else if (SENDING_FUNCTION.equals(function)) {
        final NotificationContext context = getNotificationContext(request);
        nuSC.sendNotification(context);
        nuSC.clearNotification();
        destination = "/peasCore/jsp/close.jsp";
      } else if (RELEASE_FUNCTION.equals(function)) {
        nuSC.clearNotification();
        destination = "/peasCore/jsp/close.jsp";
      } else {
        destination = "/userNotification/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private NotificationContext getNotificationContext(final HttpRequest request) {
    final NotificationContext context = new NotificationContext(getCurrentRequester());
    Enumeration<String> parameters = request.getParameterNames();
    while (parameters.hasMoreElements()) {
      final String name = parameters.nextElement();
      context.put(name, request.getParameter(name));
    }
    return context;
  }
}
