/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.clipboard.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * The home interface for the state-full EJB session ClipboardBm.
 */
public interface ClipboardBmHome extends EJBHome
{

    /**
     * Create a ClipboardBm.
     */
    ClipboardBm create(String name) throws CreateException, RemoteException;
}
