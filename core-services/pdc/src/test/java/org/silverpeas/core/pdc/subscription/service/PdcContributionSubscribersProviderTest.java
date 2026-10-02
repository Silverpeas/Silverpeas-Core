/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.pdc.subscription.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the subscribers concerned by a contribution because of its classification on the
 * PdC.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcContributionSubscribersProviderTest {

  private static final String COMPONENT_ID = "kmelia42";
  private static final String CONTRIBUTION_ID = "23";
  private static final ContributionIdentifier CONTRIBUTION =
      ContributionIdentifier.from(COMPONENT_ID, CONTRIBUTION_ID, "Publication");
  private static final int SILVER_CONTENT_ID = 1024;
  private static final int AN_AXIS = 3;
  private static final int ANOTHER_AXIS = 8;
  private static final SubscriptionSubscriber A_SUBSCRIBER = UserSubscriptionSubscriber.from("3");
  private static final SubscriptionSubscriber ANOTHER_SUBSCRIBER =
      UserSubscriptionSubscriber.from("5");
  private static final SubscriptionSubscriber A_SUBSCRIBED_GROUP =
      GroupSubscriptionSubscriber.from("12");

  @TestManagedMock
  ContentManagementEngine contentManagementEngine;
  @TestManagedMock
  PdcManager pdcManager;
  @TestManagedMock
  PdcSubscriptionService pdcSubscriptionService;
  @TestManagedMock
  SubscriptionService subscriptionService;
  @TestedBean
  PdcContributionSubscribersProvider provider;

  private final List<ClassifyPosition> classification =
      List.of(aPosition(new ClassifyValue(AN_AXIS, "/12/45/")),
          aPosition(new ClassifyValue(ANOTHER_AXIS, "/33/")));

  @BeforeEach
  void theContributionIsClassifiedOnThePdc() throws Exception {
    when(contentManagementEngine.getSilverContentId(List.of(CONTRIBUTION_ID, COMPONENT_ID)))
        .thenReturn(new TreeSet<>(List.of(SILVER_CONTENT_ID)));
    when(pdcManager.getPositions(SILVER_CONTENT_ID, COMPONENT_ID)).thenReturn(classification);
  }

  @Test
  void theSubscribersOfThePositionsSatisfiedByTheClassificationAreConcerned() {
    final PdcSubscriptionPositionCriteria aCriteria = aCriteria("1");
    final PdcSubscriptionPositionCriteria anotherCriteria = aCriteria("2");
    theClassificationSatisfies(aCriteria, anotherCriteria);
    when(subscriptionService.getSubscribers(aCriteria)).thenReturn(
        new SubscriptionSubscriberList(List.of(A_SUBSCRIBER, A_SUBSCRIBED_GROUP)));
    when(subscriptionService.getSubscribers(anotherCriteria)).thenReturn(
        new SubscriptionSubscriberList(List.of(ANOTHER_SUBSCRIBER)));

    final SubscriptionSubscriberList subscribers = provider.getSubscribersOf(CONTRIBUTION);

    assertThat(subscribers,
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER, A_SUBSCRIBED_GROUP));
  }

  @Test
  void aSubscriberOfSeveralSatisfiedPositionsIsConcernedOnlyOnce() {
    final PdcSubscriptionPositionCriteria aCriteria = aCriteria("1");
    final PdcSubscriptionPositionCriteria anotherCriteria = aCriteria("2");
    theClassificationSatisfies(aCriteria, anotherCriteria);
    when(subscriptionService.getSubscribers(aCriteria)).thenReturn(
        new SubscriptionSubscriberList(List.of(A_SUBSCRIBER)));
    when(subscriptionService.getSubscribers(anotherCriteria)).thenReturn(
        new SubscriptionSubscriberList(List.of(UserSubscriptionSubscriber.from("3"))));

    final SubscriptionSubscriberList subscribers = provider.getSubscribersOf(CONTRIBUTION);

    assertThat(subscribers, hasSize(1));
    assertThat(subscribers.get(0), is(A_SUBSCRIBER));
  }

  @Test
  void allThePositionsOfTheClassificationAreTakenIntoAccount() {
    theClassificationSatisfies();

    provider.getSubscribersOf(CONTRIBUTION);

    verify(pdcSubscriptionService).getPositionCriteriaMatching(
        List.of(classification.get(0).getValues(), classification.get(1).getValues()));
  }

  @Test
  void nobodyIsConcernedWhenNoPositionIsSatisfiedByTheClassification() {
    theClassificationSatisfies();

    assertThat(provider.getSubscribersOf(CONTRIBUTION), is(empty()));
  }

  @Test
  void nobodyIsConcernedByAContributionThatIsNotClassified() throws Exception {
    when(contentManagementEngine.getSilverContentId(List.of(CONTRIBUTION_ID, COMPONENT_ID)))
        .thenReturn(new TreeSet<>());

    assertThat(provider.getSubscribersOf(CONTRIBUTION), is(empty()));
    verify(pdcManager, never()).getPositions(anyInt(), anyString());
    verify(pdcSubscriptionService, never()).getPositionCriteriaMatching(anyCollection());
  }

  /**
   * Getting the content identifier with {@link ContentManagementEngine#getSilverContentId(String,
   * String)} registers the application as a provider of classified contents when it isn't yet the
   * case. A simple search of subscribers mustn't have such a side effect.
   */
  @Test
  void lookingForTheSubscribersDoesntRegisterTheApplicationAmongThoseClassifiedOnThePdc()
      throws Exception {
    theClassificationSatisfies();

    provider.getSubscribersOf(CONTRIBUTION);

    verify(contentManagementEngine, never()).getSilverContentId(anyString(), anyString());
    verify(contentManagementEngine, never()).getContentInstanceId(anyString());
  }

  @Test
  void nobodyIsConcernedWhenThePdcFails() throws Exception {
    when(pdcManager.getPositions(SILVER_CONTENT_ID, COMPONENT_ID)).thenThrow(
        new PdcException("failure"));

    assertThat(provider.getSubscribersOf(CONTRIBUTION), is(empty()));
  }

  @Test
  void nobodyIsConcernedWhenTheContentManagementFails() throws Exception {
    when(contentManagementEngine.getSilverContentId(List.of(CONTRIBUTION_ID, COMPONENT_ID)))
        .thenThrow(new ContentManagerException("failure"));

    assertThat(provider.getSubscribersOf(CONTRIBUTION), is(empty()));
  }

  /**
   * The provider has to be found by the subscription API of Silverpeas, otherwise the subscribers
   * to the PdC wouldn't be notified with the other subscribers.
   */
  @Test
  void theSubscribersOnThePdcAreProvidedByTheSubscriptionApi() {
    final PdcSubscriptionPositionCriteria aCriteria = aCriteria("1");
    theClassificationSatisfies(aCriteria);
    when(subscriptionService.getSubscribers(aCriteria)).thenReturn(
        new SubscriptionSubscriberList(List.of(A_SUBSCRIBER)));

    final SubscriptionSubscriberList subscribers =
        ResourceSubscriptionProvider.getSubscribersConcernedBy(CONTRIBUTION);

    assertThat(subscribers, containsInAnyOrder(A_SUBSCRIBER));
  }

  private void theClassificationSatisfies(final PdcSubscriptionPositionCriteria... criteria) {
    when(pdcSubscriptionService.getPositionCriteriaMatching(anyCollection())).thenReturn(
        List.of(criteria));
  }

  private static PdcSubscriptionPositionCriteria aCriteria(final String id) {
    return new PdcSubscriptionPositionCriteria(id, "position " + id,
        List.of(new AxisValueCriterion(AN_AXIS, "/12/")));
  }

  private static ClassifyPosition aPosition(final ClassifyValue... values) {
    return new ClassifyPosition(List.of(values));
  }
}
