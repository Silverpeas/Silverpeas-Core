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

package org.silverpeas.servlets.credentials;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Navigation case : force user to change his password.
 * @author ehugonnet
 */
public class ForcePasswordChangeHandler extends FunctionHandler {

  @Override
  public String doAction(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    String key = (String) session.getAttribute("svplogin_Key");
    try {
      String userId = getAdmin().authenticate(key, session.getId(), false, false);
      UserDetail ud = getAdmin().getUserDetail(userId);
      request.setAttribute("userDetail", ud);
      String changePasswordPage = getGeneral().getString("userLoginForcePasswordChangePage");
      return changePasswordPage;
    } catch (AdminException e) {
      // Error : go back to login page
      SilverTrace.error("peasCore", "forcePasswordChangeHandler.doAction()",
          "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
      return "/Login.jsp";
    }
  }
}
