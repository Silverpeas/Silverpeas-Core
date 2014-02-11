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
package com.silverpeas.accesscontrol;

import java.util.Collection;

import javax.inject.Inject;
import org.silverpeas.attachment.model.SimpleDocument;

import com.silverpeas.util.ComponentHelper;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentAccessController implements AccessController<SimpleDocument> {

  @Inject
  private NodeAccessController accessController;

  public SimpleDocumentAccessController() {
  }

  /**
   * For test only.
   *
   * @param accessController
   */
  SimpleDocumentAccessController(NodeAccessController accessController) {
    this.accessController = accessController;
  }

  @Override
  public boolean isUserAuthorized(String userId, SimpleDocument object) {
    ComponentHelper componentHelper = ComponentHelper.getInstance();
    String componentId = object.getInstanceId();
    if (componentHelper.isThemeTracker(componentId)) {
      String foreignId = object.getForeignId();
      if (StringUtil.isInteger(foreignId)) {
        try {
          foreignId = getActualForeignId(foreignId, object.getInstanceId());
        } catch (Exception e) {
          SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
              "root.NO_EX_MESSAGE", e);
          return false;
        }
        if (!componentHelper.isKmax(componentId)) {
          try {
            Collection<NodePK> nodes = getPublicationBm()
                .getAllFatherPK(new PublicationPK(foreignId, object.getInstanceId()));
            for (NodePK nodePk : nodes) {
              if (getNodeAccessController().isUserAuthorized(userId, nodePk)) {
                return true;
              }
            }
          } catch (Exception ex) {
            SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
                "root.NO_EX_MESSAGE", ex);
            return false;
          }
          return false;
        }
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
   * Return the 'real' id of the publication to which this file is attached to. In case of a clone
   * publication we need the cloneId (that is the original publication).
   *
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
   *
   * @return a NodeAccessController instance.
   */
  protected NodeAccessController getNodeAccessController() {
    if (accessController == null) {
      accessController = new NodeAccessController();
    }
    return accessController;
  }
}
