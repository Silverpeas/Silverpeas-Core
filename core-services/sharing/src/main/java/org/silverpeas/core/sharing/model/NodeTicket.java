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
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.sharing.security.ShareableAccessControl;
import org.silverpeas.core.sharing.security.ShareableNode;
import org.silverpeas.core.sharing.security.ShareableResource;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

/**
 *
 * @author ehugonnet
 */
@Entity
@DiscriminatorValue("Node")
public class NodeTicket extends Ticket {
  private static final long serialVersionUID = 8560572170859334369L;

  public NodeTicket(int sharedObjectId, String componentId, String creatorId, Date creationDate,
      Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creatorId, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = NODE_TYPE;
  }

  protected NodeTicket() {
    this.sharedObjectType = NODE_TYPE;
  }

  @Override
  @Transient
  public ShareableAccessControl getAccessControl() {
    return new NodeAccessControl(this);
  }

  @Override
  @Transient
  @SuppressWarnings("unchecked")
  public ShareableResource<NodeDetail> getResource() {
    NodeService nodeService = NodeService.get();
    NodeDetail node = nodeService.getDetail(new NodePK(String.valueOf(getSharedObjectId()),
        getComponentId()));
    if (node != null) {
      return new ShareableNode(getToken(), node);
    }
    return null;
  }
}
