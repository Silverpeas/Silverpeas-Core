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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

/**
 * Decorator of a TopicPublisher returned by the JMS system. It adds additional attributes in order
 * to facilitate the JMS sessions management.
 */
public class SilverpeasTopicPublisher extends JMSObjectDecorator<TopicPublisher> implements
    TopicPublisher {

  /**
   * Decorates the specified JMS topic publisher.
   * @param publisher the publisher to decorate.
   * @return the decorator of the topic publisher.
   */
  public static SilverpeasTopicPublisher decorateTopicPublisher(final TopicPublisher publisher) {
    return new SilverpeasTopicPublisher(publisher);
  }

  @Override
  public Topic getTopic() throws JMSException {
    return getDecoratedObject().getTopic();
  }

  @Override
  public void publish(Message msg) throws JMSException {
    getDecoratedObject().publish(msg);
  }

  @Override
  public void publish(Message msg, int i, int i1, long l) throws JMSException {
    getDecoratedObject().publish(msg, i, i1, l);
  }

  @Override
  public void publish(Topic topic, Message msg) throws JMSException {
    getDecoratedObject().publish(topic, msg);
  }

  @Override
  public void publish(Topic topic, Message msg, int i, int i1, long l) throws JMSException {
    getDecoratedObject().publish(topic, msg, i, i1, l);
  }

  @Override
  public void setDisableMessageID(boolean bln) throws JMSException {
    getDecoratedObject().setDisableMessageID(bln);
  }

  @Override
  public boolean getDisableMessageID() throws JMSException {
    return getDecoratedObject().getDisableMessageID();
  }

  @Override
  public void setDisableMessageTimestamp(boolean bln) throws JMSException {
    getDecoratedObject().setDisableMessageTimestamp(bln);
  }

  @Override
  public boolean getDisableMessageTimestamp() throws JMSException {
    return getDecoratedObject().getDisableMessageTimestamp();
  }

  @Override
  public void setDeliveryMode(int i) throws JMSException {
    getDecoratedObject().setDeliveryMode(i);
  }

  @Override
  public int getDeliveryMode() throws JMSException {
    return getDecoratedObject().getDeliveryMode();
  }

  @Override
  public void setPriority(int i) throws JMSException {
    getDecoratedObject().setPriority(i);
  }

  @Override
  public int getPriority() throws JMSException {
    return getDecoratedObject().getPriority();
  }

  @Override
  public void setTimeToLive(long l) throws JMSException {
    getDecoratedObject().setTimeToLive(l);
  }

  @Override
  public long getTimeToLive() throws JMSException {
    return getDecoratedObject().getTimeToLive();
  }

  @Override
  public Destination getDestination() throws JMSException {
    return getDecoratedObject().getDestination();
  }

  @Override
  public void close() throws JMSException {
    getDecoratedObject().close();
  }

  @Override
  public void send(Message msg) throws JMSException {
    getDecoratedObject().send(msg);
  }

  @Override
  public void send(Message msg, int i, int i1, long l) throws JMSException {
    getDecoratedObject().send(msg, i, i1, l);
  }

  @Override
  public void send(Destination dstntn, Message msg) throws JMSException {
    getDecoratedObject().send(dstntn, msg);
  }

  @Override
  public void send(Destination dstntn, Message msg, int i, int i1, long l) throws JMSException {
    getDecoratedObject().send(dstntn, msg, i, i1, l);
  }

  private SilverpeasTopicPublisher(final TopicPublisher publisher) {
    setDecoratedObject(publisher);
  }
}
