/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.socialNetwork.invitation.servlets;

import static com.silverpeas.socialNetwork.invitation.servlets.InvitationJSONActions.*;
import static com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import com.silverpeas.socialNetwork.myProfil.control.MyProfilSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;

public class InvitationJSONServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {

    HttpSession session = req.getSession(true);

    String sessionName = "Silverpeas_" + "myProfile";
    MyProfilSessionController mpsc =
        (MyProfilSessionController) session.getAttribute(sessionName);
    if (mpsc == null) {
      MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
      mpsc = new MyProfilSessionController(mainSessionCtrl, null);
      session.setAttribute(sessionName, mpsc);
    }

    res.setContentType("application/json");

    InvitationJSONActions action = valueOf(req.getParameter("Action"));

    Writer writer = res.getWriter();
    JSONObject jsonObject = new JSONObject();
    switch (action) {
      case SendInvitation:
      {
        String receiverId = req.getParameter("TargetUserId");
        String message = req.getParameter("Message");
        mpsc.sendInvitation(receiverId, message);
        jsonObject.put("success", true);
        break;
      }
        
      case IgnoreInvitation: 
      {
        String id = req.getParameter("Id");
        mpsc.ignoreInvitation(id);
        jsonObject.put("success", true);
        break;
      }
        
      case AcceptInvitation:
      {
        String id = req.getParameter("Id");
        mpsc.acceptInvitation(id);
        jsonObject.put("success", true);
        break;
      }

    }
    writer.write(jsonObject.toString());
  }
}