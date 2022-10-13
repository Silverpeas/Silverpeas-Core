/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.model;

import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.Folder;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.model.WithPermanentLink;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.util.URLUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * This object contains the description of a node (own attributes and children attributes)
 * @author Nicolas Eysseric
 * @version 1.0
 */
@XmlRootElement(name = "xmlField")
@XmlAccessorType(XmlAccessType.NONE)
public class NodeDetail extends AbstractI18NBean<NodeI18NDetail> implements Identifiable,
    I18nContribution, Folder, Serializable, Securable, WithPermanentLink {

  private static final long serialVersionUID = -1401884517616404337L;
  private static final String UNKNOWN = "unknown";
  public static final String DEFAULT_NODE_TYPE = "default";
  public static final String TYPE = "Node";
  public static final String FILE_LINK_TYPE = "file_link";
  public static final String STATUS_VISIBLE = "Visible";
  public static final String STATUS_INVISIBLE = "Invisible";
  public static final String NO_RIGHTS_DEPENDENCY = "-1";
  private NodePK nodePK;
  private Date creationDate;
  private String creatorId = "";
  private String path = "";
  private String fullPath;
  private int level;
  private String modelId = null;
  private String status = null;
  private NodePK fatherPK;
  @XmlElement(name = "topic")
  private Collection<NodeDetail> childrenDetails;
  private String nodeType = DEFAULT_NODE_TYPE;
  private int order = 0;
  private String rightsDependsOn = NO_RIGHTS_DEPENDENCY;
  // No persistence - useful to store nb objects contained by this node
  private int nbObjects = -1;
  // No persistence - useful to store user role
  private String userRole = null;
  private boolean useId = false;

  /**
   * Copy constructor of persisted entity, all data are deeply copied and id is set to "unknown"
   * value.
   * @param other the instance to copy.
   */
  public NodeDetail(final NodeDetail other) {
    super(other);
    this.nodePK = new NodePK(UNKNOWN, other.nodePK);
    this.setPath(other.path);
    this.creatorId = other.creatorId;
    this.creationDate = other.creationDate;
    this.level = other.level;
    this.modelId = other.modelId;
    this.status = other.status;
    this.fatherPK = new NodePK(other.fatherPK.getId(), other.fatherPK.getInstanceId());
    if (other.childrenDetails != null) {
      this.childrenDetails = new ArrayList<>(other.childrenDetails);
    }
    this.nodeType = other.nodeType;
    this.order = other.order;
    this.rightsDependsOn = other.rightsDependsOn;
    this.nbObjects = other.nbObjects;
    this.userRole = other.userRole;
    this.useId = other.useId;
  }

  /**
   * Construct an empty NodeDetail
   * @since 1.0
   */
  public NodeDetail() {
    this(NodePK.ROOT_NODE_ID, "", "", 0, NodePK.ROOT_NODE_ID);
  }

  /**
   * Create a new NodeDetail
   * @param id id of the node
   * @param name The node name
   * @param description The node description
   * @param level The node level (root level = 1)
   * @param fatherId The id of the father
   */
  public NodeDetail(String id, String name, String description, int level, String fatherId) {
    this.nodePK = new NodePK(id);
    setName(name);
    setDescription(description);
    this.fullPath = path + id + '/';
    this.level = level;
    this.fatherPK = new NodePK(fatherId);
    this.childrenDetails = null;
  }

  /**
   * Create a new NodeDetail
   * @param pk id of the node
   * @param name The node name
   * @param description The node description
   * @param level The node level (root level = 1)
   * @param fatherId The id of the father
   */
  public NodeDetail(NodePK pk, String name, String description, int level, String fatherId) {
    this.nodePK = pk;
    setName(name);
    setDescription(description);
    this.fullPath = path + pk.getId() + '/';
    this.level = level;
    this.fatherPK = new NodePK(fatherId, pk.getInstanceId());
    this.childrenDetails = null;
  }

  @Override
  protected Class<NodeI18NDetail> getTranslationType() {
    return NodeI18NDetail.class;
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

  public String getId() {
    return getNodePK().getId();
  }

  @XmlAttribute(name = "id")
  public int getLocalId() {
    return Integer.parseInt(getId());
  }

  @Override
  public String getContributionType() {
    return getNodeType().equals(DEFAULT_NODE_TYPE) ? TYPE : getNodeType();
  }

  /**
   * Is this node the root of a tree of nodes?
   * @return true if the node is the root of a tree of nodes. False otherwise.
   */
  public boolean isRoot() {
    return getNodePK().isRoot();
  }

  /**
   * Is this node a bin of contributions?
   * @return true if this node represents a bin. False otherwise.
   */
  public boolean isBin() {
    return getNodePK().isTrash();
  }

  /**
   * Is this node the one containing unclassified contributions?
   * @return true if the node isn't yet classified among other nodes. False otherwise.
   */
  public boolean isUnclassified() {
    return getNodePK().isUnclassed();
  }

  /**
   * Is this node a child of another node? A node is a child of another node if the following
   * conditions are satisfied:
   * <ul>
   *   <li>the node isn't the root one,</li>
   *   <li>the node isn't a bin,</li>
   *   <li>the node isn't unclassified (orphaned).</li>
   * </ul>
   * @return true if this node is a child of another node. False otherwise.
   */
  public boolean isChild() {
    return getLocalId() > 2;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return new NodeIdentifier(getNodePK().getInstanceId(), getNodePK().getId(),
        getContributionType());
  }

  @Override
  public User getCreator() {
    return User.getById(creatorId);
  }

  /**
   * Get the creation date
   * @return the creation date
   * @since 1.0
   */
  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * No modification is traced for nodes. Returns the creator.
   * @return the creator of the node
   */
  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  /**
   * No modification is traced for nodes. Returns the date of this node creation.
   * @return the creation date.
   */
  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
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
  public void setCreationDate(Date date) {
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
   * Méthode nécéssaire au marshalling JAXB
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
    this.fullPath = path + nodePK.getId() + '/';
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
        + getFatherPK().toString() + ", type = " + nodeType + ", order = "
        + getOrder() + ")";
  }

  public String getNodeType() {
    return nodeType;
  }

  public void setNodeType(String nodeType) {
    this.nodeType = nodeType;
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

  public String getURL() {
    return "searchResult?Type=Node&Id=" + getNodePK().getId();
  }

  public String getLink() {
    return URLUtil.getSimpleURL(URLUtil.URL_TOPIC, getNodePK().getId(),
        getNodePK().getInstanceId());
  }

  @Override
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

  public String getRightsDependsOn() {
    return rightsDependsOn;
  }

  public void setRightsDependsOn(String rightsDependsOn) {
    this.rightsDependsOn = rightsDependsOn;
  }

  public void setRightsDependsOnMe() {
    this.rightsDependsOn = this.nodePK.getId();
  }

  public boolean haveLocalRights() {
    return getNodePK().getId().equals(rightsDependsOn);
  }

  public boolean haveInheritedRights() {
    return haveRights() && !haveLocalRights();
  }

  public boolean haveRights() {
    return !rightsDependsOn.equals(NO_RIGHTS_DEPENDENCY);
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

  public boolean isFatherOf(NodeDetail node) {
    boolean isFather = false;
    // Compare componentId
    if (this.getNodePK().getInstanceId().equals(node.getNodePK().getInstanceId()) &&
        !this.getNodePK().getId().equals(node.getNodePK().getId())) {
      String thisNodePath = this.getFullPath();
      String nodePath = node.getFullPath();
      if (nodePath.startsWith(thisNodePath)) {
        isFather = true;
      }
    }
    return isFather;
  }

  /**
   * Is the specified user can access this node?
   * <p>
   * @param user a user in Silverpeas.
   * @return true if the user can access this node, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final User user) {
    return NodeAccessControl.get().isUserAuthorized(user.getId(), this);
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return NodeAccessControl.get()
        .isUserAuthorized(user.getId(), this, AccessControlContext.init()
            .onOperationsOf(AccessControlOperation.MODIFICATION));
  }

  /**
   * Is the specified user can file into this node a contribution (either another node or a
   * different kind of contributions supported by the underlying application)?
   * @param user a user in Silverpeas.
   * @return true if the user has at least the role of writer for this node.
   */
  @Override
  public boolean canBeFiledInBy(final User user) {
    Set<SilverpeasRole> role = NodeAccessControl.get()
        .getUserRoles(user.getId(), getNodePK(), AccessControlContext.init()
            .onOperationsOf(AccessControlOperation.MODIFICATION));
    return role.stream()
        .anyMatch(r -> r.isGreaterThanOrEquals(SilverpeasRole.WRITER));
  }

  public boolean canBeSharedBy(final User user) {
    final AccessControlContext context = AccessControlContext.init()
        .onOperationsOf(AccessControlOperation.SHARING);
    return NodeAccessControl.get()
        .isUserAuthorized(user.getId(), this, context);
  }
}
