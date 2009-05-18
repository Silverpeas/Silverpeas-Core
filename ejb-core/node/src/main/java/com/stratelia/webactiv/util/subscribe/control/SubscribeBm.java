/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.subscribe.control;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.*;
import com.stratelia.webactiv.util.node.model.NodePK;

/*
 * CVS Informations
 * 
 * $Id: SubscribeBm.java,v 1.2 2004/10/05 13:21:18 dlesimple Exp $
 * 
 * $Log: SubscribeBm.java,v $
 * Revision 1.2  2004/10/05 13:21:18  dlesimple
 * Couper/Coller composant
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.4  2002/01/21 15:16:12  neysseri
 * Ajout d'une fonctionnalité permettant d'avoir les abonnés à un ensemble de noeuds
 *
 * Revision 1.3  2001/12/26 14:27:42  nchaix
 * no message
 *
 */
 
/**
 * Interface declaration
 *
 *
 * @author
 */
public interface SubscribeBm extends EJBObject
{

    /**
     * addPublicationInNode()
     * This method has to be called each time a publication is added in a node.
     * This will enable to send email if some users are subscribers of this node.
     */
    // public void addPublicationInNode(PublicationPK pub, NodePK node) throws RemoteException;

    public void addSubscribe(String userId, NodePK node) throws RemoteException;

    /**
     * Method declaration
     *
     *
     * @param userId
     * @param node
     *
     * @throws RemoteException
     *
     * @see
     */
    public void removeSubscribe(String userId, NodePK node) throws RemoteException;

    /**
     * Method declaration
     *
     *
     * @param userId
     *
     * @throws RemoteException
     *
     * @see
     */
    public void removeUserSubscribes(String userId) throws RemoteException;

    /**
     * Method declaration
     *
     *
     * @param node
     * @param path
     *
     * @throws RemoteException
     *
     * @see
     */
    public void removeNodeSubscribes(NodePK node, String path) throws RemoteException;

    /**
     * Method declaration
     *
     *
     * @param userId
     *
     * @return
     *
     * @throws RemoteException
     *
     * @see
     */
    public Collection getUserSubscribePKs(String userId) throws RemoteException;

// NEWD DLE
    /**
     * Method declaration
     *
     *
     * @param userId
     * @param space
     * @param componentName
     *
     * @return
     *
     * @throws RemoteException
     *
     * @see
     */
//    public Collection getUserSubscribePKsBySpaceAndComponent(String userId, String space, String componentName) throws RemoteException;

	/**
	 * Method declaration
	 *
	 *
	 * @param userId
	 * @param componentName
	 *
	 * @return
	 *
	 * @throws RemoteException
	 *
	 * @see
	 */
	public Collection getUserSubscribePKsByComponent(String userId, String componentName) throws RemoteException;
// NEWF DLE

    /**
     * Method declaration
     *
     *
     * @param node
     *
     * @return a Collection of userId
     *
     * @throws RemoteException
     *
     * @see
     */
    public Collection getNodeSubscriberDetails(NodePK node) throws RemoteException;

	/**
     * Method declaration
     *
     *
     * @param nodePKs
     *
     * @return a Collection of userId
     *
     * @throws RemoteException
     *
     * @see
     */
	public Collection getNodeSubscriberDetails(Collection nodePKs) throws RemoteException;
}
