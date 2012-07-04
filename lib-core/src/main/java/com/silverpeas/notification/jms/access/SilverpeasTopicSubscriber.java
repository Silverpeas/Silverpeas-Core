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

package com.silverpeas.notification.jms.access;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * Decorator of a TopicSubscriber returned by the JMS system. It adds additional attributes in order
 * to facilitate the JMS sessions management.
 */
class SilverpeasTopicSubscriber extends JMSObjectDecorator<TopicSubscriber> implements
    TopicSubscriber {

  /**
   * Decorates the specified JMS topic subscriber.
   * @param subscriber the subscriber to decorate.
   * @return the decorator of the topic subscriber.
   */
  public static SilverpeasTopicSubscriber decorateTopicSubscriber(final TopicSubscriber subscriber) {
    return new SilverpeasTopicSubscriber(subscriber);
  }

  @Override
  public Topic getTopic() throws JMSException {
    return getDecoratedObject().getTopic();
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

  private SilverpeasTopicSubscriber(final TopicSubscriber subscriber) {
    setDecoratedObject(subscriber);
  }
}
