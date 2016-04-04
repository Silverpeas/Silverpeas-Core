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

package org.silverpeas.core.silverstatistics.volume.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;

/**
 * This is the alimentation statistics DAO Object
 * @author sleroux
 */
public class SilverStatisticsDAO {

  /**
   * Insert data into statistic table
   * @param con the database connection
   * @param type the statistic type
   * @param valueKeys
   * @param conf
   * @throws SQLException
   */
  static void insertDataStats(Connection con, StatType type, List<String> valueKeys,
      StatisticsConfig conf) throws SQLException {
    StringBuilder insertStatementBuf = new StringBuilder("INSERT INTO ");
    insertStatementBuf.append(conf.getTableName(type)).append("(");
    PreparedStatement prepStmt = null;
    int i = 0;

    Collection<String> theKeys = conf.getAllKeys(type);
    insertStatementBuf.append(StringUtil.join(theKeys, ','));
    insertStatementBuf.append(") ");

    insertStatementBuf.append("VALUES(?");
    for (int j = 0; j < conf.getNumberOfKeys(type) - 1; j++) {
      insertStatementBuf.append(",?");
    }
    insertStatementBuf.append(")");
    String insertStatement = insertStatementBuf.toString();

    try {

      prepStmt = con.prepareStatement(insertStatement);
      for (String currentKey : theKeys) {
        i++;
        String currentType = conf.getKeyType(type, currentKey);
        if (currentType.equals("DECIMAL")) {
          long tmpLong;
          try {
            String tmpString = valueKeys.get(i - 1);
            if (!StringUtil.isDefined(tmpString)) {
              if (!conf.isCumulKey(type, currentKey)) {
                prepStmt.setNull(i, java.sql.Types.DECIMAL);
              } else {
                prepStmt.setLong(i, 0);
              }
            } else {
              tmpLong = Long.parseLong(tmpString);
              prepStmt.setLong(i, tmpLong);
            }
          } catch (NumberFormatException e) {
            prepStmt.setLong(i, 0);
          }
        }
        if (currentType.equals("INTEGER")) {
          int tmpInt;
          try {
            String tmpString = valueKeys.get(i - 1);
            if (!StringUtil.isDefined(tmpString)) {
              if (!conf.isCumulKey(type, currentKey)) {
                prepStmt.setNull(i, java.sql.Types.INTEGER);
              } else {
                prepStmt.setInt(i, 0);
              }
            } else {
              tmpInt = Integer.parseInt(tmpString);
              prepStmt.setInt(i, tmpInt);
            }
          } catch (NumberFormatException e) {
            prepStmt.setInt(i, 0);
          }
        }
        if (currentType.equals("VARCHAR")) {
          String tmpString = valueKeys.get(i - 1);
          if (!StringUtil.isDefined(tmpString)) {
            prepStmt.setNull(i, java.sql.Types.VARCHAR);
          } else {
            prepStmt.setString(i, valueKeys.get(i - 1));
          }
        }
      }
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Update or insert statistic inside statistic table defined inside conf parameter
   * @param con the database connection
   * @param type the statistic type
   * @param valueKeys
   * @param conf
   * @throws SQLException
   * @throws IOException
   */
  public static void putDataStats(Connection con, StatType type, List<String> valueKeys,
      StatisticsConfig conf) throws SQLException {
    StringBuilder selectStatementBuf = new StringBuilder("SELECT ");
    StringBuilder updateStatementBuf = new StringBuilder("UPDATE ");
    String tableName = conf.getTableName(type);
    Statement stmt;
    ResultSet rs = null;
    boolean rowExist = false;
    boolean firstKeyInWhere = true;
    boolean STOPPUTSTAT = false;
    int k = 0;
    int countCumulKey;
    int intToAdd;
    long longToAdd;
    PreparedStatement pstmt = null;
    updateStatementBuf.append(tableName);
    updateStatementBuf.append(" SET ");

    Collection<String> theKeys = conf.getAllKeys(type);
    Iterator<String> iteratorKeys = theKeys.iterator();
    while (iteratorKeys.hasNext()) {
      String keyNameCurrent = iteratorKeys.next();
      selectStatementBuf.append(keyNameCurrent);
      if (iteratorKeys.hasNext()) {
        selectStatementBuf.append(",");
      }
      if (conf.isCumulKey(type, keyNameCurrent)) {
        updateStatementBuf.append(keyNameCurrent);
        updateStatementBuf.append("=").append(keyNameCurrent).append("+? ,");
      }
    }

    updateStatementBuf.deleteCharAt(updateStatementBuf.length() - 1);
    selectStatementBuf.append(" FROM ").append(tableName).append(" WHERE ");
    updateStatementBuf.append(" WHERE ");
    iteratorKeys = theKeys.iterator();
    while (iteratorKeys.hasNext()) {
      String keyNameCurrent = iteratorKeys.next();
      if (!conf.isCumulKey(type, keyNameCurrent)) {
        if (!firstKeyInWhere) {
          selectStatementBuf.append(" AND ");
          updateStatementBuf.append(" AND ");
        }
        selectStatementBuf.append(keyNameCurrent);
        updateStatementBuf.append(keyNameCurrent);
        String currentType = conf.getKeyType(type, keyNameCurrent);
        if ("DECIMAL".equals(currentType)) {
          try {
            Long.parseLong(valueKeys.get(k));
          } catch (Exception e) {
            STOPPUTSTAT = true;
          }
          if (!StringUtil.isDefined(valueKeys.get(k))) {
            selectStatementBuf.append("=" + "NULL");
            updateStatementBuf.append("=" + "NULL");
          } else {
            selectStatementBuf.append("=").append(valueKeys.get(k));
            updateStatementBuf.append("=").append(valueKeys.get(k));
          }
        } else if ("INTEGER".equals(currentType)) {
          try {
            Integer.valueOf(valueKeys.get(k));
          } catch (Exception e) {
            STOPPUTSTAT = true;
          }
          if (!StringUtil.isDefined(valueKeys.get(k))) {
            selectStatementBuf.append("=" + "NULL");
            updateStatementBuf.append("=" + "NULL");
          } else {
            selectStatementBuf.append("=").append(valueKeys.get(k));
            updateStatementBuf.append("=").append(valueKeys.get(k));
          }
        } else if ("VARCHAR".equals(currentType)) {
          if (!StringUtil.isDefined(valueKeys.get(k))) {
            selectStatementBuf.append("=" + "NULL");
            updateStatementBuf.append("=" + "NULL");
          } else {
            selectStatementBuf.append("='").append(valueKeys.get(k)).append("'");
            updateStatementBuf.append("='").append(valueKeys.get(k)).append("'");
          }
        }
        firstKeyInWhere = false;
      }
      k++;
    }

    String selectStatement = selectStatementBuf.toString();
    String updateStatement = updateStatementBuf.toString();
    SilverTrace
        .info("silverstatistics", "SilverStatisticsDAO.putDataStats", "root.MSG_GEN_PARAM_VALUE",
            "selectStatement=" + selectStatement);
    SilverTrace
        .info("silverstatistics", "SilverStatisticsDAO.putDataStats", "root.MSG_GEN_PARAM_VALUE",
            "updateStatementBuf=" + updateStatementBuf);
    stmt = con.createStatement();

    try {
      if (!STOPPUTSTAT) {
        rs = stmt.executeQuery(selectStatement);

        while (rs.next()) {
          countCumulKey = 0;
          if (pstmt != null) {
            pstmt.close();
          }
          pstmt = con.prepareStatement(updateStatement);

          rowExist = true;
          iteratorKeys = theKeys.iterator();
          while (iteratorKeys.hasNext()) {
            String keyNameCurrent = iteratorKeys.next();

            if (conf.isCumulKey(type, keyNameCurrent)) {
              countCumulKey++;
              String currentType = conf.getKeyType(type, keyNameCurrent);
              if (currentType.equals("INTEGER")) {
                try {
                  intToAdd = Integer.parseInt(valueKeys.get(conf.indexOfKey(type, keyNameCurrent)));
                } catch (NumberFormatException e) {
                  intToAdd = 0;
                }
                pstmt.setInt(countCumulKey, intToAdd);
              }
              if (currentType.equals("DECIMAL")) {
                try {
                  longToAdd = Long.parseLong(valueKeys.get(conf.indexOfKey(type, keyNameCurrent)));
                } catch (NumberFormatException e) {
                  longToAdd = 0;
                }
                pstmt.setLong(countCumulKey, longToAdd);
              }
            }
          }
          pstmt.executeUpdate();
        }
      }
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(pstmt);
      if ((!STOPPUTSTAT) && (!rowExist)) {
        insertDataStats(con, type, valueKeys, conf);
      }
    }
  }
}
