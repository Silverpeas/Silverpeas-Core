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
package org.silverpeas.core.webapi.node;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.security.ShareableNode;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.node.model.NodeDetail;

/**
 * A REST Web resource providing access to a node through sharing mode.
 */
@Service
@RequestScoped
@Path("sharing/nodes/{componentId}/{token}")
public class SharedNodeResource extends AbstractNodeResource {

  @PathParam("token")
  private String token;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getRoot() {
    return super.getRoot();
  }

  @GET
  @Path("{path: [0-9]+(/[0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getNode(@PathParam("path") String path) {
    return super.getNode(path);
  }

  /**
   * Get all children of any node of the application.
   *
   * @return an array of NodeEntity representing children
   */
  @GET
  @Path("{path: [0-9]+(/[0-9]+)*/children}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity[] getChildren(@PathParam("path") String path) {
    return super.getChildren(path);
  }

  @Override
  protected boolean isNodeReadable(NodeDetail node) {
    ShareableNode nodeResource = new ShareableNode(token, node);
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(token);
    return ticket != null && ticket.getAccessControl().isReadable(nodeResource);
  }

}
