/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.notification;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * It is the default implementation of the NotificationSubscriber interface. It is dedicated to be
 * extended by more business specific notification listener.
 * It encapsulates the access to the underlying messaging broker for performing the actual
 * notification subscriptions.
 *
 * The business specific notification subscriber that extend this class must be registered within
 * an IoC container so that it will be automatically instanciated and the subscription will be then
 * automatically performed by the IoC container. The IoC container will inject the implementation of
 * the messaging system used to accomplish the notification mechanism in Silverpeas.
 */
public abstract class DefaultNotificationSubscriber implements NotificationSubscriber {

  @Inject
  private MessageSubscribingService subscribingService;

  private String id;

  /**
   * Subscribes the topics this subscriber is interested on.
   * This method will be automatically called by the IoC container once instantiated.
   * This method should called the subscribeForNotifications one for each topic it is interested on.
   */
  @PostConstruct
  public abstract void subscribeOnTopics();

  /**
   * Unsubscribes from the topics this subscriber is subscribed on.
   * This method will be automatically called by the IoC container before being released.
   * This method should called the unsubscribeForNotifications one for each topic it is subscribed on.
   */
  @PreDestroy
  public abstract void unsubscribeOnTopics();

  @Override
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier for this subscriber in the underlying MOM system.
   * This method is called by the underlying MOM system at subscription.
   * @param id the unique identifier to set.
   */
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
