/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.security.authentication;

import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception
    .AuthenticationPwdChangeNotAvailException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * A set of security-related operations about a user authentication.
 *
 * The authentication is performed by a server of a remote authentication service and an instance
 * of this class manages for Silverpeas the negotiation with the service to perform the asked
 * security-related operation.
 *
 * Each concrete implementation of this abstract class must implement the communication protocol with
 * the a server of the remote service; it is dedicated to a given authentication service.
 *
 * @author tleroi
 * @author mmoquillon
 */
public abstract class Authentication {

  protected static final String module = "authentication";

  protected boolean enabled = true;

  public static final String PASSWORD_IS_ABOUT_TO_EXPIRE = "Svp_Pwd_About_To_Expire";
  public static final String PASSWORD_CHANGE_ALLOWED = "Svp_Password_Change_Allowed";
  private String authServerName;

  /**
   * Is this authentication enabled?
   * When an authentication is enabled, it can be performed against an authentication service.
   * @return true if it is enabled, false otherwise.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Gets the name of the authentication server with which this authentication communicates.
   * @return the name of the server behind the remote authentication service.
   */
  public String getServerName() {
    return this.authServerName;
  }

  /**
   * Initializes this authentication with the specified settings to communicate with a server of
   * an authentication service.
   * @param authenticationServerName the name of a remote service behind a given authentication service.
   * @param settings the settings of the server communication.
   */
  public void init(String authenticationServerName, SettingBundle settings) {
    this.authServerName = authenticationServerName;
    this.enabled =  settings.getBoolean(this.authServerName + ".enabled", true);
    loadProperties(settings);
  }

  /**
   * Authenticates the user with its specified credential (containing a password in clear).
   * If the user cannot be authenticated, an exception is thrown, whatever the reason.
   * If the authentication could not be performed because the credentials are invalid
   * (e.g. wrong password), the AuthenticationException code should be set to
   * EXCEPTION_BAD_CREDENTIALS.
   * @param credential the credential to use to authenticate the user.
   * @throws org.silverpeas.core.security.authentication.exception.AuthenticationException
   * if an error occurs while authenticating the user.
   */
  public void authenticate(final AuthenticationCredential credential) throws
      AuthenticationException {
    doSecurityOperation(new SecurityOperation(SecurityOperation.AUTHENTICATION) {
      @Override
      public <T> void perform(AuthenticationConnection<T> connection) throws AuthenticationException {
        doAuthentication(connection, credential);
      }
    });
  }

  /**
   * Changes the password of the user, authenticated with the specified credential, with the
   * specified new one. The user must be authenticated for doing a such operation.
   * The specified credential won't be updated by the password change.
   * If the user cannot be authenticated, an exception is thrown, whatever the reason.
   * If the authentication could not be performed because the credentials are invalid
   * (e.g. wrong password), the AuthenticationException code should be set to
   * EXCEPTION_BAD_CREDENTIALS.
   * @param credential the user credential used in an authentication with Silverpeas.
   * @param newPassword user new password
   * @return true if succeeded
   * @throws AuthenticationException if an error occurs while changing the user password.
   */
  public void changePassword(final AuthenticationCredential credential,
                             final String newPassword) throws AuthenticationException {
    doSecurityOperation(new SecurityOperation(SecurityOperation.PASSWORD_CHANGE) {
      @Override
      public <T> void perform(AuthenticationConnection<T> connection) throws AuthenticationException {
        doChangePassword(connection, credential, newPassword);
      }
    });
  }

  /**
   * Resets the password associated with the specified login of a user  with the new specified one.
   * Contrary to the password change, this operation doesn't require the user to be authenticated; it
   * isn't a password modification but a reset of it generally under the control of the system.
   * If the login of the user doesn't exist or if the reset cannot be done an exception is thrown.
   * @param login the user login
   * @param newPassword the new password
   * @throws AuthenticationException if an error occurs while resetting the user password.
   */
  public void resetPassword(final String login, final String newPassword) throws AuthenticationException {
      doSecurityOperation(new SecurityOperation(SecurityOperation.PASSWORD_RESET) {
        @Override
        public <T> void perform(AuthenticationConnection<T> connection) throws AuthenticationException {
          doResetPassword(connection, login, newPassword);
        }
      });
    }

  /**
   * Loads the specified properties to set the communication information with the authentication
   * service.
   * @param settings the communication settings.
   */
  protected abstract void loadProperties(SettingBundle settings);

  /**
   * Opens a connection with a server of the remote authentication service.
   * The policy of the connection management is left to the concrete Authentication implementation.
   * @param <T> the type of the authentication server's connector.
   * @return a connection with a remote authentication server.
   * @throws AuthenticationException if no connection can be established with a server of the remote
   * authentication service.
   */
  abstract protected <T> AuthenticationConnection<T> openConnection() throws AuthenticationException;

  /**
   * Closes the connection that was previously opened with the server of the remote authentication
   * service.
   * The policy of the connection management is left to the concrete Authentication implementation.
   * @param connection the connection with a remote authentication server.
   * @param <T> the type of the authentication server's connector.
   * @throws AuthenticationException if no connection was previously opened or if the connection
   * cannot be closed for any reason.
   */
  abstract protected <T> void closeConnection(AuthenticationConnection<T> connection)
      throws AuthenticationException;

  /**
   * Does the authentication by using the specified connection with the remote server and with
   * with the specified user credential.
   * @param connection the connection with a remote authentication server.
   * @param credential the credential to use to authenticate the user.
   * @param <T> the type of the authentication server's connector.
   * @throws AuthenticationException if an error occurs while authenticating the user.
   */
  abstract protected <T> void doAuthentication(AuthenticationConnection<T> connection,
                                               AuthenticationCredential credential) throws AuthenticationException;

  /**
   * Does the password change by using the specified connection with the remote server and with
   * with the specified user credential and new password.
   * By default, this operation is considered as not supported by the remote authentication service
   * and throws then an UnsupportedOperationException exception. If the authentication service
   * supports this operation, the concrete Authentication implementation has to implement this
   * method.
   * @param connection the connection with a remote authentication server.
   * @param credential the credential to use to authenticate the user.
   * @param newPassword the new password that will replace the one in the user credential.
   * @param <T> the type of the authentication server's connector.
   * @throws AuthenticationException if an error occurs while changing the user password.
   */
  protected <T> void doChangePassword(AuthenticationConnection<T> connection,
                                      AuthenticationCredential credential,
                                      String newPassword) throws AuthenticationException {
    throw new AuthenticationPwdChangeNotAvailException(
        "AuthenticationServer.changePassword", SilverpeasException.ERROR,
        "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
  }

  /**
   * Does the password reset by using the specified connection with the remote server the user login
   * for which the password has to be reset and a new password.
   * By default, this operation is considered as not supported by the remote authentication service
   * and throws then an UnsupportedOperationException exception. If the authentication service
   * supports this operation, the concrete Authentication implementation has to implement this
   * method.
   * @param connection the connection with a remote authentication server.
   * @param login the login of the user for which the password has to be reset.
   * @param newPassword the new password with which the user password will be reset.
   * @param <T> the type of the authentication server's connector.
   * @throws AuthenticationException if an error occurs while resetting the user password.
   */
  protected <T> void doResetPassword(AuthenticationConnection<T> connection, String login, String newPassword)
      throws AuthenticationException {
    throw new AuthenticationPwdChangeNotAvailException(
        "AuthenticationServer.doResetPassword", SilverpeasException.ERROR,
        "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
  }

  private void doSecurityOperation(SecurityOperation op) throws AuthenticationException {
    AuthenticationConnection connection = null;
    try {
      connection = openConnection();
      op.perform(connection);
      closeConnection(connection);
    } finally {
      try {
        if (connection != null) {
          closeConnection(connection);
        }
      } catch (AuthenticationException closeEx) {
        // The exception that could occur in the emergency stop is not interesting.
        SilverTrace.error(module, "Authentication." + op.getName(),
            "root.EX_EMERGENCY_CONNECTION_CLOSE_FAILED", "", closeEx);
      }
    }
  }

  private abstract class SecurityOperation {

    public static final String AUTHENTICATION = "authenticate";
    public static final String PASSWORD_CHANGE = "changePassword";
    public static final String PASSWORD_RESET = "resetPassword";

    private String name;

    public SecurityOperation(String operationName) {
      this.name = operationName;
    }

    public String getName() {
      return name;
    }

    public abstract <T> void perform(AuthenticationConnection<T> connection) throws AuthenticationException;
  }
}
