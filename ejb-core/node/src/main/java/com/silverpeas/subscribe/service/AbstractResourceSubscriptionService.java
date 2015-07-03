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
package com.silverpeas.subscribe.service;

import com.silverpeas.subscribe.ResourceSubscriptionService;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;

import static com.silverpeas.subscribe.SubscriptionServiceProvider.getSubscribeService;
import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractResourceSubscriptionService implements ResourceSubscriptionService {

  @PostConstruct
  final protected void register() {
    ResourceSubscriptionProvider
        .registerResourceSubscriptionService(this);
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

    Collection<SubscriptionSubscriber> subscribers = new HashSet<SubscriptionSubscriber>();

    switch (resourceType) {
      case FORUM_MESSAGE:
      case FORUM:
        // nothing is done here, explicit component implementation must exist.
        break;
      case NODE:
        Collection<NodeDetail> path = null;
        if (!"kmax".equals(componentInstanceId)) {
          path = getNodeBm().getPath(new NodePK(resourceId, componentInstanceId));
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

  protected NodeBm getNodeBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBm.class);
    } catch (final Exception e) {
      throw new SubscribeRuntimeException("DefaultResourceSubscriptionService.getNodeBm()", ERROR,
          "subscribe.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
  }
}
