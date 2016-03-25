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
package org.silverpeas.core.webapi.admin;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.webapi.admin.delegate.AdminPersonalWebDelegate;
import org.silverpeas.core.webapi.admin.tools.AbstractTool;
import org.silverpeas.core.webapi.look.delegate.LookWebDelegate;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Centralizations of admin resource processings
 * @author Yohann Chastagnier
 */
public abstract class AbstractAdminResource extends RESTWebService {

  @Inject
  private AdminWebService adminServices;

  private AdminPersonalWebDelegate adminPersonalDelegate;

  @Inject
  private SpaceAccessControl spaceAccessController;

  private LookWebDelegate lookDelegate;

  /**
   * Loading data centralization of a space
   * @param spaceId the space identifier
   * @return the space instance light representation
   */
  protected SpaceInstLight loadSpace(final String spaceId) {
    final Collection<SpaceInstLight> space = loadSpaces(spaceId);
    return space.isEmpty() ? null : space.iterator().next();
  }

  /**
   * Loading data centralization of spaces
   * @param spaceIds the space identifiers
   * @return never null collection of spaces
   */
  protected Collection<SpaceInstLight> loadSpaces(final String... spaceIds) {
    final List<SpaceInstLight> spaces = new ArrayList<>();
    SpaceInstLight space;
    for (final String spaceId : spaceIds) {
      space = getAdminServices().getSpaceById(spaceId);
      if (space != null) {
        spaces.add(space);
      }
    }
    return spaces;
  }

  /**
   * Loading data centralization of a space
   * @param componentId the component instance identifier
   * @return
   */
  protected ComponentInstLight loadComponent(final String componentId) {
    final Collection<ComponentInstLight> component = loadComponents(componentId);
    return component.isEmpty() ? null : component.iterator().next();
  }

  /**
   * Loading data centralization of components
   * @param componentIds the component instance identifiers
   * @return never null collection of components
   */
  protected Collection<ComponentInstLight> loadComponents(final String... componentIds) {
    final List<ComponentInstLight> components = new ArrayList<>();
    ComponentInstLight component;
    for (final String componentId : componentIds) {
      component = getAdminServices().getComponentById(componentId);
      if (component != null) {
        components.add(component);
      }
    }
    return components;
  }

  /**
   * Converts the given list of data into their corresponding web entities.
   * @param entityClass the entity class returned.
   * @param data data to convert.
   * @return an array with the corresponding web entities.
   */
  protected <T, E extends AbstractTypeEntity> Collection<E> asWebEntities(
      final Class<E> entityClass, final Collection<T> data) {
    return asWebEntities(entityClass, data, false);
  }

  /**
   * Converts the given list of data into their corresponding web entities.
   * @param entityClass the entity class returned.
   * @param data data to convert.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * feature is disabled
   * @return an array with the corresponding web entities.
   */
  @SuppressWarnings("unchecked")
  protected <T, E extends AbstractTypeEntity> Collection<E> asWebEntities(
      final Class<E> entityClass, final Collection<T> data, final boolean forceGettingFavorite) {
    final Collection<E> entities = new ArrayList<E>(data.size());
    for (final Object object : data) {
      if (object instanceof SpaceInstLight) {
        entities.add((E) asWebEntity((SpaceInstLight) object, forceGettingFavorite));
      } else if (object instanceof ComponentInstLight) {
        entities.add((E) asWebEntity((ComponentInstLight) object));
      } else {
        asWebEntity(object);
      }
    }
    return entities;
  }

  /**
   * Converts the given list of data into their corresponding web entities.
   * @param entityClass the entity class returned.
   * @param data data to convert.
   * @return an array with the corresponding web entities.
   */
  @SuppressWarnings("unchecked")
  protected <T, E extends AbstractTypeEntity> Collection<E> asWebPersonalEntities(
      final Class<E> entityClass, final Collection<T> data) {
    final Collection<E> entities = new ArrayList<E>(data.size());
    for (final Object object : data) {
      if (object instanceof WAComponent) {
        entities.add((E) asWebPersonalEntity((WAComponent) object));
      } else if (object instanceof ComponentInst) {
        entities.add((E) asWebPersonalEntity((ComponentInst) object));
      } else if (object instanceof AbstractTool) {
        entities.add((E) asWebPersonalEntity((AbstractTool) object));
      } else {
        asWebEntity(object);
      }
    }
    return entities;
  }

  /**
   * Converts the space into its corresponding web entity.
   * @param space the space to convert.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * @return the corresponding space entity.
   */
  protected SpaceEntity asWebEntity(final SpaceInstLight space, final boolean forceGettingFavorite) {
    checkNotFoundStatus(space);
    final StringBuilder userFavoriteSpace = new StringBuilder();
    if (isUserAuthorizedToAccessLookContext()) {
      // The user space favorite information is retrieved in the only case where Look context is
      // accessible. At this level, no user access to Look context is nonblocking
      userFavoriteSpace.append(getLookDelegate().getUserFavorite(space, forceGettingFavorite));
    }
    return SpaceEntity.createFrom(space, getUserPreferences().getLanguage())
        .withURI(AdminResourceURIs.buildURIOfSpace(space, getUriInfo()))
        .addUserFavorites(userFavoriteSpace.toString());
  }

  /**
   * Converts the space appearance into its corresponding web entity.
   * @param space the aimed space appearance to convert.
   * @param look the look associated to the given space.
   * @param wallpaper the wallpaper associated to the given space.
   * @return the corresponding space appearance entity.
   */
  protected SpaceAppearanceEntity asWebEntity(final SpaceInstLight space, final String look,
      final String wallpaper, final String css) {
    checkNotFoundStatus(space);
    return SpaceAppearanceEntity.createFrom(space, look, wallpaper, css).withURI(
        AdminResourceURIs.buildURIOfSpaceAppearance(space, getUriInfo()));
  }

  /**
   * Converts the component into its corresponding web entity.
   * @param component the component to convert.
   * @return the corresponding component entity.
   */
  protected ComponentEntity asWebEntity(final ComponentInstLight component) {
    checkNotFoundStatus(component);
    return ComponentEntity.createFrom(component, getUserPreferences().getLanguage()).withURI(
        AdminResourceURIs.buildURIOfComponent(component, getUriInfo()));
  }

  /**
   * Converts the personal component into its corresponding web entity.
   * @param component the personal component to convert.
   * @return the corresponding personal component entity.
   */
  protected PersonalComponentEntity asWebPersonalEntity(final WAComponent component) {
    checkNotFoundStatus(component);
    return PersonalComponentEntity
        .createFrom(component, getAdminPersonalDelegate().getComponentLabel(component),
            getUserPreferences().getLanguage()).withUriBase(getUriInfo().getBaseUri());
  }

  /**
   * Converts the personal component into its corresponding web entity.
   * @param component the personal component to convert.
   * @return the corresponding personal component entity.
   */
  protected PersonalComponentEntity asWebPersonalEntity(final ComponentInst component) {
    checkNotFoundStatus(component);
    return PersonalComponentEntity.createFrom(component).withUriBase(getUriInfo().getBaseUri());
  }

  /**
   * Converts the tool into its corresponding web entity.
   * @param tool the tool to convert.
   * @return the corresponding tool entity.
   */
  protected PersonalToolEntity asWebPersonalEntity(final AbstractTool tool) {
    checkNotFoundStatus(tool);
    return PersonalToolEntity.createFrom(tool).withUriBase(getUriInfo().getBaseUri());
  }

  /**
   * Converts the component into its corresponding web entity.
   * @param object any object instance.
   * @return the corresponding component entity.
   */
  protected AbstractTypeEntity asWebEntity(final Object object) {
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  /**
   * Centralization
   * @param object
   */
  private void checkNotFoundStatus(final Object object) {
    if (object == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * @return the common admin services
   */
  protected AdminWebService getAdminServices() {
    return adminServices;
  }

  /**
   * @return the commin admin personal services
   */
  protected AdminPersonalWebDelegate getAdminPersonalDelegate() {
    if (adminPersonalDelegate == null) {
      adminPersonalDelegate =
          AdminPersonalWebDelegate.getInstance(getUserDetail(), getUserPreferences(),
              getLookDelegate());
    }
    return adminPersonalDelegate;
  }

  /**
   * Verifies the requester user is authorized to access the given space
   * @param spaceId the space identifier
   */
  protected void verifyUserAuthorizedToAccessSpace(final String spaceId) {
    if (!spaceAccessController.isUserAuthorized(getUserDetail().getId(), spaceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Verifies the requester user is authorized to access the given space
   */
  protected void verifyUserAuthorizedToAccessLookContext() {
    // If the look helper is not accessible, then the user is not authorized
    if (!isUserAuthorizedToAccessLookContext()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Indicates if the requester user is authorized to access the given space
   */
  protected boolean isUserAuthorizedToAccessLookContext() {
    // If the look helper is not accessible, then the user is not authorized
    if (lookDelegate == null) {
      lookDelegate =
          LookWebDelegate.getInstance(getUserDetail(), getUserPreferences(),
              getHttpServletRequest());
    }
    return (lookDelegate != null && lookDelegate.getHelper() != null);
  }

  /**
   * @return the common look services for Web Services
   */
  protected LookWebDelegate getLookDelegate() {
    verifyUserAuthorizedToAccessLookContext();
    return lookDelegate;
  }
}
