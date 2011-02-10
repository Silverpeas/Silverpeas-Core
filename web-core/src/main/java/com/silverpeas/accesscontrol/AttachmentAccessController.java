/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.util.Collection;

/**
 * Check the access to an attachment for a user.
 * @author ehugonnet
 */
public class AttachmentAccessController implements AccessController<AttachmentDetail> {

  private NodeAccessController accessController;

  public AttachmentAccessController() {
    accessController = new NodeAccessController();
  }

  /**
   * For test only.
   * @param accessController
   */
  AttachmentAccessController(NodeAccessController accessController) {
    this.accessController = accessController;
  }

  @Override
  public boolean isUserAuthorized(String userId, AttachmentDetail object) {
    if (ComponentHelper.getInstance().isThemeTracker(object.getForeignKey().getComponentName())) {
      Collection<NodePK> nodes;
      try {
        nodes =
            getPublicationBm().getAllFatherPK(new PublicationPK(
            object.getForeignKey().getId(), object.getInstanceId()));
      } catch (Exception ex) {
        SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
            "root.NO_EX_MESSAGE", ex);
        return false;
      }
      for (NodePK nodePk : nodes) {
        if (accessController.isUserAuthorized(userId, nodePk)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  protected PublicationBm getPublicationBm() throws Exception {
    PublicationBmHome pubBmHome = (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
        JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
    return pubBmHome.create();
  }
}
