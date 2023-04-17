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

import org.silverpeas.core.security.authentication.exception.*;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * The authentication server is a proxy in Silverpeas side of the external authentication service
 * related to a given user domain. This service is identified by a unique name. The authentication
 * is delegated to an implementation of the Authentication abstract class that knows how to perform
 * the authentication with the remote service.
 * <p>
 * An external authentication service can be backed by one or more remote authentication servers.
 * So, the authentication with each server is then performed by a different Authentication instance
 * of the same type; each Authentication instance is mapped with a given server behind the external
 * authentication service.
 * <p>
 * The correct implementation of the Authentication abstract class is loaded from the properties
 * mapped to an authentication service name. Each service name identifies uniquely an external
 * security service (SQL database, LDAP, NTLM, ...)
 *
 */
public class AuthenticationServer {

  private String fallbackMode;
  private List<AuthenticationProtocol> authProtocols;
  private boolean passwordChangeAllowed;

  /**
   * Gets the authentication server identified by the specified name.
   *
   * @param serverName the authentication server name.
   * @return the authentication server with the specified name.
   */
  public static AuthenticationServer getAuthenticationServer(String serverName) {
    return new AuthenticationServer(serverName);
  }

  /**
   * Creates an authentication server proxying the external one defined by the specified name. All
   * the settings to communicate with the remote service are then loaded from the properties mapped
   * with the specified server name.
   *
   * @param authServerName an authentication server name.
   */
  private AuthenticationServer(String authServerName) {
    try {
      SettingBundle serverSettings =
          ResourceLocator.getSettingBundle("org.silverpeas.authentication." + authServerName);
      fallbackMode = serverSettings.getString("fallbackType");
      passwordChangeAllowed = serverSettings.getBoolean("allowPasswordChange", false);
      int nbServers = Integer.parseInt(serverSettings.getString("autServersCount"));
      authProtocols = new ArrayList<>(nbServers);
      for (int i = 0; i < nbServers; i++) {
        String serverName = "autServer" + i;
        if (serverSettings.getBoolean(serverName + ".enabled", true)) {
          addAuthenticationService(serverName, authServerName, serverSettings);
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Server=" + authServerName, e);
    }
  }

  @SuppressWarnings("unchecked")
  private void addAuthenticationService(final String serverName, final String authServerName,
      final SettingBundle serverSettings) {
    try {
      Constructor<AuthenticationProtocol> constructor = ((Class<AuthenticationProtocol>) Class.forName(
          serverSettings.getString(serverName + ".type"))).getConstructor();
      AuthenticationProtocol authProtocol = constructor.newInstance();
      authProtocol.init(serverName, serverSettings);
      authProtocols.add(authProtocol);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(authServerName + " / " + serverName, ex);
    }
  }

  /**
   * Is this server has one or more authentication protocols defined? An authentication server
   * cannot delegate the authentication process to an external service without knowing how to
   * communicate with it. And the communication is performed by a protocol represented here by
   * a {@link AuthenticationProtocol} object.
   * @return true if this server knows one or more protocols to communicate with an external
   * authentication service. False otherwise.
   */
  public boolean hasProtocols() {
    return !authProtocols.isEmpty();
  }

  /**
   * Authenticates the user with the specified authentication credential.
   *
   * @param credential the authentication credential to use to authenticate the user.
   * @throws org.silverpeas.core.security.authentication.exception.AuthenticationException
   * if the authentication
   * fails.
   */
  public void authenticate(final AuthenticationCredential credential) throws
      AuthenticationException {
    doSecurityOperation(new SecurityOperation(SecurityOperation.AUTHENTICATION, credential) {
      @Override
      public void performWith(AuthenticationProtocol authProtocol) throws AuthenticationException {
        authProtocol.authenticate(credential);
      }
    });
  }

  /**
   * Changes the password associated with the login in the specified credential by the one passed in
   * parameter. The credential is used to validate the authentication of the user. The password
   * modification capability is available only with some authentication services, so please use the
   * method
   * <code>isPasswordChangeAllowed()</code> to check if this operation is supported. The specified
   * credential won't be updated by the password change.
   *
   * @param credential the authentication credential of the user for which the password has to be
   * changed.
   * @param newPassword the new password that will replace the one in the specified credential.
   * @throws AuthenticationException if an error occurs while changing the password.
   */
  public void changePassword(final AuthenticationCredential credential,
      final String newPassword) throws AuthenticationException {
    if (!passwordChangeAllowed) {
      throw new AuthenticationPwdChangeNotAvailException("The password modification isn't available");
    }

    doSecurityOperation(new SecurityOperation(SecurityOperation.P_CHANGE, credential) {
      @Override
      public void performWith(AuthenticationProtocol authProtocol) throws AuthenticationException {
        authProtocol.changePassword(credential, newPassword);
      }
    });
  }

  /**
   * Is the password change is allowed by the remote authentication service represented by this
   * instance?
   *
   * @return true if the password can be changed, false otherwise.
   */
  public boolean isPasswordChangeAllowed() {
    return passwordChangeAllowed;
  }

  /**
   * Resets the password associated with the specified login by replacing it with the specified one.
   * This password reset capability is available only whether the authentication service supports
   * the password change. Please use the method
   * <code>isPasswordChangeAllowed()</code> to check this. This operation doesn't require the user
   * to be authenticated, so the reset must be under the control of the system for security reasons.
   * @param login the login of the user for which the password has to be reset.
   * @param loginIgnoreCase true to ignore case when comparing the login.
   * @param newPassword the new password of the user.
   * @throws AuthenticationException if an error occurs while resetting the password with the new
   * one.
   */
  public void resetPassword(final String login, final boolean loginIgnoreCase,
      final String newPassword) throws AuthenticationException {
    if (!passwordChangeAllowed) {
      throw new AuthenticationPwdChangeNotAvailException("The password reset isn't available");
    }

    doSecurityOperation(new SecurityOperation(SecurityOperation.P_RESET,
        AuthenticationCredential.newWithAsLogin(login)) {
      @Override
      public void performWith(AuthenticationProtocol authentication) throws AuthenticationException {
        authentication.resetPassword(login, loginIgnoreCase, newPassword);
      }
    });
  }

  private void doSecurityOperation(SecurityOperation op) throws AuthenticationException {
    boolean serverNotFound = true;
    AuthenticationException lastException = null;
    for (AuthenticationProtocol authProtocol : authProtocols) {
      if (authProtocol.isEnabled()) {
        try {
          op.performWith(authProtocol);
          serverNotFound = false;
        } catch (AuthenticationException ex) {
          AuthenticationExceptionProcessor processor =
              new AuthenticationExceptionProcessor(op);
          serverNotFound = processor.processAuthenticationException(ex);
          lastException = ex;
        }
      }
      if (!serverNotFound) {
        break;
      }
    }

    if (serverNotFound) {
      if (lastException == null) {
        throw new AuthenticationException("No server definition found");
      } else {
        throw lastException;
      }
    }
  }

  private abstract static class SecurityOperation {

    public static final String AUTHENTICATION = "authenticate";
    public static final String P_CHANGE = "changePassword";
    public static final String P_RESET = "resetPassword";
    private final String name;
    private final AuthenticationCredential credential;

    public SecurityOperation(String operationName, AuthenticationCredential credential) {
      this.credential = credential;
      this.name = operationName;
    }

    @SuppressWarnings("unused")
    public String getName() {
      return name;
    }

    public AuthenticationCredential getAuthenticationCredential() {
      return credential;
    }

    public abstract void performWith(AuthenticationProtocol authProtocol) throws AuthenticationException;
  }

  private class AuthenticationExceptionProcessor implements AuthenticationExceptionVisitor {

    private final AuthenticationCredential credential;
    private boolean continueAuthentication = true;

    public AuthenticationExceptionProcessor(final SecurityOperation operation) {
      this.credential = operation.getAuthenticationCredential();
    }

    public boolean processAuthenticationException(AuthenticationException ex) throws
        AuthenticationException {
      ex.accept(this);
      return continueAuthentication;
    }

    @Override
    public void visit(AuthenticationBadCredentialException ex) throws AuthenticationException {
      if (fallbackMode.equals("none") || fallbackMode.equals("ifNotRejected")) {
        throw ex;
      }
      continueAuthentication = true;
    }

    @Override
    public void visit(AuthenticationHostException ex) throws AuthenticationException {
      if (fallbackMode.equals("none")) {
        throw ex;
      }
      continueAuthentication = true;
    }

    @Override
    public void visit(AuthenticationException ex) throws AuthenticationException {
      if (fallbackMode.equals("none")) {
        throw ex;
      }
      continueAuthentication = true;
    }

    @Override
    public void visit(AuthenticationPwdNotAvailException ex) {
      continueAuthentication = true;
    }

    /**
     * In fact, the authentication succeeded but an exception has been thrown to alert the password
     * is about to expire.
     */
    @Override
    public void visit(AuthenticationPasswordAboutToExpireException ex) {
      credential.getCapabilities().put(AuthenticationProtocol.PASSWORD_IS_ABOUT_TO_EXPIRE, Boolean.TRUE);
      continueAuthentication = false;
    }

    @Override
    public void visit(AuthenticationPwdChangeNotAvailException ex) {
      continueAuthentication = true;
    }
  }
}
