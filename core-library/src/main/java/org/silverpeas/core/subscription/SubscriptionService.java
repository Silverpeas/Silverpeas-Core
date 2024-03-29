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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.service.SubscribeRuntimeException;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface declaration
 * @author
 */
public interface SubscriptionService extends Serializable {

  /**
   * Register a subscription. The informations of creator identifier and creation date are ignored.
   * If subscription already exists, nothing is registered.
   * @param subscription
   */
  void subscribe(Subscription subscription);

  /**
   * Register given subscriptions. The information of creator identifier and creation date are
   * ignored. If a subscription already exists, nothing is registered for it.
   * <p>
   *   IMPORTANT: runtime error will be thrown if subscription is created with Anonymous or Guest user
   * </P>
   * @param subscriptions
   * @throws SubscribeRuntimeException if attempting to create subscription with Anonymous or Guest user.
   */
  void subscribe(Collection<? extends Subscription> subscriptions) throws SubscribeRuntimeException;

  /**
   * Unregister a subscription.
   * @param subscription
   */
  void unsubscribe(Subscription subscription);

  /**
   * Unregister given subscriptions.
   * @param subscriptions
   */
  void unsubscribe(Collection<? extends Subscription> subscriptions);

  /**
   * Unregister all subscription in relation to the given subscriber. If the given subscriber is a
   * user, no subscription by a group is deleted even if the user is part of the group
   * @param subscriber
   */
  void unsubscribeBySubscriber(SubscriptionSubscriber subscriber);

  /**
   * Unregister all subscription in relation to given subscribers. If a given subscriber is a
   * user, no subscription by a group is deleted even if the user is part of the group
   * @param subscribers
   */
  void unsubscribeBySubscribers(Collection<? extends SubscriptionSubscriber> subscribers);

  /**
   * Unregister all subscriptions in relation to the given resource.
   * If given resource is a node, please notice that subscriptions of linked nodes (sub nodes) are
   * not deleted
   * @param resource the aimed resource
   *
   */
  void unsubscribeByResource(SubscriptionResource resource);

  /**
   * Unregister all subscriptions in relation to the given resources.
   * If it exists one or several resources of nodes, please notice that subscriptions of linked
   * nodes (sub nodes) are not deleted
   * @param resources the aimed resources
   *
   */
  void unsubscribeByResources(Collection<? extends SubscriptionResource> resources);

  /**
   * Checks if the given subscription already exists.
   * If the given subscription subscriber is a user but that this user is subscribed only through
   * a group subscription, the method will return false.
   * @param subscription
   * @return
   */
  boolean existsSubscription(Subscription subscription);

  /**
   * <p>
   * Gets all subscriptions in relation to the given resource.
   * </p>
   * <p>
   *  It is possible here to not specify the instance id the resource is linked to, but BE
   *  CAREFUL to do that with resources having a unique identifier.
   * </p>
   * @param resource a resource subscription which could have no instance id specified.
   * @return list of subscriptions
   */
  SubscriptionList getByResource(SubscriptionResource resource);

  /**
   * <p>
   * Gets all subscriptions in relation to the given resource.
   * </p>
   * <p>
   *  It is possible here to not specify the instance id the resource is linked to, but BE
   *  CAREFUL to do that with resources having a unique identifier.
   * </p>
   * @param resource a resource subscription which could have no instance id specified.
   * @param method an optional subscription method.
   * @return list of subscriptions
   */
  SubscriptionList getByResource(SubscriptionResource resource, SubscriptionMethod method);

  /**
   * Gets all subscriptions (COMPONENT/NODE and SELF_CREATION/FORCED) in relation to a user.
   * @param userId
   * @return list of subscriptions of users that have subscribed themselves,
   *         of users that are subscribed through a subscribed group and of users that have been
   *         subscribed by an other user
   */
  SubscriptionList getByUserSubscriber(String userId);

  /**
   * Gets all subscriptions (COMPONENT/NODE and SELF_CREATION/FORCED) in relation to a subscriber.
   * @param subscriber
   * @return list of subscriptions
   */
  SubscriptionList getBySubscriber(SubscriptionSubscriber subscriber);

  /**
   * Gets all subscriptions (COMPONENT/NODE and SELF_CREATION/FORCED) in relation to a subscriber
   * and a component (NODE or COMPONENT resources).
   * @param subscriber
   * @param instanceId
   * @return list of subscriptions
   */
  SubscriptionList getBySubscriberAndComponent(SubscriptionSubscriber subscriber,
      String instanceId);

  /**
   * Gets all subscriptions (COMPONENT/NODE and SELF_CREATION/FORCED) in relation to a subscriber
   * and a resource.
   * @param subscriber
   * @param resource
   * @return list of subscriptions
   */
  SubscriptionList getBySubscriberAndResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource);

  /**
   * Gets all subscribers (USER and/or GROUP) that are subscribed to a resource.
   * If a group subscriber is returned into result, caller has to perform it. User subscribers
   * depending to a group subscription are not returned.
   * @param resource
   * @return list of subscription subscribers
   */
  SubscriptionSubscriberList getSubscribers(SubscriptionResource resource);

  /**
   * Gets all subscribers (USER and/or GROUP) that are subscribed to a resource.
   * If a group subscriber is returned into result, caller has to perform it. User subscribers
   * depending to a group subscription are not returned.
   * @param resource
   * @param method
   * @return list of subscription subscribers
   */
  SubscriptionSubscriberList getSubscribers(SubscriptionResource resource,
      SubscriptionMethod method);

  /**
   * Gets all subscribers (USER and/or GROUP) that are subscribed to given resources.
   * If a group subscriber is returned into result, caller has to perform it. User subscribers
   * depending to a group subscription are not returned.
   * @param resources
   * @return list of subscription subscribers
   */
  SubscriptionSubscriberList getSubscribers(
      Collection<? extends SubscriptionResource> resources);

  /**
   * Gets all subscribers (USER and/or GROUP) that are subscribed to given resources.
   * If a group subscriber is returned into result, caller has to perform it. User subscribers
   * depending to a group subscription are not returned.
   * @param resources
   * @param method
   * @return list of subscription subscribers
   */
  SubscriptionSubscriberList getSubscribers(
      Collection<? extends SubscriptionResource> resources, SubscriptionMethod method);

  /**
   * Indicates if a subscriber is subscribed to a resource.
   * If user subscriber is searched but that the user is subscribed only through a group
   * subscription, the method will return false.
   * @param subscriber
   * @param resource
   * @return true if the given subscriber is subscribed to given resource
   */
  boolean isSubscriberSubscribedToResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource);

  /**
   * Indicates if a user is subscribed to a resource.
   * @param user
   * @param resource
   * @return true if user has subscribed himself, or if user is subscribed through a subscribed
   *         group or if user has been subscribed by an other user
   */
  boolean isUserSubscribedToResource(String user, SubscriptionResource resource);
}