/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.accesscontrol;

import com.stratelia.webactiv.beans.admin.OrganizationController;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Check the access to a Silverpeas workspace for a user.
 */
@Named
public class WorkspaceAccessController implements AccessController<String> {

  @Inject
  private ComponentAccessController componentAccessController;
  @Inject
  private OrganizationController organizationController;

  public WorkspaceAccessController() {
  }

  @Override
  public boolean isUserAuthorized(String userId, String spaceId) {
    boolean isAuthorized = false;
    if (spaceId == null) { // Personal space
      isAuthorized = true;
    } else {
      String[] componentIds = getOrganizationController().getAllComponentIds(spaceId);
      for (String componentId : componentIds) {
        if (getComponentAccessController().isUserAuthorized(userId, componentId)) {
          isAuthorized = true;
          break;
        }
      }
    }
    return isAuthorized;
  }

  /**
   * Gets controller of access on compoents used for performing its task.
   * @return a component access controller.
   */
  private ComponentAccessController getComponentAccessController() {
    if (componentAccessController == null) {
      componentAccessController = new ComponentAccessController();
    }
    return componentAccessController;
  }

  /**
   * Gets the organization controller used for performing its task.
   * @return an organization controller instance.
   */
  private OrganizationController getOrganizationController() {
    if (organizationController == null) {
      organizationController = new OrganizationController();
    }
    return organizationController;
  }
}
