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
package com.stratelia.silverpeas.silverstatistics.control;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.silverstatistics.model.SilverStatisticsConfigException;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.List;

/**
 * Class declaration
 * @author SLR
 */
public class SilverStatisticsEJB implements SessionBean {

  private static final String dbName = JNDINames.SILVERSTATISTICS_DATASOURCE;
  private static final long serialVersionUID = -2084739513469943886L;
  private SessionContext ctx;
  private StatisticsConfig myStatsConfig;

  /**
   * 
   * @param type
   * @param data 
   */
  public void putStats(StatType type, String data) {
    List<String> dataArray = new ArrayList<String>();
    Connection myCon = null;

    StringTokenizer stData = new StringTokenizer(data, SilverStatisticsConstants.SEPARATOR);

    while (stData.hasMoreTokens()) {
      dataArray.add(stData.nextToken());
    }

    if (myStatsConfig.isGoodDatas(type, dataArray)) {
      try {
        myCon = getConnection();
        SilverStatisticsDAO.putDataStats(myCon, type, dataArray, myStatsConfig);
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

  public void makeVolumeAlimentationForAllComponents() {
    SilverStatisticsVolumeAlimentation.makeVolumeAlimentationForAllComponents();
  }

  public void makeStatAllCumul() {
    SilverStatisticsManagerDAO.makeStatAllCumul(myStatsConfig);
  }

  /**
   * 
   * @return 
   */
  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new StatisticsRuntimeException("StatisticsEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Constructor declaration
   * @see
   */
  public SilverStatisticsEJB() {
  }

  /**
   * Method declaration
   * @see
   */
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
   * @see
   */
  @Override
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * @see
   */
  @Override
  public void ejbPassivate() {
  }

  /**
   * 
   * @param sc 
   */
  @Override
  public void setSessionContext(SessionContext sc) {
    ctx = sc;
    myStatsConfig = new StatisticsConfig();
    try {
      myStatsConfig.init();
    } catch (SilverStatisticsConfigException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsEJB.setSessionContext",
          "silverstatistics.MSG_CONFIG_FILE", e);
    }
  }
}
