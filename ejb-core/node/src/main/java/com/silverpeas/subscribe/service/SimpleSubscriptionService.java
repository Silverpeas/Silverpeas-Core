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
import com.silverpeas.subscribe.SubscriptionService;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import javax.inject.Named;

/**
 * Class declaration
 * @author
 */
@Named
public class SimpleSubscriptionService implements SubscriptionService {

  private static final long serialVersionUID = 3185180751858450677L;
  private final String dbName = JNDINames.SUBSCRIBE_DATASOURCE;
  private final SubscriptionDao subscriptionDao = new SubscriptionDao();

  /**
   * Constructor declaration
   * @see
   */
  public SimpleSubscriptionService() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscribeBmEJB.getConnection()",
              SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  @Override
  public void subscribe(Subscription subscription) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.subscribe", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      subscriptionDao.add(con, subscription);
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscribeBmEJB.addSubscription()",
              SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_ADD_SUBSCRIBE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void unsubscribe(Subscription subscription) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.unsubscribe", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      subscriptionDao.remove(con, subscription);
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscribeBmEJB.removeSubscribe()",
              SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_REMOVE_SUBSCRIBE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userId
   * @see
   */
  @Override
  public void unsubscribe(String userId) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.removeUserSubscribes",
            "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      subscriptionDao.remove(con, userId);
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscribeBmEJB.removeUserSubscribes()",
              SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_REMOVE_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param node
   * @param path
   * @see
   */
  @Override
  public void unsubscribeByPath(NodePK node, String path) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.removeSubscriptionsByPath",
            "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      subscriptionDao.removeByNodePath(con, node.getComponentName(), path);
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscribeBmEJB.removeSubscriptionsByPath()",
              SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_REMOVE_NODE_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @see
   */
  @Override
  public Collection<? extends Subscription> getUserSubscriptions(String userId) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.getUserSubscriptions", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriber(con, userId);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscribeBmEJB.getUserSubscriptions()",
              SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param componentName
   * @return
   * @see
   */
  @Override
  public Collection<? extends Subscription> getUserSubscriptionsByComponent(String userId,
      String componentName) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.getUserSubscriptionsByComponent",
            "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriberAndComponent(con, userId, componentName);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscribeBmEJB.getUserSubscribesPKsByspaceAndcomponent()",
              SilverpeasRuntimeException.ERROR,
              "subscribe.CANNOT_GET_USER_SUBSCRIBES_SPACE_COMPONENT", e);
    } finally {
      DBUtil.close(con);
    }
  }
  
  /**
   * Method declaration
   *
   * @param node
   * @return
   * @see
   */
  @Override
  public Collection<String> getSubscribers(WAPrimaryKey pk) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.getNodeSubscribersId",
            "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscribers(con, pk);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscribeBmEJB.getNodeSubscribersId()",
              SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_NODE_SUBSCRIBERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean isSubscribedToComponent(String user, String componentName) {
    Collection<? extends Subscription> subscriptions = getUserSubscriptionsByComponent(user, componentName);
    return subscriptions != null && !subscriptions.isEmpty();
  }

}
