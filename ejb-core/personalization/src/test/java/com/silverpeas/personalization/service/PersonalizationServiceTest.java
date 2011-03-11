/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.personalization.service;

import javax.sql.DataSource;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.dao.PersonalizationDetailDaoTest;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class PersonalizationServiceTest {

  private static PersonalizationService service;
  private static DataSource ds;

  public PersonalizationServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
        "spring-personalization.xml");
    service = (PersonalizationService) context.getBean("personalizationService");
    ds = (DataSource) context.getBean("dataSource");
    cleanDatabase();
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
  public void testGetDragAndDropStatus() {
    assertThat(service.getDragAndDropStatus("1000"), is(true));
    assertThat(service.getDragAndDropStatus("1010"), is(true));
    assertThat(service.getDragAndDropStatus("5000"), is(false));
  }

  @Test
  public void testSetDragAndDropStatus() {
    assertThat(service.getDragAndDropStatus("1000"), is(true));
    service.setDragAndDropStatus("1000", false);
    assertThat(service.getDragAndDropStatus("1000"), is(false));
    assertThat(service.getDragAndDropStatus("1030"), is(false));
    service.setDragAndDropStatus("1030", true);
    assertThat(service.getDragAndDropStatus("1030"), is(true));
  }

  @Test
  public void testGetFavoriteLook() {
    assertThat(service.getFavoriteLook("1000"), is("Initial"));
    assertThat(service.getFavoriteLook("1010"), is("Silverpeas"));
    assertThat(service.getFavoriteLook("5000"), is("Initial"));
  }

  @Test
  public void testSetFavoriteLook() {
    assertThat(service.getFavoriteLook("1000"), is("Initial"));
    service.setFavoriteLook("1000", "SilverpeasV5");
    assertThat(service.getFavoriteLook("1000"), is("SilverpeasV5"));
    assertThat(service.getFavoriteLook("1030"), is("Initial"));
    service.setFavoriteLook("1030", "SilverpeasV5");
    assertThat(service.getFavoriteLook("1030"), is("SilverpeasV5"));
  }

  @Test
  public void testGetLanguages() {
    assertThat(service.getFavoriteLanguage("1000"), is("fr"));
    assertThat(service.getFavoriteLanguage("1010"), is("en"));
    assertThat(service.getFavoriteLanguage("5000"), is("fr"));
  }

  @Test
  public void testSetLanguages() {
    assertThat(service.getFavoriteLanguage("1000"), is("fr"));
    service.setFavoriteLanguage("1000", "en");
    assertThat(service.getFavoriteLanguage("1000"), is("en"));
    assertThat(service.getFavoriteLanguage("1030"), is("fr"));
    service.setFavoriteLanguage("1030", "de");
    assertThat(service.getFavoriteLanguage("1030"), is("de"));
  }

  @Test
  public void testGetOnlineEditingStatus() {
    assertThat(service.getOnlineEditingStatus("1000"), is(true));
    assertThat(service.getOnlineEditingStatus("1010"), is(false));
    assertThat(service.getOnlineEditingStatus("5000"), is(false));
  }

  @Test
  public void testSetOnlineEditingStatus() {
    assertThat(service.getOnlineEditingStatus("1000"), is(true));
    service.setOnlineEditingStatus("1000", false);
    assertThat(service.getOnlineEditingStatus("1000"), is(false));
    assertThat(service.getOnlineEditingStatus("1030"), is(false));
    service.setOnlineEditingStatus("1030", true);
    assertThat(service.getOnlineEditingStatus("1030"), is(true));
  }

  @Test
  public void testGetPersonalWorkSpace() {
    assertThat(service.getPersonalWorkSpace("1000"), is(""));
    assertThat(service.getPersonalWorkSpace("1010"), is("WA47"));
    assertThat(service.getPersonalWorkSpace("5000"), is(""));
  }

  @Test
  public void testSetPersonalWorkSpace() {
    assertThat(service.getPersonalWorkSpace("1000"), is(""));
    service.setPersonalWorkSpace("1000", "WA51");
    assertThat(service.getPersonalWorkSpace("1000"), is("WA51"));
    assertThat(service.getPersonalWorkSpace("1030"), is(""));
    service.setPersonalWorkSpace("1030", "WA34");
    assertThat(service.getPersonalWorkSpace("1030"), is("WA34"));
  }
  
  
  @Test
  public void testGetThesaurusStatus() {
    assertThat(service.getThesaurusStatus("1000"), is(false));
    assertThat(service.getThesaurusStatus("1010"), is(false));
    assertThat(service.getThesaurusStatus("5000"), is(false));
  }

  @Test
  public void testSetThesaurusStatus() {
    assertThat(service.getThesaurusStatus("1000"), is(false));
    service.setThesaurusStatus("1000", true);
    assertThat(service.getThesaurusStatus("1000"), is(true));
    assertThat(service.getThesaurusStatus("1030"), is(false));
    service.setThesaurusStatus("1030", true);
    assertThat(service.getThesaurusStatus("1030"), is(true));
  }
  
  
  @Test
  public void testGetWebdavEditingStatus() {
    assertThat(service.getWebdavEditingStatus("1000"), is(true));
    assertThat(service.getWebdavEditingStatus("1010"), is(true));
    assertThat(service.getWebdavEditingStatus("5000"), is(true));
  }

  @Test
  public void testSetWebdavEditingStatus() {
    assertThat(service.getWebdavEditingStatus("1000"), is(true));
    service.setWebdavEditingStatus("1000", false);
    assertThat(service.getWebdavEditingStatus("1000"), is(false));
    assertThat(service.getWebdavEditingStatus("1030"), is(true));
    service.setWebdavEditingStatus("1030", false);
    assertThat(service.getWebdavEditingStatus("1030"), is(false));
  }

  @Test
  public void testGetUserSettings() throws Exception {
    String userId = "1000";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false,
        true, true, true);
    UserPreferences detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));

    userId = "1010";
    detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    expectedDetail = new UserPreferences(userId, "en", "Silverpeas", "WA47", false, true, false,
        true);
    assertThat(detail, is(expectedDetail));
  }

  @Test
  public void testInsertPersonalizeDetail() throws Exception {
    String userId = "1020";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Test", "WA500", false,
        false, false, false);
    service.saveUserSettings(expectedDetail);
    UserPreferences detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, is(expectedDetail));
  }
}
