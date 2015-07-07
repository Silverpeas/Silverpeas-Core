/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.accesscontrol.NodeAccessController;
import com.silverpeas.accesscontrol.PublicationAccessController;
import com.silverpeas.util.ComponentHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.silverpeas.attachment.model.SimpleDocument;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

import static com.silverpeas.accesscontrol.AccessControlOperation.*;

/**
 * @author ehugonnet
 */
@Named("simpleDocumentAccessController")
public class SimpleDocumentAccessController extends AbstractAccessController<SimpleDocument> {

  @Inject
  private ComponentAccessController componentAccessController;

  @Inject
  private NodeAccessController nodeAccessController;

  @Inject
  private PublicationAccessController publicationAccessController;

  @Override
  public boolean isUserAuthorized(String userId, SimpleDocument object,
      final AccessControlContext context) {
    Set<SilverpeasRole> componentUserRoles = null;
    boolean componentAccessAuthorized = false;

    // Node access control
    if (ComponentHelper.getInstance().isThemeTracker(object.getInstanceId())) {
      String foreignId = object.getForeignId();
      Set<SilverpeasRole> publicationUserRoles = getPublicationAccessController()
          .getUserRoles(context, userId, new PublicationPK(foreignId, object.getInstanceId()));
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
              getNodeAccessController().getUserRoles(context, userId, new NodePK(nodeId, object.
                  getInstanceId()));
          return getNodeAccessController().isUserAuthorized(nodeUserRoles) &&
              isUserAuthorizedByContext(true, userId, object, context, nodeUserRoles, "unknown");
        }
      }
    }

    // Component access control
    if (!componentAccessAuthorized && componentUserRoles == null) {
      componentUserRoles =
          getComponentAccessController().getUserRoles(context, userId, object.getInstanceId());
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
    if (nodeAccessController == null) {
      nodeAccessController = new NodeAccessController();
    }
    return nodeAccessController;
  }

  /**
   * Gets a controller of access on publication.
   * @return a PublicationAccessController instance.
   */
  protected PublicationAccessController getPublicationAccessController() {
    if (publicationAccessController == null) {
      publicationAccessController = new PublicationAccessController();
    }
    return publicationAccessController;
  }
}
