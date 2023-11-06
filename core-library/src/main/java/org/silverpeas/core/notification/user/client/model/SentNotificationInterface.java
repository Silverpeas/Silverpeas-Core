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
package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.Set;

/**
 * Interface declaration
 */
public interface SentNotificationInterface {

  static SentNotificationInterface get() {
    return ServiceProvider.getService(SentNotificationInterface.class);
  }

  void saveNotifUser(NotificationMetaData metaData, Set<UserRecipient> usersSet)
      throws NotificationException;

  List<SentNotificationDetail> getAllNotifByUser(String userId)
      throws NotificationException;

  SentNotificationDetail getNotification(final String userId, int notifId)
      throws NotificationException;

  void deleteNotif(String userId, int notifId) throws NotificationException;

  void deleteNotifByUser(String userId) throws NotificationException;
}