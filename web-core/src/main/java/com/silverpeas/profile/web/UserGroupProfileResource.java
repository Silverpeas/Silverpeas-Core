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
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.GroupsSearchCriteria;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.silverpeas.profile.web.ProfileResourceBaseURIs.GROUPS_BASE_URI;
import static com.silverpeas.util.StringUtil.isDefined;

/**
 * A REST-based Web service that acts on the user groups in Silverpeas. Each provided method is a
 * way to access a representation of one or several user groups. This representation is vehiculed as
 * a Web entity in the HTTP requests and responses.
 *
 * The user groups that are published depend on some parameters whose the domain isolation and the
 * profile of the user behind the requesting. The domain isolation defines the visibility of a user
 * or a group of users in a given domain to the others domains in Silverpeas.
 */
@Service
@RequestScoped
@Path(GROUPS_BASE_URI)
@Authenticated
public class UserGroupProfileResource extends RESTWebService {

  @Inject
  private UserProfileService profileService;

  /**
   * Creates a new instance of UserGroupProfileResource
   */
  public UserGroupProfileResource() {
  }

  /**
   * Gets all the root user groups in Silverpeas.
   *
   * @param name a pattern on the name of the root groups to retrieve. If null, all the root groups
   * are fetched.
   * @param domain the unique identifier of the domain the groups has to be related.
   * @return the JSON representation of the array of the groups matching the pattern.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public UserGroupProfileEntity[] getAllRootGroups(@QueryParam("name") String name,
          @QueryParam("domain") String domain) {
    GroupsSearchCriteria criteria;
    String domainId = (Domain.MIXED_DOMAIN_ID.equals(domain) ? null:domain);
    if (getUserDetail().isDomainRestricted()) {
      domainId = getUserDetail().getDomainId();
      criteria = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
              withRootGroupSet().
              withDomainId(domainId).
              withMixedDomainId().
              withName(name).build();
    } else {
      criteria = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
              withRootGroupSet().
              withDomainId(domainId).
              withName(name).build();
    }
    Group[] allGroups = getOrganizationController().searchGroups(criteria);
    return asWebEntity(groupsNotEmpty(allGroups), locatedAt(getUriInfo().getAbsolutePath()));
  }

  /**
   * Gets the groups of users having the priviledges to access the specified Silverpeas application
   * instance. In the context some groups are parents of others groups, only the parent groups are
   * fetched, no their subgroups.
   *
   * @param instanceId the unique identifier of the Silverpeas application instance.
   * @param roles the roles the groups must play. Null if no specific roles have to be played by the
   * groups.
   * @param resource the unique identifier of the resource in the component instance the groups to get
   * must have enough rights to access. This query filter is coupled with the <code>roles</code> one.
   * If it is not set, by default the resource refered is the whole component instance. As for
   * component instance identifier, a resource one is defined by its type followed by its identifier.
   * @param name the pattern on the name the groups name must match. Null if all groups for the
   * specified application have to be fetched.
   * @param domain the unique identifier of the domain the groups has to be related.
   * @return the JSON representation of the array with the parent groups having access the
   * application instance.
   */
  @GET
  @Path("application/{instanceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserGroupProfileEntity[] getGroupsInApplication(
          @PathParam("instanceId") String instanceId,
          @QueryParam("roles") String roles,
          @QueryParam("resource") String resource,
          @QueryParam("name") String name,
          @QueryParam("domain") String domain) {
    String[] roleNames = (isDefined(roles) ? roles.split(",") : new String[0]);
    String domainId = (Domain.MIXED_DOMAIN_ID.equals(domain) ? null:domain);
    GroupsSearchCriteria criteria;
    if (getUserDetail().isDomainRestricted()) {
      domainId = getUserDetail().getDomainId();
      criteria = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
              withComponentInstanceId(instanceId).
              withRoles(roleNames).
              withDomainId(domainId).
              withMixedDomainId().
              withName(name).build();
    } else {
      criteria = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
              withComponentInstanceId(instanceId).
              withRoles(roleNames).
              withDomainId(domainId).
              withName(name).build();
    }
    Group[] groups = getOrganizationController().searchGroups(criteria);
    URI groupsUri = getUriInfo().getBaseUriBuilder().path(GROUPS_BASE_URI).build();
    return asWebEntity(groupsNotEmpty(groups), locatedAt(groupsUri));
  }

  /**
   * Gets the group of users identified by the specified path.
   *
   * @param groupPath the path of group identifiers, from the root group downto the seeked one.
   * @return the JSON representation of the user group.
   */
  @GET
  @Path("{path: [0-9]+(/groups/[0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserGroupProfileEntity getGroup(@PathParam("path") String groupPath) {
    String[] groupIds = groupPath.split("/groups/");
    String groupId = groupIds[groupIds.length - 1];
    Group theGroup = profileService.getGroupAccessibleToUser(groupId, getUserDetail());
    return asWebEntity(theGroup, identifiedBy(getUriInfo().getAbsolutePath()));
  }

  /**
   * Gets the direct subgroups of the group of users identified by the specified path.
   *
   * @param groups the path of group identifiers, from the root group downto the group for which the
   * direct subgroups are seeked.
   * @param name a pattern the subgroup names must match. If null, all the direct subgroups are
   * fetched.
   * @return a JSON representation of the array of the direct subgroups.
   */
  @GET
  @Path("{path:[0-9]+/groups(/[0-9]+/groups)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserGroupProfileEntity[] getSubGroups(@PathParam("path") String groups,
          @QueryParam("name") String name) {
    String[] groupIds = groups.split("/groups/?");
    String groupId = groupIds[groupIds.length - 1]; // we don't check the correctness of the path
    profileService.getGroupAccessibleToUser(groupId, getUserDetail());
    GroupsSearchCriteria criteria;
    if (getUserDetail().isDomainRestricted()) {
      String domainId = getUserDetail().getDomainId();
      criteria = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
              withSuperGroupId(groupId).
              withDomainId(domainId).
              withMixedDomainId().
              withName(name).build();
    } else {
      criteria = UserGroupsSearchCriteriaBuilder.aSearchCriteria().
              withSuperGroupId(groupId).
              withName(name).build();
    }
    Group[] subgroups = getOrganizationController().searchGroups(criteria);

    return asWebEntity(groupsNotEmpty(subgroups), locatedAt(getUriInfo().getAbsolutePath()));
  }

  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException("The UserGroupProfileResource doesn't belong to any component"
            + " instances");
  }

  protected static URI locatedAt(final URI uri) {
    return uri;
  }

  protected static URI identifiedBy(final URI uri) {
    return uri;
  }

  private List<Group> groupsNotEmpty(final Group[] groups) {
    List<Group> noEmptyGroups = new ArrayList<Group>();
    for (Group group : groups) {
      if (group.getTotalNbUsers() > 0) {
        noEmptyGroups.add(group);
      }
    }
    return noEmptyGroups;
  }

  private UserGroupProfileEntity[] asWebEntity(List<? extends Group> allGroups, URI baseUri) {
    return UserGroupProfileEntity.fromGroups(allGroups, baseUri);
  }

  private UserGroupProfileEntity asWebEntity(Group group, URI groupUri) {
    return UserGroupProfileEntity.fromGroup(group).withAsUri(groupUri);
  }
}
