/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.social.invitation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.silverpeas.components.model.AbstractTestDao;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;

import org.silverpeas.util.DBUtil;

/**
 *
 * @author Bensalem Nabil
 */
public class TestInvitationService extends AbstractTestDao {

  private InvitationService invitationService;

 @Override
  public void setUp() throws Exception {
    super.setUp();
    DBUtil.getInstanceForTest(getConnection().getConnection());
    invitationService = new InvitationService();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    DBUtil.clearTestInstance();
  }

  @Override
  protected String getDatasetFileName() {
    return "socialNetwork_Invitation-dataset.xml";
  }

  /*
   * Test  Invite(Send Invitation)
   *
   *
   */
  public void testInvite() throws Exception {

    Invitation newInvitation = new Invitation(4, 2, "message 50 to 30", new Date());
    Invitation existsInvitation = new Invitation(2, 3, "lisa to martha", new Date());

    int idNewInvitation = invitationService.invite(newInvitation);
    int idExistsInvitation = invitationService.invite(existsInvitation);
    assertTrue("Invitation should have been created", idNewInvitation > 0);
    assertEquals("Old Invitation should have been existe", idExistsInvitation, -1);
    assertTrue("New id is correct", idNewInvitation > 5);
    newInvitation.setId(idNewInvitation);
    Invitation createdInvitation = invitationService.getInvitation(idNewInvitation);
    assertNotNull("Invitation not found in db", createdInvitation);
    assertEquals("Invitation in db not as expected", newInvitation, createdInvitation);
  }
  /*
   * Test ignore invitation
   *
   *
   */

  public void testignoreInvitation() throws Exception {

    Invitation expectedLisaInviteMartha = new Invitation(2, 3, "lisa to martha", toDate(2010,
        Calendar.APRIL, 3, 11, 23, 15));
    expectedLisaInviteMartha.setId(2);
    Invitation lisaInviteMartha = invitationService.getInvitation(2);
    assertNotNull("Invitation should exist", lisaInviteMartha);
    assertEquals("Invitation should be lisa", expectedLisaInviteMartha, lisaInviteMartha);
    invitationService.ignoreInvitation(2);
    lisaInviteMartha = invitationService.getInvitation(2);
    assertNull("Invitation should no longer exist", lisaInviteMartha);

  }

  /*
   * Test get invitation
   *
   *
   */
  public void testGetInvitation() throws Exception {
    Invitation simpsonInviteLisa = new Invitation(1, 2, "simpson to lisa", toDate(2010,
        Calendar.FEBRUARY, 1, 10, 34, 15));
    int id = 1;
    simpsonInviteLisa.setId(1);

    Invitation dbInvitation = invitationService.getInvitation(id);
    assertNotNull("Invitation not found in db", dbInvitation);
    assertEquals("Contact in db not as expected", simpsonInviteLisa, dbInvitation);

  }
  /*
   * Test get All my invitations sent
   *
   *
   */

  public void testGetAllMyInvitationsSent() throws Exception {

    Invitation simpsonInviteLisa = new Invitation(1, 2, "simpson to lisa", toDate(2010,
        Calendar.FEBRUARY, 1, 10, 34, 15));
    Invitation simpsonInviteNabil = new Invitation(1, 4, "simpson to nabil", toDate(2010,
        Calendar.JULY, 2, 10, 33, 10));
    int myId = 1;

    List<Invitation> invitations = invitationService.getAllMyInvitationsSent(myId);
    assertNotNull("Invitation should exist", invitations);
    assertEquals("Should have 2 invitations in db", 2, invitations.size());
    simpsonInviteLisa.setId(invitations.get(0).getId());
    assertEquals("First should be simpson to lisa", simpsonInviteLisa, invitations.get(0));
    simpsonInviteNabil.setId(invitations.get(1).getId());
    assertEquals("Second should be simpson to Nabil", simpsonInviteNabil, invitations.get(1));

  }
  /*
   * Test get All my invitations Receive
   *
   *
   */

  public void testGetAllMyInvitationsReceive() throws Exception {


    Invitation marthanviteSimpson = new Invitation(3, 1, "martha to simpson", toDate(2010,
        Calendar.MAY, 11, 15, 25, 32));

    Invitation jacquesinviteSimpson = new Invitation(5, 1, "jacques to simpson", toDate(2010,
        Calendar.JULY, 2, 10, 33, 10));
    int myId = 1;

    List<Invitation> invitations = invitationService.getAllMyInvitationsReceive(myId);
    assertNotNull("Invitation should exist", invitations);
    assertEquals("Should have 2 invitations in db", 2, invitations.size());
    marthanviteSimpson.setId(invitations.get(0).getId());
    assertEquals("First should be martha to simpson", marthanviteSimpson, invitations.get(0));
    jacquesinviteSimpson.setId(invitations.get(1).getId());
    assertEquals("Second should be jacques to simpson", jacquesinviteSimpson, invitations.get(1));

  }
  /*
   * Test  Invite(Send Invitation)
   *
   *
   */

  public void testAccepteInvitation() throws Exception {

    Invitation noInvitation = new Invitation(4, 2, "message 50 to 30", new Date());
    noInvitation.setId(9);
    Invitation existsInvitation = new Invitation(2, 3, "lisa to martha", new Date());
    existsInvitation.setId(2);
    int idNoRelation = invitationService.accepteInvitation(9);
    assertTrue("Invitation should have not existe so relation should have not been existe ",
        idNoRelation == -1);
    int idNewRelation = invitationService.accepteInvitation(2);
    assertTrue("Relation should have been existe", idNewRelation > 0);
    //this inviatation existe déja comme RelationShip
    Invitation newInvitation = new Invitation(2, 3, "lisa to martha", new Date());
    int idNewInvitation = invitationService.invite(newInvitation);
    assertTrue("Invitation should have not existe because the relation beween them  already existe ",
        idNewInvitation == -2);

  }

  private Date toDate(int year, int month, int day, int hour, int minute, int second) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day);
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();

  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
