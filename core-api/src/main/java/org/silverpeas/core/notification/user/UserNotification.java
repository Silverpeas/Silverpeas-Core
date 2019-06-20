/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.notification.user;

import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;

/**
 * A notification to a user. It is the main entry point in the User Notification API.
 * A {@link UserNotification} object can be obtained by the
 * {@link org.silverpeas.core.notification.user.builder.UserNotificationBuilder} object.
 * @author Yohann Chastagnier
 */
public interface UserNotification {

  /**
   * Gets the metadata about the notification to send so that all the properties required to
   * send the notification can be set.
   * @return the notification metadata.
   */
  NotificationMetaData getNotificationMetaData();

  /**
   * Sends this notification at the specified addresses declared within the metadata of this
   * notification.
   */
  void send();

  /**
   * Sends this notification to the specified builtin user notification address.
   * @param notificationAddress the type of the media to vehicle the notification.
   */
  void send(BuiltInNotifAddress notificationAddress);
}
