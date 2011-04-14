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
package com.stratelia.webactiv.util.node.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import javax.ejb.ObjectNotFoundException;

/**
 * This is the Node EJB-tier controller. It is implemented as a entity EJB.
 * @author Nicolas Eysseric
 */
public class NodeEJB implements EntityBean {

  private static final long serialVersionUID = -1001449049148326184L;
  private NodePK nodePK;
  private String name;
  private String description;
  private String creationDate;
  private String creatorId;
  private String path;
  private int level;
  private NodePK fatherPK;
  private String modelId;
  private String status;
  private String type;
  private int order;
  private String lang;
  private int rightsDependsOn;
  private boolean stored = false;
  // private Node father = null;
  private EntityContext context;

  /**
   * Get the attributes of THIS node
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public NodeDetail getHeader() {
    NodeDetail nd = new NodeDetail(this.nodePK, this.name, this.description,
        this.creationDate, this.creatorId, this.path, this.level,
        this.fatherPK, this.modelId, this.status, null, this.type);
    nd.setOrder(this.order);
    nd.setLanguage(this.lang);
    nd.setRightsDependsOn(this.rightsDependsOn);
    return nd;
  }

  /**
   * Get the attributes of THIS node and of its children
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public NodeDetail getDetail() throws SQLException {
    return getDetail(null);
  }

  public NodeDetail getDetail(String sorting) throws SQLException {
    NodeDetail nd = getHeader();
    if (!NodeDetail.FILE_LINK_TYPE.equals(nd.getType())) {
      Collection<NodeDetail> childrenDetails = getChildrenDetails(sorting);
      nd.setChildrenDetails(childrenDetails);
    }
    return nd;
  }

  /**
   * Get the header of each child of the node
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Collection<NodeDetail> getChildrenDetails() throws SQLException {
    return getChildrenDetails(null);
  }

  public Collection<NodeDetail> getChildrenDetails(String sorting) throws SQLException {
    Connection con = getConnection();
    try {
      return NodeDAO.getChildrenDetails(con, this.nodePK, sorting);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Update the attributes of the node
   * @param nd the NodeDetail which contains updated data
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public void setDetail(NodeDetail nd) {
    if (nd.getName() != null) {
      this.name = nd.getName();
    }
    if (nd.getDescription() != null) {
      this.description = nd.getDescription();
    }
    if (nd.getCreationDate() != null) {
      this.creationDate = nd.getCreationDate();
    }
    if (nd.getCreatorId() != null) {
      this.creatorId = nd.getCreatorId();
    }
    if (nd.getModelId() != null) {
      this.modelId = nd.getModelId();
    }
    if (nd.getStatus() != null) {
      this.status = nd.getStatus();
    }
    if (nd.getType() != null) {
      this.type = nd.getType();
    }
    if (NodeDetail.FILE_LINK_TYPE.equals(nd.getType())) {
      this.path = nd.getPath();
    }
    if (nd.getFatherPK() != null
        && StringUtil.isInteger(nd.getFatherPK().getId())
        && StringUtil.isDefined(nd.getFatherPK().getInstanceId())) {
      this.fatherPK = nd.getFatherPK();
    }
    if (StringUtil.isDefined(nd.getPath())) {
      this.path = nd.getPath();
    }
    this.order = nd.getOrder();
    this.lang = nd.getLanguage();
    // this.rightsDependsOn = nd.getRightsDependsOn();
    stored = false;
  }

  public void setRightsDependsOn(int nodeId) {
    this.rightsDependsOn = nodeId;
    stored = false;
  }

  /**
   * Create a new Node object.
   *
   * @param nd the NodeDetail which contains data
   * @return the NodePK of the new Node
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.actor.model.ActorPK
   * @throws javax.ejb.CreateException
   * @since 1.0
   */
  public NodePK ejbCreate(NodeDetail nd) throws CreateException {
    NodePK newNodePK = null;
    Connection con = getConnection();
    try {
      // insert row in the database
      newNodePK = NodeDAO.insertRow(con, nd);

      if (nd.getRightsDependsOn() == 0) {
        this.rightsDependsOn = Integer.parseInt(newNodePK.getId());
      } else {
        this.rightsDependsOn = nd.getRightsDependsOn();
      }

      if (nd.haveRights()) {
        NodeDAO.updateRightsDependency(con, newNodePK, rightsDependsOn);
      }

      nd.setNodePK(newNodePK);
      createTranslations(con, nd);

    } catch (Exception e) {
      throw new NodeRuntimeException("NodeEJB.ejbCreate()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_INSERT_ENTITY_ATTRIBUTES", e);
    } finally {
      freeConnection(con);
    }

    // set new attributes
    this.nodePK = newNodePK;
    this.name = nd.getName();
    this.description = nd.getDescription();
    this.creationDate = nd.getCreationDate();
    this.creatorId = nd.getCreatorId();
    this.path = nd.getPath();
    this.level = nd.getLevel();
    this.fatherPK = nd.getFatherPK();
    this.modelId = nd.getModelId();
    this.status = nd.getStatus();
    this.type = nd.getType();
    this.order = nd.getOrder();
    this.lang = nd.getLanguage();

    stored = true;

    return newNodePK;
  }

  private void createTranslations(Connection con, NodeDetail node)
      throws SQLException, UtilException {
    if (node.getTranslations() != null) {
      Iterator<Translation> translations = node.getTranslations().values().iterator();
      NodeI18NDetail translation = null;
      while (translations.hasNext()) {
        translation = (NodeI18NDetail) translations.next();
        if (node.getLanguage() != null
            && !node.getLanguage().equals(translation.getLanguage())) {
          translation.setObjectId(node.getNodePK().getId());
          NodeI18NDAO.saveTranslation(con, translation);
        }
      }
    }
  }

  /**
   * Create an instance of a Node object.
   *
   * @param pk the PK of the Node to instanciate
   * @return the NodePK of the instanciated Node if it exists in database
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.actor.model.ActorPK
   * @throws javax.ejb.FinderException
   * @since 1.0
   */
  public NodePK ejbFindByPrimaryKey(NodePK pk) throws FinderException {
    Connection con = getConnection();
    try {
      NodePK primary = NodeDAO.selectByPrimaryKey(con, pk);
      if (primary != null) {
        return primary;
      } else {
        SilverTrace.debug("node", "NodeEJB.ejbFindByPrimaryKey()",
            "root.EX_CANT_FIND_ENTITY", "NodeId = " + pk.getId());
        throw new ObjectNotFoundException("Cannot find node ID: " + pk);
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeEJB.ejbFindByPrimaryKey()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_FIND_ENTITY", "NodeId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public NodePK ejbFindByNameAndFatherId(NodePK pk, String name,
      int nodeFatherId) throws FinderException {

    Connection con = getConnection();

    try {
      NodePK primary = NodeDAO.selectByNameAndFatherId(con, pk, name,
          nodeFatherId);
      if (primary != null) {
        return primary;
      } else {
        SilverTrace.debug("node", "NodeEJB.ejbFindByNameAndFatherId()",
            "root.EX_CANT_FIND_ENTITY",
            "name = " + name
            + ", component = " + pk.getComponentName() + ", parent ID = " + nodeFatherId);
        throw new ObjectNotFoundException("Cannot find node named " + name + ", component " + pk.getComponentName()
            + ", parent ID " + nodeFatherId);
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeEJB.ejbFindByNameAndFatherId()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_FIND_ENTITY", "name = " + name + ", component = " + pk.getComponentName() + ", parent ID = "
          + nodeFatherId, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<NodePK> ejbFindByFatherPrimaryKey(NodePK fatherPk) {

    Connection con = getConnection();
    Collection result;

    try {
      result = NodeDAO.selectByFatherPrimaryKey(con, fatherPk);
      return result;
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeEJB.ejbFindByFatherPrimaryKey()",
          SilverpeasRuntimeException.ERROR,
          "node.GETTING_NODES_BY_FATHER_FAILED",
          "FatherId = " + fatherPk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Delete this Node and all its descendants.
   * @since 1.0
   */
  @Override
  public void ejbRemove() {
    Connection con = getConnection();
    try {
      NodeDAO.deleteRow(con, this.nodePK);
      this.nodePK = null;
    } catch (Exception ex) {
      throw new NodeRuntimeException("NodeEJB.ejbRemove()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_DELETE_ENTITY",
          "NodeId = " + this.nodePK.getId(), ex);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void setEntityContext(EntityContext context) {
    this.context = context;
  }

  @Override
  public void unsetEntityContext() {
    this.context = null;
  }

  @Override
  public void ejbActivate() {
    this.nodePK = (NodePK) context.getPrimaryKey();
    stored = false;
    // father = null;
  }

  @Override
  public void ejbPassivate() {
    this.nodePK = null;
    stored = false;
    // father = null;
  }

  /**
   * Load node attributes from database.
  @Override
   * @since 1.0
   */
  @Override
  public void ejbLoad() {
        Connection con = getConnection();
    try {
      NodeDetail nodeDetail = NodeDAO.loadRow(con, nodePK);
      this.name = nodeDetail.getName();
      this.description = nodeDetail.getDescription();
      this.creatorId = nodeDetail.getCreatorId();
      this.creationDate = nodeDetail.getCreationDate();
      this.path = nodeDetail.getPath();
      this.level = nodeDetail.getLevel();
      this.fatherPK = nodeDetail.getFatherPK();
      this.modelId = nodeDetail.getModelId();
      this.status = nodeDetail.getStatus();
      this.type = nodeDetail.getType();
      this.order = nodeDetail.getOrder();
      this.lang = nodeDetail.getLanguage();
      this.rightsDependsOn = nodeDetail.getRightsDependsOn();
    } catch (Exception ex) {
      throw new NodeRuntimeException("NodeEJB.ejbLoad()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
    } finally {
      freeConnection(con);
    }
    stored = true;
  }

  /**
   * Store node attributes into database.
   * @since 1.0
   */
  @Override
  public void ejbStore() {
    if (stored) {
      return;
    }
    Connection con = getConnection();
    try {
      NodeDetail detail = new NodeDetail(this.nodePK, this.name,
          this.description, this.creationDate, this.creatorId, this.path,
          this.level, this.fatherPK, this.modelId, this.status, null, this.type);
      detail.setOrder(this.order);
      detail.setLanguage(this.lang);
      detail.setRightsDependsOn(this.rightsDependsOn);
      NodeDAO.storeRow(con, detail);
    } catch (Exception ex) {
      throw new NodeRuntimeException("NodeEJB.ejbStore()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
    } finally {
      freeConnection(con);
    }
    stored = true;
  }

  public void ejbPostCreate(NodeDetail nd) throws CreateException {
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("node", "NodeEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }
}
