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

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserAccountBlockedException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserAccountDeactivatedException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;


/**
 * Class that provides tools to verify if the user can login in relation to its account state.
 * User: Yohann Chastagnier
 * Date: 02/02/13
 */
public class UserCanLoginVerifier extends AbstractAuthenticationVerifier {
  public static final String ERROR_INCORRECT_LOGIN_PWD = "1";
  public static final String ERROR_INCORRECT_LOGIN_PWD_DOMAIN = "6";
  public static final String ERROR_USER_ACCOUNT_BLOCKED = "Error_UserAccountBlocked";
  public static final String ERROR_USER_ACCOUNT_DEACTIVATED = "Error_UserAccountDeactivated";

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
    String errorDest = "/Login.jsp?ErrorCode=";

    Domain[] tabDomains = null;
    try {
      tabDomains = Administration.get().getAllDomains();
    } catch (AdminException e) {
      SilverTrace.error("authentication", "UserCanLoginVerifier.getErrorDestination()",
          "authentication.EX_VERIFY_USER_CAN_LOGIN",
          e);
    }

    if (getUser() == null || StringUtil.isNotDefined(getUser().getId())) {
      if(tabDomains != null && tabDomains.length > 1) {
        errorDest += ERROR_INCORRECT_LOGIN_PWD_DOMAIN;
      } else {
        errorDest += ERROR_INCORRECT_LOGIN_PWD;
      }
    } else if (!isUserStateValid()) {
      if (getUser().isDeactivatedState()) {
        errorDest += ERROR_USER_ACCOUNT_DEACTIVATED;
      } else {
        errorDest += ERROR_USER_ACCOUNT_BLOCKED;
      }
    }
    return errorDest;
  }

  /**
   * Verify if the user can login.
   */
  public void verify() throws AuthenticationException {
    if(getUser() == null) {
      // Authentication failed
      throw new AuthenticationBadCredentialException("UserCanLoginVerifier.verify()",
          SilverpeasException.ERROR, "authentication.EX_VERIFY_USER_CAN_LOGIN");
    } else if (!isUserStateValid()) {
      // For now, if user is not valid (BLOCKED, DEACTIVATED, EXPIRED, DELETED, UNKNOWN) he is
      // considered as BLOCKED.
      if (getUser().isDeactivatedState()) {
        throw new AuthenticationUserAccountDeactivatedException("UserCanLoginVerifier.verify()",
            SilverpeasException.ERROR, "authentication.EX_VERIFY_USER_CAN_LOGIN",
            "Login=" + getUser().getLogin());
      }
      throw new AuthenticationUserAccountBlockedException("UserCanLoginVerifier.verify()",
          SilverpeasException.ERROR, "authentication.EX_VERIFY_USER_CAN_LOGIN",
          "Login=" + getUser().getLogin());
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
