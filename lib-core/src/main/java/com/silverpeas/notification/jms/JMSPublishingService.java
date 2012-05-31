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

import com.silverpeas.notification.NotificationPublisher;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.PublishingException;
import com.silverpeas.notification.SilverpeasNotification;
import com.silverpeas.notification.jms.access.JMSAccessObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TopicPublisher;

/**
 * Service for publishing an event by using a JMS system. This service is managed by the IoC
 * container under the name 'notificationPublisher' as required by the Notification API. The JMS
 * system is injected as a dependency by the IoC container.
 */
@Named("notificationPublisher")
public class JMSPublishingService implements NotificationPublisher {

  @Inject
  private JMSAccessObject jmsService;

  @Override
  public void publish(SilverpeasNotification notification, NotificationTopic onTopic) {
    TopicPublisher publisher = null;
    try {
      String topicName = onTopic.getName();
      publisher = jmsService.createTopicPublisher(topicName);
      ObjectMessage message = jmsService.createObjectMessageFor(publisher);
      message.setObject(notification);
      publisher.publish(message);
    } catch (Exception ex) {
      throw new PublishingException(ex);
    } finally {
      try {
        if (publisher != null) {
          jmsService.disposeTopicPublisher(publisher);
        }
      } catch (JMSException ex) {
        Logger.getLogger(JMSPublishingService.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
