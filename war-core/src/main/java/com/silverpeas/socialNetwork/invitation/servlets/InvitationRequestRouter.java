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

import com.silverpeas.directory.model.Member;
import com.silverpeas.socialNetwork.invitation.control.InvitationSessionController;
import com.silverpeas.socialNetwork.user.model.SNContactUser;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Bensalem Nabil
 */
public class InvitationRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;
  @Override
  public String getSessionControlBeanName() {
    return "invitation";
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new InvitationSessionController(mainSessionCtrl, componentContext);
  }
/**
 * 
 * @param function
 * @param componentSC
 * @param request
 * @return
 */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";

    InvitationSessionController invitationSC = (InvitationSessionController) componentSC;

    if (function.equalsIgnoreCase("Main")) {
      //Invitations reçues
      invitationSC.lastMainAction = function;
      int myId = Integer.parseInt(invitationSC.getUserId());
      request.setAttribute("InvitationUsers", invitationSC.getAllMyInvitationsReceive(myId));
      request.setAttribute("user",new SNContactUser(invitationSC.getUserId()));
      destination = "/socialNetwork/jsp/invitation/invitationTemplate.jsp";

    }
    if (function.equalsIgnoreCase("InvitationsSent")) {
      //Invitations envoyés
      invitationSC.lastMainAction = function;
      int myId = Integer.parseInt(invitationSC.getUserId());
      request.setAttribute("InvitationUsers", invitationSC.getAllMyInvitationsSent(myId));
      request.setAttribute("user",new SNContactUser(invitationSC.getUserId()));
      destination = "/socialNetwork/jsp/invitation/invitationSentTemplate.jsp";

    } else if (function.equalsIgnoreCase("invite")) {
      String userId = request.getParameter("Recipient");
      request.setAttribute("User", new Member(invitationSC.getUserDetail(userId)));
      destination = "/directory/jsp/invitationUser.jsp";
    } else if (function.equalsIgnoreCase("SendInvitation")) {
      int receiveId;

      if (StringUtil.isInteger(request.getParameter("Recipient"))) {
        receiveId = Integer.parseInt(request.getParameter("Recipient"));
        String txtMessage = request.getParameter("txtMessage");
        invitationSC.invite(Integer.parseInt(invitationSC.getUserId()), receiveId, txtMessage);
      }
      destination = "/directory/jsp/invitationUser.jsp?popupMode=Yes&Action=SendInvitation";

    } else if (function.equalsIgnoreCase("CancelSendInvitation")) {
      destination = "/directory/jsp/invitationUser.jsp?popupMode=Yes&Action=CancelSendInvitation";

    } else if (function.equalsIgnoreCase("IgnoreInvitation")) {

      if (StringUtil.isDefined(request.getParameter("UserId"))) {

        int myId = Integer.parseInt(request.getParameter("UserId"));
        invitationSC.ignoreInvitation(myId);
      }
      return getDestination("Main", componentSC, request);

    } else if (function.equalsIgnoreCase("AcceptInvitation")) {

      if (StringUtil.isDefined(request.getParameter("UserId"))) {
        int myId = Integer.parseInt(request.getParameter("UserId"));
        invitationSC.acceptInvitation(myId);
      }
      return getDestination("Main", componentSC, request);
    } 
    return destination;

  }
}
