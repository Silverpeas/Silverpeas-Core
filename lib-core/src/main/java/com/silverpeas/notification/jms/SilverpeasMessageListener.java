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

import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.NotificationSubscriber;
import com.silverpeas.notification.PublishingException;
import com.silverpeas.notification.SilverpeasNotification;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * Listener of messages droven in JMS. A JMS message wrap the silverpeas notification that was
 * published on a given topic.
 */
public class SilverpeasMessageListener implements MessageListener {

  /**
   * Maps a JMS message listener to the specified event subscriber.
   * @param theSubscriber the subscriber with which a message listener will be linked.
   * @return a JMS message listener.
   */
  public static SilverpeasMessageListener mapMessageListenerTo(
      final NotificationSubscriber theSubscriber) {
    return new SilverpeasMessageListener(theSubscriber);
  }

  private NotificationSubscriber subscriber;
  private String topic;

  @Override
  public void onMessage(Message msg) {
    ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      ObjectMessage message = (ObjectMessage) msg;
      SilverpeasNotification notification = (SilverpeasNotification) message.getObject();
      this.subscriber.onNotification(notification, NotificationTopic.onTopic(topic));
    } catch (JMSException ex) {
      throw new PublishingException(ex);
    } finally {
      Thread.currentThread().setContextClassLoader(originalTCCL);
    }
  }

  /**
   * Specifies for which topic this listener is listening.
   * @param topicName the name of the topic.
   * @return itself.
   */
  public SilverpeasMessageListener forTopic(String topicName) {
    this.topic = topicName;
    return this;
  }

  /**
   * Gets the name of the topic to which this listener listens incoming messages.
   * @return the topic name.
   */
  public String getTopic() {
    return topic;
  }

  private SilverpeasMessageListener(final NotificationSubscriber subscriber) {
    this.subscriber = subscriber;
  }
}
