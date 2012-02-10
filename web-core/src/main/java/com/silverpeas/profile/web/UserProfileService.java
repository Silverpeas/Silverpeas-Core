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
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

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
  public String[] getRoleIds(String instanceId, String[] roleNames) {
    List<String> roleIds = new ArrayList<String>();
    List<String> listOfRoleNames = Arrays.asList(roleNames);
    ComponentInst instance = getOrganizationController().getComponentInst(instanceId);
    List<ProfileInst> profiles = instance.getAllProfilesInst();
    for (ProfileInst aProfile : profiles) {
      if (listOfRoleNames.isEmpty() || listOfRoleNames.contains(aProfile.getName())) {
        roleIds.add(aProfile.getId());
      }
    }
    return roleIds.toArray(new String[roleIds.size()]);
  }

  private OrganizationController getOrganizationController() {
    return organizationController;
  }
}
