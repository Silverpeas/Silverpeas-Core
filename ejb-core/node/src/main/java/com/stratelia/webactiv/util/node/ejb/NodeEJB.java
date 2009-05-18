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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;

/**
 * This is the Node EJB-tier controller.
 * It is implemented as a entity EJB.
 * @author Nicolas Eysseric
 */
public class NodeEJB implements EntityBean {

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
   //private Node father = null;
   private EntityContext context;

   /**
	* Get the attributes of THIS node
	* @return a NodeDetail
	* @see com.stratelia.webactiv.util.node.model.NodeDetail
	* @since 1.0
	*/
   public NodeDetail getHeader() {
      NodeDetail nd = new NodeDetail(this.nodePK, this.name, this.description, this.creationDate, this.creatorId, this.path, this.level, this.fatherPK, this.modelId, this.status, null, this.type);
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
   public NodeDetail getDetail() throws SQLException
   {
       return getDetail(null);
   }

   public NodeDetail getDetail(String sorting) throws SQLException {
          NodeDetail nd = getHeader();
          if (!NodeDetail.FILE_LINK_TYPE.equals(nd.getType())) {
            Collection childrenDetails = getChildrenDetails(sorting);
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
   public Collection getChildrenDetails() throws SQLException
   {
        return  getChildrenDetails(null);
   }

   public Collection getChildrenDetails(String sorting) throws SQLException {
          Collection result = null;
		  Connection con = getConnection();
          try {
              result = NodeDAO.getChildrenDetails(con, this.nodePK, sorting);
          } finally {
              freeConnection(con);
          }
          return result;
   }



  /**
	* Update the attributes of the node
	* @param nd the NodeDetail which contains updated data
	* @see com.stratelia.webactiv.util.node.model.NodeDetail
	* @since 1.0
	*/
   public void setDetail(NodeDetail nd) {
          if (nd.getName() != null)
                this.name = nd.getName();
          if (nd.getDescription() != null)
                this.description = nd.getDescription();
          if (nd.getCreationDate() != null)
                this.creationDate = nd.getCreationDate();
          if (nd.getCreatorId() != null)
                this.creatorId = nd.getCreatorId();
          if (nd.getModelId() != null)
                this.modelId = nd.getModelId();
          if (nd.getStatus() != null)
                this.status = nd.getStatus();
          if (nd.getType() != null)
                this.type = nd.getType();
          if (NodeDetail.FILE_LINK_TYPE.equals(nd.getType())) {
                this.path = nd.getPath();
          }
          if (nd.getFatherPK() != null && StringUtil.isInteger(nd.getFatherPK().getId()) && StringUtil.isDefined(nd.getFatherPK().getInstanceId()))
        	  this.fatherPK = nd.getFatherPK();
          if (StringUtil.isDefined(nd.getPath()))
        	  this.path = nd.getPath();
		  this.order = nd.getOrder();
		  this.lang = nd.getLanguage();
		  //this.rightsDependsOn = nd.getRightsDependsOn();
          stored = false;
   }
   
   public void setRightsDependsOn(int nodeId)
   {
	   this.rightsDependsOn = nodeId;
	   stored = false;
   }

  /**
	* Create a new Node object
	* @param nd the NodeDetail which contains data
	* @param creatorPK the PK of the user who have create this node
	* @return the NodePK of the new Node
	* @see com.stratelia.webactiv.util.node.model.NodeDetail
	* @see com.stratelia.webactiv.util.actor.model.ActorPK
	* @exception javax.ejb.CreateException
	* @exception java.sql.SQLException
	* @since 1.0
	*/
    public NodePK ejbCreate(NodeDetail nd) {
       NodePK newNodePK = null;
       Connection con = getConnection();
        try {
        	//insert row in the database
        	newNodePK = NodeDAO.insertRow(con, nd);
          
          	if (nd.getRightsDependsOn() == 0)
          		this.rightsDependsOn = Integer.parseInt(newNodePK.getId());
  			else
  				this.rightsDependsOn = nd.getRightsDependsOn();
          	
          	if (nd.haveRights())
          		NodeDAO.updateRightsDependency(con, newNodePK, rightsDependsOn);
          	
          	nd.setNodePK(newNodePK);
          	createTranslations(con, nd);
          
        } catch (Exception e) {
          throw new NodeRuntimeException("NodeEJB.ejbCreate()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_INSERT_ENTITY_ATTRIBUTES", e);
        } finally {
          freeConnection(con);
        }

        //set new attributes
        this.nodePK = newNodePK;
        this.name = nd.getName();
        this.description = nd.getDescription();
        this.creationDate = nd.getCreationDate();
        this.creatorId = nd.getCreatorId();
        /*if (!NodeDetail.FILE_LINK_TYPE.equals(nd.getType())) {
          this.path = nd.getPath() + newNodePK.getId();
        } else {
          this.path = nd.getPath();
        }*/
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
    
    private void createTranslations(Connection con, NodeDetail node) throws SQLException, UtilException
    {
    	if (node.getTranslations() != null)
    	{
    		Iterator translations = node.getTranslations().values().iterator();
    		NodeI18NDetail translation = null;
        	while (translations.hasNext())
        	{
        		translation = (NodeI18NDetail) translations.next();
        		if (node.getLanguage() != null && !node.getLanguage().equals(translation.getLanguage()))
        		{
        			translation.setObjectId(node.getNodePK().getId());
        			NodeI18NDAO.saveTranslation(con, translation);
        		}
        	}
    	}
    }

    /**
	* Create an instance of a Node object
	* @param pk the PK of the Node to instanciate
	* @return the NodePK of the instanciated Node if it exists in database
	* @see com.stratelia.webactiv.util.node.model.NodeDetail
	* @see com.stratelia.webactiv.util.actor.model.ActorPK
	* @exception javax.ejb.FinderException
	* @since 1.0
	*/
   public NodePK ejbFindByPrimaryKey(NodePK pk) throws FinderException {
      Connection con = getConnection();
      try {
               NodePK primary = NodeDAO.selectByPrimaryKey(con, pk);
               return primary;
      } catch (Exception se) {
               throw new NodeRuntimeException("NodeEJB.ejbFindByPrimaryKey()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY", "NodeId = "+pk.getId(), se);
      } finally {
          freeConnection(con);
      }
   }
   
   public NodePK ejbFindByNameAndFatherId(NodePK pk, String name, int nodeFatherId) throws FinderException {
    Connection con = getConnection();
    try {
             NodePK primary = NodeDAO.selectByNameAndFatherId(con, pk, name, nodeFatherId);
             return primary;
    } catch (Exception se) {
             throw new NodeRuntimeException("NodeEJB.ejbFindByPrimaryKey()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY", "NodeId = "+pk.getId(), se);
    } finally {
        freeConnection(con);
    }
 }

   public Collection ejbFindByFatherPrimaryKey(NodePK fatherPk) {
      Connection con = getConnection();
      Collection result;
      try {
          result = NodeDAO.selectByFatherPrimaryKey(con, fatherPk);
          return result;
      } catch (Exception ex) {
		  throw new NodeRuntimeException("NodeEJB.ejbFindByFatherPrimaryKey()", SilverpeasRuntimeException.ERROR, "node.GETTING_NODES_BY_FATHER_FAILED", "FatherId = "+fatherPk.getId(), ex);
      } finally {
          freeConnection(con);
      }

   }


    /**
	* Delete this Node and all its descendants
	* @since 1.0
	*/
   public void ejbRemove() {
      Connection con = getConnection();
      try {
          NodeDAO.deleteRow(con, this.nodePK);
          this.nodePK = null;
       } catch (Exception ex) {
		  throw new NodeRuntimeException("NodeEJB.ejbRemove()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_DELETE_ENTITY", "NodeId = "+this.nodePK.getId(), ex);
       } finally {
          freeConnection(con);
       }
   }


   public void setEntityContext(EntityContext context) {
      this.context = context;
   }

   public void unsetEntityContext() {
      this.context = null;
   }

   public void ejbActivate() {
      this.nodePK = (NodePK)context.getPrimaryKey();
      stored = false;
      //father = null;
   }

   public void ejbPassivate() {
      this.nodePK = null;
      stored = false;
      //father = null;
   }

 /**
	* Load node attributes from database
	* @since 1.0
	*/
   public void ejbLoad() {
      // try fat pk
      /*if (nodePK.nodeDetail != null) {
            NodeDetail nodeDetail = nodePK.nodeDetail;
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
            nodePK.nodeDetail = null;
            stored = true;
            return;
      }*/

      Connection con = getConnection();
      try {
            NodeDetail nodeDetail = NodeDAO.loadRow(con, nodePK);
            //this.nodePK = nodePK;
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
             throw new NodeRuntimeException("NodeEJB.ejbLoad()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = "+nodePK.getId());
       } finally {
             freeConnection(con);
       }
      stored = true;
   }

    /**
	* Store node attributes into database
	* @since 1.0
	*/
   public void ejbStore() {
      if (stored) {
        return;
      }
	  Connection con = getConnection();
      try {
			// Transform the 'special' caracters
			//name = Encode.transformStringForBD(name);
			//description = Encode.transformStringForBD(description);

			NodeDetail detail = new NodeDetail(this.nodePK, this.name, this.description, this.creationDate, this.creatorId, this.path, this.level, this.fatherPK, this.modelId, this.status, null, this.type);
			detail.setOrder(this.order);
			detail.setLanguage(this.lang);
			detail.setRightsDependsOn(this.rightsDependsOn);
			NodeDAO.storeRow(con, detail);
      } catch (Exception ex) {
          throw new NodeRuntimeException("NodeEJB.ejbStore()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = "+nodePK.getId());
      } finally {
           freeConnection(con);
      }
      stored = true;
   }


   public void ejbPostCreate(NodeDetail nd) throws CreateException { }

   private Connection getConnection()
    {
        try
        {
            return DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
        }
        catch (Exception e)
        {
            throw new NodeRuntimeException("NodeEJB.getConnection()", SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
        }
    }

    private void freeConnection(Connection con)
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                SilverTrace.error("node", "NodeEJB.freeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", "", e);
            }
        }
    }

}