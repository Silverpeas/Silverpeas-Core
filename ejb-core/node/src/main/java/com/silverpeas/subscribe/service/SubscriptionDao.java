/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class declaration
 *
 * @author
 */
public class SubscriptionDao {

  public static final String ADD_SUBSCRIPTION =
          "INSERT INTO subscribe (actorId, nodeId, space, componentName) VALUES (?, ?, ?, ? )";
  public static final String REMOVE_SUBSCRIPTION =
          "DELETE FROM subscribe WHERE actorId = ? AND nodeId = ? AND componentName = ?";
  public static final String REMOVE_USER_SUBSCRIPTIONS = "DELETE FROM subscribe WHERE actorId = ?";
  public static final String SELECT_SUBSCRIPTIONS_BY_USER = "SELECT nodeId, componentName, space FROM subscribe WHERE actorId = ?";
  public static final String SELECT_SUBSCRIBERS_FOR_NODE = "SELECT actorId FROM subscribe WHERE nodeId = ? AND componentName = ?";
  public static final String SELECT_NODE_FOR_COMPONENT_BY_USER = "SELECT nodeId, space FROM subscribe WHERE actorId = ? AND componentName = ?";
  public static final String REMOVE_SUBSCRIPTIONS_BY_PATH = "DELETE FROM subscribe WHERE componentName = ? AND nodeId IN ( "
          + "	SELECT nodeId FROM sb_node_node WHERE nodePath LIKE ? AND instanceId = ?)";

  SubscriptionDao() {
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param subscription
   * @throws SQLException
   * @see
   */
  public void add(Connection con, Subscription subscription) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.add", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(ADD_SUBSCRIPTION);
      prepStmt.setString(1, subscription.getSubscriber());
      prepStmt.setInt(2, Integer.parseInt(subscription.getTopic().getId()));
      prepStmt.setString(3, subscription.getTopic().getSpace());
      prepStmt.setString(4, subscription.getTopic().getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   *
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
      prepStmt.setString(1, subscription.getSubscriber());
      prepStmt.setInt(2, Integer.parseInt(subscription.getTopic().getId()));
      prepStmt.setString(3, subscription.getTopic().getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param userId
   * @throws SQLException
   * @see
   */
  public void remove(Connection con, String userId) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.removeByUser",
            "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_USER_SUBSCRIPTIONS);
      prepStmt.setString(1, userId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param componentName
   * @param path
   * @throws SQLException
   * @see
   */
  public void removeByNodePath(Connection con, String componentName, String path) throws
          SQLException {
    String likePath = path + '%';
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_SUBSCRIPTIONS_BY_PATH);
      prepStmt.setString(1, componentName);
      prepStmt.setString(2, likePath);
      prepStmt.setString(3, componentName);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   *
   *
   * @param con
   * @param userId
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<? extends Subscription> getSubscriptionsBySubscriber(Connection con,
          String userId) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getNodePKsByActor", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_USER);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      List<NodeSubscription> list = new ArrayList<NodeSubscription>();
      while (rs.next()) {
        NodePK nodePK = new NodePK(String.valueOf(rs.getInt("nodeId")), rs.getString("space"), rs.
                getString("componentName"));
        list.add(new NodeSubscription(userId, nodePK));
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param userId
   * @param componentName
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<? extends Subscription> getSubscriptionsBySubscriberAndComponent(Connection con,
          String userId,
          String componentName) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getNodePKsBySubscriberComponent",
            "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_NODE_FOR_COMPONENT_BY_USER);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, componentName);
      rs = prepStmt.executeQuery();
      List<NodeSubscription> list = new ArrayList<NodeSubscription>();
      while (rs.next()) {
        NodePK nodePK = new NodePK(String.valueOf(rs.getInt("nodeId")), rs.getString("space"), componentName);
        list.add(new NodeSubscription(userId, nodePK));
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   *
   *
   *
   * @param con
   * @param key
   * @return
   * @throws SQLException
   * @see
   */
  public Collection<String> getSubscribers(Connection con, WAPrimaryKey key) throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.getActorPKsByNodePK", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIBERS_FOR_NODE);
      prepStmt.setInt(1, Integer.parseInt(key.getId()));
      prepStmt.setString(2, key.getComponentName());
      rs = prepStmt.executeQuery();
      List<String> list = new ArrayList<String>();
      while (rs.next()) {
        list.add(rs.getString(1));
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public Collection<String> getSubscribers(Connection con, Collection<? extends WAPrimaryKey> pks)
          throws SQLException {
    Set<String> result = new HashSet<String>();
    for (WAPrimaryKey pk : pks) {
      findSubscribers(con, pk, result);
    }
    return result;
  }

  void findSubscribers(Connection con, WAPrimaryKey pk, Collection<String> result)
          throws SQLException {
    SilverTrace.info("subscribe", "SubscriptionDao.findSubscribers", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIBERS_FOR_NODE);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.setString(2, pk.getComponentName());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        result.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}