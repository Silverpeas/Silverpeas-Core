/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.node;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * A REST Web resource representing a list of node. It is a web service that provides an access to a
 * node referenced by its URL.
 */
@WebService
@Path(ListNodeResource.PATH + "/{instanceId}")
@Authorized
public class ListNodeResource extends RESTWebService {

  static final String PATH = "nodes";

  @Inject
  private NodeService nodeService;

  @PathParam("instanceId")
  private String instanceId;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return instanceId;
  }

  private NodeService getNodeService() {
    return nodeService;
  }

  /**
   * @return true if the current user has the admin role
   */
  private boolean isUserAdmin() {
    String[] profiles = getOrganisationController().getUserProfiles(getUser().getId(),
        getComponentId());
    for (String profile : profiles) {
      if (SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Updates order of the list of Node from the JSON representation. If the user isn't authenticated,
   * a 401 HTTP code is returned. If the user isn't authorized to save the node, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @return the new list of node after update
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity[] updateListNode(final NodeEntity[] newListNode) {
    // Verif that the current user has the Admin role to execute this REST service
    if (isUserAdmin()) {
      //Update list Node
      List<NodePK> nodePKs = new ArrayList<NodePK>();
      for (NodeEntity nodeEntity : newListNode) {
        nodePKs.add(nodeEntity.toNodePK());
      }
      getNodeService().sortNodes(nodePKs);
    }
    return newListNode;
  }
}
