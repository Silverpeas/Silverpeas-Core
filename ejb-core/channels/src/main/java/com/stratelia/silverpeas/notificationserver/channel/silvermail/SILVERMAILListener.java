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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Date;

import javax.jms.Message;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Map;

public class SILVERMAILListener extends AbstractListener {
  private static final long serialVersionUID = -6357231434570565933L;

  public SILVERMAILListener() {
  }

  @Override
  public void ejbCreate() {
  }

  /**
   * listener of NotificationServer JMS message
   * @param msg
   */
  @Override
  public void onMessage(Message msg) {
    try {
      SilverTrace.info("silvermail", "SILVERMAILListener.onMessage()",
          "root.MSG_GEN_PARAM_VALUE", "JMS Message = " + msg.toString());
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverTrace.error("silvermail", "SILVERMAILListener.onMessage()",
          "silvermail.EX_CANT_PROCESS_MSG", "JMS Message = " + msg.toString(), e);
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
      throw new NotificationServerException("SILVERMAILListener.send()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_ADD_MESSAGE", e);
    }
  }
}