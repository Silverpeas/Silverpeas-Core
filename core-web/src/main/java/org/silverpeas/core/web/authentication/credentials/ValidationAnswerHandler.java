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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.encryption.cipher.CryptMD5;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user validates his answer to his login question.
 * @author ehugonnet
 */
@Service
public class ValidationAnswerHandler extends ChangeQuestionAnswerFunctionHandler {

  @Override
  public String getFunction() {
    return "ValidateAnswer";
  }

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    String answer = request.getParameter("answer");
    boolean encryptedAnswer = getAuthenticationSettings().getBoolean("loginAnswerEncrypted", false);

    try {
      String userId = getAdminService().getUserIdByLoginAndDomain(login, domainId);
      UserDetail userDetail = getAdminService().getUserDetail(userId);
      request.setAttribute("userDetail", userDetail);
      request.setAttribute("userLanguage", userDetail.getUserPreferences().getLanguage());

      // encrypt answer if needed
      if (encryptedAnswer) {
        answer = CryptMD5.encrypt(answer);
      }

      if (answer.equals(userDetail.getLoginAnswer())) {
        return getGeneral().getString("userResetPasswordPage");
      } else {
        // Invalid answer.
        return performQuestionAnswerError(request, getGeneral().getString("userLoginQuestionPage"),
            userDetail);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("validation answer error with login {0}", new String[]{login}, e);
      return "/Login?ErrorCode=2";
    }
  }
}
