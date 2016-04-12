/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.server;

import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.channel.AbstractListener;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "AutoAcknowledge"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CHANNEL='SERVER'"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue =
        "java:/queue/notificationsQueue")},
    description = "Message driven bean to silverpeas notification")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SERVERListener extends AbstractListener implements MessageListener {

  public SERVERListener() {
  }

  @Override
  public void onMessage(Message msg) {
    try {
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverLogger.getLogger(this).error("Processing server notification error!", e);
    }
  }

  /**
   * @param notification
   * @throws NotificationServerException
   */
  @Override
  public void send(NotificationData notification) throws NotificationServerException {
    Map<String, Object> params = notification.getTargetParam();
    String sessionId = (String) params.get("SESSIONID");
    SilverMessageFactory
        .push(notification.getTargetReceipt(), notification.getMessage(), sessionId);
  }
}