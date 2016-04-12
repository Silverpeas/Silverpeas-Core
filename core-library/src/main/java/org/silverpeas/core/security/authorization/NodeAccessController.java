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

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * Check the access to a node for a user.
 * @author ehugonnet
 */
@Singleton
public class NodeAccessController extends AbstractAccessController<NodePK>
    implements NodeAccessControl {

  @Inject
  private ComponentAccessControl componentAccessController;

  @Inject
  private OrganizationController controller;

  @Inject
  private NodeService nodeService;

  NodeAccessController() {
    // Instance by IoC only.
  }

  @Override
  public boolean isUserAuthorized(String userId, NodePK nodePK,
      final AccessControlContext context) {

    boolean authorized = true;
    boolean isRoleVerificationRequired = true;

    boolean sharingOperation = context.getOperations().contains(AccessControlOperation.sharing);

    if (sharingOperation) {
      authorized = StringUtil.getBooleanValue(getOrganisationController()
          .getComponentParameterValue(nodePK.getInstanceId(), "useFolderSharing"));
      isRoleVerificationRequired = authorized;
    }

    if (isRoleVerificationRequired) {
      Set<SilverpeasRole> userRoles = getUserRoles(userId, nodePK, context);
      if (sharingOperation) {
        SilverpeasRole greaterUserRole = SilverpeasRole.getGreaterFrom(userRoles);
        return greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
      }
      return isUserAuthorized(userRoles);
    }

    return authorized;
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, NodePK nodePK) {

    // Component access control
    final Set<SilverpeasRole> componentUserRoles =
        getComponentAccessController().getUserRoles(userId, nodePK.getInstanceId(), context);
    if (!getComponentAccessController().isUserAuthorized(componentUserRoles)) {
      return;
    }

    // If rights are not handled from the node, then filling the user role containers with these
    // of component
    if (!getComponentAccessController().isRightOnTopicsEnabled(nodePK.getInstanceId())) {
      userRoles.addAll(componentUserRoles);
      return;
    }

    NodeDetail node;
    try {
      node = getNodeService().getHeader(nodePK, false);
    } catch (Exception ex) {
      SilverTrace.error("authorization", getClass().getSimpleName() + ".isUserAuthorized()",
          "root.NO_EX_MESSAGE", ex);
      return;
    }
    if (node != null) {
      if (!node.haveRights()) {
        userRoles.addAll(componentUserRoles);
        return;
      }
      userRoles.addAll(SilverpeasRole.from(getOrganisationController()
          .getUserProfiles(userId, nodePK.getInstanceId(), node.getRightsDependsOn(),
              ObjectType.NODE)));
    }
  }

  private NodeService getNodeService() {
    return nodeService;
  }

  /**
   * Gets the organization controller used for performing its task.
   * @return an organization controller instance.
   */
  private OrganizationController getOrganisationController() {
    return controller;
  }

  /**
   * Gets a controller of access on the components of a publication.
   * @return a ComponentAccessController instance.
   */
  private ComponentAccessControl getComponentAccessController() {
    return componentAccessController;
  }
}
