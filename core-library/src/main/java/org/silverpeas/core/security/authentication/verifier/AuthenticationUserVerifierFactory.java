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
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;

import static org.silverpeas.core.security.authentication.verifier.AbstractAuthenticationVerifier.getUserByCredential;
import static org.silverpeas.core.security.authentication.verifier.AbstractAuthenticationVerifier.getUserById;

/**
 * Factory that provides external verifiers of user authentication that are invoked around a
 * user authentication:
 * <ul>
 * <li>some to verify whether the user can log on in relation to its account state,</li>
 * <li>another one to verify whether the user can try to log on one more time after a login error,</li>
 * </ul>
 * @author Yohann Chastagnier
 * Date: 06/02/13
 */
public class AuthenticationUserVerifierFactory {

  private AuthenticationUserVerifierFactory() {
    throw new IllegalStateException("Factory class");
  }

  /**
   * Removes from request cache the given user.
   * @param user the user behind a login attempt.
   */
  public static void removeFromRequestCache(User user) {
    AbstractAuthenticationVerifier.removeFromRequestCache(user);
  }

  /**
   * Gets the verifier of the account state of the specified user.
   * @param user the user behind a login attempt.
   * @return the verifier that checks if the user can log on in relation to its account state
   */
  public static UserCanLoginVerifier getUserCanLoginVerifier(User user) {
    return new UserCanLoginVerifier(user);
  }

  /**
   * Gets the verifier of the account state if the specified user.
   * @param userId the unique identifier of the user behind a login attempt.
   * @return the verifier that checks if the user can log on in relation to its account state
   */
  public static UserCanLoginVerifier getUserCanLoginVerifier(String userId) {
    return getUserCanLoginVerifier(getUserById(userId));
  }

  /**
   * Gets the verifier of the account state of user referred by the specified credentials.
   * @param credential the credential of the user behind the login.
   * @return the verifier that checks if the user can log on in relation to its account state
   */
  public static UserCanLoginVerifier getUserCanLoginVerifier(AuthenticationCredential credential) {
    return getUserCanLoginVerifier(getUserByCredential(credential));
  }

  /**
   * Gets the verifier of login attempts with the specified credentials.
   * @param credential the credential of the user behind the login.
   * @return the verifier that checks if the user can try to log on one more time after a login
   * error
   */
  public static UserCanTryAgainToLoginVerifier getUserCanTryAgainToLoginVerifier(
      AuthenticationCredential credential) {
    User user = getUserByCredential(credential);
    if (user == null) {
      // Dummy user (but contains user credentials)
      UserDetail dummy = new UserDetail();
      dummy.setLogin(credential.getLogin());
      dummy.setDomainId(credential.getDomainId());
      user = dummy;
    }
    return getUserCanTryAgainToLoginVerifier(user);
  }

  /**
   * Gets the verifier of login attempts by the specified user.
   * @param user the user behind a login attempt.
   * @return the verifier that checks if the user can try to log on one more time after a login
   * error
   */
  public static synchronized UserCanTryAgainToLoginVerifier getUserCanTryAgainToLoginVerifier(
      User user) {
    return UserCanTryAgainToLoginVerifier.get(user);
  }

  /**
   * Gets the verifier about the state of the password of the specified user.
   * @param user the user behind a login attempt.
   * @return the verifier that checks if the user must change his password or if the user will soon
   * have to change his password
   */
  public static UserMustChangePasswordVerifier getUserMustChangePasswordVerifier(User user) {
    return new UserMustChangePasswordVerifier(user);
  }

  /**
   * Gets the verifier about the state of the password passed in the credentials.
   * @param credential the credential of the user behind the login.
   * @return the verifier that checks if the user must change his password or if the user will soon
   * have to change his password
   */
  public static UserMustChangePasswordVerifier getUserMustChangePasswordVerifier(
      AuthenticationCredential credential) {
    return getUserMustChangePasswordVerifier(getUserByCredential(credential));
  }

  /**
   * Gets the terms of service verifier for the user referred by the specified credentials.
   * @param credential the credential of the user behind the login.
   * @return the verifier that checks if the user must accept terms of service
   */
  public static UserMustAcceptTermsOfServiceVerifier getUserMustAcceptTermsOfServiceVerifier(
      AuthenticationCredential credential) {
    return new UserMustAcceptTermsOfServiceVerifier(getUserByCredential(credential));
  }

  /**
   * Gets the terms of service verifier for the specified token with which the verifier is mapped.
   * Each verifier is identified by a key (the token here) in a cache and these verifiers are each
   * for a given user trying to log on.
   * @param tosToken a token.
   * @return the verifier that checks if the user must accept terms of service.
   */
  public static synchronized UserMustAcceptTermsOfServiceVerifier getUserMustAcceptTermsOfServiceVerifier(
      String tosToken) {
    return UserMustAcceptTermsOfServiceVerifier.get(tosToken);
  }
}
