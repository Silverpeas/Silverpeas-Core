/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.user.server;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.notification.system.JMSOperation;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

import javax.annotation.Resource;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

@JMSDestinationDefinitions(
    value = {@JMSDestinationDefinition(
        name = "java:/queue/notificationsQueue",
        interfaceName = "javax.jms.Queue",
        destinationName = "queue/notificationsQueue")})
public class NotificationServer {

  private static final String JMS_HEADER_CHANNEL = "CHANNEL";

  @Resource(lookup = "java:/queue/notificationsQueue")
  private Queue queue;

  private Map<String, String> mJmsHeaders = new HashMap<>();

  public static NotificationServer get() {
    return ServiceProvider.getService(NotificationServer.class);
  }

  private NotificationServer() {
  }

  /**
   * @param pData a notification data
   * @return
   * @throws NotificationServerException
   */
  public long addNotification(NotificationData pData) throws NotificationServerException {
    long notificationid = 0; // a gerer plus tard (necessite une database)
    mJmsHeaders.clear();
    mJmsHeaders.put(JMS_HEADER_CHANNEL, pData.getTargetChannel());
    pData.setNotificationId(notificationid);
    String notificationAsXML = NotificationServerUtil.convertNotificationDataToXML(pData);
    try {
      jmsSendToQueue(notificationAsXML, mJmsHeaders);
    } catch (Exception e) {
      throw new NotificationServerException("NotificationServer.addNotification()",
          SilverpeasException.ERROR, "notificationServer.EX_CANT_SEND_TO_JMS_QUEUE",
          notificationAsXML, e);
    }
    return notificationid;
  }

  /**
   * Send the NotificationMessage in a JMS Queue
   */
  private void jmsSendToQueue(String notificationMessage, Map<String, String> pJmsHeaders)
      throws JMSException, NamingException {
    JMSOperation.realize(context -> {
      // Initialization
      TextMessage textMsg = context.createTextMessage();
      textMsg.setText(notificationMessage);
      // Add property
      for (Map.Entry<String, String> entry : pJmsHeaders.entrySet()) {
        textMsg.setStringProperty(entry.getKey(), entry.getValue());
      }
      try {
        context.createProducer().send(queue, textMsg);
      } catch (Exception exc) {
        SilverTrace.error("notification", "NotificationServer.jmsSendToQueue",
            "notificationServer.EX_CANT_SEND_TO_JSM_QUEUE", exc);
        throw exc;
      }
    });
  }
}
