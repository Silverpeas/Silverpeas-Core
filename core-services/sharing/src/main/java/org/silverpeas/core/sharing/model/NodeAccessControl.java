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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.sharing.security.AbstractShareableAccessControl;
import org.silverpeas.core.sharing.security.AccessControlContext;

import java.util.Collection;

/**
 * Access control of shared nodes and their content (publications and their attachments).
 */
public class NodeAccessControl extends AbstractShareableAccessControl {

  NodeAccessControl(Ticket ticket) {
    super(ticket);
  }

  @Override
  public boolean isReadable(AccessControlContext context) {
    NodePK nodePk = new NodePK(String.valueOf(getSharingTicket().getSharedObjectId()),
        getSharingTicket().getComponentId());
    Collection<NodePK> authorizedNodes = getNodeDescendants(nodePk);
    authorizedNodes.add(nodePk);
    if (context.isAboutDocument()) {
      SimpleDocument attachment = context.getDocument();
      return isPublicationReadable(new ResourceReference(attachment.
          getForeignId(), attachment.getInstanceId()), nodePk.getInstanceId(), authorizedNodes);
    }
    NodeDetail node = context.getNode();
    return authorizedNodes.contains(node.getNodePK());
  }

  protected Collection<NodePK> getPublicationFathers(ResourceReference pk) {
    return getPublicationService().getAllFatherPKInSamePublicationComponentInstance(new PublicationPK(pk.getId(), pk.getInstanceId()));
  }

  protected Collection<Location> getPublicationLocations(ResourceReference pk) {
    return getPublicationService().getAllLocations(new PublicationPK(pk.getId(),
        pk.getInstanceId()));
  }

  protected Collection<NodePK> getNodeDescendants(NodePK pk) {
    return getNodeService().getDescendantPKs(pk);
  }

  private boolean isPublicationReadable(ResourceReference pk, String instanceId,
      Collection<NodePK> authorizedNodes) {
    if (pk.getInstanceId().equals(instanceId)) {
      Collection<NodePK> fathers = getPublicationFathers(pk);
      return authorizedNodes.stream().anyMatch(fathers::contains);
    } else {
      // special case of an alias between two ECM applications
      // check if publication which contains attachment is an alias into this node
      Collection<Location> locations = getPublicationLocations(pk);
      for (Location location : locations) {
        NodePK father = new NodePK(location.getId(), location.getInstanceId());
        if (!location.isAlias() && authorizedNodes.contains(father)) {
          return true;
        }
      }
    }
    return false;
  }

  private PublicationService getPublicationService() {
    return PublicationService.get();
  }

  private NodeService getNodeService() {
    return NodeService.get();
  }
}
