/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification.helper;

import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.constant.NotifMediaType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author Yohann Chastagnier
 */
public class NotificationHelper {

  /**
   * Builds a notification data container from a given builder. After that, sends the builded notification
   * @param notificationBuider
   * @return
   */
  public static NotificationMetaData buildAndSend(final INotificationBuider notificationBuider) {
    notificationBuider.build();
    final NotificationMetaData notification = build(notificationBuider);
    send(notification);
    return notification;
  }

  /**
   * Builds a notification data container from a given builder. After that, sends the builded notification for the given
   * media type
   * @param mediaType
   * @param notificationBuider
   * @return
   */
  public static NotificationMetaData buildAndSend(final NotifMediaType mediaType,
      final INotificationBuider notificationBuider) {
    notificationBuider.build();
    final NotificationMetaData notification = build(notificationBuider);
    send(mediaType, notification);
    return notification;
  }

  /**
   * Builds a notification data container from a given builder
   * @param notificationBuider
   * @return
   */
  public static NotificationMetaData build(final INotificationBuider notificationBuider) {
    notificationBuider.build();
    return notificationBuider.getNotification();
  }

  /**
   * Sends the given notification
   * @param notification
   */
  public static void send(final NotificationMetaData notification) {
    send(null, notification);
  }

  /**
   * Sends the given notification for the given media type
   * @param mediaType
   * @param notification
   */
  public static void send(final NotifMediaType mediaType, final NotificationMetaData notification) {
    try {
      final NotificationSender sender = new NotificationSender(notification.getComponentId());
      if (mediaType != null) {
        sender.notifyUser(mediaType.getId(), notification);
      } else {
        sender.notifyUser(notification);
      }
    } catch (final NotificationManagerException e) {
      SilverTrace.warn("notification", "NotificationHelper.send()",
          "notification.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", e);
    }
  }
}
