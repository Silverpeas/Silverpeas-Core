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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



import static org.junit.Assert.*;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationDao;
import org.silverpeas.util.DBUtil;

/**
 * @author Bensalem Nabil
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-invitation-embbed-datasource.xml"})
public class TestInvitationDao {

  @Inject
  private DataSource dataSource;
  private InvitationDao dao = new InvitationDao();

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  protected ReplacementDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet =
        new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        TestInvitationDao.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/social/invitation/socialNetwork_Invitation-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;

  }

  @After
  public void clear() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    DBUtil.clearTestInstance();
  }

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  /*
   * Test Create invitation
   */
  @Test
  public void testCreateInvitation() throws Exception {
    Connection connexion = null;
    Invitation invitation = new Invitation(4, 6, "message 50 to 30", new Date());
    try {
      connexion = getConnection();
      int id = dao.createInvitation(connexion, invitation);
      assertNotNull("Invitation should have been created", id);

      assertTrue("New id is correct", id > 0);
      invitation.setId(id);
      Invitation createdInvitation = dao.getInvitation(connexion, id);
      assertNotNull("Invitation not found in db", createdInvitation);
      assertEquals("Invitation in db not as expected", invitation, createdInvitation);

    } finally {
      DBUtil.close(connexion);
    }
  }

  /*
   * Test delete invitation
   */
  @Test
  public void testDeleteInvitation() throws Exception {
    Connection connexion = null;
    Invitation expectedLisaInviteMartha = new Invitation(2, 3, "lisa to martha", toDate(2010, 4, 3,
        11, 23, 15));
    expectedLisaInviteMartha.setId(2);
    try {
      connexion = getConnection();
      Invitation lisaInviteMartha = dao.getInvitation(connexion, 2);
      assertNotNull("Invitation should exist", lisaInviteMartha);
      dao.deleteInvitation(connexion, 2);
      lisaInviteMartha = dao.getInvitation(connexion, 2);
      assertNull("Invitation should no longer exist", lisaInviteMartha);
    } finally {
      DBUtil.close(connexion);
    }
  }

  /*
   * Test delete same invitations
   */
  @Test
  public void testDeleteSameInvitations() throws Exception {
    Connection connexion = null;
    Invitation expectedLisaInviteBart =
        new Invitation(2, 6, "lisa to bart", toDate(2011, 5, 2, 10, 8, 00));
    expectedLisaInviteBart.setId(7);
    Invitation expectedBartInviteLisa =
        new Invitation(3, 2, "bart to lisa", toDate(2011, 5, 2, 10, 5, 00));
    expectedBartInviteLisa.setId(6);
    try {
      connexion = getConnection();
      Invitation lisaInviteBart = dao.getInvitation(connexion, 7);
      assertNotNull("Invitation should exist", lisaInviteBart);
      Invitation bartInviteLisa = dao.getInvitation(connexion, 6);
      assertNotNull("Invitation should exist", bartInviteLisa);
      dao.deleteSameInvitations(connexion, 7);
      lisaInviteBart = dao.getInvitation(connexion, 7);
      assertNull("Invitation should no longer exist", lisaInviteBart);
      bartInviteLisa = dao.getInvitation(connexion, 6);
      assertNull("Invitation should no longer exist", bartInviteLisa);
    } finally {
      DBUtil.close(connexion);
    }
  }

  /*
   * Test get invitation
   */
  @Test
  public void testGetInvitation() throws Exception {
    Connection connexion = null;
    Invitation simpsonInviteLisa = new Invitation(1, 2, "simpson to lisa", toDate(2010,
        Calendar.FEBRUARY, 1, 10, 34, 15));
    int id = 1;
    simpsonInviteLisa.setId(1);
    try {
      connexion = getConnection();
      Invitation dbInvitation = dao.getInvitation(connexion, id);
      assertNotNull("Invitation not found in db", dbInvitation);
      // assertEquals("Invitation in db not as expected", simpsonInviteLisa, dbInvitation);
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getId(), dbInvitation.getId());
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getSenderId(), dbInvitation.
          getSenderId());
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getReceiverId(), dbInvitation.
          getReceiverId());
      assertEquals("Contact in db not as expected", simpsonInviteLisa.getInvitationDate(),
          dbInvitation.getInvitationDate());
    } finally {
      DBUtil.close(connexion);
    }
  }

  /*
   * Test get All my invitations sent
   */
  @Test
  public void testGetAllMyInvitationsSent() throws Exception {
    Connection connexion = null;
    Invitation simpsonInviteLisa = new Invitation(1, 2, "simpson to lisa", toDate(2010,
        Calendar.FEBRUARY, 1, 10, 34, 15));
    Invitation simpsonInviteNabil = new Invitation(1, 4, "simpson to nabil", toDate(2010,
        Calendar.JULY, 2, 10, 33, 10));
    int myId = 1;
    try {
      connexion = getConnection();
      List<Invitation> invitations = dao.getAllMyInvitationsSent(connexion, myId);
      assertNotNull("Invitation should exist", invitations);
      assertEquals("Should have 2 invitations in db", 2, invitations.size());
      simpsonInviteLisa.setId(invitations.get(0).getId());
      assertEquals("First should be simpson to lisa", simpsonInviteLisa, invitations.get(0));
      simpsonInviteNabil.setId(invitations.get(1).getId());
      assertEquals("Second should be simpson to Nabil", simpsonInviteNabil, invitations.get(1));
    } finally {
      DBUtil.close(connexion);
    }
  }

  /*
   * Test get All my invitations Receive
   */
  @Test
  public void testGetAllMyInvitationsReceive() throws Exception {
    Connection connexion = null;
    Invitation marthanviteSimpson = new Invitation(3, 1, "martha to simpson", toDate(2010,
        Calendar.MAY, 11, 15, 25, 32));
    Invitation jacquesinviteSimpson = new Invitation(5, 1, "jacques to simpson", toDate(2010,
        Calendar.JULY, 2, 10, 33, 10));
    int myId = 1;
    try {
      connexion = getConnection();
      List<Invitation> invitations =
          dao.getAllMyInvitationsReceive(connexion, myId);
      assertNotNull("Invitation should exist", invitations);
      assertEquals("Should have 2 invitations in db", 2, invitations.size());
      marthanviteSimpson.setId(invitations.get(0).getId());
      assertEquals("First should be martha to simpson", marthanviteSimpson, invitations.get(0));
      jacquesinviteSimpson.setId(invitations.get(1).getId());
      assertEquals("Second should be jacques to simpson", jacquesinviteSimpson, invitations.get(1));
    } finally {
      DBUtil.close(connexion);
    }
  }

  private Date toDate(int year, int month, int day, int hour, int minute, int second) {
    GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
    return calendar.getTime();
  }
}
