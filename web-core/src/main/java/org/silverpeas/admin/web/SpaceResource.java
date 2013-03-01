/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.admin.web;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.silverpeas.admin.web.AdminResourceURIs.FORCE_GETTING_FAVORITE_PARAM;
import static org.silverpeas.admin.web.AdminResourceURIs.GET_NOT_USED_COMPONENTS_PARAM;
import static org.silverpeas.admin.web.AdminResourceURIs.GET_USED_COMPONENTS_PARAM;
import static org.silverpeas.admin.web.AdminResourceURIs.GET_USED_TOOLS_PARAM;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_APPEARANCE_URI_PART;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_BASE_URI;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_COMPONENTS_URI_PART;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_CONTENT_URI_PART;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_PERSONAL_URI_PART;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_SPACES_URI_PART;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * A REST Web resource giving space data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(SPACES_BASE_URI)
@Authenticated
public class SpaceResource extends AbstractAdminResource {

  /**
   * Gets the JSON representation of root spaces.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * feature is disabled.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Produces(APPLICATION_JSON)
  public Collection<SpaceEntity> getAll(
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      return asWebEntities(SpaceEntity.class,
          loadSpaces(getAdminServices().getAllRootSpaceIds(getUserDetail().getId())),
          forceGettingFavorite);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * feature is disabled.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Path("{spaceId}")
  @Produces(APPLICATION_JSON)
  public SpaceEntity get(@PathParam("spaceId") final String spaceId,
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return asWebEntity(loadSpace(spaceId), forceGettingFavorite);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates the space data from its JSON representation and returns it once updated.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param spaceEntity space entity to update.
   * @return the response to the HTTP PUT request with the JSON representation of the updated
   * space.
   */
  @PUT
  @Path("{spaceId}")
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  public SpaceEntity update(@PathParam("spaceId") final String spaceId,
      final SpaceEntity spaceEntity) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);

      // Old space entity data
      final SpaceEntity oldSpaceEntity = get(spaceId, true);

      // Favorite data change
      if (!oldSpaceEntity.getFavorite().equals(spaceEntity.getFavorite())) {
        // Updating space favorite
        if (spaceEntity.getFavorite().equals(String.valueOf(Boolean.TRUE))) {
          getLookDelegate().addToUserFavorites(loadSpace(spaceId));
        } else if (spaceEntity.getFavorite().equals(String.valueOf(Boolean.FALSE))) {
          getLookDelegate().removeFromUserFavorites(loadSpace(spaceId));
        }
      }

      // Space entity reloading
      return asWebEntity(loadSpace(spaceId), true);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of spaces of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_SPACES_URI_PART)
  @Produces(APPLICATION_JSON)
  public Collection<SpaceEntity> getSpaces(@PathParam("spaceId") final String spaceId,
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return asWebEntities(SpaceEntity.class,
          loadSpaces(getAdminServices().getAllSubSpaceIds(spaceId, getUserDetail().getId())),
          forceGettingFavorite);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of components of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_COMPONENTS_URI_PART)
  @Produces(APPLICATION_JSON)
  public Collection<ComponentEntity> getComponents(@PathParam("spaceId") final String spaceId) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return asWebEntities(ComponentEntity.class, loadComponents(getAdminServices()
          .getAllComponentIds(spaceId, getUserDetail().getId())));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of content of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_CONTENT_URI_PART)
  @Produces(APPLICATION_JSON)
  public Collection<AbstractTypeEntity> getContent(@PathParam("spaceId") final String spaceId,
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      final Collection<AbstractTypeEntity> content = new ArrayList<AbstractTypeEntity>();
      content.addAll(getSpaces(spaceId, forceGettingFavorite));
      content.addAll(getComponents(spaceId));
      return content;
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_APPEARANCE_URI_PART)
  @Produces(APPLICATION_JSON)
  public SpaceAppearanceEntity getAppearance(@PathParam("spaceId") final String spaceId) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      final SpaceInstLight space = loadSpace(spaceId);
      return asWebEntity(space, getLookDelegate().getLook(space),
          getLookDelegate().getWallpaper(space), getLookDelegate().getCSS(space));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of the content of user's personal space.
   * When all query parameters are set at false then the service understands that it has to return
   * all personal entities.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param getNotUsedComponents
   * @param getUsedComponents
   * @param getUsedTools
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @GET
  @Path(SPACES_PERSONAL_URI_PART)
  @Produces(APPLICATION_JSON)
  public Collection<AbstractPersonnalEntity> getPersonals(
      @QueryParam(GET_NOT_USED_COMPONENTS_PARAM) final boolean getNotUsedComponents,
      @QueryParam(GET_USED_COMPONENTS_PARAM) final boolean getUsedComponents,
      @QueryParam(GET_USED_TOOLS_PARAM) final boolean getUsedTools) {
    try {

      // When all query parameters are set at false then the service understands that it has to
      // return all personal entities
      final boolean getAll = !getNotUsedComponents && !getUsedComponents && !getUsedTools;

      final Collection<AbstractPersonnalEntity> personals =
          new ArrayList<AbstractPersonnalEntity>();

      if (getAll || getNotUsedComponents) {
        personals.addAll(asWebPersonalEntities(PersonalComponentEntity.class,
            getAdminPersonalDelegate().getNotUsedComponents()));
      }
      if (getAll || getUsedComponents) {
        personals.addAll(asWebPersonalEntities(PersonalComponentEntity.class,
            getAdminPersonalDelegate().getUsedComponents()));
      }
      if (getAll || getUsedTools) {
        personals.addAll(asWebPersonalEntities(PersonalToolEntity.class, getAdminPersonalDelegate()
            .getUsedTools()));
      }
      return personals;
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Instantiates the requested component in the user's personal space. It returns the JSON
   * representation of the instantiated component.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param componentName the name of component to add in the user's personal space
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @PUT
  @Path(SPACES_PERSONAL_URI_PART + "/{componentName}")
  @Produces(APPLICATION_JSON)
  public PersonalComponentEntity useComponent(@PathParam("componentName") final String componentName) {
    try {
      return asWebPersonalEntity(getAdminPersonalDelegate().useComponent(componentName));
    } catch (final AdminException ex) {
      SilverTrace.error("admin", "SpaceResource.useComponent",
          "root.EX_UNKNOWN_COMPONENT_OR_COMPONENT_ALREADY_USED");
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Removes from the user's personal space the instantiation of the requested component. It returns
   * the JSON representation of WAComponent.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param componentName the name of component to add in the user's personal space
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * space.
   */
  @DELETE
  @Path(SPACES_PERSONAL_URI_PART + "/{componentName}")
  @Produces(APPLICATION_JSON)
  public PersonalComponentEntity discardComponent(
      @PathParam("componentName") final String componentName) {
    try {
      return asWebPersonalEntity(getAdminPersonalDelegate().discardComponent(componentName));
    } catch (final AdminException ex) {
      SilverTrace.error("admin", "SpaceResource.discardComponent", "root.EX_UNKNOWN_COMPONENT_ID");
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException(
        "The SpaceResource doesn't belong to any component instance ids");
  }
}
