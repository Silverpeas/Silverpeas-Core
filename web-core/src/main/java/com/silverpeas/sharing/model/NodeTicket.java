/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.sharing.model;

import java.rmi.RemoteException;
import com.silverpeas.sharing.security.ShareableAccessControl;
import com.silverpeas.sharing.security.ShareableNode;
import com.silverpeas.sharing.security.ShareableResource;
import com.silverpeas.sharing.services.NodeAccessControl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

import javax.ejb.CreateException;

/**
 *
 * @author ehugonnet
 */
@Entity
@DiscriminatorValue("Node")
public class NodeTicket extends Ticket {

  private static final NodeAccessControl accessControl = new NodeAccessControl();

  public NodeTicket(int sharedObjectId, String componentId, String creatorId, Date creationDate,
      Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creatorId, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = NODE_TYPE;
  }

  public NodeTicket(int sharedObjectId, String componentId, UserDetail creator, Date creationDate,
      Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creator, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = NODE_TYPE;
  }

  protected NodeTicket() {
    this.sharedObjectType = NODE_TYPE;
  }

  @Override
  public ShareableAccessControl getAccessControl() {
    return accessControl;
  }

  @Override
  public ShareableResource<NodeDetail> getResource() {
    try {
      NodeBmHome home = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      NodeBm nodeBm = home.create();
      NodeDetail node = nodeBm.getDetail(new NodePK(String.valueOf(getSharedObjectId()),
          getComponentId()));
      if (node != null) {
        return new ShareableNode(getToken(), node);
      }
    } catch (CreateException e) {
      SilverTrace.error("fileSharing", "Ticket.getResource", "root.MSG_GEN_PARAM_VALUE", e);
    } catch (RemoteException e) {
      SilverTrace.error("fileSharing", "Ticket.getResource", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }
}
