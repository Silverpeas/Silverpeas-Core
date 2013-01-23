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

/*
 * AuthenticationSQL.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.cryptage.CryptMD5;
import com.silverpeas.util.cryptage.UnixMD5Crypt;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.jcrypt;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * This class performs the authentication using an SQL table
 * @author tleroi
 * @version
 */
public class AuthenticationSQL extends Authentication {
  protected String m_JDBCUrl;
  protected String m_AccessLogin;
  protected String m_AccessPasswd;
  protected String m_DriverClass;
  protected String m_UserTableName;
  protected String m_UserLoginColumnName;
  protected String m_UserPasswordColumnName;
  protected String m_UserPasswordAvailableColumnName;
  protected String m_PasswordEncryption;

  @Override
  public void loadProperties(ResourceLocator settings) {
    String serverName = getServerName();
    m_JDBCUrl = settings.getString(serverName + ".SQLJDBCUrl");
    m_AccessLogin = settings.getString(serverName + ".SQLAccessLogin");
    m_AccessPasswd = settings.getString(serverName + ".SQLAccessPasswd");
    m_DriverClass = settings.getString(serverName + ".SQLDriverClass");
    m_UserTableName = settings.getString(serverName + ".SQLUserTableName");
    m_UserLoginColumnName = settings.getString(serverName + ".SQLUserLoginColumnName");
    m_UserPasswordColumnName = settings.getString(serverName + ".SQLUserPasswordColumnName");
    m_UserPasswordAvailableColumnName = settings.getString(serverName
        + ".SQLUserPasswordAvailableColumnName");
    m_PasswordEncryption = settings.getString(serverName + ".SQLPasswordEncryption");
  }

  @Override
  protected AuthenticationConnection<Connection> openConnection() throws AuthenticationException {
    Properties info = new Properties();
    Driver driverSQL;

    try {
      info.setProperty("user", m_AccessLogin);
      info.setProperty("password", m_AccessPasswd);
      driverSQL = (Driver) Class.forName(m_DriverClass).newInstance();
    } catch (Exception iex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.openConnection()",
          SilverpeasException.ERROR,
          "root.EX_CANT_INSTANCIATE_DB_DRIVER", "Driver="
          + m_DriverClass, iex);
    }
    try {
      Connection connection = driverSQL.connect(m_JDBCUrl, info);
      return new AuthenticationConnection<Connection>(connection);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.openConnection()",
          SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", "JDBCUrl=" + m_JDBCUrl,
          ex);
    }
  }

  @Override
  protected void closeConnection(AuthenticationConnection connection) throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    try {
      if (sqlConnection != null) {
        sqlConnection.close();
      }
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.closeConnection()",
          SilverpeasException.ERROR,
          "root.EX_CONNECTION_CLOSE_FAILED", "JDBCUrl=" + m_JDBCUrl,
          ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
                                  AuthenticationCredential credential)
      throws AuthenticationException {
    String loginQuery;
    String sqlPassword;
    ResultSet rs = null;
    PreparedStatement stmt = null;
    String login = credential.getLogin();
    String password = credential.getPassword();
    if (password == null) {
      password = "";
    }
    try {
      String thepasswd = password;
      if (Authentication.ENC_TYPE_MD5.equals(m_PasswordEncryption))
        thepasswd = CryptMD5.crypt(password);

      if (StringUtil.isDefined(m_UserPasswordAvailableColumnName)) {
        loginQuery = "SELECT " + m_UserLoginColumnName + ", "
            + m_UserPasswordColumnName + ", "
            + m_UserPasswordAvailableColumnName + " FROM "
            + m_UserTableName + " WHERE " + m_UserLoginColumnName
            + " = ?";
      } else {
        loginQuery = "SELECT " + m_UserLoginColumnName + ", "
            + m_UserPasswordColumnName + " FROM " + m_UserTableName
            + " WHERE " + m_UserLoginColumnName + " = ?";
      }

      stmt = getSQLConnection(connection).prepareStatement(loginQuery);
      stmt.setString(1, login);
      rs = stmt.executeQuery();
      if (rs.next()) {
        if (StringUtil.isDefined(m_UserPasswordAvailableColumnName)) {
          String validString = rs
              .getString(m_UserPasswordAvailableColumnName);

          if ((validString != null)
              && (validString.equalsIgnoreCase("N"))) {
            throw new AuthenticationPwdNotAvailException(
                "AuthenticationSQL.doAuthentication()",
                SilverpeasException.ERROR,
                "authentication.EX_PWD_NOT_AVAILABLE", "User="
                + login);
          }
        }
        sqlPassword = rs.getString(m_UserPasswordColumnName);
        if (sqlPassword == null) {
          if (password.length() > 0) {
            throw new AuthenticationBadCredentialException(
                "AuthenticationSQL.doAuthentication()",
                SilverpeasException.ERROR,
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL",
                "User=" + login);
          }
        } else {
          boolean passwordsMatch = true;
          if (Authentication.ENC_TYPE_UNIX
              .equals(m_PasswordEncryption)) {
            // String crypt2Check = jcrypt.crypt(cryptPwdInDB,
            // clearPwd);
            // si crypt2Check == cryptPwdInDB alors authentification
            // OK sinon
            // notOK
            String crypt2Check = "";
            if (sqlPassword.startsWith(UnixMD5Crypt.MAGIC)) {
              crypt2Check = UnixMD5Crypt.crypt(sqlPassword, password);
            } else {
              crypt2Check = jcrypt.crypt(sqlPassword, password);
            }
            passwordsMatch = crypt2Check.equals(sqlPassword);
          } else {
            passwordsMatch = thepasswd.equals(sqlPassword);
          }
          if (!passwordsMatch) {
            throw new AuthenticationBadCredentialException(
                "AuthenticationSQL.doAuthentication()",
                SilverpeasException.ERROR,
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL",
                "User=" + login);
          }
        }
      } else {
        throw new AuthenticationBadCredentialException(
            "AuthenticationSQL.doAuthentication()",
            SilverpeasException.ERROR,
            "authentication.EX_USER_NOT_FOUND", "User=" + login);
      }
      SilverTrace.info(module,
          "AuthenticationSQL.doAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (UtilException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.doAuthentication()",
          SilverpeasException.ERROR,
          "authentication.EX_SQL_ACCESS_ERROR", ex);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.doAuthentication()",
          SilverpeasException.ERROR,
          "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private String getPassword(Connection connection, String login) throws AuthenticationException {
    String loginQuery;
    String sqlPasswd;
    ResultSet rs = null;
    PreparedStatement stmt = null;

    if ((m_UserPasswordAvailableColumnName != null)
        && (m_UserPasswordAvailableColumnName.length() > 0)) {
      loginQuery = "SELECT " + m_UserLoginColumnName + ", "
          + m_UserPasswordColumnName + ", "
          + m_UserPasswordAvailableColumnName + " FROM "
          + m_UserTableName + " WHERE " + m_UserLoginColumnName
          + " = ?";
    } else {
      loginQuery = "SELECT " + m_UserLoginColumnName + ", "
          + m_UserPasswordColumnName + " FROM " + m_UserTableName
          + " WHERE " + m_UserLoginColumnName + " = ?";
    }

    try {
      stmt = connection.prepareStatement(loginQuery);
      stmt.setString(1, login);
      rs = stmt.executeQuery();
      if (rs.next()) {
        if ((m_UserPasswordAvailableColumnName != null)
            && (m_UserPasswordAvailableColumnName.length() > 0)) {
          String validString = rs
              .getString(m_UserPasswordAvailableColumnName);

          if ("N".equalsIgnoreCase(validString)) {
            throw new AuthenticationPwdNotAvailException(
                "AuthenticationSQL.getPassword()",
                SilverpeasException.ERROR,
                "authentication.EX_PWD_NOT_AVAILABLE", "User="
                + login);
          }
        }
        sqlPasswd = rs.getString(m_UserPasswordColumnName);
      } else {
        throw new AuthenticationBadCredentialException(
            "AuthenticationSQL.doAuthentication()",
            SilverpeasException.ERROR,
            "authentication.EX_USER_NOT_FOUND", "User=" + login);
      }
      SilverTrace.info(module,
          "AuthenticationSQL.doAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.doAuthentication()",
          SilverpeasException.ERROR,
          "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
    return sqlPasswd;
  }

  private void updatePassword(Connection connection, String login, String newPassword)
      throws AuthenticationException {
    String updateQuery;

    PreparedStatement stmt = null;

    updateQuery = "UPDATE " + m_UserTableName + " SET "
        + m_UserPasswordColumnName + " = ? WHERE "
        + m_UserLoginColumnName + " = ?";

    try {
      stmt = connection.prepareStatement(updateQuery);
      stmt.setString(1, newPassword);
      stmt.setString(2, login);
      stmt.executeUpdate();
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.updatePassword()",
          SilverpeasException.ERROR,
          "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(stmt);
    }
  }

  @Override
  protected void doChangePassword(AuthenticationConnection connection,
                                  AuthenticationCredential credential, String newPassword)
      throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    String passwordInDB = getPassword(sqlConnection, login);
    String newPasswordInDB = null;
    boolean passwordsMatch = true;
    if (Authentication.ENC_TYPE_UNIX.equals(m_PasswordEncryption)) {
      String crypt2Check = "";
      if (passwordInDB.startsWith(UnixMD5Crypt.MAGIC)) {
        crypt2Check = UnixMD5Crypt.crypt(passwordInDB, oldPassword);
        newPasswordInDB = UnixMD5Crypt.crypt(passwordInDB, newPassword);
      } else {
        crypt2Check = jcrypt.crypt(passwordInDB, oldPassword);
        newPasswordInDB = jcrypt.crypt(passwordInDB, newPassword);
      }
      passwordsMatch = crypt2Check.equals(passwordInDB);
    } else if (Authentication.ENC_TYPE_MD5.equals(m_PasswordEncryption)) {
      try {
        String thepasswd = CryptMD5.crypt(oldPassword);
        newPasswordInDB = CryptMD5.crypt(newPassword);
        passwordsMatch = thepasswd.equals(passwordInDB);
      } catch (UtilException e) {
        throw new AuthenticationPwdNotAvailException(
            "AuthenticationSQL.doChangePassword()",
            SilverpeasException.ERROR,
            "authentication.EX_INCORRECT_PASSWORD",
            "Crypt Md5 impossible");
      }
    } else if (Authentication.ENC_TYPE_CLEAR.equals(m_PasswordEncryption)) {
      newPasswordInDB = newPassword;
      passwordsMatch = oldPassword.equals(passwordInDB);
    }
    if (!passwordsMatch)
      throw new AuthenticationBadCredentialException(
          "AuthenticationSQL.doChangePassword()",
          SilverpeasException.ERROR,
          "authentication.EX_INCORRECT_PASSWORD", "User=" + login);

    updatePassword(sqlConnection, login, newPasswordInDB);
  }

  @Override
  protected void doResetPassword(AuthenticationConnection connection, String login,
                                 String newPassword) throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    String passwordInDB = getPassword(sqlConnection, login);
    String newPasswordInDB = null;
    if (Authentication.ENC_TYPE_UNIX.equals(m_PasswordEncryption)) {
      if (passwordInDB.startsWith(UnixMD5Crypt.MAGIC)) {
        newPasswordInDB = UnixMD5Crypt.crypt(passwordInDB, newPassword);
      } else {
        newPasswordInDB = jcrypt.crypt(passwordInDB, newPassword);
      }
    } else if (Authentication.ENC_TYPE_MD5.equals(m_PasswordEncryption)) {
      try {
        newPasswordInDB = CryptMD5.crypt(newPassword);
      } catch (UtilException e) {
        throw new AuthenticationPwdNotAvailException(
            "AuthenticationSQL.doChangePassword()",
            SilverpeasException.ERROR,
            "authentication.EX_INCORRECT_PASSWORD",
            "Crypt Md5 impossible");
      }
    } else if (Authentication.ENC_TYPE_CLEAR.equals(m_PasswordEncryption)) {
      newPasswordInDB = newPassword;
    }

    updatePassword(sqlConnection, login, newPasswordInDB);
  }

  private static Connection getSQLConnection(AuthenticationConnection connection) {
    return (Connection) connection.getConnector();
  }
}
