/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.publication;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.attachment.AttachmentEntity;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.NodeAccessController;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;

/**
 * A REST Web resource providing access to publications through private mode.
 */
@Service
@RequestScoped
@Path("private/publications/{componentId}")
@Authorized
public class PublicationResource extends AbstractPublicationResource {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  @Inject
  private NodeAccessController nodeAccessController;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<PublicationEntity> getPublications(@QueryParam("node") String nodeId,
      @QueryParam("withAttachments") boolean withAttachments) {
    List<PublicationEntity> publications = super.getPublications(nodeId, withAttachments);
    setURIToAttachments(publications);
    return publications;
  }

  private void setURIToAttachments(List<PublicationEntity> publications) {
    if (publications != null) {
      for (PublicationEntity publication : publications) {
        List<AttachmentEntity> attachments = publication.getAttachments();
        if (attachments != null) {
          for (AttachmentEntity attachment : attachments) {
            attachment.withUri(super.getUriInfo().getBaseUri().toString());
          }
        }
      }
    }
  }

  @Override
  protected boolean isNodeReadable(NodePK nodePK) {
    return nodeAccessController.isUserAuthorized(super.getUserDetail().getId(), nodePK);
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }
}