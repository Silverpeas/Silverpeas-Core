/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.webapi.cache;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.VolatileIdentifierProvider;
import org.silverpeas.core.cache.service.VolatileResourceCacheService;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * A REST Web resource which permits to obtain volatile identifier scoped into a component
 * instance.<br>
 * Please consule {@link VolatileResourceCacheService}.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(VolatileCacheResource.VOLATILE_BASE_URI + "/{componentInstanceId}")
@Authorized
public class VolatileCacheResource extends RESTWebService {

  static final String VOLATILE_BASE_URI = "volatile";

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  /**
   * Gets a new volatile identifier into the context of {@link #componentInstanceId} retrieved from
   * the URI.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * calendars.
   * @see WebProcess#execute()
   */
  @GET
  @Path("new")
  @Produces(MediaType.TEXT_PLAIN)
  public String newVolatileStringIdentifier() {
    return process(() -> VolatileIdentifierProvider.newVolatileStringIdentifierOn(getComponentId()))
        .lowestAccessRole(SilverpeasRole.writer).execute();
  }

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  @Override
  protected String getResourceBasePath() {
    return VOLATILE_BASE_URI;
  }
}
