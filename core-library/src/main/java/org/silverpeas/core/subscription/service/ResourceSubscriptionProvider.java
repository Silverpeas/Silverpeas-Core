/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.util.ServiceProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.service.DefaultResourceSubscriptionService.DEFAULT_IMPLEMENTATION_ID;

/**
 * This common subscription provider can return results from a default {@link
 * ResourceSubscriptionService} implementations that compute basic
 * operations or from an implementation provided by the component itself in order to compute a
 * result that takes into account specific component rules.
 * @author Yohann Chastagnier
 */
public class ResourceSubscriptionProvider {

  // Component implementations.
  private static final Map<String, ResourceSubscriptionService> componentImplementations = new HashMap<>();

  private ResourceSubscriptionProvider() {
    // Provider class
  }

  /**
   * Registers a new implementation of {@link ResourceSubscriptionService}
   * to manage and to provide.
   * @param service the service instance to register.
   */
  public static void registerResourceSubscriptionService(
      AbstractResourceSubscriptionService service) {
    componentImplementations.put(service.getHandledComponentName(), service);
  }

  /**
   * Gets all subscribers registered on a component.<br>
   * This service does not look at resources handled by the component but just explicit component
   * subscriptions.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods.
   */
  public static SubscriptionSubscriberList getSubscribersOfComponent(String componentInstanceId) {
    return getSubscribersOfComponentAndTypedResource(componentInstanceId, COMPONENT, null);
  }

  /**
   * Gets all subscribers concerned by a specified resource represented by the
   * given resource type and identifier.<br>
   * The inheritance of subscription is handled by this method. So if the aimed subscription
   * resource has a parent subscription resource, subscribers of both of them are returned.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @param resourceType the type of the aimed resource.
   * @param resourceId the identifier of the aime resource.
   * @param directives some directive to apply when it is necessary.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods.
   */
  public static SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      String componentInstanceId, SubscriptionResourceType resourceType, String resourceId,
      SubscriberDirective... directives) {
    return getService(componentInstanceId).getSubscribersOfComponentAndTypedResource(
        componentInstanceId, resourceType, resourceId, directives);
  }

  /**
   * Gets all subscribers concerned by a specified subscription resource.<br>
   * The inheritance of subscription is handled by this method. So if the aimed subscription
   * resource has a parent subscription resource, subscribers of both of them are returned.
   * @param subscriptionResource the instance of subscription resource.
   * @param directives some directive to apply when it is necessary.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods.
   */
  public static SubscriptionSubscriberList getSubscribersOfSubscriptionResource(
      SubscriptionResource subscriptionResource, SubscriberDirective... directives) {
    return getService(subscriptionResource.getInstanceId())
        .getSubscribersOfSubscriptionResource(subscriptionResource, directives);
  }

  /**
   * Gets all the subscribers concerned by the specified contribution in another way than by a
   * subscription to the contribution itself or to one of the resources that contain it. For
   * example, the subscribers to a position on the PdC on which the contribution is classified.<br>
   * These subscribers are provided by all the {@link ContributionSubscribersProvider} available
   * in Silverpeas. They complete those returned by the other methods of this provider, which are
   * all about the subscriptions to the resources handled by a component instance.
   * @param contribution the unique identifier of a contribution.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods. Each subscriber is present only once in the collection.
   */
  public static SubscriptionSubscriberList getSubscribersConcernedBy(
      ContributionIdentifier contribution) {
    final Set<SubscriptionSubscriber> subscribers = new HashSet<>();
    ServiceProvider.getAllServices(ContributionSubscribersProvider.class)
        .forEach(p -> subscribers.addAll(p.getSubscribersOf(contribution)));
    return new SubscriptionSubscriberList(subscribers);
  }

  /**
   * Gets the service implemented by the component if any, the default one otherwise.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @return an instance of {@link ResourceSubscriptionService}.
   */
  private static ResourceSubscriptionService getService(String componentInstanceId) {
    final Optional<SilverpeasComponentInstance> componentInstance = OrganizationController.get()
        .getComponentInstance(componentInstanceId);
    if (componentInstance.isEmpty()) {
      throw new IllegalStateException(
          "Component instance " + componentInstanceId + " is not valid.");
    }
    return componentInstance
        .map(i -> componentImplementations.get(i.getName()))
        .orElseGet(() -> componentImplementations.get(DEFAULT_IMPLEMENTATION_ID));
  }
}
