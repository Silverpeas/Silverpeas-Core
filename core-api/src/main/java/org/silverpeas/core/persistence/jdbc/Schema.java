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

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Schema {

  private boolean isLocalConnection = true;
  private boolean haveToTestConnections = true;
  private boolean managed = false;
  private Connection connection = null;

  public Schema(Connection co) throws UtilException {
    connection = co;
    isLocalConnection = false;
  }

  public Schema() throws UtilException {
    createConnection();
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.admin.admin");
    String m_sHaveToTestConnections = settings.getString("HaveToTestConnections");
    if ("false".equalsIgnoreCase(m_sHaveToTestConnections)) {
      haveToTestConnections = false;
    }
  }

  protected final synchronized void createConnection() throws UtilException {

    try {
      if (this.connection != null) {
        DBUtil.close(this.connection);
      }
      this.connection = DBUtil.openConnection();
      if (!this.connection.getAutoCommit()) {
        managed = true;
      } else {
        managed = false;
        this.connection.setAutoCommit(false);
      }
      isLocalConnection = true;

    } catch (SQLException e) {
      throw new UtilException("Schema.createConnection",
          SilverpeasException.ERROR, "root.EX_DATASOURCE_INVALID", e);
    }
  }

  public synchronized void commit() throws UtilException {

    if (!isManaged()) {
      try {
        this.connection.commit();
      } catch (SQLException e) {
        throw new UtilException("Schema.commit", SilverpeasException.ERROR,
            "root.EX_ERR_COMMIT", e);
      }
    }
  }

  public synchronized void rollback() throws UtilException {

    if (!isManaged()) {
      try {
        this.connection.rollback();
      } catch (SQLException e) {
        throw new UtilException("Schema.rollback", SilverpeasException.ERROR,
            "root.EX_ERR_ROLLBACK", e);
      }
    }
  }

  public synchronized void close() {

    /*
     * for (Object o : statementsMap.values()) { DBUtil.close((Statement) o); }
     * statementsMap.clear();
     */
    try {
      DBUtil.close(this.connection);
    } finally {
      connection = null;
    }
  }

  /**
   * @return <code>true</code> if the connection can be used.
   */
  public boolean isOk() {
    try {
      if (this.connection == null || this.connection.isClosed()) {
        return false;
      }
      if (haveToTestConnections) {

        Statement st = connection.createStatement();
        st.close();
      }
      return true;
    } catch (SQLException e) {

      close();
      return false;
    }
  }

  /**
   * Return the value of the managed property.
   * @return the value of managed.
   */
  public boolean isManaged() {
    return managed;
  }

  /**
   * All the statements are prepared and cached
   */
  public synchronized PreparedStatement getStatement(String query) throws SQLException {

    PreparedStatement statement = getConnection().prepareStatement(query);

    return statement;
  }

  public synchronized Connection getConnection() {
    if (!isOk() && isLocalConnection) {

      try {
        createConnection();
      } catch (UtilException e) {
        SilverTrace.error("util", "Schema.getConnection", "util.CAN_T_CLOSE_CONNECTION", e);
      }
    } else {

    }

    return connection;
  }
}
