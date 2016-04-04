/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.sharing;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static org.silverpeas.core.util.logging.SilverLogger.*;

@Service
@RequestScoped
@Path("sharing/{token}")
public class SharingResource extends RESTWebService {

  @PathParam("token")
  private String token;

  @Override
  public String getComponentId() {
    return null;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SharingEntity getSharing() {
    String baseUri = getUriInfo().getBaseUri().toString();
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(token);
    if (ticket == null || !ticket.isValid()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    URI webApplicationRootUri = getWebApplicationRootUri(baseUri, ticket.getComponentId(),
        String.valueOf(ticket.getSharedObjectId()));
    return new SharingEntity(getUriInfo().getRequestUri(), webApplicationRootUri, ticket);
  }

  private URI getWebApplicationRootUri(String baseUri, String componentId, String nodeId) {
    URI uri;
    try {
      uri = new URI(baseUri + "sharing/nodes/" + componentId + "/" + token + "/" + nodeId);
    } catch (URISyntaxException ex) {
      getLogger(this).error(ex.getMessage(), ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
    return uri;
  }

}
