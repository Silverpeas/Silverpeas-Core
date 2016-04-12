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

package org.silverpeas.core.persistence.jdbc.sql;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementations of this interface must permit to execute SQL queries into processes
 * (transactional or not) without that the caller has to handle the connection to the database.
 * It is possible to call this provided methods into transactions that handles JPA operations too.
 * @author Yohann Chastagnier
 */
public interface JdbcSqlExecutor {

  /**
   * Select count SQL query executor.
   * @param selectCountQueryBuilder the select count SQL query.
   * @return the select count result.
   * @throws java.sql.SQLException
   */
  long selectCount(JdbcSqlQuery selectCountQueryBuilder) throws SQLException;

  /**
   * Select query executor.
   *
   * @param selectQuery the select SQL query.
   * @param process the row processor.
   *  @return the list of entities.
   * @throws java.sql.SQLException
   */
  <ROW_ENTITY> List<ROW_ENTITY> select(JdbcSqlQuery selectQuery,
      SelectResultRowProcess<ROW_ENTITY> process) throws SQLException;

  /**
   * Modify query executor.
   * @param modifySqlQueries the list of SQL query to execute. An SQL query is represented by a
   * string (SQL) and an Object (parameters).
   * @throws java.sql.SQLException
   */
  long executeModify(JdbcSqlQuery... modifySqlQueries) throws SQLException;

  /**
   * Modify query executor.
   * @param modifySqlQueries the list of SQL query to execute. An SQL query is represented by a
   * string (SQL) and an Object (parameters).
   * @throws java.sql.SQLException
   */
  long executeModify(List<JdbcSqlQuery> modifySqlQueries) throws SQLException;
}
