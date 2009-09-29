/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.statistic.control;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/*
 * CVS Informations
 * 
 * $Id: StatisticBmHome.java,v 1.2 2007/06/14 08:37:55 neysseri Exp $
 * 
 * $Log: StatisticBmHome.java,v $
 * Revision 1.2  2007/06/14 08:37:55  neysseri
 * no message
 *
 * Revision 1.1.1.1.20.1  2007/06/14 08:22:38  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.4  2001/12/26 12:01:47  nchaix
 * no message
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface StatisticBmHome extends EJBHome {

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
  StatisticBm create() throws RemoteException, CreateException;
}
