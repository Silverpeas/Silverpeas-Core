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
package com.stratelia.webactiv.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public abstract class Schema {

  private boolean isLocalConnection = true;
  private boolean haveToTestConnections = true;
  private boolean managed = false;
  private int connectionLot = 0;
  private Map statementsMap = new HashMap();
  private Connection connection = null;

  abstract protected String getJNDIName();

  public Schema(int cl, Connection co) throws UtilException {
    connectionLot = cl;
    connection = co;
    isLocalConnection = false;
  }

  public Schema(int cl) throws UtilException {
    connectionLot = cl;
    createConnection();
    ResourceLocator resources = new ResourceLocator(
        "com.stratelia.webactiv.beans.admin.admin", "");
    String m_sHaveToTestConnections = resources.getString("HaveToTestConnections");
    if ((m_sHaveToTestConnections != null)
        && (m_sHaveToTestConnections.equalsIgnoreCase("false"))) {
      haveToTestConnections = false;
    }
  }

  protected void createConnection() throws UtilException {
    SilverTrace.info("util", "Schema.createConnection()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Context ctx = new InitialContext();
      DataSource src = (DataSource) ctx.lookup(getJNDIName());
      this.connection = src.getConnection();
      if (!this.connection.getAutoCommit()) {
        managed = true;
      } else {
        managed = false;
        this.connection.setAutoCommit(false);
      }
      isLocalConnection = true;
      SilverTrace.info("util", "Schema.createConnection()",
          "root.MSG_GEN_PARAM_VALUE", "Connection Created !");
    } catch (NamingException e) {
      try {
        // Get the initial Context
        Context ctx = new InitialContext();
        // Look up the datasource directly without JNDI access
        DataSource dataSource = (DataSource) ctx.lookup(JNDINames.DIRECT_DATASOURCE);
        // Create a connection object
        this.connection = dataSource.getConnection();
        this.managed = false;
      } catch (NamingException ne) {
        throw new UtilException("Schema.createConnection",
            SilverpeasException.ERROR, "root.EX_DATASOURCE_NOT_FOUND",
            "Data source " + JNDINames.DIRECT_DATASOURCE + " not found", ne);
      } catch (SQLException se) {
        throw new UtilException("Schema.createConnection",
            SilverpeasException.ERROR, "can't get connection for dataSource "
            + JNDINames.DIRECT_DATASOURCE, se);
      }
    } catch (SQLException e) {
      throw new UtilException("Schema.createConnection",
          SilverpeasException.ERROR, "root.EX_DATASOURCE_INVALID", e);
    }
  }

  public synchronized void commit() throws UtilException {
    SilverTrace.info("util", "Schema.commit()", "root.MSG_GEN_ENTER_METHOD");
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
    SilverTrace.info("util", "Schema.rollback()", "root.MSG_GEN_ENTER_METHOD");
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
    SilverTrace.info("util", "Schema.close()", "root.MSG_GEN_ENTER_METHOD");
    Iterator i = statementsMap.values().iterator();

    while (i.hasNext()) {
      DBUtil.close((Statement) i.next());
    }
    statementsMap.clear();
    try {
      if (isLocalConnection) {
        connection.close();
      }
    } catch (SQLException e) {
      SilverTrace.error("util", "Schema.close", "util.CAN_T_CLOSE_CONNECTION",
          e);
    } finally {
      connection = null;
    }
  }

  /**
   * @return <code>true</code> if the connection can be used.
   */
  public boolean isOk() {
    Statement st = null;

    try {
      if (this.connection == null || this.connection.isClosed()) {
        return false;
      }
      if (haveToTestConnections) {
        SilverTrace.info("util", "Schema.isOk()",
            "root.MSG_GEN_ENTER_METHOD", "Connection Test");
        st = connection.createStatement();
        st.close();
        st = null;
      }
      return true;
    } catch (SQLException e) {
      SilverTrace.info("util", "Schema.isOk()", "root.MSG_GEN_ENTER_METHOD",
          "Connection Test Problem !!!", e);
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
  public PreparedStatement getStatement(String query) throws SQLException {
    SilverTrace.info("util", "Schema.getStatement()",
        "root.MSG_GEN_ENTER_METHOD", query);
    /*
     * PreparedStatement statement =(PreparedStatement) statementsMap.get(query); if (statement ==
     * null) { statement = connection.prepareStatement(query); statementsMap.put(query, statement);
     * } return statement;
     */
    PreparedStatement statement = getConnection().prepareStatement(query);
    SilverTrace.info("util", "Schema.getStatement()",
        "root.MSG_GEN_PARAM_VALUE", "statement=" + statement);
    return statement;
  }

  public void releaseAll(ResultSet rs, PreparedStatement ps) {
    SilverTrace.info("util", "Schema.releaseAll()",
        "root.MSG_GEN_ENTER_METHOD", "rs=" + rs + " ,ps=" + ps);
    DBUtil.close(rs, ps);
    // DBUtil.close(rs);
  }

  public void releaseStatement(PreparedStatement ps) {
    DBUtil.close(ps);
  }

  public Connection getConnection() {
    if (!isOk() && isLocalConnection) {
      SilverTrace.info("util", "Schema.getConnection",
          "root.MSG_GEN_ENTER_METHOD",
          "Connection WAS CLOSED !!!! -> Create new one");
      try {
        createConnection();
      } catch (UtilException e) {
        SilverTrace.error("util", "Schema.getConnection",
            "util.CAN_T_CLOSE_CONNECTION", e);
      }
    } else {
      SilverTrace.info("util", "Schema.getConnection",
          "root.MSG_GEN_ENTER_METHOD", "Connection Verified");
    }

    return connection;
  }

  public int getConnectionLot() {
    return connectionLot;
  }
}
