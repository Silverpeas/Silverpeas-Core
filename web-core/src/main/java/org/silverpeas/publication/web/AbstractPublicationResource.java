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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.JNDINames.PUBLICATIONBM_EJBHOME;

/**
 * A REST Web resource providing access to publications.
 */
public abstract class AbstractPublicationResource extends RESTWebService {

  @PathParam("componentId")
  protected String componentId;
  
  @Override
  public String getComponentId() {
    return componentId;
  }
  
  /**
   * Gets the nodes that are children of a parent node.
   * 
   * @param nodeId The ID of the parent node.
   * @param withAttachments Indicated whether attachments related to publications are required.
   * @return An array of the nodes whose parent is the node matching the specified ID.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PublicationEntity[] getPublications(@QueryParam("node") String nodeId,
      @QueryParam("withAttachments") boolean withAttachments) {

    NodePK nodePK = getNodePK(nodeId);
    if (!isNodeReadable(nodePK)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }

    Collection<PublicationDetail> publications = getPublicationBm().getDetailsByFatherPK(
        nodePK, null, true);

    String baseUri = getUriInfo().getAbsolutePath().toString();

    List<PublicationEntity> entities = new ArrayList<PublicationEntity>();
    for (PublicationDetail publication : publications) {
      if (publication.isValid()) {
        URI uri = getURI(publication, baseUri);
        PublicationEntity entity = fromPublicationDetail(publication, uri);
        if (withAttachments) {
          Collection<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService()
              .listDocumentsByForeignKey(publication.getPK(), null);
          entity.withAttachments(attachments, getUriInfo().getBaseUri().toString(), getToken());
        }
        entities.add(entity);
      }
    }
    return entities.toArray(new PublicationEntity[entities.size()]);
  }
  
  protected abstract String getToken();
  
  protected abstract boolean isNodeReadable(NodePK nodePK);
  
  protected abstract PublicationEntity fromPublicationDetail(
      final PublicationDetail publication, URI uri);
  
  private NodePK getNodePK(String nodeId) {
    return new NodePK(nodeId, getComponentId());
  }
  
  private URI getURI(PublicationDetail publication, String baseUri) {
    URI uri;
    try {
      uri = new URI(baseUri + "/publication/" + publication.getPK().getId());
    } catch (URISyntaxException e) {
      Logger.getLogger(PublicationResource.class.getName()).log(Level.SEVERE, null, e);
      throw new RuntimeException(e.getMessage(), e);
    }
    return uri;
  }
  
  protected NodeBm getNodeBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  protected PublicationBm getPublicationBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(PUBLICATIONBM_EJBHOME, PublicationBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }
  
}
