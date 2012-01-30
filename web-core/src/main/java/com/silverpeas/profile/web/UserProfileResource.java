/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.profile.web;

import com.silverpeas.profile.service.UserProfileService;
import com.silverpeas.rest.RESTWebService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * REST Web Service
 *
 * @author mmoquillon
 */
@Service
@Scope("request")
@Path("profile/users")
public class UserProfileResource extends RESTWebService {
  
  @Inject
  private UserProfileService service;

  /**
   * Creates a new instance of UserProfileResource
   */
  public UserProfileResource() {
  }

  /**
   * Retrieves representation of an instance of com.silverpeas.profile.UserProfileResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SelectableUser[] getAllUsers() {
    checkUserAuthentication();
    List<UserDetail> allUsers = service.getAllUsersAccessibleTo(getUserDetail().getId());
    return asWebEntity(allUsers, locatedAt(getUriInfo().getRequestUri()));
  }

  @Override
  protected String getComponentId() {
    throw new UnsupportedOperationException("The UserProfileResource doesn't belong to any component"
            + " instances");
  }
  
  protected URI locatedAt(URI uri) {
    return uri;
  }

  private SelectableUser[] asWebEntity(List<UserDetail> allUsers, URI baseUri) {
    return SelectableUser.fromUsers(allUsers, baseUri);
  }
}
