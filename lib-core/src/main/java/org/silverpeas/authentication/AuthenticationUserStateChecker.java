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
package org.silverpeas.authentication;

import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * It checks the user state is valid in relation to the authentication.
 * User: Yohann Chastagnier
 * Date: 02/02/13
 */
public class AuthenticationUserStateChecker {
  public static final String ERROR_USER_ACCOUNT_BLOCKED = "Error_UserAccountBlocked";

  /**
   * Gets the error destination.
   * @return the relative URL path of the error page for a user invalid state.
   */
  public static String getErrorDestination() {
    return "/Login.jsp?ErrorCode=" + ERROR_USER_ACCOUNT_BLOCKED;
  }

  /**
   * Verifies the state of the specified user.
   * @param userId the unique identifier of the user.
   * @throws AuthenticationException if the user hasn't a valid state. The type of the exception
   * informs about the reason of the verification failure.
   */
  public static void verify(String userId) throws AuthenticationException {
    verify(UserDetail.getById(userId));
  }

  /**
   * Is the specified user has a valid state?
   * A state is valid when the user can open a session in silverpeas, in other words, whether its
   * account is neither deleted or blocked or expired.
   * @param userId the unique identifier of the user.
   * @return true if the user can open a session in Silverpeas, false otherwise.
   */
  public static boolean isUserStateValid(String userId) throws AuthenticationException {
    return isUserStateValid(UserDetail.getById(userId));
  }

  /**
   * Verifies the state of the user identified by the specified login and domain to which he belongs.
   * @param login the login of the user to open a session in Silverpeas.
   * @param domainId the unique identifier of the domain to which he belongs.
   * @throws AuthenticationException if the user hasn't a valid state. The type of the exception
   * informs about the reason of the verification failure.
   */
  public static void verify(String login, String domainId) throws AuthenticationException {
    verify(getUser(login, domainId));
  }

  /**
   * Verifies the state of the user identified by its login and by the domain to which he belongs.
   * @param login the login of the user to open a session in Silverpeas.
   * @param domainId the unique identifier of the domain to which he belongs.
   * @throws AuthenticationException if the user hasn't a valid state. The type of the exception
   * informs about the reason of the verification failure.
   */
  public static boolean isUserStateValid(String login, String domainId) throws AuthenticationException {
    return isUserStateValid(getUser(login, domainId));
  }

  /**
   * Verifies the state of the specified user.
   * @param user the user.
   * @throws AuthenticationException if the user hasn't a valid state. The type of the exception
   * informs about the reason of the verification failure.
   */
  public static void verify(UserDetail userDetail) throws AuthenticationException {
    if (!isUserStateValid(userDetail)) {
      // For now, if user is not valid (BLOCKED, EXPIRED, ...) he is considered as BLOCKED.
      throw new AuthenticationUserAccountBlockedException("AuthenticationUserStateChecker.verify()",
          SilverpeasException.ERROR, "authentication.EX_VERIFY_USER_STATE",
          "Login=" + userDetail.getLogin());
    }
  }

  /**
   * Is the specified user has a valid state?
   * A state is valid when the user can open a session in silverpeas, in other words, whether its
   * account is neither deleted or blocked or expired.
   * @param user the user.
   * @return true if the user can open a session in Silverpeas, false otherwise.
   */
  public static boolean isUserStateValid(UserDetail userDetail) {
    return userDetail != null && userDetail.isValidState();
  }

  /**
   * Gets a user from its login and a domain.
   * @param login
   * @param domainId
   * @return
   * @throws AuthenticationException
   */
  private static UserDetail getUser(String login, String domainId) throws AuthenticationException {
    try {
      return UserDetail
          .getById(AdminReference.getAdminService().getUserIdByLoginAndDomain(login, domainId));
    } catch (AdminException e) {
      throw new AuthenticationException("AuthenticationUserStateChecker.getUser()",
          SilverpeasException.ERROR, "authentication.EX_GET_USER",
          "Login=" + login + ", domainId=" + domainId);
    }
  }
}
