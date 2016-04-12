/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.model;

import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.i18n.AbstractI18NBean;

import java.io.Serializable;
import java.util.Collection;

/**
 * This object contains the description of a node (own attributes and children attributes)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class NodeDetail extends AbstractI18NBean<NodeI18NDetail> implements Serializable {

  private static final long serialVersionUID = -1401884517616404337L;
  public final static String DEFAULT_TYPE = "default";
  public final static String FILE_LINK_TYPE = "file_link";
  public final static String STATUS_VISIBLE = "Visible";
  public final static String STATUS_INVISIBLE = "Invisible";
  private NodePK nodePK;
  private String creationDate;
  private String creatorId;
  private String path;
  private String fullPath;
  private int level;
  private String modelId = null;
  private String status = null;
  private NodePK fatherPK;
  // a NodeDetail collection
  private Collection<NodeDetail> childrenDetails;
  private String type = DEFAULT_TYPE;
  private int order = 0;
  private int rightsDependsOn = -1;
  private int nbObjects = -1; // No persistence - usefull to store nb objects
  // contained by this node
  private String userRole = null; // No persistence - usefull to store user role
  private boolean useId = false;


  public NodeDetail(NodeDetail detail) {
    new NodeDetail(detail.nodePK, detail.getName(), detail.getDescription(), detail.creationDate,
        detail.creatorId, detail.path, detail.level, detail.fatherPK, detail.modelId, detail.status,
        null, detail.type);
    setOrder(detail.order);
    setLanguage(detail.getLanguage());
    setRightsDependsOn(detail.rightsDependsOn);

  }

  /**
   * Construct an empty NodeDetail
   * @since 1.0
   */
  public NodeDetail() {
    init(NodePK.ROOT_NODE_ID, "", "", "", "", "", "0", NodePK.ROOT_NODE_ID);
  }

  /**
   * Create a new NodeDetail
   * @since 1.0
   */
  private void init(String id, String name, String description,
      String creationDate, String creatorId, String path, String level,
      String fatherId) {
    this.nodePK = new NodePK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + id + "/";
    this.level = new Integer(level);
    this.fatherPK = new NodePK(fatherId);
    this.childrenDetails = null;
  }

  private void init(String id, String name, String description,
      String creationDate, String creatorId, String path, String level,
      String fatherId, String type) {
    this.nodePK = new NodePK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + id + "/";
    this.level = new Integer(level);
    this.fatherPK = new NodePK(fatherId);
    this.childrenDetails = null;
    this.type = type;
  }

  /**
   * Create a new NodeDetail
   * @param nodePK NodePK of the node
   * @param name The node name
   * @param description The node description
   * @param creationDate A string which represent the creation date
   * @param creatorId The name of the node creator
   * @param path The node path
   * @param level The node level (root level = 1)
   * @param fatherPK The nodePK of the father
   * @param childrenDetails A NodeDetail collection which contains each child
   * @see java.util.Collection
   * @see NodePK
   * @see NodeDetail
   * @since 1.0
   */
  public NodeDetail(NodePK nodePK, String name, String description, String creationDate,
      String creatorId, String path, int level, NodePK fatherPK,
      Collection<NodeDetail> childrenDetails) {
    this.nodePK = nodePK;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + nodePK.getId() + "/";
    this.level = level;
    this.fatherPK = fatherPK;
    this.childrenDetails = childrenDetails;
  }

  /**
   * Create a new NodeDetail
   * @param id id of the node
   * @param name The node name
   * @param description The node description
   * @param creationDate A string which represent the creation date
   * @param creatorId The name of the node creator
   * @param path The node path
   * @param level The node level (root level = 1)
   * @param fatherId The id of the father
   * @see java.util.Collection
   * @see NodeDetail
   * @since 1.0
   */
  public NodeDetail(String id, String name, String description, String creationDate,
      String creatorId, String path, String level, String fatherId) {
    init(id, name, description, creationDate, creatorId, path, level, fatherId);
  }

  public NodeDetail(String id, String name, String description,
      String creationDate, String creatorId, String path, String level,
      String fatherId, String type) {
    init(id, name, description, creationDate, creatorId, path, level, fatherId, type);
  }

  public NodeDetail(NodePK nodePK, String name, String description,
      String creationDate, String creatorId, String path, int level,
      NodePK fatherPK, String modelId, String status,
      Collection<NodeDetail> childrenDetails, String type) {
    this.nodePK = nodePK;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + nodePK.getId() + "/";
    this.level = level;
    this.fatherPK = fatherPK;
    this.modelId = modelId;
    this.status = status;
    this.childrenDetails = childrenDetails;
    this.type = type;
  }

  public NodeDetail(String id, String name, String description,
      String creationDate, String creatorId, String path, int level,
      String fatherId, String modelId, String status,
      Collection<NodeDetail> childrenDetails, String type) {
    this.nodePK = new NodePK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + id + "/";
    this.level = level;
    this.fatherPK = new NodePK(fatherId);
    this.modelId = modelId;
    this.status = status;
    this.childrenDetails = null;
    this.type = type;
  }

  public NodeDetail(NodePK nodePK, String name, String description,
      String creationDate, String creatorId, String path, int level,
      NodePK fatherPK, String modelId, String status, Collection<NodeDetail> childrenDetails) {
    this.nodePK = nodePK;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + nodePK.getId() + "/";
    this.level = level;
    this.fatherPK = fatherPK;
    this.modelId = modelId;
    this.status = status;
    this.childrenDetails = childrenDetails;
  }

  public NodeDetail(String id, String name, String description,
      String creationDate, String creatorId, String path, int level,
      String fatherId, String modelId, String status, Collection<NodeDetail> childrenDetails) {
    this.nodePK = new NodePK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.fullPath = path + id + "/";
    this.level = level;
    this.fatherPK = new NodePK(fatherId);
    this.modelId = modelId;
    this.status = status;
    this.childrenDetails = null;
  }

  /**
   * Get the NodePK
   * @return The NodePK
   * @since 1.0
   */
  public NodePK getNodePK() {
    return nodePK;
  }

  public void setNodePK(NodePK nodePK) {
    this.nodePK = nodePK;
  }

  /**
   * Méthode nécéssaire au marshalling castor
   * @return
   */
  public int getId() {
    return Integer.parseInt(getNodePK().getId());
  }

  /**
   * Get the creation date
   * @return the creation date
   * @since 1.0
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * Get the creator id
   * @return the creator identifier
   * @since 1.0
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * Get the path
   * @return the path
   * @since 1.0
   */
  public String getPath() {
    return path;
  }

  /**
   * Get the level
   * @return the level
   * @since 1.0
   */
  public int getLevel() {
    return level;
  }

  /**
   * Get the modelId
   * @return the modelId
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * Get the status
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Get the NodePK of the father
   * @return the NodePK of the father
   * @since 1.0
   */
  public NodePK getFatherPK() {
    return fatherPK;
  }

  /**
   * Get the details of each child
   * @return A collection of NodeDetail
   * @see NodeDetail
   * @since 1.0
   */
  public Collection<NodeDetail> getChildrenDetails() {
    return childrenDetails;
  }

  /**
   * Set the creation date
   * @param date A string representing a date
   * @since 1.0
   */
  public void setCreationDate(String date) {
    this.creationDate = date;
  }

  /**
   * Set the creator id
   * @param creatorId The creator id
   * @since 1.0
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Set the modelId
   * @param modelId the modelId of the node
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  /**
   * Set the status
   * @param status the status of the node
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Set the details of children
   * @param childrenDetails a NodeDetail Collection
   * @since 1.0
   */
  public void setChildrenDetails(Collection<NodeDetail> childrenDetails) {
    this.childrenDetails = childrenDetails;
  }

  /**
   * Set the father of the node
   * @param fatherPK the nodePK of the father
   * @since 1.0
   */
  public void setFatherPK(NodePK fatherPK) {
    this.fatherPK = fatherPK;
  }

  /**
   * Méthode nécéssaire au marshalling castor
   * @param id
   */
  public void setId(int id) {
    getNodePK().setId(Integer.toString(id));
  }

  /**
   * Set the path
   * @param path the path of the node
   * @since 1.0
   */
  public void setPath(String path) {
    this.path = path;
    this.fullPath = path + nodePK.getId() + "/";
  }

  /**
   * Set the level
   * @param level the level of the node
   * @since 1.0
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * Get the number of children of the node
   * @return the number of children of the node
   * @since 1.0
   */
  public int getChildrenNumber() {
    return childrenDetails.size();
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * Converts the contents of the key into a readable String.
   * @return The string representation of this object
   */
  @Override
  public String toString() {
    return "(pk = " + getNodePK().toString() + ", name = " + getName()
        + ", path = " + getPath() + ", level = " + getLevel() + ", fatherPK = "
        + getFatherPK().toString() + ", type = " + type + ", order = "
        + getOrder() + ")";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof NodeDetail)) {
      return false;
    }
    return (getNodePK().getId().equals(((NodeDetail) other).getNodePK().getId()))
        && (getNodePK().getComponentName().equals(((NodeDetail) other).getNodePK().
            getComponentName()));
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public String getDefaultUrl(String componentName) {
    return URLUtil.getURL(null, getNodePK().getInstanceId()) + getURL();
  }

  public String getURL() {
    return "searchResult?Type=Node&Id=" + getNodePK().getId();
  }

  public String getLink() {
    return URLUtil.getSimpleURL(URLUtil.URL_TOPIC, getNodePK().getId(),
        getNodePK().getInstanceId());
  }

  public String getPermalink() {
    if (URLUtil.displayUniversalLinks()) {
      return URLUtil.getSimpleURL(URLUtil.URL_TOPIC, this.nodePK.getId(), this.nodePK.
          getInstanceId());
    }
    return null;
  }

  public int getNbObjects() {
    return nbObjects;
  }

  public void setNbObjects(int nbObjects) {
    this.nbObjects = nbObjects;
  }

  public int getRightsDependsOn() {
    return rightsDependsOn;
  }

  public void setRightsDependsOn(int rightsDependsOn) {
    this.rightsDependsOn = rightsDependsOn;
  }

  public void setRightsDependsOnMe() {
    this.rightsDependsOn = Integer.parseInt(this.nodePK.getId());
  }

  public boolean haveLocalRights() {
    return Integer.parseInt(getNodePK().getId()) == rightsDependsOn;
  }

  public boolean haveInheritedRights() {
    return haveRights() && !haveLocalRights();
  }

  public boolean haveRights() {
    return rightsDependsOn != -1;
  }

  public String getUserRole() {
    return userRole;
  }

  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  public boolean isUseId() {
    return useId;
  }

  public boolean hasFather() {
    return !this.fatherPK.isUnclassed() && !this.fatherPK.isUndefined();
  }

  public void setUseId(boolean useId) {
    this.useId = useId;
  }

  public String getFullPath() {
    return fullPath;
  }

  public NodeDetail clone() {
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodePK);
    node.setCreatorId(getCreatorId());
    node.setName(getName());
    node.setDescription(getDescription());
    node.setLanguage(getLanguage());
    node.setTranslations(getTranslations());
    node.setRightsDependsOn(getRightsDependsOn());
    node.setCreationDate(getCreationDate());
    node.setStatus(getStatus());
    node.setOrder(getOrder());
    node.setFatherPK(getFatherPK());
    node.setLevel(getLevel());
    node.setModelId(getModelId());
    node.setPath(getPath());
    node.setType(getType());
    return node;
  }

  public boolean isFatherOf(NodeDetail node) {
    boolean isFather = false;
    // Compare componentId
    if (this.getNodePK().getInstanceId().equals(node.getNodePK().getInstanceId())) {
      // Compare if they are same node
      if (this.getNodePK().getId().equals(node.getNodePK().getId())) {
        isFather = false;
      } else {
        String thisNodePath = this.getFullPath();
        String nodePath = node.getFullPath();
        if (nodePath.startsWith(thisNodePath)) {
          isFather = true;
        }
      }
    }
    return isFather;
  }

  /**
   * Is the specified user can access this node?
   * <p/>
   * @param user a user in Silverpeas.
   * @return true if the user can access this node, false otherwise.
   */
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<NodePK> accessController =
        AccessControllerProvider.getAccessController(NodeAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), this.getNodePK());
  }
}
