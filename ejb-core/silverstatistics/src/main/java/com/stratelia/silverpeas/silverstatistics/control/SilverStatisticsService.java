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
import com.stratelia.silverpeas.silverstatistics.model.StatisticsRuntimeException;
import com.stratelia.silverpeas.silverstatistics.util.StatType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import org.apache.commons.lang3.text.StrTokenizer;
import com.silverpeas.annotation.Service;

import javax.inject.Named;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsConstants.SEPARATOR;
import static com.stratelia.webactiv.util.JNDINames.SILVERSTATISTICS_DATASOURCE;

/**
 * Class declaration
 * @author SLR
 */
@Service
@Named("SilverStatistics")
public class SilverStatisticsService implements SilverStatistics {
  private static final long serialVersionUID = -2084739513469943886L;
  private StatisticsConfig myStatsConfig;

  /**
   * @param type
   * @param data
   */
  @Override
  public void putStats(StatType type, String data) {
    StrTokenizer stData = new StrTokenizer(data, SEPARATOR);
    List<String> dataArray = stData.getTokenList();
    if (myStatsConfig.isGoodDatas(type, dataArray)) {
      Connection myCon = DBUtil.makeConnection(SILVERSTATISTICS_DATASOURCE);
      try {
        SilverStatisticsDAO.putDataStats(myCon, type, dataArray, myStatsConfig);
        if (!myCon.getAutoCommit()) {
          myCon.commit();
        }
      } catch (SQLException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
            "silverstatistics.MSG_ALIMENTATION_BD",
            "typeOfStats = " + type + ", dataArray = " + dataArray, e);
      } catch (IOException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
            "silverstatistics.MSG_ALIMENTATION_BD", "typeOfStats = "
            + type + ", dataArray = " + dataArray, e);
      } catch (StatisticsRuntimeException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
            "MSG_CONNECTION_BD");
      } finally {
        DBUtil.close(myCon);
      }

    } else {
      SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
          "MSG_CONFIG_DATAS", "data en entree=" + data + " pour " + type);
    }
  }

  @Override
  public void makeVolumeAlimentationForAllComponents() {
    SilverStatisticsVolumeAlimentation.makeVolumeAlimentationForAllComponents();
  }

  @Override
  public void makeStatAllCumul() {
    SilverStatisticsManagerDAO.makeStatAllCumul(myStatsConfig);
  }

  /**
   * Constructor declaration
   * @see
   */
  public SilverStatisticsService() {
    myStatsConfig = new StatisticsConfig();
    try {
      myStatsConfig.init();
    } catch (SilverStatisticsConfigException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsEJB.setSessionContext",
          "silverstatistics.MSG_CONFIG_FILE", e);
    }
  }

}
