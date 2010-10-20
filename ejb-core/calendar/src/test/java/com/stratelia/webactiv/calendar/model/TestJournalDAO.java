/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.calendar.model;

import com.stratelia.webactiv.calendar.socialNetwork.SocialInformationEvent;
import com.silverpeas.components.model.AbstractTestDao;

import com.stratelia.webactiv.calendar.socialNetwork.SocialEvent;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import org.dbunit.database.IDatabaseConnection;
import org.junit.Before;
import org.junit.BeforeClass;

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

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException {
    AbstractTestDao.configureJNDIDatasource();
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.prepareData();
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
  /*
   * Test get next Events for my contact
   *
   *
   */

  public void testGetNextEventsForMyContacts() throws Exception {
    IDatabaseConnection connexion = null;

    JournalHeader s1 = new JournalHeader("", "");
    JournalHeader s2 = new JournalHeader("", "");
    JournalHeader s3 = new JournalHeader("", "");

    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
    s1.setId("1");
    s1.setName("RDV1");
    s1.setDescription("bla blab");
    s1.setDelegatorId("1");
    s1.setStartDate(formatDate.parse("2011/07/08"));
    s1.getClassification().setString("private");
    s2.setId("3");
    s2.setName("RDV3");
    s2.setDescription("bla2 blab2");
    s2.setDelegatorId("1");
    s2.setStartDate(formatDate.parse("2011/07/09"));
    s2.getClassification().setString("public");
    s3.setId("4");
    s3.setName("RDV4");
    s3.setDescription("bla4 blab4");
    s3.setDelegatorId("1");
    s3.setStartDate(formatDate.parse("2011/07/09"));
    s3.getClassification().setString("public");

    List<SocialInformationEvent> list = null;
    try {
      connexion = getConnection();
      List<String> myContactIds = new ArrayList<String>();
      myContactIds.add("1");
      list = dao.getNextEventsForMyContacts(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, 0, 0);

      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());


      list = dao.getNextEventsForMyContacts_MSS(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, 0, 0);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());


      list = null;
      list = dao.getNextEventsForMyContacts(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, 2, 1);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s2.getName(), list.get(1).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(0).getTitle());

      list = null;
      list = dao.getNextEventsForMyContacts(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, 2, 3);
      assertEquals("Should have 0 Events in db", 0, list.size());

    } finally {
      closeConnection(connexion);
    }
  }

  /*
   * Test get Last Events for my contact
   * Order desc
   *
   */
  public void testGetLastEventsForMyContacts() throws Exception {
    IDatabaseConnection connexion = null;

    JournalHeader s1 = new JournalHeader("", "");
    JournalHeader s2 = new JournalHeader("", "");
    JournalHeader s3 = new JournalHeader("", "");

    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    s1.setId("1");
    s1.setName("RDV1");
    s1.setDescription("bla blab");
    s1.setDelegatorId("1");
    s1.setStartDate(formatDate.parse("2011/07/08 14:00"));
    s1.getClassification().setString("private");
    s2.setId("3");
    s2.setName("RDV3");
    s2.setDescription("bla2 blab2");
    s2.setDelegatorId("1");
    s2.setStartDate(formatDate.parse("2011/07/09 15:00"));
    s2.getClassification().setString("public");
    s3.setId("4");
    s3.setName("RDV4");
    s3.setDescription("bla4 blab4");
    s3.setDelegatorId("1");
    s3.setStartDate(formatDate.parse("2011/07/09 09:00"));
    s3.getClassification().setString("public");

    List<SocialInformationEvent> list = null;
    try {
      connexion = getConnection();
      List<String> myContactIds = new ArrayList<String>();
      myContactIds.add("1");
      list = dao.getLastEventsForMyContacts(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, 0, 0);
//order S3,S2,S1
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());


      list = dao.getLastEventsForMyContacts_MSS(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, 0, 0);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());

//test limit
      list = null;
      list = dao.getLastEventsForMyContacts(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, 2, 1);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s3.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s1.getName(), list.get(1).getTitle());

      list = null;
      list = dao.getLastEventsForMyContacts(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, 2, 3);
      assertEquals("Should have 0 Events in db", 0, list.size());

    } finally {
      closeConnection(connexion);
    }
  }
  /*
   * Test get Last Events for my contact
   * Order desc
   *
   */

  public void testGetMyLastEvents() throws Exception {
    IDatabaseConnection connexion = null;

    JournalHeader s1 = new JournalHeader("", "");
    JournalHeader s2 = new JournalHeader("", "");
    JournalHeader s3 = new JournalHeader("", "");

    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    s1.setId("1");
    s1.setName("RDV1");
    s1.setDescription("bla blab");
    s1.setDelegatorId("1");
    s1.setStartDate(formatDate.parse("2011/07/08 14:00"));
    s1.getClassification().setString("private");
    s2.setId("3");
    s2.setName("RDV3");
    s2.setDescription("bla2 blab2");
    s2.setDelegatorId("1");
    s2.setStartDate(formatDate.parse("2011/07/09 15:00"));
    s2.getClassification().setString("public");
    s3.setId("4");
    s3.setName("RDV4");
    s3.setDescription("bla4 blab4");
    s3.setDelegatorId("1");
    s3.setStartDate(formatDate.parse("2011/07/09 09:00"));
    s3.getClassification().setString("public");

    List<SocialInformationEvent> list = null;
    try {
      connexion = getConnection();
      list = dao.getMyLastEvents(connexion.getConnection(), "2012/07/07", "1", 0, 0);
//order S3,S2,S1
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());


      list = dao.getMyLastEvents_MSS(connexion.getConnection(), "2012/07/07", "1", 0, 0);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());

//test limit
      list = null;
      list = dao.getMyLastEvents(connexion.getConnection(), "2012/07/07", "1", 2, 1);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s3.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s1.getName(), list.get(1).getTitle());

      list = null;
      list = dao.getMyLastEvents(connexion.getConnection(), "2012/07/07", "1", 2, 3);
      assertEquals("Should have 0 Events in db", 0, list.size());

    } finally {
      closeConnection(connexion);
    }
  }
}
