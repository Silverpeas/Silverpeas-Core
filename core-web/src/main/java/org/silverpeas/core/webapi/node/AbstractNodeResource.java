/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.node;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

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
  protected NodeEntity getRoot() {
    NodeDetail node = getNodeDetail(NodePK.ROOT_NODE_ID);
    if (!isNodeReadable(node)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    URI uri = super.getUriInfo().getRequestUriBuilder().path(node.getNodePK().getId()).build();
    if (super.getUriInfo().getRequestUri().toString().endsWith("/" + NodePK.ROOT_NODE_ID)) {
      uri = super.getUriInfo().getRequestUri();
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
  protected NodeEntity getNode(String path) {
    String nodeId = getNodeIdFromURI(path);
    if (nodeId.equals(NodePK.ROOT_NODE_ID)) {
      return getRoot();
    } else {
      NodeDetail node = getNodeDetail(nodeId);
      if (!isNodeReadable(node)) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      URI uri = super.getUriInfo().getRequestUri();
      return NodeEntity.fromNodeDetail(node, uri);
    }
  }

  /**
   * Get all children of any node of the application.
   *
   * @return an array of NodeEntity representing children
   */
  protected NodeEntity[] getChildren(String path) {
    String[] nodeIds = path.split("/");
    String nodeId = nodeIds[nodeIds.length - 2];
    NodeDetail node = getNodeDetail(nodeId);
    if (!isNodeReadable(node)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    String requestUri = super.getUriInfo().getRequestUri().toString();
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
    return getNodeService().getDetail(getNodePK(id));
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

  private NodeService getNodeService() {
    return NodeService.get();
  }

}
