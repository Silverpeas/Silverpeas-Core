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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.admin.component.service.DefaultSilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.SettingBundleStub;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestedBean;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.exparity.hamcrest.date.OffsetDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.contribution.tracking.ContributionTrackingTestContext.YESTERDAY;

/**
 * Unit tests on the tracking for updates of contributions.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({DefaultSilverpeasComponentInstanceProvider.class, Transaction.class})
class ContributionTrackingUpdateTest {

  @RegisterExtension
  static SettingBundleStub contributionSettings =
      new SettingBundleStub("org.silverpeas.contribution.settings.contribution");

  @TestManagedMock
  ContributionModificationContextHandler modifHandler;

  @TestManagedMock
  ContributionTrackingRepository repository;

  @TestedBean
  ContributionTrackingService service;

  ContributionTrackingTestContext context = new ContributionTrackingTestContext();


  @BeforeEach
  public void setup() {
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    context.initMocks();
    contributionSettings.put("contribution.modification.behavior.minor.componentNames", "kmelia");
  }

  @Test
  @DisplayName("No tracking is performed when updating a no tracked contribution")
  void updateDoesntImplyATrackedContribution() {
    context.setUpRequester();

    User requester = context.getRequester();
    ContributionIdentifier id = ContributionIdentifier.from("app32", "42", "NoTrackedContribution");
    NoTrackedContribution before = new NoTrackedContribution(id, YESTERDAY, requester);
    NoTrackedContribution after = before.update(OffsetDateTime.now(), requester);

    service.update(before, after);
    verify(modifHandler, never()).isMinorModification();
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("No tracking is performed when updating a tracked contribution in a non tracked " +
      "application instance")
  void updateImpliesATrackedContributionInANonTrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("Toto12");
    PublicationDetail after = updatePublication(before, context.getRequester());

    service.update(before, after);
    verify(modifHandler, never()).isMinorModification();
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when updating a tracked contribution in a tracked " +
      "application instance")
  void updateImpliesATrackedContributionInATrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = updatePublication(before, context.getRequester());

    service.update(before, after);
    verify(modifHandler, times(1)).isMinorModification();
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking event about a minor modification is correctly saved with the requester")
  void updateSavesACorrectTrackingEventAboutTheMinorModification() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = updatePublication(before, context.getRequester());

    when(modifHandler.isMinorModification()).thenReturn(Optional.of(true));
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getAction().getType(), is(TrackedActionType.MINOR_UPDATE));
      assertThat(event.getAction().getUser(), is(context.getRequester()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.update(before, after);
  }

  @Test
  @DisplayName("Tracking event about a major modification is correctly saved with the requester")
  void updateSavesACorrectTrackingEventAboutTheMajorModification() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = updatePublication(before, context.getRequester());

    when(modifHandler.isMinorModification()).thenReturn(Optional.of(false));
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getAction().getType(), is(TrackedActionType.MAJOR_UPDATE));
      assertThat(event.getAction().getUser(), is(context.getRequester()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.update(before, after);
  }

  @Test
  @DisplayName(
      "Tracking event about a classical modification is correctly saved with the " + "requester")
  void updateSavesACorrectTrackingEventAboutAClassicalModification() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = updatePublication(before, context.getRequester());

    when(modifHandler.isMinorModification()).thenReturn(Optional.empty());
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getAction().getType(), is(TrackedActionType.UPDATE));
      assertThat(event.getAction().getUser(), is(context.getRequester()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.update(before, after);
  }

  @Test
  @DisplayName("Saved Tracking event about an update in a batch process is correctly set")
  void updateInABatchProcessSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = updatePublication(before, User.getById("3"));

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      // no minor nor major modification support here
      assertThat(event.getAction().getType(), is(TrackedActionType.UPDATE));
      assertThat(event.getAction().getUser(), is(after.getLastUpdater()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.update(before, after);
  }

  @Test
  @DisplayName(
      "Saved Tracking event about an update without any updater in a batch process without any " +
          "creator is correctly set")
  void updateInABatchProcessWithoutAnyCreatorSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = updatePublication(before, null);

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      // no minor nor major modification support here
      assertThat(event.getAction().getType(), is(TrackedActionType.UPDATE));
      assertThat(event.getAction().getUser(), is(User.getSystemUser()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.update(before, after);
  }

  private PublicationDetail updatePublication(final PublicationDetail publication, User updater) {
    PublicationDetail publi = publication.copy();
    publi.setUpdateDate(new Date());
    publi.setUpdaterId(updater == null ? null : updater.getId());
    return publi;
  }
}