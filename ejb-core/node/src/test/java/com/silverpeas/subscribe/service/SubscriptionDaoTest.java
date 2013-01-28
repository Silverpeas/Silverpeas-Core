/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.subscribe.service;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.silverpeas.subscribe.Subscription;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.dbunit.database.IDatabaseConnection;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class SubscriptionDaoTest extends AbstractJndiCase {

  private static final String INSTANCE_ID = "kmelia60";
  private SubscriptionDao subscriptionDao = new SubscriptionDao();

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/silverpeas/subscribe/service/node-actors-test-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }

  @AfterClass
  public static void generalTearDown() throws IOException, NamingException, Exception {
    baseTest.shudown();
  }

  public SubscriptionDaoTest() {
  }

  /**
   * Test of add method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  public void testAdd() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "100";
      NodePK nodePk = new NodePK("0", INSTANCE_ID);
      nodePk.setSpace("100");
      NodeSubscription subscription = new NodeSubscription(userId, nodePk);
      subscriptionDao.add(connection, subscription);
      @SuppressWarnings("unchecked")
      Collection<NodeSubscription> result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(1));
      assertThat(result, hasItem(subscription));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of remove method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testRemove() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "2";
      NodePK nodePk = new NodePK("0", INSTANCE_ID);
      NodeSubscription subscription = new NodeSubscription(userId, nodePk);
      Collection<NodeSubscription> result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(1));
      assertThat(result, contains(subscription));
      subscriptionDao.remove(connection, subscription);
      result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(0));
      assertThat(result, not(hasItem(subscription)));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of removeByUser method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveByUser() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<? extends Subscription> result = subscriptionDao.getSubscriptionsBySubscriber(
          connection, userId);
      assertThat(result, hasSize(4));
      subscriptionDao.remove(connection, userId);
      result = subscriptionDao.getSubscriptionsBySubscriber(connection, userId);
      assertThat(result, hasSize(0));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of removeByNodePath method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testRemoveByNodePath() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String path = "/0/";
      String userId = "1";
      Collection<NodeSubscription> result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(3));
      result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, "11", INSTANCE_ID);
      assertThat(result, hasSize(1));
      subscriptionDao.removeByNodePath(connection, INSTANCE_ID, path);
      result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(2));
      result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, "11", INSTANCE_ID);
      assertThat(result, hasSize(0));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }

  }

  /**
   * Test of getNodePKsByActor method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testGetNodePKsByActor() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodeSubscription> result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriber(connection, userId);
      assertThat(result, hasSize(4));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("0", null, INSTANCE_ID))));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("10", null, INSTANCE_ID))));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("20", null, INSTANCE_ID))));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("0", null, "kmelia20"))));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getNodePKsBySubscriberComponent method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testGetNodePKsByActorComponent() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodeSubscription> result = (Collection<NodeSubscription>) subscriptionDao.
          getSubscriptionsBySubscriberAndComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(3));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("0", null, INSTANCE_ID))));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("10", null, INSTANCE_ID))));
      assertThat(result, hasItem(new NodeSubscription(userId, new NodePK("20", null, INSTANCE_ID))));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getActorPKsByNodePK method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  public void testGetActorPKsByNodePK() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      NodePK node = new NodePK("0", "100", INSTANCE_ID);
      Collection<String> result = subscriptionDao.getSubscribers(connection, node);
      assertThat(result, hasSize(5));
      assertThat(result, hasItem("1"));
      assertThat(result, hasItem("2"));
      assertThat(result, hasItem("3"));
      assertThat(result, hasItem("4"));
      assertThat(result, hasItem("5"));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getActorPKsByNodePKs method, of class SubscriptionDao.
   *
   * @throws Exception
   */
  @Test
  public void testGetActorPKsByNodePKs() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      List<NodePK> nodePks = java.util.Arrays.asList(new NodePK("0", "100", INSTANCE_ID),
          new NodePK("10", "100", INSTANCE_ID));
      Collection<String> result = subscriptionDao.getSubscribers(connection, nodePks);
      assertThat(result, hasSize(5));
      assertThat(result, hasItem("1"));
      assertThat(result, hasItem("2"));
      assertThat(result, hasItem("3"));
      assertThat(result, hasItem("4"));
      assertThat(result, hasItem("5"));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }
}
