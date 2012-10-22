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
package org.silverpeas.node.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.silverpeas.profile.web.UserProfileEntity;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
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
  @XmlElement
  private UserProfileEntity creator;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement(defaultValue = "")
  private Date creationDate;

  /**
   * Creates a new node entity from the specified node.
   * @param node the node to entitify.
   * @return the entity representing the specified node.
   */
  public static NodeAttrEntity fromNodeDetail(final NodeDetail node, URI uri, String lang) {
    return new NodeAttrEntity(node, uri, lang);
  }

  public static NodeAttrEntity fromNodeDetail(final NodeDetail node, String uri, String lang) {
    return fromNodeDetail(node, getURI(uri), lang);
  }

  private NodeAttrEntity(final NodeDetail node, URI uri, String lang) {
    this.setComponentId(node.getNodePK().getInstanceId());
    this.setId(node.getNodePK().getId());
    this.setUri(uri);
    if (node.getNbObjects() != -1) {
      this.setNbItems(String.valueOf(node.getNbObjects()));
    }
    this.setStatus(node.getStatus());
    this.setRole(node.getUserRole());
    this.setCreatorId(node.getCreatorId());
    this.setDescription(node.getDescription(lang));
    UserDetail user = UserDetail.getById(node.getCreatorId());
    if (user != null) {
      setCreator(UserProfileEntity.fromUser(user));
    }
    try {
      this.setCreationDate(DateUtil.parse(node.getCreationDate()));
    } catch (ParseException e) {
    }
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

  public void setCreator(UserProfileEntity creator) {
    this.creator = creator;
  }

  public UserProfileEntity getCreator() {
    return creator;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }
}