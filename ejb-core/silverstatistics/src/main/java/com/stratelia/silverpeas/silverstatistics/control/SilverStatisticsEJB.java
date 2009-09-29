/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverstatistics.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.silverstatistics.ejb.SilverStatisticsDAO;
import com.stratelia.silverpeas.silverstatistics.model.SilverStatisticsConfigException;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * 
 * 
 * @author SLR
 */

public class SilverStatisticsEJB implements SessionBean {

  private static final String dbName = JNDINames.SILVERSTATISTICS_DATASOURCE;

  private SessionContext ctx;
  private StatisticsConfig myStatsConfig;

  /**
   * Method declaration
   * 
   * 
   * @param typeOfStats
   * @param data
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void putStats(String typeOfStats, String data) {
    ArrayList dataArray = new ArrayList();
    Connection myCon = null;

    StringTokenizer stData = new StringTokenizer(data,
        SilverStatisticsConstants.SEPARATOR);

    while (stData.hasMoreTokens()) {
      dataArray.add(stData.nextToken());
    }

    if (myStatsConfig.isGoodDatas(typeOfStats, dataArray)) {
      try {
        myCon = getConnection();
        SilverStatisticsDAO.putDataStats(myCon, typeOfStats, dataArray,
            myStatsConfig);
      } catch (SQLException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
            "silverstatistics.MSG_ALIMENTATION_BD", "typeOfStats = "
                + typeOfStats + ", dataArray = " + dataArray, e);
      } catch (StatisticsRuntimeException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsEJB.putStats",
            "MSG_CONNECTION_BD");
      } finally {
        freeConnection(myCon);
      }

    } else {
      SilverTrace
          .error("silverstatistics", "SilverStatisticsEJB.putStats",
              "MSG_CONFIG_DATAS", "data en entree=" + data + " pour "
                  + typeOfStats);
    }
  }

  public void makeVolumeAlimentationForAllComponents() {
    SilverStatisticsVolumeAlimentation.makeVolumeAlimentationForAllComponents();
  }

  public void makeStatAllCumul() {
    SilverStatisticsManagerDAO.makeStatAllCumul(myStatsConfig);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);

      return con;
    } catch (Exception e) {
      throw new StatisticsRuntimeException("StatisticsEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * 
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("statistics", "StatisticsEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SilverStatisticsEJB() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbCreate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param sc
   * 
   * @see
   */
  public void setSessionContext(SessionContext sc) {
    ctx = sc;
    myStatsConfig = new StatisticsConfig();
    try {
      if (myStatsConfig.init() != 0) {
        SilverTrace.error("silverstatistics",
            "SilverStatisticsEJB.setSessionContext",
            "silverstatistics.MSG_CONFIG_FILE");
      }
    } catch (SilverStatisticsConfigException e) {
      SilverTrace.error("silverstatistics",
          "SilverStatisticsEJB.setSessionContext",
          "silverstatistics.MSG_CONFIG_FILE", e);
    }
  }

}
