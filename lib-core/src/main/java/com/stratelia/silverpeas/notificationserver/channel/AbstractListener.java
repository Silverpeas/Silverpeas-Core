/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel;

import javax.ejb.CreateException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerConstant;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.NotificationServerUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Titre : Description : Copyright : Copyright (c) 2001 Société :
 * 
 * @author eDurand
 * @version 1.0
 */

public abstract class AbstractListener implements INotificationServerChannel,
    MessageDrivenBean, MessageListener {
  protected String m_channel;
  protected String m_payload;
  protected MessageDrivenContext m_context;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public AbstractListener() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param ctx
   * 
   * @see
   */
  public void setMessageDrivenContext(MessageDrivenContext ctx) {
    m_context = ctx;
  }

  /**
   * Method declaration
   * 
   * 
   * @throws CreateException
   * 
   * @see
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * process a message received on NotificationServer JMS message queue :
   * extract the message to send
   */
  protected void processMessage(Message msg) throws NotificationServerException {
    NotificationData nd;

    extractData(msg);
    nd = NotificationServerUtil.convertXMLToNotificationData(m_payload);
    if (nd != null) {
      SilverTrace.info("notificationServer",
          "AbstractListener.processMessage()",
          "notificationServer.INFO_PROCESS_MESSAGE");
      nd.traceObject();
    }
    send(nd);
  }

  /**
   * Extract the data from the header of the JMS message
   */
  private void extractData(Message msg) throws NotificationServerException {
    TextMessage tm = (TextMessage) msg;

    try {
      m_channel = tm
          .getStringProperty(NotificationServerConstant.JMS_HEADER_CHANNEL);
    } catch (JMSException e) {
      throw new NotificationServerException("AbstractListener.extractData()",
          SilverpeasException.ERROR,
          "notificationServer.EX_CHANNEL_NOT_DEFINED", e);
    }

    try {
      m_payload = tm.getText();
    } catch (JMSException e) {
      throw new NotificationServerException("AbstractListener.extractData()",
          SilverpeasException.ERROR,
          "notificationServer.EX_NOTIF_DATA_NOT_DEFINED", e);
    }
  }

}
