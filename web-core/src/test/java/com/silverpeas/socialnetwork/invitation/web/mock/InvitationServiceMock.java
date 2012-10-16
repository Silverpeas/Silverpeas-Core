/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.socialnetwork.invitation.web.mock;

import com.silverpeas.socialnetwork.invitation.Invitation;
import com.silverpeas.socialnetwork.invitation.InvitationService;
import com.silverpeas.socialnetwork.relationShip.RelationShip;
import java.util.List;
import javax.inject.Named;
import static org.mockito.Mockito.mock;

/**
 * A mock of the invitation service dedicated to tests. This mocks is in fact a wrapper to the true
 * invitation service mocked with the mockito framework.
 */
@Named("invitationService")
public class InvitationServiceMock extends InvitationService {
  
  private InvitationService mock;
  
  public InvitationServiceMock() {
    mock = mock(InvitationService.class);
  }
  
  public InvitationService getMockedInvitationService() {
    return mock;
  }

  @Override
  public int invite(Invitation invitation) {
    return mock.invite(invitation);
  }

  @Override
  public void ignoreInvitation(int id) {
    mock.ignoreInvitation(id);
  }

  @Override
  public RelationShip getRelationShip(int relationShipId) {
    return mock.getRelationShip(relationShipId);
  }

  @Override
  public Invitation getInvitation(int senderId, int receiverId) {
    return mock.getInvitation(senderId, receiverId);
  }

  @Override
  public Invitation getInvitation(int id) {
    return mock.getInvitation(id);
  }

  @Override
  public List<Invitation> getAllMyInvitationsSent(int userId) {
    return mock.getAllMyInvitationsSent(userId);
  }

  @Override
  public List<Invitation> getAllMyInvitationsReceive(int myId) {
    return mock.getAllMyInvitationsReceive(myId);
  }

  @Override
  public int accepteInvitation(int idInvitation) {
    return mock.accepteInvitation(idInvitation);
  }
  
}
