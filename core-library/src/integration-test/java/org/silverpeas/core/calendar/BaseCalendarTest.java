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
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.core.calendar;

import org.junit.After;
import org.junit.Before;
import org.silverpeas.core.admin.service.DefaultOrganizationController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.DataSetTest;
import org.silverpeas.core.test.rule.DbSetupRule.TableLine;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.List;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for tests in the calendar engine.
 * It prepares the database to use in tests.
 */
public abstract class BaseCalendarTest extends DataSetTest {

  static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/calendar/create_table_calendar.sql";

  protected static final String INSTANCE_ID = "anInstanceId";

  private User mockedUser;

  @Override
  protected String getDbSetupTableCreationSqlScript() {
    return TABLE_CREATION_SCRIPT;
  }

  @Override
  protected <T> T getDbSetupInitializations() {
    return null;
  }

  @Before
  public void setUp() throws Exception {
    mockedUser = mock(User.class);
    when(mockedUser.getId()).thenReturn("26");
    CacheServiceProvider.clearAllThreadCaches();
    ((SessionCacheService) CacheServiceProvider.getSessionCacheService())
        .newSessionCache(mockedUser);
  }

  @After
  public void tearDown() throws Exception {
    CacheServiceProvider.clearAllThreadCaches();
  }

  protected User getMockedUser() {
    return mockedUser;
  }

  protected OrganizationController getOrganisationControllerMock() {
    return StubbedOrganizationController.getMock();
  }

  /**
   * Returns the list of calendar lines persisted into sb_calendar table.
   * @return List of lines represented by a map between column name and value.
   */
  protected List<TableLine> getCalendarTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from sb_calendar")
            .addSqlPart("order by instanceid, title, id"));
  }

  /**
   * Returns the calendar persisted into sb_calendar table corresponding to the given identifier.
   * @param id the id searched for.
   * @return List of lines represented by a map between column name and value.
   */
  protected TableLine getCalendarTableLineById(String id) throws Exception {
    return JdbcSqlQuery.unique(getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from sb_calendar")
            .where("id = ?", id)
            .addSqlPart("order by instanceid, title, id")));
  }

  /**
   * @author Yohann Chastagnier
   */
  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedOrganizationController extends DefaultOrganizationController {

    private OrganizationController mock = mock(OrganizationController.class);

    static OrganizationController getMock() {
      return ((StubbedOrganizationController) ServiceProvider
          .getService(OrganizationController.class)).mock;
    }

    @Override
    public UserDetail getUserDetail(final String sUserId) {
      return mock.getUserDetail(sUserId);
    }

    @Override
    public String[] getUserProfiles(final String userId, final String componentId) {
      return mock.getUserProfiles(userId, componentId);
    }
  }
}
