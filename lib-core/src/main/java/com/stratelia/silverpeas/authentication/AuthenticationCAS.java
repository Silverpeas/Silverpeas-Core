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

package com.stratelia.silverpeas.authentication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AuthenticationCAS extends Authentication {

  protected String m_JDBCUrl;
  protected String m_AccessLogin;
  protected String m_AccessPasswd;
  protected String m_DriverClass;

  protected String m_UserTableName;
  protected String m_UserLoginColumnName;

  protected Connection m_Connection;

  public void init(String authenticationServerName, ResourceLocator propFile) {
    // Lecture du fichier de proprietes
    m_JDBCUrl = propFile.getString(authenticationServerName + ".SQLJDBCUrl");
    m_AccessLogin = propFile.getString(authenticationServerName
        + ".SQLAccessLogin");
    m_AccessPasswd = propFile.getString(authenticationServerName
        + ".SQLAccessPasswd");
    m_DriverClass = propFile.getString(authenticationServerName
        + ".SQLDriverClass");
    m_UserTableName = propFile.getString(authenticationServerName
        + ".SQLUserTableName");
    m_UserLoginColumnName = propFile.getString(authenticationServerName
        + ".SQLUserLoginColumnName");
  }

  protected void openConnection() throws AuthenticationException {
    Properties info = new Properties();
    Driver driverSQL = null;

    try {
      info.setProperty("user", m_AccessLogin);
      info.setProperty("password", m_AccessPasswd);
      driverSQL = (Driver) Class.forName(m_DriverClass).newInstance();
    } catch (Exception iex) {
      throw new AuthenticationHostException(
          "AuthenticationCAS.openConnection()", SilverpeasException.ERROR,
          "root.EX_CANT_INSTANCIATE_DB_DRIVER", "Driver=" + m_DriverClass, iex);
    }
    try {
      m_Connection = driverSQL.connect(m_JDBCUrl, info);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationCAS.openConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", "JDBCUrl=" + m_JDBCUrl, ex);
    }
  }

  protected void internalAuthentication(String login, String passwd)
      throws AuthenticationException {
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      String loginQuery = "SELECT " + m_UserLoginColumnName + " FROM "
          + m_UserTableName + " WHERE " + m_UserLoginColumnName + " = ?";

      stmt = m_Connection.prepareStatement(loginQuery);
      stmt.setString(1, login);
      rs = stmt.executeQuery();
      if (!rs.next()) {
        throw new AuthenticationBadCredentialException(
            "AuthenticationCAS.internalAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND",
            "User=" + login);
      }
      SilverTrace.info("authentication",
          "AuthenticationCAS.internalAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationCAS.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  protected void closeConnection() throws AuthenticationException {
    try {
      if (m_Connection != null) {
        m_Connection.close();
        m_Connection = null;
      }
    } catch (SQLException ex) {
      m_Connection = null;
      throw new AuthenticationHostException(
          "AuthenticationCAS.closeConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_CLOSE_FAILED", "JDBCUrl=" + m_JDBCUrl, ex);
    }
  }

}