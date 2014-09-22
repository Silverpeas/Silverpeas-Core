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
package org.silverpeas.publication.web;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.sharing.SharingContext;

import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.attachment.web.AttachmentEntity;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.security.ShareableNode;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * A REST Web resource providing access to publications through sharing mode.
 */
@Service
@RequestScoped
@Path("sharing/publications/{token}")
public class SharedPublicationResource extends AbstractPublicationResource {

  @PathParam("token")
  private String token;
  
  private Ticket ticket;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PublicationEntity getPublication() {
    ticket = checkTicket(token);
    
    PublicationPK pk =
        new PublicationPK(String.valueOf(ticket.getSharedObjectId()), ticket.getComponentId());
    
    PublicationDetail publication = getPublicationBm().getDetail(pk);
    
    String baseUri = getUriInfo().getBaseUri().toString();
    SharingContext context = new SharingContext(baseUri, token);
    PublicationEntity entity = super.getPublicationEntity(publication, true).withContent(context);
    setSharedURIToAttachments(entity);
    return entity;
  }
  
  @GET
  @Path("node/{node}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PublicationEntity> getPublications(@PathParam("node") String nodeId,
      @QueryParam("withAttachments") boolean withAttachments) {
    
    this.ticket = checkTicket(token);

    List<PublicationEntity> publications = super.getPublications(nodeId, withAttachments);
    setSharedURIToAttachments(publications);
    return publications;
  }
  
  private void setSharedURIToAttachments(List<PublicationEntity> publications) {
    if (publications != null) {
      for (PublicationEntity publication : publications) {
        setSharedURIToAttachments(publication);
      }
    }
  }
  
  private void setSharedURIToAttachments(PublicationEntity publication) {
    List<AttachmentEntity> attachments = publication.getAttachments();
    if (attachments != null) {
      for (AttachmentEntity attachment : attachments) {
        attachment.withSharedUri(super.getUriInfo().getBaseUri().toString(), token);
      }
    }
  }
    
  @SuppressWarnings("unchecked")
  protected boolean isNodeReadable(NodePK nodePK) {
    nodePK.setComponentName(ticket.getComponentId());
    NodeDetail node = getNodeBm().getDetail(nodePK);
    ShareableNode nodeResource = new ShareableNode(token, node);
    return ticket.getAccessControl().isReadable(nodeResource);
  }
  
  private Ticket checkTicket(String token) {
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(token);
    if (ticket != null) {
      componentId = ticket.getComponentId();
    }
    if (ticket == null || !ticket.isValid()) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    return ticket;
  }
  
}