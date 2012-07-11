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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.social.relationShip;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.socialnetwork.relationShip.RelationShip;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;

import java.util.Calendar;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author Bensalem Nabil
 */
public class TestRelationShipService extends AbstractTestDao {

  private RelationShipService dao;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    dao = new RelationShipService();
  }

  @Override
  protected String getDatasetFileName() {
    return "socialNetwork_Relationship-dataset.xml";
  }


  /*
   * Test remove RelationShip
   *
   *
   */
  public void testRemoveRelationShip() throws Exception {
    RelationShip expectedRelationShip1 = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 2);
    RelationShip expectedRelationShip2 = new RelationShip(2, 1, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 1);
    expectedRelationShip1.setId(1);
    expectedRelationShip2.setId(2);

    RelationShip relationShip1 = dao.getRelationShip(1, 2);
    assertNotNull("Relationship should exist", relationShip1);
    assertEquals("Relationship should be Equals", relationShip1, expectedRelationShip1);
    RelationShip relationShip2 = dao.getRelationShip(2, 1);
    assertNotNull("Relationship should exist", relationShip2);
    assertEquals("Relationship should be Equals", relationShip2, expectedRelationShip2);
    dao.removeRelationShip(1, 2);
    relationShip1 = dao.getRelationShip(1, 2);
    assertNull("Invitation should no longer exist", relationShip1);
    relationShip2 = dao.getRelationShip(2, 1);
    assertNull("Invitation should no longer exist", relationShip2);

  }

  /*
   * Test get RelationShip
   *
   *
   */
  public void testGetRelationShip() throws Exception {

    RelationShip expectedRelationShip = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY, 01,
        10, 34, 15), 2);
    expectedRelationShip.setId(1);
    RelationShip dbrelationShip = dao.getRelationShip(expectedRelationShip.getUser1Id(), expectedRelationShip.
        getUser2Id());
    assertNotNull("RelationShip not found in db", dbrelationShip);
    assertEquals("RelationShip in db not as expected", expectedRelationShip, dbrelationShip);
  }

  /*
   * Test get All my RelationShip sent
   *
   *
   */
  public void testGetAllMyRelationShips() throws Exception {


    RelationShip expectedRelationShip1 = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 2);
    expectedRelationShip1.setId(1);
    RelationShip expectedRelationShip2 = new RelationShip(1, 3, 0, toDate(2010, Calendar.MAY, 11, 15,
        25, 32), 3);
    expectedRelationShip2.setId(4);
    int myId = 1;

    List<RelationShip> relationShips = dao.getAllMyRelationShips(myId);
    assertNotNull("Relationships should exist", relationShips);
    assertEquals("Should have 2 relationships in db", 2, relationShips.size());
    assertEquals("First should be 1 et 2", expectedRelationShip1, relationShips.get(0));
    assertEquals("Second should be 1 et 3", expectedRelationShip2, relationShips.get(1));

  }

  private Date toDate(int year, int month, int day, int hour, int minute, int second) {
    GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
    return calendar.getTime();

  }
}
