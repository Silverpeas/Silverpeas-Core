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

import com.silverpeas.notification.SubscriptionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.JMSException;
import javax.jms.TopicSubscriber;

/**
 * A subscriber of one or more topics whose the life-cycle is managed by the JMS adapter. JMS
 * doesn't support the subscription of a given subscriber to several topics. JMS is a low-level MOM
 * API so that it is required to enhance this API by adding additional features in order to provide
 * high-level messaging features.
 */
class ManagedTopicsSubscriber {

  private static Map<String, ManagedTopicsSubscriber> subscribers = Collections.synchronizedMap(
      new HashMap<String, ManagedTopicsSubscriber>());

  /**
   * Gets an existing managed subscriber by its unique identifier.
   * @param id the managed subscriber identifier.
   * @return the managed subscriber corresponding to the specified identifier or null if no such
   * subscriber exists.
   */
  public static ManagedTopicsSubscriber getManagedTopicsSubscriberById(String id) {
    ManagedTopicsSubscriber subscriber = null;
    if (subscribers.containsKey(id)) {
      subscriber = subscribers.get(id);
    }
    return subscriber;
  }

  /**
   * Gets a new managed subscriber ready to be used. This subscriber isn't saved within the context
   * of the JMS adapter, so that once used and no more referenced it will be lost.
   * @return a new managed subscriber.
   */
  public static ManagedTopicsSubscriber getNewManagedTopicsSubscriber() {
    return new ManagedTopicsSubscriber(UUID.randomUUID().toString());
  }

  private final String id;
  private final List<TopicSubscriber> subscriptions = new ArrayList<TopicSubscriber>();

  /**
   * Is this subscriber subscirbed to the specified topic?
   * @param topicName the topic name.
   * @return true if this subscriber is subscribed to the specified topic, false otherwise.
   * @throws JMSException if an error occurs while verifying the topic subscription.
   */
  public boolean isSubscribedTo(String topicName) throws JMSException {
    return getSubscription(topicName) != null;
  }

  /**
   * Gets the unique identifier of this subscriber.
   * @return the subscriber identifier.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Gets the JMS TopicSubscriber instance matching the subscription of this subscriber to the
   * specified topic.
   * @param topicName the topic name.
   * @return the TopicSubscriber matching the subsciption of this subscriber.
   * @throws JMSException if an error occurs while getting the subscription for the specified topic.
   */
  public TopicSubscriber getSubscription(String topicName) throws JMSException {
    TopicSubscriber subscription = null;
    for (TopicSubscriber topicSubscriber : subscriptions) {
      if (topicSubscriber.getTopic().getTopicName().equals(topicName)) {
        subscription = topicSubscriber;
        break;
      }
    }
    return subscription;
  }

  /**
   * Adds a new topic subscription represented by the specified JMS TopicSubscriber instance. This
   * methods attachs the topic subscription represented by the TopicSubscriber instance to this
   * managed subscriber.
   * @param topicSubscriber a TopicSubscriber instance representing an existing subscription.
   */
  public void addSubscription(final TopicSubscriber topicSubscriber) {
    subscriptions.add(topicSubscriber);
  }

  /**
   * Removes the topic subscription represented by the specified JMS TopicSubscriber instance. This
   * methods deattachs from this managed subscriber the topic subscription reprensented by this
   * TopicSubscriber instance.
   * @param topicSubscriber a TopicSubscriber instance representing an existing subscription.
   */
  public void removeSubscription(final TopicSubscriber topicSubscriber) {
    subscriptions.remove(topicSubscriber);
  }

  /**
   * Is this managed subscriber has some subscriptions?
   * @return true if this managed subscriber has at least one topic subscription, false otherwise.
   */
  public boolean hasNoSusbscriptions() {
    return subscriptions.isEmpty();
  }

  /**
   * Saves this managed subscription into the context of the JMS adapter so that it can retrieved
   * later.
   */
  public void save() {
    subscribers.put(getId(), this);
  }

  /**
   * Deletes this managed subscription in the context of the JMS adapter so that is will be lost
   * once dereferenced.
   */
  public void delete() {
    if (!subscriptions.isEmpty()) {
      throw new SubscriptionException("The subscriber cannot be deleted: it has " + subscriptions.
          size() + " subscriptions!");
    }
    subscribers.remove(getId());
  }

  private ManagedTopicsSubscriber(String id) {
    this.id = id;
  }
}
