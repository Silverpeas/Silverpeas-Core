/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.socialnetwork.relationShip.RelationShip;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.util.ListSlice;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs.USERS_BASE_URI;
import static org.silverpeas.core.util.StringUtil.isDefined;

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
   * This parameter is useful for clients that use the pagination to filter the count of user
   * profiles to sent back.
   */
  public static final String RESPONSE_HEADER_USERSIZE = "X-Silverpeas-UserSize";
  /**
   * Specific identifier of a user group meaning all the user groups in Silverpeas. In that case,
   * only the users part of a user group will be fetched.
   */
  public static final String QUERY_ALL_GROUPS = "all";
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
   * @return The user entity corresponding to the token specified in the URI.
   */
  @GET
  @Path("token")
  @Produces(MediaType.APPLICATION_JSON)
  public UserProfileEntity checkUserToken() {
    try {
      return UserProfileEntity.fromUser(getUserDetail()).withAsUri(getUriInfo().getRequestUri());
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
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
   * @param userIds requested user identifiers
   * @param groupId the unique identifier of the group the users must belong to. The particular
   * identifier "all" means all user groups.
   * @param name a pattern the name of the users has to satisfy. The wildcard * means anything
   * string of characters.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of users to sent back: those between ((page number - 1) *
   * item count in the page) and ((page number - 1) * item count in the page + item count in the
   * page).
   * @param domain the unique identifier of the domain the users have to be related.
   * @param accessLevels filters the users by the access level in Silverpeas.
   * @param userStatesToExclude the user states that users taken into account must not be in.
   * @return the JSON serialization of the array with the user profiles that matches the query.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers(@QueryParam("id") Set<String> userIds,
      @QueryParam("group") String groupId,
      @QueryParam("name") String name,
      @QueryParam("page") String page,
      @QueryParam("domain") String domain,
      @QueryParam("accessLevel") Set<UserAccessLevel> accessLevels,
      @QueryParam("userStatesToExclude") Set<UserState> userStatesToExclude) {
    String domainId = (Domain.MIXED_DOMAIN_ID.equals(domain) ? null : domain);
    if (isDefined(groupId) && !groupId.equals(QUERY_ALL_GROUPS)) {
      Group group = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
      domainId = group.getDomainId();
    }
    if (getUserDetail().isDomainRestricted()) {
      domainId = getUserDetail().getDomainId();
    }
    UserProfilesSearchCriteriaBuilder criteriaBuilder = UserProfilesSearchCriteriaBuilder
        .aSearchCriteria().withDomainId(domainId).
        withGroupId(groupId).
        withName(name).
        withPaginationPage(fromPage(page));
    if (CollectionUtil.isNotEmpty(userIds)) {
      criteriaBuilder.withUserIds(userIds.toArray(new String[userIds.size()]));
    }
    if (CollectionUtil.isNotEmpty(accessLevels)) {
      criteriaBuilder
          .withAccessLevels(accessLevels.toArray(new UserAccessLevel[accessLevels.size()]));
    }

    // Users to exclude by their state
    if (CollectionUtil.isNotEmpty(userStatesToExclude)) {
      criteriaBuilder.withUserStatesToExclude(
          userStatesToExclude.toArray(new UserState[userStatesToExclude.size()]));
    }
    ListSlice<UserDetail> users = getOrganisationController().searchUsers(criteriaBuilder.build());
    return Response.ok(
        asWebEntity(users, locatedAt(getUriInfo().getAbsolutePath()))).
        header(RESPONSE_HEADER_USERSIZE, users.getOriginalListSize()).
        header(RESPONSE_HEADER_ARRAYSIZE, users.getOriginalListSize()).build();
  }

  /**
   * Gets the profile on the user that is identified by the unique identifier refered by the URI.
   * The unique identifier in the URI accepts also the specific term <i>me</i> to refers the current
   * user of the session within which the request is received.
   *
   * @param userId the unique identifier of the user.
   * @param extended more user details (full details).
   * @return the profile of the user in a JSON representation.
   */
  @GET
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserProfileEntity getUser(@PathParam("userId") String userId,
      @QueryParam("extended") boolean extended) {
    final URI uri = identifiedBy(getUriInfo().getAbsolutePath());
    if (extended) {
      return asWebEntity(getUserFullMatching(userId), uri);
    } else {
      return asWebEntity(getUserDetailMatching(userId), uri);
    }
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
   * @param roles the name of the roles the users must play either for the component instance or for
   * a given resource of the component instance.
   * @param resource the unique identifier of the resource in the component instance the users to
   * get must have enough rights to access. This query filter is coupled with the <code>roles</code>
   * one. If it is not set, by default the resource refered is the whole component instance. As for
   * component instance identifier, a resource one is defined by its type followed by its
   * identifier.
   * @param name a pattern the name of the users has to satisfy. The wildcard * means anything
   * string of characters.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of users to sent back: those between ((page number - 1) *
   * item count in the page) and ((page number - 1) * item count in the page + item count in the
   * page).
   * @param userStatesToExclude the user states that users taken into account must not be in.
   * @return the JSON serialization of the array with the user profiles that can access the
   * Silverpeas component and that matches the query.
   */
  @GET
  @Path("application/{instanceId}")
  public Response getApplicationUsers(
      @PathParam("instanceId") String instanceId,
      @QueryParam("group") String groupId,
      @QueryParam("roles") String roles,
      @QueryParam("resource") String resource,
      @QueryParam("name") String name,
      @QueryParam("page") String page,
      @QueryParam("userStatesToExclude") Set<UserState> userStatesToExclude) {
    String[] roleNames = (isDefined(roles) ? roles.split(",") : null);
    String domainId = null;
    if (isDefined(groupId) && !groupId.equals(QUERY_ALL_GROUPS)) {
      Group group = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
      domainId = group.getDomainId();
    }
    if (getUserDetail().isDomainRestricted()) {
      domainId = getUserDetail().getDomainId();
    }
    UserProfilesSearchCriteriaBuilder criteriaBuilder = UserProfilesSearchCriteriaBuilder
        .aSearchCriteria().withDomainId(domainId).
        withComponentInstanceId(instanceId).
        withRoles(roleNames).
        withResourceId(resource).
        withGroupId(groupId).
        withName(name).
        withPaginationPage(fromPage(page));

    // Users to exclude by their state
    if (CollectionUtil.isNotEmpty(userStatesToExclude)) {
      criteriaBuilder.withUserStatesToExclude(
          userStatesToExclude.toArray(new UserState[userStatesToExclude.size()]));
    }

    ListSlice<UserDetail> users = getOrganisationController().searchUsers(criteriaBuilder.build());
    URI usersUri = getUriInfo().getBaseUriBuilder().path(USERS_BASE_URI).build();
    return Response.ok(
        asWebEntity(users, locatedAt(usersUri))).
        header(RESPONSE_HEADER_USERSIZE, users.getOriginalListSize()).
        header(RESPONSE_HEADER_ARRAYSIZE, users.getOriginalListSize()).build();
  }

  /**
   * Gets the profile on the user that is identified by the unique identifier refered by the URI.
   * The unique identifier in the URI accepts also the specific term <i>me</i> to refers the current
   * user of the session within which the request is received.
   *
   * @param userId the unique identifier of the user or <i>me</i> to refers the current user at the
   * origin of the request.
   * @param instanceId the unique identifier of the component instance the users should have access
   * to.
   * @param roles the name of the roles the users must play either for the component instance or for
   * a given resource of the component instance.
   * @param resource the unique identifier of the resource in the component instance the users to
   * get must have enough rights to access. This query filter is coupled with the <code>roles</code>
   * one. If it is not set, by default the resource refered is the whole component instance. As for
   * component instance identifier, a resource one is defined by its type followed by its
   * identifier.
   * @param name a pattern the name of the users has to satisfy. The wildcard * means anything
   * string of characters.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of users to sent back: those between ((page number - 1) *
   * item count in the page) and ((page number - 1) * item count in the page + item count in the
   * page).
   * @param domain the unique identifier of the domain the users have to be related.
   * @param userStatesToExclude the user states that users taken into account must not be in.
   * @return the profile of the user in a JSON representation.
   */
  @GET
  @Path("{userId}/contacts")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserContacts(@PathParam("userId") String userId,
      @QueryParam("application") String instanceId,
      @QueryParam("roles") String roles,
      @QueryParam("resource") String resource,
      @QueryParam("name") String name,
      @QueryParam("page") String page,
      @QueryParam("domain") String domain,
      @QueryParam("userStatesToExclude") Set<UserState> userStatesToExclude) {
    String domainId = (Domain.MIXED_DOMAIN_ID.equals(domain) ? null : domain);
    UserDetail theUser = getUserDetailMatching(userId);
    String[] roleNames = (isDefined(roles) ? roles.split(",") : null);
    String[] contactIds = getContactIds(theUser.getId());
    ListSlice<UserDetail> contacts;
    if (contactIds.length > 0) {
      UserProfilesSearchCriteriaBuilder criteriaBuilder = UserProfilesSearchCriteriaBuilder
          .aSearchCriteria().
          withComponentInstanceId(instanceId).
          withDomainId(domainId).
          withRoles(roleNames).
          withResourceId(resource).
          withUserIds(contactIds).
          withName(name).
          withPaginationPage(fromPage(page));

      // Users to exclude by their state
      if (CollectionUtil.isNotEmpty(userStatesToExclude)) {
        criteriaBuilder.withUserStatesToExclude(
            userStatesToExclude.toArray(new UserState[userStatesToExclude.size()]));
      }

      contacts = getOrganisationController().searchUsers(criteriaBuilder.build());
    } else {
      contacts = new ListSlice<UserDetail>(0, 0, 0);
    }
    URI usersUri = getUriInfo().getBaseUriBuilder().path(USERS_BASE_URI).build();
    return Response.ok(
        asWebEntity(contacts, locatedAt(usersUri))).
        header(RESPONSE_HEADER_USERSIZE, contacts.getOriginalListSize()).
        header(RESPONSE_HEADER_ARRAYSIZE, contacts.getOriginalListSize()).build();
  }

  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException(
        "The UserProfileResource doesn't belong to any component"
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

  private UserProfileExtendedEntity asWebEntity(final UserFull user, final URI userUri) {
    return UserProfileExtendedEntity.fromUser(user).withAsUri(userUri);
  }

  private UserDetail getUserDetailById(String userId) {
    UserDetail theUser = UserDetail.getById(userId);
    checkUser(userId, theUser);
    return theUser;
  }

  private UserFull getUserFullById(String userId) {
    UserFull theUser = UserFull.getById(userId);
    checkUser(userId, theUser);
    return theUser;
  }

  private void checkUser(String userId, UserDetail theUser) {
    if (theUser == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (!theUser.isAccessAdmin() && getUserDetail().isDomainRestricted() && !theUser.getDomainId().
        equals(getUserDetail().getDomainId())) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, "The user with id {0} isn''t "
          + "authorized to access the profile of user with id {1}", new Object[]{theUser.getId(),
            userId});
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
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

  /**
   * Gets the detail about the user that matchs the specified identifier. The identifier is a
   * pattern that accepts either a user unique identifier or the specific word <i>me</i>. Latest
   * means the current user of the underlying HTTP session.
   *
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

  /**
   * Gets all details about the user that matchs the specified identifier. The identifier is a
   * pattern that accepts either a user unique identifier or the specific word <i>me</i>. Latest
   * means the current user of the underlying HTTP session.
   *
   * @param identifier an identifier.
   * @return the detail about a user.
   */
  private UserFull getUserFullMatching(String identifier) {
    if (identifier.equals("me")) {
      return getUserFullById(getUserDetail().getId());
    } else {
      return getUserFullById(identifier);
    }
  }

  private PaginationPage fromPage(String page) {
    PaginationPage paginationPage = null;
    if (page != null && !page.isEmpty()) {
      String[] pageAttributes = page.split(";");
      int nth = Integer.valueOf(pageAttributes[0]);
      int count = Integer.valueOf(pageAttributes[1]);
      if (count > 0) {
        paginationPage = new PaginationPage(nth, count);
      }
    }
    return paginationPage;
  }
}
