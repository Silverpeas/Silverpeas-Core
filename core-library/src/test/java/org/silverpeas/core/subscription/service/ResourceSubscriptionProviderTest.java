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
package org.silverpeas.core.subscription.service;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the subscribers concerned by a contribution in another way than by a subscription
 * to a resource of a component instance: they are provided by all the
 * {@link ContributionSubscribersProvider} available in Silverpeas.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ResourceSubscriptionProviderTest {

  private static final ContributionIdentifier A_CONTRIBUTION =
      ContributionIdentifier.from("kmelia42", "23", "Publication");
  private static final ContributionIdentifier ANOTHER_CONTRIBUTION =
      ContributionIdentifier.from("kmelia42", "24", "Publication");
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final String A_SUBSCRIBED_GROUP = "12";

  @TestManagedBean
  AProvider aProvider;
  @TestManagedBean
  AnotherProvider anotherProvider;

  @Test
  void theSubscribersOfAllTheProvidersAreConcernedByTheContribution() {
    aProvider.subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    anotherProvider.subscribers.add(UserSubscriptionSubscriber.from(ANOTHER_SUBSCRIBER));
    anotherProvider.subscribers.add(GroupSubscriptionSubscriber.from(A_SUBSCRIBED_GROUP));

    final SubscriptionSubscriberList subscribers =
        ResourceSubscriptionProvider.getSubscribersConcernedBy(A_CONTRIBUTION);

    assertThat(subscribers.getAllIds(),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER, A_SUBSCRIBED_GROUP));
  }

  @Test
  void aSubscriberGivenBySeveralProvidersIsConcernedOnlyOnce() {
    aProvider.subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    anotherProvider.subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final SubscriptionSubscriberList subscribers =
        ResourceSubscriptionProvider.getSubscribersConcernedBy(A_CONTRIBUTION);

    assertThat(subscribers, hasSize(1));
    assertThat(subscribers.get(0).getId(), is(A_SUBSCRIBER));
  }

  @Test
  void nobodyIsConcernedByAContributionUnknownOfTheProviders() {
    aProvider.subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final SubscriptionSubscriberList subscribers =
        ResourceSubscriptionProvider.getSubscribersConcernedBy(ANOTHER_CONTRIBUTION);

    assertThat(subscribers, is(empty()));
  }

  /**
   * A provider of the subscribers concerned by {@link #A_CONTRIBUTION} only.
   */
  static class AProvider implements ContributionSubscribersProvider {

    final List<SubscriptionSubscriber> subscribers = new ArrayList<>();

    @Override
    public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
      return A_CONTRIBUTION.equals(contribution) ? new SubscriptionSubscriberList(subscribers) :
          new SubscriptionSubscriberList();
    }
  }

  /**
   * Another provider of the subscribers concerned by {@link #A_CONTRIBUTION} only.
   */
  static class AnotherProvider implements ContributionSubscribersProvider {

    final List<SubscriptionSubscriber> subscribers = new ArrayList<>();

    @Override
    public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
      return A_CONTRIBUTION.equals(contribution) ? new SubscriptionSubscriberList(subscribers) :
          new SubscriptionSubscriberList();
    }
  }
}
