/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package org.silverpeas.admin.web;

import javax.inject.Inject;
import javax.inject.Named;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * This service provides several common operations for the REST-based resources representing admin
 * entities.
 * @author Yohann Chastagnier
 */
@Named
public class AdminWebService {

  @Inject
  private OrganizationController organizationController;

  /**
   * Gets ids of available root spaces of a user
   * @param userId
   * @return never null String array
   */
  public String[] getAllRootSpaceIds(final String userId) {
    return getOrganizationController().getAllRootSpaceIds(userId);
  }

  /**
   * Gets ids of available spaces of a space and a user. It returns only spaces of the next level
   * @param spaceId
   * @param userId
   * @return never null String array
   */
  public String[] getAllSubSpaceIds(final String spaceId, final String userId) {
    return getOrganizationController().getAllSubSpaceIds(spaceId, userId);
  }

  /**
   * Gets a space from its id
   * @param spaceId
   * @return SpaceInstLight instantiated or null if not exists one with the given space id
   */
  public SpaceInstLight getSpaceById(final String spaceId) {
    return getOrganizationController().getSpaceInstLightById(spaceId);
  }

  /**
   * Gets ids of available components of a space and a user.
   * @param spaceId
   * @param userId
   * @return never null String array
   */
  public String[] getAllComponentIds(final String spaceId, final String userId) {
    return getOrganizationController().getAvailCompoIdsAtRoot(spaceId, userId);
  }

  /**
   * Gets a component from its id
   * @param componentId
   * @return ComponentInstLight instantiated or null if not exists one with the given component id
   */
  public ComponentInstLight getComponentById(final String componentId) {
    return getOrganizationController().getComponentInstLight(componentId);
  }

  /**
   * Gets the OrganizationController instance
   * @return
   */
  private OrganizationController getOrganizationController() {
    return organizationController;
  }
}
