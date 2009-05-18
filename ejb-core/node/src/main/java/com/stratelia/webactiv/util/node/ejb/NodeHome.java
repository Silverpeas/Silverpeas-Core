package com.stratelia.webactiv.util.node.ejb;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.*;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeDetail;

/** 
 * This is the Node Home interface.
 * @author Nicolas Eysseric
 */
public interface NodeHome extends EJBHome {

   /**
	* Create a new Node object
	* @param nd the NodeDetail which contains data
	* @return the new Node
	* @see com.stratelia.webactiv.util.node.model.NodeDetail
	* @see com.stratelia.webactiv.util.actor.model.ActorPK
	* @exception javax.ejb.RemoteException
	* @exception javax.ejb.CreateException
	* @exception java.sql.SQLException
	* @since 1.0
	*/
	public Node create(NodeDetail nd) throws RemoteException, CreateException;
    
	/**
	* Create an instance of a Node object
	* @param nodePK the PK of the Node to instanciate
	* @return the instanciated Node if it exists in database
	* @see com.stratelia.webactiv.util.node.model.NodePK
	* @exception javax.ejb.RemoteException
	* @exception javax.ejb.FinderException
	* @exception java.sql.SQLException
	* @since 1.0
	*/
        public Node findByPrimaryKey(NodePK nodePK) throws RemoteException, FinderException;
        
        /**
	* Create a collection of instance of a Node object
        * The collection can be empty.
	* @param fatherPK the PK of the father from all the Nodes to instanciate
	* @return the instanciated Node's collection if it exists in database
	* @see com.stratelia.webactiv.util.node.model.NodePK
	* @exception javax.ejb.RemoteException
	* @exception javax.ejb.FinderException
	* @exception java.sql.SQLException
	* @since 1.0
	*/
    public Collection findByFatherPrimaryKey(NodePK fatherPK) throws FinderException, RemoteException;
    
    public Node findByNameAndFatherId(NodePK nodePK, String name, int nodeFatherId) throws RemoteException, FinderException;
  }