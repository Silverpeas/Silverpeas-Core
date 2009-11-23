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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
 * This class performs the authentification using an SQL table
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
    m_UserPasswordColumnName = propFile.getString(authenticationServerName
        + ".SQLUserPasswordColumnName");
    m_UserPasswordAvailableColumnName = propFile
        .getString(authenticationServerName
        + ".SQLUserPasswordAvailableColumnName");
    m_PasswordEncryption = propFile.getString(authenticationServerName
        + ".SQLPasswordEncryption");
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
          "AuthenticationSQL.openConnection()", SilverpeasException.ERROR,
          "root.EX_CANT_INSTANCIATE_DB_DRIVER", "Driver=" + m_DriverClass, iex);
    }
    try {
      m_Connection = driverSQL.connect(m_JDBCUrl, info);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.openConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", "JDBCUrl=" + m_JDBCUrl, ex);
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
          "AuthenticationSQL.closeConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_CLOSE_FAILED", "JDBCUrl=" + m_JDBCUrl, ex);
    }
  }

  protected void internalAuthentication(String login, String passwd)
      throws AuthenticationException {
    String loginQuery;
    String sqlPasswd;
    ResultSet rs = null;
    PreparedStatement stmt = null;
    if (passwd == null) {
      passwd = "";
    }
    try {
      String thepasswd = passwd;
      if (Authentication.ENC_TYPE_MD5.equals(m_PasswordEncryption))
        thepasswd = CryptMD5.crypt(passwd);

      if (StringUtil.isDefined(m_UserPasswordAvailableColumnName)) {
        loginQuery = "SELECT " + m_UserLoginColumnName + ", "
            + m_UserPasswordColumnName + ", "
            + m_UserPasswordAvailableColumnName + " FROM " + m_UserTableName
            + " WHERE " + m_UserLoginColumnName + " = ?";
      } else {
        loginQuery = "SELECT " + m_UserLoginColumnName + ", "
            + m_UserPasswordColumnName + " FROM " + m_UserTableName + " WHERE "
            + m_UserLoginColumnName + " = ?";
      }

      stmt = m_Connection.prepareStatement(loginQuery);
      stmt.setString(1, login);
      rs = stmt.executeQuery();
      if (rs.next()) {
        if (StringUtil.isDefined(m_UserPasswordAvailableColumnName)) {
          String validString = rs.getString(m_UserPasswordAvailableColumnName);

          if ((validString != null) && (validString.equalsIgnoreCase("N"))) {
            throw new AuthenticationPwdNotAvailException(
                "AuthenticationSQL.internalAuthentication()",
                SilverpeasException.ERROR,
                "authentication.EX_PWD_NOT_AVAILABLE", "User=" + login);
          }
        }
        sqlPasswd = rs.getString(m_UserPasswordColumnName);
        if (sqlPasswd == null) {
          if (passwd.length() > 0) {
            throw new AuthenticationBadCredentialException(
                "AuthenticationSQL.internalAuthentication()",
                SilverpeasException.ERROR,
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "User="
                + login);
          }
        } else {
          boolean passwordsMatch = true;
          if (Authentication.ENC_TYPE_UNIX.equals(m_PasswordEncryption)) {
            // String crypt2Check = jcrypt.crypt(cryptPwdInDB, clearPwd);
            // si crypt2Check == cryptPwdInDB alors authentification OK sinon
            // notOK
            String crypt2Check = "";
            if (sqlPasswd.startsWith(UnixMD5Crypt.MAGIC)) {
              crypt2Check = UnixMD5Crypt.crypt(sqlPasswd, passwd);
            } else {
              crypt2Check = jcrypt.crypt(sqlPasswd, passwd);
            }
            passwordsMatch = crypt2Check.equals(sqlPasswd);
          } else {
            passwordsMatch = thepasswd.equals(sqlPasswd);
          }
          if (!passwordsMatch) {
            throw new AuthenticationBadCredentialException(
                "AuthenticationSQL.internalAuthentication()",
                SilverpeasException.ERROR,
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "User="
                + login);
          }
        }
      } else {
        throw new AuthenticationBadCredentialException(
            "AuthenticationSQL.internalAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND",
            "User=" + login);
      }
      SilverTrace.info("authentication",
          "AuthenticationSQL.internalAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (UtilException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  protected String getPassword(String login) throws AuthenticationException {
    String loginQuery;
    String sqlPasswd;
    ResultSet rs = null;
    PreparedStatement stmt = null;

    if ((m_UserPasswordAvailableColumnName != null)
        && (m_UserPasswordAvailableColumnName.length() > 0)) {
      loginQuery = "SELECT " + m_UserLoginColumnName + ", "
          + m_UserPasswordColumnName + ", " + m_UserPasswordAvailableColumnName
          + " FROM " + m_UserTableName + " WHERE " + m_UserLoginColumnName
          + " = ?";
    } else {
      loginQuery = "SELECT " + m_UserLoginColumnName + ", "
          + m_UserPasswordColumnName + " FROM " + m_UserTableName + " WHERE "
          + m_UserLoginColumnName + " = ?";
    }

    try {
      stmt = m_Connection.prepareStatement(loginQuery);
      stmt.setString(1, login);
      rs = stmt.executeQuery();
      if (rs.next()) {
        if ((m_UserPasswordAvailableColumnName != null)
            && (m_UserPasswordAvailableColumnName.length() > 0)) {
          String validString = rs.getString(m_UserPasswordAvailableColumnName);

          if ("N".equalsIgnoreCase(validString)) {
            throw new AuthenticationPwdNotAvailException(
                "AuthenticationSQL.getPassword()", SilverpeasException.ERROR,
                "authentication.EX_PWD_NOT_AVAILABLE", "User=" + login);
          }
        }
        sqlPasswd = rs.getString(m_UserPasswordColumnName);
      } else {
        throw new AuthenticationBadCredentialException(
            "AuthenticationSQL.internalAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND",
            "User=" + login);
      }
      SilverTrace.info("authentication",
          "AuthenticationSQL.internalAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (SQLException ex) {
      throw new AuthenticationHostException(
          "AuthenticationSQL.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
    return sqlPasswd;
  }

  /**
   * Overrides Authentication.internalChangePassword to offer password update capabilities In case
   * of SQL Authentication, this method check if password entry matches with the actual one. The
   * password update is made in the updateUserFull method.
   * @param login user login
   * @param oldPassword user old password
   * @param newPassword user new password
   * @throws AuthenticationException if oldPassword does not match with actual password
   */
  protected void internalChangePassword(String login, String oldPassword,
      String newPassword) throws AuthenticationException {
    String passwordInDB = getPassword(login);
    boolean passwordsMatch = true;
    if (Authentication.ENC_TYPE_UNIX.equals(m_PasswordEncryption)) {
      String crypt2Check = "";
      if (passwordInDB.startsWith(UnixMD5Crypt.MAGIC)) {
        crypt2Check = UnixMD5Crypt.crypt(passwordInDB, oldPassword);
      } else {
        crypt2Check = jcrypt.crypt(passwordInDB, oldPassword);
      }
      passwordsMatch = crypt2Check.equals(passwordInDB);
    } else if (Authentication.ENC_TYPE_MD5.equals(m_PasswordEncryption)) {
      try {
        String thepasswd = CryptMD5.crypt(oldPassword);
        passwordsMatch = thepasswd.equals(passwordInDB);
      } catch (UtilException e) {
        throw new AuthenticationPwdNotAvailException(
            "AuthenticationSQL.internalChangePassword()",
            SilverpeasException.ERROR, "authentication.EX_INCORRECT_PASSWORD",
            "Crypt Md5 impossible");
      }
    } else if (Authentication.ENC_TYPE_CLEAR.equals(m_PasswordEncryption)) {
      passwordsMatch = oldPassword.equals(passwordInDB);
    }
    if (!passwordsMatch)
      throw new AuthenticationBadCredentialException(
          "AuthenticationSQL.internalChangePassword()",
          SilverpeasException.ERROR, "authentication.EX_INCORRECT_PASSWORD",
          "User=" + login);
  }
}
