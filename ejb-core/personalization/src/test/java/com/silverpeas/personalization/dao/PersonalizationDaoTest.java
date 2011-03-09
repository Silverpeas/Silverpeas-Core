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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.personalization.dao;

import java.util.Vector;
import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.stratelia.webactiv.personalization.control.ejb.JdbcPersonalizationDao;
import com.stratelia.webactiv.personalization.model.PersonalizeDetail;
import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.IDatabaseConnection;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
public class PersonalizationDaoTest extends AbstractJndiCase {

  private PersonalizationDao dao = new JdbcPersonalizationDao();

  public PersonalizationDaoTest() {
  }

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase(
        "com/silverpeas/personalization/dao/personalization-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }

  @Test
  public void testGetPersonalizedDetail() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Initial", "", false, true,
        true, true);
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, "1000");
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));


    detail = dao.getPersonalizeDetail(con, "1010");
    assertThat(detail, notNullValue());
    expectedDetail = new PersonalizeDetail("en", "Silverpeas", "WA47", false, true, false, true);
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  @Test
  public void testInsertPersonalizeDetail() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Test", "WA500", false, false,
        false, false);
    dao.insertPersonalizeDetail(con, "1020", "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, "1020");
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  @Test
  public void testSetDragAndDropStatus() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1030";
    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Test", "WA500", false, true,
        false, false);
    dao.setDragAndDropStatus(con, userId, true);
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  @Test
  public void testSetFavoriteLook() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1040";

    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Favoritelook", "WA500", false, false,
        false, false);
    dao.setFavoriteLook(con, userId, "Favoritelook");
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  
  @Test
  public void testSetLanguages() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1040";
    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("en", "Test", "WA500", false, false,
        false, false);
    dao.setLanguage(con, userId, "en");
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  
  @Test
  public void testSetOnlineEditingStatus() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1040";
    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Test", "WA500", false, false,
        true, false);
    dao.setOnlineEditingStatus(con, userId, true);
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  @Test
  public void testSetPersonalWorkSpace() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1040";
    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Test", "WA100", false, false,
        false, false);
    dao.setPersonalWorkSpace(con, userId, "WA100");
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  
  
  @Test
  public void testThesaurusStatus() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1040";
    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Test", "WA500", true, false,
        false, false);
    dao.setThesaurusStatus(con, userId, true);
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  @Test
  public void testWebdavEditingStatus() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstance(con);
    String userId = "1040";
    dao.insertPersonalizeDetail(con, userId, "fr", "Test", "WA500", false, false,
        false, false);
    PersonalizeDetail expectedDetail = new PersonalizeDetail("fr", "Test", "WA500", false, false,
        false, true);
    dao.setWebdavEditingStatus(con, userId, true);
    PersonalizeDetail detail = dao.getPersonalizeDetail(con, userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
}
