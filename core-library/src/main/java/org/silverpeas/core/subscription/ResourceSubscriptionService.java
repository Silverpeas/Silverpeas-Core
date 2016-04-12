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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

/**
 * This interface defines some services for which it exists one default implementations and
 * potentially one per component.
 * @author Yohann Chastagnier
 */
public interface ResourceSubscriptionService {

  /**
   * Gets all subscribers registered on a component.<br/>
   * This service does not look at resources handled by the component but just explicit component
   * subscriptions.
   * @param componentInstanceId the identifier of the component instance from which subscription
   * are requested.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated with useful tool methods.
   */
  SubscriptionSubscriberList getSubscribersOfComponent(String componentInstanceId);

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
   * represents a collection of {@link SubscriptionSubscriber} decorated with useful tool methods.
   */
  SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(String componentInstanceId,
      SubscriptionResourceType resourceType, String resourceId);

  /**
   * Gets all subscribers concerned by a specified subscription resource.<br/>
   * The inheritance of subscription is handled by this method. So if the aimed subscription
   * resource has a parent subscription resource, subscribers of both of them are returned.
   * @param subscriptionResource the instance of subscription resource.
   * @return an instance of {@link SubscriptionSubscriberList} that
   * represents a collection of {@link SubscriptionSubscriber} decorated with useful tool methods.
   */
  SubscriptionSubscriberList getSubscribersOfSubscriptionResource(
      SubscriptionResource subscriptionResource);
}
