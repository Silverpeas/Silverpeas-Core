/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Set;

import static com.silverpeas.accesscontrol.AccessControlOperation.isPersistActionFrom;
import static com.silverpeas.accesscontrol.AccessControlOperation.isSharingActionFrom;

/**
 * Check the access to a publication for a user.
 * @author neysseric
 */
@Singleton
public class PublicationAccessController extends AbstractAccessController<PublicationPK>
    implements PublicationAccessControl {

  public static final String PUBLICATION_DETAIL_KEY = "PUBLICATION_DETAIL_KEY";

  @Inject
  private NodeAccessController accessController;

  @Inject
  private ComponentAccessController componentAccessController;

  @Override
  public boolean isUserAuthorized(String userId, PublicationPK pubPk,
      final AccessControlContext context) {
    return isUserAuthorizedByContext(userId, pubPk, context, getUserRoles(context, userId, pubPk));
  }

  /**
   * @param userId
   * @param pubPk
   * @param context
   * @param userRoles
   * @return
   */
  private boolean isUserAuthorizedByContext(String userId, PublicationPK pubPk,
      final AccessControlContext context, Set<SilverpeasRole> userRoles) {
    boolean authorized = !userRoles.isEmpty();
    boolean isRoleVerificationRequired = false;

    boolean sharingOperation = isSharingActionFrom(context.getOperations());

    // Verifying sharing is possible
    if (authorized && sharingOperation) {
      authorized =
          getComponentAccessController().isPublicationSharingEnabled(pubPk.getInstanceId());
      isRoleVerificationRequired = authorized;
    }

    // Verifying persist actions are possible
    if (authorized && isPersistActionFrom(context.getOperations())) {
      isRoleVerificationRequired = true;
    }

    // Verifying roles if necessary
    if (isRoleVerificationRequired) {
      SilverpeasRole greatestUserRole = SilverpeasRole.getGreatestFrom(userRoles);
      if (greatestUserRole == null) {
        greatestUserRole = SilverpeasRole.reader;
      }

      if (sharingOperation) {
        return greatestUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
      }

      if (SilverpeasRole.writer.equals(greatestUserRole)) {
        PublicationDetail publicationDetail =
            context.get(PUBLICATION_DETAIL_KEY, PublicationDetail.class);
        authorized =
            publicationDetail != null && (userId.equals(publicationDetail.getCreatorId())) ||
                getComponentAccessController().isCoWritingEnabled(pubPk.getInstanceId());
      } else {
        authorized = greatestUserRole.isGreaterThan(SilverpeasRole.writer);
      }
    }
    return authorized;
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, PublicationPK publicationPK) {

    // Saving the result of component access control in order to verify the alias right accesses.
    boolean componentAccessAuthorized = true;

    // Component access control
    final Set<SilverpeasRole> componentUserRoles =
        getComponentAccessController().getUserRoles(context, userId, publicationPK.getInstanceId());
    if (!getComponentAccessController().isUserAuthorized(componentUserRoles)) {
      componentAccessAuthorized = false;
    }

    if (ComponentHelper.getInstance().isThemeTracker(publicationPK.getInstanceId())) {
      if (StringUtil.isInteger(publicationPK.getId())) {
        final PublicationDetail pubDetail;
        try {
          pubDetail =
              getActualForeignPublication(publicationPK.getId(), publicationPK.getInstanceId());
          context.put(PUBLICATION_DETAIL_KEY, pubDetail);
        } catch (Exception e) {
          SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
              "root.NO_EX_MESSAGE", e);
          return;
        }

        // Check if an alias of publication is authorized
        // (special treatment in case of the user has no access right on component instance)
        if (!componentAccessAuthorized) {
          try {
            Collection<Alias> aliases = getPublicationBm().getAlias(pubDetail.getPK());
            for (Alias alias : aliases) {

              final Set<SilverpeasRole> nodeUserRoles = getNodeAccessController()
                  .getUserRoles(context, userId, new NodePK(alias.getId(), alias.getInstanceId()));
              if (getNodeAccessController().isUserAuthorized(nodeUserRoles)) {
                userRoles.addAll(nodeUserRoles);
                return;
              }
            }
            return;
          } catch (Exception e) {
            SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
                "root.NO_EX_MESSAGE", e);
            return;
          }
        }

        // If rights are not handled on directories, directory rights are not checked !
        else if (getComponentAccessController()
            .isRightOnTopicsEnabled(publicationPK.getInstanceId())) {
          try {
            Collection<NodePK> nodes = getPublicationBm().getAllFatherPK(
                new PublicationPK(pubDetail.getId(), publicationPK.getInstanceId()));
            if (!nodes.isEmpty()) {
              for (NodePK nodePk : nodes) {
                final Set<SilverpeasRole> nodeUserRoles =
                    getNodeAccessController().getUserRoles(context, userId, nodePk);
                if (getNodeAccessController().isUserAuthorized(nodeUserRoles)) {
                  userRoles.addAll(nodeUserRoles);
                  return;
                }
              }
              return;
            }
          } catch (Exception ex) {
            SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
                "root.NO_EX_MESSAGE", ex);
            return;
          }
        }
      }
    }
    if (componentAccessAuthorized) {
      userRoles.addAll(componentUserRoles);
    }
  }

  protected PublicationService getPublicationBm() {
    return publicationService;
  }

  /**
   * Return the 'real' publication to which this file is attached to. In case of a clone
   * publication we need the cloned one (that is the original publication).
   * @param foreignId
   * @param instanceId
   * @return
   * @throws Exception
   */
  private PublicationDetail getActualForeignPublication(String foreignId, String instanceId)
      throws Exception {
    PublicationDetail pubDetail =
        getPublicationBm().getDetail(new PublicationPK(foreignId, instanceId));
    if (!pubDetail.isValid() && pubDetail.haveGotClone()) {
      pubDetail =
          getPublicationBm().getDetail(new PublicationPK(pubDetail.getCloneId(), instanceId));
    }
    return pubDetail;
  }

  /**
   * Gets a controller of access on the components of a publication.
   * @return a ComponentAccessController instance.
   */
  protected ComponentAccessController getComponentAccessController() {
    if (componentAccessController == null) {
      componentAccessController = new ComponentAccessController();
    }
    return componentAccessController;
  }

  /**
   * Gets a controller of access on the nodes of a publication.
   * @return a NodeAccessController instance.
   */
  protected NodeAccessController getNodeAccessController() {
    return accessController;
  }
}