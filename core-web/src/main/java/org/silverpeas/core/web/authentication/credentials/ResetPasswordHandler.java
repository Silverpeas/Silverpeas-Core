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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailParameters;
import org.silverpeas.core.security.authentication.password.service.PasswordRulesServiceProvider;

import javax.servlet.http.HttpServletRequest;

public class ResetPasswordHandler extends FunctionHandler {

  private static final String USER_ID_LOG_DATA = "userId=";
  private static final String LOG_CONTEXT = "CredentialsServlet.resetPasswordHandler.doAction()";

  @Override
  public String doAction(HttpServletRequest request) {
    final String authenticationKey = request.getParameter("key");
    String userId;
    try {
      userId = getAdminService().getUserIdByAuthenticationKey(authenticationKey);
    } catch (Exception e) {
      return getGeneral().getString("forgottenPasswordResetError");
    }
    try {
      if (userId != null) {
        final String password = PasswordRulesServiceProvider.getPasswordRulesService().generate();
        final ForgottenPasswordMailParameters parameters = initializeParameters(userId);
        final UserFull user = getUserFull(request, userId);
        resetPassword(userId, password, user);
        parameters.setPassword(password);
        parameters.setLink(getContextPath(request) + "/ResetLoginPassword"
            + "?login=" + user.getLogin()
            + "&domainId=" + user.getDomainId());
        getForgottenPasswordMailManager().sendNewPasswordMail(parameters);
        return getGeneral().getString("forgottenPasswordReset");
      } else {
        return getGeneral().getString("forgottenPasswordResetError");
      }
    } catch (ForgottenPasswordException fpe) {
      return forgottenPasswordError(request, fpe);
    }
  }

  private ForgottenPasswordMailParameters initializeParameters(final String userId)
      throws ForgottenPasswordException {
    ForgottenPasswordMailParameters parameters = null;
    try {
      parameters = getMailParameters(userId);
    } catch (AdminException e) {
      throw new ForgottenPasswordException(LOG_CONTEXT,
          "forgottenPassword.EX_GET_USER_DETAIL", USER_ID_LOG_DATA + userId, e);
    }
    return parameters;
  }

  private UserFull getUserFull(final HttpServletRequest request, final String userId)
      throws ForgottenPasswordException {
    UserFull user;
    try {
      user = getAdminService().getUserFull(userId);
      request.setAttribute("userLanguage", user.getUserPreferences().getLanguage());
    } catch (AdminException e) {
      throw new ForgottenPasswordException(LOG_CONTEXT,
          "forgottenPassword.EX_GET_FULL_USER_DETAIL", USER_ID_LOG_DATA + userId, e);
    }
    return user;
  }

  private void resetPassword(final String userId, final String password, final UserFull user)
      throws ForgottenPasswordException {
    try {
      AuthenticationCredential credential = AuthenticationCredential
          .newWithAsLogin(user.getLogin())
          .withAsDomainId(user.getDomainId());
      AuthenticationService authenticator = AuthenticationServiceProvider.getService();
      authenticator.resetPassword(credential, password);
    } catch (AuthenticationException e) {
      throw new ForgottenPasswordException(LOG_CONTEXT,
          "forgottenPassword.EX_RESET_PASSWORD_FAILED", USER_ID_LOG_DATA + userId, e);
    }
  }
}
