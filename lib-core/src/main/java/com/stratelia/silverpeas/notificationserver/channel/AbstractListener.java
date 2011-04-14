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
 * @author eDurand
 * @version 1.0
 */

public abstract class AbstractListener implements INotificationServerChannel, MessageDrivenBean,
    MessageListener {

  private static final long serialVersionUID = -5838215003969093865L;
  protected String m_channel;
  protected String m_payload;
  protected MessageDrivenContext m_context;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractListener() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * @param ctx
   * @see
   */
  public void setMessageDrivenContext(MessageDrivenContext ctx) {
    m_context = ctx;
  }

  /**
   * Method declaration
   * @throws CreateException
   * @see
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * process a message received on NotificationServer JMS message queue : extract the message to
   * send
   */
  protected void processMessage(Message msg) throws NotificationServerException {
    extractData(msg);
    NotificationData nd = NotificationServerUtil.convertXMLToNotificationData(m_payload);
    if (nd != null) {
      SilverTrace.info("notificationServer", "AbstractListener.processMessage()",
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