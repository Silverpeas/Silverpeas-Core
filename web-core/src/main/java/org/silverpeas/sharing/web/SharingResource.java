/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.sharing.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.silverpeas.publication.web.PublicationResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.silverpeas.rest.RESTWebService;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingServiceFactory;

@Service
@Scope("request")
@Path("sharing/{token}")
public class SharingResource extends RESTWebService {
  
  @PathParam("token")
  private String token;

  @Override
  protected String getComponentId() {
    return null;
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SharingEntity getSharing() {
    String baseUri = getUriInfo().getBaseUri().toString();
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(token);
    URI webApplicationRootUri = getWebApplicationRootUri(baseUri, ticket.getComponentId(), String.valueOf(ticket.getSharedObjectId()));
    Date expiration = ticket.getEndDate();
    return new SharingEntity(getUriInfo().getRequestUri(), webApplicationRootUri, expiration);
  }
  
  private URI getWebApplicationRootUri(String baseUri, String componentId, String nodeId) {
    URI uri;
    try {
      uri = new URI(baseUri+"nodes/"+componentId+"/"+token+"/"+nodeId);
    } catch (URISyntaxException e) {
      Logger.getLogger(PublicationResource.class.getName()).log(Level.SEVERE, null, e);
      throw new RuntimeException(e.getMessage(), e);
    }
    return uri;
  }

}
