/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationserver.channel.popup;

import java.util.*;

import javax.jms.*;
import com.stratelia.silverpeas.notificationserver.*;
import com.stratelia.silverpeas.notificationserver.channel.*;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.silverpeas.silvertrace.*;

public class POPUPListener extends AbstractListener {
  public POPUPListener() {
  }

  public void ejbCreate() {
  }

  /**
   * listener of NotificationServer JMS message
   */
  public void onMessage(Message msg) {
    SilverTrace.info("popup", "POPUPListener.onMessage()",
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
    try {
      StringBuffer message = new StringBuffer();
      if (p_Message.getTargetParam().get("SOURCE") != null) {
        message.append("Source : " + p_Message.getTargetParam().get("SOURCE")
            + "\n");
      }
      if (p_Message.getTargetParam().get("DATE") != null) {
        message.append("Date : "
            + DateUtil.dateToString(((Date) p_Message.getTargetParam().get(
            "DATE")), "") + "\n");
      }
      message.append(p_Message.getMessage());
      SilverMessageFactory.push(p_Message.getTargetReceipt(), p_Message);

    } catch (Exception e) {
      throw new NotificationServerException("POPUPListener.send()",
          SilverpeasException.ERROR, "popup.EX_CANT_ADD_MESSAGE", e);
    }
  }

}