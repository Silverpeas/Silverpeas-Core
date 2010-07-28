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
package com.silverpeas.socialNetwork.invitation.control;

import com.silverpeas.socialNetwork.invitation.Invitation;
import com.silverpeas.socialNetwork.invitation.InvitationService;
import com.silverpeas.socialNetwork.invitation.model.InvitationUser;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Bensalem Nabil
 */
public class InvitationSessionController extends AbstractComponentSessionController {

  private InvitationService invitationService = null;
  public String lastMainAction = "";
  private int i = 0;

  /**
   * Standard Session Controller Constructeur
   *
   *
   * @param mainSessionCtrl   The user's profile
   * @param componentContext  The component's profile
   *
   * @see
   */
  public InvitationSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle",
        "com.silverpeas.socialNetwork.settings.socialNetworkIcons",
        "com.silverpeas.socialNetwork.settings.socialNetworkSettings");
    invitationService = new InvitationService();
  }

  public void invite(int senderId, int receiveId, String message) {


    Invitation invitation = new Invitation(senderId, receiveId, message, new Date());
    invitationService.invite(invitation);
  }

  public void ignoreInvitation(int id) {
    invitationService.ignoreInvitation(id);
  }

  public void acceptInvitation(int id) {
    invitationService.accepteInvitation(id);
  }

  public List<InvitationUser> getAllMyInvitationsSent(int myId) {
    List<InvitationUser> invitationUsers = new ArrayList<InvitationUser>();
    List<Invitation> invitations = invitationService.getAllMyInvitationsSent(myId);
    for (Invitation varI : invitations) {
      invitationUsers.add(new InvitationUser(varI, this.getUserDetail(varI.getReceiverId() + "")));
    }
    return invitationUsers;
  }

  public List<InvitationUser> getAllMyInvitationsReceive(int myId) {
    List<InvitationUser> invitationUsers = new ArrayList<InvitationUser>();
    List<Invitation> invitations = invitationService.getAllMyInvitationsReceive(myId);
    for (Invitation varI : invitations) {
      invitationUsers.add(new InvitationUser(varI, this.getUserDetail(varI.getSenderId() + "")));
    }
    return invitationUsers;

  }

  public String getStat() {

    return i + "";
  }

  public void changeStat() {
    i++;

  }
}
