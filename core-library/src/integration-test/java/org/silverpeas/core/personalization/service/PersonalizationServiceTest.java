/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.personalization.service;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.dao.PersonalizationMatcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PersonalizationServiceTest {

  private PersonalizationService service;

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  public static final Operation TABLES_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS Personalization (" +
          "id varchar(100) PRIMARY KEY NOT NULL, languages varchar(100) NULL, " +
          "look varchar(50) NULL, personalWSpace varchar(50) NULL, " +
          "thesaurusStatus int NOT NULL, dragAndDropStatus int DEFAULT 1, " +
          "onlineEditingStatus int DEFAULT 1, webdavEditingStatus int DEFAULT 0, " +
          "menuDisplay varchar(50) DEFAULT 'DISABLE')");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("Personalization");
  public static final Operation USER_PREFERENCE_SET_UP = Operations.insertInto("Personalization")
      .columns("id", "languages", "look", "personalwspace", "thesaurusstatus", "draganddropstatus",
          "webdaveditingstatus", "menuDisplay")
      .values("1000", "fr", "Initial", "", 0, 1, 1, "DISABLE")
      .values("1010", "en", "Silverpeas", "WA26", 0, 1, 1, "ALL")
      .values("2020", "de", "Silverpeas_V6", "WA26", 1, 0, 1, "BOOKMARKS").build();

  @Before
  public void prepareDataSource() {
    Operation preparation =
        Operations.sequenceOf(TABLES_CREATION, CLEAN_UP, USER_PREFERENCE_SET_UP);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);

    service = ServiceProvider.getService(PersonalizationService.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(PersonalizationServiceTest.class)
        .addJpaPersistenceFeatures()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.personalization");
          warBuilder.addAsResource(
              "org/silverpeas/personalization/settings/personalizationPeasSettings" +
                  ".properties");
            })
        .build();
  }


  @Test
  public void testGetUserSettings() throws Exception {
    String userId = "1000";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false,
        true, true, UserMenuDisplay.DISABLE);
    UserPreferences detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));

    userId = "1010";
    detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    expectedDetail = new UserPreferences(userId, "en", "Silverpeas", "WA26", false, true, true,
        UserMenuDisplay.ALL);
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));

    userId = "5000";
    detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    expectedDetail = new UserPreferences(userId, "fr", "Initial", "", false, true, true,
        UserMenuDisplay.DEFAULT);
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));
  }

  @Test
  public void testInsertPersonalizeDetail() throws Exception {
    String userId = "1020";
    UserPreferences expectedDetail = new UserPreferences(userId, "fr", "Test", "WA500", false,
        false, false, UserMenuDisplay.BOOKMARKS);
    service.saveUserSettings(expectedDetail);
    UserPreferences detail = service.getUserSettings(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));
  }
}
