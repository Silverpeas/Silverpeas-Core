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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.personalization.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.dao.PersonalizationDetailDaoTest;

/**
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-personalization.xml", "/spring-personalization-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class PersonalizationServiceTest {

  @Inject
  private PersonalizationService service;
  @Inject
  private DataSource ds;

  public PersonalizationServiceTest() {
  }

  @Before
  public void setUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        PersonalizationDetailDaoTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/personalization/dao/personalization-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(ds.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }


  @Test  
  @Transactional
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
    expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false, true, true,
        UserMenuDisplay.DEFAULT);
    assertThat(detail, is(expectedDetail));
  }

  @Test
  @Transactional
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
