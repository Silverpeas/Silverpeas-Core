/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailManager;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailParameters;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.model.Domain;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class ForgotPasswordHandler extends FunctionHandler {

  private AuthenticationService authenticator = AuthenticationServiceProvider.getService();
  private ForgottenPasswordMailManager forgottenPasswordMailManager =
      new ForgottenPasswordMailManager();

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    String userId;
    try {
      userId = getAdminService().getUserIdByLoginAndDomain(login, domainId);
    } catch (AdminException e) {
      // Login incorrect.
      request.setAttribute("login", login);

      List<Domain> domains = authenticator.getAllDomains();
      String domain = "";
      for (Domain aDomain: domains) {
        if (aDomain.getId().equals(domainId)) {
          domain = aDomain.getName();
        }
      }
      request.setAttribute("domain", domain);
      return getGeneral().getString("forgottenPasswordInvalidLogin");
    }

    try {
      if (authenticator.isPasswordChangeAllowed(domainId)) {
        String authenticationKey;
        try {
          authenticationKey = authenticator.getAuthenticationKey(login, domainId);
        } catch (AuthenticationException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.forgotPasswordHandler.doAction()",
              "forgottenPassword.EX_GET_USER_AUTHENTICATION_KEY",
              "login=" + login + " ; domainId=" + domainId, e);
        }

        // Envoi d'un mail contenant un lien permettant de lancer la réinitialisation
        // automatique du mot de passe.
        try {
          ForgottenPasswordMailParameters parameters = getMailParameters(userId);
          parameters.setLink(
              getContextPath(request) + "/ResetPassword?key=" + authenticationKey);
          forgottenPasswordMailManager.sendResetPasswordRequestMail(parameters);
          return getGeneral().getString("forgottenPasswordChangeAllowed");
        } catch (AdminException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.forgotPasswordHandler.doAction()",
              "forgottenPassword.EX_GET_USER_DETAIL", "userId=" + userId, e);
        } catch (MessagingException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.forgotPasswordHandler.doAction()",
              "forgottenPassword.EX_SEND_MAIL", "userId=" + userId, e);
        }
      } else {
        // Affichage d'un message d'information invitant à joindre l'administrateur système
        return getGeneral().getString("forgottenPasswordChangeNotAllowed");
      }
    } catch (ForgottenPasswordException fpe) {
      return forgottenPasswordError(request, fpe);
    }
  }

}
