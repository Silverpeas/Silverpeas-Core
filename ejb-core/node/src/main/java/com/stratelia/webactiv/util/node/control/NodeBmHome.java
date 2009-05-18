/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.node.control;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * This is the Node BM Home interface.
 * @author Nicolas Eysseric
 */
public interface NodeBmHome extends EJBHome
{

    /**
     * Create an instance of a Node BM object
     * @return An instanciated NodeBm
     * @see com.stratelia.webactiv.util.node.control.NodeBm
     * @exception javax.ejb.RemoteException
     * @exception javax.ejb.CreateException
     * @since 1.0
     */
    NodeBm create() throws RemoteException, CreateException;
}
