/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;

import static org.silverpeas.core.security.authentication.verifier.AbstractAuthenticationVerifier
    .getUserByCredential;
import static org.silverpeas.core.security.authentication.verifier.AbstractAuthenticationVerifier
    .getUserById;

/**
 * Factory that provides some verifiers about user authentication :
 * - one to verify if the user can login in relation to its account state
 * - one to verify if the user can try to login one more time after a login error
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
public class AuthenticationUserVerifierFactory {

  /**
   * Gets user state verifier from UserDetail.
   * @param user
   * @return the verifier that checks if the user can login in relation to its account state
   */
  public static UserCanLoginVerifier getUserCanLoginVerifier(UserDetail user) {
    return new UserCanLoginVerifier(user);
  }

  /**
   * Gets user state verifier from a user identifier.
   * @param userId
   * @return the verifier that checks if the user can login in relation to its account state
   */
  public static UserCanLoginVerifier getUserCanLoginVerifier(String userId) {
    return getUserCanLoginVerifier(getUserById(userId));
  }

  /**
   * Gets user state verifier from credentials.
   * @param credential
   * @return the verifier that checks if the user can login in relation to its account state
   */
  public static UserCanLoginVerifier getUserCanLoginVerifier(AuthenticationCredential credential) {
    return getUserCanLoginVerifier(getUserByCredential(credential));
  }

  /**
   * Gets user connection attempt verifier from a login and a domain identifier.
   * @param credential
   * @return the verifier that checks if the user can try to login one more time after a login
   *         error
   */
  public static UserCanTryAgainToLoginVerifier getUserCanTryAgainToLoginVerifier(
      AuthenticationCredential credential) {
    UserDetail user = getUserByCredential(credential);
    if (user == null) {
      // Dummy user (but contains user credentials)
      user = new UserDetail();
      user.setLogin(credential.getLogin());
      user.setDomainId(credential.getDomainId());
    }
    return getUserCanTryAgainToLoginVerifier(user);
  }

  /**
   * Gets user connection attempt verifier from a login and a domain identifier.
   * @param user
   * @return the verifier that checks if the user can try to login one more time after a login
   *         error
   */
  public synchronized static UserCanTryAgainToLoginVerifier getUserCanTryAgainToLoginVerifier(
      UserDetail user) {
    return UserCanTryAgainToLoginVerifier.get(user);
  }

  /**
   * Gets user must change his password verifier from credentials.
   * @param user
   * @return the verifier that checks if the user must change his password or if the user will soon
   *         have to change his password
   */
  public static UserMustChangePasswordVerifier getUserMustChangePasswordVerifier(UserDetail user) {
    return new UserMustChangePasswordVerifier(user);
  }

  /**
   * Gets user must change his password verifier from credentials.
   * @param credential
   * @return the verifier that checks if the user must change his password or if the user will soon
   *         have to change his password
   */
  public static UserMustChangePasswordVerifier getUserMustChangePasswordVerifier(
      AuthenticationCredential credential) {
    return getUserMustChangePasswordVerifier(getUserByCredential(credential));
  }

  /**
   * Gets user must accept terms of service verifier from credentials.
   * @param credential
   * @return the verifier that checks if the user must accept terms of service
   */
  public static UserMustAcceptTermsOfServiceVerifier getUserMustAcceptTermsOfServiceVerifier(
      AuthenticationCredential credential) {
    return new UserMustAcceptTermsOfServiceVerifier(getUserByCredential(credential));
  }

  /**
   * Gets user must accept terms of service verifier from a token.
   * @param tosToken
   * @return the verifier that checks if the user must accept terms of service
   */
  public synchronized static UserMustAcceptTermsOfServiceVerifier
  getUserMustAcceptTermsOfServiceVerifier(
      String tosToken) {
    return UserMustAcceptTermsOfServiceVerifier.get(tosToken);
  }
}
