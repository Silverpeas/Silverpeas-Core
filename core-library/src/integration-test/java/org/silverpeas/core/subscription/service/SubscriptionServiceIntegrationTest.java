/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.AbstractCommonSubscriptionIntegrationTest;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.node.model.NodePK;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Same database environment as DAO tests. User: Yohann Chastagnier Date: 24/02/13
 */
@RunWith(Arquillian.class)
public class SubscriptionServiceIntegrationTest extends AbstractCommonSubscriptionIntegrationTest {

  @Inject
  private SubscriptionService subscriptionService;

  /**
   * Test of subscribe method, of interface SubscriptionService.
   */
  @Test(expected = AssertionError.class)
  public void testSubscribeInError() {
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    subscriptionService.subscribe(new NodeSubscription(new SubscriptionSubscriber() {
      @Override
      public String getId() {
        return "100";
      }

      @Override
      public SubscriberType getType() {
        return SubscriberType.UNKNOWN;
      }
    }, nodePk));
  }

  /**
   * Test of subscribe method, of interface SubscriptionService.
   */
  @Test
  public void testSubscribe() {
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    Subscription subscription = new NodeSubscription(UserSubscriptionSubscriber.from("100"), nodePk);

    // Verifying that subscription doesn't exist
    Collection<Subscription> result = subscriptionService.
        getBySubscriberAndResource(subscription.getSubscriber(), subscription.getResource());
    assertThat(result, hasSize(0));

    subscriptionService.subscribe(subscription);
    subscriptionService.subscribe(subscription);
    subscriptionService.subscribe(subscription);
    subscriptionService.subscribe(subscription);
    subscriptionService.subscribe(subscription);

    // Verifying that subscription exists
    result = subscriptionService.
        getBySubscriberAndResource(subscription.getSubscriber(), subscription.getResource());
    assertThat(result, hasSize(1));
    assertThat(result, hasItem(subscription));
    assertThat(result.iterator().next(), not(sameInstance(subscription)));
    assertThat(result.iterator().next(), equalTo(subscription));
  }

  /**
   * Test of unsubscribe method, of interface SubscriptionService.
   */
  @Test
  public void testUnsubscribe() {
    String userId = "2";
    NodePK nodePk = new NodePK("0", INSTANCE_ID);
    Subscription subscription = new NodeSubscription(userId, nodePk);

    // Verifying that subscription exists
    Collection<Subscription> result = subscriptionService.
        getBySubscriberAndResource(subscription.getSubscriber(), subscription.getResource());
    assertThat(result, hasSize(2));
    assertThat(result, hasItem(subscription));

    subscriptionService.unsubscribe(subscription);

    // Verifying that subscription doesn't exist
    result = subscriptionService.
        getBySubscriberAndResource(subscription.getSubscriber(), subscription.getResource());
    assertThat(result, hasSize(1));
  }

  /**
   * Test of unsubscribe method, of interface SubscriptionService.
   */
  @Test
  public void testSubscriberUnsubscribe() {
    String userId = "1";
    SubscriptionSubscriber userSubscriber = UserSubscriptionSubscriber.from(userId);

    // Verifying that subscription exists
    Collection<Subscription> result = subscriptionService.getBySubscriber(userSubscriber);
    assertThat(result, hasSize(10));

    subscriptionService.unsubscribeBySubscriber(userSubscriber);

    // Verifying that subscription doesn't exist
    result = subscriptionService.getBySubscriber(userSubscriber);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of unsubscribe method, of interface SubscriptionService.
   */
  @Test
  public void testUnsubscribeByResource() throws Exception {
    String userId = "1";
    SubscriptionSubscriber userSubscriber = UserSubscriptionSubscriber.from(userId);
    SubscriptionSubscriber user11Subscriber = UserSubscriptionSubscriber.from("11");

    // Verifying state of subscription before removing
    Collection<Subscription> result = subscriptionService
        .getBySubscriberAndComponent(userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(8));
    result = subscriptionService.getBySubscriberAndComponent(user11Subscriber, INSTANCE_ID);
    assertThat(result, hasSize(2));

    subscriptionService.unsubscribeByResource(
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID)));

    // Verifying the deleted subscriptions
    result = subscriptionService.getBySubscriberAndComponent(userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(6));
    result = subscriptionService.getBySubscriberAndComponent(user11Subscriber, INSTANCE_ID);
    assertThat(result, hasSize(2));

    subscriptionService.unsubscribeByResources(Arrays
        .asList(NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID)),
        NodeSubscriptionResource.from(new NodePK("1", INSTANCE_ID)),
        NodeSubscriptionResource.from(new NodePK("10", INSTANCE_ID)),
        NodeSubscriptionResource.from(new NodePK("20", INSTANCE_ID))));

    // Verifying the deleted subscriptions
    result = subscriptionService.getBySubscriberAndComponent(userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(2));
    result = subscriptionService.getBySubscriberAndComponent(user11Subscriber, INSTANCE_ID);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of existsSubscription method, of interface SubscriptionService.
   */
  @Test
  public void testExistsSubscription() throws Exception {
    SubscriptionSubscriber subscriber = UserSubscriptionSubscriber.from("2");

    // Node - User 2 - Self creation method
    assertThat(subscriptionService.
        existsSubscription(new NodeSubscription("2", new NodePK("0", INSTANCE_ID))), is(true));

    // Node - User 2 - Self creation method
    assertThat(subscriptionService.
        existsSubscription(
        new NodeSubscription(subscriber, new NodePK("0", INSTANCE_ID), "unknownUser")),
        is(true));

    // Node - User 2 - Self creation method
    assertThat(subscriptionService.
        existsSubscription(new NodeSubscription(subscriber, new NodePK("0", INSTANCE_ID), "userA")),
        is(true));

    // Component - User 2 - Self creation method
    assertThat(subscriptionService.
        existsSubscription(new ComponentSubscription("2", INSTANCE_ID)), is(false));
  }

  /**
   * Test of getByResource method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscriptionsByNodeResource() {
    SubscriptionResource resource = NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID));
    Collection<Subscription> subscriptions = subscriptionService.getByResource(resource);
    assertThat(subscriptions, hasSize(15));

    subscriptions = subscriptionService.getByResource(resource, SubscriptionMethod.SELF_CREATION);
    assertThat(subscriptions, hasSize(5));

    resource = NodeSubscriptionResource.from(new NodePK("10", INSTANCE_ID));
    subscriptions = subscriptionService.getByResource(resource);
    assertThat(subscriptions, hasSize(3));
  }

  /**
   * Test of getByResource method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscriptionsByComponentResource() {
    SubscriptionResource resource = ComponentSubscriptionResource.from(INSTANCE_ID);
    Collection<Subscription> subscriptions = subscriptionService.getByResource(resource);
    assertThat(subscriptions, hasSize(9));

    resource = NodeSubscriptionResource.from(new NodePK("10", INSTANCE_ID));
    subscriptions = subscriptionService.getByResource(resource);
    assertThat(subscriptions, hasSize(3));
  }

  /**
   * Test of getByUserSubscriber method, of interface SubscriptionService.
   */
  @Test
  public void testGetByUserSubscriber() {

    // User that is not in a group
    Collection<Subscription> result = subscriptionService.getByUserSubscriber("1");
    assertThat(result, hasSize(10));

    // User that is in a group
    result = subscriptionService.getByUserSubscriber(USERID_OF_GROUP_WITH_ONE_USER);
    assertThat(result, hasSize(2));
    assertThat(result, hasItem(
        new NodeSubscription(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER),
            new NodePK("0", INSTANCE_ID))));
    assertThat(result, hasItem(
        new ComponentSubscription(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER),
            INSTANCE_ID)));
  }

  /**
   * Test of getBySubscriber method, of interface SubscriptionService.
   */
  @Test
  public void testGetBySubscriber() {
    // User
    Collection<Subscription> result = subscriptionService.
        getBySubscriber(UserSubscriptionSubscriber.from("1"));
    assertThat(result, hasSize(10));

    // Group
    result = subscriptionService.
        getBySubscriber(GroupSubscriptionSubscriber.from("1"));
    assertThat(result, hasSize(5));
  }

  /**
   * Test of getBySubscriberAndComponent method, of interface SubscriptionService.
   */
  @Test
  public void testGetByUserSubscriberAndComponent() {
    Collection<Subscription> result = subscriptionService.
        getBySubscriberAndComponent(UserSubscriptionSubscriber.from("1"), INSTANCE_ID);
    assertThat(result, hasSize(8));
  }

  /**
   * Test of getBySubscriberAndComponent method, of interface SubscriptionService.
   */
  @Test
  public void testGetByGroupSubscriberAndComponent() {
    Collection<Subscription> result = subscriptionService.
        getBySubscriberAndComponent(GroupSubscriptionSubscriber.from("1"), INSTANCE_ID);
    assertThat(result, hasSize(4));
  }

  /**
   * Test of getSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForNodeResource() {
    Collection<SubscriptionSubscriber> result = subscriptionService
        .getSubscribers(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)));
    assertThat(result, hasSize(10));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    result = subscriptionService
        .getSubscribers(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)),
        SubscriptionMethod.SELF_CREATION);
    assertThat(result, hasSize(5));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
  }

  /**
   * Test of getSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForComponentResource() {
    Collection<SubscriptionSubscriber> result = subscriptionService.getSubscribers(
        ComponentSubscriptionResource.from(INSTANCE_ID));
    assertThat(result, hasSize(6));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));
  }

  /**
   * Test of getUserSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetUserSubscribersForNodeResource() {
    Collection<String> result = subscriptionService
        .getSubscribers(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)))
        .getAllUserIds();
    assertThat(result, hasSize(6));
    assertThat(result, hasItem("1"));
    assertThat(result, hasItem("2"));
    assertThat(result, hasItem("3"));
    assertThat(result, hasItem("4"));
    assertThat(result, hasItem("5"));
    assertThat(result, hasItem(USERID_OF_GROUP_WITH_ONE_USER));

    result = subscriptionService
        .getSubscribers(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)),
            SubscriptionMethod.SELF_CREATION).getAllUserIds();
    assertThat(result, hasSize(5));
    assertThat(result, hasItem("1"));
    assertThat(result, hasItem("2"));
    assertThat(result, hasItem("3"));
    assertThat(result, hasItem("4"));
    assertThat(result, hasItem("5"));
  }

  /**
   * Test of getUserSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetUserSubscribersForComponentResource() {
    Collection<String> result =
        subscriptionService.getSubscribers(ComponentSubscriptionResource.from(INSTANCE_ID))
            .getAllUserIds();
    assertThat(result, hasSize(4));
    assertThat(result, hasItem("1"));
    assertThat(result, hasItem("3"));
    assertThat(result, hasItem("5"));
    assertThat(result, hasItem(USERID_OF_GROUP_WITH_ONE_USER));
  }

  /**
   * Test of getSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForNodeResources() {
    List<NodeSubscriptionResource> resources = Arrays.asList(NodeSubscriptionResource.from(
        new NodePK("0", "100", INSTANCE_ID)), NodeSubscriptionResource.from(new NodePK("10",
        "100", INSTANCE_ID)));
    Collection<SubscriptionSubscriber> result = subscriptionService.getSubscribers(resources);
    assertThat(result, hasSize(10));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    result = subscriptionService.getSubscribers(resources, SubscriptionMethod.SELF_CREATION);
    assertThat(result, hasSize(5));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
  }

  /**
   * Test of isSubscriberSubscribedToResource method, of interface SubscriptionService.
   */
  @Test
  public void testIsSubscriberSubscribedToNodeResource() {
    // User is subscribed
    assertThat(subscriptionService
        .isSubscriberSubscribedToResource(UserSubscriptionSubscriber.from("1"),
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(true));

    // User is subscribed through a group
    assertThat(subscriptionService.isSubscriberSubscribedToResource(
        UserSubscriptionSubscriber.from(USERID_OF_GROUP_WITH_ONE_USER),
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(false));

    // User is not subscribed
    assertThat(subscriptionService
        .isSubscriberSubscribedToResource(UserSubscriptionSubscriber.from("2563"),
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(false));
  }

  /**
   * Test of isSubscriberSubscribedToResource method, of interface SubscriptionService.
   */
  @Test
  public void testIsSubscriberSubscribedToComponentResource() {
    // User is subscribed
    assertThat(subscriptionService
        .isSubscriberSubscribedToResource(UserSubscriptionSubscriber.from("1"),
            ComponentSubscriptionResource.from(INSTANCE_ID)), is(true));

    // User is subscribed through a group
    assertThat(subscriptionService.isSubscriberSubscribedToResource(
        UserSubscriptionSubscriber.from(USERID_OF_GROUP_WITH_ONE_USER),
        ComponentSubscriptionResource.from(INSTANCE_ID)), is(false));

    // User is not subscribed
    assertThat(subscriptionService
        .isSubscriberSubscribedToResource(UserSubscriptionSubscriber.from("2563"),
            ComponentSubscriptionResource.from(INSTANCE_ID)), is(false));
  }

  /**
   * Test of isUserSubscribedToResource method, of interface SubscriptionService.
   */
  @Test
  public void testIsUserSubscribedToNodeResource() {
    // User is subscribed
    assertThat(subscriptionService.isUserSubscribedToResource("1",
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(true));

    // User is subscribed through a group
    assertThat(subscriptionService.isUserSubscribedToResource(USERID_OF_GROUP_WITH_ONE_USER,
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(true));

    // User is not subscribed
    assertThat(subscriptionService.isUserSubscribedToResource("2563",
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(false));
  }

  /**
   * Test of isUserSubscribedToResource method, of interface SubscriptionService.
   */
  @Test
  public void testIsUserSubscribedToComponentResource() {
    // User is subscribed
    assertThat(subscriptionService
        .isUserSubscribedToResource("1", ComponentSubscriptionResource.from(INSTANCE_ID)),
        is(true));

    // User is subscribed through a group
    assertThat(subscriptionService.isUserSubscribedToResource(USERID_OF_GROUP_WITH_ONE_USER,
        ComponentSubscriptionResource.from(INSTANCE_ID)), is(true));

    // User is not subscribed
    assertThat(subscriptionService
            .isUserSubscribedToResource("2563", ComponentSubscriptionResource.from(INSTANCE_ID)),
        is(false));
  }
}
