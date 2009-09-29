package com.stratelia.silverpeas.notificationserver.channel.server;

import java.util.Hashtable;

import javax.jms.Message;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SERVERListener extends AbstractListener {
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
    Hashtable params = p_Message.getTargetParam();
    String sessionId = (String) params.get("SESSIONID");
    SilverMessageFactory.push(p_Message.getTargetReceipt(), p_Message
        .getMessage(), sessionId);
  }
}