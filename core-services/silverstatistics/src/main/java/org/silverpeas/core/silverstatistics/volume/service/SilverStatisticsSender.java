/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConstants;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.notification.system.JMSOperation;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * @author
 */
@JMSDestinationDefinitions(
    value = {@JMSDestinationDefinition(
        name = "java:/queue/statisticsQueue",
        interfaceName = "javax.jms.Queue",
        destinationName = "queue/statisticsQueue")})
public final class SilverStatisticsSender {

  @Resource(lookup = "java:/queue/statisticsQueue")
  private Queue queue;

  public static SilverStatisticsSender get() {
    return ServiceProvider.getService(SilverStatisticsSender.class);
  }

  private SilverStatisticsSender() {
  }

  /**
   * @param typeOfStats the type of statistic to send
   * @param message the message to send
   * @throws JMSException
   * @throws NamingException
   */
  public void send(StatType typeOfStats, String message) throws JMSException, NamingException {
    JMSOperation.realize(context -> {
      TextMessage textMsg = context.createTextMessage();
      textMsg.setText(typeOfStats.toString() + SilverStatisticsConstants.SEPARATOR + message);
      try {
        context.createProducer().send(queue, textMsg);
      } catch (Exception exc) {
        SilverTrace.error("silverstatistics", "SilverStatisticsSender.send",
            "SilverStatisticsSender.EX_CANT_SEND_TO_JSM_QUEUE", exc);
        throw exc;
      }
    });
  }
}
