/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import javax.ws.rs.PUT;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import java.net.URI;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.rest.RESTWebService;
import com.stratelia.silverpeas.pdc.model.PdcException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import static com.silverpeas.pdc.model.PdcClassification.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;
import static com.silverpeas.util.StringUtil.isDefined;

/**
 * A REST Web resource that represents the predefined classifications on the PdC to classify the
 * contents that are published into a given node of a given component instance or in a whole
 * component instance.
 * 
 * A predefined classification on the PdC can be created and attached either to a component instance
 * or to a a node of a component instance. It then can be used as a default classification to
 * automatically classify the contents or as a classification template from which the contents can
 * be classified on the PdC.
 * 
 * A node in a component instance is a generic way in Silverpeas to categorize hierarchically
 * the contents; they are divided into a tree of nodes. A node can represent a topic, a tag or
 * a folder for example.
 * 
 * A classification on the PdC is defined by a set of different positions on the axis of the PdC.
 * A position is a set of one or more values of axis. A classification can be modifiable or not.
 * By default, a predefined classification is set as unmodifiable whereas the classification of a
 * content is modifiable by default.
 * 
 * The positions of a given classification can be accessed with this Web resource by the URI of the
 * position; classifications and positions are exposed in the Web by Silverpeas and are thus
 * uniquely identified by an URI in the Web.
 */
@Service
@Scope("request")
@Path("pdc/{componentId}/classification")
public class PdcPredefinedClassificationResource extends RESTWebService {

  @Inject
  private PdcServiceProvider pdcServiceProvider;
  @PathParam("componentId")
  private String componentId;

  @Override
  protected String getComponentId() {
    return componentId;
  }

  /**
   * Gets the predefined classification on the PdC that is set for the contents in the node
   * identified by the query part of the request URI. If no node identifier is provided in the URI,
   * the predefined classification set for the whole component instance is seeked.
   * 
   * A node in a component instance is a generic way in Silverpeas to categorize hierarchically the
   * contents of the component instance. If no predefined classification onto the PdC is defined
   * for the requested node, a predefined one is then looked backward among the parent
   * nodes up to the component instance itself.
   * 
   * The PdC classification is sent back in JSON.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the requested resource, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return a web entity representing the requested predefined PdC classification. If no predefined 
   * classification is defined along the path of the nodes up to the component instance, then an
   * empty classification is sent back.
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public PdcClassificationEntity getPredefinedPdCClassificationForContentsInNode(
          @QueryParam("nodeId") String nodeId) {
    checkUserPriviledges();
    try {
      return asWebEntity(pdcServiceProvider().getPredefinedClassificationForContentsIn(
              nodeId, getComponentId()), identifiedBy(getUriInfo().getRequestUri()));
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Adds a new predefined position on the PdC for the content that will be published at the specified
   * URI (the URI identifies either a given node of a given component instance or a given component
   * instance).
   * If the JSON representation of the position isn't correct (no values), then a 400 HTTP code is
   * returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the comment, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param newPosition a web entity representing the PdC position to add. The entity is passed 
   * within the request and it is serialized in JSON.
   * @return the response with the status of the position adding and, in the case of a successful
   * operation, the new PdC classification of the resource resulting of the position adding.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addPdcPosition(@QueryParam("nodeId") String nodeId,
          final PdcPositionEntity newPosition) {
    checkUserPriviledges();
    if (newPosition.getPositionValues().isEmpty()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    try {
      PdcPosition positionToAdd = newPosition.toPdcPosition();
      PdcClassification classification = getAnExistingOrCreateANewClassificationFor(nodeId,
              getComponentId());
      classification.getPositions().add(positionToAdd);
      classification = pdcServiceProvider().saveOrUpdatePredefinedClassification(classification);
      String positionId = positionToAdd.getId();
      URI newPositionURI = getUriInfo().getRequestUriBuilder().path(positionId).build();
      return Response.created(newPositionURI).entity(asWebEntity(classification, identifiedBy(
              getUriInfo().getRequestUri()))).build();
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Deletes the specified existing position by its unique identifier.
   * If the PdC position doesn't exist, nothing is done, so that the HTTP DELETE request remains
   * indempotent as defined in the HTTP specification.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the resource PdC classification, a 403 is returned.
   * If the position is the single one for the content on the PdC and the PdC contains at least one
   * mandatory axis, a 409 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param positionId the unique identifier of the position to delete in the predefined
   * classification refered by the URI.
   */
  @DELETE
  @Path("{positionId}")
  public void deletePdcPosition(@QueryParam("nodeId") String nodeId,
          @PathParam("positionId") String positionId) {
    checkUserPriviledges();
    try {
      PdcClassification classification =
              pdcServiceProvider().getPredefinedClassificationForContentsIn(nodeId, getComponentId());
      PdcPosition positionToDelete = getPdcPositionById(positionId, classification);
      classification.getPositions().remove(positionToDelete);
      classification = pdcServiceProvider().saveOrUpdatePredefinedClassification(classification);
    } catch (WebApplicationException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexisting position of id {0} in"
                + " the predefined classification for node {1} and for component instance {2}",
                new Object[]{positionId, nodeId, getComponentId()});
    } catch (PdcException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates an existing position on the PdC in the predefined classification refered by the
   * requested URI.
   * If the JSON representation of the position isn't correct (no values), then a 400 HTTP code is
   * returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the comment, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param modifiedPosition a web entity representing the new state of the PdC position to update.
   * The entity is passed within the request and it is serialized in JSON.
   * @return the response with the status of the position update and, in the case of a successful
   * operation, the new PdC classification of the resource resulting of the position update.
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{positionId}")
  public PdcClassificationEntity updatePdcPosition(@QueryParam("nodeId") String nodeId,
          @PathParam("positionId") String positionId, final PdcPositionEntity modifiedPosition) {
    checkUserPriviledges();
    if (!positionId.equals(modifiedPosition.getId())
            || modifiedPosition.getPositionValues().isEmpty()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    try {
      PdcClassification classification =
              pdcServiceProvider().getPredefinedClassificationForContentsIn(nodeId, getComponentId());
      PdcPosition positionToUpdate = getPdcPositionById(positionId, classification);
      classification.getPositions().remove(positionToUpdate);
      classification.getPositions().add(modifiedPosition.toPdcPosition());
      classification = pdcServiceProvider().saveOrUpdatePredefinedClassification(classification);
      UriBuilder uri = getUriInfo().getBaseUriBuilder().path("pdc/{componentId}/classification");
      if (isDefined(nodeId)) {
        uri.queryParam("nodeId", nodeId);
      }
      return asWebEntity(classification, identifiedBy(uri.build(getComponentId())));
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private PdcClassification getAnExistingOrCreateANewClassificationFor(String nodeId,
          String componentId) throws PdcException {
    PdcClassification classification = pdcServiceProvider().
            getPredefinedClassificationForContentsIn(nodeId, getComponentId());
    if (classification == PdcClassification.NONE_CLASSIFICATION) {
      // by default a predefined classification cannot be modified when used to classify new contents.
      classification = aPredefinedPdcClassificationForComponentInstance(componentId).forNode(nodeId);
    }
    return classification;
  }

  private PdcPosition getPdcPositionById(String positionId, final PdcClassification classification) {
    PdcPosition positionToFind = null;
    for (PdcPosition position : classification.getPositions()) {
      if (positionId.equals(position.getId())) {
        positionToFind = position;
        break;
      }
    }
    if (positionToFind == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return positionToFind;
  }

  private PdcClassificationEntity asWebEntity(final PdcClassification classification, URI uri) throws
          Exception {
    if (classification == PdcClassification.NONE_CLASSIFICATION) {
      return undefinedClassification();
    } else {
      UserPreferences userPreferences = getUserPreferences();
      PdcClassificationEntity theClassificationEntity = aPdcClassificationEntity(
              fromPdcClassification(classification),
              inLanguage(userPreferences.getLanguage()),
              atURI(uri));
      if (userPreferences.isThesaurusEnabled()) {
        UserThesaurusHolder theUserThesaurus =
                pdcServiceProvider().getThesaurusOfUser(getUserDetail());
        theClassificationEntity.withSynonymsFrom(theUserThesaurus);
      }
      return theClassificationEntity;
    }
  }

  private PdcServiceProvider pdcServiceProvider() {
    return pdcServiceProvider;
  }
  
  private static URI identifiedBy(final URI uri) {
    return uri;
  }
}
