/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.persistence.jdbc;

import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A pool of JDBC connections. It encapsulates the underlying mechanism to manage a pool of
 * connections to the data source used by Silverpeas.
 * <p>
 * Currently, it wraps the connection pool spawned by the JEE application server for the data
 * source used by Silverpeas.
 */
@Singleton
public class ConnectionPool {

  @Resource(mappedName = "java:/datasources/silverpeas")
  private DataSource dataSource;

  /**
   * Return a connection from the Silverpeas data source.
   * @return a connection from the Silverpeas data source.
   * @throws java.sql.SQLException if an error occurs while getting an available connection.
   */
  public static Connection getConnection() throws SQLException {
    ConnectionPool connectionPool = ServiceProvider.getService(ConnectionPool.class);
    return connectionPool.getDataSourceConnection();
  }

  /**
   * Return a connection from the Silverpeas data source.
   * @return a connection from the Silverpeas data source.
   * @throws java.sql.SQLException if an error occurs while getting an available connection.
   */
  Connection getDataSourceConnection() throws SQLException {
    return dataSource.getConnection();
  }

  protected ConnectionPool() {
  }
}