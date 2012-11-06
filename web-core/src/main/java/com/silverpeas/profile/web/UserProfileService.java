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

import com.stratelia.webactiv.beans.admin.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * This service provides several common operations for the REST-based resources representing the
 * users and the user groups.
 *
 */
@Named
class UserProfileService {

  @Inject
  private OrganizationController organizationController;

  /**
   * Gets the group with the specified unique identifier and that is accessible to the specified
   * user.
   *
   * @param groupId the unique identifier of the group to get.
   * @param user the user for which the group has to be accessible.
   * @return the group corresponding to the specified unique identifier.
   * @throws WebApplicationException exception if either the group doesn't exist or it cannot be
   * accessible to the specified user.
   */
  public Group getGroupAccessibleToUser(String groupId, final UserDetail user) throws
      WebApplicationException {
    Group theGroup = Group.getById(groupId);
    if (theGroup == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else {
      if (user.isDomainRestricted() && !user.getDomainId().equals(theGroup.getDomainId())) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "The user with id {0} isn''t "
            + "authorized to access the group with id {1}", new Object[]{user.getId(),
              groupId});
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
    }
    return theGroup;
  }

  /**
   * Gets the unique identifier of all of the specified role names for a given component instance.
   *
   * @param instanceId the unique identifier of the component instance for which the roles are
   * defined.
   * @param roleNames the name of the roles for which the identifier is asked.
   * @return an array of role identifiers.
   */
  public String[] getRoleIds(String instanceId, String objectId, String[] roleNames) {
    List<ProfileInst> roles;
    if (isDefined(objectId)) {
      roles = getRolesOnObjectInComponentInstance(objectId, instanceId);
    } else {
      ComponentInst instance = getOrganizationController().getComponentInst(instanceId);
      roles = instance.getAllProfilesInst();
    }
    List<String> listOfRoleNames = Arrays.asList(roleNames);
    return filterRolesId(listOfRoleNames, roles);
  }

  /**
   * Gets all the user roles that are defined for the specified object in the given component instance.
   * The roles are thoses for which the users should have to access the specified object.
   * @param objectId the unique identifier of the object, defined by the concatenation of its
   * type and its identifier.
   * @param instanceId the unique identifier of the component instance in which live the object.
   * @return a list of user profiles (user roles in Silverpeas).
   */
  private List<ProfileInst> getRolesOnObjectInComponentInstance(String objectId, String instanceId) {
    Pattern objectIdPattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
    Matcher matcher = objectIdPattern.matcher(objectId);
    if (matcher.matches() && matcher.groupCount() == 2) {
      String type = matcher.group(1);
      String id = matcher.group(2);
      return getOrganizationController().getUserProfiles(instanceId, id, type);
    }
    return null;
  }

  /**
   * Filters the specified list of user roles by their name and returns the identifier of the roles
   * that match the specified role names.
   *
   * @param roleNames the names the roles to filter have to satisfy.
   * @param roles the list of roles to filter.
   * @return an array with the identifiers of the filtered roles.
   */
  private String[] filterRolesId(List<String> roleNames, List<ProfileInst> roles) {
    List<String> roleIds = new ArrayList<String>();
    if (roles != null && !roles.isEmpty()) {
      for (ProfileInst aRole : roles) {
        if (roleNames.isEmpty() || roleNames.contains(aRole.getName())) {
          roleIds.add(aRole.getId());
        }
      }
    }
    return roleIds.toArray(new String[roleIds.size()]);
  }

  private OrganizationController getOrganizationController() {
    return organizationController;
  }
}
