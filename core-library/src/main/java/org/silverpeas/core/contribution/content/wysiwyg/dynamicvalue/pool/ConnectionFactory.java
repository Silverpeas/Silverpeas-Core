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

package org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.pool;

import org.silverpeas.core.util.ServiceProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A factory of connections to the data source into which are stored the dynamic values to use
 * in a WYSIWYG content.
 * <p>This factory delegates to a {@code org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.pool.ConnectionPool}
 * object the opening of a such connection.</p>
 * @ßee org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.pool.ConnectionPool
 *
 */
public class ConnectionFactory {

  private ConnectionFactory() {
  }

  private static ConnectionPool getConnectionPool() {
    return ServiceProvider.getService(ConnectionPool.class);
  }

  /**
   * @return
   * @throws SQLException
   */
  public static Connection getConnection() throws SQLException {
    return getConnectionPool().getConnection();
  }
}
