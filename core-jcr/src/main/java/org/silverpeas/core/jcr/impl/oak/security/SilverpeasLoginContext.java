/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.impl.oak.security;

import org.apache.jackrabbit.oak.api.AuthInfo;
import org.apache.jackrabbit.oak.spi.security.authentication.AuthInfoImpl;
import org.apache.jackrabbit.oak.spi.security.authentication.LoginContext;
import org.silverpeas.core.jcr.security.LoginModuleRegistry;
import org.silverpeas.core.jcr.security.SilverpeasJCRLoginModule;
import org.silverpeas.core.jcr.security.SilverpeasUserPrincipal;

import javax.annotation.Nonnull;
import javax.jcr.Credentials;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Context of a login/logout to the JCR repository by a user within the scope of Silverpeas out of
 * any JAAS process as the security system of Silverpeas isn't built upon this framework.
 * Nevertheless, the JAAS logic in login/logout is respected (but not how it is performed). Such a
 * context is created each time a login to the JCR is invoked, and it is reused for the logout after
 * what it is disposed.
 * <p>
 * At login or logout, it checks the type of credentials that is passed in order to invoke the
 * {@link javax.security.auth.spi.LoginModule} instances that support such a credentials. It chains
 * then their invocation to perform the actual operation of login/logout until a {@link LoginModule}
 * instance responds successfully. When an authentication succeeds, the context expects through the
 * {@link LoginModule#commit()} method the subject to be enriched with the
 * {@link java.security.Principal} that identifies the authenticated user. Otherwise a
 * {@link LoginException} is thrown.
 * </p>
 * <p>
 * The {@link LoginModule} instances to consider for a given credentials type are provided by the
 * {@link LoginModuleRegistry} object. So, any {@link LoginModule} defined for JCR authentication
 * have to register themselves to this registry by indicating the type of credentials they support.
 * </p>
 * @author mmoquillon
 */
public class SilverpeasLoginContext implements LoginContext {

  private final Subject subject;
  private final SilverpeasCallbackHandler callbackHandler;
  private Set<SilverpeasJCRLoginModule> modules;

  SilverpeasLoginContext(final Subject subject, final SilverpeasCallbackHandler callbackHandler) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
  }

  @Override
  public Subject getSubject() {
    return subject;
  }

  @Override
  public void login() throws LoginException {
    applyOnLoginModule(l -> {
      boolean status = l.login() && l.commit();
      if (status) {
        // we add AuthInfo for Oak SessionImpl to figuring out the userID mapped with the current
        // opened session (this for avoiding the fallback mechanism). This is required for reentrant
        // session mechanism.
        Set<SilverpeasUserPrincipal> principals =
            getSubject().getPrincipals(SilverpeasUserPrincipal.class);
        if (!principals.isEmpty()) {
          SilverpeasUserPrincipal principal = principals.iterator().next();
          AuthInfo authInfo = new AuthInfoImpl(principal.getUser().getId(), null, principals);
          getSubject().getPublicCredentials().add(authInfo);
        }
      }
      return status;
    });
  }

  @Override
  public void logout() throws LoginException {
    applyOnLoginModule(LoginModule::logout);
  }

  private void applyOnLoginModule(AuthOp operation) throws LoginException {
    var credentials = getCredentials();
    var iterator = getLoginModules(credentials.getClass()).iterator();
    Map<String, ?> sharedState = new HashMap<>();
    Map<String, ?> options = new HashMap<>();
    boolean succeeded = false;
    while (iterator.hasNext() && !succeeded) {
      var module = iterator.next();
      if (!module.isInitialized()) {
        module.initialize(getSubject(), callbackHandler, sharedState, options);
      }
      succeeded = operation.check(module);
    }
    if (!succeeded) {
      throw new LoginException(
          "No authentication mechanism matches the credentials " + credentials);
    }
  }

  /**
   * Gets the credentials of the user from the callback handler.
   * @return the credentials of the user being authenticated.
   * @throws LoginException if no credentials were provided for the authentication.
   */
  @Nonnull
  private Credentials getCredentials() throws LoginException {
    Credentials credentials = callbackHandler.getCredentials();
    if (credentials == null) {
      throw new LoginException("No credentials!");
    }
    return credentials;
  }

  @Nonnull
  private LoginModuleRegistry getLoginModuleRegistry() {
    return LoginModuleRegistry.getInstance();
  }

  /**
   * Gets the login modules that were registered to authenticate a user in Silverpeas.
   * @param credentialsType the type of credentials the login module to return has to support.
   * @return a set of login module able to process the specified credentials.
   * @throws LoginException if no login module are found to process the given type of credentials.
   */
  @Nonnull
  private Set<SilverpeasJCRLoginModule> getLoginModules(
      @Nonnull Class<? extends Credentials> credentialsType) throws LoginException {
    if (modules == null) {
      modules = getLoginModuleRegistry().getLoginModule(credentialsType);
      if (modules.isEmpty()) {
        modules = null;
        throw new LoginException("Unsupported credentials: " + credentialsType.getName());
      }
    }
    return modules;
  }

  @FunctionalInterface
  private interface AuthOp {

    boolean check(final LoginModule loginModule) throws LoginException;
  }
}
