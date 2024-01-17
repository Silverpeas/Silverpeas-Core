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
package org.silverpeas.core.silverstatistics.volume.dao;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.volume.model.*;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.*;

/**
 * This is the DAO Object for purge, agregat on the month
 *
 * @author sleroux
 */
public class SilverStatisticsManagerDAO extends AbstractSilverStatisticsDAO {

  private static final String CUMUL = "Cumul";

  private SilverStatisticsManagerDAO() {

  }

  /**
   * Inserts cumulative statistic data.
   *
   * @param con       the database connection
   * @param statsType the statistic type
   * @param valueKeys the keys on the values.
   * @param conf      statistic database configuration
   * @throws SQLException if an error occurs while inserting data in the database
   */
  public static void insertDataStatsCumul(Connection con, StatType statsType, List<String> valueKeys,
                                          StatisticsConfig conf) throws SQLException {
    insertData(con, conf.getTableName(statsType) + CUMUL, statsType, valueKeys, conf);
  }

  /**
   * @param con       the database connection
   * @param statsType the statistic type
   * @param valueKeys keys on values
   * @param conf      statistic database configuration
   * @throws SQLException if an error occurs while putting data in the database
   */
  public static void putDataStatsCumul(Connection con, StatType statsType, List<String> valueKeys,
                                       StatisticsConfig conf) throws SQLException {
    String tableName = conf.getTableName(statsType);
    Statements statements = computeStatements(tableName + CUMUL, statsType, valueKeys, conf);
    String selectStatement = statements.getSelectStatement();
    String updateStatement = statements.getUpdateStatement();
    List<String> theKeys = statements.getKeys();
    boolean rowExist = false;
    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(selectStatement)) {
      try (PreparedStatement pstmt = con.prepareStatement(updateStatement)) {
        while (rs.next()) {
          rowExist = true;
          initUpdateStatement(pstmt, rs, statsType, theKeys, valueKeys, conf);
          pstmt.executeUpdate();
        }
      }

    } catch (SQLException e) {
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class).error(e);
      throw e;
    } finally {
      if (!rowExist) {
        insertDataStatsCumul(con, statsType, valueKeys, conf);
      }
    }
  }

  private static void initUpdateStatement(PreparedStatement pstmt, ResultSet rs, StatType statsType,
                                          List<String> theKeys, List<String> valueKeys, StatisticsConfig conf)
      throws SQLException {
    int countCumulKey = 0;
    for (String keyNameCurrent : theKeys) {
      if (conf.isCumulKey(statsType, keyNameCurrent)) {
        countCumulKey++;
        StatDataType currentType =
            StatDataType.valueOf(conf.getKeyType(statsType, keyNameCurrent));
        if (StatDataType.INTEGER == currentType) {
          setUpdateStatementIntParam(pstmt, rs, statsType, valueKeys, conf, countCumulKey, keyNameCurrent);
        }
        if (StatDataType.DECIMAL == currentType) {
          setUpdateStatementLongParam(pstmt, rs, statsType, valueKeys, conf, countCumulKey, keyNameCurrent);
        }
      }
    }
  }

  private static void setUpdateStatementLongParam(PreparedStatement pstmt, ResultSet rs, StatType statsType, List<String> valueKeys, StatisticsConfig conf, int countCumulKey, String keyNameCurrent) throws SQLException {
    long myLong = Long.parseLong(valueKeys.get(conf.indexOfKey(statsType, keyNameCurrent)));

    if (conf.getModeCumul(statsType) == StatisticMode.Add) {
      pstmt.setLong(countCumulKey, (rs.getLong(keyNameCurrent) + myLong));
    }
    if (conf.getModeCumul(statsType) == StatisticMode.Replace) {
      pstmt.setLong(countCumulKey, myLong);
    }
  }

  private static void setUpdateStatementIntParam(PreparedStatement pstmt, ResultSet rs, StatType statsType, List<String> valueKeys, StatisticsConfig conf, int countCumulKey, String keyNameCurrent) throws SQLException {
    int intToAdd = Integer.parseInt(valueKeys.get(conf.indexOfKey(statsType, keyNameCurrent)));
    if (conf.getModeCumul(statsType) == StatisticMode.Add) {
      pstmt.setInt(countCumulKey, rs.getInt(keyNameCurrent) + intToAdd);
    }
    if (conf.getModeCumul(statsType) == StatisticMode.Replace) {
      pstmt.setInt(countCumulKey, intToAdd);
    }
  }

  /**
   * @param con       the database connection
   * @param statsType the statistic type
   * @param conf      statistic database configuration
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

  @Nonnull
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
        valueKey = Objects.requireNonNullElse(value, "");
        break;
      default:
        valueKey = "";
        break;
    }
    return valueKey;
  }

  static void deleteTablesOfTheDay(Connection con, StatType statsType, StatisticsConfig conf) {
    String deleteStatement = "DELETE FROM " + conf.getTableName(statsType);
    try (PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      SilverLogger.getLogger(SilverStatisticsManagerDAO.class)
          .error("Error while deleting tables of the day", e);
    }
  }

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

    dateStringBuf.append("'").append(year);
    dateStringBuf.append("-").append(month);
    dateStringBuf.append("-01" + "'");
    return dateStringBuf.toString();
  }

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

  private static Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new StatisticsRuntimeException(e);
    }
  }
}
