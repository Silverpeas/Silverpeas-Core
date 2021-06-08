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

import java.time.OffsetDateTime;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the tracking for deletions of contributions.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({DefaultSilverpeasComponentInstanceProvider.class, Transaction.class})
class ContributionTrackingDeletionTest {

  static final OffsetDateTime YESTERDAY = OffsetDateTime.now().minusDays(1);

  @RegisterExtension
  static SettingBundleStub contributionSettings =
      new SettingBundleStub("org.silverpeas.contribution.settings.contribution");

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
  @DisplayName("No tracking is performed when deleting a no tracked contribution")
  void deleteDoesntImplyATrackedContribution() {
    User requester = getRequester();
    ContributionIdentifier id = ContributionIdentifier.from("app32", "42", "NoTrackedContribution");
    NoTrackedContribution contribution = new NoTrackedContribution(id, YESTERDAY, requester);

    service.delete(contribution);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("No tracking is performed when deleting a tracked contribution in a non tracked " +
      "application instance")
  void deleteImpliesATrackedContributionInANonTrackedApp() {
    PublicationDetail publication = getPublication("Toto12");

    service.delete(publication);
    verify(repository, never()).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Tracking is performed when deleting a tracked contribution in a tracked " +
      "application instance")
  void deleteImpliesATrackedContributionInATrackedApp() {
    PublicationDetail publication = getPublication("kmelia2");

    service.delete(publication);
    verify(repository, times(1)).save(any(ContributionTrackingEvent.class));
  }

  @Test
  @DisplayName("Saved Tracking event about a deletion is correctly set")
  void updateSavesACorrectTrackingEventAboutTheModification() {
    PublicationDetail publication = getPublication("kmelia2");

    when(repository.save(any(ContributionTrackingEvent.class))).thenAnswer(i -> {
      ContributionTrackingEvent event = i.getArgument(0);
      // TODO wait for hamcrest-date to be available through silverpeas-test-dependencies to use it to do date time comparison
      assertThat(event.getAction().getType(), is(TrackedActionType.DELETION));
      assertThat(event.getAction().getUser(), is(getRequester()));
      assertThat(event.getContributionId().getType(), is(publication.getContributionType()));
      assertThat(event.getContributionId().getComponentInstanceId(), is(publication.getInstanceId()));
      assertThat(event.getContributionId().getLocalId(), is(publication.getId()));
      return null;
    });

    service.delete(publication);
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