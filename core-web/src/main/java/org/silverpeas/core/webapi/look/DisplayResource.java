/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.webapi.look;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * A REST Web resource giving space data.
 * @author Yohann Chastagnier
 */
@WebService
@Path(LookResourceURIs.DISPLAY_BASE_URI)
@Authenticated
public class DisplayResource extends AbstractLookResource {

  @Override
  protected String getResourceBasePath() {
    return LookResourceURIs.DISPLAY_BASE_URI;
  }

  /**
   * Gets the JSON representation of the user display context.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of display context.
   */
  @GET
  @Path(LookResourceURIs.DISPLAY_USER_CONTEXT_URI_PART)
  @Produces(APPLICATION_JSON)
  public DisplayUserContextEntity getUserContext() {
    try {
      return DisplayUserContextEntity.createFrom(getLookDelegate().getHelper(),
          getUserPreferences()).withURI(getUri().getRequestUri());
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
