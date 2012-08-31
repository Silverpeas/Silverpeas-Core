/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.stratelia.webactiv.util.node.model.NodeDetail;

@XmlRootElement
public class NodeAttrEntity {
  
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true)
  private String componentId;
  @XmlElement(defaultValue = "")
  private URI childrenURI;
  @XmlElement(defaultValue = "")
  private String rel;
  @XmlElement(defaultValue = "")
  private String nbItems;
  @XmlElement(defaultValue = "")
  private String status;
  @XmlElement(defaultValue = "")
  private String role;
  @XmlElement(defaultValue = "")
  private String creatorId;
  @XmlElement(defaultValue = "")
  private String description;
  
  /**
   * Creates a new node entity from the specified node.
   * @param node the node to entitify.
   * @return the entity representing the specified node.
   */
  public static NodeAttrEntity fromNodeDetail(final NodeDetail node, URI uri) {
    return new NodeAttrEntity(node, uri);
  }
  
  public static NodeAttrEntity fromNodeDetail(final NodeDetail node, String uri) {
    return fromNodeDetail(node, getURI(uri));
  }
  
  private NodeAttrEntity(final NodeDetail node, URI uri) {
    this.setComponentId(node.getNodePK().getInstanceId());
    this.setId(node.getNodePK().getId());
    this.setUri(uri);
    if (node.getNbObjects() != -1) {
      this.setNbItems(String.valueOf(node.getNbObjects()));
    }
    this.setStatus(node.getStatus());
    this.setRole(node.getUserRole());
    this.setCreatorId(node.getCreatorId());
    this.setDescription(node.getDescription());
  }
  
  private static URI getURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeAttrEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setChildrenURI(URI childrenURI) {
    this.childrenURI = childrenURI;
  }

  public URI getChildrenURI() {
    return childrenURI;
  }

  public void setRel(String rel) {
    this.rel = rel;
  }

  public String getRel() {
    return rel;
  }

  public void setNbItems(String nbItems) {
    this.nbItems = nbItems;
  }

  public String getNbItems() {
    return nbItems;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getRole() {
    return role;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}