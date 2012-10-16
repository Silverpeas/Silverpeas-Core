/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.accesscontrol;

import com.silverpeas.util.ComponentHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Check the access to a component for a user.
 * @author ehugonnet
 */
@Named
public class ComponentAccessController implements AccessController<String> {

  @Inject
  private OrganizationController controller;

  public ComponentAccessController() {
  }

  /**
   * For tests only.
   * @param controller the controller to set for tests.
   */
  protected void setOrganizationController(final OrganizationController controller) {
    this.controller = controller;
  }

  /**
   * Indicates that the rights are set on node as well as the component.
   * @param userId
   * @param componentId
   * @return
   */
  public boolean isRightOnTopicsEnabled(String userId, String componentId) {
    return isThemeTracker(componentId) && StringUtil.getBooleanValue(getOrganizationController().
        getComponentParameterValue(componentId, "rightsOnTopics"));
  }

  private boolean isThemeTracker(String componentId) {
    return ComponentHelper.getInstance().isThemeTracker(componentId);
  }

  @Override
  public boolean isUserAuthorized(String userId, String componentId) {
    // Personal space or user tool
    if (componentId == null || getOrganizationController().isToolAvailable(componentId)) {
      return true;
    }
    if (StringUtil.getBooleanValue(getOrganizationController().getComponentParameterValue(
        componentId, "publicFiles"))) {
      return true;
    }
    return getOrganizationController().isComponentAvailable(componentId, userId);
  }

  /**
   * Gets the organization controller used for performing its task.
   * @return an organization controller instance.
   */
  private OrganizationController getOrganizationController() {
    if (controller == null) {
      controller = new OrganizationController();
    }
    return controller;
  }
}
