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

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.authentication.AuthenticationResponse.Status;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordExpired;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordMustBeChangedAtNextLogon;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordMustBeChangedOnFirstLogin;
import org.silverpeas.core.security.authentication.exception.AuthenticationPwdNotAvailException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserAccountBlockedException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserAccountDeactivatedException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * A service for authenticating a user in Silverpeas. This service is the entry point for any
 * authentication process as it wraps all the mechanism and the delegation to perform the actual
 * authentication.
 * <p>
 * This service wraps all the mechanism to perform the authentication process itself. It uses for
 * doing an authentication server that is mapped with the user domain.
 */
@Service
@Singleton
public class AuthenticationService implements Authentication {

  private static final String DATA_SOURCE_JNDI_NAME;
  private static final String DOMAIN_TABLE_NAME;
  private static final String DOMAIN_ID_COLUMN_NAME;
  private static final String DOMAIN_AUTHENTICATION_SERVER_COLUMN_NAME;
  private static final String KEY_STORE_TABLE_NAME;
  private static final String KEY_STORE_KEY_COLUMN_NAME;
  private static final String KEY_STORE_LOGIN_COLUMN_NAME;
  private static final String KEY_STORE_DOMAIN_ID_COLUMN_NAME;
  private static final String USER_TABLE_NAME;
  private static final String USER_ID_COLUMN_NAME;
  private static final String USER_LOGIN_COLUMN_NAME;
  private static final String USER_DOMAIN_COLUMN_NAME;
  private static int autoInc = 1;

  @Inject
  private AdminController adminController;

  private static final Predicate<Domain> DOMAIN_WITH_AUTHENTICATION_SERVER = d -> {
    final AuthenticationServer authenticationServer =
        AuthenticationServer.getAuthenticationServer(d.getAuthenticationServer());
    return authenticationServer.hasProtocols();
  };

  /**
   * Constructs a new AuthenticationService instance.
   */
  protected AuthenticationService() {
  }

  /**
   * Opens a new connection to the Silverpeas database.
   */
  private static Connection openConnection() throws AuthenticationException {
    Connection connection;

    try {
      DataSource dataSource = InitialContext.doLookup(DATA_SOURCE_JNDI_NAME);
      connection = dataSource.getConnection();
    } catch (Exception iex) {
      throw new AuthenticationHostException(
          "Connection failure with datasource " + DATA_SOURCE_JNDI_NAME, iex);
    }
    return connection;
  }

  /**
   * Closes the specified connection to the Silverpeas database
   */
  private static void closeConnection(Connection con) {
    DBUtil.close(con);
  }

  /**
   * Gets all the available user domains. A domain in Silverpeas is a repository of users with its
   * own authentication process.
   * <p>
   * At each user domain is associated an authentication server that is responsible for the
   * authentication of the domain's users.
   * @return an unmodifiable list of user domains.
   */
  @Nonnull
  public List<AuthDomain> getAllAuthDomains() {
    List<AuthDomain> domains;
    try {
      domains = stream(Administration.get()
          .getAllDomains()).filter(DOMAIN_WITH_AUTHENTICATION_SERVER)
          .collect(Collectors.toList());
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
      domains = Collections.emptyList();
    }
    return domains;
  }

  @Override
  public AuthenticationResponse authenticate(final AuthenticationCredential userCredential) {
    if (StringUtil.isNotDefined(userCredential.getLogin())) {
      SilverLogger.getLogger(this).error("No login is passed in the user credentials!");
      return AuthenticationResponse.error(Status.UNKNOWN_FAILURE);
    }

    AuthenticationResponse result;
    try {
      String token = checkAuthentication(userCredential);
      result = AuthenticationResponse.succeed(token);
    } catch (AuthenticationBadCredentialException e) {
      if (isThereMultipleDomainsDefined()) {
        result = AuthenticationResponse.error(Status.BAD_LOGIN_PASSWORD_DOMAIN);
      } else {
        result = AuthenticationResponse.error(Status.BAD_LOGIN_PASSWORD);
      }
    } catch (AuthenticationPwdNotAvailException e) {
      result = AuthenticationResponse.error(Status.NO_PASSWORD);
    } catch (AuthenticationPasswordExpired e) {
      result = AuthenticationResponse.error(Status.PASSWORD_EXPIRED);
    } catch (AuthenticationPasswordMustBeChangedAtNextLogon e) {
      result = AuthenticationResponse.error(Status.PASSWORD_TO_CHANGE);
    } catch (AuthenticationPasswordMustBeChangedOnFirstLogin e) {
      result = AuthenticationResponse.error(Status.PASSWORD_TO_CHANGE_ON_FIRST_LOGIN);
    } catch (AuthenticationUserAccountBlockedException e) {
      result = AuthenticationResponse.error(Status.USER_ACCOUNT_BLOCKED);
    } catch (AuthenticationUserAccountDeactivatedException e) {
      result = AuthenticationResponse.error(Status.USER_ACCOUNT_DEACTIVATED);
    } catch (AuthenticationException ae) {
      result = AuthenticationResponse.error(Status.UNKNOWN_FAILURE);
    }

    if (!result.getStatus().succeeded()) {
      SilverLogger.getLogger(this)
          .error("authentication error ({0}) with login ''{1}'' and domain id ''{2}''",
              result.getStatus().toString(), userCredential.getLogin(),
              userCredential.getDomainId());
    }

    return result;
  }

  private String checkAuthentication(final AuthenticationCredential userCredential)
      throws AuthenticationException {
    final String key;
    if (userCredential.isPasswordSet()) {
      key = authenticateByLoginAndPasswordAndDomain(userCredential);
    } else {
      key = authenticateByLoginAndDomain(userCredential);
    }
    return key;
  }

  /**
   * Authenticates the user with the login, password, and domain contained in the specified
   * authentication credential.
   * @param credential an authentication credential with the login and the password of the user, and
   * with the domain to which the user belongs.
   * @return an authentication key if the authentication succeed, null otherwise.
   */
  private String authenticateByLoginAndPasswordAndDomain(AuthenticationCredential credential)
      throws AuthenticationException {
    // Test data coming from calling page
    String login = credential.getLogin();
    String password = credential.getPassword();
    String domainId = credential.getDomainId();
    if (login == null || password == null || domainId == null) {
      return null;
    }

    // Verify that the user can log in
    AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential).verify();

    try (Connection connection = openConnection()) {
      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      // Store information about password change capabilities
      credential.getCapabilities()
          .put(AuthenticationProtocol.PASSWORD_CHANGE_ALLOWED,
              authenticationServer.isPasswordChangeAllowed() ? "yes" : "no");

      // Authentication test
      authenticationServer.authenticate(credential);

      // Generate a random key and store it in database
      return getAuthToken(credential);

    } catch (SQLException e) {
      SilverLogger.getLogger(this).warn(e);
      throw new AuthenticationException(e);
    }
  }

  /**
   * Authenticates the user only by its login and the domain to which he belongs.
   * @param credential an authentication credential with the login and the domain to which the user
   * belongs.
   * @return an authentication key if the authentication succeed, null otherwise.
   */
  private String authenticateByLoginAndDomain(AuthenticationCredential credential)
      throws AuthenticationException {
    // Test data coming from calling page
    String login = credential.getLogin();
    String domainId = credential.getDomainId();
    if (login == null || domainId == null) {
      return null;
    }
    final boolean authenticationOK;
    try (Connection connection = openConnection()) {
      final JdbcSqlQuery query = JdbcSqlQuery.select(USER_ID_COLUMN_NAME)
          .from(USER_TABLE_NAME)
          .where(USER_DOMAIN_COLUMN_NAME + " = ?", Integer.parseInt(domainId));
      if (credential.loginIgnoreCase()) {
        query.and("lower(" + USER_LOGIN_COLUMN_NAME + ") = lower(?)", login);
      } else {
        query.and(USER_LOGIN_COLUMN_NAME + " = ?", login);
      }
      authenticationOK = !query.executeWith(connection, row -> true).isEmpty();
    } catch (Exception ex) {
      SilverLogger.getLogger(this).warn(ex);
      throw new AuthenticationException(ex);
    }

    if (authenticationOK) {
      // Verify that the user can log in
      AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential).verify();

      // Generate a random key and store it in database
      try {
        return getAuthToken(credential);
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
        throw new AuthenticationException(e);
      }
    }
    return null;
  }

  /**
   * Changes the password of the specified user credential with the specified new one. In order to
   * change the password of a user, the user will be first authenticated. The specified credential
   * won't be updated by the password change.
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
   * Changes the password and email of the specified user credential with the specified new ones. In
   * order to change the password and email of a user, the user will be first authenticated. The
   * specified credential won't be updated by the password change.
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
      throw new AuthenticationBadCredentialException(
          "The login, the password or the domain isn't set!");
    }

    // Verify that the user can log in
    final UserCanLoginVerifier userCanLoginVerifier =
        AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential);
    userCanLoginVerifier.verify();
    try (Connection connection = openConnection()) {
      getAuthenticationServer(connection, domainId).changePassword(credential, newPassword);
      AuthenticationUserVerifierFactory.removeFromRequestCache(userCanLoginVerifier.getUser());
    } catch (AuthenticationException ex) {
      SilverLogger.getLogger(this).warn(ex);
      throw ex;
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(e);
      throw new SilverpeasRuntimeException(e);
    }

    // Treatments on password change
    onPasswordAndEmailChanged(credential, email);
  }

 @Override
  public String getAuthToken(AuthenticationCredential credential) {
    String authKey = generateTokenFor(credential.getLogin());
    storeAuthenticationKey(credential.getLogin(), credential.getDomainId(), authKey);
    return authKey;
  }

  @Override
  public User getUserByAuthToken(final String authToken) throws AuthenticationException {
    Administration admin = Administration.get();
    try {
      String userId = admin.getUserIdByAuthenticationKey(authToken);
      return admin.getUserDetail(userId);
    } catch (AdminException e) {
      throw new AuthenticationException(e);
    }
  }

  /**
   * Gets the Authentication server name for the given domain.
   * @param domainId the unique domain identifier.
   * @return the authentication server name related to the specified domain.
   */
  private String getAuthenticationServerName(Connection con, String domainId)
      throws AuthenticationException {
    JdbcSqlQuery query = JdbcSqlQuery.select(DOMAIN_AUTHENTICATION_SERVER_COLUMN_NAME)
        .from(DOMAIN_TABLE_NAME)
        .where(DOMAIN_ID_COLUMN_NAME + " = ?", Integer.parseInt(domainId));
    try {
      String domainServerName = query.executeUniqueWith(con, row -> {
          String serverName = row.getString(DOMAIN_AUTHENTICATION_SERVER_COLUMN_NAME);
          if (!StringUtil.isDefined(serverName)) {
            throw new SQLException("No server found for domain of id " + domainId);
          } else {
            return serverName;
          }
      });
      if (StringUtil.isNotDefined(domainServerName)) {
        throw new SQLException("No such domain with id " + domainId);
      }
      return domainServerName;
    } catch (SQLException e) {
      throw new AuthenticationException(e.getMessage(), e);
    }
  }

  /**
   * Builds a random authentication key.
   * @param login a user login
   * @return the generated authentication key.
   */
  private static String generateTokenFor(String login) {
    // Random key generation
    long nStart = login.hashCode() * new Date().getTime() * (autoInc++);
    Random rand = new Random(nStart);
    int key = rand.nextInt();

    return String.valueOf(key);
  }

  private void storeAuthenticationKey(String login, String domainId, String sKey) {
    Transaction.performInOne(() -> {
      JdbcSqlQuery query = JdbcSqlQuery.insertInto(KEY_STORE_TABLE_NAME)
          .withInsertParam(KEY_STORE_KEY_COLUMN_NAME, Integer.parseInt(sKey))
          .withInsertParam(KEY_STORE_LOGIN_COLUMN_NAME, login)
          .withInsertParam(KEY_STORE_DOMAIN_ID_COLUMN_NAME, Integer.parseInt(domainId));

      try (Connection connection = openConnection()) {
        query.executeWith(connection);
      } catch (SQLException ex) {
        SilverLogger.getLogger(this).error(
            SilverpeasExceptionMessages.failureOnAdding("authentication key for login", login),
            ex);
      }
      return null;
    });
  }

  /**
   * Resets the specified password of the user behind the specified authentication credential with
   * the specified one. The reset operation can only be performed if the password change is allowed
   * by the domain to which the user belongs. It doesn't require the user to be authenticated but,
   * as consequence, requires to be run in a privileged mode (only an administrator or the system
   * itself can do this operation). The privileged mode isn't checked by this method, hence it is
   * the responsibility of the caller to ensure this. The specified credential won't be updated by
   * the password reset.
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
      throw new AuthenticationBadCredentialException(
          "The login, the password or the domain isn't set!");
    }

    // Verify that the user can log in
    AuthenticationUserVerifierFactory.getUserCanLoginVerifier(credential).verify();

    Connection connection = null;
    try {
      connection = openConnection();

      // Build a AuthenticationServer instance
      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      // Authentication test
      authenticationServer.resetPassword(login, credential.loginIgnoreCase(), newPassword);
    } catch (AuthenticationException ex) {
      SilverLogger.getLogger(this).warn(ex);
      throw ex;
    } finally {
      closeConnection(connection);
    }

    // Treatments on password change
    onPasswordAndEmailChanged(credential, null);
  }

  private void onPasswordAndEmailChanged(AuthenticationCredential credential, final String email)
      throws AuthenticationException {
    UserDetail user = UserDetail.getById(
        adminController.getUserIdByLoginAndDomain(credential.getLogin(), credential.getDomainId()));

    // Notify that the user has changed his password.
    AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(user)
        .notifyPasswordChange();

    // Updating some data
    UserFull userFull = adminController.getUserFull(user.getId());

    // Reset the counter of number of successful connections for the given user.
    userFull.setNbSuccessfulLoginAttempts(0);

    // Register the date of credential change
    userFull.setLastLoginCredentialUpdateDate(new Date());

    // Set email
    if (StringUtil.isDefined(email)) {
      userFull.seteMail(email);
    }

    // Persisting user data changes.
    try {
      adminController.updateUserFull(userFull);
    } catch (AdminException e) {
      throw new AuthenticationException("Cannot update user full information", e);
    }
  }

  /**
   * Is the change of a user password is allowed by specified user domain?
   * @param domainId the unique identifier of the user domain.
   * @return true if the password of the users in the specified domain can be changed, false
   * otherwise.
   */
  boolean isPasswordChangeAllowed(String domainId) {
    Connection connection = null;
    try {
      // Open connection
      connection = openConnection();

      // Build a AuthenticationServer instance
      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);

      return authenticationServer.isPasswordChangeAllowed();
    } catch (AuthenticationException ex) {
      SilverLogger.getLogger(this).warn(ex);
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

  static {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.authentication.domains");

    DATA_SOURCE_JNDI_NAME = settings.getString("SQLDomainDataSourceJNDIName");

    DOMAIN_TABLE_NAME = settings.getString("SQLDomainTableName");
    DOMAIN_ID_COLUMN_NAME = settings.getString("SQLDomainIdColumnName");
    DOMAIN_AUTHENTICATION_SERVER_COLUMN_NAME =
        settings.getString("SQLDomainAuthenticationServerColumnName");

    KEY_STORE_TABLE_NAME = settings.getString("SQLKeyStoreTableName");
    KEY_STORE_KEY_COLUMN_NAME = settings.getString("SQLKeyStoreKeyColumnName");
    KEY_STORE_LOGIN_COLUMN_NAME = settings.getString("SQLKeyStoreLoginColumnName");
    KEY_STORE_DOMAIN_ID_COLUMN_NAME = settings.getString("SQLKeyStoreDomainIdColumnName");

    USER_TABLE_NAME = settings.getString("SQLUserTableName");
    USER_ID_COLUMN_NAME = settings.getString("SQLUserIdColumnName");
    USER_LOGIN_COLUMN_NAME = settings.getString("SQLUserLoginColumnName");
    USER_DOMAIN_COLUMN_NAME = settings.getString("SQLUserDomainColumnName");
  }
}
