/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.persistence.jdbc.sql;

import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.persistence.jdbc.ConnectionPool;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
class DefaultJdbcSqlExecutor implements JdbcSqlExecutor {

  private static final String SQL_REQUEST = ". SQL request: ";

  protected DefaultJdbcSqlExecutor() {
    // Hidden constructor
  }

  @Override
  public long selectCount(JdbcSqlQuery selectCountQueryBuilder) throws SQLException {
    try (Connection con = ConnectionPool.getConnection()) {
      return selectCount(con, selectCountQueryBuilder);
    }
  }

  @Override
  public long selectCount(final Connection con, final JdbcSqlQuery selectCountQueryBuilder)
      throws SQLException {
    try (PreparedStatement st = con.prepareStatement(selectCountQueryBuilder.getSqlQuery())) {
      setParameters(st, selectCountQueryBuilder.getParameters());
      try (ResultSet rs = st.executeQuery()) {
        rs.next();
        long count = rs.getLong(1);
        if (rs.next()) {
          throw new IllegalArgumentException("select count execution error");
        }
        return count;
      } catch (SQLException e) {
        SilverLogger.getLogger(this)
            .debug(e.getMessage() + SQL_REQUEST + selectCountQueryBuilder.getSqlQuery());
        throw e;
      }
    }
  }

  @Override
  public <R> ListSlice<R> select(JdbcSqlQuery selectQuery, SelectResultRowProcess<R> process)
      throws SQLException {
    try (Connection con = ConnectionPool.getConnection()) {
      return select(con, selectQuery, process);
    }
  }

  @Override
  public <R> ListSlice<R> select(final Connection con, final JdbcSqlQuery selectQuery,
      final SelectResultRowProcess<R> process) throws SQLException {
    JdbcSqlQuery.Configuration queryConf = selectQuery.getConfiguration();
    try (PreparedStatement st = queryConf.isFirstResultScrolled() ?
        con.prepareStatement(selectQuery.getSqlQuery(), ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY) : con.prepareStatement(selectQuery.getSqlQuery())) {
      setParameters(st, selectQuery.getParameters());
      try (ResultSet rs = st.executeQuery()) {
        return fetchEntities(rs, process, queryConf);
      } catch (SQLException e) {
        SilverLogger.getLogger(this)
            .debug(e.getMessage() + SQL_REQUEST + selectQuery.getSqlQuery());
        throw e;
      }
    }
  }

  @Transactional(Transactional.TxType.MANDATORY)
  @Override
  public long executeModify(final JdbcSqlQuery... modifySqlQueries) throws SQLException {
    return executeModify(Arrays.asList(modifySqlQueries));
  }

  @Transactional(Transactional.TxType.MANDATORY)
  @Override
  public long executeModify(final Connection con, final JdbcSqlQuery... modifySqlQueries)
      throws SQLException {
    return executeModify(con, Arrays.asList(modifySqlQueries));
  }

  @Transactional(Transactional.TxType.MANDATORY)
  @Override
  public long executeModify(List<JdbcSqlQuery> modifySqlQueries) throws SQLException {
    try (Connection con = ConnectionPool.getConnection()) {
      return executeModify(con, modifySqlQueries);
    }
  }

  @Transactional(Transactional.TxType.MANDATORY)
  @Override
  public long executeModify(final Connection con, final List<JdbcSqlQuery> modifySqlQueries)
      throws SQLException {
    long nbUpdate = 0;
    for (JdbcSqlQuery modifyQuery : modifySqlQueries) {
      modifyQuery.finalizeBeforeExecution();
      try (PreparedStatement prepStmt = con.prepareStatement(modifyQuery.getSqlQuery())) {
        setParameters(prepStmt, modifyQuery.getParameters());
        nbUpdate += prepStmt.executeUpdate();
      } catch (SQLException e) {
        SilverLogger.getLogger(this)
            .debug(e.getMessage() + SQL_REQUEST + modifyQuery.getSqlQuery());
        throw e;
      }
    }
    return nbUpdate;
  }

  private <R> ListSlice<R> fetchEntities(final ResultSet rs,
      final SelectResultRowProcess<R> process, final JdbcSqlQuery.Configuration queryConf)
      throws SQLException {
    final ResultSetWrapper rsw = new ResultSetWrapper(rs);
    int idx = queryConf.getOffset();
    if (queryConf.isFirstResultScrolled()) {
      rsw.next();
      rsw.relative(idx - 1);
    }
    final int lastIdx =
        queryConf.isResultCountLimited() ? idx + queryConf.getResultLimit() - 1 : 0;
    ListSlice<R> entities = new ListSlice<>(idx, lastIdx);
    int originalSize = idx;
    for (;rsw.next(); originalSize++) {
      if (!queryConf.isResultCountLimited() ||
          (queryConf.isResultCountLimited() && entities.size() < queryConf.getResultLimit())) {
        rsw.setCurrentRowIndex(idx++);
        R entity = process.currentRow(rsw);
        if (entity != null) {
          entities.add(entity);
        }
      }
    }
    entities.setOriginalListSize(originalSize);
    return entities;
  }

  /**
   * Centralization in order to sets the parameters on a prepare statement.
   * @param preparedStatement a prepared statement which parameters must be set.
   * @param statementParameters the parameters to set.
   * @throws java.sql.SQLException on SQL error.
   */
  private static void setParameters(PreparedStatement preparedStatement, Object statementParameters)
      throws SQLException {
    final Collection<Object> parameters = getParameters(statementParameters);
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
      } else if (parameter instanceof BigInteger) {
        preparedStatement.setBigDecimal(paramIndex, new BigDecimal((BigInteger) parameter));
      } else if (parameter instanceof BigDecimal) {
        preparedStatement.setBigDecimal(paramIndex, (BigDecimal) parameter);
      } else if (parameter instanceof Timestamp) {
        preparedStatement.setTimestamp(paramIndex, (Timestamp) parameter);
      } else if (isADateTime(parameter)) {
        preparedStatement.setTimestamp(paramIndex,
            new java.sql.Timestamp(toInstant(parameter).toEpochMilli()));
      } else if (isADate(parameter)) {
        preparedStatement.setDate(paramIndex,
            new java.sql.Date(toInstant(parameter).toEpochMilli()));
      } else if (parameter instanceof Blob) {
        preparedStatement.setBlob(paramIndex, (Blob) parameter);
      } else if (parameter instanceof Clob) {
        preparedStatement.setClob(paramIndex, (Clob) parameter);
      } else {
        setObjectIdentifier(preparedStatement, paramIndex, parameter);
      }
      paramIndex++;
    }
  }

  private static void setObjectIdentifier(final PreparedStatement preparedStatement,
      final int paramIndex, final Object parameter) throws SQLException {
    try {
      Method idGetter = parameter.getClass().getDeclaredMethod("getId");
      String id = (String) idGetter.invoke(parameter);
      preparedStatement.setString(paramIndex, id);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          "SQL parameter type not handled: " + parameter.getClass(), e);
    }
  }

  private static boolean isADate(final Object parameter) {
    return parameter instanceof Date || parameter instanceof LocalDate;
  }

  private static boolean isADateTime(final Object parameter) {
    if (parameter instanceof DateTime) {
      return true;
    }
    if (parameter instanceof Instant) {
      return true;
    }
    if (parameter instanceof LocalDateTime) {
      return true;
    }
    if (parameter instanceof OffsetDateTime) {
      return true;
    }
    return parameter instanceof ZonedDateTime;
  }

  private static Instant toInstant(final Object parameter) {
    try {
      if (parameter instanceof Instant) {
        return (Instant) parameter;
      }
      Method toInstant = parameter.getClass().getMethod("toInstant");
      return (Instant) toInstant.invoke(parameter);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          "Date or date time parameter expected. But is " + parameter.getClass(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static Collection<Object> getParameters(final Object statementParameters) {
    final Collection<Object> parameters;
    if (statementParameters instanceof Object[]) {
      parameters = Arrays.asList((Object[]) statementParameters);
    } else if (statementParameters instanceof Collection) {
      parameters = (Collection) statementParameters;
    } else if (statementParameters != null) {
      parameters = Collections.singletonList(statementParameters);
    } else {
      parameters = Collections.emptyList();
    }
    return parameters;
  }
}
