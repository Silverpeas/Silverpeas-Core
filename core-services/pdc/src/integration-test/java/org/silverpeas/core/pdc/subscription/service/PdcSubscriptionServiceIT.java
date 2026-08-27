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

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.PdcSubscriptionInitialization;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.pdc.subscription.test.WarBuilder4Pdc;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.pdc.subscription.test.StubbedOrganizationController.GROUP_ID;
import static org.silverpeas.core.pdc.subscription.test.StubbedOrganizationController.USER_IN_GROUP_ID;

/**
 * Integration tests of the subscriptions on the PdC once they are taken in charge by the
 * subscription API of Silverpeas.
 * <p>
 * They verify both the lifecycle of the position criteria and the way the subscription API handles
 * them, which requires the type of resource of the PdC to be registered into the
 * {@link org.silverpeas.core.subscription.SubscriptionFactory} at startup.
 * </p>
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class PdcSubscriptionServiceIT {

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String A_USER_ID = "3";
  private static final String ANOTHER_USER_ID = "5";
  private static final String A_MANAGER_ID = "1";
  private static final int AN_AXIS = 3;
  private static final int ANOTHER_AXIS = 8;

  @Inject
  private PdcSubscriptionService pdcSubscriptionService;

  @Inject
  private SubscriptionService subscriptionService;

  @Inject
  private PdcSubscriptionInitialization initialization;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Pdc.onWarForTestClass(PdcSubscriptionServiceIT.class).build();
  }

  @Before
  public void registerThePdcAsATypeOfSubscriptionResource() {
    initialization.init();
  }

  private PdcSubscriptionPositionCriteria aPositionOnThePdc(final String name) {
    return pdcSubscriptionService.createPositionCriteria(name,
        List.of(new AxisValueCriterion(String.valueOf(AN_AXIS), "/12/45/"),
            new AxisValueCriterion(String.valueOf(ANOTHER_AXIS), "/33/")));
  }

  @Test
  public void positionCriteriaAreSavedWithTheirCriteria() {
    final PdcSubscriptionPositionCriteria created = aPositionOnThePdc("Veille juridique");

    final Optional<PdcSubscriptionPositionCriteria> saved =
        pdcSubscriptionService.getPositionCriteria(created.getId());

    assertThat(saved.isPresent(), is(true));
    assertThat(saved.get().getName(), is("Veille juridique"));
    assertThat(saved.get().getCriteria(), hasSize(2));
    assertThat(saved.get().getCriteria().get(0).getAxisId(), is(AN_AXIS));
    assertThat(saved.get().getCriteria().get(0).getValue(), is("/12/45/"));
  }

  @Test
  public void noPositionCriteriaAreReturnedForAnUnknownIdentifier() {
    assertThat(pdcSubscriptionService.getPositionCriteria("404").isPresent(), is(false));
  }

  @Test
  public void updatingPositionCriteriaDoesntTouchTheSubscriptionsOnThem() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    subscriptionService.subscribe(
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), criteria, A_USER_ID));

    pdcSubscriptionService.updatePositionCriteria(
        new PdcSubscriptionPositionCriteria(criteria.getId(), "Veille sociale",
            List.of(new AxisValueCriterion(String.valueOf(AN_AXIS), "/12/"))));

    final Optional<PdcSubscriptionPositionCriteria> updated =
        pdcSubscriptionService.getPositionCriteria(criteria.getId());
    assertThat(updated.isPresent(), is(true));
    assertThat(updated.get().getName(), is("Veille sociale"));
    assertThat(updated.get().getCriteria(), hasSize(1));
    assertThat(subscriptionService.getByResource(criteria), hasSize(1));
  }

  /**
   * The type of the resources of the PdC has to be registered into the subscription API, otherwise
   * reading a subscription on the PdC from the data source would fail.
   */
  @Test
  public void aSelfCreatedSubscriptionIsReadBackFromTheSubscriptionApi() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    subscriptionService.subscribe(
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), criteria, A_USER_ID));

    final List<Subscription> subscriptions = subscriptionService.getByUserSubscriber(A_USER_ID);

    assertThat(subscriptions, hasSize(1));
    final Subscription subscription = subscriptions.get(0);
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.SELF_CREATION));
    assertThat(subscription.getResource(), instanceOf(PdcSubscriptionPositionCriteria.class));
  }

  /**
   * A resource read from the data source is referred only by its identifier: its name and its
   * criteria have to be lazily resolved.
   */
  @Test
  public void theCriteriaOfASubscriptionReadFromTheDataSourceAreLazilyLoaded() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    subscriptionService.subscribe(
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), criteria, A_USER_ID));

    final PdcSubscriptionPositionCriteria read =
        (PdcSubscriptionPositionCriteria) subscriptionService.getByUserSubscriber(A_USER_ID)
            .get(0)
            .getResource();

    assertThat(read.getName(), is("Veille juridique"));
    assertThat(read.getCriteria(), hasSize(2));
  }

  /**
   * The identity of position criteria is the one of any subscription resource: two references to
   * the same position are equal whether they are lazily loaded or already valued. The
   * reconciliation of the forced subscribers relies on it.
   */
  @Test
  public void aLazilyLoadedPositionEqualsTheValuedOneItRefersTo() {
    final PdcSubscriptionPositionCriteria valued = aPositionOnThePdc("Veille juridique");

    final PdcSubscriptionPositionCriteria lazy =
        PdcSubscriptionPositionCriteria.from(valued.getId());

    assertThat(lazy, is(valued));
    assertThat(lazy.hashCode(), is(valued.hashCode()));
  }

  @Test
  public void aSubscriptionForcedForAUserIsNotASelfCreatedOne() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    subscriptionService.subscribe(
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), criteria,
            SubscriptionMethod.FORCED, A_MANAGER_ID));

    assertThat(subscriptionService.getByResource(criteria, SubscriptionMethod.FORCED), hasSize(1));
    assertThat(subscriptionService.getByResource(criteria, SubscriptionMethod.SELF_CREATION),
        is(empty()));
    assertThat(subscriptionService.getByUserSubscriber(A_USER_ID).get(0).getCreatorId(),
        is(A_MANAGER_ID));
  }

  @Test
  public void aUserOfAForcedGroupIsSubscribedThroughHisGroup() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    subscriptionService.subscribe(
        new PdcSubscription(GroupSubscriptionSubscriber.from(GROUP_ID), criteria,
            SubscriptionMethod.FORCED, A_MANAGER_ID));

    // the group itself is the subscriber, but the users of that group are subscribed
    assertThat(subscriptionService.getSubscribers(criteria).getAllIds(), contains(GROUP_ID));
    assertThat(subscriptionService.getSubscribers(criteria).getAllUserIds(),
        contains(USER_IN_GROUP_ID));
    assertThat(subscriptionService.getByUserSubscriber(USER_IN_GROUP_ID), hasSize(1));
    assertThat(subscriptionService.getByUserSubscriber(ANOTHER_USER_ID), is(empty()));
  }

  @Test
  public void deletingPositionCriteriaUnsubscribesAllOfTheirSubscribers() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    subscriptionService.subscribe(List.of(
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), criteria, A_USER_ID),
        new PdcSubscription(UserSubscriptionSubscriber.from(ANOTHER_USER_ID), criteria,
            SubscriptionMethod.FORCED, A_MANAGER_ID),
        new PdcSubscription(GroupSubscriptionSubscriber.from(GROUP_ID), criteria,
            SubscriptionMethod.FORCED, A_MANAGER_ID)));
    assertThat(subscriptionService.getByResource(criteria), hasSize(3));

    pdcSubscriptionService.deletePositionCriteria(criteria.getId());

    assertThat(pdcSubscriptionService.getPositionCriteria(criteria.getId()).isPresent(), is(false));
    assertThat(subscriptionService.getByResource(criteria), is(empty()));
    assertThat(subscriptionService.getByUserSubscriber(A_USER_ID), is(empty()));
    assertThat(subscriptionService.getByUserSubscriber(ANOTHER_USER_ID), is(empty()));
  }

  @Test
  public void deletingPositionCriteriaLeavesTheOtherOnesUntouched() {
    final PdcSubscriptionPositionCriteria criteria = aPositionOnThePdc("Veille juridique");
    final PdcSubscriptionPositionCriteria otherCriteria = aPositionOnThePdc("Veille sociale");
    subscriptionService.subscribe(List.of(
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), criteria, A_USER_ID),
        new PdcSubscription(UserSubscriptionSubscriber.from(A_USER_ID), otherCriteria,
            A_USER_ID)));

    pdcSubscriptionService.deletePositionCriteria(criteria.getId());

    assertThat(pdcSubscriptionService.getAllPositionCriteria(), hasSize(1));
    assertThat(subscriptionService.getByUserSubscriber(A_USER_ID), hasSize(1));
    assertThat(subscriptionService.getByResource(otherCriteria), hasSize(1));
  }

  @Test
  public void allThePositionCriteriaAreReturnedWithTheirOwnCriteria() {
    aPositionOnThePdc("Veille juridique");
    aPositionOnThePdc("Veille sociale");

    final List<PdcSubscriptionPositionCriteria> all =
        pdcSubscriptionService.getAllPositionCriteria();

    assertThat(all, hasSize(2));
    assertThat(all.stream().map(PdcSubscriptionPositionCriteria::getName).toList(),
        containsInAnyOrder("Veille juridique", "Veille sociale"));
    all.forEach(c -> assertThat(c.getCriteria(), hasSize(2)));
  }
}
