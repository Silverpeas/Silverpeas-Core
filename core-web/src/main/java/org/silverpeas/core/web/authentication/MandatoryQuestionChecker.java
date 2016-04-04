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

package org.silverpeas.core.web.authentication;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Singleton
public class MandatoryQuestionChecker {

  private final static SettingBundle general =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
  private String destination;

  public String getDestination() {
    return destination;
  }

  protected MandatoryQuestionChecker() {
  }

  public boolean check(HttpServletRequest req, String authenticationKey) {
    boolean isUserLoginQuestionMandatory = "personalQuestion".equals(general.getString(
        "forgottenPwdActive")) && general.getBoolean("userLoginQuestionMandatory", false);
    if (isUserLoginQuestionMandatory) {
      HttpSession session = req.getSession();
      session.setAttribute("svplogin_Key", authenticationKey);
      try {
        String userId = AdministrationServiceProvider.getAdminService().identify(authenticationKey,
            session.getId(), false, false);
        UserDetail userDetail = AdministrationServiceProvider.getAdminService().getUserDetail(userId);
        if (userDetail != null && !userDetail.isAnonymous() && !StringUtil.isDefined(userDetail.
            getLoginQuestion())) {
          req.setAttribute("userDetail", userDetail);
          destination = "/CredentialsServlet/ChangeQuestion";
        }
      } catch (AdminException e) {
        SilverTrace
            .error("util", "MandatoryQuestionChecker.check()", "root.MSG_GEN_EXIT_METHOD", e);
      }
    }
    return StringUtil.isDefined(destination);
  }
}
