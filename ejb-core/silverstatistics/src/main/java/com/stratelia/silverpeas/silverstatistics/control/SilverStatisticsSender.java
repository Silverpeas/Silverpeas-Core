/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverstatistics.control;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.stratelia.webactiv.util.JNDINames;

/*
 * CVS Informations
 *
 * $Id: SilverStatisticsSender.java,v 1.2 2004/11/05 14:48:04 neysseri Exp $
 *
 * $Log: SilverStatisticsSender.java,v $
 * Revision 1.2  2004/11/05 14:48:04  neysseri
 * Nettoyage sources
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.2  2002/05/16 10:08:12  mguillem
 * merge branch V2001_Statistics01
 *
 * Revision 1.1.2.2  2002/05/07 15:14:32  mguillem
 * add trace for asynchrone alimentation pb
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public final class SilverStatisticsSender {
  private QueueConnection queueConnection = null;
  private QueueSender queueSender = null;
  private QueueSession queueSession = null;
  private Queue queue = null;
  private TextMessage msg = null;

  private Context ctx = null;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws NamingException
   * 
   * @see
   */
  protected Context getInitialContext() throws NamingException {
    if (ctx == null) {
      ctx = new InitialContext();
    }
    return ctx;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SilverStatisticsSender() throws Exception {
    try {
      Context ctx = getInitialContext();

      QueueConnectionFactory factory = (QueueConnectionFactory) ctx
          .lookup(JNDINames.SILVERSTATISTICS_JMS_FACTORY);

      queueConnection = factory.createQueueConnection();

      // Create a non-transacted JMS Session
      queueSession = queueConnection.createQueueSession(false,
          Session.AUTO_ACKNOWLEDGE);

      queue = (Queue) ctx.lookup(JNDINames.SILVERSTATISTICS_JMS_QUEUE);

      queueSender = queueSession.createSender(queue);

      msg = queueSession.createTextMessage();

      queueConnection.start();

    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param message
   * 
   * @throws JMSException
   * 
   * @see
   */
  public void send(String message) throws JMSException {
    try {
      msg.setText(message);
      queueSender.send(msg);
    } catch (JMSException e) {
      throw e;
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @throws JMSException
   * 
   * @see
   */
  public void close() throws JMSException {
    queueSender.close();
    queueSession.close();
    queueConnection.close();
  }

}
