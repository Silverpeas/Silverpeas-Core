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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.admin.component.service.DefaultSilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;
import org.silverpeas.kernel.test.annotations.TestManagedBeans;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.exparity.hamcrest.date.OffsetDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.contribution.tracking.ContributionTrackingTestContext.YESTERDAY;

/**
 * Unit tests on the tracking for creation of contributions.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans({DefaultSilverpeasComponentInstanceProvider.class, Transaction.class})
class ContributionTrackingCreationTest {

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
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    contributionSettings.put("contribution.modification.behavior.minor.componentNames", "kmelia");
    context.initMocks();
  }

  @Test
  @DisplayName("No tracking is performed when creating a no tracked contribution")
  void creationDoesntImplyATrackedContribution() {
    context.setUpRequester();

    User requester = context.getRequester();
    ContributionIdentifier id = ContributionIdentifier.from("app32", "42", "NoTrackedContribution");
    NoTrackedContribution contribution = new NoTrackedContribution(id, YESTERDAY, requester);

    service.create(contribution);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("No tracking is performed when creating a tracked contribution in a non tracked " +
      "application instance")
  void creationImpliesATrackedContributionInANonTrackedApp() {
    context.setUpRequester();

    PublicationDetail publication = context.getPublication("Toto12");

    service.create(publication);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when creating a tracked contribution in a tracked " +
      "application instance")
  void creationImpliesATrackedContributionInATrackedApp() {
    context.setUpRequester();

    PublicationDetail publication = context.getPublication("kmelia2");

    service.create(publication);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Saved Tracking event about a creation is correctly set")
  void creationSavesACorrectTrackingEventAboutTheModification() {
    context.setUpRequester();

    PublicationDetail publication = context.getPublication("kmelia2");
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getType(), is(TrackedActionType.CREATION));
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

    service.create(publication);
  }

  @Test
  @DisplayName("Saved Tracking event about a creation in a batch process is correctly set")
  void creationInABatchProcessSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail publication = context.getPublication("kmelia2");

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getType(), is(TrackedActionType.CREATION));
      assertThat(event.getAction().getUser(), is(User.getById(publication.getCreatorId())));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(publication.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(),
          is(publication.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(publication.getId()));
      assertThat(event.getContext().isEmpty(), is(true));
      return null;
    });

    service.create(publication);
  }

  @Test
  @DisplayName("Saved Tracking event about a creation in a batch process without any creator is " +
      "correctly set")
  void creationInABatchProcessWithoutAnyCreatorSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail publication = context.getPublication("kmelia2");
    publication.setCreatorId(null);

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getType(), is(TrackedActionType.CREATION));
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

    service.create(publication);
  }
}