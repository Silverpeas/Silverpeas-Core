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
 * "https://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.core.silverstatistics.volume.model.StatDataType;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Common functionalities for statistics DAO
 *
 * @author mmoquillon
 */
public class AbstractSilverStatisticsDAO {

  protected AbstractSilverStatisticsDAO() {

  }

  /**
   * Inserts the specified statistics data of the given type into the referred datasource.
   *
   * @param con       a connection with the datasource
   * @param tableName the table into which the data will be inserted
   * @param type      the type of statistics
   * @param valueKeys the data to insert
   * @param conf      the statistics configuration.
   * @throws SQLException if error occurs while inserting the data into the datasource
   */
  protected static void insertData(Connection con, String tableName, StatType type,
      List<String> valueKeys, StatisticsConfig conf) throws SQLException {
    StringBuilder statementBuilder = new StringBuilder("INSERT INTO ");
    statementBuilder.append(tableName).append("(");


    Collection<String> theKeys = conf.getAllKeys(type);
    statementBuilder.append(String.join(",", theKeys));
    statementBuilder.append(") ");

    statementBuilder.append("VALUES(?");
    statementBuilder.append(",?".repeat(Math.max(0, conf.getNumberOfKeys(type) - 1)));
    statementBuilder.append(")");
    String statement = statementBuilder.toString();

    int i = 0;
    try (PreparedStatement prepStmt = con.prepareStatement(statement)) {
      for (String currentKey : theKeys) {
        i++;
        StatDataType currentType = StatDataType.valueOf(conf.getKeyType(type, currentKey));

        String tmpString = valueKeys.get(i - 1);
        if (currentType == StatDataType.DECIMAL) {
          setDecimalStatementParam(prepStmt, i, tmpString, currentKey, type, conf);
        }
        if (currentType == StatDataType.INTEGER) {
          setIntegerStatementParam(prepStmt, i, tmpString, currentKey, type, conf);
        }
        if (currentType == StatDataType.VARCHAR) {
          setVarcharStatementParam(prepStmt, i, tmpString, currentKey);
        }
      }
      prepStmt.executeUpdate();
    }
  }

  protected static Statements computeStatements(String tableName, StatType type,
      List<String> valueKeys, StatisticsConfig conf) {
    StringBuilder selectStatement = new StringBuilder("SELECT ");
    StringBuilder updateStatement = new StringBuilder("UPDATE ");
    updateStatement.append(tableName);
    updateStatement.append(" SET ");

    List<String> theKeys = conf.getAllKeys(type);
    Iterator<String> iteratorKeys = theKeys.iterator();
    prepareStatementHeader(selectStatement, updateStatement, tableName, iteratorKeys, type, conf);

    boolean stopputstat = false;
    boolean firstKeyInWhere = true;
    int k = -1;
    for (String keyNameCurrent : theKeys) {
      k++;
      if (conf.isCumulKey(type, keyNameCurrent)) {
        continue;
      }
      if (!firstKeyInWhere) {
        selectStatement.append(" AND ");
        updateStatement.append(" AND ");
      }
      selectStatement.append(keyNameCurrent);
      updateStatement.append(keyNameCurrent);
      StatDataType currentType = StatDataType.valueOf(conf.getKeyType(type, keyNameCurrent));
      if (currentType == StatDataType.DECIMAL) {
        stopputstat = !StringUtil.isLong(valueKeys.get(k));
        setStatements(selectStatement, updateStatement, k, valueKeys, false);
      } else if (currentType == StatDataType.INTEGER) {
        stopputstat = !StringUtil.isInteger(valueKeys.get(k));
        setStatements(selectStatement, updateStatement, k, valueKeys, false);
      } else if (currentType == StatDataType.VARCHAR) {
        if (keyNameCurrent.equals("dateStat")) {
          String dateFirstDayOfMonth = valueKeys.get(k).substring(0, 8);
          dateFirstDayOfMonth = dateFirstDayOfMonth + "01";
          selectStatement.append("='").append(dateFirstDayOfMonth).append("'");
          updateStatement.append("='").append(dateFirstDayOfMonth).append("'");
        } else {
          setStatements(selectStatement, updateStatement, k, valueKeys, true);
        }
      }
      firstKeyInWhere = false;
    }
    return new Statements(selectStatement, updateStatement, theKeys, stopputstat);
  }

  private static void prepareStatementHeader(StringBuilder selectStatement,
      StringBuilder updateStatement, String tableName, Iterator<String> iteratorKeys, StatType type,
      StatisticsConfig conf) {
    while (iteratorKeys.hasNext()) {
      String keyNameCurrent = iteratorKeys.next();
      selectStatement.append(keyNameCurrent);
      if (iteratorKeys.hasNext()) {
        selectStatement.append(",");
      }
      if (conf.isCumulKey(type, keyNameCurrent)) {
        updateStatement.append(keyNameCurrent);
        updateStatement.append("=");
        if (tableName.endsWith("Cumul")) {
          updateStatement.append("? ,");
        } else {
          updateStatement.append(keyNameCurrent).append("+? ,");
        }
      }
    }
    updateStatement.deleteCharAt(updateStatement.length() - 1);
    selectStatement.append(" FROM ").append(tableName).append(" WHERE ");
    updateStatement.append(" WHERE ");
  }

  private static void setStatements(StringBuilder selectStatement, StringBuilder updateStatement,
      int paramIndex, List<String> valueKeys, boolean isVarChar) {
    if (!StringUtil.isDefined(valueKeys.get(paramIndex))) {
      selectStatement.append("=" + "NULL");
      updateStatement.append("=" + "NULL");
    } else {
      selectStatement.append("=");
      updateStatement.append("=");
      if (isVarChar) {
        selectStatement.append("'").append(valueKeys.get(paramIndex)).append("'");
        updateStatement.append("'").append(valueKeys.get(paramIndex)).append("'");
      } else {
        selectStatement.append(valueKeys.get(paramIndex));
        updateStatement.append(valueKeys.get(paramIndex));
      }
    }
  }

  private static void setVarcharStatementParam(PreparedStatement prepStmt, int paramIndex,
      String paramValue, String currentKey) throws SQLException {
    if ("dateStat".equals(currentKey)) {
      String dateFirstDayOfMonth = paramValue.substring(0, 8) + "01";
      prepStmt.setString(paramIndex, dateFirstDayOfMonth);
    } else {
      if (!StringUtil.isDefined(paramValue)) {
        prepStmt.setNull(paramIndex, Types.VARCHAR);
      } else {
        prepStmt.setString(paramIndex, paramValue);
      }
    }
  }

  private static void setIntegerStatementParam(PreparedStatement prepStmt, int paramIndex,
      String paramValue, String currentKey, StatType statsType, StatisticsConfig conf)
      throws SQLException {
    try {
      if (!StringUtil.isDefined(paramValue)) {
        if (!conf.isCumulKey(statsType, currentKey)) {
          prepStmt.setNull(paramIndex, Types.INTEGER);
        } else {
          prepStmt.setInt(paramIndex, 0);
        }
      } else {
        int tmpInt = Integer.parseInt(paramValue);
        prepStmt.setInt(paramIndex, tmpInt);
      }
    } catch (NumberFormatException e) {
      prepStmt.setInt(paramIndex, 0);
    }
  }

  private static void setDecimalStatementParam(PreparedStatement prepStmt, int paramIndex,
      String paramValue, String currentKey, StatType statsType, StatisticsConfig conf)
      throws SQLException {
    try {
      if (!StringUtil.isDefined(paramValue)) {
        if (!conf.isCumulKey(statsType, currentKey)) {
          prepStmt.setNull(paramIndex, Types.DECIMAL);
        } else {
          prepStmt.setLong(paramIndex, 0);
        }
      } else {
        long tmpLong = Long.parseLong(paramValue);
        prepStmt.setLong(paramIndex, tmpLong);
      }
    } catch (NumberFormatException e) {
      prepStmt.setLong(paramIndex, 0);
    }
  }

  protected static class Statements {
    private final StringBuilder selectStatement;
    private final StringBuilder updateStatement;
    private final List<String> keys;
    private final boolean stopStatOp;

    public Statements(StringBuilder selectStatement, StringBuilder updateStatement,
        List<String> keys, boolean stopStatOp) {
      this.selectStatement = selectStatement;
      this.updateStatement = updateStatement;
      this.keys = keys;
      this.stopStatOp = stopStatOp;
    }

    public String getSelectStatement() {
      return selectStatement.toString();
    }

    public String getUpdateStatement() {
      return updateStatement.toString();
    }

    public boolean isStopStatOp() {
      return stopStatOp;
    }

    public List<String> getKeys() {
      return keys;
    }
  }
}
