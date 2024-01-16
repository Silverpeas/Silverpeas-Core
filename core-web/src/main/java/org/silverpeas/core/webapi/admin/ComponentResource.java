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

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.*;

/**
 * A REST Web resource giving component data.
 * @author Yohann Chastagnier
 */
@WebService
@Path(COMPONENTS_BASE_URI + "/{componentId}")
@Authorized
public class ComponentResource extends AbstractAdminResource {

  @PathParam("componentId")
  private String componentId;

  @XmlTransient
  private String fullComponentId;

  @Context
  private UriInfo uriInfo;

  @Override
  protected String getResourceBasePath() {
    return COMPONENTS_BASE_URI;
  }

  @Override
  protected WebResourceUri initWebResourceUri() {
    return new WebResourceUri(getResourceBasePath(), getHttpServletRequest(), uriInfo);
  }

  /**
   * Gets the JSON representation of the specified existing ComponentInstLight.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the component, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * ComponentInstLight.
   */
  @GET
  @Produces(APPLICATION_JSON)
  public ComponentEntity get() {
    try {
      final Collection<ComponentInstLight> component = loadComponents(componentId);
      return asWebEntity(component.isEmpty() ? null : component.iterator().next());
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets users and groups roles indexed by role names.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param roles aimed roles (each one separated by comma). If empty, all roles are returned.
   * @return the JSON response to the HTTP GET request.
   */
  @GET
  @Path(USERS_AND_GROUPS_ROLES_URI_PART)
  @Produces(APPLICATION_JSON)
  public Map<SilverpeasRole, UsersAndGroupsRoleEntity> getUsersAndGroupsRoles(
      @QueryParam(ROLES_PARAM) final String roles) {
    try {

      // Initializing the result
      Map<SilverpeasRole, UsersAndGroupsRoleEntity> result = new LinkedHashMap<>();

      // Aimed roles or all roles ?
      Collection<String> aimedRoles = new ArrayList<>(0);
      if (StringUtil.isDefined(roles)) {
        aimedRoles = CollectionUtil.asList(StringUtils.split(roles, ","));
      }

      // Getting space profiles
      ComponentInst componentInst = getOrganisationController().getComponentInst(getComponentId());
      List<ProfileInst> profiles = new ArrayList<>();
      profiles.addAll(componentInst.getInheritedProfiles());
      profiles.addAll(componentInst.getProfiles());

      // Building entities
      LocalizationBundle resource = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle",
              getUserPreferences().getLanguage());
      UsersAndGroupsRoleEntity roleEntity;
      for (ProfileInst profile : profiles) {
        SilverpeasRole role = SilverpeasRole.from(profile.getName());
        if (role != null && (aimedRoles.isEmpty() || aimedRoles.contains(role.getName()))) {
          roleEntity = result.get(role);
          if (roleEntity == null) {
            roleEntity = UsersAndGroupsRoleEntity
                .createFrom(role, resource.getString("JSPP." + role.getName()));
            roleEntity.withURI(getUri().getWebResourcePathBuilder()
                .path(componentId)
                .path(USERS_AND_GROUPS_ROLES_URI_PART)
                .queryParam("roles", role.getName())
                .build())
                .withParentURI(getUri().getWebResourcePathBuilder().path(componentId).build());
            result.put(role, roleEntity);
          }

          // Users
          for (String userId : profile.getAllUsers()) {
            roleEntity.addUser(getUri().getBaseUriBuilder()
                .path(ProfileResourceBaseURIs.USERS_BASE_URI)
                .path(userId)
                .build());
          }

          // Groups
          for (String groupId : profile.getAllGroups()) {
            roleEntity.addGroup(getUri().getBaseUriBuilder()
                .path(ProfileResourceBaseURIs.GROUPS_BASE_URI)
                .path(groupId)
                .build());
          }
        }
      }

      return result;
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
    // at this level, the user authorization process requires also the name of the component
    if (!StringUtil.isDefined(fullComponentId)) {
      final Collection<ComponentInstLight> components = loadComponents(componentId);
      final ComponentInstLight component =
          components.isEmpty() ? null : components.iterator().next();
      fullComponentId = component != null ? component.getId() : componentId;
    }
    return fullComponentId;
  }
}
