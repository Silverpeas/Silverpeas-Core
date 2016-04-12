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
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.initialization.Initialization;

import java.util.Collection;
import java.util.HashSet;

import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractResourceSubscriptionService implements ResourceSubscriptionService,
    Initialization {

  @Override
  public void init() throws Exception {
    ResourceSubscriptionProvider.registerResourceSubscriptionService(this);
  }

  /**
   * Gets the name of the component that the implementation handles.
   * @return the name of the component handled by the implementation.
   */
  protected abstract String getHandledComponentName();

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponent(final String componentInstanceId) {
    return getSubscribersOfComponentAndTypedResource(componentInstanceId,
        SubscriptionResourceType.COMPONENT, null);
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfSubscriptionResource(
      final SubscriptionResource subscriptionResource) {
    return getSubscribersOfComponentAndTypedResource(subscriptionResource.getInstanceId(),
        subscriptionResource.getType(), subscriptionResource.getId());
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId) {

    Collection<SubscriptionSubscriber> subscribers = new HashSet<>();

    switch (resourceType) {
      case FORUM_MESSAGE:
      case FORUM:
        // nothing is done here, explicit component implementation must exist.
        break;
      case NODE:
        Collection<NodeDetail> path = null;
        if (!"kmax".equals(componentInstanceId)) {
          path = getNodeService().getPath(new NodePK(resourceId, componentInstanceId));
        }
        if (path != null) {
          for (final NodeDetail descendant : path) {
            subscribers.addAll(getSubscribeService()
                .getSubscribers(NodeSubscriptionResource.from(descendant.getNodePK())));
          }
        }
      case COMPONENT:
        subscribers.addAll(getSubscribeService()
            .getSubscribers(ComponentSubscriptionResource.from(componentInstanceId)));
    }

    return new SubscriptionSubscriberList(subscribers);
  }

  protected NodeService getNodeService() {
    return NodeService.get();
  }
}
