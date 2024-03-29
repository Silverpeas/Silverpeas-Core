/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordAboutToExpireException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordExpired;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordMustBeChangedOnFirstLogin;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that provides tools to verify if the user have to change his password or if the user will
 * soon have to change his password.
 * @author Yohann Chastagnier Date: 14/02/13
 */
public class UserMustChangePasswordVerifier extends AbstractAuthenticationVerifier {

  private enum UserFirstLoginStep {
    CHANGE_PASSWORD,
    PASSWORD_CHANGED
  }

  private static final Map<String, UserFirstLoginStep> usersFirstLoginStep =
      new ConcurrentHashMap<>();

  protected static boolean isThatUserMustChangePasswordOnFirstLogin = false;
  protected static boolean isThatUserMustFillEmailAddressOnFirstLogin = false;
  protected static boolean isMaxConnectionActivated = false;
  protected static boolean isOffsetConnectionActivated = false;
  protected static int nbMaxConnectionsForForcing = 0;
  protected static int nbMaxConnectionsForProposing = 0;

  static {
    setup(settings.getBoolean("userMustChangePasswordOnFirstLogin", false),
        settings.getBoolean("userMustFillEmailAddressOnFirstLogin", false),
        settings.getInteger("nbSuccessfulUserConnectionsBeforeForcingPasswordChange", 0),
        settings.getInteger("nbSuccessfulUserConnectionsBeforeProposingToChangePassword", 0));
  }

  /**
   * Initializing settings.
   */
  protected static void setup(boolean userMustChangePasswordOnFirstLogin,
      boolean userMustFillEmailAddressOnFirstLogin,
      int nbSuccessfulUserConnectionsBeforeForcingPasswordChange,
      int nbSuccessfulUserConnectionsBeforeProposingToChangePassword) {

    // Default values
    isThatUserMustChangePasswordOnFirstLogin = false;
    isThatUserMustFillEmailAddressOnFirstLogin = false;
    isMaxConnectionActivated = false;
    isOffsetConnectionActivated = false;
    nbMaxConnectionsForForcing = 0;
    nbMaxConnectionsForProposing = 0;
    usersFirstLoginStep.clear();

    // Custom values
    isThatUserMustChangePasswordOnFirstLogin = userMustChangePasswordOnFirstLogin;
    isThatUserMustFillEmailAddressOnFirstLogin = userMustFillEmailAddressOnFirstLogin;
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
   * @param user the user behind a login.
   */
  protected UserMustChangePasswordVerifier(final User user) {
    super(user);
  }

  /**
   * Gets the destination on first login.
   * @return the URL of the next web page for a first login according to some settings.
   */
  public String getDestinationOnFirstLogin(HttpServletRequest request) {
    AuthenticationResponse.Status error = isThatUserMustFillEmailAddressOnFirstLogin ?
        AuthenticationResponse.Status.PASSWORD_EMAIL_TO_CHANGE_ON_FIRST_LOGIN :
        AuthenticationResponse.Status.PASSWORD_TO_CHANGE_ON_FIRST_LOGIN;
    if (request != null) {
      String language = (getUser() != null && StringUtil.isDefined(getUser().getId())) ?
          getUser().getUserPreferences().getLanguage() : I18NHelper.DEFAULT_LANGUAGE;
      String message = error.getMessage(language);
      request.setAttribute("message", message);
      request.setAttribute("isThatUserMustFillEmailAddressOnFirstLogin",
          isThatUserMustFillEmailAddressOnFirstLogin);
      if (isThatUserMustFillEmailAddressOnFirstLogin && getUser() != null) {
        request.setAttribute("emailAddress", getUser().getEmailAddress());
      }
    }
    return otherSettings.getString("passwordChangeOnFirstLoginURL") + "?ErrorCode=" + error;
  }

  /**
   * Verify if user has to change his password or if the user will soon be obliged to change his
   * password.
   */
  public void verify() throws AuthenticationException {

    if (mustForceUserToChangePasswordOnFirstLogin()) {
      throw new AuthenticationPasswordMustBeChangedOnFirstLogin(
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }

    if (proposeToUserToChangePassword()) {
      throw new AuthenticationPasswordAboutToExpireException(
          "The password has to be changed for the user with login " + getUser().getLogin());
    }

    if (mustForceUserToChangePassword()) {
      throw new AuthenticationPasswordExpired(
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
  }

  /**
   * Notifies that user has changed his password.
   */
  public void notifyPasswordChange() {
    if (isThatUserMustChangePasswordOnFirstLogin && getUser() != null) {
      usersFirstLoginStep.put(getUser().getId(), UserFirstLoginStep.PASSWORD_CHANGED);
    }
  }

  /**
   * Does the server must force user to change his password on first login?
   * @return true if the user must change his password on first login.
   */
  private boolean mustForceUserToChangePasswordOnFirstLogin() {
    boolean mustForceUserToChangePasswordOnFirstLogin = false;
    UserDetail user = (UserDetail) getUser();
    if (isThatUserMustChangePasswordOnFirstLogin && user != null && !user.isAnonymous()) {
      if (UserFirstLoginStep.PASSWORD_CHANGED.equals(usersFirstLoginStep.get(user.getId()))) {
        // User has changed his password just now, the authentication is ok
        usersFirstLoginStep.remove(user.getId());
      } else if (user.getLastLoginDate() == null ||
          UserFirstLoginStep.CHANGE_PASSWORD.equals(usersFirstLoginStep.get(user.getId()))) {
        // User must change his password
        usersFirstLoginStep.put(user.getId(), UserFirstLoginStep.CHANGE_PASSWORD);
        mustForceUserToChangePasswordOnFirstLogin = true;
      }
    }
    return mustForceUserToChangePasswordOnFirstLogin;
  }

  /**
   * Does the server must force user to change his password?
   * @return true if the user will soon be obliged to change his password.
   */
  private boolean mustForceUserToChangePassword() {
    UserDetail user = (UserDetail) getUser();
    return !(!isMaxConnectionActivated || user == null || user.isAnonymous() ||
        user.getNbSuccessfulLoginAttempts() == 0) &&
        user.getNbSuccessfulLoginAttempts() >= nbMaxConnectionsForForcing;
  }

  /**
   * Does the server proposing to user to change his password?
   * @return true if the user password has to be changed.
   */
  private boolean proposeToUserToChangePassword() {
    UserDetail user = (UserDetail) getUser();
    return !(!isMaxConnectionActivated || !isOffsetConnectionActivated || user == null ||
        user.isAnonymous() || user.getNbSuccessfulLoginAttempts() == 0) &&
        !mustForceUserToChangePassword() &&
        user.getNbSuccessfulLoginAttempts() >= nbMaxConnectionsForProposing;
  }
}
