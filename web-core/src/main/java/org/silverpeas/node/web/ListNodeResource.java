/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.node.web;

import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.EJBUtilitaire;
import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Authorized;

/**
 * A REST Web resource representing a list of node. It is a web service that provides an access to a
 * node referenced by its URL.
 */
@Service
@RequestScoped
@Path("nodes/{instanceId}")
@Authorized
public class ListNodeResource extends RESTWebService {

  @PathParam("instanceId")
  private String instanceId;

  @Override
  public String getComponentId() {
    return instanceId;
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return nodeBm;
  }

  /**
   * @return true if the current user has the admin role
   */
  private boolean isUserAdmin() {
    String[] profiles = getOrganisationController().getUserProfiles(getUserDetail().getId(), getComponentId());
    for (String profile : profiles) {
      if (SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Updates order of the list of Node from the JSON representation. If the user isn't authentified,
   * a 401 HTTP code is returned. If the user isn't authorized to save the node, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param tab of node to update order
   * @return the new list of node after update
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity[] updateListNode(final NodeEntity[] newListNode) {
    try {
      // Verif that the current user has the Admin role to execute this REST service
      if(isUserAdmin()) {
        //Update list Node
        List<NodePK> nodePKs = new ArrayList<NodePK>();
        for (NodeEntity nodeEntity : newListNode) {
          nodePKs.add(nodeEntity.toNodePK());
        }
        getNodeBm().sortNodes(nodePKs);
      }
    } catch (RemoteException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return newListNode;
  }
}
