/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.exception.SilverpeasException;

import java.net.URI;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification
    .aPredefinedPdcClassificationForComponentInstance;
import static org.silverpeas.core.webapi.pdc.PdcClassificationEntity.*;

import javax.ws.rs.*;

/**
 * A REST Web resource that represents the predefined classifications on the PdC to classify the
 * contents that are published into a given node of a given component instance or in a whole
 * component instance.
 * <p/>
 * A predefined classification on the PdC can be created and attached either to a component
 * instance
 * or to a a node of a component instance. It then can be used as a default classification to
 * automatically classify the contents or as a classification template from which the contents can
 * be classified on the PdC.
 * <p/>
 * A predefined classification associated with a given node or with a given component instance
 * follows the hierarchical structure of the node tree; it is also applicable to all contents in
 * the
 * children of the given node (or of all nodes in the component instance) in the case they aren't
 * associated explicitly with a predefined classification. So, when classifying on the PdC of a
 * content published in a given node, a predefined classification is then looked for backward in
 * the
 * hierarchical tree of nodes, from the given node up to the component instance itself; once found,
 * this predefined classification will be used to classify the content. Similarly, when editing a
 * predefined classification associated with a node, it is sought backward in the hierarchical tree
 * of nodes but the predefined classification found will be modified only for the given node.
 * <p/>
 * A node in a component instance is a generic way in Silverpeas to categorize hierarchically the
 * contents; they are divided into a tree of nodes. A node can represent a topic, a tag or a folder
 * for example.
 * <p/>
 * A classification on the PdC is defined by a set of different positions on the axis of the PdC. A
 * position is a set of one or more values of axis. A classification can be modifiable or not. By
 * default, a predefined classification is set as unmodifiable whereas the classification of a
 * content is modifiable by default.
 * <p/>
 * The positions of a given classification can be accessed with this Web resource by the URI of the
 * position; classifications and positions are exposed in the Web by Silverpeas and are thus
 * uniquely identified by an URI in the Web.
 */
@Service
@RequestScoped
@Path("pdc/classification/{componentId:[a-zA-Z]+[0-9]+}")
@Authorized
public class PdcPredefinedClassificationResource extends RESTWebService {

  private static final String BASE_URI_PATH = "pdc/classification/{componentId:[a-zA-Z]+[0-9]+}";
  @Inject
  private PdcServiceProvider pdcServiceProvider;
  @PathParam("componentId")
  private String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the predefined classification on the PdC that is set for the contents in the node
   * identified by the query part of the request URI. If no node identifier is provided in the URI,
   * the predefined classification set for the whole component instance is sought.
   * <p/>
   * A node in a component instance is a generic way in Silverpeas to categorize hierarchically the
   * contents of the component instance. If no predefined classification on the PdC is defined for
   * the requested node, a predefined one is then looked backward among the parent nodes up to the
   * component instance itself.
   *
   * The PdC classification is sent back in JSON. If the user isn't authenticated, a 401 HTTP code is
   * returned. If the user isn't authorized to access the requested resource, a 403 is returned. If
   * a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return a web entity representing the requested predefined PdC classification. If no predefined
   * classification is defined along the path of the nodes up to the component instance, then an
   * empty classification is sent back.
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public PdcClassificationEntity getPredefinedPdCClassificationForContentsInNode(
      @QueryParam("nodeId") String nodeId) {
    try {
      PdcClassification theClassification = pdcServiceProvider().
          findPredefinedClassificationForContentsIn(nodeId, getComponentId());
      return asWebEntity(theClassification, identifiedBy(theUriOf(theClassification)));
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Creates a new predefined classification for the specified node.
   * <p/>
   * If the JSON representation of the classification isn't correct (no values), then a 400 HTTP
   * code is returned. If the user isn't authenticated, a 401 HTTP code is returned. If the user
   * isn't authorized to access the classification, a 403 is returned. If the resource referred by
   * the URI already exists, a 409 HTTP core is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param nodeId the unique identifier of the node with which the classification to create is
   * associated. Can be null, in that case, the classification is associated with the component
   * instance.
   * @param classification the predefined classification to create. The entity is passed within the
   * request and it is serialized in JSON.
   * @return the response with the status of the classification creation and, in the case of a
   * successful operation, the new created PdC classification.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createPredefinedPdcClassification(@QueryParam("nodeId") String nodeId,
      final PdcClassificationEntity classification) {
    PdcClassification alreadyExistingClassification =
        pdcServiceProvider().getPredefinedClassification(nodeId, getComponentId());
    if (alreadyExistingClassification != NONE_CLASSIFICATION) {
      throw new WebApplicationException(Status.CONFLICT);
    }
    try {
      PdcClassification savedClassification = pdcServiceProvider().
          saveOrUpdatePredefinedClassification(fromWebEntity(classification));
      URI theClassificationURI = theUriOf(savedClassification);
      return Response.created(theClassificationURI)
          .entity(asWebEntity(savedClassification, identifiedBy(theClassificationURI))).build();
    } catch (ConstraintViolationException ex) {
      throw new WebApplicationException(ex, Status.BAD_REQUEST);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates the predefined classification for the specified node.
   * <p/>
   * If no predefined classification is associated with the specified node, it inherits of the
   * predefined classification of its closest parent node. So, as the updated predefined position
   * on
   * the PdC concerns only the specified node (and not the parent node), it is actually updated in
   * the new predefined classification that is created for the specified node from of the one of
   * the
   * parent node.
   * <p/>
   * If the JSON representation of the position isn't correct (no values), then a 400 HTTP code is
   * returned. If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't
   * authorized to access the comment, a 403 is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   *
   * @param nodeId the unique identifier of a node.
   * @param classification a web entity representing the new state of the PdC position to update.
   * The entity is passed within the request and it is serialized in JSON.
   * @return the response with the status of the position update and, in the case of a successful
   * operation, the new PdC classification of the resource resulting of the position update.
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PdcClassificationEntity updatePredefinedPdcClassification(
      @QueryParam("nodeId") String nodeId, final PdcClassificationEntity classification) {
    try {
      PdcClassification classificationToUpdate = pdcServiceProvider().
          findPredefinedClassificationForContentsIn(nodeId, getComponentId());
      if (nodeId != null && !nodeId.equals(classificationToUpdate.getNodeId())) {
        throw new PdcException(PdcPredefinedClassificationResource.class.getSimpleName(),
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE");
      }
      classificationToUpdate =
          (classification.isModifiable() ? classificationToUpdate.modifiable() :
              classificationToUpdate.unmodifiable())
              .withPositions(classification.getPdcPositions());
      PdcClassification updatedClassification = pdcServiceProvider().
          saveOrUpdatePredefinedClassification(classificationToUpdate);
      return asWebEntity(updatedClassification, identifiedBy(theUriOf(updatedClassification)));
    } catch (ConstraintViolationException ex) {
      throw new WebApplicationException(ex, Status.BAD_REQUEST);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private PdcClassificationEntity asWebEntity(final PdcClassification classification, URI uri)
      throws Exception {
    PdcClassificationEntity theClassificationEntity;
    if (classification == PdcClassification.NONE_CLASSIFICATION) {
      theClassificationEntity = undefinedClassification();
      theClassificationEntity.setModifiable(false);
    } else {
      UserPreferences userPreferences = getUserPreferences();
      theClassificationEntity = aPdcClassificationEntity(fromPdcClassification(classification),
          inLanguage(userPreferences.getLanguage()), atURI(uri));
      theClassificationEntity.setModifiable(classification.isModifiable());
      if (userPreferences.isThesaurusEnabled()) {
        UserThesaurusHolder theUserThesaurus =
            pdcServiceProvider().getThesaurusOfUser(getUserDetail());
        theClassificationEntity.withSynonymsFrom(theUserThesaurus);
      }
    }
    return theClassificationEntity;
  }

  private PdcClassification fromWebEntity(final PdcClassificationEntity entity) {
    String nodeId = getUriInfo().getQueryParameters().getFirst("nodeId");
    PdcClassification classification =
        aPredefinedPdcClassificationForComponentInstance(getComponentId()).
            forNode(nodeId).
            withPositions(entity.getPdcPositions());
    if (entity.isModifiable()) {
      classification.modifiable();
    } else {
      classification.unmodifiable();
    }
    return classification;
  }

  private PdcServiceProvider pdcServiceProvider() {
    return pdcServiceProvider;
  }

  private static URI identifiedBy(final URI uri) {
    return uri;
  }

  private URI theUriOf(final PdcClassification classification) {
    URI uri;
    if (classification.isPredefinedForANode()) {
      uri = getUriInfo().getBaseUriBuilder().path(BASE_URI_PATH)
          .queryParam("nodeId", classification.getNodeId())
          .build(classification.getComponentInstanceId());
    } else {
      uri = getUriInfo().getBaseUriBuilder().path(BASE_URI_PATH).build(classification.
          getComponentInstanceId());
    }
    return uri;
  }
}
