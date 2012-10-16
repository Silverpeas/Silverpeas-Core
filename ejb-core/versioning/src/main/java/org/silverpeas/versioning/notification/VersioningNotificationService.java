/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.versioning.notification;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.ATTACHMENT_TOPIC;

import javax.inject.Inject;

import com.silverpeas.notification.NotificationPublisher;
import com.stratelia.silverpeas.versioning.model.Document;

/**
 * A service to notify about the events which occurs on attachment. It provides an easy access to
 * the underlying messaging system used in the notification.
 */
public class VersioningNotificationService {

  private static VersioningNotificationService instance = new VersioningNotificationService();

  /**
   * Gets an instance of the service.
   * @return an VersioningNotificationService instance.
   */
  public static VersioningNotificationService getService() {
    return instance;
  }

  @Inject
  private NotificationPublisher publisher;

  /**
   * Notifies the registered beans a given node (with and its children) comes to be deleted.
   * @param nodes the nodes that are deleted.
   */
  public void notifyOnDeletionOf(final Document document) {
    VersioningDeletionNotification deletion = new VersioningDeletionNotification(document);
    publisher.publish(deletion, onTopic(ATTACHMENT_TOPIC.getTopicName()));
  }

  private VersioningNotificationService() {
  }
}