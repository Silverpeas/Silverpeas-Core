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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import static com.silverpeas.pdc.model.PdcClassification.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;

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
 * A predefined classification associated with a given node or with a given component instance
 * follows the hierarchical structure of the node tree; it is also applicable to all contents in the
 * children of the given node (or of all nodes in the component instance) in the case they aren't
 * associated explictly with a predefined classification. So, when classifying on the PdC of a content
 * published in a given node, a predefined classification is then looked for backward in the
 * hierarchical tree of nodes, from the given node upto the component instance itself; once found,
 * this predefined classification will be used to classify the content. Similarly, when editing
 * a predefined classification associated with a node, it is seeked backward in the hierarchical
 * tree of nodes but the predefined classification found will be modified only for the given node.
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

  private static final String BASE_URI_PATH = "pdc/{componentId}/classification";
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
   * contents of the component instance. If no predefined classification on the PdC is defined
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
      PdcClassification theClassification = getAnExistingOrCreateANewClassificationFor(nodeId,
              getComponentId());
      return asWebEntity(theClassification, identifiedBy(theUriOf(theClassification)));
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
   * 
   * If no predefined classification is associated with the specified node, it inherits of the 
   * predefined classification of its closest parent node. So, as a new predefined position on the
   * PdC is added for the contents of the specified node (and not for thoses of the parent node), 
   * it is actually added to the new predefined classification that is created for the specified node
   * from of the one of the parent node.
   * 
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
      PdcClassification savedClassification = pdcServiceProvider().
              saveOrUpdatePredefinedClassification(classification);
      PdcPosition addedPosition = getAddedPosition(savedClassification, classification);
      URI positionURI = theUriOf(addedPosition, in(savedClassification));
      return Response.created(positionURI).entity(asWebEntity(savedClassification, identifiedBy(
              theUriOf(savedClassification)))).build();
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Deletes the specified existing position by its unique identifier.
   * 
   * If no predefined classification is associated with the specified node, it inherits of the 
   * predefined classification of its closest parent node. So, as a position on the PdC is removed
   * from the classification associated with the specified node (and not from the one associated with
   * the parent node), it is actually removed from the new predefined classification that is created
   * for the specified node from of the one of the parent node.
   * 
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
      PdcClassification classification = pdcServiceProvider().
              findPredefinedClassificationForContentsIn(nodeId, getComponentId());
      PdcPosition positionToDelete = getPdcPositionById(positionId, classification);
      classification.getPositions().remove(positionToDelete);
      classification = duplicateForNodeIfNecessary(classification, nodeId);
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
   * 
   * If no predefined classification is associated with the specified node, it inherits of the 
   * predefined classification of its closest parent node. So, as the updated predefined position on
   * the PdC concerns only the specified node (and not the parent node), it is actually updated in 
   * the new predefined classification that is created for the specified node from of the one of the
   * parent node.
   * 
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
      PdcClassification theClassification = pdcServiceProvider().
              findPredefinedClassificationForContentsIn(nodeId, getComponentId());
      PdcPosition positionToUpdate = getPdcPositionById(positionId, theClassification);
      theClassification.getPositions().remove(positionToUpdate);
      theClassification = duplicateForNodeIfNecessary(theClassification, nodeId);
      theClassification.getPositions().add(modifiedPosition.toPdcPosition());
      theClassification = pdcServiceProvider().saveOrUpdatePredefinedClassification(
              theClassification);
      return asWebEntity(theClassification, identifiedBy(theUriOf(theClassification)));
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private PdcClassification getAnExistingOrCreateANewClassificationFor(String nodeId,
          String componentId) throws PdcException {
    PdcClassification classification = pdcServiceProvider().
            findPredefinedClassificationForContentsIn(nodeId, getComponentId());
    return duplicateForNodeIfNecessary(classification, nodeId);
  }
  
  private PdcClassification duplicateForNodeIfNecessary(final PdcClassification classification,
          String nodeId) {
    PdcClassification duplicate = classification;
    if (classification == PdcClassification.NONE_CLASSIFICATION) {
      duplicate = aPredefinedPdcClassificationForComponentInstance(getComponentId()).forNode(nodeId);
    } else if (nodeId != null && !nodeId.equals(classification.getNodeId())) {
      duplicate = classification.clone().forNode(nodeId);
    }
    return duplicate;
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

  private PdcClassificationEntity asWebEntity(final PdcClassification classification, URI uri)
          throws
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

  private static PdcClassification in(final PdcClassification classification) {
    return classification;
  }

  private URI theUriOf(final PdcClassification classification) {
    URI uri = null;
    if (classification.isPredefinedForANode()) {
      uri = getUriInfo().getBaseUriBuilder().path(BASE_URI_PATH).queryParam("nodeId",
              classification.getNodeId()).build(classification.getComponentInstanceId());
    } else {
      uri = getUriInfo().getBaseUriBuilder().path(BASE_URI_PATH).build(classification.
              getComponentInstanceId());
    }
    return uri;
  }

  private URI theUriOf(final PdcPosition position, final PdcClassification inClassification) {
    URI uri = null;
    if (inClassification.isPredefinedForANode()) {
      uri = getUriInfo().getAbsolutePathBuilder().path(position.getId()).queryParam("nodeId",
              inClassification.getNodeId()).build();
    } else {
      uri = getUriInfo().getAbsolutePathBuilder().path(position.getId()).build();
    }
    return uri;
  }

  private PdcPosition getAddedPosition(PdcClassification inClassification,
          PdcClassification fromClassification) {
    PdcPosition addedPosition = null;
    for (PdcPosition position : inClassification.getPositions()) {
      if (!fromClassification.getPositions().contains(position)) {
        addedPosition = position;
        break;
      }
    }
    return addedPosition;
  }
}
