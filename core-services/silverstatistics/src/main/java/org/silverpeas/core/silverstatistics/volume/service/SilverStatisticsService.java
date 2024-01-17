/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.volume.service;

import org.apache.commons.text.StringTokenizer;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.volume.dao.SilverStatisticsDAO;
import org.silverpeas.core.silverstatistics.volume.dao.SilverStatisticsManagerDAO;
import org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConfigException;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConstants.SEPARATOR;

@Service
@Singleton
public class SilverStatisticsService implements SilverStatistics {

  private final StatisticsConfig myStatsConfig;

  /**
   * @param type the statistic type (Access, Size, Volume, Connexion)
   * @param data the value to put in statistic
   */
  @Override
  public void putStats(StatType type, String data) {
    StringTokenizer stData = new StringTokenizer(data, SEPARATOR);
    List<String> dataArray = stData.getTokenList();
    if (myStatsConfig.areGoodData(type, dataArray)) {
      try(Connection myCon = DBUtil.openConnection()) {

        SilverStatisticsDAO.putDataStats(myCon, type, dataArray, myStatsConfig);
        if (!myCon.getAutoCommit()) {
          myCon.commit();
        }
      } catch (SQLException | StatisticsRuntimeException e) {
        SilverLogger.getLogger(this)
            .error("typeOfStats={0}, dataArray={1}", new Object[]{type, dataArray}, e);
      }
    } else {
      SilverLogger.getLogger(this).error("input data={0} for {1}", data, type);
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

  public SilverStatisticsService() {
    myStatsConfig = new StatisticsConfig();
    try {
      myStatsConfig.init();
    } catch (SilverStatisticsConfigException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

}
