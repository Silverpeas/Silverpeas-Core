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

import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.*;
import java.util.List;

/**
 * This is the feeding statistics DAO Object
 *
 * @author sleroux
 */
public class SilverStatisticsDAO extends AbstractSilverStatisticsDAO {

  private static final String DECIMAL = "DECIMAL";
  private static final String INTEGER = "INTEGER";

  private SilverStatisticsDAO() {
  }

  /**
   * Insert data into statistic table
   *
   * @param con       the database connection
   * @param type      the statistic type
   * @param valueKeys a list of values
   * @param conf      the statistics configuration
   * @throws SQLException if an error occurs.
   */
  static void insertDataStats(Connection con, StatType type, List<String> valueKeys,
                              StatisticsConfig conf) throws SQLException {
    insertData(con, conf.getTableName(type), type, valueKeys, conf);
  }

  /**
   * Update or insert statistic inside statistic table defined inside conf parameter
   *
   * @param con       the database connection
   * @param type      the statistic type
   * @param valueKeys the value keys
   * @param conf      the statistics configuration
   * @throws SQLException if error occurs with the datasource
   */
  public static void putDataStats(Connection con, StatType type, List<String> valueKeys,
                                  StatisticsConfig conf) throws SQLException {

    String tableName = conf.getTableName(type);
    Statements statements = computeStatements(tableName, type, valueKeys, conf);

    if (statements.isStopStatOp()) {
      return;
    }

    String selectStatement = statements.getSelectStatement();
    String updateStatement = statements.getUpdateStatement();
    List<String> theKeys = statements.getKeys();
    boolean rowExist = false;
    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(selectStatement)) {
      while (rs.next()) {
        try (PreparedStatement pstmt = con.prepareStatement(updateStatement)) {
          rowExist = true;
          initUpdateStatement(pstmt, theKeys, valueKeys, type, conf);
          pstmt.executeUpdate();
        }
      }
    } finally {
      if (!rowExist) {
        insertDataStats(con, type, valueKeys, conf);
      }
    }
  }

  private static void initUpdateStatement(PreparedStatement pstmt, List<String> theKeys, List<String> valueKeys,
                                          StatType type, StatisticsConfig conf) throws SQLException {
    int countCumulKey = 0;
    for (String keyNameCurrent : theKeys) {
      if (!conf.isCumulKey(type, keyNameCurrent)) {
        continue;
      }
      countCumulKey++;
      String currentType = conf.getKeyType(type, keyNameCurrent);
      String currentValue = valueKeys.get(conf.indexOfKey(type, keyNameCurrent));
      if (currentType.equals(INTEGER)) {
        int intToAdd = StringUtil.isInteger(currentValue) ? Integer.parseInt(currentValue) : 0;
        pstmt.setInt(countCumulKey, intToAdd);
      }
      if (currentType.equals(DECIMAL)) {
        long longToAdd = StringUtil.isLong(currentValue) ? Long.parseLong(currentValue) : 0;
        pstmt.setLong(countCumulKey, longToAdd);
      }
    }
  }
}
