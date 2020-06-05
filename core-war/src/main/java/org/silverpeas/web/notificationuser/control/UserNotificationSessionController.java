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
package org.silverpeas.web.notificationuser.control;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.ComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class UserNotificationSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = 4415724531986026943L;

  private UserNotificationWrapper currentNotification;

  /**
   * Constructor declaration
   */
  public UserNotificationSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.notificationUser.multilang.notificationUserBundle");
    setComponentRootName(URLUtil.CMP_NOTIFICATIONUSER);
  }

  /**
   * Gets the list of users whose the identifiers are specified in the given line.
   * @param userIdsLine a line of underscore-separated user's unique identifiers.
   * @return a list of users.
   */
  public List<User> getUsersFrom(String userIdsLine) {
    final List<User> users;
    if (StringUtil.isDefined(userIdsLine)) {
      final String[] userIds;
      if ("Administrators".equals(userIdsLine)) {
        userIds = getOrganisationController().getAdministratorUserIds(getUserId());
      } else {
        userIds = userIdsLine.split(",");
      }
      users = Stream.of(userIds).map(User::getById).collect(Collectors.toList());
    } else {
      users = Collections.emptyList();
    }
    return users;
  }

  /**
   * Gets the list of groups whose the identifiers are specified in the given line.
   * @param groupIdsLine a line of underscore-separated group's unique identifiers.
   * @return a list of groups.
   */
  public List<Group> getGroupsFrom(String groupIdsLine) {
    final List<Group> groups;
    if (StringUtil.isDefined(groupIdsLine)) {
      final String[] groupIds = groupIdsLine.split(",");
      groups = Stream.of(groupIds).map(Group::getById).collect(Collectors.toList());
    } else {
      groups = Collections.emptyList();
    }
    return groups;
  }

  /**
   * Prepares a user notification from the specified context ready to be customized by the
   * notification editor and then to be sent later.
   * @param context a notification context. It can contain additional parameters specific to a
   * given Silverpeas component instance.
   * @return the user notification that will be sent later.
   */
  public UserNotification prepareNotification(final NotificationContext context) {
    this.currentNotification = supplyUserNotification(context);
    return currentNotification;
  }

  /**
   * Clears any prepared notification.
   */
  public void clearNotification() {
    this.currentNotification = null;
  }

  /**
   * Sends the notification described by the specified context.
   * @param context the context of the notification to send. It contains all the properties
   * required to build the notification and to send it.
   */
  public void sendNotification(final NotificationContext context) {
    final UserNotificationWrapper userNotification = getUserNotification(context);
    final String contributionId = context.containsKey(NotificationContext.PUBLICATION_ID) ? context
        .getPublicationId() : context.getContributionId();
    userNotification.setTitle(context.getTitle())
        .setContent(context.getContent().replaceAll("[\\n\\r\\t]", ""))
        .setAttachmentLinksFor(contributionId)
        .setSender(getUserDetail())
        .setRecipientUsers(context.getAsList("recipientUsers"))
        .setRecipientGroups(context.getAsList("recipientGroups"))
        .setAsManual(context.getAsBoolean("manual"))
        .send();
    WebMessager.getInstance().addSuccess(getString("notification.user.send.success"));
  }

  private UserNotificationWrapper supplyUserNotification(final NotificationContext context) {
    final Optional<ComponentInstanceManualUserNotification> service;
    final String componentId = context.getComponentId();
    if (isDefined(componentId)) {
      service = ComponentInstanceManualUserNotification.get(componentId);
    } else {
      service = Optional.empty();
    }
    final UserNotification manualUserNotification = service
        .orElseGet(() -> ComponentInstanceManualUserNotification
            .get(getComponentRootName())
            .orElseThrow(() -> new SilverpeasRuntimeException(
              "default implementation of ComponentInstanceManualUserNotification is not available!!!")))
        .initializesWith(context);
    return new UserNotificationWrapper(manualUserNotification, getLanguage());
  }

  private UserNotificationWrapper getUserNotification(final NotificationContext context) {
    final UserNotificationWrapper notification;
    if (this.currentNotification == null) {
      notification = supplyUserNotification(context);
    } else {
      notification = this.currentNotification;
    }
    return notification;
  }
}
