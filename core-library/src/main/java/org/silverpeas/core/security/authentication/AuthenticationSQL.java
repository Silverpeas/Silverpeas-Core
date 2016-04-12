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

import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPwdNotAvailException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.security.encryption.cipher.CryptMD5;
import org.silverpeas.core.exception.SilverpeasException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class performs the authentication using an SQL table.
 */
public class AuthenticationSQL extends Authentication {

  protected String dataSourceJndiName;
  protected String userTableName;
  protected String loginColumnName;
  protected String passwordColumnName;
  protected String passwordAvailableColumnName;

  @Override
  public void loadProperties(SettingBundle settings) {
    String serverName = getServerName();
    dataSourceJndiName = settings.getString(serverName + ".SQLDataSourceJNDIName");
    userTableName = settings.getString(serverName + ".SQLUserTableName");
    loginColumnName = settings.getString(serverName + ".SQLUserLoginColumnName");
    passwordColumnName = settings.getString(serverName + ".SQLUserPasswordColumnName");
    passwordAvailableColumnName = settings.getString(serverName
        + ".SQLUserPasswordAvailableColumnName");
  }

  @Override
  protected AuthenticationConnection<Connection> openConnection() throws AuthenticationException {
    try {
      DataSource dataSource = InitialContext.doLookup(dataSourceJndiName);
      Connection connection = dataSource.getConnection();
      return new AuthenticationConnection<Connection>(connection);
    } catch (Exception iex) {
      throw new AuthenticationHostException("AuthenticationSQL.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          "Datasource=" + dataSourceJndiName, iex);
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
      throw new AuthenticationHostException("AuthenticationSQL.closeConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED",
          "Datasource=" + dataSourceJndiName, ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
      AuthenticationCredential credential) throws AuthenticationException {
    String login = credential.getLogin();
    String password = credential.getPassword();
    if (password == null) {
      password = "";
    }
    String sqlPassword = getPassword(getSQLConnection(connection), login);
    if (!StringUtil.isDefined(sqlPassword)) {
      throw new AuthenticationBadCredentialException("AuthenticationSQL.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL",
          "User=" + login);
    } else {
      checkPassword(login, password, sqlPassword);
    }
    // Verifying if the user must change his password or if user will soon have to change his
    // password
    AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(credential).verify();

  }

  private String getPassword(Connection connection, String login) throws AuthenticationException {
    String loginQuery;
    String sqlPasswd;
    ResultSet rs = null;
    PreparedStatement stmt = null;
    if (StringUtil.isDefined(passwordAvailableColumnName)) {
      loginQuery = "SELECT " + loginColumnName + ", " + passwordColumnName + ", "
          + passwordAvailableColumnName + " FROM " + userTableName + " WHERE " + loginColumnName
          + " = ?";
    } else {
      loginQuery = "SELECT " + loginColumnName + ", " + passwordColumnName + " FROM "
          + userTableName + " WHERE " + loginColumnName + " = ?";
    }

    try {
      stmt = connection.prepareStatement(loginQuery);
      stmt.setString(1, login);
      rs = stmt.executeQuery();
      if (rs.next()) {
        if (StringUtil.isDefined(passwordAvailableColumnName)) {
          String validString = rs.getString(passwordAvailableColumnName);
          if ("N".equalsIgnoreCase(validString)) {
            throw new AuthenticationPwdNotAvailException("AuthenticationSQL.getPassword()",
                SilverpeasException.ERROR, "authentication.EX_PWD_NOT_AVAILABLE", "User=" + login);
          }
        }
        sqlPasswd = rs.getString(passwordColumnName);
      } else {
        throw new AuthenticationBadCredentialException("AuthenticationSQL.doAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + login);
      }

    } catch (SQLException ex) {
      throw new AuthenticationHostException("AuthenticationSQL.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(rs, stmt);
    }
    return sqlPasswd;
  }

  private void updatePassword(Connection connection, String login, String newPassword)
      throws AuthenticationException {
    PreparedStatement stmt = null;
    String updateQuery = "UPDATE " + userTableName + " SET " + passwordColumnName + " = ? WHERE "
        + loginColumnName + " = ?";
    try {
      stmt = connection.prepareStatement(updateQuery);
      stmt.setString(1, newPassword);
      stmt.setString(2, login);
      stmt.executeUpdate();
    } catch (SQLException ex) {
      throw new AuthenticationHostException("AuthenticationSQL.updatePassword()",
          SilverpeasException.ERROR, "authentication.EX_SQL_ACCESS_ERROR", ex);
    } finally {
      DBUtil.close(stmt);
    }
  }

  @Override
  protected void doChangePassword(AuthenticationConnection connection,
      AuthenticationCredential credential, String newPassword) throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    String passwordInDB = getPassword(sqlConnection, login);
    checkPassword(login, oldPassword, passwordInDB);
    String newPasswordInDB = getNewPasswordDigest(newPassword);
    updatePassword(sqlConnection, login, newPasswordInDB);
  }

  @Override
  protected void doResetPassword(AuthenticationConnection connection, String login,
      String newPassword) throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    String newPasswordInDB = getNewPasswordDigest(newPassword);
    updatePassword(sqlConnection, login, newPasswordInDB);
  }

  private static Connection getSQLConnection(AuthenticationConnection connection) {
    return (Connection) connection.getConnector();
  }

  /**
   * Computes the digest of the new password with a new random salt.
   *
   * @param newPassword the new password for which a digest has to be computed.
   * @return the digest of the new password.
   */
  private String getNewPasswordDigest(String newPassword) {
    PasswordEncryption encryption = PasswordEncryptionProvider.getDefaultPasswordEncryption();
    return encryption.encrypt(newPassword);
  }

  /**
   * Checks the specified password associated with the specified login matches the specified
   * password digest.
   *
   * As some passwords have been errorly computed in a pure MD5 encryption, this method takes care
   * of this situation. In the case the password was encrypted with a correct cryptographic function
   * (that is to say with another a MD5 hash function), the encryption having computed the specified
   * digest is then used to encrypt the password in order to compare them.
   *
   * @param login a user login.
   * @param password the password associated with the user login.
   * @param digest the digest of the password by using the current encryption.
   * @throws AuthenticationBadCredentialException
   */
  private void checkPassword(String login, String password, String digest)
      throws AuthenticationBadCredentialException {
    try {
      PasswordEncryption encryption = PasswordEncryptionProvider.getPasswordEncryption(digest);
      encryption.check(password, digest);
    } catch (AssertionError error) {
      // the password doesn't match the digest. It is then possible the digest was a pure MD5 one!
      String actualDigest = CryptMD5.encrypt(password);
      if (!actualDigest.equals(digest)) {
        throw new AuthenticationBadCredentialException("AuthenticationSQL.doChangePassword()",
            SilverpeasException.ERROR, "authentication.EX_INCORRECT_PASSWORD", "User=" + login);
      }
    }
  }
}
