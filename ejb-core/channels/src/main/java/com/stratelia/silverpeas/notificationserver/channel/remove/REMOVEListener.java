package com.stratelia.silverpeas.notificationserver.channel.remove;

import java.io.*;
import java.util.*;
import javax.ejb.*;
import javax.jms.*;
import com.stratelia.silverpeas.notificationserver.*;
import com.stratelia.silverpeas.notificationserver.channel.*;

/**
 * Titre : Description : Copyright : Copyright (c) 2001 Société :
 * 
 * @author eDurand
 * @version 1.0
 */

public class REMOVEListener extends AbstractListener {
  public REMOVEListener() {
  }

  public void ejbCreate() {
  }

  /**
   * listener of NotificationServer JMS message
   */
  public void onMessage(Message msg) {
    // we only remove this message
  }

  public void send(NotificationData p_Message)
      throws NotificationServerException {
    // we only remove this message
  }

}