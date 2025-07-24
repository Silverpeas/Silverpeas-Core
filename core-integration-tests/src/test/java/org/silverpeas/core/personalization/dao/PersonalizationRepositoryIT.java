/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.personalization.dao;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.test.WarBuilder4LibCore;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PersonalizationRepositoryIT {

  @Inject
  private PersonalizationRepository dao;

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private final DbSetupTracker dbSetupTracker = new DbSetupTracker();

  public static final Operation TABLES_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS Personalization (" +
          "id varchar(100) PRIMARY KEY NOT NULL, languages varchar(100) NULL, " +
          "zoneId varchar(100) NULL, " +
          "look varchar(50) NULL, personalWSpace varchar(50) NULL, " +
          "thesaurusStatus int NOT NULL, dragAndDropStatus int DEFAULT 1, " +
          "onlineEditingStatus int DEFAULT 1, webdavEditingStatus int DEFAULT 0, " +
          "menuDisplay varchar(50) DEFAULT 'DISABLE')");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("Personalization");
  public static final Operation USER_PREFERENCE_SET_UP = Operations.insertInto("Personalization")
      .columns("id", "languages", "zoneId", "look", "personalwspace", "thesaurusstatus", "draganddropstatus",
          "webdaveditingstatus", "menuDisplay")
      .values("1000", "fr", "Europe/Paris", "Initial", "", 0, 1, 1, "DISABLE")
      .values("1010", "en", "UTC", "Silverpeas", "WA26", 0, 1, 1, "ALL")
      .values("2020", "de", "Europe/Berlin", "Silverpeas_V6", "WA26", 1, 0, 1, "BOOKMARKS").build();

  @Before
  public void prepareDataSource() {
    Operation preparation =
        Operations.sequenceOf(TABLES_CREATION, CLEAN_UP, USER_PREFERENCE_SET_UP);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(PersonalizationRepositoryIT.class)
        .enableAdministrationFeatures()
        .testFocusedOn(
            warBuilder -> warBuilder.addPackages(true, "org.silverpeas.core.personalization"))
        .build();
  }

  @Test
  public void testGetPersonalizedDetail() {
    String userId = "1000";
    UserPreferences expectedDetail = actualUserPreferencesForUserId(userId);
    UserPreferences detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));

    userId = "1010";
    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    expectedDetail = actualUserPreferencesForUserId(userId);
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));

    userId = "1020";
    detail = dao.getById(userId);
    assertThat(detail, nullValue());
    assertThat(detail, nullValue());

    userId = "2020";
    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    expectedDetail = actualUserPreferencesForUserId(userId);
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));
  }

  @Test
  public void testFindByDefaultSpace() {
    String spaceId = "WA26";
    List<UserPreferences> userPreferencesList = dao.findByDefaultSpace(spaceId);
    assertThat(userPreferencesList, hasSize(2));
    assertThat(
        userPreferencesList.stream().map(UserPreferences::getId).collect(Collectors.toList()),
        containsInAnyOrder("1010", "2020"));
  }

  @Test
  public void testInsertPersonalizeDetail() {
    String userId = "1020";

    // Verifying that the user does not exist
    assertThat(dao.getById(userId), nullValue());

    final UserPreferences expectedDetail1020 =
        new UserPreferences(userId, "fr", ZoneId.of("Europe/London"));
    expectedDetail1020.setLook("Test");
    expectedDetail1020.setPersonalWorkSpaceId("WA500");
    expectedDetail1020.setDisplay(UserMenuDisplay.BOOKMARKS);
    expectedDetail1020.enableThesaurus(false);
    expectedDetail1020.enableDragAndDrop(false);
    expectedDetail1020.enableWebdavEdition(false);

    Transaction.performInOne(() -> dao.save(expectedDetail1020));

    UserPreferences detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail1020));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));

    userId = "1030";
    final UserPreferences expectedDetail1030 =
        new UserPreferences(userId, "en", ZoneId.of("UTC"));
    expectedDetail1030.setLook("Silverpeas");
    expectedDetail1030.setPersonalWorkSpaceId("WA26");
    expectedDetail1030.setDisplay(UserMenuDisplay.DISABLE);
    expectedDetail1030.enableThesaurus(true);
    expectedDetail1030.enableDragAndDrop(false);
    expectedDetail1030.enableWebdavEdition(false);

    Transaction.performInOne(() -> dao.save(expectedDetail1030));

    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail1030));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));

    userId = "1040";
    final UserPreferences expectedDetail1040 =
        new UserPreferences(userId, "de", ZoneId.of("Europe/Berlin"));
    expectedDetail1040.setLook("Silverpeas_V");
    expectedDetail1040.setPersonalWorkSpaceId("WA38");
    expectedDetail1040.setDisplay(UserMenuDisplay.ALL);
    expectedDetail1040.enableThesaurus(false);
    expectedDetail1040.enableDragAndDrop(true);
    expectedDetail1040.enableWebdavEdition(false);

    Transaction.performInOne(() -> dao.save(expectedDetail1040));

    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail1040));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));

    userId = "1050";
    final UserPreferences expectedDetail1050 =
        new UserPreferences(userId, "dl", ZoneId.of("Europe/Berlin"));
    expectedDetail1050.setLook("Silverpeas_V6");
    expectedDetail1050.setPersonalWorkSpaceId("WA38");
    expectedDetail1050.setDisplay(UserMenuDisplay.DEFAULT);
    expectedDetail1050.enableThesaurus(false);
    expectedDetail1050.enableDragAndDrop(false);
    expectedDetail1050.enableWebdavEdition(true);

    Transaction.performInOne(() -> dao.save(expectedDetail1050));

    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail1050));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));
  }

  @Test
  public void testUpdatePersonalizeDetail() {
    String userId = "1000";
    final UserPreferences expectedDetail = actualUserPreferencesForUserId(userId);
    UserPreferences detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));

    assertThat(expectedDetail.getLanguage(), is(not("DUMMY")));
    expectedDetail.setLanguage("DUMMY");

    Transaction.performInOne(() -> dao.save(expectedDetail));

    UserPreferences actual = actualUserPreferencesForUserId(userId);
    assertThat(actual, notNullValue());
    assertThat(actual, PersonalizationMatcher.matches(expectedDetail));
    assertThat(actual.getLanguage(), is("DUMMY"));
  }

  @Test
  public void testDeletePersonalizeDetail() {
    String userId = "1000";
    final UserPreferences expectedDetail = actualUserPreferencesForUserId(userId);
    UserPreferences detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail));

    Transaction.performInOne(() -> {
      dao.delete(expectedDetail);
      return null;
    });

    assertThat(dao.getById(userId), nullValue());
    assertThat(actualUserPreferencesForUserId(userId), nullValue());
  }

  private UserPreferences actualUserPreferencesForUserId(String userId) {
    try {
      return JdbcSqlQuery.select(
          "id, languages, zoneId, look, personalWSpace, thesaurusStatus, dragAndDropStatus, " +
              "webdavEditingStatus, menuDisplay")
          .from("personalization")
          .where("id = ?", userId).executeUnique(
          row -> {
            var preferences = new UserPreferences(row.getString(1), row.getString(2),
                ZoneId.of(row.getString(3)));
            preferences.setLook(row.getString(4));
            preferences.setPersonalWorkSpaceId(row.getString(5));
            preferences.setDisplay(UserMenuDisplay.valueOf(row.getString(9)));
            preferences.enableThesaurus(row.getBoolean(6));
            preferences.enableDragAndDrop(row.getBoolean(7));
            preferences.enableWebdavEdition(row.getBoolean(8));
            return preferences;
          });
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
