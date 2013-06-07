/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.authentication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.stratelia.webactiv.beans.admin.UserFull;
import org.silverpeas.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.authentication.exception.AuthenticationException;
import org.silverpeas.authentication.exception.AuthenticationHostException;
import org.silverpeas.authentication.exception.AuthenticationPasswordExpired;
import org.silverpeas.authentication.exception.AuthenticationPasswordMustBeChangedAtNextLogon;
import org.silverpeas.authentication.exception.AuthenticationPasswordMustBeChangedOnFirstLogin;
import org.silverpeas.authentication.exception.AuthenticationPwdNotAvailException;
import org.silverpeas.authentication.exception.AuthenticationUserAccountBlockedException;
import org.silverpeas.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.authentication.verifier.UserMustChangePasswordVerifier;

import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A service for authenticating a user in Silverpeas. This service is the entry point for any
 * authentication process as it wraps all the mechanism and the delegation to perform the actual
 * authentication.
 *
 * This service wraps all the mechanism to perform the authentication process itself. It uses for
 * doing an authentication server that is mapped with the user domain.
 */
public class AuthenticationService {

  private static final String module = "authentication";

  static final protected String m_JDBCUrl;
  static final protected String m_AccessLogin;
  static final protected String m_AccessPasswd;
  static final protected String m_DriverClass;
  static final protected String m_DomainTableName;
  static final protected String m_DomainIdColumnName;
  static final protected String m_DomainNameColumnName;
  static final protected String m_DomainAuthenticationServerColumnName;
  static final protected String m_KeyStoreTableName;
  static final protected String m_KeyStoreKeyColumnName;
  static final protected String m_KeyStoreLoginColumnName;
  static final protected String m_KeyStoreDomainIdColumnName;
  static final protected String m_UserTableName;
  static final protected String m_UserIdColumnName;
  static final protected String m_UserLoginColumnName;
  static final protected String m_UserDomainColumnName;

  static protected int m_AutoInc = 1;
  public static final String ERROR_PWD_EXPIRED = "Error_PwdExpired";
  public static final String ERROR_PWD_MUST_BE_CHANGED = "Error_PwdMustBeChanged";

  static {
    ResourceLocator propFile = new ResourceLocator(
        "com.stratelia.silverpeas.authentication.domains", "");

    // Lecture du fichier de proprietes
    m_JDBCUrl = propFile.getString("SQLDomainJDBCUrl");
    m_AccessLogin = propFile.getString("SQLDomainAccessLogin");
    m_AccessPasswd = propFile.getString("SQLDomainAccessPasswd");
    m_DriverClass = propFile.getString("SQLDomainDriverClass");

    m_DomainTableName = propFile.getString("SQLDomainTableName");
    m_DomainIdColumnName = propFile.getString("SQLDomainIdColumnName");
    m_DomainNameColumnName = propFile.getString("SQLDomainNameColumnName");
    m_DomainAuthenticationServerColumnName = propFile.getString(
        "SQLDomainAuthenticationServerColumnName");

    m_KeyStoreTableName = propFile.getString("SQLKeyStoreTableName");
    m_KeyStoreKeyColumnName = propFile.getString("SQLKeyStoreKeyColumnName");
    m_KeyStoreLoginColumnName = propFile.getString("SQLKeyStoreLoginColumnName");
    m_KeyStoreDomainIdColumnName = propFile.getString("SQLKeyStoreDomainIdColumnName");

    m_UserTableName = propFile.getString("SQLUserTableName");
    m_UserIdColumnName = propFile.getString("SQLUserIdColumnName");
    m_UserLoginColumnName = propFile.getString("SQLUserLoginColumnName");
    m_UserDomainColumnName = propFile.getString("SQLUserDomainColumnName");
  }

  /**
   * Constructs a new AuthenticationService instance.
   */
  public AuthenticationService() {
  }

  /**
   * Opens a new connection to the Silverpeas database.
   */
  static private Connection openConnection() throws AuthenticationException {
    Properties info = new Properties();
    Driver driverSQL;
    Connection con;

    try {
      info.setProperty("user", m_AccessLogin);
      info.setProperty("password", m_AccessPasswd);
      driverSQL = (Driver) Class.forName(m_DriverClass).newInstance();
    } catch (Exception iex) {
      throw new AuthenticationHostException("AuthenticationService.openConnection()",
          SilverpeasException.ERROR, "root.EX_CANT_INSTANCIATE_DB_DRIVER", "Driver=" + m_DriverClass,
          iex);
    }
    try {
      con = driverSQL.connect(m_JDBCUrl, info);
    } catch (SQLException ex) {
      throw new AuthenticationHostException("AuthenticationService.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", "JDBCUrl=" + m_JDBCUrl, ex);
    }
    return con;
  }

  /**
   * Closes the specified connection to the Silverpeas database
   */
  static private void closeConnection(Connection con) {
    DBUtil.close(con);
  }

  /**
   * Gets all the available user domains. A domain in Silverpeas is a repository of users with its
   * its own authentication process.
   *
   * At each user domain is associated an authentication server that is responsible of the
   * authentication of the domain's users.
   *
   * @return an unmodifiable list of user domains.
   */
  public List<Domain> getAllDomains() {
    List<Domain> domains;
    try {
      domains = Arrays.asList(AdminReference.getAdminService().getAllDomains());
    } catch (AdminException e) {
      SilverTrace.error(module, "AuthenticationService", "Problem to retrieve all the domains", e);
      domains = Collections.EMPTY_LIST;
    }
    return domains;
  }

  /**
   * Authenticates a user with the specified authentication credential.
   *
   * If the authentication succeed, the security-related capabilities, mapped to the user's
   * credential, are set from information sent back by the authentication server related to the
   * domain to which the user belongs.
   *
   * @param userCredential the credential of the user to use to authenticate him.
   * @return an authentication key or null if the authentication fails. The authentication key
   * identifies uniquely the status of the user authentication and it is unique to the user so that
   * he can be identified from it.
   */
  public String authenticate(final AuthenticationCredential userCredential) {
    String key = null;
    if (userCredential.getLogin() != null) {
      if (userCredential.isPasswordSet()) {
        key = authenticateByLoginAndPasswordAndDomain(userCredential);
      } else {
        key = authenticateByLoginAndDomain(userCredential);
      }
    }
    return key;
  }

  /**
   * Authenticates the user with the login, password, and domain contained in the specified
   * authentication credential.
   *
   * @param credential an authentication credential with the login and the password of the user, and
   * with the domain to which the user belongs.
   * @return an authentication key if the authentication succeed, null otherwise.
   */
  private String authenticateByLoginAndPasswordAndDomain(AuthenticationCredential credential) {
    // Test data coming from calling page
    String login = credential.getLogin();
    String password = credential.getPassword();
    String domainId = credential.getDomainId();
    if (login == null || password == null || domainId == null) {
      return null;
    }

    Connection connection = null;
    try {
      // Open connection
      connection = openConnection();

      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      // Store information about password change capabilities
      credential.getCapabilities().put(Authentication.PASSWORD_CHANGE_ALLOWED,
          (authenticationServer.isPasswordChangeAllowed()) ? "yes" : "no");

      // Verify that the user can login
      AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential).verify();

      // Authentification test
      authenticationServer.authenticate(credential);

      // Generate a random key and store it in database
      return getAuthenticationKey(login, domainId);

    } catch (AuthenticationException ex) {
      SilverTrace.error(module,
          "AuthenticationService.authenticate()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User="
          + login, ex);
      String errorCause = "Error_2";
      Exception nested = ex.getNested();
      if (nested != null) {
        if (nested instanceof AuthenticationException) {
          ex = (AuthenticationException) nested;
        }
      }
      if (ex instanceof AuthenticationBadCredentialException) {
        errorCause = "Error_1";
      } else if (ex instanceof AuthenticationHostException) {
        errorCause = "Error_2";
      } else if (ex instanceof AuthenticationPwdNotAvailException) {
        errorCause = "Error_5";
      } else if (ex instanceof AuthenticationPasswordExpired) {
        errorCause = ERROR_PWD_EXPIRED;
      } else if (ex instanceof AuthenticationPasswordMustBeChangedAtNextLogon) {
        errorCause = ERROR_PWD_MUST_BE_CHANGED;
      } else if (ex instanceof AuthenticationPasswordMustBeChangedOnFirstLogin) {
        errorCause = UserMustChangePasswordVerifier.ERROR_PWD_MUST_BE_CHANGED_ON_FIRST_LOGIN;
      } else if (ex instanceof AuthenticationUserAccountBlockedException) {
        errorCause = UserCanLoginVerifier.ERROR_USER_ACCOUNT_BLOCKED;
      }
      return errorCause;
    } finally {
      closeConnection(connection);
    }
  }

  /**
   * Authenticates the user only by its login and the domain to which he belongs.
   *
   * @param credential an authentication credential with the login and the domain to which the user
   * belongs.
   * @return an authentication key if the authentication succeed, null otherwise.
   */
  private String authenticateByLoginAndDomain(AuthenticationCredential credential) {
    // Test data coming from calling page
    String login = credential.getLogin();
    String domainId = credential.getDomainId();
    if (login == null || domainId == null) {
      return null;
    }

    PreparedStatement prepStmt = null;
    ResultSet resultSet = null;
    Connection connection = null;
    boolean authenticationOK = false;
    try {
      // Open connection
      connection = openConnection();

      String query = "SELECT " + m_UserIdColumnName + " FROM "
          + m_UserTableName + " WHERE " + m_UserLoginColumnName + " = ? AND "
          + m_UserDomainColumnName + " = ?";
      prepStmt = connection.prepareStatement(query);

      prepStmt.setString(1, login);
      prepStmt.setInt(2, Integer.parseInt(domainId));

      resultSet = prepStmt.executeQuery();

      authenticationOK = resultSet.next();
    } catch (Exception ex) {
      SilverTrace.warn(module, "AuthenticationService.authenticate()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User=" + login, ex);
      return "Error_2";
    } finally {
      DBUtil.close(resultSet, prepStmt);
      closeConnection(connection);
    }

    String key = null;

    if (authenticationOK) {
      // Generate a random key and store it in database
      try {
        key = getAuthenticationKey(login, domainId);
      } catch (Exception e) {
        SilverTrace.warn(module, "AuthenticationService.authenticate()",
            "authentication.EX_CANT_GET_AUTHENTICATION_KEY", "DomainId=" + domainId + ";User="
            + login, e);
        return "Error_2";
      }
    }

    return key;
  }

  /**
   * Changes the password of the specified user credential with the specified new one. In order to
   * change the password of a user, the user will be first authenticated. The specified credential
   * won't be updated by the password change.
   *
   * @param credential the current authentication credential of the user.
   * @param newPassword User new password the new password to set.
   * @throws AuthenticationException if an error occurs while changing the password of the specified
   * credential.
   */
  public void changePassword(AuthenticationCredential credential, String newPassword)
      throws AuthenticationException {
    changePasswordAndEmail(credential, newPassword, null);
  }

  /**
   * Changes the password and email of the specified user credential with the specified new ones.
   * In order to change the password and email of a user, the user will be first authenticated.
   * The specified credential won't be updated by the password change.
   * @param credential the current authentication credential of the user.
   * @param newPassword User new password the new password to set.
   * @param email User email the email to set.
   * @throws AuthenticationException if an error occurs while changing the password and email of the
   * specified credential.
   */
  public void changePasswordAndEmail(AuthenticationCredential credential, String newPassword,
      String email) throws AuthenticationException {
    // Test data coming from calling page
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    String domainId = credential.getDomainId();
    if (login == null || oldPassword == null || domainId == null || newPassword == null) {
      throw new AuthenticationBadCredentialException("AuthenticationService.changePassword",
          SilverpeasException.ERROR, "authentication.EX_NULL_VALUE_DETECTED");
    }

    // Verify that the user can login
    AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential).verify();
    Connection connection = null;
    try {
      // Open connection
      connection = openConnection();

      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      // Authentication test
      authenticationServer.changePassword(credential, newPassword);
    } catch (AuthenticationException ex) {
      SilverTrace.error(module, "AuthenticationService.changePassword()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User=" + login, ex);
      throw ex;
    } finally {
      closeConnection(connection);
    }

    // Treatments on password change
    onPasswordAndEmailChanged(credential, email);
  }

  /**
   * Gets an authentication key for a given user from its specified login and from the domain to
   * which he belongs. This method doesn't perform any authentication but it only set a new
   * authentication key for the given user. This method can be used, for example, to let a user who
   * has forgotten its password of setting a new one.
   *
   * @param login the user login.
   * @param domainId the unique identifier of the domain of the user.
   * @return an authentication key.
   */
  public String getAuthenticationKey(String login, String domainId) throws AuthenticationException {
    String authKey = computeGenerationKey(login);
    storeAuthenticationKey(login, domainId, authKey);
    return authKey;
  }

  /**
   * Gets the Authentication Server name for the given domain.
   *
   * @param domainId the unique domain identifier.
   * @return the authentication server name related to the specified domain.
   */
  private String getAuthenticationServerName(Connection con, String domainId)
      throws AuthenticationException {
    Statement stmt = null;
    ResultSet rs = null;
    String query = "SELECT " + m_DomainAuthenticationServerColumnName
        + " FROM " + m_DomainTableName + " WHERE " + m_DomainIdColumnName
        + " = " + domainId + "";

    SilverTrace.info(module, "AuthenticationService.getAuthenticationServerName()",
        "root.MSG_GEN_PARAM_VALUE", "query=" + query);
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      if (rs.next()) {
        String serverName = rs.getString(m_DomainAuthenticationServerColumnName);
        if (!StringUtil.isDefined(serverName)) {
          throw new AuthenticationException(
              "AuthenticationService.getAuthenticationServerName()",
              SilverpeasException.ERROR, "authentication.EX_SERVER_NOT_FOUND",
              "DomainId=" + domainId);
        } else {
          return serverName;
        }
      } else {
        throw new AuthenticationException(
            "AuthenticationService.getAuthenticationServerName()",
            SilverpeasException.ERROR, "authentication.EX_DOMAIN_NOT_FOUND",
            "DomainId=" + domainId);
      }
    } catch (SQLException ex) {
      throw new AuthenticationException(
          "AuthenticationService.getAuthenticationServerName()",
          SilverpeasException.ERROR, "authentication.EX_DOMAIN_INFO_ERROR",
          "DomainId=" + domainId);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Builds a random authentication key.
   *
   * @param login a user login
   * @return the generated authentication key.
   */
  private String computeGenerationKey(String login) {
    // Random key generation
    long nStart = login.hashCode() * new Date().getTime() * (m_AutoInc++);
    Random rand = new Random(nStart);
    int key = rand.nextInt();

    return String.valueOf(key);
  }

  private void storeAuthenticationKey(String login, String domainId, String sKey)
      throws AuthenticationException {
    Statement stmt = null;
    int key = Integer.parseInt(sKey);

    String query = "INSERT INTO " + m_KeyStoreTableName + "("
        + m_KeyStoreKeyColumnName + ", " + m_KeyStoreLoginColumnName + ", "
        + m_KeyStoreDomainIdColumnName + ")" + " VALUES (" + key + ", '"
        + login + "', " + domainId + ")";

    Connection m_Connection = null;
    try {
      m_Connection = openConnection();

      stmt = m_Connection.createStatement();
      stmt.execute(query);
      SilverTrace.info(module, "AuthenticationService.storeAuthenticationKey()",
          "root.MSG_GEN_PARAM_VALUE", "query=" + query);
    } catch (SQLException ex) {
      SilverTrace.error(module, "AuthenticationService.storeAuthenticationKey()",
          "authentication.EX_WRITE_KEY_ERROR", "User=" + login + " exception=" + ex.getSQLState());
    } finally {
      DBUtil.close(stmt);
      closeConnection(m_Connection);
    }
  }

  /**
   * Resets the specified password of the user behind the specified authentication credential with
   * the specified one. The reset operation can only be performed if the password change is allowed
   * by the domain to which the user belongs. It doesn't require the user to be authenticated but,
   * as consequence, requires to be run in a privileged mode (only an administrator or the system
   * itself can do this operation). The privileged mode isn't checked by this method, hence it is
   * the responsibility of the caller to ensure this. The specified credential won't be updated by
   * the password reset.
   *
   * @param credential the authentication credential of the user for which the password has to be
   * reset.
   * @param newPassword the password with which the credential password will be reset.
   * @throws AuthenticationException if an error occurs while resetting the credential password.
   */
  public void resetPassword(AuthenticationCredential credential, String newPassword)
      throws AuthenticationException {
    // Test data coming from calling page
    String login = credential.getLogin();
    String domainId = credential.getDomainId();
    if (login == null || domainId == null || newPassword == null) {
      throw new AuthenticationBadCredentialException("AuthenticationService.resetPassword",
          SilverpeasException.ERROR, "authentication.EX_NULL_VALUE_DETECTED");
    }

    // Verify that the user can login
    AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential).verify();

    Connection connection = null;
    try {
      // Open connection
      connection = openConnection();

      // Build a AuthenticationServer instance
      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      // Authentication test
      authenticationServer.resetPassword(login, newPassword);
    } catch (AuthenticationException ex) {
      SilverTrace.error(module, "AuthenticationService.resetPassword()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User=" + login, ex);
      throw ex;
    } finally {
      closeConnection(connection);
    }

    // Treatments on password change
    onPasswordAndEmailChanged(credential, null);
  }

  /**
   * Treatments on password change.
   *
   * @param credential
   * @param email
   */
  private void onPasswordAndEmailChanged(AuthenticationCredential credential, final String email) {
    AdminController admin = new AdminController(null);
    UserDetail user = UserDetail
        .getById(admin.getUserIdByLoginAndDomain(credential.getLogin(), credential.getDomainId()));

    // Notify that the user has changed his password.
    AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(user)
        .notifyPasswordChange();

    // Updating some data
    UserFull userFull = admin.getUserFull(user.getId());

    // Reset the counter of number of successful connections for the given user.
    userFull.setNbSuccessfulLoginAttempts(0);

    // Register the date of credential change
    userFull.setLastLoginCredentialUpdateDate(new Date());

    // Set email
    if (StringUtil.isDefined(email)) {
      userFull.seteMail(email);
    }

    // Persisting user data changes.
    admin.updateUserFull(userFull);
  }

  /**
   * Is the change of a user password is allowed by specified user domain?
   *
   * @param domainId the unique identifier of the user domain.
   * @return true if the password of the users in the specified domain can be changed, false
   * otherwise.
   */
  public boolean isPasswordChangeAllowed(String domainId) {
    Connection connection = null;
    try {
      // Open connection
      connection = openConnection();

      // Build a AuthenticationServer instance
      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      return authenticationServer.isPasswordChangeAllowed();
    } catch (AuthenticationException ex) {
      SilverTrace.error(module, "AuthenticationService.isPasswordChangeAllowed()",
          "authentication.EX_AUTHENTICATION_STATUS_ERROR", "DomainId=" + domainId + " exception="
          + ex.getMessage());
    } finally {
      closeConnection(connection);
    }
    return false;
  }

  private AuthenticationServer getAuthenticationServer(Connection con, String domainId)
      throws AuthenticationException {
    // Get authentication server name
    String authenticationServerName = getAuthenticationServerName(con, domainId);

    // Return the AuthenticationServer instance with the specified unique name
    return AuthenticationServer.getAuthenticationServer(authenticationServerName);
  }

}
