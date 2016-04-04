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

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * It wraps the access to the data source into which are stored the dynamic values. It uses for
 * doing the JNDI name defined in the connectionSettings.properties file to get the data source and
 * then to access its backed connection pool.
 * <p>
 * Before using this class you must define the data source in your application server.
 * Its JNDI name must be declared in the connectionSetting.properties
 * file under the property key <code>datasource.jndiName</code>.</p>
 */
public class JNDIConnectionPool implements ConnectionPool {

  private static final String SETTINGS_PATH =
      "org.silverpeas.wysiwyg.dynamicvalue.pool.connectionSettings";
  private static final String JNDI_NAME_PROPERTY = "datasource.jndiName";
  private static final String DEFAULT_SILVERPEAS_DATASOURCE = "java:/datasources/silverpeas";

  private DataSource dataSource = null;

  /**
   * default constructor
   */
  public JNDIConnectionPool() {
  }

  @PostConstruct
  protected void init() {
    SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);
    String jndiName = settings.getString(JNDI_NAME_PROPERTY, DEFAULT_SILVERPEAS_DATASOURCE);
    try {
      dataSource = InitialContext.doLookup(jndiName);
    } catch (NamingException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * gets a JDBC connection via JNDI
   * @return a JDBC Connection
   */
  public Connection getConnection() throws SQLException {
    if (dataSource != null) {
      return dataSource.getConnection();
    } else {
      throw new SQLException(
          "No data source found! This occurs when no data source was declared in the " +
              SETTINGS_PATH +
              " properties file or if the declared data source was not defined in the application" +
              " server");
    }
  }

}
