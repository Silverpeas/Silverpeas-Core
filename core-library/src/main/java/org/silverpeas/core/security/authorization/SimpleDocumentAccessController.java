/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

import static org.silverpeas.core.security.authorization.AccessControlOperation.*;

/**
 * @author ehugonnet
 */
@Service
@Singleton
public class SimpleDocumentAccessController extends AbstractAccessController<SimpleDocument>
    implements SimpleDocumentAccessControl {

  private ComponentAccessControl componentAccessController;

  private NodeAccessControl nodeAccessController;

  private PublicationAccessControl publicationAccessController;

  @Inject
  SimpleDocumentAccessController(final ComponentAccessControl componentAccessController,
      final NodeAccessControl nodeAccessController,
      final PublicationAccessControl publicationAccessController) {
    // Instance by IoC only.
    this.componentAccessController = componentAccessController;
    this.nodeAccessController = nodeAccessController;
    this.publicationAccessController = publicationAccessController;
  }

  @Override
  public boolean isUserAuthorized(final String userId, final ResourceIdentifier id) {
    ContributionIdentifier docId = (ContributionIdentifier) id;
    SimpleDocumentPK docPK =
        new SimpleDocumentPK(docId.getLocalId(), docId.getComponentInstanceId());
    SimpleDocument doc = AttachmentService.get().searchDocumentById(docPK, null);
    return isUserAuthorized(userId, doc);
  }

  @Override
  public boolean isUserAuthorized(String userId, SimpleDocument object,
      final AccessControlContext context) {
    Set<SilverpeasRole> componentUserRoles = null;
    boolean componentAccessAuthorized = false;

    // Node access control
    final ComponentAccessController.DataManager componentDataManager =
        ComponentAccessController.getDataManager(context);
    if (componentDataManager.isTopicTrackerSupported(object.getInstanceId())) {
      final String foreignId = object.getForeignId();
      final Set<SilverpeasRole> parentUserRoles;
      if (isFileAttachedToWysiwygDescriptionOfNode(foreignId)) {
        String nodeId = foreignId.substring("Node_".length());
        final NodePK nodePK = new NodePK(nodeId, object.getInstanceId());
        parentUserRoles = getNodeAccessController().getUserRoles(userId, nodePK, context);
        return getNodeAccessController().isUserAuthorized(parentUserRoles) &&
            isUserAuthorizedByContext(true, userId, object, context, parentUserRoles, "unknown");
      } else {
        final PublicationPK pubPk = new PublicationPK(foreignId, object.getInstanceId());
        parentUserRoles = getPublicationAccessController().getUserRoles(userId, pubPk, context);
        final PublicationDetail publicationDetail = PublicationAccessController.getDataManager(
            context).getCurrentPublication();
        if (publicationDetail != null) {
          // PublicationDetail has been loaded by PublicationAccessController processing,
          // parentUserRoles are the one of the publication
          return isUserAuthorizedByContext(false, userId, object, context, parentUserRoles,
              publicationDetail.getCreatorId());
        }
      }

      // PublicationDetail has been loaded but not found,
      // parentUserRoles are the one of the component instance
      componentUserRoles = parentUserRoles;
      componentAccessAuthorized = getComponentAccessController().isUserAuthorized(componentUserRoles);
    }

    // Component access control
    if (!componentAccessAuthorized && componentUserRoles == null) {
      componentUserRoles = getComponentAccessController().getUserRoles(userId, object.getInstanceId(), context);
      componentAccessAuthorized = getComponentAccessController().isUserAuthorized(componentUserRoles);
    }
    return componentAccessAuthorized && isUserAuthorizedByContext(false, userId, object, context, componentUserRoles, userId);
  }

  /**
   * @param isNodeAttachmentCase
   * @param userId
   * @param object
   * @param context
   * @param userRoles
   * @param foreignUserAuthor corresponds to the user id that is the contribution author
   * @return
   */
  private boolean isUserAuthorizedByContext(final boolean isNodeAttachmentCase, String userId,
      SimpleDocument object, final AccessControlContext context, Set<SilverpeasRole> userRoles,
      String foreignUserAuthor) {
    boolean authorized = !userRoles.isEmpty();
    boolean isRoleVerificationRequired = false;

    SilverpeasRole highestUserRole = SilverpeasRole.getHighestFrom(userRoles);
    if (highestUserRole == null) {
      highestUserRole = SilverpeasRole.READER;
    }

    boolean downloadOperation = isDownloadActionFrom(context.getOperations());
    boolean sharingOperation = isSharingActionFrom(context.getOperations());

    // Checking the versions
    if (object.isVersioned() && !object.isPublic()) {
      isRoleVerificationRequired = true;
    }

    // Verifying download is possible
    if (authorized && downloadOperation && !object.isDownloadAllowedForReaders()) {
      authorized = object.isDownloadAllowedForRoles(userRoles);
      isRoleVerificationRequired = authorized;
    }

    // Verifying sharing is possible
    if (authorized && sharingOperation) {
      final ComponentAccessController.DataManager componentDataManager = ComponentAccessController.getDataManager(context);
      final User user = User.getById(userId);
      authorized = !user.isAnonymous() && componentDataManager.isFileSharingEnabledForRole(object.getInstanceId(), highestUserRole);
      isRoleVerificationRequired = false;
    }

    // Verifying persist actions are possible
    if (authorized && isPersistActionFrom(context.getOperations())) {
      isRoleVerificationRequired = true;
    }

    // Verifying roles if necessary
    if (isRoleVerificationRequired) {
      authorized = verifyAuthorizationAgainstRole(highestUserRole, isNodeAttachmentCase, userId,
          object, foreignUserAuthor, downloadOperation, context);
    }
    return authorized;
  }

  private boolean verifyAuthorizationAgainstRole(final SilverpeasRole highestUserRole,
      final boolean isNodeAttachmentCase, final String userId, final SimpleDocument object,
      final String foreignUserAuthor, final boolean downloadOperation,
      final AccessControlContext context) {
    final boolean authorized;
    if (isNodeAttachmentCase) {
      if (downloadOperation) {
        authorized = highestUserRole.isGreaterThan(SilverpeasRole.WRITER);
      } else {
        authorized = highestUserRole.isGreaterThanOrEquals(SilverpeasRole.ADMIN);
      }
    } else {
      if (SilverpeasRole.WRITER.equals(highestUserRole)) {
        final ComponentAccessController.DataManager componentDataManager = ComponentAccessController.getDataManager(context);
        authorized = userId.equals(foreignUserAuthor) || componentDataManager.isCoWritingEnabled(object.getInstanceId());
      } else {
        authorized = highestUserRole.isGreaterThan(SilverpeasRole.WRITER);
      }
    }
    return authorized;
  }

  private boolean isFileAttachedToWysiwygDescriptionOfNode(String foreignId) {
    return StringUtil.isDefined(foreignId) && foreignId.startsWith("Node_");
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

  /**
   * Gets a controller of access on publication.
   * @return a PublicationAccessController instance.
   */
  private PublicationAccessControl getPublicationAccessController() {
    return publicationAccessController;
  }
}
