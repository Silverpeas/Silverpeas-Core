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
package org.silverpeas.core.webapi.node;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.security.AccessControlContext;
import org.silverpeas.core.sharing.services.SharingServiceProvider;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * A REST Web resource providing access to a node through sharing mode.
 */
@WebService
@Path(SharedNodeResource.PATH + "/{componentId}/{token}")
public class SharedNodeResource extends AbstractNodeResource {

  static final String PATH = "sharing/nodes";

  @PathParam("token")
  private String token;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  protected boolean isNodeReadable(NodeDetail node) {
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(token);
    var ctx = AccessControlContext.about(node);
    return ticket != null && ticket.getAccessControl().isReadable(ctx);
  }

}
