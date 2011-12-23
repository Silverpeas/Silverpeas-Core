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
package com.silverpeas.pdc.service;

import com.silverpeas.node.notification.NodeDeletionNotification;
import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;
import com.stratelia.webactiv.util.node.model.NodePK;
import javax.inject.Inject;
import javax.inject.Named;
import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.*;

/**
 * Listener of notifications about some events that can have an impact on the PdC or on the
 * classification on the PdC.
 */
@Named
public class PdcNotificationListener extends DefaultNotificationSubscriber {
  
  @Inject
  private PdcClassificationService service;
  
  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(NODE_TOPIC.getTopicName()));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(NODE_TOPIC.getTopicName()));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    if (onTopic.getName().equals(NODE_TOPIC.getTopicName())) {
      NodeDeletionNotification deletion = (NodeDeletionNotification) notification;
      NodePK node = deletion.getNodePK();
      service.deletePreDefinedClassification(node.getId(), node.getInstanceId());
    }
  }
}
