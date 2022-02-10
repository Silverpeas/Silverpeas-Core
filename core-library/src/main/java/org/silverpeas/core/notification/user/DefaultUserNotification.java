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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.notification.user.client.constant.NotifMessageType;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
public class DefaultUserNotification implements UserNotification {

  private NotificationMetaData notification;

  public DefaultUserNotification() {
    this(null, null);
  }

  public DefaultUserNotification(final NotificationMetaData metaData) {
    this.notification = metaData;
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
  public void send(final BuiltInNotifAddress notificationAddress) {
    final NotificationMetaData notifMetaData = getNotificationMetaData();
    if (notifMetaData != null) {
      try {
        final NotificationSender sender = new NotificationSender(notifMetaData.getComponentId());
        if (notificationAddress != null) {
          sender.notifyUser(notificationAddress.getId(), notifMetaData);
        } else {
          sender.notifyUser(notifMetaData);
        }
      } catch (final NotificationException e) {
        SilverLogger.getLogger(this).warn(e);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
