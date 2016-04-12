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
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.channel.AbstractListener;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Date;
import java.util.Map;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "AutoAcknowledge"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue =
        "CHANNEL='SILVERMAIL'"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue =
        "java:/queue/notificationsQueue")},
    description = "Message driven bean to silverpeas notification box")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SILVERMAILListener extends AbstractListener implements MessageListener {

  public SILVERMAILListener() {
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
      SilverLogger.getLogger(this).error("Server notification processing failure", e);
    }
  }

  @Override
  public void send(NotificationData p_Message) throws NotificationServerException {
    try {
      Map<String, Object> keyValue = p_Message.getTargetParam();
      String tmpSubjectString = (String) keyValue.get("SUBJECT"); // retrieves the SUBJECT key
      // value.
      String tmpSourceString = (String) keyValue.get("SOURCE"); // retrieves the SOURCE key value.
      String tmpUrlString = (String) keyValue.get("URL"); // retrieves the URL key value.
      Date tmpDate = (Date) keyValue.get("DATE"); // retrieves the DATE key value.
      SILVERMAILMessage sm = new SILVERMAILMessage();
      sm.setUserId(Integer.parseInt(p_Message.getTargetReceipt()));
      sm.setSenderName(p_Message.getSenderName());
      sm.setSubject(tmpSubjectString);
      sm.setUrl(tmpUrlString);
      sm.setSource(tmpSourceString);
      sm.setDate(tmpDate);
      sm.setBody(p_Message.getMessage());
      SILVERMAILPersistence.addMessage(sm);
    } catch (Exception e) {
      throw new NotificationServerException("SILVERMAILListener.send()", SilverpeasException.ERROR,
          "silvermail.EX_CANT_ADD_MESSAGE", e);
    }
  }
}
