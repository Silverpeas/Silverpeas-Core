/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;
import org.silverpeas.core.util.SettingBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

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
  @SuppressWarnings("unchecked")
  protected AuthenticationConnection<Connection> openConnection() throws AuthenticationException {
    Properties info = new Properties();
    Driver driverSQL;
    try {
      info.setProperty("user", jdbcLogin);
      info.setProperty("password", jdbcPasswd);
      Constructor<Driver> constructor =
          ((Class<Driver>) Class.forName(jdbcDriver)).getConstructor();
      driverSQL = constructor.newInstance();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
        ClassNotFoundException | InvocationTargetException ex) {
      throw new AuthenticationHostException("Invalid JDBC driver: " + jdbcDriver, ex);
    }
    try {
      Connection connection = driverSQL.connect(jdbcUrl, info);
      return new AuthenticationConnection<>(connection);
    } catch (SQLException ex) {
      throw new AuthenticationHostException("Cannot connect to database at " + jdbcUrl, ex);
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
            "User not found with login: " + credential.getLogin());
      }

    } catch (SQLException ex) {
      throw new AuthenticationHostException(ex);
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