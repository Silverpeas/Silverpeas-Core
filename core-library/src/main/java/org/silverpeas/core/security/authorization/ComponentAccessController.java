/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.getBooleanValue;

/**
 * It controls the access of a user to a given Silverpeas component. A Silverpeas component can be
 * either a Silverpeas application instance (like a KMelia instance for example) or a user
 * personal tool or an administrative tool.
 * @author ehugonnet
 */
@Singleton
public class ComponentAccessController extends AbstractAccessController<String>
    implements ComponentAccessControl {

  @Inject
  private OrganizationController controller;

  ComponentAccessController() {
    // Instance by IoC only.
  }

  @Override
  public boolean isRightOnTopicsEnabled(String componentId) {
    return isTopicTrackerSupported(componentId) &&
        isComponentInstanceParameterEnabled(componentId, "rightsOnTopics");
  }

  @Override
  public boolean isCoWritingEnabled(String componentId) {
    return isTopicTrackerSupported(componentId) &&
        isComponentInstanceParameterEnabled(componentId, "coWriting");
  }

  @Override
  public boolean isFileSharingEnabled(String componentId) {
    return isTopicTrackerSupported(componentId) &&
        isComponentInstanceParameterEnabled(componentId, "useFileSharing");
  }

  @Override
  public boolean isPublicationSharingEnabled(String componentId) {
    return isTopicTrackerSupported(componentId) &&
        isComponentInstanceParameterEnabled(componentId, "usePublicationSharing");
  }

  private boolean isComponentInstanceParameterEnabled(String componentId,
      String componentParameterName) {
    return getBooleanValue(getOrganisationController().
        getComponentParameterValue(componentId, componentParameterName));
  }

  @Override
  public boolean isTopicTrackerSupported(String componentId) {
    return getOrganisationController().getComponentInst(componentId).isTopicTracker();
  }

  @Override
  public boolean isUserAuthorized(String userId, String componentId,
      final AccessControlContext context) {
    return isUserAuthorized(getUserRoles(userId, componentId, context));
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, String componentId) {

    // If userId corresponds to nothing or to a deleted or deactivated user, then no role is
    // retrieved.
    if (!UserDetail.isActivatedStateFor(userId)) {
      return;
    }


    // Personal space or user tool
    if (componentId == null || getOrganisationController().isToolAvailable(componentId)) {
      userRoles.add(SilverpeasRole.admin);
      return;
    }
    if (Administration.ADMIN_COMPONENT_ID.equals(componentId)) {
      if (getOrganisationController().getUserDetail(userId).isAccessAdmin()) {
        userRoles.add(SilverpeasRole.admin);
      }
      return;
    }

    ComponentInst componentInst = getOrganisationController().getComponentInst(componentId);
    if (componentInst == null) {
      return;
    }

    if (componentInst.isPublic() || getBooleanValue(
        getOrganisationController().getComponentParameterValue(componentId, "publicFiles"))) {
      userRoles.add(SilverpeasRole.user);
      if (!AccessControlOperation.isPersistActionFrom(context.getOperations()) &&
          !AccessControlOperation.isDownloadActionFrom(context.getOperations()) &&
          !AccessControlOperation.isSharingActionFrom(context.getOperations())) {
        // In that case, it is not necessary to check deeper the user rights
        return;
      }
    }

    if (getOrganisationController().isComponentAvailable(componentId, userId)) {
      Set<SilverpeasRole> roles =
          SilverpeasRole.from(getOrganisationController().getUserProfiles(userId, componentId));
      // If component is available, but user has no rights -> public component
      if (roles.isEmpty()) {
        userRoles.add(SilverpeasRole.user);
      } else {
        userRoles.addAll(roles);
      }
    }
  }

  /**
   * Gets the organization controller used for performing its task.
   * @return an organization controller instance.
   */
  private OrganizationController getOrganisationController() {
    return controller;
  }
}
