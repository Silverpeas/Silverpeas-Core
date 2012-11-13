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
import static org.silverpeas.admin.web.AdminResourceURIs.COMPONENTS_BASE_URI;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlTransient;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;

/**
 * A REST Web resource giving component data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(COMPONENTS_BASE_URI + "/{componentId}")
@Authorized
public class ComponentResource extends AbstractAdminResource {

  @PathParam("componentId")
  private String componentId;

  @XmlTransient
  private String fullComponentId;

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
          (components.isEmpty() ? null : components.iterator().next());
      fullComponentId = (component != null ? component.getId() : componentId);
    }
    return fullComponentId;
  }
}
