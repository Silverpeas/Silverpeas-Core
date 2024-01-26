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

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.COMPONENTS_BASE_URI;

/**
 * A REST Web resource giving components.
 * @author Nicolas EYSSERIC
 */
@WebService
@Path(COMPONENTS_BASE_URI)
@Authenticated
public class ComponentsResource extends AbstractAdminResource {

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
   * Gets the JSON representation of the existing ComponentInstLight known as "image banks".
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the
   * ComponentInstLight.
   */
  @GET
  @Produces(APPLICATION_JSON)
  public Collection<ComponentEntity> getComponentsByFilterOrParameterValue(
      @QueryParam("filter") final String filter, @QueryParam("param") final String param,
      @QueryParam("value") final String value) {
    if ("imagebanks".equals(filter)) {
      return getByParameterValue("viewInWysiwyg", "yes");
    } else if ("filebanks".equals(filter)) {
      return getByParameterValue("publicFiles", "yes");
    } else if (isDefined(param) && isDefined(value)) {
      if (!getUser().isAccessAdmin()) {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      return getByParameterValue(param, value);
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  private Collection<ComponentEntity> getByParameterValue(String param, String value) {
    try {
      Collection<ComponentInstLight> components =
          getAdminServices().getComponentsByParameterValue(param, value);
      return asWebEntities(ComponentEntity.class, components);
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
    return null;
  }
}