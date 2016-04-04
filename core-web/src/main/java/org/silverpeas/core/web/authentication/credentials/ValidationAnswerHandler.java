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

import org.silverpeas.core.security.encryption.cipher.CryptMD5;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.UserDetail;
import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user validates his answer to his login question.
 * @author ehugonnet
 */
public class ValidationAnswerHandler extends ChangeQuestionAnswerFunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    String answer = request.getParameter("answer");
    boolean answerCrypted = getAuthenticationSettings().getBoolean("loginAnswerCrypted", false);

    try {
      String userId = getAdminService().getUserIdByLoginAndDomain(login, domainId);
      UserDetail userDetail = getAdminService().getUserDetail(userId);
      request.setAttribute("userDetail", userDetail);

      // encrypt answer if needed
      if (answerCrypted) {
        answer = CryptMD5.encrypt(answer);
      }

      if (answer.equals(userDetail.getLoginAnswer())) {
        return getGeneral().getString("userResetPasswordPage");
      } else {
        // Invalid answer.
        return performQuestionAnswerError(request, getGeneral().getString("userLoginQuestionPage"),
            userDetail);
      }
    } catch (AdminException e) {
      // Error : go back to login page
      SilverTrace.error("peasCore", "validationAnswerHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
      return "/Login.jsp";
    }
  }
}
