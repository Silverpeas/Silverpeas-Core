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

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

import java.util.Collection;
import java.util.HashSet;

import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.NODE;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractResourceSubscriptionService implements ResourceSubscriptionService,
    Initialization {

  @Override
  public void init() {
    ResourceSubscriptionProvider.registerResourceSubscriptionService(this);
  }

  /**
   * Gets the name of the component that the implementation handles.
   * @return the name of the component handled by the implementation.
   */
  protected abstract String getHandledComponentName();

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponent(final String componentInstanceId) {
    return getSubscribersOfComponentAndTypedResource(componentInstanceId, COMPONENT, null);
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfSubscriptionResource(
      final SubscriptionResource subscriptionResource, final SubscriberDirective... directives) {
    return getSubscribersOfComponentAndTypedResource(subscriptionResource.getInstanceId(),
        subscriptionResource.getType(), subscriptionResource.getId(), directives);
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId, final SubscriberDirective... directives) {
    final Collection<SubscriptionSubscriber> subscribers = new HashSet<>();
    // nothing is done here about other types, explicit component implementation MUST exist.
    if (NODE == resourceType) {
      final NodePath path = !"kmax".equals(componentInstanceId) ? getNodeService()
          .getPath(new NodePK(resourceId, componentInstanceId)) : null;
      addAllSubscribersAboutNodePath(path, subscribers);
      addAllSubscribersAboutComponentInstance(componentInstanceId, subscribers);
    } else if (COMPONENT == resourceType) {
      addAllSubscribersAboutComponentInstance(componentInstanceId, subscribers);
    }
    return new SubscriptionSubscriberList(subscribers);
  }

  private void addAllSubscribersAboutComponentInstance(final String componentInstanceId,
      final Collection<SubscriptionSubscriber> subscribers) {
    subscribers.addAll(getSubscribeService()
        .getSubscribers(ComponentSubscriptionResource.from(componentInstanceId)));
  }

  protected void addAllSubscribersAboutNodePath(final NodePath nodePath,
      final Collection<SubscriptionSubscriber> subscribers) {
    if (nodePath != null) {
      for (final NodeDetail node : nodePath) {
        subscribers.addAll(getSubscribeService()
            .getSubscribers(NodeSubscriptionResource.from(node.getNodePK())));
      }
    }
  }

  protected NodeService getNodeService() {
    return NodeService.get();
  }
}
