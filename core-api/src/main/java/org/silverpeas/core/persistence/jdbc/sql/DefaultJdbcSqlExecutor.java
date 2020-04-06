/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.silverpeas.core.persistence.jdbc.ConnectionPool;
import org.silverpeas.core.persistence.jdbc.sql.setters.SqlStatementParameterSetter;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
class DefaultJdbcSqlExecutor implements JdbcSqlExecutor {

  private static final String SQL_REQUEST = ". SQL request: ";

  @Inject
  private SqlStatementParameterSetter sqlParamSetter;

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
    final JdbcSqlQuery.Configuration queryConf = selectQuery.getConfiguration();
    final String sqlQuery;
    boolean countOverPaginationMethod = isCountOverPaginationMethod(queryConf);
    if (countOverPaginationMethod) {
      final String theQuery = selectQuery.getSqlQuery().toLowerCase();
      final int selectIndex = theQuery.indexOf("select");
      if (selectIndex >= 0 && theQuery.indexOf("select", selectIndex + 1) < 0) {
        sqlQuery = selectQuery.getSqlQuery()
            .replaceFirst("(?i)(select .*)from ", "$1, COUNT(*) OVER() AS SP_MAX_ROW_COUNT FROM ");
      } else {
        sqlQuery = selectQuery.getSqlQuery();
        countOverPaginationMethod = false;
      }
    } else {
      sqlQuery = selectQuery.getSqlQuery();
    }
    try (PreparedStatement st = queryConf.isResultCountLimited() || queryConf.isFirstResultScrolled()
        ? con.prepareStatement(sqlQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        : con.prepareStatement(sqlQuery)) {
      if (queryConf.isResultCountLimited()) {
        st.setFetchSize(queryConf.getResultLimit());
        if (!queryConf.isNeedRealOriginalSize() || countOverPaginationMethod) {
          st.setMaxRows(queryConf.getOffset() + queryConf.getResultLimit());
        }
      }
      setParameters(st, selectQuery.getParameters());
      try (ResultSet rs = st.executeQuery()) {
        return fetchEntities(rs, process, queryConf, countOverPaginationMethod);
      } catch (SQLException e) {
        SilverLogger.getLogger(this).debug(e.getMessage() + SQL_REQUEST + sqlQuery);
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
      final SelectResultRowProcess<R> process, final JdbcSqlQuery.Configuration queryConf,
      final boolean countOverPaginationMethod)
      throws SQLException {
    final ResultSetWrapper rsw = new ResultSetWrapper(rs);
    int startIndex = queryConf.getOffset();
    if (queryConf.isFirstResultScrolled()) {
      rsw.next();
      rsw.relative(startIndex - 1);
    }
    final boolean resultCountLimited = queryConf.isResultCountLimited();
    final int lastIdx = resultCountLimited ? startIndex + queryConf.getResultLimit() - 1 : 0;
    final ListSlice<R> entities = new ListSlice<>(startIndex, lastIdx);
    if (!resultCountLimited) {
      fetchWithoutLimit(startIndex, rsw, process, entities);
    } else {
      fetchWithLimit(queryConf, startIndex, rsw, process, entities, countOverPaginationMethod);
    }
    return entities;
  }

  private <R> void fetchWithoutLimit(final int startIdx, final ResultSetWrapper rsw,
      final SelectResultRowProcess<R> process, final ListSlice<R> entities) throws SQLException {
    int idx = startIdx;
    for (; rsw.next(); idx++) {
      handleRow(idx, rsw, process, entities);
    }
    entities.setOriginalListSize(entities.size());
  }

  private <R> void fetchWithLimit(final JdbcSqlQuery.Configuration queryConf, final int startIdx,
      final ResultSetWrapper rsw, final SelectResultRowProcess<R> process,
      final ListSlice<R> entities, final boolean countOverPaginationMethod) throws SQLException {
    final int offsetCountAtStart = rsw.getRow();
    int idx = startIdx;
    int originalSize = (int) entities.originalListSize();
    if (queryConf.isNeedRealOriginalSize() && countOverPaginationMethod && rsw.next()) {
      handleRow(idx++, rsw, process, entities);
      originalSize = rsw.getInt("SP_MAX_ROW_COUNT");
    }
    for (; entities.size() < queryConf.getResultLimit() && rsw.next(); idx++) {
      handleRow(idx, rsw, process, entities);
    }
    if (originalSize <= 0 && queryConf.isNeedRealOriginalSize()) {
      if (rsw.last()) {
        originalSize = rsw.getRow();
      } else {
        originalSize = offsetCountAtStart == 0 ? offsetCountAtStart : idx;
      }
    }
    entities.setOriginalListSize(originalSize);
  }

  private <R> void handleRow(final int idx, final ResultSetWrapper rsw,
      final SelectResultRowProcess<R> process, final ListSlice<R> entities) throws SQLException {
    rsw.setCurrentRowIndex(idx);
    R entity = process.currentRow(rsw);
    if (entity != null) {
      entities.add(entity);
    }
  }

  private boolean isCountOverPaginationMethod(final JdbcSqlQuery.Configuration queryConf) {
    return queryConf.isResultCountLimited() && queryConf.isNeedRealOriginalSize() &&
        isCountOverPaginationMethod();
  }

  private static boolean isCountOverPaginationMethod() {
    return ResourceLocator.getGeneralSettingBundle()
        .getBoolean("jdbc.pagination.method.countOver", false);
  }

  /**
   * Centralization in order to sets the parameters on a prepare statement.
   * @param preparedStatement a prepared statement which parameters must be set.
   * @param statementParameters the parameters to set.
   * @throws java.sql.SQLException on SQL error.
   */
  private void setParameters(PreparedStatement preparedStatement, Object statementParameters)
      throws SQLException {
    final Collection<Object> parameters = getParameters(statementParameters);
    int paramIndex = 1;
    for (Object parameter : parameters) {
      setParameter(preparedStatement, paramIndex, parameter);
      paramIndex++;
    }
  }

  private void setParameter(final PreparedStatement preparedStatement, final int paramIndex,
      final Object parameter) throws SQLException {
    if (parameter == null) {
      preparedStatement.setObject(paramIndex, null);
    } else {
      sqlParamSetter.setParameter(preparedStatement, paramIndex, parameter);
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
