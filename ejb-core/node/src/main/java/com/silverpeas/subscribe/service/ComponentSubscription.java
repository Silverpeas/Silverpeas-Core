/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * @author ehugonnet
 */
public class ComponentSubscription implements Subscription {
  private final String userId;
  private final ForeignPK pk = new ForeignPK(NodePK.ROOT_NODE_ID);

  public ComponentSubscription(String userId, String instanceId) {
    this.userId = userId;
    pk.setSpace(COMPONENT_SUBSCRIPTION);
    pk.setComponentName(instanceId);
  }

  @Override
  public WAPrimaryKey getTopic() {
    return pk;
  }

  @Override
  public String getSubscriber() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ComponentSubscription that = (ComponentSubscription) o;

    if (pk != null ? !pk.equals(that.pk) : that.pk != null) {
      return false;
    }
    if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + (pk != null ? pk.hashCode() : 0);
    return result;
  }

  @Override
  public boolean isComponentSubscription() {
    return true;
  }
}
