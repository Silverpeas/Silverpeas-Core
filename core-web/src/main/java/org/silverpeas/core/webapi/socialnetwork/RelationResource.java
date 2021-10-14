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
package org.silverpeas.core.webapi.socialnetwork;

import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * It represents a resource published in the WEB that represents the relationship of a user.
 * <p>
 * The WEB resource belongs always to the current user in the session underlying at the HTTP
 * request.
 */
@RequestScoped
@Path(RelationResource.PATH)
@Authenticated
public class RelationResource extends RESTWebService {

  static final String PATH = "relations";

  @Inject
  private RelationShipService relationShipService;

  @DELETE
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteRelation(@PathParam("userId") final String userId) {
    boolean removed = relationShipService
        .removeRelationShip(Integer.parseInt(getUser().getId()), Integer.parseInt(userId));
    if (removed) {
      return Response.ok().build();
    } else {
      return Response.serverError().build();
    }
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return null;
  }
}
