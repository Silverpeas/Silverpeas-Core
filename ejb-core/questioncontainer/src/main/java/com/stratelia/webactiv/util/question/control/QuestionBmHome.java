/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.question.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/*
 * CVS Informations
 * 
 * $Id: QuestionBmHome.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: QuestionBmHome.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.2  2001/12/20 15:46:04  neysseri
 * Stabilisation Lot 2 :
 * Silvertrace et exceptions + javadoc
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface QuestionBmHome extends EJBHome {

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
  QuestionBm create() throws RemoteException, CreateException;
}
