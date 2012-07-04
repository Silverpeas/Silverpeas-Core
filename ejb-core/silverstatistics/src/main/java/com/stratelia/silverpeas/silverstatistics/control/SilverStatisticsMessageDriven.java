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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverstatistics.control;

import com.stratelia.silverpeas.silverstatistics.model.SilverStatisticsConfigException;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.util.StatType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.StringTokenizer;

import static com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsConstants.SEPARATOR;

/**
 * Class declaration
 * @author
 */
public class SilverStatisticsMessageDriven implements MessageDrivenBean, MessageListener {

  private static final long serialVersionUID = -7349058052737871887L;
  private SilverStatistics silverStatistics = null;

  public void ejbCreate() {
  }

  /**
   * Method declaration
   * @see
   */
  @Override
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * @param sc
   * @see
   */
  @Override
  public void setMessageDrivenContext(MessageDrivenContext sc) {
    SilverTrace.info("silverstatistics", "SilverStatisticsMessageDriven.setMessageDrivenContext",
        "root.MSG_GEN_PARAM_VALUE", "MessageDrivenContext=" + sc);
    StatisticsConfig myStatsConfig = new StatisticsConfig();
    try {
      myStatsConfig.init();
    } catch (SilverStatisticsConfigException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsMessageDriven.setSessionContext",
          "silverstatistics.MSG_CONFIG_FILE", e);
    }
  }

  /**
   * Method declaration
   * @param message
   */
  @Override
  public void onMessage(Message message) {
    TextMessage textMessage = (TextMessage) message;
    try {
      String msg = textMessage.getText();
      SilverTrace.debug("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
          "root.MSG_GEN_PARAM_VALUE", "msg=" + msg);

      StringTokenizer stData = new StringTokenizer(msg, SEPARATOR);
      if (stData.hasMoreTokens()) {
        String typeOfStats = stData.nextToken();
        if (typeOfStats.length() + SEPARATOR.length() < msg.length()) {
          String stat = msg.substring(typeOfStats.length() + SEPARATOR.length(), msg.length());
          SilverTrace.info("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
              "root.MSG_GEN_PARAM_VALUE", "before putStats stat=" + stat);
          getSilverStatistics().putStats(StatType.valueOf(typeOfStats), stat);
          SilverTrace.debug("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
              "after putStats");
        } else {
          SilverTrace.error("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
              "Mauvais message", msg);
        }
      }
    } catch (Exception e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsMessageDriven.onMessage",
          "Probleme jms ", e);
    }
  }

  private SilverStatistics getSilverStatistics() {
    return SilverStatisticsFactory.getFactory().getSilverStatistics();
  }
}
