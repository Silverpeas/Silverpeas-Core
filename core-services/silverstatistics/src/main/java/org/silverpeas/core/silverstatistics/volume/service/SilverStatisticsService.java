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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Singleton;

import org.silverpeas.core.silverstatistics.volume.dao.SilverStatisticsDAO;
import org.silverpeas.core.silverstatistics.volume.dao.SilverStatisticsManagerDAO;
import org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConfigException;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsRuntimeException;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import org.apache.commons.lang3.text.StrTokenizer;

import static org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConstants.SEPARATOR;

/**
 * Class declaration
 *
 * @author SLR
 */
@Singleton
public class SilverStatisticsService implements SilverStatistics {

  private StatisticsConfig myStatsConfig;

  /**
   * @param type the statistic type (Access, Size, Volume, Connexion)
   * @param data the value to put in statistic
   */
  @Override
  public void putStats(StatType type, String data) {
    StrTokenizer stData = new StrTokenizer(data, SEPARATOR);
    List<String> dataArray = stData.getTokenList();
    if (myStatsConfig.isGoodDatas(type, dataArray)) {
      try(Connection myCon = DBUtil.openConnection()) {

        SilverStatisticsDAO.putDataStats(myCon, type, dataArray, myStatsConfig);
        if (!myCon.getAutoCommit()) {
          myCon.commit();
        }
      } catch (SQLException | StatisticsRuntimeException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
            "silverstatistics.MSG_ALIMENTATION_BD",
            "typeOfStats = " + type + ", dataArray = " + dataArray, e);
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
   *
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
