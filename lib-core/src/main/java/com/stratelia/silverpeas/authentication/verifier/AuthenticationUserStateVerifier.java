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

import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.verifier.exception
    .AuthenticationUserAccountBlockedException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class that provides tools to verify user state in relation to authentication.
 * User: Yohann Chastagnier
 * Date: 02/02/13
 */
public class AuthenticationUserStateVerifier extends AbstractAuthenticationVerifier {
  public static final String ERROR_USER_ACCOUNT_BLOCKED = "Error_UserAccountBlocked";

  /**
   * Default constructor.
   * @param user
   */
  protected AuthenticationUserStateVerifier(final UserDetail user) {
    super(user);
  }

  /**
   * Gets the error destination.
   * @return
   */
  public String getErrorDestination() {
    return "/Login.jsp?ErrorCode=" + ERROR_USER_ACCOUNT_BLOCKED;
  }

  /**
   * Verify user state.
   */
  public void verify() throws AuthenticationException {
    if (!check()) {
      // For now, if user is not valid (BLOCKED, EXPIRED, ...) he is considered as BLOCKED.
      throw new AuthenticationUserAccountBlockedException(
          "AuthenticationUserStateVerifier.verify()", SilverpeasException.ERROR,
          "authentication.EX_VERIFY_USER_STATE",
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
  }

  /**
   * Check user state.
   */
  public boolean check() {
    return getUser() != null && getUser().isValidState();
  }
}
