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
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.core.admin.OrganisationController;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class declaration
 * @author
 */
@Named("subscriptionService")
public class SimpleSubscriptionService implements SubscriptionService {

  private final String dbName = JNDINames.SUBSCRIBE_DATASOURCE;
  private final SubscriptionDao subscriptionDao = new SubscriptionDao();

  @Inject
  private OrganisationController organisationController;

  /**
   * Gets a database connection.
   * @return
   */
  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  @Override
  public void subscribe(Subscription subscription) {
    subscribe(Collections.singletonList(subscription));
  }

  @Override
  public void subscribe(final Collection<? extends Subscription> subscriptions) {
    SilverTrace.info("subscribe", "SubscriptionService.subscribe", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      for (Subscription subscription : subscriptions) {
        if (!subscriptionDao.existsSubscription(con, subscription)) {
          subscriptionDao.add(con, subscription);
        }
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscriptionService.subscribe()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_ADD_SUBSCRIBE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void unsubscribe(Subscription subscription) {
    unsubscribe(Collections.singletonList(subscription));
  }

  @Override
  public void unsubscribe(final Collection<? extends Subscription> subscriptions) {
    SilverTrace.info("subscribe", "SubscriptionService.unsubscribe", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      for (Subscription subscription : subscriptions) {
        subscriptionDao.remove(con, subscription);
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscriptionService.unsubscribe()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_REMOVE_SUBSCRIBE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void unsubscribeBySubscriber(SubscriptionSubscriber subscriber) {
    unsubscribeBySubscribers(Collections.singletonList(subscriber));
  }

  @Override
  public void unsubscribeBySubscribers(
      final Collection<? extends SubscriptionSubscriber> subscribers) {
    SilverTrace.info("subscribe", "SubscriptionService.unsubscribe", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      for (SubscriptionSubscriber subscriber : subscribers) {
        subscriptionDao.removeBySubscriber(con, subscriber);
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscriptionService.unsubscribe()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_REMOVE_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void unsubscribeByResource(SubscriptionResource resource) {
    unsubscribeByResources(Collections.singletonList(resource));
  }

  @Override
  public void unsubscribeByResources(final Collection<? extends SubscriptionResource> resources) {
    SilverTrace.info("subscribe", "SubscriptionService.unsubscribe", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      for (SubscriptionResource resource : resources) {
        subscriptionDao.removeByResource(con, resource);
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException("SubscriptionService.unsubscribe()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_REMOVE_NODE_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean existsSubscription(final Subscription subscription) {
    SilverTrace
        .info("subscribe", "SubscriptionService.existsSubscription", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      return subscriptionDao.existsSubscription(con, subscription);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getByResource()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Subscription> getByResource(final SubscriptionResource resource) {
    return getByResource(resource, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public Collection<Subscription> getByResource(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    SilverTrace.info("subscribe", "SubscriptionService.getByResource", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsByResource(con, resource, method);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getByResource()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Subscription> getByUserSubscriber(final String userId) {
    SilverTrace
        .info("subscribe", "SubscriptionService.getByUserSubscriber", "root.MSG_GEN_ENTER_METHOD");
    Collection<Subscription> subscriptions =
        getBySubscriber(UserSubscriptionSubscriber.from(userId));
    for (String groupId : organisationController.getAllGroupIdsOfUser(userId)) {
      subscriptions.addAll(getBySubscriber(GroupSubscriptionSubscriber.from(groupId)));
    }
    return subscriptions;
  }

  @Override
  public Collection<Subscription> getBySubscriber(SubscriptionSubscriber subscriber) {
    SilverTrace
        .info("subscribe", "SubscriptionService.getBySubscriber", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriber(con, subscriber);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getBySubscriber()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Subscription> getBySubscriberAndComponent(
      final SubscriptionSubscriber subscriber, final String instanceId) {
    SilverTrace.info("subscribe", "SubscriptionService.getBySubscriberAndComponent",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriberAndComponent(con, subscriber, instanceId);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getBySubscriberAndComponent()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_USER_SUBSCRIBES_SPACE_COMPONENT",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Subscription> getBySubscriberAndResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource) {
    SilverTrace.info("subscribe", "SubscriptionService.getBySubscriberAndResource",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriberAndResource(con, subscriber, resource);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getBySubscriberAndResource()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_USER_SUBSCRIBES_SPACE_COMPONENT",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<SubscriptionSubscriber> getSubscribers(final SubscriptionResource resource) {
    return getSubscribers(resource, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public Collection<SubscriptionSubscriber> getSubscribers(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    SilverTrace
        .info("subscribe", "SubscriptionService.getSubscribers", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscribers(con, resource, method);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getSubscribers()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_SUBSCRIBERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<String> getUserSubscribers(SubscriptionResource resource) {
    return getUserSubscribers(resource, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public Collection<String> getUserSubscribers(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    SilverTrace
        .info("subscribe", "SubscriptionService.getUserSubscribers", "root.MSG_GEN_ENTER_METHOD");
    Set<String> userIds = new HashSet<String>();
    Set<String> groupIds = new HashSet<String>();
    for (SubscriptionSubscriber subscriber : getSubscribers(resource, method)) {
      switch (subscriber.getType()) {
        case USER:
          userIds.add(subscriber.getId());
          break;
        case GROUP:
          groupIds.add(subscriber.getId());
          break;
      }
    }

    // Retrieving users from groups if any
    for (String groupId : groupIds) {
      for (UserDetail user : organisationController.getAllUsersOfGroup(groupId)) {
        userIds.add(user.getId());
      }
    }

    return userIds;
  }

  @Override
  public Collection<SubscriptionSubscriber> getSubscribers(
      final Collection<? extends SubscriptionResource> resources) {
    return getSubscribers(resources, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public Collection<SubscriptionSubscriber> getSubscribers(
      final Collection<? extends SubscriptionResource> resources, final SubscriptionMethod method) {
    SilverTrace
        .info("subscribe", "SubscriptionService.getSubscribers", "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscribers(con, resources, method);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscriptionService.getSubscribers()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_GET_SUBSCRIBERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean isSubscriberSubscribedToResource(final SubscriptionSubscriber subscriber,
      final SubscriptionResource resource) {
    SilverTrace.info("subscribe", "SubscriptionService.isSubscriberSubscribedToResource",
        "root.MSG_GEN_ENTER_METHOD");
    return !getBySubscriberAndResource(subscriber, resource).isEmpty();
  }

  @Override
  public boolean isUserSubscribedToResource(String userId, SubscriptionResource resource) {
    SilverTrace.info("subscribe", "SubscriptionService.isSubscriberSubscribedToResource",
        "root.MSG_GEN_ENTER_METHOD");
    return getUserSubscribers(resource).contains(userId);
  }
}
