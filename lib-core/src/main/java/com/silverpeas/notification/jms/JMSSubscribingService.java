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

package com.silverpeas.notification.jms;

import com.silverpeas.notification.jms.access.JMSAccessObject;
import javax.inject.Inject;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.NotificationSubscriber;
import com.silverpeas.notification.MessageSubscribingService;
import com.silverpeas.notification.SubscriptionException;
import javax.inject.Named;
import javax.jms.TopicSubscriber;
import static com.silverpeas.notification.jms.SilverpeasMessageListener.*;

/**
 * Implementation of the subscribing service using the JMS API. This service is managed by the IoC
 * container under the name 'messageSubscribingService' as required by the Notification API. The JMS
 * system is injected as a dependency by the IoC container.
 */
@Named("messageSubscribingService")
public class JMSSubscribingService implements MessageSubscribingService {

  @Inject
  private JMSAccessObject jmsService;

  @Override
  public synchronized void subscribe(NotificationSubscriber subscriber, NotificationTopic onTopic) {

    String topicName = onTopic.getName();
    String subscriptionId = subscriber.getId();
    ManagedTopicsSubscriber topicsSubscriber =
        ManagedTopicsSubscriber.getManagedTopicsSubscriberById(subscriptionId);
    if (topicsSubscriber == null) {
      topicsSubscriber = ManagedTopicsSubscriber.getNewManagedTopicsSubscriber();
    }
    try {
      if (!topicsSubscriber.isSubscribedTo(topicName)) {
        subscriptionId = topicsSubscriber.getId();
        TopicSubscriber jmsSubscriber = jmsService.createTopicSubscriber(topicName);
        jmsSubscriber.setMessageListener(mapMessageListenerTo(subscriber).forTopic(topicName));
        topicsSubscriber.addSubscription(jmsSubscriber);
        topicsSubscriber.save();
        subscriber.setId(subscriptionId);
      }
    } catch (Exception ex) {
      throw new SubscriptionException(ex);
    }
  }

  @Override
  public synchronized void unsubscribe(NotificationSubscriber subscriber,
      NotificationTopic fromTopic) {
    try {
      ManagedTopicsSubscriber topicsSubscriber =
          ManagedTopicsSubscriber.getManagedTopicsSubscriberById(subscriber.getId());
      if (topicsSubscriber != null) {
        TopicSubscriber jmsSubscriber = topicsSubscriber.getSubscription(fromTopic.getName());
        if (jmsSubscriber != null) {
          jmsService.disposeTopicSubscriber(jmsSubscriber);
          topicsSubscriber.removeSubscription(jmsSubscriber);
          if (topicsSubscriber.hasNoSusbscriptions()) {
            topicsSubscriber.delete();
          }
        }
      }
    } catch (Exception ex) {
      throw new SubscriptionException(ex);
    }
  }

}
