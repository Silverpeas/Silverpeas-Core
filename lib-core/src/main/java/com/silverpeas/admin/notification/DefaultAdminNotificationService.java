/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.admin.notification;

import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.notification.NotificationPublisher;
import com.silverpeas.notification.NotificationSource;
import com.silverpeas.notification.SilverpeasNotification;
import com.silverpeas.notification.SilverpeasNotificationCause;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.ADMIN_COMPONENT_TOPIC;
import static com.silverpeas.notification.RegisteredTopics.ADMIN_SPACE_TOPIC;

/**
 * A service to notify about events on admin. It provides an easy access to the underlying messaging
 * system used in the notification.
 */
@Named("adminNotificationService")
public class DefaultAdminNotificationService implements AdminNotificationService {

  @Inject
  private NotificationPublisher publisher;

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.admin.notification.AdminNotificationService#notifyOnDeletionOf(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void notifyOnDeletionOf(final String spaceId, String userId) {
    SilverpeasNotification notification = new SilverpeasNotification(new NotificationSource()
        .withUserId(userId), SilverpeasNotificationCause.DELETION, spaceId);
    publisher.publish(notification, onTopic(ADMIN_SPACE_TOPIC));
  }

  @Override
  public void notifyOfComponentConfigurationChange(String componentId, String userId,
      ComponentJsonPatch changes) {
    SilverpeasNotification notification = new SilverpeasNotification(new NotificationSource()
        .withComponentInstanceId(componentId), SilverpeasNotificationCause.UPDATE, changes);
    publisher.publish(notification, onTopic(ADMIN_COMPONENT_TOPIC));
  }

}
