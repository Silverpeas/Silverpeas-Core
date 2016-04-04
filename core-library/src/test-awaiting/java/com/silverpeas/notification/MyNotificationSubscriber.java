/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.usernotification;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A subscriber on event topics for testing purpose.
 */
@Named("myNotificationSubscriber")
public class MyNotificationSubscriber implements NotificationSubscriber {

  @Inject
  private MessageSubscribingService subscribingService;
  private String id;
  private SilverpeasNotification notification;

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    this.notification = notification;
  }

  public SilverpeasNotification getReceivedNotification() {
    return notification;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void subscribeForNotifications(NotificationTopic onTopic) {
    subscribingService.subscribe(this, onTopic);
  }

  @Override
  public void unsubscribeForNotifications(NotificationTopic onTopic) {
    subscribingService.unsubscribe(this, onTopic);
  }
}
