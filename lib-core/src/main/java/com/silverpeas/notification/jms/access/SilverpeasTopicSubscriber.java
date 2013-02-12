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

package com.silverpeas.notification.jms.access;

import javax.jms.*;

/**
 * Decorator of a TopicSubscriber returned by the JMS system.
 * In JMS, a subscriber represents a subscription to a given topic. So, a client can be mapped to
 * several topic subscribers wether it has subscribed to more than one JMS topics.
 * It adds additional attributes in order to facilitate the JMS sessions management.
 */
class SilverpeasTopicSubscriber extends JMSObjectDecorator<TopicSubscriber> implements TopicSubscriber {
  
  private String id;
  private String topicName;

  /**
   * Decorates the specified JMS topic subscriber.
   * @param subscriber the subscriber to decorate.
   * @return the decorator of the topic subscriber.
   */
  public static SilverpeasTopicSubscriber decorateTopicSubscriber(final TopicSubscriber subscriber)
      throws JMSException {
    return new SilverpeasTopicSubscriber(subscriber);
  }

  /**
   * Gets the unique identifier of the topic subscription. A unique identifier is set only for
   * durable subscription, otherwise null is returned.
   * @return the unique subscriber identifier or null if the subscriber isn't a durable one.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier for this topic subscription in JMS.
   * Only durable subscription are uniquely identified in JMS, the other subscriptions belongs only
   * in the duration of the session life (and thus of the connexion life).
   * @param id the unique identifier of the durable subscription.
   */
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Topic getTopic() throws JMSException {
    return new Topic() {
      @Override
      public String getTopicName() throws JMSException {
        return topicName;
      }
    };
  }

  @Override
  public boolean getNoLocal() throws JMSException {
    return getDecoratedObject().getNoLocal();
  }

  @Override
  public String getMessageSelector() throws JMSException {
    return getDecoratedObject().getMessageSelector();
  }

  @Override
  public MessageListener getMessageListener() throws JMSException {
    return getDecoratedObject().getMessageListener();
  }

  @Override
  public void setMessageListener(MessageListener ml) throws JMSException {
    getDecoratedObject().setMessageListener(ml);
  }

  @Override
  public Message receive() throws JMSException {
    return getDecoratedObject().receive();
  }

  @Override
  public Message receive(long l) throws JMSException {
    return getDecoratedObject().receive(l);
  }

  @Override
  public Message receiveNoWait() throws JMSException {
    return getDecoratedObject().receiveNoWait();
  }

  @Override
  public void close() throws JMSException {
    getDecoratedObject().close();
  }

  private SilverpeasTopicSubscriber(final TopicSubscriber subscriber) throws JMSException {
    setDecoratedObject(subscriber);
    topicName = subscriber.getTopic().getTopicName();
  }
}
