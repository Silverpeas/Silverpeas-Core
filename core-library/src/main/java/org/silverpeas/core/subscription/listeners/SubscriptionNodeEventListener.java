/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.subscription.listeners;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.notification.NodeEvent;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;

import javax.inject.Singleton;
import javax.transaction.Transactional;

/**
 * Listener of events on the deletion of a node in a component instance to delete all subscriptions
 * on that node.
 *
 * @author mmoquillon
 */
@Bean
@Singleton
public class SubscriptionNodeEventListener
    extends AbstractProfiledResourceSubscriptionListener<NodeDetail, NodeEvent> {

  @Override
  protected NodeSubscriptionResource getSubscriptionResource(final NodeDetail resource) {
    return NodeSubscriptionResource.from(resource.getNodePK());
  }

  @Override
  protected boolean isSubscriptionEnabled(final NodeDetail resource) {
    return true;
  }

  /**
   * Listens for node move. In the case a node is moved to another component instance, then the
   * subscriptions on this resource are renewed.
   *
   * @param event the event about the move of a node.
   */
  @Transactional
  @Override
  public void onMove(NodeEvent event) {
    var nodeBeforeMove = event.getTransition().getBefore();
    var nodeAfterMove = event.getTransition().getAfter();
    if (isNodeMovedInAnotherApp(nodeBeforeMove, nodeAfterMove)) {
      var service = getSubscriptionService();
      var resource = getSubscriptionResource(nodeAfterMove);
      service.getByResource(getSubscriptionResource(nodeBeforeMove)).forEach(subscription -> {
        var renewedSubscription = new NodeSubscription(subscription.getSubscriber(), resource,
            subscription.getCreatorId());
        service.unsubscribe(subscription);
        service.subscribe(renewedSubscription);
      });

    }
  }

  private boolean isNodeMovedInAnotherApp(NodeDetail nodeBeforeMove, NodeDetail nodeAfterMove) {
    return !nodeBeforeMove.getIdentifier().getComponentInstanceId()
        .equals(nodeAfterMove.getIdentifier().getComponentInstanceId());
  }
}
  