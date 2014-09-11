/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.node.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;

/**
 * A REST Web resource providing access to a node.
 */
public abstract class AbstractNodeResource extends RESTWebService {

  @PathParam("componentId")
  protected String componentId;
  
  @Override
  public String getComponentId() {
    return componentId;
  }
  
  /**
   * Get the root of the application and its children. As this service works only in non
   * authenticated mode for the moment, children do not contain special nodes.
   *
   * @return the application root and its children
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getRoot() {
    NodeDetail node = getNodeDetail(NodePK.ROOT_NODE_ID);
    if (!isNodeReadable(node)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    URI uri = getUriInfo().getRequestUriBuilder().path(node.getNodePK().getId()).build();
    if (getUriInfo().getRequestUri().toString().endsWith("/" + NodePK.ROOT_NODE_ID)) {
      uri = getUriInfo().getRequestUri();
    }
    NodeEntity entity = NodeEntity.fromNodeDetail(node, uri);
    entity.setState("open");
    // in non authenticated mode, special nodes are unavailable
    entity.setChildren(removeSpecialNodes(entity.getChildren()));
    return entity;
  }
  
  /**
   * Get any node of the application and its children.
   *
   * @return NodeEntity representing asking node
   */
  @GET
  @Path("{path: [0-9]+(/[0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity getNode(@PathParam("path") String path) {
    String nodeId = getNodeIdFromURI(path);
    if (nodeId.equals(NodePK.ROOT_NODE_ID)) {
      return getRoot();
    } else {
      NodeDetail node = getNodeDetail(nodeId);
      if (!isNodeReadable(node)) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      URI uri = getUriInfo().getRequestUri();
      return NodeEntity.fromNodeDetail(node, uri);
    }
  }
  
  /**
   * Get all children of any node of the application.
   *
   * @return an array of NodeEntity representing children
   */
  @GET
  @Path("{path: [0-9]+(/[0-9]+)*/children}")
  @Produces(MediaType.APPLICATION_JSON)
  public NodeEntity[] getChildren(@PathParam("path") String path) {
    String[] nodeIds = path.split("/");
    String nodeId = nodeIds[nodeIds.length - 2];
    NodeDetail node = getNodeDetail(nodeId);
    if (!isNodeReadable(node)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    String requestUri = getUriInfo().getRequestUri().toString();
    String uri = requestUri.substring(0, requestUri.lastIndexOf("/"));
    NodeEntity entity = NodeEntity.fromNodeDetail(node, uri);
    if (nodeId.equals(NodePK.ROOT_NODE_ID)) {
      return removeSpecialNodes(entity.getChildren());
    }
    return entity.getChildren();
  }
  
  protected abstract boolean isNodeReadable(NodeDetail node);
  
  private String getNodeIdFromURI(String uri) {
    String[] nodeIds = uri.split("/");
    return nodeIds[nodeIds.length - 1];
  }

  private NodeDetail getNodeDetail(String id) {
    return getNodeBm().getDetail(getNodePK(id));
  }

  private NodeEntity[] removeSpecialNodes(NodeEntity[] nodes) {
    List<NodeEntity> result = new ArrayList<NodeEntity>();
    for (NodeEntity node : nodes) {
      if (!node.getAttr().getId().equals(NodePK.BIN_NODE_ID) && !node.getAttr().getId().equals(
          NodePK.UNCLASSED_NODE_ID)) {
        result.add(node);
      }
    }
    return result.toArray(new NodeEntity[0]);
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getComponentId());
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      nodeBm = EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return nodeBm;
  }
  
}
