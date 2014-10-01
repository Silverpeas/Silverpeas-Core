/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.jdbc;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
class DefaultJdbcSqlExecutor implements JdbcSqlExecutor {

  protected DefaultJdbcSqlExecutor() {
    // Hidden constructor
  }

  @Override
  public long selectCount(JdbcSqlQuery selectCountQueryBuilder) throws SQLException {
    Connection con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
      st = con.prepareStatement(selectCountQueryBuilder.getSqlQuery());
      setParameters(st, selectCountQueryBuilder.getParameters());
      rs = st.executeQuery();
      rs.next();
      long count = rs.getLong(1);
      if (rs.next()) {
        throw new IllegalArgumentException("select count execution error");
      }
      return count;
    } catch (SQLException e) {
      DBUtil.close(rs, st);
      DBUtil.close(con);
      throw e;
    }
  }

  @Override
  public <ROW_ENTITY> List<ROW_ENTITY> select(JdbcSqlQuery selectQueryBuilder,
      SelectResultRowProcess<ROW_ENTITY> process) throws SQLException {
    Connection con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
      st = con.prepareStatement(selectQueryBuilder.getSqlQuery());
      setParameters(st, selectQueryBuilder.getParameters());
      rs = st.executeQuery();
      List<ROW_ENTITY> entities = new ArrayList<ROW_ENTITY>();
      int i = 0;
      while (rs.next()) {
        int resultLimit = selectQueryBuilder.getConfiguration().getResultLimit();
        if (resultLimit > 0 && entities.size() >= resultLimit) {
          break;
        }
        ROW_ENTITY entity = process.currentRow(new ResultSetWrapper(rs, i));
        if (entity != null) {
          entities.add(entity);
        }
        i++;
      }
      return entities;
    } catch (SQLException e) {
      DBUtil.close(rs, st);
      DBUtil.close(con);
      throw e;
    }
  }

  @Override
  public long executeModify(final JdbcSqlQuery... modifySqlQueries) throws SQLException {
    return executeModify(Arrays.asList(modifySqlQueries));
  }

  @Override
  public long executeModify(List<JdbcSqlQuery> modifySqlQueries) throws SQLException {
    long nbUpdate = 0;
    Connection con = null;
    PreparedStatement prepStmt = null;
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
      for (JdbcSqlQuery modifyQuery : modifySqlQueries) {
        modifyQuery.finalizeBeforeExecution();
        try {
          prepStmt = con.prepareStatement(modifyQuery.getSqlQuery());
          setParameters(prepStmt, modifyQuery.getParameters());
          nbUpdate += prepStmt.executeUpdate();
        } finally {
          DBUtil.close(prepStmt);
        }
      }
    } catch (SQLException e) {
      DBUtil.close(prepStmt);
      DBUtil.close(con);
      throw e;
    }
    return nbUpdate;
  }

  /**
   * Centralization in order to sets the parameters on a prepare statement.
   * @param preparedStatement a prepared statement which parameters must be set.
   * @param statementParameters the parameters to set.
   * @throws java.sql.SQLException
   */
  @SuppressWarnings("unchecked")
  private static void setParameters(PreparedStatement preparedStatement, Object statementParameters)
      throws SQLException {
    final Collection<Object> parameters;
    if (statementParameters instanceof Object[]) {
      parameters = Arrays.asList((Object[]) statementParameters);
    } else if (statementParameters instanceof Collection) {
      parameters = (Collection) statementParameters;
    } else if (statementParameters != null) {
      parameters = Arrays.asList(statementParameters);
    } else {
      parameters = Collections.EMPTY_LIST;
    }
    int paramIndex = 1;
    for (Object parameter : parameters) {
      if (parameter == null) {
        preparedStatement.setObject(paramIndex, null);
      } else if (parameter instanceof String) {
        preparedStatement.setString(paramIndex, (String) parameter);
      } else if (parameter instanceof Enum) {
        preparedStatement.setString(paramIndex, ((Enum) parameter).name());
      } else if (parameter instanceof Integer) {
        preparedStatement.setInt(paramIndex, (Integer) parameter);
      } else if (parameter instanceof Long) {
        preparedStatement.setLong(paramIndex, (Long) parameter);
      } else if (parameter instanceof Timestamp) {
        preparedStatement.setTimestamp(paramIndex, (Timestamp) parameter);
      } else if (parameter instanceof Date) {
        preparedStatement.setDate(paramIndex, new java.sql.Date(((Date) parameter).getTime()));
      } else {
        try {
          Method idGetter = parameter.getClass().getDeclaredMethod("getId");
          String id = (String) idGetter.invoke(parameter);
          preparedStatement.setString(paramIndex, id);
        } catch (Exception e) {
          throw new IllegalArgumentException(
              "SQL parameter type not handled: " + parameter.getClass());
        }
      }
      paramIndex++;
    }
  }
}
