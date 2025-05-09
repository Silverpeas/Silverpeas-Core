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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs.GROUPS_BASE_URI;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A REST-based Web service that acts on the user groups in Silverpeas. Each provided method is a
 * way to access a representation of one or several user groups. This representation is carried as a
 * Web entity in the HTTP requests and responses.
 * <p>
 * The user groups that are published depend on some parameters whose the domain isolation and the
 * profile of the user behind the requesting. The domain isolation defines the visibility of a user
 * or a group of groups in a given domain to the others domains in Silverpeas.
 */
@WebService
@Path(GROUPS_BASE_URI)
@Authorized
public class UserGroupProfileResource extends RESTWebService {

  /**
   * The HTTP header parameter that provides the real size of the group profiles that match a query.
   * This parameter is useful for clients that use the pagination to filter the count of group
   * profiles to sent back.
   */
  public static final String RESPONSE_HEADER_GROUPSIZE = "X-Silverpeas-GroupSize";
  @Inject
  private UserProfileService profileService;

  /**
   * Gets all the root user groups in Silverpeas.
   *
   * @param groupIds requested group identifiers. If this parameter is filled, sub groups are also
   * returned.
   * @param withChildren if true the sub groups are also returned.
   * @param name a pattern on the name of the root groups to retrieve. If null, all the root groups
   * are fetched.
   * @param domain the unique identifier of the domain the groups has to be related.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of groups to sent back: those between ((page number - 1)
   * item count in the page) and ((page number - 1) item count in the page + item count in the
   * page).
   * @param userStatesToExclude the user states that users taken into account must not be in.
   * @return the JSON representation of the array of the groups matching the pattern.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllRootGroups(@QueryParam("ids") Set<String> groupIds,
      @QueryParam("withChildren") boolean withChildren,
      @QueryParam("name") String name, @QueryParam("page") String page,
      @QueryParam("domain") String domain,
      @QueryParam("userStatesToExclude") Set<UserState> userStatesToExclude) {
    UserGroupsSearchCriteriaBuilder criteriaBuilder = UserGroupsSearchCriteriaBuilder.
        aSearchCriteria();

    // Ids or not ids ?
    if (CollectionUtil.isNotEmpty(groupIds)) {
      // In that case, sub groups are also returned
      criteriaBuilder.withGroupIds(groupIds.toArray(new String[0]));
    } else if (!withChildren) {
      criteriaBuilder.withRootGroupSet();
    }

    // Domains
    String domainId = (Domain.MIXED_DOMAIN_ID.equals(domain) ? null : domain);
    if (getUser().isDomainRestricted()) {
      domainId = getUser().getDomainId();
      criteriaBuilder.withMixedDomainId();
    }

    // Users to exclude by their state
    userStateFilter(criteriaBuilder, userStatesToExclude);

    // Common parameters
    criteriaBuilder.withDomainId(domainId).withName(name).withPaginationPage(fromPage(page));

    SilverpeasList<Group> allGroups =
        getOrganisationController().searchGroups(criteriaBuilder.build());
    UserGroupProfileEntity[] entities =
        asWebEntity(allGroups, locatedAt(getUri().getAbsolutePath()));
    return Response.ok(entities).
        header(RESPONSE_HEADER_GROUPSIZE, allGroups.originalListSize()).
        header(RESPONSE_HEADER_ARRAYSIZE, allGroups.originalListSize()).build();
  }

  /**
   * Gets the groups of users having the privileges to access the specified Silverpeas application
   * instance. In the context some groups are parents of others groups, only the parent groups are
   * fetched, no their subgroups.
   *
   * @param instanceId the unique identifier of the Silverpeas application instance.
   * @param withChildren if true the sub groups are also returned.
   * @param roles the roles the groups must play. Null if no specific roles have to be played by the
   * groups.
   * @param matchingAllRoles boolean at true if the groups must play all the roles, false
   * otherwise.
   * @param resource the unique identifier of the resource in the component instance the groups to
   * get must have enough rights to access. This query filter is coupled with the <code>roles</code>
   * one. If it is not set, by default the resource referred is the whole component instance. As for
   * component instance identifier, a resource one is defined by its type followed by its
   * identifier.
   * @param name the pattern on the name the groups name must match. Null if all groups for the
   * specified application have to be fetched.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of groups to sent back: those between ((page number - 1)
   * item count in the page) and ((page number - 1) item count in the page + item count in the
   * page).
   * @param domain the unique identifier of the domain the groups has to be related.
   * @param userStatesToExclude the user states that users taken into account must not be in.
   * @return the JSON representation of the array with the parent groups having access the
   * application instance.
   */
  @GET
  @Path("application/{instanceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGroupsInApplication(@PathParam("instanceId") String instanceId,
      @QueryParam("withChildren") boolean withChildren,
      @QueryParam("roles") String roles,
      @QueryParam("matchingAllRoles") boolean matchingAllRoles,
      @QueryParam("resource") String resource,
      @QueryParam("name") String name,
      @QueryParam("page") String page,
      @QueryParam("domain") String domain,
      @QueryParam("userStatesToExclude") Set<UserState> userStatesToExclude) {
    String[] roleNames = (isDefined(roles) ? roles.split(",") : new String[0]);
    String domainId = (Domain.MIXED_DOMAIN_ID.equals(domain) ? null : domain);
    UserGroupsSearchCriteriaBuilder criteriaBuilder;
    if (getUser().isDomainRestricted()) {
      domainId = getUser().getDomainId();
      criteriaBuilder = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
          withComponentInstanceId(instanceId).
          withRoles(roleNames, matchingAllRoles).
          withResourceId(resource).
          withDomainId(domainId).
          withMixedDomainId().
          withName(name).
          withPaginationPage(fromPage(page));
    } else {
      criteriaBuilder = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
          withComponentInstanceId(instanceId).
          withRoles(roleNames, matchingAllRoles).
          withResourceId(resource).
          withDomainId(domainId).
          withName(name).
          withPaginationPage(fromPage(page));
    }

    if (withChildren) {
      criteriaBuilder.withChildren();
    }

    // Users to exclude by their state
    userStateFilter(criteriaBuilder, userStatesToExclude);

    SilverpeasList<Group> groups =
        getOrganisationController().searchGroups(criteriaBuilder.build());
    URI groupsUri = getUri().getBaseUriBuilder().path(GROUPS_BASE_URI).build();
    return Response.ok(asWebEntity(groups, locatedAt(groupsUri))).
        header(RESPONSE_HEADER_GROUPSIZE, groups.originalListSize()).
        header(RESPONSE_HEADER_ARRAYSIZE, groups.originalListSize()).build();
  }

  /**
   * Gets the group of users identified by the specified path.
   *
   * @param groupPath the path of group identifiers, from the root group down to the seeked one.
   * @return the JSON representation of the user group.
   */
  @GET
  @Path("{path: [0-9]+(/groups/[0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserGroupProfileEntity getGroup(@PathParam("path") String groupPath) {
    String[] groupIds = groupPath.split("/groups/");
    String groupId = groupIds[groupIds.length - 1];
    Group theGroup = profileService.getGroupAccessibleToUser(groupId, UserDetail.from(getUser()));
    return asWebEntity(theGroup, identifiedBy(getUri().getAbsolutePath()));
  }

  /**
   * Gets the direct subgroups of the group of groups identified by the specified path.
   *
   * @param groups the path of group identifiers, from the root group down to the group for which
   * the direct subgroups are seeked.
   * @param name a pattern the subgroup names must match. If null, all the direct subgroups are
   * fetched.
   * @param page the pagination parameters formatted as "page number;item count in the page". From
   * this parameter is computed the part of groups to sent back: those between ((page number - 1)
   * item count in the page) and ((page number - 1) item count in the page + item count in the
   * page).
   * @param withChildren if true the sub groups are also returned.
   * @param userStatesToExclude the user states that users taken into account must not be in.
   * @return a JSON representation of the array of the direct subgroups.
   */
  @GET
  @Path("{path:[0-9]+/groups(/[0-9]+/groups)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSubGroups(@PathParam("path") String groups, @QueryParam("name") String name,
      @QueryParam("page") String page,
      @QueryParam("withChildren") boolean withChildren,
      @QueryParam("userStatesToExclude") Set<UserState> userStatesToExclude) {
    String[] groupIds = groups.split("/groups/?");
    String groupId = groupIds[groupIds.length - 1]; // we don't check the correctness of the path
    profileService.getGroupAccessibleToUser(groupId, UserDetail.from(getUser()));
    UserGroupsSearchCriteriaBuilder criteriaBuilder;
    if (getUser().isDomainRestricted()) {
      String domainId = getUser().getDomainId();
      criteriaBuilder = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
          withSuperGroupId(groupId).
          withDomainId(domainId).
          withMixedDomainId().
          withName(name).
          withPaginationPage(fromPage(page));
    } else {
      criteriaBuilder = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
          withSuperGroupId(groupId).
          withName(name).
          withPaginationPage(fromPage(page));
    }

    if (withChildren) {
      criteriaBuilder.withChildren();
    }

    // Users to exclude by their state
    userStateFilter(criteriaBuilder, userStatesToExclude);

    SilverpeasList<Group> subgroups =
        getOrganisationController().searchGroups(criteriaBuilder.build());
    return Response.ok(asWebEntity(subgroups, locatedAt(getUri().getAbsolutePath()))).
        header(RESPONSE_HEADER_GROUPSIZE, subgroups.originalListSize()).
        header(RESPONSE_HEADER_ARRAYSIZE, subgroups.originalListSize()).build();
  }

  private void userStateFilter(final UserGroupsSearchCriteriaBuilder criteriaBuilder,
      final Set<UserState> userStatesToExclude) {
    final Set<UserState> statesToExclude = new HashSet<>();
    statesToExclude.add(UserState.REMOVED);
    if (CollectionUtil.isNotEmpty(userStatesToExclude)) {
      statesToExclude.addAll(userStatesToExclude);
    }
    criteriaBuilder.withUserStatesToExclude(statesToExclude.toArray(new UserState[0]));
  }

  @Override
  protected String getResourceBasePath() {
    return GROUPS_BASE_URI;
  }

  @Override
  public String getComponentId() {
    return null;
  }

  protected static URI locatedAt(final URI uri) {
    return uri;
  }

  protected static URI identifiedBy(final URI uri) {
    return uri;
  }

  private UserGroupProfileEntity[] asWebEntity(List<? extends Group> allGroups, URI baseUri) {
    return UserGroupProfileEntity.fromGroups(allGroups, baseUri);
  }

  private UserGroupProfileEntity asWebEntity(Group group, URI groupUri) {
    return UserGroupProfileEntity.fromGroup(group).withAsUri(groupUri);
  }

  @Override
  public void validateUserAuthorization(UserPrivilegeValidation validation) {
    User currentUser = getUser();
    if (currentUser.isAccessGuest()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }
}
