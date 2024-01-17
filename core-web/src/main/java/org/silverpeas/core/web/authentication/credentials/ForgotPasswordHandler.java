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
import org.silverpeas.core.security.authentication.AuthDomain;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailManager;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

public class ForgotPasswordHandler extends FunctionHandler {

  private final Authentication authenticator = Authentication.get();
  private final ForgottenPasswordMailManager forgottenPasswordMailManager =
      new ForgottenPasswordMailManager();

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    String userId;

    AuthDomain domain;
    String domainName = "";
    try {
      domain = authenticator.getAllAuthDomains().stream()
          .filter(d -> d.getId().equals(domainId))
          .findFirst()
          .orElseThrow();
      domainName = domain.getName();
      userId = getAdminService().getUserIdByLoginAndDomain(login, domainId);
      request.setAttribute("userLanguage", User.getById(userId).getUserPreferences().getLanguage());
    } catch (NoSuchElementException | AdminException e) {
      // Login incorrect.
      request.setAttribute("login", login);
      request.setAttribute("domain", domainName);
      return getGeneral().getString("forgottenPasswordInvalidLogin");
    }

    try {
      if (domain.getCredentialsChangePolicy().canPasswordBeChanged()) {
        return sendUserResetMail(request, login, domainId, userId);
      } else {
        // Affichage d'un message d'information invitant à joindre l'administrateur système
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
      forgottenPasswordMailManager.sendResetPasswordRequestMail(parameters);
      return getGeneral().getString("forgottenPasswordChangeAllowed");
    } catch (AdminException | AuthenticationException e) {
      throw new ForgottenPasswordException(
          "CredentialsServlet.forgotPasswordHandler.doAction()",
          "forgottenPassword.EX_GET_USER_DETAIL", "userId=" + userId, e);
    }
  }

  private String getAuthenticationKey(final String login, final String domainId) throws AuthenticationException {
      return authenticator.getAuthToken(
          AuthenticationCredential.newWithAsLogin(login).withAsDomainId(domainId));
  }

}
