/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silvertrace.SilverTrace;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.StringTokenizer;

import static org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConstants.SEPARATOR;

/**
 * Class declaration
 *
 * @author
 */
@MessageDriven(activationConfig = {
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
  @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "AutoAcknowledge"),
  @ActivationConfigProperty(propertyName = "destination", propertyValue =
      "java:/queue/statisticsQueue")}, description = "Message driven bean for statistics insertion")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SilverStatisticsMessageDriven implements MessageListener {

  public SilverStatisticsMessageDriven() {
  }

  /**
   * Insert statistics
   * @param message the message to process
   */
  @Override
  public void onMessage(Message message) {
    TextMessage textMessage = (TextMessage) message;
    try {
      String msg = textMessage.getText();
      StringTokenizer stData = new StringTokenizer(msg, SEPARATOR);
      if (stData.hasMoreTokens()) {
        String typeOfStats = stData.nextToken();
        if (typeOfStats.length() + SEPARATOR.length() < msg.length()) {
          String stat = msg.substring(typeOfStats.length() + SEPARATOR.length(), msg.length());

          getSilverStatistics().putStats(StatType.valueOf(typeOfStats), stat);
        } else {
          SilverTrace.error("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
              "Wrong message", msg);
        }
      }
    } catch (Exception e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
          "Probleme jms ", e);
    }
  }

  private SilverStatistics getSilverStatistics() {
    return SilverStatisticsProvider.getSilverStatistics();
  }
}
