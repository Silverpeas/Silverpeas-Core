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

import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.importExport.attachment.AttachmentDetail;

import org.silverpeas.util.ComponentHelper;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.control.PublicationBm;
import com.stratelia.webactiv.publication.model.PublicationPK;

/**
 * Check the access to an attachment for a user.
 * @author ehugonnet
 */
@Named
public class AttachmentAccessController extends AbstractAccessController<AttachmentDetail> {

  @Inject
  private NodeAccessController accessController;
  
  @Inject
  private PublicationAccessController publicationAccessController;

  @Inject
  private ComponentHelper componentHelper;

  public AttachmentAccessController() {
  }

  /**
   * For test only.
   * @param accessController
   */
  AttachmentAccessController(NodeAccessController accessController) {
    this.accessController = accessController;
  }

  @Override
  public boolean isUserAuthorized(String userId, AttachmentDetail object,
      final AccessControlContext context) {
    if (componentHelper.isThemeTracker(object.getForeignKey().getComponentName())) {
      String foreignId = object.getForeignKey().getId();
      if (StringUtil.isInteger(foreignId)) {
        PublicationPK pubPK =
            new PublicationPK(object.getForeignKey().getId(), object.getForeignKey()
                .getInstanceId());
        return publicationAccessController.isUserAuthorized(userId, pubPK, context);
      } else if (isFileAttachedToWysiwygDescriptionOfNode(foreignId)) {
        String nodeId = foreignId.substring("Node_".length());
        return getNodeAccessController().isUserAuthorized(userId, new NodePK(nodeId, object.
            getInstanceId()));
      }
    }
    return true;
  }

  private boolean isFileAttachedToWysiwygDescriptionOfNode(String foreignId) {
    return StringUtil.isDefined(foreignId) && foreignId.startsWith("Node_");
  }

  protected PublicationBm getPublicationBm() throws Exception {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
  }

  /**
   * Gets a controller of access on the nodes of a publication.
   * @return a NodeAccessController instance.
   */
  protected NodeAccessController getNodeAccessController() {
    if (accessController == null) {
      accessController = new NodeAccessController();
    }
    return accessController;
  }
}
