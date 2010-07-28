/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.calendar.model;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.stratelia.webactiv.calendar.socialNetwork.SocialInformationEvent;
import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.calendar.control.CalendarBmEJB;
import com.stratelia.webactiv.calendar.socialNetwork.SocialEvent;

import java.util.ArrayList;
import java.util.List;
import org.dbunit.database.IDatabaseConnection;

/**
 *
 * @author azzedine
 */
public class TestJournalDAO extends AbstractTestDao {

  private JournalDAO dao;
  private SocialEvent socialEvent;

  @Override
  protected String getDatasetFileName() {
    return "calendar-dataset.xml";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dao = new JournalDAO();

  }

  /*
   * Test get next Events
   *
   *
   */
  public void testGetNextEventsForUser() throws Exception {
    IDatabaseConnection connexion = null;

    JournalHeader s1 = new JournalHeader("", "");
    JournalHeader s2 = new JournalHeader("", "");
    JournalHeader s3 = new JournalHeader("", "");


    s1.setId("1");
    s1.setName("RDV1");
    s1.setDescription("bla blab");
    s1.setDelegatorId("1");
    s1.setStartDay("2011/07/08");
    s1.setStartHour("14:00");
    s1.getClassification().setString("private");
    s2.setId("3");
    s2.setName("RDV3");
    s2.setDescription("bla2 blab2");
    s2.setDelegatorId("1");
    s2.setStartDay("2011/07/09");
    s2.setStartHour("15:00");
    s2.getClassification().setString("public");
    s3.setId("4");
    s3.setName("RDV4");
    s3.setDescription("bla4 blab4");
    s3.setDelegatorId("1");
    s3.setStartDay("2011/07/09");
    s3.setStartHour("09:00");
    s3.getClassification().setString("public");

    List<JournalHeader> list = null;
    try {
      connexion = getConnection();
      list = dao.getNextEventsForUser(connexion.getConnection(), "2011/07/07", "1", null, 0, 0);

      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(0).getName());
      assertEquals("First should be ", s2.getName(), list.get(2).getName());
      assertEquals("First should be ", s3.getName(), list.get(1).getName());
      list = null;
      list = dao.getNextEventsForUser(connexion.getConnection(), "2011/07/07", "1", null, 2, 1);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s2.getName(), list.get(1).getName());
      assertEquals("First should be ", s3.getName(), list.get(0).getName());
      SocialInformationEvent event1 = new SocialInformationEvent(list.get(0));
      SocialInformationEvent event2 = new SocialInformationEvent(list.get(1));
      assertEquals("First should be ", s3.getName(), event1.getTitle());
      list = null;
      list = dao.getNextEventsForUser(connexion.getConnection(), "2011/07/07", "1", null, 2, 3);
      assertEquals("Should have 0 Events in db", 0, list.size());

    } finally {
      closeConnection(connexion);
    }
  }
}
