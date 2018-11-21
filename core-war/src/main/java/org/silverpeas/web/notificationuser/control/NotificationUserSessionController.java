/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.web.notificationuser.Notification;

public class NotificationUserSessionController extends AbstractComponentSessionController {

  /* parameters of a notification */
  private Notification notification = null;

  /**
   * Constructor declaration
   */
  public NotificationUserSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.notificationUser.multilang.notificationUserBundle");
    setComponentRootName(URLUtil.CMP_NOTIFICATIONUSER);
  }

  public Notification resetNotification() {
    notification = new Notification();
    return notification;
  }

  /**
   * @param notification the notification to send
   * @throws NotificationException thrown on error
   */
  @SuppressWarnings("StatementWithEmptyBody")
  public void sendMessage(Notification notification) throws NotificationException {
    NotificationSender notifSender = new NotificationSender(null);
    NotificationMetaData notifMetaData = notification.toNotificationMetaData();
    notifMetaData.setSender(getUserId());
    notifMetaData.setSource(getString("manualNotification"));
    if (getNotification() != null && getNotification().getUsers().isEmpty() &&
        getNotification().getGroups().isEmpty()) {
      // The selection container has been set from the user panel, so the notification must be
      // tagged as a manuel one.
      notifMetaData.manualUserNotification();
    } else {
      // The user panel has not been displayed and the receiver container has been set
      // automatically, so the notification is not tagged as a manual one (and centralized
      // verifications will be skipped).
    }

    notifSender.notifyUser(notification.getChannel(), notifMetaData);
  }

  private static String[] lineToArray(String src) {
    final String[] result;
    if (StringUtil.isNotDefined(src)) {
      result = new String[0];
    } else {
      result = src.split("_");
    }
    return result;
  }

  public Notification initTargets(String theTargetsUsers, String theTargetsGroups) {
    Notification notif = resetNotification();
    notif.setUsers(initTargetsUsers(theTargetsUsers));
    notif.setGroups(initTargetsGroups(theTargetsGroups));
    return notif;
  }

  private String[] initTargetsUsers(String theTargetsUsers) {
    String[] idUsers = new String[0];
    if (StringUtil.isDefined(theTargetsUsers)) {
      if ("Administrators".equals(theTargetsUsers)) {
        idUsers = getOrganisationController().getAdministratorUserIds(getUserId());
      } else {
        idUsers = lineToArray(theTargetsUsers);
      }
    }
    return idUsers;
  }

  private String[] initTargetsGroups(String theTargetsGroups) {
    String[] idGroups = new String[0];
    if (theTargetsGroups != null && theTargetsGroups.length() > 0) {
      idGroups = lineToArray(theTargetsGroups);
    }
    return idGroups;
  }

  /**
   * Gets the notification used to initialize data on page.
   * @return the notification data.
   */
  public Notification getNotification() {
    return notification;
  }
}
