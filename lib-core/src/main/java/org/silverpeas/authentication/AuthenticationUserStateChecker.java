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
import org.silverpeas.authentication.AuthenticationException;

/**
 * Class that provides tools to check or verify user state in relation to authentication.
 * User: Yohann Chastagnier
 * Date: 02/02/13
 */
public class AuthenticationUserStateChecker {
  public static final String ERROR_USER_ACCOUNT_BLOCKED = "Error_UserAccountBlocked";

  /**
   * Gets the error destination.
   * @return
   */
  public static String getErrorDestination() {
    return "/Login.jsp?ErrorCode=" + ERROR_USER_ACCOUNT_BLOCKED;
  }

  /**
   * Verify user state.
   * @param userId
   */
  public static void verify(String userId) throws AuthenticationException {
    verify(getUser(userId));
  }

  /**
   * Check user state.
   * @param userId
   */
  public static boolean check(String userId) throws AuthenticationException {
    return check(getUser(userId));
  }

  /**
   * Verify user state.
   * @param login
   * @param domainId
   */
  public static void verify(String login, String domainId) throws AuthenticationException {
    verify(getUser(login, domainId));
  }

  /**
   * Check user state.
   * @param login
   * @param domainId
   */
  public static boolean check(String login, String domainId) throws AuthenticationException {
    return check(getUser(login, domainId));
  }

  /**
   * Verify user state.
   * @param userDetail
   */
  public static void verify(UserDetail userDetail) throws AuthenticationException {
    if (!check(userDetail)) {
      // For now, if user is not valid (BLOCKED, EXPIRED, ...) he is considered as BLOCKED.
      throw new AuthenticationUserAccountBlockedException("AuthenticationUserStateChecker.verify()",
          SilverpeasException.ERROR, "authentication.EX_VERIFY_USER_STATE",
          "Login=" + userDetail.getLogin());
    }
  }

  /**
   * Check user state.
   * @param userDetail
   */
  public static boolean check(UserDetail userDetail) {
    return userDetail != null && userDetail.isValidState();
  }

  /**
   * Gets a user from its identifier.
   * @param userId
   * @return
   */
  private static UserDetail getUser(String userId) {
    return UserDetail.getById(userId);
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
