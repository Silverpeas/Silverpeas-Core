/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.personalization.control.ejb;

import javax.sql.DataSource;
import com.silverpeas.personalization.dao.PersonalizationDetailDaoTest;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class PersonalizationBmBusinessSkeletonTest {

  private static PersonalizationBmBusinessSkeleton service;

  public PersonalizationBmBusinessSkeletonTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
        "spring-personalization.xml");
    service = (PersonalizationBmBusinessSkeleton) context.getBean("personalizationService");
    DataSource ds = (DataSource) context.getBean("dataSource");
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        PersonalizationDetailDaoTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/personalization/dao/personalization-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
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
    assertThat(service.getLanguages("1000"), is("fr"));
    assertThat(service.getLanguages("1010"), is("en"));
    assertThat(service.getLanguages("5000"), is("fr"));
  }

  @Test
  public void testSetLanguages() {
    assertThat(service.getLanguages("1000"), is("fr"));
    service.setLanguages("1000", "en");
    assertThat(service.getLanguages("1000"), is("en"));
    assertThat(service.getLanguages("1030"), is("fr"));
    service.setLanguages("1030", "de");
    assertThat(service.getLanguages("1030"), is("de"));
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
  /*
  
  
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
  }*/
}
