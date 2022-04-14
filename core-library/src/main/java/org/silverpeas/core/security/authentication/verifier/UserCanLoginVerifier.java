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

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserAccountBlockedException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserAccountDeactivatedException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;


/**
 * Class that provides tools to verify if the user can connect in relation to its account state.
 * @author Yohann Chastagnier
 * Date: 02/02/13
 */
public class UserCanLoginVerifier extends AbstractAuthenticationVerifier {

  /**
   * Default constructor.
   * @param user the user behind a connexion attempt.
   */
  protected UserCanLoginVerifier(final UserDetail user) {
    super(user);
  }

  /**
   * Gets the error destination.
   * @return the relative path of the error web page.
   */
  public String getErrorDestination() {
    String errorDest = "/Login?ErrorCode=";

    Domain[] tabDomains = null;
    try {
      tabDomains = Administration.get().getAllDomains();
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }

    if (getUser() == null || StringUtil.isNotDefined(getUser().getId())) {
      if(tabDomains != null && tabDomains.length > 1) {
        errorDest += AuthenticationResponse.Status.BAD_LOGIN_PASSWORD_DOMAIN;
      } else {
        errorDest += AuthenticationResponse.Status.BAD_LOGIN_PASSWORD;
      }
    } else if (isUserStateNotValid()) {
      if (getUser().isDeactivatedState()) {
        errorDest += AuthenticationResponse.Status.USER_ACCOUNT_DEACTIVATED;
      } else {
        errorDest += AuthenticationResponse.Status.USER_ACCOUNT_BLOCKED;
      }
    }
    return errorDest;
  }

  /**
   * Verify if the user can log in.
   */
  public void verify() throws AuthenticationException {
    if(getUser() == null) {
      // Authentication failed
      throw new AuthenticationBadCredentialException("No user with such credential");
    } else if (isUserStateNotValid()) {
      // For now, if user is not valid (BLOCKED, DEACTIVATED, EXPIRED, REMOVED, DELETED, UNKNOWN)
      // he is considered as BLOCKED.
      if (getUser().isDeactivatedState()) {
        throw new AuthenticationUserAccountDeactivatedException(
            "The account of the user with login " + getUser().getLogin() + " is deactivated");
      }
      throw new AuthenticationUserAccountBlockedException(
          "The account of the user with login " + getUser().getLogin() + " is blocked"
      );
    }
  }

  /**
   * Is the specified user has a valid state?
   * A state is valid when the user can open a session in silverpeas, in other words, whether its
   * account is neither deleted nor blocked nor expired.
   * @return true if the user can open a session in Silverpeas, false otherwise.
   */
  private boolean isUserStateNotValid() {
    return getUser() == null || !getUser().isValidState();
  }
}
