/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.node.model.NodePK;

/**
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public class ComponentSubscriptionResource extends AbstractSubscriptionResource<ForeignPK> {

  /**
   * A way to get an instance of a component subscription resource.
   * @param instanceId
   * @return
   */
  public static ComponentSubscriptionResource from(String instanceId) {
    return new ComponentSubscriptionResource(instanceId);
  }

  /**
   * Default constructor.
   * @param instanceId
   */
  protected ComponentSubscriptionResource(String instanceId) {
    super(NodePK.ROOT_NODE_ID, SubscriptionResourceType.COMPONENT,
        new ForeignPK(NodePK.ROOT_NODE_ID));
    getPK().setSpace("component");
    getPK().setComponentName(instanceId);
  }

  /**
   * Gets the Silverpeas Primary Key of the aimed resource
   * @return
   */
  @Override
  public ForeignPK getPK() {
    return super.getPK();
  }
}
