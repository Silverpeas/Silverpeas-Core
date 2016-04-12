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
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlExecutorProvider.getJdbcSqlExecutor;

/**
 * This class handles list of {@link JdbcSqlQuery} instance and provide a method execute to
 * perform each one.<br/>
 * As there is no sense that the collection handles other query than those of modification,
 * this collection is oriented to SQL query modifications.<br/>
 * The queries are executed into processes (transactional or not) without
 * handling the connection to the database.
 * @author Yohann Chastagnier
 */
public class JdbcSqlQueries extends ArrayList<JdbcSqlQuery> {

  /**
   * Loads a {@link JdbcSqlQuery} collection from collection of SQL queries as string.
   * @param sqlQueries list of SQL query as string.
   * @return an instance of {@link JdbcSqlQuery} collection instance.
   */
  public static JdbcSqlQueries from(Collection<String> sqlQueries) {
    return sqlQueries.stream().map(JdbcSqlQuery::create)
        .collect(Collectors.toCollection(JdbcSqlQueries::new));
  }

  /**
   * Executes all the queries contained into the list.
   * @throws SQLException
   */
  public long execute() throws SQLException {
    return getJdbcSqlExecutor().executeModify(this);
  }
}
