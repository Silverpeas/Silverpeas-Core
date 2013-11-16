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
package org.silverpeas.subscription.bean;

import com.silverpeas.subscribe.Subscription;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 25/02/13
 */
public class NodeSubscriptionBean extends AbstractSubscriptionBean {

  private final Collection<NodeDetail> path;

  public NodeSubscriptionBean(final Subscription subscription, final Collection<NodeDetail> path,
      final ComponentInstLight component, final String language) {
    super(subscription, component, language);
    this.path = path;
  }

  @Override
  public String getPath() {
    StringBuilder result = new StringBuilder();
    for (NodeDetail node : path) {
      if (result.length() > 0) {
        result.insert(0, " > ");
      }
      if (NodePK.ROOT_NODE_ID.equals(node.getNodePK().getId())) {
        result.insert(0, super.getPath());
      } else {
        result.insert(0, node.getName(getLanguage()));
      }
    }
    return result.toString();
  }

  @Override
  public String getLink() {
    return path.iterator().next().getLink();
  }
}
