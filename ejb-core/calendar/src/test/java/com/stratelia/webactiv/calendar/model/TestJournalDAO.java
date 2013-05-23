/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.calendar.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.silverpeas.components.model.AbstractTestDao;

import com.stratelia.webactiv.calendar.socialnetwork.SocialInformationEvent;
import com.stratelia.webactiv.util.DateUtil;

import org.dbunit.database.IDatabaseConnection;

/**
 *
 * @author azzedine
 */
public class TestJournalDAO extends AbstractTestDao {

  private JournalDAO dao = new JournalDAO();

  @Override
  protected String getDatasetFileName() {
    return "calendar-dataset.xml";
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

      Date end = DateUtil.parse("2011/07/31");
      Date begin = DateUtil.parse("2011/07/01");

      list = dao.getNextEventsForUser(connexion.getConnection(), "2011/07/07", "1", null, begin, end);

      assertNotNull("Event should exist", list);
      assertEquals("Should have 3 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(0).getName());
      assertEquals("First should be ", s2.getName(), list.get(2).getName());
      assertEquals("First should be ", s3.getName(), list.get(1).getName());

      begin = DateUtil.parse("2011/07/09");
      list = null;
      list = dao.getNextEventsForUser(connexion.getConnection(), "2011/07/07", "1", null, begin, end);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s2.getName(), list.get(1).getName());
      assertEquals("First should be ", s3.getName(), list.get(0).getName());
      SocialInformationEvent event1 = new SocialInformationEvent(list.get(0));
      assertEquals("First should be ", s3.getName(), event1.getTitle());

      list = null;
      begin = DateUtil.parse("2011/07/10");
      list = dao.getNextEventsForUser(connexion.getConnection(), "2011/07/07", "1", null, begin, end);
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

      Date end = DateUtil.parse("2011/07/31");
      Date begin = DateUtil.parse("2011/07/01");

      list = dao.getNextEventsForMyContacts(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, begin, end);

      assertNotNull("Event should exist", list);
      assertEquals("Should have 3 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(0).getTitle());
      assertEquals("Second should be ", s2.getName(), list.get(2).getTitle());
      assertEquals("Third should be ", s3.getName(), list.get(1).getTitle());

      begin = DateUtil.parse("2011/07/09");
      list = null;
      list = dao.getNextEventsForMyContacts(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, begin, end);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s2.getName(), list.get(1).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(0).getTitle());

      list = null;
      begin = DateUtil.parse("2011/07/10");
      list = dao.getNextEventsForMyContacts(connexion.getConnection(), "2011/07/07", "2",
          myContactIds, begin, end);
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

      Date end = DateUtil.parse("2011/07/31");
      Date begin = DateUtil.parse("2011/07/01");

      list = dao.getLastEventsForMyContacts(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, begin, end);
//order S3,S2,S1
      assertNotNull("Event should exist", list);
      assertEquals("Should have 3 Events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(2).getTitle());
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());

//test limit
      list = null;
      begin = DateUtil.parse("2011/07/09");
      list = dao.getLastEventsForMyContacts(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, begin, end);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("Second should be ", s3.getName(), list.get(1).getTitle());

      list = null;
      begin = DateUtil.parse("2011/07/10");
      list = dao.getLastEventsForMyContacts(connexion.getConnection(), "2012/07/07", "2",
          myContactIds, begin, end);
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
      Date end = DateUtil.parse("2011/07/31");
      Date begin = DateUtil.parse("2011/07/01");
      list = dao.getMyLastEvents(connexion.getConnection(), "2012/07/07", "1", begin, end);
//order S3,S2,S1
      assertNotNull("Event should exist", list);
      assertEquals("Should have 3 events in db", 3, list.size());
      assertEquals("First should be ", s1.getName(), list.get(2).getTitle());
      assertEquals("Second should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("Third should be ", s3.getName(), list.get(1).getTitle());

//test limit
      list = null;
      begin = DateUtil.parse("2011/07/09");
      list = dao.getMyLastEvents(connexion.getConnection(), "2012/07/07", "1", begin, end);
      assertNotNull("Event should exist", list);
      assertEquals("Should have 2 Events in db", 2, list.size());
      // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
      assertEquals("First should be ", s2.getName(), list.get(0).getTitle());
      assertEquals("First should be ", s3.getName(), list.get(1).getTitle());

      list = null;
      begin = DateUtil.parse("2011/07/10");
      list = dao.getMyLastEvents(connexion.getConnection(), "2012/07/07", "1", begin, end);
      assertEquals("Should have 0 Events in db", 0, list.size());

    } finally {
      closeConnection(connexion);
    }
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-table.sql";
  }
}
