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

package com.silverpeas.notification.jms;

import com.silverpeas.notification.MessageSubscribingService;
import com.silverpeas.notification.NotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SubscriptionException;
import static com.silverpeas.notification.jms.SilverpeasMessageListener.mapMessageListenerTo;

import com.silverpeas.notification.jms.access.ConnectionFailureListener;
import com.silverpeas.notification.jms.access.JMSAccessObject;
import com.silverpeas.util.ExecutionAttempts;

import static com.silverpeas.util.ExecutionAttempts.retry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the subscribing service using the JMS API. This service is managed by the IoC
 * container under the name 'messageSubscribingService' as required by the Notification API. The JMS
 * system is injected as a dependency by the IoC container.
 */
@Named("messageSubscribingService")
public class JMSSubscribingService implements MessageSubscribingService, ConnectionFailureListener {

  @Inject
  private JMSAccessObject jmsService;

  @PostConstruct
  public void initialize() {
    jmsService.addConnectionFailureListener(this);
  }

  @Override
  public synchronized void subscribe(final NotificationSubscriber subscriber,
          NotificationTopic onTopic) {
    final String topicName = onTopic.getName();
    final String subscriberId = subscriber.getId();
    final ManagedTopicsSubscriber topicsSubscriber;
    ManagedTopicsSubscriber existingTopicsSubscriber =
            ManagedTopicsSubscriber.getManagedTopicsSubscriberById(subscriberId);
    if (existingTopicsSubscriber == null) {
      topicsSubscriber = ManagedTopicsSubscriber.getNewManagedTopicsSubscriber();
    } else {
      topicsSubscriber = existingTopicsSubscriber;
    }
    try {
      retry(2, new ExecutionAttempts.Job() {

        @Override
        public void execute() throws Exception {
          if (!topicsSubscriber.isSubscribedTo(topicName)) {
            String id = topicsSubscriber.getId();
            MessageListener listener = mapMessageListenerTo(subscriber).forTopic(topicName);
            createSubscription(topicsSubscriber, topicName, listener);
            topicsSubscriber.save();
            subscriber.setId(id);
          }
        }
      });
    } catch (Exception ex) {
      throw new SubscriptionException(ex);
    }
  }

  @Override
  public synchronized void unsubscribe(NotificationSubscriber subscriber,
          final NotificationTopic fromTopic) {
    final ManagedTopicsSubscriber topicsSubscriber =
            ManagedTopicsSubscriber.getManagedTopicsSubscriberById(subscriber.getId());
    try {
      retry(2, new ExecutionAttempts.Job() {

        @Override
        public void execute() throws Exception {
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
        }
      });
    } catch (Exception ex) {
      throw new SubscriptionException(ex);
    }
  }

  @Override
  public void onConnectionFailure() {
    Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING,
        "Connection failure detected: the subscriptions on the topics are lost. " +
            "I'm going to recreate the subscriptions");
    Collection<ManagedTopicsSubscriber> subscribers =
        ManagedTopicsSubscriber.getAllManagedTopicSubscribers();
    for (final ManagedTopicsSubscriber subscriber: subscribers) {
      Collection<TopicSubscriber> subscriptions = subscriber.getAllSubscriptions();
      for (final TopicSubscriber subscription: subscriptions) {
        try {
          retry(2, new ExecutionAttempts.Job() {

            @Override
            public void execute() throws Exception {
              MessageListener listener = subscription.getMessageListener();
              String topicName = subscription.getTopic().getTopicName();
              createSubscription(subscriber, topicName, listener);
              subscriber.removeSubscription(subscription);
            }
          });
        } catch (Exception ex) {
          try {
            String topicName = subscription.getTopic().getTopicName();
            Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE,
                "The subscription on '" + topicName + "' topic has failed!\n" + ex.getMessage());
          } catch (JMSException e) {
            Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE,
                "The subscription on a topic has failed!\n" + ex.getMessage());
          }
        }
      }
    }
  }

  private String getSubscriptionId(ManagedTopicsSubscriber subscriber, String topicName) {
    return subscriber.getId() + "::" + topicName;
  }

  private void createSubscription(ManagedTopicsSubscriber subscriber, String topicName, MessageListener listener)
      throws NamingException, JMSException {
    String subscriptionId = getSubscriptionId(subscriber, topicName);
    TopicSubscriber jmsSubscriber = jmsService.createTopicSubscriber(topicName,
        subscriptionId, listener);
    subscriber.addSubscription(jmsSubscriber);
  }
}
