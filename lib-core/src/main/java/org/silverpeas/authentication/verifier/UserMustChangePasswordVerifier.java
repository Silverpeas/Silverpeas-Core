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
import org.silverpeas.authentication.AuthenticationPasswordAboutToExpireException;
import org.silverpeas.authentication.AuthenticationPasswordExpired;

/**
 * Class that provides tools to verify if the user have to change his password or if the user will
 * soon have to change his password.
 * User: Yohann Chastagnier
 * Date: 14/02/13
 */
public class UserMustChangePasswordVerifier extends AbstractAuthenticationVerifier {

  protected static boolean isMaxConnectionActivated = false;
  protected static boolean isOffsetConnectionActivated = false;
  protected static int nbMaxConnectionsForForcing = 0;
  protected static int nbMaxConnectionsForProposing = 0;

  static {
    setup(settings.getInteger("nbSuccessfulUserConnectionsBeforeForcingPasswordChange", 0),
        settings.getInteger("nbSuccessfulUserConnectionsBeforeProposingToChangePassword", 0));
  }

  /**
   * Initializing settings.
   */
  protected static void setup(int nbSuccessfulUserConnectionsBeforeForcingPasswordChange,
      int nbSuccessfulUserConnectionsBeforeProposingToChangePassword) {

    // Default values
    isMaxConnectionActivated = false;
    isOffsetConnectionActivated = false;
    nbMaxConnectionsForForcing = 0;
    nbMaxConnectionsForProposing = 0;

    // Custom values
    nbMaxConnectionsForForcing = nbSuccessfulUserConnectionsBeforeForcingPasswordChange;
    if (nbMaxConnectionsForForcing > 0) {
      isMaxConnectionActivated = true;

      nbMaxConnectionsForProposing = nbSuccessfulUserConnectionsBeforeProposingToChangePassword;
      if (nbMaxConnectionsForProposing > 0) {
        isOffsetConnectionActivated = true;
      }
    }
  }

  /**
   * Default constructor.
   * @param user
   */
  protected UserMustChangePasswordVerifier(final UserDetail user) {
    super(user);
  }

  /**
   * Verify if user has to change his password or if the user will soon be obliged to change his
   * password.
   */
  public void verify() throws AuthenticationException {
    if (proposeToUserToChangePassword()) {
      throw new AuthenticationPasswordAboutToExpireException(
          "UserMustChangePasswordVerifier.verify()", SilverpeasException.ERROR,
          "authentication.EX_VERIFY_ASKING_USER_CHANGE_PASSWORD",
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
    if (mustForceUserToChangePassword()) {
      throw new AuthenticationPasswordExpired(
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
  }

  /**
   * Does the server must force user to change his password?
   * @return true if the user will soon be obliged to change his password.
   */
  private boolean mustForceUserToChangePassword() {
    return !(!isMaxConnectionActivated || getUser() == null ||
        getUser().getNbSuccessfulLoginAttempts() == 0) &&
        getUser().getNbSuccessfulLoginAttempts() >= nbMaxConnectionsForForcing;
  }

  /**
   * Does the server proposing to user to change his password?
   * @return true if the user password has to be changed.
   */
  private boolean proposeToUserToChangePassword() {
    return !(!isMaxConnectionActivated || !isOffsetConnectionActivated || getUser() == null ||
        getUser().getNbSuccessfulLoginAttempts() == 0) && !mustForceUserToChangePassword() &&
        getUser().getNbSuccessfulLoginAttempts() >= nbMaxConnectionsForProposing;
  }
}
