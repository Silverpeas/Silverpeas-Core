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
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authentication.AuthDomain;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailParameters;

import javax.servlet.http.HttpServletRequest;

@Service
public class ForgotPasswordHandler extends CredentialsFunctionFromLoginHandler {

  @Override
  public String getFunction() {
    return "ForgotPassword";
  }

  @Override
  public String doAction(HttpServletRequest request) {
    request.setAttribute("title", "screen.title.reinitRequested");
    LoginData loginData = fetchLoginData(request);
    if (loginData.isInvalid()) {
      // Login incorrect.
      request.setAttribute("login", loginData.getLoginId());
      request.setAttribute("domain", loginData.getDomainName());
      return getGeneral().getString("forgottenPasswordChangeNotAllowed");
    }

    ValidLoginData validLogin = (ValidLoginData) loginData;
    User user = validLogin.getUser();
    AuthDomain domain = validLogin.getDomain();
    request.setAttribute("userLanguage", user.getUserPreferences().getLanguage());
    try {
      if (domain.getCredentialsChangePolicy().canPasswordBeChanged()) {
        return sendUserResetMail(request, user.getLogin(), domain.getId(), user.getId());
      } else {
        // print out a message enjoining the user to contact the sysadmin
        return getGeneral().getString("forgottenPasswordChangeNotAllowed");
      }
    } catch (ForgottenPasswordException fpe) {
      return forgottenPasswordError(request, fpe);
    }
  }

  private String sendUserResetMail(final HttpServletRequest request, final String login,
      final String domainId, final String userId) throws ForgottenPasswordException {
    // send a mail to the user in which a link allow him to reset automatically his password
    try {
      ForgottenPasswordMailParameters parameters = getMailParameters(userId);
      parameters.setLink(
          getContextPath(request) + "/ResetPassword?key=" + getAuthenticationKey(login, domainId));
      getForgottenPasswordMailManager().sendResetPasswordRequestMail(parameters);
      return getGeneral().getString("forgottenPasswordChangeAllowed");
    } catch (AdminException | AuthenticationException e) {
      throw new ForgottenPasswordException(
          "CredentialsServlet.forgotPasswordHandler.doAction()",
          "forgottenPassword.EX_GET_USER_DETAIL", "userId=" + userId, e);
    }
  }

  private String getAuthenticationKey(final String login, final String domainId)
      throws AuthenticationException {
      return getAuthenticator().getAuthToken(
          AuthenticationCredential.newWithAsLogin(login).withAsDomainId(domainId));
  }

}
