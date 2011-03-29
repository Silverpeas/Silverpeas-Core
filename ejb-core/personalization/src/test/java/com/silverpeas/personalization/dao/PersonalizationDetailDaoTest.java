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

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.IOException;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.AfterClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
public class PersonalizationDetailDaoTest {

  private static PersonalizationDetailDao dao;
  private static ClassPathXmlApplicationContext context;
  
  public PersonalizationDetailDaoTest() {
  }

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    context = new ClassPathXmlApplicationContext("spring-personalization.xml");
    dao = (PersonalizationDetailDao) context.getBean("personalizationDetailDao");
    DataSource ds = (DataSource) context.getBean("dataSource");
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
    PersonalizationDetailDaoTest.class.getClassLoader().getResourceAsStream(
    "com/silverpeas/personalization/dao/personalization-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet); 
  }
  
   @AfterClass
  public static void tearDownClass() throws Exception {
    context.close();
  }

  @Test
  public void testGetPersonalizedDetail() throws Exception {
    String userId = "1000";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false,
        true, true, UserMenuDisplay.DISABLE);
    UserPreferences detail = dao.readByPrimaryKey(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));

    userId = "1010";
    detail = dao.readByPrimaryKey(userId);
    assertThat(detail, notNullValue());
    expectedDetail = new UserPreferences(userId, "en", "Silverpeas", "WA47", false, true, true,  UserMenuDisplay.ALL);
    assertThat(detail, is(expectedDetail));
  }

  @Test
  public void testInsertPersonalizeDetail() throws Exception {
    UserPreferences expectedDetail = new UserPreferences("1020", "fr", "Test", "WA500", false,
        false, false, UserMenuDisplay.BOOKMARKS);
    dao.save(expectedDetail);
    UserPreferences detail = dao.readByPrimaryKey("1020");
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
  }
}
