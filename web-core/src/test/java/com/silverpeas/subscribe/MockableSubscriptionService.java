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

package com.silverpeas.subscribe;

import com.silverpeas.util.Default;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author ehugonnet
 */

@Named("subscriptionService")
@Default
public class MockableSubscriptionService implements SubscriptionService {
  private SubscriptionService realService;

  public void setImplementation(SubscriptionService service) {
    this.realService = service;
  }

  @Override
  public void subscribe(Subscription subscription) {
    realService.subscribe(subscription);
  }

  @Override
  public void unsubscribe(Subscription subscription) {
    realService.unsubscribe(subscription);
  }

  @Override
  public void unsubscribe(SubscriptionSubscriber subscriber) {
    realService.unsubscribe(subscriber);
  }

  @Override
  public void unsubscribe(final SubscriptionResource resource) {
    realService.unsubscribe(resource);
  }

  @Override
  public void unsubscribe(final Collection<? extends SubscriptionResource> resources) {
    realService.unsubscribe(resources);
  }

  @Override
  public boolean existsSubscription(final Subscription subscription) {
    return realService.existsSubscription(subscription);
  }

  @Override
  public Collection<Subscription> getByResource(final SubscriptionResource resource) {
    return realService.getByResource(resource);
  }

  @Override
  public Collection<Subscription> getByUserSubscriber(final String userId) {
    return realService.getByUserSubscriber(userId);
  }

  @Override
  public Collection<Subscription> getBySubscriber(SubscriptionSubscriber subscriber) {
    return realService.getBySubscriber(subscriber);
  }

  @Override
  public Collection<Subscription> getBySubscriberAndComponent(
      final SubscriptionSubscriber subscriber, final String instanceId) {
    return realService.getBySubscriberAndComponent(subscriber, instanceId);
  }

  @Override
  public Collection<Subscription> getBySubscriberAndResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource) {
    return realService.getBySubscriberAndResource(subscriber, resource);
  }

  @Override
  public Collection<SubscriptionSubscriber> getSubscribers(final SubscriptionResource resource) {
    return realService.getSubscribers(resource);
  }

  @Override
  public Collection<String> getUserSubscribers(SubscriptionResource resource) {
    return realService.getUserSubscribers(resource);
  }

  @Override
  public Collection<SubscriptionSubscriber> getSubscribers(
      final Collection<? extends SubscriptionResource> resources) {
    return realService.getSubscribers(resources);
  }

  @Override
  public boolean isSubscriberSubscribedToResource(final SubscriptionSubscriber subscriber,
      final SubscriptionResource resource) {
    return realService.isSubscriberSubscribedToResource(subscriber, resource);
  }

  @Override
  public boolean isUserSubscribedToResource(String user, SubscriptionResource resource) {
    return realService.isUserSubscribedToResource(user, resource);
  }
}
