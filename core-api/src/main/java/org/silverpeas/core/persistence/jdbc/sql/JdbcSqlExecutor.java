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
package org.silverpeas.core.persistence.jdbc.sql;

import org.silverpeas.core.util.ListSlice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementations of this interface must permit to execute SQL queries into processes
 * (transactional or not) without that the caller has to handle the connection to the database.
 * It is possible to call this provided methods into transactions that handles JPA operations too.
 * @author Yohann Chastagnier
 */
interface JdbcSqlExecutor {

  /**
   * Select count SQL query executor.
   * @param selectCountQueryBuilder the select count SQL query.
   * @return the select count result.
   * @throws java.sql.SQLException on SQL error.
   */
  long selectCount(JdbcSqlQuery selectCountQueryBuilder) throws SQLException;

  /**
   * Select count SQL query executor.
   * @param connection an existing connection.
   * @param selectCountQueryBuilder the select count SQL query.
   * @return the select count result.
   * @throws java.sql.SQLException on SQL error.
   */
  long selectCount(Connection connection, JdbcSqlQuery selectCountQueryBuilder) throws SQLException;

  /**
   * Executes the specified query that selects a slice of the list of entities in the data source
   * @param <R> The type of the items in the list.
   * @param selectQuery the SQL query to select some entities.
   * @param process the processor of result rows.
   * @return a slice of the list of entities in the data source. This slice contains only the
   * requested entities. If the offset property is set in the configuration of the specified query
   * then the returned list matches the slice of the list of the requested entities from this
   * offset.
   * @throws java.sql.SQLException on SQL error.
   */
  <R> ListSlice<R> select(JdbcSqlQuery selectQuery, SelectResultRowProcess<R> process)
      throws SQLException;

  /**
   * Executes the specified query that selects a slice of the list of entities in the data source
   * by using the given connection.
   * @param <R> The type of the items in the list.
   * @param connection an existing connection to the data source.
   * @param selectQuery the SQL query to select some entities.
   * @param process the processor of result rows.
   * @return a slice of the list of entities in the data source. This slice contains only the
   * requested entities. If the offset property is set in the configuration of the specified query
   * then the returned list matches the slice of the list of the requested entities from this
   * offset.
   * @throws java.sql.SQLException on SQL error.
   */
  <R> ListSlice<R> select(Connection connection, JdbcSqlQuery selectQuery,
      SelectResultRowProcess<R> process) throws SQLException;

  /**
   * Modify query executor.
   * @param modifySqlQueries the list of SQL query to execute. An SQL query is represented by a
   * string (SQL) and an Object (parameters).
   * @return the number of entities that were modified.
   * @throws java.sql.SQLException on SQL error.
   */
  long executeModify(JdbcSqlQuery... modifySqlQueries) throws SQLException;

  /**
   * Modify query executor.
   * @param connection an existing connection.
   * @param modifySqlQueries the list of SQL query to execute. An SQL query is represented by a
   * string (SQL) and an Object (parameters).
   * @return the number of entities that were modified.
   * @throws java.sql.SQLException on SQL error.
   */
  long executeModify(Connection connection, JdbcSqlQuery... modifySqlQueries) throws SQLException;

  /**
   * Modify query executor.
   * @param modifySqlQueries the list of SQL query to execute. An SQL query is represented by a
   * string (SQL) and an Object (parameters).
   * @return the number of entities that were modified.
   * @throws java.sql.SQLException on SQL error.
   */
  long executeModify(List<JdbcSqlQuery> modifySqlQueries) throws SQLException;

  /**
   * Modify query executor.
   * @param connection an existing connection.
   * @param modifySqlQueries the list of SQL query to execute. An SQL query is represented by a
   * string (SQL) and an Object (parameters).
   * @return the number of entities that were modified.
   * @throws java.sql.SQLException on SQL error.
   */
  long executeModify(Connection connection, List<JdbcSqlQuery> modifySqlQueries)
      throws SQLException;
}
