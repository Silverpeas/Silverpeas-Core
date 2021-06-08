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
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.RequesterProvider;
import org.silverpeas.core.test.extention.SettingBundleStub;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestedBean;

import java.util.Date;
import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the tracking for updates of contributions.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({DefaultSilverpeasComponentInstanceProvider.class, Transaction.class})
class ContributionTrackingUpdateTest {

  static final OffsetDateTime YESTERDAY = OffsetDateTime.now().minusDays(1);

  @RegisterExtension
  static SettingBundleStub contributionSettings =
      new SettingBundleStub("org.silverpeas.contribution.settings.contribution");

  @TestManagedMock
  ContributionModificationContextHandler modifHandler;

  @TestManagedMock
  ContributionTrackingRepository repository;

  @TestedBean
  ContributionTrackingService service;

  @RequesterProvider
  User getRequester() {
    UserDetail user = new UserDetail();
    user.setId("0");
    user.setFirstName("Toto");
    user.setLastName("Tartempion");
    user.setLogin("toto");
    return user;
  }

  @BeforeEach
  public void setup() {
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    UserProvider userProvider = UserProvider.get();
    when(userProvider.getUser(anyString())).thenAnswer(i -> {
      String id = i.getArgument(0);
      if (id.equals("0")) {
        return getRequester();
      }
      UserDetail user = new UserDetail();
      user.setId(id);
      user.setLastName("Tartempion" + id);
      user.setFirstName("Toto");
      user.setLogin("toto" + id);
      return user;
    });
    contributionSettings.put("contribution.tracking.apps", "kmelia");
    contributionSettings.put("contribution.modification.behavior.minor", "true");
  }

  @Test
  @DisplayName("No tracking is performed when updating a no tracked contribution")
  void updateDoesntImplyATrackedContribution() {
    User requester = getRequester();
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
    PublicationDetail before = getPublication("Toto12");
    PublicationDetail after = updatePublication(before);

    service.update(before, after);
    verify(modifHandler, never()).isMinorModification();
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when updating a tracked contribution in a tracked " +
      "application instance")
  void updateImpliesATrackedContributionInATrackedApp() {
    PublicationDetail before = getPublication("kmelia2");
    PublicationDetail after = updatePublication(before);

    service.update(before, after);
    verify(modifHandler, times(1)).isMinorModification();
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Saved Tracking event about a minor modification is correctly set")
  void updateSavesACorrectTrackingEventAboutTheMinorModification() {
    PublicationDetail before = getPublication("kmelia2");
    PublicationDetail after = updatePublication(before);

    when(modifHandler.isMinorModification()).thenReturn(true);
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getDateTime().toInstant(),
          is(after.getLastUpdateDate().toInstant()));
      assertThat(event.getAction().getType(), is(TrackedActionType.MINOR_UPDATE));
      assertThat(event.getAction().getUser(), is(getRequester()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      return null;
    });

    service.update(before, after);
  }

  @Test
  @DisplayName("Saved Tracking event about a major modification is correctly set")
  void updateSavesACorrectTrackingEventAboutTheMajorModification() {
    PublicationDetail before = getPublication("kmelia2");
    PublicationDetail after = updatePublication(before);

    when(modifHandler.isMinorModification()).thenReturn(false);
    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      assertThat(event.getAction().getDateTime().toInstant(),
          is(after.getLastUpdateDate().toInstant()));
      assertThat(event.getAction().getType(), is(TrackedActionType.MAJOR_UPDATE));
      assertThat(event.getAction().getUser(), is(getRequester()));
      assertThat(event.getContributionId().getType(), is(after.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(after.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(after.getId()));
      return null;
    });

    service.update(before, after);
  }

  private PublicationDetail getPublication(final String instanceId) {
    PublicationDetail publi = new PublicationDetail();
    publi.setPk(new PublicationPK("23", instanceId));
    publi.setName("My publi");
    publi.setDescription("A description");
    publi.setCreationDate(Date.from(YESTERDAY.toInstant()));
    publi.setCreatorId("1");
    publi.setUpdateDate(publi.getCreationDate());
    publi.setUpdaterId(publi.getCreatorId());
    return publi;
  }

  private PublicationDetail updatePublication(final PublicationDetail publication) {
    PublicationDetail publi = publication.copy();
    publi.setUpdateDate(new Date());
    publi.setUpdaterId(getRequester().getId());
    return publi;
  }
}