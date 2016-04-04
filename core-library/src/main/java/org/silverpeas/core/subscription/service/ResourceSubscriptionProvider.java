/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * This common subscription provider can return results from a default {@link
 * ResourceSubscriptionService} implementations that compute basic
 * operations or from an implementation provided by the component itself in order to compute a
 * result that takes into account specific component rules.
 * @author Yohann Chastagnier
 */
public class ResourceSubscriptionProvider {

  // Component implementations.
  private final static Map<String, ResourceSubscriptionService> componentImplementations =
      new HashMap<String, ResourceSubscriptionService>();

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
   * Gets all subscribers registered on a component.<br/>
   * This service does not look at resources handled by the component but just explicit component
   * subscriptions.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods.
   */
  public static SubscriptionSubscriberList getSubscribersOfComponent(String componentInstanceId) {
    return getSubscribersOfComponentAndTypedResource(componentInstanceId,
        SubscriptionResourceType.COMPONENT, null);
  }

  /**
   * Gets all subscribers concerned by a specified resource represented by the
   * given resource type and identifier.<br/>
   * The inheritance of subscription is handled by this method. So if the aimed subscription
   * resource has a parent subscription resource, subscribers of both of them are returned.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @param resourceType the type of the aimed resource.
   * @param resourceId the identifier of the aime resource.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods.
   */
  public static SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      String componentInstanceId, SubscriptionResourceType resourceType, String resourceId) {
    return getService(componentInstanceId)
        .getSubscribersOfComponentAndTypedResource(componentInstanceId, resourceType, resourceId);
  }

  /**
   * Gets all subscribers concerned by a specified subscription resource.<br/>
   * The inheritance of subscription is handled by this method. So if the aimed subscription
   * resource has a parent subscription resource, subscribers of both of them are returned.
   * @param subscriptionResource the instance of subscription resource.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated
   * with useful tool methods.
   */
  public static SubscriptionSubscriberList getSubscribersOfSubscriptionResource(
      SubscriptionResource subscriptionResource) {
    return getService(subscriptionResource.getInstanceId())
        .getSubscribersOfSubscriptionResource(subscriptionResource);
  }

  /**
   * Gets the service implemented by the component if any, the default one otherwise.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @return an instance of {@link ResourceSubscriptionService}.
   */
  private static ResourceSubscriptionService getService(String componentInstanceId) {
    ComponentInstLight componentInstLight =
        OrganizationControllerProvider.getOrganisationController()
            .getComponentInstLight(componentInstanceId);
    if (componentInstLight == null) {
      throw new IllegalStateException(
          "Component instance " + componentInstanceId + " is not valid.");
    }
    ResourceSubscriptionService service =
        componentImplementations.get(componentInstLight.getName());
    if (service == null) {
      service = componentImplementations
          .get(DefaultResourceSubscriptionService.DEFAULT_IMPLEMENTATION_ID);
    }
    return service;
  }
}
