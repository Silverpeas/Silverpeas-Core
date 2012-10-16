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
package com.silverpeas.profile.web;

import com.silverpeas.annotation.Authenticated;
import static com.silverpeas.profile.web.ProfileResourceBaseURIs.USERS_BASE_URI;
import static com.silverpeas.profile.web.SearchCriteriaBuilder.aSearchCriteria;
import com.silverpeas.socialnetwork.relationShip.RelationShip;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import static com.silverpeas.util.StringUtil.isDefined;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SearchCriteria;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserSearchCriteriaFactory;
import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;

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
@RequestScoped
@Path(USERS_BASE_URI)
@Authenticated
public class UserProfileResource extends RESTWebService {

  /**
   * The HTTP header parameter that provides the real size of the user profiles that match a query.
   * This parameter is usefull for clients that use the pagination to filter the count of user
   * profiles to sent back.
   */
  public static final String RESPONSE_HEADER_USERSIZE = "X-Silverpeas-UserSize";
  /**
   * Specific identifier of a user group meaning all the user groups in Silverpeas. In that case,
   * only the users part of a user group will be fetched.
   */
  public static final String QUERY_ALL_GROUP = "all";
  @Inject
  private UserProfileService profileService;
  @Inject
  private RelationShipService relationShipService;
  private UserSearchCriteriaFactory criteriaFactory = UserSearchCriteriaFactory.getFactory();

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
   * @param groupId the unique identifier of the group the users must belong to. The particular
   * identifier "all" means all user groups.
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
    String domainId = null;
    if (isDefined(groupId) && !groupId.equals(QUERY_ALL_GROUP)) {
      Group group = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
      domainId = group.getDomainId();
    }
    SearchCriteria criteria = aSearchCriteria().withDomainId(domainId, getUserDetail()).
            withGroupId(groupId).
            withName(name).
            build();
    UserDetail[] users = getOrganizationController().searchUsers(criteria);
    UserDetail[] paginatedUsers = paginate(users, page);
    return Response.ok(
            asWebEntity(Arrays.asList(paginatedUsers), locatedAt(getUriInfo().getAbsolutePath()))).
            header(RESPONSE_HEADER_USERSIZE, users.length).build();
  }

  /**
   * Gets the profile on the user that is identified by the unique identifier refered by the URI.
   * The unique identifier in the URI accepts also the specific term <i>me</i> to refers the current
   * user of the session within which the request is received.
   *
   * @param userId the unique identifier of the user.
   * @return the profile of the user in a JSON representation.
   */
  @GET
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserProfileEntity getUser(@PathParam("userId") String userId) {
    UserDetail theUser = getUserDetailMatching(userId);
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
   * @param groupId the unique identifier of the group the users must belong to. The particular
   * identifier "all" means all user groups.
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
    String[] rolesIds = (isDefined(roles) ? profileService.getRoleIds(instanceId, roles.split(","))
            : null);
    String domainId = null;
    if (isDefined(groupId) && !groupId.equals(QUERY_ALL_GROUP)) {
      Group group = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
      domainId = group.getDomainId();
    }
    SearchCriteria criteria = aSearchCriteria().withDomainId(domainId, getUserDetail()).
            withComponentInstanceId(instanceId).
            withRoles(rolesIds).
            withGroupId(groupId).
            withName(name).
            build();
    UserDetail[] users = getOrganizationController().searchUsers(criteria);
    UserDetail[] paginatedUsers = paginate(users, page);
    URI usersUri = getUriInfo().getBaseUriBuilder().path(USERS_BASE_URI).build();
    return Response.ok(
            asWebEntity(Arrays.asList(paginatedUsers), locatedAt(usersUri))).
            header(RESPONSE_HEADER_USERSIZE, users.length).build();
  }

  /**
   * Gets the profile on the user that is identified by the unique identifier refered by the URI.
   * The unique identifier in the URI accepts also the specific term <i>me</i> to refers the current
   * user of the session within which the request is received.
   *
   * @param userId the unique identifier of the user or <i>me</i> to refers the current user at the
   * origin of the request.
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
    UserDetail theUser = getUserDetailMatching(userId);
    String[] rolesIds = (isDefined(roles) ? profileService.getRoleIds(instanceId, roles.split(","))
            : null);
    String[] contactIds = getContactIds(theUser.getId());
    UserDetail[] contacts;
    if (contactIds.length > 0) {
      SearchCriteria criteria = aSearchCriteria().withDomainId(null, getUserDetail()).
              withComponentInstanceId(instanceId).
              withRoles(rolesIds).
              withUserIds(contactIds).
              withName(name).
              build();
      contacts = getOrganizationController().searchUsers(criteria);
    } else {
      contacts = new UserDetail[0];
    }
    UserDetail[] paginatedUsers = paginate(contacts, page);
    URI usersUri = getUriInfo().getBaseUriBuilder().path(USERS_BASE_URI).build();
    return Response.ok(
            asWebEntity(Arrays.asList(paginatedUsers), locatedAt(usersUri))).
            header(RESPONSE_HEADER_USERSIZE, contacts.length).build();
  }

  @Override
  public String getComponentId() {
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
    if (!theUser.isAccessAdmin() && getUserDetail().isDomainRestricted() &&
            !theUser.getDomainId().equals(getUserDetail().getDomainId())) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, "The user with id {0} isn''t "
              + "authorized to access the profile of user with id {1}", new Object[]{theUser.getId(),
                userId});
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return theUser;
  }

  private String[] getContactIds(String userId) {
    try {
      int myId = Integer.valueOf(userId);
      List<RelationShip> relationShips = relationShipService.getAllMyRelationShips(myId);
      String[] userIds = new String[relationShips.size()];
      for (int i = 0; i < relationShips.size(); i++) {
        RelationShip relationShip = relationShips.get(i);
        if (relationShip.getUser1Id() != myId) {
          userIds[i] = String.valueOf(relationShip.getUser1Id());
        } else {
          userIds[i] = String.valueOf(relationShip.getUser2Id());
        }
      }
      return userIds;
    } catch (SQLException ex) {
      Logger.getLogger(UserProfileResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
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
  
  /**
   * Gets the detail about the user that matchs the specified identifier. The identifier is a pattern
   * that accepts either a user unique identifier or the specific word <i>me</i>. Latest means the
   * current user of the underlying HTTP session.
   * @param identifier an identifier.
   * @return the detail about a user.
   */
  private UserDetail getUserDetailMatching(String identifier) {
    if (identifier.equals("me")) {
      return getUserDetail();
    } else {
      return getUserDetailById(identifier);
    }
  }
}
