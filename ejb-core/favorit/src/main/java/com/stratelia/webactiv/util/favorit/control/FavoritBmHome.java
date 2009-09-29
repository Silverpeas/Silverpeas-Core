/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.favorit.control;

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
public interface FavoritBmHome extends EJBHome {

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
  FavoritBm create() throws RemoteException, CreateException;
}
