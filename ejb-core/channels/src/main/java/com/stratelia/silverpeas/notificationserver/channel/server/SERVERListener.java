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

package com.stratelia.silverpeas.notificationserver.channel.server;

import javax.jms.Message;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.Map;

public class SERVERListener extends AbstractListener {
  private static final long serialVersionUID = 4337750320339018904L;

  public SERVERListener() {
  }

  public void ejbCreate() {
  }

  /**
   * listener of NotificationServer JMS message
   */
  public void onMessage(Message msg) {
    SilverTrace.info("server", "SERVERListener.onMessage()",
        "root.MSG_GEN_PARAM_VALUE", "JMS message = " + msg);
    try {
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverTrace.error("popup", "POPUPListener.onMessage()",
          "popup.EX_CANT_PROCESS_MSG", "", e);
    }
  }

  /**
	*
	*/
  public void send(NotificationData p_Message)
      throws NotificationServerException {
    Map<String, Object> params = p_Message.getTargetParam();
    String sessionId = (String) params.get("SESSIONID");
    SilverMessageFactory.push(p_Message.getTargetReceipt(), p_Message
        .getMessage(), sessionId);
  }
}