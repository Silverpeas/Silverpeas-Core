/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.wysiwyg.dynamicvalue.pool;

import java.sql.Connection;
import java.sql.SQLException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * use to create connection with JNDI Before using this class you must declare a jndi resource in
 * your application server. The resource name must be declare in the connectionSetting.xml file
 */
public class ConnectionPoolWithJNDI implements ConnectionPool {

  private static ConnectionPoolInformation poolInfo = null;

  /**
   * default constructor
   */
  public ConnectionPoolWithJNDI() {
  }

  /**
   * gets a JDBC connection via JNDI
   * @return a JDBC Connection
   */
  public Connection getConnection() throws SQLException {
    SilverTrace.debug("wysiwyg", ConnectionPoolWithJNDI.class.toString(),
        " Datasource initialization : starting ...");
    if (poolInfo == null) {
      SilverTrace
          .error("wysiwig", ConnectionPoolWithJNDI.class.toString(),
          "wysiwig.DRIVER_MISSING");
      throw new TechnicalException(
          ConnectionPoolWithJNDI.class.toString() +
              " : An error occurred  during the connection initialization. The Pool information must be set");
    }
    Connection conn = null;
    // use the common mechanism to get a connection
    try {
      conn = DBUtil.makeConnection(poolInfo.getJndiName());
    } catch (UtilException e) {
      SilverTrace
          .error("wysiwig", ConnectionPoolWithJNDI.class.toString(),
          "root.EX_CANT_INSTANCIATE_DB_DRIVER");
      throw new TechnicalException(ConnectionPoolWithJNDI.class.toString() +
          " : An error occurred  during the connection initialization.", e);
    }
    SilverTrace.debug("wysiwyg", ConnectionPoolWithJDBC.class.toString(),
        " Datasource initialization : ending ...");
    return conn;
  }

  /**
   * @return the poolInfo
   */
  public ConnectionPoolInformation getPoolInformation() {
    return poolInfo;
  }

  /**
   * @param poolInfo the poolInfo to set
   */
  public void setPoolInformation(ConnectionPoolInformation poolInfo) {
    ConnectionPoolWithJNDI.poolInfo = poolInfo;
  }

}
