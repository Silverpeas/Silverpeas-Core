/*
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.notification.jms;

import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.NotificationSubscriber;
import com.silverpeas.notification.MessageSubscribingService;
import com.silverpeas.notification.SubscriptionException;
import com.stratelia.webactiv.util.JNDINames;
import java.util.UUID;
import javax.inject.Named;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static com.silverpeas.notification.jms.SilverpeasMessageListener.*;

/**
 * Implementation of the subscribing service using the JMS API.
 */
@Named("messageSubscribingService")
public class JMSSubscribingService implements MessageSubscribingService, JMSServiceProvider {

  private TopicConnectionFactory connectionFactory;

  /**
   * Constructs a new JMS subscribing service by bootstrapping the connection with the underlying
   * JMS system.
   * @throws NamingException when the underlying JMS system can be access through the name under
   * which it is supposed to be deployed.
   */
  public JMSSubscribingService() throws NamingException {
    connectionFactory = InitialContext.doLookup(JNDINames.JMS_FACTORY);
  }

  @Override
  public void subscribe(NotificationSubscriber subscriber, NotificationTopic onTopic) {
    try {
      String topicName = onTopic.getName();
      String subscriptionId = UUID.randomUUID().toString();
      Topic jmsTopic = InitialContext.doLookup(PREFIX_TOPIC_JNDI + topicName);
      TopicConnection topicConnection = connectionFactory.createTopicConnection();
      TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber topicSubscriber = session.createDurableSubscriber(jmsTopic, subscriptionId);
      topicSubscriber.setMessageListener(mapMessageListenerTo(subscriber).forTopic(topicName));
      topicConnection.start();
      subscriber.setId(subscriptionId);
    } catch (Exception ex) {
      throw new SubscriptionException(ex);
    }
  }

  @Override
  public void unsubscribe(NotificationSubscriber subscriber, NotificationTopic fromTopic) {
    try {
      TopicConnection topicConnection = connectionFactory.createTopicConnection();
      TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      session.unsubscribe(subscriber.getId());
      session.close();
    } catch (Exception ex) {
      throw new SubscriptionException(ex);
    }
  }

}
