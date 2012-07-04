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

import com.google.common.base.Joiner;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silverstatistics.model.StatDataType;
import com.stratelia.silverpeas.silverstatistics.model.StatisticMode;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsRuntimeException;
import com.stratelia.silverpeas.silverstatistics.util.StatType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is the DAO Object for purge, agregat on the month
 * @author sleroux
 */
public class SilverStatisticsManagerDAO {

  private static final String DB_NAME = JNDINames.SILVERSTATISTICS_DATASOURCE;

  /**
   * Method declaration
   * @param con
   * @param statsType
   * @param valueKeys
   * @param conf
   * @throws SQLException
   * @see
   */
  static void insertDataStatsCumul(Connection con, StatType statsType,
      List<String> valueKeys, StatisticsConfig conf) throws SQLException, IOException {
    StringBuilder insertStatementBuf = new StringBuilder("INSERT INTO ");
    insertStatementBuf.append(conf.getTableName(statsType)).append("Cumul" + "(");
    String insertStatement;
    PreparedStatement prepStmt = null;
    int i = 0;

    Collection<String> theKeys = conf.getAllKeys(statsType);
    Joiner joiner = Joiner.on(",");
    joiner.appendTo(insertStatementBuf, theKeys);
    insertStatementBuf.append(") ");

    insertStatementBuf.append("VALUES(?");
    for (int j = 0; j < conf.getNumberOfKeys(statsType) - 1; j++) {
      insertStatementBuf.append(",?");
    }
    insertStatementBuf.append(")");
    insertStatement = insertStatementBuf.toString();

    try {

      SilverTrace.info("silverstatistics", "SilverStatisticsManagerDAO.insertDataStatsCumul",
          "root.MSG_GEN_PARAM_VALUE", "insertStatement=" + insertStatement);
      prepStmt = con.prepareStatement(insertStatement);
      for (String currentKey : theKeys) {
        i++;
        String currentType = conf.getKeyType(statsType, currentKey);
        if ("DECIMAL".equals(currentType)) {
          long tmpLong;
          try {
            String tmpString = valueKeys.get(i - 1);
            if (!StringUtil.isDefined(tmpString)) {
              if (!conf.isCumulKey(statsType, currentKey)) {
                prepStmt.setNull(i, java.sql.Types.DECIMAL);
              } else {
                prepStmt.setLong(i, 0);
              }
            } else {
              tmpLong = new Long(tmpString);
              prepStmt.setLong(i, tmpLong);
            }
          } catch (NumberFormatException e) {
            prepStmt.setLong(i, 0);
          }
        }
        if ("INTEGER".equals(currentType)) {
          int tmpInt;

          try {
            String tmpString = valueKeys.get(i - 1);

            if (!StringUtil.isDefined(tmpString)) {
              if (!conf.isCumulKey(statsType, currentKey)) {
                prepStmt.setNull(i, java.sql.Types.INTEGER);
              } else {
                prepStmt.setInt(i, 0);
              }
            } else {
              tmpInt = Integer.valueOf(tmpString);
              prepStmt.setInt(i, tmpInt);
            }
          } catch (NumberFormatException e) {
            prepStmt.setInt(i, 0);
          }
        }
        if ("VARCHAR".equals(currentType)) {
          if ("dateStat".equals(currentKey)) {
            String dateFirstDayOfMonth = valueKeys.get(i - 1).substring(0, 8);

            dateFirstDayOfMonth = dateFirstDayOfMonth + "01";
            prepStmt.setString(i, dateFirstDayOfMonth);
          } else {
            String tmpString = valueKeys.get(i - 1);

            if (!StringUtil.isDefined(tmpString)) {
              prepStmt.setNull(i, java.sql.Types.VARCHAR);
            } else {
              prepStmt.setString(i, valueKeys.get(i - 1));
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
   * @param con
   * @param statsType
   * @param valueKeys
   * @param conf
   * @throws SQLException
   * @throws IOException
   * @see
   */
  public static void putDataStatsCumul(Connection con, StatType statsType,
      List<String> valueKeys, StatisticsConfig conf) throws SQLException, IOException {
    StringBuilder selectStatementBuf = new StringBuilder("SELECT ");
    StringBuilder updateStatementBuf = new StringBuilder("UPDATE ");
    String tableName = conf.getTableName(statsType);

    Statement stmt = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    boolean rowExist = false;
    boolean firstKeyInWhere = true;
    int k = 0;
    int intToAdd;

    updateStatementBuf.append(tableName).append("Cumul");
    updateStatementBuf.append(" SET ");

    Collection<String> theKeys = conf.getAllKeys(statsType);
    Iterator<String> iteratorKeys = theKeys.iterator();

    while (iteratorKeys.hasNext()) {
      String keyNameCurrent = iteratorKeys.next();
      selectStatementBuf.append(keyNameCurrent);
      if (iteratorKeys.hasNext()) {
        selectStatementBuf.append(",");
      }
      if (conf.isCumulKey(statsType, keyNameCurrent)) {
        updateStatementBuf.append(keyNameCurrent);
        updateStatementBuf.append("=? ,");
      }
    }

    updateStatementBuf.deleteCharAt(updateStatementBuf.length() - 1);

    selectStatementBuf.append(" FROM ").append(tableName).append("Cumul" + " WHERE ");
    updateStatementBuf.append(" WHERE ");

    iteratorKeys = theKeys.iterator();
    while (iteratorKeys.hasNext()) {
      String keyNameCurrent = iteratorKeys.next();
      if (!conf.isCumulKey(statsType, keyNameCurrent)) {
        if (!firstKeyInWhere) {
          selectStatementBuf.append(" AND ");
          updateStatementBuf.append(" AND ");
        }
        selectStatementBuf.append(keyNameCurrent);
        updateStatementBuf.append(keyNameCurrent);
        StatDataType currentType = StatDataType.valueOf(conf.getKeyType(statsType, keyNameCurrent));
        switch (currentType) {
          case DECIMAL:
          case INTEGER:
            if (!StringUtil.isDefined(valueKeys.get(k))) {
              selectStatementBuf.append("=NULL");
              updateStatementBuf.append("=NULL");
            } else {
              selectStatementBuf.append("=").append(valueKeys.get(k));
              updateStatementBuf.append("=").append(valueKeys.get(k));
            }
            break;
          case VARCHAR:
            if (keyNameCurrent.equals("dateStat")) {
              String dateFirstDayOfMonth = valueKeys.get(k).substring(0, 8);
              dateFirstDayOfMonth = dateFirstDayOfMonth + "01";
              selectStatementBuf.append("='").append(dateFirstDayOfMonth).append("'");
              updateStatementBuf.append("='").append(dateFirstDayOfMonth).append("'");
            } else {
              if (!StringUtil.isDefined(valueKeys.get(k))) {
                selectStatementBuf.append("=NULL");
                updateStatementBuf.append("=NULL");
              } else {
                selectStatementBuf.append("='").append(valueKeys.get(k)).append("'");
                updateStatementBuf.append("='").append(valueKeys.get(k)).append("'");
              }
            }
            break;

        }
        firstKeyInWhere = false;
      }
      k++;
    }
    try {
      stmt = con.createStatement();
      String selectStatement = selectStatementBuf.toString();
      SilverTrace.info("silverstatistics", "SilverStatisticsManagerDAO.putDataStatsCumul",
          "root.MSG_GEN_PARAM_VALUE", "selectStatement=" + selectStatement);

      rs = stmt.executeQuery(selectStatement);
      String updateStatement = updateStatementBuf.toString();
      SilverTrace.info("silverstatistics", "SilverStatisticsManagerDAO.putDataStatsCumul",
          "root.MSG_GEN_PARAM_VALUE", "updateStatement=" + updateStatement);
      pstmt = con.prepareStatement(updateStatement);
      while (rs.next()) {
        rowExist = true;
        int countCumulKey = 0;
        iteratorKeys = theKeys.iterator();
        while (iteratorKeys.hasNext()) {
          String keyNameCurrent = iteratorKeys.next();
          if (conf.isCumulKey(statsType, keyNameCurrent)) {
            countCumulKey++;
            StatDataType currentType =
                StatDataType.valueOf(conf.getKeyType(statsType, keyNameCurrent));
            if (StatDataType.INTEGER == currentType) {
              intToAdd = Integer.valueOf(valueKeys.get(conf.indexOfKey(statsType, keyNameCurrent)));
              if (conf.getModeCumul(statsType) == StatisticMode.Add) {
                pstmt.setInt(countCumulKey, rs.getInt(keyNameCurrent) + intToAdd);
              }
              if (conf.getModeCumul(statsType) == StatisticMode.Replace) {
                pstmt.setInt(countCumulKey, intToAdd);
              }
            }
            if (StatDataType.DECIMAL == currentType) {
              Long myLong = Long.valueOf(valueKeys.get(conf.indexOfKey(statsType, keyNameCurrent)));

              if (conf.getModeCumul(statsType) == StatisticMode.Add) {
                pstmt.setLong(countCumulKey, (rs.getLong(keyNameCurrent) + myLong));
              }
              if (conf.getModeCumul(statsType) == StatisticMode.Replace) {
                pstmt.setLong(countCumulKey, myLong);
              }
            }
          }
        }
        pstmt.executeUpdate();
      }

    } catch (SQLException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.putDataStatsCumul",
          "silverstatistics.MSG_ALIMENTATION_BD", e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(pstmt);

      if (!rowExist) {
        insertDataStatsCumul(con, statsType, valueKeys, conf);
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param statsType
   * @param conf
   * @throws SQLException
   * @throws IOException
   * @see
   */
  public static void makeStatCumul(Connection con, StatType statsType, StatisticsConfig conf)
      throws SQLException, IOException {
    StringBuilder selectStatementBuf = new StringBuilder("SELECT * FROM ").append(
        conf.getTableName(statsType));
    String selectStatement;
    String keyNameCurrent;
    Statement stmt = null;
    ResultSet rs = null;

    selectStatement = selectStatementBuf.toString();
    SilverTrace.info("silverstatistics", "SilverStatisticsManagerDAO.makeStatCumul",
        "root.MSG_GEN_PARAM_VALUE", "selectStatement=" + selectStatement);
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      Collection<String> theKeys = conf.getAllKeys(statsType);
      String addToValueKeys = "";

      while (rs.next()) {
        List<String> valueKeys = new ArrayList<String>();

        for (String theKey : theKeys) {
          keyNameCurrent = theKey;
          StatDataType currentType = StatDataType.valueOf(
              conf.getKeyType(statsType, keyNameCurrent));
          switch (currentType) {
            case INTEGER:
              int tmpInt = rs.getInt(keyNameCurrent);
              if (rs.wasNull()) {
                addToValueKeys = "";
              } else {
                addToValueKeys = String.valueOf(tmpInt);
              }
              break;
            case DECIMAL:
              long longValue = rs.getLong(keyNameCurrent);
              if (rs.wasNull()) {
                addToValueKeys = "";
              } else {
                addToValueKeys = String.valueOf(longValue);
              }
              break;
            case VARCHAR:
              addToValueKeys = rs.getString(keyNameCurrent);
              if (addToValueKeys == null) {
                addToValueKeys = "";
              }
              break;
          }
          valueKeys.add(addToValueKeys);
        }
        putDataStatsCumul(con, statsType, valueKeys, conf);
      }
    } catch (SQLException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatCumul",
          "silverstatistics.MSG_ALIMENTATION_BD", e);
      throw e;
    } catch (IOException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatCumul",
          "silverstatistics.MSG_ALIMENTATION_BD", e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param statsType
   * @param conf
   * @throws SQLException
   * @see
   */
  static void deleteTablesOfTheDay(Connection con, StatType statsType, StatisticsConfig conf)
      throws SQLException {
    String deleteStatement = "DELETE FROM " + conf.getTableName(statsType);
    PreparedStatement prepStmt = null;

    try {
      SilverTrace.info("silverstatistics", "SilverStatisticsManagerDAO.deleteTablesOfTheDay",
          "root.MSG_GEN_PARAM_VALUE", "deleteStatement=" + deleteStatement);
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param statsType
   * @param conf
   * @throws SQLException
   * @see
   */
  static void purgeTablesCumul(Connection con, StatType statsType, StatisticsConfig conf) throws
      SQLException {
    StringBuilder deleteStatementBuf = new StringBuilder("DELETE FROM " + conf.getTableName(
        statsType) + "Cumul WHERE dateStat<");
    PreparedStatement prepStmt = null;

    // compute the last date to delete from
    Calendar dateOfTheDay = Calendar.getInstance();
    dateOfTheDay.add(Calendar.MONTH, -(conf.getPurge(statsType)));
    deleteStatementBuf.append(getRequestDate(dateOfTheDay.get(Calendar.YEAR),
        dateOfTheDay.get(Calendar.MONTH) + 1));

    String deleteStatement = deleteStatementBuf.toString();
    SilverTrace.info("silverstatistics", "SilverStatisticsManagerDAO.purgeTablesCumul",
        "root.MSG_GEN_PARAM_VALUE", "deleteStatement=" + deleteStatement);

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  static String getRequestDate(int year, int sMonth) {
    StringBuilder dateStringBuf = new StringBuilder();
    String month = String.valueOf(sMonth);
    if (month.length() < 2) {
      month = "0" + month;
    }

    dateStringBuf.append("'").append(String.valueOf(year));
    dateStringBuf.append("-").append(month);
    dateStringBuf.append("-01" + "'");
    return dateStringBuf.toString();
  }

  /**
   * Method declaration
   * @param conf
   */
  public static void makeStatAllCumul(StatisticsConfig conf) {
    Connection con = getConnection();
    try {
      if (conf != null && con != null && conf.isValidConfigFile()) {
        for (StatType currentType : conf.getAllTypes()) {
          try {
            purgeTablesCumul(con, currentType, conf);
          } catch (SQLException e) {
            SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
                "silverstatistics.MSG_PURGE_BD", e);
          }
          try {
            makeStatCumul(con, currentType, conf);
          } catch (SQLException e) {
            SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
                "silverstatistics.MSG_CUMUL_BD", e);
          } catch (IOException e) {
            SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
                "silverstatistics.MSG_CUMUL_BD", e);
          } finally {
            try {
              deleteTablesOfTheDay(con, currentType, conf);
            } catch (SQLException e) {
              SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
                  "silverstatistics.MSG_PURGE_BD", e);
            }
          }
        }
      } else {
        if (con == null) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
              "silverstatistics.MSG_CONNECTION_BD");
        }
        if (conf == null) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
              "silverstatistics.MSG_NO_CONFIG_FILE");
        } else if (!conf.isValidConfigFile()) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManagerDAO.makeStatAllCumul",
              "silverstatistics.MSG_CONFIG_FILE");
        }
      }
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private static Connection getConnection() {
    try {
      return DBUtil.makeConnection(DB_NAME);
    } catch (Exception e) {
      throw new StatisticsRuntimeException("SilverStatisticsManagerDAO.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          "DbName=" + DB_NAME,
          e);
    }
  }
}
