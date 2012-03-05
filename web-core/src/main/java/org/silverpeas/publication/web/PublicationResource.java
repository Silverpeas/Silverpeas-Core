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
package org.silverpeas.publication.web;

import static com.stratelia.webactiv.util.JNDINames.PUBLICATIONBM_EJBHOME;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.silverpeas.rest.RESTWebService;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * A REST Web resource representing a given node.
 * It is a web service that provides an access to a node referenced by its URL.
 */
@Service
@Scope("request")
@Path("publications/{componentId}")
public class PublicationResource extends RESTWebService {
  
  @PathParam("componentId")
  private String componentId;

  @Override
  protected String getComponentId() {
    return componentId;
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PublicationEntity[] getPublications(@QueryParam("node") String nodeId,
      @QueryParam("withAttachments") boolean withAttachments) {
    
    Collection<PublicationDetail> publications;
    try {
      publications = getPublicationBm().getDetailsByFatherPK(getNodePK(nodeId), null, true);
    } catch (RemoteException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    
    String baseUri = getUriInfo().getAbsolutePath().toString();
    
    List<PublicationEntity> entities = new ArrayList<PublicationEntity>();
    for (PublicationDetail publication : publications) {
      URI uri = getURI(publication, baseUri);
      PublicationEntity entity = PublicationEntity.fromPublicationDetail(publication, uri);
      if (withAttachments) {
        Collection<AttachmentDetail> attachments =
            AttachmentController.searchAttachmentByPKAndContext(publication.getPK(), "Images");
        entity.withAttachments(attachments, getUriInfo().getBaseUri().toString());
      }
      entities.add(entity);
    }
    return entities.toArray(new PublicationEntity[0]);
  }
  
  private URI getURI(PublicationDetail publication, String baseUri) {
    URI uri;
    try {
      uri = new URI(baseUri+"/publication/"+publication.getPK().getId());
    } catch (URISyntaxException e) {
      Logger.getLogger(PublicationResource.class.getName()).log(Level.SEVERE, null, e);
      throw new RuntimeException(e.getMessage(), e);
    }
    return uri;
  }
  
  private NodePK getNodePK(String nodeId) {
    return new NodePK(nodeId, getComponentId());
  }
  
  private PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome =
          EJBUtilitaire.getEJBObjectRef(PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return publicationBm;
  }

}
