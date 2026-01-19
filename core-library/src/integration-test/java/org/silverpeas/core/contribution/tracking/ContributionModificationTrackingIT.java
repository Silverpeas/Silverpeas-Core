/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.tracking;

import jakarta.inject.Inject;
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
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.ContributionEventProcessor;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.kernel.util.SystemWrapper;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests on the tracking system about the contribution modifications through the
 * handling of ({@link Publication}s annotated with the {@link ModificationTracked} (id est the
 * publications are elective to the modification tracking).
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ContributionModificationTrackingIT {

  @Inject
  PublicationService publicationService;

  TestContext context = new TestContext();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TestContext.TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(TestContext.DATASET_SQL_SCRIPT);

  @Deployment
  public static Archive<WebArchive> createDeployment() {
    return LibCoreWarBuilder.onWarForTestClass(ContributionTrackingServiceIT.class)
        .addStubbedUserAPI()
        .addStubbedAppAPI()
        .addPackages(true, "org.silverpeas.core.contribution.tracking")
        .addClasses(PublicationRuntimeException.class,
            ContributionModificationContextHandler.class,
            ContributionOperationContextPropertyHandler.class,
            ContributionEventProcessor.class)
        .addAsResource("org/silverpeas/contribution")
        .build();
  }

  @Before
  public void setUp() throws Exception {
    assertThat(publicationService, notNullValue());

    File silverpeasHome = mavenTargetDirectoryRule.getResourceTestDirFile();
    SystemWrapper.getInstance().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
  }

  @After
  public void cleanUp() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  public void aPublicationHasAMajorUpdateWithExplicitUpdateDate() {
    context.setUpUserRequester();
    context.setUpModificationType(false);

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      Date now = new Date();
      publication.setLastUpdate(now, User.getCurrentRequester().getId());
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.MAJOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasAMinorUpdateWithExplicitUpdateDate() {
    context.setUpUserRequester();
    context.setUpModificationType(true);

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setLastUpdate(new Date(), User.getCurrentRequester().getId());
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.MINOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasAnUpdateWithExplicitUpdateDate() {
    context.setUpUserRequester();

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setLastUpdate(new Date(), User.getCurrentRequester().getId());
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.UPDATE, publication);
  }

  @Test
  public void aPublicationHasAMajorDefaultUpdate() {
    context.setUpUserRequester();
    context.setUpModificationType(false);

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setLastUpdate(publication.getLastUpdateDate(), "3");
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.MAJOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasAMinorDefaultUpdate() {
    context.setUpUserRequester();
    context.setUpModificationType(true);

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setLastUpdate(publication.getLastUpdateDate(), "3");
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.MINOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasADefaultUpdate() {
    context.setUpUserRequester();

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setLastUpdate(publication.getLastUpdateDate(),
          User.getCurrentRequester().getId());
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.UPDATE, publication);
  }

  @Test
  public void aPublicationIsUpdatedByABatchProcess() {
    // no requester behind a batch process
    // a publication update in a batch process is always a simple one

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setLastUpdate(publication.getLastUpdateDate(), "3");
      publicationService.updatePublication(publication);
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.UPDATE, publication);
  }

  @Test
  public void aPublicationIsDeleted() {
    context.setUpUserRequester();

    var publication = context.getPublication("100");

    withinTransaction(() -> publicationService.deletePublication(publication));

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.DELETION, publication);
  }

  @Test
  public void aPublicationIsDeletedInABatchProcess() {
    // no requester behind a batch process

    var publication = context.getPublication("100");

    withinTransaction(() -> publicationService.deletePublication(publication));

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.DELETION, publication);
  }

  @Test
  public void aPublicationIsCreated() {
    context.setUpUserRequester();

    var publication = getWithinTransaction(() -> {
      var newPublication = Publication.builder()
          .setTitleAndDescription("A new publication", "A new publication for testing purpose")
          .setContent("Content of my new publication")
          .created(new Date(), User.getCurrentRequester().getId())
          .build();
      return publicationService.createNewPublication(newPublication, TestContext.KMELIA_ID);
    });

    var actual = context.getPublication(publication.getIdentifier().getLocalId());
    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(actual.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.CREATION, actual);
  }

  @Test
  public void aPublicationIsCreatedInABatchProcess() {
    // no requester behind a batch process

    Publication publication = getWithinTransaction(() -> {
      var newPublication = Publication.builder()
          .setTitleAndDescription("A new publication", "A new publication for testing purpose")
          .setContent("Content of my new publication")
          .created(new Date(), "3")
          .build();
      return publicationService.createNewPublication(newPublication, TestContext.KMELIA_ID);
    });

    var actual = context.getPublication(publication.getIdentifier().getLocalId());
    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(actual.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.CREATION, actual);
  }

  @Test
  public void aPublicationIsMovedWithinAnApplication() {
    context.setUpUserRequester();

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      // we simulate a move within the same component instance
      publicationService.movePublication(publication,
          publication.getIdentifier().getComponentInstanceId());
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.INNER_MOVE, publication);
  }

  @Test
  public void aPublicationIsMovedIntoAnotherApplication() {
    context.setUpUserRequester();

    var publication = context.getPublication("100");

    withinTransaction(() ->
        publicationService.movePublication(publication, "kmelia300"));

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.OUTER_MOVE, publication);
  }

  @Test
  public void aPublicationIsMovedWithinAnApplicationByABatchProcess() {
    // no requester behind a batch process

    var publication = context.getPublication("100");

    withinTransaction(() -> {
      // we simulate a move within the same component instance
      publicationService.movePublication(publication,
          publication.getIdentifier().getComponentInstanceId());
    });

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.INNER_MOVE, publication);
  }

  @Test
  public void aPublicationIsMovedIntoAnotherApplicationByABatchProcess() {
    // no requester behind a batch process

    var publication = context.getPublication("100");

    withinTransaction(() ->
        publicationService.movePublication(publication, "kmelia300"));

    TestContext.TrackingEventRecord record =
        context.getLastTrackingEventOn(publication.getIdentifier().getLocalId());
    context.assertThatRecordMatches(record, TrackedActionType.OUTER_MOVE, publication);
  }

  private void withinTransaction(final Runnable runnable) {
    Transaction.performInOne(() -> {
      runnable.run();
      return null;
    });
  }

  private <T> T getWithinTransaction(final Callable<T> runnable) {
    return Transaction.performInOne(runnable::call);
  }
}
