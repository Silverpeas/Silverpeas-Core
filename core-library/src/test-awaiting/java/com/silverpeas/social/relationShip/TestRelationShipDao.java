/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.social.relationShip;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.silverpeas.components.model.AbstractTestDao;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.relationShip.RelationShip;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipDao;

import org.silverpeas.util.DBUtil;
import org.silverpeas.core.util.DateUtil;

import org.dbunit.database.IDatabaseConnection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Bensalem Nabil
 */
public class TestRelationShipDao extends AbstractTestDao {

  private RelationShipDao dao;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DBUtil.getInstanceForTest(getConnection().getConnection());
    dao = new RelationShipDao();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    DBUtil.clearTestInstance();
  }

  @Override
  protected String getDatasetFileName() {
    return "socialNetwork_Relationship-dataset.xml";
  }

  /*
   * Test Create RelationShip
   */
  public void testCreateRelationShip() throws Exception {
    IDatabaseConnection connexion = getConnection();
    RelationShip newRelationShip = new RelationShip(5, 6, 0, toDate(2010, Calendar.FEBRUARY, 1, 10,
        34, 15), 6);
    int id = dao.createRelationShip(connexion.getConnection(), newRelationShip);
    assertThat(id, is(notNullValue()));
    assertThat(id, is(greaterThan(0)));
    newRelationShip.setId(id);
    RelationShip createdRelationShip = dao.getRelationShip(connexion.getConnection(),
        newRelationShip.getUser1Id(), newRelationShip.getUser2Id());
    assertThat(createdRelationShip, is(notNullValue()));
    assertThat(createdRelationShip, is(newRelationShip));
  }

  /*
   * Test delete RelationShip
   */
  public void testDeleteRelationShip() throws Exception {
    IDatabaseConnection connexion = getConnection();
    RelationShip expectedRelationShip = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY, 1,
        10, 34, 15), 2);
    expectedRelationShip.setId(1);
    RelationShip relationShip = dao.getRelationShip(connexion.getConnection(), 1, 2);
    assertThat(relationShip, is(notNullValue()));
    assertThat(relationShip, is(expectedRelationShip));
    boolean endAction = dao.deleteRelationShip(connexion.getConnection(), 1, 2);
    assertThat(endAction, is(true));
    relationShip = dao.getRelationShip(connexion.getConnection(), 1, 2);
    assertThat(relationShip, is(nullValue()));
  }

  /*
   * Test get RelationShip
   */
  public void testGetRelationShip() throws Exception {
    IDatabaseConnection connexion = getConnection();
    RelationShip expectedRelationShip = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY, 1,
        10, 34, 15), 2);
    expectedRelationShip.setId(1);
    RelationShip dbrelationShip = dao.getRelationShip(connexion.getConnection(),
        expectedRelationShip.getUser1Id(), expectedRelationShip.getUser2Id());
    assertThat(dbrelationShip, is(notNullValue()));
    assertThat(dbrelationShip, is(expectedRelationShip));
    boolean isInRelationShip = dao.isInRelationShip(connexion.getConnection(), expectedRelationShip.
        getUser1Id(), expectedRelationShip.getUser2Id());
    assertThat(isInRelationShip, is(true));
  }

  /*
   * Test get RelationShipById
   */
  public void testGetRelationShipById() throws Exception {
    IDatabaseConnection connexion = getConnection();

    RelationShip expectedRelationShip = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY, 1,
        10, 34, 15), 2);
    expectedRelationShip.setId(1);
    RelationShip dbrelationShip = dao.getRelationShip(connexion.getConnection(),
        expectedRelationShip.getId());
    assertNotNull("RelationShip not found in db", dbrelationShip);
    assertEquals("RelationShip in db not as expected", expectedRelationShip, dbrelationShip);
    boolean isInRelationShip = dao.isInRelationShip(connexion.getConnection(),
        expectedRelationShip.getUser1Id(), expectedRelationShip.getUser2Id());
    assertTrue("must be true", isInRelationShip);
  }

  /*
   * Test get All my RelationShip sent
   */
  public void testGetAllMyRelationShips() throws Exception {
    IDatabaseConnection connexion = getConnection();

    RelationShip expectedRelationShip1 = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 2);
    expectedRelationShip1.setId(1);
    RelationShip expectedRelationShip2 =
        new RelationShip(1, 3, 0, toDate(2010, Calendar.MAY, 11, 15, 25, 32), 3);
    expectedRelationShip2.setId(4);
    int myId = 1;
    List<RelationShip> relationShips = dao.getAllMyRelationShips(connexion.getConnection(), myId);
    assertNotNull("Relationships should exist", relationShips);
    assertEquals("Should have 2 relationships in db", 2, relationShips.size());
    assertEquals("First should be 1 et 2", expectedRelationShip1, relationShips.get(0));
    assertEquals("Second should be 1 et 3", expectedRelationShip2, relationShips.get(1));

    Date begin = DateUtil.parse("2010/12/31");
    Date end = DateUtil.parse("2010/01/01");

    List<SocialInformation> listSIR = dao.getAllMyRelationShips(connexion.
        getConnection(), myId + "", begin, end);
    assertNotNull("Relationships should exist", listSIR);

  }

  private Date toDate(int year, int month, int day, int hour, int minute, int second) {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.YEAR, year);
    calend.set(Calendar.MONTH, month);
    calend.set(Calendar.DAY_OF_MONTH, day);
    calend.set(Calendar.HOUR_OF_DAY, hour);
    calend.set(Calendar.MINUTE, minute);
    calend.set(Calendar.SECOND, second);
    calend.set(Calendar.MILLISECOND, 0);
    return calend.getTime();
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
