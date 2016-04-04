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
package org.silverpeas.core.notification.user.server.channel;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerConstant;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.NotificationServerUtil;
import org.silverpeas.core.exception.SilverpeasException;

public abstract class AbstractListener implements INotificationServerChannel {

  protected String channel;
  protected String payLoad;

  /**
   * Constructor declaration
   *
   * @see
   */
  public AbstractListener() {
  }

  /**
   * Process a message received on NotificationServer JMS message queue : extract the message
   * content to be sent.
   *
   * @param msg the message to be proccessed.
   * @throws NotificationServerException
   */
  protected void processMessage(Message msg) throws NotificationServerException {
    extractData(msg);
    NotificationData nd = NotificationServerUtil.convertXMLToNotificationData(payLoad);
    if (nd != null) {

      nd.traceObject();
    }
    send(nd);
  }

  /**
   * Extract the data from the header of the JMS message
   * @param msg : the message to extract the notification data from.
   * @throws NotificationServerException
   */
  private void extractData(Message msg) throws NotificationServerException {
    TextMessage tm = (TextMessage) msg;
    try {
      channel = tm.getStringProperty(NotificationServerConstant.JMS_HEADER_CHANNEL);
    } catch (JMSException e) {
      throw new NotificationServerException("AbstractListener.extractData()",
          SilverpeasException.ERROR, "notificationServer.EX_CHANNEL_NOT_DEFINED", e);
    }
    try {
      payLoad = tm.getText();
    } catch (JMSException e) {
      throw new NotificationServerException("AbstractListener.extractData()",
          SilverpeasException.ERROR, "notificationServer.EX_NOTIF_DATA_NOT_DEFINED", e);
    }
  }
}