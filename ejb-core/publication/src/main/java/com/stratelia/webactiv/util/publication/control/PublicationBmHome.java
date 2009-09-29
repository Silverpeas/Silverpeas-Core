/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.publication.control;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/*
 * CVS Informations
 * 
 * $Id: PublicationBmHome.java,v 1.2 2007/12/03 14:53:38 neysseri Exp $
 * 
 * $Log: PublicationBmHome.java,v $
 * Revision 1.2  2007/12/03 14:53:38  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.2  2002/01/11 12:40:30  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface PublicationBmHome extends EJBHome {

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
  PublicationBm create() throws RemoteException, CreateException;
}
