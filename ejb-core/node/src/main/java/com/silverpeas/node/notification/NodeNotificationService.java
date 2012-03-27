/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.node.notification;

import com.silverpeas.notification.NotificationPublisher;
import com.stratelia.webactiv.util.node.model.NodePK;
import javax.inject.Inject;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.NODE_TOPIC;

/**
 * A service to notify about the creation or the deletion of nodes.
 * It provides an easy access to the underlying messaging system used in the notification.
 */
public class NodeNotificationService {

  private static NodeNotificationService instance = new NodeNotificationService();

  /**
   * Gets an instance of the service.
   * @return a NodeNotificationService instance.
   */
  public static NodeNotificationService getService() {
    return instance;
  }
  @Inject
  private NotificationPublisher publisher;

  /**
   * Notifies the registered beans a given node (with and its children) comes to be deleted.
   * @param nodes the nodes that are deleted.
   */
  public void notifyOnDeletionOf(final NodePK node) {
      NodeDeletionNotification deletion = new NodeDeletionNotification(node);
      publisher.publish(deletion, onTopic(NODE_TOPIC.getTopicName()));
  }

  private NodeNotificationService() {
  }
}
