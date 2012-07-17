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

package com.silverpeas.personalization.notification;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.ADMIN_TOPIC;

import javax.inject.Named;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.admin.notification.SpaceLogicalDeletionNotification;
import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;

/**
 * Listener of notifications about some events that can have an impact on the user preferences.
 */
@Named
public class PersonalizationNotificationListener extends DefaultNotificationSubscriber {

  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(ADMIN_TOPIC.getTopicName()));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(ADMIN_TOPIC.getTopicName()));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    if (onTopic.getName().equals(ADMIN_TOPIC.getTopicName())) {
      SpaceLogicalDeletionNotification deletion = (SpaceLogicalDeletionNotification) notification;
      String spaceId = deletion.getSpaceId();
      SilverpeasServiceProvider.getPersonalizationService().resetDefaultSpace(spaceId);
    }
  }
}
