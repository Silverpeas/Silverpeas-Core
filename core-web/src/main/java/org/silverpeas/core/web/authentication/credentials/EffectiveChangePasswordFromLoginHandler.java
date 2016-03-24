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
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user asks to change his password from login page.
 */
public class EffectiveChangePasswordFromLoginHandler extends ChangePasswordFunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("login");
    String domainId = request.getParameter("domainId");
    String oldPassword = request.getParameter("oldPassword");
    String newPassword = request.getParameter("newPassword");
    String email = request.getParameter("emailAddress");
    AuthenticationCredential credential =
        AuthenticationCredential.newWithAsLogin(login).withAsPassword(oldPassword)
            .withAsDomainId(domainId);
    try {
      // Change password.
      AuthenticationService authenticator = AuthenticationServiceProvider.getService();
      authenticator.changePasswordAndEmail(credential, newPassword, email);
      return "/AuthenticationServlet?Login=" + login + "&Password=" + newPassword + "&DomainId=" +
          domainId;
    } catch (AuthenticationException e) {
      if (StringUtil.isDefined(email)) {
        // If an email is defined, this is indicating that the user had to fill its email. After
        // an error, the user could yet fill its email
        request.setAttribute("isThatUserMustFillEmailAddressOnFirstLogin", true);
        request.setAttribute("emailAddress", email);
      }
      // Error : go back to page
      SilverTrace.error("peasCore", "ChangePasswordFromLoginHandler.doAction()",
          "peasCore.EX_CANNOT_CHANGE_PWD", "login=" + login, e);
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
      return performUrlChangePasswordError(request,
          settings.getString("changePasswordFromLoginPage") + "?Login=" + login + "&DomainId=" +
              domainId, credential);
    }
  }
}
