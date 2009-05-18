/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.coordinates.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * Interface declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface CoordinatesBmHome extends EJBHome
{

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @throws CreateException
	 * @throws RemoteException
	 *
	 * @see
	 */
	CoordinatesBm create() throws RemoteException, CreateException;
}
