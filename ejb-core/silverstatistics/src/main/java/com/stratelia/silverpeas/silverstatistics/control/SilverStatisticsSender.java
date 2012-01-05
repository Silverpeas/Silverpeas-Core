/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.silverstatistics.control;

import com.stratelia.silverpeas.silverstatistics.util.StatType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.Closeable;
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

import static com.stratelia.webactiv.util.JNDINames.SILVERSTATISTICS_JMS_FACTORY;
import static com.stratelia.webactiv.util.JNDINames.SILVERSTATISTICS_JMS_QUEUE;

/**
 * Class declaration
 *
 * @author
 */
public final class SilverStatisticsSender implements Closeable {

  private QueueConnectionFactory factory;
  private QueueConnection queueConnection = null;
  private QueueSender queueSender = null;
  private QueueSession queueSession = null;
  private TextMessage msg = null;
  private Context ctx;

  public SilverStatisticsSender() {
    try {
      ctx = new InitialContext();
      factory = (QueueConnectionFactory) ctx.lookup(SILVERSTATISTICS_JMS_FACTORY);
    } catch (NamingException ex) {
      SilverTrace
          .error("silverstatistics", "SilverStatisticsSender", "SilverStatisticsSender ", ex);
    }
  }

  /**
   * Method declaration
   *
   * @param message
   * @throws JMSException
   * @throws NamingException
   * @see
   */
  public void send(StatType typeOfStats, String message) throws JMSException, NamingException {
    queueConnection = factory.createQueueConnection();
    queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue queue = (Queue) ctx.lookup(SILVERSTATISTICS_JMS_QUEUE);
    queueSender = queueSession.createSender(queue);
    msg = queueSession.createTextMessage();
    queueConnection.start();
    msg.setText(typeOfStats.toString() + SilverStatisticsConstants.SEPARATOR + message);
    queueSender.send(msg);
  }

  @Override
  public void close() {
    try {
      if (queueSender != null) {
        queueSender.close();
      }
      if (queueSession != null) {
        queueSession.close();
      }
      if (queueConnection != null) {
        queueConnection.close();
      }
    } catch (JMSException ex) {
      SilverTrace
          .error("silverstatistics", "SilverStatisticsSender.close", "SilverStatisticsSender ", ex);
    }
  }
}
