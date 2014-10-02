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
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.control.PublicationBm;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.util.ComponentHelper;
import org.silverpeas.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

/**
 * Check the access to a publication for a user.
 * @author neysseric
 */
@Singleton
@PublicationAccessControl
public class PublicationAccessController extends AbstractAccessController<PublicationPK> {

  @Inject
  private NodeAccessController accessController;
  
  @Inject
  private OrganisationController controller;

  @Inject
  private PublicationBm publicationService;

  @Inject
  private ComponentHelper componentHelper;

  protected PublicationAccessController() {
  }

  @Override
  public boolean isUserAuthorized(String userId, PublicationPK object,
      final AccessControlContext context) {
    boolean authorized = true;
    boolean isRoleVerificationRequired = true;
    
    boolean sharingOperation = context.getOperations().contains(AccessControlOperation.sharing);
    
    // Verifying sharing is possible
    if (sharingOperation) {
      authorized =
          StringUtil.getBooleanValue(getOrganisationController().getComponentParameterValue(
              object.getInstanceId(), "usePublicationSharing"));
      isRoleVerificationRequired = authorized;
    }
    
    if (isRoleVerificationRequired &&
        componentHelper.isThemeTracker(object.getInstanceId())) {
      String foreignId = object.getId();
      try {
        foreignId = getActualForeignId(foreignId, object.getInstanceId());
      } catch (Exception e) {
        SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
            "root.NO_EX_MESSAGE", e);
        return false;
      }
      try {
        Collection<NodePK> nodes = getPublicationBm().getAllFatherPK(new PublicationPK(foreignId,
            object.getInstanceId()));
        for (NodePK nodePk : nodes) {
          if (sharingOperation) {
            Set<SilverpeasRole> userRoles = getNodeAccessController().getUserRoles(context, userId, nodePk);
            SilverpeasRole greaterUserRole = SilverpeasRole.getGreaterFrom(userRoles);
            return greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
          } else {
            if (getNodeAccessController().isUserAuthorized(userId, nodePk)) {
              return true;
            }
          }
        }
      } catch (Exception ex) {
        SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
            "root.NO_EX_MESSAGE", ex);
        return false;
      }
      return false;
    }
    return authorized;
  }

  protected PublicationBm getPublicationBm() {
    return publicationService;
  }

  /**
   * Return the 'real' id of the publication to which this file is attached to. In case of a clone
   * publication we need the cloneId (that is the original publication).
   * @param foreignId
   * @param instanceId
   * @return
   * @throws Exception
   */
  private String getActualForeignId(String foreignId, String instanceId) throws Exception {
    PublicationDetail pubDetail = getPublicationBm().getDetail(new PublicationPK(foreignId,
        instanceId));
    if (!pubDetail.isValid() && pubDetail.haveGotClone()) {
      return pubDetail.getCloneId();
    }
    return foreignId;
  }

  /**
   * Gets a controller of access on the nodes of a publication.
   * @return a NodeAccessController instance.
   */
  protected NodeAccessController getNodeAccessController() {
    return accessController;
  }
  
  /**
   * Gets the organization controller used for performing its task.
   * @return an organization controller instance.
   */
  private OrganisationController getOrganisationController() {
    return controller;
  }
}