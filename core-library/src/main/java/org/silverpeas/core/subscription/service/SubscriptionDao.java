/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.UNKNOWN;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
@Repository
public class SubscriptionDao {

  private static final String SUBSCRIBE_TABLE = "subscribe";
  private static final String RESOURCE_ID = "resourceId";
  private static final String RESOURCE_ID_CLAUSE = "resourceId = ?";
  private static final String RESOURCE_TYPE = "resourceType";
  private static final String RESOURCE_TYPE_CLAUSE = "resourceType = ?";
  private static final String INSTANCE_ID = "instanceId";
  private static final String INSTANCE_ID_CLAUSE = "instanceId = ?";
  private static final String SUBSCRIBER_ID = "subscriberId";
  private static final String SUBSCRIBER_ID_CLAUSE = "subscriberId = ?";
  private static final String SUBSCRIBER_TYPE = "subscriberType";
  private static final String SUBSCRIBER_TYPE_CLAUSE = "subscriberType = ?";
  private static final String SUBSCRIPTION_METHOD = "subscriptionMethod";
  private static final String SUBSCRIPTION_METHOD_CLAUSE = "subscriptionMethod = ?";

  private static final String SUBSCRIBE_COLUMNS =
      "subscriberId, subscriberType, subscriptionMethod, resourceId, resourceType, space, " +
          "instanceId, creatorId, creationDate";

  private static final String ADD_SUBSCRIPTION =
      "INSERT INTO subscribe (" + SUBSCRIBE_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";

  private static final String REMOVE_SUBSCRIPTIONS_BY_SUBSCRIBER =
      "DELETE FROM subscribe WHERE subscriberId = ? AND subscriberType = ?";

  private static final String REMOVE_SUBSCRIPTIONS_BY_INSTANCEID =
      "DELETE FROM subscribe WHERE instanceId = ?";

  private static final String SELECT = "SELECT ";

  private static final String SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER = SELECT + SUBSCRIBE_COLUMNS +
      " FROM subscribe WHERE subscriberId = ? AND subscriberType = ?";

  private static final String SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER_AND_COMPONENT =
      SELECT_SUBSCRIPTIONS_BY_SUBSCRIBER + " AND instanceId = ?";

  @Inject
  private SubscriptionFactory factory;

  /**
   * Method declaration
   * @param con
   * @param subscription
   * @throws SQLException
   *
   */
  public void add(Connection con, Subscription subscription) throws SQLException, AssertionError {


    if (!subscription.getSubscriber().getType().isValid() ||
        !subscription.getSubscriptionMethod().isValid() ||
        !subscription.getResource().getType().isValid()) {
      throw new AssertionError(
          "Subscriber type, subscription method or resource type is unknown ...");
    }

    PreparedStatement prepStmt = null;
    try {
      String space = subscription.getResource().getPK().getSpace();
      prepStmt = con.prepareStatement(ADD_SUBSCRIPTION);
      prepStmt.setString(1, subscription.getSubscriber().getId());
      prepStmt.setString(2, subscription.getSubscriber().getType().getName());
      prepStmt.setString(3, subscription.getSubscriptionMethod().getName());
      prepStmt.setString(4, subscription.getResource().getId());
      prepStmt.setString(5, subscription.getResource().getType().getName());
      prepStmt.setString(6, (StringUtil.isDefined(space) ? space : "-"));
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
   *
   */
  public void remove(Connection con, Subscription subscription) throws SQLException {
    final SubscriptionSubscriber subscriber = subscription.getSubscriber();
    final SubscriptionResource resource = subscription.getResource();
    JdbcSqlQuery.createDeleteFor(SUBSCRIBE_TABLE)
        .where(SUBSCRIBER_ID_CLAUSE, subscriber.getId())
        .and(SUBSCRIBER_TYPE_CLAUSE, subscriber.getType().getName())
        .and(SUBSCRIPTION_METHOD_CLAUSE, subscription.getSubscriptionMethod().getName())
        .and(RESOURCE_ID_CLAUSE, resource.getId())
        .and(RESOURCE_TYPE_CLAUSE, resource.getType().getName())
        .and(INSTANCE_ID_CLAUSE, resource.getInstanceId())
        .executeWith(con);
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @throws SQLException
   *
   */
  public void removeBySubscriber(Connection con, SubscriptionSubscriber subscriber)
      throws SQLException {
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
   *
   */
  public void removeByResource(Connection con, SubscriptionResource resource) throws SQLException {
    JdbcSqlQuery.createDeleteFor(SUBSCRIBE_TABLE)
        .where(RESOURCE_ID_CLAUSE, resource.getId())
        .and(RESOURCE_TYPE_CLAUSE, resource.getType().getName())
        .and(INSTANCE_ID_CLAUSE, resource.getInstanceId())
        .executeWith(con);
  }

  public void removeByInstanceId(Connection con, String instanceId) throws SQLException {
    try(PreparedStatement deletion = con.prepareStatement(REMOVE_SUBSCRIPTIONS_BY_INSTANCEID)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  /**
   * Method declaration
   * @param con
   * @param subscription
   * @return
   * @throws SQLException
   *
   */
  public boolean existsSubscription(Connection con, Subscription subscription) throws SQLException {
    final SubscriptionSubscriber subscriber = subscription.getSubscriber();
    final SubscriptionResource resource = subscription.getResource();
    return JdbcSqlQuery.createCountFor(SUBSCRIBE_TABLE)
        .where(SUBSCRIBER_ID_CLAUSE, subscriber.getId())
        .and(SUBSCRIBER_TYPE_CLAUSE, subscriber.getType().getName())
        .and(SUBSCRIPTION_METHOD_CLAUSE, subscription.getSubscriptionMethod().getName())
        .and(RESOURCE_ID_CLAUSE, resource.getId())
        .and(RESOURCE_TYPE_CLAUSE, resource.getType().getName())
        .and(INSTANCE_ID_CLAUSE, resource.getInstanceId())
        .executeUniqueWith(con, r -> r.getLong(1) > 0L);
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @return
   * @throws SQLException
   *
   */
  public SubscriptionList getSubscriptionsBySubscriber(Connection con,
      SubscriptionSubscriber subscriber) throws SQLException {

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
   *
   */
  public SubscriptionList getSubscriptionsBySubscriberAndComponent(Connection con,
      SubscriptionSubscriber subscriber, String instanceId) throws SQLException {

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
   * @param con
   * @param resource
   * @param method
   * @return
   * @throws SQLException
   *
   */
  public SubscriptionList getSubscriptionsByResource(Connection con,
      SubscriptionResource resource, final SubscriptionMethod method) throws SQLException {
    JdbcSqlQuery query = JdbcSqlQuery.createSelect(SUBSCRIBE_COLUMNS)
        .from(SUBSCRIBE_TABLE)
        .where(RESOURCE_ID_CLAUSE, resource.getId())
        .and(RESOURCE_TYPE_CLAUSE, resource.getType().getName());
    if (isDefined(resource.getInstanceId())) {
      query = query.and(INSTANCE_ID_CLAUSE, resource.getInstanceId());
    }
    if (method != null && !SubscriptionMethod.UNKNOWN.equals(method)) {
      query = query.and(SUBSCRIPTION_METHOD_CLAUSE, method.getName());
    }
    return new SubscriptionList(query.executeWith(con, this::createSubscriptionInstance));
  }

  /**
   * Method declaration
   * @param con
   * @param subscriber
   * @param resource
   * @return
   * @throws SQLException
   *
   */
  public SubscriptionList getSubscriptionsBySubscriberAndResource(Connection con,
      SubscriptionSubscriber subscriber, SubscriptionResource resource) throws SQLException {
    return new SubscriptionList(JdbcSqlQuery.createSelect(SUBSCRIBE_COLUMNS)
        .from(SUBSCRIBE_TABLE)
        .where(SUBSCRIBER_ID_CLAUSE, subscriber.getId())
        .and(SUBSCRIBER_TYPE_CLAUSE, subscriber.getType().getName())
        .and(RESOURCE_ID_CLAUSE, resource.getId())
        .and(RESOURCE_TYPE_CLAUSE, resource.getType().getName())
        .and(INSTANCE_ID_CLAUSE, resource.getInstanceId())
        .executeWith(con, this::createSubscriptionInstance));
  }

  /**
   * Method declaration
   * @param con
   * @param resource
   * @param method
   * @return
   * @throws SQLException
   *
   */
  public SubscriptionSubscriberList getSubscribers(Connection con,
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
  public SubscriptionSubscriberList getSubscribers(Connection con,
      Collection<? extends SubscriptionResource> resources, SubscriptionMethod method)
      throws SQLException {

    Set<SubscriptionSubscriber> result = new HashSet<>();
    for (SubscriptionResource resource : resources) {
      findSubscribers(con, resource, result, method);
    }
    return new SubscriptionSubscriberList(result);
  }

  /**
   * Centralized method.
   * @param con
   * @param resource
   * @param result
   * @param method
   * @throws SQLException
   */
  private void findSubscribers(Connection con, SubscriptionResource resource,
      Collection<SubscriptionSubscriber> result, SubscriptionMethod method) throws SQLException {
    JdbcSqlQuery query = JdbcSqlQuery.createSelect("subscriberId, subscriberType")
        .from(SUBSCRIBE_TABLE)
        .where(RESOURCE_ID_CLAUSE, resource.getId())
        .and(RESOURCE_TYPE_CLAUSE, resource.getType().getName())
        .and(INSTANCE_ID_CLAUSE, resource.getInstanceId());
    if (method != null && !SubscriptionMethod.UNKNOWN.equals(method)) {
      query = query.and(SUBSCRIPTION_METHOD_CLAUSE, method.getName());
    }
    query.executeWith(con, r -> {
      final SubscriptionSubscriber subscriber = createSubscriberInstance(r.getString(1),
          SubscriberType.from(r.getString(2)));
      if (subscriber != null) {
        result.add(subscriber);
      }
      return null;
    });
  }

  /**
   * Transforms a result set into a subscription collection
   * @param rs
   * @return
   * @throws SQLException
   */
  private SubscriptionList toList(ResultSet rs) throws SQLException {
    SubscriptionList list = new SubscriptionList();
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
  @SuppressWarnings("rawtypes")
  private Subscription createSubscriptionInstance(ResultSet rs) throws SQLException {
    SubscriberType subscriberType = SubscriberType.from(rs.getString(SUBSCRIBER_TYPE));
    SubscriptionSubscriber subscriber =
        createSubscriberInstance(rs.getString(SUBSCRIBER_ID), subscriberType);
    SubscriptionMethod subscriptionMethod =
        SubscriptionMethod.from(rs.getString(SUBSCRIPTION_METHOD));
    SubscriptionResourceType resourceType =
        SubscriptionResourceType.from(rs.getString(RESOURCE_TYPE));
    SubscriptionResource resource = factory.createSubscriptionResourceInstance(resourceType,
        rs.getString(RESOURCE_ID), rs.getString("space"), rs.getString(INSTANCE_ID));
    // Checking that data are not corrupted
    if (!subscriptionMethod.isValid() || subscriber == null || resource == null) {
      SilverLogger.getLogger(this).warn(
          "The subscription method is'nt valid or either the subscriber or the resource is'nt defined");
      return null;
    }
    if (UNKNOWN.equals(resourceType)) {
      throw new AssertionError("There is no reason to be here !");
    }
    String creatorId = rs.getString("creatorId");
    Date creationDate = rs.getTimestamp("creationDate");
    if (creationDate.getTime() <= 0) {
      creationDate = null;
    }
    final AbstractSubscription subscription = factory.createSubscriptionInstance(subscriber, resource, creatorId);
    subscription.setSubscriptionMethod(subscriptionMethod);
    subscription.setCreationDate(creationDate);
    return subscription;
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