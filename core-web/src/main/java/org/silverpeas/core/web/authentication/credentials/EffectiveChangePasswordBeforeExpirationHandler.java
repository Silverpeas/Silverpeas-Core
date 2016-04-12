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

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Navigation case : user has committed change password form.
 */
public class EffectiveChangePasswordBeforeExpirationHandler extends ChangePasswordFunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    MainSessionController controller =
        (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (controller == null) {
      return "/Login.jsp";
    }

    SettingBundle settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.passwordExpiration");
    String passwordChangeURL =
        settings.getString("passwordChangeURL", "/defaultPasswordAboutToExpire.jsp");

    UserDetail ud = controller.getCurrentUserDetail();
    try {
      String login = ud.getLogin();
      String domainId = ud.getDomainId();
      String oldPassword = request.getParameter("oldPassword");
      String newPassword = request.getParameter("newPassword");
      AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(login)
          .withAsPassword(oldPassword).withAsDomainId(domainId);
      AuthenticationService authenticator = AuthenticationServiceProvider.getService();
      authenticator.changePassword(credential, newPassword);

      GraphicElementFactory gef =
          (GraphicElementFactory) session
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      String favoriteFrame = gef.getLookFrame();

      return "/Main/" + favoriteFrame;
    } catch (AuthenticationException e) {
      SilverTrace.error("peasCore", "effectiveChangePasswordHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", e);
      return performUrlChangePasswordError(request, passwordChangeURL, ud);
    }
  }
}
