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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Authentication of a user in Silverpeas. The process of authentication is itself delegated to
 * backends that wrap transparently the communication with external user services. Those services
 * managing the authentication can be simply a database, an LDAP server or an SSO service. At each
 * backend corresponds a user Silverpeas domain and hence this is by the domain identifier the
 * service to delegate the authentication is figured out by this authentication.
 * @author mmoquillon
 */
public interface Authentication {

  /**
   * Gets an {@link Authentication} object. Only one implementation should be provided.
   * @return an instance of the implementation of this interface.
   */
  static Authentication get() {
    return ServiceProvider.getSingleton(Authentication.class);
  }

  /**
   * Is there several authentication domains with their own authentication process defined currently
   * in Silverpeas?
   * @return true if there is more than one authentication domain currently defined in Silverpeas.
   * False otherwise.
   */
  default boolean isThereMultipleDomainsDefined() {
    return getAllAuthDomains().size() > 1;
  }

  /**
   * Gets all the domains in Silverpeas that are able to take in charge the authentication of its
   * own users. Other domains (those for which no authentication protocol is defined) aren't
   * returned.
   * @return a list of all authentication domains that support currently the authentication of its
   * own users.
   */
  @Nonnull
  List<AuthDomain> getAllAuthDomains();

  /**
   * Authenticates a user with the specified authentication credential.
   * <p>
   * If the authentication succeeds, the security-related capabilities, mapped to the user's
   * credential, are set from information sent back by the authentication server related to the
   * domain to which the user belongs.
   * </p>
   * @param userCredential the credential of the user to use to authenticate him.
   * @return the response of the authentication with its status (success, failure or rejected). In
   * the case of a successful authentication, the response carries the authentication token from
   * which the user can be then identified later.
   */
  AuthenticationResponse authenticate(final AuthenticationCredential userCredential);

  /**
   * Gets an authentication token for a given user from its specified login and from the domain to
   * which he belongs. This method doesn't perform any authentication, but it only set a new
   * authentication token for the given user. This method can be used, for example, to let a user
   * who has forgotten its password of setting a new one without having to be authenticated.
   * <p>
   * To use with caution as this can be security flaw to use this method to bypass any
   * authentication process. It is only for administrative tasks or inner technical tasks requiring
   * an authentication token for them to be completed.
   * </p>
   * @param credential the credential of the user required to identify his account in Silverpeas.
   * At least his login and his domain identifier has to be set.
   * @return an authentication key.
   */
  String getAuthToken(final AuthenticationCredential credential) throws AuthenticationException;
}
