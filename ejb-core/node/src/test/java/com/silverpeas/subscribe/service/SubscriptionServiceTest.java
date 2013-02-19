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
package com.silverpeas.subscribe.service;

import com.google.common.collect.Lists;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.mock.OrganizationControllerMock;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Same database environment as DAO tests.
 * User: Yohann Chastagnier
 * Date: 24/02/13
 */
public class SubscriptionServiceTest {
  private static final String USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5 =
      "userFromGroupOnly_GroupId_5";
  private static final String INSTANCE_ID = "kmelia60";

  // Spring context
  private ClassPathXmlApplicationContext context;
  private SubscriptionService subscriptionService;
  private OrganizationController organizationController;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void setUp() throws Exception {
    context = new ClassPathXmlApplicationContext("spring-subscription.xml");

    // Beans
    final DataSource dataSource = (DataSource) context.getBean("dataSource");
    subscriptionService = (SubscriptionService) context.getBean("subscriptionService");
    organizationController = (OrganizationControllerMock) context.getBean("organizationController");

    // Database
    InitialContext ic = new InitialContext();
    ic.bind(JNDINames.SUBSCRIBE_DATASOURCE, dataSource);
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  protected IDataSet getDataSet() throws DataSetException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        this.getClass().getClassLoader()
            .getResourceAsStream("com/silverpeas/subscribe/service/node-actors-test-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @After
  public void tearDown() throws Exception {
    DBUtil.clearTestInstance();
    InitialContext ic = new InitialContext();
    ic.unbind(JNDINames.SUBSCRIBE_DATASOURCE);
    context.close();
  }

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
    Subscription subscription =
        new NodeSubscription(UserSubscriptionSubscriber.from("100"), nodePk);

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

    subscriptionService.unsubscribe(userSubscriber);

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
    Collection<Subscription> result =
        subscriptionService.getBySubscriberAndComponent(userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(8));
    result = subscriptionService.getBySubscriberAndComponent(user11Subscriber, INSTANCE_ID);
    assertThat(result, hasSize(2));

    subscriptionService.unsubscribe(NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID)));

    // Verifying the deleted subscriptions
    result = subscriptionService.getBySubscriberAndComponent(userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(6));
    result = subscriptionService.getBySubscriberAndComponent(user11Subscriber, INSTANCE_ID);
    assertThat(result, hasSize(2));

    subscriptionService.unsubscribe(Arrays
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
    initializeUsersAndGroups();

    // User that is not in a group
    Collection<Subscription> result = subscriptionService.getByUserSubscriber("1");
    assertThat(result, hasSize(10));

    // User that is in a group
    result = subscriptionService.getByUserSubscriber(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5);
    assertThat(result, hasSize(2));
    assertThat(result, hasItem(
        new NodeSubscription(GroupSubscriptionSubscriber.from("5"), new NodePK("0", INSTANCE_ID))));
    assertThat(result,
        hasItem(new ComponentSubscription(GroupSubscriptionSubscriber.from("5"), INSTANCE_ID)));
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
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("5")));
  }

  /**
   * Test of getSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForComponentResource() {
    Collection<SubscriptionSubscriber> result =
        subscriptionService.getSubscribers(ComponentSubscriptionResource.from(INSTANCE_ID));
    assertThat(result, hasSize(6));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("5")));
  }

  /**
   * Test of getUserSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetUserSubscribersForNodeResource() {
    initializeUsersAndGroups();
    Collection<String> result = subscriptionService
        .getUserSubscribers(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)));
    assertThat(result, hasSize(6));
    assertThat(result, hasItem("1"));
    assertThat(result, hasItem("2"));
    assertThat(result, hasItem("3"));
    assertThat(result, hasItem("4"));
    assertThat(result, hasItem("5"));
    assertThat(result, hasItem(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5));
  }

  /**
   * Test of getUserSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetUserSubscribersForComponentResource() {
    initializeUsersAndGroups();
    Collection<String> result =
        subscriptionService.getUserSubscribers(ComponentSubscriptionResource.from(INSTANCE_ID));
    assertThat(result, hasSize(4));
    assertThat(result, hasItem("1"));
    assertThat(result, hasItem("3"));
    assertThat(result, hasItem("5"));
    assertThat(result, hasItem(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5));
  }

  /**
   * Test of getSubscribers method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForNodeResources() {
    List<SubscriptionResource> resources = Lists
        .asList(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)),
            new SubscriptionResource[]{
                NodeSubscriptionResource.from(new NodePK("10", "100", INSTANCE_ID))});
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
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("5")));
  }

  /**
   * Test of isSubscriberSubscribedToResource method, of interface SubscriptionService.
   */
  @Test
  public void testIsSubscriberSubscribedToNodeResource() {
    initializeUsersAndGroups();

    // User is subscribed
    assertThat(subscriptionService
        .isSubscriberSubscribedToResource(UserSubscriptionSubscriber.from("1"),
            NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(true));

    // User is subscribed through a group
    assertThat(subscriptionService.isSubscriberSubscribedToResource(
        UserSubscriptionSubscriber.from(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5),
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
    initializeUsersAndGroups();

    // User is subscribed
    assertThat(subscriptionService
        .isSubscriberSubscribedToResource(UserSubscriptionSubscriber.from("1"),
            ComponentSubscriptionResource.from(INSTANCE_ID)), is(true));

    // User is subscribed through a group
    assertThat(subscriptionService.isSubscriberSubscribedToResource(
        UserSubscriptionSubscriber.from(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5),
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
    initializeUsersAndGroups();

    // User is subscribed
    assertThat(subscriptionService.isUserSubscribedToResource("1",
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID))), is(true));

    // User is subscribed through a group
    assertThat(subscriptionService
        .isUserSubscribedToResource(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5,
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
    initializeUsersAndGroups();

    // User is subscribed
    assertThat(subscriptionService
        .isUserSubscribedToResource("1", ComponentSubscriptionResource.from(INSTANCE_ID)),
        is(true));

    // User is subscribed through a group
    assertThat(subscriptionService
        .isUserSubscribedToResource(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5,
            ComponentSubscriptionResource.from(INSTANCE_ID)), is(true));

    // User is not subscribed
    assertThat(subscriptionService
        .isUserSubscribedToResource("2563", ComponentSubscriptionResource.from(INSTANCE_ID)),
        is(false));
  }

  private void initializeUsersAndGroups() {
    when(organizationController.getAllUsersOfGroup(anyString()))
        .thenAnswer(new Answer<UserDetail[]>() {

          @Override
          public UserDetail[] answer(final InvocationOnMock invocation) throws Throwable {
            String groupId = (String) invocation.getArguments()[0];
            UserDetail user = new UserDetail();
            if ("5".equals(groupId)) {
              user.setId(USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5);
              return new UserDetail[]{user};
            } else {
              return new UserDetail[]{};
            }
          }
        });

    when(organizationController.getAllGroupIdsOfUser(anyString()))
        .thenAnswer(new Answer<String[]>() {

          @Override
          public String[] answer(final InvocationOnMock invocation) throws Throwable {
            String userId = (String) invocation.getArguments()[0];
            if (USER_SUBSCRIBER_FROM_GROUP_ONLY_GROUPID_5.equals(userId)) {
              return new String[]{"5"};
            } else {
              return new String[]{};
            }
          }
        });
  }
}
