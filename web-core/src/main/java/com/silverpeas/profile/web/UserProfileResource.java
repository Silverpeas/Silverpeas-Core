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
import com.silverpeas.socialNetwork.relationShip.RelationShip;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A REST-based Web service that acts on the user profiles in Silverpeas. Each provided method is a
 * way to access a representation of one or several user profile. This representation is vehiculed
 * as a Web entity in the HTTP requests and responses.
 *
 * The users that are published depend on some parameters whose the domain isolation and the profile
 * of the user behind the requesting. The domain isolation defines the visibility of a user or a
 * group of users in a given domain to the others domains in Silverpeas.
 */
@Service
@Scope("request")
@Path(USERS_BASE_URI)
public class UserProfileResource extends RESTWebService {

  /**
   * The HTTP header parameter that provides the real size of the user profiles that match a query.
   * This parameter is usefull for clients that use the pagination to filter the count of user
   * profiles to sent back.
   */
  public static final String RESPONSE_HEADER_USERSIZE = "X-Silverpeas-UserSize";
  @Inject
  private UserProfileService profileService;
  @Inject
  private RelationShipService relationShipService;

  /**
   * Creates a new instance of UserProfileResource
   */
  public UserProfileResource() {
  }

  /**
   * Gets the users defined in Silverpeas and that matches the specified optional query parameters.
   * If no query parameters are set, then all the users in Silverpeas are sent back.
   *
   * The users to sent back can be filtered by a pattern their name has to satisfy, by the group
   * they must belong to, and by some pagination parameters.
   *
   * In the response is indicated as an HTTP header (named X-Silverpeas-UserSize) the real size of
   * the users that matches the query. This is usefull for clients that use the pagination to filter
   * the count of the answered users.
   *
   * @param groupId the unique identifier of the group the users must belong to.
   * @param name a pattern the name of the users has to satisfy. The wildcard * means anything
   * string of characters.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of users to sent back: those between ((page number - 1) *
   * item count in the page) and ((page number - 1) * item count in the page + item count in the
   * page).
   * @return the JSON serialization of the array with the user profiles that matches the query.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers(
          @QueryParam("group") String groupId,
          @QueryParam("name") String name,
          @QueryParam("page") String page) {
    checkUserAuthentication();
    String domainId = null;
    if (isDefined(groupId)) {
      Group group = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
      domainId = group.getDomainId();
    }
    UserDetail[] users = getOrganizationController().searchUsers(null, null, groupId,
            aFilteringModel(name, domainId));
    UserDetail[] paginatedUsers = paginate(users, page);
    return Response.ok(
            asWebEntity(Arrays.asList(paginatedUsers), locatedAt(getUriInfo().getAbsolutePath()))).
            header(RESPONSE_HEADER_USERSIZE, users.length).build();
  }

  /**
   * Gets the profile on the user that is identified by the unique identifier refered by the URI.
   *
   * @param userId the unique identifier of the user.
   * @return the profile of the user in a JSON representation.
   */
  @GET
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserProfileEntity getUser(@PathParam("userId") String userId) {
    checkUserAuthentication();
    UserDetail theUser = getUserDetailById(userId);
    return asWebEntity(theUser, identifiedBy(getUriInfo().getAbsolutePath()));
  }

  /**
   * Gets the profiles of the users that have access to the specified Silverpeas component instance
   * and that matches the specified optional query parameters. If no query parameters are set, then
   * all the users with the rights to access the component instance are sent back.
   *
   * The users to sent back can be filtered by a pattern their name has to satisfy, by the group
   * they must belong to, and by some pagination parameters.
   *
   * In the response is indicated as an HTTP header (named X-Silverpeas-UserSize) the real size of
   * the users that matches the query. This is usefull for clients that use the pagination to filter
   * the count of the answered users.
   *
   * @param instanceId the unique identifier of the component instance the users should have access
   * to.
   * @param groupId the unique identifier of the group the users must belong to.
   * @param name a pattern the name of the users has to satisfy. The wildcard * means anything
   * string of characters.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of users to sent back: those between ((page number - 1) *
   * item count in the page) and ((page number - 1) * item count in the page + item count in the
   * page).
   * @return the JSON serialization of the array with the user profiles that can access the
   * Silverpeas component and that matches the query.
   */
  @GET
  @Path("application/{instanceId}")
  public Response getApplicationUsers(
          @PathParam("instanceId") String instanceId,
          @QueryParam("group") String groupId,
          @QueryParam("roles") String roles,
          @QueryParam("name") String name,
          @QueryParam("page") String page) {
    checkUserAuthentication();
    String[] rolesIds = (isDefined(roles) ? profileService.getRoleIds(instanceId, roles.split(","))
            : null);
    String domainId = null;
    if (isDefined(groupId)) {
      Group group = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
      domainId = group.getDomainId();
    }
    UserDetail[] users = getOrganizationController().searchUsers(instanceId, rolesIds, groupId,
            aFilteringModel(name, domainId));
    UserDetail[] paginatedUsers = paginate(users, page);
    URI usersUri = getUriInfo().getBaseUriBuilder().path(USERS_BASE_URI).build();
    return Response.ok(
            asWebEntity(Arrays.asList(paginatedUsers), locatedAt(usersUri))).
            header(RESPONSE_HEADER_USERSIZE, users.length).build();
  }

  /**
   * Gets the profile on the user that is identified by the unique identifier refered by the URI.
   *
   * @param userId the unique identifier of the user.
   * @return the profile of the user in a JSON representation.
   */
  @GET
  @Path("{userId}/contacts")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserContacts(@PathParam("userId") String userId,
          @QueryParam("application") String instanceId,
          @QueryParam("roles") String roles,
          @QueryParam("name") String name,
          @QueryParam("page") String page) {
    checkUserAuthentication();
    UserDetail theUser = getUserDetailById(userId);
    int theUserId = Integer.valueOf(theUser.getId());
    List<RelationShip> relationships;
    try {
      relationships = relationShipService.getAllMyRelationShips(theUserId);
    } catch (SQLException ex) {
      Logger.getLogger(UserProfileResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    List<UserDetail> contacts = new ArrayList<UserDetail>(relationships.size());
    for (RelationShip relationShip : relationships) {
      UserDetail model = aFilteringModel(null, null);
      if (theUserId == relationShip.getUser1Id()) {
        model.setId(String.valueOf(relationShip.getUser2Id()));
      } else {
        model.setId(String.valueOf(relationShip.getUser1Id()));
      }
      String[] rolesIds = (isDefined(roles)
              ? profileService.getRoleIds(instanceId, roles.split(","))
              : null);
      UserDetail[] users = getOrganizationController().searchUsers(instanceId, rolesIds, null,
              model);
      if (users.length == 1) {
        UserDetail contact = users[0];
        if (isDefined(name)) {
          String pattern = name.replaceAll("\\*", "\\\\w*").toLowerCase();
          if (contact.getFirstName().toLowerCase().matches(pattern) || contact.getLastName().
                  toLowerCase().matches(pattern)) {
            contacts.add(contact);
          }
        } else {
          contacts.add(contact);
        }
      }
    }
    UserDetail[] paginatedUsers = paginate(contacts, page);
    URI usersUri = getUriInfo().getBaseUriBuilder().path(USERS_BASE_URI).build();
    return Response.ok(
            asWebEntity(Arrays.asList(paginatedUsers), locatedAt(usersUri))).
            header(RESPONSE_HEADER_USERSIZE, contacts.size()).build();
  }

  @Override
  protected String getComponentId() {
    throw new UnsupportedOperationException("The UserProfileResource doesn't belong to any component"
            + " instances");
  }

  protected static URI locatedAt(final URI uri) {
    return uri;
  }

  protected static URI identifiedBy(final URI uri) {
    return uri;
  }

  private UserProfileEntity[] asWebEntity(final List<? extends UserDetail> allUsers,
          final URI baseUri) {
    return UserProfileEntity.fromUsers(allUsers, baseUri);
  }

  private UserProfileEntity asWebEntity(final UserDetail user, final URI userUri) {
    return UserProfileEntity.fromUser(user).withAsUri(userUri);
  }

  private UserDetail getUserDetailById(String userId) {
    UserDetail theUser = UserDetail.getById(userId);
    if (theUser == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (getUserDetail().isDomainRestricted() && !theUser.getDomainId().equals(getUserDetail().
            getDomainId())) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, "The user with id {0} isn''t "
              + "authorized to access the profile of user with id {1}", new Object[]{theUser.getId(),
                userId});
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return theUser;
  }

  private UserDetail aFilteringModel(String name, String domainId) {
    UserDetail model = new UserDetail();
    if (getUserDetail().isDomainRestricted()) {
      model.setDomainId(getUserDetail().getDomainId());
    } else if (isDefined(domainId) && Integer.valueOf(domainId) > 0) {
      model.setDomainId(domainId);
    }
    if (isDefined(name)) {
      String filterByName = name.replaceAll("\\*", "%");
      model.setFirstName(filterByName);
      model.setLastName(filterByName);
    }
    return model;
  }

  private UserDetail[] paginate(List<UserDetail> users, String pagination) {
    return paginate(users.toArray(new UserDetail[users.size()]), pagination);
  }

  private UserDetail[] paginate(UserDetail[] users, String pagination) {
    try {
      UserDetail[] paginatedUsers;
      if (pagination != null && !pagination.isEmpty()) {
        String[] page = pagination.split(";");
        int nth = Integer.valueOf(page[0]);
        int count = Integer.valueOf(page[1]);
        int begin = (nth - 1) * count;
        int end = begin + count;
        if (end > users.length) {
          end = users.length;
        }
        paginatedUsers = new UserDetail[end - begin];
        for (int i = begin, j = 0; i < end; i++, j++) {
          paginatedUsers[j] = users[i];
        }
      } else {
        paginatedUsers = users;
      }
      return paginatedUsers;
    } catch (Exception ex) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
  }
}
