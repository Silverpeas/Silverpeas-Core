/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.notification.user.client.NotificationParameterNames;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.channel.AbstractListener;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Date;
import java.util.Map;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue =
        "CHANNEL='SILVERMAIL'"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue =
        "jms/queue/notificationsQueue")},
    description = "Message driven bean to silverpeas notification box")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SILVERMAILListener extends AbstractListener implements MessageListener {

  @Override
  public void onMessage(Message msg) {
    try {
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverLogger.getLogger(this).error("Server notification processing failure", e);
    }
  }

  @Override
  public void send(NotificationData data) throws NotificationServerException {
    try {
      Map<String, Object> keyValue = data.getTargetParam();
      // retrieves the SUBJECT key value.
      String tmpSubjectString = (String) keyValue.get(NotificationParameterNames.SUBJECT.toString());
      // retrieves the SOURCE key value.
      String tmpSourceString = (String) keyValue.get(NotificationParameterNames.SOURCE.toString());
      // retrieves the URL key value.
      String tmpUrlString = (String) keyValue.get(NotificationParameterNames.URL.toString());
      // retrieves the DATE key value.
      Date tmpDate = (Date) keyValue.get(NotificationParameterNames.DATE.toString());
      SILVERMAILMessage sm = new SILVERMAILMessage();
      sm.setUserId(Integer.parseInt(data.getTargetReceipt()));
      String senderName = StringUtil.isDefined(data.getSenderName()) ? data.getSenderName() : "";
      sm.setSenderName(senderName);
      sm.setSubject(tmpSubjectString);
      sm.setUrl(tmpUrlString);
      sm.setSource(tmpSourceString);
      sm.setDate(tmpDate);
      sm.setBody(data.getMessage());
      SILVERMAILPersistence.addMessage(sm);
    } catch (Exception e) {
      throw new NotificationServerException(e);
    }
  }
}
