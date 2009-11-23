/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.pool;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public class ConnectionPool {

  private static BasicDataSource pool;

  static {
    SilverTrace.debug("util", "ConnectionPool.getConnection",
        "No more free connection : we need to create a new one.");
    ResourceLocator resources = new ResourceLocator(
        "com.stratelia.webactiv.beans.admin.admin", "");
    pool = new BasicDataSource();
    pool.setPassword(resources.getString("WaProductionPswd"));
    pool.setUsername(resources.getString("WaProductionUser"));
    pool.setDriverClassName(resources.getString("AdminDBDriver"));
    pool.setUrl(resources.getString("WaProductionDb"));
  }

  public static void releaseConnections() throws SQLException {
    SilverTrace.debug("util", "ConnectionPool.releaseConnections", "start");
    synchronized (pool) {
      pool.close();
    }
  }

  /**
   * permet de recuperer une connection depuis un pool de connection libre. On libere la connection
   * en la fermant (connection.close()), elle revient alors toute seule dans le pool.
   * @return Une connection BDD vers la base de donn�e silverpeas.
   */
  public static Connection getConnection() throws SQLException {
    SilverTrace.debug("util", "ConnectionPool.getConnection", "start");
    return pool.getConnection();
  }
}