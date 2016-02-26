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
package com.silverpeas.subscribe.web;

import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.subscribe.service.AbstractResourceSubscriptionService;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.service.DefaultResourceSubscriptionService;
import com.silverpeas.subscribe.service.NodeSubscriptionResource;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.stratelia.webactiv.node.model.NodePK;

import java.util.Collection;
import java.util.HashSet;

import static com.silverpeas.subscribe.SubscriptionServiceProvider.getSubscribeService;

/**
* @author Yohann Chastagnier
*/
public class StubbedDefaultResourceSubscriptionService
    extends AbstractResourceSubscriptionService {

  @Override
  protected String getHandledComponentName() {
    return DefaultResourceSubscriptionService.DEFAULT_IMPLEMENTATION_ID;
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId) {

    Collection<SubscriptionSubscriber> subscribers = new HashSet<SubscriptionSubscriber>();

    switch (resourceType) {
      case FORUM_MESSAGE:
      case FORUM:
        // nothing is done here, explicit component implementation must exist.
        break;
      case NODE:
        subscribers.addAll(getSubscribeService().getSubscribers(
            NodeSubscriptionResource.from(new NodePK(resourceId, componentInstanceId))));
      case COMPONENT:
        subscribers.addAll(getSubscribeService()
            .getSubscribers(ComponentSubscriptionResource.from(componentInstanceId)));
    }

    return new SubscriptionSubscriberList(subscribers);
  }
}
