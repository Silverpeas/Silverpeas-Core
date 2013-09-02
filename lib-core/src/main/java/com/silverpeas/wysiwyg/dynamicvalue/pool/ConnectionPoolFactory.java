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

package com.silverpeas.wysiwyg.dynamicvalue.pool;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.sql.Connection;
import java.sql.SQLException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * This class allow to initiate a connectionPool implementation and gets a connection from the pool
 */
public class ConnectionPoolFactory {

  final static String ConfigPoolFilename = "ConnectionSettings.xml";
  private static ConnectionPoolInformation poolInfo = null;
  private static ConnectionPool pool = null;

  /**
   * 
   */
  private ConnectionPoolFactory() {
  }

  /**
   * @return
   * @throws SQLException
   */
  public static Connection getConnection() throws SQLException {
    Connection conn = null;
    synchronized (ConnectionPoolFactory.class) {
      // build a pojo which contains information about connection to database (jdni name or jdbc
      // url)
      if (poolInfo == null) {
        SilverTrace.debug("wysiwyg", ConnectionPoolFactory.class.toString(),
            " information about datasource : loading ...");
        try {
          Mapping mapping = new Mapping();

          // 1. Load the mapping information from the file
          mapping.loadMapping(new InputSource(ConnectionPoolFactory.class.getResourceAsStream(
              "mapping-config.xml")));

          // 2. Unmarshal the data
          Unmarshaller unmar = new Unmarshaller(mapping);
          poolInfo = (ConnectionPoolInformation) unmar.unmarshal(new InputSource(
              ConnectionPoolFactory.class.getResourceAsStream(ConfigPoolFilename)));

          SilverTrace.debug("wysiwyg", ConnectionPoolFactory.class.toString(),
              " information about datasource : poolInformation detail :" + poolInfo.toString());

        } catch (Exception exception) {
          SilverTrace.error("wysiwig", ConnectionPoolFactory.class.toString(),
              "wysiwig.CONNECTION_INIALIZATION_FAILED");
          throw new TechnicalException(
              "The pool  has not been initialized. Error when parsing "
              + ConfigPoolFilename, exception);
        }
        SilverTrace.debug("wysiwyg", ConnectionPoolFactory.class.toString(),
            " information about datasource : end of loading ...");
      }
    }
    // gets a connectionPool
    synchronized (ConnectionPoolFactory.class) {
      if (pool == null) {
        // check the class to instantiate the correct class.
        // this code must be replace by the use of a properties file or connectionSettings.xml if
        // there are more than 2 pool implementations
        if (StringUtil.isDefined(poolInfo.getConnectionType())) {
          String className = "com.silverpeas.wysiwyg.dynamicvalue.pool.ConnectionPoolWithJDBC";
          if ("JNDI".equalsIgnoreCase(poolInfo.getConnectionType())) {
            className = "com.silverpeas.wysiwyg.dynamicvalue.pool.ConnectionPoolWithJNDI";
          }
          // pool instantiation
          try {
            pool = (ConnectionPool) Class.forName(className).newInstance();
          } catch (Exception e) {
            SilverTrace.error("wysiwig", ConnectionPoolFactory.class.toString(),
                "root.EX_CANT_INSTANCIATE_DB_DRIVER");
            throw new TechnicalException(
                "The pool  has not been initialized. Error during object instantiation", e);
          }
          pool.setPoolInformation(poolInfo);
        } else {
          SilverTrace.error("wysiwig", ConnectionPoolFactory.class.toString(),
              "wysiwig.CONNECTION_INIALIZATION_FAILED");
          throw new TechnicalException(
              "The pool  has not been initialized. Error when reading ConnectionPoolInformation object");
        }
      }
    }
    conn = pool.getConnection();
    return conn;
  }
}
