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

package org.silverpeas.core.jcr.security;

import org.apache.jackrabbit.oak.spi.security.authentication.AbstractLoginModule;
import org.silverpeas.core.admin.user.model.User;

import javax.annotation.Nonnull;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common classes for all {@link javax.security.auth.spi.LoginModule}s that takes in charge the
 * authentication of a user accessing the Silverpeas JCR. This abstract class provides an
 * implementation of the {@link LoginModule#login()}, {@link LoginModule#commit()} and
 * {@link LoginModule#logout()} methods and for doing it expects the concrete classes to implement
 * two methods: {@link SilverpeasJCRLoginModule#authenticateUser(Credentials)} to authenticate a
 * user accessing the repository by his credentials, and
 * {@link SilverpeasJCRLoginModule#getSupportedCredentials()} to indicate the type of credentials
 * the {@link LoginModule} supports. Indeed, each {@link SilverpeasJCRLoginModule} class is defined
 * for a given type of credentials which can require a specific way of authentication process. For
 * instance, one {@link LoginModule} to authenticate a user by his tuple login/domain/password and
 * another one to authenticate a user by his own API token. Once a user is authenticated, his
 * profile (as a {@link User} instance) is then set within a {@link SilverpeasUserPrincipal} object.
 * Because the content of the JCR is, in Silverpeas, a sensitive data, anonymous authentication
 * must be by default rejected. Only authentication of the system (or system user) can be accepted
 * and as such it should be automatically represented by the virtual Silverpeas system user in the
 * {@link SilverpeasUserPrincipal}.
 */
public abstract class SilverpeasJCRLoginModule extends AbstractLoginModule {

  private Credentials userId = null;
  private Principal principal = null;

  private boolean success = false;

  private boolean initialized = false;

  @Override
  public void initialize(final Subject subject, final CallbackHandler callbackHandler,
      final Map<String, ?> sharedState, final Map<String, ?> options) {
    super.initialize(subject, callbackHandler, sharedState, options);
    this.initialized = true;
  }

  @Override
  public boolean login() throws LoginException {
    try {
      // Get credentials using a JAAS callback
      Credentials credentials = getCredentials();
      success = false;
      if (isCredentialsSupported(credentials)) {
        // Use the credentials to authenticate the subject and then to produce the principal
        // required to access the JCR
        User user = authenticateUser(credentials);
        AccessContext context = getAccessContext(credentials);
        principal = new SilverpeasUserPrincipal(user, context);
        success = true;
      }

      return success;

    } catch (Exception ex) {
      throw new LoginException(ex.getMessage());
    }
  }

  @Override
  public boolean commit() {
    if (!success) {
      clearState();
      return false;
    }
    if (!subject.isReadOnly()) {
      subject.getPrincipals().add(principal);
      // Fallback in the JCR implementations to figure out the user identifier in session. This for
      // the case the Silverpeas wrapper of the actual implementation doesn't set such information.
      // The identifier of the user for whom a session has been opened is required by the
      // reentrant JCR session mechanism as implemented in org.silverpeas.core.jcr.JCRSession.
      // See javax.jcr.Session#getUserID method
      userId = new SimpleCredentials(principal.getName(), new char[0]);
      subject.getPublicCredentials().add(userId);
    }
    return true;
  }

  @Override
  public boolean logout() throws LoginException {
    Set<Object> userCredentials = getAllCredentials();
    Set<Principal> userPrincipals = getAllPrincipals();
    return logout(userCredentials.isEmpty() ? null : userCredentials,
        userPrincipals.isEmpty() ? null : userPrincipals);
  }

  /**
   * Is this module initialized?
   * @return true if the module was initialized before any use. False otherwise.
   */
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  protected void clearState() {
    super.clearState();
    this.userId = null;
    this.principal = null;
  }

  protected Set<Object> getAllCredentials() {
    return Stream.of(userId).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  protected Set<Principal> getAllPrincipals() {
    return Stream.of(principal).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  /**
   * Authenticates the user behind the specified credentials.
   * @param credentials the credentials of a user in Silverpeas.
   * @return the user identified by the given credentials if and only if the authentication
   * succeeds.
   * @throws LoginException if the authentication of the user fails.
   */
  protected abstract User authenticateUser(final Credentials credentials) throws LoginException;

  /**
   * Gets the peculiar context under which the user behind the specified credentials accesses the
   * JCR. The context has to be fetched through some attributes in the specified credentials. By
   * default, this method returns {@link AccessContext#EMPTY} meaning no peculiar access context.
   * @param credentials the credentials of the user in which some attributes have been set in order
   * to define the current access context of the user.
   * @return the current access context of the user. By default, no peculiar context.
   */
  @Nonnull
  protected AccessContext getAccessContext(final Credentials credentials) {
    return AccessContext.EMPTY;
  }

  private boolean isCredentialsSupported(final Credentials credentials) {
    return getSupportedCredentials().stream().anyMatch(c -> c.isInstance(credentials));
  }
}
