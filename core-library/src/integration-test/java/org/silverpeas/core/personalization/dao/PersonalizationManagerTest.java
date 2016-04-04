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

package org.silverpeas.core.personalization.dao;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PersonalizationManagerTest {

  @Inject
  private PersonalizationManager dao;

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
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(PersonalizationManagerTest.class)
        .addJpaPersistenceFeatures()
        .testFocusedOn(
            (warBuilder) -> warBuilder.addPackages(true, "org.silverpeas.core.personalization"))
        .build();
  }

  @Test
  public void testGetPersonalizedDetail() throws Exception {
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
  public void testFindByDefaultSpace() throws Exception {
    String spaceId = "WA26";
    List<UserPreferences> userPreferencesList = dao.findByDefaultSpace(spaceId);
    assertThat(userPreferencesList, hasSize(2));
    assertThat(
        userPreferencesList.stream().map(UserPreferences::getId).collect(Collectors.toList()),
        containsInAnyOrder("1010", "2020"));
  }

  @Test
  public void testInsertPersonalizeDetail() throws Exception {
    String userId = "1020";

    // Verifying that the user does not exist
    assertThat(dao.getById(userId), nullValue());

    final UserPreferences expectedDetail_1020 =
        new UserPreferences(userId, "fr", "Test", "WA500", false, false, false,
            UserMenuDisplay.BOOKMARKS);

    Transaction.performInOne(() -> dao.save(expectedDetail_1020));

    UserPreferences detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail_1020));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));

    userId = "1030";
    final UserPreferences expectedDetail_1030 =
        new UserPreferences(userId, "en", "Silverpeas", "WA26", true, false, false,
            UserMenuDisplay.DISABLE);

    Transaction.performInOne(() -> dao.save(expectedDetail_1030));

    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail_1030));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));

    userId = "1040";
    final UserPreferences expectedDetail_1040 =
        new UserPreferences(userId, "de", "Silverpeas_V", "WA38", false, true, false,
            UserMenuDisplay.ALL);

    Transaction.performInOne(() -> dao.save(expectedDetail_1040));

    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail_1040));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));

    userId = "1050";
    final UserPreferences expectedDetail_1050 =
        new UserPreferences(userId, "dl", "Silverpeas_V6", "WA38", false, false, true,
            UserMenuDisplay.DEFAULT);

    Transaction.performInOne(() -> dao.save(expectedDetail_1050));

    detail = dao.getById(userId);
    assertThat(detail, notNullValue());
    assertThat(detail, PersonalizationMatcher.matches(expectedDetail_1050));
    assertThat(detail, PersonalizationMatcher.matches(actualUserPreferencesForUserId(userId)));
  }

  @Test
  public void testUpdatePersonalizeDetail() throws Exception {
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
  public void testDeletePersonalizeDetail() throws Exception {
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
      return JdbcSqlQuery.createSelect(
          "id, languages, look, personalWSpace, thesaurusStatus, dragAndDropStatus, " +
              "webdavEditingStatus, menuDisplay FROM personalization where " +
              "id = ?", userId).executeUnique(
          row -> new UserPreferences(row.getString(1), row.getString(2), row.getString(3),
              row.getString(4), row.getBoolean(5), row.getBoolean(6), row.getBoolean(7),
              UserMenuDisplay.valueOf(row.getString(8))));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
