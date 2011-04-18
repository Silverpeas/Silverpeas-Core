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

import com.silverpeas.notification.NotificationPublisher;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.PublishingException;
import com.silverpeas.notification.SilverpeasNotification;
import com.stratelia.webactiv.util.JNDINames;
import java.io.Serializable;
import javax.inject.Named;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Service for publishing an event by using a JMS system.
 */
@Named("eventPublisher")
public class JMSPublishingService implements NotificationPublisher, JMSServiceProvider {

  private TopicConnectionFactory connectionFactory;

  /**
   * Constructs a new JMS publishing service by bootstrapping the connection with the underlying
   * JMS system.
   * @throws NamingException when the underlying JMS system can be access through the name under
   * which it is supposed to be deployed.
   */
  public JMSPublishingService() throws NamingException {
    connectionFactory = InitialContext.doLookup(JNDINames.JMS_FACTORY);
  }

  @Override
  public <T extends Serializable> void publish(SilverpeasNotification<T> event, NotificationTopic onTopic) {
    try {
      String topicName = onTopic.getName();
      Topic jmsTopic = InitialContext.doLookup(PREFIX_TOPIC_JNDI + topicName);
      TopicConnection topicConnection = connectionFactory.createTopicConnection();
      TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicPublisher publisher = session.createPublisher(jmsTopic);
      ObjectMessage message = session.createObjectMessage();
      message.setObject(event);
      publisher.publish(message);
    } catch (Exception ex) {
      throw new PublishingException(ex);
    }
  }
}
