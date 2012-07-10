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
package com.silverpeas.admin.web;

import static com.silverpeas.admin.web.AdminResourceURIs.buildURIOfComponent;
import static com.silverpeas.admin.web.AdminResourceURIs.buildURIOfSpace;
import static com.silverpeas.admin.web.AdminResourceURIs.buildURIOfSpaceAppearance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.admin.web.delegate.AdminPersonalWebDelegate;
import com.silverpeas.admin.web.tools.AbstractTool;
import com.silverpeas.look.web.delegate.LookWebDelegate;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * Centralizations of admin resource processings
 * @author Yohann Chastagnier
 */
public abstract class AbstractAdminResource extends RESTWebService {

  @Inject
  private AdminWebService adminServices;

  private AdminPersonalWebDelegate adminPersonalDelegate;

  @Inject
  @Named("spaceAccessController")
  private AccessController<String> spaceAccessController;

  private LookWebDelegate lookDelegate;

  /**
   * Loading data centralization of a space
   * @param spaceId
   * @return
   */
  protected SpaceInstLight loadSpace(final String spaceId) {
    final Collection<SpaceInstLight> space = loadSpaces(spaceId);
    return space.isEmpty() ? null : space.iterator().next();
  }

  /**
   * Loading data centralization of spaces
   * @param spaceIds
   * @return never null collection of spaces
   */
  protected Collection<SpaceInstLight> loadSpaces(final String... spaceIds) {
    final List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
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
   * @param componentId
   * @return
   */
  protected ComponentInstLight loadComponent(final String componentId) {
    final Collection<ComponentInstLight> component = loadComponents(componentId);
    return component.isEmpty() ? null : component.iterator().next();
  }

  /**
   * Loading data centralization of components
   * @param componentIds
   * @return never null collection of components
   */
  protected Collection<ComponentInstLight> loadComponents(final String... componentIds) {
    final List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
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
    return SpaceEntity.createFrom(space, getUserPreferences().getLanguage())
        .withURI(buildURIOfSpace(space, getUriInfo()))
        .addUserFavorites(getLookDelegate().getUserFavorite(space, forceGettingFavorite));
  }

  /**
   * Converts the space appearance into its corresponding web entity.
   * @param space the aimed space appearance to convert.
   * @param look the look associated to the given space.
   * @param wallpaper the wallpaper associated to the given space.
   * @return the corresponding space appearance entity.
   */
  protected SpaceAppearanceEntity asWebEntity(final SpaceInstLight space, final String look,
      final String wallpaper) {
    checkNotFoundStatus(space);
    return SpaceAppearanceEntity.createFrom(space, look, wallpaper).withURI(
        buildURIOfSpaceAppearance(space, getUriInfo()));
  }

  /**
   * Converts the component into its corresponding web entity.
   * @param component the component to convert.
   * @return the corresponding component entity.
   */
  protected ComponentEntity asWebEntity(final ComponentInstLight component) {
    checkNotFoundStatus(component);
    return ComponentEntity.createFrom(component, getUserPreferences().getLanguage()).withURI(
        buildURIOfComponent(component, getUriInfo()));
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
   * @param component the component to convert.
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
   * Gets the common admin services
   * @return
   */
  protected AdminWebService getAdminServices() {
    return adminServices;
  }

  /**
   * Gets the commin admin personal services
   * @return
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
   * Checks if the requester user is authorized to access the given space
   * @param spaceId
   */
  protected void isUserAuthorizedToAccessSpace(final String spaceId) {
    if (!spaceAccessController.isUserAuthorized(getUserDetail().getId(), spaceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the common look services for Web Services
   * @return
   */
  protected LookWebDelegate getLookDelegate() {
    if (lookDelegate == null) {
      lookDelegate =
          LookWebDelegate.getInstance(getUserDetail(), getUserPreferences(),
              getHttpServletRequest());
    }
    return lookDelegate;
  }
}
