/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.tracking;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.contribution.tracking.TestContext.TrackingEventRecord;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests on the operations provided by the {@link ContributionTrackingService} object in
 * order to check the tracking event are correctly saved in the database.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ContributionTrackingServiceIT {

  @Inject
  ContributionTrackingService trackingService;

  @Inject
  ContributionModificationContextHandler modificationContextHandler;

  TestContext context = new TestContext();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TestContext.TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(TestContext.DATASET_SQL_SCRIPT);

  @Deployment
  public static Archive<WebArchive> createDeployment() {
    return WarBuilder4Publication.onWarForTestClass(ContributionTrackingServiceIT.class)
        .addClasses(ContributionModificationContextHandler.class,
            ContributionOperationContextPropertyHandler.class)
        .addPackages(true, "org.silverpeas.core.contribution.tracking")
        .addAsResource("org/silverpeas/contribution")
        .build();
  }

  @Before
  public void setUp() {
    assertThat(trackingService, notNullValue());
    assertThat(modificationContextHandler, notNullValue());

    SessionCacheService sessionCacheService =
        (SessionCacheService) CacheServiceProvider.getSessionCacheService();
    User currentUser = User.getById("1");
    sessionCacheService.newSessionCache(currentUser);
  }

  @After
  public void cleanUp() {
    CacheServiceProvider.clearAllThreadCaches();
  }

  @Test
  public void saveAMajorModification() {
    Date now = new Date();
    PublicationDetail publication = context.getPublication("100");
    PublicationDetail updated = publication.copy();
    updated.setUpdateDate(now);
    updated.setUpdaterId("1");

    withinTransaction(() -> trackingService.update(publication, updated));

    TrackingEventRecord event = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(event, TrackedActionType.MAJOR_UPDATE, updated, now);
  }

  @Test
  public void saveAMinorModification() {
    Date now = new Date();
    PublicationDetail publication = context.getPublication("100");
    PublicationDetail updated = publication.copy();
    updated.setUpdateDate(now);
    updated.setUpdaterId("1");

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("CONTRIBUTION_MODIFICATION_CONTEXT")).thenReturn("{\"isMinor\": true}");
    modificationContextHandler.parseForProperty(request);

    withinTransaction(() -> trackingService.update(publication, updated));

    TrackingEventRecord event = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(event, TrackedActionType.MINOR_UPDATE, updated, now);
  }

  @Test
  public void saveADeletion() {
    Date now = new Date();
    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> trackingService.delete(publication));

    TrackingEventRecord event = context.getLastTrackingEventOn(publication.getId());
    assertThat(event, notNullValue());
    assertThat(event.contrib.getLocalId(), is(publication.getId()));
    assertThat(event.contrib.getType(), is(publication.getContributionType()));
    assertThat(event.contrib.getComponentInstanceId(), is(publication.getInstanceId()));
    assertThat(event.action.getType(), is(TrackedActionType.DELETION));
    assertThat(event.action.getUser(), is(User.getCurrentRequester()));
    // TODO once hamcrest-date is available: use it to compare two non-accurate dates
    //assertThat(event.action.getDateTime().toInstant(), is(now.toInstant()));
  }

  private void withinTransaction(final Runnable runnable) {
    Transaction.performInOne(() -> {
      runnable.run();
      return null;
    });
  }
}
