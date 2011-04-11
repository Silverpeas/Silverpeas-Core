/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.personalization.service;

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.dao.PersonalizationDetailDaoTest;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author ehugonnet
 */
public class PersonalizationServiceTest {

  private static PersonalizationService service;
  private static DataSource ds;
  private static ClassPathXmlApplicationContext context;

  public PersonalizationServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    context = new ClassPathXmlApplicationContext(
        "spring-personalization.xml");
    service = (PersonalizationService) context.getBean("personalizationService");
    ds = (DataSource) context.getBean("dataSource");
    cleanDatabase();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    context.close();
  }

  protected static void cleanDatabase() throws IOException, SQLException, DatabaseUnitException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        PersonalizationDetailDaoTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/personalization/dao/personalization-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  @Before
  public void setUp() throws Exception {
    cleanDatabase();
  }


  @Test
  public void testGetUserSettings() throws Exception {
    String userId = "1000";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false,
        true, true, UserMenuDisplay.DISABLE);
    UserPreferences detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));

    userId = "1010";
    detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    expectedDetail = new UserPreferences(userId, "en", "Silverpeas", "WA47", false, true, true,
        UserMenuDisplay.ALL);
    assertThat(detail, is(expectedDetail));

    userId = "5000";
    detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false, false, true,
        UserMenuDisplay.DEFAULT);
    assertThat(detail, is(expectedDetail));
  }

  @Test
  public void testInsertPersonalizeDetail() throws Exception {
    String userId = "1020";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Test", "WA500", false,
        false, false, UserMenuDisplay.BOOKMARKS);
    service.saveUserSettings(expectedDetail);
    UserPreferences detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
  }
}
