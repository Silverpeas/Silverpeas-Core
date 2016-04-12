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
package com.silverpeas.social.status;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.silverpeas.components.model.AbstractTestDao;
import org.silverpeas.core.socialnetwork.status.Status;
import org.silverpeas.core.socialnetwork.status.StatusDao;

import org.silverpeas.util.DBUtil;

import org.dbunit.database.IDatabaseConnection;
import org.junit.Test;

public class TestSatusDao extends AbstractTestDao {

  private StatusDao dao;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    dao = new org.silverpeas.core.socialnetwork.status.StatusDao();
    DBUtil.getInstanceForTest(getConnection().getConnection());
  }


  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    DBUtil.clearTestInstance();
  }

  @Test
  public void testChangeStatus() throws Exception {
    IDatabaseConnection connexion = null;
    Status status = new Status(1, new Date(), "je teste");

    try {
      connexion = getConnection();
      int id = dao.changeStatus(connexion.getConnection(), status);
      assertNotNull("Status should have been created", id);
      assertTrue("New id is correct", id > 0);
      status.setId(id);
      Status nouveauStatus = dao.getStatus(connexion.getConnection(), id);
      assertNotNull("Status not found in db", nouveauStatus);
      assertEquals("Status in db not as expected", status, nouveauStatus);
    } finally {
      closeConnection(connexion);
    }

  }

  public void testGetStatus() throws Exception {
    IDatabaseConnection connexion = null;
    Status status = new Status(1, toDate(2010, Calendar.FEBRUARY, 1, 10, 34, 15), "je suis là");
    int id = 1;
    status.setId(id);
    try {
      connexion = getConnection();
      Status dbStatus = dao.getStatus(connexion.getConnection(), id);
      assertNotNull("Status not found in db", dbStatus);
      assertEquals("Status in db not as expected", status, dbStatus);

    } finally {
      closeConnection(connexion);
    }
  }

  public void testGetLastStatus() throws Exception {
    IDatabaseConnection connexion = null;
    Status status = new Status(1, toDate(2010, Calendar.JULY, 2, 10, 33, 10),
        "travaille sur readmine");
    status.setId(4);

    int userid = 1;

    try {
      connexion = getConnection();
      Status lastStatus = dao.getLastStatus(connexion.getConnection(), userid);
      assertNotNull("Status not found in db", lastStatus);
      assertEquals(status.getId(), lastStatus.getId());
      assertEquals("Status in db not as expected", status, lastStatus);

    } finally {
      closeConnection(connexion);
    }

  }

  public void testDeleteStatus() throws Exception {
    IDatabaseConnection connexion = null;
    Status expectedStatus = new Status(2, toDate(2010, Calendar.MAY, 11, 15, 25, 32), "congé");
    expectedStatus.setId(2);
    try {
      connexion = getConnection();
      Status status = dao.getStatus(connexion.getConnection(), 2);
      assertNotNull("Status should exist", status);
      dao.deleteStatus(connexion.getConnection(), 2);
      status = dao.getStatus(connexion.getConnection(), 2);
      assertNull("Status should no longer exist", status);
    } finally {
      closeConnection(connexion);
    }

  }

  public void testUpdateStatus() throws Exception {
    IDatabaseConnection connexion = null;
    Status updateStatus = new Status(2, toDate(2010, Calendar.MAY, 11, 15, 25, 32), "malade");
    updateStatus.setId(3);
    try {
      connexion = getConnection();
      Status status = dao.getStatus(connexion.getConnection(), 3);
      assertNotNull("Status should exist", status);
      dao.updateStatus(connexion.getConnection(), updateStatus);
      status = dao.getStatus(connexion.getConnection(), 3);
      assertEquals(status, updateStatus);

    } finally {
      closeConnection(connexion);
    }

  }

  @Override
  protected String getDatasetFileName() {
    return "socialNetwork_Status-dataset.xml";
  }

  private Date toDate(int year, int month, int day, int hour, int minute, int second) {
    GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
    return calendar.getTime();

  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
