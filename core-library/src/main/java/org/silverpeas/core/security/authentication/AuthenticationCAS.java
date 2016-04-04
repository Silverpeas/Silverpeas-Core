/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;

public class AuthenticationCAS extends Authentication {

  protected String jdbcUrl;
  protected String jdbcLogin;
  protected String jdbcPasswd;
  protected String jdbcDriver;
  protected String loginTableName;
  protected String loginColumnName;
  private String loginQuery;

  @Override
  public void loadProperties(SettingBundle settings) {
    String serverName = getServerName();
    jdbcUrl = settings.getString(serverName + ".SQLJDBCUrl");
    jdbcLogin = settings.getString(serverName + ".SQLAccessLogin");
    jdbcPasswd = settings.getString(serverName + ".SQLAccessPasswd");
    jdbcDriver = settings.getString(serverName + ".SQLDriverClass");
    loginTableName = settings.getString(serverName + ".SQLUserTableName");
    loginColumnName = settings.getString(serverName + ".SQLUserLoginColumnName");
    loginQuery = "SELECT " + loginColumnName + " FROM " + loginTableName + " WHERE "
        + loginColumnName + " = ?";
  }

  @Override
  protected AuthenticationConnection<Connection> openConnection() throws AuthenticationException {
    Properties info = new Properties();
    Driver driverSQL = null;
    try {
      info.setProperty("user", jdbcLogin);
      info.setProperty("password", jdbcPasswd);
      driverSQL = (Driver) Class.forName(jdbcDriver).newInstance();
    } catch (InstantiationException ex) {
      throw new AuthenticationHostException("AuthenticationCAS.openConnection()",
          SilverpeasException.ERROR, "root.EX_CANT_INSTANCIATE_DB_DRIVER",
          "Driver=" + jdbcDriver, ex);
    } catch (IllegalAccessException ex) {
      throw new AuthenticationHostException("AuthenticationCAS.openConnection()",
          SilverpeasException.ERROR, "root.EX_CANT_INSTANCIATE_DB_DRIVER",
          "Driver=" + jdbcDriver, ex);
    } catch (ClassNotFoundException ex) {
      throw new AuthenticationHostException("AuthenticationCAS.openConnection()",
          SilverpeasException.ERROR, "root.EX_CANT_INSTANCIATE_DB_DRIVER",
          "Driver=" + jdbcDriver, ex);
    }
    try {
      Connection connection = driverSQL.connect(jdbcUrl, info);
      return new AuthenticationConnection<Connection>(connection);
    } catch (SQLException ex) {
      throw new AuthenticationHostException("AuthenticationCAS.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", "JDBCUrl=" + jdbcUrl, ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
      AuthenticationCredential credential) throws AuthenticationException {
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = getSQLConnection(connection).prepareStatement(loginQuery);
      stmt.setString(1, credential.getLogin());
      rs = stmt.executeQuery();
      if (!rs.next()) {
        throw new AuthenticationBadCredentialException(
            "AuthenticationCAS.doAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + credential.getLogin());
      }

    } catch (SQLException ex) {
      throw new AuthenticationHostException("AuthenticationCAS.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  @Override
  protected void closeConnection(AuthenticationConnection connection) throws AuthenticationException {
    DBUtil.close(getSQLConnection(connection));
  }

  private static Connection getSQLConnection(AuthenticationConnection<Connection> connection) {
    return connection.getConnector();
  }
}