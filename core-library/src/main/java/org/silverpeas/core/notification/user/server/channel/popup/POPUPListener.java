/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.notification.user.client.NotificationParameterNames;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.channel.AbstractListener;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Date;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CHANNEL='POPUP'"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue =
        "jms/queue/notificationsQueue")}, description = "Message driven bean for Pop UP " +
    "notifications")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class POPUPListener extends AbstractListener implements MessageListener {

  public POPUPListener() {
    // Nothing to initialize
  }

  /**
   * listener of NotificationServer JMS message
   * @param msg
   */
  @Override
  public void onMessage(Message msg) {
    try {
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * @param message
   * @throws NotificationServerException
   */
  @Override
  public void send(NotificationData message) throws NotificationServerException {
    try {
      final StringBuilder content = new StringBuilder(500);
      final String source =
          (String) message.getTargetParam().get(NotificationParameterNames.SOURCE);
      final Date date = (Date) message.getTargetParam().get(NotificationParameterNames.DATE);
      if (source != null) {
        content.append("Source : ").append(source).append("\n");
      }
      if (date != null) {
        content.append("Date : ")
            .append(DateUtil.dateToString(date, ""))
            .append("\n");
      }
      content.append(message.getMessage());
      PopupMessageService.get().push(message.getTargetReceipt(), message);

    } catch (Exception e) {
      throw new NotificationServerException(e);
    }
  }
}
