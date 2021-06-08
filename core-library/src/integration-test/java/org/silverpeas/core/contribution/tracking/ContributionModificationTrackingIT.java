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
import org.silverpeas.core.contribution.ContributionEventProcessor;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests on the tracking system about the contribution modifications through the
 * handling of publications
 * ({@link org.silverpeas.core.contribution.publication.model.PublicationDetail}
 * is annotated with the {@link ModificationTracked} annotation and hence the publications are
 * elective to the modification tracking).
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ContributionModificationTrackingIT {

  @Inject
  PublicationService publicationService;

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
            ContributionOperationContextPropertyHandler.class, ContributionEventProcessor.class)
        .addPackages(true, "org.silverpeas.core.io.media.image.thumbnail")
        .addPackages(true, "org.silverpeas.core.contribution.tracking")
        .addAsResource("org/silverpeas/contribution")
        .build();
  }

  @Before
  public void setUp() {
    assertThat(publicationService, notNullValue());
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
  public void aPublicationHasAMajorUpdate() {
    Date now = new Date();
    PublicationDetail publication = context.getPublication("100");
    publication.setUpdateDate(now);
    publication.setUpdaterId(User.getCurrentRequester().getId());

    publicationService.setDetail(publication, true);

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.MAJOR_UPDATE, publication, now);
  }

  @Test
  public void aPublicationHasAMinorUpdate() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("CONTRIBUTION_MODIFICATION_CONTEXT")).thenReturn("{\"isMinor\": true}");
    modificationContextHandler.parseForProperty(request);

    PublicationDetail publication = context.getPublication("100");
    Date previous = publication.getLastUpdateDate();
    publication.setUpdateDate(new Date());
    publication.setUpdaterId(User.getCurrentRequester().getId());

    publicationService.setDetail(publication, true);

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.MINOR_UPDATE, publication, previous);
  }
}
