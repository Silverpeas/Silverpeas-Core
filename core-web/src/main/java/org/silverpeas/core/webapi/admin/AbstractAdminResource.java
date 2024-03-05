/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.admin;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.webapi.admin.delegate.AdminPersonalWebDelegate;
import org.silverpeas.core.webapi.admin.tools.AbstractTool;
import org.silverpeas.core.webapi.look.delegate.LookWebDelegate;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.web.SilverpeasWebResource.getBasePathBuilder;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.*;

/**
 * Centralizations of admin resource processings
 * @author Yohann Chastagnier
 */
public abstract class AbstractAdminResource extends RESTWebService {

  @Inject
  protected OrganizationController orgaController;

  @QueryParam(ADMIN_ACCESS_PARAM)
  private boolean adminAccess = false;

  private AdminPersonalWebDelegate adminPersonalDelegate;

  @Inject
  private SpaceAccessControl spaceAccessController;

  @Inject
  private ComponentAccessControl componentAccessController;

  private LookWebDelegate lookDelegate;

  /**
   * Loading data centralization of a space.
   * <p>
   *   When calling this method to load space data, it is considered that
   *   {@link #verifyUserAuthorizedToAccessSpace(String)} method has been called in order to
   *   verify user right access about the space.
   * </p>
   * @param spaceId the space identifier.
   * @return the space instance light representation.
   */
  protected SpaceInstLight loadSpace(final String spaceId) {
    return loadSpaces(false, spaceId).findFirst().orElse(null);
  }

  /**
   * Loading data centralization of spaces.
   * <p>
   *   When calling this method, the caller is loading massively space data. In a such context,
   *   the loading data are verified concerning the user authorizations.
   * </p>
   * @param spaceIds the space identifiers.
   * @return stream of {@link SpaceInstLight} instance.
   */
  protected Stream<SpaceInstLight> loadSpaces(final String... spaceIds) {
    return loadSpaces(true, spaceIds);
  }

  private Stream<SpaceInstLight> loadSpaces(final boolean authorizationCheck,
      final String... spaceIds) {
    final Stream<String> stream = (!authorizationCheck || isValidAdminAccess())
        ? Stream.of(spaceIds)
        : spaceAccessController.filterAuthorizedByUser(List.of(spaceIds), getUser().getId());
    return stream.map(orgaController::getSpaceInstLightById)
        .filter(Objects::nonNull);
  }

  /**
   * Loading data centralization of a component instance.
   * <p>
   *   When calling this method to load component instance data, it is considered that
   *   {@link #validateUserAuthorization(UserPrivilegeValidation)} method has been called in
   *   order to verify user right access about the component instance (cf.
   *   {@link org.silverpeas.core.web.rs.annotation.Authorized} class annotation).
   * </p>
   * @param componentId the component instance identifier
   * @return an instance of {@link ComponentInstLight} if any, null otherwise.
   */
  protected ComponentInstLight loadComponent(final String componentId) {
    return loadComponents(false, componentId).findFirst().orElse(null);
  }

  /**
   * Loading data centralization of components.
   * <p>
   *   When calling this method, the caller is loading massively component data. In a such context,
   *   the loading data are verified concerning the user authorizations.
   * </p>
   * @param componentIds the component instance identifiers.
   * @return stream of {@link ComponentInstLight} instance.
   */
  protected Stream<ComponentInstLight> loadComponents(final String... componentIds) {
    return loadComponents(true, componentIds);
  }

  private Stream<ComponentInstLight> loadComponents(final boolean authorizationCheck,
      final String... componentIds) {
    final Stream<String> stream = (!authorizationCheck || isValidAdminAccess())
        ? Stream.of(componentIds)
        : componentAccessController.filterAuthorizedByUser(List.of(componentIds), getUser().getId());
    return stream.map(orgaController::getComponentInstLight)
        .filter(Objects::nonNull);
  }

  /**
   * Filters the given collection of {@link ComponentInstLight} instance according to user rights.
   * @param componentInstances component instances to filter.
   * @return a filtered stream of {@link ComponentInstLight} instance.
   */
  protected Stream<ComponentInstLight> filterAuthorizedComponents(
      final Collection<ComponentInstLight> componentInstances) {
    final Stream<ComponentInstLight> stream;
    if (isValidAdminAccess()) {
      stream = componentInstances.stream();
    } else {
      final Map<String, ComponentInstLight> indexed = componentInstances.stream()
          .collect(toMap(ComponentInstLight::getId, c -> c));
      stream = componentAccessController
          .filterAuthorizedByUser(
              componentInstances.stream()
                  .map(ComponentInstLight::getId)
                  .collect(toList()), getUser().getId())
          .map(indexed::get);
    }
    return stream;
  }

  /**
   * Converts the given list of data into their corresponding web entities.
   * @param data data to convert.
   * @return an array with the corresponding web entities.
   */
  protected <T, E extends AbstractTypeEntity> Collection<E> asWebEntities(final Stream<T> data) {
    return asWebEntities(data, false);
  }

  /**
   * Converts the given list of data into their corresponding web entities.
   * @param data data to convert.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * feature is disabled
   * @return an array with the corresponding web entities.
   */
  @SuppressWarnings("unchecked")
  protected <T, E extends AbstractTypeEntity> Collection<E> asWebEntities(
      final Stream<T> data, final boolean forceGettingFavorite) {
    return (Collection<E>) data.map(object -> {
      if (object instanceof SpaceInstLight) {
        return asWebEntity((SpaceInstLight) object, forceGettingFavorite);
      } else if (object instanceof ComponentInstLight) {
        return asWebEntity((ComponentInstLight) object);
      } else {
        return asWebEntity(object);
      }
    }).collect(toList());
  }

  /**
   * Converts the given list of data into their corresponding web entities.
   * @param data data to convert.
   * @return an array with the corresponding web entities.
   */
  @SuppressWarnings("unchecked")
  protected <T, E extends AbstractTypeEntity> Collection<E> asWebPersonalEntities(
      final Collection<T> data) {
    final Collection<E> entities = new ArrayList<>(data.size());
    for (final Object object : data) {
      if (object instanceof WAComponent) {
        entities.add((E) asWebPersonalEntity((SilverpeasComponent) object));
      } else if (object instanceof ComponentInst) {
        entities.add((E) asWebPersonalEntity((SilverpeasComponentInstance) object));
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
    return SpaceEntity.createFrom(space, getUserPreferences().getLanguage()).withURI(
        getBasePathBuilder().path(SPACES_BASE_URI).path(String.valueOf(space.getLocalId())).build())
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
        getBasePathBuilder().path(SPACES_BASE_URI).path(String.valueOf(space.getLocalId()))
            .path(SPACES_APPEARANCE_URI_PART).build());
  }

  /**
   * Converts the component into its corresponding web entity.
   * @param component the component to convert.
   * @return the corresponding component entity.
   */
  protected ComponentEntity asWebEntity(final ComponentInstLight component) {
    checkNotFoundStatus(component);
    return ComponentEntity.createFrom(component, getUserPreferences().getLanguage())
        .withURI(getBasePathBuilder().path(COMPONENTS_BASE_URI).path(component.getId()).build());
  }

  /**
   * Converts the personal component into its corresponding web entity.
   * @param component the personal component to convert.
   * @return the corresponding personal component entity.
   */
  protected PersonalComponentEntity asWebPersonalEntity(final SilverpeasComponent component) {
    checkNotFoundStatus(component);
    return PersonalComponentEntity
        .createFrom(component, getAdminPersonalDelegate().getComponentLabel(component),
            getUserPreferences().getLanguage()).withUriBase(getUri().getBaseUri());
  }

  /**
   * Converts the personal component into its corresponding web entity.
   * @param component the personal component to convert.
   * @return the corresponding personal component entity.
   */
  protected PersonalComponentEntity asWebPersonalEntity(final SilverpeasComponentInstance component) {
    checkNotFoundStatus(component);
    return PersonalComponentEntity.createFrom(component).withUriBase(getUri().getBaseUri());
  }

  /**
   * Converts the tool into its corresponding web entity.
   * @param tool the tool to convert.
   * @return the corresponding tool entity.
   */
  protected PersonalToolEntity asWebPersonalEntity(final AbstractTool tool) {
    checkNotFoundStatus(tool);
    return PersonalToolEntity.createFrom(tool).withUriBase(getUri().getBaseUri());
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
   * @return the commin admin personal services
   */
  protected AdminPersonalWebDelegate getAdminPersonalDelegate() {
    if (adminPersonalDelegate == null) {
      adminPersonalDelegate = AdminPersonalWebDelegate.getInstance(getUser(), getUserPreferences(),
              getLookDelegate());
    }
    return adminPersonalDelegate;
  }

  /**
   * Verifies the requester user is authorized to access the given space.
   * @param spaceId the space identifier
   */
  protected void verifyUserAuthorizedToAccessSpace(final String spaceId) {
    if (!isValidAdminAccess() &&
        !spaceAccessController.isUserAuthorized(getUser().getId(), spaceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) {
    if (!isValidAdminAccess()) {
      super.validateUserAuthorization(validation);
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
      lookDelegate = LookWebDelegate.getInstance(getUser(), getUserPreferences(),
              getHttpServletRequest());
    }
    return lookDelegate.getHelper() != null;
  }

  /**
   * @return the common look services for Web Services
   */
  protected LookWebDelegate getLookDelegate() {
    verifyUserAuthorizedToAccessLookContext();
    return lookDelegate;
  }

  private boolean isValidAdminAccess() {
    return adminAccess && getUser().isAccessAdmin();
  }
}
