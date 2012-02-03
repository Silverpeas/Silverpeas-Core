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

import static com.silverpeas.profile.web.ProfileResourceBaseURIs.USERS_BASE_URI;
import com.silverpeas.rest.RESTWebService;
import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A REST-based Web service that acts on the user profiles in Silverpeas.
 * Each provided method is a way to access a representation of one or several user profile. This
 * representation is vehiculed as a Web entity in the HTTP requests and responses.
 * 
 * The users that are published depend on some parameters whose the domain isolation and the
 * profile of the user behind the requesting.
 * The domain isolation defines the visibility of a user or a group of users in a given domain to
 * the others domains in Silverpeas.
 */
@Service
@Scope("request")
@Path(USERS_BASE_URI)
public class UserProfileResource extends RESTWebService {
  
  @Inject
  private UserProfileService profileService;

  /**
   * Creates a new instance of UserProfileResource
   */
  public UserProfileResource() {
  }

  /**
   * Gets all the users defined in Silverpeas.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SelectableUser[] getUsers(@QueryParam("group") String groupId) {
    checkUserAuthentication();
    List<UserDetail> allUsers;
    if (isDefined(groupId)) {
      allUsers = getAllUsersInGroup(groupId);
    } else {
      allUsers = getAllUsers();
    }
    return asWebEntity(allUsers, locatedAt(getUriInfo().getAbsolutePath()));
  }
  
  @GET
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public SelectableUser getUser(@PathParam("userId") String userId) {
    checkUserAuthentication();
    UserDetail theUser = UserDetail.getById(userId);
    if (theUser == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (getUserDetail().isDomainRestricted() && !theUser.getDomainId().equals(getUserDetail().getDomainId())) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, "The user with id {0} isn''t "
                + "authorized to access the profile of user with id {1}", new Object[]{theUser.getId(),
                  userId});
        throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return asWebEntity(theUser, identifiedBy(getUriInfo().getAbsolutePath()));
  }

  @Override
  protected String getComponentId() {
    throw new UnsupportedOperationException("The UserProfileResource doesn't belong to any component"
            + " instances");
  }
  
  protected URI locatedAt(final URI uri) {
    return uri;
  }
  
  protected URI identifiedBy(final URI uri) {
    return uri;
  }

  private SelectableUser[] asWebEntity(final List<UserDetail> allUsers, final URI baseUri) {
    return SelectableUser.fromUsers(allUsers, baseUri);
  }
  
  private SelectableUser asWebEntity(final UserDetail user, final URI userUri) {
    return SelectableUser.fromUser(user).withAsUri(userUri);
  }

  private List<UserDetail> getAllUsersInGroup(String groupId) {
    Group theGroup = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
    return theGroup.getAllUsers();
  }

  private List<UserDetail> getAllUsers() {
    List<UserDetail> allUsers;
    if (getUserDetail().isDomainRestricted()) {
      allUsers = UserDetail.getAllInDomain(getUserDetail().getDomainId());
    } else {
      allUsers = UserDetail.getAll();
    }
    return allUsers;
  }
}
