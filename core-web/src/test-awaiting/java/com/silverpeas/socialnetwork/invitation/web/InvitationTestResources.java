/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.socialnetwork.invitation.web;

import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;
import org.silverpeas.core.socialnetwork.invitation.web.InvitationEntity;
import org.silverpeas.core.socialnetwork.invitation.web.mock.InvitationServiceMock;
import com.silverpeas.web.TestResources;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * The resources to use in the test on the InvitationResource REST service. Theses objects manage in
 * a single place all the resources required to perform correctly unit tests on the
 * InvitationResource published operations.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class InvitationTestResources extends TestResources {

  @Inject
  private InvitationServiceMock invitationServiceMock;
  public static final String JAVA_PACKAGE = "com.silverpeas.socialNetwork.invitation.web";
  public static final String SPRING_CONTEXT = "spring-invitation-webservice.xml";

  @PostConstruct
  public void initialize() {
    assertThat(invitationServiceMock, notNullValue());
    int currentUserId = Integer.valueOf(USER_ID_IN_TEST);
    InvitationService mock = invitationServiceMock.getMockedInvitationService();
    when(mock.getAllMyInvitationsSent(currentUserId)).thenReturn(new ArrayList<Invitation>());
    when(mock.getAllMyInvitationsReceive(currentUserId)).thenReturn(new ArrayList<Invitation>());
  }

  /**
   * Creates and saves some invitations coming from others users and received by the specified user.
   *
   * @param user the user that has received the invitations.
   * @return an array with the web representation of the invitations.
   */
  public InvitationEntity[] saveSomeReceivedInvitations(UserDetail user) {
    int userId = Integer.valueOf(user.getId());
    List<Invitation> invitations = new ArrayList<Invitation>(3);
    InvitationEntity[] invitationEntities = new InvitationEntity[3];

    Invitation invitation = new Invitation(1000, userId, "coucou 1", new Date());
    invitation.setId(0);
    invitations.add(invitation);
    invitationEntities[0] = InvitationEntity.fromInvitation(invitation);

    invitation = new Invitation(1001, userId, "coucou 2", new Date());
    invitation.setId(1);
    invitations.add(invitation);
    invitationEntities[1] = InvitationEntity.fromInvitation(invitation);

    invitation = new Invitation(1002, userId, "coucou 3", new Date());
    invitation.setId(2);
    invitations.add(invitation);
    invitationEntities[2] = InvitationEntity.fromInvitation(invitation);

    InvitationService mock = invitationServiceMock.getMockedInvitationService();
    when(mock.getAllMyInvitationsReceive(userId)).thenReturn(invitations);

    return invitationEntities;
  }

  /**
   * Creates and saves some invitations sent by the specified user to other ones.
   *
   * @param user the user that has sent the invitations.
   * @return an array with the web representation of the invitations.
   */
  public InvitationEntity[] saveSomeSentInvitations(UserDetail user) {
    int userId = Integer.valueOf(user.getId());
    List<Invitation> invitations = new ArrayList<Invitation>(3);
    InvitationEntity[] invitationEntities = new InvitationEntity[3];

    Invitation invitation = new Invitation(userId, 1005, "coucou 1", new Date());
    invitation.setId(10);
    invitations.add(invitation);
    invitationEntities[0] = InvitationEntity.fromInvitation(invitation);

    invitation = new Invitation(userId, 1006, "coucou 2", new Date());
    invitation.setId(11);
    invitations.add(invitation);
    invitationEntities[1] = InvitationEntity.fromInvitation(invitation);

    invitation = new Invitation(userId, 1007, "coucou 3", new Date());
    invitation.setId(12);
    invitations.add(invitation);
    invitationEntities[2] = InvitationEntity.fromInvitation(invitation);

    InvitationService mock = invitationServiceMock.getMockedInvitationService();
    when(mock.getAllMyInvitationsSent(userId)).thenReturn(invitations);

    return invitationEntities;
  }
}
