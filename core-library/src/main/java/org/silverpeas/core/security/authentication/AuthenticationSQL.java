/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPwdNotAvailException;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.encryption.cipher.CryptMD5;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

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
      return new AuthenticationConnection<>(connection);
    } catch (Exception iex) {
      throw new AuthenticationHostException(
          "Connection failure with datasource " + dataSourceJndiName, iex);
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
          "Cannot close the connection with datasource " + dataSourceJndiName, ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
      AuthenticationCredential credential) throws AuthenticationException {
    String login = credential.getLogin();
    boolean loginIgnoreCase = credential.loginIgnoreCase();
    String password = credential.getPassword();
    if (password == null) {
      password = "";
    }
    String sqlPassword = getPassword(getSQLConnection(connection), login, loginIgnoreCase);
    if (!StringUtil.isDefined(sqlPassword)) {
      throw new AuthenticationBadCredentialException(
          "Invalid credential for user with login: " + login);
    } else {
      checkPassword(login, password, sqlPassword);
    }
    // Verifying if the user must change his password or if user will soon have to change his
    // password
    AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(credential).verify();

  }

  private String getPassword(Connection connection, String login, final boolean ignoreCase)
      throws AuthenticationException {
    String loginQuery;
    String sqlPasswd;
    if (StringUtil.isDefined(passwordAvailableColumnName)) {
      loginQuery = "SELECT " + loginColumnName + ", " + passwordColumnName + ", "
          + passwordAvailableColumnName + " FROM " + userTableName;
    } else {
      loginQuery = "SELECT " + loginColumnName + ", " + passwordColumnName + " FROM "
          + userTableName;
    }
    if (ignoreCase) {
      loginQuery += " WHERE lower(" + loginColumnName + ") = lower(?)";
    } else {
      loginQuery += " WHERE " + loginColumnName + " = ?";
    }
    try (final PreparedStatement stmt = connection.prepareStatement(loginQuery)) {
      stmt.setString(1, login);
      try (final ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          if (StringUtil.isDefined(passwordAvailableColumnName)) {
            String validString = rs.getString(passwordAvailableColumnName);
            if ("N".equalsIgnoreCase(validString)) {
              throw new AuthenticationPwdNotAvailException(
                  "Not password set for user with login: " + login);
            }
          }
          sqlPasswd = rs.getString(passwordColumnName);
        } else {
          throw new AuthenticationBadCredentialException("User not found with login: " + login);
        }
      }
    } catch (SQLException ex) {
      throw new AuthenticationHostException(ex);
    }
    return sqlPasswd;
  }

  private void updatePassword(Connection connection, String login, final boolean loginIgnoreCase,
      String newPassword)
      throws AuthenticationException {
    String updateQuery = "UPDATE " + userTableName + " SET " + passwordColumnName + " = ? WHERE ";
    if (loginIgnoreCase) {
      updateQuery += "lower(" + loginColumnName + ") = lower(?)";
    } else {
      updateQuery += loginColumnName + " = ?";
    }
    try (final PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
      stmt.setString(1, newPassword);
      stmt.setString(2, login);
      stmt.executeUpdate();
    } catch (SQLException ex) {
      throw new AuthenticationHostException(ex);
    }
  }

  @Override
  protected void doChangePassword(AuthenticationConnection connection,
      AuthenticationCredential credential, String newPassword) throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    final boolean loginIgnoreCase = credential.loginIgnoreCase();
    String passwordInDB = getPassword(sqlConnection, login, loginIgnoreCase);
    checkPassword(login, oldPassword, passwordInDB);
    String newPasswordInDB = getNewPasswordDigest(newPassword);
    updatePassword(sqlConnection, login, loginIgnoreCase, newPasswordInDB);
  }

  @Override
  protected void doResetPassword(AuthenticationConnection connection, String login,
      final boolean loginIgnoreCase, String newPassword) throws AuthenticationException {
    Connection sqlConnection = getSQLConnection(connection);
    String newPasswordInDB = getNewPasswordDigest(newPassword);
    updatePassword(sqlConnection, login, loginIgnoreCase, newPasswordInDB);
  }

  protected static Connection getSQLConnection(AuthenticationConnection<Connection> connection) {
    return connection.getConnector();
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
   * @param login a user login, used only in case of error.
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
        throw new AuthenticationBadCredentialException(
            "Invalid credential for user with login: " + login);
      }
    }
  }
}
