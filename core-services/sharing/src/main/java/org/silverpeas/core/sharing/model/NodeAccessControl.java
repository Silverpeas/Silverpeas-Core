/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.sharing.security.AbstractShareableAccessControl;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.WAPrimaryKey;

import javax.ejb.CreateException;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Access control to shared nodes and their content.
 */
public class NodeAccessControl<R> extends AbstractShareableAccessControl<NodeTicket, R> {

  NodeAccessControl() {
    super();
  }

  @Override
  protected boolean isReadable(NodeTicket ticket, R accessedObject) throws Exception {
    NodePK nodePk = new NodePK(String.valueOf(ticket.getSharedObjectId()), ticket.getComponentId());
    Collection<NodePK> autorizedNodes = getNodeDescendants(nodePk);
    autorizedNodes.add(nodePk);
    if (accessedObject instanceof SimpleDocument) {
      SimpleDocument attachment = (SimpleDocument) accessedObject;
      return isPublicationReadable(new ForeignPK(attachment.
          getForeignId(), attachment.getInstanceId()), nodePk.getInstanceId(), autorizedNodes);
    }
    if (accessedObject instanceof NodeDetail) {
      NodeDetail node = (NodeDetail) accessedObject;
      return autorizedNodes.contains(node.getNodePK());
    }
    return false;
  }

  protected Collection<NodePK> getPublicationFathers(WAPrimaryKey pk)
      throws CreateException, RemoteException {
    return getPublicationBm().getAllFatherPK(new PublicationPK(pk.getId(), pk.getInstanceId()));
  }

  protected Collection<Alias> getPublicationAliases(WAPrimaryKey pk)
      throws CreateException, RemoteException {
    return getPublicationBm().getAlias(new PublicationPK(pk.getId(), pk.getInstanceId()));
  }

  protected Collection<NodePK> getNodeDescendants(NodePK pk)
      throws CreateException, RemoteException {
    return getNodeService().getDescendantPKs(pk);
  }

  private boolean isPublicationReadable(WAPrimaryKey pk, String instanceId,
      Collection<NodePK> authorizedNodes) throws RemoteException, CreateException {
    if (pk.getInstanceId().equals(instanceId)) {
      Collection<NodePK> fathers = getPublicationFathers(pk);
      return authorizedNodes.stream()
          .filter(node -> fathers.contains(node))
          .findFirst()
          .isPresent();
    } else {
      // special case of an alias between two ECM applications
      // check if publication which contains attachment is an alias into this node
      Collection<Alias> aliases = getPublicationAliases(pk);
      for (Alias alias : aliases) {
        NodePK aliasPK = new NodePK(alias.getId(), alias.getInstanceId());
        if (authorizedNodes.contains(aliasPK)) {
          return true;
        }
      }
    }
    return false;
  }

  private PublicationService getPublicationBm() {
    return ServiceProvider.getService(PublicationService.class);
  }

  private NodeService getNodeService() {
    return NodeService.get();
  }
}
