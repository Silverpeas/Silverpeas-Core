/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
// TODO : reporter dans CVS (done)

package com.stratelia.silverpeas.silverstatistics.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * This is the DAO Object for purge, agregat on the month
 * 
 * @author sleroux
 */
public class SilverStatisticsManagerDAO {

  private static final String DB_NAME = JNDINames.SILVERSTATISTICS_DATASOURCE;

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param StatsType
   * @param valueKeys
   * @param conf
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void insertDataStatsCumul(Connection con, String StatsType,
      ArrayList valueKeys, StatisticsConfig conf) throws SQLException {
    StringBuffer insertStatementBuf = new StringBuffer("INSERT INTO "
        + conf.getTableName(StatsType) + "Cumul" + "(");
    String insertStatement;
    PreparedStatement prepStmt = null;
    int i = 0;

    Collection theKeys = conf.getAllKeys(StatsType);
    Iterator iteratorKeys = theKeys.iterator();

    while (iteratorKeys.hasNext()) {
      insertStatementBuf.append((String) (iteratorKeys.next()));
      if (iteratorKeys.hasNext()) {
        insertStatementBuf.append(",");
      }
    }
    insertStatementBuf.append(") ");

    insertStatementBuf.append("VALUES(?");
    for (int j = 0; j < conf.getNumberOfKeys(StatsType) - 1; j++) {
      insertStatementBuf.append(",?");
    }
    insertStatementBuf.append(")");
    insertStatement = insertStatementBuf.toString();

    try {
      String currentKey = null;
      String currentType = null;

      SilverTrace.info("silverstatistics",
          "SilverStatisticsManagerDAO.insertDataStatsCumul",
          "root.MSG_GEN_PARAM_VALUE", "insertStatement=" + insertStatement);
      prepStmt = con.prepareStatement(insertStatement);
      iteratorKeys = theKeys.iterator();
      while (iteratorKeys.hasNext()) {
        i++;
        currentKey = (String) iteratorKeys.next();
        currentType = conf.getKeyType(StatsType, currentKey);
        if (currentType.equals("DECIMAL")) {
          long tmpLong = 0;

          try {
            String tmpString = (String) valueKeys.get(i - 1);

            if (tmpString.equals("") || tmpString == null) {
              if (!conf.isCumulKey(StatsType, currentKey)) {
                prepStmt.setNull(i, java.sql.Types.DECIMAL);
              } else {
                prepStmt.setLong(i, 0);
              }
            } else {
              tmpLong = new Long(tmpString).longValue();
              prepStmt.setLong(i, tmpLong);
            }
          } catch (NumberFormatException e) {
            prepStmt.setLong(i, 0);
          }
        }
        if (currentType.equals("INTEGER")) {
          int tmpInt = 0;

          try {
            String tmpString = (String) valueKeys.get(i - 1);

            if (tmpString.equals("") || tmpString == null) {
              if (!conf.isCumulKey(StatsType, currentKey)) {
                prepStmt.setNull(i, java.sql.Types.INTEGER);
              } else {
                prepStmt.setInt(i, 0);
              }
            } else {
              tmpInt = new Integer(tmpString).intValue();
              prepStmt.setInt(i, tmpInt);
            }
          } catch (NumberFormatException e) {
            prepStmt.setInt(i, 0);
          }
        }
        if (currentType.equals("VARCHAR")) {
          if (currentKey.equals("dateStat")) {
            String dateFirstDayOfMonth = ((String) valueKeys.get(i - 1))
                .substring(0, 8);

            dateFirstDayOfMonth = dateFirstDayOfMonth + "01";
            prepStmt.setString(i, dateFirstDayOfMonth);
          } else {
            String tmpString = (String) valueKeys.get(i - 1);

            if (tmpString.equals("") || tmpString == null) {
              prepStmt.setNull(i, java.sql.Types.VARCHAR);
            } else {
              prepStmt.setString(i, (String) valueKeys.get(i - 1));
            }
          }
        }
      }
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param StatsType
   * @param valueKeys
   * @param conf
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void putDataStatsCumul(Connection con, String StatsType,
      ArrayList valueKeys, StatisticsConfig conf) throws SQLException {
    StringBuffer selectStatementBuf = new StringBuffer("SELECT ");
    StringBuffer updateStatementBuf = new StringBuffer("UPDATE ");
    String tableName = conf.getTableName(StatsType);
    String selectStatement;
    String updateStatement;
    String keyNameCurrent;
    String currentType;
    Statement stmt = null;
    ResultSet rs = null;
    boolean rowExist = false;
    boolean firstKeyInWhere = true;
    int k = 0;
    int countCumulKey = 0;
    int intToAdd = 0;
    PreparedStatement pstmt = null;

    updateStatementBuf.append(tableName + "Cumul");
    updateStatementBuf.append(" SET ");

    Collection theKeys = conf.getAllKeys(StatsType);
    Iterator iteratorKeys = theKeys.iterator();

    while (iteratorKeys.hasNext()) {
      keyNameCurrent = (String) iteratorKeys.next();
      selectStatementBuf.append(keyNameCurrent);
      if (iteratorKeys.hasNext()) {
        selectStatementBuf.append(",");
      }
      if (conf.isCumulKey(StatsType, keyNameCurrent)) {
        updateStatementBuf.append(keyNameCurrent);
        updateStatementBuf.append("=? ,");
      }
    }

    updateStatementBuf.deleteCharAt(updateStatementBuf.length() - 1);

    selectStatementBuf.append(" FROM " + tableName + "Cumul" + " WHERE ");
    updateStatementBuf.append(" WHERE ");

    iteratorKeys = theKeys.iterator();
    while (iteratorKeys.hasNext()) {
      keyNameCurrent = (String) iteratorKeys.next();
      if (!conf.isCumulKey(StatsType, keyNameCurrent)) {
        if (!firstKeyInWhere) {
          selectStatementBuf.append(" AND ");
          updateStatementBuf.append(" AND ");
        }
        selectStatementBuf.append(keyNameCurrent);
        updateStatementBuf.append(keyNameCurrent);
        currentType = conf.getKeyType(StatsType, keyNameCurrent);
        if (currentType.equals("DECIMAL")) {
          if (((String) valueKeys.get(k)).equals("")
              || valueKeys.get(k) == null) {
            selectStatementBuf.append("=" + "NULL");
            updateStatementBuf.append("=" + "NULL");
          } else {
            selectStatementBuf.append("=" + (String) valueKeys.get(k));
            updateStatementBuf.append("=" + (String) valueKeys.get(k));
          }
        }
        if (currentType.equals("INTEGER")) {
          if (((String) valueKeys.get(k)).equals("")
              || valueKeys.get(k) == null) {
            selectStatementBuf.append("=" + "NULL");
            updateStatementBuf.append("=" + "NULL");
          } else {
            selectStatementBuf.append("=" + (String) valueKeys.get(k));
            updateStatementBuf.append("=" + (String) valueKeys.get(k));
          }
        }
        if (currentType.equals("VARCHAR")) {
          if (keyNameCurrent.equals("dateStat")) {
            String dateFirstDayOfMonth = ((String) valueKeys.get(k)).substring(
                0, 8);

            dateFirstDayOfMonth = dateFirstDayOfMonth + "01";
            selectStatementBuf.append("='" + dateFirstDayOfMonth + "'");
            updateStatementBuf.append("='" + dateFirstDayOfMonth + "'");
          } else {
            if (((String) valueKeys.get(k)).equals("")
                || valueKeys.get(k) == null) {
              selectStatementBuf.append("=" + "NULL");
              updateStatementBuf.append("=" + "NULL");
            } else {
              selectStatementBuf.append("='" + (String) valueKeys.get(k) + "'");
              updateStatementBuf.append("='" + (String) valueKeys.get(k) + "'");
            }
          }
        }
        firstKeyInWhere = false;
      }
      k++;
    }

    selectStatement = selectStatementBuf.toString();
    updateStatement = updateStatementBuf.toString();
    SilverTrace.info("silverstatistics",
        "SilverStatisticsManagerDAO.putDataStatsCumul",
        "root.MSG_GEN_PARAM_VALUE", "selectStatement=" + selectStatement);
    SilverTrace.info("silverstatistics",
        "SilverStatisticsManagerDAO.putDataStatsCumul",
        "root.MSG_GEN_PARAM_VALUE", "updateStatement=" + updateStatement);
    stmt = con.createStatement();

    try {
      rs = stmt.executeQuery(selectStatement);

      while (rs.next()) {

        pstmt = con.prepareStatement(updateStatement);

        rowExist = true;
        countCumulKey = 0;
        iteratorKeys = theKeys.iterator();
        while (iteratorKeys.hasNext()) {
          keyNameCurrent = (String) iteratorKeys.next();

          if (conf.isCumulKey(StatsType, keyNameCurrent)) {
            countCumulKey++;
            currentType = conf.getKeyType(StatsType, keyNameCurrent);
            if (currentType.equals("INTEGER")) {
              intToAdd = new Integer((String) valueKeys.get(conf.indexOfKey(
                  StatsType, keyNameCurrent))).intValue();
              if ((conf.getModeCumul(StatsType))
                  .equals(StatisticsConfig.MODEADD)) {
                pstmt.setInt(countCumulKey, rs.getInt(keyNameCurrent)
                    + intToAdd);
              }
              if ((conf.getModeCumul(StatsType))
                  .equals(StatisticsConfig.MODEREPLACE)) {
                pstmt.setInt(countCumulKey, intToAdd);
              }
            }
            if (currentType.equals("DECIMAL")) {
              Long myLong = new Long((String) valueKeys.get(conf.indexOfKey(
                  StatsType, keyNameCurrent)));

              if ((conf.getModeCumul(StatsType))
                  .equals(StatisticsConfig.MODEADD)) {
                pstmt.setLong(countCumulKey,
                    (rs.getLong(keyNameCurrent) + myLong.longValue()));
              }
              if ((conf.getModeCumul(StatsType))
                  .equals(StatisticsConfig.MODEREPLACE)) {
                pstmt.setLong(countCumulKey, myLong.longValue());
              }
            }
          }
        }
        pstmt.executeUpdate();
      }

    } catch (SQLException e) {
      SilverTrace.error("silverstatistics",
          "SilverStatisticsManagerDAO.putDataStatsCumul",
          "silverstatistics.MSG_ALIMENTATION_BD", e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(pstmt);

      if (!rowExist) {
        insertDataStatsCumul(con, StatsType, valueKeys, conf);
      }
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param StatsType
   * @param conf
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void makeStatCumul(Connection con, String StatsType,
      StatisticsConfig conf) throws SQLException {
    StringBuffer selectStatementBuf = new StringBuffer("SELECT * FROM "
        + conf.getTableName(StatsType));
    String selectStatement;
    String keyNameCurrent;
    String currentType;
    Statement stmt = null;
    ResultSet rs = null;

    selectStatement = selectStatementBuf.toString();
    SilverTrace.info("silverstatistics",
        "SilverStatisticsManagerDAO.makeStatCumul", "root.MSG_GEN_PARAM_VALUE",
        "selectStatement=" + selectStatement);
    stmt = con.createStatement();

    try {
      rs = stmt.executeQuery(selectStatement);
      Collection theKeys = conf.getAllKeys(StatsType);
      Iterator iteratorKeys = null;
      String addToValueKeys = "";

      while (rs.next()) {
        ArrayList valueKeys = new ArrayList();

        iteratorKeys = theKeys.iterator();
        while (iteratorKeys.hasNext()) {
          keyNameCurrent = (String) iteratorKeys.next();
          currentType = conf.getKeyType(StatsType, keyNameCurrent);
          if (currentType.equals("INTEGER")) {
            int tmpInt = rs.getInt(keyNameCurrent);

            if (rs.wasNull()) {
              addToValueKeys = "";
            } else {
              addToValueKeys = (new Integer(tmpInt)).toString();
            }

          }
          if (currentType.equals("DECIMAL")) {
            long tmplong = rs.getLong(keyNameCurrent);

            if (rs.wasNull()) {
              addToValueKeys = "";
            } else {
              addToValueKeys = (new Long(tmplong)).toString();
            }
          }
          if (currentType.equals("VARCHAR")) {
            addToValueKeys = rs.getString(keyNameCurrent);
            if (addToValueKeys == null) {
              addToValueKeys = "";
            }
          }
          valueKeys.add(addToValueKeys);
        }
        putDataStatsCumul(con, StatsType, valueKeys, conf);
      }
    } catch (SQLException e) {
      SilverTrace.error("silverstatistics",
          "SilverStatisticsManagerDAO.makeStatCumul",
          "silverstatistics.MSG_ALIMENTATION_BD", e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param StatsType
   * @param conf
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void deleteTablesOfTheDay(Connection con, String StatsType,
      StatisticsConfig conf) throws SQLException {
    String deleteStatement = "delete from " + conf.getTableName(StatsType);
    PreparedStatement prepStmt = null;

    try {
      SilverTrace.info("silverstatistics",
          "SilverStatisticsManagerDAO.deleteTablesOfTheDay",
          "root.MSG_GEN_PARAM_VALUE", "deleteStatement=" + deleteStatement);
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param StatsType
   * @param conf
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void purgeTablesCumul(Connection con, String StatsType,
      StatisticsConfig conf) throws SQLException {
    StringBuffer deleteStatementBuf = new StringBuffer("delete from "
        + conf.getTableName(StatsType) + "Cumul where dateStat<");
    String deleteStatement = null;
    PreparedStatement prepStmt = null;

    // compute the last date to delete from
    Calendar dateOfTheDay = Calendar.getInstance();
    dateOfTheDay.add(Calendar.MONTH, -(conf.getPurge(StatsType)));
    deleteStatementBuf.append(getRequestDate(dateOfTheDay.get(Calendar.YEAR),
        dateOfTheDay.get(Calendar.MONTH) + 1));

    deleteStatement = deleteStatementBuf.toString();
    SilverTrace.info("silverstatistics",
        "SilverStatisticsManagerDAO.purgeTablesCumul",
        "root.MSG_GEN_PARAM_VALUE", "deleteStatement=" + deleteStatement);

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static String getRequestDate(int sYear, int sMonth) {
    StringBuffer dateStringBuf = new StringBuffer();
    String month = (new Integer(sMonth)).toString();
    if (month.length() < 2) {
      month = "0" + month;
    }

    dateStringBuf.append("'" + ((new Integer(sYear)).toString()));
    dateStringBuf.append("-" + month);
    dateStringBuf.append("-01" + "'");
    return dateStringBuf.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @param conf
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void makeStatAllCumul(StatisticsConfig conf) {
    Connection con = getConnection();

    if ((conf != null) && (con != null) && (conf.isValidConfigFile())) {
      String currentType = "";
      Collection allTypes = conf.getAllTypes();
      Iterator iteratorType = allTypes.iterator();

      while (iteratorType.hasNext()) {
        currentType = (String) iteratorType.next();

        try {
          purgeTablesCumul(con, currentType, conf);
        } catch (SQLException e) {
          SilverTrace.error("silverstatistics",
              "SilverStatisticsManagerDAO.makeStatAllCumul",
              "silverstatistics.MSG_PURGE_BD", e);
        }

        try {
          makeStatCumul(con, currentType, conf);
        } catch (SQLException e) {
          SilverTrace.error("silverstatistics",
              "SilverStatisticsManagerDAO.makeStatAllCumul",
              "silverstatistics.MSG_CUMUL_BD", e);
        } finally {
          try {
            deleteTablesOfTheDay(con, currentType, conf);
          } catch (SQLException e) {
            SilverTrace.error("silverstatistics",
                "SilverStatisticsManagerDAO.makeStatAllCumul",
                "silverstatistics.MSG_PURGE_BD", e);
          }
        }
      }
    } else {
      if (con == null) {
        SilverTrace.error("silverstatistics",
            "SilverStatisticsManagerDAO.makeStatAllCumul",
            "silverstatistics.MSG_CONNECTION_BD");
      }
      if (conf == null) {
        SilverTrace.error("silverstatistics",
            "SilverStatisticsManagerDAO.makeStatAllCumul",
            "silverstatistics.MSG_NO_CONFIG_FILE");
      } else if (conf.isValidConfigFile() == false) {
        SilverTrace.error("silverstatistics",
            "SilverStatisticsManagerDAO.makeStatAllCumul",
            "silverstatistics.MSG_CONFIG_FILE");
      }
    }
    freeConnection(con);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private static Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(DB_NAME);

      return con;
    } catch (Exception e) {
      throw new StatisticsRuntimeException(
          "SilverStatisticsManagerDAO.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          "DbName=" + DB_NAME, e);
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
  private static void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("silverstatistics",
            "SilverStatisticsPeasDAOConnexion.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

}
