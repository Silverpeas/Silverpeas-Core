/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.notification.NodeEvent;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;

import javax.inject.Singleton;

/**
 * Listener of events on the deletion of a node in a component instance to delete all subscriptions
 * on that node.
 * @author mmoquillon
 */
@Singleton
public class SubscriptionNodeEventListener
    extends AbstractProfiledResourceSubscriptionListener<NodeDetail, NodeEvent> {

  @Override
  protected SubscriptionResource getSubscriptionResource(final NodeDetail resource) {
    return NodeSubscriptionResource.from(resource.getNodePK());
  }
}
  