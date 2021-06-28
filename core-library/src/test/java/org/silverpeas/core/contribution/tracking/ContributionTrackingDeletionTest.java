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

import static org.exparity.hamcrest.date.OffsetDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.contribution.tracking.ContributionTrackingTestContext.YESTERDAY;

/**
 * Unit tests on the tracking for deletions of contributions.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({DefaultSilverpeasComponentInstanceProvider.class, Transaction.class})
class ContributionTrackingDeletionTest {

  @RegisterExtension
  static SettingBundleStub contributionSettings =
      new SettingBundleStub("org.silverpeas.contribution.settings.contribution");

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
  @DisplayName("No tracking is performed when deleting a no tracked contribution")
  void deletionDoesntImplyATrackedContribution() {
    context.setUpRequester();

    User requester = context.getRequester();
    ContributionIdentifier id = ContributionIdentifier.from("app32", "42", "NoTrackedContribution");
    NoTrackedContribution contribution = new NoTrackedContribution(id, YESTERDAY, requester);

    service.delete(contribution);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("No tracking is performed when deleting a tracked contribution in a non tracked " +
      "application instance")
  void deletionImpliesATrackedContributionInANonTrackedApp() {
    context.setUpRequester();

    PublicationDetail publication = context.getPublication("Toto12");

    service.delete(publication);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when deleting a tracked contribution in a tracked " +
      "application instance")
  void deletionImpliesATrackedContributionInATrackedApp() {
    context.setUpRequester();

    PublicationDetail publication = context.getPublication("kmelia2");

    service.delete(publication);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Saved Tracking event about a deletion is correctly set")
  void deletionSavesACorrectTrackingEventAboutTheModification() {
    context.setUpRequester();

    PublicationDetail publication = context.getPublication("kmelia2");
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getType(), is(TrackedActionType.DELETION));
      assertThat(event.getAction().getUser(), is(context.getRequester()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(publication.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(),
          is(publication.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(publication.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.delete(publication);
  }

  @Test
  @DisplayName("Saved Tracking event about a deletion in a batch process is correctly set")
  void deletionInBatchProcessSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail publication = context.getPublication("kmelia2");
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getType(), is(TrackedActionType.DELETION));
      assertThat(event.getAction().getUser(), is(User.getSystemUser()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(publication.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(),
          is(publication.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(publication.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.delete(publication);
  }
}