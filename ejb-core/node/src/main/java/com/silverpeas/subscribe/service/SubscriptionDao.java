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

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class declaration
 * @author
 */
public class SubscriptionDao {

  private static final String SUBSCRIBE_COLUMNS =
      "subscriberId, subscriberType, subscriptionMethod, resourceId, resourceType, space, " +
          "instanceId, creatorId, creationDate";

  public static final String ADD_SUBSCRIPTION =
      "INSERT INTO subscribe (" + SUBSCRIBE_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";

  public static final String REMOVE_SUBSCRIPTION =
      "DELETE FROM subscribe WHERE subscriberId = ? AND subscriberType = ? AND subscriptionMethod" +
          " = ? AND resourceId = ? AND resourceType = ? AND instanceId = ?";

  public static final String REMOVE_SUBSCRIPTIONS_BY_SUBSCRIBER =
      "DELETE FROM subscribe WHERE subscriberId = ? AND subscriberType = ?";

  public static final String REMOVE_SUBSCRIPTIONS_BY_RESOURCE =
      "DELETE FROM subscribe WHERE instanceId = ? AND resourceId = ? AND resourceType = ?";

  public static final String SELECT_SUBSCRIBERS_BY_RESOURCE =
      "SELECT subscriberId, subscriberType FROM subscribe " +
          "WHERE resourceId = ? AND resourceType = ? AND instanceId = ?";

  public static final String SELECT_SUBSCRIPTIONS_BY_SUBSCRIPTION = "SELECT " + SUBSCRIBE_COLUMNS +
      " FROM subscribe WHERE subscriberId = ? AND subscriberType = ? AND subscriptionMethod = ? " +
      "AND resourceId = ? AND resourceType = ? AND instanceId = ?";

  public static final String SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER = "SELECT " + SUBSCRIBE_COLUMNS +
      " FROM subscribe WHERE subscriberId = ? AND subscriberType = ?";

  public static final String SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER_AND_COMPONENT =
      SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER + " AND instanceId = ?";

  public static final String SELECT_SUBSCRIPTIONS_BY_RESOURCE = "SELECT " + SUBSCRIBE_COLUMNS +
      " FROM subscribe WHERE instanceId = ? AND resourceId = ? AND resourceType = ?";

  public static final String SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER_AND_RESOURCE =
      SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER +
          "  AND instanceId = ? AND resourceId = ? AND resourceType = ?";

  /**
   * Method declaration
   * @param con
   * @param subscription
   * @throws SQLException
   * @see
   */
  public void add(Connection con, Subscription subscription) throws SQLException, AssertionError {
    SilverTrace.info("subscribe", "SubscriptionDao.add", "root.MSG_GEN_ENTER_METHOD");

    if (!subscription.getSubscriber().getType().isValid() ||
        !subscription.getSubscriptionMethod().isValid() ||
        !subscription.getResource().getType().isValid()) {
      throw new AssertionError(
          "Subscriber type, subscription method or resource type is unknown ...");
    }

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(ADD_SUBSCRIPTION);
      prepStmt.setString(1, subscription.getSubscriber().getId());
      prepStmt.setString(2, subscription.getSubscriber().getType().getName());
      prepStmt.setString(3, subscription.getSubscriptionMethod().getName());
      prepStmt.setString(4, subscription.getResource().getId());
      prepStmt.setString(5, subscription.getResource().getType().getName());
      prepStmt.setString(6, subscription.getResource().getPK().getSpace());
      prepStmt.setString(7, subscription.getResource().getInstanceId());
      prepStmt.setString(8, subscription.getCreatorId());
      prepStmt.setTimestamp(9, new Timestamp(
          subscription.getCreationDate() == null ? DateUtil.getNow().getTime() :
              subscription.getCreationDate().getTime()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscription
   * @throws SQLException
   * @see
   */
  public void remove(Connection con, Subscription subscription) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.remove", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_SUBSCRIPTION);
      prepStmt.setString(1, subscription.getSubscriber().getId());
      prepStmt.setString(2, subscription.getSubscriber().getType().getName());
      prepStmt.setString(3, subscription.getSubscriptionMethod().getName());
      prepStmt.setString(4, subscription.getResource().getId());
      prepStmt.setString(5, subscription.getResource().getType().getName());
      prepStmt.setString(6, subscription.getResource().getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @throws SQLException
   * @see
   */
  public void removeBySubscriber(Connection con, SubscriptionSubscriber subscriber)
      throws SQLException {
    SilverTrace
        .info("subscribe", "SubscriptionDao.removeBySubscriber", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_SUBSCRIPTIONS_BY_SUBSCRIBER);
      prepStmt.setString(1, subscriber.getId());
      prepStmt.setString(2, subscriber.getType().getName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @throws SQLException
   * @see
   */
  public void removeByResource(Connection con, SubscriptionResource resource) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_SUBSCRIPTIONS_BY_RESOURCE);
      prepStmt.setString(1, resource.getInstanceId());
      prepStmt.setString(2, resource.getId());
      prepStmt.setString(3, resource.getType().getName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscription
   * @return
   * @throws SQLException
   * @see
   */
  public boolean existsSubscription(Connection con, Subscription subscription) throws SQLException {
    SilverTrace
        .info("subscribe", "SubscriptionDao.existsSubscription", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_SUBSCRIPTION);
      prepStmt.setString(1, subscription.getSubscriber().getId());
      prepStmt.setString(2, subscription.getSubscriber().getType().getName());
      prepStmt.setString(3, subscription.getSubscriptionMethod().getName());
      prepStmt.setString(4, subscription.getResource().getId());
      prepStmt.setString(5, subscription.getResource().getType().getName());
      prepStmt.setString(6, subscription.getResource().getInstanceId());
      rs = prepStmt.executeQuery();
      return rs.next();
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<Subscription> getSubscriptionsBySubscriber(Connection con,
      SubscriptionSubscriber subscriber) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getSubscriptionsBySubscriber",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER);
      prepStmt.setString(1, subscriber.getId());
      prepStmt.setString(2, subscriber.getType().getName());
      rs = prepStmt.executeQuery();
      return toList(rs);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<Subscription> getSubscriptionsBySubscriberAndComponent(Connection con,
      SubscriptionSubscriber subscriber, String instanceId) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getSubscriptionsBySubscriberAndComponent",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER_AND_COMPONENT);
      prepStmt.setString(1, subscriber.getId());
      prepStmt.setString(2, subscriber.getType().getName());
      prepStmt.setString(3, instanceId);
      rs = prepStmt.executeQuery();
      return toList(rs);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param resource
   * @param method
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<Subscription> getSubscriptionsByResource(Connection con,
      SubscriptionResource resource, final SubscriptionMethod method) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getSubscriptionsByResource",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String methodQueryPart = "";
    if (method != null && !SubscriptionMethod.UNKNOWN.equals(method)) {
      methodQueryPart = " AND subscriptionMethod = ?";
    }
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_RESOURCE + methodQueryPart);
      prepStmt.setString(1, resource.getInstanceId());
      prepStmt.setString(2, resource.getId());
      prepStmt.setString(3, resource.getType().getName());
      if (StringUtil.isDefined(methodQueryPart)) {
        prepStmt.setString(4, method.getName());
      }
      rs = prepStmt.executeQuery();
      return toList(rs);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @param resource
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<Subscription> getSubscriptionsBySubscriberAndResource(Connection con,
      SubscriptionSubscriber subscriber, SubscriptionResource resource) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getSubscriptionsBySubscriberAndResource",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER_AND_RESOURCE);
      prepStmt.setString(1, subscriber.getId());
      prepStmt.setString(2, subscriber.getType().getName());
      prepStmt.setString(3, resource.getInstanceId());
      prepStmt.setString(4, resource.getId());
      prepStmt.setString(5, resource.getType().getName());
      rs = prepStmt.executeQuery();
      return toList(rs);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param resource
   * @param method
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<SubscriptionSubscriber> getSubscribers(Connection con,
      SubscriptionResource resource, SubscriptionMethod method) throws SQLException {
    return getSubscribers(con, Collections.singletonList(resource), method);
  }

  /**
   * Method declaration
   * @param con
   * @param resources
   * @param method
   * @return
   * @throws SQLException
   */
  public Collection<SubscriptionSubscriber> getSubscribers(Connection con,
      Collection<? extends SubscriptionResource> resources, SubscriptionMethod method)
      throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getSubscribers", "root.MSG_GEN_ENTER_METHOD");
    Set<SubscriptionSubscriber> result = new HashSet<SubscriptionSubscriber>();
    for (SubscriptionResource resource : resources) {
      findSubscribers(con, resource, result, method);
    }
    return result;
  }

  /**
   * Centralied method.
   * @param con
   * @param resource
   * @param result
   * @param method
   * @throws SQLException
   */
  private void findSubscribers(Connection con, SubscriptionResource resource,
      Collection<SubscriptionSubscriber> result, SubscriptionMethod method) throws SQLException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String methodQueryPart = "";
    if (method != null && !SubscriptionMethod.UNKNOWN.equals(method)) {
      methodQueryPart = " AND subscriptionMethod = ?";
    }
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIBERS_BY_RESOURCE + methodQueryPart);
      prepStmt.setString(1, resource.getId());
      prepStmt.setString(2, resource.getType().getName());
      prepStmt.setString(3, resource.getInstanceId());
      if (StringUtil.isDefined(methodQueryPart)) {
        prepStmt.setString(4, method.getName());
      }
      rs = prepStmt.executeQuery();
      SubscriptionSubscriber subscriber;
      while (rs.next()) {
        subscriber = createSubscriberInstance(rs.getString("subscriberId"),
            SubscriberType.from(rs.getString("subscriberType")));
        if (subscriber != null) {
          result.add(subscriber);
        }
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Transforms a result set into a subscription collection
   * @param rs
   * @return
   * @throws SQLException
   */
  private Collection<Subscription> toList(ResultSet rs) throws SQLException {
    List<Subscription> list = new ArrayList<Subscription>();
    Subscription subscription;
    while (rs.next()) {
      subscription = createSubscriptionInstance(rs);
      if (subscription != null) {
        list.add(subscription);
      }
    }
    return list;
  }

  /**
   * Create a subscription from a result set.
   * @param rs the result set
   * @return null if it is not possible to instance a subscription object.
   * @throws SQLException
   */
  private Subscription createSubscriptionInstance(ResultSet rs) throws SQLException {
    SubscriberType subscriberType = SubscriberType.from(rs.getString("subscriberType"));
    SubscriptionSubscriber subscriber =
        createSubscriberInstance(rs.getString("subscriberId"), subscriberType);
    SubscriptionMethod subscriptionMethod =
        SubscriptionMethod.from(rs.getString("subscriptionMethod"));
    SubscriptionResourceType resourceType =
        SubscriptionResourceType.from(rs.getString("resourceType"));
    SubscriptionResource resource =
        createResourceInstance(rs.getString("resourceId"), resourceType, rs.getString("space"),
            rs.getString("instanceId"));

    // Checking that data are not corrupted
    if (!subscriptionMethod.isValid() || subscriber == null ||
        resource == null) {
      SilverTrace.warn("subscribe", "SubscriptionDao.createFrom",
          "EX_SUBSCRIBE_TABLE_CONTAINS_CORRUPTED_DATA");
      return null;
    }

    String creatorId = rs.getString("creatorId");
    Date creationDate = rs.getTimestamp("creationDate");
    if (creationDate.getTime() <= 0) {
      creationDate = null;
    }

    final Subscription subscription;
    switch (resourceType) {
      case NODE:
        subscription =
            new NodeSubscription(subscriber, resource, subscriptionMethod, creatorId, creationDate);
        break;
      case COMPONENT:
        subscription =
            new ComponentSubscription(subscriber, resource, subscriptionMethod, creatorId,
                creationDate);
        break;
      default:
        throw new AssertionError("There is no reason to be here !");
    }
    return subscription;
  }

  /**
   * Create a resource.
   * @param resourceId identifier of the aimed resource
   * @param resourceType type of the aimed resource
   * @param space space from which comes the resource
   * @param instanceId component instance identifier from which comes the resource
   * @return null resource type is unknown.
   */
  private SubscriptionResource createResourceInstance(String resourceId,
      SubscriptionResourceType resourceType, String space, String instanceId) {
    final SubscriptionResource resource;
    switch (resourceType) {
      case NODE:
        resource = new NodeSubscriptionResource(new NodePK(resourceId, space, instanceId));
        break;
      case COMPONENT:
        resource = new ComponentSubscriptionResource(instanceId);
        break;
      default:
        resource = null;
    }
    return resource;
  }

  /**
   * Create a subscriber.
   * @param subscriberId identifier of a subscriber
   * @param subscriberType type of a subscriber
   * @return null if subscriber type is unknown.
   */
  private SubscriptionSubscriber createSubscriberInstance(String subscriberId,
      SubscriberType subscriberType) {
    final SubscriptionSubscriber subscriber;
    switch (subscriberType) {
      case USER:
        subscriber = new UserSubscriptionSubscriber(subscriberId);
        break;
      case GROUP:
        subscriber = new GroupSubscriptionSubscriber(subscriberId);
        break;
      default:
        subscriber = null;
    }
    return subscriber;
  }
}