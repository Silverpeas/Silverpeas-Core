/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.socialnetwork.myProfil.servlets;

import com.silverpeas.socialnetwork.myProfil.control.SocialNetworkService;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

public class JSONServlet extends HttpServlet {

  private static final long serialVersionUID = -843491398398079951L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (mainSessionCtrl == null) {
      JSONObject jsonStatus = new JSONObject();
      jsonStatus.put("status", "silverpeastimeout");
      PrintWriter out = response.getWriter();
      out.println(jsonStatus);
      return;
    }
    String userId = mainSessionCtrl.getUserId();
    String action = request.getParameter("Action");
    if ("updateStatus".equalsIgnoreCase(action)) {
      SocialNetworkService socialNetworkService = new SocialNetworkService(userId);
      String status = request.getParameter("status");
      // if status is empty or set with a text, update it (an empty status means no status)
      if (status != null) {
        status = socialNetworkService.changeStatusService(status);

      } // if status equal null don't do update status and do get Last status
      else {
        status = Encode.forHtml(socialNetworkService.getLastStatusService());
      }
      JSONObject jsonStatus = new JSONObject();
      jsonStatus.put("status", status);
      PrintWriter out = response.getWriter();
      out.println(jsonStatus);
    }
  }
}
