/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Navigation case : user has committed change password form.
 *
 * @author ehugonnet
 */
public class EffectiveChangePasswordHandler extends ChangePasswordFunctionHandler {

  private final AuthenticationService authenticator =
      AuthenticationServiceProvider.getService();
  private ForcePasswordChangeHandler forcePasswordChangeHandler = new ForcePasswordChangeHandler();

  @Override
  public String doAction(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    String key = (String) session.getAttribute("svplogin_Key");

    try {
      String userId = getAdminService().identify(key, session.getId(), false, false);
      UserDetail ud = getAdminService().getUserDetail(userId);
      try {
        String login = ud.getLogin();
        String domainId = ud.getDomainId();
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(login)
            .withAsPassword(oldPassword).withAsDomainId(domainId);
        authenticator.changePassword(credential, newPassword);

        return "/AuthenticationServlet?Login=" + login + "&Password=" + newPassword + "&DomainId="
            + domainId;
      } catch (AuthenticationException e) {
        SilverTrace.error("peasCore", "effectiveChangePasswordHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
        return performUrlChangePasswordError(request, forcePasswordChangeHandler.doAction(request),
            ud);
      }
    } catch (AdminException e) {
      SilverTrace.error("peasCore", "effectiveChangePasswordHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
      return forcePasswordChangeHandler.doAction(request);
    }
  }
}
