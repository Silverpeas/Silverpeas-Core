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
package com.stratelia.silverpeas.authentication.verifier;

import com.stratelia.webactiv.beans.admin.UserDetail;

import static com.stratelia.silverpeas.authentication.verifier.AbstractAuthenticationVerifier
    .getUserById;
import static com.stratelia.silverpeas.authentication.verifier.AbstractAuthenticationVerifier
    .getUserByLoginAndDomain;

/**
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
public class AuthenticationUserVerifier {

  /**
   * Gets user state verifier from UserDetail.
   * @param user
   */
  public static AuthenticationUserStateVerifier userState(UserDetail user) {
    return new AuthenticationUserStateVerifier(user);
  }

  /**
   * Gets user state verifier from a user identifier.
   * @param userId
   */
  public static AuthenticationUserStateVerifier userState(String userId) {
    return userState(getUserById(userId));
  }

  /**
   * Gets user state verifier from a login and a domain identifier.
   * @param login
   * @param domainId
   */
  public static AuthenticationUserStateVerifier userState(String login, String domainId) {
    return userState(getUserByLoginAndDomain(login, domainId));
  }

  /**
   * Gets user connection attempt verifier from a login and a domain identifier.
   * @param login
   * @param domainId
   */
  public static AuthenticationUserConnectionAttemptsVerifier userConnectionAttempts(String login,
      String domainId) {
    UserDetail user = getUserByLoginAndDomain(login, domainId);
    if (user == null) {
      // Dummy user (but contains user connection elements)
      user = new UserDetail();
      user.setLogin(login);
      user.setDomainId(domainId);
    }
    return userConnectionAttempts(user);
  }

  /**
   * Gets user connection attempt verifier from a login and a domain identifier.
   * @param user
   */
  public synchronized static AuthenticationUserConnectionAttemptsVerifier userConnectionAttempts(
      UserDetail user) {
    return AuthenticationUserConnectionAttemptsVerifier.get(user);
  }
}
