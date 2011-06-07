/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.socialNetwork.invitation;

import com.silverpeas.components.model.AbstractTestDao;
import org.dbunit.database.IDatabaseConnection;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
public class TestInvitationDao extends AbstractTestDao {

  private InvitationDao dao;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    dao = new InvitationDao();
  }

  @Override
  protected String getDatasetFileName() {
    return "socialNetwork_Invitation-dataset.xml";
  }

  /*
   * Test Create invitation
   */
  public void testCreateInvitation() throws Exception {
    IDatabaseConnection connexion = null;

    Invitation invitation = new Invitation(4, 6, "message 50 to 30", new Date());
    try {
      connexion = getConnection();
      int id = dao.createInvitation(connexion.getConnection(), invitation);
      assertNotNull("Invitation should have been created", id);

      assertTrue("New id is correct", id > 0);
      invitation.setId(id);
      Invitation createdInvitation = dao.getInvitation(connexion.getConnection(), id);
      assertNotNull("Invitation not found in db", createdInvitation);
      assertEquals("Invitation in db not as expected", invitation, createdInvitation);

    } finally {
      closeConnection(connexion);
    }
  }

  /*
   * Test delete invitation
   */
  public void testDeleteInvitation() throws Exception {
    IDatabaseConnection connexion = null;

    Invitation expectedLisaInviteMartha = new Invitation(2, 3, "lisa to martha", toDate(2010, 4, 3,
        11, 23, 15));
    expectedLisaInviteMartha.setId(2);
    try {
      connexion = getConnection();
      Invitation lisaInviteMartha = dao.getInvitation(connexion.getConnection(), 2);
      assertNotNull("Invitation should exist", lisaInviteMartha);
      dao.deleteInvitation(connexion.getConnection(), 2);
      lisaInviteMartha = dao.getInvitation(connexion.getConnection(), 2);
      assertNull("Invitation should no longer exist", lisaInviteMartha);
    } finally {
      closeConnection(connexion);
    }
  }

  /*
   * Test delete same invitations
   */
  public void testDeleteSameInvitations() throws Exception {
    IDatabaseConnection connexion = null;

    Invitation expectedLisaInviteBart =
        new Invitation(2, 6, "lisa to bart", toDate(2011, 5, 2, 10, 8, 00));
    expectedLisaInviteBart.setId(7);
    Invitation expectedBartInviteLisa=
      new Invitation(3, 2, "bart to lisa", toDate(2011, 5, 2, 10, 5, 00));
    expectedBartInviteLisa.setId(6);
    try {
      connexion = getConnection();
      Invitation lisaInviteBart = dao.getInvitation(connexion.getConnection(), 7);
      assertNotNull("Invitation should exist", lisaInviteBart);
      Invitation bartInviteLisa = dao.getInvitation(connexion.getConnection(), 6);
      assertNotNull("Invitation should exist", bartInviteLisa);
      dao.deleteSameInvitations(connexion.getConnection(), 7);
      lisaInviteBart = dao.getInvitation(connexion.getConnection(), 7);
      assertNull("Invitation should no longer exist", lisaInviteBart);
      bartInviteLisa = dao.getInvitation(connexion.getConnection(), 6);
      assertNull("Invitation should no longer exist", bartInviteLisa);
    } finally {
      closeConnection(connexion);
    }
  }

  /*
   * Test get invitation
   */
  public void testGetInvitation() throws Exception {
    IDatabaseConnection connexion = null;

    Invitation simpsonInviteLisa = new Invitation(1, 2, "simpson to lisa", toDate(2010,
        Calendar.FEBRUARY, 1, 10, 34, 15));
    int id = 1;
    simpsonInviteLisa.setId(1);
    try {
      connexion = getConnection();
      Invitation dbInvitation = dao.getInvitation(connexion.getConnection(), id);
      assertNotNull("Invitation not found in db", dbInvitation);
      // assertEquals("Invitation in db not as expected", simpsonInviteLisa, dbInvitation);
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getId(), dbInvitation.getId());
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getSenderId(), dbInvitation.
          getSenderId());
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getReceiverId(), dbInvitation
          .
          getReceiverId());
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getInvitationDate(),
          dbInvitation.
              getInvitationDate());
    } finally {
      closeConnection(connexion);
    }
  }

  /*
   * Test get All my invitations sent
   */

  public void testGetAllMyInvitationsSent() throws Exception {
    IDatabaseConnection connexion = null;
    Invitation simpsonInviteLisa = new Invitation(1, 2, "simpson to lisa", toDate(2010,
        Calendar.FEBRUARY, 1, 10, 34, 15));
    Invitation simpsonInviteNabil = new Invitation(1, 4, "simpson to nabil", toDate(2010,
        Calendar.JULY, 2, 10, 33, 10));
    int myId = 1;
    try {
      connexion = getConnection();
      List<Invitation> invitations = dao.getAllMyInvitationsSent(connexion.getConnection(), myId);
      assertNotNull("Invitation should exist", invitations);
      assertEquals("Should have 2 invitations in db", 2, invitations.size());
      simpsonInviteLisa.setId(invitations.get(0).getId());
      assertEquals("First should be simpson to lisa", simpsonInviteLisa, invitations.get(0));
      simpsonInviteNabil.setId(invitations.get(1).getId());
      assertEquals("Second should be simpson to Nabil", simpsonInviteNabil, invitations.get(1));

    } finally {
      closeConnection(connexion);
    }
  }

  /*
   * Test get All my invitations Receive
   */

  public void testGetAllMyInvitationsReceive() throws Exception {
    IDatabaseConnection connexion = null;

    Invitation marthanviteSimpson = new Invitation(3, 1, "martha to simpson", toDate(2010,
        Calendar.MAY, 11, 15, 25, 32));

    Invitation jacquesinviteSimpson = new Invitation(5, 1, "jacques to simpson", toDate(2010,
        Calendar.JULY, 2, 10, 33, 10));
    int myId = 1;
    try {
      connexion = getConnection();
      List<Invitation> invitations =
          dao.getAllMyInvitationsReceive(connexion.getConnection(), myId);
      assertNotNull("Invitation should exist", invitations);
      assertEquals("Should have 2 invitations in db", 2, invitations.size());
      marthanviteSimpson.setId(invitations.get(0).getId());
      assertEquals("First should be martha to simpson", marthanviteSimpson, invitations.get(0));
      jacquesinviteSimpson.setId(invitations.get(1).getId());
      assertEquals("Second should be jacques to simpson", jacquesinviteSimpson, invitations.get(1));

    } finally {
      closeConnection(connexion);
    }
  }

  private Date toDate(int year, int month, int day, int hour, int minute, int second) {
    GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
    return calendar.getTime();

  }
}
