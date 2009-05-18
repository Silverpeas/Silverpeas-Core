/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.subscribe.control;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/*
 * CVS Informations
 * 
 * $Id: SubscribeBmHome.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: SubscribeBmHome.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.2  2001/12/26 14:27:42  nchaix
 * no message
 *
 */
 
/**
 * Interface declaration
 *
 *
 * @author
 */
public interface SubscribeBmHome extends EJBHome
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
    SubscribeBm create() throws RemoteException, CreateException;

}
