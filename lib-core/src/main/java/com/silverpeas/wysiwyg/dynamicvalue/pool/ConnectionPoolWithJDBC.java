/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.wysiwyg.dynamicvalue.pool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * This class allow the creation of connection pool to access external resource from Silverpeas it's
 * used for the dynamic value functionalities
 */
public class ConnectionPoolWithJDBC implements ConnectionPool {

  private static DataSource ds = null;

  private static ConnectionPoolInformation poolInfo = null;

  /**
   * default constructor
   */
  public ConnectionPoolWithJDBC() {

  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.wysiwyg.pool.ConnectionPool#getConnection()
   */
  @Override
  public Connection getConnection() throws SQLException {
    if (ds == null) {
      initializeDatasource();
    }
    return ds.getConnection();
  }

  /**
   * Initializes the datasource. This datasource is based on the SharedPoolDataSource class, a dbcp
   * implementation
   */
  private synchronized static void initializeDatasource() {
    SilverTrace.debug("wysiwyg", ConnectionPoolWithJDBC.class.toString(), 
        " Datasource initialization : starting ...");
    if (ds == null) {
      // check if the information for the pool creation is present.
      if (poolInfo == null) {
        SilverTrace.error("wysiwig", ConnectionPoolWithJDBC.class.toString(),
            "wysiwig.CONNECTION_INIALIZATION_FAILED");
        throw new TechnicalException(ConnectionPoolWithJDBC.class.toString() 
            + " : An error occurred  during the connection initialization. The Pool information must be set");
      }
      SilverTrace.debug("wysiwyg", ConnectionPoolWithJDBC.class.toString(),
          " Datasource initialization : poolInfo detail :: " + poolInfo.toString());

      // driver registration
      DriverAdapterCPDS cpds = new DriverAdapterCPDS();
      try {
        cpds.setDriver(poolInfo.getDriver());
      } catch (ClassNotFoundException e) {
        SilverTrace.error("wysiwig", ConnectionPoolWithJDBC.class.toString(), "wysiwig.DRIVER_MISSING");
        throw new TechnicalException(ConnectionPoolWithJDBC.class.toString() +
            " : An error occurred  during the connection initializatoin. The JDBC driver isn't in the classpath",
            e);
      }
      cpds.setUrl(poolInfo.getUrl());
      cpds.setUser(poolInfo.getUser());
      cpds.setPassword(poolInfo.getPassword());

      // datasource object creation
      SharedPoolDataSource tds = new SharedPoolDataSource();
      tds.setConnectionPoolDataSource(cpds);
      tds.setMaxActive(poolInfo.getMaxActive());
      tds.setMaxWait(poolInfo.getMaxWait());
      tds.setMaxIdle(poolInfo.getMaxIdle());
      tds.setTimeBetweenEvictionRunsMillis(poolInfo.getTimeBetweenEvictionRunsMillis());
      tds.setNumTestsPerEvictionRun(poolInfo.getNumTestsPerEvictionRun());
      tds.setMinEvictableIdleTimeMillis(poolInfo.getMinEvictableIdleTimeMillis());
      ds = tds;
      SilverTrace.debug("wysiwyg", ConnectionPoolWithJDBC.class.toString(),
          " Datasource initialization : ending ...");
    }
  }

  /**
   * @return the poolInfo
   */
  @Override
  public ConnectionPoolInformation getPoolInformation() {
    return poolInfo;
  }

  /**
   * @param poolInfo the poolInfo to set
   */
  @Override
  public void setPoolInformation(ConnectionPoolInformation poolInfo) {
    ConnectionPoolWithJDBC.poolInfo = poolInfo;
  }

}
