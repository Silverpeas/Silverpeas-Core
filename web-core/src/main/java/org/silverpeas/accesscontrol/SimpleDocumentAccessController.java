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
import com.silverpeas.accesscontrol.AccessControlOperation;
import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.accesscontrol.NodeAccessController;
import com.silverpeas.util.ComponentHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.apache.commons.collections.CollectionUtils;
import org.silverpeas.attachment.model.SimpleDocument;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Set;

/**
 * @author ehugonnet
 */
@Named("simpleDocumentAccessController")
public class SimpleDocumentAccessController extends AbstractAccessController<SimpleDocument> {

  @Inject
  private ComponentAccessController componentAccessController;

  @Inject
  private NodeAccessController nodeAccessController;

  public SimpleDocumentAccessController() {
  }

  /**
   * For test only.
   * @param accessController
   */
  SimpleDocumentAccessController(NodeAccessController accessController) {
    this.nodeAccessController = accessController;
  }

  @Override
  public boolean isUserAuthorized(String userId, SimpleDocument object,
      final AccessControlContext context) {

    // Component access control
    final Set<SilverpeasRole> componentUserRoles =
        getComponentAccessController().getUserRoles(context, userId, object.getInstanceId());
    if (!getComponentAccessController().isUserAuthorized(componentUserRoles)) {
      return false;
    }

    // Node access control
    if (ComponentHelper.getInstance().isThemeTracker(object.getInstanceId())) {
      String foreignId = object.getForeignId();
      if (StringUtil.isInteger(foreignId)) {
        final PublicationDetail pubDetail;
        try {
          pubDetail = getActualForeignPublication(foreignId, object.getInstanceId());
        } catch (Exception e) {
          SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
              "root.NO_EX_MESSAGE", e);
          return false;
        }

        // If rights are not handled on directories, directory rights are not checked !
        if (getComponentAccessController().isRightOnTopicsEnabled(object.getInstanceId())) {
          try {
            Collection<NodePK> nodes = getPublicationBm()
                .getAllFatherPK(new PublicationPK(pubDetail.getId(), object.getInstanceId()));
            if (!nodes.isEmpty()) {
              for (NodePK nodePk : nodes) {
                final Set<SilverpeasRole> nodeUserRoles =
                    getNodeAccessController().getUserRoles(context, userId, nodePk);
                if (getNodeAccessController().isUserAuthorized(nodeUserRoles)) {
                  return isUserAuthorizedByContext(false, userId, object, context, nodeUserRoles,
                      pubDetail.getCreatorId());
                }
              }
              return false;
            }
          } catch (Exception ex) {
            SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
                "root.NO_EX_MESSAGE", ex);
            return false;
          }
        }
        return isUserAuthorizedByContext(false, userId, object, context, componentUserRoles,
            pubDetail.getCreatorId());
      } else if (isFileAttachedToWysiwygDescriptionOfNode(foreignId)) {
        String nodeId = foreignId.substring("Node_".length());
        final Set<SilverpeasRole> nodeUserRoles =
            getNodeAccessController().getUserRoles(context, userId, new NodePK(nodeId, object.
                getInstanceId()));
        return getNodeAccessController().isUserAuthorized(nodeUserRoles) &&
            isUserAuthorizedByContext(true, userId, object, context, nodeUserRoles, "unknown");
      }
    }

    return isUserAuthorizedByContext(false, userId, object, context, componentUserRoles, userId);
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
    boolean authorized = true;
    boolean isRoleVerificationRequired = false;

    // Checking the versions
    if (object.isVersioned() && !object.isPublic()) {
      isRoleVerificationRequired = true;
    }

    // Verifying download is possible
    if (context.getOperations().contains(AccessControlOperation.download) &&
        !object.isDownloadAllowedForReaders()) {
      authorized = object.isDownloadAllowedForRoles(userRoles);
      isRoleVerificationRequired = authorized;
    }
    
    // Verifying sharing is possible
    if (context.getOperations().contains(AccessControlOperation.sharing)) {
      authorized = getComponentAccessController().isSharingEnabled(object.getInstanceId());
      isRoleVerificationRequired = authorized;
    }

    // Verifying persist actions are possible
    if (authorized && !CollectionUtils
        .intersection(AccessControlOperation.PERSIST_ACTIONS, context.getOperations()).isEmpty()) {
      isRoleVerificationRequired = true;
    }

    // Verifying roles if necessary
    if (isRoleVerificationRequired) {
      SilverpeasRole greaterUserRole = SilverpeasRole.getGreaterFrom(userRoles);
      if (isNodeAttachmentCase) {
        if (context.getOperations().contains(AccessControlOperation.download)) {
          authorized = greaterUserRole.isGreaterThan(SilverpeasRole.writer);
        } else {
          authorized = greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
        }
      } else {
        if (context.getOperations().contains(AccessControlOperation.sharing)) {
          return greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
        }
        if (SilverpeasRole.writer.equals(greaterUserRole)) {
          authorized = userId.equals(foreignUserAuthor) ||
              getComponentAccessController().isCoWritingEnabled(object.getInstanceId());
        } else {
          authorized = greaterUserRole.isGreaterThan(SilverpeasRole.writer);
        }
      }
    }
    return authorized;
  }

  private boolean isFileAttachedToWysiwygDescriptionOfNode(String foreignId) {
    return StringUtil.isDefined(foreignId) && foreignId.startsWith("Node_");
  }

  protected PublicationBm getPublicationBm() throws Exception {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
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
    if (nodeAccessController == null) {
      nodeAccessController = new NodeAccessController();
    }
    return nodeAccessController;
  }
}
