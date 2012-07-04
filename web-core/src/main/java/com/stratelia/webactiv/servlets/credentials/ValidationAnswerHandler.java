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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user validates his answer to his login question.
 * @author ehugonnet
 */
public class ValidationAnswerHandler extends FunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    String answer = request.getParameter("answer");
    try {
      String userId = getAdmin().getUserIdByLoginAndDomain(login, domainId);
      UserDetail userDetail = getAdmin().getUserDetail(userId);
      request.setAttribute("userDetail", userDetail);

      if (answer.equals(userDetail.getLoginAnswer())) {
        return getGeneral().getString("userResetPasswordPage");
      } else {
        // Invalid answer.
        request.setAttribute("message", getM_Multilang().getString("invalidAnswer"));
        return getGeneral().getString("userLoginQuestionPage");
      }
    } catch (AdminException e) {
      // Error : go back to login page
      SilverTrace.error("peasCore", "validationAnswerHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
      return "/Login.jsp";
    }
  }
}
