/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.wysiwyg;

import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.cache.service.VolatileResourceCacheService;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.mvc.util.WysiwygEditorConfigRegistry;
import org.silverpeas.core.web.util.WysiwygEditorConfig;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A REST Web resource which permits to obtain volatile identifier scoped into a component
 * instance.<br>
 * Please consult {@link VolatileResourceCacheService}.
 * @author Yohann Chastagnier
 */
@WebService
@Path(WysiwygEditorConfigResource.WYSIWYG_CONFIG_BASE_URI + "/{componentInstanceId}")
@Authorized
public class WysiwygEditorConfigResource extends RESTWebService {

  static final String WYSIWYG_CONFIG_BASE_URI = "wysiwyg/editor";

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @QueryParam("configName")
  private String configName;

  /**
   * Gets the wysiwyg editor configuration according to the component instance identifier included
   * into the path URI and the optional configuration name.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * calendars.
   * @see WebProcess#execute()
   */
  @GET
  @Path("{resourceType}/{resourceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWysiwygEditorConfig(@PathParam("resourceType") String resourceType,
      @PathParam("resourceId") String resourceId) {
    // read request parameters
    final WysiwygEditorConfigParameters params =
        RequestParameterDecoder.decode(getHttpRequest(), WysiwygEditorConfigParameters.class);
    return process(() -> {
      WysiwygEditorConfig config = WysiwygEditorConfigRegistry.get().get(getConfigName());
      config.setComponentId(getComponentId());
      config.setObjectId(resourceId);
      setWysiwygEditorSessionContext(resourceType, resourceId, config);
      return Response.ok(params.applyOn(config).toJSON()).build();
    }).lowestAccessRole(SilverpeasRole.writer).execute();
  }

  /**
   * Initializing the context.
   * @param resourceType the type of the resource.
   * @param resourceId the identifier of the resource which the wysiwyg is attached to.
   * @param wysiwygEditorConfig the configuration of the wysiwyg editor.
   */
  private void setWysiwygEditorSessionContext(final String resourceType, final String resourceId,
      final WysiwygEditorConfig wysiwygEditorConfig) {
    HttpSession session = getHttpRequest().getSession();
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    session.setAttribute("WYSIWYG_ComponentId", getComponentId());
    session.setAttribute("WYSIWYG_ComponentLabel", null);
    session.setAttribute("WYSIWYG_BrowseInfo", null);
    session.setAttribute("WYSIWYG_ObjectId", resourceId);
    session.setAttribute("WYSIWYG_ObjectType", resourceType);
    session.setAttribute("WYSIWYG_Language", wysiwygEditorConfig.getLanguage());

    final SettingBundle settings = gef.getFavoriteLookSettings();
    if (settings != null) {
      final String styleSheet = settings.getString("StyleSheet", "");
      if (isDefined(styleSheet)) {
        wysiwygEditorConfig.setStylesheet(styleSheet);
      }
    }
  }

  private String getConfigName() {
    if (isDefined(configName)) {
      return configName;
    }
    return SilverpeasComponent.getByInstanceId(getComponentId()).orElseThrow(
        () -> new WebApplicationException(unknown("component behind id", getComponentId()),
            Response.Status.NOT_FOUND)).getName();
  }

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  @Override
  protected String getResourceBasePath() {
    return WYSIWYG_CONFIG_BASE_URI;
  }
}
