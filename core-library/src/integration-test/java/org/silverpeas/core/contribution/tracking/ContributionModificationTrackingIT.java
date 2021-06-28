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
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.ContributionEventProcessor;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Inject;
import java.io.File;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

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

  TestContext context = new TestContext();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TestContext.TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(TestContext.DATASET_SQL_SCRIPT);

  @Deployment
  public static Archive<WebArchive> createDeployment() {
    return WarBuilder4Publication.onWarForTestClass(ContributionTrackingServiceIT.class)
        .addJcrFeatures()
        .addClasses(ContributionEventProcessor.class)
        .addPackages(true, "org.silverpeas.core.io.media.image.thumbnail")
        .addPackages(true, "org.silverpeas.core.contribution.tracking")
        .addAsResource("org/silverpeas/contribution")
        .build();
  }

  @Before
  public void setUp() throws Exception {
    assertThat(publicationService, notNullValue());

    File silverpeasHome = mavenTargetDirectoryRule.getResourceTestDirFile();
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
    WAComponentRegistry.get().init();
  }

  @After
  public void cleanUp() {
    CacheServiceProvider.clearAllThreadCaches();
  }

  @Test
  public void aPublicationHasAMajorUpdateWithExplicitUpdateDate() {
    context.setUpUserRequester();
    context.setUpModificationType(false);

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      Date now = new Date();
      publication.setUpdateDate(now);
      publication.setUpdaterId(User.getCurrentRequester().getId());

      publicationService.setDetail(publication, true);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.MAJOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasAMinorUpdateWithExplicitUpdateDate() {
    context.setUpUserRequester();
    context.setUpModificationType(true);

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setUpdateDate(new Date());
      publication.setUpdaterId(User.getCurrentRequester().getId());

      publicationService.setDetail(publication, true);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.MINOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasAnUpdateWithExplicitUpdateDate() {
    context.setUpUserRequester();

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setUpdateDate(new Date());
      publication.setUpdaterId(User.getCurrentRequester().getId());

      publicationService.setDetail(publication, true);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.UPDATE, publication);
  }

  @Test
  public void aPublicationHasAMajorDefaultUpdate() {
    context.setUpUserRequester();
    context.setUpModificationType(false);

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setUpdaterId("3");
      publicationService.setDetail(publication);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.MAJOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasAMinorDefaultUpdate() {
    context.setUpUserRequester();
    context.setUpModificationType(true);

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setUpdaterId("3");
      publicationService.setDetail(publication);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.MINOR_UPDATE, publication);
  }

  @Test
  public void aPublicationHasADefaultUpdate() {
    context.setUpUserRequester();

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setUpdaterId(User.getCurrentRequester().getId());
      publicationService.setDetail(publication);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.UPDATE, publication);
  }

  @Test
  public void aPublicationIsUpdatedByABatchProcess() {
    // no requester behind a batch process
    // a publication update in a batch process is always a simple one

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publication.setUpdaterId("3");
      publicationService.setDetail(publication);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.UPDATE, publication);
  }

  @Test
  public void aPublicationIsDeleted() {
    context.setUpUserRequester();

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publicationService.removePublication(publication.getPK());
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.DELETION, publication);
  }

  @Test
  public void aPublicationIsDeletedInABatchProcess() {
    // no requester behind a batch process

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publicationService.removePublication(publication.getPK());
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.DELETION, publication);
  }

  @Test
  public void aPublicationIsCreated() {
    context.setUpUserRequester();

    Mutable<PublicationPK> pk = Mutable.empty();
    withinTransaction(() -> {
      PublicationDetail publication = new PublicationDetail();
      publication.setPk(new PublicationPK(PublicationPK.UNKNOWN_ID, TestContext.KMELIA_ID));
      publication.setName("A new publication");
      publication.setDescription("A new publication for testing purpose");
      publication.setCreatorId(User.getCurrentRequester().getId());
      publication.setCreationDate(new Date());
      publication.setAuthor(publication.getCreatorId());
      publication.setStatus(PublicationDetail.VALID_STATUS);
      publication.setIndexOperation(IndexManager.NONE);
      pk.set(publicationService.createPublication(publication));
    });

    PublicationDetail saved = context.getPublication(pk.get().getId());
    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(saved.getId());
    context.assertThatRecordMatches(record, TrackedActionType.CREATION, saved);
  }

  @Test
  public void aPublicationIsCreatedInABatchProcess() {
    // no requester behind a batch process

    Mutable<PublicationPK> pk = Mutable.empty();
    withinTransaction(() -> {
      PublicationDetail publication = new PublicationDetail();
      publication.setPk(new PublicationPK(PublicationPK.UNKNOWN_ID, TestContext.KMELIA_ID));
      publication.setName("A new publication");
      publication.setDescription("A new publication for testing purpose");
      publication.setCreatorId("-3");
      publication.setCreationDate(new Date());
      publication.setAuthor(publication.getCreatorId());
      publication.setStatus(PublicationDetail.VALID_STATUS);
      publication.setIndexOperation(IndexManager.NONE);
      pk.set(publicationService.createPublication(publication));
    });

    PublicationDetail saved = context.getPublication(pk.get().getId());
    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(saved.getId());
    context.assertThatRecordMatches(record, TrackedActionType.CREATION, saved);
  }

  @Test
  public void aPublicationIsMovedWithinAnApplication() {
    context.setUpUserRequester();

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publicationService.movePublication(publication.getPK(),
          new NodePK("111", publication.getInstanceId()), false);
      PublicationDetail moved = publicationService.getDetail(publication.getPK());
      publicationService.setDetail(moved, false, ResourceEvent.Type.MOVE);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.INNER_MOVE, publication);
  }

  @Test
  public void aPublicationIsMovedIntoAnotherApplication() {
    context.setUpUserRequester();

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publicationService.movePublication(
          new PublicationPK(publication.getId(), publication.getInstanceId()),
          new NodePK(NodePK.ROOT_NODE_ID, "kmelia300"), false);
      PublicationDetail moved = publicationService.getDetail(new PublicationPK("100", "kmelia300"));
      publicationService.setDetail(moved, false, ResourceEvent.Type.MOVE);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.OUTER_MOVE, publication);
  }

  @Test
  public void aPublicationIsMovedWithinAnApplicationByABatchProcess() {
    // no requester behind a batch process

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publicationService.movePublication(publication.getPK(),
          new NodePK("111", publication.getInstanceId()), false);
      PublicationDetail moved = publicationService.getDetail(publication.getPK());
      publicationService.setDetail(moved, false, ResourceEvent.Type.MOVE);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.INNER_MOVE, publication);
  }

  @Test
  public void aPublicationIsMovedIntoAnotherApplicationByABatchProcess() {
    // no requester behind a batch process

    PublicationDetail publication = context.getPublication("100");

    withinTransaction(() -> {
      publicationService.movePublication(
          new PublicationPK(publication.getId(), publication.getInstanceId()),
          new NodePK(NodePK.ROOT_NODE_ID, "kmelia300"), false);
      PublicationDetail moved = publicationService.getDetail(new PublicationPK("100", "kmelia300"));
      publicationService.setDetail(moved, false, ResourceEvent.Type.MOVE);
    });

    TestContext.TrackingEventRecord record = context.getLastTrackingEventOn(publication.getId());
    context.assertThatRecordMatches(record, TrackedActionType.OUTER_MOVE, publication);
  }

  private void withinTransaction(final Runnable runnable) {
    Transaction.performInOne(() -> {
      runnable.run();
      return null;
    });
  }
}
