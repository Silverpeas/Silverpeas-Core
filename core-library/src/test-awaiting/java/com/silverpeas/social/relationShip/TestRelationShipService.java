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
import org.silverpeas.core.socialnetwork.relationShip.RelationShip;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipService;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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

  /**
   * Test remove RelationShip
   */
  @Test
  public void testRemoveRelationShip() throws Exception {
    RelationShip expectedRelationShip1 = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 2);
    RelationShip expectedRelationShip2 = new RelationShip(2, 1, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 1);
    expectedRelationShip1.setId(1);
    expectedRelationShip2.setId(2);
    RelationShip relationShip1 = dao.getRelationShip(1, 2);
    assertThat(relationShip1, is(notNullValue()));
    assertThat(relationShip1, is(expectedRelationShip1));
    RelationShip relationShip2 = dao.getRelationShip(2, 1);
    assertThat(relationShip2, is(notNullValue()));
    assertThat(relationShip2, is(expectedRelationShip2));
    dao.removeRelationShip(1, 2);
    relationShip1 = dao.getRelationShip(1, 2);
    assertThat(relationShip1, is(nullValue()));
    relationShip2 = dao.getRelationShip(2, 1);
    assertThat(relationShip2, is(nullValue()));

  }

  /**
   * Test get RelationShip
   */
  public void testGetRelationShip() throws Exception {
    RelationShip expectedRelationShip = new RelationShip(1, 2, 0,
        toDate(2010, Calendar.FEBRUARY, 01, 10, 34, 15), 2);
    expectedRelationShip.setId(1);
    RelationShip dbrelationShip = dao.getRelationShip(expectedRelationShip.getUser1Id(),
        expectedRelationShip.getUser2Id());
    assertThat(dbrelationShip, is(notNullValue()));
    assertThat(dbrelationShip, is(expectedRelationShip));
  }

  /**
   * Test get All my RelationShip sent
   */
  public void testGetAllMyRelationShips() throws Exception {

    RelationShip expectedRelationShip1 = new RelationShip(1, 2, 0, toDate(2010, Calendar.FEBRUARY,
        1, 10, 34, 15), 2);
    expectedRelationShip1.setId(1);
    RelationShip expectedRelationShip2 = new RelationShip(1, 3, 0,
        toDate(2010, Calendar.MAY, 11, 15, 25, 32), 3);
    expectedRelationShip2.setId(4);
    int myId = 1;
    List<RelationShip> relationShips = dao.getAllMyRelationShips(myId);
    assertThat(relationShips, is(notNullValue()));
    assertThat(relationShips, hasSize(2));
    assertThat(relationShips, contains(expectedRelationShip1, expectedRelationShip2));

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
