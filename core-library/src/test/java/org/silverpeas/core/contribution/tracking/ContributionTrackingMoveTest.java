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
import org.silverpeas.core.contribution.publication.model.PublicationPK;
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
import static org.silverpeas.core.contribution.tracking.ContributionTrackingService.INNER_MOVE_CONTEXT;
import static org.silverpeas.core.contribution.tracking.ContributionTrackingService.OUTER_MOVE_CONTEXT;
import static org.silverpeas.core.contribution.tracking.ContributionTrackingTestContext.YESTERDAY;

/**
 * Unit tests on the tracking for move of contributions.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans({DefaultSilverpeasComponentInstanceProvider.class, Transaction.class})
class ContributionTrackingMoveTest {

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
  @DisplayName("No tracking is performed when moving a no tracked contribution")
  void moveDoesntImplyATrackedContribution() {
    context.setUpRequester();

    User requester = context.getRequester();
    ContributionIdentifier id = ContributionIdentifier.from("app32", "42", "NoTrackedContribution");
    NoTrackedContribution before = new NoTrackedContribution(id, YESTERDAY, requester);
    NoTrackedContribution after = before.update(OffsetDateTime.now(), requester);

    service.move(before, after);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("No tracking is performed when moving a tracked contribution in a non tracked " +
      "application instance")
  void innerMoveImpliesATrackedContributionInANonTrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("Toto12");
    PublicationDetail after = movePublication(before, true);

    service.move(before, after);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("No tracking is performed when moving a tracked contribution from a non tracked " +
      "application instance to another non tracked application instance")
  void outerMoveImpliesATrackedContributionInANonTrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("Toto12");
    PublicationDetail after = movePublication(before, false);
    after.getPK().setComponentName("Toto18");

    service.move(before, after);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when moving a tracked contribution from a non tracked " +
      "application instance to a tracked application instance")
  void outerMoveToATrackedAppImpliesATrackedContributionInANonTrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("Toto12");
    PublicationDetail after = movePublication(before, false);

    service.move(before, after);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when moving a tracked contribution from a tracked " +
      "application instance to a non tracked application instance")
  void outerMoveToANonTrackedAppImpliesATrackedContributionInATrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, false);
    after.getPK().setComponentName("Toto18");

    service.move(before, after);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when moving a tracked contribution in a tracked " +
      "application instance")
  void innerMoveImpliesATrackedContributionInATrackedApp() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, true);

    service.move(before, after);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when moving a tracked contribution from a tracked " +
      "application instance to another one")
  void outerMoveImpliesATrackedContributionBetweenBothTrackedApps() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, false);

    service.move(before, after);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Saved Tracking event about an inner move is correctly set")
  void innerMoveSavesACorrectTrackingEventAboutTheModification() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, true);

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      String moveCtx = String.format(INNER_MOVE_CONTEXT, before.getInstanceId());

      assertThat(event.getAction().getType(), is(TrackedActionType.INNER_MOVE));
      assertThat(event.getAction().getUser(), is(context.getRequester()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(before.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(before.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(before.getId()));
      assertThat(event.getContext(), is(moveCtx));
      return null;
    });

    service.move(before, after);
  }

  @Test
  @DisplayName("Saved Tracking event about an outer move is correctly set")
  void outerMoveSavesACorrectTrackingEventAboutTheModification() {
    context.setUpRequester();

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, false);

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      String moveCtx =
          String.format(OUTER_MOVE_CONTEXT, before.getInstanceId(), after.getInstanceId());

      assertThat(event.getAction().getType(), is(TrackedActionType.OUTER_MOVE));
      assertThat(event.getAction().getUser(), is(context.getRequester()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(before.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(before.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(before.getId()));
      assertThat(event.getContext(), is(moveCtx));
      return null;
    });

    service.move(before, after);
  }

  @Test
  @DisplayName("Saved Tracking event about an inner move in a batch process is correctly set")
  void innerMoveInABatchProcessSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, true);

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      String moveCtx = String.format(INNER_MOVE_CONTEXT, before.getInstanceId());

      assertThat(event.getAction().getType(), is(TrackedActionType.INNER_MOVE));
      assertThat(event.getAction().getUser(), is(User.getSystemUser()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(before.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(),
          is(before.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(before.getId()));
      assertThat(event.getContext(), is(moveCtx));
      return null;
    });

    service.move(before, after);
  }

  @Test
  @DisplayName("Saved Tracking event about an outer move in a batch process is correctly set")
  void outerMoveInABatchProcessSavesACorrectTrackingEventAboutTheModification() {
    // no requester in a batch process

    PublicationDetail before = context.getPublication("kmelia2");
    PublicationDetail after = movePublication(before, false);

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      String moveCtx = String.format(OUTER_MOVE_CONTEXT, before.getInstanceId(),
          after.getInstanceId());

      assertThat(event.getAction().getType(), is(TrackedActionType.OUTER_MOVE));
      assertThat(event.getAction().getUser(), is(User.getSystemUser()));
      assertThat(event.getAction().getDateTime(),
          within(1, ChronoUnit.MINUTES, OffsetDateTime.now()));
      assertThat(event.getContributionId().getType(), is(before.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(),
          is(before.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(before.getId()));
      assertThat(event.getContext(), is(moveCtx));
      return null;
    });

    service.move(before, after);
  }

  private PublicationDetail movePublication(PublicationDetail publication, boolean innerMove) {
    PublicationDetail moved = publication.copy();
    if (!innerMove) {
      moved.setPk(new PublicationPK(publication.getId(), "kmelia8"));
    }
    return moved;
  }
}