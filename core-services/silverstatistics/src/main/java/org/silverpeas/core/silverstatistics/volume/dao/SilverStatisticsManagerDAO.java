/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.volume.dao;

import org.jetbrains.annotations.NotNull;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.volume.model.StatDataType;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silverstatistics.volume.model.StatisticMode;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsRuntimeException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

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

  private SilverStatisticsManagerDAO() {

  }

  /**
   * @param con the database connection
   * @param statsType the statistic type
   * @param valueKeys
   * @param conf statistic database configuration
   * @throws SQLException
   */
  public static void insertDataStatsCumul(Connection con, StatType statsType, List<String> valueKeys,
      StatisticsConfig conf) throws SQLException {
    StringBuilder insertStatementBuf = new StringBuilder("INSERT INTO ");
    insertStatementBuf.append(conf.getTableName(statsType)).append("Cumul" + "(");
    String insertStatement;
    PreparedStatement prepStmt = null;
    int i = 0;

    Collection<String> theKeys = conf.getAllKeys(statsType);
    insertStatementBuf.append(StringUtil.join(theKeys, ','));
    insertStatementBuf.append(") ");

    insertStatementBuf.append("VALUES(?");
    for (int j = 0; j < conf.getNumberOfKeys(statsType) - 1; j++) {
      insertStatementBuf.append(",?");
    }
    insertStatementBuf.append(")");
    insertStatement = insertStatementBuf.toString();

    try {


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
              tmpLong = Long.valueOf(tmpString);
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
   * @param con the database connection
   * @param statsType the statistic type
   * @param valueKeys
   * @param conf statistic database configuration
   * @throws SQLException
   */
  public static void putDataStatsCumul(Connection con, StatType statsType, List<String> valueKeys,
      StatisticsConfig conf) throws SQLException {
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


      rs = stmt.executeQuery(selectStatement);
      String updateStatement = updateStatementBuf.toString();

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
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class).error(e);
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
   * @param con the database connection
   * @param statsType the statistic type
   * @param conf statistic database configuration
   * @throws SQLException
   */
  public static void makeStatCumul(Connection con, StatType statsType, StatisticsConfig conf) {
    String keyNameCurrent;
    String selectStatement = "SELECT * FROM " + conf.getTableName(statsType);
    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(selectStatement)) {
      Collection<String> theKeys = conf.getAllKeys(statsType);
      while (rs.next()) {
        List<String> valueKeys = new ArrayList<>();

        for (String theKey : theKeys) {
          keyNameCurrent = theKey;
          StatDataType currentType =
              StatDataType.valueOf(conf.getKeyType(statsType, keyNameCurrent));
          final String addToValueKeys = getValueKey(rs, keyNameCurrent, currentType);
          valueKeys.add(addToValueKeys);
        }
        putDataStatsCumul(con, statsType, valueKeys, conf);
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class)
          .error("Error while making stat cummul", e);
    }
  }

  @NotNull
  private static String getValueKey(final ResultSet rs, final String keyNameCurrent,
      final StatDataType currentType) throws SQLException {
    final String valueKey;
    switch (currentType) {
      case INTEGER:
        int tmpInt = rs.getInt(keyNameCurrent);
        if (rs.wasNull()) {
          valueKey = "";
        } else {
          valueKey = String.valueOf(tmpInt);
        }
        break;
      case DECIMAL:
        long longValue = rs.getLong(keyNameCurrent);
        if (rs.wasNull()) {
          valueKey = "";
        } else {
          valueKey = String.valueOf(longValue);
        }
        break;
      case VARCHAR:
        String value = rs.getString(keyNameCurrent);
        if (value == null) {
          valueKey = "";
        } else {
          valueKey = value;
        }
        break;
      default:
        valueKey = "";
        break;
    }
    return valueKey;
  }

  /**
   * @param con
   * @param statsType
   * @param conf
   * @throws SQLException
   */
  static void deleteTablesOfTheDay(Connection con, StatType statsType, StatisticsConfig conf) {
    String deleteStatement = "DELETE FROM " + conf.getTableName(statsType);
    try(PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.executeUpdate();
    } catch(SQLException e) {
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class)
          .error("Error while deleting tables of the day", e);
    }
  }

  /**
   * @param con
   * @param statsType
   * @param conf
   * @throws SQLException
   */
  static void purgeTablesCumul(Connection con, StatType statsType, StatisticsConfig conf) {
    StringBuilder deleteStatementBuf =
        new StringBuilder("DELETE FROM " + conf.getTableName(statsType) + "Cumul WHERE dateStat<");
    // compute the last date to delete from
    Calendar dateOfTheDay = Calendar.getInstance();
    dateOfTheDay.add(Calendar.MONTH, -(conf.getPurge(statsType)));
    deleteStatementBuf.append(
        getRequestDate(dateOfTheDay.get(Calendar.YEAR), dateOfTheDay.get(Calendar.MONTH) + 1));
    String deleteStatement = deleteStatementBuf.toString();

    try (final PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class)
          .error("Error while purging stat cumul", e);
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
    try (final Connection con = getConnection()) {
      if (conf != null && conf.isValidConfigFile()) {
        for (StatType currentType : conf.getAllTypes()) {
            purgeTablesCumul(con, currentType, conf);
            makeStatCumul(con, currentType, conf);
            deleteTablesOfTheDay(con, currentType, conf);
        }
      } else {
        if (conf == null) {
          SilverLogger.getLogger(SilverStatisticsManagerDAO.class).error("No config file provided");
        } else if (!conf.isValidConfigFile()) {
          SilverLogger.getLogger(SilverStatisticsManagerDAO.class).error("No valid config file");
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class).error(e);
    }
  }

  /**
   * @return
   */
  private static Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new StatisticsRuntimeException(e);
    }
  }
}
