/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.servlets.credentials;

import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordException;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordMailManager;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordMailParameters;
import com.stratelia.webactiv.beans.admin.AdminException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author ehugonnet
 */
public class ForgotPasswordHandler extends FunctionHandler {

  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
  private ForgottenPasswordMailManager forgottenPasswordMailManager =
      new ForgottenPasswordMailManager();

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    String userId = null;
    try {
      userId = getAdmin().getUserIdByLoginAndDomain(login, domainId);
    } catch (AdminException e) {
      // Login incorrect.
      request.setAttribute("login", login);

      Map<String, String> domains = lpAuth.getAllDomains();
      String domain = "";
      for (Entry<String, String> entry : domains.entrySet()) {
        if (entry.getKey().equals(domainId)) {
          domain = entry.getValue();
        }
      }
      request.setAttribute("domain", domain);
      return getGeneral().getString("forgottenPasswordInvalidLogin");
    }

    try {
      if (lpAuth.isPasswordChangeAllowed(domainId)) {
        String authenticationKey = null;
        try {
          authenticationKey = lpAuth.getAuthenticationKey(login, domainId);
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
