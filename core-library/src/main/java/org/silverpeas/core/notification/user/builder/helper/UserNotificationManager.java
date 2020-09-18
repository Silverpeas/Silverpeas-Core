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
package org.silverpeas.core.notification.user.builder.helper;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;

/**
 * @author: Yohann Chastagnier
 */
@Service
public class UserNotificationManager {

  protected UserNotificationManager() {
    // hidden constructor.
  }

  /**
   * Builds a notification data container from a given builder. After that, sends the builded
   * notification
   * @param notificationBuilder the builder to use to construct the notification.
   */
  public void buildAndSend(final UserNotificationBuilder notificationBuilder) {
    notificationBuilder.build().send();
  }

  /**
   * Builds a notification data container from a given builder. After that, sends the built
   * notification to the specified builtin notification address.
   * @param notificationAddress the address at which the notification should be sent.
   * @param notificationBuilder the builder to use to construct the notification.
   */
  public void buildAndSend(final BuiltInNotifAddress notificationAddress,
      final UserNotificationBuilder notificationBuilder) {
    notificationBuilder.build().send(notificationAddress);
  }

  /**
   * Builds a notification data container from a given builder
   * @param notificationBuilder the builder to use to construct the notification.
   * @return the metadata about the notification to send.
   */
  public NotificationMetaData build(final UserNotificationBuilder notificationBuilder) {
    return notificationBuilder.build().getNotificationMetaData();
  }
}
