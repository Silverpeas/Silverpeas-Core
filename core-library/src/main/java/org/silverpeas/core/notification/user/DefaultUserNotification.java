/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.notification.user;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.constant.NotifMediaType;
import org.silverpeas.core.notification.user.client.constant.NotifMessageType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
public class DefaultUserNotification implements UserNotification {

  private NotificationMetaData notification;

  public DefaultUserNotification() {
    this(null, null);
  }

  public DefaultUserNotification(final String title, final String content) {
    if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(content)) {
      notification = new NotificationMetaData(NotifMessageType.NORMAL.getId(), title, content);
    } else {
      notification = new NotificationMetaData();
    }
  }

  public DefaultUserNotification(final String title, final Map<String, SilverpeasTemplate> templates,
      final String content) {
    notification = new NotificationMetaData(NotifMessageType.NORMAL.getId(), title, templates, content);
  }

  @Override
  public NotificationMetaData getNotificationMetaData() {
    return notification;
  }

  @Override
  public void send() {
    send(null);
  }

  @Override
  public void send(final NotifMediaType mediaType) {
    if (notification != null) {
      try {
        final NotificationSender sender = new NotificationSender(notification.getComponentId());
        if (mediaType != null) {
          sender.notifyUser(mediaType.getId(), notification);
        } else {
          sender.notifyUser(notification);
        }
      } catch (final NotificationManagerException e) {
        SilverTrace.warn("notification", "IUserNotification.send()",
            "notification.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS",
            "componentId=" + notification.getComponentId(), e);
      }
    }
  }
}
