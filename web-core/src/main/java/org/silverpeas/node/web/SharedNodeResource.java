/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.node.web;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.security.ShareableNode;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
 * A REST Web resource providing access to a node through sharing mode.
 */
@Service
@RequestScoped
@Path("sharing/nodes/{componentId}/{token}")
public class SharedNodeResource extends AbstractNodeResource {

  @PathParam("token")
  private String token;

  @Override
  @SuppressWarnings("unchecked")
  protected boolean isNodeReadable(NodeDetail node) {
    ShareableNode nodeResource = new ShareableNode(token, node);
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(token);
    return ticket != null && ticket.getAccessControl().isReadable(nodeResource);
  }

}
