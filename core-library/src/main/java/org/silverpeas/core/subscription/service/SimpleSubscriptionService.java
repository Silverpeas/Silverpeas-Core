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

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

@Service
@Named("subscriptionService")
public class SimpleSubscriptionService implements SubscriptionService, ComponentInstanceDeletion {
  private static final long serialVersionUID = 7299411620583862933L;

  @Inject
  private SubscriptionDao subscriptionDao;

  @Inject
  private OrganizationController organisationController;

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    }
  }

  @Transactional
  @Override
  public void subscribe(Subscription subscription) {
    subscribe(Collections.singletonList(subscription));
  }

  @Transactional
  @Override
  public void subscribe(final Collection<? extends Subscription> subscriptions) {
    Connection con = null;
    try {
      con = getConnection();
      for (Subscription subscription : subscriptions) {
        if (!subscriptionDao.existsSubscription(con, subscription)) {
          subscription.getSubscriber().checkValid();
          subscriptionDao.add(con, subscription);
        }
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Transactional
  @Override
  public void unsubscribe(Subscription subscription) {
    unsubscribe(Collections.singletonList(subscription));
  }

  @Transactional
  @Override
  public void unsubscribe(final Collection<? extends Subscription> subscriptions) {
    Connection con = null;
    try {
      con = getConnection();
      for (Subscription subscription : subscriptions) {
        subscriptionDao.remove(con, subscription);
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Transactional
  @Override
  public void unsubscribeBySubscriber(SubscriptionSubscriber subscriber) {
    unsubscribeBySubscribers(Collections.singletonList(subscriber));
  }

  @Transactional
  @Override
  public void unsubscribeBySubscribers(
      final Collection<? extends SubscriptionSubscriber> subscribers) {

    Connection con = null;
    try {
      con = getConnection();
      for (SubscriptionSubscriber subscriber : subscribers) {
        subscriptionDao.removeBySubscriber(con, subscriber);
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Transactional
  @Override
  public void unsubscribeByResource(SubscriptionResource resource) {
    unsubscribeByResources(Collections.singletonList(resource));
  }

  @Transactional
  @Override
  public void unsubscribeByResources(final Collection<? extends SubscriptionResource> resources) {

    Connection con = null;
    try {
      con = getConnection();
      for (SubscriptionResource resource : resources) {
        subscriptionDao.removeByResource(con, resource);
      }
    } catch (SQLException e) {
      DBUtil.rollback(con);
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean existsSubscription(final Subscription subscription) {
    Connection con = null;

    try {
      con = getConnection();
      return subscriptionDao.existsSubscription(con, subscription);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SubscriptionList getByResource(final SubscriptionResource resource) {
    return getByResource(resource, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public SubscriptionList getByResource(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    try (final Connection con = getConnection()) {
      return subscriptionDao.getSubscriptionsByResource(con, resource, method);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    }
  }

  @Override
  public SubscriptionList getByUserSubscriber(final String userId) {
    SubscriptionList subscriptions =
        getBySubscriber(UserSubscriptionSubscriber.from(userId));
    for (String groupId : organisationController.getAllGroupIdsOfUser(userId)) {
      subscriptions.addAll(getBySubscriber(GroupSubscriptionSubscriber.from(groupId)));
    }
    return subscriptions;
  }

  @Override
  public SubscriptionList getBySubscriber(SubscriptionSubscriber subscriber) {
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriber(con, subscriber);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SubscriptionList getBySubscriberAndComponent(final SubscriptionSubscriber subscriber,
      final String instanceId) {

    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriberAndComponent(con, subscriber, instanceId);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SubscriptionList getBySubscriberAndResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource) {

    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscriptionsBySubscriberAndResource(con, subscriber, resource);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(final SubscriptionResource resource) {
    return getSubscribers(resource, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscribers(con, resource, method);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(
      final Collection<? extends SubscriptionResource> resources) {
    return getSubscribers(resources, SubscriptionMethod.UNKNOWN);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(
      final Collection<? extends SubscriptionResource> resources, final SubscriptionMethod method) {
    Connection con = null;
    try {
      con = getConnection();
      return subscriptionDao.getSubscribers(con, resources, method);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean isSubscriberSubscribedToResource(final SubscriptionSubscriber subscriber,
      final SubscriptionResource resource) {

    return !getBySubscriberAndResource(subscriber, resource).isEmpty();
  }

  @Override
  public boolean isUserSubscribedToResource(String userId, SubscriptionResource resource) {

    return getSubscribers(resource).getAllUserIds().contains(userId);
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Transactional
  @Override
  public void delete(final String componentInstanceId) {
    try(Connection connection = getConnection()) {
      subscriptionDao.removeByInstanceId(connection, componentInstanceId);
    } catch (SQLException ex) {
      throw new SilverpeasRuntimeException(ex.getMessage(), ex);
    }
  }
}
