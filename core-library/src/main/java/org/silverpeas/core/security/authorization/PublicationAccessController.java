/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

import static org.silverpeas.core.security.authorization.AccessControlOperation.isPersistActionFrom;
import static org.silverpeas.core.security.authorization.AccessControlOperation.isSharingActionFrom;

/**
 * Check the access to a publication for a user.
 * @author neysseric
 */
@Singleton
public class PublicationAccessController extends AbstractAccessController<PublicationPK>
    implements PublicationAccessControl {

  public static final String PUBLICATION_DETAIL_KEY = "PUBLICATION_DETAIL_KEY";

  @Inject
  private NodeAccessControl nodeAccessController;

  @Inject
  private ComponentAccessControl componentAccessController;

  @Inject
  private PublicationService publicationService;

  PublicationAccessController() {
    // Instance by IoC only.
  }

  @Override
  public boolean isUserAuthorized(String userId, PublicationPK pubPk,
      final AccessControlContext context) {
    return isUserAuthorizedByContext(userId, pubPk, context, getUserRoles(userId, pubPk, context));
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
        getComponentAccessController().getUserRoles(userId, publicationPK.getInstanceId(), context);
    if (!getComponentAccessController().isUserAuthorized(componentUserRoles)) {
      componentAccessAuthorized = false;
    }

    if (componentAccessController.isTopicTrackerSupported(publicationPK.getInstanceId())) {
      if (StringUtil.isInteger(publicationPK.getId())) {
        final PublicationDetail pubDetail;
        try {
          pubDetail =
              getActualForeignPublication(publicationPK.getId(), publicationPK.getInstanceId());
          context.put(PUBLICATION_DETAIL_KEY, pubDetail);
        } catch (Exception e) {
          SilverTrace.error("authorization", getClass().getSimpleName() + ".isUserAuthorized()",
              "root.NO_EX_MESSAGE", e);
          return;
        }

        // Check if an alias of publication is authorized
        // (special treatment in case of the user has no access right on component instance)
        if (!componentAccessAuthorized) {
          try {
            Collection<Alias> aliases = getPublicationService().getAlias(pubDetail.getPK());
            for (Alias alias : aliases) {

              final Set<SilverpeasRole> nodeUserRoles = getNodeAccessController()
                  .getUserRoles(userId, new NodePK(alias.getId(), alias.getInstanceId()), context);
              if (getNodeAccessController().isUserAuthorized(nodeUserRoles)) {
                userRoles.addAll(nodeUserRoles);
                return;
              }
            }
            return;
          } catch (Exception e) {
            SilverTrace.error("authorization", getClass().getSimpleName() + ".isUserAuthorized()",
                "root.NO_EX_MESSAGE", e);
            return;
          }
        }

        // If rights are not handled on directories, directory rights are not checked !
        else if (getComponentAccessController()
            .isRightOnTopicsEnabled(publicationPK.getInstanceId())) {
          try {
            Collection<NodePK> nodes = getPublicationService().getAllFatherPK(
                new PublicationPK(pubDetail.getId(), publicationPK.getInstanceId()));
            if (!nodes.isEmpty()) {
              for (NodePK nodePk : nodes) {
                final Set<SilverpeasRole> nodeUserRoles =
                    getNodeAccessController().getUserRoles(userId, nodePk, context);
                if (getNodeAccessController().isUserAuthorized(nodeUserRoles)) {
                  userRoles.addAll(nodeUserRoles);
                  return;
                }
              }
              return;
            }
          } catch (Exception ex) {
            SilverTrace.error("authorization", getClass().getSimpleName() + ".isUserAuthorized()",
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

  protected PublicationService getPublicationService() {
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
        getPublicationService().getDetail(new PublicationPK(foreignId, instanceId));
    if (!pubDetail.isValid() && pubDetail.haveGotClone()) {
      pubDetail =
          getPublicationService().getDetail(new PublicationPK(pubDetail.getCloneId(), instanceId));
    }
    return pubDetail;
  }

  /**
   * Gets a controller of access on the components of a publication.
   * @return a ComponentAccessController instance.
   */
  private ComponentAccessControl getComponentAccessController() {
    return componentAccessController;
  }

  /**
   * Gets a controller of access on the nodes of a publication.
   * @return a NodeAccessController instance.
   */
  private NodeAccessControl getNodeAccessController() {
    return nodeAccessController;
  }
}