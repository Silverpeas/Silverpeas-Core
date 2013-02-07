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
package org.silverpeas.authentication.verifier;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.silverpeas.authentication.AuthenticationException;
import org.silverpeas.authentication.verifier.exception.AuthenticationUserAccountBlockedException;

/**
 * Class that provides tools to verify if the user can login in relation to its account state.
 * User: Yohann Chastagnier
 * Date: 02/02/13
 */
public class UserCanLoginVerifier extends AbstractAuthenticationVerifier {
  public static final String ERROR_USER_ACCOUNT_BLOCKED = "Error_UserAccountBlocked";

  /**
   * Default constructor.
   * @param user
   */
  protected UserCanLoginVerifier(final UserDetail user) {
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
    if (!isUserStateValid()) {
      // For now, if user is not valid (BLOCKED, EXPIRED, ...) he is considered as BLOCKED.
      throw new AuthenticationUserAccountBlockedException("UserCanLoginVerifier.verify()",
          SilverpeasException.ERROR, "authentication.EX_VERIFY_USER_CAN_LOGIN",
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
  }

  /**
   * Is the specified user has a valid state?
   * A state is valid when the user can open a session in silverpeas, in other words, whether its
   * account is neither deleted or blocked or expired.
   * @return true if the user can open a session in Silverpeas, false otherwise.
   */
  private boolean isUserStateValid() {
    return getUser() != null && getUser().isValidState();
  }
}
