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
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
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
@Singleton
public class SimpleDocumentAccessController extends AbstractAccessController<SimpleDocument>
    implements SimpleDocumentAccessControl {

  @Inject
  private ComponentAccessControl componentAccessController;

  @Inject
  private NodeAccessControl nodeAccessController;

  @Inject
  private PublicationAccessControl publicationAccessController;

  SimpleDocumentAccessController () {
    // Instance by IoC only.
  }

  @Override
  public boolean isUserAuthorized(String userId, SimpleDocument object,
      final AccessControlContext context) {
    Set<SilverpeasRole> componentUserRoles = null;
    boolean componentAccessAuthorized = false;

    // Node access control
    if (componentAccessController.isTopicTrackerSupported(object.getInstanceId())) {
      String foreignId = object.getForeignId();
      Set<SilverpeasRole> publicationUserRoles = getPublicationAccessController()
          .getUserRoles(userId, new PublicationPK(foreignId, object.getInstanceId()), context);
      PublicationDetail publicationDetail =
          context.get(PublicationAccessController.PUBLICATION_DETAIL_KEY, PublicationDetail.class);
      if (publicationDetail != null) {
        // As publicationDetail exists, publicationUserRoles are the one of the publication
        return isUserAuthorizedByContext(false, userId, object, context, publicationUserRoles,
            publicationDetail.getCreatorId());

      } else {
        // As publicationDetail does not exist, publicationUserRoles are the one of the component
        // instance
        componentUserRoles = publicationUserRoles;
        componentAccessAuthorized =
            getComponentAccessController().isUserAuthorized(componentUserRoles);
        if (componentAccessAuthorized && isFileAttachedToWysiwygDescriptionOfNode(foreignId)) {
          String nodeId = foreignId.substring("Node_".length());
          final Set<SilverpeasRole> nodeUserRoles =
              getNodeAccessController().getUserRoles(userId, new NodePK(nodeId, object.
                  getInstanceId()), context);
          return getNodeAccessController().isUserAuthorized(nodeUserRoles) &&
              isUserAuthorizedByContext(true, userId, object, context, nodeUserRoles, "unknown");
        }
      }
    }

    // Component access control
    if (!componentAccessAuthorized && componentUserRoles == null) {
      componentUserRoles =
          getComponentAccessController().getUserRoles(userId, object.getInstanceId(), context);
      componentAccessAuthorized =
          getComponentAccessController().isUserAuthorized(componentUserRoles);
    }
    return componentAccessAuthorized &&
        isUserAuthorizedByContext(false, userId, object, context, componentUserRoles, userId);
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
      authorized = getComponentAccessController().isFileSharingEnabled(object.getInstanceId());
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

      if (isNodeAttachmentCase) {
        if (downloadOperation) {
          authorized = greatestUserRole.isGreaterThan(SilverpeasRole.writer);
        } else {
          authorized = greatestUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
        }
      } else {
        if (sharingOperation) {
          return greatestUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
        }
        if (SilverpeasRole.writer.equals(greatestUserRole)) {
          authorized = userId.equals(foreignUserAuthor) ||
              getComponentAccessController().isCoWritingEnabled(object.getInstanceId());
        } else {
          authorized = greatestUserRole.isGreaterThan(SilverpeasRole.writer);
        }
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
