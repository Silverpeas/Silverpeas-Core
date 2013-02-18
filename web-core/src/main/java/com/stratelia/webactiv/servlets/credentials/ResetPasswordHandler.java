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

package com.stratelia.webactiv.servlets.credentials;

import org.silverpeas.authentication.AuthenticationCredential;
import org.silverpeas.authentication.exception.AuthenticationException;
import org.silverpeas.authentication.AuthenticationService;
import org.silverpeas.authentication.password.ForgottenPasswordException;
import org.silverpeas.authentication.password.ForgottenPasswordMailParameters;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserFull;
import org.silverpeas.password.service.PasswordServiceFactory;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author ehugonnet
 */
public class ResetPasswordHandler extends FunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    try {
      String authenticationKey = request.getParameter("key");
      String userId = null;
      try {
        userId = getAdmin().getUserIdByAuthenticationKey(authenticationKey);
      } catch (Exception e) {
        return getGeneral().getString("forgottenPasswordResetError");
      }
      if (userId != null) {
        String password = PasswordServiceFactory.getPasswordService().generate();
        ForgottenPasswordMailParameters parameters = null;
        try {
          parameters = getMailParameters(userId);
        } catch (AdminException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.resetPasswordHandler.doAction()",
              "forgottenPassword.EX_GET_USER_DETAIL", "userId=" + userId, e);
        }

        UserFull user;
        try {
          user = getAdmin().getUserFull(userId);
        } catch (AdminException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.resetPasswordHandler.doAction()",
              "forgottenPassword.EX_GET_FULL_USER_DETAIL", "userId=" + userId, e);
        }

        try {
          AuthenticationCredential credential = AuthenticationCredential
              .newWithAsLogin(user.getLogin())
              .withAsDomainId(user.getDomainId());
          AuthenticationService authenticator = new AuthenticationService();
          authenticator.resetPassword(credential, password);
        } catch (AuthenticationException e1) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.resetPasswordHandler.doAction()",
              "forgottenPassword.EX_RESET_PASSWORD_FAILED", "userId=" + userId, e1);
        }

        parameters.setPassword(password);
        parameters.setLink(getContextPath(request) + "/ResetLoginPassword"
            + "?login=" + user.getLogin()
            + "&domainId=" + user.getDomainId());
        try {
          getForgottenPasswordMailManager().sendNewPasswordMail(parameters);
        } catch (MessagingException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.resetPasswordHandler.doAction()",
              "forgottenPassword.EX_SEND_MAIL", "userId=" + userId, e);
        }

        return getGeneral().getString("forgottenPasswordReset");
      } else {
        return getGeneral().getString("forgottenPasswordResetError");
      }
    } catch (ForgottenPasswordException fpe) {
      return forgottenPasswordError(request, fpe);
    }
  }
}
