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
  @Path("node/{nodeId}")
  @Produces(MediaType.APPLICATION_JSON)
  public PublicationEntity[] getPublications(@PathParam("nodeId") String nodeId) {
    Collection<PublicationDetail> publications;
    try {
      publications = getPublicationBm().getDetailsByFatherPK(getNodePK(nodeId), null, true);
    } catch (RemoteException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    
    String requestUri = getUriInfo().getRequestUri().toString();
    String baseUri = requestUri.substring(0, requestUri.indexOf("/node"));
    
    List<PublicationEntity> entities = new ArrayList<PublicationEntity>();
    for (PublicationDetail publication : publications) {
      URI uri;
      try {
        uri = new URI(baseUri+"/publication/"+publication.getPK().getId());
      } catch (URISyntaxException e) {
        Logger.getLogger(PublicationResource.class.getName()).log(Level.SEVERE, null, e);
        throw new RuntimeException(e.getMessage(), e);
      }
      PublicationEntity entity = PublicationEntity.fromPublicationDetail(publication, uri);
      Collection<AttachmentDetail> attachments = AttachmentController.searchAttachmentByPKAndContext(publication.
          getPK(), "Images");
      entity.withAttachments(attachments);
      entities.add(entity);
    }
    return entities.toArray(new PublicationEntity[0]);
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
