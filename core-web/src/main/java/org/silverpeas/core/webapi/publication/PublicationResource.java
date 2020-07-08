/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.publication;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.PublicationLink;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.NodeAccessController;
import org.silverpeas.core.security.authorization.PublicationAccessController;
import org.silverpeas.core.webapi.attachment.AttachmentEntity;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * A REST Web resource providing access to publications through private mode.
 */
@WebService
@Path(PublicationResource.PATH + "/{componentId}")
@Authorized
public class PublicationResource extends AbstractPublicationResource {

  static final String PATH = "private/publications";

  @PathParam("componentId")
  protected String componentId;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  @Inject
  private NodeAccessController nodeAccessController;

  @Inject
  private PublicationAccessController publicationAccessController;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<PublicationEntity> getPublications(@QueryParam("node") String nodeId,
      @QueryParam("withAttachments") boolean withAttachments) {
    List<PublicationEntity> publications = super.getPublications(nodeId, withAttachments);
    setURIToAttachments(publications);
    return publications;
  }

  @DELETE
  @Path("{pubId}/links/{linkId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteLink(@PathParam("pubId") String pubId, @PathParam("linkId") String linkId) {

    PublicationPK pk = new PublicationPK(pubId, componentId);

    //Checking publication exists
    CompletePublication publication = getPublicationService().getCompletePublication(pk);
    if (publication == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    //Checking publication is modified by user
    if (!publicationAccessController.isUserAuthorized(getUser().getId(), pk, AccessControlContext
        .init().onOperationsOf(AccessControlOperation.modification))) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    List<PublicationLink> links = publication.getLinkList();
    for (PublicationLink link : links) {
      if (link.getId().equals(linkId)) {
        getPublicationService().deleteLink(linkId);
        return Response.ok().build();
      }
    }

    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  private void setURIToAttachments(List<PublicationEntity> publications) {
    if (publications != null) {
      for (PublicationEntity publication : publications) {
        List<AttachmentEntity> attachments = publication.getAttachments();
        if (attachments != null) {
          for (AttachmentEntity attachment : attachments) {
            attachment.withBaseUri(getUri().getBaseUri().toString());
          }
        }
      }
    }
  }

  @Override
  protected boolean isNodeReadable(NodePK nodePK) {
    return nodeAccessController.isUserAuthorized(super.getUser().getId(), nodePK);
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }
}