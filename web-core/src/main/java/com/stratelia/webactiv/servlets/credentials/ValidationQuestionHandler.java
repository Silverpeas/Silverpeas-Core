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

import com.silverpeas.authentication.SilverpeasSessionOpener;
import com.silverpeas.util.cryptage.CryptMD5;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Navigation case : user has validated login question form.
 * @author ehugonnet
 */
public class ValidationQuestionHandler extends FunctionHandler {
  private static final SilverpeasSessionOpener sessionOpenener = new SilverpeasSessionOpener();

  @Override
  public String doAction(HttpServletRequest request) {
    HttpSession session = request.getSession();
    String key = (String) session.getAttribute("svplogin_Key");
    boolean answerCrypted = getAuthenticationSettings().getBoolean("loginAnswerCrypted", false);

    try {
      String userId = getAdmin().authenticate(key, session.getId(), false, false);
      UserDetail userDetail = getAdmin().getUserDetail(userId);
      String question = request.getParameter("question");
      String answer = request.getParameter("answer");
      userDetail.setLoginQuestion(question);

      // encrypt the answer if needed
      if (answerCrypted) {
        answer = CryptMD5.encrypt(answer);
      }
      userDetail.setLoginAnswer(answer);
      getAdmin().updateUser(userDetail);

      if (getGeneral().getBoolean("userLoginForcePasswordChange", false)) {
        return getGeneral().getString("userLoginForcePasswordChangePage");
      }
      return sessionOpenener.openSession(request, key);
    } catch (AdminException e) {
      // Error : go back to login page
      SilverTrace.error("peasCore", "validationQuestionHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", e);
      return "/Login.jsp";
    }
  }
}
