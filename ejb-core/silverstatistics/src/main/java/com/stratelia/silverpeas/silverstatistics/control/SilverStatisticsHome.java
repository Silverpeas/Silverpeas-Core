/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.silverstatistics.control;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * Interface declaration
 *
 *
 * @author SLR
 */
public interface SilverStatisticsHome extends EJBHome
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
    SilverStatistics create() throws RemoteException, CreateException;
}
