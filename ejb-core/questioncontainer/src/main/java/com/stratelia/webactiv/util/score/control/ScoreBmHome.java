/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

//
// -- Java Code Generation Process --

package com.stratelia.webactiv.util.score.control;

// Import Statements
import java.rmi.RemoteException;

import javax.ejb.CreateException;

/*
 * CVS Informations
 *
 * $Id: ScoreBmHome.java,v 1.2 2008/05/28 08:39:50 ehugonnet Exp $
 *
 * $Log: ScoreBmHome.java,v $
 * Revision 1.2  2008/05/28 08:39:50  ehugonnet
 * Imports inutiles
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.2  2001/12/21 13:51:08  scotte
 * no message
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface ScoreBmHome extends javax.ejb.EJBHome {

  /*
   * Method: create
   */

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
  public ScoreBm create() throws RemoteException, CreateException;
}
