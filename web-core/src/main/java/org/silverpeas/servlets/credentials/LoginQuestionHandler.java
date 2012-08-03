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

package org.silverpeas.servlets.credentials;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user forgot his password and will be asked for his login question.
 * @author ehugonnet
 */
public class LoginQuestionHandler extends FunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    String login = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");
    try {
      String userId = getAdmin().getUserIdByLoginAndDomain(login, domainId);
      UserDetail userDetail = getAdmin().getUserDetail(userId);
      if (StringUtil.isDefined(userDetail.getLoginQuestion())) {
        request.setAttribute("userDetail", userDetail);
        return getGeneral().getString("userLoginQuestionPage");
      } else {
        // page d'erreur : veuillez contacter votre admin
        return "/Login.jsp";
      }
    } catch (AdminException e) {
      // Error : go back to login page
      SilverTrace.error("peasCore", "loginQuestionHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
      return "/Login.jsp";
    }
  }
}
