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
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.List;

import static org.silverpeas.core.webapi.pdc.PdcClassificationEntity.*;
import static org.silverpeas.core.webapi.pdc.PdcServiceProvider.forContentOfId;
import static org.silverpeas.core.webapi.pdc.PdcServiceProvider.inComponentOfId;

/**
 * A REST Web resource that represents the classification of a Silverpeas's resource on the
 * classification plan (named PdC).
 *
 * A classification on the PdC is defined by the different positions of the classified resource on
 * the axis of the PdC. A position is then a set of one or more values in the different axis of the
 * PdC. A classification on the PdC can be or not modifiable; by default a predefined classification
 * used to classify new published contents isn't modifiable whereas the classification of a content
 * can be modified.
 *
 * The positions of a given classification can be accessed with this Web resource by the URI of the
 * position; classifications and positions are exposed in the Web by Silverpeas and are thus
 * uniquely identified by an URI in the Web.
 */
@Service
@RequestScoped
@Path("pdc/classification/{componentId:[a-zA-Z]+[0-9]+}/{contentId}")
@Authorized
public class PdcClassificationResource extends RESTWebService {

  @Inject
  private PdcServiceProvider pdcServiceProvider;
  @PathParam("componentId")
  private String componentId;
  @PathParam("contentId")
  private String contentId;

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  protected String getContentId() {
    return this.contentId;
  }

  /**
   * Gets classification on the PdC of the resource identified by the requested URI. The PdC
   * classification is sent back in JSON. If the user isn't authenticated, a 401 HTTP code is
   * returned. If the user isn't authorized to access the requested resource, a 403 is returned. If
   * a problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @return a web entity representing the PdC classification of the resource. The entity is
   * serialized in JSON.
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public PdcClassificationEntity getPdCClassification() {
    try {
      URI itsURI = getUriInfo().getAbsolutePath();
      return thePdcClassificationOfTheRequestedResource(identifiedBy(itsURI));
    } catch (ContentManagerException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Deletes the specified existing position by its unique identifier. If the PdC position doesn't
   * exist, nothing is done, so that the HTTP DELETE request remains idempotent as defined in the
   * HTTP specification. If the user isn't authenticated, a 401 HTTP code is returned. If the user
   * isn't authorized to access the resource PdC classification, a 403 is returned. If the position
   * is the single one for the content on the PdC and the PdC contains at least one mandatory axis,
   * a 409 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   *
   * @param positionId the unique identifier of the position to delete in the classification of the
   * requested resource.
   */
  @DELETE
  @Path("{positionId}")
  public void deletePdcPosition(@PathParam("positionId") int positionId) {
    try {
      pdcServiceProvider().deletePosition(positionId, forContentOfId(getContentId()),
          inComponentOfId(getComponentId()));
    } catch (ContentManagerException ex) {
      SilverLogger.getLogger(this).warn(ex.getMessage());
    } catch (PdcPositionDeletionException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new WebApplicationException(Status.CONFLICT);
    } catch (PdcException ex) {
      SilverLogger.getLogger(this).warn(ex.getMessage());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Adds a new position on the PdC into the classification of the resource identified by the
   * requested URI. If the JSON representation of the position isn't correct (no values), then a 400
   * HTTP code is returned. If the user isn't authenticated, a 401 HTTP code is returned. If the user
   * isn't authorized to access the comment, a 403 is returned. If a problem occurs when processing
   * the request, a 503 HTTP code is returned.
   *
   * @param newPosition a web entity representing the PdC position to add. The entity is passed
   * within the request and it is serialized in JSON.
   * @return the response with the status of the position adding and, in the case of a successful
   * operation, the new PdC classification of the resource resulting of the position adding.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addPdcPosition(final PdcPositionEntity newPosition) {
    if (newPosition.getPositionValues().isEmpty()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    try {
      ClassifyPosition position = newPosition.toClassifyPosition();
      pdcServiceProvider().addPosition(
          position,
          forContentOfId(getContentId()),
          inComponentOfId(getComponentId()));
      String positionId = String.valueOf(position.getPositionId());
      URI newPositionURI = getUriInfo().getAbsolutePathBuilder().path(positionId).build();
      URI itsURI = getUriInfo().getAbsolutePath();
      return Response.created(newPositionURI).
          entity(thePdcClassificationOfTheRequestedResource(identifiedBy(itsURI))).
          build();
    } catch (ContentManagerException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.BAD_REQUEST);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates an existing position on the PdC into the classification of the resource identified by
   * the requested URI. If the JSON representation of the position isn't correct (no values), then a
   * 400 HTTP code is returned. If the user isn't authenticated, a 401 HTTP code is returned. If the
   * user isn't authorized to access the comment, a 403 is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param modifiedPosition a web entity representing the new state of the PdC position to update.
   * The entity is passed within the request and it is serialized in JSON.
   * @return the response with the status of the position update and, in the case of a successful
   * operation, the new PdC classification of the resource resulting of the position update.
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{positionId}")
  public PdcClassificationEntity updatePdcPosition(@PathParam("positionId") String positionId,
      final PdcPositionEntity modifiedPosition) {
    if (!positionId.equals(modifiedPosition.getId())
        || modifiedPosition.getPositionValues().isEmpty()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    try {
      ClassifyPosition position = modifiedPosition.toClassifyPosition();
      pdcServiceProvider().updatePosition(
          position,
          forContentOfId(getContentId()),
          inComponentOfId(getComponentId()));
      URI itsURI = getUriInfo().getBaseUriBuilder().path(
          "pdc/classification/{componentId}/{contentId}").build(getComponentId(), getContentId());
      return thePdcClassificationOfTheRequestedResource(identifiedBy(itsURI));
    } catch (ContentManagerException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.BAD_REQUEST);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private PdcClassificationEntity thePdcClassificationOfTheRequestedResource(final URI uri) throws
      Exception {
    UserPreferences userPreferences = getUserPreferences();
    List<ClassifyPosition> contentPositions = pdcServiceProvider().getAllPositions(
        forContentOfId(getContentId()),
        inComponentOfId(getComponentId()));
    PdcClassificationEntity theClassificationEntity = aPdcClassificationEntity(
        fromPositions(contentPositions),
        inLanguage(userPreferences.getLanguage()),
        atURI(uri));
    if (userPreferences.isThesaurusEnabled()) {
      UserThesaurusHolder theUserThesaurus =
          pdcServiceProvider().getThesaurusOfUser(getUserDetail());
      theClassificationEntity.withSynonymsFrom(theUserThesaurus);
    }
    return theClassificationEntity;
  }

  private PdcServiceProvider pdcServiceProvider() {
    return pdcServiceProvider;
  }

  private static URI identifiedBy(final URI uri) {
    return uri;
  }
}
