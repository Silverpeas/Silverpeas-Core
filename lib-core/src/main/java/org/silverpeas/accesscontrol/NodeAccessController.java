/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.accesscontrol;

import com.silverpeas.accesscontrol.AbstractAccessController;
import com.silverpeas.accesscontrol.AccessControlContext;
import com.silverpeas.accesscontrol.AccessControlOperation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.node.control.NodeBm;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * Check the access to a node for a user.
 * @author ehugonnet
 */
@Singleton
@NodeAccessControl
public class NodeAccessController extends AbstractAccessController<NodePK> {

  @Inject
  private ComponentAccessController componentAccessController;

  @Inject
  private OrganisationController controller;

  @Inject
  private NodeBm nodeService;

  protected NodeAccessController() {
  }

  @Override
  public boolean isUserAuthorized(String userId, NodePK nodePK,
      final AccessControlContext context) {
    
    boolean authorized = true;
    boolean isRoleVerificationRequired = true;
    
    boolean sharingOperation = context.getOperations().contains(AccessControlOperation.sharing);
    
    if (sharingOperation) {
      authorized =
        StringUtil.getBooleanValue(getOrganisationController().getComponentParameterValue(
            nodePK.getInstanceId(), "useFolderSharing"));
      isRoleVerificationRequired = authorized;
    }
    
    if (isRoleVerificationRequired) {
      Set<SilverpeasRole> userRoles = getUserRoles(context, userId, nodePK);
      if (sharingOperation) {
        SilverpeasRole greaterUserRole = SilverpeasRole.getGreaterFrom(userRoles);
        return greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
      }
      return isUserAuthorized(userRoles);
    }
    
    return authorized;
  }

  public boolean isUserAuthorized(Set<SilverpeasRole> nodeUserRoles) {
    return CollectionUtil.isNotEmpty(nodeUserRoles);
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, NodePK nodePK) {

    // Component access control
    final Set<SilverpeasRole> componentUserRoles =
        getComponentAccessController().getUserRoles(context, userId, nodePK.getInstanceId());
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
      node = getNodeBm().getHeader(nodePK, false);
    } catch (Exception ex) {
      SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
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

  public NodeBm getNodeBm() {
    return nodeService;
  }

  /**
   * Gets the organization controller used for performing its task.
   * @return an organization controller instance.
   */
  private OrganisationController getOrganisationController() {
    return controller;
  }

  /**
   * Gets a controller of access on the components of a publication.
   * @return a ComponentAccessController instance.
   */
  protected ComponentAccessController getComponentAccessController() {
    return componentAccessController;
  }
}
