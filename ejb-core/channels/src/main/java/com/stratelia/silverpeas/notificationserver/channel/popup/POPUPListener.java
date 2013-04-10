/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.silverpeas.notificationserver.channel.popup;


import java.util.Date;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
  @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "AutoAcknowledge"),
  @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CHANNEL='POPUP'"),
  @ActivationConfigProperty(propertyName = "destination", propertyValue =
      "java:/queue/notificationsQueue")},
    description = "Message driven bean for Pop UP notifications")
public class POPUPListener extends AbstractListener implements MessageListener {

  private static final long serialVersionUID = 6562344573142185894L;

  public POPUPListener() {
  }

  /**
   * listener of NotificationServer JMS message
   *
   * @param msg
   */
  @Override
  public void onMessage(Message msg) {
    SilverTrace.info("popup", "POPUPListener.onMessage()", "root.MSG_GEN_PARAM_VALUE",
        "JMS message = " + msg);
    try {
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverTrace.error("popup", "POPUPListener.onMessage()", "popup.EX_CANT_PROCESS_MSG", "", e);
    }
  }

  /**
   *
   * @param message
   * @throws NotificationServerException
   */
  @Override
  public void send(NotificationData message) throws NotificationServerException {
    try {
      StringBuilder content = new StringBuilder(500);
      if (message.getTargetParam().get("SOURCE") != null) {
        content.append("Source : ").append(message.getTargetParam().get("SOURCE")).append("\n");
      }
      if (message.getTargetParam().get("DATE") != null) {
        content.append("Date : ").append(DateUtil.dateToString(((Date) message.getTargetParam().get(
            "DATE")), "")).append("\n");
      }
      content.append(message.getMessage());
      SilverMessageFactory.push(message.getTargetReceipt(), message);

    } catch (Exception e) {
      throw new NotificationServerException("POPUPListener.send()",
          SilverpeasException.ERROR, "popup.EX_CANT_ADD_MESSAGE", e);
    }
  }
}