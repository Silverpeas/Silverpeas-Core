/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class SubscriptionDaoTest {

  private static final String INSTANCE_ID = "kmelia60";
  private static SubscriptionDao subscriptionDao = new SubscriptionDao();

  // Spring context
  private ClassPathXmlApplicationContext context;
  private DataSource dataSource;

  @Before
  public void setUp() throws Exception {

    // Spring
    context = new ClassPathXmlApplicationContext("spring-subscription.xml");
    dataSource = (DataSource) context.getBean("dataSource");

    // Database
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
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
    context.close();
  }

  /**
   * Gets the database connection
   * @return
   * @throws SQLException
   */
  private Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * Test of getSubscriptionsByResource method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByNodeResource() throws Exception {
    SubscriptionResource resource = NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID));
    Collection<Subscription> subscriptions =
        subscriptionDao.getSubscriptionsByResource(getConnection(), resource, null);
    assertThat(subscriptions, hasSize(15));

    subscriptions = subscriptionDao
        .getSubscriptionsByResource(getConnection(), resource, SubscriptionMethod.UNKNOWN);
    assertThat(subscriptions, hasSize(15));

    subscriptions = subscriptionDao
        .getSubscriptionsByResource(getConnection(), resource, SubscriptionMethod.FORCED);
    assertThat(subscriptions, hasSize(10));

    resource = NodeSubscriptionResource.from(new NodePK("10", INSTANCE_ID));
    subscriptions = subscriptionDao.getSubscriptionsByResource(getConnection(), resource, null);
    assertThat(subscriptions, hasSize(3));
  }

  /**
   * Test of getSubscriptionsByResource method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByComponentResource() throws Exception {
    SubscriptionResource resource = ComponentSubscriptionResource.from(INSTANCE_ID);
    Collection<Subscription> subscriptions =
        subscriptionDao.getSubscriptionsByResource(getConnection(), resource, null);
    assertThat(subscriptions, hasSize(9));

    resource = NodeSubscriptionResource.from(new NodePK("10", INSTANCE_ID));
    subscriptions = subscriptionDao.getSubscriptionsByResource(getConnection(), resource, null);
    assertThat(subscriptions, hasSize(3));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test(expected = AssertionError.class)
  public void testAddNodeSubscriptionForUnknownBySelfCreationMethod() throws Exception {
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    assertAddSubscription(new NodeSubscription(new SubscriptionSubscriber() {
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
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddNodeSubscriptionForUserBySelfCreationMethod() throws Exception {
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    Subscription subscription =
        new NodeSubscription(UserSubscriptionSubscriber.from("100"), nodePk);
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.USER));
    assertThat(subscription.getResource().getId(), is("26"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.NODE));
    assertThat(subscription.getResource().getPK(), is((WAPrimaryKey) nodePk));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.SELF_CREATION));
    assertThat(subscription.getCreatorId(), is("100"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddNodeSubscriptionForUserBySelfCreationMethodAndExplicitCreatorId()
      throws Exception {
    String userId = "100";
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    Subscription subscription =
        new NodeSubscription(UserSubscriptionSubscriber.from(userId), nodePk, userId);
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.USER));
    assertThat(subscription.getResource().getId(), is("26"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.NODE));
    assertThat(subscription.getResource().getPK(), is((WAPrimaryKey) nodePk));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.SELF_CREATION));
    assertThat(subscription.getCreatorId(), is("100"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddNodeSubscriptionForUserByForcedMethodAndExplicitDifferentCreatorId()
      throws Exception {
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    Subscription subscription =
        new NodeSubscription(UserSubscriptionSubscriber.from("100"), nodePk, "200");
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.USER));
    assertThat(subscription.getResource().getId(), is("26"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.NODE));
    assertThat(subscription.getResource().getPK(), is((WAPrimaryKey) nodePk));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.FORCED));
    assertThat(subscription.getCreatorId(), is("200"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddNodeSubscriptionForGroupByExplicitCreatorId() throws Exception {
    String groupId = "100";
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    Subscription subscription =
        new NodeSubscription(GroupSubscriptionSubscriber.from(groupId), nodePk, groupId);
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.GROUP));
    assertThat(subscription.getResource().getId(), is("26"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.NODE));
    assertThat(subscription.getResource().getPK(), is((WAPrimaryKey) nodePk));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.FORCED));
    assertThat(subscription.getCreatorId(), is("100"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddNodeSubscriptionForGroupByExplicitDifferentCreatorId() throws Exception {
    NodePK nodePk = new NodePK("26", INSTANCE_ID);
    nodePk.setSpace("100");
    Subscription subscription =
        new NodeSubscription(GroupSubscriptionSubscriber.from("100"), nodePk, "200");
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.GROUP));
    assertThat(subscription.getResource().getId(), is("26"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.NODE));
    assertThat(subscription.getResource().getPK(), is((WAPrimaryKey) nodePk));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.FORCED));
    assertThat(subscription.getCreatorId(), is("200"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddComponentSubscriptionForUserBySelfCreationMethod() throws Exception {
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from("100"), INSTANCE_ID);
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.USER));
    assertThat(subscription.getResource().getId(), is("0"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.COMPONENT));
    assertThat(subscription.getResource().getInstanceId(), is(INSTANCE_ID));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.SELF_CREATION));
    assertThat(subscription.getCreatorId(), is("100"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddComponentSubscriptionForUserBySelfCreationMethodAndExplicitCreatorId()
      throws Exception {
    String userId = "100";
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), INSTANCE_ID, userId);
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.USER));
    assertThat(subscription.getResource().getId(), is("0"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.COMPONENT));
    assertThat(subscription.getResource().getInstanceId(), is(INSTANCE_ID));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.SELF_CREATION));
    assertThat(subscription.getCreatorId(), is("100"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddComponentSubscriptionForUserByForcedMethodAndExplicitDifferentCreatorId()
      throws Exception {
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from("100"), INSTANCE_ID, "200");
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.USER));
    assertThat(subscription.getResource().getId(), is("0"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.COMPONENT));
    assertThat(subscription.getResource().getInstanceId(), is(INSTANCE_ID));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.FORCED));
    assertThat(subscription.getCreatorId(), is("200"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testComponentNodeSubscriptionForGroupByExplicitCreatorId() throws Exception {
    String groupId = "100";
    Subscription subscription =
        new ComponentSubscription(GroupSubscriptionSubscriber.from(groupId), INSTANCE_ID, groupId);
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.GROUP));
    assertThat(subscription.getResource().getId(), is("0"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.COMPONENT));
    assertThat(subscription.getResource().getInstanceId(), is(INSTANCE_ID));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.FORCED));
    assertThat(subscription.getCreatorId(), is("100"));
  }

  /**
   * Test of add method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testAddComponentSubscriptionForGroupByExplicitDifferentCreatorId() throws Exception {
    Subscription subscription =
        new ComponentSubscription(GroupSubscriptionSubscriber.from("100"), INSTANCE_ID, "200");
    assertAddSubscription(subscription);
    assertThat(subscription.getSubscriber().getId(), is("100"));
    assertThat(subscription.getSubscriber().getType(), is(SubscriberType.GROUP));
    assertThat(subscription.getResource().getId(), is("0"));
    assertThat(subscription.getResource().getType(), is(SubscriptionResourceType.COMPONENT));
    assertThat(subscription.getResource().getInstanceId(), is(INSTANCE_ID));
    assertThat(subscription.getSubscriptionMethod(), is(SubscriptionMethod.FORCED));
    assertThat(subscription.getCreatorId(), is("200"));
  }

  /**
   * Centralization.
   * @param subscription
   * @throws Exception
   */
  private void assertAddSubscription(Subscription subscription) throws Exception {

    // Verifying that subscription doesn't exist
    Collection<Subscription> result = subscriptionDao.
        getSubscriptionsBySubscriberAndResource(getConnection(), subscription.getSubscriber(),
            subscription.getResource());
    assertThat(result, hasSize(0));

    subscriptionDao.add(getConnection(), subscription);
    subscriptionDao.add(getConnection(), subscription);
    subscriptionDao.add(getConnection(), subscription);

    // Verifying that subscription exists
    result = subscriptionDao.
        getSubscriptionsBySubscriberAndResource(getConnection(), subscription.getSubscriber(),
            subscription.getResource());
    assertThat(result, hasSize(3));
    assertThat(result, hasItem(subscription));
    assertThat(result.iterator().next(), not(sameInstance(subscription)));
    assertThat(result.iterator().next(), equalTo(subscription));
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveNodeSubscriptionForUserBySelfCreationMethod() throws Exception {
    String userId = "2";
    NodePK nodePk = new NodePK("0", INSTANCE_ID);
    Subscription subscription = new NodeSubscription(userId, nodePk);
    assertRemoveSubscription(subscription, 2);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveNodeSubscriptionForUserBySelfCreationMethodWithExplicitCreatorId()
      throws Exception {
    String userId = "2";
    Subscription subscription =
        new NodeSubscription(UserSubscriptionSubscriber.from(userId), new NodePK("0", INSTANCE_ID),
            userId);
    assertRemoveSubscription(subscription, 2);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveNodeSubscriptionForUserByForcedMethodWithExplicitDifferentCreatorId()
      throws Exception {
    String userId = "2";
    Subscription subscription =
        new NodeSubscription(UserSubscriptionSubscriber.from(userId), new NodePK("0", INSTANCE_ID),
            "userA");
    assertRemoveSubscription(subscription, 2);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveNodeSubscriptionForGroup() throws Exception {
    String groupId = "2";
    NodePK nodePk = new NodePK("0", INSTANCE_ID);
    Subscription subscription =
        new NodeSubscription(GroupSubscriptionSubscriber.from(groupId), nodePk, "userA");
    assertRemoveSubscription(subscription, 1);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveComponentSubscriptionForUserBySelfCreationMethod() throws Exception {
    String userId = "5";
    Subscription subscription = new ComponentSubscription(userId, INSTANCE_ID);
    assertRemoveSubscription(subscription, 2);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveComponentSubscriptionForUserBySelfCreationMethodWithExplicitCreatorId()
      throws Exception {
    String userId = "5";
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), INSTANCE_ID, userId);
    assertRemoveSubscription(subscription, 2);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveComponentSubscriptionForUserByForcedMethodWithExplicitDifferentCreatorId()
      throws Exception {
    String userId = "5";
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), INSTANCE_ID, "userC");
    assertRemoveSubscription(subscription, 2);
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveComponentSubscriptionForGroup() throws Exception {
    String groupId = "5";
    Subscription subscription =
        new ComponentSubscription(GroupSubscriptionSubscriber.from(groupId), INSTANCE_ID, "userA");
    assertRemoveSubscription(subscription, 1);
  }

  /**
   * Centralization.
   * @param subscription
   * @throws Exception
   */
  private void assertRemoveSubscription(Subscription subscription, int nbBeforeRemove)
      throws Exception {

    // Verifying that subscription exists
    Collection<Subscription> result = subscriptionDao.
        getSubscriptionsBySubscriberAndResource(getConnection(), subscription.getSubscriber(),
            subscription.getResource());
    assertThat(result, hasSize(nbBeforeRemove));
    assertThat(result, hasItem(subscription));

    subscriptionDao.remove(getConnection(), subscription);

    // Verifying that subscription doesn't exist
    result = subscriptionDao.
        getSubscriptionsBySubscriberAndResource(getConnection(), subscription.getSubscriber(),
            subscription.getResource());
    assertThat(result, hasSize(nbBeforeRemove - 1));
  }

  /**
   * Test of removeBySubscriber method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testRemoveByUserSubscriberBySelfCreation() throws Exception {
    String userId = "1";
    SubscriptionSubscriber userSubscriber = UserSubscriptionSubscriber.from(userId);

    // Verifying that subscription exists
    Collection<Subscription> result =
        subscriptionDao.getSubscriptionsBySubscriber(getConnection(), userSubscriber);
    assertThat(result, hasSize(10));

    subscriptionDao.removeBySubscriber(getConnection(), userSubscriber);

    // Verifying that subscription doesn't exist
    result = subscriptionDao.getSubscriptionsBySubscriber(getConnection(), userSubscriber);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of removeByResource method, of class SubscriptionDao.
   */
  @Test
  public void testRemoveByResource() throws Exception {
    String userId = "1";
    SubscriptionSubscriber userSubscriber = UserSubscriptionSubscriber.from(userId);
    SubscriptionSubscriber user11Subscriber = UserSubscriptionSubscriber.from("11");

    // Verifying state of subscription before removing
    Collection<Subscription> result =
        selectSubscriptionsBySubscriberAndInstanceId(getConnection(), userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(8));
    result = selectSubscriptionsBySubscriberAndInstanceId(getConnection(), user11Subscriber,
        INSTANCE_ID);
    assertThat(result, hasSize(2));

    subscriptionDao.removeByResource(getConnection(),
        NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID)));
    subscriptionDao.removeByResource(getConnection(),
        NodeSubscriptionResource.from(new NodePK("1", INSTANCE_ID)));

    // Verifying the deleted subscriptions
    result =
        selectSubscriptionsBySubscriberAndInstanceId(getConnection(), userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(6));
    result = selectSubscriptionsBySubscriberAndInstanceId(getConnection(), user11Subscriber,
        INSTANCE_ID);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of existsSubscription method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testExistsSubscription() throws Exception {
    SubscriptionSubscriber subscriber = UserSubscriptionSubscriber.from("2");

    // Node - User 2 - Self creation method
    assertThat(subscriptionDao.
        existsSubscription(getConnection(),
            new NodeSubscription("2", new NodePK("0", INSTANCE_ID))), is(true));

    // Node - User 2 - Self creation method
    assertThat(subscriptionDao.
        existsSubscription(getConnection(),
            new NodeSubscription(subscriber, new NodePK("0", INSTANCE_ID), "unknownUser")),
        is(true));

    // Node - User 2 - Self creation method
    assertThat(subscriptionDao.
        existsSubscription(getConnection(),
            new NodeSubscription(subscriber, new NodePK("0", INSTANCE_ID), "userA")), is(true));

    // Component - User 2 - Self creation method
    assertThat(subscriptionDao.
        existsSubscription(getConnection(), new ComponentSubscription("2", INSTANCE_ID)),
        is(false));
  }

  /**
   * Test of getSubscriptionsBySubscriber method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByUserSubscriber() throws Exception {
    Collection<Subscription> result = subscriptionDao.
        getSubscriptionsBySubscriber(getConnection(), UserSubscriptionSubscriber.from("1"));
    assertThat(result, hasSize(10));
  }

  /**
   * Test of getSubscriptionsBySubscriber method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByGroupSubscriber() throws Exception {
    Collection<Subscription> result = subscriptionDao.
        getSubscriptionsBySubscriber(getConnection(), GroupSubscriptionSubscriber.from("1"));
    assertThat(result, hasSize(5));
  }

  /**
   * Test of getSubscriptionsBySubscriberAndComponent method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByUserSubscriberAndComponent() throws Exception {
    Collection<Subscription> result = subscriptionDao.
        getSubscriptionsBySubscriberAndComponent(getConnection(),
            UserSubscriptionSubscriber.from("1"), INSTANCE_ID);
    assertThat(result, hasSize(8));
  }

  /**
   * Test of getSubscriptionsBySubscriberAndComponent method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByGroupSubscriberAndComponent() throws Exception {
    Collection<Subscription> result = subscriptionDao.
        getSubscriptionsBySubscriberAndComponent(getConnection(),
            GroupSubscriptionSubscriber.from("1"), INSTANCE_ID);
    assertThat(result, hasSize(4));
  }

  /**
   * Test of getSubscribers method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscribersForNodeResource() throws Exception {
    Collection<SubscriptionSubscriber> result = subscriptionDao.getSubscribers(getConnection(),
        NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)), null);
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
   * Test of getSubscribers method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscribersForComponentResource() throws Exception {
    Collection<SubscriptionSubscriber> result = subscriptionDao
        .getSubscribers(getConnection(), ComponentSubscriptionResource.from(INSTANCE_ID), null);
    assertThat(result, hasSize(6));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("5")));
  }

  /**
   * Test of getSubscribers method, of class SubscriptionDao.
   * @throws Exception
   */
  @Test
  public void testGetSubscribersForNodeResources() throws Exception {
    List<SubscriptionResource> resources = Lists
        .asList(NodeSubscriptionResource.from(new NodePK("0", "100", INSTANCE_ID)),
            new SubscriptionResource[]{
                NodeSubscriptionResource.from(new NodePK("10", "100", INSTANCE_ID))});
    Collection<SubscriptionSubscriber> result =
        subscriptionDao.getSubscribers(getConnection(), resources, null);
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

    result = subscriptionDao.getSubscribers(getConnection(), resources, SubscriptionMethod.UNKNOWN);
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

    result = subscriptionDao
        .getSubscribers(getConnection(), resources, SubscriptionMethod.SELF_CREATION);
    assertThat(result, hasSize(5));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
  }

  /**
   * Search data for testing.
   * @param con
   * @param subscriber
   * @param instanceId
   * @return
   */
  private Collection<Subscription> selectSubscriptionsBySubscriberAndInstanceId(Connection con,
      SubscriptionSubscriber subscriber, String instanceId) throws Exception {
    return subscriptionDao.getSubscriptionsBySubscriberAndComponent(con, subscriber, instanceId);
  }

  /**
   * Test of selectSubscriptionsBySubscriberAndInstanceId method, included in this class of tests.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByUserSubscriberAndInstanceId() throws Exception {
    String userId = "1";
    SubscriptionSubscriber userSubscriber = UserSubscriptionSubscriber.from(userId);
    Collection<Subscription> result =
        selectSubscriptionsBySubscriberAndInstanceId(getConnection(), userSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(8));

    // User - Node - Self creation
    assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("0", null, INSTANCE_ID))));
    assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("10", null, INSTANCE_ID))));
    assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("20", null, INSTANCE_ID))));

    // User - Component - Self creation
    assertThat(result, hasItem(new ComponentSubscription(userId, INSTANCE_ID)));

    // User - Node - forced
    assertThat(result,
        hasItem(new NodeSubscription(userSubscriber, new NodePK("0", null, INSTANCE_ID), "userA")));
    assertThat(result, hasItem(
        new NodeSubscription(userSubscriber, new NodePK("10", null, INSTANCE_ID), "userC")));
    assertThat(result, hasItem(
        new NodeSubscription(userSubscriber, new NodePK("20", null, INSTANCE_ID), "userC")));

    // User - Component - forced
    assertThat(result, hasItem(new ComponentSubscription(userSubscriber, INSTANCE_ID, "userB")));
  }

  /**
   * Test of selectSubscriptionsBySubscriberAndInstanceId method, included in this class of tests.
   * @throws Exception
   */
  @Test
  public void testGetSubscriptionsByGroupSubscriberAndInstanceId() throws Exception {
    String groupId = "1";
    SubscriptionSubscriber groupSubscriber = GroupSubscriptionSubscriber.from(groupId);
    Collection<Subscription> result =
        selectSubscriptionsBySubscriberAndInstanceId(getConnection(), groupSubscriber, INSTANCE_ID);
    assertThat(result, hasSize(4));

    // Group - Node - forced
    assertThat(result, hasItem(
        new NodeSubscription(groupSubscriber, new NodePK("0", null, INSTANCE_ID), "userA")));
    assertThat(result, hasItem(
        new NodeSubscription(groupSubscriber, new NodePK("10", null, INSTANCE_ID), "userA")));
    assertThat(result, hasItem(
        new NodeSubscription(groupSubscriber, new NodePK("20", null, INSTANCE_ID), "userA")));

    // Group - Component - forced
    assertThat(result, hasItem(new ComponentSubscription(groupSubscriber, INSTANCE_ID, "userA")));
  }
}
